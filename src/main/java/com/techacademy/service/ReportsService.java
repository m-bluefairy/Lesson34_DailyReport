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

    // 日付でレポートを検索するメソッド
    public Reports findByReportDate(String reportDate) {
        LocalDate date = LocalDate.parse(reportDate);
        return reportsRepository.findByReportDate(date);
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
        Reports existingReport =reportsRepository.findByReportDate(reportDate);
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

    // ----- 追加:ここから -----
    //日付更新（追加）
    @Transactional
    public ErrorKinds update(Reports reports, String newDate) {

    	// 新しい日時が空でない場合のみチェックを実施
    	if (!newDate.isEmpty()) {
    		// newDateをLocalDateに変換してreportsにセット
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
        }

        return ErrorKinds.SUCCESS;
    }

    // 日付削除処理
    @Transactional
    public ErrorKinds delete(String reportDate, UserDetail userDetail) {
        LocalDate date = LocalDate.parse(reportDate);
        Reports report = reportsRepository.findByReportDate(date);
        if (report == null) {
        	// レポートが見つからない場合
        	 return ErrorKinds.NOT_FOUND;
        }

        // 削除処理
        report.setDeleteFlg(true);
        reportsRepository.save(report);
        return ErrorKinds.SUCCESS;
    }

    // ----- 追加:ここまで -----

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
        // findByIdで検索
        Optional<Reports> option = reportsRepository.findById(id);
        // 取得できなかった場合はnullを返す
        return option.orElse(null);
    }

}