# ============================================================================
# Phase 2 — Codebase Analysis
# ============================================================================
# Replicates: The deep codebase scan we did via Explore sub-agent
# Scans source code and extracts architecture context into a JSON file
# that Phase 4 uses to enrich issue bodies.
# ============================================================================

$ErrorActionPreference = "Stop"
. "$PSScriptRoot\config.ps1"
Write-Phase "PHASE 2" "Codebase Analysis — Extract architecture context from source code"

Set-Location $ServiceRoot

# Initialize context object
$context = @{
    serviceName    = $ServiceName
    analyzedAt     = (Get-Date -Format "yyyy-MM-dd HH:mm:ss")
    java           = @{}
    blueprint      = @{}
    config         = @{}
    dependencies   = @()
    summary        = @{}
}

# ── Step 2.1: Count Java classes per package ──
Write-Host "[2.1] Scanning Java source files..." -ForegroundColor Yellow
$javaFiles = Get-ChildItem -Path "src" -Recurse -Filter "*.java" -ErrorAction SilentlyContinue
$packages = @{}
$classList = @()

foreach ($file in $javaFiles) {
    $relativePath = $file.FullName.Replace($ServiceRoot + "\", "").Replace("\", "/")
    $content = Get-Content $file.FullName -Raw -ErrorAction SilentlyContinue

    # Extract package name
    $packageMatch = [regex]::Match($content, "package\s+([\w.]+)\s*;")
    $packageName = if ($packageMatch.Success) { $packageMatch.Groups[1].Value } else { "default" }

    # Extract class/interface/enum name
    $classMatch = [regex]::Match($content, "(public\s+)?(abstract\s+)?(class|interface|enum)\s+(\w+)")
    $className = if ($classMatch.Success) { $classMatch.Groups[4].Value } else { $file.BaseName }
    $classType = if ($classMatch.Success) { $classMatch.Groups[3].Value } else { "class" }

    # Count lines
    $loc = ($content -split "`n").Count

    # Detect key annotations/interfaces
    $annotations = @()
    if ($content -match "@Path\b")       { $annotations += "JAX-RS" }
    if ($content -match "@WebService\b") { $annotations += "JAX-WS" }
    if ($content -match "@WebMethod\b")  { $annotations += "JAX-WS" }
    if ($content -match "implements Processor\b") { $annotations += "Camel-Processor" }
    if ($content -match "extends RouteBuilder\b") { $annotations += "Camel-RouteBuilder" }

    if (-not $packages.ContainsKey($packageName)) { $packages[$packageName] = @() }
    $packages[$packageName] += $className

    $classList += @{
        className   = $className
        classType   = $classType
        package     = $packageName
        filePath    = $relativePath
        loc         = $loc
        annotations = $annotations
    }
}

$context.java = @{
    totalClasses = $classList.Count
    totalFiles   = $javaFiles.Count
    packages     = $packages
    packageCount = $packages.Keys.Count
    classes      = $classList
}

Write-Host "  Found $($classList.Count) Java classes across $($packages.Keys.Count) packages" -ForegroundColor Green
foreach ($pkg in ($packages.Keys | Sort-Object)) {
    Write-Host "    $pkg : $($packages[$pkg].Count) classes ($($packages[$pkg] -join ', '))" -ForegroundColor Gray
}

# ── Step 2.2: Analyze blueprint.xml ──
Write-Host "`n[2.2] Analyzing blueprint.xml..." -ForegroundColor Yellow
$blueprintPath = Get-ChildItem -Path "src" -Recurse -Filter "blueprint.xml" -ErrorAction SilentlyContinue | Select-Object -First 1

if ($blueprintPath) {
    $bpContent = Get-Content $blueprintPath.FullName -Raw

    # Extract bean definitions
    $beanMatches = [regex]::Matches($bpContent, '<bean\s+id="([^"]+)"\s+class="([^"]+)"')
    $beans = @()
    foreach ($m in $beanMatches) {
        $beans += @{ id = $m.Groups[1].Value; class = $m.Groups[2].Value }
    }

    # Extract Camel routes
    $routeMatches = [regex]::Matches($bpContent, '<route\s+id="([^"]+)"')
    $routes = @()
    foreach ($m in $routeMatches) { $routes += $m.Groups[1].Value }

    # Extract CXF endpoints
    $cxfMatches = [regex]::Matches($bpContent, '<(?:cxf:(?:rs)?server|cxf:cxfEndpoint)\s+id="([^"]+)"\s+address="([^"]*)"')
    $cxfJaxrsMatches = [regex]::Matches($bpContent, '<jaxrs:server\s+id="([^"]+)"\s+address="([^"]*)"')
    $endpoints = @()
    foreach ($m in ($cxfMatches + $cxfJaxrsMatches)) {
        $endpoints += @{ id = $m.Groups[1].Value; address = $m.Groups[2].Value }
    }

    $context.blueprint = @{
        filePath   = $blueprintPath.FullName.Replace($ServiceRoot + "\", "").Replace("\", "/")
        beanCount  = $beans.Count
        beans      = $beans
        routeCount = $routes.Count
        routes     = $routes
        endpoints  = $endpoints
    }

    Write-Host "  Beans: $($beans.Count), Routes: $($routes.Count), CXF Endpoints: $($endpoints.Count)" -ForegroundColor Green
} else {
    Write-Host "  No blueprint.xml found." -ForegroundColor Red
}

# ── Step 2.3: Analyze pom.xml dependencies ──
Write-Host "`n[2.3] Extracting pom.xml dependencies..." -ForegroundColor Yellow
$pomPath = Join-Path $ServiceRoot "pom.xml"
if (Test-Path $pomPath) {
    $pomContent = Get-Content $pomPath -Raw

    $depMatches = [regex]::Matches($pomContent, '<dependency>\s*<groupId>([^<]+)</groupId>\s*<artifactId>([^<]+)</artifactId>(?:\s*<version>([^<]*)</version>)?')
    $deps = @()
    foreach ($m in $depMatches) {
        $deps += @{
            groupId    = $m.Groups[1].Value
            artifactId = $m.Groups[2].Value
            version    = if ($m.Groups[3].Value) { $m.Groups[3].Value } else { "managed" }
        }
    }

    # Extract parent/BOM
    $parentMatch = [regex]::Match($pomContent, '<parent>\s*<groupId>([^<]+)</groupId>\s*<artifactId>([^<]+)</artifactId>\s*<version>([^<]+)</version>')
    $parentInfo = if ($parentMatch.Success) {
        @{ groupId = $parentMatch.Groups[1].Value; artifactId = $parentMatch.Groups[2].Value; version = $parentMatch.Groups[3].Value }
    } else { $null }

    $context.dependencies = $deps
    $context.pom = @{
        dependencyCount = $deps.Count
        parent          = $parentInfo
    }

    Write-Host "  Found $($deps.Count) dependencies" -ForegroundColor Green
    if ($parentInfo) { Write-Host "  Parent BOM: $($parentInfo.groupId):$($parentInfo.artifactId):$($parentInfo.version)" -ForegroundColor Gray }
} else {
    Write-Host "  No pom.xml found." -ForegroundColor Red
}

# ── Step 2.4: Analyze config properties ──
Write-Host "`n[2.4] Extracting configuration properties..." -ForegroundColor Yellow
$configFiles = Get-ChildItem -Path "src" -Recurse -Filter "*.properties" -ErrorAction SilentlyContinue
$configProps = @()

foreach ($cf in $configFiles) {
    $lines = Get-Content $cf.FullName -ErrorAction SilentlyContinue
    foreach ($line in $lines) {
        if ($line -match "^\s*([^#=][^=]*)=(.*)$") {
            $configProps += @{
                key   = $Matches[1].Trim()
                value = $Matches[2].Trim()
                file  = $cf.FullName.Replace($ServiceRoot + "\", "").Replace("\", "/")
            }
        }
    }
}

$context.config = @{
    propertyCount = $configProps.Count
    properties    = $configProps
}
Write-Host "  Found $($configProps.Count) config properties across $($configFiles.Count) file(s)" -ForegroundColor Green

# ── Step 2.5: Build summary ──
$context.summary = @{
    totalJavaClasses  = $classList.Count
    totalPackages     = $packages.Keys.Count
    totalBeans        = $context.blueprint.beanCount
    totalRoutes       = $context.blueprint.routeCount
    totalDependencies = $context.dependencies.Count
    totalProperties   = $configProps.Count
    totalFiles        = (git ls-files 2>&1 | Measure-Object).Count
}

# ── Step 2.6: Write context JSON ──
Write-Host "`n[2.6] Writing architecture context JSON..." -ForegroundColor Yellow
New-Item -ItemType Directory -Path $ContextDir -Force | Out-Null
$contextPath = Join-Path $ContextDir "architecture-context.json"
$context | ConvertTo-Json -Depth 10 | Set-Content -Path $contextPath -Encoding UTF8
Write-Host "  Written to: $contextPath" -ForegroundColor Green

# Commit the context file
git add $ContextDir 2>&1 | Out-Null
$hasChanges = git diff --cached --quiet 2>&1; $changed = $LASTEXITCODE -ne 0
if ($changed) {
    git commit -m "chore: add auto-generated architecture context from codebase analysis" 2>&1 | Out-Null
    Write-Host "  Committed context file." -ForegroundColor Green
}

Write-Host "`n[Phase 2] COMPLETE — Context at $contextPath" -ForegroundColor Green
