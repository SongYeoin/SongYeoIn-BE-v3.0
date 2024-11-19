package com.syi.project.file.dto;

import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter  // ModelAttribute 바인딩을 위해 Setter 필요
@NoArgsConstructor  // ModelAttribute 바인딩을 위해 필요
public class FileUpdateDTO {
  private List<Long> deleteFileIds;  // 삭제할 파일 ID 목록
  private List<MultipartFile> newFiles;  // 새로 추가할 파일 목록
}
