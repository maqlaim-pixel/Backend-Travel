package com.travelvista.repository;

import com.travelvista.model.Lead;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface LeadRepository extends JpaRepository<Lead, Long> {
    List<Lead> findByStatus(String status);
    List<Lead> findAllByOrderByCreatedAtDesc();

    @Query("SELECT COUNT(l) FROM Lead l")
    long countAll();

    @Query("SELECT COUNT(l) FROM Lead l WHERE l.status = 'new'")
    long countNew();

    @Query("SELECT l.status, COUNT(l) FROM Lead l GROUP BY l.status")
    List<Object[]> countByStatus();
}
