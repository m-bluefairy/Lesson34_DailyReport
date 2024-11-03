#!/bin/bash

# MySQLの起動
service mysql start

# MySQLが完全に起動するまで待機
sleep 10

# 初期設定が必要であれば、ここでSQLスクリプトを実行する
# cat /tmp/mysql_setup.sql | mysql -u root -prootpass

# フォアグラウンドでMySQLを実行
tail -f /var/log/mysql/error.log

