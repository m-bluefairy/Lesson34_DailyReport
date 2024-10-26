package com.techacademy.entity;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

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
    @NotNull // 日付が必須であることを示す
    @DateTimeFormat(pattern = "yyyy-MM-dd") // 日付フォーマットを指定
    private static String reportDate;

    // タイトル
    @Column(columnDefinition = "VARCHAR(100)", nullable = false)
    @NotEmpty(message = "タイトルは必須です。") // バリデーションメッセージを追加
    private String title;

    // 内容
    @Column(columnDefinition = "LONGTEXT", nullable = false)
    @NotEmpty(message = "内容は必須です。") // バリデーションメッセージを追加
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
    public static String getReportDate() {
        return reportDate;
    }

    public void setReportDate(@NotNull String reportDate) {
        Reports.reportDate = reportDate; // setterの名前を修正
    }

    // fieldNameのgetterとsetter
    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }
}
