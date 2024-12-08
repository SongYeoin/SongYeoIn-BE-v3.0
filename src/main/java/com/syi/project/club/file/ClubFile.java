package com.syi.project.club.file;

import com.syi.project.club.entity.Club;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ClubFile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "club_file_id")
    private Long id;

    private Long clubId;

    private Long fileId;


    @Builder
    public ClubFile(Long clubId, Long fileId) {
        this.clubId = clubId;
        this.fileId = fileId;
    }

    public void updateFileId(Long fileId) {
        this.fileId = fileId;
    }

}