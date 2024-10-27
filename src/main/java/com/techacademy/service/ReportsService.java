package com.techacademy.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
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

    // 日報保存
    @Transactional
    public ErrorKinds save(Reports reports) {
        // 日付チェック
        ErrorKinds result = reportsDateCheck(reports);
        if (ErrorKinds.CHECK_OK != result) {
            return result;
        }

        // 日付重複チェックを削除し、IDに基づく保存処理を行う
        reports.setDeleteFlg(false);
        LocalDateTime now = LocalDateTime.now();
        reports.setCreatedAt(now);
        reports.setUpdatedAt(now);

        reportsRepository.save(reports);
        return ErrorKinds.SUCCESS;
    }

    // 日付更新
    @Transactional
    public ErrorKinds update(Reports reports, LocalDate newReportDate) {
        if (newReportDate != null) {
            reports.setReportDate(newReportDate); // StringではなくLocalDateに変更
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
        if (reports.getReportDate() == null) {
            return ErrorKinds.DATE_FORMAT_ERROR; // 日付フォーマットエラー
        }
        return ErrorKinds.CHECK_OK;
    }

    // 日報削除
    @Transactional
    public ErrorKinds delete(Long id, UserDetail userDetail) {
        Optional<Reports> reportsOpt = findById(id);
        if (!reportsOpt.isPresent()) {
            return ErrorKinds.DATECHECK_ERROR;
        }

        Reports reports = reportsOpt.get();

        // userDetail.getReports()のチェックを修正
        if (userDetail.getEmployee().getReportList().contains(reports)) {
            return ErrorKinds.LOGINCHECK_ERROR;
        }

        LocalDateTime now = LocalDateTime.now();
        reports.setUpdatedAt(now);
        reports.setDeleteFlg(true);
        reportsRepository.save(reports);

        return ErrorKinds.SUCCESS;
    }

    // 日報取得（IDで取得する）
    public Optional<Reports> findById(Long id) {
        return reportsRepository.findById(id);
    }

    // 日報一覧表示
    public List<Reports> findAll() {
        return reportsRepository.findAll();
    }

    // 現在の日報を取得（IDで取得）
    public Reports findCurrentReports(Long id) {
        Optional<Reports> reportsOpt = findById(id);
        if (!reportsOpt.isPresent()) {
            throw new RuntimeException("日報が見つかりません");
        }
        return reportsOpt.get();
    }
}
