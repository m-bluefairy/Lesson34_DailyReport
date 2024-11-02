package com.techacademy;

import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {
    /** 認証・認可設定 */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.formLogin(login -> login.loginProcessingUrl("/login") // 従業員番号・パスワードの送信先
                .loginPage("/login") // ログイン画面
                .defaultSuccessUrl("/") // ログイン成功後のリダイレクト先
                .failureUrl("/login?error") // ログイン失敗時のリダイレクト先
                .permitAll() // ログイン画面は未ログインでアクセス可
        ).logout(logout -> logout.logoutSuccessUrl("/login") // ログアウト後のリダイレクト先
        ).authorizeHttpRequests(
                auth -> auth.requestMatchers(PathRequest.toStaticResources().atCommonLocations()).permitAll() // css等は未ログインでアクセス可
                        .requestMatchers("/employees/**").hasAuthority("ADMIN") // 「/employees/**」は管理者のみアクセス可
                        .requestMatchers("/reports/**").hasAnyAuthority("USER", "ADMIN") // 日報関連は一般ユーザーも管理者もアクセス可
                        .anyRequest().authenticated()); // その他のリクエストは認証が必要

        return http.build();
    }

    /** ハッシュ化したパスワードの比較に使用する */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
