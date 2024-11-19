package com.syi.project.club.dto;

import com.syi.project.club.entity.Club;
import com.syi.project.common.enums.CheckStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

public class ClubRequestDTO {

    @Getter
    @NoArgsConstructor
    public static class ClubCreate{
        private String participants;  // 참여자
        private String content;  // 내용
        private LocalDate regDate;  // 작성일
        private LocalDate studyDate;  // 활동일
        private CheckStatus checkStatus;  // 승인 상태

        public ClubCreate(String participants, String content,
                                 LocalDate regDate, LocalDate studyDate, CheckStatus checkStatus){
            this.participants = participants;
            this.content = content;
            this.regDate = regDate != null ? regDate : LocalDate.now();
            this.studyDate = studyDate;
            this.checkStatus = checkStatus != null ? checkStatus : CheckStatus.W;
        }

        // DTO -> entity
        public Club toEntity(Long writerId, Long courseId){
            return Club.builder()
                    .participants(participants)
                    .content(content)
                    .studyDate(studyDate)
                    .checkStatus(checkStatus != null ? checkStatus : CheckStatus.W)
                    .regDate(regDate != null ? regDate : LocalDate.now())
                    .writerId(writerId)
                    .courseId(courseId)
                    .build();
        }
    }

    @Getter
    @NoArgsConstructor
    public static class ClubUpdate{
        private String participants;
        private String content;
        private LocalDate regDate;
        private LocalDate studyDate;

        public ClubUpdate(String participants, String content, LocalDate regDate, LocalDate studyDate){
            this.participants = participants;
            this.content = content;
            this.regDate = regDate != null ? regDate : LocalDate.now();
            this.studyDate = studyDate;
        }

        // DTO -> entity
        public Club toEntity(){
            return Club.builder()
                    .participants(participants)
                    .content(content)
                    .studyDate(studyDate)
                    .regDate(regDate != null ? regDate : LocalDate.now())
                    .build();
        }
    }

    @Getter
    @NoArgsConstructor
    public static class ClubApproval{
        private CheckStatus checkStatus;
        private String checkMessage;

        public ClubApproval(CheckStatus checkStatus, String checkMessage){
            this.checkStatus = checkStatus != null ? checkStatus : CheckStatus.W;
            this.checkMessage = checkMessage;
        }

        // DTO -> entity
        public Club toEntity(){
            return Club.builder()
                    .checkStatus(checkStatus != null ? checkStatus : CheckStatus.W)
                    .checkMessage(checkMessage)
                    .build();
        }
    }

}