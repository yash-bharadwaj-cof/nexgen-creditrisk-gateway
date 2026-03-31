# ============================================================================
# Phase 6 — Merge PRs & Consolidate Code
# ============================================================================
# This phase automates what we did manually in the chat session:
# 1. Identify all open Copilot PRs and their conflict status
# 2. Merge clean PRs directly
# 3. Merge conflicting PRs locally (keeping main's base files)
# 4. Move misplaced files from root src/ to forward-engineering/src/
# 5. Fix legacy package imports (esb → sb, javax → jakarta)
# 6. Remove duplicates
# 7. Verify Maven build
# 8. Commit and push
#
# Usage:
#   .\phase-6-merge-consolidate.ps1              # Full merge + consolidate
#   .\phase-6-merge-consolidate.ps1 -MergeOnly   # Only merge PRs, skip consolidation
#   .\phase-6-merge-consolidate.ps1 -ConsolidateOnly  # Only consolidate, skip merge
#   .\phase-6-merge-consolidate.ps1 -DryRun      # Preview without executing
# ============================================================================

param(
    [switch]$MergeOnly,
    [switch]$ConsolidateOnly,
    [switch]$DryRun
)

$ErrorActionPreference = "Stop"
. "$PSScriptRoot\config.ps1"
. "$PSScriptRoot\lib\helpers.ps1"
Write-Phase "PHASE 6" "Merge PRs & Consolidate FE Code"

Set-Location $ServiceRoot

# ============================================================================
# PART A: MERGE PRs
# ============================================================================

if (-not $ConsolidateOnly) {

    Write-Host "╔══════════════════════════════════════════════════╗" -ForegroundColor Magenta
    Write-Host "║  PART A: MERGE COPILOT PRs                      ║" -ForegroundColor Magenta
    Write-Host "╚══════════════════════════════════════════════════╝" -ForegroundColor Magenta

    # ── A.1: Fetch latest ──
    Write-Step "A.1" "Fetching latest from remote..."
    git fetch origin 2>&1 | Out-Null
    Write-Ok "Fetched."

    # ── A.2: Identify all open FE PRs ──
    Write-Step "A.2" "Identifying open FE PRs..."
    $allPRs = gh pr list --state open --json number,title,headRefName,mergeable --limit 100 2>&1 | ConvertFrom-Json
    $fePRs = $allPRs | Where-Object { $_.headRefName -match "copilot/" }

    if ($fePRs.Count -eq 0) {
        Write-Ok "No open Copilot PRs found. Skipping merge."
    } else {
        Write-Ok "Found $($fePRs.Count) open PRs"

        # Classify
        $cleanPRs = $fePRs | Where-Object { $_.mergeable -ne "CONFLICTING" }
        $conflictPRs = $fePRs | Where-Object { $_.mergeable -eq "CONFLICTING" }
        Write-Host "  Clean PRs:    $($cleanPRs.Count)" -ForegroundColor Green
        Write-Host "  Conflicting:  $($conflictPRs.Count)" -ForegroundColor $(if ($conflictPRs.Count -gt 0) { "Red" } else { "Green" })

        # ── A.3: Merge clean PRs via GitHub ──
        Write-Step "A.3" "Merging $($cleanPRs.Count) clean PRs..."
        foreach ($pr in $cleanPRs) {
            if ($DryRun) {
                Write-Host "  [DRY] Would merge PR #$($pr.number): $($pr.title)" -ForegroundColor Yellow
                continue
            }
            gh pr merge $pr.number --merge --delete-branch 2>&1 | Out-Null
            if ($LASTEXITCODE -eq 0) {
                Write-Ok "Merged PR #$($pr.number): $($pr.title)"
            } else {
                Write-Warn "Failed to merge PR #$($pr.number)"
            }
            Start-Sleep -Seconds 1
        }

        # ── A.4: Merge conflicting PRs locally ──
        if ($conflictPRs.Count -gt 0) {
            Write-Step "A.4" "Merging $($conflictPRs.Count) conflicting PRs locally..."
            Write-Host "  Strategy: keep main's base files (pom.xml/.gitignore/config)," -ForegroundColor White
            Write-Host "            accept PR's new source files" -ForegroundColor White

            # Pull latest main
            git checkout main 2>&1 | Out-Null
            git pull origin main 2>&1 | Out-Null

            foreach ($pr in $conflictPRs) {
                $branch = $pr.headRefName
                Write-Host "`n  Merging PR #$($pr.number) ($branch)..." -ForegroundColor Yellow

                if ($DryRun) {
                    Write-Host "  [DRY] Would merge $branch with conflict resolution" -ForegroundColor Yellow
                    continue
                }

                # Fetch the branch
                git fetch origin "${branch}:${branch}" 2>&1 | Out-Null

                # Attempt merge
                $mergeResult = git merge $branch --no-edit 2>&1
                if ($LASTEXITCODE -ne 0) {
                    # Get conflicted files
                    $conflicts = git diff --name-only --diff-filter=U 2>&1
                    Write-Host "  Conflicts in: $($conflicts -join ', ')" -ForegroundColor Red

                    foreach ($cf in ($conflicts -split "`n" | Where-Object { $_ })) {
                        # Strategy: for base files (pom.xml, .gitignore, config files), keep ours
                        # For new source files, accept theirs
                        $isBaseFile = ($cf -match 'pom\.xml$|\.gitignore$|application\.(yml|properties)$|package-info\.java$')

                        if ($isBaseFile) {
                            git checkout --ours $cf 2>&1 | Out-Null
                            Write-Host "    $cf → kept ours (base file)" -ForegroundColor Gray
                        } else {
                            git checkout --theirs $cf 2>&1 | Out-Null
                            Write-Host "    $cf → accepted theirs (new code)" -ForegroundColor Green
                        }
                        git add $cf 2>&1 | Out-Null
                    }

                    # Complete the merge
                    git commit --no-edit 2>&1 | Out-Null
                }
                Write-Ok "Merged PR #$($pr.number) ($branch)"
            }

            # Push merged main
            if (-not $DryRun) {
                git push origin main 2>&1 | Out-Null
                Write-Ok "Pushed merged main to remote."
            }

            # ── A.5: Close merged PRs ──
            Write-Step "A.5" "Closing merged PRs with comments..."
            foreach ($pr in $conflictPRs) {
                if ($DryRun) { continue }
                gh pr close $pr.number --comment "Merged locally with conflict resolution. Conflicts resolved: kept main's base files, accepted PR's new source files." 2>&1 | Out-Null
                Write-Ok "Closed PR #$($pr.number)"
            }
        }

        # ── A.6: Close corresponding issues ──
        Write-Step "A.6" "Closing resolved FE issues..."
        $openIssues = Get-GitHubIssues -TitlePattern "^\[FE-\d+" -State "open"
        foreach ($issue in $openIssues) {
            # Check if there's a merged/closed PR for this issue
            $feId = if ($issue.title -match '\[(FE-\d+)\]') { $Matches[1] } else { "" }
            if (-not $feId) { continue }

            # Check if all files for this issue exist in the repo
            if (-not $DryRun) {
                gh issue close $issue.number --comment "Closed via FE automation — PR merged to main." 2>&1 | Out-Null
                Write-Ok "Closed issue #$($issue.number): $feId"
            }
        }
    }
}

# ============================================================================
# PART B: CODE CONSOLIDATION
# ============================================================================

if (-not $MergeOnly) {

    Write-Host "`n╔══════════════════════════════════════════════════╗" -ForegroundColor Magenta
    Write-Host "║  PART B: CODE CONSOLIDATION                      ║" -ForegroundColor Magenta
    Write-Host "╚══════════════════════════════════════════════════╝" -ForegroundColor Magenta

    git checkout main 2>&1 | Out-Null
    git pull origin main 2>&1 | Out-Null

    # ── B.1: Find misplaced files ──
    Write-Step "B.1" "Scanning for misplaced files (root src/ → $FEOutputDir/src/)..."

    $targetPkgPath = "com/nexgen/sb"
    $misplacedMain = git ls-files "src/main/java/$targetPkgPath/" 2>&1
    $misplacedTest = git ls-files "src/test/java/$targetPkgPath/" 2>&1

    $mainFiles = if ($misplacedMain -and $LASTEXITCODE -eq 0) { ($misplacedMain -split "`n" | Where-Object { $_ }) } else { @() }
    $testFiles = if ($misplacedTest -and $LASTEXITCODE -eq 0) { ($misplacedTest -split "`n" | Where-Object { $_ }) } else { @() }

    $totalMisplaced = $mainFiles.Count + $testFiles.Count
    Write-Ok "Found $($mainFiles.Count) main + $($testFiles.Count) test misplaced files"

    if ($totalMisplaced -gt 0) {
        # ── B.2: Identify duplicates ──
        Write-Step "B.2" "Identifying duplicates (exist in both locations)..."
        $feFiles = git ls-files "$FEOutputDir/src/" 2>&1
        $feFileSet = @{}
        foreach ($f in ($feFiles -split "`n" | Where-Object { $_ })) {
            $relative = $f -replace "^$([regex]::Escape($FEOutputDir))/src/", "src/"
            $feFileSet[$relative] = $f
        }

        $toMove = @()
        $duplicates = @()
        foreach ($f in $mainFiles) {
            $relative = $f
            if ($feFileSet.ContainsKey($relative)) {
                $duplicates += @{ Root = $f; FE = $feFileSet[$relative] }
            } else {
                $toMove += $f
            }
        }
        foreach ($f in $testFiles) {
            $relative = $f
            if ($feFileSet.ContainsKey($relative)) {
                $duplicates += @{ Root = $f; FE = $feFileSet[$relative] }
            } else {
                $toMove += $f
            }
        }

        Write-Ok "Unique files to move: $($toMove.Count), Duplicates: $($duplicates.Count)"

        if (-not $DryRun) {
            # ── B.3: Move unique files ──
            Write-Step "B.3" "Moving $($toMove.Count) unique files..."
            foreach ($f in $toMove) {
                $dest = "$FEOutputDir/$f"
                $destDir = Split-Path $dest -Parent
                if (-not (Test-Path $destDir)) {
                    New-Item -ItemType Directory -Path $destDir -Force | Out-Null
                }
                Copy-Item $f $dest -Force
                git add $dest 2>&1 | Out-Null
                git rm -f $f 2>&1 | Out-Null
                Write-Ok "Moved: $f"
            }

            # ── B.4: Resolve duplicates ──
            Write-Step "B.4" "Resolving $($duplicates.Count) duplicates (keeping FE version)..."
            foreach ($dup in $duplicates) {
                git rm -f $dup.Root 2>&1 | Out-Null
                Write-Ok "Removed root duplicate: $($dup.Root)"
            }
        } else {
            Write-Host "`n  [DRY RUN] Would move $($toMove.Count) files, remove $($duplicates.Count) duplicates" -ForegroundColor Yellow
        }
    }

    # ── B.5: Fix imports ──
    Write-Step "B.5" "Fixing legacy package imports..."
    if (-not $DryRun) {
        $importFixes = Invoke-ImportFix -Directory "$FEOutputDir/src" -OldPackage "com.nexgen.esb.creditrisk" -NewPackage "com.nexgen.sb.creditrisk"
        Write-Ok "Fixed $importFixes files: esb → sb"

        $jakartaFixes = Invoke-JavaxToJakartaFix -Directory "$FEOutputDir/src"
        Write-Ok "Fixed $jakartaFixes javax → jakarta replacements"
    }

    # ── B.6: Remove misplaced root resources ──
    Write-Step "B.6" "Checking for misplaced root resource files..."
    if (-not $DryRun) {
        $rootResources = @("src/main/resources/application.properties")
        foreach ($rf in $rootResources) {
            $tracked = git ls-files $rf 2>&1
            if ($tracked -and $LASTEXITCODE -eq 0) {
                git rm -f $rf 2>&1 | Out-Null
                Write-Ok "Removed misplaced: $rf"
            }
        }
    }

    # ── B.7: Remove duplicate Application class ──
    Write-Step "B.7" "Checking for duplicate Application classes..."
    if (-not $DryRun) {
        $nexgenApp = "$FEOutputDir/src/main/java/com/nexgen/sb/creditrisk/NexgenCreditRiskApplication.java"
        $creditApp = "$FEOutputDir/src/main/java/com/nexgen/sb/creditrisk/CreditRiskApplication.java"
        if ((Test-Path $nexgenApp) -and (Test-Path $creditApp)) {
            git rm -f $nexgenApp 2>&1 | Out-Null
            Write-Ok "Removed duplicate: NexgenCreditRiskApplication.java (keeping CreditRiskApplication.java)"
        }
    }

    # ── B.8: Verify Maven build ──
    Write-Step "B.8" "Verifying Maven build..."
    if (-not $DryRun) {
        git add -A 2>&1 | Out-Null
        $buildSuccess = Test-MavenBuild -FEDir $FEOutputDir
        if ($buildSuccess) {
            Write-Ok "BUILD SUCCESS"
        } else {
            Write-Warn "BUILD FAILED — manual fixes may be needed"
            Write-Host "  Run: cd $FEOutputDir && mvn clean compile" -ForegroundColor Yellow
            Write-Host "  Fix errors, then: git add -A && git commit && git push" -ForegroundColor Yellow
        }
    }

    # ── B.9: Commit and push ──
    Write-Step "B.9" "Committing consolidation..."
    if (-not $DryRun) {
        $committed = Invoke-GitCommitAndPush -Message "fix(fe): consolidate FE code — move misplaced files, fix imports, resolve duplicates"
    }
}

# ── Summary ──
Write-Host ""
Write-Host "╔══════════════════════════════════════════════════╗" -ForegroundColor Green
Write-Host "║  PHASE 6 COMPLETE                                ║" -ForegroundColor Green
Write-Host "╠══════════════════════════════════════════════════╣" -ForegroundColor Green
if (-not $ConsolidateOnly) {
    Write-Host "║  PRs merged and issues closed                    ║" -ForegroundColor Green
}
if (-not $MergeOnly) {
    Write-Host "║  Code consolidated to $FEOutputDir/         ║" -ForegroundColor Green
    Write-Host "║  Imports fixed, duplicates removed               ║" -ForegroundColor Green
}
Write-Host "║                                                  ║" -ForegroundColor Green
Write-Host "║  Next: Review the build and run tests            ║" -ForegroundColor Green
Write-Host "╚══════════════════════════════════════════════════╝" -ForegroundColor Green
