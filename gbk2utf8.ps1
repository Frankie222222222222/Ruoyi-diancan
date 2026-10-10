# ==========================================================
# GBK -> UTF-8 ת�������� BOM��
# ������ .vue / .js / .ts / .html / .css ��ǰ���ļ�
# ==========================================================
param(
    [Parameter(Mandatory=$false, Position=0)]
    [string]$TargetPath = "E:\ruoyi-vue3\RuoYi-Vue-frontend"
)

# ��չ��
$exts = @("*.vue", "*.js", "*.ts", "*.html", "*.css", "*.scss", "*.json", "*.svg", "*.md", "*.txt")

$gbk = [System.Text.Encoding]::GetEncoding(936)  # GBK
$utf8NoBom = New-Object System.Text.UTF8Encoding($false)
$utf8WithBom = New-Object System.Text.UTF8Encoding($true)

$converted = 0
$skipped = 0
$total = 0

Write-Host "`n========== GBK -> UTF-8 ==========" -ForegroundColor Cyan
Write-Host "Ŀ¼: $TargetPath"
Write-Host "��չ��: $($exts -join ', ')`n"

foreach ($ext in $exts) {
    Get-ChildItem -Path $TargetPath -Filter $ext -Recurse -ErrorAction SilentlyContinue -Force | ForEach-Object {
        if ($_.FullName -match '\\node_modules\\|\\dist\\|\\\.git\\') { return }
        $total++
        $bytes = [System.IO.File]::ReadAllBytes($_.FullName)

        $hasBom = $false
        $body = $bytes
        if ($bytes.Length -ge 3 -and $bytes[0] -eq 0xEF -and $bytes[1] -eq 0xBB -and $bytes[2] -eq 0xBF) {
            $hasBom = $true
            $body = $bytes[3..($bytes.Length - 1)]
        }

        # �� GBK ���� body
        try {
            $txtGbk = $gbk.GetString($body)
        } catch {
            return
        }
        # ����Ƿ�����ɴ�ӡ�� ASCII
        $hasGbk = $false
        foreach ($c in $txtGbk.ToCharArray()) {
            if ($c -ge 0x4E00 -and $c -le 0x9FFF) {
                $hasGbk = $true
                break
            }
        }
        if (-not $hasGbk) { $skipped++; return }

        # �� UTF-8 ���±���
        $newBytes = $utf8NoBom.GetBytes($txtGbk)
        if ($hasBom) {
            $final = New-Object byte[] ($newBytes.Length + 3)
            $final[0] = 0xEF; $final[1] = 0xBB; $final[2] = 0xBF
            [Array]::Copy($newBytes, 0, $final, 3, $newBytes.Length)
            [System.IO.File]::WriteAllBytes($_.FullName, $final)
        } else {
            [System.IO.File]::WriteAllBytes($_.FullName, $newBytes)
        }

        $converted++
        $rel = $_.FullName.Substring($TargetPath.Length).TrimStart('\')
        Write-Host "  [GBK->UTF8] $rel" -ForegroundColor Green
    }
}

Write-Host "`n========== ��� ==========" -ForegroundColor Cyan
Write-Host "ɨ��: $total  ����(������): $skipped  ת��: $converted" -ForegroundColor Green
Write-Host "============================`n"
