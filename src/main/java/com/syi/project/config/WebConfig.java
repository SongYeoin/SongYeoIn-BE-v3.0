package com.syi.project.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
  @Override
  public void addCorsMappings(CorsRegistry registry) {
    registry.addMapping("/**") // 모든 경로에 대해 CORS를 허용
        .allowedOriginPatterns("*") // 모든 출처 허용 (배포 시에는 특정 출처만 허용 권장)
        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // 허용할 HTTP 메서드를 지정
        .allowedHeaders("Authorization", "Content-Type")
        .exposedHeaders("Custom-Header")
        .allowedHeaders("*") // 모든 헤더 허용
        .allowCredentials(true) // 인증 정보를 포함한 요청을 허용합니다. (예: 쿠키, HTTP 인증 정보)
        .maxAge(3600);
  }
}
