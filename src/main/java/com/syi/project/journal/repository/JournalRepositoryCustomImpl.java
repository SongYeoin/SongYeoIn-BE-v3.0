package com.syi.project.journal.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.syi.project.common.entity.Criteria;
import com.syi.project.journal.entity.Journal;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import java.time.LocalDate;
import java.util.List;
import static com.syi.project.journal.entity.QJournal.journal;

@RequiredArgsConstructor
public class JournalRepositoryCustomImpl implements JournalRepositoryCustom {
  private final JPAQueryFactory queryFactory;

  // 기본 쿼리 로직을 공통 메서드로 추출
  private JPAQuery<Journal> getBaseQuery() {
    return queryFactory
        .selectFrom(journal)
        .leftJoin(journal.member).fetchJoin()
        .leftJoin(journal.course).fetchJoin();
  }

  // 검색 조건을 통합한 메서드
  private BooleanExpression[] createSearchConditions(
      Long memberId,
      Long courseId,
      String searchType,
      String searchKeyword,
      LocalDate startDate,
      LocalDate endDate
  ) {
    return new BooleanExpression[] {
        memberId != null ? journal.member.id.eq(memberId) : null,
        courseId != null ? journal.course.id.eq(courseId) : null,
        searchByMemberInfo(searchType, searchKeyword),
        searchByDateRange(startDate, endDate)
    };
  }

  @Override
  public Page<Journal> findAllWithConditions(Criteria criteria, Long memberId, LocalDate startDate, LocalDate endDate) {
    // 공통 쿼리 생성
    List<Journal> journals = getBaseQuery()
        .where(createSearchConditions(memberId, null, criteria.getType(), criteria.getKeyword(), startDate, endDate))
        .offset(criteria.getPageable().getOffset())
        .limit(criteria.getPageable().getPageSize())
        .orderBy(journal.createdAt.desc())
        .fetch();

    return PageableExecutionUtils.getPage(journals, criteria.getPageable(), () ->
        queryFactory
            .select(journal.count())
            .from(journal)
            .where(createSearchConditions(memberId, null, criteria.getType(), criteria.getKeyword(), startDate, endDate))
            .fetchOne()
    );
  }

  @Override
  public Page<Journal> searchJournalsForStudent(
      Long memberId, Long courseId, LocalDate startDate, LocalDate endDate, Pageable pageable
  ) {
    // 공통 쿼리 생성
    List<Journal> journals = getBaseQuery()
        .where(createSearchConditions(memberId, courseId, null, null, startDate, endDate))
        .offset(pageable.getOffset())
        .limit(pageable.getPageSize())
        .orderBy(journal.createdAt.desc())
        .fetch();

    return PageableExecutionUtils.getPage(journals, pageable, () ->
        queryFactory
            .select(journal.count())
            .from(journal)
            .where(createSearchConditions(memberId, courseId, null, null, startDate, endDate))
            .fetchOne()
    );
  }

  @Override
  public Page<Journal> searchJournalsForAdmin(
      Long courseId, String searchType, String searchKeyword,
      LocalDate startDate, LocalDate endDate, Pageable pageable
  ) {
    // 공통 쿼리 생성
    List<Journal> journals = getBaseQuery()
        .where(createSearchConditions(null, courseId, searchType, searchKeyword, startDate, endDate))
        .offset(pageable.getOffset())
        .limit(pageable.getPageSize())
        .orderBy(journal.createdAt.desc())
        .fetch();

    return PageableExecutionUtils.getPage(journals, pageable, () ->
        queryFactory
            .select(journal.count())
            .from(journal)
            .where(createSearchConditions(null, courseId, searchType, searchKeyword, startDate, endDate))
            .fetchOne()
    );
  }

  private BooleanExpression searchByMemberInfo(String searchType, String keyword) {
    if (searchType == null || keyword == null) return null;

    return switch (searchType) {
      case "name" -> journal.member.name.contains(keyword);
      case "username" -> journal.member.username.contains(keyword);
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
}
