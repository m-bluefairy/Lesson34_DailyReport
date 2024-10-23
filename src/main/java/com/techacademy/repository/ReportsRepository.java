package com.techacademy.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.techacademy.entity.Reports;
import java.time.LocalDate;
import java.util.List;

public interface ReportsRepository extends JpaRepository<Reports, Long> {
    List<Reports> findByReportDate(LocalDate reportDate); // 戻り値をListに変更
}
