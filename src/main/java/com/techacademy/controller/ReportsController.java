package com.techacademy.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

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
    // 日報新規登録画面
    @GetMapping(value = "/add")
    public String create() {
        return "reports/new";
    }
    // 日報新規登録処理
    @PostMapping(value = "/add")
    public String add(@Validated Reports reports, Model model) {

        try {
        	ErrorKinds result = reportsService.save(reports);

            if (result != ErrorKinds.SUCCESS) {
                model.addAttribute(ErrorMessage.getErrorName(result), ErrorMessage.getErrorValue(result));
                return create();
            }

        } catch (DataIntegrityViolationException e) {
            model.addAttribute(ErrorMessage.getErrorName(ErrorKinds.DUPLICATE_EXCEPTION_ERROR),
                    ErrorMessage.getErrorValue(ErrorKinds.DUPLICATE_EXCEPTION_ERROR));
            return create();
        }

        return "redirect:/reports";
    }

}
