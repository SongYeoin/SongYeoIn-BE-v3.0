package com.syi.project.common.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.IntegerSchema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.media.IntegerSchema;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

import java.util.stream.Collectors;
import org.springdoc.core.annotations.ParameterObject;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springdoc.core.models.GroupedOpenApi;
import java.util.stream.Collectors;

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

  // 페이징 파라미터를 위한 별도의 ParameterObject 설정 추가
  @Bean
  public GroupedOpenApi publicApi() {
    return GroupedOpenApi.builder()
        .group("public")
        .addOpenApiCustomizer(openApi -> {
          openApi.getPaths().values().forEach(pathItem -> {
            pathItem.readOperations().forEach(operation -> {
              if (operation.getParameters() != null) {  // null 체크 추가
                operation.setParameters(operation.getParameters().stream()
                    .filter(parameter -> !parameter.getName().startsWith("pageable"))
                    .collect(Collectors.toList()));
              }
            });
          });
        })
        .pathsToMatch("/**")
        .build();
  }
}
