package com.mangaon.recycleers.model;

/**
 * Concrete result class for native graph queries.
 *
 * WHY: Spring Data JPA interface projections with native queries use camelCase
 * conversion for column aliases (total_qty -> getTotalQty). This conversion is
 * unreliable across Spring Boot versions and often returns NULL even when the
 * column has data.
 *
 * FIX: Use a concrete class with a constructor that Spring/Hibernate can call
 * directly. Native queries with @SqlResultSetMapping or ConstructorResult are
 * the most reliable approach, but for simplicity we keep the interface and fix
 * the alias names to use camelCase directly in the SQL (no underscore).
 *
 * This class is used by both TyreInwardGraphRepository and
 * MaterialOutwardGraphRepository as the projection target.
 */
public class GraphRawRow {

    private final String periodLabel;
    private final Double totalQty;
    private final Double totalValue;

    public GraphRawRow(String periodLabel, Double totalQty, Double totalValue) {
        this.periodLabel = periodLabel;
        this.totalQty    = totalQty    != null ? totalQty    : 0.0;
        this.totalValue  = totalValue  != null ? totalValue  : 0.0;
    }

    public String getPeriodLabel() { return periodLabel; }
    public Double getTotalQty()    { return totalQty;    }
    public Double getTotalValue()  { return totalValue;  }
}