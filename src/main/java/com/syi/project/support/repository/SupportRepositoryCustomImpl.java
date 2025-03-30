package com.syi.project.support.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.syi.project.support.entity.QSupport;
import com.syi.project.support.entity.Support;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;

@RequiredArgsConstructor
public class SupportRepositoryCustomImpl implements SupportRepositoryCustom {
  private final JPAQueryFactory queryFactory;

  @Override
  public Page<Support> searchSupports(Long memberId, String keyword, Pageable pageable) {
    // 검색 조건 구성
    BooleanExpression memberFilter = memberId != null
        ? QSupport.support.member.id.eq(memberId)
        : null;

    BooleanExpression notDeleted = QSupport.support.deletedBy.isNull();

    // 제목 검색 조건 - 키워드가 있을 경우에만 적용
    // final 키워드 추가
    final BooleanExpression titleCondition;
    if (keyword != null && !keyword.isEmpty()) {
      titleCondition = QSupport.support.title.containsIgnoreCase(keyword);
    } else {
      titleCondition = null;
    }

    // 쿼리 실행 (fetch join 사용)
    List<Support> supports = queryFactory
        .selectFrom(QSupport.support)
        .leftJoin(QSupport.support.member).fetchJoin()
        .where(notDeleted, memberFilter, titleCondition)
        .offset(pageable.getOffset())
        .limit(pageable.getPageSize())
        .orderBy(QSupport.support.regDate.desc(), QSupport.support.id.desc())
        .fetch();

    // count 쿼리 최적화
    return PageableExecutionUtils.getPage(supports, pageable, () ->
        queryFactory
            .select(QSupport.support.count())
            .from(QSupport.support)
            .where(notDeleted, memberFilter, titleCondition)
            .fetchOne()
    );
  }
}