package com.syi.project.club.file;

import com.syi.project.file.enums.FileStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClubFileRepository extends JpaRepository<ClubFile, Long> {

    List<ClubFile> findByClubId(Long id);

}
