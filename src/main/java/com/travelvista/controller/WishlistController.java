package com.travelvista.controller;

import com.travelvista.model.*;
import com.travelvista.repository.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/customer/wishlist")
public class WishlistController {

    private final UserRepository userRepository;
    private final WishlistRepository wishlistRepository;
    private final PackageRepository packageRepository;

    public WishlistController(UserRepository userRepository, WishlistRepository wishlistRepository,
                              PackageRepository packageRepository) {
        this.userRepository = userRepository;
        this.wishlistRepository = wishlistRepository;
        this.packageRepository = packageRepository;
    }

    private User getUser(Authentication auth) {
        if (auth == null || auth.getPrincipal() == null) return null;
        Object principal = auth.getPrincipal();
        if (principal instanceof User) return (User) principal;
        if (auth.getName() != null) return userRepository.findByEmail(auth.getName()).orElse(null);
        return null;
    }

    // Add to wishlist
    @PostMapping
    public ResponseEntity<?> addToWishlist(Authentication auth, @RequestBody Map<String, Object> body) {
        User user = getUser(auth);
        if (user == null) return ResponseEntity.status(401).body(Map.of("error", "Not authenticated"));

        Long packageId = body.get("packageId") != null ? Long.valueOf(body.get("packageId").toString()) : null;
        if (packageId == null) return ResponseEntity.badRequest().body(Map.of("error", "Package ID required"));

        if (wishlistRepository.existsByUserIdAndTravelPackageId(user.getId(), packageId)) {
            return ResponseEntity.status(409).body(Map.of("error", "Already in wishlist"));
        }

        TravelPackage pkg = packageRepository.findById(packageId).orElse(null);
        if (pkg == null) return ResponseEntity.badRequest().body(Map.of("error", "Package not found"));

        Wishlist w = new Wishlist();
        w.setUser(user);
        w.setTravelPackage(pkg);
        wishlistRepository.save(w);

        return ResponseEntity.ok(Map.of("success", true, "message", "Added to wishlist"));
    }

    // Get wishlist
    @GetMapping
    public ResponseEntity<?> getWishlist(Authentication auth) {
        User user = getUser(auth);
        if (user == null) return ResponseEntity.status(401).body(Map.of("error", "Not authenticated"));

        List<Wishlist> items = wishlistRepository.findByUserIdOrderByCreatedAtDesc(user.getId());
        List<Map<String, Object>> list = new ArrayList<>();
        for (Wishlist w : items) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", w.getId());
            if (w.getTravelPackage() != null) {
                TravelPackage p = w.getTravelPackage();
                map.put("packageId", p.getId());
                map.put("title", p.getTitle());
                map.put("slug", p.getSlug());
                map.put("destination", p.getDestination());
                map.put("state", p.getState());
                map.put("startingPrice", p.getStartingPrice());
                map.put("durationDays", p.getDurationDays());
                map.put("durationNights", p.getDurationNights());
                map.put("coverImage", p.getCoverImage());
                map.put("rating", p.getRating());
                map.put("category", p.getCategory());
                map.put("tags", p.getTags());
            }
            map.put("createdAt", w.getCreatedAt() != null ? w.getCreatedAt().toString() : null);
            list.add(map);
        }
        return ResponseEntity.ok(list);
    }

    // Check if package is wishlisted
    @GetMapping("/check/{packageId}")
    public ResponseEntity<?> checkWishlist(Authentication auth, @PathVariable Long packageId) {
        User user = getUser(auth);
        if (user == null) return ResponseEntity.ok(Map.of("wishlisted", false));
        boolean exists = wishlistRepository.existsByUserIdAndTravelPackageId(user.getId(), packageId);
        return ResponseEntity.ok(Map.of("wishlisted", exists));
    }

    // Remove from wishlist
    @DeleteMapping("/{packageId}")
    public ResponseEntity<?> removeFromWishlist(Authentication auth, @PathVariable Long packageId) {
        User user = getUser(auth);
        if (user == null) return ResponseEntity.status(401).body(Map.of("error", "Not authenticated"));
        wishlistRepository.deleteByUserIdAndTravelPackageId(user.getId(), packageId);
        return ResponseEntity.ok(Map.of("success", true, "message", "Removed from wishlist"));
    }
}
