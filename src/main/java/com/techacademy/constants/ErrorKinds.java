package com.techacademy.constants;

// エラーメッセージ定義
public enum ErrorKinds {

    // エラー内容とそのメッセージ
    // 空白チェックエラー
    BLANK_ERROR("入力が必須です。"),
    // 半角英数字チェックエラー
    HALFSIZE_ERROR("半角英数字で入力してください。"),
    // 桁数(8桁~16桁以外)チェックエラー
    RANGECHECK_ERROR("桁数は8桁から16桁である必要があります。"),
    // 重複チェックエラー(例外あり)
    DUPLICATE_EXCEPTION_ERROR("重複したデータが存在します。"),
    // 重複チェックエラー(例外なし)
    DUPLICATE_ERROR("重複したデータが検出されました。"),
    // ログイン中削除チェックエラー
    LOGINCHECK_ERROR("ログイン中のユーザーを削除することはできません。"),
    // 日付チェックエラー
    DATECHECK_ERROR("日付が無効です。"),
    // 日付無効エラー
    INVALID_DATE("無効な日付です。"),
    // 日付フォーマットエラー
    DATE_FORMAT_ERROR("日付形式が正しくありません。"),
    // チェックOK
    CHECK_OK("チェックOK"),
    // 正常終了
    SUCCESS("成功"),
    // レポートが見つからないエラー
    NOT_FOUND("レポートが見つかりません。");

    private final String message; // エラーメッセージ

    // コンストラクタ
    ErrorKinds(String message) {
        this.message = message;
    }

    // メッセージを取得するメソッド
    public String getMessage() {
        return message;
    }
}