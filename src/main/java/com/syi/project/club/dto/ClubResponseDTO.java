package com.syi.project.club.dto;

import com.syi.project.club.entity.Club;
import com.syi.project.common.enums.CheckStatus;
import com.syi.project.file.dto.FileDownloadDTO;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
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
        private String url;

        public ClubList(String writer, String checker, CheckStatus checkStatus, String checkMessage,
                        LocalDate regDate, LocalDate studyDate, String url) {
            this.writer = writer;
            this.checker = checker;
            this.checkStatus = checkStatus;
            this.checkMessage = checkMessage;
            this.regDate = regDate;
            this.studyDate = studyDate;
            this.url = url;
        }

        // entity -> DTO
        public static ClubList toDTO(Club club, String writer, String checker, String url){
            return new ClubList(
                    writer,
                    checker,
                    club.getCheckStatus(),
                    club.getCheckMessage(),
                    club.getRegDate(),
                    club.getStudyDate(),
                    url
            );
        }
    }

//    private Long id;  // 동아리코드
//    private String name;  // 프로그램명
//    private String writer;  // 작성자
//    private String checker;  // 승인자
//    private String participants;  // 참여자
//    private String content;  // 내용
//    private LocalDate regDate;  // 작성일
//    private LocalDate studyDate;  // 활동일
//    private CheckStatus checkStatus;  // 승인 상태
//    private String checkMessage;  // 승인 메시지

    public static class ClubDetail{

    }



}
