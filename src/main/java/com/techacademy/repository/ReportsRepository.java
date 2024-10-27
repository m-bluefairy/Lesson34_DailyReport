package com.techacademy.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.techacademy.entity.Reports;
import java.util.List;

@Repository
public interface ReportsRepository extends JpaRepository<Reports, Long> {

    // 日報を日付で検索するクエリ
    List<Reports> findByReportDate(String reportDate);
}