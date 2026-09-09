package com.travelvista.repository;

import com.travelvista.model.TravelPackage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.Optional;

public interface PackageRepository extends JpaRepository<TravelPackage, Long> {
    Optional<TravelPackage> findBySlug(String slug);
    List<TravelPackage> findByStatus(String status);
    List<TravelPackage> findByStatusAndFeatured(String status, Boolean featured);
    List<TravelPackage> findByCategory(String category);

    @Query("SELECT COUNT(p) FROM TravelPackage p")
    long countAll();

    @Query("SELECT COUNT(p) FROM TravelPackage p WHERE p.status = 'published'")
    long countPublished();

    @Query("SELECT COUNT(p) FROM TravelPackage p WHERE p.featured = true")
    long countFeatured();
}
