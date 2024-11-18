package com.syi.project.journal.entity;

import com.syi.project.common.entity.BaseTimeEntity;
import com.syi.project.file.entity.File;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "journal_files")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class JournalFile extends BaseTimeEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "journal_id")
  private Journal journal;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "file_id")
  private File file;
}