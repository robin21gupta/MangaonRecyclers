package com.mangaon.recycleers.model;

public class GraphDataPoint {

    private String label;
    private Double qty;
    private Double value;

    public GraphDataPoint() {}

    public GraphDataPoint(String label, Double qty, Double value) {
        this.label = label != null ? label : "";
        // FIX: null-safe rounding — inward queries always return 0.0 for total_value
        this.qty   = (qty   != null && !qty.isNaN()   && !qty.isInfinite())
                ? Math.round(qty   * 100.0) / 100.0 : 0.0;
        this.value = (value != null && !value.isNaN() && !value.isInfinite())
                ? Math.round(value * 100.0) / 100.0 : 0.0;
    }

    public String getLabel() { return label; }
    public Double getQty()   { return qty;   }
    public Double getValue() { return value; }

    public void setLabel(String label) { this.label = label; }
    public void setQty(Double qty)     { this.qty   = qty;   }
    public void setValue(Double value) { this.value = value; }
}