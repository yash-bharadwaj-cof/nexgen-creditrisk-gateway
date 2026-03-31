# ============================================================================
# RE Automation — Orchestrator
# ============================================================================
# Runs all phases in sequence, exactly replicating the chat session workflow.
#
# Usage:
#   .\run-all.ps1                       # Run all phases 0→6
#   .\run-all.ps1 -FromPhase 3          # Resume from Phase 3
#   .\run-all.ps1 -DryRun               # Preview what would execute
#   .\run-all.ps1 -OnlyPhase 5          # Run only Phase 5
# ============================================================================

param(
    [int]$FromPhase = 0,         # Start from this phase (0-6)
    [int]$OnlyPhase = -1,        # Run only this single phase (-1 = run all)
    [switch]$DryRun,             # Preview without executing
    [switch]$SkipRepoCreation    # Skip Phase 0 if repo already exists
)

$ErrorActionPreference = "Stop"
. "$PSScriptRoot\config.ps1"

$phases = @(
    @{ Num = 0; Name = "Repository Setup";       Script = "phase-0-repo-setup.ps1";       Desc = "Create GitHub repo, init git, push code" }
    @{ Num = 1; Name = "GitHub Infrastructure";   Script = "phase-1-github-infra.ps1";     Desc = "24 labels, 2 milestones, templates, project board" }
    @{ Num = 2; Name = "Codebase Analysis";       Script = "phase-2-codebase-analysis.ps1"; Desc = "Scan source → extract architecture context JSON" }
    @{ Num = 3; Name = "Template Preparation";    Script = "phase-3-template-prep.ps1";    Desc = "Copy 10 RE templates into repo, commit, push" }
    @{ Num = 4; Name = "Issue Filing";            Script = "phase-4-issue-filing.ps1";     Desc = "File 10 enriched issues with context + labels + milestone" }
    @{ Num = 5; Name = "Copilot Assignment";      Script = "phase-5-copilot-assign.ps1";   Desc = "Assign copilot-swe-agent to all 10 issues" }
    @{ Num = 6; Name = "Monitor";                 Script = "phase-6-monitor.ps1";          Desc = "Dashboard: track PRs, completion status" }
)

# ── Banner ──
Write-Host ""
Write-Host "╔══════════════════════════════════════════════════════════════════╗" -ForegroundColor Magenta
Write-Host "║                                                                  ║" -ForegroundColor Magenta
Write-Host "║     RE AUTOMATION PIPELINE — ORCHESTRATOR                        ║" -ForegroundColor Magenta
Write-Host "║                                                                  ║" -ForegroundColor Magenta
Write-Host "║     Service:  $($ServiceName.PadRight(48))║" -ForegroundColor Magenta
Write-Host "║     GitHub:   $GitHubOrg/$ServiceName" -ForegroundColor Magenta
Write-Host "║     Phases:   0 → 6 (7 phases)                                  ║" -ForegroundColor Magenta
Write-Host "║                                                                  ║" -ForegroundColor Magenta
Write-Host "╚══════════════════════════════════════════════════════════════════╝" -ForegroundColor Magenta
Write-Host ""

# ── Phase Plan ──
Write-Host "  EXECUTION PLAN:" -ForegroundColor White
Write-Host "  ───────────────" -ForegroundColor Gray

foreach ($phase in $phases) {
    $willRun = $false
    if ($OnlyPhase -ge 0) {
        $willRun = ($phase.Num -eq $OnlyPhase)
    } else {
        $willRun = ($phase.Num -ge $FromPhase)
    }

    if ($phase.Num -eq 0 -and $SkipRepoCreation) { $willRun = $false }

    $status = if ($willRun) { "RUN " } else { "SKIP" }
    $color = if ($willRun) { "Green" } else { "DarkGray" }
    Write-Host ("  [{0}] Phase {1}: {2,-25} — {3}" -f $status, $phase.Num, $phase.Name, $phase.Desc) -ForegroundColor $color
}

if ($DryRun) {
    Write-Host "`n  DRY RUN — No phases executed." -ForegroundColor Yellow
    exit 0
}

Write-Host ""

# ── Precondition Checks ──
Write-Host "  PRECONDITION CHECKS:" -ForegroundColor White

# Check gh CLI
$ghVersion = gh --version 2>&1 | Select-Object -First 1
if ($LASTEXITCODE -ne 0) { throw "GitHub CLI (gh) not found. Install from https://cli.github.com/" }
Write-Host "  [OK] gh CLI: $ghVersion" -ForegroundColor Green

# Check git
$gitVersion = git --version 2>&1
if ($LASTEXITCODE -ne 0) { throw "git not found." }
Write-Host "  [OK] git: $gitVersion" -ForegroundColor Green

# Check authentication
$ghUser = gh api user --jq ".login" 2>&1
if ($LASTEXITCODE -ne 0) { throw "gh CLI not authenticated. Run: gh auth login" }
Write-Host "  [OK] Authenticated as: $ghUser" -ForegroundColor Green

# Check service root exists
if (-not (Test-Path $ServiceRoot)) { throw "Service root not found: $ServiceRoot" }
Write-Host "  [OK] Service root: $ServiceRoot" -ForegroundColor Green

# Check templates source (needed for Phase 3)
if ($FromPhase -le 3 -and $OnlyPhase -ne 5 -and $OnlyPhase -ne 6) {
    if (-not (Test-Path $TemplateSource)) { throw "Template source not found: $TemplateSource" }
    Write-Host "  [OK] Template source: $TemplateSource" -ForegroundColor Green
}

Write-Host ""

# ── Execute Phases ──
$startTime = Get-Date
$executedPhases = 0

foreach ($phase in $phases) {
    $willRun = $false
    if ($OnlyPhase -ge 0) {
        $willRun = ($phase.Num -eq $OnlyPhase)
    } else {
        $willRun = ($phase.Num -ge $FromPhase)
    }
    if ($phase.Num -eq 0 -and $SkipRepoCreation) { $willRun = $false }

    if (-not $willRun) { continue }

    $phaseStart = Get-Date
    $scriptPath = Join-Path $PSScriptRoot $phase.Script

    try {
        if ($phase.Num -eq 0) {
            & $scriptPath -SkipIfExists
        } else {
            & $scriptPath
        }
        $executedPhases++
        $elapsed = (Get-Date) - $phaseStart
        Write-Host "`n  Phase $($phase.Num) completed in $([math]::Round($elapsed.TotalSeconds))s`n" -ForegroundColor Green
    }
    catch {
        Write-Host "`n  Phase $($phase.Num) FAILED: $_" -ForegroundColor Red
        Write-Host "  Resume from this phase with: .\run-all.ps1 -FromPhase $($phase.Num)" -ForegroundColor Yellow
        exit 1
    }
}

# ── Summary ──
$totalElapsed = (Get-Date) - $startTime
Write-Host ""
Write-Host "╔══════════════════════════════════════════════════════════════════╗" -ForegroundColor Green
Write-Host "║  PIPELINE COMPLETE                                               ║" -ForegroundColor Green
Write-Host "║                                                                  ║" -ForegroundColor Green
Write-Host "║  Phases executed: $executedPhases                                           ║" -ForegroundColor Green
Write-Host "║  Total time:     $([math]::Round($totalElapsed.TotalMinutes, 1)) minutes                                     ║" -ForegroundColor Green
Write-Host "║                                                                  ║" -ForegroundColor Green
Write-Host "║  Next steps:                                                     ║" -ForegroundColor Green
Write-Host "║  1. Monitor PRs:    .\phase-6-monitor.ps1 -Watch                ║" -ForegroundColor Green
Write-Host "║  2. Review PRs:     https://github.com/$GitHubOrg/$ServiceName/pulls    ║" -ForegroundColor Green
Write-Host "║  3. Merge PRs to complete RE cycle                               ║" -ForegroundColor Green
Write-Host "╚══════════════════════════════════════════════════════════════════╝" -ForegroundColor Green
