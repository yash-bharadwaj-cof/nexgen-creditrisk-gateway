# ============================================================================
# FE Automation — Shared Configuration
# ============================================================================
# Dot-sourced by all phase scripts. Modify these values for any service.
# This is the ONLY file you need to edit when running FE automation on a
# different project.
# ============================================================================

# --- SERVICE IDENTITY ---
$ServiceName       = "nexgen-creditrisk-gateway"
$GitHubOrg         = "yash-bharadwaj-cof"
$RepoVisibility    = "public"                      # public | private

# --- LOCAL PATHS ---
$ServiceRoot       = "d:\OneDrive - Coforge Limited\Desktop\Github\$ServiceName"

# --- REPO-RELATIVE PATHS ---
$FEPromptFile      = "docs/fe/fe-migration-prompt.md"   # FE migration prompt (input)
$DOEFile           = "docs/fe/00-design-of-experiments.md"
$FEOutputDir       = "forward-engineering"               # Target output folder
$FEIssueBodiesDir  = ".github/fe-issue-bodies"           # Generated issue bodies
$FEContextDir      = ".github/fe-context"                # Parsed FE context JSON
$WikiDir           = "docs/wiki"

# --- MILESTONES ---
$FEMilestones = @(
    @{ Title = "M3: Forward Engineering"; Description = "Spring Boot migration — all FE issues (scaffolding through CI/CD)." }
    @{ Title = "M4: FE Verification"; Description = "Build verification, integration testing, and deployment validation." }
)

# --- FE LABEL TAXONOMY ---
# Extends the RE labels. Only includes labels specific to FE.
$FELabels = @(
    # Phase labels (FE-specific — RE labels assumed to exist from RE automation)
    @{ Name = "phase:FE";           Color = "D93F0B"; Desc = "Forward Engineering" }
    @{ Name = "phase:consolidation"; Color = "B60205"; Desc = "Code consolidation phase" }
    # Type labels (FE-specific)
    @{ Name = "type:scaffold";    Color = "C5DEF5"; Desc = "Project scaffolding" }
    @{ Name = "type:migration";   Color = "C5DEF5"; Desc = "Code migration task" }
    @{ Name = "type:config";      Color = "C5DEF5"; Desc = "Configuration task" }
    @{ Name = "type:cicd";        Color = "C5DEF5"; Desc = "CI/CD pipeline" }
    # Priority labels
    @{ Name = "priority:critical"; Color = "B60205"; Desc = "Critical path — blocks everything" }
    @{ Name = "priority:high";     Color = "D93F0B"; Desc = "High priority" }
    @{ Name = "priority:medium";   Color = "FBCA04"; Desc = "Medium priority" }
    @{ Name = "priority:low";      Color = "0E8A16"; Desc = "Low priority" }
    # Wave labels (execution waves from dependency graph)
    @{ Name = "wave:1";  Color = "1D76DB"; Desc = "Wave 1 — Foundation (no dependencies)" }
    @{ Name = "wave:2";  Color = "0075CA"; Desc = "Wave 2 — Core services" }
    @{ Name = "wave:3";  Color = "006B75"; Desc = "Wave 3 — Integration services" }
    @{ Name = "wave:4";  Color = "5319E7"; Desc = "Wave 4 — Orchestration & wiring" }
    @{ Name = "wave:5";  Color = "E4E669"; Desc = "Wave 5 — Testing & deployment" }
)

# --- FE ISSUE MANIFEST ---
# Each entry defines one FE issue. Parsed from the migration prompt.
# Update this manifest if your project has a different decomposition.
#
# Fields:
#   Id        — Issue identifier (e.g. FE-001)
#   Title     — Issue title as shown on GitHub
#   Desc      — Short description for issue body
#   Wave      — Execution wave (1-5)
#   DependsOn — Array of prerequisite FE issue IDs
#   Labels    — Array of label names
#   Sections  — Which sections of the FE prompt are relevant (for context injection)
$FEManifest = @(
    @{
        Id        = "FE-001"
        Title     = "[FE-001] Project Scaffolding — pom.xml, Application class, Base Config"
        Desc      = "Create the Spring Boot project skeleton: pom.xml with all dependencies, CreditRiskApplication.java, application.yml with all profiles, .gitignore."
        Wave      = 1
        DependsOn = @()
        Labels    = @("phase:FE","type:scaffold","priority:critical","wave:1")
        Sections  = @(1,2,6,7)
    }
    @{
        Id        = "FE-002"
        Title     = "[FE-002] Configuration Classes — @ConfigurationProperties Beans"
        Desc      = "Create BureauProperties, ScoringProperties, NexgenProperties, SecurityConfig, CxfConfig with @ConfigurationProperties binding."
        Wave      = 1
        DependsOn = @("FE-001")
        Labels    = @("phase:FE","type:config","scope:config","priority:critical","wave:1")
        Sections  = @(2,7)
    }
    @{
        Id        = "FE-003"
        Title     = "[FE-003] Data Models — Jakarta XML Binding Annotations"
        Desc      = "Migrate all model classes from javax.xml.bind to jakarta.xml.bind. CreditRiskReqType, CreditRiskResType, CreditScoreDetail, DebtServiceDetail, EmploymentRiskDetail, IncomeVerificationDetail, RequestHeader, ResponseHeader, ProvinceType, RequestChannelType."
        Wave      = 2
        DependsOn = @("FE-002")
        Labels    = @("phase:FE","type:migration","scope:model","priority:high","wave:2")
        Sections  = @(3,5,8)
    }
    @{
        Id        = "FE-004"
        Title     = "[FE-004] WSDL Contracts — CreditRiskService + ExternalBureauService"
        Desc      = "Copy and adapt WSDLs to forward-engineering/src/main/resources/wsdl/. Update namespace references."
        Wave      = 1
        DependsOn = @("FE-001")
        Labels    = @("phase:FE","type:migration","scope:endpoint","wave:1")
        Sections  = @(5,6)
    }
    @{
        Id        = "FE-005"
        Title     = "[FE-005] REST Controller — Spring MVC + OpenAPI"
        Desc      = "Create CreditRiskController with @RestController, @GetMapping for /api/v1/creditrisk/assess with 11 query params, SpringDoc annotations."
        Wave      = 2
        DependsOn = @("FE-003")
        Labels    = @("phase:FE","type:migration","scope:endpoint","priority:high","wave:2")
        Sections  = @(4,5)
    }
    @{
        Id        = "FE-006"
        Title     = "[FE-006] SOAP Endpoints — CXF 4.x JAX-WS"
        Desc      = "Create CreditRiskSoapEndpoint and GatewaySoapEndpoint using CXF 4.x Spring Boot starter. Wire WSS4J interceptors via CxfConfig."
        Wave      = 2
        DependsOn = @("FE-003","FE-004")
        Labels    = @("phase:FE","type:migration","scope:endpoint","scope:security","priority:high","wave:2")
        Sections  = @(4,5)
    }
    @{
        Id        = "FE-007"
        Title     = "[FE-007] Validation Service — Business Rules CR-001..CR-007"
        Desc      = "Create ValidationService implementing all 7 validation rules from legacy CreditRiskRequestValidator processor."
        Wave      = 2
        DependsOn = @("FE-003")
        Labels    = @("phase:FE","type:migration","scope:logic","priority:high","wave:2")
        Sections  = @(4,8)
    }
    @{
        Id        = "FE-008"
        Title     = "[FE-008] Scoring Strategies — 3 Strategy Implementations"
        Desc      = "Migrate ScoringStrategy interface + Standard/Conservative/Aggressive implementations. Create ScoringStrategyService to resolve strategy by product type."
        Wave      = 2
        DependsOn = @("FE-003")
        Labels    = @("phase:FE","type:migration","scope:logic","priority:high","wave:2")
        Sections  = @(4,8)
    }
    @{
        Id        = "FE-009"
        Title     = "[FE-009] Bureau Service — Stubbed SOAP Client"
        Desc      = "Create BureauService with build/call/map methods. Bureau call is STUBBED (returns mock response). Generated classes from ExternalBureauService.wsdl."
        Wave      = 2
        DependsOn = @("FE-003")
        Labels    = @("phase:FE","type:migration","scope:integration","priority:high","wave:2")
        Sections  = @(4,5)
    }
    @{
        Id        = "FE-010"
        Title     = "[FE-010] Risk Calculation Service — Score + Category Engine"
        Desc      = "Create RiskCalculationService with calculate() method. Same hardcoded factors (0.03, 0.006), same risk thresholds, same category mapping."
        Wave      = 2
        DependsOn = @("FE-003")
        Labels    = @("phase:FE","type:migration","scope:logic","priority:high","wave:2")
        Sections  = @(4,8)
    }
    @{
        Id        = "FE-011"
        Title     = "[FE-011] Orchestration Service — Replace 4 Camel Routes"
        Desc      = "Create CreditRiskOrchestrationService.processRequest() that wires validation → scoring → bureau → risk calculation → async logging. Single method replaces 4 Camel XML routes."
        Wave      = 3
        DependsOn = @("FE-007","FE-008","FE-009","FE-010","FE-012","FE-014")
        Labels    = @("phase:FE","type:migration","scope:route","priority:critical","wave:3")
        Sections  = @(4)
    }
    @{
        Id        = "FE-012"
        Title     = "[FE-012] MongoDB Transaction Logging — @Async + MongoTemplate"
        Desc      = "Create TransactionLogService with logAsync() and logErrorAsync(). Uses Spring Data MongoTemplate. Replaces Camel wireTap pattern."
        Wave      = 2
        DependsOn = @("FE-002")
        Labels    = @("phase:FE","type:migration","scope:logging","priority:high","wave:2")
        Sections  = @(4,7)
    }
    @{
        Id        = "FE-013"
        Title     = "[FE-013] Security Configuration — Basic Auth + WS-Security"
        Desc      = "Configure Spring Security for REST Basic Auth. SOAP security handled by CXF WSS4J interceptors (already in CxfConfig)."
        Wave      = 2
        DependsOn = @("FE-002")
        Labels    = @("phase:FE","type:migration","scope:security","priority:high","wave:2")
        Sections  = @(2,5)
    }
    @{
        Id        = "FE-014"
        Title     = "[FE-014] Error Handling + Logging — @ControllerAdvice + logback"
        Desc      = "Create GlobalExceptionHandler, CreditRiskProcessingException, ValidationException, logback-spring.xml with SIN masking and profile-specific logging."
        Wave      = 2
        DependsOn = @("FE-001")
        Labels    = @("phase:FE","type:migration","scope:logging","priority:high","wave:2")
        Sections  = @(4,6)
    }
    @{
        Id        = "FE-015"
        Title     = "[FE-015] Unit + Integration Tests — JUnit 5 + Mockito"
        Desc      = "Create tests for all services: ValidationServiceTest, ScoringStrategyServiceTest, BureauServiceTest, RiskCalculationServiceTest, CreditRiskOrchestrationServiceTest, CreditRiskControllerTest. Target >=80% coverage."
        Wave      = 4
        DependsOn = @("FE-011")
        Labels    = @("phase:FE","type:test","priority:high","wave:4")
        Sections  = @(2,10)
    }
    @{
        Id        = "FE-016"
        Title     = "[FE-016] CI/CD Pipeline + EC2 Deployment Scripts"
        Desc      = "Create GitHub Actions workflow (ci.yml), systemd service unit, deploy.sh script for EC2 deployment."
        Wave      = 5
        DependsOn = @("FE-015")
        Labels    = @("phase:FE","type:cicd","phase:deploy","priority:medium","wave:5")
        Sections  = @(2,6)
    }
)

# --- DISCUSSION CATEGORIES ---
$FEDiscussions = @(
    @{
        Title    = "Forward Engineering — Migration Decisions & Progress"
        Category = "General"
        Body     = "Central discussion thread for the Forward Engineering migration. Track decisions, blockers, and architectural choices here."
    }
    @{
        Title    = "FE Code Review & Consolidation Notes"
        Category = "General"
        Body     = "Post-merge consolidation notes: file moves, import fixes, duplicate resolution, and build verification."
    }
)

# --- COPILOT AGENT ---
$CopilotAgentUser = "copilot-swe-agent"

# --- HELPER FUNCTIONS ---
function Write-Phase {
    param([string]$Phase, [string]$Message)
    Write-Host "`n========================================" -ForegroundColor Cyan
    Write-Host "  $Phase" -ForegroundColor Cyan
    Write-Host "  $Message" -ForegroundColor White
    Write-Host "========================================`n" -ForegroundColor Cyan
}

function Write-Step {
    param([string]$Step, [string]$Message)
    Write-Host "[$Step] $Message" -ForegroundColor Yellow
}

function Write-Ok {
    param([string]$Message)
    Write-Host "  $Message" -ForegroundColor Green
}

function Write-Skip {
    param([string]$Message)
    Write-Host "  $Message" -ForegroundColor Gray
}

function Write-Warn {
    param([string]$Message)
    Write-Host "  WARNING: $Message" -ForegroundColor Red
}
