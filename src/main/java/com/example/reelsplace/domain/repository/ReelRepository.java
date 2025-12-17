package com.example.reelsplace.domain.repository;

import com.example.reelsplace.domain.entity.Reel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ReelRepository extends JpaRepository<Reel, Long> {
    
    Page<Reel> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
    
    boolean existsByUserIdAndReelUrl(Long userId, String reelUrl);
    
    Optional<Reel> findByIdAndUserId(Long id, Long userId);
    
    long countByUserId(Long userId);
}
