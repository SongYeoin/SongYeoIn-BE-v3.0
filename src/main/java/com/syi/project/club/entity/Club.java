package com.syi.project.club.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;

@Entity
@Data
public class Club{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;    //코드번호
    private String writer;  //작성자
    private String checker; //승인자
    private String participants;    //참여자

    @Column(length=500)
    private String content; //내용
    private LocalDate regDate;  //작성일
    private LocalDate studyDate;    //활동일

    @Column(length=1)
    private String checkStatus; //승인상태('Y', 'N', 'W')
    private String checkCmt;    //승인메시지

    private String fileOriginalName;    //원본파일이름
    private String fileSavedName;   //저장된 파일이름
    private String fileType;    //파일타입
    private Long fileSize;  //파일크기
    private String filepath;    //파일경로
    private LocalDate fileRegDate;  //파일등록날짜

    //수강코드번호? enroll
}
