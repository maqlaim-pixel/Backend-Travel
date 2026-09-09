package com.travelvista.controller;

import com.travelvista.model.*;
import com.travelvista.repository.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/wishlists")
public class AdminWishlistController {

    private final UserRepository userRepository;
    private final WishlistRepository wishlistRepository;
    private final PackageRepository packageRepository;

    public AdminWishlistController(UserRepository userRepository, WishlistRepository wishlistRepository,
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

    // Get all client wishlists — admin/editor view
    @GetMapping
    public ResponseEntity<?> getAllWishlists(Authentication auth) {
        User admin = getUser(auth);
        if (admin == null) return ResponseEntity.status(401).body(Map.of("error", "Not authenticated"));

        List<Wishlist> allWishlists = wishlistRepository.findAll();
        
        // Group by user
        Map<Long, List<Map<String, Object>>> groupedByUser = new LinkedHashMap<>();
        Map<Long, Map<String, Object>> userInfo = new LinkedHashMap<>();
        
        for (Wishlist w : allWishlists) {
            Long userId = w.getUser().getId();
            
            if (!userInfo.containsKey(userId)) {
                Map<String, Object> uinfo = new LinkedHashMap<>();
                uinfo.put("userId", userId);
                uinfo.put("userName", w.getUser().getName());
                uinfo.put("userEmail", w.getUser().getEmail());
                uinfo.put("userPhone", w.getUser().getPhone());
                userInfo.put(userId, uinfo);
                groupedByUser.put(userId, new ArrayList<>());
            }
            
            Map<String, Object> pkgInfo = new LinkedHashMap<>();
            pkgInfo.put("wishlistId", w.getId());
            pkgInfo.put("addedAt", w.getCreatedAt() != null ? w.getCreatedAt().toString() : null);
            if (w.getTravelPackage() != null) {
                TravelPackage p = w.getTravelPackage();
                pkgInfo.put("packageId", p.getId());
                pkgInfo.put("title", p.getTitle());
                pkgInfo.put("slug", p.getSlug());
                pkgInfo.put("destination", p.getDestination());
                pkgInfo.put("state", p.getState());
                pkgInfo.put("country", p.getCountry());
                pkgInfo.put("category", p.getCategory());
                pkgInfo.put("startingPrice", p.getStartingPrice());
                pkgInfo.put("durationDays", p.getDurationDays());
                pkgInfo.put("durationNights", p.getDurationNights());
                pkgInfo.put("coverImage", p.getCoverImage());
            }
            groupedByUser.get(userId).add(pkgInfo);
        }
        
        // Build response: list of users with their wishlist items
        List<Map<String, Object>> result = new ArrayList<>();
        for (Map.Entry<Long, List<Map<String, Object>>> entry : groupedByUser.entrySet()) {
            Map<String, Object> item = new LinkedHashMap<>(userInfo.get(entry.getKey()));
            item.put("wishlistCount", entry.getValue().size());
            item.put("packages", entry.getValue());
            
            // Add interest summary
            List<String> destinations = entry.getValue().stream()
                .map(p -> (String) p.get("destination"))
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
            List<String> categories = entry.getValue().stream()
                .map(p -> (String) p.get("category"))
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
            List<String> states = entry.getValue().stream()
                .map(p -> (String) p.get("state"))
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
            
            item.put("interestedDestinations", destinations);
            item.put("interestedCategories", categories);
            item.put("interestedStates", states);
            
            result.add(item);
        }
        
        return ResponseEntity.ok(result);
    }

    // Get a specific client's wishlist
    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getUserWishlist(Authentication auth, @PathVariable Long userId) {
        User admin = getUser(auth);
        if (admin == null) return ResponseEntity.status(401).body(Map.of("error", "Not authenticated"));

        List<Wishlist> items = wishlistRepository.findByUserIdOrderByCreatedAtDesc(userId);
        User client = userRepository.findById(userId).orElse(null);
        if (client == null) return ResponseEntity.badRequest().body(Map.of("error", "User not found"));
        
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("userId", client.getId());
        result.put("userName", client.getName());
        result.put("userEmail", client.getEmail());
        result.put("userPhone", client.getPhone());
        
        List<Map<String, Object>> packages = new ArrayList<>();
        for (Wishlist w : items) {
            Map<String, Object> pkgInfo = new LinkedHashMap<>();
            pkgInfo.put("wishlistId", w.getId());
            pkgInfo.put("addedAt", w.getCreatedAt() != null ? w.getCreatedAt().toString() : null);
            if (w.getTravelPackage() != null) {
                TravelPackage p = w.getTravelPackage();
                pkgInfo.put("packageId", p.getId());
                pkgInfo.put("title", p.getTitle());
                pkgInfo.put("slug", p.getSlug());
                pkgInfo.put("destination", p.getDestination());
                pkgInfo.put("state", p.getState());
                pkgInfo.put("category", p.getCategory());
                pkgInfo.put("startingPrice", p.getStartingPrice());
                pkgInfo.put("coverImage", p.getCoverImage());
            }
            packages.add(pkgInfo);
        }
        result.put("packages", packages);
        result.put("wishlistCount", packages.size());
        
        return ResponseEntity.ok(result);
    }

    // Get package interest summary — most wishlisted packages
    @GetMapping("/interests/summary")
    public ResponseEntity<?> getInterestSummary(Authentication auth) {
        User admin = getUser(auth);
        if (admin == null) return ResponseEntity.status(401).body(Map.of("error", "Not authenticated"));

        List<Wishlist> allWishlists = wishlistRepository.findAll();
        
        // Count wishlists per package
        Map<Long, Map<String, Object>> packageInterests = new LinkedHashMap<>();
        for (Wishlist w : allWishlists) {
            if (w.getTravelPackage() == null) continue;
            Long pkgId = w.getTravelPackage().getId();
            
            if (!packageInterests.containsKey(pkgId)) {
                Map<String, Object> info = new LinkedHashMap<>();
                TravelPackage p = w.getTravelPackage();
                info.put("packageId", pkgId);
                info.put("title", p.getTitle());
                info.put("slug", p.getSlug());
                info.put("destination", p.getDestination());
                info.put("state", p.getState());
                info.put("category", p.getCategory());
                info.put("startingPrice", p.getStartingPrice());
                info.put("coverImage", p.getCoverImage());
                info.put("wishlistCount", 0);
                info.put("interestedUsers", new ArrayList<>());
                packageInterests.put(pkgId, info);
            }
            
            Map<String, Object> info = packageInterests.get(pkgId);
            info.put("wishlistCount", (int) info.get("wishlistCount") + 1);
            List<Map<String, Object>> users = (List<Map<String, Object>>) info.get("interestedUsers");
            users.add(Map.of(
                "userId", w.getUser().getId(),
                "userName", w.getUser().getName(),
                "userEmail", w.getUser().getEmail(),
                "addedAt", w.getCreatedAt() != null ? w.getCreatedAt().toString() : null
            ));
        }
        
        // Sort by most wishlisted
        List<Map<String, Object>> sorted = packageInterests.values().stream()
            .sorted((a, b) -> (int) b.get("wishlistCount") - (int) a.get("wishlistCount"))
            .collect(Collectors.toList());
        
        // Overall stats
        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("totalWishlists", allWishlists.size());
        stats.put("uniqueClients", allWishlists.stream().map(w -> w.getUser().getId()).distinct().count());
        stats.put("uniquePackages", allWishlists.stream().map(w -> w.getTravelPackage().getId()).distinct().count());
        stats.put("packages", sorted);
        
        return ResponseEntity.ok(stats);
    }
}
