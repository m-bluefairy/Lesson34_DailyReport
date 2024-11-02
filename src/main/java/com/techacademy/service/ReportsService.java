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
import com.techacademy.entity.Employee;
import com.techacademy.entity.Reports;
import com.techacademy.repository.ReportsRepository;
import com.techacademy.repository.EmployeeRepository;

@Service
public class ReportsService {

    private final ReportsRepository reportsRepository;
    private final EmployeeRepository employeeRepository;

    @Autowired
    public ReportsService(ReportsRepository reportsRepository, EmployeeRepository employeeRepository) {
        this.reportsRepository = reportsRepository;
        this.employeeRepository = employeeRepository;
    }

    // 日報保存
    @Transactional
    public ErrorKinds save(Reports reports, String employeeCode) {
        // 日付チェック
        ErrorKinds result = reportsDateCheck(reports);
        if (ErrorKinds.CHECK_OK != result) {
            logErrors(result);
            return result;
        }

        // 日報に必要な情報を設定
        reports.setEmployeeCode(employeeCode);
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
            reports.setReportDate(newReportDate);
            ErrorKinds result = reportsDateCheck(reports);
            if (ErrorKinds.CHECK_OK != result) {
                logErrors(result);
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
            return ErrorKinds.DATE_FORMAT_ERROR;
        }
        return ErrorKinds.CHECK_OK;
    }

    // エラーログの出力
    private void logErrors(ErrorKinds error) {
        switch (error) {
            case DATE_FORMAT_ERROR:
                System.out.println("エラー: 日付が未入力です。");
                break;
            default:
                break;
        }
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

    // 従業員削除処理に紐づく日報削除を追加
    @Transactional
    public ErrorKinds deleteReportsByEmployee(String employeeCode) {
        // 従業員情報を取得
        Employee employee = employeeRepository.findByCode(employeeCode);
        if (employee == null) {
            return ErrorKinds.NOT_FOUND; // 従業員が見つからない場合
        }

        // 従業員に紐づく日報情報を取得
        List<Reports> reportsList = reportsRepository.findByEmployee(employee);

        // 従業員に紐づく日報情報を削除
        for (Reports report : reportsList) {
            reportsRepository.delete(report);
        }

        // 従業員自体を削除する場合
        employeeRepository.delete(employee);

        return ErrorKinds.SUCCESS; // 成功時のレスポンス
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

    // 従業員コードに基づいて日報を取得
    public List<Reports> findByEmployeeCode(String employeeCode) {
        return reportsRepository.findByEmployeeCode(employeeCode);
    }

    // 従業員コードに基づいて日報の存在をチェック
    public boolean existsByEmployeeCode(String employeeCode) {
        return reportsRepository.existsByEmployeeCode(employeeCode);
    }

    // 従業員コードと日付に基づいて日報の存在をチェック
    public boolean existsByEmployeeCodeAndReportDate(String employeeCode, LocalDate reportDate) {
        return reportsRepository.existsByEmployeeCodeAndReportDate(employeeCode, reportDate);
    }

    // 従業員に関連する日報を取得するメソッドを追加
    public List<Reports> findByEmployee(Employee employee) {
        return reportsRepository.findByEmployee(employee);
    }
}