package com.techacademy.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.techacademy.entity.Reports;

@Repository
public interface ReportsRepository extends JpaRepository<Reports, Long> {
    // IDで日報を検索するメソッドはデフォルトで提供されているため追加は不要
}