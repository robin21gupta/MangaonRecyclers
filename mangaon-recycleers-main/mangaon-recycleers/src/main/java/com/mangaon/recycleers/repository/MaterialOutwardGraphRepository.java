package com.mangaon.recycleers.repository;

import com.mangaon.recycleers.model.MaterialOutward;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MaterialOutwardGraphRepository extends JpaRepository<MaterialOutward, Long> {

    // ── WEEKLY ───────────────────────────────────────────────────────────────
    @Query(value =
            "SELECT TO_CHAR(date_of_sale,'IYYY-\"W\"IW') AS \"periodLabel\", " +
                    "CAST(COALESCE(SUM(net_weight),0) AS FLOAT) AS \"totalQty\", " +
                    "CAST(COALESCE(SUM(total_amount),0) AS FLOAT) AS \"totalValue\" " +
                    "FROM material_outward WHERE material_type = :type " +
                    "GROUP BY TO_CHAR(date_of_sale,'IYYY-\"W\"IW') ORDER BY 1 ASC",
            nativeQuery = true)
    List<GraphRawRowProjection> findWeeklyByType(@Param("type") String type);

    // ── FORTNIGHTLY ──────────────────────────────────────────────────────────
    @Query(value =
            "SELECT CONCAT(CAST(EXTRACT(YEAR FROM date_of_sale) AS TEXT),'-FN'," +
                    "LPAD(CAST(CEIL(EXTRACT(DOY FROM date_of_sale)/14.0) AS TEXT),2,'0')) AS \"periodLabel\", " +
                    "CAST(COALESCE(SUM(net_weight),0) AS FLOAT) AS \"totalQty\", " +
                    "CAST(COALESCE(SUM(total_amount),0) AS FLOAT) AS \"totalValue\" " +
                    "FROM material_outward WHERE material_type = :type GROUP BY 1 ORDER BY 1 ASC",
            nativeQuery = true)
    List<GraphRawRowProjection> findFortnightlyByType(@Param("type") String type);

    // ── MONTHLY ───────────────────────────────────────────────────────────────
    @Query(value =
            "SELECT TO_CHAR(date_of_sale,'Mon YYYY') AS \"periodLabel\", " +
                    "CAST(COALESCE(SUM(net_weight),0) AS FLOAT) AS \"totalQty\", " +
                    "CAST(COALESCE(SUM(total_amount),0) AS FLOAT) AS \"totalValue\" " +
                    "FROM material_outward WHERE material_type = :type " +
                    "GROUP BY TO_CHAR(date_of_sale,'Mon YYYY'),EXTRACT(YEAR FROM date_of_sale),EXTRACT(MONTH FROM date_of_sale) " +
                    "ORDER BY EXTRACT(YEAR FROM date_of_sale) ASC, EXTRACT(MONTH FROM date_of_sale) ASC",
            nativeQuery = true)
    List<GraphRawRowProjection> findMonthlyByType(@Param("type") String type);

    // ── YEARLY ────────────────────────────────────────────────────────────────
    @Query(value =
            "SELECT CAST(EXTRACT(YEAR FROM date_of_sale) AS TEXT) AS \"periodLabel\", " +
                    "CAST(COALESCE(SUM(net_weight),0) AS FLOAT) AS \"totalQty\", " +
                    "CAST(COALESCE(SUM(total_amount),0) AS FLOAT) AS \"totalValue\" " +
                    "FROM material_outward WHERE material_type = :type " +
                    "GROUP BY EXTRACT(YEAR FROM date_of_sale) ORDER BY 1 ASC",
            nativeQuery = true)
    List<GraphRawRowProjection> findYearlyByType(@Param("type") String type);

    // ── DAILY (date range, filtered by type) ─────────────────────────────────
    @Query(value =
            "SELECT TO_CHAR(date_of_sale,'DD Mon YYYY') AS \"periodLabel\", " +
                    "CAST(COALESCE(SUM(net_weight),0) AS FLOAT) AS \"totalQty\", " +
                    "CAST(COALESCE(SUM(total_amount),0) AS FLOAT) AS \"totalValue\" " +
                    "FROM material_outward " +
                    "WHERE material_type = :type " +
                    "AND (:fromDate IS NULL OR date_of_sale >= CAST(:fromDate AS DATE)) " +
                    "AND (:toDate   IS NULL OR date_of_sale <= CAST(:toDate   AS DATE)) " +
                    "GROUP BY date_of_sale, TO_CHAR(date_of_sale,'DD Mon YYYY') " +
                    "ORDER BY date_of_sale ASC",
            nativeQuery = true)
    List<GraphRawRowProjection> findDailyByType(
            @Param("type") String type,
            @Param("fromDate") String fromDate,
            @Param("toDate") String toDate);

    // ── CLIENT-WISE MONTHLY ───────────────────────────────────────────────────
    @Query(value =
            "SELECT CONCAT(TO_CHAR(m.date_of_sale,'Mon YYYY'),' · ',COALESCE(c.client_name,'Unknown')) AS \"periodLabel\", " +
                    "CAST(COALESCE(SUM(m.net_weight),0) AS FLOAT) AS \"totalQty\", " +
                    "CAST(COALESCE(SUM(m.total_amount),0) AS FLOAT) AS \"totalValue\" " +
                    "FROM material_outward m LEFT JOIN clients c ON c.id = m.client_id " +
                    "WHERE m.material_type = :type " +
                    "GROUP BY TO_CHAR(m.date_of_sale,'Mon YYYY'),EXTRACT(YEAR FROM m.date_of_sale),EXTRACT(MONTH FROM m.date_of_sale),c.client_name " +
                    "ORDER BY EXTRACT(YEAR FROM m.date_of_sale) ASC, EXTRACT(MONTH FROM m.date_of_sale) ASC",
            nativeQuery = true)
    List<GraphRawRowProjection> findMonthlyByTypeAndClient(@Param("type") String type);

    // ── Projection interface ─────────────────────────────────────────────────
    interface GraphRawRowProjection {
        String getPeriodLabel();
        Double getTotalQty();
        Double getTotalValue();
    }
}