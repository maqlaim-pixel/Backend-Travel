package com.travelvista.repository;

import com.travelvista.model.Enquiry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface EnquiryRepository extends JpaRepository<Enquiry, Long> {
    List<Enquiry> findByUserIdOrderByCreatedAtDesc(Long userId);
    long countByUserId(Long userId);
    long countByUserIdAndStatus(Long userId, String status);
    List<Enquiry> findAllByOrderByCreatedAtDesc();
    @Query("SELECT COUNT(e) FROM Enquiry e")
    long countAll();
    long countByStatus(String status);

    @Query("SELECT COUNT(e) FROM Enquiry e WHERE e.status = 'pending'")
    long countPending();

    @Query("SELECT COUNT(e) FROM Enquiry e WHERE e.status = 'confirmed'")
    long countConfirmed();

    boolean existsByEnquiryRef(String enquiryRef);
}
