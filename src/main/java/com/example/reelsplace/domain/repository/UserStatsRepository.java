package com.example.reelsplace.domain.repository;

import com.example.reelsplace.domain.entity.UserStats;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserStatsRepository extends JpaRepository<UserStats, Long> {
    
    Optional<UserStats> findByUserId(Long userId);
}
