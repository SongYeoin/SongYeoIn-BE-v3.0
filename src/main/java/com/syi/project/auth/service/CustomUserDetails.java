package com.syi.project.auth.service;

import com.syi.project.auth.dto.AuthUserDTO;
import java.util.Collection;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@RequiredArgsConstructor
@Slf4j
public class CustomUserDetails implements UserDetails {

  private final AuthUserDTO authUser;

  /**
   * 사용자 권한 정보 반환
   *
   * @return GrantedAuthority 타입의 권한 목록
   */
  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    log.debug("사용자 권한 정보 반환 - 역할: {}", authUser.getRole());
    return List.of(new SimpleGrantedAuthority(authUser.getRole().name()));
  }

  @Override
  public String getPassword() {
    return null;  // 패스워드 필요하지 않음
  }

  @Override
  public String getUsername() {
    return authUser.getMemberId();
  }

  @Override
  public boolean isAccountNonExpired() {
    return true;
  }

  @Override
  public boolean isAccountNonLocked() {
    return true;
  }

  @Override
  public boolean isCredentialsNonExpired() {
    return true;
  }

  @Override
  public boolean isEnabled() {
    return true;
  }
}
