package com.example.reelsplace.global.response;

import com.example.reelsplace.global.exception.ErrorCode;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 에러 응답 DTO
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ErrorResponse {
    
    private String message;
    
    private ErrorResponse(String message) {
        this.message = message;
    }
    
    public static ErrorResponse of(ErrorCode errorCode) {
        return new ErrorResponse(errorCode.getMessage());
    }
    
    public static ErrorResponse of(String message) {
        return new ErrorResponse(message);
    }
}
