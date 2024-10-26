package com.techacademy.service;

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

     // 日付重複チェック
        if (findByReportDate(Reports.getReportDate()) != null) {
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
    public ErrorKinds update(Reports reports, String newReportDate) {
        if (newReportDate.isEmpty()) {
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

    private ErrorKinds reportsDateCheck(Reports reports) {
		// TODO 自動生成されたメソッド・スタブ
		return null;
	}

	// 日報削除
    @Transactional
    public ErrorKinds delete(String reportDate, UserDetail userDetail) {
        if (reportDate.equals(userDetail.getReports().getReportList())) {
            return ErrorKinds.LOGINCHECK_ERROR;
        }
        Reports reports = findByReportDate(reportDate);
        LocalDateTime now = LocalDateTime.now();
        reports.setUpdatedAt(now);
        reports.setDeleteFlg(true);

        return ErrorKinds.SUCCESS;
    }

    public Reports findByReportDate(String reportDate) {
		// TODO 自動生成されたメソッド・スタブ
		return null;
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

    // 現在の日報を取得するメソッド
    public Reports findCurrenReports(String reportDate) {
        return findByReportDate(reportDate);
    }
}
