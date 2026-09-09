package com.travelvista.service;

import com.travelvista.model.EditSession;
import com.travelvista.repository.EditSessionRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class EditSessionService {

    private final EditSessionRepository sessionRepo;

    private static final int EDIT_WINDOW_HOURS = 3;

    public EditSessionService(EditSessionRepository sessionRepo) {
        this.sessionRepo = sessionRepo;
    }

    /**
     * Create or extend a 3-hour edit session after OTP verification.
     */
    public EditSession createSession(Long userId, Long recordId, String recordType, String purpose) {
        // Check if there's an existing active session
        Optional<EditSession> existing = sessionRepo.findTopByUserIdAndRecordIdAndRecordTypeAndPurposeAndIsActiveTrueOrderByCreatedAtDesc(
                userId, recordId, recordType, purpose);

        if (existing.isPresent() && !existing.get().isExpired()) {
            // Extend the existing session
            EditSession session = existing.get();
            session.setExpiresAt(LocalDateTime.now().plusHours(EDIT_WINDOW_HOURS));
            sessionRepo.save(session);
            return session;
        }

        // Create new session
        EditSession session = new EditSession(
                userId, recordId, recordType, purpose,
                LocalDateTime.now().plusHours(EDIT_WINDOW_HOURS)
        );
        return sessionRepo.save(session);
    }

    /**
     * Check if a user has an active edit session for a record.
     */
    public boolean hasActiveSession(Long userId, Long recordId, String recordType, String purpose) {
        Optional<EditSession> session = sessionRepo.findTopByUserIdAndRecordIdAndRecordTypeAndPurposeAndIsActiveTrueOrderByCreatedAtDesc(
                userId, recordId, recordType, purpose);
        return session.isPresent() && !session.get().isExpired();
    }

    /**
     * Get the active edit session for a record.
     */
    public Optional<EditSession> getActiveSession(Long userId, Long recordId, String recordType, String purpose) {
        Optional<EditSession> session = sessionRepo.findTopByUserIdAndRecordIdAndRecordTypeAndPurposeAndIsActiveTrueOrderByCreatedAtDesc(
                userId, recordId, recordType, purpose);
        if (session.isPresent() && !session.get().isExpired()) {
            return session;
        }
        return Optional.empty();
    }

    /**
     * Invalidate (close) a session after the action is completed.
     */
    public void invalidateSession(Long userId, Long recordId, String recordType, String purpose) {
        sessionRepo.findTopByUserIdAndRecordIdAndRecordTypeAndPurposeAndIsActiveTrueOrderByCreatedAtDesc(
                userId, recordId, recordType, purpose
        ).ifPresent(session -> {
            session.setIsActive(false);
            sessionRepo.save(session);
        });
    }

    /**
     * Deactivate expired sessions (can be called by a scheduled task).
     */
    public void deactivateExpiredSessions() {
        var expired = sessionRepo.findExpiredActiveSessions(LocalDateTime.now());
        for (EditSession session : expired) {
            session.setIsActive(false);
            sessionRepo.save(session);
        }
    }
}
