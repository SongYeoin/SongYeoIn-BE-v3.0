package com.syi.project.period.repository;


import com.syi.project.period.eneity.Period;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PeriodRepository extends JpaRepository<Period, Long> ,PeriodRepositoryCustom {
}
