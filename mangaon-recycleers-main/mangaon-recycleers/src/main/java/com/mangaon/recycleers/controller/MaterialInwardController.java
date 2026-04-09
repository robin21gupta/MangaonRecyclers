package com.mangaon.recycleers.controller;

import com.mangaon.recycleers.dto.InvoicePhotoDto;
import com.mangaon.recycleers.dto.MaterialInwardRequest;
import com.mangaon.recycleers.model.MaterialInward;
import com.mangaon.recycleers.service.InvoicePhotoService;
import com.mangaon.recycleers.service.MaterialInwardService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tyre-inwards")
public class MaterialInwardController {

    private final MaterialInwardService   materialInwardService;
    private final InvoicePhotoService     invoicePhotoService;

    public MaterialInwardController(MaterialInwardService materialInwardService,
                                    InvoicePhotoService invoicePhotoService) {
        this.materialInwardService = materialInwardService;
        this.invoicePhotoService   = invoicePhotoService;
    }

    @GetMapping
    public ResponseEntity<List<MaterialInward>> getAll() {
        return ResponseEntity.ok(materialInwardService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<MaterialInward> getById(@PathVariable Long id) {
        return materialInwardService.getById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<MaterialInward> create(@RequestBody MaterialInwardRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(materialInwardService.create(req));
    }

    @PutMapping("/{id}")
    public ResponseEntity<MaterialInward> update(@PathVariable Long id,
                                                 @RequestBody MaterialInwardRequest req) {
        try {
            return ResponseEntity.ok(materialInwardService.update(id, req));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        try {
            invoicePhotoService.deleteAllForEntry("INWARD", id);
            materialInwardService.delete(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping(value = "/{id}/invoice", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadInvoice(
            @PathVariable Long id,
            @RequestPart(value = "file", required = false) MultipartFile file,
            @RequestParam(value = "invoiceNumber", required = false) String invoiceNumber) {

        if (materialInwardService.getById(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        try {
            boolean hasFile   = file != null && !file.isEmpty();
            boolean hasNumber = invoiceNumber != null && !invoiceNumber.isBlank();

            if (!hasFile && !hasNumber) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Provide at least a file or an invoice number"));
            }

            InvoicePhotoDto dto = hasFile
                    ? invoicePhotoService.saveFile(file, "INWARD", id, invoiceNumber)
                    : invoicePhotoService.saveInvoiceNumber("INWARD", id, invoiceNumber);

            return ResponseEntity.status(HttpStatus.CREATED).body(dto);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (IOException e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Upload failed: " + e.getMessage()));
        }
    }

    @GetMapping("/{id}/invoices")
    public ResponseEntity<List<InvoicePhotoDto>> listInvoices(@PathVariable Long id) {
        return ResponseEntity.ok(invoicePhotoService.getForEntry("INWARD", id));
    }
}