package com.travelvista.controller;

import com.travelvista.model.Country;
import com.travelvista.repository.CountryRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
public class CountryController {

    private final CountryRepository repo;

    public CountryController(CountryRepository repo) { this.repo = repo; }

    // ── Public ──────────────────────────────────────────────
    @GetMapping("/api/countries")
    public List<Country> listAll(@RequestParam(defaultValue = "published") String status) {
        return repo.findByStatusOrderBySortOrder(status);
    }

    @GetMapping("/api/countries/indian")
    public List<Country> listIndian(@RequestParam(defaultValue = "published") String status) {
        return repo.findByStatusAndIsIndianOrderBySortOrder(status, true);
    }

    @GetMapping("/api/countries/international")
    public List<Country> listInternational(@RequestParam(defaultValue = "published") String status) {
        return repo.findByStatusAndIsIndianOrderBySortOrder(status, false);
    }

    @GetMapping("/api/countries/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        return repo.findById(id)
            .map(c -> ResponseEntity.ok((Object) c))
            .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/api/countries/slug/{slug}")
    public ResponseEntity<?> getBySlug(@PathVariable String slug) {
        return repo.findBySlug(slug)
            .map(c -> ResponseEntity.ok((Object) c))
            .orElse(ResponseEntity.notFound().build());
    }

    // ── Admin ───────────────────────────────────────────────
    @PostMapping("/api/admin/countries")
    public ResponseEntity<?> create(@RequestBody Map<String, Object> body) {
        Country c = new Country();
        applyFields(c, body);
        if (repo.existsBySlug(c.getSlug())) {
            return ResponseEntity.badRequest().body(Map.of("error", "Slug already exists"));
        }
        return ResponseEntity.ok(repo.save(c));
    }

    @PutMapping("/api/admin/countries/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        return repo.findById(id).map(c -> {
            applyFields(c, body);
            String slug = (String) body.getOrDefault("slug", c.getSlug());
            if (repo.existsBySlugAndIdNot(slug, id)) {
                return ResponseEntity.badRequest().body((Object) Map.of("error", "Slug already exists"));
            }
            c.setSlug(slug);
            c.setUpdatedAt(LocalDateTime.now());
            return ResponseEntity.ok((Object) repo.save(c));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/api/admin/countries/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        repo.deleteById(id);
        return ResponseEntity.ok(Map.of("success", true));
    }

    private void applyFields(Country c, Map<String, Object> body) {
        if (body.containsKey("name")) c.setName((String) body.get("name"));
        if (body.containsKey("slug")) c.setSlug((String) body.get("slug"));
        if (body.containsKey("code")) c.setCode((String) body.get("code"));
        if (body.containsKey("description")) c.setDescription((String) body.get("description"));
        if (body.containsKey("image")) c.setImage((String) body.get("image"));
        if (body.containsKey("heroImage")) c.setHeroImage((String) body.get("heroImage"));
        if (body.containsKey("seoTitle")) c.setSeoTitle((String) body.get("seoTitle"));
        if (body.containsKey("seoDescription")) c.setSeoDescription((String) body.get("seoDescription"));
        if (body.containsKey("sortOrder")) c.setSortOrder((Integer) body.get("sortOrder"));
        if (body.containsKey("status")) c.setStatus((String) body.get("status"));
        if (body.containsKey("featured")) c.setFeatured((Boolean) body.get("featured"));
        if (body.containsKey("isIndian")) c.setIsIndian((Boolean) body.get("isIndian"));
    }
}
