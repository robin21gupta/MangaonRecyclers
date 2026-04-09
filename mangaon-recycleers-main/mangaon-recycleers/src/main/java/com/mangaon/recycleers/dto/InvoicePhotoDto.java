package com.mangaon.recycleers.dto;

import java.time.LocalDateTime;



public class InvoicePhotoDto {

    private Long          id;
    private String        entryType;
    private Long          entryId;
    private String        invoiceNumber;
    private String        originalFilename;
    private String        contentType;
    private Long          fileSize;
    /** Clickable URL:  /api/invoices/{id}/view  */
    private String        viewUrl;
    private LocalDateTime uploadedAt;

    public InvoicePhotoDto() {}

    public InvoicePhotoDto(Long id, String entryType, Long entryId,
                           String invoiceNumber, String originalFilename,
                           String contentType, Long fileSize,
                           String viewUrl, LocalDateTime uploadedAt) {
        this.id               = id;
        this.entryType        = entryType;
        this.entryId          = entryId;
        this.invoiceNumber    = invoiceNumber;
        this.originalFilename = originalFilename;
        this.contentType      = contentType;
        this.fileSize         = fileSize;
        this.viewUrl          = viewUrl;
        this.uploadedAt       = uploadedAt;
    }

    // ── Getters & Setters ──────────────────────────────────────
    public Long getId()
    { return id; }
    public void          setId(Long v)
    { this.id = v; }
    public String getEntryType()
    { return entryType; }
    public void setEntryType(String v)
    { this.entryType = v; }
    public Long getEntryId()
    { return entryId; }
    public void setEntryId(Long v)
    { this.entryId = v; }
    public String getInvoiceNumber()
    { return invoiceNumber; }
    public void setInvoiceNumber(String v)
    { this.invoiceNumber = v; }
    public String getOriginalFilename()
    { return originalFilename; }
    public void setOriginalFilename(String v)
    { this.originalFilename = v; }
    public String getContentType()
    { return contentType; }
    public void  setContentType(String v)
    { this.contentType = v; }
    public Long getFileSize()
    { return fileSize; }
    public void setFileSize(Long v)
    { this.fileSize = v; }
    public String  getViewUrl()
    { return viewUrl; }
    public void setViewUrl(String v)
    { this.viewUrl = v; }
    public LocalDateTime getUploadedAt()
    { return uploadedAt; }
    public void          setUploadedAt(LocalDateTime v)
    { this.uploadedAt = v; }
}