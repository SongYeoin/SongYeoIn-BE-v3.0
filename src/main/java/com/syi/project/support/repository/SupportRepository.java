package com.syi.project.support.repository;

import com.syi.project.support.entity.Support;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SupportRepository extends JpaRepository<Support, Long>, SupportRepositoryCustom {
  Page<Support> findByMemberIdAndDeletedByIsNullAndTitleContainingIgnoreCase(Long memberId, String title, Pageable pageable);
  Page<Support> findByMemberIdAndDeletedByIsNull(Long memberId, Pageable pageable);
  Page<Support> findByDeletedByIsNullAndTitleContainingIgnoreCase(String title, Pageable pageable);
  Page<Support> findByDeletedByIsNull(Pageable pageable);
  Optional<Support> findByIdAndMemberIdAndDeletedByIsNull(Long id, Long memberId);
  Optional<Support> findByIdAndDeletedByIsNull(Long id);
}