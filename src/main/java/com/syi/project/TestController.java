package com.syi.project;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "testController")
public class TestController {

  @GetMapping("/api/test")
  @Operation(summary = "test 추가", description = "테스트합니다.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "성공", content = {@Content(mediaType = "application/json")}),
      @ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content),
      @ApiResponse(responseCode = "500", description = "서버 에러", content = @Content)
  })
  public String test() {
    return "{\"Proxy test successful\"}";
  }
}
