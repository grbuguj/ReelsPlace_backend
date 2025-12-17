package com.example.reelsplace.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

/**
 * 에러 코드 정의
 */
@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    
    // 400 Bad Request
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "잘못된 입력값입니다."),
    INVALID_REEL_URL(HttpStatus.BAD_REQUEST, "잘못된 릴스 URL 형식입니다."),
    INVALID_MAP_APP(HttpStatus.BAD_REQUEST, "잘못된 지도 앱입니다."),
    
    // 401 Unauthorized
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "인증이 필요합니다."),
    
    // 403 Forbidden
    FORBIDDEN(HttpStatus.FORBIDDEN, "접근 권한이 없습니다."),
    
    // 404 Not Found
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."),
    REEL_NOT_FOUND(HttpStatus.NOT_FOUND, "릴스를 찾을 수 없습니다."),
    PLACE_NOT_FOUND(HttpStatus.NOT_FOUND, "장소를 찾을 수 없습니다."),
    
    // 409 Conflict
    REEL_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 저장된 릴스입니다."),
    
    // 500 Internal Server Error
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 오류가 발생했습니다.");
    
    private final HttpStatus status;
    private final String message;
}
