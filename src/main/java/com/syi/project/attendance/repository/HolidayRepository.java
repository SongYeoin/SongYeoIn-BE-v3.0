package com.syi.project.attendance.repository;

import com.syi.project.attendance.entity.Holiday;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface HolidayRepository extends JpaRepository<Holiday, Long> {

  // 특정 날짜의 공휴일 정보 조회
  Optional<Holiday> findByDate(LocalDate date);

  // 특정 연도(기간)의 모든 공휴일 조회
  List<Holiday> findAllByDateBetween(LocalDate startDate, LocalDate endDate);

  // 특정 연도의 모든 공휴일만 조회
  @Query("SELECT h FROM Holiday h WHERE YEAR(h.date) = :year")
  List<Holiday> findAllByYear(@Param("year") int year);

  // ✅ 특정 날짜가 존재하는지 확인
  boolean existsByDate(LocalDate date);

  long countByDateBetween(LocalDate startDate, LocalDate endDate);
}
