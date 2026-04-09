package com.mangaon.recycleers.controller;

import com.mangaon.recycleers.model.PurchaseSaleTrail;
import com.mangaon.recycleers.service.PurchaseSaleTrailService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/trail")
// ✅ FIX: Removed @CrossOrigin(origins = "*")
//    CORS is now handled centrally in SecurityConfig only.
public class PurchaseSaleTrailController {

    private final PurchaseSaleTrailService trailService;

    public PurchaseSaleTrailController(PurchaseSaleTrailService trailService) {
        this.trailService = trailService;
    }

    @GetMapping
    public ResponseEntity<List<PurchaseSaleTrail>> getTrail(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end
    ) {
        return ResponseEntity.ok(trailService.getTrail(start, end));
    }

    @GetMapping("/summary")
    public ResponseEntity<List<Map<String, Object>>> getSummary(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end
    ) {
        return ResponseEntity.ok(trailService.getDailySummary(start, end));
    }

    @GetMapping("/totals")
    public ResponseEntity<Map<String, Object>> getTotals(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end
    ) {
        return ResponseEntity.ok(trailService.getGrandTotals(start, end));
    }
}