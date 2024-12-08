package com.syi.project.club.service;

import com.syi.project.auth.entity.Member;
import com.syi.project.auth.repository.MemberRepository;
import com.syi.project.club.dto.ClubRequestDTO;
import com.syi.project.club.dto.ClubResponseDTO;
import com.syi.project.club.entity.Club;
import com.syi.project.club.file.ClubFile;
import com.syi.project.club.file.ClubFileRepository;
import com.syi.project.club.repository.ClubRepository;
import com.syi.project.common.entity.Criteria;
import com.syi.project.common.enums.CheckStatus;
import com.syi.project.common.exception.ErrorCode;
import com.syi.project.common.exception.InvalidRequestException;
import com.syi.project.common.utils.S3Uploader;
import com.syi.project.file.dto.FileResponseDTO;
import com.syi.project.file.entity.File;
import com.syi.project.file.repository.FileRepository;
import com.syi.project.file.service.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
    @Autowired
    private S3Uploader s3Uploader;

    //등록
    @Transactional
    public ClubResponseDTO.ClubList createClub(ClubRequestDTO.ClubCreate clubDTO, Long writerId,
                                               Long courseId, LocalDate regDate, CheckStatus checkStatus) {
        Club club = clubDTO.toEntity(writerId, courseId, regDate, checkStatus);
        club = clubRepository.save(club);

        return ClubResponseDTO.ClubList.toListDTO(club, getMemberName(writerId), null, null);
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
        ClubFile clubFile = clubFileRepository.findByClubId(club.getId()).stream().findFirst().orElse(null);
        FileResponseDTO savedFile = null;

        if (clubFile != null) {
            File file = clubFile.getFile();
            if(file != null){
                savedFile = FileResponseDTO.from(file, s3Uploader);
            }
        }

//        List<String> fileIcons = clubFiles.stream()
//                .map(clubFile -> {
//                    File file = fileRepository.findById(clubFile.getFileId()).orElse(null);
//                    return (file != null && file.getPath() != null && !file.getPath().isEmpty()) ? "clip_icon" : "";
//                }).collect(Collectors.toList());

        String writer = getMemberName(club.getWriterId());
        String checker = club.getCheckerId() != null ? getMemberName(club.getCheckerId()) : null;

        return ClubResponseDTO.ClubList.toListDTO(club, writer, checker, savedFile);
    }

    //상세
    public ClubResponseDTO.ClubDetail getClubDetail(Long clubId){
        // 클럽 정보 조회
        Club club = clubRepository.findById(clubId)
                .orElseThrow(() -> new IllegalArgumentException("해당 클럽을 찾을 수 없습니다. ID: " + clubId));

        // 파일 정보 조회
//        List<ClubFile> clubFiles = clubFileRepository.findByClubId(clubId);
//        List<String> fileNames = new ArrayList<>();
//        for (ClubFile clubFile : clubFiles) {
//            File file = fileRepository.findById(clubFile.getFileId()).orElse(null);
//            if (file != null && file.getOriginalName() != null && !file.getOriginalName().isEmpty()) {
//                fileNames.add(file.getOriginalName()); // 파일 이름 추가
//            } else {
//                fileNames.add(""); // 파일 정보가 없으면 공란
//            }
//        }

        // 클럽 파일 조회 (하나의 파일만 존재)
        ClubFile clubFile = clubFileRepository.findByClubId(clubId).stream().findFirst().orElse(null);
        FileResponseDTO savedFile = null;

        if (clubFile != null) {
            File file = clubFile.getFile();
            if (file != null) {
                savedFile = FileResponseDTO.from(file, s3Uploader);
            }
        }

        // 작성자 및 승인자 이름 조회
        String writer = getMemberName(club.getWriterId());
        String checker = club.getCheckerId() != null ? getMemberName(club.getCheckerId()) : null;

        // Club -> ClubResponseDTO.ClubDetail 변환
        return ClubResponseDTO.ClubDetail.toDetailDTO(club, writer, checker, savedFile);
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

        FileResponseDTO fileDto = null;

        // 승인 상태에 따른 수정 처리
        if (club.getCheckStatus() == CheckStatus.W) {
            // 대기 상태일 때는 파일 수정 불가
//            if (file != null) {
//                throw new InvalidRequestException(ErrorCode.CANNOT_MODIFY_FILE_IN_WAITING);
//            }
//
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
//            if (clubUpdate != null) {
//                throw new InvalidRequestException(ErrorCode.CANNOT_MODIFY_APPROVED);
//            }

            Member member = memberRepository.findById(loggedInUserId)
                    .orElseThrow(() -> new IllegalArgumentException("회원이 존재하지 않습니다."));

            // 파일 처리
            if (file != null) {
                String dirName = "club/" + clubId;

                // 기존 파일이 있는 경우 삭제하고 새 파일로 교체
                ClubFile clubFile = clubFileRepository.findByClubId(clubId).stream().findFirst().orElse(null);

                if (clubFile != null) {
                    // 기존 파일 삭제
                    File existingFile = clubFile.getFile();
                    if (existingFile != null) {
                        fileService.deleteFile(existingFile.getId(), member); // 기존 파일 삭제
                    }

                    // 새 파일 업로드
                    File uploadedFile = fileService.uploadFile(file, dirName, member);
                    clubFile.updateFile(uploadedFile);

                    // 파일 DTO 생성
                    fileDto = FileResponseDTO.from(uploadedFile, s3Uploader);
                } else {
                    // 기존 파일이 없는 경우 새 파일 업로드
                    File uploadedFile = fileService.uploadFile(file, dirName, member);
                    ClubFile newClubFile = new ClubFile(club, uploadedFile);
                    clubFileRepository.save(newClubFile);

                    // 파일 DTO 생성
                    fileDto = FileResponseDTO.from(uploadedFile, s3Uploader);
                }
            }
        } else {
            throw new InvalidRequestException(ErrorCode.CANNOT_MODIFY_PENDING);
        }

        // 클럽 저장
        Club updatedClub = clubRepository.save(club);

        String writer = getMemberName(club.getWriterId());

        // 응답 DTO로 변환
        return ClubResponseDTO.ClubList.toListDTO(updatedClub, writer, null, fileDto);
    }

    //관리자 수정
    public ClubResponseDTO.ClubList approveClub(Long clubId, ClubRequestDTO.ClubApproval clubApproval,
                                                Long adminId) {
        // 클럽 조회
        Club club = clubRepository.findById(clubId)
                .orElseThrow(() -> new NoSuchElementException("클럽이 존재하지 않습니다."));

        club.updateApprove(adminId, clubApproval.getCheckStatus(), clubApproval.getCheckMessage());

        Club updatedClub = clubRepository.save(club);
        String writer = getMemberName(club.getWriterId());
        String adminName = getMemberName(adminId);

        return ClubResponseDTO.ClubList.toListDTO(updatedClub, writer, adminName, null);
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

