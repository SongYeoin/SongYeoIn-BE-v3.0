package com.syi.project.notice.repository;

import com.syi.project.notice.entity.Notice;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NoticeRepository extends JpaRepository<Notice, Long>, NoticeRepositoryCustom {

  Optional<Notice> findByIdAndMemberIdAndDeletedByIsNull(Long id, Long memberId);

}
