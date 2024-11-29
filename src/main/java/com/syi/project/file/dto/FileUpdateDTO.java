package com.syi.project.file.dto;

import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

// 다중 파일 수정 요청을 처리
// 단일 파일 수정은 FileUpdateDTO를 사용하지 않고 직접 MultipartFile을 받아서 처리함
@Getter
@Setter  // ModelAttribute 바인딩을 위해 Setter 필요
@NoArgsConstructor  // ModelAttribute 바인딩을 위해 필요
public class FileUpdateDTO {
  private List<Long> deleteFileIds;  // 삭제할 파일 ID 목록
  private List<MultipartFile> newFiles;  // 새로 추가할 파일 목록
}
