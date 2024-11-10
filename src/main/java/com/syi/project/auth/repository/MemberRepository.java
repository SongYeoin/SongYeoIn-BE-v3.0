package com.syi.project.auth.repository;

import com.syi.project.auth.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member, Long> {

  // 아이디 중복 체크
  boolean existsByMemberId(String memberId);

  // 이메일 중복 체크
  boolean existsByEmail(String email);

}
