# ============================================================================
# Phase 3 — File FE Issues on GitHub
# ============================================================================
# Generates enriched, self-contained issue bodies from the FE manifest +
# parsed context, then creates GitHub Issues with labels, milestones,
# and project board assignment.
#
# Each issue body contains:
# - Full Copilot agent instructions
# - Relevant sections from the FE migration prompt
# - Incremental build rules
# - Dependency context
# - Acceptance criteria
# ============================================================================

$ErrorActionPreference = "Stop"
. "$PSScriptRoot\config.ps1"
. "$PSScriptRoot\lib\helpers.ps1"
Write-Phase "PHASE 3" "Issue Filing — Create $($FEManifest.Count) enriched FE issues on GitHub"

Set-Location $ServiceRoot

# ── Step 3.1: Load parsed context from Phase 1 ──
Write-Step "3.1" "Loading FE context from Phase 1..."
$contextPath = Join-Path $FEContextDir "fe-prompt-context.json"
if (-not (Test-Path $contextPath)) {
    throw "FE context not found at $contextPath. Run Phase 1 first."
}
$ctx = Get-Content $contextPath -Raw | ConvertFrom-Json
Write-Ok "Context loaded: $($ctx.issueCount) issues, $($ctx.waveCount) waves"

# ── Step 3.2: Resolve milestone number ──
Write-Step "3.2" "Resolving milestone M3..."
$milestoneNum = Get-MilestoneNumber -TitlePrefix "M3"
if ($milestoneNum) {
    Write-Ok "M3 milestone number: $milestoneNum"
} else {
    Write-Warn "Milestone M3 not found. Issues will have no milestone."
}

# ── Step 3.3: Read the full FE migration prompt ──
Write-Step "3.3" "Reading FE migration prompt sections..."
$sections = Read-FEPrompt -PromptPath $FEPromptFile
$buildRules = if ($sections.ContainsKey(0)) { $sections[0] } else { "" }
Write-Ok "Loaded $($sections.Count) sections + build rules"

# ── Step 3.4: Build reusable context blocks ──
Write-Step "3.4" "Building reusable context blocks..."

# Technology summary
$techSummary = @"
### Technology Stack

| Aspect | Decision |
|--------|----------|
$(foreach ($d in $ctx.technologyDecisions) {
    "| $($d.Name) | $($d.Selected) |`n"
})
"@

# Package mapping
$pkgBlock = @"
### Package Mapping

| Legacy | Target |
|--------|--------|
$(foreach ($p in $ctx.packageMapping) {
    "| ``$($p.Legacy)`` | ``$($p.Target)`` |`n"
})
"@

# Migration rules
$rulesBlock = @"
### Key Migration Rules

$(foreach ($r in $ctx.migrationRules) {
    "- **$($r.Rule)** — $($r.Detail)`n"
})
"@

# ── Step 3.5: Create issue body directory ──
Write-Step "3.5" "Setting up $FEIssueBodiesDir/..."
New-Item -ItemType Directory -Path $FEIssueBodiesDir -Force | Out-Null

# ── Step 3.6: Check existing issues ──
$existingIssues = Get-GitHubIssues -TitlePattern "^\[FE-\d+"
Write-Ok "Found $($existingIssues.Count) existing FE issues"

# ── Step 3.7: Generate and file issues ──
Write-Step "3.7" "Filing $($FEManifest.Count) FE issues..."
$createdIssues = @()

foreach ($fe in $FEManifest) {
    # Check if issue already exists
    $alreadyExists = $existingIssues | Where-Object { $_.title -match [regex]::Escape($fe.Id) }
    if ($alreadyExists) {
        Write-Skip "Issue $($fe.Id) already exists (#$($alreadyExists[0].number)). Skipping."
        $createdIssues += @{ Number = $alreadyExists[0].number; Id = $fe.Id }
        continue
    }

    # Build dependency context
    $depContext = ""
    if ($fe.DependsOn.Count -gt 0) {
        $depContext = @"

### Dependencies

This issue depends on the following issues being completed first:
$(foreach ($dep in $fe.DependsOn) {
    $depItem = $FEManifest | Where-Object { $_.Id -eq $dep }
    "- **$dep**: $($depItem.Title -replace '^\[FE-\d+\]\s*', '')`n"
})

> **IMPORTANT**: These prerequisite issues have already created files in the
> ``$FEOutputDir/`` directory. Do NOT recreate or overwrite those files.
> Import from existing classes and add to existing config files.
"@
    }

    # Extract relevant prompt sections
    $relevantSections = ""
    foreach ($secNum in $fe.Sections) {
        if ($sections.ContainsKey($secNum)) {
            $relevantSections += "`n`n---`n`n$($sections[$secNum])"
        }
    }

    # Build the issue body
    $body = @"
## $($fe.Id): $($fe.Desc)

### Instructions for Copilot Agent

You are implementing a Forward Engineering migration task for the **$ServiceName** service.
This is a **JBoss Fuse 6.3 → Spring Boot 3.3.x** migration.

**All output files MUST be placed under ``$FEOutputDir/src/``** — NOT the repository root ``src/``.

$buildRules

---
$depContext

$techSummary

$pkgBlock

$rulesBlock

### Target Directory Structure

``````
$($ctx.directoryStructure)
``````

---

### Relevant Specification Sections
$relevantSections

---

### Acceptance Criteria

- [ ] All files created under ``$FEOutputDir/src/`` (not repo root)
- [ ] Existing files modified (not recreated)
- [ ] All imports use ``com.nexgen.sb.creditrisk`` package
- [ ] All imports use ``jakarta.*`` (not ``javax.*``)
- [ ] Code compiles: ``cd $FEOutputDir && mvn clean compile``
$(foreach ($ac in $ctx.acceptanceCriteria) {
    "- [ ] $ac`n"
})
"@

    # Write body file
    $bodyFile = Join-Path $FEIssueBodiesDir "$($fe.Id.ToLower()).md"
    Set-Content -Path $bodyFile -Value $body -Encoding UTF8

    # Build label arguments
    $labelArgs = @()
    foreach ($l in $fe.Labels) {
        $labelArgs += "--label"
        $labelArgs += $l
    }
    $milestoneArg = @()
    if ($milestoneNum) {
        $milestoneArg = @("--milestone", $milestoneNum)
    }

    # File the issue
    $issueUrl = & gh issue create --title $fe.Title --body-file $bodyFile @labelArgs @milestoneArg 2>&1
    $issueNumber = ($issueUrl -split "/")[-1]
    $createdIssues += @{ Number = $issueNumber; Id = $fe.Id; Title = $fe.Title }
    Write-Ok "Created: #$issueNumber — $($fe.Id) [$($fe.Labels -join ', ')]"

    Start-Sleep -Milliseconds 500
}

# ── Step 3.8: Add issues to project board ──
Write-Step "3.8" "Adding issues to project board..."
$boardName = "$ServiceName — Forward Engineering"
$boardNum = Get-ProjectBoardNumber -BoardTitle $boardName

if ($boardNum) {
    foreach ($issue in $createdIssues) {
        Add-IssueToProject -IssueNumber $issue.Number -ProjectNumber $boardNum
        Write-Ok "Added #$($issue.Number) ($($issue.Id)) to project board"
    }
} else {
    Write-Warn "Project board not found. Run Phase 2 first."
}

# ── Step 3.9: Commit issue body files ──
Write-Step "3.9" "Committing issue body files..."
Invoke-GitCommitAndPush -Message "chore(fe): add enriched issue bodies for $($FEManifest.Count) FE issues"

# ── Summary ──
Write-Host "`n[Phase 3] COMPLETE — $($createdIssues.Count) FE issues filed" -ForegroundColor Green
Write-Host "  Issues created: $(($createdIssues | Where-Object { $_.Number }).Count)" -ForegroundColor Cyan
Write-Host "  Waves:          $($FEManifest | ForEach-Object { $_.Wave } | Sort-Object -Unique | ForEach-Object { "W$_" })" -ForegroundColor Cyan
Write-Host "  Next:           Run Phase 4 to assign Copilot agent" -ForegroundColor Cyan
