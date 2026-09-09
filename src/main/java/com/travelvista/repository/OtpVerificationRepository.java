package com.travelvista.repository;

import com.travelvista.model.OtpVerification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface OtpVerificationRepository extends JpaRepository<OtpVerification, Long> {

    // ── Existing lookups (used by enquiry/lead OTP flows — do not break) ──

    Optional<OtpVerification> findTopByEmailAndPurposeAndRecordTypeAndRecordIdOrderByCreatedAtDesc(
            String email, String purpose, String recordType, Long recordId);

    List<OtpVerification> findByEmailAndPurposeAndRecordTypeAndRecordIdAndVerifiedFalse(
            String email, String purpose, String recordType, Long recordId);

    @Modifying
    @Transactional
    @Query("DELETE FROM OtpVerification o WHERE o.expiresAt < :now")
    void deleteExpired(LocalDateTime now);

    @Query("SELECT COUNT(o) FROM OtpVerification o WHERE o.email = :email AND o.createdAt > :since")
    long countRecentByEmail(String email, LocalDateTime since);

    // ── New lookups for REGISTER / LOGIN / BOOKING OTP flows ──
    // Auth OTPs use sentinel values: recordType = "account", recordId = 0.
    // This keeps the existing NOT NULL constraints intact (no DB migration
    // needed) while all queries above and below keep working unchanged.

    /**
     * Latest successful LOGIN OTP verification timestamp — used to enforce the
     * 24-hour re-OTP rule without adding a new table.
     */
    @Query("SELECT MAX(o.verifiedAt) FROM OtpVerification o " +
           "WHERE o.email = :email AND o.purpose = 'LOGIN' AND o.verified = true")
    LocalDateTime findLastLoginVerifiedAt(@Param("email") String email);

    /**
     * Code-specific lookup of an UNUSED OTP — enables strict single-use
     * verification for REGISTER / LOGIN / BOOKING: only the exact code of an
     * unverified record can succeed, so an old or already-used code can never
     * verify even after newer OTPs have been issued.
     */
    Optional<OtpVerification> findByEmailAndPurposeAndRecordTypeAndRecordIdAndCodeAndVerifiedFalse(
            String email, String purpose, String recordType, Long recordId, String code);

    /**
     * Invalidate pending (unverified) OTPs of a purpose for the SAME record
     * scope only, so a stale code can never verify after a newer one is sent:
     * - AUTH flows (REGISTER/LOGIN): recordType="account", recordId=0
     * - BOOKING flow: recordType="booking", recordId=<booking id>
     * Legacy enquiry/lead OTPs are never touched because their purpose strings
     * are "edit"/"delete" with different record scopes.
     * (Marking verified=true reuses the existing "used" convention.)
     */
    @Modifying
    @Transactional
    @Query("UPDATE OtpVerification o SET o.verified = true " +
           "WHERE o.email = :email AND o.purpose = :purpose " +
           "AND o.recordType = :recordType AND o.recordId = :recordId AND o.verified = false")
    void invalidatePending(@Param("email") String email, @Param("purpose") String purpose,
                           @Param("recordType") String recordType, @Param("recordId") Long recordId);
}
