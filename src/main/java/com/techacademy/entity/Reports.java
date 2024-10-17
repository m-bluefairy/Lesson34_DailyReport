package com.techacademy.entity;

import java.time.LocalDateTime;
import java.time.LocalDate;
import org.hibernate.validator.constraints.Length;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;

import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;



@Data
@Entity
@Table(name = "reports")
public class Reports {

    public static enum Role {
        GENERAL("一般"), ADMIN("管理者");

    	private String value;

        private Role(String value) {
            this.value = value;
        }

        public String getValue() {
            return this.value;
        }
    }

 // ID
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 日付
    @Column(columnDefinition="DATE", nullable = false)
    private LocalDate reportDate;

    // タイトル
    @Column(columnDefinition="VARCHAR(100)", nullable = false)
    private String title;

    //内容
    @Column(columnDefinition="LONGTEXT", nullable = false)
    @NotEmpty
    private String content;

    // 社員番号
    @Column(length = 10)
    @NotEmpty
    @Length(max = 10)
    private String employeeCode;

    // 削除フラグ(論理削除を行うため)
    @Column(columnDefinition="TINYINT", nullable = false)
    private boolean deleteFlg;

    // 登録日時
    @Column(nullable = false)
    private LocalDateTime createdAt;

    // 更新日時
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    // 日付を取得するメソッド
    public LocalDate getReportsDate() {
        return reportDate;
    }

    public void setReportsDate(LocalDate reportDate) {
        this.reportDate = reportDate;
    }

}
