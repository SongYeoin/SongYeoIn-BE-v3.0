package com.syi.project.notice.service;

import com.syi.project.auth.entity.Member;
import com.syi.project.auth.repository.MemberRepository;
import com.syi.project.common.exception.ErrorCode;
import com.syi.project.common.exception.InvalidRequestException;
import com.syi.project.common.utils.S3Uploader;
import com.syi.project.course.entity.Course;
import com.syi.project.course.repository.CourseRepository;
import com.syi.project.file.entity.File;
import com.syi.project.file.service.FileService;
import com.syi.project.notice.dto.NoticeRequestDTO;
import com.syi.project.notice.dto.NoticeResponseDTO;
import com.syi.project.notice.entity.Notice;
import com.syi.project.notice.entity.NoticeFile;
import com.syi.project.notice.repository.NoticeRepository;
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
@Slf4j
@Transactional(readOnly = true)
public class NoticeService {

  private final NoticeRepository noticeRepository;
  private final MemberRepository memberRepository;
  private final CourseRepository courseRepository;
  private final FileService fileService;
  private final S3Uploader s3Uploader;

  // 허용 파일 확장자 및 MIME 타입
  private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList(
      "hwp", "hwpx", "doc", "docx", "xls", "xlsx", "ppt", "pptx", "pdf",
      "jpeg", "jpg", "png", "gif", "bmp", "tiff", "tif", "webp", "svg"
  );

  private static final List<String> ALLOWED_MIME_TYPES = Arrays.asList(
      "application/msword",
      "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
      "application/vnd.ms-excel",
      "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
      "application/vnd.ms-powerpoint",
      "application/vnd.openxmlformats-officedocument.presentationml.presentation",
      "application/pdf",
      "image/jpeg",
      "image/png",
      "image/gif",
      "image/bmp",
      "image/tiff",
      "image/svg+xml",
      "image/webp",
      "application/x-hwp",
      "application/x-hwpml"
  );

  // 공지사항 생성
  @Transactional
  public NoticeResponseDTO createNotice(Long memberId, NoticeRequestDTO requestDTO,
      List<MultipartFile> files) {
    log.info("공지사항 생성 시작 - memberId: {}, courseId: {}", memberId, requestDTO.getCourseId());
    Member member = getMember(memberId);

    Course course = null;
    if (!requestDTO.isGlobal() && requestDTO.getCourseId() != null) {
      course = courseRepository.findById(requestDTO.getCourseId())
          .orElseThrow(() -> {
            log.error("교육과정을 찾을 수 없습니다 - courseId: {}", requestDTO.getCourseId());
            return new InvalidRequestException(ErrorCode.COURSE_NOT_FOUND);
          });
    }

    Notice notice = requestDTO.toEntity(member, course);
    noticeRepository.save(notice);
    log.info("공지사항 저장 완료 - id: {}", notice.getId());

    // 파일 업로드 처리
    if (files != null && !files.isEmpty()) {
      files.forEach(file -> {
        validateFile(file);
        try {
          File uploadedFile = fileService.uploadFile(file, "notices", member);
          NoticeFile noticeFile = NoticeFile.builder()
              .notice(notice)
              .file(uploadedFile)
              .build();
          notice.addFile(noticeFile);
          log.info("공지사항 파일 업로드 완료 - fileId: {}", uploadedFile.getId());
        } catch (Exception e) {
          log.error("파일 업로드 실패 - filename: {}", file.getOriginalFilename(), e);
          throw new InvalidRequestException(ErrorCode.FILE_UPLOAD_FAILED);
        }
      });
    }
    return NoticeResponseDTO.fromEntity(notice, s3Uploader);
  }

  // 공지사항 목록 조회
  public List<NoticeResponseDTO> getNotices(Long courseId, Long memberId) {
    log.info("공지사항 목록 조회 시작 - courseId: {}, memberId: {}", courseId, memberId);

    List<Notice> notices = noticeRepository.findNoticesByCourseIdAndGlobal(courseId);

    List<NoticeResponseDTO> noticeDtos = notices.stream()
        .map(notice -> NoticeResponseDTO.fromEntity(notice, s3Uploader))
        .collect(Collectors.toList());
    log.info("공지사항 목록 조회 완료 - 조회된 공지 수: {}", noticeDtos.size());
    return noticeDtos;
  }

  // 공지사항 상세 조회
  @Transactional
  public NoticeResponseDTO getNoticeDetail(Long id, Long memberId) {
    log.info("공지사항 상세 조회 시작 - id: {}, memberId: {}", id, memberId);

    Notice notice = noticeRepository.findById(id)
        .orElseThrow(() -> {
          log.error("공지사항을 찾을 수 없습니다 - id: {}", id);
          return new InvalidRequestException(ErrorCode.NOTICE_NOT_FOUND);
        });

    notice.incrementViewCount();
    log.info("공지사항 조회수 증가 - id: {}, viewCount: {}", id, notice.getViewCount());

    return NoticeResponseDTO.fromEntity(notice, s3Uploader);
  }

  // 공지사항 수정
  @Transactional
  public NoticeResponseDTO updateNotice(Long id, Long memberId, NoticeRequestDTO requestDTO,
      List<MultipartFile> newFiles, List<Long> deleteFileIds) {
    log.info("공지사항 수정 시작 - id: {}, memberId: {}", id, memberId);

    Member member = getMember(memberId);

    Notice notice = noticeRepository.findByIdAndMemberIdAndDeletedByIsNull(id, memberId)
        .orElseThrow(() -> {
          log.error("공지사항 수정 권한이 없습니다 - id: {}, memberId: {}", id, memberId);
          return new InvalidRequestException(ErrorCode.NOTICE_UPDATE_DENIED);
        });

    notice.update(requestDTO.getTitle(), requestDTO.getContent(), requestDTO.isGlobal());
    log.info("공지사항 기본 정보 수정 완료 - noticeId: {}", id);

    // 기존 파일 삭제
    if (deleteFileIds != null && !deleteFileIds.isEmpty()) {
      notice.getFiles().removeIf(noticeFile -> {
        if (deleteFileIds.contains(noticeFile.getFile().getId())) {
          try {
            fileService.deleteFile(noticeFile.getFile().getId(), member);
            log.info("공지사항 기존 파일 삭제 완료 - fileId: {}", noticeFile.getFile().getId());
          } catch (Exception e) {
            log.error("공지사항 기존 파일 삭제 실패 - fileId: {}", noticeFile.getFile().getId(), e);
          }
          return true;
        }
        return false;
      });
    }

    // 새 파일 추가
    if (newFiles != null && !newFiles.isEmpty()) {
      newFiles.forEach(file -> {
        validateFile(file);
        try {
          File uploadedFile = fileService.uploadFile(file, "notices", notice.getMember());
          NoticeFile noticeFile = NoticeFile.builder()
              .notice(notice)
              .file(uploadedFile)
              .build();
          notice.addFile(noticeFile);
          log.info("공지사항 새 파일 업로드 완료 - fileId: {}", uploadedFile.getId());
        } catch (Exception e) {
          log.error("공지사항 새 파일 업로드 실패 - filename: {}", file.getOriginalFilename(), e);
          throw new InvalidRequestException(ErrorCode.FILE_UPLOAD_FAILED);
        }
      });
    }
    return NoticeResponseDTO.fromEntity(notice, s3Uploader);
  }

  // 공지사항 삭제
  @Transactional
  public void deleteNotice(Long id, Long memberId) {
    log.info("공지사항 삭제 시작 - id: {}, memberId: {}", id, memberId);

    Member member = getMember(memberId);

    Notice notice = noticeRepository.findByIdAndMemberIdAndDeletedByIsNull(id, memberId)
        .orElseThrow(() -> {
          log.error("공지사항 삭제 권한이 없습니다 - id: {}, memberId: {}", id, memberId);
          return new InvalidRequestException(ErrorCode.NOTICE_DELETE_DENIED);
        });

    // 관련 파일 삭제
    notice.getFiles().forEach(noticeFile -> {
      try {
        fileService.deleteFile(noticeFile.getFile().getId(), member);
        log.info("공지사항 파일 삭제 완료 - fileId: {}", noticeFile.getFile().getId());
      } catch (Exception e) {
        log.error("공지사항 파일 삭제 실패 - fileId: {}", noticeFile.getFile().getId(), e);
      }
    });

    notice.markAsDeleted(memberId);
    noticeRepository.delete(notice);
    log.info("공지사항 삭제 완료 - id: {}", id);
  }

  // 사용자 정보 호출
  private Member getMember(Long memberId) {
    return memberRepository.findByIdAndIsDeletedFalse(memberId)
        .orElseThrow(() -> {
          log.error("사용자를 찾을 수 없습니다 - memberId: {}", memberId);
          return new InvalidRequestException(ErrorCode.USER_NOT_FOUND);
        });
  }

  // 파일 검증 로직
  private void validateFile(MultipartFile file) {
    if (file == null) {
      log.error("파일이 null입니다.");
      throw new InvalidRequestException(ErrorCode.INVALID_FILE_FORMAT, "파일이 null입니다.");
    }

    String originalFilename = file.getOriginalFilename();
    String mimeType = file.getContentType();

    if (originalFilename == null || originalFilename.isEmpty()) {
      log.error("파일 이름이 비어있거나 null입니다. MIME 타입: {}", mimeType);
      throw new InvalidRequestException(ErrorCode.INVALID_FILE_FORMAT, "파일 이름이 비어있거나 null입니다.");
    }

    if (mimeType == null || mimeType.isEmpty()) {
      log.error("MIME 타입이 비어있거나 null입니다. 파일 이름: {}", originalFilename);
      throw new InvalidRequestException(ErrorCode.INVALID_FILE_FORMAT, "MIME 타입이 비어있거나 null입니다.");
    }

    // 확장자 검증
    String extension = getFileExtension(originalFilename);
    if (!ALLOWED_EXTENSIONS.contains(extension.toLowerCase())) {
      log.error("허용되지 않은 파일 형식입니다. 파일 이름: {}, 확장자: {}", originalFilename, extension);
      throw new InvalidRequestException(ErrorCode.INVALID_FILE_FORMAT,
          String.format("허용되지 않은 파일 형식입니다: %s", extension));
    }

    // MIME 타입 검증
    if (!ALLOWED_MIME_TYPES.contains(mimeType)) {
      log.error("허용되지 않은 MIME 타입입니다. 파일 이름: {}, MIME 타입: {}", originalFilename, mimeType);
      throw new InvalidRequestException(ErrorCode.INVALID_FILE_FORMAT,
          String.format("허용되지 않은 MIME 타입입니다: %s", mimeType));
    }

    log.info("파일 검증 완료 - filename: {}, mimeType: {}", originalFilename, mimeType);
  }

  private String getFileExtension(String fileName) {
    try {
      return fileName.substring(fileName.lastIndexOf(".") + 1);
    } catch (StringIndexOutOfBoundsException e) {
      log.error("파일 확장자를 추출할 수 없습니다. 파일 이름: {}", fileName, e);
      throw new InvalidRequestException(ErrorCode.INVALID_FILE_FORMAT, "파일 확장자를 추출할 수 없습니다.");
    }
  }

}
