package com.syi.project.journal.repository;

import com.syi.project.journal.entity.Journal;
import java.time.LocalDate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface JournalRepository extends JpaRepository<Journal, Long>, JournalRepositoryCustom {

  List<Journal> findByCourseId(Long courseId);
  List<Journal> findByCourseIdAndMemberId(Long courseId, Long memberId);
  Optional<Journal> findByIdAndMemberId(Long id, Long memberId);
  boolean existsByMemberIdAndCourseIdAndEducationDate(Long memberId, Long courseId, LocalDate educationDate);

}