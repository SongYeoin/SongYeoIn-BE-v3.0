package com.syi.project.common.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

  @Bean
  public OpenAPI customOpenAPI() {
    return new OpenAPI()
        .components(new Components()
            .addSecuritySchemes("BearerAuth", new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT"))) // JWT 형식의 Bearer 토큰 인증 스키마
        .info(new Info()
            .title("SongYeoIn API")
            .description("송여인 사이트에 사용되는 API의 기능들을 설명")
            .version("3.0")
            .contact(new Contact()
                .name("SongYeoIn")
                .email("email@example.com")
                .url("https://www.example.com"))) // 연락처 정보
        .addSecurityItem(new SecurityRequirement().addList("BearerAuth")); // 보안 요구 사항 추가
  }
}
