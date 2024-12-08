package com.syi.project.club.file;

import com.syi.project.club.entity.Club;
import com.syi.project.file.entity.File;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ClubFile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "club_file_id")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)  // ManyToOne에서 OneToOne으로 변경
    @JoinColumn(name = "club_id")
    private Club club;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "file_id")
    private File file;


    @Builder
    public ClubFile(Club club, File file) {
        this.club = club;
        this.file = file;
    }

    public void updateFile(File file) {
        this.file = file;
    }
    public void updateClub(Club club) {
        this.club = club;
        if(club != null){
            club.setFile(this);
        }
    }

}