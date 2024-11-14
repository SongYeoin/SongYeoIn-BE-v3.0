package com.syi.project.club.dto;

import com.syi.project.common.enums.CheckStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;

@Getter
@ToString
@AllArgsConstructor
public class ClubDTO {
    private Long id;  // 동아리코드
    private String name;  // 프로그램명
    private String writer;  // 작성자
    private String checker;  // 승인자
    private String participants;  // 참여자
    private String content;  // 내용
    private LocalDate regDate;  // 작성일
    private LocalDate studyDate;  // 활동일
    private CheckStatus checkStatus;  // 승인 상태
    private String checkMessage;  // 승인 메시지

    private String fileOriginalName;  // 원본 파일 이름
    private String fileSavedName;  // 저장된 파일 이름
    private String fileType;  // 파일 타입
    private Long fileSize;  // 파일 크기
    private String filepath;  // 파일 경로
    private LocalDate fileRegDate;  // 파일 등록 날짜
}
