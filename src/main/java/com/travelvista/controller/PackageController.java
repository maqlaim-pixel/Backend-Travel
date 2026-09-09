package com.travelvista.controller;

import com.travelvista.dto.PackageRequest;
import com.travelvista.model.TravelPackage;
import com.travelvista.service.PackageService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/packages")
public class PackageController {

    private final PackageService packageService;

    public PackageController(PackageService packageService) {
        this.packageService = packageService;
    }

    // ── Public endpoints ──────────────────────────────────────────────

    @GetMapping
    public ResponseEntity<List<TravelPackage>> getAll(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String status) {
        if ("featured".equals(status)) {
            return ResponseEntity.ok(packageService.getFeatured());
        }
        if (category != null) {
            return ResponseEntity.ok(packageService.getByCategory(category));
        }
        if ("published".equals(status)) {
            return ResponseEntity.ok(packageService.getPublished());
        }
        return ResponseEntity.ok(packageService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        return packageService.getById(id)
                .map(pkg -> ResponseEntity.ok((Object) pkg))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/slug/{slug}")
    public ResponseEntity<?> getBySlug(@PathVariable String slug) {
        return packageService.getBySlug(slug)
                .map(pkg -> ResponseEntity.ok((Object) pkg))
                .orElse(ResponseEntity.notFound().build());
    }

    // ── Admin endpoints ───────────────────────────────────────────────

    @PostMapping
    public ResponseEntity<TravelPackage> create(@RequestBody PackageRequest request) {
        return ResponseEntity.ok(packageService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody PackageRequest request) {
        try {
            return ResponseEntity.ok(packageService.update(id, request));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        packageService.delete(id);
        return ResponseEntity.ok(Map.of("message", "Package deleted"));
    }
}
