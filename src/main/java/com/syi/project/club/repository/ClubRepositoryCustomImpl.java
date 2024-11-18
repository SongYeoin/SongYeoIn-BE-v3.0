package com.syi.project.club.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.syi.project.club.dto.ClubResponseDTO;
import com.syi.project.club.entity.QClub;
import com.syi.project.common.Criteria;
import com.syi.project.enroll.entity.QEnroll;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class ClubRepositoryCustomImpl implements ClubRepositoryCustom {

    private final JPAQueryFactory queryFactory;
    private QClub club = QClub.club;
    private QEnroll enroll = QEnroll.enroll;

    @PersistenceContext
    private EntityManager entityManager;

    public ClubRepositoryCustomImpl(EntityManager entityManager) {
        this.queryFactory = new JPAQueryFactory(entityManager);
    }

    // 클래스 번호(courseId)에 해당하는 동아리 목록을 페이징 처리하여 조회
    @Override
    public Page<ClubResponseDTO.ClubList> findClubsByCriteria(Criteria cri, Long courseId, Pageable pageable) {
        BooleanBuilder builder = new BooleanBuilder();

        // 클래스 번호(courseId)에 따른 필터링
        if (courseId != null) {
            builder.and(club.courseId.eq(courseId));
        }

        // 총 데이터 개수 조회
        long total = queryFactory
                .selectFrom(club)
                .where(builder)
                .fetchCount();

        var clubs = queryFactory.select(Projections.bean(ClubResponseDTO.ClubList.class,
club.memberId,
                        club.checker,
                        club.checkStatus,
                        club.checkMessage,
                        club.regDate,
                        club.studyDate))
                .from(club)
                .fetch();

        System.out.println("clubs" + clubs.get(0).getStudyDate());

        return new PageImpl <>(clubs, pageable, total);




        // 동아리 목록 데이터 페이징 조회
        List<ClubResponseDTO.ClubList> clubs = queryFactory.select(Projections.bean(ClubResponseDTO.ClubList.class,
                        club.id.as("clubId"),   // 동아리 ID
                        member.name.as("writer"),  // 작성자 이름
                        club.checker,
                        club.checkStatus,
                        club.checkMessage,
                        club.regDate,
                        club.studyDate))
                .from(club)
                .join(member).on(club.memberId.eq(member.id)) // Club.memberId와 Member.id 조인
                .where(builder)
                .offset(pageable.getOffset())  // 페이징 offset 적용
                .limit(pageable.getPageSize())  // 페이징 limit 적용
                .fetch();

        // 총 데이터 개수 조회
        long total = queryFactory
                .selectFrom(club)
                .join(member).on(club.memberId.eq(member.id))  // 총 개수 조회 시에도 조인 필요
                .where(builder)
                .fetchCount();

        // 페이징 처리된 결과 반환
        return new PageImpl<>(clubs, pageable, total);
    }

    //    public List<ClubDTO> findAll() {
//        return queryFactory.select(Projections.bean(ClubDTO.class,
//                club.id))
//                .from(club).fetch();
//    }
}
