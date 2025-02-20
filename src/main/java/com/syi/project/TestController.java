package com.syi.project;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;


import com.syi.project.common.utils.S3Uploader;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class TestController {

  private final S3Uploader s3Uploader;

  @Operation(summary = "파일 업로드 테스트")  // Swagger 문서 설명 추가
  @PostMapping(value = "/upload", consumes = "multipart/form-data")  // content type 지정
  public String uploadFile(
      @Parameter(description = "업로드할 파일")  // 파라미터 설명 추가
      @RequestParam("file") MultipartFile file
  ) throws IOException {
    return s3Uploader.uploadFile(file, "test", LocalDate.now());  // 현재 날짜 전달
  }
}

//@RestController
//@Tag(name = "testController")
//public class TestController {
//
//  @GetMapping("/api/test")
//  @Operation(summary = "test 추가", description = "테스트합니다.")
//  @ApiResponses(value = {
//      @ApiResponse(responseCode = "200", description = "성공", content = {@Content(mediaType = "application/json")}),
//      @ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content),
//      @ApiResponse(responseCode = "500", description = "서버 에러", content = @Content)
//  })
//  public String test() {
//    return "{\"Proxy test successful\"}";
//  }
//}