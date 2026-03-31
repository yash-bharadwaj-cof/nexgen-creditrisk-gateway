# Gap Report

---

| **Field**            | **Details**                                          |
|----------------------|------------------------------------------------------|
| **Project Name**     | NexGen Modernization                                 |
| **Application Name** | nexgen-creditrisk-gateway                            |
| **Version**          | 1.0                                                  |
| **Date**             | 31-Mar-2026                                          |
| **Prepared By**      | RE Automation (Local)                                |
| **Reviewed By**      | _Pending_                                            |
| **Status**           | Draft                                                |

---

## 1. Executive Summary

This gap report documents all gaps identified during the reverse engineering of the nexgen-creditrisk-gateway application — a JBoss Fuse 6.3 / Apache Camel 2.17 credit risk assessment service. The analysis examined 33 Java classes across 7 packages, 4 Camel routes, blueprint.xml configuration, and 17 application properties.

> **Total Gaps Identified:** 33
>
> **Critical:** 6 | **High:** 12 | **Medium:** 13 | **Low:** 2
>
> **Overall Risk Assessment:** High
>
> **Key Finding:** The application has zero test coverage, exposes PII in logs and MongoDB, uses multiple hardcoded business constants, and lacks fundamental security controls (rate limiting, authentication on REST/Gateway endpoints).

---

## 2. Scope & Methodology

### 2.1 Analysis Scope

| **Dimension**              | **Details**                                                       |
|----------------------------|-------------------------------------------------------------------|
| Source System               | nexgen-creditrisk-gateway (JBoss Fuse 6.3.0.redhat-187, Camel 2.17, CXF 3.1.5) |
| Target System               | Modernized microservice (Spring Boot / Quarkus — TBD)            |
| Modules Analyzed            | All 7 packages: service, processor, scoring, model, generated, logging + blueprint.xml + config |
| Modules Excluded            | None — full codebase analyzed                                    |
| Analysis Period             | March 2026                                                        |

### 2.2 Gap Categories

| **Category**               | **Description**                                                  |
|----------------------------|------------------------------------------------------------------|
| **Functional**             | Missing or incomplete business functionality                      |
| **Technical**              | Technology, architecture, or infrastructure gaps                  |
| **Data**                   | Data model, migration, or integration data gaps                   |
| **Integration**            | Missing or incompatible integration points                        |
| **Security**               | Security controls, compliance, or vulnerability gaps              |
| **Performance**            | Performance, scalability, or reliability gaps                     |
| **Testing**                | Test coverage, automation, or environment gaps                    |
| **Dead Code**              | Dead code paths, unused components, or unreachable branches       |
| **Hardcoded Values**       | Hardcoded/static values that bypass dynamic business logic        |

### 2.3 Severity Definitions

| **Severity** | **Definition**                                                              | **Action Required**        |
|--------------|-----------------------------------------------------------------------------|----------------------------|
| **Critical** | Blocks migration/go-live; no workaround available                           | Must resolve before go-live|
| **High**     | Significant impact on functionality or operations; workaround may exist     | Resolve before go-live     |
| **Medium**   | Moderate impact; workaround available                                       | Plan resolution post-go-live if needed |
| **Low**      | Minor impact; cosmetic or nice-to-have                                      | Address in future iteration|

---

## 3. Gap Summary Dashboard

### 3.1 Gaps by Category

| **Category**       | **Critical** | **High** | **Medium** | **Low** | **Total** |
|--------------------|--------------|----------|------------|---------|-----------|
| Functional         | 0            | 3        | 2          | 0       | 5         |
| Technical          | 0            | 1        | 3          | 0       | 4         |
| Security           | 2            | 2        | 1          | 0       | 5         |
| Testing            | 1            | 0        | 0          | 0       | 1         |
| Integration        | 0            | 1        | 1          | 0       | 2         |
| Validation         | 0            | 1        | 3          | 1       | 5         |
| Error Handling     | 0            | 1        | 2          | 0       | 3         |
| Dead Code          | 0            | 0        | 0          | 1       | 1         |
| Hardcoded Values   | 1            | 3        | 1          | 0       | 5         |
| Code Quality       | 2            | 0        | 0          | 0       | 2         |
| **Total**          | **6**        | **12**   | **13**     | **2**   | **33**    |

### 3.2 Gaps by Module/Component

| **Module/Component**       | **Critical** | **High** | **Medium** | **Low** | **Total** |
|----------------------------|--------------|----------|------------|---------|-----------|
| RiskScoreCalculator        | 1            | 3        | 2          | 0       | 6         |
| CreditRiskRequestValidator | 0            | 1        | 3          | 1       | 5         |
| TransactionLogger          | 1            | 0        | 1          | 0       | 2         |
| Scoring Strategies (all 3) | 0            | 0        | 2          | 0       | 2         |
| Blueprint / Config         | 1            | 2        | 2          | 0       | 5         |
| ErrorProcessor             | 0            | 1        | 1          | 0       | 2         |
| Endpoints (REST/SOAP/GW)   | 1            | 2        | 1          | 0       | 4         |
| Model Classes              | 0            | 0        | 0          | 1       | 1         |
| Overall Architecture       | 1            | 3        | 1          | 0       | 5         |
| Generated Classes          | 0            | 0        | 0          | 0       | 0         |

### 3.3 Resolution Status

| **Status**              | **Count** | **Percentage** |
|-------------------------|-----------|----------------|
| Open                    | 33        | 100%           |
| In Progress             | 0         | 0%             |
| Resolved                | 0         | 0%             |
| Accepted (Won't Fix)    | 0         | 0%             |
| Deferred                | 0         | 0%             |

---

## 4. Detailed Gap Analysis

### 4.1 GAP-001: Zero Test Coverage

| **Attribute**             | **Details**                                              |
|---------------------------|----------------------------------------------------------|
| **Gap ID**                | GAP-001                                                  |
| **Gap Title**             | No Test Files Exist — 0% Test Coverage                   |
| **Category**              | Testing                                                  |
| **Severity**              | Critical                                                 |
| **Status**                | Open                                                     |
| **Module/Component**      | All — entire codebase                                    |
| **Source Code Reference** | `src/test/java/` — directory doesn't exist or is empty   |
| **Discovered During**     | Code Review                                              |

#### Current State (As-Is)

No test files of any kind exist in the project. The `src/test/java/` directory contains no unit tests, integration tests, or any automated test infrastructure. Zero of the 33 Java classes have test coverage.

#### Expected/Target State (To-Be)

Complete test suite with ≥80% line coverage for all business-critical processors and scoring strategies. Estimated 198 test cases needed (see RE-008 Test Case Inventory).

#### Gap Description

Without any automated tests, there is no way to verify correct behavior during or after migration. Every business rule, scoring calculation, validation, and integration path is untested.

#### Impact Analysis

| **Impact Area**       | **Description**                                          |
|-----------------------|----------------------------------------------------------|
| Business Impact       | Cannot verify scoring accuracy; risk of incorrect credit decisions post-migration |
| Technical Impact      | No regression safety net; every change is high-risk      |
| User Impact           | Potential for incorrect risk assessments reaching end-users |
| Migration Impact      | Blocks confident migration; must create full test suite first |

#### Recommended Resolution

| **Option** | **Description**                              | **Effort** | **Risk** | **Recommended** |
|------------|----------------------------------------------|------------|----------|-----------------|
| Option A   | Create full test suite (198 TCs) pre-migration | L        | Low      | Yes             |
| Option B   | Create critical tests only (52 TCs) pre-migration | M      | Medium   | Acceptable      |

---

### 4.2 GAP-002: No Actual Income Verification

| **Attribute**             | **Details**                                              |
|---------------------------|----------------------------------------------------------|
| **Gap ID**                | GAP-002                                                  |
| **Gap Title**             | Income Verification Always Self-Reported — No External Check |
| **Category**              | Functional                                               |
| **Severity**              | High                                                     |
| **Status**                | Open                                                     |
| **Module/Component**      | COMP-009 — RiskScoreCalculator                           |
| **Source Code Reference** | `RiskScoreCalculator.java:L110-L120`                     |
| **Discovered During**     | Code Review                                              |

#### Current State (As-Is)

```java
verificationStatus = "SELF_REPORTED";  // Always hardcoded
verifiedIncome = request.getAnnualIncome();  // No verification
```

Income is never verified against any external source. The `verifiedIncome` field is simply copied from the self-reported value.

#### Expected/Target State (To-Be)

Integrate with income verification services (employment verification, tax records, bank statements) to validate reported income.

#### Impact Analysis

| **Impact Area**       | **Description**                                          |
|-----------------------|----------------------------------------------------------|
| Business Impact       | Risk of income fraud; inflated income leading to overlending |
| Migration Impact      | Must add verification workflow in target system          |

#### Recommended Resolution

| **Option** | **Description**                              | **Effort** | **Risk** | **Recommended** |
|------------|----------------------------------------------|------------|----------|-----------------|
| Option A   | Add external income verification integration | XL         | Medium   | Yes (phased)    |
| Option B   | Flag as known limitation; mitigate with lower amount thresholds | S | High | Interim only |

---

### 4.3 GAP-003: Hardcoded Monthly Debt Factor (3%)

| **Attribute**             | **Details**                                              |
|---------------------------|----------------------------------------------------------|
| **Gap ID**                | GAP-003                                                  |
| **Gap Title**             | Monthly Debt Estimated with Hardcoded 3% of Total Balance |
| **Category**              | Hardcoded Values                                         |
| **Severity**              | High                                                     |
| **Status**                | Open                                                     |
| **Module/Component**      | COMP-009 — RiskScoreCalculator                           |
| **Source Code Reference** | `RiskScoreCalculator.java:L132`                          |

#### Current State (As-Is)

`monthlyDebt = creditDetail.getTotalBalance() * 0.03`

A flat 3% of total outstanding balance is used as monthly debt with no consideration of account types, actual minimum payments, or interest rates.

#### Expected/Target State (To-Be)

Calculate monthly debt from actual minimum payments by account type, or make the factor configurable per product type.

#### Recommended Resolution

| **Option** | **Description**                                  | **Effort** | **Risk** | **Recommended** |
|------------|--------------------------------------------------|------------|----------|-----------------|
| Option A   | Externalize to config per product type           | S          | Low      | Yes             |
| Option B   | Use bureau-reported minimum payment data          | M          | Low      | Better (if data available) |

---

### 4.4 GAP-004: Hardcoded Monthly Payment Estimation (0.6%)

| **Attribute**             | **Details**                                              |
|---------------------------|----------------------------------------------------------|
| **Gap ID**                | GAP-004                                                  |
| **Gap Title**             | Requested Amount Monthly Payment Uses Hardcoded 0.6% Factor |
| **Category**              | Hardcoded Values                                         |
| **Severity**              | High                                                     |
| **Status**                | Open                                                     |
| **Module/Component**      | COMP-009 — RiskScoreCalculator                           |
| **Source Code Reference** | `RiskScoreCalculator.java:L143`                          |

#### Current State (As-Is)

`requestMonthly = request.getRequestedAmount() * 0.006`

Arbitrary 0.6% monthly payment estimation regardless of product type, loan term, or interest rate.

#### Expected/Target State (To-Be)

Calculate based on amortization schedule (term, rate, product type). A $500,000 mortgage and a $5,000 credit card should not use the same payment factor.

#### Recommended Resolution

| **Option** | **Description**                                  | **Effort** | **Risk** | **Recommended** |
|------------|--------------------------------------------------|------------|----------|-----------------|
| Option A   | Implement amortization calculation per product   | M          | Low      | Yes             |
| Option B   | Externalize factor per product type to config    | S          | Low      | Acceptable      |

---

### 4.5 GAP-005: No Loan-to-Value (LTV) Calculation

| **Attribute**             | **Details**                                              |
|---------------------------|----------------------------------------------------------|
| **Gap ID**                | GAP-005                                                  |
| **Gap Title**             | No LTV Calculation for Secured Products                  |
| **Category**              | Functional                                               |
| **Severity**              | High                                                     |
| **Status**                | Open                                                     |
| **Module/Component**      | COMP-009 — RiskScoreCalculator                           |
| **Discovered During**     | Business Rule Analysis                                   |

#### Gap Description

For MORTGAGE and AUTO_LOAN products (which use ConservativeScoringStrategy), there is no loan-to-value calculation. LTV is a fundamental ratio for secured lending that compares the loan amount to the collateral value.

#### Recommended Resolution

Add collateral/property value input field and LTV calculation, especially for Conservative strategy products.

---

### 4.6 GAP-006: No Employment Stability Assessment

| **Attribute**             | **Details**                                              |
|---------------------------|----------------------------------------------------------|
| **Gap ID**                | GAP-006                                                  |
| **Gap Title**             | Employment Risk Only Considers Type, Not Duration/Stability |
| **Category**              | Functional                                               |
| **Severity**              | Medium                                                   |
| **Status**                | Open                                                     |
| **Module/Component**      | COMP-009 — RiskScoreCalculator, EmploymentRiskDetail     |
| **Source Code Reference** | `RiskScoreCalculator.java:L122-L133`, `EmploymentRiskDetail.java` |

#### Gap Description

`EmploymentRiskDetail` model has `yearsEmployed` and `industryCategory` fields defined but they are **never populated or used** in scoring. Employment risk assessment only maps employment type (FULL_TIME → LOW, etc.) without considering how long someone has held their position.

---

### 4.7 GAP-007: No Credit History Age Analysis

| **Attribute**             | **Details**                                              |
|---------------------------|----------------------------------------------------------|
| **Gap ID**                | GAP-007                                                  |
| **Gap Title**             | No Credit History Duration or Payment Trend Analysis     |
| **Category**              | Functional                                               |
| **Severity**              | High                                                     |
| **Status**                | Open                                                     |
| **Module/Component**      | COMP-008 — BureauResponseMapper, COMP-009               |

#### Gap Description

Only snapshot credit data is used (score, balance, limits, delinquency count). There is no analysis of credit age, account opening dates, payment history trends, or credit mix.

---

### 4.8 GAP-008: No Bureau Score Dispute Handling

| **Attribute**             | **Details**                                              |
|---------------------------|----------------------------------------------------------|
| **Gap ID**                | GAP-008                                                  |
| **Gap Title**             | No Process for Applicant to Dispute Bureau Score         |
| **Category**              | Functional                                               |
| **Severity**              | Medium                                                   |
| **Status**                | Open                                                     |
| **Module/Component**      | Overall process                                          |

---

### 4.9 GAP-009: No Date of Birth Age Check

| **Attribute**             | **Details**                                              |
|---------------------------|----------------------------------------------------------|
| **Gap ID**                | GAP-009                                                  |
| **Gap Title**             | No Minimum Age Check on Date of Birth                    |
| **Category**              | Validation                                               |
| **Severity**              | High                                                     |
| **Status**                | Open                                                     |
| **Module/Component**      | COMP-005 — CreditRiskRequestValidator                    |
| **Source Code Reference** | `CreditRiskRequestValidator.java:L62-L64`                |

#### Current State (As-Is)

DOB validated for format only (YYYY-MM-DD regex). No check for:
- Minimum age (credit applicant must be 18+)
- Future date (DOB cannot be tomorrow)
- Maximum age reasonableness (e.g., < 120 years)

#### Recommended Resolution

Add age calculation and bounds checking after format validation.

---

### 4.10 GAP-010: No SIN Checksum Validation

| **Attribute**             | **Details**                                              |
|---------------------------|----------------------------------------------------------|
| **Gap ID**                | GAP-010                                                  |
| **Gap Title**             | SIN Format Validated but No Luhn Checksum Check          |
| **Category**              | Validation                                               |
| **Severity**              | Medium                                                   |
| **Status**                | Open                                                     |
| **Module/Component**      | COMP-005 — CreditRiskRequestValidator                    |
| **Source Code Reference** | `CreditRiskRequestValidator.java:L56-L60`                |

#### Gap Description

Canadian SINs have a built-in Luhn algorithm checksum. The current validation only checks the format (NNN-NNN-NNN) but not whether the digits form a valid SIN. This means syntactically correct but invalid SINs (e.g., 000-000-000) would pass validation.

---

### 4.11 GAP-011: No Postal Code to Province Cross-Validation

| **Attribute**             | **Details**                                              |
|---------------------------|----------------------------------------------------------|
| **Gap ID**                | GAP-011                                                  |
| **Gap Title**             | Postal Code and Province Validated Independently         |
| **Category**              | Validation                                               |
| **Severity**              | Medium                                                   |
| **Status**                | Open                                                     |
| **Module/Component**      | COMP-005 — CreditRiskRequestValidator                    |

#### Gap Description

Postal code format and province enum are both validated, but there's no check that the postal code's first letter corresponds to the stated province (e.g., postal codes starting with "M" should be in ON, not BC).

---

### 4.12 GAP-012: No Mandatory Field Dependencies by Product Type

| **Attribute**             | **Details**                                              |
|---------------------------|----------------------------------------------------------|
| **Gap ID**                | GAP-012                                                  |
| **Gap Title**             | No Conditional Validation Based on Product Type          |
| **Category**              | Validation                                               |
| **Severity**              | Medium                                                   |
| **Status**                | Open                                                     |
| **Module/Component**      | COMP-005 — CreditRiskRequestValidator                    |

#### Gap Description

All fields use the same validation regardless of product type. For example, `yearsEmployed` should be mandatory for employment-sensitive assessments, and collateral information should be required for mortgage/auto products.

---

### 4.13 GAP-013: No Age Check — Minor

| **Attribute**             | **Details**                                              |
|---------------------------|----------------------------------------------------------|
| **Gap ID**                | GAP-013                                                  |
| **Gap Title**             | DOB Reasonableness — No Future Date or Max Age Check     |
| **Category**              | Validation                                               |
| **Severity**              | Low                                                      |
| **Status**                | Open                                                     |
| **Module/Component**      | COMP-005 — CreditRiskRequestValidator                    |

---

### 4.14 GAP-014: No Province-Specific Scoring Adjustments

| **Attribute**             | **Details**                                              |
|---------------------------|----------------------------------------------------------|
| **Gap ID**                | GAP-014                                                  |
| **Gap Title**             | Province Captured But Ignored in Scoring                 |
| **Category**              | Functional                                               |
| **Severity**              | High                                                     |
| **Status**                | Open                                                     |
| **Module/Component**      | Scoring Strategies, Blueprint config                     |

#### Current State (As-Is)

`SUPPORTED_PROVINCE_LIST=ON,BC,AB,QC` exists in config but is never referenced in code. ProvinceType validation accepts all 13 provinces but scoring completely ignores which province the applicant is in.

#### Expected/Target State (To-Be)

Province-specific risk adjustments to account for different regulatory environments (e.g., QC civil law vs common law), real estate markets, and economic conditions.

---

### 4.15 GAP-015: Hardcoded Bureau Connector — No Fallback

| **Attribute**             | **Details**                                              |
|---------------------------|----------------------------------------------------------|
| **Gap ID**                | GAP-015                                                  |
| **Gap Title**             | Single Bureau Endpoint with No Circuit Breaker or Retry  |
| **Category**              | Integration                                              |
| **Severity**              | High                                                     |
| **Status**                | Open                                                     |
| **Module/Component**      | Blueprint — `bureauEndpoint` configuration               |
| **Source Code Reference** | `blueprint.xml:L102`                                     |

#### Current State (As-Is)

Single CXF endpoint with 30s connection + 30s receive timeout. No retry mechanism, no circuit breaker, no fallback bureau, no bulkhead isolation.

#### Expected/Target State (To-Be)

Circuit breaker (Camel EIP or Resilience4j), retry with exponential backoff, fallback scoring model when bureau is unavailable, bulkhead to isolate bureau call latency.

---

### 4.16 GAP-016: No Correlation ID Propagation

| **Attribute**             | **Details**                                              |
|---------------------------|----------------------------------------------------------|
| **Gap ID**                | GAP-016                                                  |
| **Gap Title**             | No W3C Trace Context or Distributed Tracing              |
| **Category**              | Technical                                                |
| **Severity**              | Medium                                                   |
| **Status**                | Open                                                     |
| **Module/Component**      | All routes                                               |

#### Gap Description

Transaction ID is generated but not propagated as W3C Trace Context headers. No distributed tracing integration (Jaeger, Zipkin, etc.). Makes it difficult to trace requests across the bureau call boundary.

---

### 4.17 GAP-017: PII Exposed in Logs and MongoDB

| **Attribute**             | **Details**                                              |
|---------------------------|----------------------------------------------------------|
| **Gap ID**                | GAP-017                                                  |
| **Gap Title**             | Personal Data Logged and Stored Unencrypted              |
| **Category**              | Security                                                 |
| **Severity**              | Critical                                                 |
| **Status**                | Open                                                     |
| **Module/Component**      | COMP-011 — TransactionLogger, COMP-005 — Validator       |
| **Source Code Reference** | `TransactionLogger.java:L29-L33`, `CreditRiskRequestValidator.java` |

#### Current State (As-Is)

- ApplicantId logged in console output
- ApplicantId, province, product type stored in MongoDB `transactions` collection in plain text
- SIN, DOB, names passed through pipeline without masking
- No encryption at rest on MongoDB audit trail

#### Impact Analysis

| **Impact Area**       | **Description**                                    |
|-----------------------|----------------------------------------------------|
| Business Impact       | Regulatory non-compliance (PIPEDA, provincial privacy laws) |
| Technical Impact      | Data breach risk; unauthorized access to PII       |
| Migration Impact      | Must add encryption/masking before go-live         |

#### Recommended Resolution

| **Option** | **Description**                     | **Effort** | **Risk** | **Recommended** |
|------------|-------------------------------------|------------|----------|-----------------|
| Option A   | Mask PII in logs; encrypt in MongoDB | M         | Low      | Yes             |
| Option B   | Separate audit trail DB with RBAC    | L         | Low      | Better          |

---

### 4.18 GAP-018: No Request/Response Encryption Enforcement

| **Attribute**             | **Details**                                              |
|---------------------------|----------------------------------------------------------|
| **Gap ID**                | GAP-018                                                  |
| **Gap Title**             | No TLS Enforcement or Certificate Pinning                |
| **Category**              | Security                                                 |
| **Severity**              | Critical                                                 |
| **Status**                | Open                                                     |
| **Module/Component**      | Blueprint — all endpoints                                |

#### Gap Description

REST endpoint has no encryption enforcement in blueprint. SOAP endpoint has schema validation but no encryption. Bureau endpoint uses HTTPS but lacks certificate pinning. No mutual TLS (mTLS) configured.

---

### 4.19 GAP-019: No Rate Limiting or API Abuse Prevention

| **Attribute**             | **Details**                                              |
|---------------------------|----------------------------------------------------------|
| **Gap ID**                | GAP-019                                                  |
| **Gap Title**             | No Throttling, Rate Limiting, or DoS Protection          |
| **Category**              | Security                                                 |
| **Severity**              | High                                                     |
| **Status**                | Open                                                     |
| **Module/Component**      | All endpoints                                            |

#### Gap Description

No throttling on any endpoint. No concurrent call limits. No API key rotation. No DDoS protection. An attacker could flood the bureau endpoint through the REST or Gateway endpoints (which have no authentication).

---

### 4.20 GAP-020: Weak Credential Management for Bureau

| **Attribute**             | **Details**                                              |
|---------------------------|----------------------------------------------------------|
| **Gap ID**                | GAP-020                                                  |
| **Gap Title**             | Bureau Credentials and WSS4J Config in Plain Text Files  |
| **Category**              | Security                                                 |
| **Severity**              | High                                                     |
| **Status**                | Open                                                     |
| **Module/Component**      | Blueprint — bureauEndpoint, WSS4J config                 |

#### Current State (As-Is)

```xml
<entry key="subscriberCode" value="NEXGEN-001"/>
<entry key="subscriberName" value="NexGen Financial"/>
```

Bureau subscriber credentials and WSS4J username/password callbacks are stored in plain text in configuration files. No credential rotation, no HSM, no vault integration.

#### Recommended Resolution

Move to external secret management (HashiCorp Vault, AWS Secrets Manager, or Azure Key Vault).

---

### 4.21 GAP-021: No REST/Gateway Authentication

| **Attribute**             | **Details**                                              |
|---------------------------|----------------------------------------------------------|
| **Gap ID**                | GAP-021                                                  |
| **Gap Title**             | REST and Gateway Endpoints Have No Authentication        |
| **Category**              | Security                                                 |
| **Severity**              | Medium                                                   |
| **Status**                | Open                                                     |
| **Module/Component**      | COMP-001, COMP-003 — REST and Gateway CXF endpoints      |

#### Gap Description

Only the SOAP endpoint (`creditRiskSoapEndpoint`) has WS-Security authentication. The REST endpoint and Gateway endpoint accept unauthenticated requests. Any client can invoke credit assessments without credentials.

---

### 4.22 GAP-022: Silent Failure on MongoDB Logging

| **Attribute**             | **Details**                                              |
|---------------------------|----------------------------------------------------------|
| **Gap ID**                | GAP-022                                                  |
| **Gap Title**             | Transaction Logging Fails Silently — No Alert or DLQ     |
| **Category**              | Error Handling                                           |
| **Severity**              | High                                                     |
| **Status**                | Open                                                     |
| **Module/Component**      | COMP-011 — TransactionLogger                             |
| **Source Code Reference** | `TransactionLogger.java:L65-L67`                         |

#### Current State (As-Is)

```java
} catch (Exception e) {
    LOG.error("Failed to log transaction to MongoDB", e);
    // Silent failure — no rethrow, no DLQ, no alert
}
```

If MongoDB is down, the audit trail is lost with no way to recover. No dead letter queue, no health check, no monitoring alert.

---

### 4.23 GAP-023: No Bureau Call Timeout + Failover Handling

| **Attribute**             | **Details**                                              |
|---------------------------|----------------------------------------------------------|
| **Gap ID**                | GAP-023                                                  |
| **Gap Title**             | Bureau SOAP Call Has No Circuit Breaker or Fallback      |
| **Category**              | Integration                                              |
| **Severity**              | Medium                                                   |
| **Status**                | Open                                                     |
| **Module/Component**      | Blueprint — bureau SOAP call in all routes               |

#### Gap Description

30-second timeout is configured but:
- No explicit handling for `SocketTimeoutException`
- No circuit breaker prevents repeated calls to a failing bureau
- No fallback scoring when the bureau is slow
- Timeout exception falls through to generic CR-500 handler

---

### 4.24 GAP-024: Generic Error Code CR-500 for All System Errors

| **Attribute**             | **Details**                                              |
|---------------------------|----------------------------------------------------------|
| **Gap ID**                | GAP-024                                                  |
| **Gap Title**             | All Non-Validation Errors Map to Generic CR-500          |
| **Category**              | Error Handling                                           |
| **Severity**              | Medium                                                   |
| **Status**                | Open                                                     |
| **Module/Component**      | COMP-013 — ErrorProcessor                                |
| **Source Code Reference** | `ErrorProcessor.java:L42-L48`                            |

#### Gap Description

Cannot distinguish between timeout, parsing error, bureau connectivity failure, MongoDB error, or unexpected runtime exception. All produce CR-500 with "Internal service error".

#### Recommended Resolution

Add specific error codes: CR-510 (bureau timeout), CR-520 (bureau error), CR-530 (database error), CR-540 (serialization error).

---

### 4.25 GAP-025: No Partial Bureau Response Handling

| **Attribute**             | **Details**                                              |
|---------------------------|----------------------------------------------------------|
| **Gap ID**                | GAP-025                                                  |
| **Gap Title**             | Partial Bureau Response Proceeds with Null/Zero Defaults |
| **Category**              | Error Handling                                           |
| **Severity**              | Medium                                                   |
| **Status**                | Open                                                     |
| **Module/Component**      | COMP-008, COMP-009                                       |

#### Gap Description

If bureau returns a response with missing fields (e.g., null creditScore), the scoring proceeds with default values (0 for score, 0.0 for utilization) without flagging that the data is incomplete.

---

### 4.26 GAP-026: Hardcoded Income Verification Status

| **Attribute**             | **Details**                                              |
|---------------------------|----------------------------------------------------------|
| **Gap ID**                | GAP-026                                                  |
| **Gap Title**             | Income Verification Status Always "SELF_REPORTED"        |
| **Category**              | Hardcoded Values                                         |
| **Severity**              | Critical                                                 |
| **Status**                | Open                                                     |
| **Module/Component**      | COMP-009 — RiskScoreCalculator                           |
| **Source Code Reference** | `RiskScoreCalculator.java:L110-L112`                     |

#### Current State

`verificationStatus = "SELF_REPORTED"` is hardcoded. No external verification occurs. Verified income equals reported income unconditionally.

---

### 4.27 GAP-027: Hardcoded Scoring Model Version

| **Attribute**             | **Details**                                              |
|---------------------------|----------------------------------------------------------|
| **Gap ID**                | GAP-027                                                  |
| **Gap Title**             | Scoring Model Version Hardcoded as Strategy Name + "-v1.0" |
| **Category**              | Hardcoded Values                                         |
| **Severity**              | Medium                                                   |
| **Status**                | Open                                                     |
| **Module/Component**      | COMP-009 — RiskScoreCalculator                           |

#### Gap Description

Model version is constructed from the strategy name with a hardcoded suffix. No actual model versioning, no A/B testing support, no model registry.

---

### 4.28 GAP-028: Hardcoded Risk Factor Thresholds

| **Attribute**             | **Details**                                              |
|---------------------------|----------------------------------------------------------|
| **Gap ID**                | GAP-028                                                  |
| **Gap Title**             | Risk Factor Thresholds Are Magic Numbers in Code         |
| **Category**              | Hardcoded Values                                         |
| **Severity**              | High                                                     |
| **Status**                | Open                                                     |
| **Module/Component**      | COMP-009 — RiskScoreCalculator                           |
| **Source Code Reference** | `RiskScoreCalculator.java:L149-L167`                     |

#### Current State

```java
if (bureauScore < 650)     // Magic number
if (utilization > 0.50)    // Magic number
if (delinquency > 0)       // Magic number
if (inquiryCount > 5)      // Magic number
if (dti > 0.40)            // Magic number
```

All thresholds are hardcoded literals. Same thresholds apply regardless of strategy.

---

### 4.29 GAP-029: Duplicate Code Across Scoring Strategies

| **Attribute**             | **Details**                                              |
|---------------------------|----------------------------------------------------------|
| **Gap ID**                | GAP-029                                                  |
| **Gap Title**             | Three Scoring Strategies Duplicate Same Algorithm Skeleton |
| **Category**              | Code Quality                                             |
| **Severity**              | Critical                                                 |
| **Status**                | Open                                                     |
| **Module/Component**      | StandardScoringStrategy, ConservativeScoringStrategy, AggressiveScoringStrategy |

#### Gap Description

All 3 strategies implement the same algorithm structure with different constants (thresholds, weights, multipliers). The scoring formula, normalization logic, and employment score lookup are copied across all 3 files. Any algorithm fix must be applied in 3 places.

#### Recommended Resolution

Extract common scoring formula to abstract base class with configurable parameters.

---

### 4.30 GAP-030: Magic Numbers Throughout Scoring

| **Attribute**             | **Details**                                              |
|---------------------------|----------------------------------------------------------|
| **Gap ID**                | GAP-030                                                  |
| **Gap Title**             | Magic Numbers in Scoring Strategies Not Extracted as Constants |
| **Category**              | Code Quality                                             |
| **Severity**              | Critical                                                 |
| **Status**                | Open                                                     |
| **Module/Component**      | All 3 scoring strategies + RiskScoreCalculator           |

#### Examples

| **Value** | **Location**                  | **Meaning**                            |
|-----------|-------------------------------|----------------------------------------|
| 0.03      | RiskScoreCalculator:L132      | Monthly debt estimation factor         |
| 0.006     | RiskScoreCalculator:L143      | Monthly payment estimation factor      |
| 200, 250, 150 | Strategy normalization    | DTI normalization multipliers          |
| 150, 200, 120 | Strategy normalization    | Utilization normalization multipliers  |
| 750, 780, 720 | Strategy categorize       | Bureau score EXCELLENT thresholds      |
| 0.30, 0.25, 0.40 | Strategy categorize   | DTI EXCELLENT thresholds               |
| 550       | Score normalization           | Bureau score range (850-300)           |

---

### 4.31 GAP-031: No Batch Processing Endpoint

| **Attribute**             | **Details**                                              |
|---------------------------|----------------------------------------------------------|
| **Gap ID**                | GAP-031                                                  |
| **Gap Title**             | BATCH Channel Type Defined in Enum But No Batch Endpoint |
| **Category**              | Technical                                                |
| **Severity**              | Medium                                                   |
| **Module/Component**      | RequestChannelType enum, Blueprint routes                |

#### Gap Description

`RequestChannelType` enum includes `BATCH` but there is no batch processing route or endpoint. This is either dead code or an incomplete feature.

---

### 4.32 GAP-032: No MongoDB Injection Protection

| **Attribute**             | **Details**                                              |
|---------------------------|----------------------------------------------------------|
| **Gap ID**                | GAP-032                                                  |
| **Gap Title**             | MongoDB Operations Don't Explicitly Prevent NoSQL Injection |
| **Category**              | Technical                                                |
| **Severity**              | Medium                                                   |
| **Module/Component**      | COMP-011 — TransactionLogger                             |

#### Gap Description

While MongoDB BSON Document builder provides some inherent protection, input values from exchange properties are inserted without explicit sanitization. If any property contains MongoDB operators like `$gt`, `$ne`, etc., it could potentially affect query behavior if the same collection is queried elsewhere.

---

### 4.33 GAP-033: Dead Code — Service Implementation Stubs

| **Attribute**             | **Details**                                              |
|---------------------------|----------------------------------------------------------|
| **Gap ID**                | GAP-033                                                  |
| **Gap Title**             | Service Interface Implementations Return Null — Dead Code |
| **Category**              | Dead Code                                                |
| **Severity**              | Low                                                      |
| **Module/Component**      | CreditRiskRestSvc, CreditRiskSoapSvcImpl                 |

#### Gap Description

Both `CreditRiskRestSvc.assessCreditRisk()` and `CreditRiskSoapSvcImpl.assessCreditRisk()` have method bodies that return null. All actual processing is handled by Camel route processor chains. These implementations are never directly invoked — CXF uses them only as service interface declarations for WSDL/WADL generation.

---

## 5. Functional Gaps

| **Gap ID** | **Title**                             | **Severity** | **Module**        | **Resolution**                    | **Status** |
|------------|---------------------------------------|--------------|-------------------|-----------------------------------|------------|
| GAP-002    | No actual income verification         | High         | RiskScoreCalculator | Add external verification        | Open       |
| GAP-005    | No LTV calculation                    | High         | RiskScoreCalculator | Add collateral assessment        | Open       |
| GAP-006    | Employment stability not assessed     | Medium       | RiskScoreCalculator | Populate yearsEmployed           | Open       |
| GAP-007    | No credit history age analysis        | High         | BureauResponseMapper | Add credit age factors          | Open       |
| GAP-008    | No bureau score dispute process       | Medium       | Process            | Add dispute workflow             | Open       |

---

## 6. Technical Gaps

| **Gap ID** | **Title**                            | **Severity** | **Area**        | **Resolution**                    | **Status** |
|------------|--------------------------------------|--------------|-----------------|-----------------------------------|------------|
| GAP-016    | No correlation ID / distributed tracing | Medium    | Architecture    | Add W3C Trace Context             | Open       |
| GAP-031    | BATCH channel type but no batch endpoint | Medium   | Feature gap     | Implement or remove enum value    | Open       |
| GAP-032    | MongoDB injection risk               | Medium       | Data integrity  | Add input sanitization            | Open       |
| GAP-014    | Province captured but unused in scoring | High      | Business logic  | Add province-specific factors     | Open       |

---

## 7. Data Gaps

No data model gaps identified beyond those covered in Hardcoded Values (Section 12) and Dead Code (Section 11).

---

## 8. Integration Gaps

| **Gap ID** | **Title**                            | **Severity** | **Integration**     | **Resolution**                    | **Status** |
|------------|--------------------------------------|--------------|---------------------|-----------------------------------|------------|
| GAP-015    | Single bureau, no circuit breaker    | High         | Bureau SOAP         | Add Resilience4j/Hystrix          | Open       |
| GAP-023    | No timeout-specific handling         | Medium       | Bureau SOAP         | Add explicit timeout handling     | Open       |

---

## 9. Security Gaps

| **Gap ID** | **Title**                            | **Severity** | **Security Area**   | **Resolution**                    | **Status** |
|------------|--------------------------------------|--------------|---------------------|-----------------------------------|------------|
| GAP-017    | PII in logs and MongoDB              | Critical     | Information Disclosure | Mask logs, encrypt MongoDB      | Open       |
| GAP-018    | No TLS enforcement / cert pinning    | Critical     | Transport Security   | Enforce TLS 1.3+, add pinning   | Open       |
| GAP-019    | No rate limiting                     | High         | Abuse Prevention     | Implement throttling             | Open       |
| GAP-020    | Credentials in plain text config     | High         | Credential Management | Move to secrets manager         | Open       |
| GAP-021    | REST/Gateway endpoints unauthenticated | Medium     | Authentication      | Add API key / OAuth2             | Open       |

---

## 10. Testing Gaps

| **Gap ID** | **Title**                       | **Severity** | **Test Area** | **Resolution**                   | **Status** |
|------------|---------------------------------|--------------|---------------|----------------------------------|------------|
| GAP-001    | Zero test coverage              | Critical     | All           | Create full 198-TC test suite    | Open       |

---

## 11. Dead Code Gaps

| **Gap ID** | **Title**                      | **Severity** | **Component**            | **Source Location**                 | **Evidence**                                  | **Risk if Not Removed**         | **Resolution**   | **Status** |
|------------|--------------------------------|--------------|--------------------------|-------------------------------------|-----------------------------------------------|---------------------------------|------------------|------------|
| GAP-033    | Service impl stubs return null | Low          | CreditRiskRestSvc, CreditRiskSoapSvcImpl | service/*.java              | Methods return null; Camel handles all logic   | Confusion during migration      | Remove or delegate | Open     |

---

## 12. Hardcoded Value Gaps

| **Gap ID** | **Title**                          | **Severity** | **Component**        | **Field**           | **Hardcoded Value** | **Source Location**        | **Expected Dynamic Behavior**                     | **Resolution**        | **Status** |
|------------|------------------------------------|--------------|----------------------|---------------------|---------------------|----------------------------|---------------------------------------------------|-----------------------|------------|
| GAP-003    | Monthly debt factor                | High         | RiskScoreCalculator  | monthlyDebt         | 0.03 (3%)           | L132                       | Configurable per product type or from bureau data | Externalize to config | Open       |
| GAP-004    | Monthly payment factor             | High         | RiskScoreCalculator  | requestMonthly      | 0.006 (0.6%)        | L143                       | Amortization schedule per product/term/rate       | Implement calculation | Open       |
| GAP-026    | Income verification status         | Critical     | RiskScoreCalculator  | verificationStatus  | "SELF_REPORTED"      | L110                       | Actual verification against external source       | Add verification flow | Open       |
| GAP-027    | Scoring model version              | Medium       | RiskScoreCalculator  | scoringModelVersion | name+"-v1.0"         | score calc                 | Dynamic from model registry                       | Add versioning        | Open       |
| GAP-028    | Risk factor thresholds             | High         | RiskScoreCalculator  | multiple            | 650,0.50,5,0.40     | L149-L167                  | Configurable per strategy/environment             | Extract to config     | Open       |

---

## 13. Missing Business Logic Gaps

| **Gap ID** | **Title**                          | **Severity** | **Expected Rule**                       | **Business Domain** | **Identified By**  | **Resolution**                          | **Status** |
|------------|------------------------------------|--------------|-----------------------------------------|---------------------|--------------------|------------------------------------------|------------|
| GAP-002    | No income verification             | High         | Verify income against external sources  | Risk Assessment     | RE code analysis   | Implement verification workflow          | Open       |
| GAP-005    | No LTV calculation                 | High         | Loan-to-Value ratio for secured products| Risk Assessment     | RE code analysis   | Add collateral value input and LTV calc  | Open       |
| GAP-006    | No employment stability            | Medium       | Factor employment duration into risk    | Risk Assessment     | RE code analysis   | Populate and use yearsEmployed field     | Open       |
| GAP-014    | Province ignored in scoring        | High         | Province-specific risk adjustments      | Risk Assessment     | RE config analysis | Add province factors to strategies       | Open       |

---

## 14. Client-Raised Gaps

_No client review sessions have been conducted yet. This section will be populated after stakeholder review of this gap report._

| **Gap ID** | **Title** | **Severity** | **Raised By** | **Date Raised** | **Category** | **Description** | **Resolution** | **Status** |
|------------|-----------|--------------|---------------|-----------------|--------------|-----------------|----------------|------------|
| —          | —         | —            | —             | —               | —            | _Pending client review_ | —       | —          |

---

## 15. Gap Resolution Priority Matrix

| **Priority** | **Gap IDs**                                              | **Action**                                     |
|--------------|----------------------------------------------------------|------------------------------------------------|
| **P0 — Pre-Migration** | GAP-001 (tests), GAP-017 (PII), GAP-018 (TLS) | Must resolve before any migration begins       |
| **P1 — Before Go-Live** | GAP-002, GAP-003, GAP-004, GAP-015, GAP-019, GAP-020, GAP-026, GAP-029, GAP-030 | Resolve during migration development phase     |
| **P2 — Go-Live** | GAP-005, GAP-007, GAP-009, GAP-014, GAP-022, GAP-024, GAP-028 | Resolve before production deployment  |
| **P3 — Post-Go-Live** | GAP-006, GAP-008, GAP-010, GAP-011, GAP-012, GAP-016, GAP-021, GAP-023, GAP-025, GAP-027, GAP-031, GAP-032, GAP-033 | Address in subsequent iterations |

---

## 16. Appendices

### Appendix A: Gap ID Naming Convention

Format: `GAP-[NNN]` where NNN is sequential.

| **Range**     | **Domain**                |
|---------------|---------------------------|
| GAP-001       | Testing                   |
| GAP-002..008  | Functional                |
| GAP-009..013  | Validation                |
| GAP-014       | Province-specific logic   |
| GAP-015..016  | Integration / Technical   |
| GAP-017..021  | Security                  |
| GAP-022..025  | Error Handling            |
| GAP-026..028  | Hardcoded Values          |
| GAP-029..030  | Code Quality              |
| GAP-031..032  | Technical / Other         |
| GAP-033       | Dead Code                 |

### Appendix B: Cross-Reference to Other RE Documents

| **Gap ID** | **RE-003 (Sequence)** | **RE-004 (Business Rules)** | **RE-008 (Test Cases)** | **RE-010 (F2F Mapping)** |
|------------|------------------------|-----------------------------|-------------------------|--------------------------|
| GAP-001    | —                      | —                           | All TCs                 | —                        |
| GAP-002    | SD-001 Step 13         | BR-017                      | TC-RSC-006..007         | Sec 5.3                  |
| GAP-003    | SD-001 Step 12         | BR-015                      | TC-RSC-008, MIG-HC-001  | —                        |
| GAP-004    | SD-001 Step 12         | BR-019                      | TC-RSC-018, MIG-HC-002  | —                        |
| GAP-009    | SD-001 Step 2          | BR-004                      | MIG-VL-001, MIG-VL-002  | —                        |
| GAP-010    | SD-001 Step 2          | BR-005                      | MIG-VL-003              | —                        |
| GAP-011    | SD-001 Step 2          | BR-006, BR-007              | MIG-VL-004              | —                        |
| GAP-017    | SD-004 Step 2          | —                           | TC-SEC-006, TC-SEC-007  | —                        |
| GAP-022    | SD-004 Step 5          | —                           | TC-LOG-006              | —                        |
| GAP-024    | SD-005 Step 3          | BR-045                      | TC-ERR-003..004         | —                        |

---

> **Document Control:**
> | Version | Date | Author | Changes |
> |---------|------|--------|---------|
> | 1.0 | 31-Mar-2026 | RE Automation (Local) | Initial draft — 33 gaps across 10 categories, priority matrix, cross-references |
