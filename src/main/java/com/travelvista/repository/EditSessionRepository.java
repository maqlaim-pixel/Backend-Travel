package com.travelvista.repository;

import com.travelvista.model.EditSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.Optional;

public interface EditSessionRepository extends JpaRepository<EditSession, Long> {

    Optional<EditSession> findTopByUserIdAndRecordIdAndRecordTypeAndPurposeAndIsActiveTrueOrderByCreatedAtDesc(
            Long userId, Long recordId, String recordType, String purpose);

    @Query("SELECT e FROM EditSession e WHERE e.isActive = true AND e.expiresAt < :now")
    java.util.List<EditSession> findExpiredActiveSessions(LocalDateTime now);
}
