package com.syi.project.file.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum FileStatus {
  ACTIVE("활성"),
  DELETED("삭제됨"),
  TEMPORARY("임시");

  private final String description;
}
