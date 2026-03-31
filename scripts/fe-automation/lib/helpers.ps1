# ============================================================================
# FE Automation — Shared Library Functions
# ============================================================================
# Reusable helpers for GitHub API calls, prompt parsing, and file operations.
# Dot-sourced by phase scripts that need these utilities.
# ============================================================================

# ── GitHub API Helpers ──────────────────────────────────────────────────────

function Get-GitHubIssues {
    <#
    .SYNOPSIS
        Lists issues matching a title pattern.
    #>
    param(
        [string]$TitlePattern = "",
        [string]$State = "all",
        [int]$Limit = 200
    )
    $issues = gh issue list --limit $Limit --state $State --json number,title,state,assignees,labels 2>&1 | ConvertFrom-Json
    if ($TitlePattern) {
        $issues = $issues | Where-Object { $_.title -match $TitlePattern }
    }
    return $issues
}

function Get-GitHubPRs {
    <#
    .SYNOPSIS
        Lists PRs matching a branch pattern.
    #>
    param(
        [string]$BranchPattern = "",
        [string]$State = "all",
        [int]$Limit = 200
    )
    $prs = gh pr list --state $State --limit $Limit --json number,title,state,headRefName,mergeable 2>&1 | ConvertFrom-Json
    if ($BranchPattern) {
        $prs = $prs | Where-Object { $_.headRefName -match $BranchPattern }
    }
    return $prs
}

function Get-MilestoneNumber {
    <#
    .SYNOPSIS
        Gets the milestone number by title prefix.
    #>
    param([string]$TitlePrefix)
    $num = gh api "repos/$GitHubOrg/$ServiceName/milestones" --jq ".[] | select(.title | startswith(`"$TitlePrefix`")) | .number" 2>&1
    if ($LASTEXITCODE -ne 0 -or -not $num) { return $null }
    return $num.Trim()
}

function Get-ProjectBoardNumber {
    <#
    .SYNOPSIS
        Gets the project board number by title. Creates if not found and -Create is specified.
    #>
    param(
        [string]$BoardTitle,
        [switch]$Create
    )
    $projects = gh project list --owner $GitHubOrg --format json 2>&1 | ConvertFrom-Json
    $board = $projects.projects | Where-Object { $_.title -eq $BoardTitle }

    if ($board) {
        return $board.number
    }
    if ($Create) {
        $result = gh project create --owner $GitHubOrg --title $BoardTitle --format json 2>&1 | ConvertFrom-Json
        gh project link $result.number --owner $GitHubOrg --repo "$GitHubOrg/$ServiceName" 2>&1 | Out-Null
        return $result.number
    }
    return $null
}

function Add-IssueToProject {
    <#
    .SYNOPSIS
        Adds a GitHub issue to a project board.
    #>
    param(
        [int]$IssueNumber,
        [int]$ProjectNumber
    )
    gh project item-add $ProjectNumber --owner $GitHubOrg --url "https://github.com/$GitHubOrg/$ServiceName/issues/$IssueNumber" 2>&1 | Out-Null
}

# ── FE Prompt Parsing Helpers ──────────────────────────────────────────────

function Read-FEPrompt {
    <#
    .SYNOPSIS
        Reads the FE migration prompt and extracts sections by heading number.
    .DESCRIPTION
        Returns a hashtable: { 1 = "section 1 content", 2 = "section 2 content", ... }
    #>
    param([string]$PromptPath)

    if (-not (Test-Path $PromptPath)) {
        throw "FE migration prompt not found: $PromptPath"
    }
    $content = Get-Content $PromptPath -Raw
    $sections = @{}

    # Extract the critical build rules block (before §1)
    if ($content -match '(?s)>\s*\*\*CRITICAL.*?(?=\n## \d)') {
        $sections[0] = $Matches[0]
    }

    # Extract numbered sections: ## 1. through ## 10.
    $pattern = '(?ms)^## (\d+)\.\s+(.*?)(?=^## \d+\.|^---\s*$|\z)'
    $matches = [regex]::Matches($content, $pattern)
    foreach ($m in $matches) {
        $secNum = [int]$m.Groups[1].Value
        $sections[$secNum] = $m.Value
    }

    return $sections
}

function Get-DependencyTree {
    <#
    .SYNOPSIS
        Builds a topologically sorted execution order from the FE manifest.
    .DESCRIPTION
        Returns FE issue IDs in dependency-respecting order for serial execution.
    #>
    param([array]$Manifest)

    $resolved = @()
    $remaining = [System.Collections.ArrayList]@($Manifest)

    while ($remaining.Count -gt 0) {
        $batch = @()
        foreach ($item in @($remaining)) {
            $depsResolved = $true
            foreach ($dep in $item.DependsOn) {
                if ($dep -notin $resolved) {
                    $depsResolved = $false
                    break
                }
            }
            if ($depsResolved) { $batch += $item }
        }

        if ($batch.Count -eq 0 -and $remaining.Count -gt 0) {
            Write-Warn "Circular dependency detected. Remaining: $($remaining.Id -join ', ')"
            $batch = @($remaining[0])
        }

        foreach ($item in $batch) {
            $resolved += $item.Id
            $remaining.Remove($item) | Out-Null
        }
    }

    return $resolved
}

# ── File & Build Helpers ───────────────────────────────────────────────────

function Test-MavenBuild {
    <#
    .SYNOPSIS
        Runs Maven compile in the FE output directory. Returns $true on success.
    #>
    param([string]$FEDir)

    Push-Location $FEDir
    try {
        $output = mvn clean compile -q 2>&1
        $success = $LASTEXITCODE -eq 0
        return $success
    }
    finally {
        Pop-Location
    }
}

function Get-MisplacedFiles {
    <#
    .SYNOPSIS
        Finds Java files under root src/ that belong in the FE target package.
    #>
    param(
        [string]$TargetPackage = "com.nexgen.sb",
        [string]$FEOutputDir = "forward-engineering"
    )

    $pkgPath = $TargetPackage -replace '\.', '/'
    $rootFiles = git ls-files "src/main/java/$pkgPath/" 2>&1
    if ($LASTEXITCODE -ne 0) { return @() }

    $misplaced = @()
    foreach ($f in ($rootFiles -split "`n" | Where-Object { $_ })) {
        $relativePath = $f -replace "^src/", "$FEOutputDir/src/"
        $misplaced += @{
            Source      = $f
            Destination = $relativePath
            Exists      = (Test-Path $relativePath)
        }
    }
    return $misplaced
}

function Invoke-ImportFix {
    <#
    .SYNOPSIS
        Replaces legacy package imports in all Java files under a directory.
    #>
    param(
        [string]$Directory,
        [string]$OldPackage,
        [string]$NewPackage
    )

    $files = git grep -l ([regex]::Escape($OldPackage)) -- "$Directory/" 2>&1
    if ($LASTEXITCODE -ne 0 -or -not $files) { return 0 }

    $fixCount = 0
    foreach ($f in ($files -split "`n" | Where-Object { $_ })) {
        $content = Get-Content $f -Raw
        $newContent = $content -replace [regex]::Escape($OldPackage), $NewPackage
        Set-Content $f -Value $newContent -NoNewline
        $fixCount++
    }
    return $fixCount
}

function Invoke-JavaxToJakartaFix {
    <#
    .SYNOPSIS
        Replaces javax.* imports with jakarta.* in all Java files under a directory.
    #>
    param([string]$Directory)

    $replacements = @(
        @{ Old = 'import javax.annotation.PostConstruct'; New = 'import jakarta.annotation.PostConstruct' }
        @{ Old = 'import javax.annotation.PreDestroy';    New = 'import jakarta.annotation.PreDestroy' }
        @{ Old = 'import javax.xml.bind';                 New = 'import jakarta.xml.bind' }
        @{ Old = 'import javax.xml.ws';                   New = 'import jakarta.xml.ws' }
        @{ Old = 'import javax.jws';                      New = 'import jakarta.jws' }
        @{ Old = 'import javax.validation';               New = 'import jakarta.validation' }
    )

    $totalFixes = 0
    foreach ($r in $replacements) {
        $files = git grep -l ([regex]::Escape($r.Old)) -- "$Directory/" 2>&1
        if ($LASTEXITCODE -ne 0 -or -not $files) { continue }

        foreach ($f in ($files -split "`n" | Where-Object { $_ })) {
            $content = Get-Content $f -Raw
            $newContent = $content -replace [regex]::Escape($r.Old), $r.New
            Set-Content $f -Value $newContent -NoNewline
            $totalFixes++
        }
    }
    return $totalFixes
}

# ── Git Helpers ────────────────────────────────────────────────────────────

function Invoke-GitCommitAndPush {
    <#
    .SYNOPSIS
        Stages, commits, and pushes if there are staged changes.
    #>
    param([string]$Message)

    git add -A 2>&1 | Out-Null
    git diff --cached --quiet 2>&1
    if ($LASTEXITCODE -ne 0) {
        git commit -m $Message 2>&1 | Out-Null
        git push origin main 2>&1 | Out-Null
        Write-Ok "Committed and pushed: $Message"
        return $true
    }
    Write-Skip "No changes to commit."
    return $false
}
