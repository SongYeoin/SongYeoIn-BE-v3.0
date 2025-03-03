package com.syi.project.club.repository;

import static com.syi.project.auth.entity.QMember.member;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.syi.project.club.entity.Club;
import com.syi.project.club.entity.QClub;
import com.syi.project.common.entity.Criteria;
import com.syi.project.common.enums.CheckStatus;
import com.syi.project.enroll.entity.QEnroll;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;


public class ClubRepositoryCustomImpl implements ClubRepositoryCustom {

    private final JPAQueryFactory queryFactory;
    private QClub club = QClub.club;

    @PersistenceContext
    private EntityManager entityManager;


    public ClubRepositoryCustomImpl(EntityManager entityManager) {
        this.queryFactory = new JPAQueryFactory(entityManager);
        this.entityManager = entityManager;
    }

    // 클래스 번호(courseId)에 해당하는 동아리 목록을 페이징 처리하여 조회
    @Override
    public Page<Club> findClubListByCourseId(Criteria cri, Long courseId, Pageable pageable) {

        BooleanBuilder builder = new BooleanBuilder();

        // 클래스 번호(courseId)에 따른 필터링
        if (courseId != null) {
            builder.and(club.courseId.eq(courseId));
        }

        // 검색 조건 처리
        if (cri.getType() != null && cri.getKeyword() != null && !cri.getKeyword().isEmpty()) {
            switch (cri.getType()) {
                case "C": // 승인 상태 필터링
                    if ("W".equals(cri.getKeyword())) {
                        builder.and(club.checkStatus.eq(CheckStatus.W)); // '대기' -> "W"
                    } else if ("Y".equals(cri.getKeyword())) {
                        builder.and(club.checkStatus.eq(CheckStatus.Y)); // '승인' -> "Y"
                    } else if ("N".equals(cri.getKeyword())) {
                        builder.and(club.checkStatus.eq(CheckStatus.N)); // '미승인' -> "N"
                    }
                    break;

                case "W": // 작성자 필터링 (작성자 ID로 검색)
                        //builder.and(club.writerId.eq(Long.valueOf(cri.getKeyword()))); // 작성자 ID로 검색
                    builder.and(club.writerId.in(
                      JPAExpressions.select(member.id)
                        .from(member)
                        .where(member.name.contains(cri.getKeyword()))
                    ));
                    break;

                case "P": // 참여자 필터링 (참여자 이름으로 검색)
                        builder.and(club.participants.contains(cri.getKeyword())); // 참여자 이름으로 검색
                    break;

                case "CN": // 동아리명 필터링 (동아리 이름으로 검색)
                        builder.and(club.clubName.contains(cri.getKeyword())); // 동아리 이름으로 검색
                    break;

                default:
                    break;
            }
        }

        // 동아리 목록을 쿼리하고 페이징 처리
        // 작성일 내림차순, 활동일 내림차순으로 정렬
        List<Club> clubs = queryFactory.selectFrom(club)
                .where(builder) // 조건을 BooleanBuilder로 설정
                .orderBy(
                    club.regDate.desc(), // 작성일 내림차순
                    club.studyDate.desc() // 활동일 내림차순
                )
                .offset(pageable.getOffset()) // 페이징 처리
                .limit(pageable.getPageSize()) // 페이지 크기
                .fetch(); // 결과 가져오기

        // 전체 동아리 수 조회
        long total = queryFactory.selectFrom(club)
                .where(builder)
                .fetchCount(); // 전체 개수 조회

        return new PageImpl<>(clubs, pageable, total); // 결과를 페이징 처리하여 반환
    }

}
