package com.syi.project.club.dto;

import com.syi.project.club.entity.Club;
import com.syi.project.common.enums.CheckStatus;
import java.time.LocalTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;

public class ClubRequestDTO {

    @Getter
    @NoArgsConstructor
    public static class ClubCreate{
        private String participants;  // 참여자
        private String content;  // 내용
        private LocalDate studyDate;  // 활동일
        private int participantCount;   //총 인원
        private String clubName;    //동아리명
        private String contactNumber;   //대표연락처
        private LocalTime startTime;    //시작시간
        private LocalTime endTime;    //종료시간

        public ClubCreate(String participants, String content, LocalDate studyDate,
                        String clubName, String contactNumber, LocalTime startTime, LocalTime endTime, int participantCount){
            this.participants = participants;
            this.content = content;
            this.studyDate = studyDate;
            this.clubName = clubName;
            this.contactNumber = contactNumber;
            this.startTime = startTime;
            this.endTime = endTime;
            this.participantCount = participantCount;
        }

        // DTO -> entity
        public Club toEntity(Long writerId, Long courseId, LocalDate regDate, CheckStatus checkStatus){
            return Club.builder()
                    .participants(participants)
                    .content(content)
                    .studyDate(studyDate)
                    .clubName(clubName)
                    .contactNumber(contactNumber)
                    .startTime(startTime)
                    .endTime(endTime)
                    .participantCount(participantCount)
                    .checkStatus(checkStatus)
                    .regDate(regDate)
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
        private LocalDate studyDate;
        private MultipartFile file;
        private int participantCount;   //총 인원
        private String clubName;    //동아리명
        private String contactNumber;   //대표연락처
        private LocalTime startTime;    //시작시간
        private LocalTime endTime;    //종료시간

        public ClubUpdate(String participants, String content, LocalDate studyDate, MultipartFile file,
            String clubName, String contactNumber, LocalTime startTime, LocalTime endTime, int participantCount){
            this.participants = participants;
            this.content = content;
            this.studyDate = studyDate;
            this.file = file;
            this.clubName = clubName;
            this.contactNumber = contactNumber;
            this.startTime = startTime;
            this.endTime = endTime;
            this.participantCount = participantCount;
        }

        // DTO -> entity
        public Club toEntity(){
            return Club.builder()
                    .participants(participants)
                    .content(content)
                    .studyDate(studyDate)
                    .clubName(clubName)
                    .contactNumber(contactNumber)
                    .startTime(startTime)
                    .endTime(endTime)
                    .participantCount(participantCount)
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
