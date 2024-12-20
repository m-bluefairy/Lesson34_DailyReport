package com.techacademy.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.techacademy.constants.ErrorKinds;
import com.techacademy.entity.Employee;
import com.techacademy.entity.Reports; // Reportsエンティティをインポート
import com.techacademy.repository.EmployeeRepository;

@Service
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final PasswordEncoder passwordEncoder;
    private final ReportsService reportsService; // 追加

    @Autowired
    public EmployeeService(EmployeeRepository employeeRepository, PasswordEncoder passwordEncoder, ReportsService reportsService) {
        this.employeeRepository = employeeRepository;
        this.passwordEncoder = passwordEncoder;
        this.reportsService = reportsService; // 追加
    }

    // 従業員保存
    @Transactional
    public ErrorKinds save(Employee employee) {
        // パスワードチェック
        ErrorKinds result = employeePasswordCheck(employee);
        if (ErrorKinds.CHECK_OK != result) {
            return result;
        }

        // 従業員番号重複チェック
        if (findByCode(employee.getCode()) != null) {
            return ErrorKinds.DUPLICATE_ERROR;
        }

        employee.setDeleteFlg(false);

        LocalDateTime now = LocalDateTime.now();
        employee.setCreatedAt(now);
        employee.setUpdatedAt(now);

        employeeRepository.save(employee);
        return ErrorKinds.SUCCESS;
    }

    // 従業員更新を行なう
    @Transactional
    public ErrorKinds update(Employee employee, String newPassword) {
        // 新しいパスワードが空でない場合のみチェックを実施
        if (!newPassword.isEmpty()) {
            // newPasswordをemployeeにセットしてチェックを実施
            employee.setPassword(newPassword);
            ErrorKinds result = employeePasswordCheck(employee);
            if (ErrorKinds.CHECK_OK != result) {
                return result;
            }
        }

        // 更新日時の設定
        LocalDateTime now = LocalDateTime.now();
        employee.setUpdatedAt(now);

        employeeRepository.save(employee);
        return ErrorKinds.SUCCESS;
    }

    // 従業員削除
    @Transactional
    public ErrorKinds delete(String code, UserDetail userDetail) {
        // 自分を削除しようとした場合はエラーメッセージを表示
        if (code.equals(userDetail.getEmployee().getCode())) {
            return ErrorKinds.LOGINCHECK_ERROR;
        }

        // 従業員を取得
        Employee employee = findByCode(code);
        if (employee == null) {
            return ErrorKinds.NOT_FOUND; // 従業員が見つからない場合はエラーを返す
        }

        // 削除対象の従業員に紐づいている日報が存在するか確認
        List<Reports> reportList = reportsService.findByEmployee(employee);
        if (!reportList.isEmpty()) {
            // 日報が存在する場合は、個別に削除する
            for (Reports report : reportList) {
                reportsService.deleteReport(report.getId()); // 日報を削除
            }
        }

        // 従業員を削除する
        employee.setDeleteFlg(true); // 論理削除
        employeeRepository.save(employee);

        return ErrorKinds.SUCCESS;
    }

    // 従業員一覧表示処理
    public List<Employee> findAll() {
        return employeeRepository.findAll();
    }

    // 1件を検索
    public Employee findByCode(String code) {
        // findByIdで検索
        Optional<Employee> option = employeeRepository.findById(code);
        // 取得できなかった場合はnullを返す
        return option.orElse(null);
    }

    // 現在の従業員を取得するメソッド
    public Employee findCurrentEmployee(String employeeCode) {
        return findByCode(employeeCode);
    }

    // 従業員パスワードチェック
    private ErrorKinds employeePasswordCheck(Employee employee) {
        // 従業員パスワードの半角英数字チェック処理
        if (isHalfSizeCheckError(employee)) {
            return ErrorKinds.HALFSIZE_ERROR;
        }

        // 従業員パスワードの8文字～16文字チェック処理
        if (isOutOfRangePassword(employee)) {
            return ErrorKinds.RANGECHECK_ERROR;
        }

        employee.setPassword(passwordEncoder.encode(employee.getPassword()));
        return ErrorKinds.CHECK_OK;
    }

    // 従業員パスワードの半角英数字チェック処理
    private boolean isHalfSizeCheckError(Employee employee) {
        // 半角英数字チェック
        Pattern pattern = Pattern.compile("^[A-Za-z0-9]+$");
        Matcher matcher = pattern.matcher(employee.getPassword());
        return !matcher.matches();
    }

    // 従業員パスワードの8文字～16文字チェック処理
    public boolean isOutOfRangePassword(Employee employee) {
        // 桁数チェック
        int passwordLength = employee.getPassword().length();
        return passwordLength < 8 || 16 < passwordLength;
    }
}