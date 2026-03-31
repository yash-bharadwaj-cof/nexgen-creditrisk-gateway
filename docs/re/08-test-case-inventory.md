# Test Case Inventory

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

## 1. Overview

This document provides a comprehensive inventory of all test cases identified and derived during reverse engineering of the nexgen-creditrisk-gateway application. The application currently has **zero test coverage** — no test files exist in `src/test/java/`. All test cases below are newly derived from source code analysis and must be created for migration validation.

---

## 2. Test Summary Dashboard

### 2.1 Test Counts by Type

| **Test Type**        | **Total** | **Automated** | **Manual** | **Pending** |
|----------------------|-----------|---------------|------------|-------------|
| Unit Tests           | 124       | 0             | 0          | 124         |
| Integration Tests    | 28        | 0             | 0          | 28          |
| Functional/E2E Tests | 18        | 0             | 0          | 18          |
| Regression Tests     | 12        | 0             | 0          | 12          |
| Performance Tests    | 6         | 0             | 0          | 6           |
| Security Tests       | 10        | 0             | 0          | 10          |
| **Total**            | **198**   | **0**         | **0**      | **198**     |

### 2.2 Test Counts by Priority

| **Priority** | **Total** | **Pass** | **Fail** | **Not Run** | **Blocked** |
|--------------|-----------|----------|----------|-------------|-------------|
| Critical     | 52        | 0        | 0        | 52          | 0           |
| High         | 78        | 0        | 0        | 78          | 0           |
| Medium       | 48        | 0        | 0        | 48          | 0           |
| Low          | 20        | 0        | 0        | 20          | 0           |

### 2.3 Coverage Summary

| **Area**                        | **Total Rules/Features** | **Covered** | **Coverage %** |
|---------------------------------|--------------------------|-------------|----------------|
| Business Rules (BR-001..BR-045) | 45                       | 0           | 0%             |
| Components (33 classes)         | 33                       | 0           | 0%             |
| API Endpoints (4 routes)        | 4                        | 0           | 0%             |
| Scoring Strategies (3)          | 3                        | 0           | 0%             |

### 2.4 Migration Test Summary

| **Migration Test Category**         | **Total** | **Automated** | **Manual** | **Pending** |
|-------------------------------------|-----------|---------------|------------|-------------|
| Field-to-Field Mapping Tests        | 15        | 0             | 0          | 15          |
| Dead Code Removal Tests             | 4         | 0             | 0          | 4           |
| Hardcoded Value Replacement Tests   | 6         | 0             | 0          | 6           |
| Missing Validation Tests            | 6         | 0             | 0          | 6           |
| Source-Target Regression Tests      | 12        | 0             | 0          | 12          |
| **Total Migration Tests**           | **43**    | **0**         | **0**      | **43**      |

---

## 3. Test Case Details

### 3.1 TC-VAL-001: Applicant ID Required — Valid Input

| **Attribute**            | **Details**                                              |
|--------------------------|----------------------------------------------------------|
| **Test Case ID**         | TC-VAL-001                                               |
| **Test Case Name**       | Applicant ID — Valid non-blank value accepted             |
| **Test Type**            | Unit                                                     |
| **Priority**             | Critical                                                 |
| **Module/Component**     | COMP-005 — CreditRiskRequestValidator                    |
| **Business Rule Ref**    | BR-001                                                   |
| **Automation Status**    | To Be Automated                                          |

#### Test Steps

| **Step #** | **Action**                                     | **Input Data**               | **Expected Result**                         |
|------------|------------------------------------------------|------------------------------|---------------------------------------------|
| 1          | Create CreditRiskReqType with applicantId="A001" | applicantId="A001"         | No exception thrown                         |
| 2          | Call process(exchange)                         | Exchange with valid request  | VALIDATED_REQUEST property set on exchange  |

---

### 3.2 TC-VAL-002: Applicant ID Required — Null

| **Attribute**            | **Details**                                              |
|--------------------------|----------------------------------------------------------|
| **Test Case ID**         | TC-VAL-002                                               |
| **Test Case Name**       | Applicant ID — Null value throws CR-001                  |
| **Test Type**            | Unit                                                     |
| **Priority**             | Critical                                                 |
| **Module/Component**     | COMP-005 — CreditRiskRequestValidator                    |
| **Business Rule Ref**    | BR-001                                                   |

#### Test Steps

| **Step #** | **Action**                                     | **Input Data**               | **Expected Result**                         |
|------------|------------------------------------------------|------------------------------|---------------------------------------------|
| 1          | Create request with applicantId=null           | applicantId=null             | ValidationException thrown                  |
| 2          | Verify exception error code                    | —                            | errorCode = "CR-001"                        |
| 3          | Verify exception message                       | —                            | message = "Applicant ID is required"        |

---

### 3.3 TC-VAL-003: Applicant ID Required — Blank

| **Test Case ID** | TC-VAL-003 |
|---|---|
| **Test Case Name** | Applicant ID — Blank string throws CR-001 |
| **Priority** | Critical |
| **Business Rule Ref** | BR-001 |
| **Input** | applicantId = "   " (whitespace) |
| **Expected** | ValidationException with CR-001 |

---

### 3.4 TC-VAL-004: First Name Required — Null

| **Test Case ID** | TC-VAL-004 |
|---|---|
| **Test Case Name** | First Name — Null throws CR-002 |
| **Priority** | High |
| **Business Rule Ref** | BR-002 |
| **Input** | firstName = null |
| **Expected** | ValidationException with CR-002 |

---

### 3.5 TC-VAL-005: Last Name Required — Null

| **Test Case ID** | TC-VAL-005 |
|---|---|
| **Test Case Name** | Last Name — Null throws CR-003 |
| **Priority** | High |
| **Business Rule Ref** | BR-003 |
| **Input** | lastName = null |
| **Expected** | ValidationException with CR-003 |

---

### 3.6 TC-VAL-006: DOB — Valid Format YYYY-MM-DD

| **Test Case ID** | TC-VAL-006 |
|---|---|
| **Test Case Name** | DOB — Valid YYYY-MM-DD format accepted |
| **Priority** | High |
| **Business Rule Ref** | BR-004 |
| **Input** | dateOfBirth = "1990-06-15" |
| **Expected** | No exception thrown |

---

### 3.7 TC-VAL-007: DOB — Invalid Format

| **Test Case ID** | TC-VAL-007 |
|---|---|
| **Test Case Name** | DOB — Invalid format throws CR-004 |
| **Priority** | High |
| **Business Rule Ref** | BR-004 |
| **Input** | dateOfBirth = "06/15/1990" |
| **Expected** | ValidationException with CR-004 |

---

### 3.8 TC-VAL-008: DOB — Null

| **Test Case ID** | TC-VAL-008 |
|---|---|
| **Priority** | High |
| **Business Rule Ref** | BR-004 |
| **Input** | dateOfBirth = null |
| **Expected** | ValidationException with CR-004 |

---

### 3.9 TC-VAL-009: SIN — Valid Format With Dashes

| **Test Case ID** | TC-VAL-009 |
|---|---|
| **Priority** | Critical |
| **Business Rule Ref** | BR-005 |
| **Input** | sin = "123-456-789" |
| **Expected** | No exception thrown |

---

### 3.10 TC-VAL-010: SIN — Valid Format Without Dashes

| **Test Case ID** | TC-VAL-010 |
|---|---|
| **Priority** | Critical |
| **Business Rule Ref** | BR-005 |
| **Input** | sin = "123456789" |
| **Expected** | No exception thrown |

---

### 3.11 TC-VAL-011: SIN — Invalid Format

| **Test Case ID** | TC-VAL-011 |
|---|---|
| **Priority** | Critical |
| **Business Rule Ref** | BR-005 |
| **Input** | sin = "12-345-6789" |
| **Expected** | ValidationException with CR-005 |

---

### 3.12 TC-VAL-012: Province — Valid (ON)

| **Test Case ID** | TC-VAL-012 |
|---|---|
| **Priority** | High |
| **Business Rule Ref** | BR-006 |
| **Input** | province = "ON" |
| **Expected** | No exception thrown |

---

### 3.13 TC-VAL-013: Province — Invalid (XX)

| **Test Case ID** | TC-VAL-013 |
|---|---|
| **Priority** | High |
| **Business Rule Ref** | BR-006 |
| **Input** | province = "XX" |
| **Expected** | ValidationException with CR-006 |

---

### 3.14 TC-VAL-014: Province — All 13 Valid Values

| **Test Case ID** | TC-VAL-014 |
|---|---|
| **Priority** | Medium |
| **Business Rule Ref** | BR-006 |
| **Input** | Each of ON, QC, BC, AB, MB, SK, NS, NB, NL, PE, NT, YT, NU |
| **Expected** | No exception thrown for each |

---

### 3.15 TC-VAL-015: Postal Code — Valid With Space

| **Test Case ID** | TC-VAL-015 |
|---|---|
| **Priority** | Medium |
| **Business Rule Ref** | BR-007 |
| **Input** | postalCode = "M5V 3A8" |
| **Expected** | No exception thrown |

---

### 3.16 TC-VAL-016: Postal Code — Valid Without Space

| **Test Case ID** | TC-VAL-016 |
|---|---|
| **Priority** | Medium |
| **Business Rule Ref** | BR-007 |
| **Input** | postalCode = "M5V3A8" |
| **Expected** | No exception thrown |

---

### 3.17 TC-VAL-017: Postal Code — Invalid Format

| **Test Case ID** | TC-VAL-017 |
|---|---|
| **Priority** | Medium |
| **Business Rule Ref** | BR-007 |
| **Input** | postalCode = "12345" |
| **Expected** | ValidationException with CR-007 |

---

### 3.18 TC-VAL-018: Postal Code — Null (Optional)

| **Test Case ID** | TC-VAL-018 |
|---|---|
| **Priority** | Low |
| **Business Rule Ref** | BR-007 |
| **Input** | postalCode = null |
| **Expected** | No exception (postal code is optional) |

---

### 3.19 TC-VAL-019: Null Request Body

| **Test Case ID** | TC-VAL-019 |
|---|---|
| **Priority** | Critical |
| **Business Rule Ref** | BR-008 |
| **Input** | Exchange body = null |
| **Expected** | ValidationException with CR-000 |

---

### 3.20 TC-VAL-020: Logging Properties Set After Validation

| **Test Case ID** | TC-VAL-020 |
|---|---|
| **Priority** | Medium |
| **Business Rule Ref** | BR-001..BR-007 |
| **Input** | Valid complete request |
| **Expected** | Exchange properties set: TRANSACTION_ID, SOURCE_SYSTEM, APPLICANT_ID, LOG_PROVINCE, LOG_PRODUCT_TYPE, LOG_REQUEST_CHANNEL |

---

## 4. Test Cases by Module

### 4.1 Module: CreditRiskRequestValidator (COMP-005)

| **TC ID** | **Test Name**                          | **Type** | **Priority** | **Automation** | **Status** | **Covers** |
|-----------|----------------------------------------|----------|--------------|----------------|------------|------------|
| TC-VAL-001 | Applicant ID valid                   | Unit     | Critical     | To Be Auto     | Not Run    | BR-001     |
| TC-VAL-002 | Applicant ID null                    | Unit     | Critical     | To Be Auto     | Not Run    | BR-001     |
| TC-VAL-003 | Applicant ID blank                   | Unit     | Critical     | To Be Auto     | Not Run    | BR-001     |
| TC-VAL-004 | First Name null                      | Unit     | High         | To Be Auto     | Not Run    | BR-002     |
| TC-VAL-005 | Last Name null                       | Unit     | High         | To Be Auto     | Not Run    | BR-003     |
| TC-VAL-006 | DOB valid format                     | Unit     | High         | To Be Auto     | Not Run    | BR-004     |
| TC-VAL-007 | DOB invalid format                   | Unit     | High         | To Be Auto     | Not Run    | BR-004     |
| TC-VAL-008 | DOB null                             | Unit     | High         | To Be Auto     | Not Run    | BR-004     |
| TC-VAL-009 | SIN valid with dashes                | Unit     | Critical     | To Be Auto     | Not Run    | BR-005     |
| TC-VAL-010 | SIN valid without dashes             | Unit     | Critical     | To Be Auto     | Not Run    | BR-005     |
| TC-VAL-011 | SIN invalid format                   | Unit     | Critical     | To Be Auto     | Not Run    | BR-005     |
| TC-VAL-012 | Province valid ON                    | Unit     | High         | To Be Auto     | Not Run    | BR-006     |
| TC-VAL-013 | Province invalid XX                  | Unit     | High         | To Be Auto     | Not Run    | BR-006     |
| TC-VAL-014 | Province all 13 valid                | Unit     | Medium       | To Be Auto     | Not Run    | BR-006     |
| TC-VAL-015 | Postal code with space               | Unit     | Medium       | To Be Auto     | Not Run    | BR-007     |
| TC-VAL-016 | Postal code without space            | Unit     | Medium       | To Be Auto     | Not Run    | BR-007     |
| TC-VAL-017 | Postal code invalid                  | Unit     | Medium       | To Be Auto     | Not Run    | BR-007     |
| TC-VAL-018 | Postal code null (optional)          | Unit     | Low          | To Be Auto     | Not Run    | BR-007     |
| TC-VAL-019 | Null request body                    | Unit     | Critical     | To Be Auto     | Not Run    | BR-008     |
| TC-VAL-020 | Logging properties set               | Unit     | Medium       | To Be Auto     | Not Run    | BR-001..007|

### 4.2 Module: ScoringStrategyProcessor (COMP-006)

| **TC ID** | **Test Name**                          | **Type** | **Priority** | **Covers** |
|-----------|----------------------------------------|----------|--------------|------------|
| TC-STR-001 | MORTGAGE → ConservativeStrategy      | Unit     | Critical     | BR-009     |
| TC-STR-002 | AUTO_LOAN → ConservativeStrategy     | Unit     | Critical     | BR-009     |
| TC-STR-003 | CREDIT_CARD → AggressiveStrategy     | Unit     | Critical     | BR-009     |
| TC-STR-004 | LINE_OF_CREDIT → AggressiveStrategy  | Unit     | Critical     | BR-009     |
| TC-STR-005 | Unknown product, default=standard     | Unit     | High         | BR-009     |
| TC-STR-006 | Unknown product, default=conservative | Unit     | High         | BR-009     |
| TC-STR-007 | Case-insensitive matching ("mortgage")| Unit     | Medium       | BR-009     |
| TC-STR-008 | Null product type → StandardStrategy | Unit     | High         | BR-009     |
| TC-STR-009 | SCORING_STRATEGY property set         | Unit     | High         | BR-009     |

### 4.3 Module: BureauRequestBuilder (COMP-007)

| **TC ID** | **Test Name**                          | **Type** | **Priority** | **Covers** |
|-----------|----------------------------------------|----------|--------------|------------|
| TC-BRB-001 | Valid request → BureauInquiryRequest | Unit     | Critical     | —          |
| TC-BRB-002 | Subscriber code from config          | Unit     | High         | —          |
| TC-BRB-003 | Subscriber name from config          | Unit     | High         | —          |
| TC-BRB-004 | RequestId prefix + UUID              | Unit     | High         | —          |
| TC-BRB-005 | Subject fields mapped correctly      | Unit     | Critical     | —          |
| TC-BRB-006 | Timestamp format correct             | Unit     | Medium       | —          |
| TC-BRB-007 | Operation headers set (inquire)      | Unit     | High         | —          |
| TC-BRB-008 | BUREAU_REQUEST_ID property set       | Unit     | Medium       | —          |

### 4.4 Module: BureauResponseMapper (COMP-008)

| **TC ID** | **Test Name**                          | **Type** | **Priority** | **Covers** |
|-----------|----------------------------------------|----------|--------------|------------|
| TC-BRM-001 | Null response → BUREAU_ERROR, CR-301 | Unit     | Critical     | BR-012     |
| TC-BRM-002 | Error code in response → CR-302      | Unit     | Critical     | BR-013     |
| TC-BRM-003 | Normal response → BUREAU_ERROR=false | Unit     | Critical     | —          |
| TC-BRM-004 | Score ≥ 800 → EXCEPTIONAL            | Unit     | High         | BR-010     |
| TC-BRM-005 | Score 740-799 → VERY_GOOD            | Unit     | High         | BR-010     |
| TC-BRM-006 | Score 670-739 → GOOD                 | Unit     | High         | BR-010     |
| TC-BRM-007 | Score 580-669 → FAIR                 | Unit     | High         | BR-010     |
| TC-BRM-008 | Score < 580 → POOR                   | Unit     | High         | BR-010     |
| TC-BRM-009 | Utilization: balance/limit           | Unit     | High         | BR-011     |
| TC-BRM-010 | Utilization: zero limit → 0.0        | Unit     | High         | BR-011     |
| TC-BRM-011 | Utilization: null limit → 0.0        | Unit     | Medium       | BR-011     |

### 4.5 Module: RiskScoreCalculator (COMP-009)

| **TC ID** | **Test Name**                                    | **Type** | **Priority** | **Covers**   |
|-----------|--------------------------------------------------|----------|--------------|--------------|
| TC-RSC-001 | Bureau error → W215E status code               | Unit     | Critical     | BR-014       |
| TC-RSC-002 | Bureau error → INDETERMINATE risk category      | Unit     | Critical     | BR-014       |
| TC-RSC-003 | Bureau error → overallScore = 0                 | Unit     | Critical     | BR-014       |
| TC-RSC-004 | Bureau error → REFER_TO_UNDERWRITER             | Unit     | Critical     | BR-014       |
| TC-RSC-005 | Bureau error → accuracyCode = LM                | Unit     | High         | BR-014       |
| TC-RSC-006 | Income verification → SELF_REPORTED             | Unit     | High         | BR-017       |
| TC-RSC-007 | Verified income = reported income               | Unit     | High         | BR-017       |
| TC-RSC-008 | Monthly debt = totalBalance × 0.03              | Unit     | Critical     | BR-015       |
| TC-RSC-009 | DTI calculation: debt / (income/12)             | Unit     | Critical     | BR-016       |
| TC-RSC-010 | DTI: zero income → 1.0                          | Unit     | High         | BR-016       |
| TC-RSC-011 | Employment: FULL_TIME → LOW                     | Unit     | High         | BR-018       |
| TC-RSC-012 | Employment: PART_TIME → MODERATE                | Unit     | Medium       | BR-018       |
| TC-RSC-013 | Employment: RETIRED → MODERATE                  | Unit     | Medium       | BR-018       |
| TC-RSC-014 | Employment: SELF_EMPLOYED → MODERATE_HIGH       | Unit     | Medium       | BR-018       |
| TC-RSC-015 | Employment: CONTRACT → MODERATE_HIGH            | Unit     | Medium       | BR-018       |
| TC-RSC-016 | Employment: UNEMPLOYED → HIGH                   | Unit     | Medium       | BR-018       |
| TC-RSC-017 | Employment: null → HIGH                         | Unit     | Medium       | BR-018       |
| TC-RSC-018 | Request monthly = amount × 0.006                | Unit     | High         | BR-019       |
| TC-RSC-019 | TDSR calculation                                | Unit     | Critical     | BR-020       |
| TC-RSC-020 | Risk factor: score < 650 → LOW_CREDIT_SCORE     | Unit     | High         | BR-036       |
| TC-RSC-021 | Risk factor: util > 0.50 → HIGH_UTILIZATION     | Unit     | High         | BR-037       |
| TC-RSC-022 | Risk factor: delinquency > 0 → PAST_DELINQ     | Unit     | High         | BR-038       |
| TC-RSC-023 | Risk factor: inquiry > 5 → EXCESSIVE_INQUIRIES  | Unit     | High         | BR-039       |
| TC-RSC-024 | Risk factor: DTI > 0.40 → HIGH_DTI             | Unit     | High         | BR-040       |
| TC-RSC-025 | Risk factor: UNEMPLOYED → NO_EMPLOYMENT         | Unit     | High         | BR-041       |
| TC-RSC-026 | Risk factor: none triggered → empty list        | Unit     | Medium       | BR-036..041  |
| TC-RSC-027 | Multiple risk factors simultaneously            | Unit     | High         | BR-036..041  |
| TC-RSC-028 | Success → accuracyCode = FD                     | Unit     | Medium       | —            |

### 4.6 Module: StandardScoringStrategy

| **TC ID** | **Test Name**                                    | **Type** | **Priority** | **Covers** |
|-----------|--------------------------------------------------|----------|--------------|------------|
| TC-SSS-001 | Risk: score≥750, DTI<0.30, util<0.30 → EXCELLENT | Unit    | Critical     | BR-021     |
| TC-SSS-002 | Risk: score=750, DTI=0.30 → GOOD (not EXCELLENT) | Unit    | Critical     | BR-021     |
| TC-SSS-003 | Risk: score≥680 → GOOD                           | Unit    | High         | BR-021     |
| TC-SSS-004 | Risk: score≥620 → FAIR                            | Unit    | High         | BR-021     |
| TC-SSS-005 | Risk: score≥560 → POOR                            | Unit    | High         | BR-021     |
| TC-SSS-006 | Risk: score<560 → VERY_POOR                       | Unit    | High         | BR-021     |
| TC-SSS-007 | Overall score: weights 40/30/20/10                | Unit    | Critical     | BR-022     |
| TC-SSS-008 | Overall score: min=0, max=100 bounds              | Unit    | High         | BR-022     |
| TC-SSS-009 | Employment: FULL_TIME=100                         | Unit    | Medium       | BR-023     |
| TC-SSS-010 | Employment: UNEMPLOYED=10                         | Unit    | Medium       | BR-023     |
| TC-SSS-011 | Employment: null=50                               | Unit    | Medium       | BR-023     |
| TC-SSS-012 | Recommend: EXCELLENT → APPROVE                    | Unit    | Critical     | BR-024     |
| TC-SSS-013 | Recommend: GOOD, ratio≤5.0 → APPROVE              | Unit    | Critical     | BR-024     |
| TC-SSS-014 | Recommend: GOOD, ratio>5.0 → APPROVE_WITH_COND    | Unit    | Critical     | BR-024     |
| TC-SSS-015 | Recommend: FAIR, ratio≤3.0 → APPROVE_WITH_COND    | Unit    | High         | BR-024     |
| TC-SSS-016 | Recommend: FAIR, ratio>3.0 → REFER_TO_UNDERWRITER | Unit    | High         | BR-024     |
| TC-SSS-017 | Recommend: POOR → REFER_TO_UNDERWRITER             | Unit    | High         | BR-024     |
| TC-SSS-018 | Recommend: VERY_POOR → DECLINE                     | Unit    | Critical     | BR-024     |
| TC-SSS-019 | Afford: DSR<0.28 → COMFORTABLE                    | Unit    | High         | BR-025     |
| TC-SSS-020 | Afford: DSR<0.36 → MANAGEABLE                     | Unit    | Medium       | BR-025     |
| TC-SSS-021 | Afford: DSR<0.44 → STRETCHED                      | Unit    | Medium       | BR-025     |
| TC-SSS-022 | Afford: DSR≥0.44 → OVEREXTENDED                   | Unit    | Medium       | BR-025     |

### 4.7 Module: ConservativeScoringStrategy

| **TC ID** | **Test Name**                                    | **Type** | **Priority** | **Covers** |
|-----------|--------------------------------------------------|----------|--------------|------------|
| TC-CSS-001 | Risk: score≥780, DTI<0.25, util<0.25 → EXCELLENT | Unit    | Critical     | BR-026     |
| TC-CSS-002 | Risk: score≥720, DTI<0.35 → GOOD                | Unit    | High         | BR-026     |
| TC-CSS-003 | Risk: score≥660 → FAIR                           | Unit    | High         | BR-026     |
| TC-CSS-004 | Risk: score≥600 → POOR                           | Unit    | High         | BR-026     |
| TC-CSS-005 | Risk: score<600 → VERY_POOR                      | Unit    | High         | BR-026     |
| TC-CSS-006 | Overall score: weights 35/35/15/15               | Unit    | Critical     | BR-027     |
| TC-CSS-007 | DTI multiplier = 250 (stricter)                  | Unit    | High         | BR-027     |
| TC-CSS-008 | Util multiplier = 200 (stricter)                 | Unit    | High         | BR-027     |
| TC-CSS-009 | Employment: UNEMPLOYED=0                         | Unit    | Medium       | BR-028     |
| TC-CSS-010 | Employment: STUDENT=20                           | Unit    | Medium       | BR-028     |
| TC-CSS-011 | Recommend: EXCELLENT, ratio≤5 → APPROVE          | Unit    | Critical     | BR-029     |
| TC-CSS-012 | Recommend: EXCELLENT, ratio>5 → APPROVE_W_COND   | Unit    | Critical     | BR-029     |
| TC-CSS-013 | Recommend: GOOD, ratio≤4 → APPROVE_W_COND        | Unit    | High         | BR-029     |
| TC-CSS-014 | Recommend: GOOD, ratio>4 → REFER                 | Unit    | High         | BR-029     |
| TC-CSS-015 | Recommend: FAIR → REFER_TO_UNDERWRITER           | Unit    | High         | BR-029     |
| TC-CSS-016 | Recommend: POOR → DECLINE                        | Unit    | Critical     | BR-029     |
| TC-CSS-017 | Recommend: VERY_POOR → DECLINE                   | Unit    | Critical     | BR-029     |
| TC-CSS-018 | Afford: DSR<0.25 → COMFORTABLE                  | Unit    | High         | BR-030     |
| TC-CSS-019 | Afford: DSR<0.32 → MANAGEABLE                   | Unit    | Medium       | BR-030     |
| TC-CSS-020 | Afford: DSR<0.40 → STRETCHED                    | Unit    | Medium       | BR-030     |
| TC-CSS-021 | Afford: DSR≥0.40 → OVEREXTENDED                 | Unit    | Medium       | BR-030     |

### 4.8 Module: AggressiveScoringStrategy

| **TC ID** | **Test Name**                                    | **Type** | **Priority** | **Covers** |
|-----------|--------------------------------------------------|----------|--------------|------------|
| TC-ASS-001 | Risk: score≥720, DTI<0.40 → EXCELLENT           | Unit    | Critical     | BR-031     |
| TC-ASS-002 | Risk: score≥650 → GOOD                           | Unit    | High         | BR-031     |
| TC-ASS-003 | Risk: score≥580 → FAIR                           | Unit    | High         | BR-031     |
| TC-ASS-004 | Risk: score≥520 → POOR                           | Unit    | High         | BR-031     |
| TC-ASS-005 | Risk: score<520 → VERY_POOR                      | Unit    | High         | BR-031     |
| TC-ASS-006 | Overall score: weights 50/25/15/10               | Unit    | Critical     | BR-032     |
| TC-ASS-007 | DTI multiplier = 150 (relaxed)                   | Unit    | High         | BR-032     |
| TC-ASS-008 | Util multiplier = 120 (relaxed)                  | Unit    | High         | BR-032     |
| TC-ASS-009 | Employment: UNEMPLOYED=20                        | Unit    | Medium       | BR-033     |
| TC-ASS-010 | Employment: STUDENT=50                           | Unit    | Medium       | BR-033     |
| TC-ASS-011 | Recommend: EXCELLENT → APPROVE                   | Unit    | Critical     | BR-034     |
| TC-ASS-012 | Recommend: GOOD → APPROVE                        | Unit    | Critical     | BR-034     |
| TC-ASS-013 | Recommend: FAIR → APPROVE_WITH_CONDITIONS        | Unit    | High         | BR-034     |
| TC-ASS-014 | Recommend: POOR → REFER_TO_UNDERWRITER           | Unit    | High         | BR-034     |
| TC-ASS-015 | Recommend: VERY_POOR → DECLINE                   | Unit    | Critical     | BR-034     |
| TC-ASS-016 | Afford: DSR<0.35 → COMFORTABLE                  | Unit    | High         | BR-035     |
| TC-ASS-017 | Afford: DSR<0.45 → MANAGEABLE                   | Unit    | Medium       | BR-035     |
| TC-ASS-018 | Afford: DSR<0.55 → STRETCHED                    | Unit    | Medium       | BR-035     |
| TC-ASS-019 | Afford: DSR≥0.55 → OVEREXTENDED                 | Unit    | Medium       | BR-035     |

### 4.9 Module: RestResponseBuilder (COMP-010)

| **TC ID** | **Test Name**                          | **Type** | **Priority** | **Covers** |
|-----------|----------------------------------------|----------|--------------|------------|
| TC-RRB-001 | Response body set correctly          | Unit     | High         | —          |
| TC-RRB-002 | Content-Type = application/xml       | Unit     | High         | —          |
| TC-RRB-003 | HTTP status = 200                    | Unit     | High         | —          |
| TC-RRB-004 | Null response handling               | Unit     | Medium       | —          |

### 4.10 Module: GatewayRequestPreProcessor (COMP-004)

| **TC ID** | **Test Name**                          | **Type** | **Priority** | **Covers** |
|-----------|----------------------------------------|----------|--------------|------------|
| TC-GWP-001 | Null header → create with GATEWAY    | Unit     | Critical     | BR-043     |
| TC-GWP-002 | Null header → UUID transactionId     | Unit     | High         | BR-043     |
| TC-GWP-003 | Null header → timestamp set          | Unit     | High         | BR-043     |
| TC-GWP-004 | Null channel → set to API            | Unit     | High         | BR-043     |
| TC-GWP-005 | Existing header → unchanged          | Unit     | High         | BR-043     |
| TC-GWP-006 | Existing channel → unchanged         | Unit     | Medium       | BR-043     |

### 4.11 Module: ErrorProcessor (COMP-013)

| **TC ID** | **Test Name**                          | **Type** | **Priority** | **Covers** |
|-----------|----------------------------------------|----------|--------------|------------|
| TC-ERR-001 | ValidationException → 400, errorCode | Unit     | Critical     | BR-044     |
| TC-ERR-002 | ValidationException → correct message | Unit     | High         | BR-044     |
| TC-ERR-003 | Other exception → 500, CR-500        | Unit     | Critical     | BR-045     |
| TC-ERR-004 | Other exception → "Internal error"   | Unit     | High         | BR-045     |
| TC-ERR-005 | Error response has timestamp         | Unit     | Medium       | —          |
| TC-ERR-006 | Error response has transactionId     | Unit     | Medium       | —          |

### 4.12 Module: TransactionLogger (COMP-011)

| **TC ID** | **Test Name**                          | **Type** | **Priority** | **Covers** |
|-----------|----------------------------------------|----------|--------------|------------|
| TC-LOG-001 | Success → document with status=success | Unit   | High         | —          |
| TC-LOG-002 | Error → document with status=error   | Unit     | High         | —          |
| TC-LOG-003 | All exchange props mapped to document | Unit     | High         | —          |
| TC-LOG-004 | Timestamp set to current time        | Unit     | Medium       | —          |
| TC-LOG-005 | MongoDB insert called                | Unit     | High         | —          |
| TC-LOG-006 | MongoDB exception → logged, not thrown| Unit     | Critical     | —          |

### 4.13 Module: Model Classes (10 classes)

| **TC ID** | **Test Name**                          | **Type** | **Priority** | **Covers** |
|-----------|----------------------------------------|----------|--------------|------------|
| TC-MOD-001 | CreditRiskReqType getters/setters    | Unit     | Low          | —          |
| TC-MOD-002 | CreditRiskResType getters/setters    | Unit     | Low          | —          |
| TC-MOD-003 | CreditRiskResType riskFactors list   | Unit     | Low          | —          |
| TC-MOD-004 | ProvinceType.fromValue() — valid     | Unit     | Medium       | —          |
| TC-MOD-005 | ProvinceType.fromValue() — invalid   | Unit     | Medium       | —          |
| TC-MOD-006 | RequestChannelType.fromValue() valid | Unit     | Medium       | —          |
| TC-MOD-007 | XML marshaling CreditRiskReqType     | Unit     | Medium       | —          |
| TC-MOD-008 | XML marshaling CreditRiskResType     | Unit     | Medium       | —          |

---

## 5. Negative / Error Test Cases

| **TC ID** | **Test Name**                           | **Error Condition**                   | **Expected Behavior**                       | **Priority** |
|-----------|-----------------------------------------|---------------------------------------|---------------------------------------------|--------------|
| TC-NEG-001 | All fields null                        | Completely empty request              | ValidationException CR-000 (null body)      | Critical     |
| TC-NEG-002 | All fields blank                       | Every string = ""                     | ValidationException CR-001 (first checked)  | High         |
| TC-NEG-003 | Bureau timeout                         | 30s SOAP timeout exceeded             | ErrorProcessor → CR-500, HTTP 500           | Critical     |
| TC-NEG-004 | Bureau returns null                    | BureauInquiryResponse = null          | W215E, INDETERMINATE, REFER_TO_UNDERWRITER  | Critical     |
| TC-NEG-005 | Bureau returns error code              | errorCode field populated             | W215E, INDETERMINATE, REFER_TO_UNDERWRITER  | Critical     |
| TC-NEG-006 | Zero annual income                     | annualIncome = 0                      | DTI = 1.0 (default worst case)              | High         |
| TC-NEG-007 | Negative annual income                 | annualIncome = -50000                 | Potential calculation anomaly               | High         |
| TC-NEG-008 | Very high bureau score (950)           | Bureau score beyond expected range    | Should clamp or handle gracefully           | Medium       |
| TC-NEG-009 | Very low bureau score (0)             | Bureau score at minimum               | VERY_POOR categorization                    | Medium       |
| TC-NEG-010 | MongoDB down                           | Connection refused                    | Silent failure in logger, request succeeds  | High         |
| TC-NEG-011 | Extremely large requestedAmount        | requestedAmount = 999999999           | Ratio calculation should not overflow       | Medium       |
| TC-NEG-012 | Special characters in names            | firstName = "O'Brien"                 | Should pass validation                      | Low          |

---

## 6. Boundary Value Test Cases

| **TC ID** | **Test Name**                    | **Field/Parameter** | **Boundary Value** | **Expected Result**   | **Priority** |
|-----------|----------------------------------|---------------------|--------------------|-----------------------|--------------|
| TC-BND-001 | Bureau score = 800 (EXCEPTIONAL) | bureauScore        | 800                | EXCEPTIONAL range     | High         |
| TC-BND-002 | Bureau score = 799 (VERY_GOOD)   | bureauScore        | 799                | VERY_GOOD range       | High         |
| TC-BND-003 | Bureau score = 740 (VERY_GOOD)   | bureauScore        | 740                | VERY_GOOD range       | High         |
| TC-BND-004 | Bureau score = 739 (GOOD)        | bureauScore        | 739                | GOOD range            | High         |
| TC-BND-005 | Bureau score = 670 (GOOD)        | bureauScore        | 670                | GOOD range            | High         |
| TC-BND-006 | Bureau score = 669 (FAIR)        | bureauScore        | 669                | FAIR range            | High         |
| TC-BND-007 | Bureau score = 580 (FAIR)        | bureauScore        | 580                | FAIR range            | High         |
| TC-BND-008 | Bureau score = 579 (POOR)        | bureauScore        | 579                | POOR range            | High         |
| TC-BND-009 | Standard risk: score=750, DTI=0.29 | bureauScore, DTI | 750, 0.29          | EXCELLENT             | Critical     |
| TC-BND-010 | Standard risk: score=750, DTI=0.30 | bureauScore, DTI | 750, 0.30          | GOOD (not EXCELLENT)  | Critical     |
| TC-BND-011 | Utilization = 0.50 (threshold)   | utilizationRate    | 0.50               | No HIGH_UTILIZATION flag | High       |
| TC-BND-012 | Utilization = 0.51 (over)        | utilizationRate    | 0.51               | HIGH_UTILIZATION flag | High         |
| TC-BND-013 | Bureau score = 650 (threshold)   | bureauScore        | 650                | No LOW_CREDIT_SCORE   | High         |
| TC-BND-014 | Bureau score = 649 (under)       | bureauScore        | 649                | LOW_CREDIT_SCORE flag | High         |
| TC-BND-015 | Inquiry count = 5 (threshold)    | inquiryCount       | 5                  | No EXCESSIVE_INQUIRIES | Medium      |
| TC-BND-016 | Inquiry count = 6 (over)         | inquiryCount       | 6                  | EXCESSIVE_INQUIRIES   | Medium       |
| TC-BND-017 | DTI = 0.40 (threshold)           | debtToIncomeRatio  | 0.40               | No HIGH_DTI flag      | High         |
| TC-BND-018 | DTI = 0.41 (over)               | debtToIncomeRatio  | 0.41               | HIGH_DTI flag         | High         |
| TC-BND-019 | Standard afford: DSR=0.279       | debtServiceRatio   | 0.279              | COMFORTABLE           | Medium       |
| TC-BND-020 | Standard afford: DSR=0.28        | debtServiceRatio   | 0.28               | MANAGEABLE            | Medium       |
| TC-BND-021 | Amount/income ratio = 5.0 exactly| ratio              | 5.0                | GOOD → APPROVE (≤5)   | High         |
| TC-BND-022 | Amount/income ratio = 5.01       | ratio              | 5.01               | GOOD → APPROVE_W_COND | High         |

---

## 7. Integration Test Cases

| **TC ID** | **Test Name**                         | **Systems/Components**                 | **Integration Point**    | **Expected Behavior**                  | **Priority** |
|-----------|---------------------------------------|----------------------------------------|--------------------------|----------------------------------------|--------------|
| TC-INT-001 | REST full pipeline — happy path      | CXF REST → All processors → Logger     | REST endpoint            | 200 OK + valid CreditRiskResType       | Critical     |
| TC-INT-002 | SOAP full pipeline — happy path      | CXF SOAP → WSS4J → Processors → Logger | SOAP endpoint            | SOAP response + valid assessment       | Critical     |
| TC-INT-003 | Gateway full pipeline — happy path   | CXF GW → PreProc → Processors → Logger | Gateway endpoint         | SOAP response + enriched headers       | Critical     |
| TC-INT-004 | REST → bureau integration            | REST route → Bureau SOAP call          | CXF → external SOAP      | BureauInquiryResponse received         | Critical     |
| TC-INT-005 | Wire tap → MongoDB                   | Any route → TransactionLogger → Mongo  | Wire tap → MongoDB insert | Document inserted in transactions      | High         |
| TC-INT-006 | REST validation error → error response | REST → Validator → ErrorProcessor    | Validation error path    | 400 + error XML                        | High         |
| TC-INT-007 | SOAP validation error → SOAP fault   | SOAP → Validator → ErrorProcessor      | Validation error path    | SOAP fault with error details          | High         |
| TC-INT-008 | Bureau failure → indeterminate outcome | Route → Bureau (mocked fail) → Calculator | Bureau error path   | W215E + INDETERMINATE                  | Critical     |
| TC-INT-009 | WSS4J security — valid credentials   | SOAP client → WSS4J interceptor        | Security layer           | Request proceeds to route              | High         |
| TC-INT-010 | WSS4J security — invalid credentials | SOAP client → WSS4J interceptor        | Security layer           | SOAP fault (unauthorized)              | High         |
| TC-INT-011 | GW pre-processor header enrichment   | GW route → GatewayRequestPreProcessor  | Gateway entry            | Headers populated before validation    | High         |
| TC-INT-012 | Concurrent request handling          | Multiple REST requests simultaneously  | Thread safety            | Each request gets independent exchange | Medium       |

---

## 8. Performance Test Cases

| **TC ID** | **Test Name**                        | **Scenario**                  | **Target Metric**      | **Acceptance Criteria**            |
|-----------|--------------------------------------|-------------------------------|------------------------|------------------------------------|
| TC-PRF-001 | REST endpoint response time         | Single request, bureau mocked | Response time          | < 500ms for 95th percentile       |
| TC-PRF-002 | SOAP endpoint response time         | Single request, bureau mocked | Response time          | < 500ms for 95th percentile       |
| TC-PRF-003 | Load test — 50 concurrent users     | 50 parallel REST requests     | Throughput             | > 50 requests/sec                  |
| TC-PRF-004 | Load test — 200 concurrent users    | 200 parallel REST requests    | Response time          | < 2s for 95th percentile          |
| TC-PRF-005 | Bureau timeout impact               | Bureau delays 25-30s          | Queue behavior         | Other requests not blocked         |
| TC-PRF-006 | MongoDB logging impact              | MongoDB slow (5s insert)      | REST response time     | Logging doesn't affect response    |

---

## 9. Security Test Cases

| **TC ID** | **Test Name**                      | **Vulnerability Type**         | **Test Method**                   | **Expected Result**               |
|-----------|------------------------------------|--------------------------------|-----------------------------------|------------------------------------|
| TC-SEC-001 | XML injection via REST params     | Injection                      | XML entities in query parameters  | Input sanitized/rejected          |
| TC-SEC-002 | SOAP XML bomb (billion laughs)    | DoS                            | Nested entity expansion in SOAP   | Parser limits prevent expansion   |
| TC-SEC-003 | WSS4J replay attack               | Broken Authentication          | Replay captured WS-Security header| Token rejected (if nonce/timestamp)|
| TC-SEC-004 | REST endpoint without auth        | Missing Authentication         | Access REST without credentials   | Currently allowed (GAP flagged)   |
| TC-SEC-005 | Gateway endpoint without auth     | Missing Authentication         | Access GW without credentials     | Currently allowed (GAP flagged)   |
| TC-SEC-006 | PII in logs verification          | Information Disclosure         | Check log output for SIN/DOB      | PII should be masked              |
| TC-SEC-007 | PII in MongoDB verification       | Information Disclosure         | Check MongoDB documents           | Sensitive fields should be encrypted |
| TC-SEC-008 | MongoDB injection attempt         | NoSQL Injection                | Special chars in applicantId      | Input sanitized by BSON builder   |
| TC-SEC-009 | TLS enforcement on bureau call    | Insecure Transport             | Verify HTTPS used for bureau      | TLS 1.2+ enforced                 |
| TC-SEC-010 | Large payload attack              | DoS                            | Send oversized request body       | Request size limited              |

---

## 10. Test Environment Requirements

| **Environment** | **Purpose**          | **Configuration**                              | **Test Data**                |
|-----------------|----------------------|------------------------------------------------|------------------------------|
| Local Dev       | Unit Tests           | JUnit 4/5 + Mockito; Camel Test Kit           | Mock objects, test fixtures  |
| CI Pipeline     | Unit + Integration   | Maven + Surefire/Failsafe; embedded Karaf      | Seeded test data, mock bureau|
| QA Staging      | E2E / Security       | Full JBoss Fuse 6.3 deployment; mock bureau    | Production-like test data    |
| Performance     | Load / Stress        | JBoss Fuse; JMeter; mock bureau with delays    | Parameterized request sets   |

---

## 11. Test Data Requirements

| **Test Data Set**          | **Purpose**                 | **Entities**                | **Record Count** | **Refresh** |
|----------------------------|-----------------------------|------------------------------|------------------|-------------|
| Valid applicant profiles   | Happy path testing          | CreditRiskReqType            | 20               | Per run     |
| Invalid inputs             | Negative testing            | CreditRiskReqType (invalid)  | 15               | Per run     |
| Bureau mocked responses    | Bureau response mapping     | BureauInquiryResponse        | 10               | Per run     |
| Boundary score values      | Boundary testing            | Bureau scores: 300-900       | 12               | Static      |
| Strategy test data         | Strategy comparison testing | Multiple product types       | 12               | Static      |

---

## 12. Traceability Matrix

| **Business Rule** | **TC-VAL** | **TC-STR** | **TC-BRB** | **TC-BRM** | **TC-RSC** | **TC-SSS** | **TC-CSS** | **TC-ASS** | **TC-INT** |
|-------------------|------------|------------|------------|------------|------------|------------|------------|------------|------------|
| BR-001            | 001-003    |            |            |            |            |            |            |            |            |
| BR-002            | 004        |            |            |            |            |            |            |            |            |
| BR-003            | 005        |            |            |            |            |            |            |            |            |
| BR-004            | 006-008    |            |            |            |            |            |            |            |            |
| BR-005            | 009-011    |            |            |            |            |            |            |            |            |
| BR-006            | 012-014    |            |            |            |            |            |            |            |            |
| BR-007            | 015-018    |            |            |            |            |            |            |            |            |
| BR-008            | 019        |            |            |            |            |            |            |            |            |
| BR-009            |            | 001-009    |            |            |            |            |            |            |            |
| BR-010            |            |            |            | 004-008    |            |            |            |            |            |
| BR-011            |            |            |            | 009-011    |            |            |            |            |            |
| BR-012            |            |            |            | 001        |            |            |            |            |            |
| BR-013            |            |            |            | 002        |            |            |            |            |            |
| BR-014            |            |            |            |            | 001-005    |            |            |            | 008        |
| BR-015            |            |            |            |            | 008        |            |            |            |            |
| BR-016            |            |            |            |            | 009-010    |            |            |            |            |
| BR-017            |            |            |            |            | 006-007    |            |            |            |            |
| BR-018            |            |            |            |            | 011-017    |            |            |            |            |
| BR-019            |            |            |            |            | 018        |            |            |            |            |
| BR-020            |            |            |            |            | 019        |            |            |            |            |
| BR-021            |            |            |            |            |            | 001-006    |            |            |            |
| BR-022            |            |            |            |            |            | 007-008    |            |            |            |
| BR-023            |            |            |            |            |            | 009-011    |            |            |            |
| BR-024            |            |            |            |            |            | 012-018    |            |            |            |
| BR-025            |            |            |            |            |            | 019-022    |            |            |            |
| BR-026            |            |            |            |            |            |            | 001-005    |            |            |
| BR-027            |            |            |            |            |            |            | 006-008    |            |            |
| BR-028            |            |            |            |            |            |            | 009-010    |            |            |
| BR-029            |            |            |            |            |            |            | 011-017    |            |            |
| BR-030            |            |            |            |            |            |            | 018-021    |            |            |
| BR-031            |            |            |            |            |            |            |            | 001-005    |            |
| BR-032            |            |            |            |            |            |            |            | 006-008    |            |
| BR-033            |            |            |            |            |            |            |            | 009-010    |            |
| BR-034            |            |            |            |            |            |            |            | 011-015    |            |
| BR-035            |            |            |            |            |            |            |            | 016-019    |            |
| BR-036..041       |            |            |            |            | 020-027    |            |            |            |            |
| BR-043            |            |            |            |            |            |            |            |            | 011        |
| BR-044            |            |            |            |            |            |            |            |            | 006-007    |
| BR-045            |            |            |            |            |            |            |            |            |            |

---

## 13. Gaps & Risks

### 13.1 Uncovered Areas

| **Area**                       | **Description**                                      | **Risk**   | **Recommended Action**                  |
|--------------------------------|------------------------------------------------------|------------|-----------------------------------------|
| Income verification logic      | No test for the fake "SELF_REPORTED" status          | High       | Create TC once real verification added  |
| Province-specific scoring      | Province captured but never used in scoring           | High       | Add tests once province rules added     |
| LTV calculation                | No collateral assessment exists                       | High       | Add tests once LTV rules implemented    |
| Batch processing               | BATCH channel type exists but no batch endpoint       | Medium     | Add tests if batch support is built     |
| Concurrent bureau calls        | Thread safety of scoring processor unclear            | Medium     | Add concurrency tests                   |

### 13.2 Test Risks

| **#** | **Risk**                                          | **Impact** | **Mitigation**                                    |
|-------|---------------------------------------------------|------------|---------------------------------------------------|
| 1     | No existing tests → no regression baseline        | High       | Create full test suite before migration           |
| 2     | Bureau mock may not match real bureau behavior    | High       | Contract testing with real bureau in QA           |
| 3     | MongoDB mock may not cover all failure modes      | Medium     | Use embedded MongoDB for integration tests        |
| 4     | Strategy threshold changes require test updates   | Medium     | Data-driven tests with external parameters        |
| 5     | Camel route testing requires Camel Test Kit       | Medium     | Ensure JBoss Fuse testing dependencies available  |

---

## 14. Migration-Specific Test Cases

### 14.1 Field-to-Field Mapping Test Cases

| **TC ID**   | **Test Name**                                    | **Source Field**          | **Target Field**        | **Mapping Type** | **Validation**                                  | **Priority** | **F2F Ref** |
|-------------|--------------------------------------------------|---------------------------|-------------------------|------------------|-------------------------------------------------|--------------|-------------|
| MIG-F2F-001 | applicantId direct mapping                       | req.applicantId           | res.applicantId         | 🟢 Direct        | Source value equals target value                | High         | Sec 5.1     |
| MIG-F2F-002 | bureauScore → creditScoreDetail.bureauScore      | bureauResp.creditScore    | creditScoreDetail.bureauScore | 🟢 Direct   | Score value preserved                           | Critical     | Sec 5.2     |
| MIG-F2F-003 | bureauScore → scoreRange transform               | bureauResp.creditScore    | creditScoreDetail.scoreRange  | 🟡 Transform | 800→EXCEPTIONAL, 740→VERY_GOOD, etc.          | Critical     | Sec 5.2     |
| MIG-F2F-004 | totalBalance/totalCreditLimit → utilizationRate  | two fields                | one calculated field    | 🟡 Transform     | Ratio calculated correctly                      | High         | Sec 5.2     |
| MIG-F2F-005 | annualIncome → reportedIncome                    | req.annualIncome          | income.reportedIncome   | 🟢 Direct        | Value preserved                                 | High         | Sec 5.3     |
| MIG-F2F-006 | annualIncome → verifiedIncome                    | req.annualIncome          | income.verifiedIncome   | 🟢 Direct        | Same as reported (HARDCODED gap)                | High         | Sec 5.3     |
| MIG-F2F-007 | employmentStatus → riskLevel transform           | req.employmentStatus      | employment.riskLevel    | 🟡 Transform     | FULL_TIME→LOW, etc.                            | High         | Sec 5.3     |
| MIG-F2F-008 | strategy.categorizeRisk → riskCategory           | 3 inputs                  | response.riskCategory   | 🟡 Transform     | Correct category per strategy rules             | Critical     | Sec 5.4     |
| MIG-F2F-009 | strategy.calculateOverallScore → overallScore    | 4 inputs + weights        | response.overallScore   | 🟡 Transform     | Score 0-100 per strategy weights                | Critical     | Sec 5.4     |
| MIG-F2F-010 | strategy.determineRecommendation → recommendation| riskCategory + ratio      | response.recommendation | 🟡 Transform     | Correct recommendation per strategy             | Critical     | Sec 5.4     |
| MIG-F2F-011 | Request fields → BureauSubject fields            | multiple request fields   | bureau subject fields   | 🟢 Direct        | firstName, lastName, DOB, SIN mapped            | High         | Sec 5.5     |
| MIG-F2F-012 | Config → BureauSubscriber fields                 | config properties         | subscriber fields       | 🟢 Direct        | BUREAU_SUBSCRIBER_CODE/NAME preserved           | Medium       | Sec 5.5     |
| MIG-F2F-013 | Exchange props → MongoDB document fields         | exchange properties       | BSON document fields    | 🟢 Direct        | All 6 logging properties mapped                 | Medium       | Sec 5.6     |
| MIG-F2F-014 | riskCategory → MongoDB document                  | response.riskCategory     | document.riskCategory   | 🟢 Direct        | Value preserved in audit trail                  | Medium       | Sec 5.6     |
| MIG-F2F-015 | Error fields → MongoDB document                  | exception details         | document.errorCode/Msg  | 🟢 Direct        | Error details preserved in audit trail          | Medium       | Sec 5.6     |

### 14.2 Dead Code Removal Test Cases

| **TC ID**   | **Test Name**                                          | **Dead Code Reference**            | **Verification**                                              | **Priority** |
|-------------|--------------------------------------------------------|------------------------------------|---------------------------------------------------------------|--------------|
| MIG-DC-001  | CreditRiskRestSvc.assessCreditRisk() stub removed      | CreditRiskRestSvc.java             | All REST functionality works via Camel route                  | Medium       |
| MIG-DC-002  | CreditRiskSoapSvcImpl.assessCreditRisk() stub removed  | CreditRiskSoapSvcImpl.java         | All SOAP functionality works via Camel route                  | Medium       |
| MIG-DC-003  | Unused model fields removed (yearsEmployed)            | EmploymentRiskDetail.java          | No regression on employment risk assessment                   | Low          |
| MIG-DC-004  | Unused model fields removed (industryCategory)         | EmploymentRiskDetail.java          | No regression on employment risk assessment                   | Low          |

### 14.3 Hardcoded Value Replacement Test Cases

| **TC ID**   | **Test Name**                                        | **Field**              | **Source Behavior (Hardcoded)** | **Target Behavior (Dynamic)**                           | **Priority** |
|-------------|------------------------------------------------------|------------------------|---------------------------------|---------------------------------------------------------|--------------|
| MIG-HC-001  | Monthly debt uses configurable factor                | monthlyDebt            | Always totalBalance × 0.03     | Configurable per product type or actual minimum payment  | Critical     |
| MIG-HC-002  | Request monthly uses amortization                    | requestMonthly         | Always amount × 0.006          | Calculated from term, rate, product type                | Critical     |
| MIG-HC-003  | Income verification is dynamic                       | verificationStatus     | Always "SELF_REPORTED"          | Actual verification workflow (external call)             | High         |
| MIG-HC-004  | Scoring model version is dynamic                     | scoringModelVersion    | strategyName + "-v1.0"         | Actual version from config/registry                     | Medium       |
| MIG-HC-005  | Bureau subscriber from vault                         | subscriberCode/Name    | From app_config.properties      | From secrets manager (Vault/AWS SM)                     | High         |
| MIG-HC-006  | Risk factor thresholds configurable                  | 650, 0.50, 5, 0.40    | Hardcoded in RiskScoreCalculator| From external config per strategy                       | Medium       |

### 14.4 Missing Validation Test Cases

| **TC ID**   | **Test Name**                                    | **Field**      | **Validation Added**                  | **Valid → Expected**    | **Invalid → Expected**              | **Priority** |
|-------------|--------------------------------------------------|----------------|---------------------------------------|-------------------------|-------------------------------------|--------------|
| MIG-VL-001  | DOB minimum age validation (18 years)            | dateOfBirth    | Age ≥ 18 years old                   | 200 OK                  | 400 + "Applicant must be 18+"       | Critical     |
| MIG-VL-002  | DOB not future date                              | dateOfBirth    | Date ≤ today                         | 200 OK                  | 400 + "DOB cannot be in the future" | High         |
| MIG-VL-003  | SIN checksum validation (Luhn)                   | SIN            | Luhn algorithm check                 | 200 OK                  | 400 + "Invalid SIN checksum"        | Medium       |
| MIG-VL-004  | Postal code matches province                     | postalCode     | Postal prefix = province             | 200 OK                  | 400 + "Postal code doesn't match"   | Medium       |
| MIG-VL-005  | Annual income must be non-negative               | annualIncome   | Value ≥ 0                            | 200 OK                  | 400 + "Invalid income value"        | High         |
| MIG-VL-006  | Requested amount must be positive                | requestedAmount| Value > 0                            | 200 OK                  | 400 + "Invalid requested amount"    | High         |

### 14.5 Source-Target Regression Test Cases

| **TC ID**   | **Test Name**                                    | **Scenario**                                   | **Required Result**                                    | **Priority** |
|-------------|--------------------------------------------------|------------------------------------------------|--------------------------------------------------------|--------------|
| MIG-REG-001 | Standard strategy EXCELLENT assessment           | Score=800, DTI=0.20, Util=0.15, FULL_TIME     | Same riskCategory, overallScore, recommendation        | Critical     |
| MIG-REG-002 | Standard strategy POOR assessment                | Score=550, DTI=0.45, Util=0.60, UNEMPLOYED    | Same riskCategory, overallScore, recommendation        | Critical     |
| MIG-REG-003 | Conservative strategy MORTGAGE assessment        | MORTGAGE product, Score=750, DTI=0.30          | Same outputs as source system                          | Critical     |
| MIG-REG-004 | Aggressive strategy CREDIT_CARD assessment       | CREDIT_CARD product, Score=700, DTI=0.35       | Same outputs as source system                          | Critical     |
| MIG-REG-005 | Bureau error → INDETERMINATE flow                | Bureau returns null                             | W215E, INDETERMINATE, score=0, REFER                  | Critical     |
| MIG-REG-006 | Validation error CR-001                          | Missing applicantId                             | Same error code and message as source                  | High         |
| MIG-REG-007 | Multiple risk factors identified                 | Low score + high DTI + high utilization         | Same risk factors list                                 | High         |
| MIG-REG-008 | Gateway header enrichment                        | Request without headers via gateway             | sourceSystem=GATEWAY, channel=API, UUID generated      | High         |
| MIG-REG-009 | REST response format                             | Valid REST request                              | Content-Type: application/xml, HTTP 200                | High         |
| MIG-REG-010 | SOAP response format                             | Valid SOAP request                              | Proper SOAP envelope with CreditRiskResType            | High         |
| MIG-REG-011 | Transaction logged to MongoDB                    | Successful assessment                           | Document in transactions with status=success           | Medium       |
| MIG-REG-012 | Error logged to MongoDB                          | Failed assessment                               | Document in transactions with status=error             | Medium       |

---

## 15. Appendices

### Appendix A: Test ID Naming Convention

| **Prefix** | **Domain**                        |
|------------|-----------------------------------|
| TC-VAL-    | Validation rules                  |
| TC-STR-    | Strategy selection                |
| TC-BRB-    | Bureau request builder            |
| TC-BRM-    | Bureau response mapper            |
| TC-RSC-    | Risk score calculator             |
| TC-SSS-    | Standard scoring strategy         |
| TC-CSS-    | Conservative scoring strategy     |
| TC-ASS-    | Aggressive scoring strategy       |
| TC-RRB-    | REST response builder             |
| TC-GWP-    | Gateway pre-processor             |
| TC-ERR-    | Error processor                   |
| TC-LOG-    | Transaction logger                |
| TC-MOD-    | Model classes                     |
| TC-NEG-    | Negative / error test cases       |
| TC-BND-    | Boundary value test cases         |
| TC-INT-    | Integration test cases            |
| TC-PRF-    | Performance test cases            |
| TC-SEC-    | Security test cases               |
| MIG-F2F-   | Migration field mapping tests     |
| MIG-DC-    | Migration dead code tests         |
| MIG-HC-    | Migration hardcoded value tests   |
| MIG-VL-    | Migration missing validation tests|
| MIG-REG-   | Migration regression tests        |

---

> **Document Control:**
> | Version | Date | Author | Changes |
> |---------|------|--------|---------|
> | 1.0 | 31-Mar-2026 | RE Automation (Local) | Initial draft — 198 test cases across 24 categories, 43 migration-specific tests |
