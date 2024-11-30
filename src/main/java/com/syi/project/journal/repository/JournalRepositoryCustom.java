package com.syi.project.journal.repository;

import com.syi.project.journal.entity.Journal;
import com.syi.project.common.entity.Criteria;
import org.springframework.data.domain.Page;
import java.time.LocalDate;
import org.springframework.data.domain.Pageable;

public interface JournalRepositoryCustom {

  Page<Journal> findAllWithConditions(Criteria criteria, Long memberId, LocalDate startDate, LocalDate endDate);

  // 수강생용 검색 추가
  Page<Journal> searchJournalsForStudent(
      Long memberId,
      Long courseId,
      LocalDate startDate,
      LocalDate endDate,
      Pageable pageable
  );

  // 관리자용 검색 추가
  Page<Journal> searchJournalsForAdmin(
      Long courseId,
      String searchType,  // "name" 또는 "username"
      String searchKeyword,
      LocalDate startDate,
      LocalDate endDate,
      Pageable pageable
  );

}