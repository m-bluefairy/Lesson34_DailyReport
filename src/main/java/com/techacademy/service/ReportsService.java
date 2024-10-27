package com.techacademy.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.techacademy.constants.ErrorKinds;
import com.techacademy.entity.Reports;
import com.techacademy.repository.ReportsRepository;

@Service
public class ReportsService {

    private final ReportsRepository reportsRepository;

    @Autowired
    public ReportsService(ReportsRepository reportsRepository) {
        this.reportsRepository = reportsRepository;
    }

    // 日報保存
    @Transactional
    public ErrorKinds save(Reports reports) {
        // 日付チェック
        ErrorKinds result = reportsDateCheck(reports);
        if (ErrorKinds.CHECK_OK != result) {
            return result;
        }

        // 日付重複チェック
        if (findByReportDate(reports.getReportDate()) != null) {
            return ErrorKinds.DUPLICATE_ERROR;
        }

        reports.setDeleteFlg(false);
        LocalDateTime now = LocalDateTime.now();
        reports.setCreatedAt(now);
        reports.setUpdatedAt(now);

        reportsRepository.save(reports);
        return ErrorKinds.SUCCESS;
    }

    // 日付更新
    @Transactional
    public ErrorKinds update(Reports reports, String newReportDate) {
        if (!newReportDate.isEmpty()) {
            reports.setReportDate(newReportDate);
            ErrorKinds result = reportsDateCheck(reports);
            if (ErrorKinds.CHECK_OK != result) {
                return result;
            }
        }

        // 更新日時の設定
        LocalDateTime now = LocalDateTime.now();
        reports.setUpdatedAt(now);
        reportsRepository.save(reports);

        return ErrorKinds.SUCCESS;
    }

    // 日付フォーマットチェック
    private ErrorKinds reportsDateCheck(Reports reports) {
        if (reports.getReportDate() == null || reports.getReportDate().isEmpty()) {
            return ErrorKinds.DATE_FORMAT_ERROR; // 日付フォーマットエラー
        }
        return ErrorKinds.CHECK_OK;
    }

    // 日報削除
    @Transactional
    public ErrorKinds delete(String reportDate, UserDetail userDetail) {
        Reports reports = findByReportDate(reportDate);
        if (reports == null) {
            return ErrorKinds.DATECHECK_ERROR;
        }

        if (userDetail.getReports().getReportList().contains(reports)) {
            return ErrorKinds.LOGINCHECK_ERROR;
        }

        LocalDateTime now = LocalDateTime.now();
        reports.setUpdatedAt(now);
        reports.setDeleteFlg(true);
        reportsRepository.save(reports);

        return ErrorKinds.SUCCESS;
    }

    // 日報取得
    public Reports findByReportDate(String reportDate) {
        return reportsRepository.findByReportDate(reportDate);
    }

    // 日報一覧表示
    public List<Reports> findAll() {
        return reportsRepository.findAll();
    }

    // 現在の日報を取得
    public Reports findCurrenReports(String reportDate) {
        Reports reports = findByReportDate(reportDate);
        if (reports == null) {
            throw new RuntimeException("日報が見つかりません");
        }
        return reports;
    }
}