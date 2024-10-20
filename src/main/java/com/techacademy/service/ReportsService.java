package com.techacademy.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;

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

    // 日付でレポートを検索するメソッド
    public Reports findByReportDate(String reportDate) {
        if (reportDate == null || reportDate.isEmpty()) {
            throw new IllegalArgumentException("日付が入力されていません。");
        }

        try {
            LocalDate date = LocalDate.parse(reportDate);
            return reportsRepository.findByReportDate(date);
        } catch (DateTimeParseException e) {
            System.err.println("日付の解析に失敗しました: " + reportDate);
            throw new IllegalArgumentException("無効な日付形式です。正しい形式で入力してください。例: YYYY-MM-DD");
        }
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
        LocalDate reportDate = reports.getReportDate();
        Reports existingReport = reportsRepository.findByReportDate(reportDate);
        if (existingReport != null) {
            return ErrorKinds.DUPLICATE_ERROR;
        }

        reports.setDeleteFlg(false);
        LocalDateTime now = LocalDateTime.now();
        reports.setCreatedAt(now);
        reports.setUpdatedAt(now);

        reportsRepository.save(reports);
        return ErrorKinds.SUCCESS;
    }

    // 日付更新（追加）
    @Transactional
    public ErrorKinds update(Reports reports, String newDate) {
        if (newDate == null || newDate.isEmpty()) {
            throw new IllegalArgumentException("新しい日付が入力されていません。");
        }

        try {
            LocalDate newReportDate = LocalDate.parse(newDate);
            reports.setReportDate(newReportDate);

            // 日付チェック
            ErrorKinds result = reportsDateCheck(reports);
            if (ErrorKinds.CHECK_OK != result) {
                return result;
            }

            // 日付重複チェック
            Reports existingReport = reportsRepository.findByReportDate(newReportDate);
            if (existingReport != null && !existingReport.getId().equals(reports.getId())) {
                return ErrorKinds.DUPLICATE_ERROR;
            }

            // 更新日時の設定
            reports.setUpdatedAt(LocalDateTime.now());
            reportsRepository.save(reports);
        } catch (DateTimeParseException e) {
            System.err.println("新しい日付の解析に失敗しました: " + newDate);
            return ErrorKinds.INVALID_DATE; // 必要に応じてエラータイプを設定
        }

        return ErrorKinds.SUCCESS;
    }

    // 日付削除処理
    @Transactional
    public ErrorKinds delete(String reportDate, UserDetail userDetail) {
        if (reportDate == null || reportDate.isEmpty()) {
            throw new IllegalArgumentException("日付が入力されていません。");
        }

        LocalDate date;
        try {
            date = LocalDate.parse(reportDate);
        } catch (DateTimeParseException e) {
            System.err.println("日付の解析に失敗しました: " + reportDate);
            return ErrorKinds.INVALID_DATE;
        }

        Reports report = reportsRepository.findByReportDate(date);
        if (report == null) {
            return ErrorKinds.NOT_FOUND;
        }

        // 削除処理
        report.setDeleteFlg(true);
        reportsRepository.save(report);
        return ErrorKinds.SUCCESS;
    }

    private ErrorKinds reportsDateCheck(Reports reports) {
        // 日付チェックのロジックを実装
        return ErrorKinds.CHECK_OK;
    }

    // 日報一覧表示処理
    public List<Reports> findAll() {
        return reportsRepository.findAll();
    }

    // 1件を検索
    public Reports findById(Long id) {
        Optional<Reports> option = reportsRepository.findById(id);
        return option.orElse(null);
    }
}
