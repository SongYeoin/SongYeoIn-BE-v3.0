package com.syi.project.club.repository;

import com.syi.project.club.entity.Club;
import com.syi.project.common.Criteria;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ClubRepositoryCustom {

    // 클래스 번호(classNo)에 해당하는 동아리 목록을 페이징 처리하여 조회
    Page<Club> findClubsByCriteria(Criteria cri, Integer classNo, Pageable pageable);
}
