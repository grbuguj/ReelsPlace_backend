package com.example.reelsplace.api.internal.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * Google Places API 응답 DTO
 */
@Data
public class GooglePlacesResponse {
    
    private List<PlaceResult> results;
    private String status;
    
    @Data
    public static class PlaceResult {
        @JsonProperty("place_id")
        private String placeId;
        
        private String name;
        
        @JsonProperty("formatted_address")
        private String formattedAddress;
        
        private BigDecimal rating;
        
        @JsonProperty("user_ratings_total")
        private Integer userRatingsTotal;
        
        private List<Photo> photos;
    }
    
    @Data
    public static class Photo {
        @JsonProperty("photo_reference")
        private String photoReference;
        
        private Integer height;
        private Integer width;
    }
}
