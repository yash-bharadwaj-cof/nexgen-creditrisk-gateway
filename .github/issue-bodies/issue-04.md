## RE-004: Business Rules Catalog — Scoring Strategies & Validation

### Instructions for Copilot Agent

You are generating a **completed** Business Rules Catalog for the `nexgen-creditrisk-gateway` JBoss Fuse service. Follow the template structure exactly. Extract every business rule from the source code — validations, computations, decisions, constraints.

**Template:** [`docs/re/templates/04-BUSINESS-RULES-CATALOG-TEMPLATE_Version3.md`](docs/re/templates/04-BUSINESS-RULES-CATALOG-TEMPLATE_Version3.md)
**Output file to create:** `docs/re/04-business-rules-catalog.md`

### Known Business Rules in the Codebase

#### Validation Rules (CreditRiskRequestValidator.java)
| Rule Code | Field | Validation | Exception |
|-----------|-------|-----------|-----------|
| CR-001 | applicantId | Required, not empty | ValidationException |
| CR-002 | firstName | Required, not empty | ValidationException |
| CR-003 | lastName | Required, not empty | ValidationException |
| CR-004 | dateOfBirth | Format: dd-MM-yyyy (strict parsing) | ValidationException |
| CR-005 | sinNumber | Format: 9-digit numeric (regex `^\d{9}$`) | ValidationException |
| CR-006 | province | Must be in SUPPORTED_PROVINCE_LIST (ON,BC,AB,QC) | ValidationException |
| CR-007 | postalCode | Canadian format (regex `^[A-Za-z]\d[A-Za-z]\s?\d[A-Za-z]\d$`) | ValidationException |

#### Scoring Strategy Selection (ScoringStrategyProcessor.java)
| Product Type | Strategy Selected |
|-------------|-------------------|
| MORTGAGE, AUTO_LOAN | ConservativeScoringStrategy |
| CREDIT_CARD, LINE_OF_CREDIT | AggressiveScoringStrategy |
| All others | StandardScoringStrategy (default) |

#### Standard Scoring Strategy (StandardScoringStrategy.java)
- **Weights**: Bureau=40%, DTI=30%, Employment=20%, Income=10%
- **Risk Categories**: EXCELLENT (≥750 & DTI<30% & util<30%), GOOD (≥680), FAIR (≥620), POOR (≥560), VERY_POOR (<560)
- **Affordability**: COMFORTABLE (<0.28), MANAGEABLE (<0.36), STRETCHED (<0.44), UNAFFORDABLE (≥0.44)

#### Conservative Scoring Strategy (ConservativeScoringStrategy.java)
- **Weights**: Bureau=35%, DTI=35%, Employment=15%, Income=15%
- **Risk Categories**: EXCELLENT (≥780 & DTI<25%), GOOD (≥720 & DTI<35%), stricter thresholds throughout

#### Aggressive Scoring Strategy (AggressiveScoringStrategy.java)
- **Weights**: Bureau=50%, DTI=25%, Employment=15%, Income=10%
- **Risk Categories**: EXCELLENT (≥720 & DTI<40%), GOOD (≥650), FAIR (≥580), more lenient thresholds

#### Bureau Response Mapping (BureauResponseMapper.java)
| Score Range | Category |
|-------------|----------|
| ≥ 800 | EXCEPTIONAL |
| ≥ 740 | VERY_GOOD |
| ≥ 670 | GOOD |
| ≥ 580 | FAIR |
| < 580 | POOR |

#### Error Handling Rules (ErrorProcessor.java)
| Exception Type | HTTP Status | Behavior |
|---------------|-------------|----------|
| ValidationException | 400 | Returns validation error message |
| All other exceptions | 500 | Returns generic error |

#### Bureau Error Handling (RiskScoreCalculator.java)
- Bureau error code "W215E" triggers special handling
- Risk factors are identified from bureau response data

### Scope — All Template Sections

1. **Business Rules Summary** — Counts by category and severity
2. **Rule Health Summary** — Active, pending validation, dead, hardcoded
3. **Business Rules Detail** — One BR-XXX entry per rule with: pseudocode, source code reference, source line numbers, related entities, traceability to components/test cases
4. **Validation Rules Table** — All 7 CR-XXX rules consolidated
5. **Computation Rules** — Scoring formulas, weight calculations, affordability calculations
6. **Decision Rules (Decision Tables)** — Strategy selection decision table, risk categorization tables for all 3 strategies, bureau score categorization
7. **Workflow / State Transition Rules** — Request processing state machine
8. **Authorization Rules** — WSS4J authentication rules
9. **Unresolved / Ambiguous Rules** — Any unclear logic
10. **Potentially Missing Business Rules** — Expected validations not found in code
11. **Rule Execution Order & Conflicts** — Order of rule execution across the pipeline
12. **Rule Execution Chains** — Validation → Strategy Selection → Bureau → Scoring → Response

### Acceptance Criteria

- [ ] All 7 validation rules (CR-001→CR-007) have BR-XXX entries with pseudocode and source line refs
- [ ] All 3 scoring strategies have complete decision tables with thresholds
- [ ] Bureau score categorization rules are documented
- [ ] Error handling rules are cataloged
- [ ] Strategy selection decision table is complete
- [ ] Rule execution order across the Camel pipeline is documented
- [ ] Missing business rules section identifies gaps (e.g., missing email validation, missing amount range checks)
- [ ] Rule health summary is filled
- [ ] Output file is created at `docs/re/04-business-rules-catalog.md`
