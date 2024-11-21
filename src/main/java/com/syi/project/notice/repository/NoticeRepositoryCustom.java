package com.syi.project.notice.repository;

import com.syi.project.notice.entity.Notice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface NoticeRepositoryCustom {

  Page<Notice> findNoticesByCourseIdAndGlobal(Long courseId, String titleKeyword, Pageable pageable);

}
