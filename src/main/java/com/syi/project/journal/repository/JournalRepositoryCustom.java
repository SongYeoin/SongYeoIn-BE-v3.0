package com.syi.project.journal.repository;

import com.syi.project.journal.entity.Journal;
import com.syi.project.common.entity.Criteria;
import org.springframework.data.domain.Page;
import java.time.LocalDate;

public interface JournalRepositoryCustom {
  Page<Journal> findAllWithConditions(Criteria criteria, Long memberId, LocalDate startDate, LocalDate endDate);
}