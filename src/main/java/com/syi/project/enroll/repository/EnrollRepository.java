package com.syi.project.enroll.repository;

import com.syi.project.enroll.entity.Enroll;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EnrollRepository extends JpaRepository<Enroll, Long>, EnrollRepositoryCustom {

}
