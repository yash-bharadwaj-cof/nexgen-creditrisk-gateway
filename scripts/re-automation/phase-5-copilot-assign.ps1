# ============================================================================
# Phase 5 — Copilot Agent Assignment
# ============================================================================
# Replicates: gh issue edit --add-assignee copilot-swe-agent (x10)
# This triggers GitHub's Copilot coding agent to autonomously work on
# each issue, create branches, generate RE documents, and open PRs.
# ============================================================================

$ErrorActionPreference = "Stop"
. "$PSScriptRoot\config.ps1"
Write-Phase "PHASE 5" "Copilot Assignment — Assign copilot-swe-agent to all RE issues"

Set-Location $ServiceRoot

# ── Step 5.1: Verify Copilot agent user exists ──
Write-Host "[5.1] Verifying Copilot agent user..." -ForegroundColor Yellow
$agentCheck = gh api "users/$CopilotAgentUser" --jq ".login,.type" 2>&1
if ($LASTEXITCODE -ne 0) {
    throw "Copilot agent user '$CopilotAgentUser' not found. Ensure Copilot coding agent is enabled for your account."
}
Write-Host "  Agent verified: $($agentCheck -join ' / ')" -ForegroundColor Green

# ── Step 5.2: Find all open RE issues ──
Write-Host "`n[5.2] Finding open RE issues..." -ForegroundColor Yellow
$openIssues = gh issue list --limit 100 --state open --json number,title,assignees 2>&1 | ConvertFrom-Json
$reIssues = $openIssues | Where-Object { $_.title -match "^\[RE-\d+" }
Write-Host "  Found $($reIssues.Count) open RE issues" -ForegroundColor Green

if ($reIssues.Count -eq 0) {
    Write-Host "  No open RE issues to assign. All may be closed/merged already." -ForegroundColor Yellow
    Write-Host "`n[Phase 5] COMPLETE — Nothing to assign" -ForegroundColor Green
    exit 0
}

# ── Step 5.3: Assign Copilot to each issue ──
Write-Host "`n[5.3] Assigning Copilot agent to issues..." -ForegroundColor Yellow
$assigned = 0
$skipped = 0

foreach ($issue in $reIssues) {
    $alreadyAssigned = $issue.assignees | Where-Object { $_.login -eq "Copilot" -or $_.login -eq $CopilotAgentUser }

    if ($alreadyAssigned) {
        Write-Host "  #$($issue.number): Already assigned to Copilot. Skipping." -ForegroundColor Gray
        $skipped++
        continue
    }

    gh issue edit $issue.number --add-assignee $CopilotAgentUser 2>&1 | Out-Null
    if ($LASTEXITCODE -eq 0) {
        Write-Host "  #$($issue.number): Assigned to Copilot — $($issue.title)" -ForegroundColor Green
        $assigned++
    } else {
        Write-Host "  #$($issue.number): FAILED to assign" -ForegroundColor Red
    }

    # Delay between assignments to avoid rate limiting
    Start-Sleep -Seconds 2
}

# ── Step 5.4: Verify assignments ──
Write-Host "`n[5.4] Verifying assignments..." -ForegroundColor Yellow
Start-Sleep -Seconds 3
$verifyIssues = gh issue list --limit 100 --state open --json number,assignees --jq '.[] | select(.assignees | map(.login) | any(. == "Copilot")) | .number' 2>&1
$verifiedCount = ($verifyIssues -split "`n" | Where-Object { $_ -match "\d+" }).Count
Write-Host "  Verified: $verifiedCount issues have Copilot assigned" -ForegroundColor Green

Write-Host "`n[Phase 5] COMPLETE — Assigned: $assigned, Skipped: $skipped" -ForegroundColor Green
Write-Host "  Copilot will now start working autonomously." -ForegroundColor Cyan
Write-Host "  Monitor progress with: .\phase-6-monitor.ps1" -ForegroundColor Cyan
