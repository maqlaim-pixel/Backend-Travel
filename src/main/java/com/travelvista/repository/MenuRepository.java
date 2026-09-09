package com.travelvista.repository;

import com.travelvista.model.Menu;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface MenuRepository extends JpaRepository<Menu, Long> {
    List<Menu> findByStatusOrderByDisplayOrderAsc(String status);
    Optional<Menu> findBySlugAndStatus(String slug, String status);
    Optional<Menu> findBySlug(String slug);
}
