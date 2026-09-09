package com.travelvista.controller;

import com.travelvista.model.Activity;
import com.travelvista.repository.ActivityRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/activities")
public class ActivityController {

    private final ActivityRepository repo;

    public ActivityController(ActivityRepository repo) { this.repo = repo; }

    @GetMapping
    public ResponseEntity<List<Activity>> getAll(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String status) {
        if ("published".equals(status)) return ResponseEntity.ok(repo.findByStatus("published"));
        if (category != null) return ResponseEntity.ok(repo.findByCategory(category));
        return ResponseEntity.ok(repo.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        return repo.findById(id).map(a -> ResponseEntity.ok((Object) a))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/slug/{slug}")
    public ResponseEntity<?> getBySlug(@PathVariable String slug) {
        return repo.findBySlug(slug).map(a -> ResponseEntity.ok((Object) a))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Activity> create(@RequestBody Activity activity) {
        activity.setCreatedAt(LocalDateTime.now());
        activity.setUpdatedAt(LocalDateTime.now());
        if (activity.getSlug() == null || activity.getSlug().isEmpty()) {
            activity.setSlug(activity.getName().toLowerCase().replaceAll("[^a-z0-9]+", "-").replaceAll("-+", "-"));
        }
        return ResponseEntity.ok(repo.save(activity));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody Activity activity) {
        return repo.findById(id).map(existing -> {
            activity.setId(id);
            activity.setCreatedAt(existing.getCreatedAt());
            activity.setUpdatedAt(LocalDateTime.now());
            if (activity.getSlug() == null || activity.getSlug().isEmpty()) {
                activity.setSlug(activity.getName().toLowerCase().replaceAll("[^a-z0-9]+", "-").replaceAll("-+", "-"));
            }
            return ResponseEntity.ok(repo.save(activity));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        repo.deleteById(id);
        return ResponseEntity.ok(Map.of("message", "Deleted"));
    }
}
