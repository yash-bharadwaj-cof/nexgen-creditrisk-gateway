## RE-007: BDD Feature Specs — Gherkin Scenarios from Code

### Instructions for Copilot Agent

You are generating **completed** BDD Feature Specifications in Gherkin syntax for the `nexgen-creditrisk-gateway` JBoss Fuse service. Follow the template structure exactly. Derive scenarios directly from the source code behavior.

**Template:** [`docs/re/templates/07-BDD-FEATURE-SPECS-TEMPLATE_Version2.md`](docs/re/templates/07-BDD-FEATURE-SPECS-TEMPLATE_Version2.md)
**Output file to create:** `docs/re/07-bdd-feature-specs.md`

### Features to Derive from Codebase

#### FEAT-001: Credit Risk Assessment via REST
- Source: `CreditRiskRestSvc.java`, `in_rest_creditRiskRouter` Camel route
- Happy path: valid 11 params → bureau call → scored response (HTTP 200 JSON)
- Error paths: missing required fields, invalid formats, bureau timeout, bureau error

#### FEAT-002: Credit Risk Assessment via SOAP
- Source: `CreditRiskSoapSvc.java`, `in_soap_creditRiskRouter` Camel route
- Happy path: valid SOAP request → bureau call → scored response
- Security: WSS4J UsernameToken authentication required

#### FEAT-003: Credit Risk Assessment via Gateway
- Source: `GatewayRequestPreProcessor.java`, `in_gw_creditRiskRouter` Camel route
- Happy path: gateway request → preprocessing (channel=GATEWAY, source=API) → validation → scoring
- Security: WSS4J UsernameToken authentication required

#### FEAT-004: Input Validation
- Source: `CreditRiskRequestValidator.java`
- 7 scenarios for each validation rule (CR-001 through CR-007)
- Boundary conditions: DOB format, SIN format (9 digits), province list, postal code format

#### FEAT-005: Scoring Strategy Selection
- Source: `ScoringStrategyProcessor.java`
- MORTGAGE/AUTO_LOAN → Conservative, CREDIT_CARD/LINE_OF_CREDIT → Aggressive, default → Standard
- Unknown product types → Standard fallback

#### FEAT-006: Standard Scoring Strategy
- Source: `StandardScoringStrategy.java`
- Score thresholds: EXCELLENT (≥750, DTI<30%, util<30%), GOOD (≥680), FAIR (≥620), POOR (≥560)
- Affordability: COMFORTABLE (<0.28), MANAGEABLE (<0.36), STRETCHED (<0.44), UNAFFORDABLE (≥0.44)
- Weights: Bureau=40%, DTI=30%, Employment=20%, Income=10%

#### FEAT-007: Conservative Scoring Strategy
- Source: `ConservativeScoringStrategy.java`
- Stricter thresholds: EXCELLENT (≥780, DTI<25%), GOOD (≥720, DTI<35%)

#### FEAT-008: Aggressive Scoring Strategy
- Source: `AggressiveScoringStrategy.java`
- Lenient thresholds: EXCELLENT (≥720, DTI<40%), GOOD (≥650), FAIR (≥580)

#### FEAT-009: Bureau Score Categorization
- Source: `BureauResponseMapper.java`
- Score ranges: EXCEPTIONAL (≥800), VERY_GOOD (≥740), GOOD (≥670), FAIR (≥580), POOR (<580)

#### FEAT-010: Transaction Logging
- Source: `TransactionLogger.java`, `LoggerConstants.java`, Wire Tap route
- Async logging to MongoDB after every request
- 17 fields logged per transaction

#### FEAT-011: Error Handling
- Source: `ErrorProcessor.java`
- ValidationException → 400, all others → 500

### Gap-Exposing Scenarios (Section 8)

Generate `@gap` tagged scenarios for:
- Missing validations (e.g., no email format check, no amount range validation, no negative number checks)
- Hardcoded values (any static values in scoring/mapping)
- Dead code paths (CreditRiskSoapSvcImpl returns null — stubbed implementation)
- Transaction logging failure scenarios (MongoDB unavailable)

### Scope — All Template Sections

1. **Feature Index** — All 11+ features listed
2. **Feature Specifications** — Full Gherkin with Background, Scenarios, Scenario Outlines with Examples tables
3. **Tag Reference** — functional, execution, classification, priority, and migration-specific tags
4. **Step Definition Patterns** — Reusable Given/When/Then patterns
5. **Coverage Analysis** — Feature-to-business-rule coverage matrix
6. **Gap-Exposing Scenarios** — Missing validation, hardcoded value, dead code, transaction logging scenarios
7. **Gap Scenario Summary** — current vs target system pass/fail matrix

### Acceptance Criteria

- [ ] At least 11 features with full Gherkin syntax including Background, Scenarios, and Scenario Outlines
- [ ] Scenario Outlines use Examples tables for parameterized data (score thresholds, validation inputs)
- [ ] All 7 validation rules (CR-001→CR-007) have corresponding scenarios
- [ ] All 3 scoring strategies have decision-boundary scenarios
- [ ] Migration-specific tags (@gap, @dead-code, @hardcoded, @current-behavior, @target-behavior) are used
- [ ] Gap-exposing scenarios are provided in Section 8
- [ ] Feature-to-business-rule traceability is complete
- [ ] Output file is created at `docs/re/07-bdd-feature-specs.md`
