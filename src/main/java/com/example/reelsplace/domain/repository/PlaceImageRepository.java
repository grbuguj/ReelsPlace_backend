package com.example.reelsplace.domain.repository;

import com.example.reelsplace.domain.entity.PlaceImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PlaceImageRepository extends JpaRepository<PlaceImage, Long> {
    
    List<PlaceImage> findByPlaceIdOrderBySortOrder(Long placeId);
}
