package com.syi.project.journal.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.syi.project.common.entity.Criteria;
import com.syi.project.journal.entity.Journal;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.support.PageableExecutionUtils;
import java.time.LocalDate;
import java.util.List;
import static com.syi.project.journal.entity.QJournal.journal;

@RequiredArgsConstructor
public class JournalRepositoryCustomImpl implements JournalRepositoryCustom {
  private final JPAQueryFactory queryFactory;

  @Override
  public Page<Journal> findAllWithConditions(Criteria criteria, Long memberId, LocalDate startDate, LocalDate endDate) {
    List<Journal> journals = queryFactory
        .selectFrom(journal)
        .where(
            searchByType(criteria.getType(), criteria.getKeyword()),
            searchByDateRange(startDate, endDate),
            searchByMemberId(memberId)
        )
        .offset(criteria.getPageable().getOffset())
        .limit(criteria.getPageable().getPageSize())
        .orderBy(journal.createdAt.desc())
        .fetch();

    return PageableExecutionUtils.getPage(journals, criteria.getPageable(), () ->
        queryFactory
            .select(journal.count())
            .from(journal)
            .where(
                searchByType(criteria.getType(), criteria.getKeyword()),
                searchByDateRange(startDate, endDate),
                searchByMemberId(memberId)
            )
            .fetchOne()
    );
  }

  private BooleanExpression searchByType(String type, String keyword) {
    if (type == null || keyword == null) return null;

    return switch (type) {
      case "W" -> journal.member.name.contains(keyword);
      case "I" -> journal.member.id.eq(Long.parseLong(keyword));
      default -> null;
    };
  }

  private BooleanExpression searchByDateRange(LocalDate startDate, LocalDate endDate) {
    if (startDate != null && endDate != null) {
      return journal.createdAt.between(startDate.atStartOfDay(), endDate.atTime(23, 59, 59));
    }
    if (startDate != null) {
      return journal.createdAt.goe(startDate.atStartOfDay());
    }
    if (endDate != null) {
      return journal.createdAt.loe(endDate.atTime(23, 59, 59));
    }
    return null;
  }

  private BooleanExpression searchByMemberId(Long memberId) {
    return memberId != null ? journal.member.id.eq(memberId) : null;
  }
}