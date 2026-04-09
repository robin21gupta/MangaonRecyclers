package com.mangaon.recycleers.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "invoice_photo",
        indexes = {
                @Index(name = "idx_invoice_entry", columnList = "entry_type, entry_id")
        })
public class InvoicePhoto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "entry_type", nullable = false, length = 10)
    private String entryType;

    @Column(name = "entry_id", nullable = false)
    private Long entryId;

    @Column(name = "invoice_number", length = 100)
    private String invoiceNumber;

    @Column(name = "original_filename", length = 255)
    private String originalFilename;

    @Column(name = "content_type", length = 100)
    private String contentType;

    @Column(name = "file_size")
    private Long fileSize;

    @Column(name = "storage_path", nullable = false, length = 512)
    private String storagePath;

    @Column(name = "uploaded_at", nullable = false, updatable = false)
    private LocalDateTime uploadedAt;

    @PrePersist
    protected void onCreate() {
        this.uploadedAt = LocalDateTime.now();
    }

    public InvoicePhoto() {}

    public InvoicePhoto(String entryType, Long entryId, String invoiceNumber,
                        String originalFilename, String contentType,
                        Long fileSize, String storagePath) {
        this.entryType        = entryType;
        this.entryId          = entryId;
        this.invoiceNumber    = invoiceNumber;
        this.originalFilename = originalFilename;
        this.contentType      = contentType;
        this.fileSize         = fileSize;
        this.storagePath      = storagePath;
    }

    public Long   getId()                        { return id; }
    public String getEntryType()                 { return entryType; }
    public void   setEntryType(String v)         { this.entryType = v; }
    public Long   getEntryId()                   { return entryId; }
    public void   setEntryId(Long v)             { this.entryId = v; }
    public String getInvoiceNumber()             { return invoiceNumber; }
    public void   setInvoiceNumber(String v)     { this.invoiceNumber = v; }
    public String getOriginalFilename()          { return originalFilename; }
    public void   setOriginalFilename(String v)  { this.originalFilename = v; }
    public String getContentType()               { return contentType; }
    public void   setContentType(String v)       { this.contentType = v; }
    public Long   getFileSize()                  { return fileSize; }
    public void   setFileSize(Long v)            { this.fileSize = v; }
    public String getStoragePath()               { return storagePath; }
    public void   setStoragePath(String v)       { this.storagePath = v; }
    public LocalDateTime getUploadedAt()         { return uploadedAt; }

    @Override
    public String toString() {
        return "InvoicePhoto{id=" + id + ", entryType=" + entryType
                + ", entryId=" + entryId + ", invoiceNumber=" + invoiceNumber
                + ", file=" + originalFilename + "}";
    }
}