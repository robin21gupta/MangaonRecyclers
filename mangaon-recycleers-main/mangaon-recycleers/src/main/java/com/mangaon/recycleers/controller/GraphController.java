package com.mangaon.recycleers.controller;

import com.mangaon.recycleers.dto.GraphSeriesResponse;
import com.mangaon.recycleers.service.GraphService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/dashboard/graph")
@PreAuthorize("hasAnyAuthority('ACCOUNT','ADMIN','OPERATOR','MASTER','ROLE_ACCOUNT','ROLE_ADMIN','ROLE_OPERATOR','ROLE_MASTER')")
public class GraphController {

    private final GraphService graphService;

    public GraphController(GraphService graphService) {
        this.graphService = graphService;
    }

    @GetMapping("/items")
    public ResponseEntity<List<Map<String, String>>> getItems() {
        return ResponseEntity.ok(List.of(
                Map.of("key", "TYRE_OIL",      "label", "Sale - Pyrolysis Oil",    "source", "OUTWARD"),
                Map.of("key", "CARBON_POWER",  "label", "Sale - Carbon Powder",     "source", "OUTWARD"),
                Map.of("key", "STEEL",         "label", "Sale - Steel Wire",        "source", "OUTWARD"),
                Map.of("key", "CRUMB_RUBBER",  "label", "Purchase - Crumb Rubber",  "source", "INWARD"),
                Map.of("key", "TYRE_INWARD",   "label", "Purchase - Pyrolysis Oil", "source", "INWARD"),
                Map.of("key", "WOOD_INWARD",   "label", "Purchase - Wood",          "source", "INWARD"),
                Map.of("key", "OTHERS_INWARD", "label", "Purchase - Others",        "source", "INWARD"),
                Map.of("key", "ALL_INWARD",    "label", "Purchase - All Types",     "source", "INWARD")
        ));
    }

    @GetMapping("/series")
    public ResponseEntity<GraphSeriesResponse> getSeries(
            @RequestParam String item,
            @RequestParam(defaultValue = "MONTHLY") String period,
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to) {

        if ("DAILY".equalsIgnoreCase(period) && from != null && to != null) {
            return ResponseEntity.ok(graphService.getSeries(item, period, from, to));
        }
        return ResponseEntity.ok(graphService.getSeries(item, period));
    }

    @GetMapping("/all")
    public ResponseEntity<List<GraphSeriesResponse>> getAllSeries(
            @RequestParam(defaultValue = "MONTHLY") String period) {
        return ResponseEntity.ok(graphService.getAllSeries(period));
    }

    @GetMapping("/inward-by-supplier")
    public ResponseEntity<GraphSeriesResponse> getInwardBySupplier(
            @RequestParam(required = false) String type) {
        if (type != null && !type.isBlank()) {
            return ResponseEntity.ok(graphService.getInwardBySupplierAndType(type));
        }
        return ResponseEntity.ok(graphService.getInwardBySupplier());
    }

    @GetMapping("/outward-by-client")
    public ResponseEntity<GraphSeriesResponse> getOutwardByClient(
            @RequestParam String item) {
        return ResponseEntity.ok(graphService.getOutwardByClient(item));
    }

    @GetMapping("/purchase-rate")
    public ResponseEntity<List<GraphSeriesResponse>> getPurchaseRateVariation(
            @RequestParam(defaultValue = "MONTHLY") String period,
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to) {
        return ResponseEntity.ok(graphService.getPurchaseRateVariation(period, from, to));
    }
}