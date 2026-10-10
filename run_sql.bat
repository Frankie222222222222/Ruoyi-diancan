@echo off
setlocal
set MYSQL="C:\Program Files\MySQL\MySQL Server 5.7\bin\mysql.exe"
set DB=ry-vue
set OPTS=-uroot -p123456 --default-character-set=utf8mb4
echo === %1 ===
%MYSQL% %OPTS% %DB% < %1
echo exit=%ERRORLEVEL%
echo.
