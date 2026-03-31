# ============================================================================
# Phase 4 — Issue Filing & Enrichment
# ============================================================================
# Replicates: Generate enriched issue body → gh issue create → add labels →
#             assign milestone → add to project board
# Uses the architecture context from Phase 2 to auto-enrich issue bodies.
# ============================================================================

$ErrorActionPreference = "Stop"
. "$PSScriptRoot\config.ps1"
Write-Phase "PHASE 4" "Issue Filing — Create 10 enriched RE issues on GitHub"

Set-Location $ServiceRoot

# ── Step 4.1: Load architecture context from Phase 2 ──
Write-Host "[4.1] Loading architecture context..." -ForegroundColor Yellow
$contextPath = Join-Path $ContextDir "architecture-context.json"
if (-not (Test-Path $contextPath)) {
    throw "Architecture context not found at $contextPath. Run Phase 2 first."
}
$ctx = Get-Content $contextPath -Raw | ConvertFrom-Json
Write-Host "  Loaded: $($ctx.summary.totalJavaClasses) classes, $($ctx.summary.totalPackages) packages, $($ctx.summary.totalBeans) beans, $($ctx.summary.totalRoutes) routes" -ForegroundColor Green

# ── Step 4.2: Get milestone number for M1 ──
Write-Host "`n[4.2] Resolving milestone M1..." -ForegroundColor Yellow
$milestoneNum = gh api "repos/$GitHubOrg/$ServiceName/milestones" --jq '.[] | select(.title | startswith("M1")) | .number' 2>&1
if (-not $milestoneNum) {
    Write-Host "  WARNING: Milestone M1 not found. Issues will have no milestone." -ForegroundColor Red
} else {
    Write-Host "  M1 milestone number: $milestoneNum" -ForegroundColor Green
}

# ── Step 4.3: Build architecture summary block (reusable across issues) ──
$archSummary = @"
### Codebase Architecture Summary

| Attribute | Value |
|-----------|-------|
| **Total Java Classes** | $($ctx.summary.totalJavaClasses) |
| **Packages** | $($ctx.summary.totalPackages) |
| **Blueprint Beans** | $($ctx.summary.totalBeans) |
| **Camel Routes** | $($ctx.summary.totalRoutes) |
| **Dependencies** | $($ctx.summary.totalDependencies) |
| **Config Properties** | $($ctx.summary.totalProperties) |
| **Total Files** | $($ctx.summary.totalFiles) |
"@

# Build package table
$pkgTable = "### Package Structure`n`n| Package | Classes | Count |`n|---------|---------|-------|`n"
foreach ($pkg in ($ctx.java.packages.PSObject.Properties | Sort-Object Name)) {
    $classes = $pkg.Value -join ", "
    $pkgTable += "| ``$($pkg.Name)`` | $classes | $($pkg.Value.Count) |`n"
}

# Build beans table
$beanTable = "### Blueprint Beans`n`n| Bean ID | Class |`n|---------|-------|`n"
foreach ($bean in $ctx.blueprint.beans) {
    $beanTable += "| ``$($bean.id)`` | ``$($bean.class)`` |`n"
}

# Build routes list
$routeList = "### Camel Routes`n`n"
foreach ($route in $ctx.blueprint.routes) {
    $routeList += "- ``$route```n"
}

# ── Step 4.4: Create issue body files and file issues ──
Write-Host "`n[4.3] Creating $IssueBodiesDir/ directory..." -ForegroundColor Yellow
New-Item -ItemType Directory -Path $IssueBodiesDir -Force | Out-Null

# Check existing issues
$existingIssues = gh issue list --limit 100 --state all --json number,title --jq ".[].title" 2>&1

Write-Host "`n[4.4] Filing $($REManifest.Count) issues..." -ForegroundColor Yellow
$createdIssues = @()

foreach ($re in $REManifest) {
    # Check if issue already exists
    $alreadyExists = $existingIssues | Select-String -Pattern ([regex]::Escape($re.Id)) -SimpleMatch
    if ($alreadyExists) {
        Write-Host "  Issue for $($re.Id) already exists. Skipping." -ForegroundColor Gray
        continue
    }

    # Generate enriched issue body
    $body = @"
## $($re.Id): $($re.Title -replace '^\[RE-\d+\]\s*', '')

### Instructions for Copilot Agent

You are generating a **completed** reverse engineering document for the ``$ServiceName`` service.
Follow the template structure exactly and fill every section with real data extracted from the codebase.

**Template:** [``$TemplateDestDir/$($re.Template)``]($TemplateDestDir/$($re.Template))
**Output file to create:** ``$OutputDir/$($re.Output)``

$archSummary

$pkgTable

$beanTable

$routeList

### Source Files to Analyze

All files in the repository. Key source files:

$(foreach ($cls in $ctx.java.classes) { "- ``$($cls.filePath)`` ($($cls.className) — $($cls.classType), $($cls.loc) LOC)`n" })

### Acceptance Criteria

- [ ] All template sections are filled with actual codebase data
- [ ] No placeholder text remains (no ``_[...]_`` markers)
- [ ] File references use correct repository paths
- [ ] Output file is created at ``$OutputDir/$($re.Output)``
"@

    # Write body file
    $bodyFile = Join-Path $IssueBodiesDir "issue-$($re.Num.ToString('D2')).md"
    Set-Content -Path $bodyFile -Value $body -Encoding UTF8

    # File the issue
    $labelArgs = ($re.Labels | ForEach-Object { "--label `"$_`"" }) -join " "
    $milestoneArg = if ($milestoneNum) { "--milestone $milestoneNum" } else { "" }

    $issueUrl = Invoke-Expression "gh issue create --title `"$($re.Title)`" --body-file `"$bodyFile`" $labelArgs $milestoneArg 2>&1"
    $issueNumber = ($issueUrl -split "/")[-1]
    $createdIssues += @{ Number = $issueNumber; Id = $re.Id; Title = $re.Title }
    Write-Host "  Created: #$issueNumber — $($re.Id)" -ForegroundColor Green

    # Small delay to avoid rate limiting
    Start-Sleep -Milliseconds 500
}

# ── Step 4.5: Add issues to project board ──
Write-Host "`n[4.5] Adding issues to project board..." -ForegroundColor Yellow
$projects = gh project list --owner $GitHubOrg --format json 2>&1 | ConvertFrom-Json
$board = $projects.projects | Where-Object { $_.title -match $ServiceName }

if ($board) {
    foreach ($issue in $createdIssues) {
        gh project item-add $board.number --owner $GitHubOrg --url "https://github.com/$GitHubOrg/$ServiceName/issues/$($issue.Number)" 2>&1 | Out-Null
        Write-Host "  Added #$($issue.Number) to project board" -ForegroundColor Green
    }
} else {
    Write-Host "  WARNING: Project board not found. Skipping board assignment." -ForegroundColor Red
}

# ── Step 4.6: Commit issue body files ──
Write-Host "`n[4.6] Committing issue body files..." -ForegroundColor Yellow
git add $IssueBodiesDir 2>&1 | Out-Null
$hasChanges = git diff --cached --quiet 2>&1; $changed = $LASTEXITCODE -ne 0
if ($changed) {
    git commit -m "chore: add enriched issue body files for all $($REManifest.Count) RE issues" 2>&1 | Out-Null
    git push origin main 2>&1 | Out-Null
    Write-Host "  Committed and pushed." -ForegroundColor Green
} else {
    Write-Host "  No new changes to commit." -ForegroundColor Gray
}

Write-Host "`n[Phase 4] COMPLETE — $($createdIssues.Count) issues filed" -ForegroundColor Green
