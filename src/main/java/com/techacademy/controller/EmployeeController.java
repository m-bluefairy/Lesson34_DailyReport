package com.techacademy.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.techacademy.constants.ErrorKinds;
import com.techacademy.constants.ErrorMessage;

import com.techacademy.entity.Employee;
import com.techacademy.service.EmployeeService;
import com.techacademy.service.UserDetail;

@Controller
@RequestMapping("employees")
public class EmployeeController {

    private final EmployeeService employeeService;

    @Autowired
    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    // 従業員一覧画面
    @GetMapping
    public String list(Model model) {
        model.addAttribute("listSize", employeeService.findAll().size());
        model.addAttribute("employeeList", employeeService.findAll());
        return "employees/list";
    }

    // 従業員詳細画面
    @GetMapping(value = "/{code}/")
    public String detail(@PathVariable String code, Model model) {
        model.addAttribute("employee", employeeService.findByCode(code));
        return "employees/detail";
    }

    // 従業員新規登録画面
    @GetMapping(value = "/add")
    public String create(@ModelAttribute Employee employee) {
        return "employees/new";
    }

    // 従業員新規登録処理
    @PostMapping(value = "/add")
    public String add(@Validated Employee employee, BindingResult res, Model model) {
        if ("".equals(employee.getPassword())) {
            model.addAttribute(ErrorMessage.getErrorName(ErrorKinds.BLANK_ERROR),
                    ErrorMessage.getErrorValue(ErrorKinds.BLANK_ERROR));
            return create(employee);
        }

        try {
            ErrorKinds result = employeeService.save(employee);
            if (ErrorMessage.contains(result)) {
                model.addAttribute(ErrorMessage.getErrorName(result), ErrorMessage.getErrorValue(result));
                return create(employee);
            }
        } catch (DataIntegrityViolationException e) {
            model.addAttribute(ErrorMessage.getErrorName(ErrorKinds.DUPLICATE_EXCEPTION_ERROR),
                    ErrorMessage.getErrorValue(ErrorKinds.DUPLICATE_EXCEPTION_ERROR));
            return create(employee);
        }

        return "redirect:/employees";
    }

    // ----- 追加:ここから -----
    /** 従業員更新画面を表示 */
    @GetMapping("/{code}/update")
    public String edit(@PathVariable("code") String code, Model model) {
        if (code == null) {
            model.addAttribute("employee", new Employee()); // 新規作成時の処理
        } else {
            model.addAttribute("employee", employeeService.findByCode(code));
        }
        return "employees/update";
    }

    @PostMapping("/{code}/update")
    public String update(@Validated Employee employee, BindingResult res, Model model) {
        if (res.hasErrors()) {
            model.addAttribute("employee", employee);
            return "employees/update";
        }

        String code = employee.getCode();
        var savedEmployee = employeeService.findByCode(code);

        savedEmployee.setName(employee.getName());
        savedEmployee.setRole(employee.getRole());

        String password = employee.getPassword();

        // パスワードのエラー表示
        try {
            ErrorKinds result = employeeService.update(savedEmployee, password);
            if (ErrorMessage.contains(result)) {
                model.addAttribute(ErrorMessage.getErrorName(result), ErrorMessage.getErrorValue(result));
                model.addAttribute("employee", savedEmployee); // エラー時にも従業員情報を保持
                return "employees/update";
            }
        } catch (DataIntegrityViolationException e) {
            model.addAttribute(ErrorMessage.getErrorValue(ErrorKinds.DUPLICATE_EXCEPTION_ERROR));
            return "employees/update";
        }

        return "redirect:/employees";
    }
    // ----- 追加:ここまで -----

    // 従業員削除処理
    @PostMapping(value = "/{code}/delete")
    public String delete(@PathVariable String code, @AuthenticationPrincipal UserDetail userDetail, Model model) {
        ErrorKinds result = employeeService.delete(code, userDetail);

        if (ErrorMessage.contains(result)) {
            // 日報が存在する場合のエラーメッセージを追加
            if (result == ErrorKinds.DUPLICATE_ERROR) {
                model.addAttribute(ErrorMessage.getErrorName(result), "この従業員は日報を持っているため、削除できません。");
            } else {
                model.addAttribute(ErrorMessage.getErrorName(result), ErrorMessage.getErrorValue(result));
            }
            model.addAttribute("employee", employeeService.findByCode(code));
            return detail(code, model);
        }

        return "redirect:/employees";
    }
}