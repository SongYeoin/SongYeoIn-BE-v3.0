package com.syi.project.club.file;

import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClubFileRepository {

    List<ClubFile> findByClubId(Long id);
}
