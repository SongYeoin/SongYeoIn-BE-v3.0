package com.syi.project.file.repository;

import com.syi.project.file.entity.File;
import com.syi.project.file.enums.FileStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FileRepository extends JpaRepository<File, Long> {

  Optional<File> findByObjectKeyAndStatus(String objectKey, FileStatus status);
  List<File> findByUploadedByIdAndStatus(Long uploadedBy, FileStatus status);
  boolean existsByObjectKey(String objectKey);

}
