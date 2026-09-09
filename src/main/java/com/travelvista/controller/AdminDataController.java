package com.travelvista.controller;

import com.travelvista.model.*;
import com.travelvista.repository.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/admin/data")
public class AdminDataController {

    private final BookingRepository bookingRepository;
    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final PackageRepository packageRepository;

    public AdminDataController(BookingRepository bookingRepository, ReviewRepository reviewRepository,
                               UserRepository userRepository, PackageRepository packageRepository) {
        this.bookingRepository = bookingRepository;
        this.reviewRepository = reviewRepository;
        this.userRepository = userRepository;
        this.packageRepository = packageRepository;
    }

    private List<String> parseImages(String imagesJson) {
        if (imagesJson == null || imagesJson.isBlank()) return List.of();
        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            return Arrays.asList(mapper.readValue(imagesJson, String[].class));
        } catch (Exception e) {
            return List.of();
        }
    }

    // ── All Bookings (Admin) ──────────────────────────────────
    @GetMapping("/bookings")
    public ResponseEntity<?> getAllBookings(
            @RequestParam(required = false) String status) {
        List<Booking> bookings;
        if (status != null && !status.equals("all")) {
            bookings = bookingRepository.findByStatus(status);
        } else {
            bookings = bookingRepository.findAllByOrderByCreatedAtDesc();
        }

        List<Map<String, Object>> list = new ArrayList<>();
        for (Booking b : bookings) {
            Map<String, Object> bm = new LinkedHashMap<>();
            bm.put("id", b.getId());
            bm.put("bookingRef", b.getBookingRef());
            bm.put("customerName", b.getUser() != null ? b.getUser().getName() : "Unknown");
            bm.put("customerEmail", b.getUser() != null ? b.getUser().getEmail() : "");
            bm.put("packageName", b.getPackageTitle());
            bm.put("travelDate", b.getTravelDate() != null ? b.getTravelDate().toString() : null);
            bm.put("endDate", b.getEndDate() != null ? b.getEndDate().toString() : null);
            bm.put("travelers", b.getTravelers());
            bm.put("totalAmount", b.getTotalAmount());
            bm.put("status", b.getStatus());
            bm.put("paymentStatus", b.getPaymentStatus());
            bm.put("createdAt", b.getCreatedAt() != null ? b.getCreatedAt().toString() : null);
            list.add(bm);
        }
        return ResponseEntity.ok(list);
    }

    // ── Update Booking Status ──────────────────────────────────
    @PutMapping("/bookings/{id}/status")
    public ResponseEntity<?> updateBookingStatus(
            @PathVariable Long id, @RequestBody Map<String, String> body) {
        String status = body.get("status");
        String paymentStatus = body.get("paymentStatus");

        return bookingRepository.findById(id).map(booking -> {
            if (status != null) booking.setStatus(status);
            if (paymentStatus != null) booking.setPaymentStatus(paymentStatus);
            booking.setUpdatedAt(java.time.LocalDateTime.now());
            bookingRepository.save(booking);
            return ResponseEntity.ok(Map.of("success", true, "message", "Booking updated"));
        }).orElse(ResponseEntity.notFound().build());
    }

    // ── All Reviews (Admin) ────────────────────────────────────
    @GetMapping("/reviews")
    public ResponseEntity<?> getAllReviews() {
        List<Review> reviews = reviewRepository.findAllByOrderByCreatedAtDesc();

        List<Map<String, Object>> list = new ArrayList<>();
        for (Review r : reviews) {
            Map<String, Object> rm = new LinkedHashMap<>();
            rm.put("id", r.getId());
            rm.put("rating", r.getRating());
            rm.put("comment", r.getComment());
            // Parse images JSON using Jackson
            rm.put("images", parseImages(r.getImages()));
            rm.put("createdAt", r.getCreatedAt() != null ? r.getCreatedAt().toString() : null);
            if (r.getUser() != null) {
                rm.put("userName", r.getUser().getName());
                rm.put("userEmail", r.getUser().getEmail());
            }
            if (r.getTravelPackage() != null) {
                rm.put("packageId", r.getTravelPackage().getId());
                rm.put("packageName", r.getTravelPackage().getTitle());
            }
            list.add(rm);
        }
        return ResponseEntity.ok(list);
    }

    // ── Delete Review ──────────────────────────────────────────
    @DeleteMapping("/reviews/{id}")
    public ResponseEntity<?> deleteReview(@PathVariable Long id) {
        reviewRepository.deleteById(id);
        return ResponseEntity.ok(Map.of("success", true, "message", "Review deleted"));
    }
}
