package com.techacademy.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
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
    public ErrorKinds save(Reports reports, String employeeCode) { // employeeCodeを引数として追加
        // 日付チェック
        ErrorKinds result = reportsDateCheck(reports);
        if (ErrorKinds.CHECK_OK != result) {
            return result;
        }

        // 日報に必要な情報を設定
        reports.setEmployeeCode(employeeCode); // 社員番号を設定
        reports.setDeleteFlg(false); // 削除フラグをfalseに設定
        LocalDateTime now = LocalDateTime.now();
        reports.setCreatedAt(now); // 登録日時を設定
        reports.setUpdatedAt(now); // 更新日時を設定

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
    public void deleteReport(Long id) throws DataIntegrityViolationException {
        if (reportsRepository.existsById(id)) {
            reportsRepository.deleteById(id);
        } else {
            throw new DataIntegrityViolationException("指定された日報が見つかりません。");
        }
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
