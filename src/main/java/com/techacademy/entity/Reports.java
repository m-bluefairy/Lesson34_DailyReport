package com.techacademy.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;
import jakarta.validation.constraints.Size;

@Data
@Entity
@Table(name = "reports")
public class Reports {

    // ID
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 日付
    @Column(columnDefinition = "DATE", nullable = false)
    @NotNull(message = "日付は必須です。") // 日付が必須であることを示す
    @DateTimeFormat(pattern = "yyyy-MM-dd") // 日付フォーマットを指定
    private LocalDate reportDate; // String から LocalDate に変更

    // タイトル
    @Column(columnDefinition = "VARCHAR(100)", nullable = false)
    @NotEmpty(message = "タイトルは必須です。") // 必須チェック
    @Size(max = 100, message = "100文字以下で入力してください") // 桁数チェック
    private String title;

    // 内容
    @Column(columnDefinition = "LONGTEXT", nullable = false)
    @NotEmpty(message = "内容は必須です。") // 必須チェック
    @Size(max = 1000, message = "1000文字以下で入力してください") // 桁数チェック
    private String content;

    // 社員番号
    @Column(name = "employee_code", nullable = false)
    private String employeeCode;

    // 社員情報
    @ManyToOne
    @JoinColumn(name = "employee_code", referencedColumnName = "code", insertable = false, updatable = false)
    private Employee employee;

    // 削除フラグ(論理削除を行うため)
    @Column(columnDefinition = "TINYINT", nullable = false)
    private boolean deleteFlg;

    // 登録日時
    @Column(nullable = false)
    private LocalDateTime createdAt;

    // 更新日時
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    // フィールド名
    @Column(columnDefinition = "VARCHAR(100)")
    private String fieldName;

    // 日付を取得するメソッド
    public LocalDate getReportDate() {
        return reportDate; // LocalDate を返す
    }

    public void setReportDate(@NotNull LocalDate reportDate) { // 引数を LocalDate に変更
        this.reportDate = reportDate;
    }

    // fieldNameのgetterとsetter
    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }
}