package com.syi.project.journal.repository;

import com.syi.project.journal.entity.JournalFile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public interface JournalFileRepository extends JpaRepository<JournalFile, Long> {
  List<JournalFile> findByJournalId(Long journalId);
  void deleteByJournalId(Long journalId);
}