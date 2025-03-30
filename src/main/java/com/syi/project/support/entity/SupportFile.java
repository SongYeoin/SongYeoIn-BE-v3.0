package com.syi.project.support.entity;

import com.syi.project.common.entity.BaseTimeEntity;
import com.syi.project.file.entity.File;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "support_files")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SupportFile extends BaseTimeEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "support_id", nullable = false)
  private Support support;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "file_id", nullable = false)
  private File file;

  @Builder
  public SupportFile(Support support, File file) {
    this.support = support;
    this.file = file;
  }
}