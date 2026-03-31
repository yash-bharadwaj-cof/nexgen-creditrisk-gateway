# ============================================================================
# FE Automation — Master Orchestrator
# ============================================================================
# Runs all FE automation phases in sequence, replicating the complete
# Forward Engineering workflow from the chat session.
#
# Usage:
#   .\run-all.ps1                       # Run phases 1→6
#   .\run-all.ps1 -FromPhase 3          # Resume from Phase 3
#   .\run-all.ps1 -OnlyPhase 5          # Run only Phase 5 (monitor)
#   .\run-all.ps1 -DryRun               # Preview without executing
#   .\run-all.ps1 -SkipMerge            # Skip Phase 6 (merge/consolidate)
# ============================================================================

param(
    [int]$FromPhase = 1,         # Start from this phase (1-6)
    [int]$OnlyPhase = -1,        # Run only this single phase (-1 = all)
    [switch]$DryRun,             # Preview without executing
    [switch]$SkipMerge           # Skip Phase 6 (useful if PRs still pending)
)

$ErrorActionPreference = "Stop"
. "$PSScriptRoot\config.ps1"

$phases = @(
    @{ Num = 1; Name = "Parse FE Prompt";         Script = "phase-1-parse-prompt.ps1";       Desc = "Parse migration prompt → structured context JSON" }
    @{ Num = 2; Name = "GitHub Infrastructure";    Script = "phase-2-github-infra.ps1";       Desc = "Labels, milestones, templates, project board, discussions" }
    @{ Num = 3; Name = "File FE Issues";           Script = "phase-3-file-issues.ps1";        Desc = "Create enriched FE issues with self-contained prompts" }
    @{ Num = 4; Name = "Copilot Assignment";       Script = "phase-4-copilot-assign.ps1";     Desc = "Assign copilot-swe-agent to all FE issues" }
    @{ Num = 5; Name = "Monitor Dashboard";        Script = "phase-5-monitor.ps1";            Desc = "Track PR generation, issue completion, conflict status" }
    @{ Num = 6; Name = "Merge & Consolidate";      Script = "phase-6-merge-consolidate.ps1";  Desc = "Merge PRs, move files, fix imports, verify build" }
)

# ── Banner ──
Write-Host ""
Write-Host "╔══════════════════════════════════════════════════════════════════════╗" -ForegroundColor Magenta
Write-Host "║                                                                      ║" -ForegroundColor Magenta
Write-Host "║     FE AUTOMATION PIPELINE — FORWARD ENGINEERING ORCHESTRATOR        ║" -ForegroundColor Magenta
Write-Host "║                                                                      ║" -ForegroundColor Magenta
Write-Host "║     Service:     $($ServiceName.PadRight(47))║" -ForegroundColor Magenta
Write-Host "║     GitHub:      $GitHubOrg/$ServiceName" -ForegroundColor Magenta
Write-Host "║     FE Output:   $FEOutputDir/" -ForegroundColor Magenta
Write-Host "║     FE Prompt:   $FEPromptFile" -ForegroundColor Magenta
Write-Host "║     Issues:      $($FEManifest.Count) issues across $(($FEManifest | ForEach-Object { $_.Wave } | Sort-Object -Unique).Count) waves" -ForegroundColor Magenta
Write-Host "║     Phases:      1 → 6 (6 phases)                                   ║" -ForegroundColor Magenta
Write-Host "║                                                                      ║" -ForegroundColor Magenta
Write-Host "╚══════════════════════════════════════════════════════════════════════╝" -ForegroundColor Magenta
Write-Host ""

# ── Execution Plan ──
Write-Host "  EXECUTION PLAN:" -ForegroundColor White
Write-Host "  ───────────────" -ForegroundColor Gray

foreach ($phase in $phases) {
    $willRun = $false
    if ($OnlyPhase -ge 0) {
        $willRun = ($phase.Num -eq $OnlyPhase)
    } else {
        $willRun = ($phase.Num -ge $FromPhase)
    }
    if ($phase.Num -eq 6 -and $SkipMerge) { $willRun = $false }

    $status = if ($willRun) { "RUN " } else { "SKIP" }
    $color = if ($willRun) { "Green" } else { "DarkGray" }
    Write-Host ("  [{0}] Phase {1}: {2,-26} — {3}" -f $status, $phase.Num, $phase.Name, $phase.Desc) -ForegroundColor $color
}

if ($DryRun) {
    Write-Host "`n  DRY RUN — No phases executed." -ForegroundColor Yellow
    exit 0
}

Write-Host ""

# ── Precondition Checks ──
Write-Host "  PRECONDITION CHECKS:" -ForegroundColor White

# gh CLI
$ghVersion = gh --version 2>&1 | Select-Object -First 1
if ($LASTEXITCODE -ne 0) { throw "GitHub CLI (gh) not found. Install from https://cli.github.com/" }
Write-Host "  [OK] gh CLI: $ghVersion" -ForegroundColor Green

# git
$gitVersion = git --version 2>&1
if ($LASTEXITCODE -ne 0) { throw "git not found." }
Write-Host "  [OK] git: $gitVersion" -ForegroundColor Green

# Authentication
$ghUser = gh api user --jq ".login" 2>&1
if ($LASTEXITCODE -ne 0) { throw "gh CLI not authenticated. Run: gh auth login" }
Write-Host "  [OK] Authenticated as: $ghUser" -ForegroundColor Green

# Service root
if (-not (Test-Path $ServiceRoot)) { throw "Service root not found: $ServiceRoot" }
Write-Host "  [OK] Service root: $ServiceRoot" -ForegroundColor Green

# FE migration prompt
if (-not (Test-Path (Join-Path $ServiceRoot $FEPromptFile))) {
    throw "FE migration prompt not found: $FEPromptFile"
}
Write-Host "  [OK] FE prompt: $FEPromptFile" -ForegroundColor Green

# Maven (optional — only needed for Phase 6)
$mvnCheck = mvn -version 2>&1 | Select-Object -First 1
if ($LASTEXITCODE -eq 0) {
    Write-Host "  [OK] Maven: $mvnCheck" -ForegroundColor Green
} else {
    Write-Host "  [--] Maven: not found (needed for Phase 6 build verify)" -ForegroundColor Yellow
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
    if ($phase.Num -eq 6 -and $SkipMerge) { $willRun = $false }
    if (-not $willRun) { continue }

    $phaseStart = Get-Date
    $scriptPath = Join-Path $PSScriptRoot $phase.Script

    try {
        & $scriptPath
        $executedPhases++
        $elapsed = (Get-Date) - $phaseStart
        Write-Host "`n  Phase $($phase.Num) completed in $([math]::Round($elapsed.TotalSeconds))s`n" -ForegroundColor Green
    }
    catch {
        Write-Host "`n  Phase $($phase.Num) FAILED: $_" -ForegroundColor Red
        Write-Host "  Resume with: .\run-all.ps1 -FromPhase $($phase.Num)" -ForegroundColor Yellow
        exit 1
    }

    # Pause between phases 4 and 5 to let Copilot start working
    if ($phase.Num -eq 4 -and $OnlyPhase -lt 0) {
        Write-Host ""
        Write-Host "  ┌─────────────────────────────────────────────────────────────┐" -ForegroundColor Yellow
        Write-Host "  │  Copilot agent is now working on FE issues.                 │" -ForegroundColor Yellow
        Write-Host "  │  Phase 5 (Monitor) will show real-time progress.            │" -ForegroundColor Yellow
        Write-Host "  │  Phase 6 (Merge) should run AFTER all PRs are generated.    │" -ForegroundColor Yellow
        Write-Host "  │                                                             │" -ForegroundColor Yellow
        Write-Host "  │  Press ENTER to continue to monitoring, or Ctrl+C to stop.  │" -ForegroundColor Yellow
        Write-Host "  └─────────────────────────────────────────────────────────────┘" -ForegroundColor Yellow
        Read-Host "  "
    }
}

# ── Summary ──
$totalElapsed = (Get-Date) - $startTime

Write-Host ""
Write-Host "╔══════════════════════════════════════════════════════════════════════╗" -ForegroundColor Green
Write-Host "║  FE PIPELINE COMPLETE                                                ║" -ForegroundColor Green
Write-Host "║                                                                      ║" -ForegroundColor Green
Write-Host "║  Service:          $ServiceName" -ForegroundColor Green
Write-Host "║  Phases executed:  $executedPhases                                              ║" -ForegroundColor Green
Write-Host "║  Total time:       $([math]::Round($totalElapsed.TotalMinutes, 1)) minutes                                          ║" -ForegroundColor Green
Write-Host "║                                                                      ║" -ForegroundColor Green
Write-Host "║  Outputs:                                                            ║" -ForegroundColor Green
Write-Host "║  • FE context:     $FEContextDir/fe-prompt-context.json              ║" -ForegroundColor Green
Write-Host "║  • Issue bodies:   $FEIssueBodiesDir/                                ║" -ForegroundColor Green
Write-Host "║  • FE code:        $FEOutputDir/                                     ║" -ForegroundColor Green
Write-Host "║                                                                      ║" -ForegroundColor Green
Write-Host "║  Post-pipeline:                                                      ║" -ForegroundColor Green
Write-Host "║  1. Run tests:     cd $FEOutputDir && mvn clean verify               ║" -ForegroundColor Green
Write-Host "║  2. Review code:   https://github.com/$GitHubOrg/$ServiceName        ║" -ForegroundColor Green
Write-Host "║  3. Monitor:       .\phase-5-monitor.ps1 -Watch                     ║" -ForegroundColor Green
Write-Host "╚══════════════════════════════════════════════════════════════════════╝" -ForegroundColor Green
