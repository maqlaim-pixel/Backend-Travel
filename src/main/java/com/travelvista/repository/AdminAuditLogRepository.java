package com.travelvista.repository;

import com.travelvista.model.AdminAuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AdminAuditLogRepository extends JpaRepository<AdminAuditLog, Long> {
    List<AdminAuditLog> findAllByOrderByCreatedAtDesc();
    List<AdminAuditLog> findByRecordTypeAndRecordIdOrderByCreatedAtDesc(String recordType, Long recordId);
    List<AdminAuditLog> findByAdminIdOrderByCreatedAtDesc(Long adminId);
}
