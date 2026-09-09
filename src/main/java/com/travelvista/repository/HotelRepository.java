package com.travelvista.repository;

import com.travelvista.model.Hotel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.Optional;

public interface HotelRepository extends JpaRepository<Hotel, Long> {
    Optional<Hotel> findBySlug(String slug);
    List<Hotel> findByStatus(String status);
    List<Hotel> findByCategory(String category);
    List<Hotel> findByFeaturedTrue();

    @Query("SELECT COUNT(h) FROM Hotel h WHERE h.status = :status")
    long countByStatus(String status);

    boolean existsBySlug(String slug);
}
