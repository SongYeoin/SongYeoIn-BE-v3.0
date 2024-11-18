package com.syi.project.club.controller.admin;

import com.syi.project.club.dto.ClubDTO;
import com.syi.project.club.service.ClubService;
import com.syi.project.common.Criteria;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/manager")
public class ClubManagerController {

    private static final Logger log = LoggerFactory.getLogger(ClubManagerController.class);

    private final ClubService clubService;

    public ClubManagerController(ClubService clubService) {
        this.clubService = clubService;
    }



    // 페이징 처리된 club 목록 조회 (비동기 처리)
    @GetMapping("/class/club/list")
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
    @GetMapping("/class/club/get")
    public ResponseEntity<ClubDTO> clubGetPageGET(@RequestParam("clubNo") int clubNo) {
        ClubDTO club = clubService.getPage(clubNo);
        return new ResponseEntity<>(club, HttpStatus.OK);
    }

    // 수정 (비동기 처리)
    @PostMapping("/class/club/modify")
    public ResponseEntity<String> clubModifyAdminPOST(@RequestBody ClubDTO club) {
        boolean success = clubService.modifyAdmin(club);
        if (success) {
            return new ResponseEntity<>("modify success", HttpStatus.OK);
        } else {
            return new ResponseEntity<>("modify fail", HttpStatus.BAD_REQUEST);
        }
    }

    // 삭제 (비동기 처리)
    @PostMapping("/class/club/delete")
    public ResponseEntity<String> clubDeletePOST(@RequestParam("clubNo") int clubNo) {
        boolean success = clubService.delete(clubNo);
        if (success) {
            return new ResponseEntity<>("delete success", HttpStatus.OK);
        } else {
            return new ResponseEntity<>("delete fail", HttpStatus.BAD_REQUEST);
        }
    }




}
