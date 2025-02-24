package com.syi.project.common.exception.handler;

import com.syi.project.auth.entity.Member;
import com.syi.project.common.enums.Role;
import com.syi.project.common.exception.ErrorCode;
import com.syi.project.common.exception.InvalidRequestException;
import com.syi.project.journal.entity.Journal;
import com.syi.project.journal.repository.JournalRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class JournalErrorHandler {
  private final JournalRepository journalRepository;

  public Journal validateJournalExists(Long journalId) {
    return journalRepository.findByIdWithFile(journalId)
        .orElseThrow(() -> {
          log.error("교육일지를 찾을 수 없음 - journalId: {}", journalId);
          return new InvalidRequestException(ErrorCode.JOURNAL_NOT_FOUND);
        });
  }

  public void validateJournalFile(Journal journal) {
    if (journal.getJournalFile() == null) {
      log.error("교육일지에 첨부된 파일이 없음 - journalId: {}", journal.getId());
      throw new InvalidRequestException(ErrorCode.JOURNAL_FILE_NOT_FOUND);
    }
  }

  public void validateAccess(Journal journal, Member member) {
    if (!(member.getRole() == Role.ADMIN || journal.getMember().getId().equals(member.getId()))) {
      log.error("교육일지 접근 권한 없음 - journalId: {}, memberId: {}", journal.getId(), member.getId());
      throw new InvalidRequestException(ErrorCode.JOURNAL_ACCESS_DENIED);
    }
  }
}