## RE-008: Test Case Inventory — Comprehensive Test Matrix

### Instructions for Copilot Agent

You are generating a **completed** Test Case Inventory for the `nexgen-creditrisk-gateway` JBoss Fuse service. Follow the template structure exactly. Derive test cases from the source code behavior, business rules, and BDD features.

**Template:** [`docs/re/templates/08-TEST-CASE-INVENTORY-TEMPLATE_Version3.md`](docs/re/templates/08-TEST-CASE-INVENTORY-TEMPLATE_Version3.md)
**Output file to create:** `docs/re/08-test-case-inventory.md`

### Test Areas to Cover

#### Functional Test Cases
1. **REST Endpoint Tests** — `/service/rest/creditrisk/assess` with all combinations of 11 query params
2. **SOAP Endpoint Tests** — SOAP request/response with WSS4J auth
3. **Gateway Endpoint Tests** — Gateway request with preprocessing
4. **Validation Tests** — All 7 rules (CR-001→CR-007) with valid, invalid, boundary, empty, null inputs
5. **Strategy Selection Tests** — All product type → strategy mappings
6. **Standard Scoring Tests** — All threshold boundaries (750, 680, 620, 560) with DTI and utilization combinations
7. **Conservative Scoring Tests** — All threshold boundaries (780, 720) with stricter DTI
8. **Aggressive Scoring Tests** — All threshold boundaries (720, 650, 580) with lenient DTI
9. **Bureau Request Building Tests** — Field mapping from CreditRiskReqType to BureauInquiryRequest
10. **Bureau Response Mapping Tests** — Score categorization boundaries (800, 740, 670, 580)
11. **Risk Score Calculation Tests** — Overall score computation, risk factor identification, bureau error "W215E"
12. **Error Handling Tests** — ValidationException→400, generic→500

#### Negative/Error Test Cases
- Missing required fields (each of the 7 validated fields)
- Invalid date format, invalid SIN format, invalid postal code, unsupported province
- Bureau service timeout (30000ms), bureau connection failure, bureau error response
- Null/empty request body, malformed SOAP envelope
- WSS4J authentication failure (invalid credentials, missing security header)

#### Boundary Value Test Cases
- Score thresholds: 559/560, 619/620, 679/680, 749/750, 799/800 (Standard)
- Score thresholds: 719/720, 779/780 (Conservative)
- Score thresholds: 579/580, 649/650, 719/720 (Aggressive)
- DTI thresholds: 0.279/0.28, 0.359/0.36, 0.439/0.44 (Standard affordability)
- Postal code boundaries, SIN boundaries (000000000 vs 999999999)
- Province list boundary (valid ON vs invalid XX)

#### Integration Test Cases
- Full REST end-to-end with mocked bureau
- Full SOAP end-to-end with WSS4J and mocked bureau
- Full Gateway end-to-end with preprocessing and mocked bureau
- MongoDB Wire Tap logging verification
- Bureau timeout and retry behavior

#### Performance Test Cases
- Response time under load (concurrent REST/SOAP/Gateway requests)
- Bureau timeout impact on overall response time
- MongoDB logging throughput via Wire Tap

#### Security Test Cases
- WSS4J UsernameToken validation (SOAP/Gateway endpoints)
- REST endpoint without authentication (currently no auth!)
- JAAS/LDAP integration
- Input injection (SQL/NoSQL/XML injection in all string fields)

#### Migration-Specific Tests
- Field-to-field mapping verification (source → target)
- Dead code removal verification (CreditRiskSoapSvcImpl)
- Hardcoded value replacement tests
- Source-target regression tests

### Scope — All Template Sections

1. **Test Summary Dashboard** — Counts by type, priority, coverage, migration tests
2. **Test Case Details** — Full TC-XXX entries with objective, pre-conditions, test data, test steps, expected results, post-conditions
3. **Tests by Module** — Organized by component
4. **Negative/Error Tests** — Invalid input combinations
5. **Boundary Value Tests** — All score/DTI/validation thresholds
6. **Integration Tests** — Cross-component flow tests
7. **Performance Tests** — Response time, throughput targets
8. **Security Tests** — Auth, injection, transport security
9. **Traceability Matrix** — TC → BR → FEAT → COMP mapping
10. **Migration-Specific Tests** — F2F mapping, dead code, hardcoded values

### Acceptance Criteria

- [ ] At least 50 test cases covering all test types
- [ ] Every validation rule (CR-001→CR-007) has positive, negative, and boundary test cases
- [ ] All 3 scoring strategies have boundary test cases at every threshold
- [ ] Bureau score categorization has boundary tests at 580/670/740/800
- [ ] Traceability matrix maps every test to BR-XXX and COMP-XXX
- [ ] Security test cases cover WSS4J, REST auth gap, input injection
- [ ] Migration-specific tests section is populated
- [ ] Test summary dashboard has counts
- [ ] Output file is created at `docs/re/08-test-case-inventory.md`
