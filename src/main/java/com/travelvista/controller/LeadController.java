package com.travelvista.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.travelvista.dto.LeadRequest;
import com.travelvista.model.*;
import com.travelvista.repository.AdminAuditLogRepository;
import com.travelvista.repository.LeadRepository;
import com.travelvista.repository.UserRepository;
import com.travelvista.service.EditSessionService;
import com.travelvista.service.LeadService;
import com.travelvista.service.OtpService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/leads")
public class LeadController {

    private final LeadService leadService;
    private final LeadRepository leadRepository;
    private final UserRepository userRepository;
    private final OtpService otpService;
    private final EditSessionService editSessionService;
    private final AdminAuditLogRepository auditLogRepository;
    private final ObjectMapper objectMapper;

    public LeadController(LeadService leadService, LeadRepository leadRepository,
                          UserRepository userRepository, OtpService otpService,
                          EditSessionService editSessionService,
                          AdminAuditLogRepository auditLogRepository, ObjectMapper objectMapper) {
        this.leadService = leadService;
        this.leadRepository = leadRepository;
        this.userRepository = userRepository;
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

    // ── Public: submit an enquiry ─────────────────────────────────────

    @PostMapping("/public/submit")
    public ResponseEntity<?> submitEnquiry(@RequestBody LeadRequest request) {
        if (request.getName() == null || request.getName().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Name is required"));
        }
        Lead lead = leadService.create(request);
        return ResponseEntity.ok(Map.of("message", "Enquiry submitted successfully", "id", lead.getId()));
    }

    // ── Admin: manage leads ───────────────────────────────────────────

    @GetMapping
    public ResponseEntity<?> getAllLeads(
            @RequestParam(required = false) String status) {
        if (status != null) {
            return ResponseEntity.ok(leadService.getByStatus(status));
        }
        return ResponseEntity.ok(leadService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getLead(@PathVariable Long id) {
        return leadService.getById(id)
                .map(lead -> ResponseEntity.ok((Object) lead))
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        String status = body.get("status");
        if (status == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Status is required"));
        }
        Lead lead = leadService.updateStatus(id, status);
        return ResponseEntity.ok(lead);
    }

    @PostMapping("/{id}/notes")
    public ResponseEntity<?> addNote(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        String note = body.get("note");
        if (note == null || note.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Note is required"));
        }
        Lead lead = leadService.addNote(id, note);
        return ResponseEntity.ok(lead);
    }

    // ── OTP Endpoints for Lead Edit/Delete ────────────────────────────

    @PostMapping("/{id}/otp")
    public ResponseEntity<?> sendOtp(Authentication auth, @PathVariable Long id, @RequestBody Map<String, String> body) {
        User user = getUser(auth);
        if (user == null) return ResponseEntity.status(401).body(Map.of("error", "Not authenticated"));

        var opt = leadRepository.findById(id);
        if (opt.isEmpty()) return ResponseEntity.status(404).body(Map.of("error", "Lead not found"));

        Lead lead = opt.get();

        // Verify ownership via email match
        if (lead.getEmail() == null || !lead.getEmail().equals(user.getEmail())) {
            return ResponseEntity.status(403).body(Map.of("error", "You can only manage your own leads"));
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
            otpService.generateAndSendOtp(email, purpose, id, "lead");
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

    @PostMapping("/{id}/verify-otp")
    public ResponseEntity<?> verifyOtp(Authentication auth, @PathVariable Long id, @RequestBody Map<String, String> body) {
        User user = getUser(auth);
        if (user == null) return ResponseEntity.status(401).body(Map.of("error", "Not authenticated"));

        var opt = leadRepository.findById(id);
        if (opt.isEmpty()) return ResponseEntity.status(404).body(Map.of("error", "Lead not found"));

        Lead lead = opt.get();
        if (lead.getEmail() == null || !lead.getEmail().equals(user.getEmail())) {
            return ResponseEntity.status(403).body(Map.of("error", "You can only manage your own leads"));
        }

        String purpose = body.getOrDefault("purpose", "edit");
        String code = body.get("code");
        if (code == null || code.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "OTP code is required"));
        }

        try {
            boolean verified = otpService.verifyOtp(user.getEmail(), purpose, id, "lead", code);
            if (verified) {
                EditSession session = editSessionService.createSession(user.getId(), id, "lead", purpose);
                return ResponseEntity.ok(Map.of(
                        "success", true,
                        "message", "Verification successful! You have 3 hours to " + purpose + " this lead.",
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

    @GetMapping("/{id}/edit-status")
    public ResponseEntity<?> getEditStatus(Authentication auth, @PathVariable Long id) {
        User user = getUser(auth);
        if (user == null) return ResponseEntity.status(401).body(Map.of("error", "Not authenticated"));

        var editSession = editSessionService.getActiveSession(user.getId(), id, "lead", "edit");
        var deleteSession = editSessionService.getActiveSession(user.getId(), id, "lead", "delete");

        java.util.Map<String, Object> status = new java.util.LinkedHashMap<>();
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

    @PutMapping("/{id}")
    public ResponseEntity<?> updateLead(Authentication auth, @PathVariable Long id, @RequestBody Map<String, Object> body) {
        User user = getUser(auth);
        if (user == null) return ResponseEntity.status(401).body(Map.of("error", "Not authenticated"));

        if (!editSessionService.hasActiveSession(user.getId(), id, "lead", "edit")) {
            return ResponseEntity.status(403).body(Map.of(
                    "error", "Edit access expired. Please verify OTP again.",
                    "code", "EDIT_EXPIRED"
            ));
        }

        var opt = leadRepository.findById(id);
        if (opt.isEmpty()) return ResponseEntity.status(404).body(Map.of("error", "Lead not found"));

        Lead lead = opt.get();
        if (lead.getEmail() == null || !lead.getEmail().equals(user.getEmail())) {
            return ResponseEntity.status(403).body(Map.of("error", "You can only edit your own leads"));
        }

        if (body.containsKey("destination")) lead.setDestination(body.get("destination") != null ? body.get("destination").toString() : null);
        if (body.containsKey("travelDate")) {
            String dateStr = body.get("travelDate") != null ? body.get("travelDate").toString() : null;
            if (dateStr != null && !dateStr.isEmpty()) {
                try { lead.setTravelDate(LocalDate.parse(dateStr)); } catch (Exception ignored) {}
            } else {
                lead.setTravelDate(null);
            }
        }
        if (body.containsKey("travelers")) lead.setTravelers(Integer.valueOf(body.get("travelers").toString()));
        if (body.containsKey("budget")) lead.setBudget(body.get("budget") != null ? body.get("budget").toString() : null);
        if (body.containsKey("message")) lead.setMessage(body.get("message") != null ? body.get("message").toString() : "");

        lead.setUpdatedAt(LocalDateTime.now());
        leadRepository.save(lead);

        return ResponseEntity.ok(Map.of("success", true, "message", "Lead updated successfully"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteLead(Authentication auth, @PathVariable Long id) {
        User user = getUser(auth);
        if (user == null) return ResponseEntity.status(401).body(Map.of("error", "Not authenticated"));

        if (!editSessionService.hasActiveSession(user.getId(), id, "lead", "delete")) {
            return ResponseEntity.status(403).body(Map.of(
                    "error", "Delete access expired. Please verify OTP again.",
                    "code", "DELETE_EXPIRED"
            ));
        }

        var opt = leadRepository.findById(id);
        if (opt.isEmpty()) return ResponseEntity.status(404).body(Map.of("error", "Lead not found"));

        Lead lead = opt.get();
        if (lead.getEmail() == null || !lead.getEmail().equals(user.getEmail())) {
            return ResponseEntity.status(403).body(Map.of("error", "You can only delete your own leads"));
        }

        leadRepository.deleteById(id);
        editSessionService.invalidateSession(user.getId(), id, "lead", "delete");

        return ResponseEntity.ok(Map.of("success", true, "message", "Lead deleted successfully"));
    }

    // ── Admin: delete lead with audit trail ────────────────────────────

    @DeleteMapping("/admin/{id}")
    public ResponseEntity<?> adminDeleteLead(Authentication auth, @PathVariable Long id, @RequestBody(required = false) Map<String, String> body) {
        User admin = getAdmin(auth);
        if (admin == null) return ResponseEntity.status(403).body(Map.of("error", "Admin access required"));

        var opt = leadRepository.findById(id);
        if (opt.isEmpty()) return ResponseEntity.status(404).body(Map.of("error", "Lead not found"));

        Lead lead = opt.get();
        String reason = body != null ? body.getOrDefault("reason", "No reason provided") : "No reason provided";

        // Create audit log
        try {
            java.util.Map<String, Object> data = new java.util.LinkedHashMap<>();
            data.put("id", lead.getId());
            data.put("name", lead.getName());
            data.put("email", lead.getEmail());
            data.put("phone", lead.getPhone());
            data.put("destination", lead.getDestination());
            data.put("status", lead.getStatus());
            data.put("createdAt", lead.getCreatedAt() != null ? lead.getCreatedAt().toString() : null);

            String recordData = objectMapper.writeValueAsString(data);
            AdminAuditLog auditLog = new AdminAuditLog(
                    admin.getId(), admin.getEmail(), "delete_lead",
                    id, "lead", recordData, reason
            );
            auditLogRepository.save(auditLog);
        } catch (Exception e) {
            System.err.println("Failed to create audit log: " + e.getMessage());
        }

        leadRepository.deleteById(id);

        return ResponseEntity.ok(Map.of("success", true, "message", "Lead deleted by admin"));
    }
}
