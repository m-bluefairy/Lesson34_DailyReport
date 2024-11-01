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
    public String create(@AuthenticationPrincipal UserDetail userDetail, Model model, @ModelAttribute Reports reports) {
        model.addAttribute("reports", reports); // エラーのあるreportsオブジェクトをモデルに追加
        model.addAttribute("title", new Reports());

        // 現在の従業員情報を取得してモデルに追加
        if (userDetail != null && userDetail.getCurrentEmployee() != null) {
            String employeeCode = userDetail.getEmployeeCode();
            Employee employee = employeeService.findCurrentEmployee(employeeCode);
            model.addAttribute("employee", employee);
        }
        return "reports/new";
    }

    // 日報新規登録処理
    @PostMapping(value = "/add")
    public String add(@Validated @ModelAttribute Reports reports, BindingResult bindingResult, Model model, @AuthenticationPrincipal UserDetail userDetail) {
        if (bindingResult.hasErrors()) {
            return create(userDetail, model, reports); // エラーがある場合、エラーのあるreportsを渡して再表示
        }

        // ユーザーから従業員情報を取得
        if (userDetail != null && userDetail.getCurrentEmployee() != null) {
            String employeeCode = userDetail.getEmployeeCode(); // 従業員コードを取得
            reports.setEmployeeCode(employeeCode); // Reportsオブジェクトに従業員コードを設定
        } else {
            model.addAttribute("errorMessage", "従業員情報が取得できません。");
            return create(userDetail, model, reports); // 従業員情報が取得できない場合、エラーメッセージを表示
        }

        ErrorKinds saveResult = reportsService.save(reports, reports.getEmployeeCode()); // 新規日報を保存

        if (saveResult != ErrorKinds.SUCCESS) {
            model.addAttribute("errorMessage", saveResult.getMessage()); // エラーメッセージをモデルに追加
            return create(userDetail, model, reports); // エラーがあった場合、再度新規作成画面に戻る
        }

        // 新規登録成功時に日報一覧画面へリダイレクト
        return "redirect:/reports"; // 成功した場合はリダイレクト
    }

    // 日報更新画面を表示
    @GetMapping("/{id}/update")
    public String edit(@PathVariable("id") Long id, @AuthenticationPrincipal UserDetail userDetail, Model model) {
        Optional<Reports> reportOpt = reportsService.findById(id);
        if (!reportOpt.isPresent()) {
            model.addAttribute("errorMessage", "指定された日報が見つかりません。");
            return "error";
        }

        Reports report = reportOpt.get();
        model.addAttribute("reports", report);

        // ログインしているユーザーの従業員情報を取得
        if (userDetail != null && userDetail.getCurrentEmployee() != null) {
            Employee employee = userDetail.getCurrentEmployee();
            model.addAttribute("employee", employee);
        } else {
            model.addAttribute("employee", null);  // 従業員情報がない場合
        }

        return "reports/update";
    }

    // 日報更新処理
    @PostMapping("/{id}/update")
    public String update(@Validated @ModelAttribute Reports reports, BindingResult res, Model model, @AuthenticationPrincipal UserDetail userDetail) {
        if (res.hasErrors()) {
            model.addAttribute("reports", reports);
            model.addAttribute("org.springframework.validation.BindingResult.reports", res); // BindingResultを渡す
            return "reports/update"; // エラーがある場合は再度フォームに戻る
        }

        Long id = reports.getId();
        Optional<Reports> savedReportsOpt = reportsService.findById(id);

        if (!savedReportsOpt.isPresent()) {
            model.addAttribute("errorMessage", "指定された日報が見つかりません。");
            return "error";
        }

        Reports savedReports = savedReportsOpt.get();
        savedReports.setTitle(reports.getTitle());
        savedReports.setContent(reports.getContent());

        if (reports.getReportDate() != null) {
            savedReports.setReportDate(reports.getReportDate());
        }

        // 従業員情報の取得
        if (userDetail != null && userDetail.getCurrentEmployee() != null) {
            String employeeCode = userDetail.getEmployeeCode();
            Employee employee = employeeService.findCurrentEmployee(employeeCode);
            model.addAttribute("employee", employee);
        }

        // 日付のエラー表示
        try {
            ErrorKinds result = reportsService.update(savedReports, reports.getReportDate());

            if (ErrorMessage.contains(result)) {
                model.addAttribute(ErrorMessage.getErrorName(result), ErrorMessage.getErrorValue(result));
                model.addAttribute("reports", savedReports);
                return edit(savedReports.getId(), userDetail, model); // エラー発生時、同じ画面に戻る
            }

        } catch (DataIntegrityViolationException e) {
            model.addAttribute(ErrorMessage.getErrorValue(ErrorKinds.DUPLICATE_EXCEPTION_ERROR));
            model.addAttribute("reports", savedReports); // 失敗時に再表示するために追加
            return edit(savedReports.getId(), userDetail, model);
        }

        return "redirect:/reports";
    }

    // 日報詳細画面
    @GetMapping("/{id}/detail")
    public String showReportDetail(@PathVariable Long id, @RequestParam(value = "date", required = false) String date, Model model) {
        Optional<Reports> reportOpt = reportsService.findById(id);

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
            reportsService.deleteReport(id);
            redirectAttributes.addFlashAttribute("message", "日報が削除されました。");
        } catch (DataIntegrityViolationException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "削除に失敗しました。関連データが存在する可能性があります。");
        }

        return "redirect:/reports"; // 削除後のリダイレクト先
    }

}
