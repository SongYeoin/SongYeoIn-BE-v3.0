package com.syi.project.journal.repository;

import com.syi.project.journal.entity.Journal;
import java.time.LocalDate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface JournalRepository extends JpaRepository<Journal, Long>, JournalRepositoryCustom {

  List<Journal> findByCourseIdAndIsDeletedFalse(Long courseId);
  List<Journal> findByCourseIdAndMemberIdAndIsDeletedFalse(Long courseId, Long memberId);
  Optional<Journal> findByIdAndMemberIdAndIsDeletedFalse(Long id, Long memberId);
  boolean existsByMemberIdAndCourseIdAndEducationDate(Long memberId, Long courseId, LocalDate educationDate);

  @Query("SELECT j FROM Journal j LEFT JOIN FETCH j.journalFile jf LEFT JOIN FETCH jf.file f WHERE j.id = :journalId AND j.isDeleted = false")
  Optional<Journal> findByIdWithFile(@Param("journalId") Long journalId);

  @Query("SELECT j FROM Journal j LEFT JOIN FETCH j.journalFile jf LEFT JOIN FETCH jf.file f WHERE j.id IN :journalIds AND j.isDeleted = false")
  List<Journal> findAllByIdsWithFiles(@Param("journalIds") List<Long> journalIds);
}