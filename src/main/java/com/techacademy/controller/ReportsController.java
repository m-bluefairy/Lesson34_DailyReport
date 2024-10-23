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

import java.util.List;

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

        // 現在の従業員情報を取得してモデルに追加
        if (userDetail != null && userDetail.getEmployee() != null) {
            String employeeCode = userDetail.getEmployee().getCode();
            Employee employee = employeeService.findCurrentEmployee(employeeCode);
            model.addAttribute("employee", employee);
        } else {
            return handleError(model, "従業員情報が不正です。");
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
            reports.setEmployeeCode(userDetail.getEmployee().getCode());
            ErrorKinds result = reportsService.save(reports);
            if (result != ErrorKinds.SUCCESS) {
                model.addAttribute(ErrorMessage.getErrorName(result), ErrorMessage.getErrorValue(result));
                return create(userDetail, model);
            }
        } catch (DataIntegrityViolationException e) {
            model.addAttribute(ErrorMessage.getErrorName(ErrorKinds.DUPLICATE_EXCEPTION_ERROR),
                    ErrorMessage.getErrorValue(ErrorKinds.DUPLICATE_EXCEPTION_ERROR));
            return create(userDetail, model);
        } catch (Exception e) {
            return handleError(model, "予期しないエラーが発生しました。");
        }

        return "redirect:/reports";
    }

    // 日報詳細画面
    @GetMapping(value = "/{reportDate}")
    public String detail(@PathVariable String reportDate, Model model) {
        if (!isValidDate(reportDate)) {
            return handleError(model, "無効な日付形式です。正しい形式で入力してください。例: YYYY-MM-DD");
        }

        List<Reports> reports = reportsService.findByReportDate(reportDate);
        if (reports.isEmpty()) {
            return handleError(model, "指定された日報は存在しません。");
        }

        model.addAttribute("reports", reports.get(0));
        return "reports/detail";
    }

    // 日付形式を確認するヘルパーメソッド
    private boolean isValidDate(String date) {
        return date.matches("\\d{4}-\\d{2}-\\d{2}"); // YYYY-MM-DD形式をチェック
    }

    // エラーハンドリングメソッド
    private String handleError(Model model, String errorMessage) {
        model.addAttribute("errorMessage", errorMessage);
        return "reports/error"; // エラーページへリダイレクト
    }

    // 日付削除処理
    @PostMapping(value = "/{reportDate}/delete")
    public String delete(@PathVariable String reportDate, @AuthenticationPrincipal UserDetail userDetail, Model model) {
        ErrorKinds result = reportsService.delete(reportDate, userDetail);

        if (ErrorMessage.contains(result)) {
            model.addAttribute(ErrorMessage.getErrorReportsDate(result), ErrorMessage.getErrorValue(result));
            return detail(reportDate, model);
        }

        return "redirect:/reports";
    }
}
