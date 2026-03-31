## RE-010: Field-to-Field Mapping — All Endpoints & Transformations

### Instructions for Copilot Agent

You are generating a **completed** Field-to-Field Mapping document for the `nexgen-creditrisk-gateway` JBoss Fuse service. Follow the template structure exactly. Map every field transformation across every endpoint and processor in the pipeline.

**Template:** [`docs/re/templates/10-FIELD-TO-FIELD-MAPPING-TEMPLATE_Version2.md`](docs/re/templates/10-FIELD-TO-FIELD-MAPPING-TEMPLATE_Version2.md)
**Output file to create:** `docs/re/10-field-to-field-mapping.md`

### All Mapping Points in the Pipeline

#### Mapping 1: REST Query Params → CreditRiskReqType
Source: `CreditRiskRestSvc.java` — 11 JAX-RS `@QueryParam` → `CreditRiskReqType` constructor
| Query Param | Target Field | Type | Transformation |
|------------|-------------|------|---------------|
| applicantId | CreditRiskReqType.applicantId | String | Direct |
| firstName | CreditRiskReqType.firstName | String | Direct |
| lastName | CreditRiskReqType.lastName | String | Direct |
| dateOfBirth | CreditRiskReqType.dateOfBirth | String | Direct (format: dd-MM-yyyy) |
| sinNumber | CreditRiskReqType.sinNumber | String | Direct (9-digit) |
| province | CreditRiskReqType.province | String | Direct |
| postalCode | CreditRiskReqType.postalCode | String | Direct |
| productType | CreditRiskReqType.productType | String | Direct |
| loanAmount | CreditRiskReqType.loanAmount | Double | Direct |
| annualIncome | CreditRiskReqType.annualIncome | Double | Direct |
| employmentStatus | CreditRiskReqType.employmentStatus | String | Direct |

#### Mapping 2: CreditRiskReqType → BureauInquiryRequest
Source: `BureauRequestBuilder.java`
- Maps personal fields (name, DOB, SIN) → BureauSubject
- Injects BUREAU_REQUEST_ID_PREFIX, BUREAU_SUBSCRIBER_CODE, BUREAU_SUBSCRIBER_NAME from config → BureauSubscriber
- Maps financial fields (loanAmount, income) to request

#### Mapping 3: BureauInquiryResponse → CreditScoreDetail
Source: `BureauResponseMapper.java`
- Maps 10 bureau response fields → 8 CreditScoreDetail fields
- Derives scoreCategory from bureauScore using threshold rules (800/740/670/580)

#### Mapping 4: CreditScoreDetail + CreditRiskReqType → CreditRiskResType
Source: `RiskScoreCalculator.java`
- Input: CreditScoreDetail, CreditRiskReqType, selected ScoringStrategy
- Computes: overallScore (weighted), riskCategory, recommendation, affordabilityRating, debtServiceRatio
- Handles: bureau error code "W215E"
- Identifies: riskFactors list

#### Mapping 5: Gateway preprocessing
Source: `GatewayRequestPreProcessor.java`
- Sets requestChannel = "GATEWAY" (hardcoded)
- Sets sourceSystem = "API" (hardcoded)

#### Mapping 6: Transaction → MongoDB Document
Source: `TransactionLogger.java`, `LoggerConstants.java`
- Maps 17 fields from request/response into MongoDB document
- All LoggerConstants field names → document field keys

#### Mapping 7: CreditRiskResType → REST JSON Response
Source: `RestResponseBuilder.java`
- Sets HTTP status 200
- Sets content-type application/json
- Body: serialized CreditRiskResType

#### Mapping 8: Error → Error Response
Source: `ErrorProcessor.java`
- ValidationException → HTTP 400 + validation error message
- Other exceptions → HTTP 500 + generic error message

### Scope — All Template Sections

1. **Mapping Legend** — Transformation types, data types, status definitions
2. **Endpoint Field Mappings** — All 8 mapping points above with source field, target field, data type, transformation rule, nullable, default/hardcoded value, validation, notes
3. **Transformation Rules** — Detailed rules for: score categorization (thresholds), risk category determination (per strategy), affordability rating calculation, DTI computation, overall score weighting
4. **Completeness Metrics** — How many fields are mapped vs unmapped, coverage percentage per endpoint
5. **Missing Mappings** — Fields that exist in models but are never populated (dead fields)
6. **Hardcoded Mappings** — Gateway channel/source, default strategy, error codes

### Acceptance Criteria

- [ ] All 8 mapping points are documented with per-field tables
- [ ] Every field in CreditRiskReqType (13 fields) is mapped from its source
- [ ] Every field in CreditRiskResType (12 fields) is mapped from its computation source
- [ ] Every field in BureauInquiryRequest is mapped from CreditRiskReqType
- [ ] Every field in BureauInquiryResponse is mapped to CreditScoreDetail
- [ ] All 17 LoggerConstants are mapped to MongoDB document fields
- [ ] Score categorization threshold rules are documented as transformation rules
- [ ] All 3 scoring strategy weight formulas are documented
- [ ] Hardcoded values in mappings are flagged with file:line references
- [ ] Dead fields (never populated) are identified
- [ ] Completeness metrics are calculated
- [ ] Output file is created at `docs/re/10-field-to-field-mapping.md`
