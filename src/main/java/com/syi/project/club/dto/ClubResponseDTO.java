package com.syi.project.club.dto;

import com.syi.project.club.entity.Club;
import com.syi.project.club.file.ClubFile;
import com.syi.project.common.enums.CheckStatus;
import com.syi.project.file.dto.FileDownloadDTO;
import com.syi.project.file.entity.File;
import com.syi.project.file.repository.FileRepository;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ClubResponseDTO {

    @Getter
    @NoArgsConstructor
    public static class ClubList{
        private String writer;
        private String checker;  // 승인자
        private CheckStatus checkStatus;  // 승인 상태
        private String checkMessage;  // 승인 메시지
        private LocalDate regDate;  // 작성일
        private LocalDate studyDate;  // 활동일
        private List<String> fileIcons; // 파일 상태 (아이콘 여부 또는 URL)

        public ClubList(String writer, String checker, CheckStatus checkStatus, String checkMessage,
                        LocalDate regDate, LocalDate studyDate, List<String> fileIcons) {
            this.writer = writer;
            this.checker = checker;
            this.checkStatus = checkStatus;
            this.checkMessage = checkMessage;
            this.regDate = regDate;
            this.studyDate = studyDate;
            this.fileIcons = fileIcons;
        }

        // entity -> DTO
        public static ClubList toListDTO(Club club, String writer, String checker, List<String> fileIcons){
            return new ClubList(
                    writer,
                    checker,
                    club.getCheckStatus(),
                    club.getCheckMessage(),
                    club.getRegDate(),
                    club.getStudyDate(),
                    fileIcons
            );
        }
    }

    @Getter
    @NoArgsConstructor
    public static class ClubDetail{

        private String writer;
        private String checker;  // 승인자
        private String participants;  // 참여자
        private String content;  // 내용
        private CheckStatus checkStatus;  // 승인 상태
        private String checkMessage;  // 승인 메시지
        private LocalDate regDate;  // 작성일
        private LocalDate studyDate;  // 활동일;
        private List<String> fileNames;

        public ClubDetail(String writer, String checker, String participants, String content, CheckStatus checkStatus, String checkMessage,
                               LocalDate regDate, LocalDate studyDate, List<String> fileNames) {
            this.writer = writer;
            this.checker = checker;
            this.participants = participants;
            this.content = content;
            this.checkStatus = checkStatus;
            this.checkMessage = checkMessage;
            this.regDate = regDate;
            this.studyDate = studyDate;
            this.fileNames = fileNames;
        }

        // entity -> DTO
        public static ClubDetail toDetailDTO(Club club, String writer, String checker, List<String> fileNames){
            return new ClubDetail(
                    writer,
                    checker,
                    club.getParticipants(),
                    club.getContent(),
                    club.getCheckStatus(),
                    club.getCheckMessage(),
                    club.getRegDate(),
                    club.getStudyDate(),
                    fileNames
            );
        }
    }
}
