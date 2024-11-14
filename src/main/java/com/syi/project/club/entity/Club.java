package com.syi.project.club.entity;

import com.syi.project.club.dto.ClubDTO;
import com.syi.project.club.dto.ClubRequestDTO;
import com.syi.project.common.enums.CheckStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Columns;
import org.hibernate.validator.constraints.Length;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Entity
@Getter
@NoArgsConstructor
public class Club{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;    //코드번호

    @Column(nullable = false)
    private String writer;  //작성자

    private String checker; //승인자

    @NotBlank(message = "참여자 명단을 입력해주세요")
    @Column(nullable = false)
    private String participants;    //참여자

    @Length(max = 100, message = "100자 이하로 입력해주세요")
    private String content; //내용
    @DateTimeFormat(pattern="yyyy-MM-dd")
    private LocalDate regDate;  //작성일
    @DateTimeFormat(pattern="yyyy-MM-dd")
    @Column(nullable = false)
    private LocalDate studyDate;    //활동일

    @Enumerated(EnumType.STRING)
    @Column(length=1, nullable = false)
    private CheckStatus checkStatus; //승인상태('Y', 'N', 'W')
    private String checkMessage;    //승인메시지

    @Column(nullable = false)
    private Long courseId;

    private String fileOriginalName;    //원본파일이름
    private String fileSavedName;   //저장된 파일이름
    private String fileType;    //파일타입
    private Long fileSize;  //파일크기
    private String filepath;    //파일경로
    private LocalDate fileRegDate;  //파일등록날짜

    @Builder
    public Club(Long courseId, String writer, String participants, String content,
                LocalDate regDate, LocalDate studyDate, CheckStatus checkStatus, String checker, String checkMessage) {
        this.courseId = courseId;
        this.writer = writer;
        this.participants = participants;
        this.content = content;
        this.regDate = regDate != null ? regDate : LocalDate.now();  // 기본 값 설정
        this.studyDate = studyDate;
        this.checkStatus = checkStatus != null ? checkStatus : CheckStatus.W;
        this.checker = checker;
        this.checkMessage = checkMessage;
    }

    // entity -> DTO
    public ClubRequestDTO.ClubCreate toCreateDTO() {
        return new ClubRequestDTO.ClubCreate(
                this.courseId, this.writer, this.participants, this.content, this.regDate, this.studyDate, this.checkStatus);
    }

    public ClubRequestDTO.ClubUpdate toUpdateDTO() {
        return new ClubRequestDTO.ClubUpdate(
                this.participants, this.content, this.regDate, this.studyDate);
    }

    public ClubRequestDTO.ClubApproval toApprovalDTO() {
        return new ClubRequestDTO.ClubApproval(
                this.checker, this.checkStatus, this.checkMessage);
    }
}
