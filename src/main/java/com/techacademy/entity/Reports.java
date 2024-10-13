
package com.techacademy.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.SQLRestriction;
import org.hibernate.validator.constraints.Length;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Data
@Entity
@Table(name = "reports")
@SQLRestriction("delete_flg = false")
public class Reports {

    public static enum Role {
        GENERAL("一般"), ADMIN("管理者");

        private String date;

        private Role(String date) {
            this.date = date;
        }

        public String getValue() {
            return this.date;
        }
    }

    // ID
    @Column(columnDefinition="INT", nullable = false)
    private String id;

    // 日付
    @Column(columnDefinition="DATE", nullable = false)
    private boolean reportdate;

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
    private String employeecode;

    // 削除フラグ(論理削除を行うため)
    @Column(columnDefinition="TINYINT", nullable = false)
    private boolean deleteFlg;

    // 登録日時
    @Column(nullable = false)
    private LocalDateTime createdAt;

    // 更新日時
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}