package com.techacademy.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.techacademy.constants.ErrorKinds;
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
        if (findByDate(reports.getReportsDate()) != null) {
            return ErrorKinds.DUPLICATE_ERROR;
        }

        reports.setDeleteFlg(false);

        LocalDateTime now = LocalDateTime.now();
        reports.setCreatedAt(now);
        reports.setUpdatedAt(now);

        reportsRepository.save(reports);
        return ErrorKinds.SUCCESS;
    }


	public List<Reports> findAll() {
		// TODO 自動生成されたメソッド・スタブ
		return reportsRepository.findAll();
	}

}