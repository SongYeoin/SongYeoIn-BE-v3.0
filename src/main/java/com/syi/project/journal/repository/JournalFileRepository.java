package com.syi.project.journal.repository;

import com.syi.project.journal.entity.JournalFile;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.stereotype.Repository;

@Repository
public interface JournalFileRepository extends JpaRepository<JournalFile, Long> {
  Optional<JournalFile> findByJournalId(Long journalId);  // List에서 Optional로 변경
  void deleteByJournalId(Long journalId);
}