package com.travelvista.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.travelvista.model.*;
import com.travelvista.repository.*;
import com.travelvista.service.EditSessionService;
import com.travelvista.service.OtpDeliveryException;
import com.travelvista.service.OtpService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@RestController
@RequestMapping("/api")
public class EnquiryController {

    private final UserRepository userRepository;
    private final EnquiryRepository enquiryRepository;
    private final BookingRepository bookingRepository;
    private final PackageRepository packageRepository;
    private final OtpService otpService;
    private final EditSessionService editSessionService;
    private final AdminAuditLogRepository auditLogRepository;
    private final ObjectMapper objectMapper;

    public EnquiryController(UserRepository userRepository, EnquiryRepository enquiryRepository,
                             BookingRepository bookingRepository, PackageRepository packageRepository,
                             OtpService otpService, EditSessionService editSessionService,
                             AdminAuditLogRepository auditLogRepository, ObjectMapper objectMapper) {
        this.userRepository = userRepository;
        this.enquiryRepository = enquiryRepository;
        this.bookingRepository = bookingRepository;
        this.packageRepository = packageRepository;
        this.otpService = otpService;
        this.editSessionService = editSessionService;
        this.auditLogRepository = auditLogRepository;
        this.objectMapper = objectMapper;
    }

    private User getUser(Authentication auth) {
        if (auth == null || auth.getPrincipal() == null) return null;
        Object principal = auth.getPrincipal();
        if (principal instanceof User) return (User) principal;
        if (auth.getName() != null) return userRepository.findByEmail(auth.getName()).orElse(null);
        return null;
    }

    private User getAdmin(Authentication auth) {
        User user = getUser(auth);
        if (user == null) return null;
        // Check via DB role — match super_admin, admin, editor, or sales
        if (user.getRole() != null) {
            String roleName = user.getRole().getName();
            if ("super_admin".equalsIgnoreCase(roleName)
                    || "admin".equalsIgnoreCase(roleName)
                    || "editor".equalsIgnoreCase(roleName)
                    || "sales".equalsIgnoreCase(roleName)) {
                return user;
            }
        }
        // Check via JWT authority (fallback)
        if (auth.getAuthorities() != null) {
            boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> {
                    String authority = a.getAuthority();
                    return authority.equalsIgnoreCase("ROLE_super_admin")
                            || authority.equalsIgnoreCase("ROLE_admin")
                            || authority.equalsIgnoreCase("ROLE_editor")
                            || authority.equalsIgnoreCase("ROLE_sales");
                });
            if (isAdmin) return user;
        }
        return null;
    }

    private String generateRef() {
        long count = enquiryRepository.countAll();
        String ref;
        int retries = 0;
        do {
            count++;
            ref = "TV-ENQ-" + String.format("%06d", count);
            retries++;
            if (retries > 50) break;
        } while (enquiryRepository.existsByEnquiryRef(ref));
        return ref;
    }

    private String generateBookingRef() {
        long num = bookingRepository.countAll() + 1001;
        return "TV-BKG-" + String.format("%06d", num);
    }

    // ════════════════════════════════════════════════════════════
    // CUSTOMER ENDPOINTS
    // ════════════════════════════════════════════════════════════

    // Submit enquiry
    @PostMapping("/customer/enquiries")
    public ResponseEntity<?> submitEnquiry(Authentication auth, @RequestBody Map<String, Object> body) {
        User user = getUser(auth);
        if (user == null) return ResponseEntity.status(401).body(Map.of("error", "Not authenticated"));

        Long packageId = body.get("packageId") != null ? Long.valueOf(body.get("packageId").toString()) : null;
        String travelDateStr = body.get("travelDate") != null ? body.get("travelDate").toString() : null;
        Integer travelers = body.get("travelers") != null ? Integer.valueOf(body.get("travelers").toString()) : 1;
        String budget = body.get("budget") != null ? body.get("budget").toString() : null;
        String message = body.get("message") != null ? body.get("message").toString() : "";

        if (packageId == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Package ID is required"));
        }

        Optional<TravelPackage> pkgOpt = packageRepository.findById(packageId);
        if (pkgOpt.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Package not found"));
        }

        TravelPackage pkg = pkgOpt.get();

        Enquiry enquiry = new Enquiry();
        enquiry.setEnquiryRef(generateRef());
        enquiry.setUser(user);
        enquiry.setTravelPackage(pkg);
        enquiry.setPackageTitle(pkg.getTitle());
        enquiry.setDestination(pkg.getDestination());
        enquiry.setTravelers(travelers);
        enquiry.setBudget(budget);
        enquiry.setMessage(message);
        enquiry.setStatus("pending");

        if (travelDateStr != null && !travelDateStr.isEmpty()) {
            try {
                enquiry.setTravelDate(LocalDate.parse(travelDateStr));
            } catch (Exception ignored) {}
        }

        enquiryRepository.save(enquiry);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("success", true);
        response.put("enquiryRef", enquiry.getEnquiryRef());
        response.put("message", "Enquiry submitted successfully");
        response.put("packageName", pkg.getTitle());
        response.put("destination", pkg.getDestination());

        return ResponseEntity.ok(response);
    }

    // Get customer's enquiries
    @GetMapping("/customer/enquiries")
    public ResponseEntity<?> myEnquiries(Authentication auth) {
        User user = getUser(auth);
        if (user == null) return ResponseEntity.status(401).body(Map.of("error", "Not authenticated"));

        List<Enquiry> enquiries = enquiryRepository.findByUserIdOrderByCreatedAtDesc(user.getId());
        List<Map<String, Object>> list = new ArrayList<>();
        for (Enquiry e : enquiries) {
            Map<String, Object> map = enquirySummary(e);
            // Add edit session info
            var editSession = editSessionService.getActiveSession(user.getId(), e.getId(), "enquiry", "edit");
            var deleteSession = editSessionService.getActiveSession(user.getId(), e.getId(), "enquiry", "delete");
            map.put("canEdit", editSession.isPresent());
            map.put("canDelete", deleteSession.isPresent());
            map.put("editExpiresAt", editSession.map(s -> s.getExpiresAt().toString()).orElse(null));
            map.put("deleteExpiresAt", deleteSession.map(s -> s.getExpiresAt().toString()).orElse(null));
            map.put("editRemainingTime", editSession.map(EditSession::getRemainingTimeFormatted).orElse(null));
            list.add(map);
        }
        return ResponseEntity.ok(list);
    }

    // Get customer dashboard stats
    @GetMapping("/customer/enquiries/stats")
    public ResponseEntity<?> enquiryStats(Authentication auth) {
        User user = getUser(auth);
        if (user == null) return ResponseEntity.status(401).body(Map.of("error", "Not authenticated"));

        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("total", enquiryRepository.countByUserId(user.getId()));
        stats.put("pending", enquiryRepository.countByUserIdAndStatus(user.getId(), "pending"));
        stats.put("contacted", enquiryRepository.countByUserIdAndStatus(user.getId(), "contacted"));
        stats.put("confirmed", enquiryRepository.countByUserIdAndStatus(user.getId(), "confirmed"));
        return ResponseEntity.ok(stats);
    }

    // ════════════════════════════════════════════════════════════
    // OTP ENDPOINTS
    // ════════════════════════════════════════════════════════════

    // Send OTP for edit/delete
    @PostMapping("/customer/enquiries/{id}/otp")
    public ResponseEntity<?> sendOtp(Authentication auth, @PathVariable Long id, @RequestBody Map<String, String> body) {
        User user = getUser(auth);
        if (user == null) return ResponseEntity.status(401).body(Map.of("error", "Not authenticated"));

        Optional<Enquiry> opt = enquiryRepository.findById(id);
        if (opt.isEmpty()) return ResponseEntity.status(404).body(Map.of("error", "Enquiry not found"));

        Enquiry enquiry = opt.get();

        // Verify ownership
        if (enquiry.getUser() == null || !enquiry.getUser().getId().equals(user.getId())) {
            return ResponseEntity.status(403).body(Map.of("error", "You can only manage your own enquiries"));
        }

        String purpose = body.getOrDefault("purpose", "edit");
        if (!"edit".equals(purpose) && !"delete".equals(purpose)) {
            return ResponseEntity.badRequest().body(Map.of("error", "Purpose must be 'edit' or 'delete'"));
        }

        String email = user.getEmail();
        if (email == null || email.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "No email address on file"));
        }

        try {
            OtpVerification otp = otpService.generateAndSendOtp(email, purpose, id, "enquiry");
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "OTP sent to " + email,
                    "expiresIn", "10 minutes",
                    "purpose", purpose
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // Verify OTP and create edit session
    @PostMapping("/customer/enquiries/{id}/verify-otp")
    public ResponseEntity<?> verifyOtp(Authentication auth, @PathVariable Long id, @RequestBody Map<String, String> body) {
        User user = getUser(auth);
        if (user == null) return ResponseEntity.status(401).body(Map.of("error", "Not authenticated"));

        Optional<Enquiry> opt = enquiryRepository.findById(id);
        if (opt.isEmpty()) return ResponseEntity.status(404).body(Map.of("error", "Enquiry not found"));

        Enquiry enquiry = opt.get();
        if (enquiry.getUser() == null || !enquiry.getUser().getId().equals(user.getId())) {
            return ResponseEntity.status(403).body(Map.of("error", "You can only manage your own enquiries"));
        }

        String purpose = body.getOrDefault("purpose", "edit");
        String code = body.get("code");
        if (code == null || code.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "OTP code is required"));
        }

        try {
            boolean verified = otpService.verifyOtp(user.getEmail(), purpose, id, "enquiry", code);
            if (verified) {
                // Create 3-hour edit session
                EditSession session = editSessionService.createSession(user.getId(), id, "enquiry", purpose);
                return ResponseEntity.ok(Map.of(
                        "success", true,
                        "message", "Verification successful! You have 3 hours to " + purpose + " this enquiry.",
                        "expiresAt", session.getExpiresAt().toString(),
                        "remainingTime", session.getRemainingTimeFormatted(),
                        "purpose", purpose
                ));
            } else {
                return ResponseEntity.badRequest().body(Map.of("error", "Invalid OTP code. Please try again."));
            }
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // Check edit session status
    @GetMapping("/customer/enquiries/{id}/edit-status")
    public ResponseEntity<?> getEditStatus(Authentication auth, @PathVariable Long id) {
        User user = getUser(auth);
        if (user == null) return ResponseEntity.status(401).body(Map.of("error", "Not authenticated"));

        var editSession = editSessionService.getActiveSession(user.getId(), id, "enquiry", "edit");
        var deleteSession = editSessionService.getActiveSession(user.getId(), id, "enquiry", "delete");

        Map<String, Object> status = new LinkedHashMap<>();
        status.put("canEdit", editSession.isPresent());
        status.put("canDelete", deleteSession.isPresent());
        editSession.ifPresent(s -> {
            status.put("editExpiresAt", s.getExpiresAt().toString());
            status.put("editRemainingTime", s.getRemainingTimeFormatted());
        });
        deleteSession.ifPresent(s -> {
            status.put("deleteExpiresAt", s.getExpiresAt().toString());
            status.put("deleteRemainingTime", s.getRemainingTimeFormatted());
        });

        return ResponseEntity.ok(status);
    }

    // Edit enquiry (requires active edit session)
    @PutMapping("/customer/enquiries/{id}")
    public ResponseEntity<?> updateEnquiry(Authentication auth, @PathVariable Long id, @RequestBody Map<String, Object> body) {
        User user = getUser(auth);
        if (user == null) return ResponseEntity.status(401).body(Map.of("error", "Not authenticated"));

        // Check edit session
        if (!editSessionService.hasActiveSession(user.getId(), id, "enquiry", "edit")) {
            return ResponseEntity.status(403).body(Map.of(
                    "error", "Edit access expired. Please verify OTP again.",
                    "code", "EDIT_EXPIRED"
            ));
        }

        Optional<Enquiry> opt = enquiryRepository.findById(id);
        if (opt.isEmpty()) return ResponseEntity.status(404).body(Map.of("error", "Enquiry not found"));

        Enquiry enquiry = opt.get();
        if (enquiry.getUser() == null || !enquiry.getUser().getId().equals(user.getId())) {
            return ResponseEntity.status(403).body(Map.of("error", "You can only edit your own enquiries"));
        }

        // Update allowed fields
        if (body.containsKey("travelDate")) {
            String dateStr = body.get("travelDate") != null ? body.get("travelDate").toString() : null;
            if (dateStr != null && !dateStr.isEmpty()) {
                try {
                    enquiry.setTravelDate(LocalDate.parse(dateStr));
                } catch (Exception ignored) {}
            } else {
                enquiry.setTravelDate(null);
            }
        }
        if (body.containsKey("travelers")) {
            enquiry.setTravelers(Integer.valueOf(body.get("travelers").toString()));
        }
        if (body.containsKey("budget")) {
            enquiry.setBudget(body.get("budget") != null ? body.get("budget").toString() : null);
        }
        if (body.containsKey("message")) {
            enquiry.setMessage(body.get("message") != null ? body.get("message").toString() : "");
        }

        enquiry.setUpdatedAt(LocalDateTime.now());
        enquiryRepository.save(enquiry);

        return ResponseEntity.ok(Map.of("success", true, "message", "Enquiry updated successfully"));
    }

    // Delete enquiry (requires active delete session)
    @DeleteMapping("/customer/enquiries/{id}")
    public ResponseEntity<?> deleteEnquiry(Authentication auth, @PathVariable Long id) {
        User user = getUser(auth);
        if (user == null) return ResponseEntity.status(401).body(Map.of("error", "Not authenticated"));

        // Check delete session
        if (!editSessionService.hasActiveSession(user.getId(), id, "enquiry", "delete")) {
            return ResponseEntity.status(403).body(Map.of(
                    "error", "Delete access expired. Please verify OTP again.",
                    "code", "DELETE_EXPIRED"
            ));
        }

        Optional<Enquiry> opt = enquiryRepository.findById(id);
        if (opt.isEmpty()) return ResponseEntity.status(404).body(Map.of("error", "Enquiry not found"));

        Enquiry enquiry = opt.get();
        if (enquiry.getUser() == null || !enquiry.getUser().getId().equals(user.getId())) {
            return ResponseEntity.status(403).body(Map.of("error", "You can only delete your own enquiries"));
        }

        enquiryRepository.deleteById(id);
        editSessionService.invalidateSession(user.getId(), id, "enquiry", "delete");

        return ResponseEntity.ok(Map.of("success", true, "message", "Enquiry deleted successfully"));
    }

    // ════════════════════════════════════════════════════════════
    // ADMIN ENDPOINTS
    // ════════════════════════════════════════════════════════════

    // List all enquiries
    @GetMapping("/admin/enquiries")
    public ResponseEntity<?> allEnquiries() {
        List<Enquiry> enquiries = enquiryRepository.findAllByOrderByCreatedAtDesc();
        List<Map<String, Object>> list = new ArrayList<>();
        for (Enquiry e : enquiries) {
            Map<String, Object> map = enquirySummary(e);
            if (e.getUser() != null) {
                map.put("userName", e.getUser().getName());
                map.put("userEmail", e.getUser().getEmail());
                map.put("userPhone", e.getUser().getPhone());
                map.put("userId", e.getUser().getId());
            }
            list.add(map);
        }
        return ResponseEntity.ok(list);
    }

    // Admin enquiry stats
    @GetMapping("/admin/enquiries/stats")
    public ResponseEntity<?> adminEnquiryStats() {
        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("total", enquiryRepository.countAll());
        stats.put("pending", enquiryRepository.countPending());
        stats.put("confirmed", enquiryRepository.countConfirmed());
        stats.put("contacted", enquiryRepository.countByStatus("contacted"));
        stats.put("rejected", enquiryRepository.countByStatus("rejected"));
        return ResponseEntity.ok(stats);
    }

    // Update enquiry status
    @PutMapping("/admin/enquiries/{id}")
    public ResponseEntity<?> adminUpdateEnquiry(Authentication auth, @PathVariable Long id, @RequestBody Map<String, Object> body) {
        Optional<Enquiry> opt = enquiryRepository.findById(id);
        if (opt.isEmpty()) return ResponseEntity.status(404).body(Map.of("error", "Enquiry not found"));

        Enquiry enquiry = opt.get();
        String newStatus = body.get("status") != null ? body.get("status").toString() : enquiry.getStatus();
        String adminNotes = body.get("adminNotes") != null ? body.get("adminNotes").toString() : null;

        enquiry.setStatus(newStatus);
        if (adminNotes != null) enquiry.setAdminNotes(adminNotes);
        enquiry.setUpdatedAt(LocalDateTime.now());

        if ("confirmed".equals(newStatus)) {
            enquiry.setConfirmedAt(LocalDateTime.now());
            try {
                createBookingFromEnquiry(enquiry);
            } catch (OtpDeliveryException e) {
                // Booking OTP email failed — do NOT confirm the enquiry.
                // Nothing was persisted (save() below is not reached), so the
                // admin can simply retry once mail is fixed.
                return ResponseEntity.status(502).body(Map.of(
                        "error", "Booking was created but the confirmation OTP email could not be sent. Please fix SMTP and retry.",
                        "emailFailed", true));
            }
        }

        enquiryRepository.save(enquiry);
        return ResponseEntity.ok(Map.of("success", true, "message", "Enquiry updated", "status", newStatus));
    }

    // Admin delete enquiry with audit trail
    @DeleteMapping("/admin/enquiries/{id}")
    public ResponseEntity<?> adminDeleteEnquiry(Authentication auth, @PathVariable Long id, @RequestBody(required = false) Map<String, String> body) {
        User admin = getAdmin(auth);
        if (admin == null) return ResponseEntity.status(403).body(Map.of("error", "Admin access required"));

        Optional<Enquiry> opt = enquiryRepository.findById(id);
        if (opt.isEmpty()) return ResponseEntity.status(404).body(Map.of("error", "Enquiry not found"));

        Enquiry enquiry = opt.get();
        String reason = body != null ? body.getOrDefault("reason", "No reason provided") : "No reason provided";

        // Create audit log before deletion
        try {
            String recordData = objectMapper.writeValueAsString(enquirySummary(enquiry));
            AdminAuditLog auditLog = new AdminAuditLog(
                    admin.getId(), admin.getEmail(), "delete_enquiry",
                    id, "enquiry", recordData, reason
            );
            auditLogRepository.save(auditLog);
        } catch (Exception e) {
            System.err.println("Failed to create audit log: " + e.getMessage());
        }

        // Invalidate any active edit sessions for this enquiry
        if (enquiry.getUser() != null) {
            editSessionService.invalidateSession(enquiry.getUser().getId(), id, "enquiry", "edit");
            editSessionService.invalidateSession(enquiry.getUser().getId(), id, "enquiry", "delete");
        }

        enquiryRepository.deleteById(id);

        return ResponseEntity.ok(Map.of("success", true, "message", "Enquiry deleted by admin"));
    }

    // ════════════════════════════════════════════════════════════
    // HELPERS
    // ════════════════════════════════════════════════════════════

    /**
     * Creates the booking in PENDING_CONFIRMATION state and sends a BOOKING OTP
     * to the customer's email. The booking becomes "confirmed" ONLY after the
     * customer verifies the OTP (CustomerController.verifyBookingOtp).
     * An email failure propagates OtpDeliveryException so the admin sees the
     * API error instead of a false success; the pending booking is rolled back.
     */
    private void createBookingFromEnquiry(Enquiry enquiry) {
        boolean exists = bookingRepository.findByUserIdOrderByCreatedAtDesc(enquiry.getUser().getId())
            .stream().anyMatch(b -> b.getPackageTitle() != null && b.getPackageTitle().equals(enquiry.getPackageTitle())
                && ("confirmed".equals(b.getStatus()) || "pending_confirmation".equals(b.getStatus())));
        if (exists) return;

        Booking booking = new Booking();
        booking.setBookingRef(generateBookingRef());
        booking.setUser(enquiry.getUser());
        booking.setTravelPackage(enquiry.getTravelPackage());
        booking.setPackageTitle(enquiry.getPackageTitle());
        booking.setTravelDate(enquiry.getTravelDate());
        booking.setTravelers(enquiry.getTravelers());
        // NOT permanently confirmed until BOOKING OTP is verified.
        booking.setStatus("pending_confirmation");
        booking.setPaymentStatus("unpaid");

        if (enquiry.getTravelPackage() != null && enquiry.getTravelPackage().getStartingPrice() != null) {
            booking.setTotalAmount(enquiry.getTravelPackage().getStartingPrice()
                .multiply(BigDecimal.valueOf(enquiry.getTravelers())));
        }

        if (enquiry.getTravelPackage() != null && enquiry.getTravelDate() != null) {
            int nights = enquiry.getTravelPackage().getDurationNights() != null ? enquiry.getTravelPackage().getDurationNights() : 0;
            booking.setEndDate(enquiry.getTravelDate().plusDays(nights));
        }

        Booking saved = bookingRepository.save(booking);

        // Send the BOOKING OTP. On failure, remove the pending booking so the
        // admin can simply retry the confirmation — no orphan pending rows.
        try {
            otpService.sendBookingOtp(enquiry.getUser().getEmail(), saved.getId());
        } catch (RuntimeException e) {
            bookingRepository.delete(saved);
            throw e;
        }
    }

    private Map<String, Object> enquirySummary(Enquiry e) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", e.getId());
        map.put("enquiryRef", e.getEnquiryRef());
        map.put("packageName", e.getPackageTitle());
        map.put("destination", e.getDestination());
        map.put("travelDate", e.getTravelDate() != null ? e.getTravelDate().toString() : null);
        map.put("travelers", e.getTravelers());
        map.put("budget", e.getBudget());
        map.put("message", e.getMessage());
        map.put("status", e.getStatus());
        map.put("adminNotes", e.getAdminNotes());
        map.put("createdAt", e.getCreatedAt() != null ? e.getCreatedAt().toString() : null);
        map.put("confirmedAt", e.getConfirmedAt() != null ? e.getConfirmedAt().toString() : null);
        if (e.getTravelPackage() != null) {
            map.put("packageSlug", e.getTravelPackage().getSlug());
            map.put("packageImage", e.getTravelPackage().getCoverImage());
        }
        return map;
    }
}
