package com.syi.project.auth.entity;

import com.syi.project.common.enums.CheckStatus;
import com.syi.project.common.enums.Role;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.LocalDate;
import lombok.Getter;
import org.springframework.format.annotation.DateTimeFormat;

@Entity
@Getter
public class Member {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true)
  private String memberId;

  @Column(nullable = false)
  private String password;

  @Column(nullable = false)
  private String name;

  @Column(nullable = false)
  private String birthday;

  @Column(nullable = false, unique = true)
  private String email;

  @DateTimeFormat(pattern = "yyyy-MM-dd")
  private LocalDate enrollDate = LocalDate.now();

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 1)
  private CheckStatus checkStatus = CheckStatus.W;

  @Column(nullable = false)
  private Boolean isDeleted = false;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private Role role;

  private String profileUrl;

  // 기본 생성자
  protected Member() {
  }

  // 필수 필드를 초기화하는 생성자
  public Member(String memberId, String password, String name, String birthday, String email,
      Role role) {
    this.memberId = memberId;
    this.password = password;
    this.name = name;
    this.birthday = birthday;
    this.email = email;
    this.role = role;
    this.enrollDate = LocalDate.now();
    this.checkStatus = CheckStatus.W;
    this.isDeleted = false;
  }

}
