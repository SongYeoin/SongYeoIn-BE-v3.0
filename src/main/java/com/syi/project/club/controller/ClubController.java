package com.syi.project.club.controller;

import com.syi.project.auth.dto.AuthUserDTO;
import com.syi.project.auth.dto.MemberDTO;
import com.syi.project.auth.service.CustomUserDetails;
import com.syi.project.auth.service.MemberService;
import com.syi.project.club.dto.ClubRequestDTO;
import com.syi.project.club.dto.ClubResponseDTO;
import com.syi.project.club.entity.Club;
import com.syi.project.club.service.ClubService;
import com.syi.project.common.config.JwtProvider;
import com.syi.project.common.dto.PageInfoDTO;
import com.syi.project.common.entity.Criteria;
import com.syi.project.common.enums.CheckStatus;
import com.syi.project.common.exception.ErrorCode;
import com.syi.project.common.exception.InvalidRequestException;
import com.syi.project.common.utils.S3Uploader;
import com.syi.project.course.dto.CourseResponseDTO;
import com.syi.project.course.service.CourseService;
import com.syi.project.enroll.dto.EnrollResponseDTO;
import com.syi.project.enroll.service.EnrollService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/club")
public class ClubController {

    private static final Logger log = LoggerFactory.getLogger(ClubController.class);

    @Autowired
    private ClubService clubService;
    @Autowired
    private JwtProvider jwtProvider;
    @Autowired
    private MemberService memberService;
    @Autowired
    private EnrollService enrollService;

    public ClubController(ClubService clubService) {
        this.clubService = clubService;
    }

    @Autowired
    private CourseService courseService;
    @Autowired
    private S3Uploader s3Uploader;


    @GetMapping
    public ResponseEntity<List<EnrollResponseDTO>> getCourseById(@AuthenticationPrincipal CustomUserDetails customUserDetails){
        Long id = customUserDetails.getId();

        List<EnrollResponseDTO> courseList = enrollService.findEnrollmentsByMemberId(id);
        log.info("get courseList with ID: {} successfully", courseList);
        return ResponseEntity.ok(courseList);
    }

    // 등록
    @GetMapping("/{courseId}/register")
    public ResponseEntity<MemberDTO> getCurrentUser(@AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }

        MemberDTO userResponse = memberService.getMemberDetail(userDetails.getId());
        return ResponseEntity.ok(userResponse);
    }

    @PostMapping("/{courseId}")
    public ResponseEntity<ClubResponseDTO.ClubList> createClub(@Valid @RequestBody ClubRequestDTO.ClubCreate dto,
                                                               @PathVariable Long courseId,
                                                               @AuthenticationPrincipal CustomUserDetails customUserDetails) {

        try{

        Long writerId = customUserDetails.getId();
            if (writerId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
            }
        LocalDate regDate = LocalDate.now();
        CheckStatus checkStatus = CheckStatus.W;

            log.info("Creating club with courseId: {}, writerId: {}, regDate: {}", courseId, writerId, regDate);


            ClubResponseDTO.ClubList response = clubService.createClub(dto, writerId, courseId, regDate, checkStatus);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
        }catch (Exception e) {
            log.error("Error creating club", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }


    // 페이징 처리된 club 목록 조회 (비동기 처리)
    @GetMapping("/{courseId}/list")
    public ResponseEntity<Map<String, Object>> getClubListByCourseId(@PathVariable(value = "courseId", required = false) Long courseId,
                                                     @RequestParam(value = "pageNum", defaultValue = "1") int pageNum,
                                                     @RequestParam(value = "type", required = false) String type,
                                                     @RequestParam(value = "keyword", required = false) String keyword) {
        if (courseId == null) {
            return ResponseEntity.ok(Collections.emptyMap());
        }

        // Criteria 설정
        Criteria cri = new Criteria();
        cri.setPageNum(pageNum);
        cri.setType(type);

        // 승인 상태 키워드 변환
        cri.setKeywordFromTypeAndKeyword(type, keyword);
//        if ("C".equals(type)) {
//            cri.setKeyword("대기".equals(keyword) ? "W" : "승인".equals(keyword) ? "Y" : "미승인".equals(keyword) ? "N" : "");
//        } else {
//            cri.setKeyword(keyword);
//        }

        // 페이징된 결과 조회
        Page<ClubResponseDTO.ClubList> clubPage = clubService.getClubListWithPaging(cri, courseId);

        Map<String, Object> response = new HashMap<>();
        response.put("list", clubPage.getContent());    //페이징된 동아리 리스트
        response.put("pageInfo", new PageInfoDTO(
                clubPage.getTotalElements(),
                clubPage.getTotalPages(),
                clubPage.getNumber() + 1,
                clubPage.getSize(),
                clubPage.hasNext(),
                clubPage.hasPrevious()
        ));    //페이지정보

        return ResponseEntity.ok(response);
    }

    // 페이지 정보 반환 (Pageable에서 제공하는 기본 정보 사용)
//    private Map<String, Object> getPageInfo(Page<ClubResponseDTO.ClubList> clubPage) {
//        Map<String, Object> pageInfo = new HashMap<>();
//        pageInfo.put("totalElements", clubPage.getTotalElements()); // 전체 데이터 개수
//        pageInfo.put("totalPages", clubPage.getTotalPages()); // 전체 페이지 수
//        pageInfo.put("currentPage", clubPage.getNumber() + 1); // 현재 페이지 (1부터 시작)
//        pageInfo.put("pageSize", clubPage.getSize()); // 한 페이지당 항목 수
//        pageInfo.put("hasNext", clubPage.hasNext()); // 다음 페이지 존재 여부
//        pageInfo.put("hasPrevious", clubPage.hasPrevious()); // 이전 페이지 존재 여부
//        return pageInfo;
//    }

    //상세
    @GetMapping("/{clubId}/detail")
    public ResponseEntity<ClubResponseDTO.ClubDetail> getClubDetail(@PathVariable("clubId") Long clubId) {
        ClubResponseDTO.ClubDetail club = clubService.getClubDetail(clubId);
        return ResponseEntity.ok(club);
    }

    //수정
    @GetMapping("/{clubId}")
    public ResponseEntity<ClubResponseDTO.ClubDetail> updateClub(@PathVariable Long clubId,
                                                                 @AuthenticationPrincipal CustomUserDetails customUserDetails) {

        Long loggedInUserId = customUserDetails.getId();
        Club club = clubService.getClub(clubId);

        if (!club.getWriterId().equals(loggedInUserId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null); // 작성자만 조회할 수 있음
        }

        ClubResponseDTO.ClubDetail clubResponse = clubService.getClubDetail(clubId);
        return ResponseEntity.ok(clubResponse);
    }

    @PutMapping("/{clubId}")
    public ResponseEntity<ClubResponseDTO.ClubList> updateClub(
            @PathVariable Long clubId,
            @RequestPart(value = "club", required = false) ClubRequestDTO.ClubUpdate clubRequest,
            @RequestPart(value = "file", required = false) MultipartFile file,
            @AuthenticationPrincipal CustomUserDetails customUserDetails){

        Long loggedInUserId = customUserDetails.getId();

        log.info("수신된 클럽 업데이트 요청: clubId={}, 데이터={}", clubId, clubRequest);
        log.info("수신된 파일 정보: {}", file != null ? file.getOriginalFilename() : "없음");

        ClubResponseDTO.ClubList clubResponse = clubService.updateClub(clubId, clubRequest, file, loggedInUserId);
        log.info("클럽 업데이트 성공: clubId={}", clubResponse.getClubId());
        return ResponseEntity.ok(clubResponse);
    }

    //삭제
    @DeleteMapping("/{clubId}")
    public ResponseEntity<Map<String, String>> deleteClub(
            @PathVariable("clubId") Long clubId,
            @AuthenticationPrincipal CustomUserDetails customUserDetails,
            @RequestParam(value = "courseId", required = false) Long courseId) {

        // 로그인한 사용자의 ID 가져오기
        Long loggedInUserId = customUserDetails.getId();
        Club club = clubService.getClub(clubId);

        if (!club.getWriterId().equals(loggedInUserId)) {
            throw new IllegalArgumentException("Only the creator can delete the club.");
        }

        if (club.getCheckStatus() != CheckStatus.W) {
            throw new IllegalArgumentException("Only clubs with pending approval can be deleted.");
        }

        clubService.delete(clubId, loggedInUserId);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Club deleted successfully");
        response.put("redirectUrl", courseId != null ? "/club/list?courseId=" + courseId : null);

        return ResponseEntity.ok(response);
    }

    // 파일 다운로드
//    @GetMapping("/file/download")
//    public ResponseEntity<Resource> downloadFile(@RequestParam("fileUrl") String fileUrl) {
//        try {
//            InputStream fileStream = s3Uploader.downloadFile(fileUrl);
//            InputStreamResource resource = new InputStreamResource(fileStream);
//
//            // 파일명 추출
//            String fileName = fileUrl.substring(fileUrl.lastIndexOf("/") + 1);
//
//            return ResponseEntity.ok()
//                    .header("Content-Disposition", "attachment; filename=\"" + fileName + "\"")
//                    .body(resource);
//        } catch (Exception e) {
//            throw new RuntimeException("파일 다운로드 중 에러가 발생했습니다.", e);
//        }
//    }


//

//
//    // Approve Club
//    @PatchMapping("/{clubId}/approval")
//    public ResponseEntity<ClubResponseDTO.ClubList> approveClub(@PathVariable Long clubId,
//                                                                @RequestBody ClubRequestDTO.ClubApproval dto,
//                                                                @RequestParam String checkerId,
//                                                                @RequestParam String url) {
//        ClubResponseDTO.ClubList response = clubService.approveClub(clubId, dto, checkerId, url);
//        return ResponseEntity.ok(response);
//    }
//
//
}

