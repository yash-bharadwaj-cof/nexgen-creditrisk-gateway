# ============================================================================
# RE Automation — Shared Configuration
# ============================================================================
# This file is dot-sourced by all phase scripts. Modify these values to
# target a different service repository.
# ============================================================================

# --- SERVICE IDENTITY ---
$ServiceName       = "nexgen-creditrisk-gateway"
$GitHubOrg         = "yash-bharadwaj-cof"
$RepoVisibility    = "public"                                  # public | private

# --- LOCAL PATHS ---
$ServiceRoot       = "d:\OneDrive - Coforge Limited\Desktop\Github\$ServiceName"
$TemplateSource    = "D:\OneDrive - Coforge Limited\New_ReverseEng_Temp\Fw_ Reverse Engineering Templates"

# --- REPO-RELATIVE PATHS ---
$TemplateDestDir   = "docs/re/templates"
$OutputDir         = "docs/re"
$IssueBodiesDir    = ".github/issue-bodies"
$ContextDir        = ".github/context"

# --- MILESTONES ---
$Milestones = @(
    @{ Title = "M1: Reverse Engineering"; Description = "Complete analysis of all legacy service components. 10 RE Issues (RE-001 to RE-010) covering endpoints, models, routes, business logic, integrations, config, security, and tech debt." }
    @{ Title = "M2: Verification & Validation"; Description = "Verify all RE documents against source code. Cross-reference validation across all 10 documents." }
)

# --- LABEL TAXONOMY (24 labels — exact replica of what we created) ---
$Labels = @(
    # Phase labels
    @{ Name = "phase:RE";       Color = "1D76DB"; Desc = "Reverse Engineering" }
    @{ Name = "phase:VV";       Color = "0E8A16"; Desc = "Verification & Validation" }
    @{ Name = "phase:FE";       Color = "D93F0B"; Desc = "Forward Engineering" }
    @{ Name = "phase:testing";  Color = "FBCA04"; Desc = "Testing phase" }
    @{ Name = "phase:deploy";   Color = "5319E7"; Desc = "Deployment phase" }
    # Type labels
    @{ Name = "type:analysis";  Color = "C5DEF5"; Desc = "RE analysis task" }
    @{ Name = "type:document";  Color = "C5DEF5"; Desc = "Documentation deliverable" }
    @{ Name = "type:decision";  Color = "C5DEF5"; Desc = "Decision record" }
    @{ Name = "type:design";    Color = "C5DEF5"; Desc = "Design artifact" }
    @{ Name = "type:implement"; Color = "C5DEF5"; Desc = "Implementation task" }
    @{ Name = "type:test";      Color = "C5DEF5"; Desc = "Test artifact" }
    @{ Name = "type:bug";       Color = "C5DEF5"; Desc = "Bug or defect" }
    # Scope labels
    @{ Name = "scope:endpoint";    Color = "E4E669"; Desc = "REST/SOAP endpoints" }
    @{ Name = "scope:route";       Color = "E4E669"; Desc = "Camel routes" }
    @{ Name = "scope:model";       Color = "E4E669"; Desc = "Data models" }
    @{ Name = "scope:logic";       Color = "E4E669"; Desc = "Business logic / strategies" }
    @{ Name = "scope:integration"; Color = "E4E669"; Desc = "External service integration" }
    @{ Name = "scope:config";      Color = "E4E669"; Desc = "Configuration & environment" }
    @{ Name = "scope:security";    Color = "E4E669"; Desc = "Security & auth" }
    @{ Name = "scope:logging";     Color = "E4E669"; Desc = "Logging & monitoring" }
    # Status labels
    @{ Name = "status:draft";      Color = "BFD4F2"; Desc = "Draft — work in progress" }
    @{ Name = "status:in-review";  Color = "BFD4F2"; Desc = "Ready for review" }
    @{ Name = "status:verified";   Color = "BFD4F2"; Desc = "Verified and approved" }
    @{ Name = "status:blocked";    Color = "BFD4F2"; Desc = "Blocked by dependency" }
)

# --- RE DOCUMENT MANIFEST ---
# Each entry maps: template file → issue title → output file → labels
$REManifest = @(
    @{
        Id       = "RE-001"
        Num      = 1
        Template = "01-DISCOVERY-REPORT-TEMPLATE_Version3.md"
        Title    = "[RE-001] Discovery Report — Full Service Analysis"
        Output   = "01-discovery-report.md"
        Labels   = @("phase:RE","type:analysis","scope:endpoint","scope:route","scope:model","scope:logic","scope:integration","scope:config","scope:security")
    }
    @{
        Id       = "RE-002"
        Num      = 2
        Template = "02-COMPONENT-CATALOG-TEMPLATE_Version3.md"
        Title    = "[RE-002] Component Catalog — All Classes Inventoried"
        Output   = "02-component-catalog.md"
        Labels   = @("phase:RE","type:analysis","scope:model","scope:logic","scope:endpoint")
    }
    @{
        Id       = "RE-003"
        Num      = 3
        Template = "03-SEQUENCE-DIAGRAMS-TEMPLATE_Version3.md"
        Title    = "[RE-003] Sequence Diagrams — All Request Flows Traced"
        Output   = "03-sequence-diagrams.md"
        Labels   = @("phase:RE","type:analysis","scope:route","scope:endpoint")
    }
    @{
        Id       = "RE-004"
        Num      = 4
        Template = "04-BUSINESS-RULES-CATALOG-TEMPLATE_Version3.md"
        Title    = "[RE-004] Business Rules Catalog — Scoring Strategies & Validation"
        Output   = "04-business-rules-catalog.md"
        Labels   = @("phase:RE","type:analysis","scope:logic")
    }
    @{
        Id       = "RE-005"
        Num      = 5
        Template = "05-DATA-DICTIONARY-TEMPLATE_Version4.md"
        Title    = "[RE-005] Data Dictionary — All Entities, Fields, Types"
        Output   = "05-data-dictionary.md"
        Labels   = @("phase:RE","type:analysis","scope:model")
    }
    @{
        Id       = "RE-006"
        Num      = 6
        Template = "06-DATA-FLOW-DIAGRAM-TEMPLATE_Version2.md"
        Title    = "[RE-006] Data Flow Diagrams — Context, Level 1, Level 2"
        Output   = "06-data-flow-diagrams.md"
        Labels   = @("phase:RE","type:analysis","scope:route","scope:model")
    }
    @{
        Id       = "RE-007"
        Num      = 7
        Template = "07-BDD-FEATURE-SPECS-TEMPLATE_Version2.md"
        Title    = "[RE-007] BDD Feature Specs — Gherkin Scenarios from Code"
        Output   = "07-bdd-feature-specs.md"
        Labels   = @("phase:RE","type:analysis","scope:logic","scope:endpoint")
    }
    @{
        Id       = "RE-008"
        Num      = 8
        Template = "08-TEST-CASE-INVENTORY-TEMPLATE_Version3.md"
        Title    = "[RE-008] Test Case Inventory — Comprehensive Test Matrix"
        Output   = "08-test-case-inventory.md"
        Labels   = @("phase:RE","type:analysis","scope:logic","scope:endpoint")
    }
    @{
        Id       = "RE-009"
        Num      = 9
        Template = "09-GAP-REPORT-TEMPLATE_Version2.md"
        Title    = "[RE-009] Gap Report — All Gaps Across All Dimensions"
        Output   = "09-gap-report.md"
        Labels   = @("phase:RE","type:analysis","scope:logic","scope:security","scope:config")
    }
    @{
        Id       = "RE-010"
        Num      = 10
        Template = "10-FIELD-TO-FIELD-MAPPING-TEMPLATE_Version2.md"
        Title    = "[RE-010] Field-to-Field Mapping — All Endpoints & Transformations"
        Output   = "10-field-to-field-mapping.md"
        Labels   = @("phase:RE","type:analysis","scope:model","scope:endpoint","scope:integration")
    }
)

# --- COPILOT AGENT ---
$CopilotAgentUser = "copilot-swe-agent"

# --- HELPER FUNCTION ---
function Write-Phase {
    param([string]$Phase, [string]$Message)
    Write-Host "`n========================================" -ForegroundColor Cyan
    Write-Host "  $Phase" -ForegroundColor Cyan
    Write-Host "  $Message" -ForegroundColor White
    Write-Host "========================================`n" -ForegroundColor Cyan
}
