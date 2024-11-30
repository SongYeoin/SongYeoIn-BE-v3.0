package com.syi.project.common.config;

import com.syi.project.auth.repository.JwtBlacklistRepository;
import com.syi.project.auth.service.CustomUserDetailsService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@Slf4j
public class SecurityConfig {

  private final CustomUserDetailsService customUserDetailsService;
  private final JwtProvider jwtProvider;
  private final JwtBlacklistRepository jwtBlacklistRepository;

  // JWT 인증 필터 Bean 설정
  @Bean
  public JwtAuthenticationFilter jwtAuthenticationFilter() {
    return new JwtAuthenticationFilter(jwtProvider, customUserDetailsService, jwtBlacklistRepository);
  }

  // PasswordEncoder Bean 설정
  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  /**
   * SecurityFilterChain 을 구성하여 스프링 시큐리티 설정을 정의. JWT 기반의 인증 방식을 사용하며 특정 경로에 대한 접근을 허용하거나 인증을 요구
   *
   * @param http HttpSecurity 객체
   * @return SecurityFilterChain 보안 설정 체인
   * @throws Exception 예외 발생 시 처리
   */
  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
        .csrf(AbstractHttpConfigurer::disable) // CSRF 보호 비활성화 (JWT 인증을 사용할 때 일반적으로 사용되지 않음)
        .cors(Customizer.withDefaults()) // CORS 설정 활성화
        .sessionManagement(session -> session.sessionCreationPolicy(
            SessionCreationPolicy.STATELESS)) // 세션을 사용하지 않고 JWT 인증 사용
        .authorizeHttpRequests(authorize -> authorize
            // 인증 없이 접근 가능한 경로 설정
            .requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**",
                "/webjars/**", "/", "/admin/member/**", "/admin/course/**", "/admin/schedule/**", "/admin/attendance/**","/member/**", "/club/**", "/admin/club/**", "/admin/notice/**", "/notice/**", "/refresh","/api/**", "/journals/**", "/admin/journals/**").permitAll()
            // 해당 경로는 인증 필요
            .requestMatchers("/jwt/test").authenticated()
            // 관리자 전용 엔드포인트 접근 설정 예시
            .requestMatchers("/admin/**").hasRole("ADMIN")
            // 나머지 모든 요청은 인증을 오구
            .anyRequest().authenticated()
        )
        .exceptionHandling(exceptions -> exceptions
            .authenticationEntryPoint((request, response, authException) -> response.sendError(
                HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized"))
            .accessDeniedHandler((request, response, accessDeniedException) -> response.sendError(
                HttpServletResponse.SC_FORBIDDEN, "Forbidden"))
        )
        .addFilterBefore(jwtAuthenticationFilter(),
            UsernamePasswordAuthenticationFilter.class);

    return http.build();
  }

  /*@Bean
  public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();
    configuration.setAllowedOrigins(List.of("http://localhost:3000")); // 프론트엔드 주소
    configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
    configuration.setAllowedHeaders(List.of("*"));
    configuration.setExposedHeaders(List.of("Authorization"));

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);
    return source;
  }*/
}