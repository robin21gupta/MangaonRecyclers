package com.mangaon.recycleers.dto;

import java.math.BigDecimal;

public class MaterialInwardRequest {

    private Long   supplierId;
    private Long   useId;
    private String invoiceNumber;
    private String materialType;   // kept as String — converted in service
    private String vehicleNumber;
    private String driverName;
    private String driverNumber;
    private String date;           // kept as String "yyyy-MM-dd" — converted in service
    private BigDecimal grossWeight;
    private BigDecimal tareWeight;
    private Integer    quantity;
    private String     type;
    private BigDecimal rate;
    private String     notes;

    public MaterialInwardRequest() {}

    // ── Getters ──────────────────────────────────────────────────────────────
    public Long        getSupplierId()   { return supplierId; }
    public Long        getUseId()        { return useId; }
    public String      getMaterialType() { return materialType; }
    public String getInvoiceNumber() { return invoiceNumber; }
    public String      getVehicleNumber(){ return vehicleNumber; }
    public String      getDriverName()   { return driverName; }
    public String      getDriverNumber() { return driverNumber; }
    public String      getDate()         { return date; }
    public BigDecimal  getGrossWeight()  { return grossWeight; }
    public BigDecimal  getTareWeight()   { return tareWeight; }
    public Integer     getQuantity()     { return quantity; }
    public String      getType()         { return type; }
    public BigDecimal  getRate()         { return rate; }
    public String      getNotes()        { return notes; }

    // ── Setters ──────────────────────────────────────────────────────────────
    public void setSupplierId(Long v)    { this.supplierId = v; }
    public void setUseId(Long v)         { this.useId = v; }
    public void setMaterialType(String v){ this.materialType = v; }
    public void setInvoiceNumber(String invoiceNumber) { this.invoiceNumber = invoiceNumber; }
    public void setVehicleNumber(String v){ this.vehicleNumber = v; }
    public void setDriverName(String v)  { this.driverName = v; }
    public void setDriverNumber(String v){ this.driverNumber = v; }
    public void setDate(String v)        { this.date = v; }
    public void setGrossWeight(BigDecimal v){ this.grossWeight = v; }
    public void setTareWeight(BigDecimal v) { this.tareWeight = v; }
    public void setQuantity(Integer v)   { this.quantity = v; }
    public void setType(String v)        { this.type = v; }
    public void setRate(BigDecimal v)    { this.rate = v; }
    public void setNotes(String v)       { this.notes = v; }
}