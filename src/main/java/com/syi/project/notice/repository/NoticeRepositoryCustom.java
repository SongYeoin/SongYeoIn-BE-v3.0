package com.syi.project.notice.repository;

import com.syi.project.notice.entity.Notice;
import java.util.List;

public interface NoticeRepositoryCustom {

  List<Notice> findNoticesByCourseIdAndGlobal(Long courseId);

}
