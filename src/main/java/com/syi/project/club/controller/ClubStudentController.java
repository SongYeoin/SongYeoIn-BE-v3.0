package com.syi.project.club.controller;

import com.syi.project.club.dto.ClubDTO;
import com.syi.project.club.dto.ClubRequestDTO;
import com.syi.project.club.service.ClubService;
import com.syi.project.common.entity.Criteria;
import com.syi.project.common.config.JwtProvider;
import com.syi.project.course.service.CourseService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/")
public class ClubStudentController {

    private static final Logger log = LoggerFactory.getLogger(ClubStudentController.class);

    @Autowired
    private ClubService clubService;

    public ClubStudentController(ClubService clubService) {this.clubService = clubService; }

    @Autowired
    private CourseService courseService;

    @Value("C:/upload/temp")
    private String fileUploadPath;

    @Autowired
    private JwtProvider jwtProvider;

    // 등록
    @GetMapping("/club/register")
    public void clubRegister(@RequestParam(value = "classNo", required = false) Integer classNo) {
        log.info("등록 파트 진입");
        System.out.println("enroll get classNo : " + classNo);
    }

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

    @PostMapping("/club/register")
    @ResponseBody
    public Map<String, String> clubRegister(@RequestBody ClubRequestDTO.ClubCreate club, @RequestHeader("Authorization") String token, @RequestParam(value = "classNo", required = false) Integer classNo) {
        log.info("ClubDTO : " + club);

        // JWT 토큰에서 memberNo 추출
        String memberId = jwtProvider.getMemberId(token);

        System.out.println("enroll post classNo : " + classNo);

        clubService.register(club, classNo, memberId);

        Map<String, String> response = new HashMap<>();
        response.put("result", "enroll success");

        return response;

//        if (result) {
//            return ResponseEntity.ok("Club registered successfully.");
//        } else {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Club registration failed.");
//        }
    }

    // 페이징 처리된 club 목록 조회 (비동기 처리)
    @GetMapping("/club/list")
    public Map<String, Object> getClubListByClassNo(@RequestParam(value = "classNo", required = false) Integer classNo,
                                                    @RequestParam(value = "pageNum", defaultValue = "1") int pageNum,
                                                    @RequestParam(value = "type", required = false) String type,
                                                    @RequestParam(value = "keyword", required = false) String keyword) {
        if (classNo == null) {
            return Collections.emptyMap();
        }

        // Pageable 객체 생성
        //Pageable pageable = PageRequest.of(pageNum - 1, 10, Sort.by(Sort.Order.asc("clubNo"))); // pageNum - 1 because Pageable starts from 0

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
        Page<ClubDTO> clubPage = clubService.getClubListWithPaging(cri, classNo);

        Map<String, Object> response = new HashMap<>();
        response.put("list", clubPage.getContent());    //페이징된 동아리 리스트
        response.put("pageInfo", getPageInfo(clubPage));    //페이지정보

        return response;
    }

    // 페이지 정보 반환 (Pageable에서 제공하는 기본 정보 사용)
    private Map<String, Object> getPageInfo(Page<ClubDTO> clubPage) {
        Map<String, Object> pageInfo = new HashMap<>();
        pageInfo.put("totalElements", clubPage.getTotalElements()); // 전체 데이터 개수
        pageInfo.put("totalPages", clubPage.getTotalPages()); // 전체 페이지 수
        pageInfo.put("currentPage", clubPage.getNumber() + 1); // 현재 페이지 (1부터 시작)
        pageInfo.put("pageSize", clubPage.getSize()); // 한 페이지당 항목 수
        pageInfo.put("hasNext", clubPage.hasNext()); // 다음 페이지 존재 여부
        pageInfo.put("hasPrevious", clubPage.hasPrevious()); // 이전 페이지 존재 여부
        return pageInfo;
    }

    // 상세조회 (비동기 처리)
//    @GetMapping("/club/get")
//    public ResponseEntity<ClubDTO> clubGetPageGET(@RequestParam("clubNo") int clubNo) {
//        ClubDTO club = clubService.getPage(clubNo);
//        return new ResponseEntity<>(club, HttpStatus.OK);
//    }

    // 상세
    @GetMapping("/club/get")
    public Map<String, Object> clubGetDetail(@RequestParam("clubNo") int clubNo) {
        ClubDTO club = clubService.getPage(clubNo);

        Map<String, Object> response = new HashMap<>();
        response.put("club", club);

        return response;
    }

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
    @PostMapping("/club/modify")
    public String clubModify(@RequestBody ClubDTO club,
                                 @RequestParam("clubNo") int clubNo,
                                 @RequestParam(value = "classNo", required = false) Integer classNo,
                                 @RequestParam(value = "file", required = false) MultipartFile file) throws Exception {

        // 승인 상태일 때 파일 첨부 검증
        if ("승인".equals(club.getCheckStatus())) {
            if (file == null || file.isEmpty()) {
                return "파일을 선택해 주세요.";
            }
        }

        // 파일 업로드 처리
        if (file != null && !file.isEmpty()) {
            // 파일 이름 정리 (파일의 불필요한 경로, 요소를 제거하고 이름만 남겨놓음)
            String fileName = StringUtils.cleanPath(file.getOriginalFilename());
            // 파일이 저장될 경로 설정
            Path uploadPath = Paths.get(fileUploadPath);

            log.info(">>> File upload path: {}", uploadPath);

            // 업로드 경로가 존재하지 않으면 생성
            if (!Files.exists(uploadPath)) {
                log.info(">>> Creating directory: {}", uploadPath);
                Files.createDirectories(uploadPath);
            }

            // 파일 저장 경로 설정
            Path filePath = uploadPath.resolve(fileName);
            log.info(">>> File path: {}", filePath);

            try {
                // 기존 파일 삭제
                if (club.getfileOriginalName() != null && !club.getfileOriginalName().isEmpty()) {
                    Path oldFilePath = uploadPath.resolve(club.getfileOriginalName());
                    if (Files.exists(oldFilePath)) {
                        log.info(">>> Deleting old file: {}", oldFilePath);
                        Files.delete(oldFilePath);
                    }
                }

                // 파일 저장
                Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
                club.setFileName(fileName); // 파일 이름 설정
                log.info(">>> File uploaded successfully: {}", fileName);
            } catch (IOException e) {
                log.error(">>> File upload failed: {}", fileName, e);
                throw new Exception("파일 업로드 실패", e);
            }
        }

        // 클럽 정보 수정
        clubService.modify(club);
        return "modify success";
    }

    // 첨부파일 다운로드
    @GetMapping("/club/downloadFile")
    public ResponseEntity<InputStreamResource> downloadFile(@RequestParam("fileName") String fileName) {
        try {
            // 파일 이름 정리(불필요한 경로 구분기호 제거하고 이름만 남기기)
            String cleanedFileName = StringUtils.cleanPath(fileName);

            // 파일 객체 생성(최종적인 파일 경로를 생성함 (파일 저장할 경로 + 정리된 파일 이름)
            File file = new File(fileUploadPath, cleanedFileName);

            // 파일이 존재하지 않으면 404 에러 반환
            if (!file.exists()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
            }

            // 파일 입력 스트림 생성
            InputStream inputStream = new FileInputStream(file);

            // HTTP 헤더 설정 (파일 다운로드를 위한 설정)
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + URLEncoder.encode(cleanedFileName, "UTF-8"));

            // 파일 스트림을 ResponseEntity로 반환
            InputStreamResource resource = new InputStreamResource(inputStream);
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(resource);
        } catch (IOException e) {
            // 서버 에러 시 500 에러 반환
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    // 삭제 처리
    @PostMapping("/club/delete")
    @ResponseBody
    public String clubDelete(@RequestParam("clubNo") int clubNo) {
        clubService.delete(clubNo);
        return "delete success";
    }
}
