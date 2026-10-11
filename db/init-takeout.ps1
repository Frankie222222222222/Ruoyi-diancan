# e:\ruoyi-vue3\db\init-takeout.ps1
# One-shot init orchestrator for takeout module
# Usage:
#   .\db\init-takeout.ps1            # run all steps
#   .\db\init-takeout.ps1 -From 5    # resume from step 5
#   .\db\init-takeout.ps1 -Only 7    # run only step 7
param(
    [int]$From = 0,
    [int]$To   = 999,
    [int[]]$Only = @()
)

$ErrorActionPreference = "Stop"
$env:Path = "C:\Program Files\MySQL\MySQL Server 5.7\bin;$env:Path"
$env:MYSQL_PWD = "123456"
$DB = "ry-vue"; $USER = "root"; $HOST_ = "127.0.0.1"; $PORT = 3306
$ROOT = Split-Path -Parent $PSScriptRoot
$SqlDir = Join-Path $ROOT "RuoYi-Vue\sql\takeout"

function Run-SqlFile($relPath) {
    $full = if (Test-Path $relPath) { (Resolve-Path $relPath).Path } else { Join-Path $SqlDir $relPath }
    if (-not (Test-Path $full)) { throw "SQL file not found: $full" }
    Write-Host "==> [Step] $full" -ForegroundColor Cyan
    # Use SOURCE so UTF-8 bytes don't get re-encoded by PowerShell pipe
    $forward = ($full -replace '\\','/')
    & mysql -h $HOST_ -P $PORT -u $USER --default-character-set=utf8mb4 $DB -e "SOURCE $forward" 2>&1 | Out-String
    if ($LASTEXITCODE -ne 0) {
        throw "Failed: $full (exit=$LASTEXITCODE)"
    }
    Write-Host "OK: $full" -ForegroundColor Green
}

$Steps = @(
    @{ N=1;  Desc="RuoYi base ry_20260417.sql";          Path="RuoYi-Vue\sql\ry_20260417.sql" }
    @{ N=2;  Desc="quartz scheduler (optional)";         Path="RuoYi-Vue\sql\quartz.sql" }
    @{ N=3;  Desc="clean v1 leftovers";                  Path="db\clean-v1.sql" }
    @{ N=4;  Desc="merchant table";                      Path="takeout_merchant.sql" }
    @{ N=5;  Desc="dish category";                       Path="takeout_dish_category.sql" }
    @{ N=6;  Desc="dish table (root dir GBK-broken, skip if table exists)"; Path="sql\takeout_dish.sql" }
    @{ N=7;  Desc="C-end user";                          Path="takeout_user.sql" }
    @{ N=8;  Desc="rider";                               Path="takeout_rider.sql" }
    @{ N=9;  Desc="coupon + coupon_user";                Path="takeout_coupon.sql" }
    @{ N=10; Desc="dispatch";                            Path="takeout_dispatch.sql" }
    @{ N=11; Desc="dispatch lng/lat supplement";         Path="sql\takeout_dispatch_supplement.sql" }
    @{ N=12; Desc="payment";                             Path="takeout_payment.sql" }
    @{ N=13; Desc="rating";                              Path="takeout_rating.sql" }
    @{ N=14; Desc="complaint";                           Path="takeout_complaint.sql" }
    @{ N=15; Desc="order (old status CHAR(1))";          Path="takeout_order.sql" }
    @{ N=16; Desc="v2 migration (kitchen/rider cols)";   Path="takeout_v2_migration.sql" }
    @{ N=17; Desc="v3 dine-in migration";                Path="migration_20261011_dine_in.sql" }
    @{ N=18; Desc="dict seeds";                          Path="db\seed-dict.sql" }
    @{ N=19; Desc="takeout v2 menus";                    Path="takeout_menu_v2.sql" }
    @{ N=20; Desc="dish sales menu + history backfill";  Path="takeout_dish_order_link.sql" }
    @{ N=21; Desc="bind menus to roles";                 Path="db\bind-roles.sql" }
    @{ N=22; Desc="ops config + banners/announcements";  Path="takeout_config_data.sql" }
    @{ N=23; Desc="demo data (merchant/dish/order)";     Path="takeout_demo_data.sql" }
    @{ N=24; Desc="demo data (rider/coupon/rating)";     Path="takeout_demo_data_supplement.sql" }
    @{ N=25; Desc="test data";                           Path="takeout_test_data.sql" }
)

$runSet = if ($Only.Count) { $Only } else { @($From..$To) }
foreach ($s in $Steps) {
    if ($s.N -notin $runSet) { continue }
    Write-Host "===== Step $($s.N): $($s.Desc) =====" -ForegroundColor Yellow
    Run-SqlFile $s.Path
}

Write-Host ""
Write-Host "All steps done. Run .\db\verify-db.ps1 for final check." -ForegroundColor Green
