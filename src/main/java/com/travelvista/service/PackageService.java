package com.travelvista.service;

import com.travelvista.dto.PackageRequest;
import com.travelvista.model.TravelPackage;
import com.travelvista.repository.PackageRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PackageService {

    private final PackageRepository packageRepository;

    public PackageService(PackageRepository packageRepository) {
        this.packageRepository = packageRepository;
    }

    public List<TravelPackage> getAll() {
        return packageRepository.findAll();
    }

    public List<TravelPackage> getPublished() {
        return packageRepository.findByStatus("published");
    }

    public List<TravelPackage> getFeatured() {
        return packageRepository.findByStatusAndFeatured("published", true);
    }

    public List<TravelPackage> getByCategory(String category) {
        return packageRepository.findByCategory(category);
    }

    public Optional<TravelPackage> getById(Long id) {
        return packageRepository.findById(id);
    }

    public Optional<TravelPackage> getBySlug(String slug) {
        return packageRepository.findBySlug(slug);
    }

    public TravelPackage create(PackageRequest req) {
        TravelPackage pkg = new TravelPackage();
        copyFields(req, pkg);
        if (pkg.getSlug() == null || pkg.getSlug().isEmpty()) {
            pkg.setSlug(slugify(req.getTitle()));
        }
        return packageRepository.save(pkg);
    }

    public TravelPackage update(Long id, PackageRequest req) {
        TravelPackage pkg = packageRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Package not found"));
        copyFields(req, pkg);
        return packageRepository.save(pkg);
    }

    public void delete(Long id) {
        packageRepository.deleteById(id);
    }

    private void copyFields(PackageRequest req, TravelPackage pkg) {
        if (req.getTitle() != null) pkg.setTitle(req.getTitle());
        if (req.getSlug() != null) pkg.setSlug(req.getSlug());
        if (req.getDescription() != null) pkg.setDescription(req.getDescription());
        if (req.getShortDescription() != null) pkg.setShortDescription(req.getShortDescription());
        if (req.getDestination() != null) pkg.setDestination(req.getDestination());
        if (req.getState() != null) pkg.setState(req.getState());
        if (req.getCountry() != null) pkg.setCountry(req.getCountry());
        if (req.getDurationDays() != null) pkg.setDurationDays(req.getDurationDays());
        if (req.getDurationNights() != null) pkg.setDurationNights(req.getDurationNights());
        if (req.getStartingPrice() != null) pkg.setStartingPrice(req.getStartingPrice());
        if (req.getCurrency() != null) pkg.setCurrency(req.getCurrency());
        if (req.getCoverImage() != null) pkg.setCoverImage(req.getCoverImage());
        if (req.getHighlights() != null) pkg.setHighlights(req.getHighlights());
        if (req.getInclusions() != null) pkg.setInclusions(req.getInclusions());
        if (req.getExclusions() != null) pkg.setExclusions(req.getExclusions());
        if (req.getCategory() != null) pkg.setCategory(req.getCategory());
        if (req.getTags() != null) pkg.setTags(req.getTags());
        if (req.getStatus() != null) pkg.setStatus(req.getStatus());
        if (req.getFeatured() != null) pkg.setFeatured(req.getFeatured());
        if (req.getRating() != null) pkg.setRating(req.getRating());
    }

    private String slugify(String text) {
        if (text == null) return "";
        return text.toLowerCase()
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-")
                .replaceAll("-+", "-")
                .trim();
    }
}
