# BDD Feature Specifications

---

| **Field**            | **Details**                                        |
|----------------------|----------------------------------------------------|
| **Project Name**     | NexGen Credit Risk Platform Migration              |
| **Application Name** | nexgen-creditrisk-gateway (JBoss Fuse ESB Service) |
| **Version**          | 1.0.0                                              |
| **Date**             | 31-Mar-2026                                        |
| **Prepared By**      | Copilot Reverse Engineering Agent                  |
| **Reviewed By**      | —                                                  |
| **Status**           | Draft                                              |

---

## 1. Overview

This document contains Behavior-Driven Development (BDD) feature specifications written in Gherkin syntax, derived from the reverse engineering analysis of the `nexgen-creditrisk-gateway` JBoss Fuse ESB service. These specifications capture the expected behavior of the system from a user/stakeholder perspective and are directly traceable to the source code components identified during the RE analysis.

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

| **Feature ID** | **Feature Name**                          | **Module/Component**               | **Scenarios** | **Priority** | **Status** |
|----------------|-------------------------------------------|------------------------------------|---------------|--------------|------------|
| FEAT-001       | Credit Risk Assessment via REST           | CreditRiskRestSvc, in_rest route   | 6             | High         | Draft      |
| FEAT-002       | Credit Risk Assessment via SOAP           | CreditRiskSoapSvc, in_soap route   | 4             | High         | Draft      |
| FEAT-003       | Credit Risk Assessment via Gateway        | GatewayRequestPreProcessor         | 5             | High         | Draft      |
| FEAT-004       | Input Validation                          | CreditRiskRequestValidator         | 10            | High         | Draft      |
| FEAT-005       | Scoring Strategy Selection                | ScoringStrategyProcessor           | 6             | High         | Draft      |
| FEAT-006       | Standard Scoring Strategy                 | StandardScoringStrategy            | 7             | High         | Draft      |
| FEAT-007       | Conservative Scoring Strategy             | ConservativeScoringStrategy        | 5             | High         | Draft      |
| FEAT-008       | Aggressive Scoring Strategy               | AggressiveScoringStrategy          | 5             | High         | Draft      |
| FEAT-009       | Bureau Score Categorization               | BureauResponseMapper               | 6             | High         | Draft      |
| FEAT-010       | Transaction Logging                       | TransactionLogger, Wire Tap route  | 5             | Medium       | Draft      |
| FEAT-011       | Error Handling                            | ErrorProcessor                     | 4             | High         | Draft      |

---

## 3. Feature Specifications

### 3.1 FEAT-001: Credit Risk Assessment via REST

**File:** `features/rest/credit-risk-rest-assessment.feature`
**Component(s):** `CreditRiskRestSvc.java`, `in_rest_creditRiskRouter` Camel route
**Business Rules:** CR-001, CR-002, CR-003, CR-004, CR-005, CR-006, CR-007
**Priority:** High

```gherkin
@rest @priority-high @FEAT-001 @regression
Feature: Credit Risk Assessment via REST
  As an API consumer
  I want to submit a credit risk assessment request via REST GET
  So that I receive a scored credit risk response with recommendation

  Background:
    Given the nexgen-creditrisk-gateway service is running
    And the credit bureau service is available
    And the MongoDB transaction store is available

  # --- Happy Path Scenarios ---

  @happy-path @smoke @current-behavior
  Scenario: Valid request with all 11 parameters returns scored response
    Given a valid REST request with the following parameters:
      | applicantId      | APP-001234          |
      | firstName        | John                |
      | lastName         | Smith               |
      | dateOfBirth      | 1985-06-15          |
      | sin              | 123-456-789         |
      | employmentStatus | FULL_TIME           |
      | annualIncome     | 95000.00            |
      | province         | ON                  |
      | postalCode       | M5V 3A8             |
      | productType      | PERSONAL_LOAN       |
      | requestedAmount  | 25000.00            |
    When the GET /assess endpoint is called
    Then the HTTP response status is 200
    And the response Content-Type is "application/xml"
    And the response body contains a riskCategory
    And the response body contains an overallScore
    And the response body contains a recommendation
    And the transaction is logged to MongoDB

  @happy-path @current-behavior
  Scenario: Valid request with only mandatory parameters returns scored response
    Given a valid REST request with mandatory parameters only:
      | applicantId | APP-001235  |
      | firstName   | Jane        |
      | lastName    | Doe         |
      | dateOfBirth | 1990-03-22  |
      | sin         | 987-654-321 |
    When the GET /assess endpoint is called
    Then the HTTP response status is 200
    And the response body contains a riskCategory
    And the response body contains a recommendation

  # --- Error Path Scenarios ---

  @negative @validation @current-behavior
  Scenario: Missing required field returns HTTP 400
    Given a REST request with applicantId "APP-001236"
    And the firstName parameter is omitted
    When the GET /assess endpoint is called
    Then the HTTP response status is 400
    And the response body contains errorCode "CR-002"
    And the response body contains errorMessage "First Name field is blank"

  @negative @error-handling @current-behavior
  Scenario: Bureau service timeout returns HTTP 500
    Given a valid REST request for applicant "APP-001237"
    And the credit bureau service is configured to timeout
    When the GET /assess endpoint is called
    Then the HTTP response status is 500
    And the response body contains errorCode "CR-500"
    And the transaction is logged with status "error"

  @negative @error-handling @current-behavior
  Scenario: Bureau service returns an error response
    Given a valid REST request for applicant "APP-001238"
    And the credit bureau service returns an error with code "BUR-404"
    When the GET /assess endpoint is called
    Then the HTTP response status is 500
    And the response body contains errorCode "CR-500"

  @negative @validation @current-behavior
  Scenario: Invalid date of birth format returns HTTP 400
    Given a REST request with applicantId "APP-001239"
    And the dateOfBirth is set to "15-06-1985"
    And all other mandatory fields are valid
    When the GET /assess endpoint is called
    Then the HTTP response status is 400
    And the response body contains errorCode "CR-004"
    And the response body contains errorMessage "Date of Birth must be in YYYY-MM-DD format"
```

#### Scenario Traceability

| **Scenario**                                    | **Business Rule(s)** | **Test Case(s)** | **Component(s)**           |
|-------------------------------------------------|----------------------|------------------|----------------------------|
| Valid request with all 11 parameters            | CR-001–CR-007        | TC-001           | CreditRiskRestSvc          |
| Valid request with mandatory parameters only    | CR-001–CR-005        | TC-002           | CreditRiskRestSvc          |
| Missing required field returns HTTP 400         | CR-002               | TC-003           | CreditRiskRequestValidator |
| Bureau service timeout returns HTTP 500         | —                    | TC-004           | ErrorProcessor             |
| Bureau service returns an error response        | —                    | TC-005           | BureauResponseMapper       |
| Invalid date of birth format returns HTTP 400   | CR-004               | TC-006           | CreditRiskRequestValidator |

---

### 3.2 FEAT-002: Credit Risk Assessment via SOAP

**File:** `features/soap/credit-risk-soap-assessment.feature`
**Component(s):** `CreditRiskSoapSvc.java`, `CreditRiskSoapSvcImpl.java`, `in_soap_creditRiskRouter` Camel route
**Business Rules:** CR-001–CR-007
**Priority:** High

```gherkin
@soap @priority-high @FEAT-002 @regression
Feature: Credit Risk Assessment via SOAP
  As an enterprise integration consumer
  I want to submit a credit risk assessment request via SOAP web service
  So that I receive a scored credit risk response using WS-I compliant messaging

  Background:
    Given the nexgen-creditrisk-gateway SOAP endpoint is running
    And the SOAP endpoint is secured with WSS4J UsernameToken authentication
    And the credit bureau service is available

  # --- Happy Path Scenarios ---

  @happy-path @smoke @security @current-behavior
  Scenario: Valid SOAP request with correct credentials returns scored response
    Given a valid SOAP CreditRiskRequest payload with all required fields
    And the WS-Security UsernameToken header contains valid credentials
    When the assessCreditRisk SOAP operation is invoked
    Then the SOAP response contains a CreditRiskResponse element
    And the riskCategory element is populated
    And the recommendation element is populated
    And the HTTP status is 200

  @negative @security @current-behavior
  Scenario: SOAP request without WS-Security header is rejected
    Given a valid SOAP CreditRiskRequest payload
    And no WS-Security UsernameToken header is included
    When the assessCreditRisk SOAP operation is invoked
    Then the SOAP response contains a SOAP Fault
    And the fault code indicates an authentication failure

  @negative @security @current-behavior
  Scenario: SOAP request with invalid credentials is rejected
    Given a valid SOAP CreditRiskRequest payload
    And the WS-Security UsernameToken header contains invalid credentials
    When the assessCreditRisk SOAP operation is invoked
    Then the SOAP response contains a SOAP Fault
    And the fault code indicates an authentication failure

  @dead-code @gap @current-behavior
  Scenario: SOAP implementation delegates to Camel route (not direct implementation)
    Given a valid SOAP CreditRiskRequest payload with valid credentials
    When the assessCreditRisk SOAP operation is invoked
    Then the CreditRiskSoapSvcImpl does not process the request directly
    And the request is delegated to the in_soap_creditRiskRouter Camel route
    And the CreditRiskSoapSvcImpl.assessCreditRisk method returns null directly
```

#### Scenario Traceability

| **Scenario**                                                  | **Business Rule(s)** | **Test Case(s)** | **Component(s)**     |
|---------------------------------------------------------------|----------------------|------------------|----------------------|
| Valid SOAP request with correct credentials                   | CR-001–CR-007        | TC-010           | CreditRiskSoapSvc    |
| SOAP request without WS-Security header is rejected           | —                    | TC-011           | WSS4J Filter         |
| SOAP request with invalid credentials is rejected             | —                    | TC-012           | WSS4J Filter         |
| SOAP implementation delegates to Camel route                  | —                    | TC-013           | CreditRiskSoapSvcImpl|

---

### 3.3 FEAT-003: Credit Risk Assessment via Gateway

**File:** `features/gateway/credit-risk-gateway-assessment.feature`
**Component(s):** `GatewayRequestPreProcessor.java`, `in_gw_creditRiskRouter` Camel route
**Business Rules:** CR-001–CR-007
**Priority:** High

```gherkin
@gateway @priority-high @FEAT-003 @regression
Feature: Credit Risk Assessment via Gateway
  As an enterprise integration system (e.g., policy administration platform)
  I want to submit credit risk assessment requests via the Gateway SOAP endpoint
  So that requests are pre-processed and enriched before scoring

  Background:
    Given the nexgen-creditrisk-gateway Gateway endpoint is running
    And the Gateway endpoint is secured with WSS4J UsernameToken authentication
    And the credit bureau service is available

  # --- Happy Path Scenarios ---

  @happy-path @smoke @current-behavior
  Scenario: Gateway request without request header gets header auto-populated
    Given a valid gateway CreditRiskRequest with no RequestHeader
    And the WS-Security UsernameToken header contains valid credentials
    When the gateway endpoint receives the request
    Then the GatewayRequestPreProcessor sets sourceSystem to "GATEWAY"
    And a new UUID transactionId is generated
    And a timestamp is set on the request header
    And the request proceeds to validation and scoring

  @happy-path @current-behavior
  Scenario: Gateway request without requestChannel gets channel set to API
    Given a valid gateway CreditRiskRequest with requestChannel not set
    When the GatewayRequestPreProcessor processes the request
    Then the requestChannel is set to "API"

  @happy-path @current-behavior
  Scenario: Gateway request with existing request header preserves the header
    Given a valid gateway CreditRiskRequest with RequestHeader already set
    And the existing header has transactionId "TXN-EXISTING-001"
    When the GatewayRequestPreProcessor processes the request
    Then the existing transactionId "TXN-EXISTING-001" is preserved
    And the sourceSystem remains unchanged

  @happy-path @smoke @current-behavior
  Scenario: Full gateway happy path — valid request returns scored response
    Given a valid gateway CreditRiskRequest for applicant "APP-GW-001"
    And the WS-Security UsernameToken header contains valid credentials
    When the gateway endpoint processes the request end-to-end
    Then the response contains a populated riskCategory
    And the response contains a recommendation
    And the transaction is logged with requestChannel "API" and sourceSystem "GATEWAY"

  @negative @security @current-behavior
  Scenario: Gateway request without WS-Security header is rejected
    Given a valid gateway CreditRiskRequest payload
    And no WS-Security UsernameToken header is included
    When the gateway endpoint receives the request
    Then the request is rejected with an authentication fault
```

#### Scenario Traceability

| **Scenario**                                               | **Business Rule(s)** | **Test Case(s)** | **Component(s)**              |
|------------------------------------------------------------|----------------------|------------------|-------------------------------|
| Gateway request without header gets header auto-populated  | —                    | TC-020           | GatewayRequestPreProcessor    |
| Gateway request without channel gets channel set to API    | —                    | TC-021           | GatewayRequestPreProcessor    |
| Gateway request with existing header preserves header      | —                    | TC-022           | GatewayRequestPreProcessor    |
| Full gateway happy path                                    | CR-001–CR-007        | TC-023           | GatewayRequestPreProcessor    |
| Gateway request without WS-Security header is rejected     | —                    | TC-024           | WSS4J Filter                  |

---

### 3.4 FEAT-004: Input Validation

**File:** `features/validation/credit-risk-input-validation.feature`
**Component(s):** `CreditRiskRequestValidator.java`
**Business Rules:** CR-001, CR-002, CR-003, CR-004, CR-005, CR-006, CR-007
**Priority:** High

```gherkin
@validation @priority-high @FEAT-004 @regression
Feature: Credit Risk Request Input Validation
  As the credit risk gateway
  I want to validate all incoming credit risk assessment requests
  So that only well-formed, complete requests are processed by the scoring engine

  Background:
    Given the CreditRiskRequestValidator is active in the processing pipeline

  # --- Required Field Validation (CR-001 through CR-005) ---

  @negative @validation @current-behavior @BR-CR-001
  Scenario: Missing applicantId returns validation error CR-001
    Given a credit risk request with firstName "John", lastName "Smith", dateOfBirth "1985-06-15", sin "123-456-789"
    And the applicantId field is blank
    When the validator processes the request
    Then a ValidationException is thrown with errorCode "CR-001"
    And the error message is "Applicant ID field is blank"

  @negative @validation @current-behavior @BR-CR-002
  Scenario: Missing firstName returns validation error CR-002
    Given a credit risk request with applicantId "APP-001", lastName "Smith", dateOfBirth "1985-06-15", sin "123-456-789"
    And the firstName field is blank
    When the validator processes the request
    Then a ValidationException is thrown with errorCode "CR-002"
    And the error message is "First Name field is blank"

  @negative @validation @current-behavior @BR-CR-003
  Scenario: Missing lastName returns validation error CR-003
    Given a credit risk request with applicantId "APP-001", firstName "John", dateOfBirth "1985-06-15", sin "123-456-789"
    And the lastName field is blank
    When the validator processes the request
    Then a ValidationException is thrown with errorCode "CR-003"
    And the error message is "Last Name field is blank"

  @negative @validation @boundary @current-behavior @BR-CR-004
  Scenario Outline: Date of birth format validation (CR-004)
    Given a credit risk request with all required fields
    And the dateOfBirth is set to "<dob_value>"
    When the validator processes the request
    Then the validation result is "<result>"
    And the error code is "<error_code>"

    Examples:
      | dob_value    | result  | error_code |
      | 1985-06-15   | VALID   |            |
      | 2000-01-01   | VALID   |            |
      |              | INVALID | CR-004     |
      | 15-06-1985   | INVALID | CR-004     |
      | 1985/06/15   | INVALID | CR-004     |
      | 1985-13-01   | VALID   |            |
      | not-a-date   | INVALID | CR-004     |
      # Note: 1985-13-01 is marked VALID because the current regex (^\d{4}-\d{2}-\d{2}$) only validates
      # date format, not calendar semantics. Month=13 satisfies the regex pattern and passes validation.
      # GAP-004 (Section 8.1) exposes this gap and defines target behavior where semantic validation is required.

  @negative @validation @boundary @current-behavior @BR-CR-005
  Scenario Outline: Social Insurance Number format validation (CR-005)
    Given a credit risk request with all required fields
    And the socialInsuranceNumber is set to "<sin_value>"
    When the validator processes the request
    Then the validation result is "<result>"
    And the error code is "<error_code>"

    Examples:
      | sin_value   | result  | error_code |
      | 123-456-789 | VALID   |            |
      | 123456789   | VALID   |            |
      |             | INVALID | CR-005     |
      | 12345678    | INVALID | CR-005     |
      | 1234-567-89 | INVALID | CR-005     |
      | ABC-DEF-GHI | INVALID | CR-005     |
      | 123 456 789 | INVALID | CR-005     |

  @negative @validation @boundary @current-behavior @BR-CR-006
  Scenario Outline: Province code validation (CR-006)
    Given a credit risk request with all mandatory fields valid
    And the province is set to "<province_value>"
    When the validator processes the request
    Then the validation result is "<result>"
    And the error code is "<error_code>"

    Examples:
      | province_value | result  | error_code |
      | ON             | VALID   |            |
      | QC             | VALID   |            |
      | BC             | VALID   |            |
      | AB             | VALID   |            |
      | MB             | VALID   |            |
      | SK             | VALID   |            |
      | NS             | VALID   |            |
      | NB             | VALID   |            |
      | NL             | VALID   |            |
      | PE             | VALID   |            |
      | NT             | VALID   |            |
      | YT             | VALID   |            |
      | NU             | VALID   |            |
      |                | VALID   |            |
      | XX             | INVALID | CR-006     |
      | USA            | INVALID | CR-006     |

  @negative @validation @boundary @current-behavior @BR-CR-007
  Scenario Outline: Postal code format validation (CR-007)
    Given a credit risk request with all mandatory fields valid
    And the postalCode is set to "<postal_value>"
    When the validator processes the request
    Then the validation result is "<result>"
    And the error code is "<error_code>"

    Examples:
      | postal_value | result  | error_code |
      | M5V 3A8      | VALID   |            |
      | M5V3A8       | VALID   |            |
      |              | VALID   |            |
      | 12345        | INVALID | CR-007     |
      | M5V-3A8      | INVALID | CR-007     |
      | M5V 3A       | INVALID | CR-007     |

  @happy-path @current-behavior
  Scenario: Null request body returns validation error CR-000
    Given the request body is null
    When the validator processes the request
    Then a ValidationException is thrown with errorCode "CR-000"
    And the error message is "Request body is null or empty"
```

#### Scenario Traceability

| **Scenario**                                 | **Business Rule(s)** | **Test Case(s)** | **Component(s)**           |
|----------------------------------------------|----------------------|------------------|----------------------------|
| Missing applicantId → CR-001                 | CR-001               | TC-030           | CreditRiskRequestValidator |
| Missing firstName → CR-002                   | CR-002               | TC-031           | CreditRiskRequestValidator |
| Missing lastName → CR-003                    | CR-003               | TC-032           | CreditRiskRequestValidator |
| DOB format validation outline                | CR-004               | TC-033           | CreditRiskRequestValidator |
| SIN format validation outline                | CR-005               | TC-034           | CreditRiskRequestValidator |
| Province code validation outline             | CR-006               | TC-035           | CreditRiskRequestValidator |
| Postal code format validation outline        | CR-007               | TC-036           | CreditRiskRequestValidator |
| Null request body → CR-000                   | —                    | TC-037           | CreditRiskRequestValidator |

---

### 3.5 FEAT-005: Scoring Strategy Selection

**File:** `features/scoring/scoring-strategy-selection.feature`
**Component(s):** `ScoringStrategyProcessor.java`
**Business Rules:** SS-001, SS-002, SS-003
**Priority:** High

```gherkin
@scoring @priority-high @FEAT-005 @regression
Feature: Scoring Strategy Selection
  As the credit risk scoring engine
  I want to select the appropriate scoring strategy based on product type
  So that risk is assessed using the most suitable thresholds for the product category

  Background:
    Given the ScoringStrategyProcessor is active in the processing pipeline
    And a validated credit risk request is available in the exchange

  # --- Strategy Selection Scenarios ---

  @happy-path @data-driven @current-behavior
  Scenario Outline: Product type maps to correct scoring strategy
    Given a validated request with productType "<product_type>"
    When the ScoringStrategyProcessor processes the request
    Then the selected scoring strategy is "<expected_strategy>"

    Examples:
      | product_type      | expected_strategy |
      | MORTGAGE          | CONSERVATIVE      |
      | AUTO_LOAN         | CONSERVATIVE      |
      | CREDIT_CARD       | AGGRESSIVE        |
      | LINE_OF_CREDIT    | AGGRESSIVE        |
      | PERSONAL_LOAN     | STANDARD          |
      | BUSINESS_LOAN     | STANDARD          |

  @happy-path @boundary @current-behavior
  Scenario: Unknown product type falls back to Standard strategy
    Given a validated request with productType "UNKNOWN_PRODUCT_XYZ"
    When the ScoringStrategyProcessor processes the request
    Then the selected scoring strategy is "STANDARD"

  @happy-path @current-behavior
  Scenario: Product type matching is case-insensitive
    Given a validated request with productType "mortgage"
    When the ScoringStrategyProcessor processes the request
    Then the selected scoring strategy is "CONSERVATIVE"

  @happy-path @current-behavior
  Scenario: Default strategy override selects Conservative when configured
    Given the ScoringStrategyProcessor defaultStrategy is configured to "conservative"
    And a validated request with productType "PERSONAL_LOAN"
    When the ScoringStrategyProcessor processes the request
    Then the selected scoring strategy is "CONSERVATIVE"

  @happy-path @current-behavior
  Scenario: SCORING_STRATEGY exchange property is set after processing
    Given a validated request with productType "CREDIT_CARD"
    When the ScoringStrategyProcessor processes the request
    Then the exchange property "SCORING_STRATEGY" is set to an AggressiveScoringStrategy instance
    And the strategy name is "AGGRESSIVE"
```

#### Scenario Traceability

| **Scenario**                                      | **Business Rule(s)** | **Test Case(s)** | **Component(s)**          |
|---------------------------------------------------|----------------------|------------------|---------------------------|
| Product type maps to correct scoring strategy     | SS-001, SS-002       | TC-040           | ScoringStrategyProcessor  |
| Unknown product type falls back to Standard       | SS-003               | TC-041           | ScoringStrategyProcessor  |
| Product type matching is case-insensitive         | SS-001               | TC-042           | ScoringStrategyProcessor  |
| Default strategy override                         | SS-003               | TC-043           | ScoringStrategyProcessor  |
| SCORING_STRATEGY exchange property is set         | —                    | TC-044           | ScoringStrategyProcessor  |

---

### 3.6 FEAT-006: Standard Scoring Strategy

**File:** `features/scoring/standard-scoring-strategy.feature`
**Component(s):** `StandardScoringStrategy.java`
**Business Rules:** SCR-STD-001 through SCR-STD-006
**Priority:** High

```gherkin
@scoring @standard @priority-high @FEAT-006 @regression
Feature: Standard Scoring Strategy
  As the credit risk scoring engine
  I want to categorize risk using the standard balanced scoring thresholds
  So that personal loans and other general credit products are assessed fairly

  Background:
    Given the StandardScoringStrategy is being used for scoring

  # --- Risk Categorization Scenarios ---

  @happy-path @data-driven @boundary @current-behavior
  Scenario Outline: Bureau score, DTI, and utilization determine risk category
    Given a bureau score of <bureau_score>
    And a debt-to-income ratio of <dti>
    And a utilization rate of <util>
    When the standard strategy categorizes the risk
    Then the risk category is "<risk_category>"

    Examples:
      | bureau_score | dti  | util | risk_category |
      | 750          | 0.25 | 0.25 | EXCELLENT     |
      | 780          | 0.10 | 0.10 | EXCELLENT     |
      | 750          | 0.35 | 0.25 | GOOD          |
      | 750          | 0.25 | 0.35 | GOOD          |
      | 680          | 0.50 | 0.50 | GOOD          |
      | 679          | 0.20 | 0.20 | FAIR          |
      | 620          | 0.40 | 0.40 | FAIR          |
      | 619          | 0.20 | 0.20 | POOR          |
      | 560          | 0.50 | 0.50 | POOR          |
      | 559          | 0.20 | 0.20 | VERY_POOR     |
      | 300          | 0.80 | 0.80 | VERY_POOR     |

  # --- Affordability Rating Scenarios ---

  @happy-path @data-driven @boundary @current-behavior
  Scenario Outline: Debt service ratio maps to affordability rating
    Given a debt service ratio of <dsr>
    When the standard strategy determines the affordability rating
    Then the affordability rating is "<affordability>"

    Examples:
      | dsr  | affordability |
      | 0.10 | COMFORTABLE   |
      | 0.27 | COMFORTABLE   |
      | 0.28 | MANAGEABLE    |
      | 0.35 | MANAGEABLE    |
      | 0.36 | STRETCHED     |
      | 0.43 | STRETCHED     |
      | 0.44 | OVEREXTENDED  |
      | 0.80 | OVEREXTENDED  |

  # --- Recommendation Scenarios ---

  @happy-path @data-driven @current-behavior
  Scenario Outline: Risk category and loan-to-income ratio determine recommendation
    Given a risk category of "<risk_category>"
    And a requested amount of <amount>
    And an annual income of <income>
    When the standard strategy determines the recommendation
    Then the recommendation is "<recommendation>"

    Examples:
      | risk_category | amount    | income   | recommendation           |
      | EXCELLENT     | 25000.00  | 95000.00 | APPROVE                  |
      | GOOD          | 50000.00  | 95000.00 | APPROVE                  |
      | GOOD          | 500000.00 | 95000.00 | APPROVE_WITH_CONDITIONS  |
      | FAIR          | 10000.00  | 95000.00 | APPROVE_WITH_CONDITIONS  |
      | FAIR          | 500000.00 | 95000.00 | REFER_TO_UNDERWRITER     |
      | POOR          | 10000.00  | 95000.00 | REFER_TO_UNDERWRITER     |
      | VERY_POOR     | 10000.00  | 95000.00 | DECLINE                  |

  # --- Score Weight Scenarios ---

  @happy-path @current-behavior
  Scenario: Score weights are Bureau=40%, DTI=30%, Utilization=20%, Employment=10%
    Given inputs: bureauScore=700, dti=0.30, utilization=0.30, employmentStatus="FULL_TIME"
    When the standard strategy calculates the overall score
    Then the bureau component weight is 40%
    And the DTI component weight is 30%
    And the utilization component weight is 20%
    And the employment component weight is 10%
    And the overall score is between 0 and 100
```

#### Scenario Traceability

| **Scenario**                                         | **Business Rule(s)** | **Test Case(s)** | **Component(s)**         |
|------------------------------------------------------|----------------------|------------------|--------------------------|
| Bureau score/DTI/utilization → risk category         | SCR-STD-001          | TC-050           | StandardScoringStrategy  |
| Debt service ratio → affordability rating            | SCR-STD-002          | TC-051           | StandardScoringStrategy  |
| Risk category/loan-to-income → recommendation        | SCR-STD-003          | TC-052           | StandardScoringStrategy  |
| Score weights 40/30/20/10                            | SCR-STD-004          | TC-053           | StandardScoringStrategy  |

---

### 3.7 FEAT-007: Conservative Scoring Strategy

**File:** `features/scoring/conservative-scoring-strategy.feature`
**Component(s):** `ConservativeScoringStrategy.java`
**Business Rules:** SCR-CON-001 through SCR-CON-004
**Priority:** High

```gherkin
@scoring @conservative @priority-high @FEAT-007 @regression
Feature: Conservative Scoring Strategy
  As the credit risk scoring engine
  I want to categorize risk using stricter conservative thresholds
  So that mortgage and auto loan applicants are held to higher underwriting standards

  Background:
    Given the ConservativeScoringStrategy is being used for scoring
    And this strategy is applied for MORTGAGE and AUTO_LOAN product types

  @happy-path @data-driven @boundary @current-behavior
  Scenario Outline: Conservative thresholds categorize risk more strictly
    Given a bureau score of <bureau_score>
    And a debt-to-income ratio of <dti>
    And a utilization rate of <util>
    When the conservative strategy categorizes the risk
    Then the risk category is "<risk_category>"

    Examples:
      | bureau_score | dti  | util | risk_category |
      | 780          | 0.20 | 0.20 | EXCELLENT     |
      | 780          | 0.30 | 0.20 | GOOD          |
      | 720          | 0.30 | 0.30 | GOOD          |
      | 720          | 0.40 | 0.30 | FAIR          |
      | 660          | 0.40 | 0.40 | FAIR          |
      | 659          | 0.30 | 0.30 | POOR          |
      | 600          | 0.50 | 0.50 | POOR          |
      | 599          | 0.20 | 0.20 | VERY_POOR     |

  @happy-path @data-driven @boundary @current-behavior
  Scenario Outline: Conservative affordability thresholds are stricter than standard
    Given a debt service ratio of <dsr>
    When the conservative strategy determines the affordability rating
    Then the affordability rating is "<affordability>"

    Examples:
      | dsr  | affordability |
      | 0.10 | COMFORTABLE   |
      | 0.24 | COMFORTABLE   |
      | 0.25 | MANAGEABLE    |
      | 0.31 | MANAGEABLE    |
      | 0.32 | STRETCHED     |
      | 0.39 | STRETCHED     |
      | 0.40 | OVEREXTENDED  |

  @happy-path @data-driven @current-behavior
  Scenario Outline: Conservative recommendations are stricter — FAIR and POOR lead to DECLINE
    Given a risk category of "<risk_category>"
    And a requested amount of <amount>
    And an annual income of <income>
    When the conservative strategy determines the recommendation
    Then the recommendation is "<recommendation>"

    Examples:
      | risk_category | amount    | income    | recommendation          |
      | EXCELLENT     | 300000.00 | 95000.00  | APPROVE                 |
      | EXCELLENT     | 500000.00 | 95000.00  | APPROVE_WITH_CONDITIONS |
      | GOOD          | 200000.00 | 95000.00  | APPROVE_WITH_CONDITIONS |
      | GOOD          | 500000.00 | 95000.00  | REFER_TO_UNDERWRITER    |
      | FAIR          | 100000.00 | 95000.00  | REFER_TO_UNDERWRITER    |
      | POOR          | 100000.00 | 95000.00  | DECLINE                 |
      | VERY_POOR     | 100000.00 | 95000.00  | DECLINE                 |

  @happy-path @current-behavior
  Scenario: Conservative strategy uses Bureau=35%, DTI=35%, Utilization=15%, Employment=15% weights
    Given inputs: bureauScore=750, dti=0.25, utilization=0.20, employmentStatus="FULL_TIME"
    When the conservative strategy calculates the overall score
    Then the bureau component weight is 35%
    And the DTI component weight is 35%
    And the utilization component weight is 15%
    And the employment component weight is 15%
    And the overall score is between 0 and 100
```

#### Scenario Traceability

| **Scenario**                                           | **Business Rule(s)** | **Test Case(s)** | **Component(s)**              |
|--------------------------------------------------------|----------------------|------------------|-------------------------------|
| Conservative risk categorization thresholds            | SCR-CON-001          | TC-060           | ConservativeScoringStrategy   |
| Conservative affordability thresholds                  | SCR-CON-002          | TC-061           | ConservativeScoringStrategy   |
| Conservative recommendation rules                      | SCR-CON-003          | TC-062           | ConservativeScoringStrategy   |
| Conservative score weights 35/35/15/15                 | SCR-CON-004          | TC-063           | ConservativeScoringStrategy   |

---

### 3.8 FEAT-008: Aggressive Scoring Strategy

**File:** `features/scoring/aggressive-scoring-strategy.feature`
**Component(s):** `AggressiveScoringStrategy.java`
**Business Rules:** SCR-AGG-001 through SCR-AGG-004
**Priority:** High

```gherkin
@scoring @aggressive @priority-high @FEAT-008 @regression
Feature: Aggressive Scoring Strategy
  As the credit risk scoring engine
  I want to categorize risk using lenient aggressive thresholds
  So that credit card and line of credit applicants are assessed with appropriate flexibility

  Background:
    Given the AggressiveScoringStrategy is being used for scoring
    And this strategy is applied for CREDIT_CARD and LINE_OF_CREDIT product types

  @happy-path @data-driven @boundary @current-behavior
  Scenario Outline: Aggressive lenient thresholds categorize risk more generously
    Given a bureau score of <bureau_score>
    And a debt-to-income ratio of <dti>
    And a utilization rate of <util>
    When the aggressive strategy categorizes the risk
    Then the risk category is "<risk_category>"

    Examples:
      | bureau_score | dti  | util | risk_category |
      | 720          | 0.35 | 0.50 | EXCELLENT     |
      | 720          | 0.45 | 0.50 | GOOD          |
      | 650          | 0.50 | 0.60 | GOOD          |
      | 649          | 0.50 | 0.60 | FAIR          |
      | 580          | 0.60 | 0.70 | FAIR          |
      | 579          | 0.60 | 0.70 | POOR          |
      | 520          | 0.70 | 0.80 | POOR          |
      | 519          | 0.70 | 0.80 | VERY_POOR     |

  @happy-path @data-driven @boundary @current-behavior
  Scenario Outline: Aggressive affordability thresholds are more lenient than standard
    Given a debt service ratio of <dsr>
    When the aggressive strategy determines the affordability rating
    Then the affordability rating is "<affordability>"

    Examples:
      | dsr  | affordability |
      | 0.10 | COMFORTABLE   |
      | 0.34 | COMFORTABLE   |
      | 0.35 | MANAGEABLE    |
      | 0.44 | MANAGEABLE    |
      | 0.45 | STRETCHED     |
      | 0.54 | STRETCHED     |
      | 0.55 | OVEREXTENDED  |

  @happy-path @data-driven @current-behavior
  Scenario Outline: Aggressive strategy approves FAIR applicants with conditions
    Given a risk category of "<risk_category>"
    When the aggressive strategy determines the recommendation
    Then the recommendation is "<recommendation>"

    Examples:
      | risk_category | recommendation          |
      | EXCELLENT     | APPROVE                 |
      | GOOD          | APPROVE                 |
      | FAIR          | APPROVE_WITH_CONDITIONS |
      | POOR          | REFER_TO_UNDERWRITER    |
      | VERY_POOR     | DECLINE                 |

  @happy-path @current-behavior
  Scenario: Aggressive strategy uses Bureau=50%, DTI=25%, Utilization=15%, Employment=10% weights
    Given inputs: bureauScore=700, dti=0.35, utilization=0.40, employmentStatus="FULL_TIME"
    When the aggressive strategy calculates the overall score
    Then the bureau component weight is 50%
    And the DTI component weight is 25%
    And the utilization component weight is 15%
    And the employment component weight is 10%
    And the overall score is between 0 and 100
```

#### Scenario Traceability

| **Scenario**                                         | **Business Rule(s)** | **Test Case(s)** | **Component(s)**           |
|------------------------------------------------------|----------------------|------------------|----------------------------|
| Aggressive risk categorization thresholds            | SCR-AGG-001          | TC-070           | AggressiveScoringStrategy  |
| Aggressive affordability thresholds                  | SCR-AGG-002          | TC-071           | AggressiveScoringStrategy  |
| Aggressive recommendation rules                      | SCR-AGG-003          | TC-072           | AggressiveScoringStrategy  |
| Aggressive score weights 50/25/15/10                 | SCR-AGG-004          | TC-073           | AggressiveScoringStrategy  |

---

### 3.9 FEAT-009: Bureau Score Categorization

**File:** `features/bureau/bureau-score-categorization.feature`
**Component(s):** `BureauResponseMapper.java`
**Business Rules:** BR-MAP-001 through BR-MAP-005
**Priority:** High

```gherkin
@bureau @priority-high @FEAT-009 @regression
Feature: Bureau Score Categorization
  As the credit risk gateway
  I want to map numeric bureau scores to named score range categories
  So that downstream processors can use standardized score labels

  Background:
    Given the BureauResponseMapper is active in the processing pipeline

  # --- Score Range Mapping ---

  @happy-path @data-driven @boundary @current-behavior
  Scenario Outline: Bureau score maps to correct score range category
    Given a bureau response with creditScore <score>
    When the BureauResponseMapper processes the response
    Then the bureauScoreRange is set to "<expected_range>"

    Examples:
      | score | expected_range |
      | 850   | EXCEPTIONAL    |
      | 800   | EXCEPTIONAL    |
      | 799   | VERY_GOOD      |
      | 740   | VERY_GOOD      |
      | 739   | GOOD           |
      | 670   | GOOD           |
      | 669   | FAIR           |
      | 580   | FAIR           |
      | 579   | POOR           |
      | 300   | POOR           |

  # --- Error Handling ---

  @negative @error-handling @current-behavior
  Scenario: Null bureau response sets BUREAU_ERROR property to true
    Given the bureau response is null
    When the BureauResponseMapper processes the response
    Then the exchange property "BUREAU_ERROR" is set to true
    And the exchange property "BUREAU_ERROR_CODE" is set to "CR-301"

  @negative @error-handling @current-behavior
  Scenario: Bureau response with error code sets BUREAU_ERROR to true
    Given a bureau response with errorCode "BUR-404" and errorMessage "Applicant not found"
    When the BureauResponseMapper processes the response
    Then the exchange property "BUREAU_ERROR" is set to true
    And the exchange property "BUREAU_ERROR_CODE" is set to "CR-302"
    And the exchange property "BUREAU_ERROR_MSG" is set to "Applicant not found"

  @happy-path @current-behavior
  Scenario: Valid bureau response populates all credit score detail fields
    Given a bureau response with:
      | creditScore          | 720       |
      | delinquencyCount     | 0         |
      | inquiryCount         | 2         |
      | openTradelineCount   | 5         |
      | totalCreditLimit     | 50000.00  |
      | totalBalance         | 15000.00  |
    When the BureauResponseMapper processes the response
    Then the CREDIT_SCORE_DETAIL exchange property is populated
    And the bureauScore is 720
    And the bureauScoreRange is "VERY_GOOD"
    And the utilizationRate is 0.30
    And BUREAU_ERROR is set to false

  @boundary @current-behavior
  Scenario: Zero total credit limit results in zero utilization rate
    Given a bureau response with totalCreditLimit 0 and totalBalance 5000
    When the BureauResponseMapper processes the response
    Then the utilizationRate in CREDIT_SCORE_DETAIL is 0.0

  @boundary @current-behavior
  Scenario: Null credit score returns UNKNOWN score range
    Given a bureau response with a null creditScore
    When the BureauResponseMapper processes the response
    Then the bureauScoreRange is "UNKNOWN"
```

#### Scenario Traceability

| **Scenario**                                     | **Business Rule(s)** | **Test Case(s)** | **Component(s)**      |
|--------------------------------------------------|----------------------|------------------|-----------------------|
| Bureau score → score range category              | BR-MAP-001           | TC-080           | BureauResponseMapper  |
| Null bureau response sets BUREAU_ERROR           | BR-MAP-002           | TC-081           | BureauResponseMapper  |
| Bureau error response sets BUREAU_ERROR          | BR-MAP-003           | TC-082           | BureauResponseMapper  |
| Valid bureau response populates all fields        | BR-MAP-004           | TC-083           | BureauResponseMapper  |
| Zero credit limit → zero utilization             | BR-MAP-005           | TC-084           | BureauResponseMapper  |
| Null credit score → UNKNOWN range                | BR-MAP-001           | TC-085           | BureauResponseMapper  |

---

### 3.10 FEAT-010: Transaction Logging

**File:** `features/logging/transaction-logging.feature`
**Component(s):** `TransactionLogger.java`, `LoggerConstants.java`, Wire Tap Camel route
**Business Rules:** LOG-001 through LOG-003
**Priority:** Medium

```gherkin
@logging @priority-medium @FEAT-010 @regression
Feature: Transaction Logging
  As the credit risk gateway operations team
  I want every credit risk request and response to be logged asynchronously to MongoDB
  So that all transactions are auditable and traceable

  Background:
    Given the TransactionLogger is wired into the Wire Tap Camel route
    And the MongoDB instance is configured with host, port, database, and collection

  # --- Successful Logging ---

  @happy-path @smoke @current-behavior
  Scenario: Successful credit risk request is logged to MongoDB with 17 fields
    Given a completed credit risk assessment with all response fields populated
    When the Wire Tap route triggers the TransactionLogger
    Then a document is inserted into MongoDB containing:
      | transactionId   |
      | applicantId     |
      | province        |
      | productType     |
      | requestChannel  |
      | sourceSystem    |
      | timestamp       |
      | riskCategory    |
      | overallScore    |
      | recommendation  |
      | status          |
      | errorCode       |
      | errorMessage    |
      | requestPayload  |
      | responsePayload |
      | bureauScore     |
      | application     |
    And the status field in the log document is "success"

  @happy-path @current-behavior
  Scenario: Failed credit risk request is logged to MongoDB with error details
    Given a credit risk assessment that resulted in an error
    And the ERROR_CODE exchange property is "CR-400"
    And the ERROR_MESSAGE exchange property is "Validation failed"
    When the Wire Tap route triggers the TransactionLogger
    Then a document is inserted into MongoDB
    And the status field is "error"
    And the errorCode field is "CR-400"
    And the errorMessage field is "Validation failed"

  @happy-path @current-behavior
  Scenario: Logging occurs asynchronously and does not block primary response
    Given a valid credit risk request
    When the endpoint processes the request end-to-end
    Then the credit risk response is returned to the caller first
    And the Wire Tap route executes the TransactionLogger asynchronously afterward

  @negative @error-handling @gap @current-behavior
  Scenario: MongoDB connection failure is caught and primary processing continues
    Given the MongoDB instance is unreachable
    When the Wire Tap route triggers the TransactionLogger
    Then the TransactionLogger catches the connection exception
    And an error is logged to the application log
    But no exception propagates back to the primary route

  @negative @error-handling @gap @current-behavior
  Scenario: MongoDB port misconfiguration logs a connection error silently
    Given the MongoDB port configuration is invalid
    When the Wire Tap route triggers the TransactionLogger
    Then the TransactionLogger logs an error
    And the primary service response is not affected
```

#### Scenario Traceability

| **Scenario**                                          | **Business Rule(s)** | **Test Case(s)** | **Component(s)**   |
|-------------------------------------------------------|----------------------|------------------|--------------------|
| Successful request logged with 17 fields              | LOG-001              | TC-090           | TransactionLogger  |
| Failed request logged with error details              | LOG-001              | TC-091           | TransactionLogger  |
| Logging is asynchronous (Wire Tap pattern)            | LOG-002              | TC-092           | Wire Tap route     |
| MongoDB failure caught, primary processing continues  | LOG-003              | TC-093           | TransactionLogger  |
| MongoDB misconfiguration logs error silently          | LOG-003              | TC-094           | TransactionLogger  |

---

### 3.11 FEAT-011: Error Handling

**File:** `features/error/error-handling.feature`
**Component(s):** `ErrorProcessor.java`
**Business Rules:** ERR-001, ERR-002
**Priority:** High

```gherkin
@error-handling @priority-high @FEAT-011 @regression
Feature: Centralized Error Handling
  As the credit risk gateway
  I want all exceptions to be caught and mapped to appropriate HTTP responses
  So that callers receive meaningful, consistent error responses

  Background:
    Given the ErrorProcessor is registered as the onException handler in the Camel route

  # --- Validation Error Handling ---

  @negative @validation @current-behavior
  Scenario: ValidationException is mapped to HTTP 400 Bad Request
    Given a ValidationException is thrown with errorCode "CR-004" and message "Date of Birth must be in YYYY-MM-DD format"
    When the ErrorProcessor handles the exception
    Then the HTTP response status is 400
    And the response body contains statusCode "CR-004"
    And the response body contains statusMessage "Date of Birth must be in YYYY-MM-DD format"
    And the riskCategory in the response is "ERROR"
    And the recommendation in the response is "REFER_TO_UNDERWRITER"

  @negative @validation @data-driven @current-behavior
  Scenario Outline: All validation errors (CR-001 through CR-007) produce HTTP 400
    Given a ValidationException with errorCode "<error_code>" and message "<error_message>"
    When the ErrorProcessor handles the exception
    Then the HTTP response status is 400
    And the response statusCode is "<error_code>"

    Examples:
      | error_code | error_message                                   |
      | CR-001     | Applicant ID field is blank                     |
      | CR-002     | First Name field is blank                       |
      | CR-003     | Last Name field is blank                        |
      | CR-004     | Date of Birth field is blank                    |
      | CR-004     | Date of Birth must be in YYYY-MM-DD format      |
      | CR-005     | Social Insurance Number field is blank          |
      | CR-005     | Social Insurance Number must be in NNN-NNN-NNN format |
      | CR-006     | Invalid province code: XX                       |
      | CR-007     | Invalid postal code format                      |

  # --- System Error Handling ---

  @negative @error-handling @current-behavior
  Scenario: Non-validation exception is mapped to HTTP 500 Internal Server Error
    Given a RuntimeException is thrown during bureau processing
    When the ErrorProcessor handles the exception
    Then the HTTP response status is 500
    And the response statusCode is "CR-500"
    And the response statusMessage is "Internal service error occurred. Please contact support."
    And the riskCategory in the response is "ERROR"

  @negative @error-handling @current-behavior
  Scenario: Error details are stored as exchange properties for transaction logging
    Given any exception is caught by the ErrorProcessor
    When the ErrorProcessor handles the exception
    Then the exchange property "ERROR_CODE" is populated
    And the exchange property "ERROR_MESSAGE" is populated
    And the response Content-Type is "application/xml"
```

#### Scenario Traceability

| **Scenario**                                          | **Business Rule(s)** | **Test Case(s)** | **Component(s)**  |
|-------------------------------------------------------|----------------------|------------------|-------------------|
| ValidationException → HTTP 400                        | ERR-001              | TC-100           | ErrorProcessor    |
| All CR-001–CR-007 errors produce HTTP 400             | ERR-001              | TC-101           | ErrorProcessor    |
| Non-validation exception → HTTP 500                   | ERR-002              | TC-102           | ErrorProcessor    |
| Error details stored as exchange properties           | ERR-002              | TC-103           | ErrorProcessor    |

---

## 4. Tag Reference

Tags used across all feature files for categorization and test execution filtering.

### Functional Tags

| **Tag**             | **Description**                                                   |
|---------------------|-------------------------------------------------------------------|
| `@rest`             | Scenarios related to the REST endpoint                            |
| `@soap`             | Scenarios related to the SOAP endpoint                            |
| `@gateway`          | Scenarios related to the Gateway SOAP endpoint                    |
| `@validation`       | Scenarios related to input validation (CreditRiskRequestValidator)|
| `@scoring`          | Scenarios related to the scoring engine                           |
| `@standard`         | Scenarios specific to StandardScoringStrategy                     |
| `@conservative`     | Scenarios specific to ConservativeScoringStrategy                 |
| `@aggressive`       | Scenarios specific to AggressiveScoringStrategy                   |
| `@bureau`           | Scenarios related to bureau response mapping                      |
| `@logging`          | Scenarios related to transaction logging                          |
| `@error-handling`   | Scenarios related to error handling                               |
| `@FEAT-001`         | Links scenario to FEAT-001                                        |
| `@FEAT-002`         | Links scenario to FEAT-002                                        |
| `@FEAT-003`         | Links scenario to FEAT-003                                        |
| `@FEAT-004`         | Links scenario to FEAT-004                                        |
| `@FEAT-005`         | Links scenario to FEAT-005                                        |
| `@FEAT-006`         | Links scenario to FEAT-006                                        |
| `@FEAT-007`         | Links scenario to FEAT-007                                        |
| `@FEAT-008`         | Links scenario to FEAT-008                                        |
| `@FEAT-009`         | Links scenario to FEAT-009                                        |
| `@FEAT-010`         | Links scenario to FEAT-010                                        |
| `@FEAT-011`         | Links scenario to FEAT-011                                        |
| `@BR-CR-001`        | Links scenario to business rule CR-001                            |
| `@BR-CR-002`        | Links scenario to business rule CR-002                            |
| `@BR-CR-003`        | Links scenario to business rule CR-003                            |
| `@BR-CR-004`        | Links scenario to business rule CR-004                            |
| `@BR-CR-005`        | Links scenario to business rule CR-005                            |
| `@BR-CR-006`        | Links scenario to business rule CR-006                            |
| `@BR-CR-007`        | Links scenario to business rule CR-007                            |

### Execution Tags

| **Tag**             | **Description**                                      |
|---------------------|------------------------------------------------------|
| `@smoke`            | Included in smoke test suite                          |
| `@regression`       | Included in full regression suite                     |
| `@wip`              | Work in progress — not ready for execution            |
| `@manual`           | Requires manual testing                               |
| `@automated`        | Fully automated scenario                              |

### Classification Tags

| **Tag**             | **Description**                                      |
|---------------------|------------------------------------------------------|
| `@happy-path`       | Normal/expected behavior                              |
| `@negative`         | Error/failure scenarios                               |
| `@boundary`         | Boundary/edge conditions                              |
| `@data-driven`      | Parameterized with example data                       |
| `@validation`       | Input validation scenarios                            |
| `@error-handling`   | System error handling scenarios                       |
| `@security`         | Security-related scenarios                            |
| `@performance`      | Performance-related scenarios                         |

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

Common step patterns for reuse across feature files.

### Given Steps

| **Step Pattern**                                                          | **Description**                                           |
|---------------------------------------------------------------------------|-----------------------------------------------------------|
| `Given the nexgen-creditrisk-gateway service is running`                  | Service health pre-condition                              |
| `Given the credit bureau service is available`                            | External dependency pre-condition                         |
| `Given the MongoDB transaction store is available`                        | Logging dependency pre-condition                          |
| `Given a valid REST request with the following parameters:`               | Sets up a full REST request using a data table            |
| `Given a valid REST request for applicant {string}`                       | Sets up a minimal valid REST request by applicant ID      |
| `Given a REST request with applicantId {string}`                          | Partial request pre-condition for validation testing      |
| `Given a credit risk request with all required fields`                    | Baseline valid request for validation edge cases          |
| `Given a validated credit risk request is available in the exchange`      | Pre-validates a request and stores it on the exchange     |
| `Given a validated request with productType {string}`                     | Sets product type on a pre-validated request              |
| `Given a bureau score of {int}`                                           | Sets bureau score for scoring strategy scenarios          |
| `Given a debt-to-income ratio of {double}`                                | Sets DTI for scoring scenarios                            |
| `Given a utilization rate of {double}`                                    | Sets credit utilization rate for scoring scenarios        |
| `Given a debt service ratio of {double}`                                  | Sets debt service ratio for affordability scenarios       |
| `Given a risk category of {string}`                                       | Sets risk category for recommendation scenarios           |
| `Given a bureau response with creditScore {int}`                          | Sets up a mock bureau response                            |
| `Given the bureau response is null`                                       | Simulates null bureau response                            |
| `Given the MongoDB instance is unreachable`                               | Simulates MongoDB connectivity failure                    |
| `Given the {strategy} is being used for scoring`                          | Activates a specific scoring strategy                     |

### When Steps

| **Step Pattern**                                                          | **Description**                                           |
|---------------------------------------------------------------------------|-----------------------------------------------------------|
| `When the GET /assess endpoint is called`                                 | Invokes the REST endpoint                                 |
| `When the assessCreditRisk SOAP operation is invoked`                     | Invokes the SOAP operation                                |
| `When the gateway endpoint receives the request`                          | Invokes the Gateway endpoint                              |
| `When the validator processes the request`                                | Triggers CreditRiskRequestValidator.process()             |
| `When the ScoringStrategyProcessor processes the request`                 | Triggers ScoringStrategyProcessor.process()               |
| `When the {strategy} strategy categorizes the risk`                       | Invokes categorizeRisk() on a scoring strategy            |
| `When the {strategy} strategy determines the affordability rating`        | Invokes determineAffordabilityRating()                    |
| `When the {strategy} strategy determines the recommendation`              | Invokes determineRecommendation()                         |
| `When the {strategy} strategy calculates the overall score`               | Invokes calculateOverallScore()                           |
| `When the BureauResponseMapper processes the response`                    | Triggers BureauResponseMapper.process()                   |
| `When the Wire Tap route triggers the TransactionLogger`                  | Fires the async logging route                             |
| `When the ErrorProcessor handles the exception`                           | Triggers ErrorProcessor.process()                         |
| `When the GatewayRequestPreProcessor processes the request`               | Triggers GatewayRequestPreProcessor.process()             |

### Then Steps

| **Step Pattern**                                                          | **Description**                                           |
|---------------------------------------------------------------------------|-----------------------------------------------------------|
| `Then the HTTP response status is {int}`                                  | Verifies HTTP status code                                 |
| `Then the response Content-Type is {string}`                              | Verifies Content-Type header                              |
| `Then the response body contains a riskCategory`                          | Verifies riskCategory is present in response              |
| `Then the response body contains errorCode {string}`                      | Verifies error code in response body                      |
| `Then the response body contains errorMessage {string}`                   | Verifies error message in response body                   |
| `Then a ValidationException is thrown with errorCode {string}`            | Verifies ValidationException type and code                |
| `Then the error message is {string}`                                      | Verifies exception message                                |
| `Then the risk category is {string}`                                      | Verifies categorizeRisk() return value                    |
| `Then the affordability rating is {string}`                               | Verifies determineAffordabilityRating() return value      |
| `Then the recommendation is {string}`                                     | Verifies determineRecommendation() return value           |
| `Then the overall score is between 0 and 100`                             | Verifies normalized score output range                    |
| `Then the selected scoring strategy is {string}`                          | Verifies strategy name on exchange property               |
| `Then the bureauScoreRange is {string}`                                   | Verifies bureau score range mapping                       |
| `Then the exchange property {string} is set to {string}`                  | Verifies Camel exchange property value                    |
| `Then the transaction is logged to MongoDB`                               | Verifies MongoDB insert was called                        |
| `Then the transaction is logged with status {string}`                     | Verifies status field in logged document                  |
| `Then a document is inserted into MongoDB containing:`                    | Verifies MongoDB document fields via data table           |
| `Then the SOAP response contains a SOAP Fault`                            | Verifies SOAP fault in response                           |
| `Then the validation result is {string}`                                  | Verifies VALID or INVALID outcome                         |
| `Then the error code is {string}`                                         | Verifies error code string (empty for valid cases)        |

---

## 6. Coverage Analysis

### Feature-to-Business-Rule Coverage

| **Business Rule** | **Description**                                      | **Covered By Feature(s)**         | **Scenario Count** | **Coverage** |
|-------------------|------------------------------------------------------|-----------------------------------|--------------------|--------------|
| CR-001            | ApplicantId required                                 | FEAT-001, FEAT-004, FEAT-011      | 5                  | Full         |
| CR-002            | FirstName required                                   | FEAT-001, FEAT-004, FEAT-011      | 5                  | Full         |
| CR-003            | LastName required                                    | FEAT-001, FEAT-004, FEAT-011      | 5                  | Full         |
| CR-004            | DateOfBirth required, YYYY-MM-DD format              | FEAT-001, FEAT-004, FEAT-011      | 8                  | Full         |
| CR-005            | SIN required, NNN-NNN-NNN format (9 digits)          | FEAT-004, FEAT-011                | 7                  | Full         |
| CR-006            | Province must be a valid Canadian province code      | FEAT-004                          | 16                 | Full         |
| CR-007            | Postal code must match Canadian format               | FEAT-004, FEAT-011                | 6                  | Full         |
| SS-001            | MORTGAGE/AUTO_LOAN → Conservative strategy           | FEAT-005                          | 2                  | Full         |
| SS-002            | CREDIT_CARD/LINE_OF_CREDIT → Aggressive strategy     | FEAT-005                          | 2                  | Full         |
| SS-003            | Default → Standard strategy (fallback)               | FEAT-005                          | 3                  | Full         |
| SCR-STD-001       | Standard risk categories and thresholds              | FEAT-006                          | 11                 | Full         |
| SCR-STD-002       | Standard affordability thresholds                    | FEAT-006                          | 8                  | Full         |
| SCR-STD-003       | Standard recommendation rules                        | FEAT-006                          | 7                  | Full         |
| SCR-STD-004       | Standard weights: Bureau=40%, DTI=30%, Util=20%, Emp=10% | FEAT-006                     | 1                  | Full         |
| SCR-CON-001       | Conservative risk categories and thresholds          | FEAT-007                          | 8                  | Full         |
| SCR-CON-002       | Conservative affordability thresholds                | FEAT-007                          | 7                  | Full         |
| SCR-CON-003       | Conservative recommendation rules                    | FEAT-007                          | 7                  | Full         |
| SCR-CON-004       | Conservative weights: Bureau=35%, DTI=35%, Util=15%, Emp=15% | FEAT-007                 | 1                  | Full         |
| SCR-AGG-001       | Aggressive risk categories and thresholds            | FEAT-008                          | 8                  | Full         |
| SCR-AGG-002       | Aggressive affordability thresholds                  | FEAT-008                          | 7                  | Full         |
| SCR-AGG-003       | Aggressive recommendation rules                      | FEAT-008                          | 5                  | Full         |
| SCR-AGG-004       | Aggressive weights: Bureau=50%, DTI=25%, Util=15%, Emp=10% | FEAT-008                   | 1                  | Full         |
| BR-MAP-001        | Bureau score ranges: EXCEPTIONAL≥800, VERY_GOOD≥740, GOOD≥670, FAIR≥580, POOR<580 | FEAT-009 | 10       | Full         |
| BR-MAP-002        | Null bureau response → BUREAU_ERROR=true, CR-301     | FEAT-009                          | 1                  | Full         |
| BR-MAP-003        | Bureau error code → BUREAU_ERROR=true, CR-302        | FEAT-009                          | 1                  | Full         |
| BR-MAP-004        | Valid response populates all CreditScoreDetail fields | FEAT-009                          | 1                  | Full         |
| BR-MAP-005        | Zero credit limit → zero utilization rate            | FEAT-009                          | 1                  | Full         |
| LOG-001           | All transactions logged to MongoDB with 17 fields    | FEAT-010                          | 2                  | Full         |
| LOG-002           | Logging is asynchronous (Wire Tap pattern)           | FEAT-010                          | 1                  | Full         |
| LOG-003           | MongoDB failure does not affect primary response     | FEAT-010                          | 2                  | Partial      |
| ERR-001           | ValidationException → HTTP 400                       | FEAT-011                          | 2                  | Full         |
| ERR-002           | All other exceptions → HTTP 500                      | FEAT-011                          | 2                  | Full         |

### Uncovered Business Rules

| **Business Rule** | **Rule Description**                                          | **Reason for No Coverage**                                       |
|-------------------|---------------------------------------------------------------|------------------------------------------------------------------|
| LOG-003           | MongoDB failure logging failure alert                         | Partial — alerting mechanism not implemented in current source   |
| —                 | Email format validation                                       | Not in source code — gap identified (see Section 8)              |
| —                 | Requested amount range validation                             | Not in source code — gap identified (see Section 8)              |
| —                 | Annual income range validation (negative/zero)                | Not in source code — gap identified (see Section 8)              |

---

## 7. Notes & Observations

| **#** | **Observation**                                                                                             | **Feature(s)**       | **Impact**   |
|-------|-------------------------------------------------------------------------------------------------------------|----------------------|--------------|
| 1     | `CreditRiskSoapSvcImpl.assessCreditRisk()` returns `null` directly — it is a stubbed/dead implementation. The real processing is handled by the Camel route. | FEAT-002 | High         |
| 2     | `CreditRiskRestSvc.assessCreditRisk()` also returns `null` — the JAX-RS method is a placeholder; the Camel `in_rest_creditRiskRouter` handles actual processing. | FEAT-001 | High         |
| 3     | No email field exists in `CreditRiskReqType` — if email is required by business, this is a data model gap. | FEAT-004             | Medium       |
| 4     | No numeric range validation for `annualIncome` or `requestedAmount` — negative values are accepted silently. | FEAT-004             | High         |
| 5     | Province validation only fires when province is provided (optional) — a blank province passes validation.  | FEAT-004             | Low          |
| 6     | Postal code validation only fires when postal code is provided (optional) — blank passes.                  | FEAT-004             | Low          |
| 7     | `TransactionLogger` creates a new `MongoClient` per transaction — no connection pooling, potential performance issue. | FEAT-010    | Medium       |
| 8     | Date-of-birth format regex `^\d{4}-\d{2}-\d{2}$` validates format only — does not validate actual date values (e.g., month=13). | FEAT-004 | Low     |
| 9     | SIN regex `^\d{3}-?\d{3}-?\d{3}$` allows both `123-456-789` and `123456789` — mixed separators (e.g., `123-456789`) are also valid. | FEAT-004 | Low |
| 10    | `StandardScoringStrategy` class Javadoc states "Weights: Bureau=40%, DTI=30%, Employment=20%, Income=10%" — incorrect labels. The actual constants are `WEIGHT_UTILIZATION = 20` and `WEIGHT_EMPLOYMENT = 10`. The Javadoc mislabels the 3rd weight as "Employment" (should be "Utilization") and the 4th as "Income" (should be "Employment"). The code logic is correct; only the comment needs correction. | FEAT-006 | Low |
| 11    | `determineAffordabilityRating` in StandardScoringStrategy returns "OVEREXTENDED" (not "UNAFFORDABLE" as mentioned in issue description). | FEAT-006 | Medium |

---

## 8. Gap-Exposing Scenarios

This section consolidates all scenarios that expose gaps — scenarios that document expected behavior NOT present in the current source code. These scenarios will **FAIL** against the source system but should **PASS** on the target system after migration.

### 8.1 Missing Validation Scenarios

```gherkin
@gap @validation @target-behavior @FEAT-004
Feature: Missing Input Validation Gaps
  These scenarios expose input validation gaps found during reverse engineering of
  CreditRiskRequestValidator. The current source system does not implement these
  validations; they must be added in the target system.

  # GAP-001: No email format validation
  @gap @validation @target-behavior @priority-medium
  Scenario: Invalid email format is rejected (target behavior — not in current source)
    Given a valid credit risk request
    And the email field is set to "not-an-email-address"
    When the endpoint is called
    Then the service should return a 400 Bad Request
    And the error message should indicate invalid email format

  # GAP-002: No amount range validation — negative requestedAmount accepted silently
  @gap @validation @target-behavior @priority-high
  Scenario: Negative requested amount is rejected (target behavior — not in current source)
    Given a valid credit risk request with all mandatory fields
    And the requestedAmount is set to -5000.00
    When the GET /assess endpoint is called
    Then the service should return a 400 Bad Request
    And the error message should indicate requestedAmount must be greater than zero

  # GAP-003: No annual income range validation — zero and negative values accepted
  @gap @validation @target-behavior @priority-high
  Scenario Outline: Invalid annual income values are rejected (target behavior — not in current source)
    Given a valid credit risk request with all mandatory fields
    And the annualIncome is set to <income_value>
    When the GET /assess endpoint is called
    Then the service should return a 400 Bad Request
    And the error message should indicate annualIncome must be a positive value

    Examples:
      | income_value |
      | -1.00        |
      | 0.00         |
      | -100000.00   |

  # GAP-004: DOB regex validates format but not date semantics
  @gap @validation @target-behavior @priority-low
  Scenario: Semantically invalid date (e.g., month=13) is rejected (target behavior — not in current source)
    Given a valid credit risk request
    And the dateOfBirth is set to "1985-13-01"
    When the endpoint is called
    Then the service should return a 400 Bad Request
    And the error message should indicate the date is not a valid calendar date

  # GAP-005: SIN validation allows mixed separator patterns
  @gap @validation @target-behavior @priority-low
  Scenario: Mixed SIN separator format is rejected (target behavior — not in current source)
    Given a valid credit risk request
    And the socialInsuranceNumber is set to "123-456789"
    When the endpoint is called
    Then the service should return a 400 Bad Request
    And the error message should indicate invalid SIN format
```

### 8.2 Hardcoded Value Scenarios

```gherkin
@gap @hardcoded @FEAT-006 @FEAT-007 @FEAT-008
Feature: Hardcoded Scoring Threshold Gaps
  These scenarios document scoring thresholds that are hardcoded in the source.
  The target system should externalize these thresholds to configuration.

  # GAP-006: Standard strategy thresholds are hardcoded in Java constants
  @current-behavior @hardcoded @priority-medium
  Scenario: Standard EXCELLENT threshold is always 750 (hardcoded — current behavior)
    Given the StandardScoringStrategy is configured
    When a request with bureauScore=750, dti=0.25, utilization=0.25 is scored
    Then the risk category is always "EXCELLENT"
    And the threshold 750 is hardcoded and cannot be changed via configuration

  @target-behavior @hardcoded @priority-medium
  Scenario: Standard EXCELLENT threshold is driven by external configuration (target behavior)
    Given the scoring configuration has EXCELLENT_THRESHOLD set to 760
    When a request with bureauScore=755, dti=0.25, utilization=0.25 is scored
    Then the risk category is "GOOD" (not EXCELLENT) because threshold is now 760

  # GAP-007: Conservative strategy weights are hardcoded literals
  @current-behavior @hardcoded @priority-medium
  Scenario: Conservative strategy weights are hardcoded as literals 35/35/15/15 (current behavior)
    Given the ConservativeScoringStrategy is used
    When any credit risk request is scored
    Then the weights applied are always Bureau=35, DTI=35, Utilization=15, Employment=15
    And these values are not configurable at runtime

  # GAP-008: Affordability label "OVEREXTENDED" vs expected "UNAFFORDABLE"
  @current-behavior @hardcoded @priority-low
  Scenario: All strategies return "OVEREXTENDED" for high debt service ratio (current behavior)
    Given a debt service ratio above the highest threshold (e.g., 0.60)
    When any scoring strategy determines the affordability rating
    Then the affordability rating returned is "OVEREXTENDED"
    And the label is hardcoded as "OVEREXTENDED" not "UNAFFORDABLE"

  @target-behavior @hardcoded @priority-low
  Scenario: Target system returns "UNAFFORDABLE" for debt service ratio ≥0.44 (target behavior)
    Given the target system is configured with affordability label "UNAFFORDABLE" at threshold 0.44
    And a standard scoring scenario with debt service ratio 0.50
    When the affordability rating is determined
    Then the affordability rating is "UNAFFORDABLE"
```

### 8.3 Dead Code Scenarios

```gherkin
@gap @dead-code @FEAT-002
Feature: Dead Code Verification — SOAP Implementation Stub
  CreditRiskSoapSvcImpl.assessCreditRisk() returns null directly without any processing.
  This is a dead code path. The real processing is done by the Camel route.
  The target system must eliminate this pattern and provide a real implementation.

  # GAP-009: SOAP service implementation is a stub that returns null
  @current-behavior @dead-code @priority-high @migration-critical
  Scenario: CreditRiskSoapSvcImpl.assessCreditRisk returns null (current — dead code)
    Given the CreditRiskSoapSvcImpl is called directly (bypassing Camel route)
    When assessCreditRisk is invoked with a valid CreditRiskRequest
    Then the return value is null
    And no bureau call is made
    And no scoring is performed
    And no logging occurs

  @target-behavior @dead-code @priority-high @migration-critical
  Scenario: Target SOAP implementation delegates to the scoring service (target behavior)
    Given the target CreditRiskSoapSvcImpl is called
    When assessCreditRisk is invoked with a valid CreditRiskRequest
    Then a non-null CreditRiskResType is returned
    And the response contains a valid riskCategory
    And the transaction is logged

  # GAP-010: REST service method also returns null
  @current-behavior @dead-code @priority-high @migration-critical
  Scenario: CreditRiskRestSvc.assessCreditRisk returns null (current — dead code)
    Given the CreditRiskRestSvc JAX-RS method is called directly (bypassing Camel route)
    When assessCreditRisk is invoked with valid parameters
    Then the return value is null
    And no bureau call is made
    And no processing occurs

  @target-behavior @dead-code @priority-high @migration-critical
  Scenario: Target REST implementation delegates to the scoring service (target behavior)
    Given the target CreditRiskRestSvc is called
    When assessCreditRisk is invoked with valid parameters
    Then a non-null CreditRiskResType is returned
    And the response contains a valid riskCategory

  # GAP-011: Dead field — requestChannel default in gateway only
  @current-behavior @dead-code @priority-low
  Scenario: requestChannel "API" default only set via GatewayRequestPreProcessor
    Given a request arrives via the REST or SOAP endpoint
    And the requestChannel field is not populated by the caller
    When the request is processed
    Then requestChannel remains null (not defaulted by REST or SOAP pre-processors)
    And only the Gateway pre-processor sets a default requestChannel
```

### 8.4 Transaction Logging Scenarios

```gherkin
@logging @migration-critical @FEAT-010
Feature: Transaction Logging Failure Scenarios
  These scenarios validate that transaction logging failures are handled gracefully
  and do not affect primary service availability.

  # GAP-012: No alerting on MongoDB failure
  @gap @logging @target-behavior @priority-medium
  Scenario: MongoDB unavailability generates a monitoring alert (target behavior — not in current source)
    Given the MongoDB transaction store is unreachable
    When the Wire Tap route triggers the TransactionLogger
    Then the TransactionLogger catches the connection exception
    And an error is written to the application log
    And a monitoring alert is triggered (e.g., via JMX or metrics endpoint)
    But the primary credit risk response is still returned to the caller

  # GAP-013: New MongoClient per transaction — no connection pooling
  @gap @logging @current-behavior @hardcoded @priority-medium
  Scenario: A new MongoDB connection is created for every transaction (current — no pooling)
    Given multiple concurrent credit risk requests are submitted
    When the Wire Tap route triggers the TransactionLogger for each request
    Then a new MongoClient instance is created per request
    And the connection is closed after each insert
    And no connection pool is used

  @target-behavior @logging @priority-medium
  Scenario: MongoDB connection pooling is used for transaction logging (target behavior)
    Given the target TransactionLogger uses a shared MongoClient with connection pooling
    When multiple concurrent credit risk requests trigger the TransactionLogger
    Then a single pooled MongoClient handles all logging inserts
    And connection overhead is minimized

  # GAP-014: 17 fields logged — verify completeness
  @logging @current-behavior @migration-critical
  Scenario: All 17 required fields are present in every logged transaction document
    Given a completed credit risk assessment (success or error)
    When the TransactionLogger writes the document to MongoDB
    Then the document contains exactly these 17 fields:
      | transactionId   |
      | applicantId     |
      | province        |
      | productType     |
      | requestChannel  |
      | sourceSystem    |
      | timestamp       |
      | riskCategory    |
      | overallScore    |
      | recommendation  |
      | status          |
      | errorCode       |
      | errorMessage    |
      | requestPayload  |
      | responsePayload |
      | bureauScore     |
      | application     |

  # GAP-015: Log timestamp uses SimpleDateFormat — not thread-safe
  @gap @logging @current-behavior @hardcoded @priority-medium
  Scenario: SimpleDateFormat instance is shared (static) — not thread-safe (current behavior)
    Given multiple concurrent requests trigger the TransactionLogger simultaneously
    When each thread accesses the static TIMESTAMP_FORMAT SimpleDateFormat
    Then a race condition may corrupt the timestamp format
    And the logged timestamp may be malformed under high concurrency
```

### 8.5 Gap Scenario Summary

| **Scenario Category**       | **Gap ID(s)**                    | **Count** | **Tags**                               | **Current System** | **Target System** |
|-----------------------------|----------------------------------|-----------|----------------------------------------|--------------------|-------------------|
| Missing Validation          | GAP-001 to GAP-005               | 7         | `@gap @validation @target-behavior`    | ❌ FAIL            | ✅ PASS           |
| Hardcoded Values            | GAP-006 to GAP-008               | 4         | `@gap @hardcoded`                      | ❌ FAIL            | ✅ PASS           |
| Dead Code                   | GAP-009 to GAP-011               | 5         | `@gap @dead-code`                      | ✅ PASS (no effect)| ✅ PASS (removed) |
| Transaction Logging Failure | GAP-012 to GAP-015               | 4         | `@logging @migration-critical`         | ❌ FAIL / ⚠️ RISK  | ✅ PASS           |

---

## 9. Appendices

### Appendix A: Gherkin Best Practices

- Use declarative (what) not imperative (how) language
- Keep scenarios independent — no scenario should depend on another
- Use Background for common Given steps shared across all scenarios in a feature
- Use Scenario Outline + Examples for data-driven testing
- One scenario should test one behavior
- Prefer concrete examples over abstract descriptions

### Appendix B: Glossary

| **Term**             | **Definition**                                                                                 |
|----------------------|------------------------------------------------------------------------------------------------|
| BDD                  | Behavior-Driven Development — a software development approach using Gherkin plain-text specs   |
| Gherkin              | Domain-specific language for writing BDD feature specifications                                |
| DTI                  | Debt-to-Income Ratio — total monthly debt / gross monthly income                               |
| DSR                  | Debt Service Ratio — mortgage payment / gross monthly income                                   |
| SIN                  | Social Insurance Number — Canadian government-issued 9-digit identifier                        |
| WSS4J                | Apache Web Services Security for Java — used for UsernameToken SOAP authentication             |
| Wire Tap             | Camel EIP pattern that routes a copy of the message to a secondary route without affecting the primary flow |
| SOAP Fault           | Structured error response in the SOAP messaging protocol                                       |
| Exchange Property    | Named value stored on a Camel Exchange for cross-processor communication                       |
| MongoDB              | NoSQL document store used for transaction audit logging                                        |
| JBoss Fuse           | Red Hat enterprise integration platform based on Apache Camel and OSGi                        |
| Camel Route          | Apache Camel integration pipeline definition                                                   |

---

> **Document Control:**
> | Version | Date | Author | Changes |
> |---------|------|--------|---------|
> | 0.1 | 31-Mar-2026 | Copilot RE Agent | Initial draft — all 11 features, 63 scenarios, 15 gap scenarios derived from source code |
