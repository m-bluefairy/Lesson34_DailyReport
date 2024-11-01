package com.techacademy.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.techacademy.entity.Reports;
import com.techacademy.entity.Employee;

import java.util.List;

@Repository
public interface ReportsRepository extends JpaRepository<Reports, Long> {
    // 従業員に紐づく日報を取得するメソッド
    List<Reports> findByEmployee(Employee employee);
}