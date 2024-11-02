package com.techacademy.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.techacademy.entity.Reports;
import com.techacademy.entity.Employee;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ReportsRepository extends JpaRepository<Reports, Long> {
    // 従業員に紐づく日報を取得するメソッド
    List<Reports> findByEmployee(Employee employee);

    // 全従業員の日報を取得するメソッド（削除フラグが立っていないもの）
    List<Reports> findAllByDeleteFlgFalse();

    // 特定の従業員の日報を取得するメソッド（削除フラグが立っていないもの）
    List<Reports> findByEmployeeAndDeleteFlgFalse(Employee employee);

    // 従業員コードに基づいて日報を取得するメソッド
    List<Reports> findByEmployeeCode(String employeeCode);

    // 従業員コードに基づいて日報の存在を確認するメソッド
    boolean existsByEmployeeCode(String employeeCode);

    // 従業員コードと日付に基づいて日報の存在を確認するメソッド
    boolean existsByEmployeeCodeAndReportDate(String employeeCode, LocalDate reportDate);
}
