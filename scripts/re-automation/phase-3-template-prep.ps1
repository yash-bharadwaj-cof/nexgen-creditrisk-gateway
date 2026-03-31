# ============================================================================
# Phase 3 — Template Preparation
# ============================================================================
# Replicates: Copy-Item RE templates → git add → git commit → git push
# ============================================================================

$ErrorActionPreference = "Stop"
. "$PSScriptRoot\config.ps1"
Write-Phase "PHASE 3" "Template Preparation — Copy RE templates into repo"

Set-Location $ServiceRoot

# ── Step 3.1: Validate source templates exist ──
Write-Host "[3.1] Checking template source: $TemplateSource" -ForegroundColor Yellow
if (-not (Test-Path $TemplateSource)) {
    throw "Template source folder not found: $TemplateSource"
}

$sourceTemplates = Get-ChildItem -Path $TemplateSource -Filter "*.md"
Write-Host "  Found $($sourceTemplates.Count) template files" -ForegroundColor Green

# ── Step 3.2: Create destination directory ──
Write-Host "`n[3.2] Creating destination: $TemplateDestDir/" -ForegroundColor Yellow
New-Item -ItemType Directory -Path $TemplateDestDir -Force | Out-Null

# ── Step 3.3: Copy templates ──
Write-Host "`n[3.3] Copying templates..." -ForegroundColor Yellow
foreach ($tmpl in $sourceTemplates) {
    $destFile = Join-Path $TemplateDestDir $tmpl.Name
    Copy-Item -Path $tmpl.FullName -Destination $destFile -Force
    $sizeKB = [math]::Round($tmpl.Length / 1024, 1)
    Write-Host "  Copied: $($tmpl.Name) (${sizeKB}KB)" -ForegroundColor Green
}

# ── Step 3.4: Verify all manifest templates are present ──
Write-Host "`n[3.4] Verifying manifest coverage..." -ForegroundColor Yellow
$missing = @()
foreach ($re in $REManifest) {
    $tmplPath = Join-Path $TemplateDestDir $re.Template
    if (-not (Test-Path $tmplPath)) {
        $missing += $re.Template
        Write-Host "  MISSING: $($re.Template)" -ForegroundColor Red
    }
}
if ($missing.Count -gt 0) {
    throw "Missing $($missing.Count) template(s) required by manifest: $($missing -join ', ')"
}
Write-Host "  All $($REManifest.Count) manifest templates present." -ForegroundColor Green

# ── Step 3.5: Commit and push ──
Write-Host "`n[3.5] Committing templates..." -ForegroundColor Yellow
git add $TemplateDestDir 2>&1 | Out-Null
$hasChanges = git diff --cached --quiet 2>&1; $changed = $LASTEXITCODE -ne 0
if ($changed) {
    $templateList = ($REManifest | ForEach-Object { "- $($_.Template)" }) -join "`n"
    git commit -m "docs: add $($REManifest.Count) RE template files for Copilot agent reference`n`n$templateList" 2>&1 | Out-Null
    git push origin main 2>&1 | Out-Null
    Write-Host "  Committed and pushed $($REManifest.Count) templates." -ForegroundColor Green
} else {
    Write-Host "  Templates already committed. No changes." -ForegroundColor Gray
}

Write-Host "`n[Phase 3] COMPLETE" -ForegroundColor Green
