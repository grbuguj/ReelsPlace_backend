package com.example.reelsplace.api.internal.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 장소 생성 요청
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreatePlacesRequest {
    private List<String> addresses;
}
