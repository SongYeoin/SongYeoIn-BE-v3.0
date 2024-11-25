package com.syi.project.club.file;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClubFileRepository extends JpaRepository<ClubFile, Integer> {

    List<ClubFile> findByClubId(Long id);
}
