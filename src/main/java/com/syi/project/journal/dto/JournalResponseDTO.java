//package com.syi.project.journal.dto;
//
//import com.syi.project.journal.entity.Journal;
//import java.time.LocalDateTime;
//import lombok.Builder;
//import lombok.Getter;
//
//// 일지 작성 후 조회할 때 사용될 객체
//@Getter
//public class JournalResponseDTO {
//
//  private Long id;
//  private String memberName;
//  private String courseName;
//  private String title;
//  private String content;
//  private String fileUrl;
//  private String fileName;
//  private String fileSize;
//  private LocalDateTime createdAt;
//
//  @Builder
//  public JournalResponseDTO(Journal journal) {
//    this.id = journal.getId();
//    this.memberName = journal.getMember().getName();
//    this.courseName = journal.getCourse().getName();
//    this.title = journal.getTitle();
//    this.content = journal.getContent();
//    this.fileUrl = journal.getFileUrl();
//    this.fileName = journal.getFileName();
//    this.fileSize = journal.getFileSize();
//    this.createdAt = journal.getCreatedAt();
//  }
//
//}

package com.syi.project.journal.dto;

import com.syi.project.journal.entity.Journal;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
public class JournalResponseDTO {

  private Long id;
  //private String memberName;
  //private String courseName;
  private String title;
  private String content;
  private String fileUrl;
  private String fileName;
  private String fileSize;
  private LocalDateTime createdAt;

  @Builder
  public JournalResponseDTO(Journal journal) {
    this.id = journal.getId();
    //this.memberName = journal.getMember().getName();
    //this.courseName = journal.getCourse().getName();
    this.title = journal.getTitle();
    this.content = journal.getContent();
    this.fileUrl = journal.getFileUrl();
    this.fileName = journal.getFileName();
    this.fileSize = journal.getFileSize();
    this.createdAt = journal.getCreatedAt();
  }
}