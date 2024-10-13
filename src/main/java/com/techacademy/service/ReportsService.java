package com.techacademy.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.techacademy.repository.ReportsRepository;

@Service
public class ReportsService {

    @SuppressWarnings("unused")
	private final ReportsRepository reportsRepository;

    @Autowired
    public ReportsService(ReportsRepository reportsRepository) {
        this.reportsRepository = reportsRepository;

    }

	public Object findAll() {
		// TODO 自動生成されたメソッド・スタブ
		return null;
	}

}