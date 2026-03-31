# ============================================================================
# Phase 0 — Repository Setup
# ============================================================================
# Replicates: gh repo create → git init → git add -A → git commit → git push
# ============================================================================

param(
    [switch]$SkipIfExists   # Skip if repo already exists on GitHub
)

$ErrorActionPreference = "Stop"
. "$PSScriptRoot\config.ps1"
Write-Phase "PHASE 0" "Repository Setup — Create GitHub repo, init, push code"

# ── Step 0.1: Check if repo already exists ──
Write-Host "[0.1] Checking if repo exists on GitHub..." -ForegroundColor Yellow
$repoCheck = gh repo view "$GitHubOrg/$ServiceName" --json name 2>&1
if ($LASTEXITCODE -eq 0) {
    if ($SkipIfExists) {
        Write-Host "  Repo already exists. Skipping creation (-SkipIfExists)." -ForegroundColor Green
    } else {
        Write-Host "  Repo already exists: https://github.com/$GitHubOrg/$ServiceName" -ForegroundColor Green
        Write-Host "  Use -SkipIfExists to skip this phase." -ForegroundColor Gray
    }
} else {
    # ── Step 0.2: Create the GitHub repo ──
    Write-Host "[0.2] Creating GitHub repo: $GitHubOrg/$ServiceName ($RepoVisibility)..." -ForegroundColor Yellow
    gh repo create "$GitHubOrg/$ServiceName" --"$RepoVisibility" --description "Legacy JBoss Fuse ESB service — Reverse Engineering target" 2>&1
    if ($LASTEXITCODE -ne 0) { throw "Failed to create repo" }
    Write-Host "  Repo created: https://github.com/$GitHubOrg/$ServiceName" -ForegroundColor Green
}

# ── Step 0.3: Initialize local git if needed ──
Set-Location $ServiceRoot
if (-not (Test-Path ".git")) {
    Write-Host "[0.3] Initializing git repository..." -ForegroundColor Yellow
    git init 2>&1 | Out-Null
    git remote add origin "https://github.com/$GitHubOrg/$ServiceName.git" 2>&1 | Out-Null
    Write-Host "  Git initialized with remote origin." -ForegroundColor Green
} else {
    Write-Host "[0.3] Git already initialized." -ForegroundColor Green
}

# ── Step 0.4: Stage, commit, and push ──
$status = git status --porcelain 2>&1
if ($status) {
    Write-Host "[0.4] Staging and committing all files..." -ForegroundColor Yellow
    git add -A 2>&1 | Out-Null
    $fileCount = (git diff --cached --numstat 2>&1 | Measure-Object).Count
    git commit -m "initial: $ServiceName — JBoss Fuse 6.3 / Camel 2.17 legacy service" 2>&1 | Out-Null
    Write-Host "  Committed $fileCount files." -ForegroundColor Green

    Write-Host "[0.5] Pushing to origin/main..." -ForegroundColor Yellow
    git push -u origin main 2>&1 | Out-Null
    Write-Host "  Pushed to https://github.com/$GitHubOrg/$ServiceName" -ForegroundColor Green
} else {
    Write-Host "[0.4] Working tree clean. Checking if remote is up to date..." -ForegroundColor Green
    $behind = git rev-list --count "origin/main..HEAD" 2>&1
    if ($behind -gt 0) {
        Write-Host "[0.5] Pushing $behind local commit(s)..." -ForegroundColor Yellow
        git push origin main 2>&1 | Out-Null
        Write-Host "  Pushed." -ForegroundColor Green
    } else {
        Write-Host "[0.5] Already up to date with origin/main." -ForegroundColor Green
    }
}

Write-Host "`n[Phase 0] COMPLETE" -ForegroundColor Green
