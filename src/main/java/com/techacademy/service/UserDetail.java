package com.techacademy.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.techacademy.entity.Employee;
import com.techacademy.entity.Reports;

public class UserDetail implements UserDetails {
    private static final long serialVersionUID = 1L;

    private final Employee employee;
    private final List<SimpleGrantedAuthority> authorities;

    // コンストラクタ
    public UserDetail(Employee employee) {
        this.employee = employee;

        // 権限を設定
        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority(employee.getRole().toString())); // ロールを権限として追加
        this.authorities = authorities;
    }

    // Employeeオブジェクトを返す
    public Employee getEmployee() {
        return employee;
    }

    // 従業員コードを取得するメソッド
    public String getEmployeeCode() {
        return employee.getEmployeeCode(); // Employee クラスの getEmployeeCode() を利用
    }

    // 管理者かどうかを判定するメソッド
    public boolean isAdmin() {
        return "ADMIN".equals(employee.getRole().name()); // ロールが "ADMIN" なら管理者と判定
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return employee.getPassword();
    }

    @Override
    public String getUsername() {
        return employee.getCode(); // ユーザー名として従業員コードを返す
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    // 現在の従業員の日報リストを取得するメソッド
    public List<Reports> getReports() {
        return employee.getReportList(); // Employee クラスのリレーションに基づく日報リストを返す
    }
}