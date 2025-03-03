package com.syi.project.journal.service;

import com.syi.project.auth.entity.Member;
import com.syi.project.auth.repository.MemberRepository;
import com.syi.project.common.entity.Criteria;
import com.syi.project.common.enums.Role;
import com.syi.project.common.exception.ErrorCode;
import com.syi.project.common.exception.InvalidRequestException;
import com.syi.project.common.exception.handler.JournalErrorHandler;
import com.syi.project.common.utils.S3Uploader;
import com.syi.project.course.entity.Course;
import com.syi.project.course.repository.CourseRepository;
import com.syi.project.course.service.CourseService;
import com.syi.project.enroll.repository.EnrollRepository;
import com.syi.project.file.dto.FileDownloadDTO;
import com.syi.project.file.service.FileService;
import com.syi.project.journal.dto.JournalCourseResponseDTO;
import com.syi.project.journal.dto.JournalRequestDTO;
import com.syi.project.journal.dto.JournalResponseDTO;
import com.syi.project.journal.entity.Journal;
import com.syi.project.journal.entity.JournalFile;
import com.syi.project.journal.repository.JournalRepository;
import com.syi.project.file.entity.File;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.MediaType;


@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class JournalService {

  private final JournalRepository journalRepository;
  private final MemberRepository memberRepository;
  private final CourseRepository courseRepository;
  private final FileService fileService;
  private final CourseService courseService;
  private final S3Uploader s3Uploader;
  private final EnrollRepository enrollRepository;
  private final JournalErrorHandler journalErrorHandler; // 생성자 주입 추가

  private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList("hwp", "hwpx", "docx", "doc");

  // 신규: 공통 검증 메서드들
  private Member validateAndGetMember(Long memberId) {
    return memberRepository.findById(memberId)
        .orElseThrow(() -> new InvalidRequestException(ErrorCode.USER_NOT_FOUND));
  }

  private Journal validateAndGetJournal(Long journalId) {
    return journalRepository.findById(journalId)
        .orElseThrow(() -> {
          log.error("교육일지를 찾을 수 없음 - journalId: {}", journalId);
          return new IllegalArgumentException("존재하지 않는 교육일지입니다.");
        });
  }

  private Journal validateAndGetJournalWithMember(Long journalId, Long memberId) {
    return journalRepository.findByIdAndMemberId(journalId, memberId)
        .orElseThrow(() -> {
          log.error("교육일지를 찾을 수 없거나 접근 권한 없음 - journalId: {}, memberId: {}", journalId, memberId);
          return new IllegalArgumentException("존재하지 않거나 접근 권한이 없는 교육일지입니다.");
        });
  }

  private void validateAccess(Journal journal, Member member) {
    if (!(member.getRole() == Role.ADMIN || journal.getMember().getId().equals(member.getId()))) {
      throw new InvalidRequestException(ErrorCode.JOURNAL_ACCESS_DENIED);
    }
  }

  // 신규: 관리자 권한 체크 메서드 추가
  private void validateMemberRole(Member member) {
    if (member.getRole() == Role.ADMIN) {
      log.error("관리자의 교육일지 등록 시도 - memberId: {}", member.getId());
      throw new IllegalArgumentException("관리자는 교육일지를 등록할 수 없습니다.");
    }
  }

  // 신규: 파일 관련 공통 메서드들
  private void validateAndProcessFile(MultipartFile file, String action) {
    // 파일 필수 체크는 DTO에서 처리되므로 제거
    String originalFilename = file.getOriginalFilename();

    // 파일 형식 검사만 여기서 처리
    String extension = originalFilename.substring(originalFilename.lastIndexOf(".") + 1).toLowerCase();
    if (!ALLOWED_EXTENSIONS.contains(extension)) {
      throw new InvalidRequestException(ErrorCode.JOURNAL_INVALID_FILE_TYPE);
    }
  }

  private void updateJournalFile(Journal journal, MultipartFile newFile, Member member) {
    JournalFile existingJournalFile = journal.getJournalFile();

    if (existingJournalFile != null) {
      fileService.deleteFile(existingJournalFile.getFile().getId(), member);
      log.info("기존 파일 삭제 완료 - fileId: {}", existingJournalFile.getFile().getId());
    }

    File savedFile = fileService.uploadFile(newFile, "journals", member, journal.getEducationDate());
    log.info("새 파일 업로드 완료 - fileName: {}", savedFile.getOriginalName());

    if (existingJournalFile != null) {
      existingJournalFile.updateFile(savedFile);
    } else {
      journal.setFile(JournalFile.builder()
          .journal(journal)
          .file(savedFile)
          .build());
    }
  }

  // 기존 메서드 유지
  public Page<JournalResponseDTO> getStudentJournals(
      Long memberId,
      Long courseId,
      LocalDate startDate,
      LocalDate endDate,
      Criteria criteria
  ) {
    log.debug("수강생 교육일지 검색 시작 - memberId: {}, courseId: {}, criteria: {}", memberId, courseId, criteria);

    validateSearchCriteria(startDate, endDate, criteria.getPageNum());
    validateEnrollment(memberId, courseId);

    Page<Journal> journals = journalRepository.searchJournalsForStudent(
        memberId,
        courseId,
        startDate,
        endDate,
        PageRequest.of(criteria.getPageNum() - 1, criteria.getAmount())
    );

    log.info("수강생 교육일지 검색 완료 - 총 {}건 검색됨", journals.getTotalElements());

    return journals.map(journal -> JournalResponseDTO.from(journal, s3Uploader));
  }

  // 기존 메서드 유지
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

    validateSearchCriteria(startDate, endDate, criteria.getPageNum());

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

  // 수정: 검증 로직 개선
  @Transactional
  public JournalResponseDTO createJournal(Long memberId, JournalRequestDTO.Create requestDTO) {
    log.debug("교육일지 등록 처리 시작 - memberId: {}, courseId: {}", memberId, requestDTO.getCourseId());

    Member member = validateAndGetMember(memberId);
    validateMemberRole(member);
    validateEnrollment(memberId, requestDTO.getCourseId());

    String fileName = requestDTO.getFile().getOriginalFilename();
    if (fileName != null) {
      String extension = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
      if (!ALLOWED_EXTENSIONS.contains(extension)) {
        throw new InvalidRequestException(ErrorCode.JOURNAL_INVALID_FILE_TYPE);
      }
    }

    Course course = courseRepository.findById(requestDTO.getCourseId())
        .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 강좌입니다."));

    validateEducationDate(requestDTO.getEducationDate(), course, member);

    Journal journal = Journal.builder()
        .member(member)
        .course(course)
        .title(requestDTO.getTitle())
        .content(requestDTO.getContent())
        .educationDate(requestDTO.getEducationDate())
        .build();

    updateJournalFile(journal, requestDTO.getFile(), member);

    Journal savedJournal = journalRepository.save(journal);
    log.info("교육일지 등록 완료 - journalId: {}", savedJournal.getId());

    return JournalResponseDTO.from(savedJournal, s3Uploader);
  }

  // 수정: 검증 로직 개선
  public JournalResponseDTO getJournal(Long journalId, Long memberId) {
    log.debug("교육일지 상세 조회 시작 - journalId: {}, memberId: {}", journalId, memberId);

    Member member = validateAndGetMember(memberId);
    Journal journal = validateAndGetJournal(journalId);
    validateAccess(journal, member);

    log.info("교육일지 상세 조회 완료 - journalId: {}, 제목: {}", journalId, journal.getTitle());
    return JournalResponseDTO.from(journal, s3Uploader);
  }

  // 수정: 검증 및 파일 처리 로직 개선
  @Transactional
  public JournalResponseDTO updateJournal(Long memberId, Long journalId, JournalRequestDTO.Update requestDTO) {
    log.debug("교육일지 수정 시작 - journalId: {}, memberId: {}", journalId, memberId);

    Member member = validateAndGetMember(memberId);
    Journal journal = validateAndGetJournalWithMember(journalId, memberId);

    if (!journal.getEducationDate().equals(requestDTO.getEducationDate())) {
      validateEducationDate(requestDTO.getEducationDate(), journal.getCourse(), member);
    }

    // 새 파일이 있는 경우에만 파일 처리
    if (requestDTO.getFile() != null && !requestDTO.getFile().isEmpty()) {
      validateAndProcessFile(requestDTO.getFile(), "수정");
      updateJournalFile(journal, requestDTO.getFile(), member);
    }

    journal.update(
        requestDTO.getTitle(),
        requestDTO.getContent(),
        requestDTO.getEducationDate()  // 교육일자 추가
    );

    log.info("교육일지 수정 완료 - journalId: {}", journalId);
    return JournalResponseDTO.from(journal, s3Uploader);
  }

  // 수정: 검증 로직 개선
  @Transactional
  public void deleteJournal(Long memberId, Long journalId) {
    log.debug("교육일지 삭제 시작 - journalId: {}, memberId: {}", journalId, memberId);

    Member member = validateAndGetMember(memberId);
    Journal journal = validateAndGetJournalWithMember(journalId, memberId);

    if (journal.getJournalFile() != null) {
      fileService.deleteFile(journal.getJournalFile().getFile().getId(), member);
      log.info("교육일지 첨부파일 삭제 완료 - fileId: {}", journal.getJournalFile().getFile().getId());
    }

    journalRepository.delete(journal);
    log.info("교육일지 삭제 완료 - journalId: {}", journalId);
  }

  // 기존 메서드 유지
  private void validateSearchCriteria(LocalDate startDate, LocalDate endDate, int pageNum) {
    log.debug("날짜 및 페이징 조건 유효성 검증 시작 - startDate: {}, endDate: {}, pageNum: {}",
        startDate, endDate, pageNum);

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

  // 수강생 교육과정
  private void validateEnrollment(Long memberId, Long courseId) {
    log.debug("수강생-교육과정 매칭 검증 시작 - memberId: {}, courseId: {}", memberId, courseId);

    boolean isEnrolled = enrollRepository.existsByMemberIdAndCourseId(memberId, courseId);
    if (!isEnrolled) {
      log.error("수강생-교육과정 매칭 실패 - memberId: {}, courseId: {}", memberId, courseId);
      throw new IllegalArgumentException("해당 과정의 수강생이 아닙니다.");
    }

    log.debug("수강생-교육과정 매칭 검증 완료");
  }

  // 관리자 교육과정 조회
  public List<JournalCourseResponseDTO> getActiveCourses(Long adminId) {
    log.debug("관리자의 활성화된 교육과정 목록 조회 시작 - adminId: {}", adminId);

    return courseService.getAllCoursesByAdminId(adminId).stream()
        .map(courseListDTO -> JournalCourseResponseDTO.builder()
            .id(courseListDTO.getCourseId())
            .name(courseListDTO.getCourseName())
            .build())
        .collect(Collectors.toList());
  }

  // 파일 다운로드
  public ResponseEntity<Resource> downloadJournalFile(Long journalId, Long memberId) {
    log.debug("교육일지 파일 다운로드 시작 - journalId: {}", journalId);

    Member member = validateAndGetMember(memberId);

    // 교육일지 존재 여부, 접근 권한, 파일 존재 여부를 핸들러로 이동
    Journal journal = journalErrorHandler.validateJournalExists(journalId);
    journalErrorHandler.validateAccess(journal, member);
    journalErrorHandler.validateJournalFile(journal);

    File file = journal.getJournalFile().getFile();
    FileDownloadDTO downloadDTO = fileService.downloadFile(file.getId(), member);

    return fileService.getDownloadResponseEntity(downloadDTO);
  }

  // zip 다운로드
  public ResponseEntity<Resource> downloadJournalsAsZip(List<Long> journalIds, Long memberId) {
    Member member = validateAndGetMember(memberId);
    if (member.getRole() != Role.ADMIN) {
      throw new InvalidRequestException(ErrorCode.ACCESS_DENIED);
    }

    List<Journal> journals = journalRepository.findAllByIdsWithFiles(journalIds);
    List<File> files = journals.stream()
        .map(j -> j.getJournalFile().getFile())
        .collect(Collectors.toList());

    Resource zipResource = fileService.downloadFilesAsZip(files, "교육일지_일괄다운로드.zip");

    return ResponseEntity.ok()
        .contentType(MediaType.APPLICATION_OCTET_STREAM)
        .header(HttpHeaders.CONTENT_DISPOSITION,
            "attachment; filename*=UTF-8''교육일지_일괄다운로드.zip")
        .body(zipResource);
  }

  private void validateEducationDate(LocalDate educationDate, Course course, Member member) {
    // 미래 날짜 검증
    if (educationDate.isAfter(LocalDate.now())) {
      throw new InvalidRequestException(ErrorCode.JOURNAL_INVALID_DATE, "교육일자는 미래 날짜일 수 없습니다.");
    }

    // 과정 기간 내 날짜인지 검증
    if (educationDate.isBefore(course.getStartDate()) || educationDate.isAfter(course.getEndDate())) {
      throw new InvalidRequestException(
          ErrorCode.JOURNAL_DATE_OUT_OF_RANGE,
          String.format("교육일자는 과정 기간(%s ~ %s) 내여야 합니다.",
              course.getStartDate(), course.getEndDate())
      );
    }

    // 동일 과정 내 동일 날짜 중복 작성 검증
    boolean exists = journalRepository.existsByMemberIdAndCourseIdAndEducationDate(
        member.getId(), course.getId(), educationDate);
    if (exists) {
      throw new InvalidRequestException(ErrorCode.JOURNAL_DUPLICATE_DATE);
    }
  }
}