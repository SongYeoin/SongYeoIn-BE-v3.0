package com.syi.project.auth.repository;

import com.syi.project.auth.entity.Member;
import com.syi.project.common.enums.CheckStatus;
import com.syi.project.common.enums.Role;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MemberRepository extends JpaRepository<Member, Long> {

  // 아이디 중복 체크
  boolean existsByUsername(String username);

  // 이메일 중복 체크
  boolean existsByEmail(String email);

  Optional<Member> findByUsernameAndIsDeletedFalse(String username);

  Optional<Member> findByIdAndIsDeletedFalse(Long id);

  // 회원 목록 조회
  @Query("SELECT m FROM Member m WHERE (:checkStatus IS NULL OR m.checkStatus = :checkStatus) " +
      "AND (:role IS NULL OR m.role = :role) AND m.isDeleted = false")
  Page<Member> findByStatusAndRole(
      @Param("checkStatus") CheckStatus checkStatus,
      @Param("role") Role role,
      Pageable pageable
  );
}