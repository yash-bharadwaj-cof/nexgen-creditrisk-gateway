# ============================================================================
# Phase 6 — Monitor PRs & Completion
# ============================================================================
# Replicates: gh pr list + gh issue list — the monitoring we did at the end
# Polls GitHub for PR status and displays a dashboard.
# ============================================================================

param(
    [switch]$Watch,              # Continuously poll every 60 seconds
    [int]$IntervalSeconds = 60   # Poll interval (only with -Watch)
)

$ErrorActionPreference = "Stop"
. "$PSScriptRoot\config.ps1"
Write-Phase "PHASE 6" "Monitor — Track Copilot PR generation and issue completion"

Set-Location $ServiceRoot

function Show-Dashboard {
    Write-Host "`n╔══════════════════════════════════════════════════════════════════════╗" -ForegroundColor Cyan
    Write-Host "║  RE AUTOMATION DASHBOARD — $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')                        ║" -ForegroundColor Cyan
    Write-Host "║  Repo: $GitHubOrg/$ServiceName                                       " -ForegroundColor Cyan
    Write-Host "╠══════════════════════════════════════════════════════════════════════╣" -ForegroundColor Cyan

    # ── Issues Status ──
    Write-Host "║                                                                      ║" -ForegroundColor Cyan
    Write-Host "║  ISSUES                                                              ║" -ForegroundColor Cyan
    Write-Host "╠══════════════════════════════════════════════════════════════════════╣" -ForegroundColor Cyan

    $allIssues = gh issue list --limit 100 --state all --json number,title,state,assignees 2>&1 | ConvertFrom-Json
    $reIssues = $allIssues | Where-Object { $_.title -match "^\[RE-\d+" } | Sort-Object { $_.number }

    $openCount = 0; $closedCount = 0
    foreach ($issue in $reIssues) {
        $stateIcon = if ($issue.state -eq "CLOSED") { "[DONE]"; $closedCount++ } else { "[OPEN]"; $openCount++ }
        $stateColor = if ($issue.state -eq "CLOSED") { "Green" } else { "Yellow" }
        $assignees = ($issue.assignees | ForEach-Object { $_.login }) -join ", "
        Write-Host ("  {0,-8} #{1,-4} {2,-55} {3}" -f $stateIcon, $issue.number, ($issue.title.Substring(0, [Math]::Min(55, $issue.title.Length))), $assignees) -ForegroundColor $stateColor
    }

    Write-Host "`n  Summary: $closedCount/$($reIssues.Count) issues closed" -ForegroundColor White

    # ── PRs Status ──
    Write-Host "`n╠══════════════════════════════════════════════════════════════════════╣" -ForegroundColor Cyan
    Write-Host "║  PULL REQUESTS                                                       ║" -ForegroundColor Cyan
    Write-Host "╠══════════════════════════════════════════════════════════════════════╣" -ForegroundColor Cyan

    $allPRs = gh pr list --state all --json number,title,state,headRefName 2>&1 | ConvertFrom-Json
    $rePRs = $allPRs | Where-Object { $_.headRefName -match "copilot/re-" } | Sort-Object { $_.number }

    $mergedCount = 0; $openPRCount = 0
    foreach ($pr in $rePRs) {
        $stateIcon = switch ($pr.state) {
            "MERGED" { "[MERGED]"; $mergedCount++ }
            "OPEN"   { "[OPEN]  "; $openPRCount++ }
            "CLOSED" { "[CLOSED]" }
            default  { "[$($pr.state)]" }
        }
        $stateColor = switch ($pr.state) {
            "MERGED" { "Green" }
            "OPEN"   { "Yellow" }
            default  { "Red" }
        }
        Write-Host ("  {0,-10} PR #{1,-4} {2}" -f $stateIcon, $pr.number, $pr.title.Substring(0, [Math]::Min(55, $pr.title.Length))) -ForegroundColor $stateColor
    }

    Write-Host "`n  Summary: $mergedCount merged, $openPRCount open out of $($rePRs.Count) total PRs" -ForegroundColor White

    # ── Completion Assessment ──
    Write-Host "`n╠══════════════════════════════════════════════════════════════════════╣" -ForegroundColor Cyan
    $totalDone = $mergedCount
    $pctComplete = if ($REManifest.Count -gt 0) { [math]::Round(($totalDone / $REManifest.Count) * 100) } else { 0 }
    $bar = ("█" * [math]::Floor($pctComplete / 5)) + ("░" * (20 - [math]::Floor($pctComplete / 5)))
    Write-Host "  PROGRESS: [$bar] $pctComplete% ($totalDone/$($REManifest.Count) merged)" -ForegroundColor $(if ($pctComplete -eq 100) { "Green" } else { "Cyan" })
    Write-Host "╚══════════════════════════════════════════════════════════════════════╝" -ForegroundColor Cyan

    # Return completion status
    return @{ Complete = ($mergedCount -eq $REManifest.Count); Merged = $mergedCount; Open = $openPRCount }
}

# ── Run once or watch ──
$status = Show-Dashboard

if ($Watch) {
    Write-Host "`n  Watching... (Ctrl+C to stop, polling every ${IntervalSeconds}s)" -ForegroundColor Gray
    while (-not $status.Complete) {
        Start-Sleep -Seconds $IntervalSeconds
        Clear-Host
        $status = Show-Dashboard
    }
    Write-Host "`n  ALL RE DOCUMENTS MERGED. Pipeline complete!" -ForegroundColor Green
} else {
    if ($status.Complete) {
        Write-Host "`n[Phase 6] ALL COMPLETE — All $($REManifest.Count) RE documents merged!" -ForegroundColor Green
    } else {
        Write-Host "`n[Phase 6] IN PROGRESS — $($status.Merged) merged, $($status.Open) PRs open" -ForegroundColor Yellow
        Write-Host "  Run with -Watch to continuously monitor: .\phase-6-monitor.ps1 -Watch" -ForegroundColor Gray
    }
}
