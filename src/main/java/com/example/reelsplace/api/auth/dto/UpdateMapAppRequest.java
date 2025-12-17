package com.example.reelsplace.api.auth.dto;

import com.example.reelsplace.domain.enums.MapApp;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 기본 지도 앱 변경 요청 DTO
 */
@Getter
@NoArgsConstructor
public class UpdateMapAppRequest {
    
    @NotNull(message = "지도 앱은 필수입니다.")
    private MapApp defaultMapApp;
}
