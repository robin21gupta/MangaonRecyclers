package com.mangaon.recycleers.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.mangaon.recycleers.enums.OutwardMaterialType;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Entity
@Table(name = "material_outward")
public class MaterialOutward {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Client client;

    @Enumerated(EnumType.STRING)
    @Column(name = "material_type")
    private OutwardMaterialType materialType;

    private String vehicleNumber;
    private String driverName;
    private String driverNumber;

    @JsonFormat(pattern = "dd-MM-yyyy")
    @Column(name = "date_of_sale")
    private LocalDate dateOfSale;

    private BigDecimal grossWeight;
    private BigDecimal tareWeight;
    private BigDecimal netWeight;
    private BigDecimal rate;
    private BigDecimal totalAmount;

    // ── Added to match MaterialInward ──
    private Integer quantity;
    private String type;

    private String notes;

    @Column(name = "invoice_number")
    private String invoiceNumber;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        calculate();
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
        calculate();
    }

    private void calculate() {
        if (grossWeight != null && tareWeight != null) {
            netWeight = grossWeight.subtract(tareWeight);
        }
        if (netWeight != null && rate != null) {
            totalAmount = netWeight.multiply(rate);
        }
    }

    public Long getId() { return id; }

    public Client getClient() { return client; }
    public void setClient(Client client) { this.client = client; }

    public OutwardMaterialType getMaterialType() { return materialType; }
    public void setMaterialType(OutwardMaterialType materialType) { this.materialType = materialType; }

    public String getVehicleNumber() { return vehicleNumber; }
    public void setVehicleNumber(String vehicleNumber) { this.vehicleNumber = vehicleNumber; }

    public String getDriverName() { return driverName; }
    public void setDriverName(String driverName) { this.driverName = driverName; }

    public String getDriverNumber() { return driverNumber; }
    public void setDriverNumber(String driverNumber) { this.driverNumber = driverNumber; }

    public LocalDate getDateOfSale() { return dateOfSale; }
    public void setDateOfSale(LocalDate dateOfSale) { this.dateOfSale = dateOfSale; }

    public BigDecimal getGrossWeight() { return grossWeight; }
    public void setGrossWeight(BigDecimal grossWeight) { this.grossWeight = grossWeight; }

    public BigDecimal getTareWeight() { return tareWeight; }
    public void setTareWeight(BigDecimal tareWeight) { this.tareWeight = tareWeight; }

    public BigDecimal getNetWeight() { return netWeight; }

    public BigDecimal getRate() { return rate; }
    public void setRate(BigDecimal rate) { this.rate = rate; }

    public BigDecimal getTotalAmount() { return totalAmount; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public String getInvoiceNumber() { return invoiceNumber; }
    public void setInvoiceNumber(String invoiceNumber) { this.invoiceNumber = invoiceNumber; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}