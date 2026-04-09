package com.mangaon.recycleers.repository;

import com.mangaon.recycleers.model.PurchaseSaleTrail;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface PurchaseSaleTrailRepository extends JpaRepository<PurchaseSaleTrail, Long> {

    // All trail records in date range, newest first
    List<PurchaseSaleTrail> findByEntryDateBetweenOrderByEntryDateDescIdDesc(
            LocalDate start, LocalDate end
    );

    // Only PURCHASE or only SALE
    List<PurchaseSaleTrail> findByEntryTypeAndEntryDateBetweenOrderByEntryDateDescIdDesc(
            PurchaseSaleTrail.EntryType type, LocalDate start, LocalDate end
    );

    // Delete existing trail record when source is updated or deleted
    void deleteBySourceIdAndSourceTable(Long sourceId, String sourceTable);

    // Daily summary: date, totalPurchase, totalSale
    @Query("""
        SELECT t.entryDate,
               SUM(CASE WHEN t.entryType = 'PURCHASE' THEN t.amount ELSE 0 END) AS totalPurchase,
               SUM(CASE WHEN t.entryType = 'SALE'     THEN t.amount ELSE 0 END) AS totalSale
        FROM PurchaseSaleTrail t
        WHERE t.entryDate BETWEEN :start AND :end
        GROUP BY t.entryDate
        ORDER BY t.entryDate ASC
    """)
    List<Object[]> getDailySummary(
            @Param("start") LocalDate start,
            @Param("end")   LocalDate end
    );

    // ── FIXED: return type changed from Object[] to List<Object[]> ─────────
    // Spring Data JPA always wraps query results in a List.
    // Declaring return type as Object[] was causing the ClassCastException
    // because JPA returned List<Object[]> and Java tried to cast the List
    // itself (which is Object[]) to BigDecimal when accessing row[0].
    @Query("""
        SELECT
          SUM(CASE WHEN t.entryType = 'PURCHASE' THEN t.amount ELSE 0 END),
          SUM(CASE WHEN t.entryType = 'SALE'     THEN t.amount ELSE 0 END)
        FROM PurchaseSaleTrail t
        WHERE t.entryDate BETWEEN :start AND :end
    """)
    List<Object[]> getGrandTotals(      // ← was Object[], now List<Object[]>
                                        @Param("start") LocalDate start,
                                        @Param("end")   LocalDate end
    );
    @Modifying
    @Transactional
    @Query("UPDATE PurchaseSaleTrail t SET t.invoiceNumber = :invoiceNumber " +
            "WHERE t.sourceId = :sourceId AND t.sourceTable = :sourceTable")
    void updateInvoiceNumberBySourceId(
            @Param("sourceId")      Long   sourceId,
            @Param("sourceTable")   String sourceTable,
            @Param("invoiceNumber") String invoiceNumber
    );
}