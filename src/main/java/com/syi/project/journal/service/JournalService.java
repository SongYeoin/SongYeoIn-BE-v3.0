package com.syi.project.journal.service;

import com.syi.project.auth.entity.Member;
import com.syi.project.auth.repository.MemberRepository;
import com.syi.project.common.enums.Role;
import com.syi.project.common.utils.S3Uploader;
import com.syi.project.course.entity.Course;
import com.syi.project.course.repository.CourseRepository;
import com.syi.project.journal.dto.JournalRequestDTO;
import com.syi.project.journal.dto.JournalResponseDTO;
import com.syi.project.journal.entity.Journal;
import com.syi.project.journal.repository.JournalRepository;
import java.io.IOException;
import java.text.DecimalFormat;
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
  private final S3Uploader s3Uploader;

  private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList("hwp", "docx", "doc");

  // 교육 일지 등록 (로그인한 사용자 본인만 가능)
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

    String fileUrl = null;
    String fileName = null;
    String fileSize = null;

    // 파일 업로드
    if (requestDTO.getFile() != null && !requestDTO.getFile().isEmpty()) {
      log.debug("파일 업로드 시작 - fileName: {}", requestDTO.getFile().getOriginalFilename());
      validateFile(requestDTO.getFile());
      try {
        fileUrl = s3Uploader.uploadFile(requestDTO.getFile(), "journals");
        fileName = requestDTO.getFile().getOriginalFilename();
        fileSize = formatFileSize(requestDTO.getFile().getSize());
        log.debug("파일 업로드 완료 - fileUrl: {}", fileUrl);
      } catch (IOException e) {
        log.error("파일 업로드 실패 - fileName: {}", requestDTO.getFile().getOriginalFilename(), e);
        throw new RuntimeException("파일 업로드 중 오류가 발생했습니다.", e);
      }
    }

    Journal journal = Journal.builder()
        .member(member)
        .course(course)
        .title(requestDTO.getTitle())
        .content(requestDTO.getContent())
        .fileUrl(fileUrl)
        .fileName(fileName)
        .fileSize(fileSize)
        .build();

    Journal savedJournal = journalRepository.save(journal);
    log.info("교육일지 등록 완료 - journalId: {}", savedJournal.getId());

    return new JournalResponseDTO(savedJournal);
  }

  // 목록 조회: 권한에 따라 다르게 처리
  public List<JournalResponseDTO> getJournalsByCourse(Long courseId, Long memberId) {
    log.debug("교육일지 목록 조회 시작 - courseId: {}, memberId: {}", courseId, memberId);

    Member member = memberRepository.findById(memberId)
        .orElseThrow(() -> {
          log.error("회원 정보를 찾을 수 없음 - memberId: {}", memberId);
          return new IllegalArgumentException("존재하지 않는 회원입니다.");
        });

    List<JournalResponseDTO> result;
    // 관리자는 해당 강좌의 모든 일지 조회 가능
    if (member.getRole() == Role.ADMIN) {
      log.debug("관리자 권한으로 전체 교육일지 조회");
      result = journalRepository.findByCourseId(courseId).stream()
          .map(JournalResponseDTO::new)
          .collect(Collectors.toList());
    } else {
      // 수강생은 자신의 일지만 조회 가능
      log.debug("수강생 권한으로 본인 교육일지만 조회");
      result = journalRepository.findByCourseIdAndMemberId(courseId, memberId).stream()
          .map(JournalResponseDTO::new)
          .collect(Collectors.toList());
    }

    log.info("교육일지 목록 조회 완료 - 조회된 일지 수: {}", result.size());
    return result;
  }

  // 상세 조회: 권한에 따라 다르게 처리
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

    // 관리자이거나 본인의 일지인 경우에만 조회 가능
    if (member.getRole() == Role.ADMIN ||
        journal.getMember().getId().equals(memberId)) {
      log.info("교육일지 상세 조회 완료 - journalId: {}", journalId);
      return new JournalResponseDTO(journal);
    }

    log.error("교육일지 접근 권한 없음 - memberId: {}, journalId: {}", memberId, journalId);
    throw new IllegalArgumentException("해당 교육일지에 대한 접근 권한이 없습니다.");
  }

  // 수정: 본인 작성 일지만 수정 가능
  @Transactional
  public JournalResponseDTO updateJournal(Long memberId, Long journalId, JournalRequestDTO requestDTO) {
    log.debug("교육일지 수정 시작 - journalId: {}, memberId: {}", journalId, memberId);

    Journal journal = journalRepository.findByIdAndMemberId(journalId, memberId)
        .orElseThrow(() -> {
          log.error("교육일지를 찾을 수 없거나 접근 권한 없음 - journalId: {}, memberId: {}", journalId, memberId);
          return new IllegalArgumentException("존재하지 않거나 접근 권한이 없는 교육일지입니다.");
        });

    String fileUrl = journal.getFileUrl();
    String fileName = journal.getFileName();
    String fileSize = journal.getFileSize();

    if (requestDTO.getFile() != null && !requestDTO.getFile().isEmpty()) {
      log.debug("새로운 파일 업로드 시작 - fileName: {}", requestDTO.getFile().getOriginalFilename());
      validateFile(requestDTO.getFile());

      // 기존 파일이 있다면 S3에서 삭제
      if (journal.getFileUrl() != null) {
        log.debug("기존 파일 삭제 - fileUrl: {}", journal.getFileUrl());
        s3Uploader.deleteFile(journal.getFileUrl());
      }

      try {
        fileUrl = s3Uploader.uploadFile(requestDTO.getFile(), "journals");
        fileName = requestDTO.getFile().getOriginalFilename();
        fileSize = formatFileSize(requestDTO.getFile().getSize());
        log.debug("새로운 파일 업로드 완료 - fileUrl: {}", fileUrl);
      } catch (IOException e) {
        log.error("파일 업로드 실패 - fileName: {}", requestDTO.getFile().getOriginalFilename(), e);
        throw new RuntimeException("파일 업로드 중 오류가 발생했습니다.", e);
      }
    }

    journal.update(requestDTO.getTitle(), requestDTO.getContent(),
        fileUrl, fileName, fileSize);

    log.info("교육일지 수정 완료 - journalId: {}", journalId);
    return new JournalResponseDTO(journal);
  }

  // 삭제: 본인 작성 일지만 삭제 가능
  @Transactional
  public void deleteJournal(Long memberId, Long journalId) {
    log.debug("교육일지 삭제 시작 - journalId: {}, memberId: {}", journalId, memberId);

    Journal journal = journalRepository.findByIdAndMemberId(journalId, memberId)
        .orElseThrow(() -> {
          log.error("교육일지를 찾을 수 없거나 접근 권한 없음 - journalId: {}, memberId: {}", journalId, memberId);
          return new IllegalArgumentException("존재하지 않거나 접근 권한이 없는 교육일지입니다.");
        });

    if (journal.getFileUrl() != null) {
      log.debug("S3 파일 삭제 시작 - fileUrl: {}", journal.getFileUrl());
      s3Uploader.deleteFile(journal.getFileUrl());
    }

    journalRepository.delete(journal);
    log.info("교육일지 삭제 완료 - journalId: {}", journalId);
  }

  // 파일 검증 (허용된 파일 형식인지 확인함)
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

  // 파일 크기 읽기 쉬운 형식으로 보여지게 함
  private String formatFileSize(long size) {
    if (size <= 0) return "0";
    final String[] units = new String[] { "B", "KB", "MB", "GB", "TB" };
    int digitGroups = (int) (Math.log10(size)/Math.log10(1024));
    return new DecimalFormat("#,##0.#").format(size/Math.pow(1024, digitGroups))
        + " " + units[digitGroups];
  }
}