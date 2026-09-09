package com.travelvista.repository;

import com.travelvista.model.Country;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface CountryRepository extends JpaRepository<Country, Long> {
    Optional<Country> findBySlug(String slug);
    List<Country> findByStatusAndIsIndianOrderBySortOrder(String status, Boolean isIndian);
    List<Country> findByStatusOrderBySortOrder(String status);
    List<Country> findByStatusAndFeaturedOrderBySortOrder(String status, Boolean featured);
    boolean existsBySlugAndIdNot(String slug, Long id);
    boolean existsBySlug(String slug);
}
