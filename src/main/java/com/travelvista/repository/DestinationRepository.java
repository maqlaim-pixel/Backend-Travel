package com.travelvista.repository;

import com.travelvista.model.Destination;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.Optional;

public interface DestinationRepository extends JpaRepository<Destination, Long> {
    Optional<Destination> findBySlug(String slug);
    List<Destination> findByStatus(String status);
    List<Destination> findByStatusOrderBySortOrderAsc(String status);
    List<Destination> findByType(String type);
    List<Destination> findByFeaturedTrue();

    @Query("SELECT COUNT(d) FROM Destination d WHERE d.status = :status")
    long countByStatus(String status);

    boolean existsBySlug(String slug);
}
