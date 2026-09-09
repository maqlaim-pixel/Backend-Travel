package com.travelvista.controller;

import com.travelvista.model.Lead;
import com.travelvista.repository.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final PackageRepository packageRepository;
    private final DestinationRepository destinationRepository;
    private final ActivityRepository activityRepository;
    private final HotelRepository hotelRepository;
    private final BlogRepository blogRepository;
    private final BookingRepository bookingRepository;
    private final LeadRepository leadRepository;
    private final EnquiryRepository enquiryRepository;
    private final ReviewRepository reviewRepository;

    public DashboardController(PackageRepository packageRepository,
                               DestinationRepository destinationRepository,
                               ActivityRepository activityRepository,
                               HotelRepository hotelRepository,
                               BlogRepository blogRepository,
                               BookingRepository bookingRepository,
                               LeadRepository leadRepository,
                               EnquiryRepository enquiryRepository,
                               ReviewRepository reviewRepository) {
        this.packageRepository = packageRepository;
        this.destinationRepository = destinationRepository;
        this.activityRepository = activityRepository;
        this.hotelRepository = hotelRepository;
        this.blogRepository = blogRepository;
        this.bookingRepository = bookingRepository;
        this.leadRepository = leadRepository;
        this.enquiryRepository = enquiryRepository;
        this.reviewRepository = reviewRepository;
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        Map<String, Object> stats = new HashMap<>();

        // All counts from database — ZERO hardcoded values
        stats.put("totalPackages", packageRepository.countAll());
        stats.put("publishedPackages", packageRepository.countPublished());
        stats.put("totalDestinations", destinationRepository.count());
        stats.put("totalActivities", activityRepository.count());
        stats.put("totalHotels", hotelRepository.count());
        stats.put("totalBlogs", blogRepository.count());
        stats.put("totalBookings", bookingRepository.countAll());
        stats.put("totalLeads", leadRepository.countAll());
        stats.put("newLeads", leadRepository.countNew());
        stats.put("totalEnquiries", enquiryRepository.countAll());
        stats.put("totalReviews", reviewRepository.countAll());

        return ResponseEntity.ok(stats);
    }

    @GetMapping("/recent-leads")
    public ResponseEntity<List<?>> getRecentLeads() {
        List<Lead> leads = leadRepository.findAllByOrderByCreatedAtDesc();
        if (leads.size() > 10) {
            return ResponseEntity.ok(leads.subList(0, 10));
        }
        return ResponseEntity.ok(leads);
    }
}
