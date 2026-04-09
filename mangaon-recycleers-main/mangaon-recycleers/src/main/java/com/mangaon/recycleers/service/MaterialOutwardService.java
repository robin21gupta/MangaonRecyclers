package com.mangaon.recycleers.service;

import com.mangaon.recycleers.dto.MaterialOutwardRequest;
import com.mangaon.recycleers.enums.OutwardMaterialType;
import com.mangaon.recycleers.model.Client;
import com.mangaon.recycleers.model.MaterialOutward;
import com.mangaon.recycleers.repository.ClientRepository;
import com.mangaon.recycleers.repository.MaterialOutwardRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;

@Service
public class MaterialOutwardService {

    private final MaterialOutwardRepository repository;
    private final ClientRepository clientRepository;
    private final PurchaseSaleTrailService trailService;

    private static final DateTimeFormatter FMT_ISO    = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter FMT_LEGACY = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    public MaterialOutwardService(MaterialOutwardRepository repository,
                                  ClientRepository clientRepository,
                                  PurchaseSaleTrailService trailService) {
        this.repository       = repository;
        this.clientRepository = clientRepository;
        this.trailService     = trailService;
    }

    // ── READ ─────────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<MaterialOutward> getAll() {
        return repository.findAllWithAssociations();
    }

    @Transactional(readOnly = true)
    public Optional<MaterialOutward> getById(Long id) {
        return repository.findByIdWithAssociations(id);
    }

    // ── CREATE ────────────────────────────────────────────────────────────────

    @Transactional
    public MaterialOutward create(MaterialOutwardRequest req) {
        MaterialOutward entity = new MaterialOutward();
        mapRequestToEntity(req, entity);
        MaterialOutward saved = repository.save(entity);
        trailService.recordOutward(saved);   // ✅ trail mein record hoga
        return saved;
    }

    // ── UPDATE ────────────────────────────────────────────────────────────────

    @Transactional
    public MaterialOutward update(Long id, MaterialOutwardRequest req) {
        MaterialOutward entity = repository.findByIdWithAssociations(id)
                .orElseThrow(() -> new RuntimeException("Material Outward not found: " + id));
        mapRequestToEntity(req, entity);
        MaterialOutward saved = repository.save(entity);
        trailService.recordOutward(saved);   // ✅ update hone par trail bhi sync hoga
        return saved;
    }

    // ── DELETE ────────────────────────────────────────────────────────────────

    @Transactional
    public void delete(Long id) {
        trailService.removeOutwardTrail(id);   // ✅ pehle trail se hatao
        repository.deleteById(id);             // ✅ phir main record delete
    }

    // ── HELPERS ───────────────────────────────────────────────────────────────

    private LocalDate parseDate(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new RuntimeException("Date of sale is required");
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

    private void mapRequestToEntity(MaterialOutwardRequest req, MaterialOutward entity) {
        if (req.getMaterialType() != null) {
            entity.setMaterialType(OutwardMaterialType.fromJson(req.getMaterialType()));
        }

        entity.setDateOfSale(parseDate(req.getDateOfSale()));
        entity.setVehicleNumber(req.getVehicleNumber());
        entity.setDriverName(req.getDriverName());
        entity.setDriverNumber(req.getDriverNumber());
        entity.setGrossWeight(req.getGrossWeight());
        entity.setTareWeight(req.getTareWeight());
        entity.setRate(req.getRate());
        entity.setNotes(req.getNotes());
        entity.setInvoiceNumber(req.getInvoiceNumber());

        if (req.getClientId() != null) {
            Client client = clientRepository.findById(req.getClientId())
                    .orElseThrow(() -> new RuntimeException("Client not found: " + req.getClientId()));
            entity.setClient(client);
        } else {
            entity.setClient(null);
        }
    }
}