# RE Automation Scripts

Automated pipeline that replicates the reverse engineering process performed via the VS Code chat session. These scripts reproduce **exactly** the same steps, in the same order, using the same tools (`gh` CLI, `git`, PowerShell).

## Prerequisites

- **PowerShell 7+** (or Windows PowerShell 5.1)
- **GitHub CLI** (`gh`) — authenticated with `gh auth login`
- **Git** — configured with push access to the target repo
- **Copilot coding agent** — enabled on your GitHub account
- RE templates in the configured `$TemplateSource` path

## Quick Start

```powershell
# Run the full pipeline (Phases 0→6)
.\run-all.ps1

# Resume from a specific phase
.\run-all.ps1 -FromPhase 3

# Run a single phase
.\run-all.ps1 -OnlyPhase 5

# Preview without executing
.\run-all.ps1 -DryRun
```

## Phase Reference

| Phase | Script | What It Does |
|-------|--------|-------------|
| 0 | `phase-0-repo-setup.ps1` | Create GitHub repo, `git init`, push code |
| 1 | `phase-1-github-infra.ps1` | Create 24 labels, 2 milestones, issue/PR templates, project board |
| 2 | `phase-2-codebase-analysis.ps1` | Scan source code → extract architecture context to JSON |
| 3 | `phase-3-template-prep.ps1` | Copy 10 RE templates into repo, commit, push |
| 4 | `phase-4-issue-filing.ps1` | File 10 enriched GitHub issues with codebase context |
| 5 | `phase-5-copilot-assign.ps1` | Assign `copilot-swe-agent` to trigger autonomous RE doc generation |
| 6 | `phase-6-monitor.ps1` | Dashboard showing issue/PR status and completion % |

## Configuration

Edit `config.ps1` to target a different service:

```powershell
$ServiceName    = "nexgen-vehicle-rating"        # Change this
$GitHubOrg      = "yash-bharadwaj-cof"           # Your GitHub org/user
$ServiceRoot    = "d:\...\$ServiceName"           # Local code path
$TemplateSource = "d:\...\RE Templates"           # Where templates live
```

## Monitoring

```powershell
# One-time status check
.\phase-6-monitor.ps1

# Continuous watch (polls every 60s)
.\phase-6-monitor.ps1 -Watch

# Custom interval
.\phase-6-monitor.ps1 -Watch -IntervalSeconds 30
```

## What Happens After

1. Copilot creates **10 branches** (one per RE document)
2. Copilot generates the filled RE document and opens a **Pull Request**
3. You review and **merge** each PR
4. RE documents land in `docs/re/` on `main`
