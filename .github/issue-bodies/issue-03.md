## RE-003: Sequence Diagrams — All Request Flows Traced

### Instructions for Copilot Agent

You are generating **completed** Sequence Diagrams for the `nexgen-creditrisk-gateway` JBoss Fuse service. Follow the template structure exactly. Produce ASCII sequence diagrams for every request flow path through the service.

**Template:** [`docs/re/templates/03-SEQUENCE-DIAGRAMS-TEMPLATE_Version3.md`](docs/re/templates/03-SEQUENCE-DIAGRAMS-TEMPLATE_Version3.md)
**Output file to create:** `docs/re/03-sequence-diagrams.md`

### The 4 Camel Routes to Diagram (from blueprint.xml)

**Route 1: `in_rest_creditRiskRouter`** (REST inbound)
```
jaxrs:restEndpoint → requestValidator → scoringStrategyProcessor → bureauRequestBuilder → cxf:bureauEndpoint → bureauResponseMapper → riskScoreCalculator → restResponseBuilder → wireTap(direct:logTransaction)
Exception: onException → errorProcessor
```

**Route 2: `in_soap_creditRiskRouter`** (SOAP inbound)
```
cxf:soapEndpoint → requestValidator → scoringStrategyProcessor → bureauRequestBuilder → cxf:bureauEndpoint → bureauResponseMapper → riskScoreCalculator → wireTap(direct:logTransaction)
Exception: onException → errorProcessor
```

**Route 3: `in_gw_creditRiskRouter`** (Gateway inbound)
```
cxf:gwEndpoint → gwRequestPreProcessor → requestValidator → scoringStrategyProcessor → bureauRequestBuilder → cxf:bureauEndpoint → bureauResponseMapper → riskScoreCalculator → wireTap(direct:logTransaction)
Exception: onException → errorProcessor
```

**Route 4: `creditRisk_logging_route`** (Wire Tap logging)
```
direct:logTransaction → transactionLogger.logTransaction → MongoDB
```

### Key Component Behaviors for Diagramming

| Component | Key Behavior |
|-----------|-------------|
| `CreditRiskRestSvc` | JAX-RS GET `/assess` with 11 query params → constructs `CreditRiskReqType` |
| `CreditRiskRequestValidator` | 7 validation rules (CR-001→CR-007): applicantId, firstName, lastName, DOB format, SIN format, province, postalCode. Throws `ValidationException` on failure |
| `ScoringStrategyProcessor` | Maps product type → strategy: MORTGAGE/AUTO_LOAN→Conservative, CREDIT_CARD/LINE_OF_CREDIT→Aggressive, else→Standard |
| `BureauRequestBuilder` | Maps `CreditRiskReqType` → `BureauInquiryRequest`, injects subscriber code/name/prefix |
| `BureauResponseMapper` | Maps `BureauInquiryResponse` → `CreditScoreDetail`, categorizes score: 800+=EXCEPTIONAL, 740+=VERY_GOOD, 670+=GOOD, 580+=FAIR, <580=POOR |
| `RiskScoreCalculator` | Builds `CreditRiskResType`, calculates overall score using strategy weights, handles bureau errors (code "W215E"), identifies risk factors |
| `GatewayRequestPreProcessor` | Sets defaults: channel=GATEWAY, source=API |
| `RestResponseBuilder` | Sets HTTP 200, content-type application/json |
| `ErrorProcessor` | ValidationException→400, all others→500 |
| `TransactionLogger` | Logs to MongoDB via Wire Tap (async) |

### Scope — Required Diagrams

Create ASCII sequence diagrams for:

1. **SD-001**: REST Happy Path — Full end-to-end flow from REST client through bureau call to response
2. **SD-002**: SOAP Happy Path — Full end-to-end SOAP flow
3. **SD-003**: Gateway Happy Path — Full gateway flow including preprocessing
4. **SD-004**: Validation Failure Flow — Request fails validation (CR-001 through CR-007 scenarios)
5. **SD-005**: Bureau Service Error Flow — External bureau call fails or returns error code "W215E"
6. **SD-006**: Transaction Logging via Wire Tap — Async logging to MongoDB
7. **SD-007**: Scoring Strategy Selection — How product type determines the scoring strategy

Also include:
- **Cross-Cutting Patterns**: Authentication (WSS4J), Error Handling, Transaction Logging
- **Interaction Summary Matrix**: Component-to-component call matrix
- **Dead Code Flow Paths**: Any unreachable code paths (e.g., SOAP service impl returning null)
- **Annotations**: Mark gaps, hardcoded values, and dead code on diagrams

### Acceptance Criteria

- [ ] At least 7 sequence diagrams (SD-001 through SD-007) in ASCII format
- [ ] Each diagram has participants table, step-by-step description with annotations, error/alternative flows
- [ ] All 4 Camel routes are represented
- [ ] Cross-cutting patterns documented (auth, error handling, logging)
- [ ] Dead code flow paths section is filled
- [ ] Annotation summary per diagram identifies gaps, hardcoded values, dead code
- [ ] Interaction summary matrix is complete
- [ ] Output file is created at `docs/re/03-sequence-diagrams.md`
