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
        model.addAttribute("listSize", reportsService.findAll().size());
        model.addAttribute("rList", reportsService.findAll());
        return "reports/list";
    }

    // 日報詳細画面
    @GetMapping(value = "/{reportDate}/")
    public String detail(@PathVariable String reportDate, Model model) {
        model.addAttribute("reports", reportsService.findByReportDate(reportDate));
        return "reports/detail";
    }

    // 日報新規登録画面
    @GetMapping(value = "/add")
    public String create(Model model) {
    	model.addAttribute("reports", new Reports());
        return "reports/new";
    }

    // 日報新規登録処理
    @PostMapping(value = "/add")
    public String add(@Validated @ModelAttribute Reports reports, BindingResult bindingResult, Model model) {
    	if (bindingResult.hasErrors()) {
    		// バリデーションエラーがあれば、エラーメッセージを表示
            return create(model);
        }

        try {
        	ErrorKinds result = reportsService.save(reports);
            if (result != ErrorKinds.SUCCESS) {
                model.addAttribute(ErrorMessage.getErrorName(result), ErrorMessage.getErrorValue(result));
                return create(model);
            }
        }
        catch (DataIntegrityViolationException e) {
            model.addAttribute(ErrorMessage.getErrorName(ErrorKinds.DUPLICATE_EXCEPTION_ERROR),
            		ErrorMessage.getErrorValue(ErrorKinds.DUPLICATE_EXCEPTION_ERROR));
            return create(model);
        }

        return "redirect:/reports";
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
