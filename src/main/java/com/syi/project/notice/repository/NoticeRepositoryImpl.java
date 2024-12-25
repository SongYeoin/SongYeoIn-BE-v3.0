package com.syi.project.notice.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.syi.project.notice.entity.Notice;
import com.syi.project.notice.entity.QNotice;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class NoticeRepositoryImpl implements NoticeRepositoryCustom {

  private final JPAQueryFactory queryFactory;

  @Override
  public Page<Notice> findNoticesByCourseId(Long courseId, String titleKeyword, Pageable pageable) {
    QNotice notice = QNotice.notice;

    // 조건 정의
    BooleanExpression titleCondition = titleKeyword != null && !titleKeyword.isBlank()
        ? notice.title.containsIgnoreCase(titleKeyword)
        : null;

    // 데이터 조회 쿼리
    JPAQuery<Notice> query = queryFactory.selectFrom(notice)
        .where(
            notice.deletedBy.isNull(),
            notice.course.id.eq(courseId),
            titleCondition
        )
        .orderBy(
            notice.isPinned.desc(), // 상단고정 공지 우선
            notice.regDate.desc()   // 최신순
        );

    // 페이징 처리
    List<Notice> notices = query
        .offset(pageable.getOffset())
        .limit(pageable.getPageSize())
        .fetch();

    // 카운트 쿼리
    JPAQuery<Long> countQuery = queryFactory.select(notice.count())
        .from(notice)
        .where(
            notice.deletedBy.isNull(),
            notice.course.id.eq(courseId),
            titleCondition
        );

    return PageableExecutionUtils.getPage(notices, pageable, countQuery::fetchOne);
  }

}
