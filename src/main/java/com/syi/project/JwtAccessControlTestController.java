package com.syi.project;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/jwt/test")
public class JwtAccessControlTestController {

  /**
   * 보호된 테스트 엔드포인트
   *
   * @return ResponseEntity<String>
   */
  @GetMapping
  public ResponseEntity<String> protectedEndpoint() {
    return ResponseEntity.ok("This is a protected endpoint");
  }

  /**
   * 공개 테스트 엔드포인트
   *
   * @return ResponseEntity<String>
   */
  @GetMapping("/public")
  public ResponseEntity<String> publicEndpoint() {
    return ResponseEntity.ok("This is a public endpoint");
  }
}
