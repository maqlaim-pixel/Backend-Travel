package com.travelvista.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "admin_audit_logs")
public class AdminAuditLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "admin_id", nullable = false)
    private Long adminId;

    @Column(name = "admin_email", nullable = false, length = 150)
    private String adminEmail;

    @Column(name = "action", nullable = false, length = 50)
    private String action; // "delete_lead", "delete_enquiry"

    @Column(name = "record_id", nullable = false)
    private Long recordId;

    @Column(name = "record_type", nullable = false, length = 20)
    private String recordType; // "lead" or "enquiry"

    @Column(name = "record_data", columnDefinition = "TEXT")
    private String recordData; // JSON snapshot of deleted record

    @Column(name = "reason", columnDefinition = "TEXT")
    private String reason;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public AdminAuditLog() {}

    public AdminAuditLog(Long adminId, String adminEmail, String action, Long recordId, String recordType, String recordData, String reason) {
        this.adminId = adminId;
        this.adminEmail = adminEmail;
        this.action = action;
        this.recordId = recordId;
        this.recordType = recordType;
        this.recordData = recordData;
        this.reason = reason;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getAdminId() { return adminId; }
    public void setAdminId(Long adminId) { this.adminId = adminId; }
    public String getAdminEmail() { return adminEmail; }
    public void setAdminEmail(String adminEmail) { this.adminEmail = adminEmail; }
    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
    public Long getRecordId() { return recordId; }
    public void setRecordId(Long recordId) { this.recordId = recordId; }
    public String getRecordType() { return recordType; }
    public void setRecordType(String recordType) { this.recordType = recordType; }
    public String getRecordData() { return recordData; }
    public void setRecordData(String recordData) { this.recordData = recordData; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
