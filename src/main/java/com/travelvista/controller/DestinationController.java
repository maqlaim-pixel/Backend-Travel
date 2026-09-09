package com.travelvista.controller;

import com.travelvista.model.Destination;
import com.travelvista.repository.DestinationRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/destinations")
public class DestinationController {

    private final DestinationRepository repo;
    private final JdbcTemplate jdbc;

    public DestinationController(DestinationRepository repo, JdbcTemplate jdbc) {
        this.repo = repo;
        this.jdbc = jdbc;
    }

    // Public: get published destinations
    @GetMapping
    public ResponseEntity<List<Destination>> getAll(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String status) {
        if ("published".equals(status)) return ResponseEntity.ok(repo.findByStatus("published"));
        if (type != null) return ResponseEntity.ok(repo.findByType(type));
        return ResponseEntity.ok(repo.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        return repo.findById(id).map(d -> ResponseEntity.ok((Object) d))
                .orElse(ResponseEntity.notFound().build());
    }

    // Public: get destination by slug with full CMS data + related packages
    @GetMapping("/slug/{slug}")
    public ResponseEntity<?> getBySlug(@PathVariable String slug) {
        return repo.findBySlug(slug).map(dest -> {
            Map<String, Object> result = new HashMap<>();
            result.put("destination", dest);

            // Fetch related packages for this destination
            try {
                List<Map<String, Object>> packages = jdbc.queryForList(
                    "SELECT id, title, slug, destination, state, starting_price, duration_days, duration_nights, cover_image, rating, review_count, description, category, featured, inclusions, tags " +
                    "FROM packages WHERE LOWER(destination) = LOWER(?) OR LOWER(state) = LOWER(?) ORDER BY featured DESC, id DESC",
                    dest.getName(), dest.getState() != null ? dest.getState() : dest.getName()
                );
                result.put("packages", packages);
            } catch (Exception e) {
                result.put("packages", List.of());
            }

            return ResponseEntity.ok((Object) result);
        }).orElse(ResponseEntity.notFound().build());
    }

    // Admin CRUD
    @PostMapping
    public ResponseEntity<Destination> create(@RequestBody Destination dest) {
        dest.setCreatedAt(LocalDateTime.now());
        dest.setUpdatedAt(LocalDateTime.now());
        if (dest.getSlug() == null || dest.getSlug().isEmpty()) {
            dest.setSlug(dest.getName().toLowerCase().replaceAll("[^a-z0-9]+", "-").replaceAll("-+", "-"));
        }
        return ResponseEntity.ok(repo.save(dest));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody Destination dest) {
        return repo.findById(id).map(existing -> {
            dest.setId(id);
            dest.setCreatedAt(existing.getCreatedAt());
            dest.setUpdatedAt(LocalDateTime.now());
            if (dest.getSlug() == null || dest.getSlug().isEmpty()) {
                dest.setSlug(dest.getName().toLowerCase().replaceAll("[^a-z0-9]+", "-").replaceAll("-+", "-"));
            }
            return ResponseEntity.ok(repo.save(dest));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<?> delete(@PathVariable Long id) {
        if (!repo.existsById(id)) return ResponseEntity.notFound().build();
        jdbc.update("DELETE FROM menu_destinations WHERE destination_id = ?", id);
        repo.deleteById(id);
        return ResponseEntity.ok(Map.of("message", "Deleted"));
    }

    // Text file import endpoint
    @PostMapping("/import-text")
    public ResponseEntity<?> importText(@RequestBody Map<String, String> body) {
        String content = body.get("content");
        if (content == null || content.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "No content provided"));
        }
        return ResponseEntity.ok(Map.of("content", content.trim()));
    }
}
