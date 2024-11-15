package com.syi.project.journal.repository;

import com.syi.project.journal.entity.Journal;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JournalRepository extends JpaRepository<Journal, Long> {

  List<Journal> findByCourseId(Long courseId);  // 관리자용 조회
  List<Journal> findByCourseIdAndMemberId(Long courseId, Long memberId);  // 수강생용 조회
  Optional<Journal> findByIdAndMemberId(Long id, Long memberId);  // 수정, 삭제용

  // 추후에 기능에 따라 쿼리메서드 추가하기
}
