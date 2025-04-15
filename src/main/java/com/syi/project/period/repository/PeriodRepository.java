package com.syi.project.period.repository;


import com.syi.project.period.entity.Period;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PeriodRepository extends JpaRepository<Period, Long> ,PeriodRepositoryCustom {
}
