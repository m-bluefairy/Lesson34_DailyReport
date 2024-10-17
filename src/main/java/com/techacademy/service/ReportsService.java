package com.techacademy.service;

import java.time.LocalDate;
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

    public Reports findByCode(String reportsDate) {
        LocalDate date = LocalDate.parse(reportsDate);
        // 文字列をLocalDateに変換
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
        Reports existingReport = findByDate(reports.getReportsDate());
        if (existingReport != null) {
            return ErrorKinds.DUPLICATE_ERROR;
        }

        reports.setDeleteFlg(false);
        LocalDateTime now = LocalDateTime.now();
        reports.setCreatedAt(now);
        reports.setUpdatedAt(now);

        reportsRepository.save(reports);
        return ErrorKinds.SUCCESS;

        // 日時の設定
        LocalDateTime now = LocalDateTime.now();
        reports.setUpdatedAt(now);

        reportsRepository.save(reports);
        return ErrorKinds.SUCCESS;
    }

    // ----- 追加:ここから -----
    //日付更新（追加）を行なう
    @Transactional
    public ErrorKinds update(Reports reports, String newDate) {

    	// 新しい日時が空でない場合のみチェックを実施
    	if (!newDate.isEmpty()) {

    	// newDateをLocalDateに変換してreportsにセット
    	LocalDate newReportDate = LocalDate.parse(newDate);
    	reports.setReportsDate(newReportDate);

    	// 日付チェック
        ErrorKinds result = reportsDateCheck(reports);
        if (ErrorKinds.CHECK_OK != result) {
        return result;
        }

       // 日付重複チェック
       Reports existingReport = findByDate(newReportDate);
       if (existingReport != null && !existingReport.getId().equals(reports.getId())) {
    	   return ErrorKinds.DUPLICATE_ERROR;
    	}
       // 更新日時の設定
      reports.setUpdatedAt(LocalDateTime.now());

       // 更新をリポジトリに保存
        reportsRepository.save(reports);
        }

        return ErrorKinds.SUCCESS; // 成功
    }

    // 日付削除処理
    @Transactional
    public ErrorKinds delete(String reportsDate, UserDetail userDetail) {
        LocalDate date = LocalDate.parse(reportsDate);

        Reports report = reportsRepository.findByReportDate(date);
        if (report == null) {
        	// レポートが見つからない場合
        	return ErrorKinds.NOT_FOUND;
        }

        report.setDeleteFlg(true);
        reportsRepository.save(report);
        // 成功
        return ErrorKinds.SUCCESS;
    }
    // 日報の日付でレポートを検索する
    public Reports findByReportsDate(String reportsDate) {
        LocalDate date = LocalDate.parse(reportsDate); // 文字列をLocalDateに変換
        return reportsRepository.findByReportDate(date);
    }

    // ----- 追加:ここまで -----


	private ErrorKinds reportsDateCheck(Reports reports) {
		// 日付チェックのロジックを実装
		return ErrorKinds.CHECK_OK;
	}

	public List<Reports> findAll() {
		return reportsRepository.findAll();
	}

}