package com.mangaon.recycleers.service;

import com.mangaon.recycleers.model.GraphDataPoint;
import com.mangaon.recycleers.dto.GraphSeriesResponse;
import com.mangaon.recycleers.repository.MaterialOutwardGraphRepository;
import com.mangaon.recycleers.repository.MaterialInwardGraphRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class GraphService {

    private final MaterialOutwardGraphRepository outwardRepo;
    private final MaterialInwardGraphRepository  inwardRepo;

    public GraphService(MaterialOutwardGraphRepository outwardRepo,
                        MaterialInwardGraphRepository  inwardRepo) {
        this.outwardRepo = outwardRepo;
        this.inwardRepo  = inwardRepo;
    }

    // ── Single series ─────────────────────────────────────────────────────────
    public GraphSeriesResponse getSeries(String item, String period) {
        List<GraphDataPoint> points;
        switch (item.toUpperCase()) {
            case "TYRE_OIL":       points = outwardToPoints(fetchOutward("TYRE_OIL",     period)); break;
            case "CARBON_POWER":   points = outwardToPoints(fetchOutward("CARBON_POWER", period)); break;
            case "STEEL":          points = outwardToPoints(fetchOutward("STEEL",        period)); break;
            case "TYRE_INWARD":    points = inwardToPoints(fetchInward("PYROLYSIS_OIL",  period)); break;
            case "CRUMB_RUBBER":   points = inwardToPoints(fetchInward("CRUMB_RUBBER",   period)); break;
            case "WOOD_INWARD":    points = inwardToPoints(fetchInward("WOOD",           period)); break;
            case "OTHERS_INWARD":  points = inwardToPoints(fetchInward("OTHERS",         period)); break;
            case "ALL_INWARD":     points = inwardToPoints(fetchAllInward(period)); break;
            default:               points = List.of();
        }
        return buildResponse(item.toUpperCase(), period.toUpperCase(), points);
    }

    // ── Single series with date range (DAILY) ─────────────────────────────────
    public GraphSeriesResponse getSeries(String item, String period, String from, String to) {
        if (!"DAILY".equalsIgnoreCase(period)) {
            return getSeries(item, period);
        }
        List<GraphDataPoint> points;
        switch (item.toUpperCase()) {
            case "TYRE_OIL":
                points = outwardToPoints(outwardRepo.findDailyByType("TYRE_OIL",     from, to)); break;
            case "CARBON_POWER":
                points = outwardToPoints(outwardRepo.findDailyByType("CARBON_POWER", from, to)); break;
            case "STEEL":
                points = outwardToPoints(outwardRepo.findDailyByType("STEEL",        from, to)); break;
            case "TYRE_INWARD":
                points = inwardToPoints(inwardRepo.findDailyByType("PYROLYSIS_OIL",  from, to)); break;
            case "CRUMB_RUBBER":
                points = inwardToPoints(inwardRepo.findDailyByType("CRUMB_RUBBER",   from, to)); break;
            case "WOOD_INWARD":
                points = inwardToPoints(inwardRepo.findDailyByType("WOOD",           from, to)); break;
            case "OTHERS_INWARD":
                points = inwardToPoints(inwardRepo.findDailyByType("OTHERS",         from, to)); break;
            case "ALL_INWARD":
                points = inwardToPoints(inwardRepo.findDaily(from, to)); break;
            default:
                points = List.of();
        }
        return buildResponse(item.toUpperCase(), "DAILY", points);
    }

    // ── All series (summary cards) ────────────────────────────────────────────
    public List<GraphSeriesResponse> getAllSeries(String period) {
        return List.of(
                getSeries("TYRE_OIL",      period),
                getSeries("CARBON_POWER",  period),
                getSeries("STEEL",         period),
                getSeries("CRUMB_RUBBER",  period),
                getSeries("TYRE_INWARD",   period),
                getSeries("WOOD_INWARD",   period),
                getSeries("OTHERS_INWARD", period)
        );
    }

    // ── Inward by supplier ────────────────────────────────────────────────────
    public GraphSeriesResponse getInwardBySupplier() {
        GraphSeriesResponse r = new GraphSeriesResponse(
                "Purchase – All Types (by Supplier)", "MONTHLY",
                inwardToPoints(inwardRepo.findMonthlyBySupplier()));
        r.setItem("ALL_INWARD");
        return r;
    }

    public GraphSeriesResponse getInwardBySupplierAndType(String type) {
        String dbType = mapInwardKeyToDbType(type);
        GraphSeriesResponse r = new GraphSeriesResponse(
                displayName(type) + " (by Supplier)", "MONTHLY",
                inwardToPoints(inwardRepo.findMonthlyBySupplierAndType(dbType)));
        r.setItem(type.toUpperCase());
        return r;
    }

    // ── Outward by client ─────────────────────────────────────────────────────
    public GraphSeriesResponse getOutwardByClient(String item) {
        GraphSeriesResponse r = new GraphSeriesResponse(
                displayName(item) + " (by Client)", "MONTHLY",
                outwardToPoints(outwardRepo.findMonthlyByTypeAndClient(item.toUpperCase().trim())));
        r.setItem(item.toUpperCase());
        return r;
    }

    // ── Purchase Rate Variation ───────────────────────────────────────────────
    public List<GraphSeriesResponse> getPurchaseRateVariation(String period, String from, String to) {
        return List.of(
                buildPurchaseRateSeries("CRUMB_RUBBER",  "CRUMB_RUBBER",  period),
                buildPurchaseRateSeries("TYRE_INWARD",   "PYROLYSIS_OIL", period),
                buildPurchaseRateSeries("WOOD_INWARD",   "WOOD",          period),
                buildPurchaseRateSeries("OTHERS_INWARD", "OTHERS",        period)
        );
    }

    private GraphSeriesResponse buildPurchaseRateSeries(String itemKey, String dbType, String period) {
        List<MaterialInwardGraphRepository.GraphRawRowProjection> rows;
        switch (period.toUpperCase()) {
            case "WEEKLY": rows = inwardRepo.findWeeklyRateByType(dbType);  break;
            case "YEARLY": rows = inwardRepo.findYearlyRateByType(dbType);  break;
            default:       rows = inwardRepo.findMonthlyRateByType(dbType); break;
        }
        return buildResponse(itemKey.toUpperCase(), period.toUpperCase(), inwardToPoints(rows));
    }

    // ── Fetch helpers ─────────────────────────────────────────────────────────

    private List<MaterialOutwardGraphRepository.GraphRawRowProjection> fetchOutward(
            String type, String period) {
        return switch (period.toUpperCase()) {
            case "WEEKLY"      -> outwardRepo.findWeeklyByType(type);
            case "FORTNIGHTLY" -> outwardRepo.findFortnightlyByType(type);
            case "YEARLY"      -> outwardRepo.findYearlyByType(type);
            default            -> outwardRepo.findMonthlyByType(type);
        };
    }

    private List<MaterialInwardGraphRepository.GraphRawRowProjection> fetchInward(
            String dbType, String period) {
        return switch (period.toUpperCase()) {
            case "WEEKLY"      -> inwardRepo.findWeeklyByType(dbType);
            case "FORTNIGHTLY" -> inwardRepo.findFortnightlyByType(dbType);
            case "YEARLY"      -> inwardRepo.findYearlyByType(dbType);
            default            -> inwardRepo.findMonthlyByType(dbType);
        };
    }

    private List<MaterialInwardGraphRepository.GraphRawRowProjection> fetchAllInward(String period) {
        return switch (period.toUpperCase()) {
            case "WEEKLY"      -> inwardRepo.findWeekly();
            case "FORTNIGHTLY" -> inwardRepo.findFortnightly();
            case "YEARLY"      -> inwardRepo.findYearly();
            default            -> inwardRepo.findMonthly();
        };
    }

    // ── Build response ────────────────────────────────────────────────────────
    private GraphSeriesResponse buildResponse(String itemKey, String period, List<GraphDataPoint> points) {
        GraphSeriesResponse r = new GraphSeriesResponse(displayName(itemKey), period, points);
        r.setItem(itemKey);
        return r;
    }

    // ── Projection → GraphDataPoint ───────────────────────────────────────────
    private List<GraphDataPoint> outwardToPoints(
            List<MaterialOutwardGraphRepository.GraphRawRowProjection> rows) {
        return rows.stream()
                .map(r -> new GraphDataPoint(
                        r.getPeriodLabel(),
                        r.getTotalQty()   != null ? r.getTotalQty()   : 0.0,
                        r.getTotalValue() != null ? r.getTotalValue() : 0.0))
                .collect(Collectors.toList());
    }

    private List<GraphDataPoint> inwardToPoints(
            List<MaterialInwardGraphRepository.GraphRawRowProjection> rows) {
        return rows.stream()
                .map(r -> new GraphDataPoint(
                        r.getPeriodLabel(),
                        r.getTotalQty()   != null ? r.getTotalQty()   : 0.0,
                        r.getTotalValue() != null ? r.getTotalValue() : 0.0))
                .collect(Collectors.toList());
    }

    // ── Helpers ───────────────────────────────────────────────────────────────
    private String mapInwardKeyToDbType(String key) {
        return switch (key.toUpperCase()) {
            case "TYRE_INWARD"   -> "PYROLYSIS_OIL";
            case "CRUMB_RUBBER"  -> "CRUMB_RUBBER";
            case "WOOD_INWARD"   -> "WOOD";
            case "OTHERS_INWARD" -> "OTHERS";
            default              -> key.toUpperCase();
        };
    }

    public static String displayName(String item) {
        return switch (item.toUpperCase()) {
            case "TYRE_OIL"      -> "Sale - Pyrolysis Oil";
            case "CARBON_POWER"  -> "Sale - Carbon Powder";
            case "STEEL"         -> "Sale - Steel Wire";
            case "CRUMB_RUBBER"  -> "Purchase - Crumb Rubber";
            case "TYRE_INWARD"   -> "Purchase - Pyrolysis Oil";
            case "WOOD_INWARD"   -> "Purchase - Wood";
            case "OTHERS_INWARD" -> "Purchase - Others";
            case "ALL_INWARD"    -> "Purchase - All Types";
            default              -> item;
        };
    }
}