package com.example.reelsplace.domain.repository;

import com.example.reelsplace.domain.entity.ReelPlace;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ReelPlaceRepository extends JpaRepository<ReelPlace, Long> {
    
    @Query("SELECT rp FROM ReelPlace rp JOIN FETCH rp.reel WHERE rp.place.id = :placeId")
    List<ReelPlace> findByPlaceIdWithReel(Long placeId);
    
    @Query("SELECT rp FROM ReelPlace rp JOIN FETCH rp.place WHERE rp.reel.id = :reelId")
    List<ReelPlace> findByReelIdWithPlace(Long reelId);
}
