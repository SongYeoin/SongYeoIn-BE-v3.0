package com.syi.project.club.service;

import com.syi.project.club.dto.ClubResponseDTO;
import com.syi.project.club.dto.ClubRequestDTO;
import com.syi.project.club.entity.Club;
import com.syi.project.club.repository.ClubRepository;
import com.syi.project.common.entity.Criteria;
import com.syi.project.common.enums.CheckStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
public class ClubService {

    @Autowired
    private ClubRepository clubRepository;

    //등록
    @Transactional
    public ClubResponseDTO.ClubList createClub(ClubRequestDTO.ClubCreate clubDTO, Long writerId, Long courseId, LocalDate regDate, CheckStatus checkStatus) {
//        Club club = Club.builder()
//                .courseId(courseId)  // 프로그램
//                .writerId(writerId)  // 작성자명
//                .checkStatus(checkStatus)
//                .studyDate(clubDTO.getStudyDate())
//                .regDate(regDate)
//                .participants(clubDTO.getParticipants())
//                .content(clubDTO.getContent())
//                .build();
//        clubRepository.save(club);
        Club club = clubDTO.toEntity(writerId, courseId, regDate, checkStatus);
        club = clubRepository.save(club);

        return ClubResponseDTO.ClubList.toDTO(club, String.valueOf(writerId), null, null);
    }

    //리스트(페이징)
    public Page<ClubResponseDTO.ClubList> getClubListWithPaging(Criteria cri, Long courseId) {
        // Pageable 객체 생성 (cri에서 pageNum과 amount 가져오기)
        Pageable pageable = cri.getPageable();

         System.out.println("service: " + clubRepository.findClubListByCourseId(cri, courseId, pageable));
         //페이징과 조건에 맞는 동아리 목록 조회
         return clubRepository.findClubListByCourseId(cri, courseId, pageable)
                 .map(club -> ClubResponseDTO.ClubList.toDTO(
                         club,
                         getMemberName(club.getWriterId()),
                         club.getCheckerId() != null ? getMemberName(club.getCheckerId()) : null,
                         null // URL이나 다른 파일 관련 로직이 필요하면 추가
                 ));
    }

    private String getMemberName(Long memberId) {
        // 더미 메서드: 실제 MemberService 로직으로 대체
        return memberId != null ? "Member" + memberId : null;
    }


//    private ClubDTO convertToDTO(Club club) {
//        return new ClubDTO(club.getClubNo(), club.getcourseId(), club.getName(), club.getStatus());
//    }



//    //조회
//    public ClubDTO getPage(int clubNo) {
//        Club club = clubRepository.findById((long) clubNo).orElse(null);
//        return convertToDTO(club);
//    }

//    //수정
//    @Transactional
//    public int modify(ClubDTO clubDTO) {
//        Club club = clubRepository.findById(clubDTO.getId()).orElse(null);
//        if (club != null) {
//            club.setName(clubDTO.getName());
//            club.setWriter(clubDTO.getWriter());
//            club.setCheckStatus(clubDTO.getCheckStatus());
//            club.setCheckCmt(clubDTO.getCheckCmt());
//            club.setStudyDate(clubDTO.getStudyDate());
//            club.setParticipants(clubDTO.getParticipants());
//            club.setContent(clubDTO.getContent());
//            clubRepository.save(club);  // Update the club
//            return 1;
//        }
//        return 0; // 실패시 0 반환
//    }
//
//    // 관리자가 수정
//    @Transactional
//    public int modifyAdmin(ClubDTO clubDTO) {
//        Club club = clubRepository.findById(clubDTO.getId()).orElse(null);
//        if (club != null) {
//            club.setName(clubDTO.getName());
//            club.setWriter(clubDTO.getWriter());
//            club.setCheckStatus(clubDTO.getCheckStatus());
//            club.setCheckCmt(clubDTO.getCheckCmt());
//            club.setStudyDate(clubDTO.getStudyDate());
//            club.setParticipants(clubDTO.getParticipants());
//            club.setContent(clubDTO.getContent());
//            clubRepository.save(club);  // Update the club
//            return 1;
//        }
//        return 0; // 실패시 0 반환
//    }

    // 삭제
//    @Transactional
//    public int delete(int clubNo) {
//        Club club = clubRepository.findById((long) clubNo).orElse(null);
//        if (club != null) {
//            clubRepository.delete(club);
//            return 1;
//        }
//        return 0; // 실패시 0 반환
//    }

//    // 로드 시 반 번호
//    public Long getDefaultcourseIdByMember(Long memberNo) {
//        // Assuming a method in the repository to get the courseId by memberNo
//        return clubRepository.findDefaultcourseIdByMember(memberNo);
//    }

//    // 수강 반 목록
//    public List<SyclassVO> getcourseIdListByMember(Long memberNo) {
//        // You can implement this part based on your data structure
//        // For example, assuming SyclassVO is a class representing class details
//        return clubRepository.findcourseIdListByMember(memberNo);
//    }

//    // 동아리 신청 총 갯수
//    public int getTotal(Criteria cri, Long courseId) {
//        // Assuming that the count of clubs can be fetched for a specific courseId and criteria
//        return (int) clubRepository.countBycourseId(courseId);
//    }

//    // Club -> ClubDTO 변환 메소드
//    private ClubDTO convertToDTO(Club club) {
//        if (club == null) {
//            return null;
//        }
//        ClubDTO clubDTO = new ClubDTO();
//        clubDTO.setId(club.getId());
//        clubDTO.setName(club.getName());
//        clubDTO.setWriter(club.getWriter());
//        clubDTO.setCheckStatus(club.getCheckStatus());
//        clubDTO.setCheckCmt(club.getCheckCmt());
//        clubDTO.setStudyDate(club.getStudyDate());
//        clubDTO.setParticipants(club.getParticipants());
//        clubDTO.setContent(club.getContent());
//        clubDTO.setcourseId(club.getcourseId());
//        clubDTO.setMemberNo(club.getMemberNo());
//        clubDTO.setFileOriginalName(club.getFileOriginalName());
//        clubDTO.setFileSavedName(club.getFileSavedName());
//        clubDTO.setFileType(club.getFileType());
//        clubDTO.setFileSize(club.getFileSize());
//        clubDTO.setFilepath(club.getFilepath());
//        clubDTO.setFileRegDate(club.getFileRegDate());
//        return clubDTO;
//    }

//
//
//
//    // Update Club
//    public ClubResponseDTO.ClubList updateClub(Long clubId, ClubRequestDTO.ClubUpdate dto, String url) {
//        Club club = clubRepository.findById(clubId)
//                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 Club입니다."));
//
//        club = dto.toEntity(); // 업데이트할 내용 반영
//        clubRepository.save(club);
//        return ClubResponseDTO.ClubList.toDTO(club, String.valueOf(club.getWriterId()), null, url);
//    }
//
//    // Approval
//    public ClubResponseDTO.ClubList approveClub(Long clubId, ClubRequestDTO.ClubApproval dto, String checkerId, String url) {
//        Club club = clubRepository.findById(clubId)
//                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 Club입니다."));
//
//        club = dto.toEntity(); // 승인 상태 변경 반영
//        clubRepository.save(club);
//        return ClubResponseDTO.ClubList.toDTO(club, String.valueOf(club.getWriterId()), checkerId, url);
//    }
//
//    // Find Club
//    public ClubResponseDTO.ClubList getClub(Long clubId, String url) {
//        Club club = clubRepository.findById(clubId)
//                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 Club입니다."));
//        return ClubResponseDTO.ClubList.toDTO(club, String.valueOf(club.getWriterId()), String.valueOf(club.getCheckerId()), url);
//    }
}

