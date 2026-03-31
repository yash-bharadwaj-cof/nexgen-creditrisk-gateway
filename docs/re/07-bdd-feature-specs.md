# BDD Feature Specifications

---

| **Field**            | **Details**                                    |
|----------------------|------------------------------------------------|
| **Project Name**     | NexGen ESB Migration                           |
| **Application Name** | nexgen-creditrisk-gateway                      |
| **Version**          | 1.0                                            |
| **Date**             | 31-Mar-2026                                    |
| **Prepared By**      | Copilot Reverse Engineering Agent              |
| **Reviewed By**      | —                                              |
| **Status**           | Draft                                          |

---

## 1. Overview

This document contains Behavior-Driven Development (BDD) feature specifications written in Gherkin syntax, derived from the reverse engineering analysis of the `nexgen-creditrisk-gateway` JBoss Fuse / Apache Camel service. These specifications capture the expected behavior of the system from a stakeholder perspective and serve as the authoritative behavioral contract for migration validation.

Scenarios are derived directly from source code inspection of the Camel routes, processors, scoring strategies, and service interfaces. Gap-exposing scenarios (Section 8) document behaviors that are absent in the current source system but required in the target system.

### Gherkin Keywords Reference

| **Keyword**        | **Purpose**                                               |
|--------------------|-----------------------------------------------------------|
| `Feature`          | Describes a feature or functionality                       |
| `Background`       | Steps common to all scenarios in a feature                 |
| `Scenario`         | A concrete example of expected behavior                    |
| `Scenario Outline` | Parameterized scenario with multiple data examples         |
| `Given`            | Pre-condition — establishes the initial context            |
| `When`             | Action/Event — describes the trigger                       |
| `Then`             | Expected outcome — verifiable result                       |
| `And` / `But`      | Additional steps in Given/When/Then                        |
| `Examples`         | Data table for Scenario Outline parameters                 |
| `@tag`             | Metadata tags for categorization and filtering             |

---

## 2. Feature Index

| **Feature ID** | **Feature Name**                              | **Module/Component**                | **Scenarios** | **Priority** | **Status** |
|----------------|-----------------------------------------------|-------------------------------------|---------------|--------------|------------|
| FEAT-001       | Credit Risk Assessment via REST               | CreditRiskRestSvc / in_rest route   | 6             | High         | Draft      |
| FEAT-002       | Credit Risk Assessment via SOAP               | CreditRiskSoapSvc / in_soap route   | 5             | High         | Draft      |
| FEAT-003       | Credit Risk Assessment via Gateway            | GatewayRequestPreProcessor          | 5             | High         | Draft      |
| FEAT-004       | Input Validation                              | CreditRiskRequestValidator          | 12            | High         | Draft      |
| FEAT-005       | Scoring Strategy Selection                    | ScoringStrategyProcessor            | 7             | High         | Draft      |
| FEAT-006       | Standard Scoring Strategy                     | StandardScoringStrategy             | 10            | High         | Draft      |
| FEAT-007       | Conservative Scoring Strategy                 | ConservativeScoringStrategy         | 8             | High         | Draft      |
| FEAT-008       | Aggressive Scoring Strategy                   | AggressiveScoringStrategy           | 8             | High         | Draft      |
| FEAT-009       | Bureau Score Categorization                   | BureauResponseMapper                | 8             | High         | Draft      |
| FEAT-010       | Transaction Logging                           | TransactionLogger / Wire Tap route  | 5             | Medium       | Draft      |
| FEAT-011       | Error Handling                                | ErrorProcessor                      | 5             | High         | Draft      |

---

## 3. Feature Specifications

### 3.1 FEAT-001: Credit Risk Assessment via REST

**File:** `features/rest/credit-risk-rest-assessment.feature`
**Component(s):** `CreditRiskRestSvc`, `in_rest_creditRiskRouter`
**Business Rules:** CR-001, CR-002, CR-003, CR-004, CR-005, CR-006, CR-007
**Priority:** High

```gherkin
@rest @priority-high @FEAT-001
Feature: Credit Risk Assessment via REST
  As an external consumer system
  I want to submit credit risk assessment requests via a REST GET endpoint
  So that I can obtain a scored credit risk response for an applicant

  Background:
    Given the nexgen-creditrisk-gateway service is running
    And the credit bureau SOAP service is reachable

  # --- Happy Path Scenarios ---

  @happy-path @smoke @automated @regression
  Scenario: Valid REST request with all 11 parameters returns a scored response
    Given a valid REST GET request to "/assess"
    And the query parameters are:
      | applicantId      | APP-001234       |
      | firstName        | Jane             |
      | lastName         | Doe              |
      | dateOfBirth      | 1985-06-15       |
      | sin              | 123-456-789      |
      | employmentStatus | FULL_TIME        |
      | annualIncome     | 95000.00         |
      | province         | ON               |
      | postalCode       | M5V 3A8          |
      | productType      | MORTGAGE         |
      | requestedAmount  | 450000.00        |
    When the REST endpoint "/assess" is called
    Then the HTTP response status code is 200
    And the response Content-Type is "application/xml"
    And the response body contains a "riskCategory" field
    And the response body contains a "recommendation" field
    And the response body contains a "overallScore" field

  @happy-path @automated
  Scenario: Valid REST request for CREDIT_CARD product returns aggressive strategy result
    Given a valid REST GET request to "/assess"
    And the query parameter "productType" is "CREDIT_CARD"
    And all other required parameters are valid
    When the REST endpoint "/assess" is called
    Then the HTTP response status code is 200
    And the scoring strategy "AGGRESSIVE" is applied

  # --- Error Paths ---

  @negative @validation @automated
  Scenario: REST request with missing required field returns 400 Bad Request
    Given a REST GET request to "/assess"
    And the query parameter "applicantId" is omitted
    When the REST endpoint "/assess" is called
    Then the HTTP response status code is 400
    And the response contains error code "CR-001"

  @negative @error-handling @automated
  Scenario: REST request when bureau service times out returns 500 error
    Given a valid REST GET request to "/assess" with all required parameters
    And the credit bureau SOAP service is configured to time out after 5 seconds
    When the REST endpoint "/assess" is called
    Then the HTTP response status code is 500
    And the response contains error code "CR-500"
    And the error is logged

  @negative @error-handling @automated
  Scenario: REST request when bureau returns an error response returns 500 error
    Given a valid REST GET request to "/assess" with all required parameters
    And the credit bureau SOAP service returns an error response with code "BUR-999"
    When the REST endpoint "/assess" is called
    Then the HTTP response status code is 500
    And the response contains error code "CR-500"

  @negative @validation @automated
  Scenario: REST request with invalid dateOfBirth format returns 400 Bad Request
    Given a valid REST GET request to "/assess"
    And the query parameter "dateOfBirth" is "15/06/1985"
    When the REST endpoint "/assess" is called
    Then the HTTP response status code is 400
    And the response contains error code "CR-004"
    And the error message indicates "Date of Birth must be in YYYY-MM-DD format"
```

#### Scenario Traceability

| **Scenario**                                              | **Business Rule(s)** | **Test Case(s)** | **Component(s)**         |
|-----------------------------------------------------------|----------------------|------------------|--------------------------|
| Valid REST request with all 11 parameters                 | CR-001→CR-007        | TC-001           | CreditRiskRestSvc        |
| Valid REST request for CREDIT_CARD product                | CR-001→CR-007        | TC-002           | CreditRiskRestSvc        |
| REST request with missing required field                  | CR-001               | TC-003           | CreditRiskRequestValidator|
| REST request when bureau service times out                | —                    | TC-004           | in_rest_creditRiskRouter |
| REST request when bureau returns an error response        | —                    | TC-005           | BureauResponseMapper     |
| REST request with invalid dateOfBirth format              | CR-004               | TC-006           | CreditRiskRequestValidator|

---

### 3.2 FEAT-002: Credit Risk Assessment via SOAP

**File:** `features/soap/credit-risk-soap-assessment.feature`
**Component(s):** `CreditRiskSoapSvc`, `CreditRiskSoapSvcImpl`, `in_soap_creditRiskRouter`
**Business Rules:** CR-001, CR-002, CR-003, CR-004, CR-005, CR-006, CR-007
**Priority:** High

```gherkin
@soap @priority-high @FEAT-002
Feature: Credit Risk Assessment via SOAP
  As an enterprise integration consumer
  I want to submit credit risk requests via a SOAP web service
  So that I can obtain a scored credit risk response using a standards-based interface

  Background:
    Given the nexgen-creditrisk-gateway SOAP endpoint is deployed
    And the WSDL is available at the service URL
    And the credit bureau SOAP service is reachable

  # --- Happy Path Scenarios ---

  @happy-path @smoke @automated @regression
  Scenario: Valid SOAP request with WSS4J UsernameToken returns scored response
    Given a valid SOAP "assessCreditRisk" request
    And the SOAP request body contains a fully populated CreditRiskRequest element
    And the SOAP header contains a valid WSS4J UsernameToken
    When the SOAP endpoint "assessCreditRisk" is called
    Then the SOAP response contains a CreditRiskResponse element
    And the CreditRiskResponse contains a non-null "riskCategory"
    And the CreditRiskResponse contains a non-null "recommendation"

  # --- Security Scenarios ---

  @security @automated @regression
  Scenario: SOAP request without WSS4J UsernameToken is rejected
    Given a valid SOAP "assessCreditRisk" request body
    But no WSS4J UsernameToken is present in the SOAP header
    When the SOAP endpoint "assessCreditRisk" is called
    Then the service returns a SOAP Fault
    And the Fault code indicates an authentication error

  @security @automated @regression
  Scenario: SOAP request with invalid WSS4J credentials is rejected
    Given a valid SOAP "assessCreditRisk" request body
    And the SOAP header contains a WSS4J UsernameToken with incorrect credentials
    When the SOAP endpoint "assessCreditRisk" is called
    Then the service returns a SOAP Fault
    And the Fault code indicates an authentication error

  # --- Error Paths ---

  @negative @validation @automated
  Scenario: SOAP request with missing required CreditRiskRequest fields returns validation error
    Given a SOAP "assessCreditRisk" request
    And a valid WSS4J UsernameToken is present
    But the "applicantId" field is omitted from the CreditRiskRequest
    When the SOAP endpoint "assessCreditRisk" is called
    Then the SOAP response contains a CreditRiskResponse with error code "CR-001"

  @negative @dead-code @current-behavior
  Scenario: SOAP implementation returns null (stubbed implementation — dead code)
    Given a valid authenticated SOAP "assessCreditRisk" request
    When the SOAP endpoint "assessCreditRisk" is called via CreditRiskSoapSvcImpl
    Then the CreditRiskSoapSvcImpl.assessCreditRisk method returns null
    And no actual scoring processing occurs in the SOAP service implementation
```

#### Scenario Traceability

| **Scenario**                                              | **Business Rule(s)**   | **Test Case(s)** | **Component(s)**            |
|-----------------------------------------------------------|------------------------|------------------|-----------------------------|
| Valid SOAP request with UsernameToken                     | CR-001→CR-007          | TC-010           | CreditRiskSoapSvc           |
| SOAP request without UsernameToken                        | SEC-001                | TC-011           | in_soap_creditRiskRouter    |
| SOAP request with invalid credentials                     | SEC-001                | TC-012           | in_soap_creditRiskRouter    |
| SOAP request with missing required fields                 | CR-001                 | TC-013           | CreditRiskRequestValidator  |
| SOAP implementation returns null (dead code)              | —                      | TC-014           | CreditRiskSoapSvcImpl       |

---

### 3.3 FEAT-003: Credit Risk Assessment via Gateway

**File:** `features/gateway/credit-risk-gateway-assessment.feature`
**Component(s):** `GatewayRequestPreProcessor`, `in_gw_creditRiskRouter`
**Business Rules:** CR-001, CR-002, CR-003, CR-004, CR-005, CR-006, CR-007
**Priority:** High

```gherkin
@gateway @priority-high @FEAT-003
Feature: Credit Risk Assessment via Gateway
  As an integration platform (e.g. policy administration system)
  I want to submit credit risk requests via the Gateway SOAP endpoint
  So that requests are pre-processed with standard channel/source metadata before scoring

  Background:
    Given the nexgen-creditrisk-gateway Gateway endpoint is deployed
    And the credit bureau SOAP service is reachable

  # --- Happy Path Scenarios ---

  @happy-path @smoke @automated @regression
  Scenario: Valid gateway request is pre-processed and returns a scored response
    Given a valid gateway SOAP request with a populated CreditRiskRequest
    And a valid WSS4J UsernameToken is present in the SOAP header
    When the gateway endpoint "assessCreditRisk" is called
    Then GatewayRequestPreProcessor sets requestChannel to "API"
    And the RequestHeader.sourceSystem is set to "GATEWAY"
    And a UUID transactionId is generated in the RequestHeader
    And the response contains a valid scored CreditRiskResponse

  @happy-path @automated
  Scenario: Gateway request that already includes a RequestHeader does not overwrite it
    Given a valid gateway SOAP request
    And the request already contains a RequestHeader with transactionId "TX-EXISTING-001"
    When the gateway endpoint "assessCreditRisk" is called
    Then the RequestHeader is not replaced
    And the transactionId remains "TX-EXISTING-001"

  @happy-path @automated
  Scenario: Gateway request that already specifies requestChannel does not overwrite it
    Given a valid gateway SOAP request
    And the request already contains requestChannel "BATCH"
    When the gateway endpoint "assessCreditRisk" is called
    Then the requestChannel remains "BATCH"
    And it is not overwritten to "API"

  # --- Security Scenarios ---

  @security @automated @regression
  Scenario: Gateway SOAP request without WSS4J UsernameToken is rejected
    Given a valid gateway SOAP request body
    But no WSS4J UsernameToken is present in the SOAP header
    When the gateway endpoint "assessCreditRisk" is called
    Then the service returns a SOAP Fault
    And the Fault code indicates an authentication error

  # --- Error Paths ---

  @negative @validation @automated
  Scenario: Gateway request with null body is rejected with validation error
    Given the gateway endpoint receives a request with a null body
    When GatewayRequestPreProcessor processes the exchange
    Then no NullPointerException is thrown
    And the exchange continues to the validation step
    And the validator returns error code "CR-000"
```

#### Scenario Traceability

| **Scenario**                                              | **Business Rule(s)**   | **Test Case(s)** | **Component(s)**                  |
|-----------------------------------------------------------|------------------------|------------------|-----------------------------------|
| Valid gateway request pre-processed and scored            | CR-001→CR-007          | TC-020           | GatewayRequestPreProcessor        |
| Gateway request with existing RequestHeader               | —                      | TC-021           | GatewayRequestPreProcessor        |
| Gateway request with existing requestChannel              | —                      | TC-022           | GatewayRequestPreProcessor        |
| Gateway request without UsernameToken                     | SEC-001                | TC-023           | in_gw_creditRiskRouter            |
| Gateway request with null body                            | CR-000                 | TC-024           | GatewayRequestPreProcessor        |

---

### 3.4 FEAT-004: Input Validation

**File:** `features/validation/credit-risk-input-validation.feature`
**Component(s):** `CreditRiskRequestValidator`
**Business Rules:** CR-001, CR-002, CR-003, CR-004, CR-005, CR-006, CR-007
**Priority:** High

```gherkin
@validation @priority-high @FEAT-004
Feature: Input Validation
  As the nexgen-creditrisk-gateway service
  I want to validate all incoming credit risk request fields
  So that only well-formed requests reach the bureau and scoring engine

  Background:
    Given the CreditRiskRequestValidator processor is active in the Camel route

  # --- Required Field Validation (CR-001 to CR-003) ---

  @negative @validation @automated @regression @BR-CR-001
  Scenario: Request with blank applicantId is rejected with CR-001
    Given a CreditRiskRequest with all valid fields
    But the "applicantId" field is blank
    When the validator processes the request
    Then a ValidationException is thrown
    And the error code is "CR-001"
    And the error message is "Applicant ID field is blank"

  @negative @validation @automated @regression @BR-CR-002
  Scenario: Request with blank firstName is rejected with CR-002
    Given a CreditRiskRequest with all valid fields
    But the "firstName" field is blank
    When the validator processes the request
    Then a ValidationException is thrown
    And the error code is "CR-002"
    And the error message is "First Name field is blank"

  @negative @validation @automated @regression @BR-CR-003
  Scenario: Request with blank lastName is rejected with CR-003
    Given a CreditRiskRequest with all valid fields
    But the "lastName" field is blank
    When the validator processes the request
    Then a ValidationException is thrown
    And the error code is "CR-003"
    And the error message is "Last Name field is blank"

  # --- DateOfBirth Validation (CR-004) ---

  @negative @validation @automated @regression @BR-CR-004
  Scenario: Request with blank dateOfBirth is rejected with CR-004
    Given a CreditRiskRequest with all valid fields
    But the "dateOfBirth" field is blank
    When the validator processes the request
    Then a ValidationException is thrown
    And the error code is "CR-004"

  @negative @validation @automated @regression @data-driven @BR-CR-004
  Scenario Outline: Request with invalid dateOfBirth format is rejected with CR-004
    Given a CreditRiskRequest with all valid fields
    And the "dateOfBirth" field is "<invalid_dob>"
    When the validator processes the request
    Then a ValidationException is thrown
    And the error code is "CR-004"
    And the error message is "Date of Birth must be in YYYY-MM-DD format"

    Examples:
      | invalid_dob  |
      | 15/06/1985   |
      | 06-15-1985   |
      | 19850615     |
      | 1985/06/15   |
      | 15-Jun-1985  |
      | 85-06-15     |

  @happy-path @validation @automated @BR-CR-004
  Scenario: Request with valid dateOfBirth format YYYY-MM-DD passes validation
    Given a CreditRiskRequest with all valid fields
    And the "dateOfBirth" field is "1985-06-15"
    When the validator processes the request
    Then no ValidationException is thrown

  # --- Social Insurance Number Validation (CR-005) ---

  @negative @validation @automated @regression @BR-CR-005
  Scenario: Request with blank SIN is rejected with CR-005
    Given a CreditRiskRequest with all valid fields
    But the "socialInsuranceNumber" field is blank
    When the validator processes the request
    Then a ValidationException is thrown
    And the error code is "CR-005"

  @negative @validation @automated @regression @data-driven @BR-CR-005
  Scenario Outline: Request with invalid SIN format is rejected with CR-005
    Given a CreditRiskRequest with all valid fields
    And the "socialInsuranceNumber" field is "<invalid_sin>"
    When the validator processes the request
    Then a ValidationException is thrown
    And the error code is "CR-005"
    And the error message is "Social Insurance Number must be in NNN-NNN-NNN format"

    Examples:
      | invalid_sin   |
      | 12345678      |
      | 1234567890    |
      | ABC-DEF-GHI   |
      | 123-45-6789   |
      | 123 456 789   |
      | 12-3456-789   |

  @happy-path @validation @automated @data-driven @BR-CR-005
  Scenario Outline: Request with valid SIN format passes validation
    Given a CreditRiskRequest with all valid fields
    And the "socialInsuranceNumber" field is "<valid_sin>"
    When the validator processes the request
    Then no ValidationException is thrown

    Examples:
      | valid_sin   |
      | 123-456-789 |
      | 123456789   |

  # --- Province Validation (CR-006) ---

  @negative @validation @automated @regression @BR-CR-006
  Scenario: Request with invalid province code is rejected with CR-006
    Given a CreditRiskRequest with all valid fields
    And the "province" field is "XX"
    When the validator processes the request
    Then a ValidationException is thrown
    And the error code is "CR-006"
    And the error message contains "Invalid province code: XX"

  @happy-path @validation @automated @data-driven @BR-CR-006
  Scenario Outline: Request with a valid Canadian province code passes validation
    Given a CreditRiskRequest with all valid fields
    And the "province" field is "<province_code>"
    When the validator processes the request
    Then no ValidationException is thrown

    Examples:
      | province_code |
      | ON            |
      | BC            |
      | AB            |
      | QC            |
      | MB            |
      | SK            |
      | NS            |
      | NB            |
      | NL            |
      | PE            |
      | NT            |
      | NU            |
      | YT            |

  @happy-path @validation @automated @BR-CR-006
  Scenario: Request with blank province code skips province validation
    Given a CreditRiskRequest with all valid fields
    And the "province" field is blank
    When the validator processes the request
    Then no ValidationException is thrown for province

  # --- Postal Code Validation (CR-007) ---

  @negative @validation @automated @regression @BR-CR-007
  Scenario: Request with invalid postal code format is rejected with CR-007
    Given a CreditRiskRequest with all valid fields
    And the "postalCode" field is "12345"
    When the validator processes the request
    Then a ValidationException is thrown
    And the error code is "CR-007"
    And the error message is "Invalid postal code format"

  @happy-path @validation @automated @data-driven @BR-CR-007
  Scenario Outline: Request with valid Canadian postal code format passes validation
    Given a CreditRiskRequest with all valid fields
    And the "postalCode" field is "<postal_code>"
    When the validator processes the request
    Then no ValidationException is thrown

    Examples:
      | postal_code |
      | M5V 3A8     |
      | M5V3A8      |
      | K1A 0B1     |
      | V6B2W9      |

  @happy-path @validation @automated @BR-CR-007
  Scenario: Request with blank postal code skips postal code validation
    Given a CreditRiskRequest with all valid fields
    And the "postalCode" field is blank
    When the validator processes the request
    Then no ValidationException is thrown for postal code
```

#### Scenario Traceability

| **Scenario**                                              | **Business Rule(s)** | **Test Case(s)** | **Component(s)**              |
|-----------------------------------------------------------|----------------------|------------------|-------------------------------|
| Blank applicantId rejected                                | CR-001               | TC-030           | CreditRiskRequestValidator    |
| Blank firstName rejected                                  | CR-002               | TC-031           | CreditRiskRequestValidator    |
| Blank lastName rejected                                   | CR-003               | TC-032           | CreditRiskRequestValidator    |
| Blank dateOfBirth rejected                                | CR-004               | TC-033           | CreditRiskRequestValidator    |
| Invalid DOB format rejected (6 formats)                   | CR-004               | TC-034           | CreditRiskRequestValidator    |
| Valid DOB format passes                                   | CR-004               | TC-035           | CreditRiskRequestValidator    |
| Blank SIN rejected                                        | CR-005               | TC-036           | CreditRiskRequestValidator    |
| Invalid SIN format rejected (6 formats)                   | CR-005               | TC-037           | CreditRiskRequestValidator    |
| Valid SIN format passes (2 variants)                      | CR-005               | TC-038           | CreditRiskRequestValidator    |
| Invalid province code rejected                            | CR-006               | TC-039           | CreditRiskRequestValidator    |
| Valid Canadian province codes pass (13)                   | CR-006               | TC-040           | CreditRiskRequestValidator    |
| Blank province skips validation                           | CR-006               | TC-041           | CreditRiskRequestValidator    |
| Invalid postal code format rejected                       | CR-007               | TC-042           | CreditRiskRequestValidator    |
| Valid Canadian postal code formats pass (4 variants)      | CR-007               | TC-043           | CreditRiskRequestValidator    |
| Blank postal code skips validation                        | CR-007               | TC-044           | CreditRiskRequestValidator    |

---

### 3.5 FEAT-005: Scoring Strategy Selection

**File:** `features/scoring/scoring-strategy-selection.feature`
**Component(s):** `ScoringStrategyProcessor`
**Business Rules:** STRAT-001, STRAT-002, STRAT-003
**Priority:** High

```gherkin
@scoring @priority-high @FEAT-005
Feature: Scoring Strategy Selection
  As the scoring engine
  I want to select the appropriate scoring strategy based on the product type
  So that each credit product is evaluated with appropriately calibrated risk thresholds

  Background:
    Given a validated CreditRiskRequest is stored in the exchange property "VALIDATED_REQUEST"

  # --- Conservative Strategy Selection ---

  @happy-path @automated @regression @STRAT-001
  Scenario: MORTGAGE product type selects Conservative strategy
    Given the "productType" in the request is "MORTGAGE"
    When ScoringStrategyProcessor processes the exchange
    Then the selected scoring strategy is "CONSERVATIVE"
    And the exchange property "SCORING_STRATEGY" contains a ConservativeScoringStrategy instance

  @happy-path @automated @regression @STRAT-001
  Scenario: AUTO_LOAN product type selects Conservative strategy
    Given the "productType" in the request is "AUTO_LOAN"
    When ScoringStrategyProcessor processes the exchange
    Then the selected scoring strategy is "CONSERVATIVE"

  # --- Aggressive Strategy Selection ---

  @happy-path @automated @regression @STRAT-002
  Scenario: CREDIT_CARD product type selects Aggressive strategy
    Given the "productType" in the request is "CREDIT_CARD"
    When ScoringStrategyProcessor processes the exchange
    Then the selected scoring strategy is "AGGRESSIVE"
    And the exchange property "SCORING_STRATEGY" contains an AggressiveScoringStrategy instance

  @happy-path @automated @regression @STRAT-002
  Scenario: LINE_OF_CREDIT product type selects Aggressive strategy
    Given the "productType" in the request is "LINE_OF_CREDIT"
    When ScoringStrategyProcessor processes the exchange
    Then the selected scoring strategy is "AGGRESSIVE"

  # --- Standard Strategy (Default Fallback) ---

  @happy-path @automated @regression @STRAT-003
  Scenario: Unknown product type falls back to Standard strategy
    Given the "productType" in the request is "PERSONAL_LOAN"
    When ScoringStrategyProcessor processes the exchange
    Then the selected scoring strategy is "STANDARD"
    And the exchange property "SCORING_STRATEGY" contains a StandardScoringStrategy instance

  @happy-path @automated @STRAT-003
  Scenario: Null product type falls back to Standard strategy
    Given the "productType" in the request is null
    When ScoringStrategyProcessor processes the exchange
    Then the selected scoring strategy is "STANDARD"

  @happy-path @data-driven @automated @regression
  Scenario Outline: Product type to strategy mapping is correct
    Given the "productType" in the request is "<product_type>"
    When ScoringStrategyProcessor processes the exchange
    Then the selected scoring strategy is "<expected_strategy>"

    Examples:
      | product_type     | expected_strategy |
      | MORTGAGE         | CONSERVATIVE      |
      | AUTO_LOAN        | CONSERVATIVE      |
      | CREDIT_CARD      | AGGRESSIVE        |
      | LINE_OF_CREDIT   | AGGRESSIVE        |
      | PERSONAL_LOAN    | STANDARD          |
      | STUDENT_LOAN     | STANDARD          |
      | OTHER            | STANDARD          |
```

#### Scenario Traceability

| **Scenario**                                  | **Business Rule(s)** | **Test Case(s)** | **Component(s)**          |
|-----------------------------------------------|----------------------|------------------|---------------------------|
| MORTGAGE → Conservative                       | STRAT-001            | TC-050           | ScoringStrategyProcessor  |
| AUTO_LOAN → Conservative                      | STRAT-001            | TC-051           | ScoringStrategyProcessor  |
| CREDIT_CARD → Aggressive                      | STRAT-002            | TC-052           | ScoringStrategyProcessor  |
| LINE_OF_CREDIT → Aggressive                   | STRAT-002            | TC-053           | ScoringStrategyProcessor  |
| Unknown product type → Standard               | STRAT-003            | TC-054           | ScoringStrategyProcessor  |
| Null product type → Standard                  | STRAT-003            | TC-055           | ScoringStrategyProcessor  |
| Product type mapping table (7 entries)        | STRAT-001→003        | TC-056           | ScoringStrategyProcessor  |

---

### 3.6 FEAT-006: Standard Scoring Strategy

**File:** `features/scoring/standard-scoring-strategy.feature`
**Component(s):** `StandardScoringStrategy`
**Business Rules:** SCORE-STD-001 through SCORE-STD-005
**Priority:** High

```gherkin
@scoring @standard-strategy @priority-high @FEAT-006
Feature: Standard Scoring Strategy
  As the scoring engine
  I want to apply the Standard scoring strategy for general credit products
  So that applicants are categorized using balanced bureau-score, DTI, and utilization thresholds

  Background:
    Given the StandardScoringStrategy is instantiated
    And the strategy weights are: Bureau=40%, DTI=30%, Utilization=20%, Employment=10%

  # --- Risk Categorization ---

  @happy-path @automated @regression @data-driven @SCORE-STD-001
  Scenario Outline: Bureau score and DTI ratio determines risk category
    Given a bureau score of <bureau_score>
    And a debt-to-income ratio of <dti_ratio>
    And a utilization rate of <util_rate>
    When the Standard strategy categorizes the risk
    Then the risk category is "<expected_category>"

    Examples:
      | bureau_score | dti_ratio | util_rate | expected_category |
      | 780          | 0.20      | 0.15      | EXCELLENT         |
      | 750          | 0.29      | 0.29      | EXCELLENT         |
      | 749          | 0.20      | 0.15      | GOOD              |
      | 750          | 0.30      | 0.15      | GOOD              |
      | 750          | 0.20      | 0.30      | GOOD              |
      | 720          | 0.25      | 0.25      | GOOD              |
      | 680          | 0.35      | 0.40      | GOOD              |
      | 679          | 0.25      | 0.25      | FAIR              |
      | 620          | 0.40      | 0.50      | FAIR              |
      | 619          | 0.25      | 0.25      | POOR              |
      | 560          | 0.45      | 0.55      | POOR              |
      | 559          | 0.25      | 0.25      | VERY_POOR         |
      | 300          | 0.80      | 0.90      | VERY_POOR         |

  # --- Affordability Rating ---

  @happy-path @automated @regression @data-driven @SCORE-STD-002
  Scenario Outline: Debt service ratio determines affordability rating
    Given a debt service ratio of <dsr>
    When the Standard strategy determines the affordability rating
    Then the affordability rating is "<expected_rating>"

    Examples:
      | dsr   | expected_rating |
      | 0.10  | COMFORTABLE     |
      | 0.27  | COMFORTABLE     |
      | 0.279 | COMFORTABLE     |
      | 0.28  | MANAGEABLE      |
      | 0.35  | MANAGEABLE      |
      | 0.359 | MANAGEABLE      |
      | 0.36  | STRETCHED       |
      | 0.43  | STRETCHED       |
      | 0.439 | STRETCHED       |
      | 0.44  | OVEREXTENDED    |
      | 0.80  | OVEREXTENDED    |

  # --- Recommendation Logic ---

  @happy-path @automated @regression @data-driven @SCORE-STD-003
  Scenario Outline: Risk category and amount-to-income ratio determines recommendation
    Given a risk category of "<risk_category>"
    And a requested amount of <requested_amount>
    And an annual income of <annual_income>
    When the Standard strategy determines the recommendation
    Then the recommendation is "<expected_recommendation>"

    Examples:
      | risk_category | requested_amount | annual_income | expected_recommendation  |
      | EXCELLENT     | 50000.00         | 100000.00     | APPROVE                  |
      | GOOD          | 400000.00        | 100000.00     | APPROVE_WITH_CONDITIONS  |
      | GOOD          | 300000.00        | 100000.00     | APPROVE                  |
      | FAIR          | 400000.00        | 100000.00     | REFER_TO_UNDERWRITER     |
      | FAIR          | 100000.00        | 100000.00     | APPROVE_WITH_CONDITIONS  |
      | POOR          | 50000.00         | 100000.00     | REFER_TO_UNDERWRITER     |
      | VERY_POOR     | 50000.00         | 100000.00     | DECLINE                  |

  # --- Employment Score Contribution ---

  @happy-path @automated @data-driven @SCORE-STD-004
  Scenario Outline: Employment status contributes to the overall score calculation
    Given a bureau score of 700
    And a debt-to-income ratio of 0.30
    And a utilization rate of 0.25
    And an employment status of "<employment_status>"
    When the Standard strategy calculates the overall score
    Then the overall score is <expected_score>

    Examples:
      | employment_status | expected_score |
      | FULL_TIME         | 67             |
      | PART_TIME         | 63             |
      | SELF_EMPLOYED     | 62             |
      | CONTRACT          | 61             |
      | RETIRED           | 65             |
      | STUDENT           | 57             |
      | UNEMPLOYED        | 47             |

  # --- Boundary Conditions ---

  @boundary @automated @SCORE-STD-001
  Scenario: Bureau score exactly at EXCELLENT threshold (750) with satisfying DTI and utilization
    Given a bureau score of 750
    And a debt-to-income ratio of 0.29
    And a utilization rate of 0.29
    When the Standard strategy categorizes the risk
    Then the risk category is "EXCELLENT"

  @boundary @automated @SCORE-STD-001
  Scenario: Bureau score exactly at EXCELLENT threshold (750) with DTI at boundary (0.30) is GOOD
    Given a bureau score of 750
    And a debt-to-income ratio of 0.30
    And a utilization rate of 0.15
    When the Standard strategy categorizes the risk
    Then the risk category is "GOOD"
```

#### Scenario Traceability

| **Scenario**                                            | **Business Rule(s)** | **Test Case(s)** | **Component(s)**         |
|---------------------------------------------------------|----------------------|------------------|--------------------------|
| Risk category thresholds (13 data rows)                 | SCORE-STD-001        | TC-060           | StandardScoringStrategy  |
| Affordability rating thresholds (11 data rows)          | SCORE-STD-002        | TC-061           | StandardScoringStrategy  |
| Recommendation logic (7 data rows)                      | SCORE-STD-003        | TC-062           | StandardScoringStrategy  |
| Employment status score contribution (7 statuses)       | SCORE-STD-004        | TC-063           | StandardScoringStrategy  |
| Boundary: EXCELLENT at exact threshold                  | SCORE-STD-001        | TC-064           | StandardScoringStrategy  |
| Boundary: EXCELLENT fails at DTI=0.30                   | SCORE-STD-001        | TC-065           | StandardScoringStrategy  |

---

### 3.7 FEAT-007: Conservative Scoring Strategy

**File:** `features/scoring/conservative-scoring-strategy.feature`
**Component(s):** `ConservativeScoringStrategy`
**Business Rules:** SCORE-CON-001 through SCORE-CON-004
**Priority:** High

```gherkin
@scoring @conservative-strategy @priority-high @FEAT-007
Feature: Conservative Scoring Strategy
  As the scoring engine
  I want to apply the Conservative scoring strategy for secured credit products
  So that mortgages and auto loans are evaluated with stricter risk thresholds

  Background:
    Given the ConservativeScoringStrategy is instantiated
    And the strategy is used for MORTGAGE and AUTO_LOAN product types

  # --- Risk Categorization ---

  @happy-path @automated @regression @data-driven @SCORE-CON-001
  Scenario Outline: Bureau score and financial ratios determine risk category (conservative)
    Given a bureau score of <bureau_score>
    And a debt-to-income ratio of <dti_ratio>
    And a utilization rate of <util_rate>
    When the Conservative strategy categorizes the risk
    Then the risk category is "<expected_category>"

    Examples:
      | bureau_score | dti_ratio | util_rate | expected_category |
      | 800          | 0.20      | 0.20      | EXCELLENT         |
      | 780          | 0.24      | 0.24      | EXCELLENT         |
      | 780          | 0.25      | 0.20      | GOOD              |
      | 779          | 0.20      | 0.20      | GOOD              |
      | 750          | 0.34      | 0.30      | GOOD              |
      | 720          | 0.35      | 0.30      | FAIR              |
      | 719          | 0.20      | 0.20      | FAIR              |
      | 660          | 0.40      | 0.40      | FAIR              |
      | 659          | 0.20      | 0.20      | POOR              |
      | 600          | 0.50      | 0.50      | POOR              |
      | 599          | 0.20      | 0.20      | VERY_POOR         |
      | 300          | 0.90      | 0.90      | VERY_POOR         |

  # --- Affordability Rating (Stricter Thresholds) ---

  @happy-path @automated @regression @data-driven @SCORE-CON-002
  Scenario Outline: Conservative debt service ratio thresholds for affordability
    Given a debt service ratio of <dsr>
    When the Conservative strategy determines the affordability rating
    Then the affordability rating is "<expected_rating>"

    Examples:
      | dsr   | expected_rating |
      | 0.10  | COMFORTABLE     |
      | 0.249 | COMFORTABLE     |
      | 0.25  | MANAGEABLE      |
      | 0.319 | MANAGEABLE      |
      | 0.32  | STRETCHED       |
      | 0.399 | STRETCHED       |
      | 0.40  | OVEREXTENDED    |
      | 0.80  | OVEREXTENDED    |

  # --- Recommendation Logic (Conservative — more likely to decline) ---

  @happy-path @automated @regression @data-driven @SCORE-CON-003
  Scenario Outline: Conservative recommendation is stricter than Standard
    Given a risk category of "<risk_category>"
    And a requested amount of <requested_amount>
    And an annual income of <annual_income>
    When the Conservative strategy determines the recommendation
    Then the recommendation is "<expected_recommendation>"

    Examples:
      | risk_category | requested_amount | annual_income | expected_recommendation  |
      | EXCELLENT     | 400000.00        | 100000.00     | APPROVE                  |
      | EXCELLENT     | 600000.00        | 100000.00     | APPROVE_WITH_CONDITIONS  |
      | GOOD          | 300000.00        | 100000.00     | APPROVE_WITH_CONDITIONS  |
      | GOOD          | 500000.00        | 100000.00     | REFER_TO_UNDERWRITER     |
      | FAIR          | 50000.00         | 100000.00     | REFER_TO_UNDERWRITER     |
      | POOR          | 50000.00         | 100000.00     | DECLINE                  |
      | VERY_POOR     | 50000.00         | 100000.00     | DECLINE                  |

  # --- Boundary Conditions ---

  @boundary @automated @SCORE-CON-001
  Scenario: Conservative EXCELLENT requires all three conditions simultaneously
    Given a bureau score of 780
    And a debt-to-income ratio of 0.24
    And a utilization rate of 0.24
    When the Conservative strategy categorizes the risk
    Then the risk category is "EXCELLENT"

  @boundary @automated @SCORE-CON-001
  Scenario: Conservative EXCELLENT fails if utilization rate is at boundary (0.25)
    Given a bureau score of 780
    And a debt-to-income ratio of 0.20
    And a utilization rate of 0.25
    When the Conservative strategy categorizes the risk
    Then the risk category is "GOOD"
```

#### Scenario Traceability

| **Scenario**                                              | **Business Rule(s)** | **Test Case(s)** | **Component(s)**              |
|-----------------------------------------------------------|----------------------|------------------|-------------------------------|
| Risk category thresholds (12 data rows)                   | SCORE-CON-001        | TC-070           | ConservativeScoringStrategy   |
| Affordability rating thresholds (8 data rows)             | SCORE-CON-002        | TC-071           | ConservativeScoringStrategy   |
| Recommendation logic (7 data rows)                        | SCORE-CON-003        | TC-072           | ConservativeScoringStrategy   |
| Boundary: EXCELLENT requires all 3 conditions             | SCORE-CON-001        | TC-073           | ConservativeScoringStrategy   |
| Boundary: EXCELLENT fails at utilization=0.25             | SCORE-CON-001        | TC-074           | ConservativeScoringStrategy   |

---

### 3.8 FEAT-008: Aggressive Scoring Strategy

**File:** `features/scoring/aggressive-scoring-strategy.feature`
**Component(s):** `AggressiveScoringStrategy`
**Business Rules:** SCORE-AGG-001 through SCORE-AGG-004
**Priority:** High

```gherkin
@scoring @aggressive-strategy @priority-high @FEAT-008
Feature: Aggressive Scoring Strategy
  As the scoring engine
  I want to apply the Aggressive scoring strategy for unsecured credit products
  So that credit cards and lines of credit are evaluated with more lenient risk thresholds

  Background:
    Given the AggressiveScoringStrategy is instantiated
    And the strategy is used for CREDIT_CARD and LINE_OF_CREDIT product types

  # --- Risk Categorization ---

  @happy-path @automated @regression @data-driven @SCORE-AGG-001
  Scenario Outline: Bureau score and financial ratios determine risk category (aggressive)
    Given a bureau score of <bureau_score>
    And a debt-to-income ratio of <dti_ratio>
    And a utilization rate of <util_rate>
    When the Aggressive strategy categorizes the risk
    Then the risk category is "<expected_category>"

    Examples:
      | bureau_score | dti_ratio | util_rate | expected_category |
      | 800          | 0.30      | 0.50      | EXCELLENT         |
      | 720          | 0.39      | 0.60      | EXCELLENT         |
      | 720          | 0.40      | 0.50      | GOOD              |
      | 719          | 0.20      | 0.20      | GOOD              |
      | 680          | 0.45      | 0.60      | GOOD              |
      | 650          | 0.50      | 0.70      | GOOD              |
      | 649          | 0.20      | 0.20      | FAIR              |
      | 580          | 0.55      | 0.75      | FAIR              |
      | 579          | 0.20      | 0.20      | POOR              |
      | 520          | 0.60      | 0.80      | POOR              |
      | 519          | 0.20      | 0.20      | VERY_POOR         |
      | 300          | 0.90      | 0.95      | VERY_POOR         |

  # --- Affordability Rating (Lenient Thresholds) ---

  @happy-path @automated @regression @data-driven @SCORE-AGG-002
  Scenario Outline: Aggressive debt service ratio thresholds for affordability
    Given a debt service ratio of <dsr>
    When the Aggressive strategy determines the affordability rating
    Then the affordability rating is "<expected_rating>"

    Examples:
      | dsr   | expected_rating |
      | 0.10  | COMFORTABLE     |
      | 0.349 | COMFORTABLE     |
      | 0.35  | MANAGEABLE      |
      | 0.449 | MANAGEABLE      |
      | 0.45  | STRETCHED       |
      | 0.549 | STRETCHED       |
      | 0.55  | OVEREXTENDED    |
      | 0.90  | OVEREXTENDED    |

  # --- Recommendation Logic (Aggressive — more approvals) ---

  @happy-path @automated @regression @data-driven @SCORE-AGG-003
  Scenario Outline: Aggressive recommendation approves more applicants than Standard
    Given a risk category of "<risk_category>"
    And any requested amount and annual income
    When the Aggressive strategy determines the recommendation
    Then the recommendation is "<expected_recommendation>"

    Examples:
      | risk_category | expected_recommendation  |
      | EXCELLENT     | APPROVE                  |
      | GOOD          | APPROVE                  |
      | FAIR          | APPROVE_WITH_CONDITIONS  |
      | POOR          | REFER_TO_UNDERWRITER     |
      | VERY_POOR     | DECLINE                  |

  # --- Boundary Conditions ---

  @boundary @automated @SCORE-AGG-001
  Scenario: Aggressive EXCELLENT threshold at bureau 720 with DTI just under 0.40
    Given a bureau score of 720
    And a debt-to-income ratio of 0.39
    And a utilization rate of 0.50
    When the Aggressive strategy categorizes the risk
    Then the risk category is "EXCELLENT"

  @boundary @automated @SCORE-AGG-001
  Scenario: Aggressive EXCELLENT fails when DTI is exactly 0.40
    Given a bureau score of 720
    And a debt-to-income ratio of 0.40
    And a utilization rate of 0.50
    When the Aggressive strategy categorizes the risk
    Then the risk category is "GOOD"

  @boundary @automated @SCORE-AGG-001
  Scenario: Aggressive FAIR lower boundary at bureau score 580
    Given a bureau score of 580
    And a debt-to-income ratio of 0.50
    And a utilization rate of 0.70
    When the Aggressive strategy categorizes the risk
    Then the risk category is "FAIR"
```

#### Scenario Traceability

| **Scenario**                                              | **Business Rule(s)** | **Test Case(s)** | **Component(s)**            |
|-----------------------------------------------------------|----------------------|------------------|-----------------------------|
| Risk category thresholds (12 data rows)                   | SCORE-AGG-001        | TC-080           | AggressiveScoringStrategy   |
| Affordability rating thresholds (8 data rows)             | SCORE-AGG-002        | TC-081           | AggressiveScoringStrategy   |
| Recommendation logic (5 data rows)                        | SCORE-AGG-003        | TC-082           | AggressiveScoringStrategy   |
| Boundary: EXCELLENT at DTI=0.39                           | SCORE-AGG-001        | TC-083           | AggressiveScoringStrategy   |
| Boundary: EXCELLENT fails at DTI=0.40                     | SCORE-AGG-001        | TC-084           | AggressiveScoringStrategy   |
| Boundary: FAIR at bureau score 580                        | SCORE-AGG-001        | TC-085           | AggressiveScoringStrategy   |

---

### 3.9 FEAT-009: Bureau Score Categorization

**File:** `features/bureau/bureau-score-categorization.feature`
**Component(s):** `BureauResponseMapper`
**Business Rules:** BUREAU-001 through BUREAU-005
**Priority:** High

```gherkin
@bureau @priority-high @FEAT-009
Feature: Bureau Score Categorization
  As the bureau response mapper
  I want to map raw bureau credit scores to standardized score range categories
  So that downstream components have a normalized bureau score tier for reporting

  Background:
    Given the BureauResponseMapper processor is active in the Camel route

  # --- Happy Path: Score Range Mapping ---

  @happy-path @automated @regression @data-driven @BUREAU-001
  Scenario Outline: Bureau credit score is mapped to the correct score range category
    Given the credit bureau returns a credit score of <bureau_score>
    When BureauResponseMapper processes the bureau response
    Then the bureauScoreRange in the CreditScoreDetail is "<expected_range>"

    Examples:
      | bureau_score | expected_range |
      | 900          | EXCEPTIONAL    |
      | 850          | EXCEPTIONAL    |
      | 800          | EXCEPTIONAL    |
      | 799          | VERY_GOOD      |
      | 740          | VERY_GOOD      |
      | 739          | GOOD           |
      | 670          | GOOD           |
      | 669          | FAIR           |
      | 580          | FAIR           |
      | 579          | POOR           |
      | 400          | POOR           |
      | 300          | POOR           |

  @boundary @automated @BUREAU-001
  Scenario: Bureau score exactly at EXCEPTIONAL lower boundary (800) maps to EXCEPTIONAL
    Given the credit bureau returns a credit score of 800
    When BureauResponseMapper processes the bureau response
    Then the bureauScoreRange is "EXCEPTIONAL"

  @boundary @automated @BUREAU-001
  Scenario: Bureau score at 799 maps to VERY_GOOD (not EXCEPTIONAL)
    Given the credit bureau returns a credit score of 799
    When BureauResponseMapper processes the bureau response
    Then the bureauScoreRange is "VERY_GOOD"

  # --- Utilization Rate Calculation ---

  @happy-path @automated @regression @BUREAU-002
  Scenario: Utilization rate is calculated when credit limit is greater than zero
    Given the credit bureau returns totalCreditLimit of 10000.00
    And the credit bureau returns totalBalance of 3500.00
    When BureauResponseMapper processes the bureau response
    Then the CreditScoreDetail.utilizationRate is 0.35

  @happy-path @automated @BUREAU-002
  Scenario: Utilization rate defaults to 0.0 when credit limit is zero
    Given the credit bureau returns totalCreditLimit of 0.00
    And the credit bureau returns totalBalance of 500.00
    When BureauResponseMapper processes the bureau response
    Then the CreditScoreDetail.utilizationRate is 0.0

  @happy-path @automated @BUREAU-002
  Scenario: Utilization rate defaults to 0.0 when credit limit is null
    Given the credit bureau returns a null totalCreditLimit
    When BureauResponseMapper processes the bureau response
    Then the CreditScoreDetail.utilizationRate is 0.0

  # --- Bureau Error Handling ---

  @negative @error-handling @automated @regression @BUREAU-003
  Scenario: Null bureau response sets BUREAU_ERROR property to true
    Given the credit bureau response is null
    When BureauResponseMapper processes the bureau response
    Then the exchange property "BUREAU_ERROR" is true
    And the exchange property "BUREAU_ERROR_CODE" is "CR-301"

  @negative @error-handling @automated @regression @BUREAU-004
  Scenario: Bureau response with error code sets BUREAU_ERROR property to true
    Given the credit bureau response contains errorCode "BUR-999"
    And the error message is "Bureau service unavailable"
    When BureauResponseMapper processes the bureau response
    Then the exchange property "BUREAU_ERROR" is true
    And the exchange property "BUREAU_ERROR_CODE" is "CR-302"
    And the exchange property "BUREAU_ERROR_MSG" is "Bureau service unavailable"

  @negative @error-handling @automated @BUREAU-005
  Scenario: Null bureau score maps to UNKNOWN score range
    Given the credit bureau returns a null credit score
    When BureauResponseMapper processes the bureau response
    Then the bureauScoreRange in the CreditScoreDetail is "UNKNOWN"
```

#### Scenario Traceability

| **Scenario**                                        | **Business Rule(s)** | **Test Case(s)** | **Component(s)**       |
|-----------------------------------------------------|----------------------|------------------|------------------------|
| Score range mapping (12 data rows)                  | BUREAU-001           | TC-090           | BureauResponseMapper   |
| Boundary: 800 → EXCEPTIONAL                         | BUREAU-001           | TC-091           | BureauResponseMapper   |
| Boundary: 799 → VERY_GOOD                           | BUREAU-001           | TC-092           | BureauResponseMapper   |
| Utilization rate calculated from limit/balance      | BUREAU-002           | TC-093           | BureauResponseMapper   |
| Utilization defaults to 0.0 when limit = 0          | BUREAU-002           | TC-094           | BureauResponseMapper   |
| Utilization defaults to 0.0 when limit = null       | BUREAU-002           | TC-095           | BureauResponseMapper   |
| Null bureau response → BUREAU_ERROR=true, CR-301    | BUREAU-003           | TC-096           | BureauResponseMapper   |
| Bureau error code → BUREAU_ERROR=true, CR-302       | BUREAU-004           | TC-097           | BureauResponseMapper   |
| Null bureau score → UNKNOWN range                   | BUREAU-005           | TC-098           | BureauResponseMapper   |

---

### 3.10 FEAT-010: Transaction Logging

**File:** `features/logging/transaction-logging.feature`
**Component(s):** `TransactionLogger`, `LoggerConstants`, Wire Tap route
**Business Rules:** LOG-001 through LOG-004
**Priority:** Medium

```gherkin
@logging @priority-medium @FEAT-010
Feature: Transaction Logging
  As the operations team
  I want every credit risk transaction to be asynchronously logged to MongoDB
  So that a complete audit trail of all requests and responses is maintained

  Background:
    Given the TransactionLogger is configured with a reachable MongoDB instance
    And the Wire Tap route is active in the Camel route definition

  # --- Happy Path: Successful Transaction Logging ---

  @happy-path @smoke @automated @regression @LOG-001
  Scenario: Successful credit risk assessment is logged to MongoDB with all 17 fields
    Given a successful credit risk assessment completes
    And the exchange contains the following properties:
      | TRANSACTION_ID    | TX-001234             |
      | APPLICANT_ID      | APP-001234            |
      | PROVINCE          | ON                    |
      | PRODUCT_TYPE      | MORTGAGE              |
      | REQUEST_CHANNEL   | REST                  |
      | SOURCE_SYSTEM     | DIRECT                |
      | RISK_CATEGORY     | GOOD                  |
      | OVERALL_SCORE     | 72                    |
      | RECOMMENDATION    | APPROVE               |
      | BUREAU_SCORE      | 710                   |
    When the TransactionLogger.logTransaction method is invoked
    Then a MongoDB document is inserted with 17 fields
    And the document contains "transactionId" equal to "TX-001234"
    And the document contains "applicantId" equal to "APP-001234"
    And the document contains "status" equal to "success"
    And the document contains "riskCategory" equal to "GOOD"
    And the document contains "recommendation" equal to "APPROVE"

  @happy-path @automated @LOG-001
  Scenario: Transaction logging occurs asynchronously via Wire Tap pattern
    Given a valid credit risk assessment is in progress
    When the Wire Tap route triggers the transaction logger
    Then the primary response is returned without waiting for logging to complete
    And the transaction is logged asynchronously to MongoDB

  # --- Error Path: Failed Request Logging ---

  @negative @automated @regression @LOG-002
  Scenario: Failed credit risk assessment is logged with error status
    Given a credit risk assessment that failed validation
    And the exchange contains ERROR_CODE "CR-001" and ERROR_MESSAGE "Applicant ID field is blank"
    And the exchange does not contain a "RISK_RESPONSE" property
    When the TransactionLogger.logTransaction method is invoked
    Then a MongoDB document is inserted
    And the document contains "status" equal to "error"
    And the document contains "errorCode" equal to "CR-001"
    And the document contains "errorMessage" equal to "Applicant ID field is blank"

  # --- MongoDB Unavailability ---

  @negative @error-handling @automated @regression @gap @logging @migration-critical @LOG-003
  Scenario: MongoDB unavailable — transaction logging failure does not affect primary response
    Given the MongoDB instance is unreachable
    When a valid credit risk assessment is processed
    Then the primary credit risk response is returned successfully
    And a logging error is recorded in the application log
    But no exception is propagated to the client

  @negative @automated @LOG-004
  Scenario: MongoDB connection closed after each logging operation
    Given a successful credit risk assessment completes
    When the TransactionLogger.logTransaction method is invoked
    Then the MongoClient connection is closed in the finally block
    And no connection leak occurs
```

#### Scenario Traceability

| **Scenario**                                            | **Business Rule(s)** | **Test Case(s)** | **Component(s)**    |
|---------------------------------------------------------|----------------------|------------------|---------------------|
| Successful assessment logged with 17 fields             | LOG-001              | TC-100           | TransactionLogger   |
| Logging via Wire Tap is asynchronous                    | LOG-001              | TC-101           | Wire Tap route      |
| Failed assessment logged with error status              | LOG-002              | TC-102           | TransactionLogger   |
| MongoDB unavailable — primary response unaffected       | LOG-003              | TC-103           | TransactionLogger   |
| MongoClient connection closed in finally                | LOG-004              | TC-104           | TransactionLogger   |

---

### 3.11 FEAT-011: Error Handling

**File:** `features/error/error-handling.feature`
**Component(s):** `ErrorProcessor`
**Business Rules:** ERR-001, ERR-002
**Priority:** High

```gherkin
@error-handling @priority-high @FEAT-011
Feature: Error Handling
  As the nexgen-creditrisk-gateway service
  I want to centrally handle all exceptions and return standardized error responses
  So that clients always receive a well-formed error response with appropriate HTTP status codes

  Background:
    Given the ErrorProcessor is registered in the Camel route's onException clause

  # --- Validation Exception Handling ---

  @happy-path @automated @regression @ERR-001
  Scenario: ValidationException is mapped to HTTP 400 Bad Request
    Given the exchange contains a ValidationException with error code "CR-001"
    And the exception message is "Applicant ID field is blank"
    When ErrorProcessor processes the exchange
    Then the HTTP response status code is 400
    And the ResponseHeader.statusCode is "CR-001"
    And the ResponseHeader.statusMessage is "Applicant ID field is blank"
    And the response riskCategory is "ERROR"
    And the response recommendation is "REFER_TO_UNDERWRITER"

  @happy-path @automated @data-driven @regression @ERR-001
  Scenario Outline: All validation error codes produce HTTP 400 responses
    Given the exchange contains a ValidationException with error code "<error_code>"
    When ErrorProcessor processes the exchange
    Then the HTTP response status code is 400
    And the ResponseHeader.statusCode is "<error_code>"

    Examples:
      | error_code |
      | CR-000     |
      | CR-001     |
      | CR-002     |
      | CR-003     |
      | CR-004     |
      | CR-005     |
      | CR-006     |
      | CR-007     |

  # --- General Exception Handling ---

  @negative @automated @regression @ERR-002
  Scenario: Any non-validation exception is mapped to HTTP 500 Internal Server Error
    Given the exchange contains a NullPointerException
    When ErrorProcessor processes the exchange
    Then the HTTP response status code is 500
    And the ResponseHeader.statusCode is "CR-500"
    And the ResponseHeader.statusMessage is "Internal service error occurred. Please contact support."
    And the response riskCategory is "ERROR"
    And the error is logged at ERROR level

  @negative @automated @ERR-002
  Scenario: Bureau timeout exception is mapped to HTTP 500 Internal Server Error
    Given the exchange contains a SocketTimeoutException from the bureau call
    When ErrorProcessor processes the exchange
    Then the HTTP response status code is 500
    And the ResponseHeader.statusCode is "CR-500"

  # --- Response Structure ---

  @happy-path @automated @regression @ERR-001
  Scenario: Error response always includes a timestamp in the ResponseHeader
    Given the exchange contains any exception
    When ErrorProcessor processes the exchange
    Then the ResponseHeader.timestamp is set to the current date and time
    And the timestamp format matches "yyyy-MM-dd'T'HH:mm:ss"
```

#### Scenario Traceability

| **Scenario**                                        | **Business Rule(s)** | **Test Case(s)** | **Component(s)**  |
|-----------------------------------------------------|----------------------|------------------|-------------------|
| ValidationException → HTTP 400                      | ERR-001              | TC-110           | ErrorProcessor    |
| All 8 CR-00X codes produce HTTP 400 (data-driven)   | ERR-001              | TC-111           | ErrorProcessor    |
| Non-validation exception → HTTP 500                 | ERR-002              | TC-112           | ErrorProcessor    |
| Bureau timeout → HTTP 500                           | ERR-002              | TC-113           | ErrorProcessor    |
| Error response includes timestamp                   | ERR-001, ERR-002     | TC-114           | ErrorProcessor    |

---

## 4. Tag Reference

_Tags used across all feature files for categorization and test execution filtering._

### Functional Tags

| **Tag**               | **Description**                                                   |
|-----------------------|-------------------------------------------------------------------|
| `@rest`               | Scenarios related to the REST endpoint (`CreditRiskRestSvc`)      |
| `@soap`               | Scenarios related to the SOAP endpoint (`CreditRiskSoapSvc`)      |
| `@gateway`            | Scenarios related to the Gateway endpoint                         |
| `@validation`         | Scenarios that exercise input validation rules                    |
| `@scoring`            | Scenarios related to scoring strategy processing                  |
| `@bureau`             | Scenarios related to bureau response mapping                      |
| `@logging`            | Scenarios that validate transaction logging                       |
| `@error-handling`     | Scenarios that exercise error handling paths                      |
| `@FEAT-001`           | Links scenario to FEAT-001 (REST Assessment)                      |
| `@FEAT-002`           | Links scenario to FEAT-002 (SOAP Assessment)                      |
| `@FEAT-003`           | Links scenario to FEAT-003 (Gateway Assessment)                   |
| `@FEAT-004`           | Links scenario to FEAT-004 (Input Validation)                     |
| `@FEAT-005`           | Links scenario to FEAT-005 (Strategy Selection)                   |
| `@FEAT-006`           | Links scenario to FEAT-006 (Standard Strategy)                    |
| `@FEAT-007`           | Links scenario to FEAT-007 (Conservative Strategy)                |
| `@FEAT-008`           | Links scenario to FEAT-008 (Aggressive Strategy)                  |
| `@FEAT-009`           | Links scenario to FEAT-009 (Bureau Categorization)                |
| `@FEAT-010`           | Links scenario to FEAT-010 (Transaction Logging)                  |
| `@FEAT-011`           | Links scenario to FEAT-011 (Error Handling)                       |
| `@BR-CR-001`          | Links scenario to business rule CR-001 (ApplicantId required)     |
| `@BR-CR-002`          | Links scenario to business rule CR-002 (FirstName required)       |
| `@BR-CR-003`          | Links scenario to business rule CR-003 (LastName required)        |
| `@BR-CR-004`          | Links scenario to business rule CR-004 (DOB required + format)    |
| `@BR-CR-005`          | Links scenario to business rule CR-005 (SIN required + format)    |
| `@BR-CR-006`          | Links scenario to business rule CR-006 (Province valid code)      |
| `@BR-CR-007`          | Links scenario to business rule CR-007 (Postal code format)       |

### Execution Tags

| **Tag**             | **Description**                                      |
|---------------------|------------------------------------------------------|
| `@smoke`            | Included in smoke test suite                          |
| `@regression`       | Included in full regression suite                     |
| `@wip`              | Work in progress — not ready for execution            |
| `@manual`           | Requires manual testing                               |
| `@automated`        | Fully automated scenario                              |

### Classification Tags

| **Tag**               | **Description**                                      |
|-----------------------|------------------------------------------------------|
| `@happy-path`         | Normal/expected behavior                              |
| `@negative`           | Error/failure scenarios                               |
| `@boundary`           | Boundary/edge conditions                              |
| `@data-driven`        | Parameterized with example data                       |
| `@validation`         | Input validation scenarios                            |
| `@error-handling`     | System error handling scenarios                       |
| `@security`           | Security-related scenarios                            |

### Priority Tags

| **Tag**               | **Description**                                    |
|-----------------------|----------------------------------------------------|
| `@priority-high`      | Must-have — critical functionality                  |
| `@priority-medium`    | Should-have — important functionality               |
| `@priority-low`       | Nice-to-have — minor functionality                  |

### Migration-Specific Tags

| **Tag**               | **Description**                                                    |
|-----------------------|--------------------------------------------------------------------|
| `@current-behavior`   | Scenario documents the current source system behavior               |
| `@target-behavior`    | Scenario documents the expected target system behavior              |
| `@gap`                | Scenario exposes a gap — will fail against source, pass on target   |
| `@dead-code`          | Scenario related to a dead code path                                |
| `@hardcoded`          | Scenario documents hardcoded/static value behavior                  |
| `@migration-critical` | Scenario must be validated during migration acceptance testing      |
| `@logging`            | Scenario validates transaction logging behavior                     |

---

## 5. Step Definition Patterns

_Common step patterns for reuse across feature files._

### Given Steps

| **Step Pattern**                                                                | **Description**                                        |
|---------------------------------------------------------------------------------|--------------------------------------------------------|
| `Given the nexgen-creditrisk-gateway service is running`                         | Verifies the Camel context and endpoints are active    |
| `Given the credit bureau SOAP service is reachable`                              | Stubs or mocks the bureau endpoint as available        |
| `Given a valid REST GET request to {path}`                                       | Constructs a valid REST request builder                |
| `Given the query parameters are: (data table)`                                   | Populates REST query params from a data table          |
| `Given a valid SOAP {operation} request`                                         | Constructs a valid SOAP envelope                       |
| `Given a CreditRiskRequest with all valid fields`                                | Constructs a fully valid request object                |
| `Given a validated CreditRiskRequest is stored in exchange property {prop}`      | Pre-populates exchange with validated request          |
| `Given a bureau score of {score}`                                                | Sets up scoring inputs with a specific bureau score    |
| `Given a debt-to-income ratio of {ratio}`                                        | Sets the DTI ratio for scoring                         |
| `Given a utilization rate of {rate}`                                             | Sets the utilization rate for scoring                  |
| `Given a debt service ratio of {dsr}`                                            | Sets the debt service ratio for affordability          |
| `Given a risk category of {category}`                                            | Sets the risk category for recommendation              |
| `Given the credit bureau returns a credit score of {score}`                      | Sets up mock bureau response with specified score      |
| `Given the credit bureau response is null`                                       | Simulates null bureau response                         |
| `Given the MongoDB instance is unreachable`                                      | Mocks MongoDB as unavailable                           |
| `Given the exchange contains a {exception_type} with error code {code}`          | Populates exchange exception property                  |

### When Steps

| **Step Pattern**                                                                | **Description**                                        |
|---------------------------------------------------------------------------------|--------------------------------------------------------|
| `When the REST endpoint {path} is called`                                        | Executes the REST request via HTTP client              |
| `When the SOAP endpoint {operation} is called`                                   | Executes the SOAP request via SOAP client              |
| `When the gateway endpoint {operation} is called`                                | Executes the gateway SOAP request                      |
| `When the validator processes the request`                                       | Invokes CreditRiskRequestValidator.process()           |
| `When ScoringStrategyProcessor processes the exchange`                           | Invokes ScoringStrategyProcessor.process()             |
| `When the Standard strategy categorizes the risk`                                | Calls StandardScoringStrategy.categorizeRisk()         |
| `When the Conservative strategy categorizes the risk`                            | Calls ConservativeScoringStrategy.categorizeRisk()     |
| `When the Aggressive strategy categorizes the risk`                              | Calls AggressiveScoringStrategy.categorizeRisk()       |
| `When the Standard strategy determines the affordability rating`                 | Calls StandardScoringStrategy.determineAffordabilityRating() |
| `When the Standard strategy determines the recommendation`                       | Calls StandardScoringStrategy.determineRecommendation() |
| `When BureauResponseMapper processes the bureau response`                        | Invokes BureauResponseMapper.process()                 |
| `When the TransactionLogger.logTransaction method is invoked`                    | Calls TransactionLogger.logTransaction()               |
| `When ErrorProcessor processes the exchange`                                     | Invokes ErrorProcessor.process()                       |

### Then Steps

| **Step Pattern**                                                                | **Description**                                        |
|---------------------------------------------------------------------------------|--------------------------------------------------------|
| `Then the HTTP response status code is {status_code}`                            | Asserts HTTP status code                               |
| `Then the response body contains a {field} field`                                | Asserts response field presence                        |
| `Then the response contains error code {code}`                                   | Asserts error code in response                         |
| `Then a ValidationException is thrown`                                           | Asserts ValidationException was raised                 |
| `Then no ValidationException is thrown`                                          | Asserts no exception raised                            |
| `Then the error code is {code}`                                                  | Asserts the ValidationException error code             |
| `Then the error message is {message}`                                            | Asserts the ValidationException message                |
| `Then the selected scoring strategy is {strategy_name}`                          | Asserts exchange property SCORING_STRATEGY name        |
| `Then the risk category is {category}`                                           | Asserts categorizeRisk() return value                  |
| `Then the affordability rating is {rating}`                                      | Asserts determineAffordabilityRating() return value    |
| `Then the recommendation is {recommendation}`                                    | Asserts determineRecommendation() return value         |
| `Then the overall score is {score}`                                              | Asserts calculateOverallScore() return value           |
| `Then the bureauScoreRange in the CreditScoreDetail is {range}`                  | Asserts mapped score range field                       |
| `Then the CreditScoreDetail.utilizationRate is {rate}`                           | Asserts calculated utilization rate                    |
| `Then the exchange property {prop} is {value}`                                   | Asserts an exchange property value                     |
| `Then a MongoDB document is inserted with {count} fields`                        | Verifies MongoDB insert was called with document       |
| `Then the document contains {field} equal to {value}`                            | Asserts a specific field in the logged document        |
| `Then the ResponseHeader.statusCode is {code}`                                   | Asserts error response header code                     |
| `Then the ResponseHeader.statusMessage is {message}`                             | Asserts error response header message                  |
| `Then the service returns a SOAP Fault`                                          | Asserts SOAP fault in response                         |

---

## 6. Coverage Analysis

### Feature-to-Business-Rule Coverage

| **Business Rule** | **Rule Description**                          | **Covered By Feature(s)**        | **Scenario Count** | **Coverage** |
|-------------------|-----------------------------------------------|----------------------------------|--------------------|--------------|
| CR-001            | ApplicantId is required                       | FEAT-001, FEAT-002, FEAT-004     | 4                  | Full         |
| CR-002            | FirstName is required                         | FEAT-004                         | 2                  | Full         |
| CR-003            | LastName is required                          | FEAT-004                         | 2                  | Full         |
| CR-004            | DateOfBirth required and YYYY-MM-DD format    | FEAT-001, FEAT-004               | 9                  | Full         |
| CR-005            | SIN required and NNN-NNN-NNN format           | FEAT-004                         | 5                  | Full         |
| CR-006            | Province must be valid Canadian province code | FEAT-004                         | 4                  | Full         |
| CR-007            | PostalCode must match Canadian format         | FEAT-004                         | 3                  | Full         |
| SEC-001           | WSS4J UsernameToken authentication            | FEAT-002, FEAT-003               | 4                  | Full         |
| STRAT-001         | MORTGAGE/AUTO_LOAN → Conservative strategy    | FEAT-005                         | 3                  | Full         |
| STRAT-002         | CREDIT_CARD/LINE_OF_CREDIT → Aggressive       | FEAT-005                         | 3                  | Full         |
| STRAT-003         | Default fallback → Standard strategy          | FEAT-005                         | 3                  | Full         |
| SCORE-STD-001     | Standard risk categorization thresholds       | FEAT-006                         | 15                 | Full         |
| SCORE-STD-002     | Standard affordability rating thresholds      | FEAT-006                         | 11                 | Full         |
| SCORE-STD-003     | Standard recommendation logic                 | FEAT-006                         | 7                  | Full         |
| SCORE-STD-004     | Employment status score contribution          | FEAT-006                         | 7                  | Full         |
| SCORE-CON-001     | Conservative risk categorization thresholds   | FEAT-007                         | 14                 | Full         |
| SCORE-CON-002     | Conservative affordability thresholds         | FEAT-007                         | 8                  | Full         |
| SCORE-CON-003     | Conservative recommendation logic             | FEAT-007                         | 7                  | Full         |
| SCORE-AGG-001     | Aggressive risk categorization thresholds     | FEAT-008                         | 14                 | Full         |
| SCORE-AGG-002     | Aggressive affordability thresholds           | FEAT-008                         | 8                  | Full         |
| SCORE-AGG-003     | Aggressive recommendation logic               | FEAT-008                         | 5                  | Full         |
| BUREAU-001        | Bureau score → score range category mapping   | FEAT-009                         | 14                 | Full         |
| BUREAU-002        | Utilization rate calculation from bureau data | FEAT-009                         | 3                  | Full         |
| BUREAU-003        | Null bureau response sets BUREAU_ERROR        | FEAT-009                         | 1                  | Full         |
| BUREAU-004        | Bureau error response sets BUREAU_ERROR       | FEAT-009                         | 1                  | Full         |
| LOG-001           | All transactions logged to MongoDB async      | FEAT-010                         | 2                  | Full         |
| LOG-002           | Failed requests logged with error status      | FEAT-010                         | 1                  | Full         |
| LOG-003           | MongoDB unavailable — primary response intact | FEAT-010                         | 1                  | Full         |
| ERR-001           | ValidationException → HTTP 400                | FEAT-011                         | 3                  | Full         |
| ERR-002           | Other exceptions → HTTP 500                   | FEAT-011                         | 2                  | Full         |

### Uncovered Business Rules

| **Business Rule** | **Rule Description**                          | **Reason for No Coverage**                                      |
|-------------------|-----------------------------------------------|-----------------------------------------------------------------|
| N/A — Email validation    | No email format validation exists    | Not implemented in source; covered as gap in Section 8.1        |
| N/A — Amount range        | No amount range validation exists    | Not implemented in source; covered as gap in Section 8.1        |
| N/A — Negative amount     | No negative amount check exists      | Not implemented in source; covered as gap in Section 8.1        |
| N/A — SOAP impl scoring   | CreditRiskSoapSvcImpl returns null   | Dead code; covered as dead code in Section 8.3                  |

---

## 7. Notes & Observations

| **#** | **Observation**                                                                                           | **Feature(s)**        | **Impact**  |
|-------|-----------------------------------------------------------------------------------------------------------|-----------------------|-------------|
| 1     | `CreditRiskSoapSvcImpl.assessCreditRisk()` returns null — SOAP scoring is a stub, routing occurs via Camel | FEAT-002              | High        |
| 2     | `CreditRiskRequestValidator` comments list CR-006 as the last rule but source code also validates CR-007 (postal code) — Javadoc mismatch | FEAT-004 | Low   |
| 3     | Gateway pre-processor sets `requestChannel` only when null — existing channel values are preserved         | FEAT-003              | Low         |
| 4     | `TransactionLogger` opens and closes a new `MongoClient` per transaction — potential performance concern   | FEAT-010              | Medium      |
| 5     | No email address field exists in `CreditRiskReqType` — gap in applicant contact validation                 | FEAT-004              | Medium      |
| 6     | No validation of `annualIncome` or `requestedAmount` for negative values or range limits                   | FEAT-004              | Medium      |
| 7     | `StandardScoringStrategy` uses the label "OVEREXTENDED" in code but the issue template specifies "UNAFFORDABLE" — naming discrepancy | FEAT-006 | Low |
| 8     | `WEIGHT_UTILIZATION` field in `StandardScoringStrategy` is labelled as Employment weight (10) — Javadoc comment says Employment=10%, Utilization=20% but constant name is `WEIGHT_UTILIZATION` mapped to 20 and `WEIGHT_EMPLOYMENT` to 10; actual code is correct | FEAT-006 | Low |

---

## 8. Gap-Exposing Scenarios

_This section consolidates all scenarios that expose gaps — scenarios that document expected behavior NOT present in the current source code. These scenarios will FAIL against the source system but should PASS on the target system after migration._

### 8.1 Missing Validation Scenarios

```gherkin
@gap @validation @target-behavior @FEAT-004
Feature: Missing Validation Gaps
  As the nexgen-creditrisk-gateway target system
  I want to validate all applicable input fields comprehensively
  So that malformed requests are rejected before processing

  # GAP-001: No email format validation in current source

  @gap @validation @target-behavior @priority-medium
  Scenario: Email address field is present but not validated in current system
    Given a CreditRiskRequest with all valid fields
    And the "emailAddress" field is set to "not-an-email"
    When the validator processes the request
    Then the current system does NOT return a validation error for emailAddress
    And the target system SHOULD return a 400 error with "Invalid email address format"

  # GAP-002: No amount range validation

  @gap @validation @target-behavior @priority-medium
  Scenario Outline: Requested amount is not range-validated in current system
    Given a CreditRiskRequest with all valid fields
    And the "requestedAmount" is set to <invalid_amount>
    When the validator processes the request
    Then the current system does NOT reject the request
    And the target system SHOULD return a 400 error for invalid amount

    Examples:
      | invalid_amount |
      | -1000.00       |
      | 0.00           |
      | -0.01          |

  # GAP-003: No negative annual income validation

  @gap @validation @target-behavior @priority-medium
  Scenario: Negative annual income is not validated in current system
    Given a CreditRiskRequest with all valid fields
    And the "annualIncome" is set to -50000.00
    When the validator processes the request
    Then the current system does NOT return a validation error for annualIncome
    And the target system SHOULD return a 400 error with "Annual income must be a positive value"

  # GAP-004: Province validation is optional (skipped when blank)

  @gap @validation @current-behavior @priority-low
  Scenario: Blank province bypasses province validation
    Given a CreditRiskRequest with all valid fields
    And the "province" field is blank
    When the validator processes the request
    Then no province validation error is thrown
    And the current behavior is that province is treated as optional

  # GAP-005: Postal code validation is optional (skipped when blank)

  @gap @validation @current-behavior @priority-low
  Scenario: Blank postal code bypasses postal code validation
    Given a CreditRiskRequest with all valid fields
    And the "postalCode" field is blank
    When the validator processes the request
    Then no postal code validation error is thrown
    And the current behavior is that postal code is treated as optional
```

### 8.2 Hardcoded Value Scenarios

```gherkin
@gap @hardcoded @FEAT-003
Feature: Hardcoded Value Gaps
  These scenarios document fields that use hardcoded/static values
  instead of dynamic runtime values in the current source system.

  # GAP-006: GatewayRequestPreProcessor hardcodes sourceSystem to "GATEWAY"

  @current-behavior @hardcoded @priority-medium
  Scenario: Gateway pre-processor always sets sourceSystem to hardcoded "GATEWAY"
    Given a gateway SOAP request where the RequestHeader is null
    When GatewayRequestPreProcessor processes the exchange
    Then the RequestHeader.sourceSystem is always "GATEWAY"
    And this value is hardcoded — it cannot be overridden by the caller

  @target-behavior @priority-medium
  Scenario: Target system should derive sourceSystem from authenticated caller identity
    Given a gateway SOAP request from a known external system "POLICY_ADMIN_SYSTEM"
    When the target gateway pre-processor processes the exchange
    Then the RequestHeader.sourceSystem is set to "POLICY_ADMIN_SYSTEM"
    And the value is derived from the authenticated caller, not hardcoded

  # GAP-007: GatewayRequestPreProcessor hardcodes requestChannel to "API"

  @current-behavior @hardcoded @priority-low
  Scenario: Gateway pre-processor always defaults requestChannel to hardcoded "API"
    Given a gateway SOAP request where the requestChannel is null
    When GatewayRequestPreProcessor processes the exchange
    Then the requestChannel is always set to "API"
    And this value is hardcoded — all null-channel gateway requests become "API"

  # GAP-008: ErrorProcessor hardcodes INTERNAL error message

  @current-behavior @hardcoded @priority-low
  Scenario: ErrorProcessor always returns the same generic message for HTTP 500 errors
    Given the exchange contains any non-validation exception
    When ErrorProcessor processes the exchange
    Then the response message is always "Internal service error occurred. Please contact support."
    And this message is hardcoded regardless of the actual exception type

  # GAP-009: Timestamp formatting in ErrorProcessor uses hardcoded format

  @current-behavior @hardcoded @priority-low
  Scenario: ErrorProcessor response timestamp always uses hardcoded "yyyy-MM-dd'T'HH:mm:ss" format
    Given the exchange contains any exception
    When ErrorProcessor processes the exchange
    Then the ResponseHeader.timestamp format is always "yyyy-MM-dd'T'HH:mm:ss"
    And no timezone information is included in the timestamp
```

### 8.3 Dead Code Scenarios

```gherkin
@gap @dead-code @FEAT-002
Feature: Dead Code Verification — SOAP Service Implementation
  These scenarios document dead code paths in the current source system
  that must be identified and addressed before or during migration.

  # GAP-010: CreditRiskSoapSvcImpl.assessCreditRisk always returns null

  @dead-code @current-behavior @priority-high @migration-critical
  Scenario: CreditRiskSoapSvcImpl.assessCreditRisk always returns null
    Given a valid authenticated SOAP assessCreditRisk request
    When the request is dispatched to CreditRiskSoapSvcImpl
    Then CreditRiskSoapSvcImpl.assessCreditRisk returns null
    And no scoring, validation, or bureau call is executed via this code path
    And the actual routing and scoring occurs exclusively through the Camel route

  @dead-code @target-behavior @priority-high @migration-critical
  Scenario: Target SOAP implementation should invoke the scoring pipeline
    Given a valid authenticated SOAP assessCreditRisk request
    When the request is dispatched to the target CreditRiskSoapSvc implementation
    Then the request is processed through the full validation and scoring pipeline
    And a populated CreditRiskResponse is returned (not null)

  # GAP-011: defaultStrategy field in ScoringStrategyProcessor only applies if not MORTGAGE/AUTO_LOAN/CREDIT_CARD/LOC

  @dead-code @current-behavior @priority-low
  Scenario: defaultStrategy=conservative is dead code when productType is a known product
    Given the ScoringStrategyProcessor is configured with defaultStrategy="conservative"
    And the request productType is "MORTGAGE"
    When ScoringStrategyProcessor resolves the strategy
    Then the defaultStrategy configuration has no effect
    And ConservativeScoringStrategy is selected due to productType, not defaultStrategy
    And the defaultStrategy field is effectively dead code for all known product types
```

### 8.4 Transaction Logging Scenarios

```gherkin
@logging @migration-critical @FEAT-010
Feature: Transaction Logging Behavior — Gap Scenarios
  These scenarios validate transaction logging edge cases and gaps
  that are critical for migration acceptance testing.

  # GAP-012: MongoDB unavailable — no resilience mechanism in current source

  @gap @logging @migration-critical @current-behavior @priority-high
  Scenario: MongoDB unavailable — current system swallows the error silently
    Given the MongoDB instance is unreachable
    And a valid credit risk assessment is processed
    When TransactionLogger.logTransaction is invoked
    Then the exception is caught and logged at ERROR level in application logs
    And the transaction document is NOT persisted
    And no alerting or retry mechanism exists in the current system

  @gap @logging @migration-critical @target-behavior @priority-high
  Scenario: MongoDB unavailable — target system should implement retry or dead-letter queue
    Given the MongoDB instance is unreachable
    And a valid credit risk assessment is processed
    When the target transaction logger is invoked
    Then the logging failure should trigger a retry attempt
    Or the logging failure should route to a dead-letter queue
    And an alert should be raised to the operations team

  # GAP-013: No 17th field (APPLICATION) is populated in current TransactionLogger

  @gap @logging @current-behavior @priority-low
  Scenario: LOG_APPLICATION constant exists but is not set by TransactionLogger
    Given a successful credit risk assessment completes
    When TransactionLogger.logTransaction is invoked
    Then the MongoDB document does NOT contain an "application" field
    And the LOG_APPLICATION constant defined in LoggerConstants is never populated
    And the effective logged field count is 16, not 17

  # GAP-014: New MongoClient per transaction is a scalability gap

  @gap @logging @hardcoded @priority-medium
  Scenario: TransactionLogger creates a new MongoClient connection per transaction
    Given a high volume of concurrent credit risk assessments
    When multiple TransactionLogger.logTransaction calls are made concurrently
    Then each invocation creates a new MongoClient instance
    And each invocation closes the MongoClient in the finally block
    And this pattern may cause connection exhaustion under load
    And the target system should use a connection-pooled MongoClient
```

### 8.5 Gap Scenario Summary

| **Scenario Category**      | **Gap ID(s)**              | **Count** | **Tags**                              | **Current System**     | **Target System**  |
|----------------------------|----------------------------|-----------|---------------------------------------|------------------------|--------------------|
| Missing Validation         | GAP-001, GAP-002, GAP-003  | 5         | `@gap @validation @target-behavior`   | ❌ FAIL (no check)     | ✅ PASS            |
| Optional Field Gaps        | GAP-004, GAP-005           | 2         | `@gap @validation @current-behavior`  | ✅ PASS (intentional)  | ⚠️ Review Required |
| Hardcoded Values           | GAP-006, GAP-007, GAP-008, GAP-009 | 5 | `@gap @hardcoded`                    | ❌ FAIL                | ✅ PASS            |
| Dead Code — SOAP Null Impl | GAP-010                    | 2         | `@gap @dead-code @migration-critical` | ✅ PASS (null returned)| ✅ PASS (removed)  |
| Dead Code — defaultStrategy| GAP-011                    | 1         | `@gap @dead-code`                     | ✅ PASS (no effect)    | ✅ PASS (removed)  |
| Transaction Logging Gaps   | GAP-012, GAP-013, GAP-014  | 4         | `@gap @logging @migration-critical`   | ❌ FAIL / ⚠️ Partial  | ✅ PASS            |
| **Total Gap Scenarios**    | GAP-001 → GAP-014          | **19**    | —                                     | —                      | —                  |

---

## 9. Appendices

### Appendix A: Gherkin Best Practices

- Use declarative (what) not imperative (how) language
- Keep scenarios independent — no scenario should depend on another
- Use Background for common Given steps shared across all scenarios in a feature
- Use Scenario Outline + Examples for data-driven testing
- One scenario should test one behavior
- All @gap scenarios document a behavior delta between the current and target system
- @dead-code scenarios should be verified by code inspection prior to migration

### Appendix B: Glossary

| **Term**             | **Definition**                                                                                      |
|----------------------|-----------------------------------------------------------------------------------------------------|
| BDD                  | Behavior-Driven Development — collaborative specification technique using Gherkin                    |
| Bureau Score         | Credit score returned by the external credit bureau SOAP service                                     |
| DTI                  | Debt-to-Income Ratio — total monthly debt payments divided by gross monthly income                   |
| DSR                  | Debt Service Ratio — housing costs as a fraction of gross income                                     |
| SIN                  | Social Insurance Number — 9-digit Canadian government-issued identifier                              |
| Wire Tap             | Apache Camel EIP pattern for async message copying to a secondary endpoint without blocking the main flow |
| WSS4J UsernameToken  | WS-Security standard for SOAP header-based username/password authentication                         |
| Gap Scenario         | A scenario that describes expected target system behavior absent from the current source system      |
| Dead Code            | Source code that exists but is never executed in normal or known processing flows                    |
| Hardcoded Value      | A static literal embedded in source code that should be configurable or dynamically derived          |
| Conservative         | Scoring strategy with stricter thresholds — used for MORTGAGE and AUTO_LOAN                         |
| Aggressive           | Scoring strategy with lenient thresholds — used for CREDIT_CARD and LINE_OF_CREDIT                  |
| Standard             | Default scoring strategy with balanced thresholds — used for all other product types                |

---

> **Document Control:**
> | Version | Date | Author | Changes |
> |---------|------|--------|---------|
> | 0.1 | 31-Mar-2026 | Copilot Reverse Engineering Agent | Initial draft — all 11 features, 19 gap scenarios, full coverage matrix derived from source code |
