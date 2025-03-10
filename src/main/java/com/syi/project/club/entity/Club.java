package com.syi.project.club.entity;

import com.syi.project.club.dto.ClubRequestDTO;
import com.syi.project.club.file.ClubFile;
import com.syi.project.common.enums.CheckStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalTime;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;

//import static com.syi.project.file.entity.QFile.file;

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

    @NotBlank(message = "참여자를 선택해주세요")
    @Column(nullable = false)
    private String participants;    //참여자

    @Column(nullable = false)
    private int participantCount;   //총 인원

    @NotBlank(message = "동아리명을 입력해주세요")
    @Column(nullable = false)
    private String clubName;    //동아리명

    private String contactNumber;   //대표연락처

    @Length(max = 100, message = "100자 이하로 입력해주세요")
    private String content; //내용

    @DateTimeFormat(pattern="yyyy-MM-dd")
    private LocalDate regDate;  //작성일

    @DateTimeFormat(pattern="yyyy-MM-dd")
    @Column(nullable = false)
    private LocalDate studyDate;    //활동일

    @NotNull(message = "시작 시간을 입력해주세요.")
    @DateTimeFormat(pattern = "HH:mm")
    @Column(nullable = false)
    private LocalTime startTime;    //시작시간

    @NotNull(message = "종료 시간을 입력해주세요.")
    @DateTimeFormat(pattern = "HH:mm")
    @Column(nullable = false)
    private LocalTime endTime;    //종료시간

    @Enumerated(EnumType.STRING)
    @Column(length=1, nullable = false)
    private CheckStatus checkStatus; //승인상태('Y', 'N', 'W')

    private String checkMessage;    //승인메시지

    @Column(nullable = false)
    private Long courseId;  //프로그램정보

    @OneToOne(mappedBy = "club", cascade = CascadeType.ALL, orphanRemoval = true)
    private ClubFile clubFile; // 단일파일

    @Builder
    public Club(Long courseId, Long writerId, String participants, String content,
                LocalDate regDate, LocalDate studyDate, CheckStatus checkStatus, Long checkerId, String checkMessage,
                String clubName, String contactNumber, LocalTime startTime, LocalTime endTime, int participantCount) {
        this.courseId = courseId;
        this.writerId = writerId;
        this.participants = participants;
        this.content = content;
        this.regDate = regDate != null ? regDate : LocalDate.now();  // 기본 값 설정
        this.studyDate = studyDate;
        this.checkStatus = checkStatus != null ? checkStatus : CheckStatus.W;
        this.checkerId = checkerId;
        this.checkMessage = checkMessage;
        this.clubName = clubName;
        this.contactNumber = contactNumber;
        this.startTime = startTime;
        this.endTime = endTime;
        this.participantCount = participantCount;
    }

    // entity -> DTO
    public ClubRequestDTO.ClubCreate toCreateDTO() {
        return new ClubRequestDTO.ClubCreate(
                this.participants, this.content, this.studyDate, this.clubName, this.contactNumber, this.startTime, this.endTime, this.participantCount);
    }

    public ClubRequestDTO.ClubUpdate toUpdateDTO(MultipartFile file) {
        return new ClubRequestDTO.ClubUpdate(
                this.participants, this.content, this.studyDate, this.clubName, this.contactNumber, this.startTime, this.endTime, this.participantCount);
    }

    public ClubRequestDTO.ClubApproval toApprovalDTO() {
        return new ClubRequestDTO.ClubApproval(
                this.checkStatus, this.checkMessage);
    }

    // ClubFile과의 연관 관계 없이 처리하는 메서드
//    public ClubFile associateFile(Long fileId) {
//        return ClubFile.builder()
//                .clubId(this.id)
//                .fileId(fileId)
//                .build();
//    }

    // Club에서 파일 URL을 가져오는 메서드 (연관 관계 없이 접근)
//    public List<String> getFileUrls(List<ClubFile> clubFiles, FileRepository fileRepository) {
//        List<String> fileUrls = new ArrayList<>();
//        for (ClubFile clubFile : clubFiles) {
//            // ClubFile을 통해 fileId를 사용하여 File 엔티티를 조회
//            Long fileId = clubFile.getFileId();
//            File file = fileRepository.findById(fileId).orElse(null);  // fileId로 File을 조회
//            if (file != null && file.getPath() != null && !file.getPath().isEmpty()) {
//                fileUrls.add("clip_icon"); // 파일 위치가 있으면 클립 아이콘 표시
//            } else {
//                fileUrls.add(""); // 파일 위치가 없으면 공백
//            }
//        }
//        return fileUrls;
//    }

    public void updateDetails(String participants, String content, LocalDate studyDate, LocalDate regDate,
                                String clubName, String contactNumber, LocalTime startTime, LocalTime endTime, int participantCount) {
        this.participants = participants;
        this.content = content;
        this.studyDate = studyDate;
        this.regDate = regDate;
        this.clubName = clubName;
        this.contactNumber = contactNumber;
        this.startTime = startTime;
        this.endTime = endTime;
        this.participantCount = participantCount;
    }

    // 파일 설정 메서드
    public void setFile(ClubFile clubFile) {
        this.clubFile = clubFile;
    }

    // 파일 URL 가져오기
    public String getFileUrl() {
        return this.clubFile != null && this.clubFile.getFile() != null
                ? this.clubFile.getFile().getPath()
                : null;
    }

    public void updateApprove(Long adminId, CheckStatus checkStatus, String checkMessage) {
        this.checkStatus = checkStatus;
        this.checkMessage = checkMessage;
        this.checkerId = adminId;
    }

}
