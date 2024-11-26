package com.syi.project.journal;

import com.syi.project.auth.entity.Member;
import com.syi.project.auth.repository.MemberRepository;
import com.syi.project.common.entity.Criteria;
import com.syi.project.common.enums.Role;
import com.syi.project.common.utils.S3Uploader;
import com.syi.project.course.entity.Course;
import com.syi.project.course.repository.CourseRepository;
import com.syi.project.file.service.FileService;
import com.syi.project.journal.dto.JournalRequestDTO;
import com.syi.project.journal.dto.JournalResponseDTO;
import com.syi.project.journal.entity.Journal;
import com.syi.project.journal.entity.JournalFile;
import com.syi.project.journal.repository.JournalFileRepository;
import com.syi.project.journal.repository.JournalRepository;
import com.syi.project.file.entity.File;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
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
  private final FileService fileService;
  private final JournalFileRepository journalFileRepository;
  private final S3Uploader s3Uploader;

  private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList("hwp", "docx", "doc");

  // 페이징 검색 메서드 추가
  public Page<JournalResponseDTO> searchJournals(Criteria criteria, Long memberId, LocalDate startDate, LocalDate endDate) {
    log.debug("교육일지 검색 시작 - memberId: {}, criteria: {}", memberId, criteria);

    Member member = memberRepository.findById(memberId)
        .orElseThrow(() -> {
          log.error("회원 정보를 찾을 수 없음 - memberId: {}", memberId);
          return new IllegalArgumentException("존재하지 않는 회원입니다.");
        });

    // 날짜 유효성 검증
    validateDateRange(startDate, endDate);

    // 페이지 사이즈 설정 (기본 20개)
    if (criteria.getAmount() <= 0) {
      criteria.setAmount(20);
    }

    // 페이지 번호 검증
    if (criteria.getPageNum() <= 0) {
      criteria.setPageNum(1);
    }

    Page<Journal> journalPage = journalRepository.findAllWithConditions(
        criteria,
        member.getRole() == Role.ADMIN ? null : memberId,  // 관리자가 아닌 경우 본인 글만 조회
        startDate,
        endDate
    );

    log.info("교육일지 검색 완료 - 총 {}건 검색됨", journalPage.getTotalElements());

    return new PageImpl<>(
        journalPage.getContent().stream()
            .map(journal -> JournalResponseDTO.from(journal, s3Uploader))
            .collect(Collectors.toList()),
        criteria.getPageable(),
        journalPage.getTotalElements()
    );
  }

  // 날짜 범위 검증 메서드
  private void validateDateRange(LocalDate startDate, LocalDate endDate) {
    if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
      log.error("잘못된 날짜 범위 - startDate: {}, endDate: {}", startDate, endDate);
      throw new IllegalArgumentException("시작일이 종료일보다 늦을 수 없습니다.");
    }
  }

  @Transactional
  public JournalResponseDTO createJournal(Long memberId, JournalRequestDTO requestDTO) {
    log.debug("교육일지 등록 처리 시작 - memberId: {}, courseId: {}", memberId, requestDTO.getCourseId());

    Member member = memberRepository.findById(memberId)
        .orElseThrow(() -> {
          log.error("회원 정보를 찾을 수 없음 - memberId: {}", memberId);
          return new IllegalArgumentException("존재하지 않는 회원입니다.");
        });

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

    Member member = memberRepository.findById(memberId)
        .orElseThrow(() -> new IllegalArgumentException("회원이 존재하지 않습니다."));

    Journal journal = journalRepository.findByIdAndMemberId(journalId, memberId)
        .orElseThrow(() -> {
          log.error("교육일지를 찾을 수 없거나 접근 권한 없음 - journalId: {}, memberId: {}", journalId, memberId);
          return new IllegalArgumentException("존재하지 않거나 접근 권한이 없는 교육일지입니다.");
        });

    // 파일 수정 로직
    if (requestDTO.getFile() != null && !requestDTO.getFile().isEmpty()) {
      validateFile(requestDTO.getFile());

      // 기존 파일이 있다면 삭제
      if (journal.getJournalFile() != null) {
        fileService.deleteFile(journal.getJournalFile().getFile().getId(), member);
        log.info("기존 파일 삭제 완료 - fileId: {}", journal.getJournalFile().getFile().getId());
      }

      // 새 파일 업로드
      File savedFile = fileService.uploadFile(requestDTO.getFile(), "journals", member);
      log.info("새 파일 업로드 완료 - fileName: {}", savedFile.getOriginalName());

      JournalFile journalFile = JournalFile.builder()
          .journal(journal)
          .file(savedFile)
          .build();

      journal.setFile(journalFile);
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
}