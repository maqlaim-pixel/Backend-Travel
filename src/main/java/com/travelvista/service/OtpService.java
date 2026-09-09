package com.travelvista.service;

import com.travelvista.model.OtpVerification;
import com.travelvista.repository.OtpVerificationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class OtpService {

    private static final Logger log = LoggerFactory.getLogger(OtpService.class);

    // Sentinel values for auth OTPs (REGISTER / LOGIN / BOOKING).
    // Keeps the existing record_id / record_type NOT NULL columns untouched —
    // no DB migration needed.
    public static final String RECORD_TYPE_ACCOUNT = "account";
    public static final long RECORD_ID_NONE = 0L;

    private final OtpVerificationRepository otpRepo;
    private final JavaMailSender mailSender;

    @Value("${spring.mail.username:}")
    private String mailFrom;

    private static final int OTP_LENGTH = 6;
    private static final int OTP_EXPIRY_MINUTES = 10;
    private static final int MAX_OTPS_PER_HOUR = 10;

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    public OtpService(
            OtpVerificationRepository otpRepo,
            JavaMailSender mailSender
    ) {
        this.otpRepo = otpRepo;
        this.mailSender = mailSender;
    }

    /**
     * Generate + save + send OTP.
     *
     * Supported purposes:
     * REGISTER, LOGIN, BOOKING,
     * EDIT_ENQUIRY, DELETE_ENQUIRY, EDIT_LEAD, DELETE_LEAD
     * (also legacy lowercase "edit"/"delete" used by the enquiry/lead controllers)
     */
    public OtpVerification generateAndSendOtp(
            String email,
            String purpose,
            Long recordId,
            String recordType
    ) {

        if (email == null || email.trim().isEmpty()) {
            throw new RuntimeException("Email is required");
        }

        if (purpose == null || purpose.trim().isEmpty()) {
            throw new RuntimeException("OTP purpose is required");
        }

        // Rate limiting: max 10 OTPs per hour per email across all purposes
        long recentCount = otpRepo.countRecentByEmail(
                email,
                LocalDateTime.now().minusHours(1)
        );

        if (recentCount >= MAX_OTPS_PER_HOUR) {
            throw new RuntimeException(
                    "Too many OTP requests. Please try again later."
            );
        }

        // Invalidate previous unverified OTPs of the same purpose + record scope
        // so a stale code can never be used after a newer one is issued.
        try {
            otpRepo.invalidatePending(email.trim(), purpose,
                    recordType == null ? RECORD_TYPE_ACCOUNT : recordType,
                    recordId == null ? RECORD_ID_NONE : recordId);
        } catch (Exception e) {
            log.warn("Could not invalidate previous pending OTPs for purpose {}: {}", purpose, e.getMessage());
        }

        // Generate OTP
        String code = generateCode();

        // Save OTP (expires in 10 minutes)
        OtpVerification otp = new OtpVerification(
                email.trim(),
                code,
                purpose,
                recordId == null ? RECORD_ID_NONE : recordId,
                recordType == null ? RECORD_TYPE_ACCOUNT : recordType,
                LocalDateTime.now().plusMinutes(OTP_EXPIRY_MINUTES)
        );

        otpRepo.save(otp);

        // Send email — throws OtpDeliveryException on failure so the caller can
        // return an accurate API error instead of pretending success.
        sendOtpEmail(email.trim(), code, purpose);

        return otp;
    }

    /**
     * Legacy enquiry/lead verification — unchanged semantics (a verified record
     * for the same email+purpose+record keeps returning true while unexpired).
     */
    public boolean verifyOtp(
            String email,
            String purpose,
            Long recordId,
            String recordType,
            String code
    ) {

        if (email == null || code == null || code.trim().isEmpty()) {
            return false;
        }

        Optional<OtpVerification> optOtp =
                otpRepo.findTopByEmailAndPurposeAndRecordTypeAndRecordIdOrderByCreatedAtDesc(
                        email,
                        purpose,
                        recordType,
                        recordId
                );

        if (optOtp.isEmpty()) {
            return false;
        }

        OtpVerification otp = optOtp.get();

        // Already verified
        if (otp.getVerified()) {
            return true;
        }

        // Max attempts
        if (otp.isMaxAttemptsExceeded()) {
            throw new RuntimeException(
                    "Too many failed attempts. Please request a new OTP."
            );
        }

        // Expired
        if (otp.isExpired()) {
            throw new RuntimeException("OTP has expired. Please request a new OTP.");
        }

        // Count attempt
        otp.setAttempts(otp.getAttempts() + 1);

        // Verify code
        if (otp.getCode().equals(code.trim())) {

            otp.setVerified(true);
            otp.setVerifiedAt(LocalDateTime.now());
            otpRepo.save(otp);

            return true;
        }

        otpRepo.save(otp);

        return false;
    }

    /**
     * Strict, single-use verification for REGISTER / LOGIN / BOOKING.
     *
     * Differences vs legacy verifyOtp:
     * - only an UNVERIFIED record whose code matches exactly can succeed,
     * - an already-used code can never verify again (single-use),
     * - a stale/invalid code consumes one attempt of the newest OTP record,
     * - successful verification stamps verifiedAt for the 24-hour login rule.
     */
    @Transactional
    public boolean verifyAuthOtp(String email, String purpose, String code) {
        return verifyAuthOtpScoped(email, purpose, RECORD_TYPE_ACCOUNT, RECORD_ID_NONE, code);
    }

    /**
     * Strict, single-use verification for BOOKING OTPs (record-scoped):
     * only an unverified OTP for THIS booking + email can succeed.
     * Returns true on success; throws on expired/attempt-exhausted; false otherwise.
     */
    @Transactional
    public boolean verifyBookingOtp(String email, Long bookingId, String code) {
        return verifyAuthOtpScoped(email, "BOOKING", "booking", bookingId, code);
    }

    /**
     * Send a fresh BOOKING OTP for the given booking (rate limiting handled in
     * generateAndSendOtp). Throws OtpDeliveryException on email failure.
     */
    public void sendBookingOtp(String email, Long bookingId) {
        generateAndSendOtp(email, "BOOKING", bookingId, "booking");
    }

    /**
     * Record-scoped strict single-use verification used by BOOKING.
     */
    private boolean verifyAuthOtpScoped(String email, String purpose, String recordType, Long recordId, String code) {

        if (email == null || code == null || code.trim().isEmpty()) {
            return false;
        }

        email = email.trim();
        code = code.trim();

        // 1) Exact single-use match for this record scope.
        Optional<OtpVerification> exact =
                otpRepo.findByEmailAndPurposeAndRecordTypeAndRecordIdAndCodeAndVerifiedFalse(
                        email, purpose, recordType, recordId, code
                );

        if (exact.isPresent()) {
            OtpVerification otp = exact.get();

            if (otp.isMaxAttemptsExceeded()) {
                throw new RuntimeException(
                        "Too many failed attempts. Please request a new OTP.");
            }

            if (otp.isExpired()) {
                throw new RuntimeException(
                        "OTP has expired. Please request a new OTP.");
            }

            otp.setVerified(true);
            otp.setVerifiedAt(LocalDateTime.now());
            otpRepo.save(otp);

            log.info("AUTH OTP verified: purpose={} email={} recordType={} recordId={} otpId={}",
                    purpose, email, recordType, recordId, otp.getId());
            return true;
        }

        // 2) No matching code — consume one attempt on the newest unverified OTP
        //    of this record scope so brute force is limited to 5 tries per OTP.
        Optional<OtpVerification> newest =
                otpRepo.findTopByEmailAndPurposeAndRecordTypeAndRecordIdOrderByCreatedAtDesc(
                        email, purpose, recordType, recordId
                );

        if (newest.isPresent()) {
            OtpVerification otp = newest.get();

            if (!otp.getVerified() && !otp.isExpired() && !otp.isMaxAttemptsExceeded()) {
                otp.setAttempts(otp.getAttempts() + 1);
                otpRepo.save(otp);

                if (otp.isMaxAttemptsExceeded()) {
                    throw new RuntimeException(
                            "Too many failed attempts. Please request a new OTP.");
                }
            }
        }

        log.warn("AUTH OTP verification failed: purpose={} email={} recordType={} recordId={}",
                purpose, email, recordType, recordId);
        return false;
    }

    /**
     * Timestamp of the last successful LOGIN OTP verification, or null.
     * Backs the "re-OTP every 24 hours" rule.
     */
    public LocalDateTime getLastLoginVerifiedAt(String email) {
        return otpRepo.findLastLoginVerifiedAt(email);
    }

    /**
     * True when a LOGIN OTP verification happened within the last 24 hours.
     */
    public boolean isLoginWithin24h(String email) {
        LocalDateTime last = getLastLoginVerifiedAt(email);
        return last != null && last.isAfter(LocalDateTime.now().minusHours(24));
    }

    /**
     * Invalidate all unused AUTH OTPs for a purpose (account scope).
     */
    public void invalidateAuthOtps(String email, String purpose) {
        otpRepo.invalidatePending(email, purpose, RECORD_TYPE_ACCOUNT, RECORD_ID_NONE);
    }

    /**
     * Check whether a valid verified OTP exists (legacy enquiry/lead flows —
     * signature and semantics unchanged).
     */
    public boolean hasVerifiedOtp(
            String email,
            String purpose,
            Long recordId,
            String recordType
    ) {

        Optional<OtpVerification> optOtp =
                otpRepo.findTopByEmailAndPurposeAndRecordTypeAndRecordIdOrderByCreatedAtDesc(
                        email, purpose, recordType, recordId
                );

        return optOtp.isPresent()
                && optOtp.get().getVerified()
                && !optOtp.get().isExpired();
    }

    /**
     * Mark OTP as used (legacy enquiry/lead flows).
     */
    public void invalidateOtps(
            String email,
            String purpose,
            Long recordId,
            String recordType
    ) {

        var otps =
                otpRepo.findByEmailAndPurposeAndRecordTypeAndRecordIdAndVerifiedFalse(
                        email,
                        purpose,
                        recordType,
                        recordId
                );

        for (OtpVerification otp : otps) {
            otp.setVerified(true);
            otpRepo.save(otp);
        }
    }

    /**
     * Generate 6 digit OTP using SecureRandom.
     */
    private String generateCode() {

        StringBuilder sb = new StringBuilder(OTP_LENGTH);

        for (int i = 0; i < OTP_LENGTH; i++) {
            sb.append(SECURE_RANDOM.nextInt(10));
        }

        return sb.toString();
    }

    /**
     * Send OTP email. NEVER logs the OTP, the password, or credentials.
     * On any failure: logs the full underlying exception server-side and throws
     * OtpDeliveryException so the API returns an accurate error instead of a
     * false success.
     */
    private void sendOtpEmail(
            String to,
            String code,
            String purpose
    ) {

        long start = System.currentTimeMillis();

        try {

            SimpleMailMessage message = new SimpleMailMessage();

            if (mailFrom != null && !mailFrom.trim().isEmpty()) {
                message.setFrom(mailFrom);
            } else {
                message.setFrom("noreply@travelvista.com");
            }

            message.setTo(to);

            message.setSubject(getSubject(purpose));

            message.setText(getEmailBody(to, code, purpose));

            log.info("[MAIL] OTP send attempt: purpose={} recipient={} mailFrom={}",
                    purpose, to, mailFrom != null && !mailFrom.trim().isEmpty() ? mailFrom : "default");

            mailSender.send(message);

            log.info("[MAIL] OTP email sent OK in {} ms: purpose={} recipient={}",
                    System.currentTimeMillis() - start, purpose, to);

        } catch (Exception e) {

            // Log the real root cause (e.g. Gmail SMTP 535 auth failure) —
            // this is what makes Railway debugging possible.
            log.error("[MAIL] OTP EMAIL SEND FAILED: purpose={} recipient={}", purpose, to, e);

            if (e.getCause() != null) {
                log.error("[MAIL] Root cause: {}", String.valueOf(e.getCause()));
            }

            throw new OtpDeliveryException(
                    "Unable to send OTP email. Please try again later.", e
            );
        }
    }

    /**
     * Email subject according to purpose.
     */
    private String getSubject(String purpose) {

        if (purpose == null) {
            return "TravelVista Verification Code";
        }

        switch (purpose.toUpperCase()) {

            case "REGISTER":
                return "TravelVista Registration OTP";

            case "LOGIN":
                return "TravelVista Login OTP";

            case "BOOKING":
                return "TravelVista Booking Verification OTP";

            case "EDIT_ENQUIRY":
                return "TravelVista Enquiry Edit OTP";

            case "DELETE_ENQUIRY":
                return "TravelVista Enquiry Delete OTP";

            case "EDIT_LEAD":
                return "TravelVista Lead Edit OTP";

            case "DELETE_LEAD":
                return "TravelVista Lead Delete OTP";

            // Legacy purposes used by EnquiryController / LeadController
            case "edit":
            case "delete":
                return "TravelVista Verification Code";

            default:
                return "TravelVista Verification Code";
        }
    }

    /**
     * Email body according to purpose.
     */
    private String getEmailBody(
            String to,
            String code,
            String purpose
    ) {

        String action;

        if (purpose == null) {
            action = "verify your account";
        } else {

            switch (purpose.toUpperCase()) {

                case "REGISTER":
                    action = "complete your registration";
                    break;

                case "LOGIN":
                    action = "complete your login";
                    break;

                case "BOOKING":
                    action = "confirm your booking";
                    break;

                case "EDIT_ENQUIRY":
                    action = "edit your enquiry";
                    break;

                case "DELETE_ENQUIRY":
                    action = "delete your enquiry";
                    break;

                case "EDIT_LEAD":
                    action = "edit your lead";
                    break;

                case "DELETE_LEAD":
                    action = "delete your lead";
                    break;

                // Legacy purposes
                case "edit":
                case "delete":
                    action = "edit or delete your enquiry/lead";
                    break;

                default:
                    action = "verify your request";
            }
        }

        return
                "Hello,\n\n" +
                "Your TravelVista verification code is:\n\n" +
                "    " + code + "\n\n" +
                "Use this OTP to " + action + ".\n\n" +
                "This code will expire in "
                + OTP_EXPIRY_MINUTES
                + " minutes.\n\n" +
                "If you did not request this code, please ignore this email.\n\n" +
                "Regards,\n" +
                "TravelVista Team";
    }
}
