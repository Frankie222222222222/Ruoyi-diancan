# e:\ruoyi-vue3\db\verify-db.ps1
# Post-init verification
$ErrorActionPreference = "Stop"
$env:Path = "C:\Program Files\MySQL\MySQL Server 5.7\bin;$env:Path"
$env:MYSQL_PWD = "123456"

function Sql($q) {
    & mysql -h 127.0.0.1 -u root -N -B ry-vue -e $q 2>$null
    if ($LASTEXITCODE -ne 0) { throw "mysql failed: $q" }
}

$missing = New-Object System.Collections.Generic.List[string]

# 1) tables
$tables = @(
    "takeout_merchant","takeout_dish_category","takeout_dish","takeout_user",
    "takeout_rider","takeout_coupon","takeout_coupon_user","takeout_order",
    "takeout_order_item","takeout_dispatch","takeout_payment","takeout_rating",
    "takeout_complaint","takeout_dine_table",
    "takeout_banner","takeout_announcement","takeout_help_category","takeout_help_article"
)
Write-Host "=== Tables ===" -ForegroundColor Cyan
foreach ($t in $tables) {
    $n = Sql "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema='ry-vue' AND table_name='$t';"
    if ($n -ne 1) { Write-Host "MISSING: $t" -ForegroundColor Red; $missing.Add($t) }
    else { Write-Host "  OK: $t" }
}

# 2) key fields
$fields = @(
    @{ T='takeout_order'; C='kitchen_id' },
    @{ T='takeout_order'; C='rider_id' },
    @{ T='takeout_order'; C='order_type' },
    @{ T='takeout_order'; C='table_id' },
    @{ T='takeout_user';  C='role' },
    @{ T='takeout_order'; C='kitchen_accept_time' },
    @{ T='takeout_order'; C='ready_time' }
)
Write-Host "`n=== Key Fields ===" -ForegroundColor Cyan
foreach ($f in $fields) {
    $n = Sql "SELECT COUNT(*) FROM information_schema.columns WHERE table_schema='ry-vue' AND table_name='$($f.T)' AND column_name='$($f.C)';"
    if ($n -ne 1) { Write-Host "MISSING: $($f.T).$($f.C)" -ForegroundColor Red; $missing.Add("$($f.T).$($f.C)") }
    else { Write-Host "  OK: $($f.T).$($f.C)" }
}

# 3) dict
Write-Host "`n=== Dict ===" -ForegroundColor Cyan
$dict = Sql "SELECT dict_type, COUNT(*) FROM sys_dict_data WHERE dict_type LIKE 'takeout%' GROUP BY dict_type;"
$dict | ForEach-Object { Write-Host "  $_" }

# 4) menus
$menus = Sql "SELECT COUNT(*) FROM sys_menu WHERE menu_id BETWEEN 2000 AND 2200 OR perms LIKE 'takeout%';"
Write-Host "`n=== takeout menus: $menus (expected >= 50) ===" -ForegroundColor Cyan

# 5) role grants
$grants = Sql "SELECT COUNT(*) FROM sys_role_menu WHERE menu_id BETWEEN 2000 AND 2200;"
Write-Host "=== role-menu grants: $grants (expected >= 60) ===" -ForegroundColor Cyan

# 6) row counts
Write-Host "`n=== Row counts ===" -ForegroundColor Cyan
$rows = Sql "SELECT 'merchant',COUNT(*) FROM takeout_merchant UNION ALL SELECT 'dish',COUNT(*) FROM takeout_dish UNION ALL SELECT 'dish_cat',COUNT(*) FROM takeout_dish_category UNION ALL SELECT 'order',COUNT(*) FROM takeout_order UNION ALL SELECT 'order_item',COUNT(*) FROM takeout_order_item UNION ALL SELECT 'rider',COUNT(*) FROM takeout_rider UNION ALL SELECT 'user',COUNT(*) FROM takeout_user UNION ALL SELECT 'coupon',COUNT(*) FROM takeout_coupon UNION ALL SELECT 'rating',COUNT(*) FROM takeout_rating UNION ALL SELECT 'complaint',COUNT(*) FROM takeout_complaint UNION ALL SELECT 'dispatch',COUNT(*) FROM takeout_dispatch UNION ALL SELECT 'payment',COUNT(*) FROM takeout_payment UNION ALL SELECT 'dine_table',COUNT(*) FROM takeout_dine_table UNION ALL SELECT 'banner',COUNT(*) FROM takeout_banner;"
$rows | ForEach-Object { Write-Host "  $_" }

# 7) sample admin grants
Write-Host "`n=== sample admin (role_id=1) takeout menus ===" -ForegroundColor Cyan
Sql "SELECT m.menu_id, m.menu_name, m.perms FROM sys_menu m JOIN sys_role_menu rm ON rm.menu_id=m.menu_id WHERE rm.role_id=1 AND (m.menu_id BETWEEN 2000 AND 2200 OR m.perms LIKE 'takeout%') ORDER BY m.menu_id LIMIT 15;" | ForEach-Object { Write-Host "  $_" }

if ($missing.Count) {
    Write-Host "`n[FAIL] Missing: $($missing -join ', ')" -ForegroundColor Red
    exit 1
} else {
    Write-Host "`n[OK] All checks passed" -ForegroundColor Green
}
