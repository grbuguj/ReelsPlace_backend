package com.example.reelsplace.global.config;

import com.example.reelsplace.global.security.CustomOAuth2UserService;
import com.example.reelsplace.global.security.JwtAuthenticationFilter;
import com.example.reelsplace.global.security.OAuth2AuthenticationSuccessHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Spring Security 설정
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final CustomOAuth2UserService customOAuth2UserService;
    private final OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // CSRF 비활성화 (JWT 사용)
                .csrf(csrf -> csrf.disable())
                
                // 세션 사용 안함
                .sessionManagement(session -> 
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                
                // 요청 권한 설정
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/oauth2/**").permitAll()              // OAuth2 인증 (Spring 기본 경로)
                        .requestMatchers("/login/**").permitAll()             // 로그인 페이지
                        .requestMatchers("/api/v1/internal/**").permitAll()      // Internal API (TODO: 별도 인증 필요)
                        .requestMatchers("/test.html").permitAll()
                        .requestMatchers("/address-extraction-test.html").permitAll()
                        .requestMatchers("/reels-test.html").permitAll()
                        .requestMatchers("/place-pipeline.html").permitAll()
                        .requestMatchers("/new_test.html").permitAll()
                        .requestMatchers("/privacy.html").permitAll()
                        .requestMatchers("/terms.html").permitAll()
                        .requestMatchers("/deletion.html").permitAll()
                        .anyRequest().authenticated()                            // 나머지는 인증 필요
                )
                
                // OAuth2 로그인 설정
                .oauth2Login(oauth2 -> oauth2
                        .userInfoEndpoint(userInfo -> 
                                userInfo.userService(customOAuth2UserService))
                        .successHandler(oAuth2AuthenticationSuccessHandler)
                )
                
                // JWT 필터 추가
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
