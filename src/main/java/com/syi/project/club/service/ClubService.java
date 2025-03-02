package com.syi.project.club.service;

import com.syi.project.auth.entity.Member;
import com.syi.project.auth.repository.MemberRepository;
import com.syi.project.club.controller.ClubController;
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
import java.util.Arrays;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ClubService {

    @Autowired
    private final ClubRepository clubRepository;
    @Autowired
    private final ClubFileRepository clubFileRepository;
    @Autowired
    private final FileRepository fileRepository;
    @Autowired
    private final MemberRepository memberRepository;
    @Autowired
    private final FileService fileService;
    @Autowired
    private final S3Uploader s3Uploader;

    private static final Logger log = LoggerFactory.getLogger(ClubController.class);
    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList("hwp", "hwpx", "docx", "doc");

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
    @Transactional
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
    @Transactional
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
    @Transactional
    public ClubResponseDTO.ClubList updateClub(Long clubId, ClubRequestDTO.ClubUpdate clubUpdate,
                                               MultipartFile file, Long loggedInUserId) {
        // 클럽 조회
        Club club = clubRepository.findById(clubId)
                .orElseThrow(() -> new NoSuchElementException("클럽이 존재하지 않습니다."));

        // 작성자와 로그인 사용자가 동일한지 확인
        verifyWriter(club.getWriterId(), loggedInUserId);

        if (file != null) {
            // 파일 유효성 검사
            validateFile(file, clubId);
        }

        // 승인 상태에 따른 수정 처리
        if (club.getCheckStatus() == CheckStatus.W) {
            // 대기 상태: 클럽 정보 업데이트
            updateClubDetails(club, clubUpdate);

            // 필요시 파일도 업데이트
            if (file != null) {
                updateClubFile(club, file, loggedInUserId);
            }
        } else if (club.getCheckStatus() == CheckStatus.Y) {
//            // 승인 상태: 필요한 경우 클럽 정보 업데이트
//            if (clubUpdate != null) {
//                updateClubDetails(club, clubUpdate);
//            }

            // 파일 업데이트
            if (file != null) {
                updateClubFile(club, file, loggedInUserId);
            }
        } else {
            throw new InvalidRequestException(ErrorCode.CANNOT_MODIFY_PENDING);
        }

        // 클럽 저장
        Club updatedClub = clubRepository.save(club);
        log.info("클럽 업데이트 완료: clubId={}", updatedClub.getId());

        // 파일 정보 가져오기
        FileResponseDTO fileDto = getClubFileInfo(clubId);

        String writer = getMemberName(club.getWriterId());

        // 응답 DTO로 변환
        return ClubResponseDTO.ClubList.toListDTO(updatedClub, writer, null, fileDto);
    }

    // 클럽 상세 정보 업데이트 메서드
    private void updateClubDetails(Club club, ClubRequestDTO.ClubUpdate update) {
        club.updateDetails(
          update.getParticipants(),
          update.getContent(),
          update.getStudyDate(),
          LocalDate.now(),
          update.getClubName(),
          update.getContactNumber(),
          update.getStartTime(),
          update.getEndTime(),
          update.getParticipantCount()
        );
    }

    // 클럽 파일 업데이트 메서드
    private FileResponseDTO updateClubFile(Club club, MultipartFile file, Long loggedInUserId) {
        String dirName = "club";
        Member member = memberRepository.findById(loggedInUserId)
          .orElseThrow(() -> new IllegalArgumentException("회원이 존재하지 않습니다."));

        // 기존 파일 찾기
        ClubFile clubFile = clubFileRepository.findByClubId(club.getId()).stream().findFirst().orElse(null);

        if (clubFile != null) {
            // 기존 파일이 있는 경우 삭제하고 새 파일로 교체
            File existingFile = clubFile.getFile();
            if (existingFile != null) {
                fileService.deleteFile(existingFile.getId(), member);
            }

            // 새 파일 업로드
            File uploadedFile = fileService.uploadFile(file, dirName, member);
            clubFile.updateFile(uploadedFile);
            clubFileRepository.save(clubFile);

            return FileResponseDTO.from(uploadedFile, s3Uploader);
        } else {
            // 기존 파일이 없는 경우 새 파일 업로드
            File uploadedFile = fileService.uploadFile(file, dirName, member);
            ClubFile newClubFile = ClubFile.builder()
              .club(club)
              .file(uploadedFile)
              .build();
            clubFileRepository.save(newClubFile);

            return FileResponseDTO.from(uploadedFile, s3Uploader);
        }
    }

    // 클럽 파일 정보 조회 메서드
    private FileResponseDTO getClubFileInfo(Long clubId) {
        return clubFileRepository.findByClubId(clubId).stream()
          .findFirst()
          .map(clubFile -> FileResponseDTO.from(clubFile.getFile(), s3Uploader))
          .orElse(null);
    }

    //관리자 수정
    @Transactional
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

    // 파일확장자 및 개수 제한
    private void validateFile(MultipartFile file, Long clubId) {
        // 파일이 없으면 검증 생략
        if (file == null || file.isEmpty()) {
            return;
        }

        // 1. 파일 확장자 검사
        String originalFilename = file.getOriginalFilename();
        if (originalFilename != null) {
            String extension = originalFilename.substring(originalFilename.lastIndexOf(".") + 1).toLowerCase();

            if (!ALLOWED_EXTENSIONS.contains(extension)) {
                throw new InvalidRequestException(ErrorCode.INVALID_FILE_FORMAT,
                  "허용되지 않는 파일 형식입니다. 허용된 확장자: " + String.join(", ", ALLOWED_EXTENSIONS));
            }
        }

        // 2. 파일 개수 검사 (clubId가 null이 아닌 경우에만 수행)
        if (clubId != null) {
            ClubFile existingClubFile = clubFileRepository.findByClubId(clubId).stream().findFirst().orElse(null);

            // 기존 파일이 없는 상태에서 새 파일 추가는 항상 가능
            if (existingClubFile == null) {
                return;
            }

            // 이미 파일이 존재하는데 새 파일을 추가하려는 경우
            long fileCount = clubFileRepository.findByClubId(clubId).size();
            if (fileCount >= 1 && existingClubFile == null) {
                throw new InvalidRequestException(ErrorCode.FILE_COUNT_EXCEEDED, "클럽당 최대 1개의 파일만 허용됩니다.");
            }
        }
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

