package com.travelvista.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "edit_sessions")
public class EditSession {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "record_id", nullable = false)
    private Long recordId;

    @Column(name = "record_type", nullable = false, length = 20)
    private String recordType; // "enquiry" or "lead"

    @Column(name = "purpose", nullable = false, length = 50)
    private String purpose; // "edit", "delete"

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public EditSession() {}

    public EditSession(Long userId, Long recordId, String recordType, String purpose, LocalDateTime expiresAt) {
        this.userId = userId;
        this.recordId = recordId;
        this.recordType = recordType;
        this.purpose = purpose;
        this.expiresAt = expiresAt;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public Long getRecordId() { return recordId; }
    public void setRecordId(Long recordId) { this.recordId = recordId; }
    public String getRecordType() { return recordType; }
    public void setRecordType(String recordType) { this.recordType = recordType; }
    public String getPurpose() { return purpose; }
    public void setPurpose(String purpose) { this.purpose = purpose; }
    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }
    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(this.expiresAt);
    }

    public long getRemainingMinutes() {
        long minutes = java.time.Duration.between(LocalDateTime.now(), this.expiresAt).toMinutes();
        return Math.max(0, minutes);
    }

    public String getRemainingTimeFormatted() {
        long totalMinutes = getRemainingMinutes();
        long hours = totalMinutes / 60;
        long minutes = totalMinutes % 60;
        if (hours > 0) return hours + "h " + minutes + "m remaining";
        return minutes + "m remaining";
    }
}
