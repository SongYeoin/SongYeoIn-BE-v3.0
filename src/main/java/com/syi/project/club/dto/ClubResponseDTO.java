package com.syi.project.club.dto;

import com.syi.project.club.entity.Club;
import com.syi.project.common.enums.CheckStatus;
import com.syi.project.file.dto.FileResponseDTO;
import java.time.LocalTime;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

public class ClubResponseDTO {

    @Getter
    @NoArgsConstructor
    public static class ClubList{
        private Long clubId;
        private String writer;
        private String checker;  // 승인자
        private CheckStatus checkStatus;  // 승인 상태
        private String checkMessage;  // 승인 메시지
        private LocalDate regDate;  // 작성일
        private LocalDate studyDate;  // 활동일
        private FileResponseDTO file; // 단일파일

        public ClubList(Long clubId, String writer, String checker, CheckStatus checkStatus, String checkMessage,
                        LocalDate regDate, LocalDate studyDate, FileResponseDTO file) {
            this.clubId = clubId;
            this.writer = writer;
            this.checker = checker;
            this.checkStatus = checkStatus;
            this.checkMessage = checkMessage;
            this.regDate = regDate;
            this.studyDate = studyDate;
            this.file = file;
        }

        // entity -> DTO
        public static ClubList toListDTO(Club club, String writer, String checker, FileResponseDTO file){
            return new ClubList(
                    club.getId(),
                    writer,
                    checker,
                    club.getCheckStatus(),
                    club.getCheckMessage(),
                    club.getRegDate(),
                    club.getStudyDate(),
                    file
            );
        }
    }

    @Getter
    @NoArgsConstructor
    public static class ClubDetail{

        private Long clubId;
        private String writer;
        private String checker;  // 승인자
        private String participants;  // 참여자
        private String content;  // 내용
        private CheckStatus checkStatus;  // 승인 상태
        private String checkMessage;  // 승인 메시지
        private LocalDate regDate;  // 작성일
        private LocalDate studyDate;  // 활동일;
        private FileResponseDTO file; // 단일파일
        private String clubName;    //동아리명
        private String contactNumber;   //대표연락처
        private LocalTime startTime;    //시작시간
        private LocalTime endTime;    //종료시간
        private int participantCount;   //총 인원

        public ClubDetail(Long clubId, String writer, String checker, String participants, String content, CheckStatus checkStatus, String checkMessage,
                               LocalDate regDate, LocalDate studyDate, FileResponseDTO file,
                        String clubName, String contactNumber, LocalTime startTime, LocalTime endTime, int participantCount) {
            this.clubId = clubId;
            this.writer = writer;
            this.checker = checker;
            this.participants = participants;
            this.content = content;
            this.checkStatus = checkStatus;
            this.checkMessage = checkMessage;
            this.regDate = regDate;
            this.studyDate = studyDate;
            this.file = file;
            this.clubName = clubName;
            this.contactNumber = contactNumber;
            this.startTime = startTime;
            this.endTime = endTime;
            this.participantCount = participantCount;
        }

        // entity -> DTO
        public static ClubDetail toDetailDTO(Club club, String writer, String checker, FileResponseDTO file){
            return new ClubDetail(
                    club.getId(),
                    writer,
                    checker,
                    club.getParticipants(),
                    club.getContent(),
                    club.getCheckStatus(),
                    club.getCheckMessage(),
                    club.getRegDate(),
                    club.getStudyDate(),
                    file,
                    club.getClubName(),
                    club.getContactNumber(),
                    club.getStartTime(),
                    club.getEndTime(),
                    club.getParticipantCount()
            );
        }
    }
}
