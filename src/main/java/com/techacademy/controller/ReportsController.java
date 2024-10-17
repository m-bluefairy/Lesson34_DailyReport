package com.techacademy.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.techacademy.constants.ErrorKinds;
import com.techacademy.constants.ErrorMessage;

import com.techacademy.entity.reports;
import com.techacademy.service.ReportsService;
import com.techacademy.service.UserDetail;

@Controller
@RequestMapping("reports")
public class ReportsController {

    private final ReportsService reportsService;

    @Autowired
    public ReportsController(ReportsService reportsService) {
        this.reportsService = reportsService;
    }

    //日報一覧画面
    @GetMapping
    public String list(Model model) {

        model.addAttribute("listSize", ((Object) reportsService.findAll()));
        model.addAttribute("rList", reportsService.findAll());

        return "reports/list";
    }

    // 日報詳細画面
    @GetMapping(value = "/{reportsDate}/")
    public String detail(@PathVariable String reportsDate, Model model) {

        model.addAttribute("reports", reportsService.findByCode(reportsDate));
        return "reports/detail";
    }

    // 日報新規登録画面
    @GetMapping(value = "/add")
    public String create() {
        return "reports/new";
    }

    // 日報新規登録処理
    @PostMapping(value = "/add")
    public String add(@Validated Reports reports, Model model) {

    	if ("".equals(reports.getReportsDate())) {
            // 日付が空白だった場合
            model.addAttribute(ErrorMessage.getErrorName(ErrorKinds.BLANK_ERROR),
                    ErrorMessage.getErrorValue(ErrorKinds.BLANK_ERROR));

            return create(reports);
        }

        try {
        	ErrorKinds result = reportsService.save(reports);

            if (result != ErrorKinds.SUCCESS) {
                model.addAttribute(ErrorMessage.getErrorName(result), ErrorMessage.getErrorValue(result));
                return create();
            }
        }

        catch (DataIntegrityViolationException e) {
            model.addAttribute(ErrorMessage.getErrorName(ErrorKinds.DUPLICATE_EXCEPTION_ERROR),
                    ErrorMessage.getErrorValue(ErrorKinds.DUPLICATE_EXCEPTION_ERROR));
            return create();
        }

        return "redirect:/reports";
        }

        // 日付削除処理
        @PostMapping(value = "/{reportsDate}/delete")
        public String delete(@PathVariable String reportsDate, @AuthenticationPrincipal UserDetail userDetail, Model model) {

            ErrorKinds result = reportsService.delete(reportsDate, userDetail);

            if (ErrorMessage.contains(result)) {
                model.addAttribute(ErrorMessage.getErrorReportsDate(result), ErrorMessage.getErrorValue(result));
                model.addAttribute("reports", reportsService.findByReportsDate(reportsDate));
                return detail(reportsDate, model);
            }

            return "redirect:/reports";
    }

}
