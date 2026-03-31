# ============================================================================
# Phase 1 — GitHub Infrastructure Setup
# ============================================================================
# Replicates: 24 labels → 2 milestones → issue templates → PR template →
#             project board creation & linking
# ============================================================================

$ErrorActionPreference = "Stop"
. "$PSScriptRoot\config.ps1"
Write-Phase "PHASE 1" "GitHub Infrastructure — Labels, Milestones, Templates, Project Board"

Set-Location $ServiceRoot

# ── Step 1.1: Create 24 Labels ──
Write-Host "[1.1] Creating $($Labels.Count) labels..." -ForegroundColor Yellow
foreach ($label in $Labels) {
    $existing = gh label list --json name --jq ".[].name" 2>&1 | Select-String -Pattern "^$([regex]::Escape($label.Name))$" -SimpleMatch
    if ($existing) {
        Write-Host "  Label '$($label.Name)' already exists. Skipping." -ForegroundColor Gray
    } else {
        gh label create $label.Name --color $label.Color --description $label.Desc 2>&1 | Out-Null
        Write-Host "  Created: $($label.Name)" -ForegroundColor Green
    }
}

# ── Step 1.2: Create Milestones ──
Write-Host "`n[1.2] Creating $($Milestones.Count) milestones..." -ForegroundColor Yellow
$existingMilestones = gh api "repos/$GitHubOrg/$ServiceName/milestones" --jq ".[].title" 2>&1
foreach ($ms in $Milestones) {
    if ($existingMilestones -match [regex]::Escape($ms.Title)) {
        Write-Host "  Milestone '$($ms.Title)' already exists. Skipping." -ForegroundColor Gray
    } else {
        gh api "repos/$GitHubOrg/$ServiceName/milestones" --method POST `
            -f title="$($ms.Title)" `
            -f description="$($ms.Description)" `
            -f state="open" 2>&1 | Out-Null
        Write-Host "  Created: $($ms.Title)" -ForegroundColor Green
    }
}

# ── Step 1.3: Create Issue Templates ──
Write-Host "`n[1.3] Creating issue templates..." -ForegroundColor Yellow
$issueTemplateDir = ".github/ISSUE_TEMPLATE"
New-Item -ItemType Directory -Path $issueTemplateDir -Force | Out-Null

# re-analysis.yml
$reAnalysisTemplate = @"
name: "RE Analysis Task"
description: "Reverse Engineering analysis issue"
labels: ["phase:RE", "type:analysis"]
body:
  - type: textarea
    id: analysis-prompt
    attributes:
      label: Analysis Prompt
      description: Describe what to analyze
    validations:
      required: true
  - type: textarea
    id: scope
    attributes:
      label: Scope
      description: Files and components in scope
    validations:
      required: true
  - type: textarea
    id: acceptance-criteria
    attributes:
      label: Acceptance Criteria
      description: What constitutes a complete analysis
    validations:
      required: true
"@
Set-Content -Path "$issueTemplateDir/re-analysis.yml" -Value $reAnalysisTemplate -Encoding UTF8
Write-Host "  Created: re-analysis.yml" -ForegroundColor Green

# re-verification.yml
$reVerifyTemplate = @"
name: "RE Verification Task"
description: "Verify a reverse engineering document against source code"
labels: ["phase:VV", "type:analysis"]
body:
  - type: textarea
    id: verification-scope
    attributes:
      label: Verification Scope
      description: Which RE document to verify and against what
    validations:
      required: true
  - type: textarea
    id: verification-checklist
    attributes:
      label: Verification Checklist
      description: Specific items to verify
    validations:
      required: true
"@
Set-Content -Path "$issueTemplateDir/re-verification.yml" -Value $reVerifyTemplate -Encoding UTF8
Write-Host "  Created: re-verification.yml" -ForegroundColor Green

# config.yml (chooser)
$configYml = @"
blank_issues_enabled: true
contact_links:
  - name: RE/FE Process Guide
    url: https://github.com/$GitHubOrg/$ServiceName/blob/main/docs/RE-AUTOMATION-PROCESS-SUMMARY.md
    about: Read the RE automation process guide
"@
Set-Content -Path "$issueTemplateDir/config.yml" -Value $configYml -Encoding UTF8
Write-Host "  Created: config.yml" -ForegroundColor Green

# ── Step 1.4: Create PR Template ──
Write-Host "`n[1.4] Creating PR template..." -ForegroundColor Yellow
$prTemplate = @"
## Summary
<!-- Brief description of changes -->

## RE Document
<!-- Which RE document does this PR deliver? -->
- [ ] RE-001 Discovery Report
- [ ] RE-002 Component Catalog
- [ ] RE-003 Sequence Diagrams
- [ ] RE-004 Business Rules Catalog
- [ ] RE-005 Data Dictionary
- [ ] RE-006 Data Flow Diagrams
- [ ] RE-007 BDD Feature Specs
- [ ] RE-008 Test Case Inventory
- [ ] RE-009 Gap Report
- [ ] RE-010 Field-to-Field Mapping

## Checklist
- [ ] All template sections are filled with real codebase data
- [ ] No placeholder text remains (no ``_[...]_`` markers)
- [ ] File references use correct paths
- [ ] Cross-references (BR-XXX, COMP-XXX, GAP-XXX) are valid
- [ ] Output file is in ``docs/re/`` directory

## Linked Issue
Closes #
"@
Set-Content -Path ".github/PULL_REQUEST_TEMPLATE.md" -Value $prTemplate -Encoding UTF8
Write-Host "  Created: PULL_REQUEST_TEMPLATE.md" -ForegroundColor Green

# ── Step 1.5: Create Project Board ──
Write-Host "`n[1.5] Creating project board..." -ForegroundColor Yellow
$existingProjects = gh project list --owner $GitHubOrg --format json 2>&1 | ConvertFrom-Json
$boardName = "$ServiceName - RE/FE Tracker"
$existingBoard = $existingProjects.projects | Where-Object { $_.title -eq $boardName }

if ($existingBoard) {
    Write-Host "  Project '$boardName' already exists (ID: $($existingBoard.number)). Skipping." -ForegroundColor Gray
} else {
    $boardResult = gh project create --owner $GitHubOrg --title $boardName --format json 2>&1 | ConvertFrom-Json
    Write-Host "  Created project board: $boardName (Number: $($boardResult.number))" -ForegroundColor Green

    # Link repo to project
    gh project link $boardResult.number --owner $GitHubOrg --repo "$GitHubOrg/$ServiceName" 2>&1 | Out-Null
    Write-Host "  Linked repo to project board." -ForegroundColor Green
}

# ── Step 1.6: Commit and push infrastructure files ──
Write-Host "`n[1.6] Committing infrastructure files..." -ForegroundColor Yellow
git add .github/ 2>&1 | Out-Null
$hasChanges = git diff --cached --quiet 2>&1; $changed = $LASTEXITCODE -ne 0
if ($changed) {
    git commit -m "chore: add issue templates, PR template, and config" 2>&1 | Out-Null
    git push origin main 2>&1 | Out-Null
    Write-Host "  Committed and pushed." -ForegroundColor Green
} else {
    Write-Host "  No new changes to commit." -ForegroundColor Gray
}

Write-Host "`n[Phase 1] COMPLETE" -ForegroundColor Green
