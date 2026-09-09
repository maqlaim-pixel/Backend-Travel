package com.travelvista.controller;

import com.travelvista.model.Blog;
import com.travelvista.repository.BlogRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/blogs")
public class BlogController {

    private final BlogRepository repo;

    public BlogController(BlogRepository repo) { this.repo = repo; }

    @GetMapping
    public ResponseEntity<List<Blog>> getAll(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String status) {
        if ("published".equals(status)) return ResponseEntity.ok(repo.findByStatus("published"));
        if (category != null) return ResponseEntity.ok(repo.findByCategory(category));
        return ResponseEntity.ok(repo.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        return repo.findById(id).map(b -> ResponseEntity.ok((Object) b))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/slug/{slug}")
    public ResponseEntity<?> getBySlug(@PathVariable String slug) {
        return repo.findBySlug(slug).map(b -> ResponseEntity.ok((Object) b))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Blog> create(@RequestBody Blog blog) {
        blog.setCreatedAt(LocalDateTime.now());
        blog.setUpdatedAt(LocalDateTime.now());
        if (blog.getSlug() == null || blog.getSlug().isEmpty()) {
            blog.setSlug(blog.getTitle().toLowerCase().replaceAll("[^a-z0-9]+", "-").replaceAll("-+", "-"));
        }
        return ResponseEntity.ok(repo.save(blog));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody Blog blog) {
        return repo.findById(id).map(existing -> {
            blog.setId(id);
            blog.setCreatedAt(existing.getCreatedAt());
            blog.setUpdatedAt(LocalDateTime.now());
            if (blog.getSlug() == null || blog.getSlug().isEmpty()) {
                blog.setSlug(blog.getTitle().toLowerCase().replaceAll("[^a-z0-9]+", "-").replaceAll("-+", "-"));
            }
            return ResponseEntity.ok(repo.save(blog));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        repo.deleteById(id);
        return ResponseEntity.ok(Map.of("message", "Deleted"));
    }
}
