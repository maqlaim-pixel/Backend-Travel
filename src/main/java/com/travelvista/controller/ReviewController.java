package com.travelvista.controller;

import com.travelvista.model.Review;
import com.travelvista.model.User;
import com.travelvista.model.TravelPackage;
import com.travelvista.repository.ReviewRepository;
import com.travelvista.repository.UserRepository;
import com.travelvista.repository.PackageRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/packages/{packageId}/reviews")
public class ReviewController {

    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final PackageRepository packageRepository;

    public ReviewController(ReviewRepository reviewRepository, UserRepository userRepository,
                            PackageRepository packageRepository) {
        this.reviewRepository = reviewRepository;
        this.userRepository = userRepository;
        this.packageRepository = packageRepository;
    }

    private User getUser(Authentication auth) {
        if (auth == null || auth.getPrincipal() == null) return null;
        Object principal = auth.getPrincipal();
        if (principal instanceof User) return (User) principal;
        if (auth.getName() != null) return userRepository.findByEmail(auth.getName()).orElse(null);
        return null;
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

    // Public: get reviews for a package + user's own review info
    @GetMapping
    public ResponseEntity<?> getReviews(Authentication auth, @PathVariable Long packageId) {
        List<Review> reviews = reviewRepository.findByTravelPackageIdOrderByCreatedAtDesc(packageId);
        User user = getUser(auth);

        List<Map<String, Object>> list = new ArrayList<>();
        for (Review r : reviews) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", r.getId());
            map.put("rating", r.getRating());
            map.put("comment", r.getComment());
            map.put("images", parseImages(r.getImages()));
            map.put("createdAt", r.getCreatedAt() != null ? r.getCreatedAt().toString() : null);
            if (r.getUser() != null) {
                map.put("userId", r.getUser().getId());
                map.put("userName", r.getUser().getName());
                map.put("userProfileImage", r.getUser().getProfileImage());
            }
            list.add(map);
        }

        // Include current user's review status
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("reviews", list);
        response.put("totalReviews", reviews.size());

        if (user != null) {
            boolean hasReviewed = reviewRepository.existsByUserIdAndTravelPackageId(user.getId(), packageId);
            response.put("hasReviewed", hasReviewed);
            if (hasReviewed) {
                Review myReview = reviewRepository.findByUserIdAndTravelPackageId(user.getId(), packageId);
                if (myReview != null) {
                    response.put("myReviewId", myReview.getId());
                    response.put("myReviewRating", myReview.getRating());
                    response.put("myReviewComment", myReview.getComment());
                    response.put("myReviewImages", parseImages(myReview.getImages()));
                }
            }
        } else {
            response.put("hasReviewed", false);
        }

        return ResponseEntity.ok(response);
    }

    // Authenticated: submit a review (1 user per package) with optional images
    @PostMapping
    public ResponseEntity<?> submitReview(Authentication auth, @PathVariable Long packageId,
                                          @RequestBody Map<String, Object> body) {
        User user = getUser(auth);
        if (user == null) return ResponseEntity.status(401).body(Map.of("error", "Not authenticated"));

        Optional<TravelPackage> pkgOpt = packageRepository.findById(packageId);
        if (pkgOpt.isEmpty()) return ResponseEntity.badRequest().body(Map.of("error", "Package not found"));

        // Check if user already reviewed this package
        if (reviewRepository.existsByUserIdAndTravelPackageId(user.getId(), packageId)) {
            return ResponseEntity.badRequest().body(Map.of("error", "You have already reviewed this package. You can edit your existing review instead."));
        }

        Integer rating = body.get("rating") != null ? Integer.valueOf(body.get("rating").toString()) : 5;
        String comment = body.get("comment") != null ? body.get("comment").toString() : "";

        if (comment.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Review comment is required"));
        }

        Review review = new Review();
        review.setUser(user);
        review.setTravelPackage(pkgOpt.get());
        review.setRating(Math.min(5, Math.max(1, rating)));
        review.setComment(comment.trim());

        // Handle images (up to 2)
        Object imagesObj = body.get("images");
        if (imagesObj instanceof List) {
            List<?> images = (List<?>) imagesObj;
            if (images.size() > 2) {
                return ResponseEntity.badRequest().body(Map.of("error", "Maximum 2 images allowed per review"));
            }
            List<String> imageUrls = new ArrayList<>();
            for (Object img : images) {
                if (img != null && !img.toString().isEmpty()) {
                    imageUrls.add(img.toString());
                }
            }
            if (!imageUrls.isEmpty()) {
                review.setImages(imageUrls.toString());
            }
        }

        reviewRepository.save(review);
        return ResponseEntity.ok(Map.of("success", true, "message", "Review submitted"));
    }

    // ── Edit a review (PUT) ──────────────────────────────────────
    @PutMapping("/{reviewId}")
    public ResponseEntity<?> editReview(Authentication auth, @PathVariable Long packageId,
                                         @PathVariable Long reviewId,
                                         @RequestBody Map<String, Object> body) {
        User user = getUser(auth);
        if (user == null) return ResponseEntity.status(401).body(Map.of("error", "Not authenticated"));

        Optional<Review> reviewOpt = reviewRepository.findById(reviewId);
        if (reviewOpt.isEmpty()) return ResponseEntity.notFound().build();

        Review review = reviewOpt.get();

        // Only the review owner can edit
        if (review.getUser() == null || !review.getUser().getId().equals(user.getId())) {
            return ResponseEntity.status(403).body(Map.of("error", "You can only edit your own reviews"));
        }

        if (body.containsKey("rating") && body.get("rating") != null) {
            review.setRating(Math.min(5, Math.max(1, Integer.valueOf(body.get("rating").toString()))));
        }
        if (body.containsKey("comment") && body.get("comment") != null) {
            String comment = body.get("comment").toString().trim();
            if (comment.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Review comment is required"));
            }
            review.setComment(comment);
        }

        // Handle images update (up to 2)
        if (body.containsKey("images")) {
            Object imagesObj = body.get("images");
            if (imagesObj instanceof List) {
                List<?> images = (List<?>) imagesObj;
                if (images.size() > 2) {
                    return ResponseEntity.badRequest().body(Map.of("error", "Maximum 2 images allowed per review"));
                }
                List<String> imageUrls = new ArrayList<>();
                for (Object img : images) {
                    if (img != null && !img.toString().isEmpty()) {
                        imageUrls.add(img.toString());
                    }
                }
                review.setImages(imageUrls.isEmpty() ? null : imageUrls.toString());
            } else {
                review.setImages(null);
            }
        }

        reviewRepository.save(review);
        return ResponseEntity.ok(Map.of("success", true, "message", "Review updated"));
    }

    // ── Delete a review ──────────────────────────────────────────
    @DeleteMapping("/{reviewId}")
    public ResponseEntity<?> deleteReview(Authentication auth, @PathVariable Long packageId,
                                           @PathVariable Long reviewId) {
        User user = getUser(auth);
        if (user == null) return ResponseEntity.status(401).body(Map.of("error", "Not authenticated"));

        Optional<Review> reviewOpt = reviewRepository.findById(reviewId);
        if (reviewOpt.isEmpty()) return ResponseEntity.notFound().build();

        Review review = reviewOpt.get();
        if (review.getUser() == null || !review.getUser().getId().equals(user.getId())) {
            return ResponseEntity.status(403).body(Map.of("error", "You can only delete your own reviews"));
        }

        reviewRepository.deleteById(reviewId);
        return ResponseEntity.ok(Map.of("success", true, "message", "Review deleted"));
    }
}
