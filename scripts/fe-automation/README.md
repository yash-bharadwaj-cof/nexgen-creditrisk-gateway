# FE Automation Pipeline

Automates the complete **Forward Engineering** workflow — from parsing a migration prompt to filing GitHub Issues, assigning Copilot, merging PRs, and consolidating code.

This pipeline replicates the manual process performed in the chat session, packaged as reusable PowerShell scripts that work for **any** service migration.

---

## Architecture

```
scripts/fe-automation/
├── config.ps1                    # Shared configuration (edit for your project)
├── run-all.ps1                   # Master orchestrator (phases 1→6)
├── lib/
│   └── helpers.ps1               # Reusable GitHub API + file utility functions
├── phase-1-parse-prompt.ps1      # Parse FE migration prompt → context JSON
├── phase-2-github-infra.ps1      # Labels, milestones, project board, discussions
├── phase-3-file-issues.ps1       # File enriched FE issues on GitHub
├── phase-4-copilot-assign.ps1    # Assign copilot-swe-agent to all issues
├── phase-5-monitor.ps1           # Live dashboard: issues, PRs, progress
└── phase-6-merge-consolidate.ps1 # Merge PRs, move files, fix imports, verify build
```

---

## Prerequisites

| Tool | Version | Purpose |
|------|---------|---------|
| **PowerShell** | 7.x+ | Script runtime |
| **GitHub CLI** (`gh`) | 2.x+ | GitHub API operations |
| **Git** | 2.x+ | Version control |
| **Maven** | 3.9+ | Build verification (Phase 6) |
| **Java** | 21+ | Compilation (Phase 6) |

GitHub CLI must be authenticated: `gh auth login`

---

## Quick Start

### Run the Full Pipeline

```powershell
cd scripts/fe-automation
.\run-all.ps1
```

### Resume from a Specific Phase

```powershell
.\run-all.ps1 -FromPhase 3     # Resume from issue filing
.\run-all.ps1 -OnlyPhase 5     # Run only the monitor
.\run-all.ps1 -DryRun          # Preview without executing
```

### Run Individual Phases

```powershell
.\phase-1-parse-prompt.ps1       # Parse FE migration prompt
.\phase-2-github-infra.ps1       # Set up GitHub infrastructure
.\phase-3-file-issues.ps1        # File issues on GitHub
.\phase-4-copilot-assign.ps1     # Assign Copilot to issues
.\phase-5-monitor.ps1 -Watch     # Live dashboard
.\phase-6-merge-consolidate.ps1  # Merge & consolidate
```

---

## Phase Reference

### Phase 1: Parse FE Migration Prompt

**Input:** `docs/fe/fe-migration-prompt.md`
**Output:** `.github/fe-context/fe-prompt-context.json`

Reads the FE migration prompt and extracts structured data:
- Migration overview (source → target platform)
- Technology decisions
- Package mapping (legacy → target)
- Service/architecture mapping
- Target directory structure
- Configuration YAML template
- Migration rules
- Acceptance criteria
- Issue dependency graph + topological execution order

### Phase 2: GitHub Infrastructure

Creates (idempotently):
- **Labels** — FE-specific labels (phase, type, priority, wave)
- **Milestones** — M3: Forward Engineering, M4: FE Verification
- **Issue Templates** — `fe-migration.yml` for Copilot agent tasks
- **PR Template** — Incremental build checklist
- **Project Board** — FE tracker
- **GitHub Discussions** — Migration decisions, code review notes

### Phase 3: File FE Issues

Generates enriched, self-contained issue bodies from the FE manifest, then creates GitHub Issues with:
- Full Copilot agent instructions
- Relevant FE prompt sections (context-injected per issue)
- Incremental build rules (conflict prevention)
- Dependency context
- Acceptance criteria
- Labels, milestone, and project board assignment

### Phase 4: Copilot Assignment

Assigns `copilot-swe-agent` to all open FE issues in dependency-resolved order. This triggers GitHub's Copilot coding agent to autonomously create branches, generate code, and open PRs.

### Phase 5: Monitor Dashboard

Live monitoring dashboard showing:
- Issue status grouped by execution wave
- PR status (merged/open/conflict)
- Progress bar with completion percentage
- Conflict warnings

Use `.\phase-5-monitor.ps1 -Watch` for continuous polling.

### Phase 6: Merge & Consolidate

Automates conflict resolution and code consolidation:

**Part A — Merge PRs:**
1. Merge clean PRs directly via GitHub
2. Merge conflicting PRs locally (strategy: keep main's base files, accept PR's new source files)
3. Close PRs with resolution comments
4. Close corresponding FE issues

**Part B — Code Consolidation:**
1. Find misplaced files (root `src/` → `forward-engineering/src/`)
2. Move unique files, remove duplicates
3. Fix legacy package imports (`com.nexgen.esb` → `com.nexgen.sb`)
4. Fix `javax.*` → `jakarta.*` imports
5. Remove misplaced root resources
6. Verify Maven build
7. Commit and push

---

## Adapting for a New Service

### 1. Edit `config.ps1`

Update these values:

```powershell
$ServiceName  = "your-service-name"
$GitHubOrg    = "your-org"
$ServiceRoot  = "C:\path\to\your\service"
$FEOutputDir  = "forward-engineering"    # or your target folder
```

### 2. Write Your FE Migration Prompt

Create `docs/fe/fe-migration-prompt.md` following this structure:

```markdown
# Forward Engineering Migration Prompt

## Your Service Name — Source Platform → Target Platform

> **CRITICAL — Incremental Build Rules**
> (10 conflict-prevention rules)

## 1. Migration Overview
(table: source platform, target platform, approach, output location)

## 2. Finalized Technology Decisions
(tables: core stack, security, testing, deployment)

## 3. Package Mapping
(table: legacy package → target package)

## 4. Architecture Mapping
(legacy flow → target flow, service mapping table)

## 5. Endpoint Mapping
(REST, SOAP, external integrations)

## 6. Target Directory Structure
(fenced code block with full tree)

## 7. Configuration Mapping
(fenced YAML code block)

## 8. Key Migration Rules
(numbered list with bold rule + detail)

## 9. Issue Dependency Graph
(fenced code block with ASCII dependency tree)

## 10. Acceptance Criteria
(checkbox list)
```

### 3. Update the FE Manifest

In `config.ps1`, update `$FEManifest` to match your issue decomposition:

```powershell
$FEManifest = @(
    @{
        Id        = "FE-001"
        Title     = "[FE-001] Your First Task"
        Desc      = "Description for Copilot agent"
        Wave      = 1
        DependsOn = @()
        Labels    = @("phase:FE","type:scaffold","wave:1")
        Sections  = @(1,2,6,7)   # Which prompt sections are relevant
    }
    # ... more issues
)
```

### 4. Run the Pipeline

```powershell
cd scripts/fe-automation
.\run-all.ps1
```

---

## Conflict Resolution Strategy

When Copilot generates PRs that conflict (common when multiple PRs modify `pom.xml` or `.gitignore`), Phase 6 resolves them automatically:

| File Type | Strategy | Reason |
|-----------|----------|--------|
| `pom.xml` | Keep main's version | Base scaffold is correct; PRs may recreate with wrong settings |
| `.gitignore` | Keep main's version | Already complete from scaffolding |
| `application.yml` | Keep main's version | Base config is correct |
| `package-info.java` | Keep main's version | Already exists |
| **New source files** | Accept PR's version | New code from Copilot |

---

## Outputs

| Output | Location | Created By |
|--------|----------|------------|
| FE context JSON | `.github/fe-context/fe-prompt-context.json` | Phase 1 |
| Execution order | `.github/fe-context/execution-order.json` | Phase 1 |
| Issue body files | `.github/fe-issue-bodies/fe-*.md` | Phase 3 |
| FE source code | `forward-engineering/src/` | Copilot + Phase 6 |
| GitHub Issues | Repository issues tab | Phase 3 |
| GitHub Project Board | Repository projects tab | Phase 2 |
| GitHub Discussions | Repository discussions tab | Phase 2 |

---

## Troubleshooting

| Problem | Solution |
|---------|----------|
| `gh` not found | Install GitHub CLI: `winget install GitHub.cli` |
| Not authenticated | Run `gh auth login` |
| FE prompt not found | Create `docs/fe/fe-migration-prompt.md` |
| Phase fails midway | Resume: `.\run-all.ps1 -FromPhase <N>` |
| PRs still pending | Run monitor: `.\phase-5-monitor.ps1 -Watch` |
| Build fails after consolidation | Check error, fix manually, then `git commit && git push` |
| Copilot not assigning | Ensure Copilot coding agent is enabled for repos |
