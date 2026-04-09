package com.mangaon.recycleers.repository;

import com.mangaon.recycleers.model.MaterialInward;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MaterialInwardGraphRepository extends JpaRepository<MaterialInward, Long> {

    // ── WEEKLY ───────────────────────────────────────────────────────────────
    @Query(value =
            "SELECT TO_CHAR(inward_date,'IYYY-\"W\"IW') AS \"periodLabel\", " +
                    "CAST(COALESCE(SUM(net_weight),0) AS FLOAT) AS \"totalQty\", " +
                    "CAST(COALESCE(SUM(total_amount),0) AS FLOAT) AS \"totalValue\" " +
                    "FROM tyre_inward WHERE material_type = :type " +
                    "GROUP BY TO_CHAR(inward_date,'IYYY-\"W\"IW') ORDER BY 1 ASC",
            nativeQuery = true)
    List<GraphRawRowProjection> findWeeklyByType(@Param("type") String type);

    // ── FORTNIGHTLY ──────────────────────────────────────────────────────────
    @Query(value =
            "SELECT CONCAT(CAST(EXTRACT(YEAR FROM inward_date) AS TEXT),'-FN'," +
                    "LPAD(CAST(CEIL(EXTRACT(DOY FROM inward_date)/14.0) AS TEXT),2,'0')) AS \"periodLabel\", " +
                    "CAST(COALESCE(SUM(net_weight),0) AS FLOAT) AS \"totalQty\", " +
                    "CAST(COALESCE(SUM(total_amount),0) AS FLOAT) AS \"totalValue\" " +
                    "FROM tyre_inward WHERE material_type = :type GROUP BY 1 ORDER BY 1 ASC",
            nativeQuery = true)
    List<GraphRawRowProjection> findFortnightlyByType(@Param("type") String type);

    // ── MONTHLY ───────────────────────────────────────────────────────────────
    @Query(value =
            "SELECT TO_CHAR(inward_date,'Mon YYYY') AS \"periodLabel\", " +
                    "CAST(COALESCE(SUM(net_weight),0) AS FLOAT) AS \"totalQty\", " +
                    "CAST(COALESCE(SUM(total_amount),0) AS FLOAT) AS \"totalValue\" " +
                    "FROM tyre_inward WHERE material_type = :type " +
                    "GROUP BY TO_CHAR(inward_date,'Mon YYYY'),EXTRACT(YEAR FROM inward_date),EXTRACT(MONTH FROM inward_date) " +
                    "ORDER BY EXTRACT(YEAR FROM inward_date) ASC, EXTRACT(MONTH FROM inward_date) ASC",
            nativeQuery = true)
    List<GraphRawRowProjection> findMonthlyByType(@Param("type") String type);

    // ── YEARLY ────────────────────────────────────────────────────────────────
    @Query(value =
            "SELECT CAST(EXTRACT(YEAR FROM inward_date) AS TEXT) AS \"periodLabel\", " +
                    "CAST(COALESCE(SUM(net_weight),0) AS FLOAT) AS \"totalQty\", " +
                    "CAST(COALESCE(SUM(total_amount),0) AS FLOAT) AS \"totalValue\" " +
                    "FROM tyre_inward WHERE material_type = :type " +
                    "GROUP BY EXTRACT(YEAR FROM inward_date) ORDER BY 1 ASC",
            nativeQuery = true)
    List<GraphRawRowProjection> findYearlyByType(@Param("type") String type);

    // ── DAILY (date range, filtered by type) ─────────────────────────────────
    @Query(value =
            "SELECT TO_CHAR(inward_date,'DD Mon YYYY') AS \"periodLabel\", " +
                    "CAST(COALESCE(SUM(net_weight),0) AS FLOAT) AS \"totalQty\", " +
                    "CAST(COALESCE(SUM(total_amount),0) AS FLOAT) AS \"totalValue\" " +
                    "FROM tyre_inward " +
                    "WHERE material_type = :type " +
                    "AND (:fromDate IS NULL OR inward_date >= CAST(:fromDate AS DATE)) " +
                    "AND (:toDate   IS NULL OR inward_date <= CAST(:toDate   AS DATE)) " +
                    "GROUP BY inward_date, TO_CHAR(inward_date,'DD Mon YYYY') " +
                    "ORDER BY inward_date ASC",
            nativeQuery = true)
    List<GraphRawRowProjection> findDailyByType(
            @Param("type") String type,
            @Param("fromDate") String fromDate,
            @Param("toDate") String toDate);

    // ── DAILY ALL TYPES ───────────────────────────────────────────────────────
    @Query(value =
            "SELECT TO_CHAR(inward_date,'DD Mon YYYY') AS \"periodLabel\", " +
                    "CAST(COALESCE(SUM(net_weight),0) AS FLOAT) AS \"totalQty\", " +
                    "CAST(COALESCE(SUM(total_amount),0) AS FLOAT) AS \"totalValue\" " +
                    "FROM tyre_inward " +
                    "WHERE (:fromDate IS NULL OR inward_date >= CAST(:fromDate AS DATE)) " +
                    "AND   (:toDate   IS NULL OR inward_date <= CAST(:toDate   AS DATE)) " +
                    "GROUP BY inward_date, TO_CHAR(inward_date,'DD Mon YYYY') " +
                    "ORDER BY inward_date ASC",
            nativeQuery = true)
    List<GraphRawRowProjection> findDaily(
            @Param("fromDate") String fromDate,
            @Param("toDate") String toDate);

    // ── ALL INWARD TYPES — WEEKLY ─────────────────────────────────────────────
    @Query(value =
            "SELECT TO_CHAR(inward_date,'IYYY-\"W\"IW') AS \"periodLabel\", " +
                    "CAST(COALESCE(SUM(net_weight),0) AS FLOAT) AS \"totalQty\", " +
                    "CAST(COALESCE(SUM(total_amount),0) AS FLOAT) AS \"totalValue\" " +
                    "FROM tyre_inward GROUP BY TO_CHAR(inward_date,'IYYY-\"W\"IW') ORDER BY 1 ASC",
            nativeQuery = true)
    List<GraphRawRowProjection> findWeekly();

    // ── ALL INWARD TYPES — FORTNIGHTLY ────────────────────────────────────────
    @Query(value =
            "SELECT CONCAT(CAST(EXTRACT(YEAR FROM inward_date) AS TEXT),'-FN'," +
                    "LPAD(CAST(CEIL(EXTRACT(DOY FROM inward_date)/14.0) AS TEXT),2,'0')) AS \"periodLabel\", " +
                    "CAST(COALESCE(SUM(net_weight),0) AS FLOAT) AS \"totalQty\", " +
                    "CAST(COALESCE(SUM(total_amount),0) AS FLOAT) AS \"totalValue\" " +
                    "FROM tyre_inward GROUP BY 1 ORDER BY 1 ASC",
            nativeQuery = true)
    List<GraphRawRowProjection> findFortnightly();

    // ── ALL INWARD TYPES — MONTHLY ────────────────────────────────────────────
    @Query(value =
            "SELECT TO_CHAR(inward_date,'Mon YYYY') AS \"periodLabel\", " +
                    "CAST(COALESCE(SUM(net_weight),0) AS FLOAT) AS \"totalQty\", " +
                    "CAST(COALESCE(SUM(total_amount),0) AS FLOAT) AS \"totalValue\" " +
                    "FROM tyre_inward " +
                    "GROUP BY TO_CHAR(inward_date,'Mon YYYY'),EXTRACT(YEAR FROM inward_date),EXTRACT(MONTH FROM inward_date) " +
                    "ORDER BY EXTRACT(YEAR FROM inward_date) ASC, EXTRACT(MONTH FROM inward_date) ASC",
            nativeQuery = true)
    List<GraphRawRowProjection> findMonthly();

    // ── ALL INWARD TYPES — YEARLY ─────────────────────────────────────────────
    @Query(value =
            "SELECT CAST(EXTRACT(YEAR FROM inward_date) AS TEXT) AS \"periodLabel\", " +
                    "CAST(COALESCE(SUM(net_weight),0) AS FLOAT) AS \"totalQty\", " +
                    "CAST(COALESCE(SUM(total_amount),0) AS FLOAT) AS \"totalValue\" " +
                    "FROM tyre_inward GROUP BY EXTRACT(YEAR FROM inward_date) ORDER BY 1 ASC",
            nativeQuery = true)
    List<GraphRawRowProjection> findYearly();

    // ── SUPPLIER-WISE MONTHLY (all types) ─────────────────────────────────────
    @Query(value =
            "SELECT CONCAT(TO_CHAR(t.inward_date,'Mon YYYY'),' · ',COALESCE(s.supplier_name,'Unknown')) AS \"periodLabel\", " +
                    "CAST(COALESCE(SUM(t.net_weight),0) AS FLOAT) AS \"totalQty\", " +
                    "CAST(COALESCE(SUM(t.total_amount),0) AS FLOAT) AS \"totalValue\" " +
                    "FROM tyre_inward t LEFT JOIN suppliers s ON s.id = t.supplier_id " +
                    "GROUP BY TO_CHAR(t.inward_date,'Mon YYYY'),EXTRACT(YEAR FROM t.inward_date),EXTRACT(MONTH FROM t.inward_date),s.supplier_name " +
                    "ORDER BY EXTRACT(YEAR FROM t.inward_date) ASC, EXTRACT(MONTH FROM t.inward_date) ASC",
            nativeQuery = true)
    List<GraphRawRowProjection> findMonthlyBySupplier();

    // ── SUPPLIER-WISE MONTHLY (filtered by material type) ────────────────────
    @Query(value =
            "SELECT CONCAT(TO_CHAR(t.inward_date,'Mon YYYY'),' · ',COALESCE(s.supplier_name,'Unknown')) AS \"periodLabel\", " +
                    "CAST(COALESCE(SUM(t.net_weight),0) AS FLOAT) AS \"totalQty\", " +
                    "CAST(COALESCE(SUM(t.total_amount),0) AS FLOAT) AS \"totalValue\" " +
                    "FROM tyre_inward t LEFT JOIN suppliers s ON s.id = t.supplier_id " +
                    "WHERE t.material_type = :type " +
                    "GROUP BY TO_CHAR(t.inward_date,'Mon YYYY'),EXTRACT(YEAR FROM t.inward_date),EXTRACT(MONTH FROM t.inward_date),s.supplier_name " +
                    "ORDER BY EXTRACT(YEAR FROM t.inward_date) ASC, EXTRACT(MONTH FROM t.inward_date) ASC",
            nativeQuery = true)
    List<GraphRawRowProjection> findMonthlyBySupplierAndType(@Param("type") String type);

    // ── RATE VARIATION: avg rate (total_amount/net_weight) per period ─────────
    @Query(value =
            "SELECT TO_CHAR(inward_date,'Mon YYYY') AS \"periodLabel\", " +
                    "CAST(COALESCE(SUM(net_weight),0) AS FLOAT) AS \"totalQty\", " +
                    "CAST(CASE WHEN SUM(net_weight)>0 THEN SUM(total_amount)/SUM(net_weight) ELSE 0 END AS FLOAT) AS \"totalValue\" " +
                    "FROM tyre_inward WHERE material_type = :type " +
                    "GROUP BY TO_CHAR(inward_date,'Mon YYYY'),EXTRACT(YEAR FROM inward_date),EXTRACT(MONTH FROM inward_date) " +
                    "ORDER BY EXTRACT(YEAR FROM inward_date) ASC, EXTRACT(MONTH FROM inward_date) ASC",
            nativeQuery = true)
    List<GraphRawRowProjection> findMonthlyRateByType(@Param("type") String type);

    @Query(value =
            "SELECT TO_CHAR(inward_date,'IYYY-\"W\"IW') AS \"periodLabel\", " +
                    "CAST(COALESCE(SUM(net_weight),0) AS FLOAT) AS \"totalQty\", " +
                    "CAST(CASE WHEN SUM(net_weight)>0 THEN SUM(total_amount)/SUM(net_weight) ELSE 0 END AS FLOAT) AS \"totalValue\" " +
                    "FROM tyre_inward WHERE material_type = :type " +
                    "GROUP BY TO_CHAR(inward_date,'IYYY-\"W\"IW') ORDER BY 1 ASC",
            nativeQuery = true)
    List<GraphRawRowProjection> findWeeklyRateByType(@Param("type") String type);

    @Query(value =
            "SELECT CAST(EXTRACT(YEAR FROM inward_date) AS TEXT) AS \"periodLabel\", " +
                    "CAST(COALESCE(SUM(net_weight),0) AS FLOAT) AS \"totalQty\", " +
                    "CAST(CASE WHEN SUM(net_weight)>0 THEN SUM(total_amount)/SUM(net_weight) ELSE 0 END AS FLOAT) AS \"totalValue\" " +
                    "FROM tyre_inward WHERE material_type = :type " +
                    "GROUP BY EXTRACT(YEAR FROM inward_date) ORDER BY 1 ASC",
            nativeQuery = true)
    List<GraphRawRowProjection> findYearlyRateByType(@Param("type") String type);

    // ── Projection interface ─────────────────────────────────────────────────
    interface GraphRawRowProjection {
        String getPeriodLabel();
        Double getTotalQty();
        Double getTotalValue();
    }
}