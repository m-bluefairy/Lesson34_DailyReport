package com.techacademy.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.techacademy.entity.Reports;
import java.time.LocalDate;


public interface ReportsRepository extends JpaRepository<Reports, Long> {
    Reports findByReportDate(LocalDate reportDate); // 戻り値をListに変更
}
