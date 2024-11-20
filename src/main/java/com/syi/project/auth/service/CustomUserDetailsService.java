package com.syi.project.auth.service;

import com.syi.project.auth.dto.AuthUserDTO;
import com.syi.project.auth.entity.Member;
import com.syi.project.auth.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomUserDetailsService implements UserDetailsService {

  private final MemberRepository memberRepository;


  /**
   * 사용자 ID를 통해 사용자 정보를 조회하고 UserDetails 로 반환
   *
   * @param username 로그인 아이디(username)
   * @return UserDetails 타입의 사용자 정보 객체
   * @throws UsernameNotFoundException 사용자 정보가 없을 경우 예외 발생
   */
  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    log.debug("사용자 정보 조회 시작 - 사용자 아이디(username): {}", username);

    //username으로 사용자 조회
    Member member = memberRepository.findByUsernameAndIsDeletedFalse(username)
        .orElseThrow(() -> {
          log.warn("삭제되었거나 존재하지 않는 사용자 아이디(username): {}", username);
          return new UsernameNotFoundException("사용자를 찾을 수 없거나 삭제된 계정입니다: " + username);
        });

    log.info("사용자 정보 조회 성공 - 사용자 아이디(username): {}", member.getUsername());

    // 엔티티를 DTO로 변환하여 반환
    AuthUserDTO authUser = AuthUserDTO.fromEntity(member);
    return new CustomUserDetails(authUser);
  }

  /**
   * 사용자 ID를 통해 사용자 정보를 조회하고 UserDetails 로 반환
   *
   * @param id 사용자 기본키 ID
   * @return UserDetails 객체
   * @throws UsernameNotFoundException 사용자 정보를 찾을 수 없을 때 발생
   */
  public UserDetails loadUserById(Long id) throws UsernameNotFoundException {
    log.debug("사용자 정보 조회 시작 - 사용자 ID: {}", id);

    Member member = memberRepository.findByIdAndIsDeletedFalse(id)
        .orElseThrow(() -> {
          log.warn("삭제되었거나 존재하지 않는 사용자 ID: {}", id);
          return new UsernameNotFoundException("사용자를 찾을 수 없거나 삭제된 계정입니다: " + id);
        });

    log.info("사용자 정보 조회 성공 - 사용자 ID: {}", member.getId());

    // 엔티티를 DTO로 변환하여 반환
    AuthUserDTO authUser = AuthUserDTO.fromEntity(member);
    return new CustomUserDetails(authUser);
  }
}
