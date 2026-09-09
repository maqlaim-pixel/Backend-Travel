package com.travelvista.repository;

import com.travelvista.model.Blog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.Optional;

public interface BlogRepository extends JpaRepository<Blog, Long> {
    Optional<Blog> findBySlug(String slug);
    List<Blog> findByStatus(String status);
    List<Blog> findByCategory(String category);

    @Query("SELECT COUNT(b) FROM Blog b WHERE b.status = :status")
    long countByStatus(String status);

    boolean existsBySlug(String slug);
}
