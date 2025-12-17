package com.example.reelsplace.domain.repository;

import com.example.reelsplace.domain.entity.Place;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface PlaceRepository extends JpaRepository<Place, Long> {
    
    @Query("SELECT p FROM Place p LEFT JOIN FETCH p.images WHERE p.user.id = :userId ORDER BY p.createdAt DESC")
    Page<Place> findByUserIdWithImagesOrderByCreatedAtDesc(Long userId, Pageable pageable);
    
    Optional<Place> findByIdAndUserId(Long id, Long userId);
    
    Optional<Place> findByUserIdAndGooglePlaceId(Long userId, String googlePlaceId);
    
    boolean existsByUserIdAndGooglePlaceId(Long userId, String googlePlaceId);
    
    long countByUserId(Long userId);
}
