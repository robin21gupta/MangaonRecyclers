package com.mangaon.recycleers.service;

import com.mangaon.recycleers.dto.InvoicePhotoDto;
import com.mangaon.recycleers.model.InvoicePhoto;
import com.mangaon.recycleers.repository.InvoicePhotoRepository;
import com.mangaon.recycleers.repository.PurchaseSaleTrailRepository;  // ← ADD
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class InvoicePhotoService {

    private static final long   MAX_SIZE_BYTES   = 10L * 1024 * 1024;
    private static final String SENTINEL_NO_FILE = "NO_FILE";

    private final InvoicePhotoRepository      repo;
    private final PurchaseSaleTrailRepository trailRepo;  // ← ADD

    @Value("${app.invoice.upload-dir:./uploads/invoices}")
    private String uploadDir;

    // ← UPDATE constructor
    public InvoicePhotoService(InvoicePhotoRepository repo,
                               PurchaseSaleTrailRepository trailRepo) {
        this.repo      = repo;
        this.trailRepo = trailRepo;
    }

    @Transactional
    public InvoicePhotoDto saveInvoiceNumber(String entryType, Long entryId,
                                             String invoiceNumber) {
        InvoicePhoto photo = new InvoicePhoto();
        photo.setEntryType(entryType.toUpperCase());
        photo.setEntryId(entryId);
        photo.setInvoiceNumber(invoiceNumber != null ? invoiceNumber.trim() : null);
        photo.setStoragePath(SENTINEL_NO_FILE);
        InvoicePhotoDto dto = toDto(repo.save(photo));

        // ← ADD: sync invoice number to trail record
        syncInvoiceNumberToTrail(entryType, entryId, invoiceNumber);

        return dto;
    }

    @Transactional
    public InvoicePhotoDto saveFile(MultipartFile file, String entryType,
                                    Long entryId, String invoiceNumber) throws IOException {
        validateFile(file);

        String ext        = getExtension(file.getOriginalFilename());
        String yearMonth  = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM"));
        String typeFolder = entryType.toUpperCase();
        String filename   = UUID.randomUUID().toString().replace("-", "")
                + (ext.isEmpty() ? "" : "." + ext);
        String relPath    = typeFolder + "/" + yearMonth + "/" + filename;

        Path dest = Paths.get(uploadDir, typeFolder, yearMonth, filename);
        Files.createDirectories(dest.getParent());
        Files.copy(file.getInputStream(), dest, StandardCopyOption.REPLACE_EXISTING);

        InvoicePhoto photo = new InvoicePhoto();
        photo.setEntryType(typeFolder);
        photo.setEntryId(entryId);
        photo.setInvoiceNumber(invoiceNumber != null && !invoiceNumber.isBlank()
                ? invoiceNumber.trim() : null);
        photo.setOriginalFilename(file.getOriginalFilename());
        photo.setContentType(file.getContentType());
        photo.setFileSize(file.getSize());
        photo.setStoragePath(relPath);
        InvoicePhotoDto dto = toDto(repo.save(photo));

        // ← ADD: sync invoice number to trail record (only if invoiceNumber provided)
        if (invoiceNumber != null && !invoiceNumber.isBlank()) {
            syncInvoiceNumberToTrail(entryType, entryId, invoiceNumber);
        }

        return dto;
    }

    // ── NEW PRIVATE METHOD ─────────────────────────────────────────────────
    // Maps entryType ("INWARD" / "OUTWARD") → sourceTable name used in trail,
    // then updates the trail row so invoice number is visible in trail & CSV.
    private void syncInvoiceNumberToTrail(String entryType, Long entryId,
                                          String invoiceNumber) {
        if (invoiceNumber == null || invoiceNumber.isBlank()) return;

        String sourceTable = switch (entryType.toUpperCase()) {
            case "INWARD"  -> "tyre_inward";
            case "OUTWARD" -> "material_outward";
            default        -> null;
        };

        if (sourceTable != null) {
            trailRepo.updateInvoiceNumberBySourceId(
                    entryId, sourceTable, invoiceNumber.trim());
        }
    }

    // ── REST OF THE METHODS UNCHANGED ─────────────────────────────────────

    @Transactional(readOnly = true)
    public List<InvoicePhotoDto> getForEntry(String entryType, Long entryId) {
        return repo.findByEntryTypeAndEntryId(entryType.toUpperCase(), entryId)
                .stream().map(this::toDto).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Optional<InvoicePhotoDto> getLatestForEntry(String entryType, Long entryId) {
        return repo.findTopByEntryTypeAndEntryIdOrderByUploadedAtDesc(
                entryType.toUpperCase(), entryId).map(this::toDto);
    }

    public Path getPhysicalPath(Long id) {
        InvoicePhoto photo = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Invoice photo not found: " + id));
        if (SENTINEL_NO_FILE.equals(photo.getStoragePath()))
            throw new RuntimeException("No file for this record");
        return Paths.get(uploadDir)
                .resolve(photo.getStoragePath().replace("/", java.io.File.separator));
    }

    public String getContentType(Long id) {
        return repo.findById(id)
                .map(InvoicePhoto::getContentType)
                .orElse(null);
    }

    @Transactional
    public void delete(Long id) {
        InvoicePhoto photo = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Invoice photo not found: " + id));
        tryDeleteFile(photo);
        repo.delete(photo);
    }

    @Transactional
    public void deleteAllForEntry(String entryType, Long entryId) {
        List<InvoicePhoto> photos = repo.findByEntryTypeAndEntryId(
                entryType.toUpperCase(), entryId);
        photos.forEach(this::tryDeleteFile);
        repo.deleteByEntryTypeAndEntryId(entryType.toUpperCase(), entryId);
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty())
            throw new IllegalArgumentException("File must not be empty");
        if (file.getSize() > MAX_SIZE_BYTES)
            throw new IllegalArgumentException("File exceeds 10 MB limit");
        String ct = file.getContentType() != null ? file.getContentType().toLowerCase() : "";
        if (!ct.startsWith("image/") && !ct.equals("application/pdf"))
            throw new IllegalArgumentException("Unsupported file type: " + ct);
    }

    private String getExtension(String fn) {
        if (fn == null || !fn.contains(".")) return "";
        return fn.substring(fn.lastIndexOf('.') + 1).toLowerCase();
    }

    private void tryDeleteFile(InvoicePhoto p) {
        if (p.getStoragePath() == null || SENTINEL_NO_FILE.equals(p.getStoragePath())) return;
        try {
            Files.deleteIfExists(Paths.get(uploadDir)
                    .resolve(p.getStoragePath().replace("/", java.io.File.separator)));
        } catch (IOException ignored) {}
    }

    private InvoicePhotoDto toDto(InvoicePhoto p) {
        InvoicePhotoDto dto = new InvoicePhotoDto();
        dto.setId(p.getId());
        dto.setEntryType(p.getEntryType());
        dto.setEntryId(p.getEntryId());
        dto.setInvoiceNumber(p.getInvoiceNumber());
        dto.setOriginalFilename(p.getOriginalFilename());
        dto.setContentType(p.getContentType());
        dto.setFileSize(p.getFileSize());
        dto.setUploadedAt(p.getUploadedAt());
        if (p.getStoragePath() != null && !SENTINEL_NO_FILE.equals(p.getStoragePath()))
            dto.setViewUrl("/api/invoices/" + p.getId() + "/view");
        return dto;
    }
}