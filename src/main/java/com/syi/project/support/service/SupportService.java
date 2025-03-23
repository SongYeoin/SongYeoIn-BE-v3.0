package com.syi.project.support.service;

import com.syi.project.auth.entity.Member;
import com.syi.project.auth.repository.MemberRepository;
import com.syi.project.common.exception.ErrorCode;
import com.syi.project.common.exception.InvalidRequestException;
import com.syi.project.common.utils.S3Uploader;
import com.syi.project.file.dto.FileDownloadDTO;
import com.syi.project.file.entity.File;
import com.syi.project.file.service.FileService;
import com.syi.project.support.dto.SupportRequestDTO;
import com.syi.project.support.dto.SupportResponseDTO;
import com.syi.project.support.entity.DeveloperResponse;
import com.syi.project.support.entity.Support;
import com.syi.project.support.entity.SupportFile;
import com.syi.project.support.repository.DeveloperResponseRepository;
import com.syi.project.support.repository.SupportRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class SupportService {

  private final SupportRepository supportRepository;
  private final MemberRepository memberRepository;
  private final DeveloperResponseRepository developerResponseRepository;
  private final FileService fileService;
  private final S3Uploader s3Uploader;
  private final SupportDiscordService supportDiscordService;

  // 허용 파일 확장자 및 MIME 타입 - 고정 이미지 파일만 허용
  private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList(
      "jpeg", "jpg", "png", "bmp", "tiff", "tif", "webp", "svg"
  );

  private static final List<String> ALLOWED_MIME_TYPES = Arrays.asList(
      "image/jpeg",
      "image/png",
      "image/bmp",
      "image/tiff",
      "image/svg+xml",
      "image/webp"
  );

  // 문의 생성
  @Transactional
  public SupportResponseDTO createSupport(Long memberId, SupportRequestDTO requestDTO) {
    Member member = getMember(memberId);
    Support support = requestDTO.toEntity(member);
    supportRepository.save(support);

    log.info("문의 저장 - id: {}", support.getId());

    // 파일 업로드 처리
    if (requestDTO.getFiles() != null && !requestDTO.getFiles().isEmpty()) {
      requestDTO.getFiles().forEach(file -> {
        validateFile(file);
        try {
          File uploadedFile = fileService.uploadFile(file, "supports", member, support.getRegDate().toLocalDate());
          SupportFile supportFile = SupportFile.builder()
              .support(support)
              .file(uploadedFile)
              .build();
          support.addFile(supportFile);
          log.info("문의 파일 업로드 완료 - fileId: {}", uploadedFile.getId());
        } catch (Exception e) {
          log.error("파일 업로드 실패 - filename: {}", file.getOriginalFilename(), e);
          throw new InvalidRequestException(ErrorCode.FILE_UPLOAD_FAILED);
        }
      });
    }

    return SupportResponseDTO.fromEntity(support, s3Uploader);
  }

  // 문의 목록 조회 (학생용 - 자신의 문의만)
  public Page<SupportResponseDTO> getMySupports(Long memberId, Pageable pageable, String keyword) {
    log.info("내 문의 검색 - memberId: {}, keyword: '{}'", memberId, keyword);
    Page<Support> supports = supportRepository.searchSupports(memberId, keyword, pageable);
    log.info("검색 결과 - 총 건수: {}", supports.getTotalElements());

    // 각 문의에 대한 개발팀 응답 조회 및 DTO 변환
    return supports.map(support -> {
      DeveloperResponse developerResponse = developerResponseRepository.findBySupportId(support.getId())
          .orElse(null);
      return SupportResponseDTO.fromEntity(support, developerResponse, s3Uploader);
    });
  }

  // 문의 목록 조회 (관리자용 - 전체)
  public Page<SupportResponseDTO> getAllSupports(Pageable pageable, String keyword) {
    Page<Support> supports = supportRepository.searchSupports(null, keyword, pageable);

    // 각 문의에 대한 개발팀 응답 조회 및 DTO 변환
    return supports.map(support -> {
      DeveloperResponse developerResponse = developerResponseRepository.findBySupportId(support.getId())
          .orElse(null);
      return SupportResponseDTO.fromEntity(support, developerResponse, s3Uploader);
    });
  }

  // 페이지네이션 정렬 설정 (작성일, ID 기준 내림차순)
  private Pageable getPageableWithSort(Pageable pageable) {
    return PageRequest.of(
        pageable.getPageNumber(),
        pageable.getPageSize(),
        Sort.by(Sort.Order.desc("regDate"), Sort.Order.desc("id"))
    );
  }

  // 문의 상세 조회
  public SupportResponseDTO getSupportDetail(Long id, Long memberId) {
    Support support = supportRepository.findByIdAndDeletedByIsNull(id)
        .orElseThrow(() -> {
          log.error("문의를 찾을 수 없음 - id: {}", id);
          return new InvalidRequestException(ErrorCode.SUPPORT_NOT_FOUND);
        });

    Member member = getMember(memberId);

    // 관리자가 아니고 작성자도 아닌 경우 접근 불가
    if (!member.getRole().name().equals("ADMIN") && !support.getMember().getId().equals(memberId)) {
      log.error("문의 조회 권한 없음 - id: {}", id);
      throw new InvalidRequestException(ErrorCode.SUPPORT_ACCESS_DENIED);
    }

    // 개발팀 응답 조회
    DeveloperResponse developerResponse = developerResponseRepository.findBySupportId(id).orElse(null);

    return SupportResponseDTO.fromEntity(support, developerResponse, s3Uploader);
  }

  // 문의 확인 처리 (관리자용)
  @Transactional
  public SupportResponseDTO confirmSupport(Long id, Long memberId) {
    Support support = supportRepository.findByIdAndDeletedByIsNull(id)
        .orElseThrow(() -> {
          log.error("문의를 찾을 수 없음 - id: {}", id);
          return new InvalidRequestException(ErrorCode.SUPPORT_NOT_FOUND);
        });

    support.confirm();
    log.info("문의 확인 처리 완료 - id: {}", id);

    return SupportResponseDTO.fromEntity(support, s3Uploader);
  }

  // 문의 확인 처리 취소 (관리자용)
  @Transactional
  public SupportResponseDTO unconfirmSupport(Long id, Long memberId) {
    Support support = supportRepository.findByIdAndDeletedByIsNull(id)
        .orElseThrow(() -> {
          log.error("문의를 찾을 수 없음 - id: {}", id);
          return new InvalidRequestException(ErrorCode.SUPPORT_NOT_FOUND);
        });

    support.unconfirm();
    log.info("문의 확인 취소 완료 - id: {}", id);

    return SupportResponseDTO.fromEntity(support, s3Uploader);
  }

  // 문의 삭제
  @Transactional
  public void deleteSupport(Long id, Long memberId) {
    Support support = supportRepository.findByIdAndMemberIdAndDeletedByIsNull(id, memberId)
        .orElseThrow(() -> {
          log.error("문의 삭제 권한 없음 - id: {}", id);
          return new InvalidRequestException(ErrorCode.SUPPORT_DELETE_DENIED);
        });

    // 관련 파일 삭제
    Member member = getMember(memberId);
    support.getFiles().forEach(supportFile -> {
      try {
        fileService.deleteFile(supportFile.getFile().getId(), member);
        log.info("문의 파일 삭제 완료 - fileId: {}", supportFile.getFile().getId());
      } catch (Exception e) {
        log.error("문의 파일 삭제 실패 - fileId: {}", supportFile.getFile().getId(), e);
      }
    });

    support.markAsDeleted(memberId);
    log.info("문의 삭제 완료 - id: {}", id);
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

  // 사용자 정보 호출
  private Member getMember(Long memberId) {
    return memberRepository.findByIdAndDeletedByIsNull(memberId)
        .orElseThrow(() -> {
          log.error("사용자를 찾을 수 없음 - memberId: {}", memberId);
          return new InvalidRequestException(ErrorCode.USER_NOT_FOUND);
        });
  }

  // "개발팀에게 전달" 기능을 추가
  @Transactional
  public void sendToDevTeam(Long id, Long memberId, String additionalComment) {
    Support support = supportRepository.findByIdAndDeletedByIsNull(id)
        .orElseThrow(() -> {
          log.error("문의를 찾을 수 없음 - id: {}", id);
          return new InvalidRequestException(ErrorCode.SUPPORT_NOT_FOUND);
        });

    // 관리자 권한 확인
    Member member = getMember(memberId);
    if (!member.getRole().name().equals("ADMIN")) {
      log.error("개발팀에게 전달 권한 없음 - memberId: {}", memberId);
      throw new InvalidRequestException(ErrorCode.ACCESS_DENIED);
    }

    // 디스코드로 문의 내용 전송 (추가 메시지 포함)
    supportDiscordService.sendSupportToDiscord(support, additionalComment);
    log.info("문의가 개발팀에게 전달됨 - id: {}", id);

    // 문의 확인 처리
    if (!support.isConfirmed()) {
      support.confirm();
      log.info("문의 확인 처리 완료 - id: {}", id);
    }
  }

  /**
   * 문의 첨부파일 다운로드
   * @param supportId 문의 ID
   * @param fileId 파일 ID
   * @param memberId 사용자 ID
   * @return 파일 다운로드 응답
   */
  public ResponseEntity<Resource> downloadSupportFile(Long supportId, Long fileId, Long memberId) {
    // 1. 문의 존재 및 접근 권한 확인
    Support support = supportRepository.findByIdAndDeletedByIsNull(supportId)
        .orElseThrow(() -> {
          log.error("문의를 찾을 수 없음 - supportId: {}", supportId);
          return new InvalidRequestException(ErrorCode.SUPPORT_NOT_FOUND);
        });

    Member member = getMember(memberId);

    // 2. 관리자가 아니고 작성자도 아닌 경우 접근 불가
    if (!member.getRole().name().equals("ADMIN") && !support.getMember().getId().equals(memberId)) {
      log.error("문의 파일 다운로드 권한 없음 - supportId: {}, fileId: {}", supportId, fileId);
      throw new InvalidRequestException(ErrorCode.SUPPORT_ACCESS_DENIED);
    }

    // 3. 파일이 해당 문의에 속하는지 확인
    SupportFile supportFile = support.getFiles().stream()
        .filter(sf -> sf.getFile().getId().equals(fileId))
        .findFirst()
        .orElseThrow(() -> {
          log.error("해당 문의에 속한 파일이 아님 - supportId: {}, fileId: {}", supportId, fileId);
          return new InvalidRequestException(ErrorCode.FILE_NOT_FOUND);
        });

    // 4. FileService를 통해 파일 다운로드 수행
    FileDownloadDTO downloadDTO = fileService.downloadFile(fileId, member);

    // 5. 다운로드 응답 반환
    return fileService.getDownloadResponseEntity(downloadDTO);
  }
}