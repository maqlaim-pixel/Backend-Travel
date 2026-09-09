package com.travelvista.controller;

import com.travelvista.model.Destination;
import com.travelvista.model.Menu;
import com.travelvista.model.TravelPackage;
import com.travelvista.repository.MenuRepository;
import com.travelvista.repository.DestinationRepository;
import com.travelvista.repository.PackageRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/menus")
public class MenuController {

    private final MenuRepository menuRepository;
    private final DestinationRepository destinationRepository;
    private final PackageRepository packageRepository;

    public MenuController(MenuRepository menuRepository, DestinationRepository destinationRepository,
                          PackageRepository packageRepository) {
        this.menuRepository = menuRepository;
        this.destinationRepository = destinationRepository;
        this.packageRepository = packageRepository;
    }

    // GET /api/menus — all published menus
    @GetMapping
    public ResponseEntity<?> allMenus() {
        List<Menu> menus = menuRepository.findByStatusOrderByDisplayOrderAsc("published");
        List<Map<String, Object>> list = menus.stream().map(this::menuSummary).collect(Collectors.toList());
        return ResponseEntity.ok(list);
    }

    // GET /api/menus/{slug} — single menu with SEO + destinations
    @GetMapping("/{slug}")
    public ResponseEntity<?> menuBySlug(@PathVariable String slug) {
        Menu menu = menuRepository.findBySlug(slug).orElse(null);
        if (menu == null) {
            return ResponseEntity.status(404).body(Map.of("error", "Menu not found"));
        }

        Map<String, Object> response = menuDetail(menu);
        return ResponseEntity.ok(response);
    }

    // GET /api/menus/{slug}/destinations — destinations for a menu
    @GetMapping("/{slug}/destinations")
    public ResponseEntity<?> menuDestinations(@PathVariable String slug) {
        Menu menu = menuRepository.findBySlug(slug).orElse(null);
        if (menu == null) {
            return ResponseEntity.status(404).body(Map.of("error", "Menu not found"));
        }

        List<Map<String, Object>> destList = menu.getDestinations().stream()
            .filter(d -> "published".equals(d.getStatus()))
            .map(this::destSummary)
            .collect(Collectors.toList());

        return ResponseEntity.ok(destList);
    }

    // GET /api/destinations/public — all published destinations (public)
    @GetMapping("/destinations/all")
    public ResponseEntity<?> allDestinations() {
        List<Destination> dests = destinationRepository.findByStatusOrderBySortOrderAsc("published");
        List<Map<String, Object>> list = dests.stream().map(this::destSummary).collect(Collectors.toList());
        return ResponseEntity.ok(list);
    }

    // GET /api/destinations/public/{slug} — single destination with its packages
    @GetMapping("/destinations/{slug}")
    public ResponseEntity<?> destinationBySlug(@PathVariable String slug) {
        Destination dest = destinationRepository.findBySlug(slug).orElse(null);
        if (dest == null) {
            return ResponseEntity.status(404).body(Map.of("error", "Destination not found"));
        }

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("id", dest.getId());
        response.put("name", dest.getName());
        response.put("slug", dest.getSlug());
        response.put("description", dest.getDescription());
        response.put("shortDescription", dest.getShortDescription());
        response.put("image", dest.getImage());
        response.put("country", dest.getCountry());
        response.put("state", dest.getState());
        response.put("type", dest.getType());
        response.put("tagline", dest.getTagline());
        response.put("bestTime", dest.getBestTime());
        response.put("avgTemp", dest.getAvgTemp());
        response.put("languages", dest.getLanguages());

        // Find packages for this destination
        List<TravelPackage> packages = packageRepository.findByStatus("published").stream()
            .filter(p -> dest.getName().equalsIgnoreCase(p.getDestination())
                || dest.getName().equalsIgnoreCase(p.getState()))
            .collect(Collectors.toList());

        response.put("packages", packages.stream().map(this::pkgSummary).collect(Collectors.toList()));
        response.put("packageCount", packages.size());

        return ResponseEntity.ok(response);
    }

    // ── Helpers ────────────────────────────────────────────────

    private Map<String, Object> menuSummary(Menu m) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", m.getId());
        map.put("name", m.getName());
        map.put("slug", m.getSlug());
        map.put("description", m.getDescription());
        map.put("icon", m.getIcon());
        map.put("displayOrder", m.getDisplayOrder());
        map.put("destinationCount", m.getDestinations() != null ? m.getDestinations().size() : 0);
        return map;
    }

    private Map<String, Object> menuDetail(Menu m) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", m.getId());
        map.put("name", m.getName());
        map.put("slug", m.getSlug());
        map.put("description", m.getDescription());
        map.put("icon", m.getIcon());
        map.put("pageTitle", m.getPageTitle());
        map.put("pageSubtitle", m.getPageSubtitle());
        map.put("pageHeroImage", m.getPageHeroImage());
        map.put("pageContent", m.getPageContent());
        map.put("seoTitle", m.getSeoTitle());
        map.put("seoDescription", m.getSeoDescription());
        map.put("seoKeywords", m.getSeoKeywords());

        // Destinations grouped by type
        List<Destination> publishedDests = m.getDestinations().stream()
            .filter(d -> "published".equals(d.getStatus()))
            .collect(Collectors.toList());

        // Group by type (domestic / international)
        Map<String, List<Map<String, Object>>> byType = publishedDests.stream()
            .collect(Collectors.groupingBy(
                d -> d.getType() != null ? d.getType() : "domestic",
                LinkedHashMap::new,
                Collectors.mapping(this::destSummary, Collectors.toList())
            ));
        map.put("destinationsByType", byType);
        map.put("destinations", publishedDests.stream().map(this::destSummary).collect(Collectors.toList()));
        map.put("destinationCount", publishedDests.size());

        // Top featured packages across these destinations
        List<TravelPackage> allPkgs = packageRepository.findByStatus("published");
        Set<Long> destIds = publishedDests.stream().map(Destination::getId).collect(Collectors.toSet());
        List<Map<String, Object>> topPackages = allPkgs.stream()
            .filter(p -> {
                String dest = p.getDestination();
                return publishedDests.stream().anyMatch(d ->
                    dest != null && (dest.equalsIgnoreCase(d.getName()) || dest.equalsIgnoreCase(d.getState()))
                );
            })
            .limit(12)
            .map(this::pkgSummary)
            .collect(Collectors.toList());
        map.put("topPackages", topPackages);

        return map;
    }

    private Map<String, Object> destSummary(Destination d) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", d.getId());
        map.put("name", d.getName());
        map.put("slug", d.getSlug());
        map.put("type", d.getType());
        map.put("country", d.getCountry());
        map.put("state", d.getState());
        map.put("shortDescription", d.getShortDescription());
        map.put("image", d.getImage());
        map.put("tagline", d.getTagline());
        map.put("bestTime", d.getBestTime());
        return map;
    }

    private Map<String, Object> pkgSummary(TravelPackage p) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", p.getId());
        map.put("title", p.getTitle());
        map.put("slug", p.getSlug());
        map.put("shortDescription", p.getShortDescription());
        map.put("destination", p.getDestination());
        map.put("durationDays", p.getDurationDays());
        map.put("durationNights", p.getDurationNights());
        map.put("startingPrice", p.getStartingPrice());
        map.put("rating", p.getRating());
        map.put("image", p.getCoverImage());
        map.put("category", p.getCategory());
        map.put("tags", p.getTags());
        return map;
    }
}
