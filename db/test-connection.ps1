# e:\ruoyi-vue3\db\test-connection.ps1
# 验证 MySQL 连接
$ErrorActionPreference = "Stop"
$env:Path = "C:\Program Files\MySQL\MySQL Server 5.7\bin;$env:Path"
$env:MYSQL_PWD = "123456"
$conn = & mysql -h 127.0.0.1 -P 3306 -u root -e "SELECT VERSION();"
if ($LASTEXITCODE -ne 0) { Write-Error "MySQL 连接失败,exit code $LASTEXITCODE"; exit 1 }
Write-Host "OK MySQL: $conn"
