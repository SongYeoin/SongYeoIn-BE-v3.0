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
import com.syi.project.course.service.CourseService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collections;
import java.util.HashMap;
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

    public AdminClubController(ClubService clubService) {
        this.clubService = clubService;
    }


    // 페이징 처리된 club 목록 조회 (비동기 처리)
    @GetMapping
    public ResponseEntity<Map<String, Object>> getClubListByCourseId(@RequestParam(value = "courseId", required = false) Long courseId,
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
    @GetMapping("/{clubId}")
    public ResponseEntity<ClubResponseDTO.ClubDetail> getClubDetail(@PathVariable("clubId") Long clubId) {
        ClubResponseDTO.ClubDetail club = clubService.getClubDetail(clubId);
        return ResponseEntity.ok(club);
    }

    //수정
    @GetMapping("/{clubId}")
    public ResponseEntity<ClubResponseDTO.ClubDetail> updateClub(@PathVariable Long clubId,
                                                                 @AuthenticationPrincipal CustomUserDetails customUserDetails) {
        ClubResponseDTO.ClubDetail clubResponse = clubService.getClubDetail(clubId);
        return ResponseEntity.ok(clubResponse);
    }

    @PutMapping("/{clubId}")
    public ResponseEntity<ClubResponseDTO.ClubList> updateClub(
            @PathVariable Long clubId,
            @RequestPart(value = "club", required = false) ClubRequestDTO.ClubApproval clubRequest,
            @AuthenticationPrincipal AuthUserDTO authUserDTO){

        if(!authUserDTO.getRole().equals(Role.ADMIN)){
            throw new InvalidRequestException(ErrorCode.ACCESS_DENIED);
        }

        Long adminId = authUserDTO.getId();
        ClubResponseDTO.ClubList clubResponse = clubService.approveClub(clubId, clubRequest, adminId);
        return ResponseEntity.ok(clubResponse);
    }

    //삭제
    @DeleteMapping("/{clubId}")
    public ResponseEntity<Map<String, String>> deleteClub(
            @PathVariable("clubId") Long clubId,
            @AuthenticationPrincipal AuthUserDTO authUserDTO,
            @RequestParam(value = "courseId", required = false) Long courseId) {


        if (!authUserDTO.getRole().equals(Role.ADMIN)) {
            throw new IllegalArgumentException("Only administrators can delete the club.");
        }

        Club club = clubService.getClub(clubId);

        if (club.getCheckStatus() != CheckStatus.W) {
            throw new IllegalArgumentException("Only clubs with pending approval can be deleted.");
        }

        clubService.deleteAsAdmin(clubId, authUserDTO.getId());

        Map<String, String> response = new HashMap<>();
        response.put("message", "Club deleted successfully");
        response.put("redirectUrl", courseId != null ? "/club/list?courseId=" + courseId : null);

        return ResponseEntity.ok(response);
    }








//    // 수정 (비동기 처리)
//    @PostMapping("/club/modify")
//    public ResponseEntity<String> clubModifyAdminPOST(@RequestBody ClubResponseDTO club) {
//        boolean success = clubService.modifyAdmin(club);
//        if (success) {
//            return new ResponseEntity<>("modify success", HttpStatus.OK);
//        } else {
//            return new ResponseEntity<>("modify fail", HttpStatus.BAD_REQUEST);
//        }
//    }

//    // 삭제 (비동기 처리)
//    @PostMapping("/club/delete")
//    public ResponseEntity<String> clubDeletePOST(@RequestParam("clubNo") int clubNo) {
//        boolean success = clubService.delete(clubNo);
//        if (success) {
//            return new ResponseEntity<>("delete success", HttpStatus.OK);
//        } else {
//            return new ResponseEntity<>("delete fail", HttpStatus.BAD_REQUEST);
//        }
//    }




}
