package com.techacademy.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.techacademy.entity.Employee;
import com.techacademy.entity.Reports; // 追加

public class UserDetail implements UserDetails {
    private static final long serialVersionUID = 1L;

    private final Employee employee;
    private final List<SimpleGrantedAuthority> authorities;

    public UserDetail(Employee employee) {
        this.employee = employee;

        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority(employee.getRole().toString()));
        this.authorities = authorities;
    }

    public Employee getEmployee() {
        return employee;
    }

    // 現在の従業員を取得するメソッド
    public Employee getCurrentEmployee() {
        return this.employee;
    }

    // 従業員コードを取得するメソッド
    public String getEmployeeCode() {
        return employee.getEmployeeCode(); // Employee クラスに getEmployeeCode() が必要
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
        return employee.getCode();
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

    // 現在の従業員の報告を取得するメソッド
    public List<Reports> getReports() {
        return null; // TODO: 実装を追加
    }
}