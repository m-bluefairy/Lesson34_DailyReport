package com.techacademy.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.techacademy.constants.ErrorKinds;
import com.techacademy.constants.ErrorMessage;
import com.techacademy.entity.Reports;
import com.techacademy.entity.Employee;
import com.techacademy.service.ReportsService;
import com.techacademy.service.UserDetail;
import com.techacademy.service.EmployeeService;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Optional;

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
            return create(userDetail, model); // エラーがある場合、再度新規作成画面に戻る
        }

        // ログイン中の従業員情報を取得し、社員番号を設定
        Employee employee = userDetail.getEmployee();
        String employeeCode = employee.getCode(); // 社員番号を取得

        // Reportsエンティティに設定する
        reports.setEmployee(employee); // 従業員オブジェクトを設定（必要に応じて）

        // 日報を保存
        ErrorKinds result = reportsService.save(reports, employeeCode); // 社員番号を渡す

        if (ErrorMessage.contains(result)) {
            model.addAttribute(ErrorMessage.getErrorName(result), ErrorMessage.getErrorValue(result));
            return create(userDetail, model); // エラー発生時、同じ画面に戻る
        }

        return "redirect:/reports"; // 登録成功時に一覧へリダイレクト
    }

    // 日報更新画面を表示
    @GetMapping("/{id}/update")
    public String edit(@PathVariable("id") Long id, @AuthenticationPrincipal UserDetail userDetail, Model model) {
        // Modelに登録
        Optional<Reports> reportOpt = reportsService.findById(id);
        if (!reportOpt.isPresent()) {
            model.addAttribute("errorMessage", "指定された日報が見つかりません。");
            return "error";
        }

        model.addAttribute("reports", reportOpt.get());

        // 従業員情報の取得
        if (userDetail != null && userDetail.getEmployee() != null) {
            String employeeCode = userDetail.getEmployee().getCode();
            Employee employee = employeeService.findCurrentEmployee(employeeCode);
            model.addAttribute("employee", employee);
        }

        return "reports/update";
    }

    // 日報更新処理
    @PostMapping("/{id}/update")
    public String update(@Validated @ModelAttribute Reports reports, BindingResult res, Model model, @AuthenticationPrincipal UserDetail userDetail) {
        if (res.hasErrors()) {
            model.addAttribute("reports", reports);
            return "reports/update"; // エラーがある場合、更新画面に戻る
        }

        // 登録済みの日報データを取得
        Long id = reports.getId();
        Optional<Reports> savedReportsOpt = reportsService.findById(id);

        if (!savedReportsOpt.isPresent()) {
            model.addAttribute("errorMessage", "指定された日報が見つかりません。");
            return "error";
        }

        Reports savedReports = savedReportsOpt.get();

        // 登録済みの日報データにリクエストの項目を設定する
        savedReports.setTitle(reports.getTitle());
        savedReports.setContent(reports.getContent());

        // LocalDate に変換
        if (reports.getReportDate() != null) {
            savedReports.setReportDate(reports.getReportDate());
        }

        // 従業員情報の取得
        if (userDetail != null && userDetail.getEmployee() != null) {
            String employeeCode = userDetail.getEmployee().getCode();
            Employee employee = employeeService.findCurrentEmployee(employeeCode);
            model.addAttribute("employee", employee);
        }

        // 日付のエラー表示
        try {
            ErrorKinds result = reportsService.update(savedReports, reports.getReportDate());

            if (ErrorMessage.contains(result)) {
                model.addAttribute(ErrorMessage.getErrorName(result), ErrorMessage.getErrorValue(result));
                model.addAttribute("reports", savedReports);
                return "reports/update"; // エラー発生時、同じ画面に戻る
            }

        } catch (DataIntegrityViolationException e) {
            model.addAttribute(ErrorMessage.getErrorValue(ErrorKinds.DUPLICATE_EXCEPTION_ERROR));
            model.addAttribute("reports", savedReports); // 失敗時に再表示するために追加
            return "reports/update";
        }

        // 一覧画面にリダイレクト
        return "redirect:/reports";
    }

    // 日報詳細画面
    @GetMapping("/{id}")
    public String showReportDetail(@PathVariable Long id, @RequestParam(value = "date", required = false) String date, Model model) {
        Optional<Reports> reportOpt = reportsService.findById(id);

        // 日報が見つからない場合のエラーハンドリング
        if (!reportOpt.isPresent()) {
            model.addAttribute("errorMessage", "指定された日報が見つかりません。");
            return "error"; // エラーページにリダイレクト
        }

        Reports report = reportOpt.get();
        model.addAttribute("reports", report);

        // 従業員情報の取得
        if (report.getEmployee() != null) {
            Employee employee = employeeService.findCurrentEmployee(report.getEmployee().getCode());
            model.addAttribute("employee", employee);
        } else {
            model.addAttribute("employee", null);
        }

        // 日付を解析
        if (date != null) {
            try {
                LocalDate parsedDate = LocalDate.parse(date);
                model.addAttribute("parsedDate", parsedDate);
            } catch (DateTimeParseException e) {
                model.addAttribute("errorMessage", "Invalid date format. Please use yyyy-MM-dd.");
            }
        }

        return "reports/detail";
    }

    // 日報削除処理
    @PostMapping("/{id}/delete")
    public String deleteReport(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            reportsService.deleteReport(id); // エラー解消
            redirectAttributes.addFlashAttribute("message", "日報が削除されました。");
        } catch (DataIntegrityViolationException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "削除に失敗しました。関連データが存在する可能性があります。");
        }

        return "redirect:/reports"; // 削除後のリダイレクト先
    }
}
