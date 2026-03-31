# ============================================================================
# Phase 5 — Monitor FE PRs & Dashboard
# ============================================================================
# Displays a live dashboard showing FE issue status, PR status, and
# overall completion progress. Use -Watch for continuous polling.
# ============================================================================

param(
    [switch]$Watch,
    [int]$IntervalSeconds = 60
)

$ErrorActionPreference = "Stop"
. "$PSScriptRoot\config.ps1"
. "$PSScriptRoot\lib\helpers.ps1"
Write-Phase "PHASE 5" "Monitor — Track FE issue completion and PR status"

Set-Location $ServiceRoot

function Show-FEDashboard {
    $timestamp = Get-Date -Format "yyyy-MM-dd HH:mm:ss"

    Write-Host ""
    Write-Host "╔═══════════════════════════════════════════════════════════════════════════╗" -ForegroundColor Cyan
    Write-Host "║  FE AUTOMATION DASHBOARD — $timestamp                             ║" -ForegroundColor Cyan
    Write-Host "║  Repo: $GitHubOrg/$ServiceName                                            " -ForegroundColor Cyan
    Write-Host "╠═══════════════════════════════════════════════════════════════════════════╣" -ForegroundColor Cyan

    # ── Issues ──
    Write-Host "║                                                                           ║" -ForegroundColor Cyan
    Write-Host "║  ISSUES ($($FEManifest.Count) total)                                                     ║" -ForegroundColor Cyan
    Write-Host "╠═══════════════════════════════════════════════════════════════════════════╣" -ForegroundColor Cyan

    $allIssues = Get-GitHubIssues -TitlePattern "^\[FE-\d+" -State "all"
    $openCount = 0; $closedCount = 0

    # Group by wave
    foreach ($wave in ($FEManifest | Group-Object Wave | Sort-Object Name)) {
        Write-Host "  Wave $($wave.Name):" -ForegroundColor White

        foreach ($feItem in $wave.Group) {
            $ghIssue = $allIssues | Where-Object { $_.title -match [regex]::Escape($feItem.Id) } | Select-Object -First 1

            if (-not $ghIssue) {
                Write-Host ("    [ --- ] {0,-8} {1}" -f $feItem.Id, ($feItem.Title -replace '^\[FE-\d+\]\s*', '').Substring(0, [Math]::Min(55, ($feItem.Title -replace '^\[FE-\d+\]\s*', '').Length))) -ForegroundColor DarkGray
                continue
            }

            if ($ghIssue.state -eq "CLOSED") {
                $icon = "[DONE]"; $closedCount++; $color = "Green"
            } else {
                $icon = "[OPEN]"; $openCount++; $color = "Yellow"
            }
            $shortTitle = ($feItem.Title -replace '^\[FE-\d+\]\s*', '')
            if ($shortTitle.Length -gt 50) { $shortTitle = $shortTitle.Substring(0, 50) + "..." }
            $assignees = ($ghIssue.assignees | ForEach-Object { $_.login }) -join ", "
            Write-Host ("    {0,-8} #{1,-4} {2,-55} {3}" -f $icon, $ghIssue.number, $shortTitle, $assignees) -ForegroundColor $color
        }
    }

    Write-Host "`n  Issues: $closedCount closed / $openCount open / $($FEManifest.Count) total" -ForegroundColor White

    # ── PRs ──
    Write-Host ""
    Write-Host "╠═══════════════════════════════════════════════════════════════════════════╣" -ForegroundColor Cyan
    Write-Host "║  PULL REQUESTS                                                            ║" -ForegroundColor Cyan
    Write-Host "╠═══════════════════════════════════════════════════════════════════════════╣" -ForegroundColor Cyan

    $allPRs = Get-GitHubPRs -BranchPattern "copilot/fe-|copilot/fix-" -State "all"
    $mergedCount = 0; $openPRCount = 0; $conflictCount = 0

    foreach ($pr in ($allPRs | Sort-Object { $_.number })) {
        $stateIcon = switch ($pr.state) {
            "MERGED" { "[MERGED]"; $mergedCount++ }
            "OPEN"   {
                if ($pr.mergeable -eq "CONFLICTING") {
                    "[CONFLICT]"; $conflictCount++; $openPRCount++
                } else {
                    "[OPEN]   "; $openPRCount++
                }
            }
            "CLOSED" { "[CLOSED]" }
            default  { "[$($pr.state)]" }
        }
        $stateColor = switch ($pr.state) {
            "MERGED" { "Green" }
            "OPEN"   { if ($pr.mergeable -eq "CONFLICTING") { "Red" } else { "Yellow" } }
            default  { "DarkGray" }
        }
        $shortTitle = $pr.title
        if ($shortTitle.Length -gt 55) { $shortTitle = $shortTitle.Substring(0, 55) + "..." }
        Write-Host ("  {0,-12} PR #{1,-4} {2}" -f $stateIcon, $pr.number, $shortTitle) -ForegroundColor $stateColor
    }

    Write-Host "`n  PRs: $mergedCount merged, $openPRCount open ($conflictCount conflicts), $($allPRs.Count) total" -ForegroundColor White

    # ── Progress Bar ──
    Write-Host ""
    Write-Host "╠═══════════════════════════════════════════════════════════════════════════╣" -ForegroundColor Cyan
    $totalTarget = $FEManifest.Count
    $pctComplete = if ($totalTarget -gt 0) { [math]::Round(($closedCount / $totalTarget) * 100) } else { 0 }
    $barFull = [math]::Floor($pctComplete / 5)
    $barEmpty = 20 - $barFull
    $bar = ("█" * $barFull) + ("░" * $barEmpty)
    $barColor = if ($pctComplete -eq 100) { "Green" } elseif ($pctComplete -ge 50) { "Cyan" } else { "Yellow" }
    Write-Host "  PROGRESS: [$bar] $pctComplete% ($closedCount/$totalTarget issues closed)" -ForegroundColor $barColor

    if ($conflictCount -gt 0) {
        Write-Host "  WARNING:  $conflictCount PR(s) have merge conflicts — run Phase 6 to resolve" -ForegroundColor Red
    }

    Write-Host "╚═══════════════════════════════════════════════════════════════════════════╝" -ForegroundColor Cyan

    return @{
        Complete      = ($closedCount -eq $totalTarget)
        IssuesClosed  = $closedCount
        IssuesOpen    = $openCount
        PRsMerged     = $mergedCount
        PRsOpen       = $openPRCount
        PRsConflict   = $conflictCount
    }
}

# ── Run ──
$status = Show-FEDashboard

if ($Watch) {
    Write-Host "`n  Watching... (Ctrl+C to stop, polling every ${IntervalSeconds}s)" -ForegroundColor Gray
    while (-not $status.Complete) {
        Start-Sleep -Seconds $IntervalSeconds
        Clear-Host
        $status = Show-FEDashboard
    }
    Write-Host "`n  ALL FE ISSUES COMPLETE. Forward Engineering done!" -ForegroundColor Green
} else {
    if ($status.Complete) {
        Write-Host "`n[Phase 5] ALL COMPLETE — All $($FEManifest.Count) FE issues closed!" -ForegroundColor Green
    } else {
        Write-Host "`n[Phase 5] IN PROGRESS" -ForegroundColor Yellow
        Write-Host "  Run with -Watch: .\phase-5-monitor.ps1 -Watch" -ForegroundColor Gray
        if ($status.PRsConflict -gt 0) {
            Write-Host "  Resolve conflicts: .\phase-6-merge-consolidate.ps1" -ForegroundColor Red
        }
    }
}
