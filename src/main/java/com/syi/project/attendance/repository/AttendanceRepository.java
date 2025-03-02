package com.syi.project.attendance.repository;

import com.syi.project.attendance.entity.Attendance;
import java.time.LocalDate;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AttendanceRepository extends JpaRepository<Attendance, Long>,AttendanceRepositoryCustom {

  boolean existsByMemberIdAndDate(Long id, LocalDate localDate);

  boolean existsByMemberIdAndDateAndExitTimeNotNull(Long id, LocalDate localDate);

  boolean existsByMemberIdAndDateAndEnterTimeNotNull(Long id, LocalDate localDate);

  /**
   * 특정 학생, 날짜, 교시에 대한 출석 기록을 조회합니다.
   */
  @Query("SELECT a FROM Attendance a WHERE a.memberId = :memberId AND a.date = :date AND a.periodId = :periodId")
  Optional<Attendance> findByMemberIdAndDateAndPeriodId(
      @Param("memberId") Long memberId,
      @Param("date") LocalDate date,
      @Param("periodId") Long periodId);
}
