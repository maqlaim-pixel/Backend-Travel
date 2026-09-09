package com.travelvista.controller;

import com.travelvista.model.Hotel;
import com.travelvista.repository.HotelRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/hotels")
public class HotelController {

    private final HotelRepository repo;

    public HotelController(HotelRepository repo) { this.repo = repo; }

    @GetMapping
    public ResponseEntity<List<Hotel>> getAll(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String status) {
        if ("published".equals(status)) return ResponseEntity.ok(repo.findByStatus("published"));
        if (category != null) return ResponseEntity.ok(repo.findByCategory(category));
        return ResponseEntity.ok(repo.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        return repo.findById(id).map(h -> ResponseEntity.ok((Object) h))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/slug/{slug}")
    public ResponseEntity<?> getBySlug(@PathVariable String slug) {
        return repo.findBySlug(slug).map(h -> ResponseEntity.ok((Object) h))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Hotel> create(@RequestBody Hotel hotel) {
        hotel.setCreatedAt(LocalDateTime.now());
        hotel.setUpdatedAt(LocalDateTime.now());
        if (hotel.getSlug() == null || hotel.getSlug().isEmpty()) {
            hotel.setSlug(hotel.getName().toLowerCase().replaceAll("[^a-z0-9]+", "-").replaceAll("-+", "-"));
        }
        return ResponseEntity.ok(repo.save(hotel));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody Hotel hotel) {
        return repo.findById(id).map(existing -> {
            hotel.setId(id);
            hotel.setCreatedAt(existing.getCreatedAt());
            hotel.setUpdatedAt(LocalDateTime.now());
            if (hotel.getSlug() == null || hotel.getSlug().isEmpty()) {
                hotel.setSlug(hotel.getName().toLowerCase().replaceAll("[^a-z0-9]+", "-").replaceAll("-+", "-"));
            }
            return ResponseEntity.ok(repo.save(hotel));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        repo.deleteById(id);
        return ResponseEntity.ok(Map.of("message", "Deleted"));
    }
}
