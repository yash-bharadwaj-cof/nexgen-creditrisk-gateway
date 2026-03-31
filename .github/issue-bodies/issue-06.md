## RE-006: Data Flow Diagrams — Context, Level 1, Level 2

### Instructions for Copilot Agent

You are generating **completed** Data Flow Diagrams for the `nexgen-creditrisk-gateway` JBoss Fuse service. Follow the template structure exactly. Produce DFDs at Context (L0), Level 1, and Level 2 in ASCII format.

**Template:** [`docs/re/templates/06-DATA-FLOW-DIAGRAM-TEMPLATE_Version2.md`](docs/re/templates/06-DATA-FLOW-DIAGRAM-TEMPLATE_Version2.md)
**Output file to create:** `docs/re/06-data-flow-diagrams.md`

### External Entities

| Entity | Type | Interface |
|--------|------|-----------|
| REST Client | External Actor | JAX-RS GET `/service/rest/creditrisk/assess` (11 query params) |
| SOAP Client | External Actor | CXF SOAP `/service/soap/creditrisk` (WSS4J UsernameToken) |
| Guidewire Gateway | External System | CXF SOAP `/service/creditriskapi` (WSS4J UsernameToken) |
| Credit Bureau | External System | CXF SOAP client at `BUREAU_ENDPOINT_URL` |
| MongoDB | Data Store | mongo-java-driver 3.4.2, via Wire Tap |

### Data Processes (from 4 Camel routes)

| Process | Description | Key Transformations |
|---------|-------------|-------------------|
| P1: Request Validation | CreditRiskRequestValidator | Validates 7 fields (CR-001→CR-007), throws ValidationException |
| P2: Strategy Selection | ScoringStrategyProcessor | Product type → scoring strategy mapping |
| P3: Bureau Request Building | BureauRequestBuilder | CreditRiskReqType → BureauInquiryRequest |
| P4: Bureau Service Call | CXF client invocation | SOAP call to external credit bureau |
| P5: Bureau Response Mapping | BureauResponseMapper | BureauInquiryResponse → CreditScoreDetail + score categorization |
| P6: Risk Score Calculation | RiskScoreCalculator | CreditScoreDetail + strategy → CreditRiskResType (overall score, risk category, recommendation, affordability) |
| P7: Response Formatting | RestResponseBuilder | Sets HTTP 200, content-type JSON (REST route only) |
| P8: Gateway Preprocessing | GatewayRequestPreProcessor | Sets defaults: channel=GATEWAY, source=API (Gateway route only) |
| P9: Transaction Logging | TransactionLogger | Async Wire Tap → MongoDB document insert |
| P10: Error Handling | ErrorProcessor | Exception → HTTP error response (400/500) |

### Data Flows

| Flow | From | To | Data |
|------|------|----|------|
| DF-01 | REST Client | P1 | 11 query params (applicantId, firstName, lastName, DOB, SIN, province, postalCode, productType, loanAmount, income, employment) |
| DF-02 | SOAP Client | P1 | SOAP XML body (same fields) |
| DF-03 | Gateway | P8 | SOAP XML body (defaults added) |
| DF-04 | P1 | P2 | Validated CreditRiskReqType |
| DF-05 | P2 | P3 | CreditRiskReqType + selected strategy |
| DF-06 | P3 | P4 | BureauInquiryRequest |
| DF-07 | P4 | Credit Bureau | SOAP request |
| DF-08 | Credit Bureau | P5 | BureauInquiryResponse |
| DF-09 | P5 | P6 | CreditScoreDetail |
| DF-10 | P6 | P7 | CreditRiskResType |
| DF-11 | P7 | REST Client | HTTP 200 JSON response |
| DF-12 | P6 | P9 | Transaction data (Wire Tap) |
| DF-13 | P9 | MongoDB | Document insert |
| DF-14 | P10 | Clients | Error response (400/500) |

### Scope — Required Diagrams

1. **Context Diagram (DFD Level 0)** — Single process "Credit Risk Assessment Service" with all 5 external entities
2. **DFD Level 1** — Decomposed into the 10 processes (P1–P10) showing all data flows
3. **DFD Level 2 — Request Validation** — Detailed flow of 7 validation rules
4. **DFD Level 2 — Scoring** — Detailed flow of strategy selection, weight application, scoring computation
5. **DFD Level 2 — Bureau Integration** — Request mapping, SOAP call, response mapping, error handling
6. **Data Flow Catalog** — Complete table of all data flows with content description
7. **Data Stores** — MongoDB collection schema, in-memory model objects
8. **Transformation Rules** — All data mappings (req→bureau, bureau→score, score→response)
9. **Transaction Logging Detail** — Wire Tap mechanism, MongoDB document structure
10. **Annotations** — Gap/hardcoded/dead code markers on DFDs

### Acceptance Criteria

- [ ] Context diagram (L0) shows all 5 external entities and data flows
- [ ] Level 1 DFD decomposes into all 10 processes
- [ ] At least 3 Level 2 DFDs (validation, scoring, bureau integration)
- [ ] All data flows are numbered and cataloged
- [ ] Data transformation rules are documented (field-level mappings)
- [ ] MongoDB logging data flow is documented
- [ ] Wire Tap async pattern is shown in DFDs
- [ ] Annotations mark gaps, dead code, hardcoded values
- [ ] Output file is created at `docs/re/06-data-flow-diagrams.md`
