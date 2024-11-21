package com.syi.project.club.entity;

import com.syi.project.club.dto.ClubRequestDTO;
import com.syi.project.club.file.ClubFile;
import com.syi.project.common.enums.CheckStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Entity
@Getter
@NoArgsConstructor
public class Club{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "club_id")
    private Long id;    //코드번호

    @Column(nullable = false)
    private Long writerId;  //작성자

    private Long checkerId; //승인자

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
    private Long courseId;  //프로그램정보

    @Builder
    public Club(Long courseId, Long writerId, String participants, String content,
                LocalDate regDate, LocalDate studyDate, CheckStatus checkStatus, Long checkerId, String checkMessage) {
        this.courseId = courseId;
        this.writerId = writerId;
        this.participants = participants;
        this.content = content;
        this.regDate = regDate != null ? regDate : LocalDate.now();  // 기본 값 설정
        this.studyDate = studyDate;
        this.checkStatus = checkStatus != null ? checkStatus : CheckStatus.W;
        this.checkerId = checkerId;
        this.checkMessage = checkMessage;
    }

    // entity -> DTO
    public ClubRequestDTO.ClubCreate toCreateDTO() {
        return new ClubRequestDTO.ClubCreate(
                this.participants, this.content, this.studyDate);
    }

    public ClubRequestDTO.ClubUpdate toUpdateDTO() {
        return new ClubRequestDTO.ClubUpdate(
                this.participants, this.content, this.regDate, this.studyDate);
    }

    public ClubRequestDTO.ClubApproval toApprovalDTO() {
        return new ClubRequestDTO.ClubApproval(
                this.checkStatus, this.checkMessage);
    }

    // ClubFile과의 연관 관계 없이 처리하는 메서드
    public ClubFile associateFile(Long fileId) {
        return ClubFile.builder()
                .clubId(this.id)
                .fileId(fileId)
                .build();
    }
}
