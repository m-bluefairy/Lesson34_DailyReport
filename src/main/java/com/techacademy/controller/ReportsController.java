package com.techacademy.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.prepost.PreAuthorize; // 追加
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
    @PreAuthorize("hasAnyAuthority('USER', 'ADMIN')") // 一般ユーザーと管理者がアクセス可
    public String list(@AuthenticationPrincipal UserDetail userDetail, Model model) {
        if (userDetail == null || userDetail.getEmployee() == null) {
            model.addAttribute("errorMessage", "従業員情報が取得できません。");
            return "error";
        }

        // 管理者の場合は全従業員の日報を表示
        if (userDetail.isAdmin()) {
            model.addAttribute("listSize", reportsService.findAll().size());
            model.addAttribute("reportsList", reportsService.findAll());
        } else {
            // 一般従業員の場合は自身の日報のみを表示
            String employeeCode = userDetail.getEmployeeCode();
            model.addAttribute("listSize", reportsService.findByEmployeeCode(employeeCode).size());
            model.addAttribute("reportsList", reportsService.findByEmployeeCode(employeeCode));
        }

        return "reports/list";
    }

    // 日報新規登録画面
    @GetMapping(value = "/add")
    @PreAuthorize("hasAuthority('USER')") // 一般ユーザーがアクセス可
    public String create(@AuthenticationPrincipal UserDetail userDetail, Model model, @ModelAttribute Reports reports) {
        model.addAttribute("reports", reports);
        model.addAttribute("title", new Reports());

        if (userDetail != null && userDetail.getEmployee() != null) {
            String employeeCode = userDetail.getEmployeeCode();
            Employee employee = employeeService.findCurrentEmployee(employeeCode);
            model.addAttribute("employee", employee);
        }
        return "reports/new";
    }

    // 日報新規登録処理
    @PostMapping(value = "/add")
    @PreAuthorize("hasAuthority('USER')") // 一般ユーザーがアクセス可
    public String add(@Validated @ModelAttribute Reports reports, BindingResult bindingResult, Model model, @AuthenticationPrincipal UserDetail userDetail) {
        if (bindingResult.hasErrors()) {
            return create(userDetail, model, reports);
        }

        if (userDetail != null && userDetail.getEmployee() != null) {
            String employeeCode = userDetail.getEmployeeCode();
            reports.setEmployeeCode(employeeCode);
        } else {
            model.addAttribute("errorMessage", "従業員情報が取得できません。");
            return create(userDetail, model, reports);
        }

        ErrorKinds saveResult = reportsService.save(reports, reports.getEmployeeCode());

        if (saveResult != ErrorKinds.SUCCESS) {
            model.addAttribute("errorMessage", saveResult.getMessage());
            return create(userDetail, model, reports);
        }

        return "redirect:/reports";
    }

    // 日報更新画面を表示
    @GetMapping("/{id}/update")
    @PreAuthorize("hasAuthority('USER')") // 一般ユーザーがアクセス可
    public String edit(@PathVariable("id") Long id, @AuthenticationPrincipal UserDetail userDetail, Model model) {
        Optional<Reports> reportOpt = reportsService.findById(id);
        if (!reportOpt.isPresent()) {
            model.addAttribute("errorMessage", "指定された日報が見つかりません。");
            return "error";
        }

        Reports report = reportOpt.get();
        model.addAttribute("reports", report);

        if (userDetail != null && userDetail.getEmployee() != null) {
            Employee employee = userDetail.getEmployee();
            model.addAttribute("employee", employee);
        } else {
            model.addAttribute("employee", null);
        }

        return "reports/update";
    }

    // 日報更新処理
    @PostMapping("/{id}/update")
    @PreAuthorize("hasAuthority('USER')") // 一般ユーザーがアクセス可
    public String update(@PathVariable("id") Long id,
                         @Validated @ModelAttribute Reports reports,
                         BindingResult res,
                         Model model,
                         @AuthenticationPrincipal UserDetail userDetail) {
        if (res.hasErrors()) {
            Reports existingReport = reportsService.findById(id).orElse(null);
            if (existingReport != null) {
                reports.setEmployee(existingReport.getEmployee());
            }
            model.addAttribute("reports", reports);
            model.addAttribute("org.springframework.validation.BindingResult.reports", res);
            return "reports/update";
        }

        Optional<Reports> savedReportsOpt = reportsService.findById(id);
        if (!savedReportsOpt.isPresent()) {
            model.addAttribute("errorMessage", "指定された日報が見つかりません。");
            return "error";
        }

        Reports savedReports = savedReportsOpt.get();
        savedReports.setTitle(reports.getTitle());
        savedReports.setContent(reports.getContent());

        if (userDetail != null && userDetail.getEmployee() != null) {
            String employeeCode = userDetail.getEmployeeCode();
            Employee employee = employeeService.findCurrentEmployee(employeeCode);
            savedReports.setEmployee(employee);
            model.addAttribute("employee", employee);
        }

        if (reports.getReportDate() != null) {
            savedReports.setReportDate(reports.getReportDate());
        }

        try {
            ErrorKinds result = reportsService.update(savedReports, reports.getReportDate());

            if (ErrorMessage.contains(result)) {
                model.addAttribute(ErrorMessage.getErrorName(result), ErrorMessage.getErrorValue(result));
                model.addAttribute("reports", savedReports);
                return edit(savedReports.getId(), userDetail, model);
            }

        } catch (DataIntegrityViolationException e) {
            model.addAttribute(ErrorMessage.getErrorValue(ErrorKinds.DUPLICATE_EXCEPTION_ERROR));
            model.addAttribute("reports", savedReports);
            return edit(savedReports.getId(), userDetail, model);
        }

        return "redirect:/reports";
    }

    // 日報詳細画面
    @GetMapping("/{id}/detail")
    @PreAuthorize("hasAnyAuthority('USER', 'ADMIN')") // 一般ユーザーと管理者がアクセス可
    public String showReportDetail(@PathVariable Long id, @RequestParam(value = "date", required = false) String date, Model model) {
        Optional<Reports> reportOpt = reportsService.findById(id);

        if (!reportOpt.isPresent()) {
            model.addAttribute("errorMessage", "指定された日報が見つかりません。");
            return "error";
        }

        Reports report = reportOpt.get();
        model.addAttribute("reports", report);

        if (report.getEmployee() != null) {
            Employee employee = employeeService.findCurrentEmployee(report.getEmployee().getCode());
            model.addAttribute("employee", employee);
        } else {
            model.addAttribute("employee", null);
        }

        if (date != null) {
            try {
                LocalDate parsedDate = LocalDate.parse(date);
                model.addAttribute("parsedDate", parsedDate);
            } catch (DateTimeParseException e) {
                model.addAttribute("errorMessage", "日付の形式が無効です。 yyyy-MM-dd を使用してください。");
            }
        }

        return "reports/detail";
    }

    // 日報削除処理
    @PostMapping("/{id}/delete")
    @PreAuthorize("hasAuthority('ADMIN')") // 管理者のみアクセス可
    public String deleteReport(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            reportsService.deleteReport(id);
            redirectAttributes.addFlashAttribute("message", "日報が削除されました。");
        } catch (DataIntegrityViolationException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "削除に失敗しました。関連データが存在する可能性があります。");
        }

        return "redirect:/reports";
    }
}