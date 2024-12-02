package com.syi.project.common.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

  @Override
  public void addCorsMappings(CorsRegistry registry) {
    registry.addMapping("/**") // 모든 경로에 대해 CORS를 허용
        .allowedOrigins("https://d3b6eekx5tpsjk.cloudfront.net") //React 앱이 실행되는 주소를 허용
        .allowedMethods("GET", "POST","PATCH", "PUT", "DELETE", "OPTIONS") // 허용할 HTTP 메서드를 지정
        .allowedHeaders("*") // 모든 헤더 허용
        .exposedHeaders("Authorization", "Refresh-Token") // 클라이언트에서 접근 가능한 헤더
        .allowCredentials(true) // 인증 정보를 포함한 요청을 허용합니다. (예: 쿠키, HTTP 인증 정보)
        .maxAge(3600);
  }
}
