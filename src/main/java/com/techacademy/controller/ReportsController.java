package com.techacademy.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.techacademy.constants.ErrorKinds;
import com.techacademy.constants.ErrorMessage;
import com.techacademy.entity.Reports;
import com.techacademy.entity.Employee;
import com.techacademy.service.ReportsService;
import com.techacademy.service.UserDetail;
import com.techacademy.service.EmployeeService;

import java.util.List; // これを追加

@Controller
@RequestMapping("reports")
public class ReportsController {

    private final ReportsService reportsService;
    private final EmployeeService employeeService;

    @Autowired
    public ReportsController(ReportsService reportsService, EmployeeService employeeService) {
        this.reportsService = reportsService;
        this.employeeService = employeeService;
    }

    // 日報一覧画面
    @GetMapping
    public String list(Model model) {
        model.addAttribute("listSize", reportsService.findAll().size());
        model.addAttribute("reportsList", reportsService.findAll());
        return "reports/list";
    }

    // 日報新規登録画面
    @GetMapping(value = "/add")
    public String create(@AuthenticationPrincipal UserDetail userDetail, Model model) {
        model.addAttribute("reports", new Reports());
        model.addAttribute("title", new Reports());

        // 現在の従業員情報を取得してモデルに追加
        if (userDetail != null && userDetail.getEmployee() != null) {
            String employeeCode = userDetail.getEmployee().getCode();
            Employee employee = employeeService.findCurrentEmployee(employeeCode);
            model.addAttribute("employee", employee);
        }
        return "reports/new";
    }

    // 日報新規登録処理
    @PostMapping(value = "/add")
    public String add(@Validated @ModelAttribute Reports reports, BindingResult bindingResult, Model model, @AuthenticationPrincipal UserDetail userDetail) {
        if (bindingResult.hasErrors()) {
            return create(userDetail, model);
        }

        try {
            ErrorKinds result = reportsService.save(reports);

            if (ErrorMessage.contains(result)) {
                model.addAttribute(ErrorMessage.getErrorName(result), ErrorMessage.getErrorValue(result));
                return create(userDetail, model);
            }

        } catch (DataIntegrityViolationException e) {
            model.addAttribute(ErrorMessage.getErrorName(ErrorKinds.DUPLICATE_EXCEPTION_ERROR),
                ErrorMessage.getErrorValue(ErrorKinds.DUPLICATE_EXCEPTION_ERROR));
            return create(userDetail, model);
        }

        return "redirect:/reports";
    }

    // 日報更新画面を表示
    @GetMapping("/{reportDate}/update")
    public String edit(@PathVariable("reportDate") String reportDate, Model model) {
        // Modelに登録
        if (reportDate == null) {
            model.addAttribute("reports", new Reports()); // 新規作成時の処理
        } else {
            model.addAttribute("reports", reportsService.findByReportDate(reportDate).get(0)); // 一つ目のレポートを取得
        }
        return "reports/update";
    }

    @PostMapping("/{reportDate}/update")
    public String update(@Validated Reports reports, BindingResult res, Model model) {
        if (res.hasErrors()) {
            // エラーあり
            model.addAttribute("reports", reports);
            return "reports/update";
        }

        // 登録済みの日報データを取得
        String reportDate = reports.getReportDate();
        Reports savedReports = reportsService.findByReportDate(reportDate).get(0); // 一つ目のレポートを取得

        // 登録済みの日報データにリクエストの項目を設定する
        savedReports.setTitle(reports.getTitle());
        savedReports.setContent(reports.getContent());

        // 日付のエラー表示
        try {
            ErrorKinds result = reportsService.update(savedReports, reportDate);

            if (ErrorMessage.contains(result)) {
                model.addAttribute(ErrorMessage.getErrorName(result), ErrorMessage.getErrorValue(result));
                model.addAttribute("reports", savedReports);
                return "employees/update";
            }

        } catch (DataIntegrityViolationException e) {
            model.addAttribute(ErrorMessage.getErrorValue(ErrorKinds.DUPLICATE_EXCEPTION_ERROR));
            return "reports/update";
        }

        // 一覧画面にリダイレクト
        return "redirect:/reports";
    }

    // 日報詳細画面
    @GetMapping("/{reportDate}")
    public String showReportDetail(@PathVariable String reportDate, Model model) {
        List<Reports> reportsList = reportsService.findByReportDate(reportDate);

        // 日報が見つからない場合のエラーハンドリング
        if (reportsList.isEmpty()) {
            model.addAttribute("errorMessage", "指定された日報が見つかりません。");
            return "error"; // エラーページにリダイレクト
        }

        Reports report = reportsList.get(0); // 一つ目のレポートを取得
        model.addAttribute("reports", report);

        // 従業員情報の取得
        if (report.getEmployee() != null) {
            Employee employee = employeeService.findCurrentEmployee(report.getEmployee().getCode());
            model.addAttribute("employee", employee);
        } else {
            model.addAttribute("employee", null);
        }

        return "reports/detail";
    }

}