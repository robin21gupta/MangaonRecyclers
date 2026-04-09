package com.mangaon.recycleers.service;

import com.mangaon.recycleers.model.MaterialOutward;
import com.mangaon.recycleers.model.PurchaseSaleTrail;
import com.mangaon.recycleers.model.MaterialInward;
import com.mangaon.recycleers.repository.PurchaseSaleTrailRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

@Service
public class PurchaseSaleTrailService {

    private final PurchaseSaleTrailRepository trailRepo;

    public PurchaseSaleTrailService(PurchaseSaleTrailRepository trailRepo) {
        this.trailRepo = trailRepo;
    }

    // ── Record PURCHASE when MaterialInward is saved/updated ──────────────────

    @Transactional
    public void recordInward(MaterialInward inward) {
        // Delete old trail entry for this source first (handles update case)
        trailRepo.deleteBySourceIdAndSourceTable(inward.getId(), "tyre_inward");

        PurchaseSaleTrail trail = new PurchaseSaleTrail();
        trail.setEntryType(PurchaseSaleTrail.EntryType.PURCHASE);
        trail.setEntryDate(inward.getDate());
        trail.setMaterialType(
                inward.getMaterialType() != null ? inward.getMaterialType().name() : null
        );
        trail.setPartyName(
                inward.getSupplier() != null ? inward.getSupplier().getSupplierName() : null
        );
        trail.setVehicleNumber(inward.getVehicleNumber());
        trail.setDriverName(inward.getDriverName());
        trail.setGrossWeight(inward.getGrossWeight());
        trail.setTareWeight(inward.getTareWeight());
        trail.setNetWeight(inward.getNetWeight());
        trail.setQuantity(inward.getQuantity());
        trail.setTyreType(inward.getType());
        trail.setRate(inward.getRate());
        trail.setAmount(
                inward.getTotalAmount() != null ? inward.getTotalAmount() : BigDecimal.ZERO
        );
        trail.setInvoiceNumber(inward.getInvoiceNumber());
        trail.setNotes(inward.getNotes());
        trail.setSourceId(inward.getId());
        trail.setSourceTable("tyre_inward");

        trailRepo.save(trail);
    }

    // ── Record SALE when MaterialOutward is saved/updated ─────────────────────

    @Transactional
    public void recordOutward(MaterialOutward outward) {
        // Delete old trail entry for this source first (handles update case)
        trailRepo.deleteBySourceIdAndSourceTable(outward.getId(), "material_outward");

        PurchaseSaleTrail trail = new PurchaseSaleTrail();
        trail.setEntryType(PurchaseSaleTrail.EntryType.SALE);
        trail.setEntryDate(outward.getDateOfSale());
        trail.setMaterialType(
                outward.getMaterialType() != null ? outward.getMaterialType().name() : null
        );
        trail.setPartyName(
                outward.getClient() != null ? outward.getClient().getClientName() : null
        );
        trail.setVehicleNumber(outward.getVehicleNumber());
        trail.setDriverName(outward.getDriverName());
        trail.setGrossWeight(outward.getGrossWeight());
        trail.setTareWeight(outward.getTareWeight());
        trail.setNetWeight(outward.getNetWeight());
        trail.setQuantity(null);      // outward has no quantity
        trail.setTyreType(null);      // outward has no tyre type
        trail.setRate(outward.getRate());
        trail.setAmount(
                outward.getTotalAmount() != null ? outward.getTotalAmount() : BigDecimal.ZERO
        );
        trail.setInvoiceNumber(outward.getInvoiceNumber());
        trail.setNotes(outward.getNotes());
        trail.setSourceId(outward.getId());
        trail.setSourceTable("material_outward");

        trailRepo.save(trail);
    }

    // ── Delete trail when source inward is deleted ────────────────────────────

    @Transactional
    public void removeInwardTrail(Long inwardId) {
        trailRepo.deleteBySourceIdAndSourceTable(inwardId, "tyre_inward");
    }

    // ── Delete trail when source outward is deleted ───────────────────────────

    @Transactional
    public void removeOutwardTrail(Long outwardId) {
        trailRepo.deleteBySourceIdAndSourceTable(outwardId, "material_outward");
    }

    // ── Fetch all trail records in date range ─────────────────────────────────

    public List<PurchaseSaleTrail> getTrail(LocalDate start, LocalDate end) {
        return trailRepo.findByEntryDateBetweenOrderByEntryDateDescIdDesc(start, end);
    }

    // ── Daily summary: [{date, purchase, sale, profit}] ───────────────────────

    public List<Map<String, Object>> getDailySummary(LocalDate start, LocalDate end) {
        List<Object[]> rows = trailRepo.getDailySummary(start, end);
        List<Map<String, Object>> result = new ArrayList<>();
        for (Object[] row : rows) {
            BigDecimal purchase = row[1] != null ? (BigDecimal) row[1] : BigDecimal.ZERO;
            BigDecimal sale     = row[2] != null ? (BigDecimal) row[2] : BigDecimal.ZERO;
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("date",     row[0]);
            map.put("purchase", purchase);
            map.put("sale",     sale);
            map.put("profit",   sale.subtract(purchase));
            result.add(map);
        }
        return result;
    }

    // ── Grand totals ──────────────────────────────────────────────────────────

    public Map<String, Object> getGrandTotals(LocalDate start, LocalDate end) {
        List<Object[]> rows = trailRepo.getGrandTotals(start, end);

        BigDecimal purchase = BigDecimal.ZERO;
        BigDecimal sale     = BigDecimal.ZERO;

        if (rows != null && !rows.isEmpty()) {
            Object[] row = rows.get(0);
            if (row[0] != null) purchase = new BigDecimal(row[0].toString());
            if (row[1] != null) sale     = new BigDecimal(row[1].toString());
        }

        Map<String, Object> map = new LinkedHashMap<>();
        map.put("totalPurchase", purchase);
        map.put("totalSale",     sale);
        map.put("totalProfit",   sale.subtract(purchase));
        return map;
    }
}