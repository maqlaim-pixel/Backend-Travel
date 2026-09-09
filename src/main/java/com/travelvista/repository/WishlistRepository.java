package com.travelvista.repository;

import com.travelvista.model.Wishlist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

public interface WishlistRepository extends JpaRepository<Wishlist, Long> {
    List<Wishlist> findByUserIdOrderByCreatedAtDesc(Long userId);
    Optional<Wishlist> findByUserIdAndTravelPackageId(Long userId, Long packageId);
    
    @Query("SELECT CASE WHEN COUNT(w) > 0 THEN true ELSE false END FROM Wishlist w WHERE w.user.id = :userId AND w.travelPackage.id = :packageId")
    boolean existsByUserIdAndTravelPackageId(Long userId, Long packageId);
    
    @Transactional
    @Modifying
    @Query("DELETE FROM Wishlist w WHERE w.user.id = :userId AND w.travelPackage.id = :packageId")
    void deleteByUserIdAndTravelPackageId(Long userId, Long packageId);
    
    long countByUserId(Long userId);
}
