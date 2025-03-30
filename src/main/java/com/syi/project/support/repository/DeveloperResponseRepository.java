package com.syi.project.support.repository;

import com.syi.project.support.entity.DeveloperResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DeveloperResponseRepository extends JpaRepository<DeveloperResponse, Long> {
  Optional<DeveloperResponse> findBySupportId(Long supportId);
}