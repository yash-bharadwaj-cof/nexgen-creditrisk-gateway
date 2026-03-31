# ============================================================================
# Phase 1 — Parse FE Migration Prompt
# ============================================================================
# Reads the FE migration prompt and extracts structured context into a JSON
# file. This context is consumed by Phase 3 (issue filing) to generate
# enriched, self-contained issue bodies for the Copilot coding agent.
#
# Input:  docs/fe/fe-migration-prompt.md
# Output: .github/fe-context/fe-prompt-context.json
# ============================================================================

$ErrorActionPreference = "Stop"
. "$PSScriptRoot\config.ps1"
. "$PSScriptRoot\lib\helpers.ps1"
Write-Phase "PHASE 1" "Parse FE Migration Prompt → Structured Context JSON"

Set-Location $ServiceRoot

# ── Step 1.1: Read the FE migration prompt ──
Write-Step "1.1" "Reading FE migration prompt: $FEPromptFile"

if (-not (Test-Path $FEPromptFile)) {
    throw "FE migration prompt not found: $FEPromptFile. Create it first."
}

$promptContent = Get-Content $FEPromptFile -Raw
$sections = Read-FEPrompt -PromptPath $FEPromptFile
Write-Ok "Parsed $($sections.Count) sections from FE prompt"

# ── Step 1.2: Extract migration overview ──
Write-Step "1.2" "Extracting migration overview..."

$overview = @{}
if ($sections.ContainsKey(1)) {
    $tableRows = [regex]::Matches($sections[1], '\|\s*\*\*(.+?)\*\*\s*\|\s*(.+?)\s*\|')
    foreach ($row in $tableRows) {
        $key = $row.Groups[1].Value.Trim()
        $val = $row.Groups[2].Value.Trim()
        $overview[$key] = $val
    }
}
Write-Ok "Extracted $($overview.Count) overview fields"

# ── Step 1.3: Extract technology decisions ──
Write-Step "1.3" "Extracting technology decisions..."

$decisions = @()
if ($sections.ContainsKey(2)) {
    $decisionRows = [regex]::Matches($sections[2], '\|\s*(.+?)\s*\|\s*(.+?)\s*\|\s*(.+?)\s*\|')
    foreach ($row in $decisionRows) {
        $col1 = $row.Groups[1].Value.Trim()
        $col2 = $row.Groups[2].Value.Trim()
        $col3 = $row.Groups[3].Value.Trim()
        # Skip header rows
        if ($col1 -match '^-+$' -or $col1 -eq 'Decision' -or $col1 -eq 'Aspect') { continue }
        $decisions += @{ Name = $col1; Selected = $col2; Rationale = $col3 }
    }
}
Write-Ok "Extracted $($decisions.Count) technology decisions"

# ── Step 1.4: Extract package mapping ──
Write-Step "1.4" "Extracting package mapping..."

$packageMap = @()
if ($sections.ContainsKey(3)) {
    $pkgRows = [regex]::Matches($sections[3], '\|\s*`?(.+?)`?\s*\|\s*`?(.+?)`?\s*\|')
    foreach ($row in $pkgRows) {
        $legacy = $row.Groups[1].Value.Trim(' ', '`')
        $target = $row.Groups[2].Value.Trim(' ', '`')
        if ($legacy -match '^-+$' -or $legacy -eq 'Legacy Package') { continue }
        $packageMap += @{ Legacy = $legacy; Target = $target }
    }
}
Write-Ok "Extracted $($packageMap.Count) package mappings"

# ── Step 1.5: Extract service mapping ──
Write-Step "1.5" "Extracting architecture / service mapping..."

$serviceMap = @()
if ($sections.ContainsKey(4)) {
    $svcRows = [regex]::Matches($sections[4], '\|\s*`?(.+?)`?\s*\|\s*`?(.+?)`?\s*\|\s*`?(.+?)`?\s*\|')
    foreach ($row in $svcRows) {
        $legacy = $row.Groups[1].Value.Trim(' ', '`')
        $target = $row.Groups[2].Value.Trim(' ', '`')
        $method = $row.Groups[3].Value.Trim(' ', '`')
        if ($legacy -match '^-+$' -or $legacy -eq 'Legacy Processor') { continue }
        $serviceMap += @{ LegacyProcessor = $legacy; TargetService = $target; Method = $method }
    }
}
Write-Ok "Extracted $($serviceMap.Count) service mappings"

# ── Step 1.6: Extract directory structure ──
Write-Step "1.6" "Extracting target directory structure..."

$dirStructure = ""
if ($sections.ContainsKey(6)) {
    $codeBlockMatch = [regex]::Match($sections[6], '(?ms)```\n(.+?)```')
    if ($codeBlockMatch.Success) {
        $dirStructure = $codeBlockMatch.Groups[1].Value.Trim()
    }
}
Write-Ok "Extracted directory tree ($($dirStructure.Split("`n").Count) lines)"

# ── Step 1.7: Extract configuration mapping (YAML block) ──
Write-Step "1.7" "Extracting configuration mapping..."

$configYaml = ""
if ($sections.ContainsKey(7)) {
    $yamlMatch = [regex]::Match($sections[7], '(?ms)```yaml\n(.+?)```')
    if ($yamlMatch.Success) {
        $configYaml = $yamlMatch.Groups[1].Value.Trim()
    }
}
Write-Ok "Extracted application.yml template ($($configYaml.Split("`n").Count) lines)"

# ── Step 1.8: Extract migration rules ──
Write-Step "1.8" "Extracting migration rules..."

$migrationRules = @()
if ($sections.ContainsKey(8)) {
    $ruleMatches = [regex]::Matches($sections[8], '(?m)^\d+\.\s*\*\*(.+?)\*\*\s*—?\s*(.*?)$')
    foreach ($r in $ruleMatches) {
        $migrationRules += @{ Rule = $r.Groups[1].Value.Trim(); Detail = $r.Groups[2].Value.Trim() }
    }
}
Write-Ok "Extracted $($migrationRules.Count) migration rules"

# ── Step 1.9: Extract acceptance criteria ──
Write-Step "1.9" "Extracting acceptance criteria..."

$acceptanceCriteria = @()
if ($sections.ContainsKey(10)) {
    $acMatches = [regex]::Matches($sections[10], '(?m)^-\s*\[[ x]\]\s*(.+?)$')
    foreach ($ac in $acMatches) {
        $acceptanceCriteria += $ac.Groups[1].Value.Trim()
    }
}
Write-Ok "Extracted $($acceptanceCriteria.Count) acceptance criteria"

# ── Step 1.10: Extract dependency graph ──
Write-Step "1.10" "Extracting issue dependency graph..."

$dependencyGraph = ""
if ($sections.ContainsKey(9)) {
    $graphMatch = [regex]::Match($sections[9], '(?ms)```\n(.+?)```')
    if ($graphMatch.Success) {
        $dependencyGraph = $graphMatch.Groups[1].Value.Trim()
    }
}
Write-Ok "Extracted dependency graph"

# ── Step 1.11: Build and write context JSON ──
Write-Step "1.11" "Writing context JSON to $FEContextDir/"

New-Item -ItemType Directory -Path $FEContextDir -Force | Out-Null

$context = @{
    parsedAt           = (Get-Date -Format "yyyy-MM-dd HH:mm:ss")
    sourceFile         = $FEPromptFile
    serviceName        = $ServiceName
    overview           = $overview
    technologyDecisions = $decisions
    packageMapping     = $packageMap
    serviceMapping     = $serviceMap
    directoryStructure = $dirStructure
    configurationYaml  = $configYaml
    migrationRules     = $migrationRules
    acceptanceCriteria = $acceptanceCriteria
    dependencyGraph    = $dependencyGraph
    issueCount         = $FEManifest.Count
    waveCount          = ($FEManifest | ForEach-Object { $_.Wave } | Sort-Object -Unique).Count
}

$jsonPath = Join-Path $FEContextDir "fe-prompt-context.json"
$context | ConvertTo-Json -Depth 10 | Set-Content $jsonPath -Encoding UTF8
Write-Ok "Context JSON written: $jsonPath"

# ── Step 1.12: Compute execution order ──
Write-Step "1.12" "Computing dependency-resolved execution order..."

$executionOrder = Get-DependencyTree -Manifest $FEManifest
$orderPath = Join-Path $FEContextDir "execution-order.json"
@{ order = $executionOrder; waves = ($FEManifest | Group-Object Wave | Sort-Object Name | ForEach-Object { @{ wave = [int]$_.Name; issues = $_.Group.Id } }) } | ConvertTo-Json -Depth 5 | Set-Content $orderPath -Encoding UTF8
Write-Ok "Execution order: $($executionOrder -join ' → ')"

# ── Step 1.13: Commit ──
Write-Step "1.13" "Committing context files..."
Invoke-GitCommitAndPush -Message "chore(fe): add parsed FE prompt context JSON"

Write-Host "`n[Phase 1] COMPLETE — FE prompt parsed into structured context" -ForegroundColor Green
Write-Host "  Context: $jsonPath" -ForegroundColor Cyan
Write-Host "  Issues:  $($FEManifest.Count) issues across $($context.waveCount) waves" -ForegroundColor Cyan
