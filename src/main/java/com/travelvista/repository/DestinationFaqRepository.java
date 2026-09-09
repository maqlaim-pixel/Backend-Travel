package com.travelvista.repository;

import com.travelvista.model.DestinationFaq;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface DestinationFaqRepository extends JpaRepository<DestinationFaq, Long> {
    List<DestinationFaq> findByDestinationIdAndIsActiveTrueOrderBySortOrderAsc(Long destinationId);
}
