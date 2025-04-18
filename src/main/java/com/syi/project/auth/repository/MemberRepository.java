package com.syi.project.auth.repository;

import com.syi.project.auth.entity.Member;
import com.syi.project.common.enums.CheckStatus;
import com.syi.project.common.enums.Role;
import java.util.List;
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

  Optional<Member> findByUsernameAndDeletedByIsNull(String username);

  Optional<Member> findByIdAndDeletedByIsNull(Long id);

  // 회원 목록 조회
  @Query("SELECT m FROM Member m " +
      "WHERE (:checkStatus IS NULL OR m.checkStatus = :checkStatus) " +
      "AND (:role IS NULL OR m.role = :role) " +
      "AND (:word IS NULL OR LOWER(m.name) LIKE LOWER(CONCAT('%', :word, '%'))) " +
      "AND m.deletedBy IS NULL " +
      "ORDER BY m.enrollDate DESC")
  Page<Member> findByStatusAndRole(
      @Param("checkStatus") CheckStatus checkStatus,
      @Param("role") Role role,
      @Param("word") String word,
      Pageable pageable
  );

  @Query("SELECT m FROM Member m WHERE m.role = 'ADMIN' AND m.deletedBy IS NULL")
  List<Member> findAdminList();


  @Query(value = "SELECT m FROM Enroll e " +
      "JOIN Member m ON e.memberId = m.id " +
      "WHERE e.courseId = :courseId AND e.deletedBy IS NULL",
      countQuery = "SELECT COUNT(m) FROM Enroll e " +
          "JOIN Member m ON e.memberId = m.id " +
          "WHERE e.courseId = :courseId AND e.deletedBy IS NULL")
  Page<Member> findMemberByCourseId(@Param("courseId") Long courseId, Pageable pageable);
}