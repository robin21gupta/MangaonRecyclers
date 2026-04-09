package com.mangaon.recycleers.dto;

import java.math.BigDecimal;

public class MaterialOutwardRequest {

    private String materialType;  // "TYRE_OIL" | "CARBON_POWER" | "STEEL"
    private Long clientId;
    private Long useId;
    private String invoiceNumber;
    private String vehicleNumber;
    private String driverName;
    private String driverNumber;
    private String dateOfSale;    // "dd-MM-yyyy"
    private BigDecimal grossWeight;
    private BigDecimal tareWeight;
    private BigDecimal rate;
    private String notes;

    public MaterialOutwardRequest() {}

    // Getters & Setters
    public String getMaterialType() { return materialType; }
    public void setMaterialType(String materialType) { this.materialType = materialType; }
    public Long getClientId() { return clientId; }
    public String getInvoiceNumber() { return invoiceNumber; }
    public void setInvoiceNumber(String invoiceNumber) { this.invoiceNumber = invoiceNumber; }
    public void setClientId(Long clientId) { this.clientId = clientId; }
    public Long getUseId() { return useId; }
    public void setUseId(Long useId) { this.useId = useId; }
    public String getVehicleNumber() { return vehicleNumber; }
    public void setVehicleNumber(String vehicleNumber) { this.vehicleNumber = vehicleNumber; }
    public String getDriverName() { return driverName; }
    public void setDriverName(String driverName) { this.driverName = driverName; }
    public String getDriverNumber() { return driverNumber; }
    public void setDriverNumber(String driverNumber) { this.driverNumber = driverNumber; }
    public String getDateOfSale() { return dateOfSale; }
    public void setDateOfSale(String dateOfSale) { this.dateOfSale = dateOfSale; }
    public BigDecimal getGrossWeight() { return grossWeight; }
    public void setGrossWeight(BigDecimal grossWeight) { this.grossWeight = grossWeight; }
    public BigDecimal getTareWeight() { return tareWeight; }
    public void setTareWeight(BigDecimal tareWeight) { this.tareWeight = tareWeight; }
    public BigDecimal getRate() { return rate; }
    public void setRate(BigDecimal rate) { this.rate = rate; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}