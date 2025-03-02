package com.syi.project.support.repository;

import com.syi.project.support.entity.Support;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface SupportRepositoryCustom {
  Page<Support> searchSupports(Long memberId, String keyword, Pageable pageable);
}