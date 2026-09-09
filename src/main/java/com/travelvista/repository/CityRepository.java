package com.travelvista.repository;

import com.travelvista.model.City;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface CityRepository extends JpaRepository<City, Long> {
    Optional<City> findBySlug(String slug);
    List<City> findByStatusAndStateIdOrderBySortOrder(String status, Long stateId);
    List<City> findByStateIdOrderBySortOrder(Long stateId);
    List<City> findByStatusOrderBySortOrder(String status);
    List<City> findByStatusAndFeaturedOrderBySortOrder(String status, Boolean featured);
    long countByStatus(String status);
    boolean existsBySlugAndIdNot(String slug, Long id);
    boolean existsBySlug(String slug);
}
