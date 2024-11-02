package com.techacademy.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class TopController {

    // ログイン画面表示
    @GetMapping(value = "/login")
    public String login() {
        return "login/login";
    }

    // ログイン後のトップページ表示
    @GetMapping(value = "/")
    public String top(Model model) {
        try {
            // 修正後は、日報一覧画面に当たる「/reports」にリダイレクト
            return "redirect:/reports";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "予期しないエラーが発生しました: " + e.getMessage());
            return "error"; // エラーページを表示
        }
    }

}