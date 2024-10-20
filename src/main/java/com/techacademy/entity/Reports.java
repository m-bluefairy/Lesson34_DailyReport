package com.techacademy.entity;

import java.time.LocalDateTime;
import java.time.LocalDate;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

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
    private LocalDate reportDate;

    // タイトル
    @Column(columnDefinition = "VARCHAR(100)", nullable = false)
    private String title;

    // 内容
    @Column(columnDefinition = "LONGTEXT", nullable = false)
    @NotEmpty
    private String content;

    // 社員情報
    @ManyToOne
    @JoinColumn(name = "employee_code", referencedColumnName = "code", nullable = false, insertable = false, updatable = false)
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
        return reportDate;
    }

    public void setReportsDate(LocalDate reportDate) {
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
