package com.syi.project.file.dto;

import lombok.Builder;
import lombok.Getter;
import org.springframework.core.io.Resource;

@Getter
@Builder
public class FileDownloadDTO {
  private String originalName;
  private String contentType;
  private Resource resource;
}