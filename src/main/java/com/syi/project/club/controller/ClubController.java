package com.syi.project.club.controller;

import com.syi.project.club.dto.ClubRequestDTO;
import com.syi.project.club.dto.ClubResponseDTO;
import com.syi.project.club.entity.Club;
import com.syi.project.club.service.ClubService;
import com.syi.project.common.config.JwtProvider;
import com.syi.project.common.entity.Criteria;
import com.syi.project.common.enums.CheckStatus;
import com.syi.project.course.service.CourseService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.*;

@RestController
@RequestMapping("/club")
public class ClubController {

    private static final Logger log = LoggerFactory.getLogger(ClubController.class);

    @Autowired
    private ClubService clubService;
    @Autowired
    private JwtProvider jwtProvider;

    public ClubController(ClubService clubService) {
        this.clubService = clubService;
    }

    @Autowired
    private CourseService courseService;

//    @Value("C:/upload/temp")
//    private String fileUploadPath;

    // 등록

    // 등록 (비동기 처리)
//    @GetMapping("/club/register")
//    public ResponseEntity<String> clubRegister(@RequestBody ClubDTO club) {
//        boolean success = clubService.register(club);
//        if (success) {
//            return new ResponseEntity<>("register success", HttpStatus.OK);
//        } else {
//            return new ResponseEntity<>("register fail", HttpStatus.BAD_REQUEST);
//        }
//    }

//    @PostMapping
//    @ResponseBody
//    public ResponseEntity<Map<String, String>> createClub(@RequestBody ClubRequestDTO.ClubCreate club,
//                                                          AuthenticationPrincipal Long memberId,
//                                                          @RequestParam(value = "courseId", required = false) Long courseId) {
//        log.info("ClubDTO : " + club);
//
//        System.out.println("enroll post courseId : " + courseId);
//
//        Map<String, String> response = new HashMap<>();
//
//        if(clubService.register(club, courseId, memberId)) {
//            response.put("result", "enroll success");
//            return new ResponseEntity<>(response, HttpStatus.OK);
//        }else{
//            response.put("result", "enroll failed");
//            response.put("message", "Please try again later");
//            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
//        }
////        if (result) {
////            return ResponseEntity.ok("Club registered successfully.");
////        } else {
////            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Club registration failed.");
////        }
//    }

    // 등록
    @PostMapping
    public ResponseEntity<ClubResponseDTO.ClubList> createClub(@RequestBody ClubRequestDTO.ClubCreate dto,
                                                               @RequestParam Long courseId,
                                                               @RequestHeader("Authorization") String token) {

        Long writerId = jwtProvider.getMemberPrimaryKeyId(token).orElse(null);
        LocalDate regDate = LocalDate.now();
        CheckStatus checkStatus = CheckStatus.W;

        ClubResponseDTO.ClubList response = clubService.createClub(dto, writerId, courseId, regDate, checkStatus);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }


    // 페이징 처리된 club 목록 조회 (비동기 처리)
    @GetMapping
    public Map<String, Object> getClubListByCourseId(@RequestParam(value = "courseId", required = false) Long courseId,
                                                     @RequestParam(value = "pageNum", defaultValue = "1") int pageNum,
                                                     @RequestParam(value = "type", required = false) String type,
                                                     @RequestParam(value = "keyword", required = false) String keyword) {
        if (courseId == null) {
            return Collections.emptyMap();
        }

        // Criteria 설정
        Criteria cri = new Criteria();
        cri.setPageNum(pageNum);
        cri.setType(type);

        // 승인 상태 키워드 변환
        if ("C".equals(type)) {
            cri.setKeyword("대기".equals(keyword) ? "W" : "승인".equals(keyword) ? "Y" : "미승인".equals(keyword) ? "N" : "");
        } else {
            cri.setKeyword(keyword);
        }

        // 페이징된 결과 조회
        Page<ClubResponseDTO.ClubList> clubPage = clubService.getClubListWithPaging(cri, courseId);

        Map<String, Object> response = new HashMap<>();
        response.put("list", clubPage.getContent());    //페이징된 동아리 리스트
        response.put("pageInfo", getPageInfo(clubPage));    //페이지정보

        return response;
    }

    // 페이지 정보 반환 (Pageable에서 제공하는 기본 정보 사용)
    private Map<String, Object> getPageInfo(Page<ClubResponseDTO.ClubList> clubPage) {
        Map<String, Object> pageInfo = new HashMap<>();
        pageInfo.put("totalElements", clubPage.getTotalElements()); // 전체 데이터 개수
        pageInfo.put("totalPages", clubPage.getTotalPages()); // 전체 페이지 수
        pageInfo.put("currentPage", clubPage.getNumber() + 1); // 현재 페이지 (1부터 시작)
        pageInfo.put("pageSize", clubPage.getSize()); // 한 페이지당 항목 수
        pageInfo.put("hasNext", clubPage.hasNext()); // 다음 페이지 존재 여부
        pageInfo.put("hasPrevious", clubPage.hasPrevious()); // 이전 페이지 존재 여부
        return pageInfo;
    }












    // Get Club
//    @GetMapping("/{clubId}")
//    public ResponseEntity<ClubResponseDTO.ClubList> getClub(@PathVariable Long clubId,
//                                                            @RequestParam String url) {
//        ClubResponseDTO.ClubList response = clubService.getClub(clubId, url);
//        return ResponseEntity.ok(response);
//    }



    // 상세조회 (비동기 처리)
//    @GetMapping("/club/get")
//    public ResponseEntity<ClubDTO> clubGetPageGET(@RequestParam("clubNo") int clubNo) {
//        ClubDTO club = clubService.getPage(clubNo);
//        return new ResponseEntity<>(club, HttpStatus.OK);
//    }

//    // 상세
//    @GetMapping("/club/get")
//    public Map<String, Object> clubGetDetail(@RequestParam("clubNo") int clubNo) {
//        ClubResponseDTO club = clubService.getPage(clubNo);
//
//        Map<String, Object> response = new HashMap<>();
//        response.put("club", club);
//
//        return response;
//    }

    // 수정 (비동기 처리)
//    @PostMapping("/class/club/modify")
//    public ResponseEntity<String> clubModifyAdminPOST(@RequestBody ClubDTO club) {
//        boolean success = clubService.modifyAdmin(club);
//        if (success) {
//            return new ResponseEntity<>("modify success", HttpStatus.OK);
//        } else {
//            return new ResponseEntity<>("modify fail", HttpStatus.BAD_REQUEST);
//        }
//    }


    // 수정
//    @PostMapping
//    public ResponseEntity<ClubResponseDTO.ClubList> updateClub(@PathVariable Long courseId,
//                                                               @RequestBody ClubRequestDTO.ClubUpdate dto,
//                                                               @RequestParam String url) {
//        ClubResponseDTO.ClubList response = clubService.updateClub();
//
//
//    public ResponseEntity<String> clubModify(@RequestBody @Valid ClubResponseDTO club,
//                                             @RequestParam("clubNo") int clubNo,
//                                             @RequestParam(value = "courseId", required = false) Long courseId,
//                                             @RequestParam(value = "file", required = false) MultipartFile file) {
//
//        // 승인 상태일 때 파일 첨부 검증
//        if ("승인".equals(club.getCheckStatus())) {
//            if (file == null || file.isEmpty()) {
//                return ResponseEntity.badRequest().body("파일을 선택해 주세요.");
//            }
//        }
//
//
//
//            // 클럽 정보 수정
//            clubService.modify(club);
//            return ResponseEntity.ok("modify success");
//
//        } catch (IOException e) {
//            log.severe(">>> File upload failed: " + e.getMessage());
//            return ResponseEntity.status(500).body("파일 업로드 실패");
//        }
//    }
//}
//
//    // 첨부파일 다운로드
//    @GetMapping("/club/downloadFile")
//    public ResponseEntity<InputStreamResource> downloadFile(@RequestParam("fileName") String fileName) {
//        try {
//            // 파일 이름 정리(불필요한 경로 구분기호 제거하고 이름만 남기기)
//            String cleanedFileName = StringUtils.cleanPath(fileName);
//
//            // 파일 객체 생성(최종적인 파일 경로를 생성함 (파일 저장할 경로 + 정리된 파일 이름)
//            File file = new File(fileUploadPath, cleanedFileName);
//
//            // 파일이 존재하지 않으면 404 에러 반환
//            if (!file.exists()) {
//                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
//            }
//
//            // 파일 입력 스트림 생성
//            InputStream inputStream = new FileInputStream(file);
//
//            // HTTP 헤더 설정 (파일 다운로드를 위한 설정)
//            HttpHeaders headers = new HttpHeaders();
//            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + URLEncoder.encode(cleanedFileName, "UTF-8"));
//
//            // 파일 스트림을 ResponseEntity로 반환
//            InputStreamResource resource = new InputStreamResource(inputStream);
//            return ResponseEntity.ok()
//                    .headers(headers)
//                    .body(resource);
//        } catch (IOException e) {
//            // 서버 에러 시 500 에러 반환
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
//        }
//    }

//    // 삭제 처리
//    @PostMapping("/club/delete")
//    @ResponseBody
//    public String clubDelete(@RequestParam("clubNo") int clubNo) {
//        clubService.delete(clubNo);
//        return "delete success";
//    }


//
//    // Update Club
//    @PutMapping("/{clubId}")
//    public ResponseEntity<ClubResponseDTO.ClubList> updateClub(@PathVariable Long clubId,
//                                                               @RequestBody ClubRequestDTO.ClubUpdate dto,
//                                                               @RequestParam String url) {
//        ClubResponseDTO.ClubList response = clubService.updateClub(clubId, dto, url);
//        return ResponseEntity.ok(response);
//    }
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

