package com.travelvista.repository;

import com.travelvista.model.Activity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.Optional;

public interface ActivityRepository extends JpaRepository<Activity, Long> {
    Optional<Activity> findBySlug(String slug);
    List<Activity> findByStatus(String status);
    List<Activity> findByCategory(String category);

    @Query("SELECT COUNT(a) FROM Activity a WHERE a.status = :status")
    long countByStatus(String status);

    boolean existsBySlug(String slug);
}
