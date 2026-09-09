package com.travelvista.repository;

import com.travelvista.model.FAQ;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface FAQRepository extends JpaRepository<FAQ, Long> {
    List<FAQ> findByEntityTypeAndStatusOrderBySortOrder(String entityType, String status);
    List<FAQ> findByStatusOrderBySortOrder(String status);
}
