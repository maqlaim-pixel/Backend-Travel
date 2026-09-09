package com.travelvista.repository;

import com.travelvista.model.State;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface StateRepository extends JpaRepository<State, Long> {
    Optional<State> findBySlug(String slug);
    List<State> findByStatusAndCountryIdOrderBySortOrder(String status, Long countryId);
    List<State> findByCountryIdOrderBySortOrder(Long countryId);
    List<State> findByStatusOrderBySortOrder(String status);
    List<State> findByStatusAndFeaturedOrderBySortOrder(String status, Boolean featured);
    boolean existsBySlugAndIdNot(String slug, Long id);
    boolean existsBySlug(String slug);
    long countByStatus(String status);
}
