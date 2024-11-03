#!/bin/bash
# Spring Bootアプリケーションの起動
java -jar /var/www/java/DailyReportSystemApplication-0.0.1-SNAPSHOT.jar &

# 起動が完了するまで待機（必要に応じて時間を調整）
sleep 10

# Apache2の起動
rm -f /var/run/apache2/apache2.pid
apachectl -D FOREGROUND
