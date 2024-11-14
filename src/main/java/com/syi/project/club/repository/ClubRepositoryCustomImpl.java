package com.syi.project.club.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.syi.project.club.entity.Club;
import com.syi.project.common.Criteria;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class ClubRepositoryCustomImpl implements ClubRepositoryCustom{

    @Autowired
    private final JPAQueryFactory queryFactory;

    private QClub club = QClub.club;

//    @PersistenceContext
//    private EntityManager entityManager;

    public ClubRepositoryCustomImpl(EntityManager entityManager) {
        this.queryFactory = new JPAQueryFactory(entityManager);
    }


    // 클래스 번호(classNo)에 해당하는 동아리 목록을 페이징 처리하여 조회
    @Override
    public Page<Club> findClubsByCriteria(Criteria cri, Integer classNo, Pageable pageable) {
        BooleanBuilder builder = new BooleanBuilder();

        // 클래스 번호(classNo)에 따른 필터링
        if (classNo != null) {
            builder.and(club.name.eq(classNo));
        }

        // 검색 타입과 키워드에 따른 조건 추가
        if ("C".equals(cri.getType()) && cri.getKeyword() != null) {
            String status = cri.getKeyword();
            if ("W".equals(status)) {
                builder.and(club.status.eq("W"));
            } else if ("Y".equals(status)) {
                builder.and(club.status.eq("Y"));
            } else if ("N".equals(status)) {
                builder.and(club.status.eq("N"));
            }
        } else if (cri.getKeyword() != null) {
            builder.and(club.name.contains(cri.getKeyword())); // 예시: club 이름으로 검색
        }

        // 쿼리 실행: 페이징과 조건을 결합한 조회
        List<Club> clubs = queryFactory
                .selectFrom(club)
                .where(builder)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        // 총 데이터 개수 조회
        long total = queryFactory
                .selectFrom(club)
                .where(builder)
                .fetchCount();

        return new PageImpl<>(clubs, pageable, total);
    }
}
