# ==========================================================
# 批量 GBK -> UTF-8 转换（保留 BOM）
# ==========================================================
param(
    [string]$TargetPath = "E:\ruoyi-vue3\RuoYi-Vue3"
)

$exts = @("*.vue", "*.js", "*.ts", "*.html", "*.css", "*.scss", "*.json", "*.svg", "*.md")
$gbk = [System.Text.Encoding]::GetEncoding(936)
$utf8WithBom = New-Object System.Text.UTF8Encoding($true)

$converted = 0
$skipped = 0
$total = 0
$errors = @()

Write-Host "`n========== GBK -> UTF-8 (保留 BOM) ==========" -ForegroundColor Cyan
Write-Host "目录: $TargetPath`n"

foreach ($ext in $exts) {
    Get-ChildItem -Path $TargetPath -Filter $ext -Recurse -ErrorAction SilentlyContinue -Force | ForEach-Object {
        if ($_.FullName -match '\\node_modules\\|\\dist\\|\\\.git\\') { return }
        $total++
        $bytes = [System.IO.File]::ReadAllBytes($_.FullName)

        # 去 BOM
        $body = $bytes
        $hadBom = $false
        if ($bytes.Length -ge 3 -and $bytes[0] -eq 0xEF -and $bytes[1] -eq 0xBB -and $bytes[2] -eq 0xBF) {
            $hadBom = $true
            $body = $bytes[3..($bytes.Length - 1)]
        }

        # GBK 解码
        try {
            $txt = $gbk.GetString($body)
        } catch {
            $errors += "$($_.FullName): GBK decode failed"
            return
        }

        # 检查是否含 CJK 字符
        $hasChinese = $false
        foreach ($c in $txt.ToCharArray()) {
            if ($c -ge 0x4E00 -and $c -le 0x9FFF) { $hasChinese = $true; break }
        }
        if (-not $hasChinese) { $skipped++; return }

        # 写入 UTF-8 with BOM
        $newBytes = $utf8WithBom.GetBytes($txt)
        [System.IO.File]::WriteAllBytes($_.FullName, $newBytes)
        $converted++

        $rel = $_.FullName.Substring($TargetPath.Length).TrimStart('\')
        Write-Host "  [CONVERT] $rel" -ForegroundColor Green
    }
}

Write-Host "`n========== 完成 ==========" -ForegroundColor Cyan
Write-Host "扫描: $total"
Write-Host "跳过(无中文): $skipped"
Write-Host "已转换: $converted" -ForegroundColor Green
if ($errors.Count -gt 0) {
    Write-Host "错误:" -ForegroundColor Red
    $errors | ForEach-Object { Write-Host "  $_" -ForegroundColor Red }
}
Write-Host "============================`n"
