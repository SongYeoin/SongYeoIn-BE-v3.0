package com.syi.project.journal.service;

import com.syi.project.auth.entity.Member;
import com.syi.project.auth.repository.MemberRepository;
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
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class JournalService {

  private final JournalRepository journalRepository;
  private final MemberRepository memberRepository;
  private final CourseRepository courseRepository;
  private final FileService fileService;
  private final JournalFileRepository journalFileRepository;
  private final S3Uploader s3Uploader;

  private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList("hwp", "docx", "doc");

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

    // 1. Journal 생성
    Journal journal = Journal.builder()
        .member(member)
        .course(course)
        .title(requestDTO.getTitle())
        .content(requestDTO.getContent())
        .build();

    // 2. 파일 처리
    if (requestDTO.getFile() != null && !requestDTO.getFile().isEmpty()) {
      validateFile(requestDTO.getFile());
      File savedFile = fileService.uploadFile(requestDTO.getFile(), "journals", member);

      JournalFile journalFile = JournalFile.builder()
          .journal(journal)
          .file(savedFile)
          .build();

      journal.addFile(journalFile);
    }

    Journal savedJournal = journalRepository.save(journal);
    return JournalResponseDTO.from(savedJournal, s3Uploader);  // new 대신 from 사용
  }

  public List<JournalResponseDTO> getJournalsByCourse(Long courseId, Long memberId) {
    log.debug("교육일지 목록 조회 시작 - courseId: {}, memberId: {}", courseId, memberId);

    Member member = memberRepository.findById(memberId)
        .orElseThrow(() -> {
          log.error("회원 정보를 찾을 수 없음 - memberId: {}", memberId);
          return new IllegalArgumentException("존재하지 않는 회원입니다.");
        });

    List<Journal> journals;
    if (member.getRole() == Role.MANAGER) {
      log.debug("관리자 권한으로 전체 교육일지 조회");
      journals = journalRepository.findByCourseId(courseId);
    } else {
      log.debug("수강생 권한으로 본인 교육일지만 조회");
      journals = journalRepository.findByCourseIdAndMemberId(courseId, memberId);
    }

    return journals.stream()
        .map(journal -> JournalResponseDTO.from(journal, s3Uploader))  // new 대신 from 사용
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

    if (member.getRole() == Role.MANAGER ||
        journal.getMember().getId().equals(memberId)) {
      log.info("교육일지 상세 조회 완료 - journalId: {}", journalId);
      return JournalResponseDTO.from(journal, s3Uploader);  // new 대신 from 사용
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
      // 기존 파일 URL을 가져옴 (null일 수 있음)
      String oldFileUrl = journal.getJournalFiles().isEmpty()
          ? null
          : journal.getJournalFiles().get(0).getFile().getUrl(s3Uploader);  // getUrl() 호출

      try {
        // S3Uploader에서 파일 수정 처리
        String newFileUrl = s3Uploader.updateFile(requestDTO.getFile(), "journals", oldFileUrl);

        // 새로운 파일 저장 (JournalFile 엔티티 생성)
        File savedFile = fileService.uploadFile(requestDTO.getFile(), "journals", member);
        JournalFile journalFile = JournalFile.builder()
            .journal(journal)
            .file(savedFile)
            .build();

        journal.addFile(journalFile);
      } catch (IOException e) {
        log.error("파일 수정 중 에러 발생", e);
        throw new RuntimeException("파일 수정 중 오류가 발생했습니다.", e);
      }
    }

    // 제목과 내용 수정
    journal.update(requestDTO.getTitle(), requestDTO.getContent());

    return JournalResponseDTO.from(journal, s3Uploader);  // 새로 저장된 교육일지 반환
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

    // 연관된 파일들 삭제
    journal.getJournalFiles().forEach(journalFile -> {
      fileService.deleteFile(journalFile.getFile().getId(), member);
    });

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