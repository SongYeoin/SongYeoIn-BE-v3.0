package com.syi.project.club.controller.admin;

import com.syi.project.auth.dto.AuthUserDTO;
import com.syi.project.auth.service.CustomUserDetails;
import com.syi.project.club.dto.ClubRequestDTO;
import com.syi.project.club.dto.ClubResponseDTO;
import com.syi.project.club.entity.Club;
import com.syi.project.club.service.ClubService;
import com.syi.project.common.config.JwtProvider;
import com.syi.project.common.dto.PageInfoDTO;
import com.syi.project.common.entity.Criteria;
import com.syi.project.common.enums.CheckStatus;
import com.syi.project.common.enums.Role;
import com.syi.project.common.exception.ErrorCode;
import com.syi.project.common.exception.InvalidRequestException;
import com.syi.project.common.utils.S3Uploader;
import com.syi.project.course.dto.CourseDTO;
import com.syi.project.course.dto.CourseResponseDTO;
import com.syi.project.course.service.CourseService;
import io.swagger.v3.oas.annotations.Operation;
import java.io.InputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin/club")
public class AdminClubController {

    private static final Logger log = LoggerFactory.getLogger(AdminClubController.class);

    private final ClubService clubService;
    @Autowired
    private JwtProvider jwtProvider;
    @Autowired
    private CourseService courseService;
    @Autowired
    private S3Uploader s3Uploader;

    public AdminClubController(ClubService clubService) {
        this.clubService = clubService;
    }

    @GetMapping
    public ResponseEntity<List<CourseDTO>> getAvailableCourses(){
        List<CourseDTO> availableCourses = courseService.getAvailableCourses();
        log.info("성공적으로 {} 개의 교육 과정을 조회했습니다.", availableCourses.size());

        return ResponseEntity.ok(availableCourses);

    }

    // 페이징 처리된 club 목록 조회 (비동기 처리)
    @GetMapping("/{courseId}/list")
    public ResponseEntity<Map<String, Object>> getClubListByCourseId(@PathVariable(value = "courseId", required = false) Long courseId,
                                                                     @RequestParam(value = "pageNum", defaultValue = "1") int pageNum,
                                                                     @RequestParam(value = "status", required = false) String status) {
        if (courseId == null) {
            return ResponseEntity.ok(Collections.emptyMap());
        }

        // Criteria 설정
        Criteria cri = new Criteria();
        cri.setPageNum(pageNum);

        // 승인 상태 키워드 변환
        //cri.setKeywordFromTypeAndKeyword(type, keyword);
        // status 파라미터를 type과 keyword로 변환
        if (status != null && !status.equals("ALL")) {
            cri.setType("C");
            cri.setKeyword(status); // Y, N, W 값을 그대로 전달
        }


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
//    @GetMapping("/{clubId}")
//    public ResponseEntity<ClubResponseDTO.ClubDetail> updateClub(@PathVariable Long clubId,
//                                                                 @AuthenticationPrincipal CustomUserDetails customUserDetails) {
//        ClubResponseDTO.ClubDetail clubResponse = clubService.getClubDetail(clubId);
//        return ResponseEntity.ok(clubResponse);
//    }

    @PutMapping("/{clubId}")
    public ResponseEntity<ClubResponseDTO.ClubList> updateClub(
            @PathVariable Long clubId,
            @RequestBody ClubRequestDTO.ClubApproval clubRequest,
            @AuthenticationPrincipal CustomUserDetails customUserDetails){

        if(!customUserDetails.getRole().equals(Role.ADMIN)){
            throw new InvalidRequestException(ErrorCode.ACCESS_DENIED);
        }

        Long adminId = customUserDetails.getId();
        ClubResponseDTO.ClubList clubResponse = clubService.approveClub(clubId, clubRequest, adminId);
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

        if (!customUserDetails.getRole().equals(Role.ADMIN)) {
            throw new IllegalArgumentException("Only administrators can delete the club.");
        }

        Club club = clubService.getClub(clubId);

        if (club.getCheckStatus() != CheckStatus.W) {
            throw new IllegalArgumentException("Only clubs with pending approval can be deleted.");
        }

        clubService.deleteAsAdmin(clubId, loggedInUserId);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Club deleted successfully");
        response.put("redirectUrl", courseId != null ? "/admin/club/list?courseId=" + courseId : null);

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "클럽 파일 다운로드")
    @GetMapping("/{clubId}/download")
    public ResponseEntity<Resource> downloadClubFile(
      @PathVariable Long clubId,
      @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        log.info("클럽 파일 다운로드 요청 - clubId: {}, memberId: {}, 역할: {}",
          clubId, userDetails.getId(), userDetails.getRole());

        return clubService.downloadClubFile(clubId, userDetails.getId());
    }

    @Operation(summary = "클럽 파일 일괄 다운로드")
    @PostMapping("/download-batch")
    public ResponseEntity<Resource> downloadClubFilesBatch(
      @RequestBody List<Long> clubIds,
      @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        log.info("클럽 파일 일괄 다운로드 요청 - clubIds: {}, memberId: {}, 역할: {}",
          clubIds, userDetails.getId(), userDetails.getRole());

        return clubService.downloadClubFilesBatch(clubIds, userDetails.getId());
    }
}
