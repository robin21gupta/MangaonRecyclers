package com.mangaon.recycleers.controller;

import com.mangaon.recycleers.dto.InvoicePhotoDto;
import com.mangaon.recycleers.service.InvoicePhotoService;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/invoices")
// FIX: Add CrossOrigin so the browser can load images from this endpoint.
// Without this, the <img> tag gets blocked by CORS even though the page
// is on the same host — because the browser sends an Origin header for
// cross-origin image requests triggered from JavaScript fetch/Image().
//@CrossOrigin(origins = "*")
public class InvoicePhotoController {

    private final InvoicePhotoService service;

    public InvoicePhotoController(InvoicePhotoService service) {
        this.service = service;
    }

    // ── Upload ────────────────────────────────────────────────────────────
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> upload(
            @RequestParam(value = "file",          required = false) MultipartFile file,
            @RequestParam("entryType")                               String        entryType,
            @RequestParam("entryId")                                 Long          entryId,
            @RequestParam(value = "invoiceNumber", required = false) String        invoiceNumber) {

        try {
            boolean hasFile   = file != null && !file.isEmpty();
            boolean hasNumber = invoiceNumber != null && !invoiceNumber.isBlank();

            if (!hasFile && !hasNumber) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Provide at least a file or an invoice number"));
            }

            InvoicePhotoDto dto = hasFile
                    ? service.saveFile(file, entryType, entryId, invoiceNumber)
                    : service.saveInvoiceNumber(entryType, entryId, invoiceNumber);

            return ResponseEntity.ok(dto);

        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
        } catch (IOException ex) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "File storage failed: " + ex.getMessage()));
        }
    }

    // ── FIX: View file inline ─────────────────────────────────────────────
    // GET /api/invoices/{id}/view
    // FIX 1: Added @CrossOrigin on the class (above) — this is the main fix.
    // FIX 2: Content-Disposition is "inline" so the browser displays it
    //        instead of downloading it. Previously this was set correctly
    //        but the CORS block prevented the image from loading at all.
    // FIX 3: Detect content type from the stored contentType field first
    //        (saved during upload), falling back to file extension guessing.
    //        This prevents image/jpeg files from being served as
    //        application/octet-stream which causes "could not load image".
    @GetMapping("/{id}/view")
    public ResponseEntity<Resource> view(@PathVariable Long id) {
        try {
            Path path = service.getPhysicalPath(id);
            if (path == null || !Files.exists(path)) {
                return ResponseEntity.notFound().build();
            }

            Resource resource = new UrlResource(path.toUri());
            if (!resource.exists() || !resource.isReadable()) {
                return ResponseEntity.notFound().build();
            }

            // FIX: Use content type stored during upload, not just guessed from extension
            String contentType = service.getContentType(id);
            if (contentType == null || contentType.isBlank()) {
                contentType = guessContentType(path.getFileName().toString());
            }
            // Final fallback
            if (contentType == null || contentType.isBlank()) {
                contentType = "application/octet-stream";
            }

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline")
                    .header(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "*")
                    .header(HttpHeaders.CACHE_CONTROL, "max-age=3600, public")
                    .contentType(MediaType.parseMediaType(contentType))
                    .body(resource);

        } catch (MalformedURLException ex) {
            return ResponseEntity.internalServerError().build();
        } catch (RuntimeException ex) {
            // getPhysicalPath throws RuntimeException when record not found
            return ResponseEntity.notFound().build();
        }
    }

    // ── List all invoices for entry ───────────────────────────────────────
    @GetMapping
    public ResponseEntity<List<InvoicePhotoDto>> getAll(
            @RequestParam String entryType,
            @RequestParam Long   entryId) {
        return ResponseEntity.ok(service.getForEntry(entryType, entryId));
    }

    // ── Latest invoice for entry ──────────────────────────────────────────
    @GetMapping("/latest")
    public ResponseEntity<InvoicePhotoDto> getLatest(
            @RequestParam String entryType,
            @RequestParam Long   entryId) {
        Optional<InvoicePhotoDto> dto = service.getLatestForEntry(entryType, entryId);
        return dto.map(ResponseEntity::ok).orElse(ResponseEntity.noContent().build());
    }

    // ── Delete single ─────────────────────────────────────────────────────
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    // ── Delete all for entry ──────────────────────────────────────────────
    @DeleteMapping("/entry")
    public ResponseEntity<Void> deleteForEntry(
            @RequestParam String entryType,
            @RequestParam Long   entryId) {
        service.deleteAllForEntry(entryType, entryId);
        return ResponseEntity.noContent().build();
    }

    // ── Helper ────────────────────────────────────────────────────────────
    private String guessContentType(String fn) {
        if (fn == null) return "application/octet-stream";
        String l = fn.toLowerCase();
        if (l.endsWith(".pdf"))               return "application/pdf";
        if (l.endsWith(".jpg") || l.endsWith(".jpeg")) return "image/jpeg";
        if (l.endsWith(".png"))               return "image/png";
        if (l.endsWith(".webp"))              return "image/webp";
        if (l.endsWith(".heic"))              return "image/heic";
        if (l.endsWith(".gif"))               return "image/gif";
        return "application/octet-stream";
    }
}