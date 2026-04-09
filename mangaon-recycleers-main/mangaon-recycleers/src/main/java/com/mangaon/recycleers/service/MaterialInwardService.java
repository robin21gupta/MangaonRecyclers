package com.mangaon.recycleers.service;

import com.mangaon.recycleers.dto.MaterialInwardRequest;
import com.mangaon.recycleers.enums.InwardMaterialType;
import com.mangaon.recycleers.model.MaterialInward;
import com.mangaon.recycleers.model.Supplier;
import com.mangaon.recycleers.repository.MaterialInwardRepository;
import com.mangaon.recycleers.repository.SupplierRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;

@Service
public class MaterialInwardService {

    private final MaterialInwardRepository materialInwardRepository;
    private final SupplierRepository supplierRepository;
    private final PurchaseSaleTrailService trailService;

    private static final DateTimeFormatter FMT_ISO    = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter FMT_LEGACY = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    public MaterialInwardService(MaterialInwardRepository repo,
                                 SupplierRepository suppliers,
                                 PurchaseSaleTrailService trailService) {
        this.materialInwardRepository = repo;
        this.supplierRepository       = suppliers;
        this.trailService             = trailService;
    }

    // ── READ ──────────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<MaterialInward> getAll() {
        return materialInwardRepository.findAllWithAssociations();
    }

    @Transactional(readOnly = true)
    public Optional<MaterialInward> getById(Long id) {
        return materialInwardRepository.findByIdWithAssociations(id);
    }

    // ── CREATE ────────────────────────────────────────────────────────────────

    @Transactional
    public MaterialInward create(MaterialInwardRequest req) {
        MaterialInward entry = new MaterialInward();
        apply(entry, req);
        MaterialInward saved = materialInwardRepository.save(entry);
        trailService.recordInward(saved);   // ✅ trail mein record hoga
        return saved;
    }

    // ── UPDATE ────────────────────────────────────────────────────────────────

    @Transactional
    public MaterialInward update(Long id, MaterialInwardRequest req) {
        MaterialInward entry = materialInwardRepository.findByIdWithAssociations(id)
                .orElseThrow(() -> new RuntimeException("MaterialInward not found: " + id));
        apply(entry, req);
        MaterialInward saved = materialInwardRepository.save(entry);
        trailService.recordInward(saved);   // ✅ update hone par trail bhi sync hoga
        return saved;
    }

    // ── DELETE ────────────────────────────────────────────────────────────────

    @Transactional
    public void delete(Long id) {
        MaterialInward entry = materialInwardRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("MaterialInward not found: " + id));
        trailService.removeInwardTrail(id);         // ✅ pehle trail se hatao
        materialInwardRepository.delete(entry);     // ✅ phir main record delete
    }

    // ── COUNT ─────────────────────────────────────────────────────────────────

    public long getCount() {
        return materialInwardRepository.count();
    }

    // ── HELPERS ───────────────────────────────────────────────────────────────

    private LocalDate parseDate(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new RuntimeException("Inward date is required");
        }
        try {
            return LocalDate.parse(raw.trim(), FMT_ISO);
        } catch (DateTimeParseException e1) {
            try {
                return LocalDate.parse(raw.trim(), FMT_LEGACY);
            } catch (DateTimeParseException e2) {
                throw new RuntimeException(
                        "Invalid date format. Expected yyyy-MM-dd or dd-MM-yyyy, got: " + raw);
            }
        }
    }

    private void apply(MaterialInward e, MaterialInwardRequest r) {

        // Supplier (required)
        Supplier supplier = supplierRepository.findById(r.getSupplierId())
                .orElseThrow(() -> new RuntimeException("Supplier not found"));
        e.setSupplier(supplier);

        // MaterialType (optional)
        if (r.getMaterialType() != null && !r.getMaterialType().isBlank()) {
            try {
                e.setMaterialType(InwardMaterialType.fromJson(r.getMaterialType()));
            } catch (IllegalArgumentException ex) {
                throw new RuntimeException(
                        "Invalid material type: " + r.getMaterialType() +
                                ". Allowed: CRUMB_RUBBER, TYRE, WOOD, OTHERS"
                );
            }
        }

        // Date
        e.setDate(parseDate(r.getDate()));

        // Required weights
        if (r.getGrossWeight() == null) throw new RuntimeException("Gross weight is required");
        if (r.getTareWeight()  == null) throw new RuntimeException("Tare weight is required");

        // Other fields
        e.setVehicleNumber(r.getVehicleNumber());
        e.setDriverName(r.getDriverName());
        e.setDriverNumber(r.getDriverNumber());
        e.setGrossWeight(r.getGrossWeight());
        e.setTareWeight(r.getTareWeight());
        e.setQuantity(r.getQuantity());
        e.setType(r.getType());
        e.setRate(r.getRate());
        e.setNotes(r.getNotes());
        e.setInvoiceNumber(r.getInvoiceNumber());
    }
}