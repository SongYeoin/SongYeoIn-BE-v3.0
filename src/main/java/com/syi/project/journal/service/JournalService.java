package com.syi.project.journal.service;

import com.syi.project.auth.entity.Member;
import com.syi.project.auth.repository.MemberRepository;
import com.syi.project.common.entity.Criteria;
import com.syi.project.common.enums.CourseStatus;
import com.syi.project.common.enums.Role;
import com.syi.project.common.utils.S3Uploader;
import com.syi.project.course.dto.CourseResponseDTO;
import com.syi.project.course.entity.Course;
import com.syi.project.course.repository.CourseRepository;
import com.syi.project.enroll.repository.EnrollRepository;
import com.syi.project.file.dto.FileDownloadDTO;
import com.syi.project.file.service.FileService;
import com.syi.project.journal.dto.JournalCourseResponseDTO;
import com.syi.project.journal.dto.JournalRequestDTO;
import com.syi.project.journal.dto.JournalResponseDTO;
import com.syi.project.journal.entity.Journal;
import com.syi.project.journal.entity.JournalFile;
import com.syi.project.journal.repository.JournalFileRepository;
import com.syi.project.journal.repository.JournalRepository;
import com.syi.project.file.entity.File;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class JournalService {

  private final JournalRepository journalRepository;
  private final MemberRepository memberRepository;
  private final CourseRepository courseRepository;
  @Autowired  // 추가
  private final FileService fileService;
  private final JournalFileRepository journalFileRepository;
  private final S3Uploader s3Uploader;
  private final EnrollRepository enrollRepository;  // 추가된 수강생-교육과정 매핑을 위한 레포지토리

  private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList("hwp", "docx", "doc");

  // 학생 교육일지 검색 메서드
  public Page<JournalResponseDTO> getStudentJournals(
      Long memberId,
      Long courseId,
      LocalDate startDate,
      LocalDate endDate,
      Criteria criteria
  ) {
    log.debug("학생 교육일지 검색 시작 - memberId: {}, courseId: {}, criteria: {}", memberId, courseId, criteria);

    // 날짜 및 페이징 조건 유효성 검증
    validateSearchCriteria(startDate, endDate, criteria.getPageNum());
    // 수강생-교육과정 매칭 검증
    validateEnrollment(memberId, courseId);

    // 페이지 크기 설정 (20개로 고정)
    criteria.setAmount(20);

    // 학생 전용 교육일지 검색
    Page<Journal> journals = journalRepository.searchJournalsForStudent(
        memberId,
        courseId,
        startDate,
        endDate,
        PageRequest.of(criteria.getPageNum() - 1, criteria.getAmount())
    );

    log.info("학생 교육일지 검색 완료 - 총 {}건 검색됨", journals.getTotalElements());

    return journals.map(journal -> JournalResponseDTO.from(journal, s3Uploader));
  }

  // 관리자 교육일지 검색 메서드
  public Page<JournalResponseDTO> getAdminJournals(
      Long courseId,
      String searchType,
      String searchKeyword,
      LocalDate startDate,
      LocalDate endDate,
      Criteria criteria
  ) {
    log.debug("관리자 교육일지 검색 시작 - courseId: {}, searchType: {}, keyword: {}, criteria: {}",
        courseId, searchType, searchKeyword, criteria);

    // 날짜 및 페이징 조건 유효성 검증
    validateSearchCriteria(startDate, endDate, criteria.getPageNum());

    // 페이지 크기 설정 (20개로 고정)
    criteria.setAmount(20);

    // 관리자 전용 교육일지 검색
    Page<Journal> journals = journalRepository.searchJournalsForAdmin(
        courseId,
        searchType,
        searchKeyword,
        startDate,
        endDate,
        PageRequest.of(criteria.getPageNum() - 1, criteria.getAmount())
    );

    log.info("관리자 교육일지 검색 완료 - 총 {}건 검색됨", journals.getTotalElements());

    return journals.map(journal -> JournalResponseDTO.from(journal, s3Uploader));
  }

  // 날짜 및 페이징 조건 유효성 검증 메서드
  private void validateSearchCriteria(LocalDate startDate, LocalDate endDate, int pageNum) {
    log.debug("날짜 및 페이징 조건 유효성 검증 시작 - startDate: {}, endDate: {}, pageNum: {}", startDate, endDate, pageNum);

    if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
      log.error("잘못된 날짜 범위 - startDate: {}, endDate: {}", startDate, endDate);
      throw new IllegalArgumentException("시작일이 종료일보다 늦을 수 없습니다.");
    }

    if (pageNum <= 0) {
      log.error("잘못된 페이지 번호 - pageNum: {}", pageNum);
      throw new IllegalArgumentException("페이지 번호는 1 이상이어야 합니다.");
    }

    log.debug("날짜 및 페이징 조건 유효성 검증 완료");
  }

  // 수강생-교육과정 매칭 검증 메서드
  private void validateEnrollment(Long memberId, Long courseId) {
    log.debug("수강생-교육과정 매칭 검증 시작 - memberId: {}, courseId: {}", memberId, courseId);

    boolean isEnrolled = enrollRepository.existsByMemberIdAndCourseId(memberId, courseId);
    if (!isEnrolled) {
      log.error("수강생-교육과정 매칭 실패 - memberId: {}, courseId: {}", memberId, courseId);
      throw new IllegalArgumentException("해당 과정의 수강생이 아닙니다.");
    }

    log.debug("수강생-교육과정 매칭 검증 완료");
  }

  @Transactional
  public JournalResponseDTO createJournal(Long memberId, JournalRequestDTO requestDTO) {
    log.debug("교육일지 등록 처리 시작 - memberId: {}, courseId: {}", memberId, requestDTO.getCourseId());

    Member member = memberRepository.findById(memberId)
        .orElseThrow(() -> {
          log.error("회원 정보를 찾을 수 없음 - memberId: {}", memberId);
          return new IllegalArgumentException("존재하지 않는 회원입니다.");
        });

    // 관리자 권한 체크 추가
    if (member.getRole() == Role.ADMIN) {
      log.error("관리자의 교육일지 등록 시도 - memberId: {}", memberId);
      throw new IllegalArgumentException("관리자는 교육일지를 등록할 수 없습니다.");
    }

    // 수강생-교육과정 매칭 검증 추가
    validateEnrollment(memberId, requestDTO.getCourseId());

    // 파일 필수 체크 추가
    if (requestDTO.getFile() == null || requestDTO.getFile().isEmpty()) {
      log.error("파일 미첨부 - memberId: {}", memberId);
      throw new IllegalArgumentException("교육일지 등록 시 파일 첨부는 필수입니다.");
    }

    Course course = courseRepository.findById(requestDTO.getCourseId())
        .orElseThrow(() -> {
          log.error("강좌 정보를 찾을 수 없음 - courseId: {}", requestDTO.getCourseId());
          return new IllegalArgumentException("존재하지 않는 강좌입니다.");
        });

    Journal journal = Journal.builder()
        .member(member)
        .course(course)
        .title(requestDTO.getTitle())
        .content(requestDTO.getContent())
        .build();

    // 단일 파일 처리
    if (requestDTO.getFile() != null && !requestDTO.getFile().isEmpty()) {
      validateFile(requestDTO.getFile());
      File savedFile = fileService.uploadFile(requestDTO.getFile(), "journals", member);
      log.info("교육일지 파일 업로드 완료 - fileName: {}", savedFile.getOriginalName());

      JournalFile journalFile = JournalFile.builder()
          .journal(journal)
          .file(savedFile)
          .build();

      journal.setFile(journalFile);
    }

    Journal savedJournal = journalRepository.save(journal);
    log.info("교육일지 등록 완료 - journalId: {}", savedJournal.getId());
    return JournalResponseDTO.from(savedJournal, s3Uploader);
  }

  public List<JournalResponseDTO> getJournalsByCourse(Long courseId, Long memberId) {
    log.debug("교육일지 목록 조회 시작 - courseId: {}, memberId: {}", courseId, memberId);

    Member member = memberRepository.findById(memberId)
        .orElseThrow(() -> {
          log.error("회원 정보를 찾을 수 없음 - memberId: {}", memberId);
          return new IllegalArgumentException("존재하지 않는 회원입니다.");
        });

    List<Journal> journals;
    if (member.getRole() == Role.ADMIN) {
      log.debug("관리자 권한으로 전체 교육일지 조회");
      journals = journalRepository.findByCourseId(courseId);
    } else {
      log.debug("수강생 권한으로 본인 교육일지만 조회");
      journals = journalRepository.findByCourseIdAndMemberId(courseId, memberId);
    }

    log.info("교육일지 목록 조회 완료 - courseId: {}, 조회된 교육일지 수: {}", courseId, journals.size());
    return journals.stream()
        .map(journal -> JournalResponseDTO.from(journal, s3Uploader))
        .collect(Collectors.toList());
  }

  public JournalResponseDTO getJournal(Long journalId, Long memberId) {
    log.debug("교육일지 상세 조회 시작 - journalId: {}, memberId: {}", journalId, memberId);

    Member member = memberRepository.findById(memberId)
        .orElseThrow(() -> {
          log.error("회원 정보를 찾을 수 없음 - memberId: {}", memberId);
          return new IllegalArgumentException("존재하지 않는 회원입니다.");
        });

    Journal journal = journalRepository.findById(journalId)
        .orElseThrow(() -> {
          log.error("교육일지를 찾을 수 없음 - journalId: {}", journalId);
          return new IllegalArgumentException("존재하지 않는 교육일지입니다.");
        });

    if (member.getRole() == Role.ADMIN ||
        journal.getMember().getId().equals(memberId)) {
      log.info("교육일지 상세 조회 완료 - journalId: {}, 제목: {}", journalId, journal.getTitle());
      return JournalResponseDTO.from(journal, s3Uploader);
    }

    log.error("교육일지 접근 권한 없음 - memberId: {}, journalId: {}", memberId, journalId);
    throw new IllegalArgumentException("해당 교육일지에 대한 접근 권한이 없습니다.");
  }

  @Transactional
  public JournalResponseDTO updateJournal(Long memberId, Long journalId, JournalRequestDTO requestDTO) {
    log.debug("교육일지 수정 시작 - journalId: {}, memberId: {}", journalId, memberId);

    // 회원 확인 (유지)
    Member member = memberRepository.findById(memberId)
        .orElseThrow(() -> new IllegalArgumentException("회원이 존재하지 않습니다."));

    // 교육일지 확인 (유지)
    Journal journal = journalRepository.findByIdAndMemberId(journalId, memberId)
        .orElseThrow(() -> {
          log.error("교육일지를 찾을 수 없거나 접근 권한 없음 - journalId: {}, memberId: {}", journalId, memberId);
          return new IllegalArgumentException("존재하지 않거나 접근 권한이 없는 교육일지입니다.");
        });

    // 파일 필수 체크 (유지)
    if (requestDTO.getFile() == null || requestDTO.getFile().isEmpty()) {
      log.error("파일 미첨부 - memberId: {}", memberId);
      throw new IllegalArgumentException("교육일지 수정 시 파일 첨부는 필수입니다.");
    }

    // 파일 수정 로직 (수정)
    if (requestDTO.getFile() != null && !requestDTO.getFile().isEmpty()) {
      validateFile(requestDTO.getFile());

      // 기존 파일 정보 가져오기
      JournalFile existingJournalFile = journal.getJournalFile();

      if (existingJournalFile != null) {
        // 기존 파일 삭제
        fileService.deleteFile(existingJournalFile.getFile().getId(), member);
        log.info("기존 파일 삭제 완료 - fileId: {}", existingJournalFile.getFile().getId());

        // 새 파일 업로드
        File savedFile = fileService.uploadFile(requestDTO.getFile(), "journals", member);
        log.info("새 파일 업로드 완료 - fileName: {}", savedFile.getOriginalName());

        // 기존 JournalFile 엔티티 업데이트
        existingJournalFile.updateFile(savedFile);
      }
    }

    journal.update(requestDTO.getTitle(), requestDTO.getContent());
    log.info("교육일지 수정 완료 - journalId: {}", journalId);

    return JournalResponseDTO.from(journal, s3Uploader);
  }

  @Transactional
  public void deleteJournal(Long memberId, Long journalId) {
    log.debug("교육일지 삭제 시작 - journalId: {}, memberId: {}", journalId, memberId);

    Member member = memberRepository.findById(memberId)
        .orElseThrow(() -> new IllegalArgumentException("회원이 존재하지 않습니다."));

    Journal journal = journalRepository.findByIdAndMemberId(journalId, memberId)
        .orElseThrow(() -> {
          log.error("교육일지를 찾을 수 없거나 접근 권한 없음 - journalId: {}, memberId: {}", journalId, memberId);
          return new IllegalArgumentException("존재하지 않거나 접근 권한이 없는 교육일지입니다.");
        });

    // 첨부파일이 있는 경우에만 삭제
    if (journal.getJournalFile() != null) {
      fileService.deleteFile(journal.getJournalFile().getFile().getId(), member);
      log.info("교육일지 첨부파일 삭제 완료 - fileId: {}", journal.getJournalFile().getFile().getId());
    }

    journalRepository.delete(journal);
    log.info("교육일지 삭제 완료 - journalId: {}", journalId);
  }

  private void validateFile(MultipartFile file) {
    log.debug("파일 유효성 검사 시작 - fileName: {}", file.getOriginalFilename());

    String originalFilename = file.getOriginalFilename();
    if (originalFilename == null || originalFilename.isEmpty()) {
      log.error("파일명이 비어있음");
      throw new IllegalArgumentException("파일명이 비어있습니다.");
    }

    String extension = originalFilename.substring(originalFilename.lastIndexOf(".") + 1).toLowerCase();
    if (!ALLOWED_EXTENSIONS.contains(extension)) {
      log.error("허용되지 않는 파일 형식 - extension: {}", extension);
      throw new IllegalArgumentException(
          String.format("허용되지 않는 파일 형식입니다. 허용된 확장자: %s", String.join(", ", ALLOWED_EXTENSIONS))
      );
    }

    log.debug("파일 유효성 검사 완료");
  }

  public List<JournalCourseResponseDTO> getActiveCourses() {
    log.debug("활성화된 교육과정 목록 조회 시작");

    return courseRepository.findByDeletedByIsNull().stream()  // 삭제되지 않은 과정만 조회
        .filter(course -> course.getStatus() == CourseStatus.Y)  // 활성화된 과정만 필터
        .sorted(Comparator.comparing(Course::getStartDate).reversed())  // 최신순 정렬
        .map(JournalCourseResponseDTO::of)
        .collect(Collectors.toList());
  }

  public ResponseEntity<Resource> downloadJournalFile(Long journalId, Long memberId) {
    log.debug("교육일지 파일 다운로드 시작 - journalId: {}", journalId);

    Member member = memberRepository.findById(memberId)
        .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));

    Journal journal = journalRepository.findById(journalId)
        .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 교육일지입니다."));

    if (journal.getJournalFile() == null) {
      throw new IllegalArgumentException("첨부된 파일이 없습니다.");
    }

    FileDownloadDTO downloadDTO = fileService.downloadFile(journal.getJournalFile().getFile().getId(), member);
    return fileService.getDownloadResponseEntity(downloadDTO);
  }
}