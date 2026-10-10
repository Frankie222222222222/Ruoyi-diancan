# ==========================================================
# 批量将文件强制转换为 UTF-8 (带 BOM) 编码
# 解决 RuoYi 项目中 Vue/Java/XML 文件被改 GBK 后乱码问题
# ==========================================================
# 用法：右键 PowerShell → 用管理员运行 → 输入文件路径或拖入文件夹

param(
    [Parameter(Mandatory=$false, Position=0)]
    [string]$TargetPath = "E:\ruoyi-vue3"
)

$ErrorActionPreference = "Stop"
$utf8Bom = New-Object System.Text.UTF8Encoding($true)  # 带 BOM

function Convert-FileToUtf8Bom {
    param([string]$Path)
    try {
        # 检测 BOM
        $bytes = [System.IO.File]::ReadAllBytes($Path)
        $hasBom = ($bytes.Length -ge 3 -and $bytes[0] -eq 0xEF -and $bytes[1] -eq 0xBB -and $bytes[2] -eq 0xBF)

        # 尝试用 UTF-8 解码（带 BOM 严格模式）
        $utf8Strict = New-Object System.Text.UTF8Encoding($true, $true)
        try {
            $content = $utf8Strict.GetString($bytes)
            if (-not $hasBom) {
                # 当前无 BOM，按 UTF-8 内容理解，强制加 BOM
                [System.IO.File]::WriteAllText($Path, $content, $utf8Bom)
                return "OK (无BOM→加BOM)"
            }
            return "SKIP (已有BOM)"
        }
        catch {
            # UTF-8 解码失败 → 可能是 GBK
            try {
                $gbk = [System.Text.Encoding]::GetEncoding("GB18030")
                $content = $gbk.GetString($bytes)
                [System.IO.File]::WriteAllText($Path, $content, $utf8Bom)
                return "FIX (GBK→UTF-8 BOM)"
            }
            catch {
                return "FAIL: $($_)"
            }
        }
    }
    catch {
        return "ERR: $($_)"
    }
}

if (-not (Test-Path $TargetPath)) {
    Write-Host "路径不存在: $TargetPath" -ForegroundColor Red
    exit 1
}

$exts = @("*.vue", "*.js", "*.ts", "*.java", "*.xml", "*.html", "*.json", "*.css", "*.md", "*.sql", "*.yml", "*.yaml", "*.properties")
$total = 0
$fixed = 0
$skipped = 0
$failed = 0

Write-Host "`n========== 开始扫描: $TargetPath ==========" -ForegroundColor Cyan
Write-Host "扩展名: $($exts -join ', ')`n"

foreach ($ext in $exts) {
    Get-ChildItem -Path $TargetPath -Filter $ext -Recurse -ErrorAction SilentlyContinue -Force | ForEach-Object {
        # 跳过依赖目录
        if ($_.FullName -match '\\node_modules\\|\\target\\|\\\.git\\|\\dist\\') {
            return
        }
        $total++
        $result = Convert-FileToUtf8Bom -Path $_.FullName
        $rel = $_.FullName.Substring($TargetPath.Length).TrimStart('\\')
        switch -Regex ($result) {
            '^OK' {
                $fixed++
                Write-Host "  [FIX] $rel" -ForegroundColor Green
            }
            '^SKIP' {
                $skipped++
            }
            '^FIX' {
                $fixed++
                Write-Host "  [GBK→UTF8] $rel" -ForegroundColor Yellow
            }
            default {
                $failed++
                Write-Host "  [FAIL] $rel  $result" -ForegroundColor Red
            }
        }
    }
}

Write-Host "`n========== 扫描完成 ==========" -ForegroundColor Cyan
Write-Host "扫描文件: $total"
Write-Host "已修复:  $fixed" -ForegroundColor Green
Write-Host "已跳过:  $skipped"
Write-Host "失败:    $failed" -ForegroundColor Red
Write-Host "============================`n"
