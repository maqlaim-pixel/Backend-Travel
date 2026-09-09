package com.travelvista.repository;

import com.travelvista.model.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByTravelPackageIdOrderByCreatedAtDesc(Long packageId);
    List<Review> findAllByOrderByCreatedAtDesc();
    long countByTravelPackageId(Long packageId);
    @Query("SELECT COUNT(r) FROM Review r")
    long countAll();
    boolean existsByUserIdAndTravelPackageId(Long userId, Long packageId);
    Review findByUserIdAndTravelPackageId(Long userId, Long packageId);
}
