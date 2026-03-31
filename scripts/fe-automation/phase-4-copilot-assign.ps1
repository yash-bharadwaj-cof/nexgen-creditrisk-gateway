# ============================================================================
# Phase 4 — Assign Copilot Agent to FE Issues
# ============================================================================
# Assigns the copilot-swe-agent to all open FE issues, triggering
# autonomous code generation. Issues are assigned in dependency order
# (wave 1 first, then wave 2, etc.)
# ============================================================================

$ErrorActionPreference = "Stop"
. "$PSScriptRoot\config.ps1"
. "$PSScriptRoot\lib\helpers.ps1"
Write-Phase "PHASE 4" "Copilot Assignment — Assign copilot-swe-agent to all FE issues"

Set-Location $ServiceRoot

# ── Step 4.1: Verify Copilot agent ──
Write-Step "4.1" "Verifying Copilot agent user..."
$agentCheck = gh api "users/$CopilotAgentUser" --jq ".login,.type" 2>&1
if ($LASTEXITCODE -ne 0) {
    throw "Copilot agent '$CopilotAgentUser' not found. Ensure Copilot coding agent is enabled."
}
Write-Ok "Agent verified: $($agentCheck -join ' / ')"

# ── Step 4.2: Load execution order ──
Write-Step "4.2" "Loading execution order..."
$orderPath = Join-Path $FEContextDir "execution-order.json"
if (Test-Path $orderPath) {
    $executionData = Get-Content $orderPath -Raw | ConvertFrom-Json
    $executionOrder = $executionData.order
    Write-Ok "Execution order: $($executionOrder -join ' → ')"
} else {
    $executionOrder = Get-DependencyTree -Manifest $FEManifest
    Write-Ok "Computed execution order: $($executionOrder -join ' → ')"
}

# ── Step 4.3: Find open FE issues ──
Write-Step "4.3" "Finding open FE issues..."
$openIssues = Get-GitHubIssues -TitlePattern "^\[FE-\d+" -State "open"
Write-Ok "Found $($openIssues.Count) open FE issues"

if ($openIssues.Count -eq 0) {
    Write-Host "  No open FE issues to assign. All may be complete." -ForegroundColor Yellow
    Write-Host "`n[Phase 4] COMPLETE — Nothing to assign" -ForegroundColor Green
    exit 0
}

# ── Step 4.4: Assign in execution order ──
Write-Step "4.4" "Assigning Copilot agent to issues..."
$assigned = 0
$skipped = 0

foreach ($feId in $executionOrder) {
    $issue = $openIssues | Where-Object { $_.title -match [regex]::Escape($feId) }
    if (-not $issue) {
        Write-Skip "$feId not found in open issues (may be closed)."
        continue
    }
    $issue = $issue | Select-Object -First 1

    $alreadyAssigned = $issue.assignees | Where-Object { $_.login -eq "Copilot" -or $_.login -eq $CopilotAgentUser }
    if ($alreadyAssigned) {
        Write-Skip "#$($issue.number): $feId already assigned. Skipping."
        $skipped++
        continue
    }

    gh issue edit $issue.number --add-assignee $CopilotAgentUser 2>&1 | Out-Null
    if ($LASTEXITCODE -eq 0) {
        $wave = ($FEManifest | Where-Object { $_.Id -eq $feId }).Wave
        Write-Ok "#$($issue.number): $feId assigned (Wave $wave)"
        $assigned++
    } else {
        Write-Warn "#$($issue.number): $feId FAILED to assign"
    }

    Start-Sleep -Seconds 2
}

# ── Step 4.5: Verify ──
Write-Step "4.5" "Verifying assignments..."
Start-Sleep -Seconds 3
$verified = Get-GitHubIssues -TitlePattern "^\[FE-\d+" -State "open"
$withCopilot = $verified | Where-Object { $_.assignees | Where-Object { $_.login -eq "Copilot" -or $_.login -eq $CopilotAgentUser } }
Write-Ok "Verified: $($withCopilot.Count) issues assigned to Copilot"

Write-Host "`n[Phase 4] COMPLETE — Assigned: $assigned, Skipped: $skipped" -ForegroundColor Green
Write-Host "  Copilot will start generating code autonomously." -ForegroundColor Cyan
Write-Host "  Monitor with: .\phase-5-monitor.ps1 -Watch" -ForegroundColor Cyan
