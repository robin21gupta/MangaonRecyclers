package com.mangaon.recycleers.controller;

import com.mangaon.recycleers.dto.InvoicePhotoDto;
import com.mangaon.recycleers.dto.MaterialOutwardRequest;
import com.mangaon.recycleers.model.MaterialOutward;
import com.mangaon.recycleers.service.InvoicePhotoService;
import com.mangaon.recycleers.service.MaterialOutwardService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/material-outwards")
// ✅ FIX: Removed @CrossOrigin(origins = "*")
//    CORS is now handled centrally in SecurityConfig only.
public class MaterialOutwardController {

    private final MaterialOutwardService materialOutwardService;
    private final InvoicePhotoService invoicePhotoService;

    public MaterialOutwardController(MaterialOutwardService materialOutwardService,
                                     InvoicePhotoService invoicePhotoService) {
        this.materialOutwardService = materialOutwardService;
        this.invoicePhotoService = invoicePhotoService;
    }

    @GetMapping
    public ResponseEntity<List<MaterialOutward>> getAll() {
        return ResponseEntity.ok(materialOutwardService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        return materialOutwardService.getById(id)
                .<ResponseEntity<?>>map(mo -> ResponseEntity.ok(mo))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Material Outward not found with id: " + id)));
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody MaterialOutwardRequest req) {
        try {
            MaterialOutward created = materialOutwardService.create(req);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id,
                                    @RequestBody MaterialOutwardRequest req) {
        try {
            MaterialOutward updated = materialOutwardService.update(id, req);
            return ResponseEntity.ok(updated);
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        try {
            invoicePhotoService.deleteAllForEntry("OUTWARD", id);
            materialOutwardService.delete(id);
            return ResponseEntity.noContent().build();
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping(value = "/{id}/invoice", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadInvoice(
            @PathVariable Long id,
            @RequestPart(value = "file", required = false) MultipartFile file,
            @RequestParam(value = "invoiceNumber", required = false) String invoiceNumber
    ) {
        if (materialOutwardService.getById(id).isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Material Outward not found with id: " + id));
        }

        if ((file == null || file.isEmpty()) && (invoiceNumber == null || invoiceNumber.isBlank())) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Provide at least a file or an invoice number"));
        }

        try {
            InvoicePhotoDto dto = (file != null && !file.isEmpty())
                    ? invoicePhotoService.saveFile(file, "OUTWARD", id, invoiceNumber)
                    : invoicePhotoService.saveInvoiceNumber("OUTWARD", id, invoiceNumber);

            return ResponseEntity.status(HttpStatus.CREATED).body(dto);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Upload failed: " + e.getMessage()));
        }
    }

    @GetMapping("/{id}/invoices")
    public ResponseEntity<?> listInvoices(@PathVariable Long id) {
        return materialOutwardService.getById(id)
                .<ResponseEntity<?>>map(mo -> ResponseEntity.ok(invoicePhotoService.getForEntry("OUTWARD", id)))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Material Outward not found with id: " + id)));
    }
}