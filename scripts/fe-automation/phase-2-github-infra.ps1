# ============================================================================
# Phase 2 — GitHub Infrastructure for FE
# ============================================================================
# Creates: FE-specific labels, milestones, issue templates, PR template,
#          project board, and GitHub Discussions.
# Idempotent — skips items that already exist.
# ============================================================================

$ErrorActionPreference = "Stop"
. "$PSScriptRoot\config.ps1"
. "$PSScriptRoot\lib\helpers.ps1"
Write-Phase "PHASE 2" "GitHub Infrastructure — Labels, Milestones, Templates, Project Board, Discussions"

Set-Location $ServiceRoot

# ── Step 2.1: Create FE Labels ──
Write-Step "2.1" "Creating $($FELabels.Count) FE labels..."
$existingLabels = gh label list --json name --jq ".[].name" --limit 200 2>&1
foreach ($label in $FELabels) {
    $exists = $existingLabels | Select-String -Pattern "^$([regex]::Escape($label.Name))$" -SimpleMatch
    if ($exists) {
        Write-Skip "Label '$($label.Name)' exists."
    } else {
        gh label create $label.Name --color $label.Color --description $label.Desc 2>&1 | Out-Null
        Write-Ok "Created: $($label.Name)"
    }
}

# ── Step 2.2: Create FE Milestones ──
Write-Step "2.2" "Creating $($FEMilestones.Count) FE milestones..."
$existingMS = gh api "repos/$GitHubOrg/$ServiceName/milestones" --jq ".[].title" 2>&1
foreach ($ms in $FEMilestones) {
    if ($existingMS -match [regex]::Escape($ms.Title)) {
        Write-Skip "Milestone '$($ms.Title)' exists."
    } else {
        gh api "repos/$GitHubOrg/$ServiceName/milestones" --method POST `
            -f title="$($ms.Title)" `
            -f description="$($ms.Description)" `
            -f state="open" 2>&1 | Out-Null
        Write-Ok "Created: $($ms.Title)"
    }
}

# ── Step 2.3: Create FE Issue Template ──
Write-Step "2.3" "Creating FE issue template..."
$issueTemplateDir = ".github/ISSUE_TEMPLATE"
New-Item -ItemType Directory -Path $issueTemplateDir -Force | Out-Null

$feTemplate = @"
name: "FE Migration Task"
description: "Forward Engineering migration issue for Copilot coding agent"
labels: ["phase:FE", "type:migration"]
body:
  - type: textarea
    id: migration-prompt
    attributes:
      label: Migration Prompt
      description: Self-contained instructions for the Copilot coding agent
    validations:
      required: true
  - type: textarea
    id: files-to-create
    attributes:
      label: Files to Create/Modify
      description: List of files this issue should produce
    validations:
      required: true
  - type: textarea
    id: dependencies
    attributes:
      label: Dependencies
      description: FE issues that must be completed before this one
    validations:
      required: false
  - type: textarea
    id: acceptance-criteria
    attributes:
      label: Acceptance Criteria
      description: What must be true when this issue is done
    validations:
      required: true
"@
Set-Content -Path "$issueTemplateDir/fe-migration.yml" -Value $feTemplate -Encoding UTF8
Write-Ok "Created: fe-migration.yml"

# ── Step 2.4: Create/Update PR Template ──
Write-Step "2.4" "Creating FE PR template..."
$prTemplate = @"
## Summary
<!-- Brief description of what this PR implements -->

## FE Issue
<!-- Which FE issue does this PR implement? -->
- [ ] FE-001 Scaffolding
- [ ] FE-002 Config
- [ ] FE-003 Models
- [ ] FE-004 WSDLs
- [ ] FE-005 REST Controller
- [ ] FE-006 SOAP Endpoints
- [ ] FE-007 Validation Service
- [ ] FE-008 Scoring Strategies
- [ ] FE-009 Bureau Service
- [ ] FE-010 Risk Calculation
- [ ] FE-011 Orchestration
- [ ] FE-012 MongoDB Logging
- [ ] FE-013 Security Config
- [ ] FE-014 Error Handling
- [ ] FE-015 Tests
- [ ] FE-016 CI/CD

## Incremental Build Checklist
- [ ] All files are under ``forward-engineering/src/`` (NOT repo root ``src/``)
- [ ] pom.xml was NOT recreated (only dependencies added)
- [ ] .gitignore was NOT modified
- [ ] Existing files were modified, not recreated
- [ ] All imports use ``com.nexgen.sb.creditrisk`` (NOT ``com.nexgen.esb``)
- [ ] All imports use ``jakarta.*`` (NOT ``javax.*``)

## Linked Issue
Closes #
"@
Set-Content -Path ".github/PULL_REQUEST_TEMPLATE.md" -Value $prTemplate -Encoding UTF8
Write-Ok "Created: PULL_REQUEST_TEMPLATE.md"

# ── Step 2.5: Create FE Project Board ──
Write-Step "2.5" "Creating FE project board..."
$boardName = "$ServiceName — Forward Engineering"
$boardNum = Get-ProjectBoardNumber -BoardTitle $boardName -Create
if ($boardNum) {
    Write-Ok "Project board ready: #$boardNum — $boardName"
} else {
    Write-Warn "Failed to create project board."
}

# ── Step 2.6: Create GitHub Discussions ──
Write-Step "2.6" "Creating GitHub Discussions..."

# Ensure Discussions are enabled (requires API)
foreach ($disc in $FEDiscussions) {
    # Get discussion category ID
    $catQuery = "query { repository(owner: `"$GitHubOrg`", name: `"$ServiceName`") { discussionCategories(first: 20) { nodes { id, name } } } }"
    $catResult = gh api graphql -f query="$catQuery" 2>&1 | ConvertFrom-Json
    $category = $catResult.data.repository.discussionCategories.nodes | Where-Object { $_.name -eq $disc.Category }

    if (-not $category) {
        Write-Warn "Discussion category '$($disc.Category)' not found. Ensure Discussions are enabled."
        continue
    }

    # Check if discussion already exists
    $existQuery = "query { repository(owner: `"$GitHubOrg`", name: `"$ServiceName`") { discussions(first: 50) { nodes { title } } } }"
    $existResult = gh api graphql -f query="$existQuery" 2>&1 | ConvertFrom-Json
    $existing = $existResult.data.repository.discussions.nodes | Where-Object { $_.title -eq $disc.Title }

    if ($existing) {
        Write-Skip "Discussion '$($disc.Title)' already exists."
        continue
    }

    # Create discussion
    $repoIdQuery = "query { repository(owner: `"$GitHubOrg`", name: `"$ServiceName`") { id } }"
    $repoId = (gh api graphql -f query="$repoIdQuery" 2>&1 | ConvertFrom-Json).data.repository.id

    $createMutation = "mutation { createDiscussion(input: { repositoryId: `"$repoId`", categoryId: `"$($category.id)`", title: `"$($disc.Title)`", body: `"$($disc.Body)`" }) { discussion { number, url } } }"
    $createResult = gh api graphql -f query="$createMutation" 2>&1 | ConvertFrom-Json

    if ($createResult.data.createDiscussion.discussion) {
        Write-Ok "Created discussion #$($createResult.data.createDiscussion.discussion.number): $($disc.Title)"
    } else {
        Write-Warn "Failed to create discussion: $($disc.Title)"
    }
}

# ── Step 2.7: Commit infrastructure files ──
Write-Step "2.7" "Committing infrastructure files..."
Invoke-GitCommitAndPush -Message "chore(fe): add FE issue templates, PR template, labels"

Write-Host "`n[Phase 2] COMPLETE — GitHub infrastructure ready" -ForegroundColor Green
Write-Host "  Labels:      $($FELabels.Count) created/verified" -ForegroundColor Cyan
Write-Host "  Milestones:  $($FEMilestones.Count) created/verified" -ForegroundColor Cyan
Write-Host "  Discussions: $($FEDiscussions.Count) created/verified" -ForegroundColor Cyan
Write-Host "  Board:       $boardName (#$boardNum)" -ForegroundColor Cyan
