package com.travelvista.repository;

import com.travelvista.model.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByUserIdOrderByCreatedAtDesc(Long userId);
    List<Booking> findAllByOrderByCreatedAtDesc();
    List<Booking> findByStatus(String status);
    long countByUserId(Long userId);
    @Query("SELECT COUNT(b) FROM Booking b")
    long countAll();
    @Query("SELECT COUNT(b) FROM Booking b WHERE b.status = :status")
    long countByStatus(String status);
}
