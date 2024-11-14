package com.syi.project.club.repository;

import com.syi.project.club.entity.Club;
import com.syi.project.common.Criteria;
import com.syi.project.course.dto.CourseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClubRepository extends JpaRepository<Club, Long>, ClubRepositoryCustom {

    // 클럽명이나 작성자로 검색할 수 있는 메서드 예시
    List<Club> findByNameContainingOrWriterContaining(String name, String writer);

    // 클래스 번호(classNo)에 해당하는 동아리 목록을 페이징 처리하여 조회
    Page<Club> findClubsByCriteria(Criteria cri, Integer classNo, Pageable pageable);

    // 특정 memberNo에 대한 기본 반 번호 조회
    Integer findDefaultClassNoByMember(Integer memberNo);

    // 특정 memberNo에 대한 수강 반 목록 조회
    List<CourseDTO> findClassNoListByMember(Integer memberNo);

    // 특정 classNo에 해당하는 동아리 신청 총 갯수
    long countByClassNo(Integer classNo);
}
