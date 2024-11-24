package com.syi.project.club.service;

import com.syi.project.auth.dto.AuthUserDTO;
import com.syi.project.auth.repository.MemberRepository;
import com.syi.project.club.dto.ClubRequestDTO;
import com.syi.project.club.dto.ClubResponseDTO;
import com.syi.project.club.entity.Club;
import com.syi.project.club.file.ClubFile;
import com.syi.project.club.file.ClubFileRepository;
import com.syi.project.club.repository.ClubRepository;
import com.syi.project.common.entity.Criteria;
import com.syi.project.common.enums.CheckStatus;
import com.syi.project.common.enums.Role;
import com.syi.project.common.exception.ErrorCode;
import com.syi.project.common.exception.InvalidRequestException;
import com.syi.project.file.entity.File;
import com.syi.project.file.repository.FileRepository;
import com.syi.project.file.service.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.syi.project.auth.entity.Member;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
public class ClubService {

    @Autowired
    private ClubRepository clubRepository;
    @Autowired
    private ClubFileRepository clubFileRepository;
    @Autowired
    private FileRepository fileRepository;
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private FileService fileService;

    //등록
    @Transactional
    public ClubResponseDTO.ClubList createClub(ClubRequestDTO.ClubCreate clubDTO, Long writerId,
                                               Long courseId, LocalDate regDate, CheckStatus checkStatus) {
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

        return ClubResponseDTO.ClubList.toListDTO(club, getMemberName(writerId), null, Collections.emptyList());
    }

    private String getMemberName(Long memberId) {
        // 더미 메서드: 실제 MemberService 로직으로 대체
        return memberId != null ? memberRepository.findById(memberId).map(Member::getName).orElse("Unknown") : null;
    }

    //리스트(페이징)
    public Page<ClubResponseDTO.ClubList> getClubListWithPaging(Criteria cri, Long courseId) {
        // Pageable 객체 생성 (cri에서 pageNum과 amount 가져오기)
        Pageable pageable = cri.getPageable();
        return clubRepository.findClubListByCourseId(cri, courseId, pageable).map(this::toClubListDTO);

         //System.out.println("service: " + clubRepository.findClubListByCourseId(cri, courseId, pageable));
         //페이징과 조건에 맞는 동아리 목록 조회
//         return clubRepository.findClubListByCourseId(cri, courseId, pageable)
//                 .map(club -> {
//                     // 파일 상태 처리
//                     List<ClubFile> clubFiles = clubFileRepository.findByClubId(club.getId());
//                     List<String> fileIcons = new ArrayList<>();
//                     for (ClubFile clubFile : clubFiles) {
//                         File file = fileRepository.findById(clubFile.getFileId()).orElse(null);
//                         if (file != null && file.getPath() != null && !file.getPath().isEmpty()) {
//                             fileIcons.add("clip_icon"); // 파일이 있으면 클립 아이콘
//                         } else {
//                             fileIcons.add(""); // 파일이 없으면 공백
//                         }
//                     }
//
//                     // 작성자 및 승인자 이름 조회
//                     String writer = getMemberName(club.getWriterId());
//                     String checker = club.getCheckerId() != null ? getMemberName(club.getCheckerId()) : null;
//
//                     // Club -> ClubResponseDTO.ClubList 변환
//                     return ClubResponseDTO.ClubList.toListDTO(club, writer, checker, fileIcons);
//                 });
    }

    //페이징DTO변환
    private ClubResponseDTO.ClubList toClubListDTO(Club club) {
        List<ClubFile> clubFiles = clubFileRepository.findByClubId(club.getId());
        List<String> fileIcons = clubFiles.stream()
                .map(clubFile -> {
                    File file = fileRepository.findById(clubFile.getFileId()).orElse(null);
                    return (file != null && file.getPath() != null && !file.getPath().isEmpty()) ? "clip_icon" : "";
                }).collect(Collectors.toList());

        String writer = getMemberName(club.getWriterId());
        String checker = club.getCheckerId() != null ? getMemberName(club.getCheckerId()) : null;

        return ClubResponseDTO.ClubList.toListDTO(club, writer, checker, fileIcons);
    }

    //상세
    public ClubResponseDTO.ClubDetail getClubDetail(Long clubId){
        // 클럽 정보 조회
        Club club = clubRepository.findById(clubId)
                .orElseThrow(() -> new IllegalArgumentException("해당 클럽을 찾을 수 없습니다. ID: " + clubId));

        // 파일 정보 조회
        List<ClubFile> clubFiles = clubFileRepository.findByClubId(clubId);
        List<String> fileNames = new ArrayList<>();
        for (ClubFile clubFile : clubFiles) {
            File file = fileRepository.findById(clubFile.getFileId()).orElse(null);
            if (file != null && file.getOriginalName() != null && !file.getOriginalName().isEmpty()) {
                fileNames.add(file.getOriginalName()); // 파일 이름 추가
            } else {
                fileNames.add(""); // 파일 정보가 없으면 공란
            }
        }

        // 작성자 및 승인자 이름 조회
        String writer = getMemberName(club.getWriterId());
        String checker = club.getCheckerId() != null ? getMemberName(club.getCheckerId()) : null;

        // Club -> ClubResponseDTO.ClubDetail 변환
        return ClubResponseDTO.ClubDetail.toDetailDTO(club, writer, checker, fileNames);
    }

    // 클럽 정보 조회
    @Transactional
    public Club getClub(Long clubId) {
        return clubRepository.findById(clubId)
                .orElseThrow(() -> new IllegalArgumentException("클럽이 존재하지 않습니다."));
    }

    //작성자확인
    private void verifyWriter(Long writerId, Long loggedInUserId) {
        if (!writerId.equals(loggedInUserId)) {
            throw new InvalidRequestException(ErrorCode.ACCESS_DENIED);
        }
    }

    //수정
    public ClubResponseDTO.ClubList updateClub(Long clubId, ClubRequestDTO.ClubUpdate clubUpdate,
                                               MultipartFile file, Long loggedInUserId) {
        // 클럽 조회
        Club club = clubRepository.findById(clubId)
                .orElseThrow(() -> new NoSuchElementException("클럽이 존재하지 않습니다."));

        // 작성자와 로그인 사용자가 동일한지 확인
        verifyWriter(club.getWriterId(), loggedInUserId);

        // 승인 상태에 따른 수정 처리
        if (club.getCheckStatus() == CheckStatus.W) {
            // 대기 상태: 활동날, 내용, 참여자 수정 가능
            if (clubUpdate != null) {
                club.updateDetails(
                        clubUpdate.getParticipants(),
                        clubUpdate.getContent(),
                        clubUpdate.getStudyDate(),
                        LocalDate.now()
                );
            }
        } else if (club.getCheckStatus() == CheckStatus.Y) {
            // 승인 상태: 파일만 수정 가능
            if (clubUpdate != null) {
                throw new InvalidRequestException(ErrorCode.CANNOT_MODIFY_APPROVED);
            }

            Member member = memberRepository.findById(loggedInUserId)
                    .orElseThrow(() -> new IllegalArgumentException("회원이 존재하지 않습니다."));

            // 파일 처리
            if (file != null) {
                String dirName = "club/" + clubId;
                if (club.getFileId() != null) {
                    // 기존 파일이 있는 경우 수정
                    fileService.updateFile(club.getFileId(), file, dirName, member); // 간단한 Member 객체 생성
                } else {
                    // 새로운 파일 업로드
                    File uploadedFile = fileService.uploadFile(file, dirName, member);
                    club.updateFileId(uploadedFile.getId());
                }
            }
        } else {
            throw new InvalidRequestException(ErrorCode.CANNOT_MODIFY_PENDING);
        }

        // 클럽 저장
        Club updatedClub = clubRepository.save(club);

        String writer = getMemberName(club.getWriterId());

        // 응답 DTO로 변환
        return ClubResponseDTO.ClubList.toListDTO(updatedClub, writer, null, Collections.emptyList());
    }

    public ClubResponseDTO.ClubList approveClub(Long clubId, ClubRequestDTO.ClubApproval clubApproval,
                                                Long adminId) {
        // 클럽 조회
        Club club = clubRepository.findById(clubId)
                .orElseThrow(() -> new NoSuchElementException("클럽이 존재하지 않습니다."));

        club.updateApprove(adminId, clubApproval.getCheckStatus(), clubApproval.getCheckMessage());

        Club updatedClub = clubRepository.save(club);
        String writer = getMemberName(club.getWriterId());
        String adminName = getMemberName(adminId);

        return ClubResponseDTO.ClubList.toListDTO(updatedClub, writer, adminName, Collections.emptyList());
    }

    //삭제
    @Transactional
    public void delete(Long clubId, Long loggedInUserId) {
        Club club = clubRepository.findById(clubId)
                .orElseThrow(() -> new IllegalArgumentException("클럽이 존재하지 않습니다. ID: " + clubId));

        // 작성자와 로그인한 사용자 비교
        verifyWriter(club.getWriterId(), loggedInUserId);

        if (club.getCheckStatus() != CheckStatus.W) {
            throw new InvalidRequestException(ErrorCode.CANNOT_DELETE_APPROVED);
        }

        clubRepository.deleteById(clubId);
    }

    @Transactional
    public void deleteAsAdmin(Long clubId, Long adminId) {
        Club club = clubRepository.findById(clubId)
                .orElseThrow(() -> new IllegalArgumentException("Club with ID " + clubId + " does not exist."));

        if (club.getCheckStatus() != CheckStatus.W) {
            throw new InvalidRequestException(ErrorCode.CANNOT_DELETE_APPROVED);
        }

        clubRepository.deleteById(clubId);
    }


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

