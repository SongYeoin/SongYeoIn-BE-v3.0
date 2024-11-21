package com.syi.project.notice.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.syi.project.notice.entity.Notice;
import com.syi.project.notice.entity.QNotice;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class NoticeRepositoryImpl implements NoticeRepositoryCustom {

  private final JPAQueryFactory queryFactory;

  @Override
  public List<Notice> findNoticesByCourseIdAndGlobal(Long courseId) {
    QNotice notice = QNotice.notice;

    return queryFactory.selectFrom(notice)
        .where(
            notice.deletedBy.isNull(),
            courseId != null
                ? notice.course.id.eq(courseId).or(notice.isGlobal.isTrue()) // 특정 반 공지 또는 전체 공지
                : notice.isGlobal.isTrue() // courseId가 null이면 전체 공지만 조회
        )
        .orderBy(
            notice.isGlobal.desc(), // 전체 공지가 상단에 오도록 정렬
            notice.noticeRegDate.desc() // 최신 공지가 이후 정렬
        )
        .fetch();
  }

}
