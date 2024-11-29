package com.syi.project.file.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

// 파일의 상태를 정의
@Getter
@RequiredArgsConstructor
public enum FileStatus {
  ACTIVE("활성"),
  DELETED("삭제됨"),
  TEMPORARY("임시");

  private final String description;
}
