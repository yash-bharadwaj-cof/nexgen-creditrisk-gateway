# Business Rules Catalog

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

This catalog documents all business rules identified during reverse engineering of the nexgen-creditrisk-gateway application. The service implements a credit risk assessment pipeline using the Strategy Pattern, with three scoring strategies (Standard, Conservative, Aggressive) selected based on product type. Business rules govern validation, scoring, risk categorization, and recommendation generation.

### Rule Classification

| **Category**       | **Description**                                                    |
|--------------------|--------------------------------------------------------------------|
| **Validation**     | Input validation, data format checks, required field enforcement    |
| **Computation**    | Calculations, formulas, derived values                              |
| **Decision**       | Conditional logic, branching, routing decisions                     |
| **Authorization**  | Access control, permission checks, role-based rules                 |
| **Workflow**       | Process flow, state transitions, sequencing rules                   |
| **Constraint**     | Data integrity constraints, business limits, thresholds             |
| **Derivation**     | Rules that derive or infer data from other data                     |

---

## 2. Business Rules Summary

| **Category**       | **Count** | **Critical** | **Major** | **Minor** |
|--------------------|-----------|--------------|-----------|-----------|
| Validation         | 8         | 2            | 4         | 2         |
| Computation        | 12        | 4            | 6         | 2         |
| Decision           | 15        | 6            | 7         | 2         |
| Authorization      | 1         | 1            | 0         | 0         |
| Workflow           | 3         | 1            | 2         | 0         |
| Constraint         | 6         | 2            | 3         | 1         |
| Derivation         | 5         | 1            | 3         | 1         |
| **Total**          | **50**    | **17**       | **25**    | **8**     |

### 2.1 Rule Health Summary

| **Status**                            | **Count** | **Percentage** |
|---------------------------------------|-----------|----------------|
| ✅ Active & Validated by Client        | 0         | 0%             |
| 🟡 Active & Pending Client Validation | 42        | 84%            |
| ❌ Client Identified as Incorrect      | 0         | 0%             |
| ⚫ Dead Code Rule                      | 2         | 4%             |
| ⚪ Hardcoded / Bypassed Rule           | 6         | 12%            |

---

## 3. Business Rules Detail

### 3.1 BR-001: Applicant ID Required

| **Attribute**            | **Details**                                              |
|--------------------------|----------------------------------------------------------|
| **Rule ID**              | BR-001                                                   |
| **Rule Name**            | Applicant ID Required                                    |
| **Category**             | Validation                                               |
| **Severity**             | Critical                                                 |
| **Business Domain**      | Credit Risk Assessment — Request Validation              |
| **Description**          | Every credit risk assessment request must include a non-blank applicantId |
| **Source Location**      | `src/main/java/.../processor/CreditRiskRequestValidator.java` |
| **Source Line(s)**       | L42-L45                                                  |
| **Trigger/Condition**    | Request received at any endpoint                         |
| **Input(s)**             | `request.getApplicantId()`                               |
| **Output(s)/Action**     | Pass validation or throw ValidationException             |
| **Exception Handling**   | Throws `ValidationException("CR-001", "Applicant ID is required")` |
| **Client Validated**     | 🟡 Pending                                               |
| **Client Comments**      | —                                                        |
| **Dead Code Flag**       | No                                                       |
| **Hardcoded Flag**       | No                                                       |
| **Execution Priority**   | 1 (first validation executed)                            |

#### Rule Logic (Pseudocode)

```
IF applicantId IS NULL OR applicantId IS BLANK THEN
    THROW ValidationException("CR-001", "Applicant ID is required")
END IF
```

#### Rule Logic (Source Code Reference)

```java
// File: CreditRiskRequestValidator.java
// Method: process(Exchange)
// Lines: 42-45
if (request.getApplicantId() == null || request.getApplicantId().trim().isEmpty()) {
    throw new ValidationException("CR-001", "Applicant ID is required");
}
```

#### Traceability

| **Linked To**             | **Reference**          |
|---------------------------|------------------------|
| Test Case                 | TC-VAL-001, TC-VAL-002 |
| Component                 | COMP-005               |

---

### 3.2 BR-002: First Name Required

| **Attribute**            | **Details**                                              |
|--------------------------|----------------------------------------------------------|
| **Rule ID**              | BR-002                                                   |
| **Rule Name**            | First Name Required                                      |
| **Category**             | Validation                                               |
| **Severity**             | Major                                                    |
| **Business Domain**      | Credit Risk Assessment — Request Validation              |
| **Description**          | First name must be provided and non-blank                |
| **Source Location**      | `src/main/java/.../processor/CreditRiskRequestValidator.java` |
| **Source Line(s)**       | L47-L50                                                  |
| **Trigger/Condition**    | Request received at any endpoint                         |
| **Input(s)**             | `request.getFirstName()`                                 |
| **Output(s)/Action**     | Pass validation or throw ValidationException             |
| **Exception Handling**   | Throws `ValidationException("CR-002", "First name is required")` |
| **Client Validated**     | 🟡 Pending                                               |
| **Dead Code Flag**       | No                                                       |
| **Hardcoded Flag**       | No                                                       |
| **Execution Priority**   | 2                                                        |

#### Rule Logic (Pseudocode)

```
IF firstName IS NULL OR firstName IS BLANK THEN
    THROW ValidationException("CR-002", "First name is required")
END IF
```

---

### 3.3 BR-003: Last Name Required

| **Attribute**            | **Details**                                              |
|--------------------------|----------------------------------------------------------|
| **Rule ID**              | BR-003                                                   |
| **Rule Name**            | Last Name Required                                       |
| **Category**             | Validation                                               |
| **Severity**             | Major                                                    |
| **Source Location**      | `src/main/java/.../processor/CreditRiskRequestValidator.java` |
| **Source Line(s)**       | L52-L55                                                  |
| **Trigger/Condition**    | Request received                                         |
| **Input(s)**             | `request.getLastName()`                                  |
| **Output(s)/Action**     | Pass or throw ValidationException("CR-003")              |
| **Client Validated**     | 🟡 Pending                                               |
| **Dead Code Flag**       | No                                                       |
| **Hardcoded Flag**       | No                                                       |
| **Execution Priority**   | 3                                                        |

---

### 3.4 BR-004: Date of Birth Format Validation

| **Attribute**            | **Details**                                              |
|--------------------------|----------------------------------------------------------|
| **Rule ID**              | BR-004                                                   |
| **Rule Name**            | Date of Birth Format Validation                          |
| **Category**             | Validation                                               |
| **Severity**             | Major                                                    |
| **Source Location**      | `src/main/java/.../processor/CreditRiskRequestValidator.java` |
| **Source Line(s)**       | L57-L64                                                  |
| **Input(s)**             | `request.getDateOfBirth()`                               |
| **Output(s)/Action**     | Pass or throw ValidationException("CR-004")              |
| **Client Validated**     | 🟡 Pending                                               |
| **Dead Code Flag**       | No                                                       |
| **Hardcoded Flag**       | No                                                       |
| **Execution Priority**   | 4                                                        |

#### Rule Logic (Pseudocode)

```
IF dateOfBirth IS NULL OR dateOfBirth IS BLANK THEN
    THROW ValidationException("CR-004", "Date of birth is required")
END IF
IF dateOfBirth DOES NOT MATCH "^\d{4}-\d{2}-\d{2}$" THEN
    THROW ValidationException("CR-004", "Date of birth must be in YYYY-MM-DD format")
END IF
```

> ⚠️ **GAP**: No age reasonableness check (minimum 18 years, maximum 120 years). No future date check.

---

### 3.5 BR-005: SIN Format Validation

| **Attribute**            | **Details**                                              |
|--------------------------|----------------------------------------------------------|
| **Rule ID**              | BR-005                                                   |
| **Rule Name**            | Social Insurance Number Format Validation                |
| **Category**             | Validation                                               |
| **Severity**             | Critical                                                 |
| **Source Location**      | `src/main/java/.../processor/CreditRiskRequestValidator.java` |
| **Source Line(s)**       | L56-L60                                                  |
| **Input(s)**             | `request.getSocialInsuranceNumber()`                     |
| **Output(s)/Action**     | Pass or throw ValidationException("CR-005")              |
| **Client Validated**     | 🟡 Pending                                               |
| **Dead Code Flag**       | No                                                       |
| **Hardcoded Flag**       | No                                                       |
| **Execution Priority**   | 5                                                        |

#### Rule Logic (Pseudocode)

```
IF sin IS NULL OR sin IS BLANK THEN
    THROW ValidationException("CR-005", "Social Insurance Number is required")
END IF
IF sin DOES NOT MATCH "^\d{3}-?\d{3}-?\d{3}$" THEN
    THROW ValidationException("CR-005", "Invalid SIN format")
END IF
```

> ⚠️ **GAP**: No Luhn algorithm checksum validation for Canadian SIN.

---

### 3.6 BR-006: Province Validation

| **Attribute**            | **Details**                                              |
|--------------------------|----------------------------------------------------------|
| **Rule ID**              | BR-006                                                   |
| **Rule Name**            | Province Must Be Valid Canadian Province/Territory        |
| **Category**             | Validation                                               |
| **Severity**             | Major                                                    |
| **Source Location**      | `src/main/java/.../processor/CreditRiskRequestValidator.java` |
| **Source Line(s)**       | L66-L70                                                  |
| **Input(s)**             | `request.getProvince()`                                  |
| **Output(s)/Action**     | Pass or throw ValidationException("CR-006")              |
| **Client Validated**     | 🟡 Pending                                               |
| **Dead Code Flag**       | No                                                       |
| **Hardcoded Flag**       | No                                                       |
| **Execution Priority**   | 6                                                        |

#### Rule Logic (Pseudocode)

```
VALID_PROVINCES = {ON, QC, BC, AB, MB, SK, NS, NB, NL, PE, NT, YT, NU}
IF province NOT IN VALID_PROVINCES THEN
    THROW ValidationException("CR-006", "Invalid province")
END IF
```

> 📝 **NOTE**: The `SUPPORTED_PROVINCE_LIST` config only has `ON,BC,AB,QC` but validation accepts all 13 provinces/territories via the ProvinceType enum.

---

### 3.7 BR-007: Postal Code Format Validation

| **Attribute**            | **Details**                                              |
|--------------------------|----------------------------------------------------------|
| **Rule ID**              | BR-007                                                   |
| **Rule Name**            | Canadian Postal Code Format Validation                   |
| **Category**             | Validation                                               |
| **Severity**             | Minor                                                    |
| **Source Location**      | `src/main/java/.../processor/CreditRiskRequestValidator.java` |
| **Source Line(s)**       | L72-L75                                                  |
| **Input(s)**             | `request.getPostalCode()`                                |
| **Output(s)/Action**     | Pass (if null/blank skipped) or throw ValidationException("CR-007") |
| **Client Validated**     | 🟡 Pending                                               |
| **Dead Code Flag**       | No                                                       |
| **Hardcoded Flag**       | No                                                       |
| **Execution Priority**   | 7                                                        |

#### Rule Logic (Pseudocode)

```
IF postalCode IS NOT NULL AND postalCode IS NOT BLANK THEN
    IF postalCode DOES NOT MATCH "^[A-Za-z]\d[A-Za-z]\s?\d[A-Za-z]\d$" THEN
        THROW ValidationException("CR-007", "Invalid postal code format")
    END IF
END IF
```

> ⚠️ **GAP**: No check that postal code belongs to the stated province.

---

### 3.8 BR-008: Null Request Body Validation

| **Attribute**            | **Details**                                              |
|--------------------------|----------------------------------------------------------|
| **Rule ID**              | BR-008                                                   |
| **Rule Name**            | Request Body Must Not Be Null                            |
| **Category**             | Validation                                               |
| **Severity**             | Minor                                                    |
| **Source Location**      | `src/main/java/.../processor/CreditRiskRequestValidator.java` |
| **Source Line(s)**       | L38-L41                                                  |
| **Input(s)**             | Exchange body                                             |
| **Output(s)/Action**     | Throw ValidationException("CR-000") if null              |
| **Client Validated**     | 🟡 Pending                                               |
| **Dead Code Flag**       | No                                                       |
| **Hardcoded Flag**       | No                                                       |
| **Execution Priority**   | 0 (first check before all others)                        |

---

### 3.9 BR-009: Scoring Strategy Selection — Product Type Routing

| **Attribute**            | **Details**                                              |
|--------------------------|----------------------------------------------------------|
| **Rule ID**              | BR-009                                                   |
| **Rule Name**            | Scoring Strategy Selection Based on Product Type         |
| **Category**             | Decision                                                 |
| **Severity**             | Critical                                                 |
| **Business Domain**      | Credit Risk Assessment — Strategy Resolution             |
| **Description**          | The scoring strategy applied to a credit assessment depends on the product type. Secured products use conservative scoring; unsecured products use aggressive scoring; unknown products fall back to configuration default. |
| **Source Location**      | `src/main/java/.../processor/ScoringStrategyProcessor.java` |
| **Source Line(s)**       | L20-L42                                                  |
| **Trigger/Condition**    | After request validation passes                          |
| **Input(s)**             | `request.getProductType()`, `SCORING_DEFAULT_STRATEGY` config |
| **Output(s)/Action**     | Sets SCORING_STRATEGY property on Exchange               |
| **Exception Handling**   | Falls back to StandardScoringStrategy if config unrecognized |
| **Client Validated**     | 🟡 Pending                                               |
| **Dead Code Flag**       | No                                                       |
| **Hardcoded Flag**       | No                                                       |
| **Execution Priority**   | N/A — single decision point                              |

#### Rule Logic (Pseudocode)

```
IF productType = "MORTGAGE" OR productType = "AUTO_LOAN" THEN
    strategy = ConservativeScoringStrategy
ELSE IF productType = "CREDIT_CARD" OR productType = "LINE_OF_CREDIT" THEN
    strategy = AggressiveScoringStrategy
ELSE IF configDefault = "conservative" THEN
    strategy = ConservativeScoringStrategy
ELSE
    strategy = StandardScoringStrategy
END IF
```

---

### 3.10 BR-010: Bureau Score Range Classification

| **Attribute**            | **Details**                                              |
|--------------------------|----------------------------------------------------------|
| **Rule ID**              | BR-010                                                   |
| **Rule Name**            | Bureau Credit Score Range Classification                 |
| **Category**             | Derivation                                               |
| **Severity**             | Major                                                    |
| **Source Location**      | `src/main/java/.../processor/BureauResponseMapper.java`  |
| **Source Line(s)**       | L50-L62                                                  |
| **Input(s)**             | `bureauResponse.getCreditScore()` (integer)              |
| **Output(s)/Action**     | Maps score to descriptive range string                   |
| **Client Validated**     | 🟡 Pending                                               |
| **Dead Code Flag**       | No                                                       |
| **Hardcoded Flag**       | No                                                       |

#### Rule Logic (Pseudocode)

```
IF score >= 800 THEN range = "EXCEPTIONAL"
ELSE IF score >= 740 THEN range = "VERY_GOOD"
ELSE IF score >= 670 THEN range = "GOOD"
ELSE IF score >= 580 THEN range = "FAIR"
ELSE range = "POOR"
```

---

### 3.11 BR-011: Credit Utilization Rate Calculation

| **Attribute**            | **Details**                                              |
|--------------------------|----------------------------------------------------------|
| **Rule ID**              | BR-011                                                   |
| **Rule Name**            | Credit Utilization Rate Calculation                      |
| **Category**             | Computation                                              |
| **Severity**             | Major                                                    |
| **Source Location**      | `src/main/java/.../processor/BureauResponseMapper.java`  |
| **Source Line(s)**       | L64-L70                                                  |
| **Input(s)**             | `totalBalance`, `totalCreditLimit`                       |
| **Output(s)/Action**     | `utilizationRate = totalBalance / totalCreditLimit`      |
| **Client Validated**     | 🟡 Pending                                               |
| **Dead Code Flag**       | No                                                       |
| **Hardcoded Flag**       | No                                                       |

#### Rule Logic (Pseudocode)

```
IF totalCreditLimit IS NOT NULL AND totalCreditLimit > 0 THEN
    utilizationRate = totalBalance / totalCreditLimit
ELSE
    utilizationRate = 0.0
END IF
```

---

### 3.12 BR-012: Bureau Error Handling — Null Response

| **Attribute**            | **Details**                                              |
|--------------------------|----------------------------------------------------------|
| **Rule ID**              | BR-012                                                   |
| **Rule Name**            | Bureau Null Response Handling                            |
| **Category**             | Decision                                                 |
| **Severity**             | Critical                                                 |
| **Source Location**      | `src/main/java/.../processor/BureauResponseMapper.java`  |
| **Source Line(s)**       | L30-L35                                                  |
| **Input(s)**             | Bureau SOAP response                                     |
| **Output(s)/Action**     | Sets BUREAU_ERROR=true, error code CR-301                |
| **Client Validated**     | 🟡 Pending                                               |

#### Rule Logic (Pseudocode)

```
IF bureauResponse IS NULL THEN
    SET BUREAU_ERROR = true
    SET BUREAU_ERROR_CODE = "CR-301"
    LOG "Bureau response is null"
END IF
```

---

### 3.13 BR-013: Bureau Error Handling — Error Code in Response

| **Attribute**            | **Details**                                              |
|--------------------------|----------------------------------------------------------|
| **Rule ID**              | BR-013                                                   |
| **Rule Name**            | Bureau Error Code Response Handling                      |
| **Category**             | Decision                                                 |
| **Severity**             | Critical                                                 |
| **Source Location**      | `src/main/java/.../processor/BureauResponseMapper.java`  |
| **Source Line(s)**       | L37-L42                                                  |
| **Input(s)**             | `bureauResponse.getErrorCode()`                          |
| **Output(s)/Action**     | Sets BUREAU_ERROR=true, error code CR-302                |
| **Client Validated**     | 🟡 Pending                                               |

#### Rule Logic (Pseudocode)

```
IF bureauResponse.errorCode IS NOT NULL THEN
    SET BUREAU_ERROR = true
    SET BUREAU_ERROR_CODE = "CR-302"
    LOG "Bureau returned error: {errorCode} - {errorMessage}"
END IF
```

---

### 3.14 BR-014: Bureau Error — Indeterminate Risk Assessment

| **Attribute**            | **Details**                                              |
|--------------------------|----------------------------------------------------------|
| **Rule ID**              | BR-014                                                   |
| **Rule Name**            | Indeterminate Assessment When Bureau Unavailable         |
| **Category**             | Decision                                                 |
| **Severity**             | Critical                                                 |
| **Source Location**      | `src/main/java/.../processor/RiskScoreCalculator.java`   |
| **Source Line(s)**       | L55-L75                                                  |
| **Input(s)**             | BUREAU_ERROR exchange property                           |
| **Output(s)/Action**     | statusCode=W215E, riskCategory=INDETERMINATE, overallScore=0, recommendation=REFER_TO_UNDERWRITER, accuracyCode=LM |
| **Client Validated**     | 🟡 Pending                                               |
| **Dead Code Flag**       | No                                                       |
| **Hardcoded Flag**       | No                                                       |

#### Rule Logic (Pseudocode)

```
IF BUREAU_ERROR = true THEN
    response.statusCode = "W215E"
    response.statusMessage = "Bureau data unavailable"
    response.riskCategory = "INDETERMINATE"
    response.overallScore = 0
    response.recommendation = "REFER_TO_UNDERWRITER"
    response.accuracyCode = "LM"
END IF
```

---

### 3.15 BR-015: Monthly Debt Estimation

| **Attribute**            | **Details**                                              |
|--------------------------|----------------------------------------------------------|
| **Rule ID**              | BR-015                                                   |
| **Rule Name**            | Monthly Debt Estimation from Total Balance               |
| **Category**             | Computation                                              |
| **Severity**             | Major                                                    |
| **Source Location**      | `src/main/java/.../processor/RiskScoreCalculator.java`   |
| **Source Line(s)**       | L132                                                     |
| **Input(s)**             | `creditDetail.getTotalBalance()`                         |
| **Output(s)/Action**     | `monthlyDebt = totalBalance * 0.03`                      |
| **Client Validated**     | 🟡 Pending                                               |
| **Dead Code Flag**       | No                                                       |
| **Hardcoded Flag**       | **Yes — 3% multiplier hardcoded**                        |

#### Rule Logic (Source Code Reference)

```java
// File: RiskScoreCalculator.java — Line 132
double monthlyDebt = creditDetail.getTotalBalance() * 0.03;
```

> ⚪ **HARDCODED**: The 3% monthly debt factor is a magic number. Should be configurable per product type or derived from actual minimum payment data.

---

### 3.16 BR-016: Debt-to-Income Ratio Calculation

| **Attribute**            | **Details**                                              |
|--------------------------|----------------------------------------------------------|
| **Rule ID**              | BR-016                                                   |
| **Rule Name**            | Debt-to-Income Ratio Calculation                         |
| **Category**             | Computation                                              |
| **Severity**             | Critical                                                 |
| **Source Location**      | `src/main/java/.../processor/RiskScoreCalculator.java`   |
| **Source Line(s)**       | L115-L120                                                |
| **Input(s)**             | `monthlyDebt`, `annualIncome`                            |
| **Output(s)/Action**     | `DTI = monthlyDebt / monthlyIncome`                      |
| **Client Validated**     | 🟡 Pending                                               |
| **Dead Code Flag**       | No                                                       |
| **Hardcoded Flag**       | No                                                       |

#### Rule Logic (Pseudocode)

```
monthlyIncome = annualIncome / 12
IF monthlyIncome <= 0 THEN
    DTI = 1.0    // Worst case default
ELSE
    DTI = monthlyDebt / monthlyIncome
END IF
```

---

### 3.17 BR-017: Income Verification Status

| **Attribute**            | **Details**                                              |
|--------------------------|----------------------------------------------------------|
| **Rule ID**              | BR-017                                                   |
| **Rule Name**            | Income Verification Always Self-Reported                 |
| **Category**             | Derivation                                               |
| **Severity**             | Major                                                    |
| **Source Location**      | `src/main/java/.../processor/RiskScoreCalculator.java`   |
| **Source Line(s)**       | L110-L112                                                |
| **Input(s)**             | `request.getAnnualIncome()`                              |
| **Output(s)/Action**     | verificationStatus="SELF_REPORTED", verifiedIncome=reportedIncome |
| **Client Validated**     | 🟡 Pending                                               |
| **Dead Code Flag**       | No                                                       |
| **Hardcoded Flag**       | **Yes — verification status always "SELF_REPORTED"**     |

> ⚪ **HARDCODED**: No actual income verification occurs. Verified income is always set to the self-reported value.

---

### 3.18 BR-018: Employment Risk Level Classification

| **Attribute**            | **Details**                                              |
|--------------------------|----------------------------------------------------------|
| **Rule ID**              | BR-018                                                   |
| **Rule Name**            | Employment Status to Risk Level Mapping                  |
| **Category**             | Decision                                                 |
| **Severity**             | Major                                                    |
| **Source Location**      | `src/main/java/.../processor/RiskScoreCalculator.java`   |
| **Source Line(s)**       | L122-L133                                                |
| **Input(s)**             | `request.getEmploymentStatus()`                          |
| **Output(s)/Action**     | Maps employment status to risk level string              |
| **Client Validated**     | 🟡 Pending                                               |

#### Rule Logic (Pseudocode)

```
IF employmentStatus = "FULL_TIME" THEN riskLevel = "LOW"
ELSE IF employmentStatus = "PART_TIME" OR "RETIRED" THEN riskLevel = "MODERATE"
ELSE IF employmentStatus = "SELF_EMPLOYED" OR "CONTRACT" THEN riskLevel = "MODERATE_HIGH"
ELSE riskLevel = "HIGH"
```

---

### 3.19 BR-019: Requested Amount Monthly Payment Estimation

| **Attribute**            | **Details**                                              |
|--------------------------|----------------------------------------------------------|
| **Rule ID**              | BR-019                                                   |
| **Rule Name**            | Requested Amount Monthly Payment Factor                  |
| **Category**             | Computation                                              |
| **Severity**             | Major                                                    |
| **Source Location**      | `src/main/java/.../processor/RiskScoreCalculator.java`   |
| **Source Line(s)**       | L143                                                     |
| **Input(s)**             | `request.getRequestedAmount()`                           |
| **Output(s)/Action**     | `requestMonthly = requestedAmount * 0.006`               |
| **Client Validated**     | 🟡 Pending                                               |
| **Dead Code Flag**       | No                                                       |
| **Hardcoded Flag**       | **Yes — 0.6% monthly payment factor hardcoded**          |

> ⚪ **HARDCODED**: Arbitrary 0.6% payment estimation. Does not account for loan term, interest rate, or product type.

---

### 3.20 BR-020: Total Debt Service Ratio Calculation

| **Attribute**            | **Details**                                              |
|--------------------------|----------------------------------------------------------|
| **Rule ID**              | BR-020                                                   |
| **Rule Name**            | Total Debt Service Ratio Including New Debt              |
| **Category**             | Computation                                              |
| **Severity**             | Critical                                                 |
| **Source Location**      | `src/main/java/.../processor/RiskScoreCalculator.java`   |
| **Source Line(s)**       | L140-L147                                                |
| **Input(s)**             | `monthlyDebt`, `requestMonthly`, `monthlyIncome`         |
| **Output(s)/Action**     | `TDSR = (monthlyDebt + requestMonthly) / monthlyIncome`  |
| **Client Validated**     | 🟡 Pending                                               |

#### Rule Logic (Pseudocode)

```
debtServiceRatio = monthlyDebt / monthlyIncome
totalDebtServiceRatio = (monthlyDebt + requestMonthly) / monthlyIncome
affordabilityRating = strategy.determineAffordabilityRating(totalDebtServiceRatio)
```

---

### 3.21 BR-021: Standard Strategy — Risk Categorization

| **Attribute**            | **Details**                                              |
|--------------------------|----------------------------------------------------------|
| **Rule ID**              | BR-021                                                   |
| **Rule Name**            | Standard Scoring — Risk Category Thresholds              |
| **Category**             | Decision                                                 |
| **Severity**             | Critical                                                 |
| **Source Location**      | `src/main/java/.../scoring/StandardScoringStrategy.java` |
| **Source Line(s)**       | L28-L39                                                  |
| **Input(s)**             | `bureauScore`, `DTI`, `utilizationRate`                  |
| **Output(s)/Action**     | Risk category string                                     |
| **Client Validated**     | 🟡 Pending                                               |

#### Rule Logic (Pseudocode)

```
IF bureauScore >= 750 AND DTI < 0.30 AND utilization < 0.30 THEN "EXCELLENT"
ELSE IF bureauScore >= 680 THEN "GOOD"
ELSE IF bureauScore >= 620 THEN "FAIR"
ELSE IF bureauScore >= 560 THEN "POOR"
ELSE "VERY_POOR"
```

---

### 3.22 BR-022: Standard Strategy — Overall Score Calculation

| **Attribute**            | **Details**                                              |
|--------------------------|----------------------------------------------------------|
| **Rule ID**              | BR-022                                                   |
| **Rule Name**            | Standard Scoring — Weighted Overall Score                |
| **Category**             | Computation                                              |
| **Severity**             | Critical                                                 |
| **Source Location**      | `src/main/java/.../scoring/StandardScoringStrategy.java` |
| **Source Line(s)**       | L40-L53                                                  |
| **Input(s)**             | `bureauScore`, `DTI`, `utilizationRate`, `employmentStatus` |
| **Output(s)/Action**     | Integer 0-100 overall composite score                    |

#### Rule Logic (Pseudocode)

```
WEIGHTS: Bureau=40%, DTI=30%, Utilization=20%, Employment=10%

normalizedBureau = min(100, (bureauScore - 300) / 550 * 100)
normalizedDTI = max(0, 100 - (DTI * 200))
normalizedUtil = max(0, 100 - (utilization * 150))
employmentScore = EMPLOYMENT_LOOKUP[employmentStatus]

overallScore = round(max(0, min(100,
    normalizedBureau * 0.40 +
    normalizedDTI * 0.30 +
    normalizedUtil * 0.20 +
    employmentScore * 0.10
)))
```

---

### 3.23 BR-023: Standard Strategy — Employment Score Mapping

| **Attribute**            | **Details**                                              |
|--------------------------|----------------------------------------------------------|
| **Rule ID**              | BR-023                                                   |
| **Rule Name**            | Standard Scoring — Employment Score Lookup               |
| **Category**             | Derivation                                               |
| **Severity**             | Major                                                    |
| **Source Location**      | `src/main/java/.../scoring/StandardScoringStrategy.java` |
| **Source Line(s)**       | L114-L124                                                |

| **Employment Status** | **Score** |
|-----------------------|-----------|
| FULL_TIME             | 100       |
| RETIRED               | 80        |
| PART_TIME             | 70        |
| SELF_EMPLOYED         | 65        |
| CONTRACT              | 60        |
| STUDENT               | 40        |
| UNEMPLOYED            | 10        |
| (null/other)          | 50        |

---

### 3.24 BR-024: Standard Strategy — Recommendation Rules

| **Attribute**            | **Details**                                              |
|--------------------------|----------------------------------------------------------|
| **Rule ID**              | BR-024                                                   |
| **Rule Name**            | Standard Scoring — Recommendation by Risk Category       |
| **Category**             | Decision                                                 |
| **Severity**             | Critical                                                 |
| **Source Location**      | `src/main/java/.../scoring/StandardScoringStrategy.java` |
| **Source Line(s)**       | L54-L67                                                  |

#### Rule Logic (Pseudocode)

```
ratio = requestedAmount / annualIncome

SWITCH riskCategory:
    EXCELLENT → "APPROVE"
    GOOD → IF ratio > 5.0 THEN "APPROVE_WITH_CONDITIONS" ELSE "APPROVE"
    FAIR → IF ratio > 3.0 THEN "REFER_TO_UNDERWRITER" ELSE "APPROVE_WITH_CONDITIONS"
    POOR → "REFER_TO_UNDERWRITER"
    VERY_POOR → "DECLINE"
    DEFAULT → "REFER_TO_UNDERWRITER"
```

---

### 3.25 BR-025: Standard Strategy — Affordability Rating

| **Attribute**            | **Details**                                              |
|--------------------------|----------------------------------------------------------|
| **Rule ID**              | BR-025                                                   |
| **Rule Name**            | Standard Scoring — Affordability Rating Thresholds       |
| **Category**             | Decision                                                 |
| **Severity**             | Major                                                    |
| **Source Location**      | `src/main/java/.../scoring/StandardScoringStrategy.java` |
| **Source Line(s)**       | L68-L73                                                  |

#### Rule Logic (Pseudocode)

```
IF debtServiceRatio < 0.28 THEN "COMFORTABLE"
ELSE IF debtServiceRatio < 0.36 THEN "MANAGEABLE"
ELSE IF debtServiceRatio < 0.44 THEN "STRETCHED"
ELSE "OVEREXTENDED"
```

---

### 3.26 BR-026: Conservative Strategy — Risk Categorization

| **Attribute**            | **Details**                                              |
|--------------------------|----------------------------------------------------------|
| **Rule ID**              | BR-026                                                   |
| **Rule Name**            | Conservative Scoring — Risk Category Thresholds          |
| **Category**             | Decision                                                 |
| **Severity**             | Critical                                                 |
| **Source Location**      | `src/main/java/.../scoring/ConservativeScoringStrategy.java` |
| **Source Line(s)**       | L28-L39                                                  |

#### Rule Logic (Pseudocode)

```
IF bureauScore >= 780 AND DTI < 0.25 AND utilization < 0.25 THEN "EXCELLENT"
ELSE IF bureauScore >= 720 AND DTI < 0.35 THEN "GOOD"
ELSE IF bureauScore >= 660 THEN "FAIR"
ELSE IF bureauScore >= 600 THEN "POOR"
ELSE "VERY_POOR"
```

> 📝 **NOTE**: Thresholds are 30 points higher than Standard for each tier. Also checks DTI for GOOD tier (Standard only checks bureau score).

---

### 3.27 BR-027: Conservative Strategy — Overall Score Calculation

| **Attribute**            | **Details**                                              |
|--------------------------|----------------------------------------------------------|
| **Rule ID**              | BR-027                                                   |
| **Rule Name**            | Conservative Scoring — Weighted Overall Score            |
| **Category**             | Computation                                              |
| **Severity**             | Critical                                                 |
| **Source Location**      | `src/main/java/.../scoring/ConservativeScoringStrategy.java` |
| **Source Line(s)**       | L40-L53                                                  |

#### Rule Logic (Pseudocode)

```
WEIGHTS: Bureau=35%, DTI=35%, Utilization=15%, Employment=15%

normalizedBureau = min(100, (bureauScore - 300) / 550 * 100)
normalizedDTI = max(0, 100 - (DTI * 250))       // Stricter multiplier (250 vs 200)
normalizedUtil = max(0, 100 - (utilization * 200)) // Stricter multiplier (200 vs 150)
employmentScore = CONSERVATIVE_EMPLOYMENT_LOOKUP[employmentStatus]

overallScore = round(max(0, min(100, weighted_average)))
```

---

### 3.28 BR-028: Conservative Strategy — Employment Score Mapping

| **Employment Status** | **Standard Score** | **Conservative Score** | **Delta** |
|-----------------------|--------------------|-----------------------|-----------|
| FULL_TIME             | 100                | 100                   | 0         |
| RETIRED               | 80                 | 75                    | -5        |
| PART_TIME             | 70                 | 55                    | -15       |
| SELF_EMPLOYED         | 65                 | 50                    | -15       |
| CONTRACT              | 60                 | 45                    | -15       |
| STUDENT               | 40                 | 20                    | -20       |
| UNEMPLOYED            | 10                 | 0                     | -10       |
| (null/other)          | 50                 | 40                    | -10       |

---

### 3.29 BR-029: Conservative Strategy — Recommendation Rules

| **Attribute**            | **Details**                                              |
|--------------------------|----------------------------------------------------------|
| **Rule ID**              | BR-029                                                   |
| **Rule Name**            | Conservative Scoring — Recommendation                    |
| **Category**             | Decision                                                 |
| **Source Location**      | `src/main/java/.../scoring/ConservativeScoringStrategy.java` |

#### Rule Logic (Pseudocode)

```
ratio = requestedAmount / annualIncome

SWITCH riskCategory:
    EXCELLENT → IF ratio > 5.0 THEN "APPROVE_WITH_CONDITIONS" ELSE "APPROVE"
    GOOD → IF ratio > 4.0 THEN "REFER_TO_UNDERWRITER" ELSE "APPROVE_WITH_CONDITIONS"
    FAIR → "REFER_TO_UNDERWRITER"
    POOR, VERY_POOR → "DECLINE"
    DEFAULT → "REFER_TO_UNDERWRITER"
```

> 📝 **NOTE**: Significantly stricter than Standard — EXCELLENT can still get conditions; FAIR always referred; POOR declines (Standard only refers).

---

### 3.30 BR-030: Conservative Strategy — Affordability Rating

```
IF debtServiceRatio < 0.25 THEN "COMFORTABLE"
ELSE IF debtServiceRatio < 0.32 THEN "MANAGEABLE"
ELSE IF debtServiceRatio < 0.40 THEN "STRETCHED"
ELSE "OVEREXTENDED"
```

> Thresholds 3-4 points lower than Standard at each tier.

---

### 3.31 BR-031: Aggressive Strategy — Risk Categorization

| **Attribute**            | **Details**                                              |
|--------------------------|----------------------------------------------------------|
| **Rule ID**              | BR-031                                                   |
| **Rule Name**            | Aggressive Scoring — Risk Category Thresholds            |
| **Category**             | Decision                                                 |
| **Source Location**      | `src/main/java/.../scoring/AggressiveScoringStrategy.java` |

#### Rule Logic (Pseudocode)

```
IF bureauScore >= 720 AND DTI < 0.40 THEN "EXCELLENT"
ELSE IF bureauScore >= 650 THEN "GOOD"
ELSE IF bureauScore >= 580 THEN "FAIR"
ELSE IF bureauScore >= 520 THEN "POOR"
ELSE "VERY_POOR"
```

> Thresholds 30 points lower than Standard; EXCELLENT DTI threshold relaxed to 0.40 (vs 0.30).

---

### 3.32 BR-032: Aggressive Strategy — Overall Score Calculation

```
WEIGHTS: Bureau=50%, DTI=25%, Utilization=15%, Employment=10%

normalizedDTI = max(0, 100 - (DTI * 150))        // More relaxed (150 vs 200)
normalizedUtil = max(0, 100 - (utilization * 120)) // More relaxed (120 vs 150)
```

> Bureau-score-dominant weighting (50%) reflects unsecured product risk model.

---

### 3.33 BR-033: Aggressive Strategy — Employment Score Mapping

| **Employment Status** | **Standard Score** | **Aggressive Score** | **Delta** |
|-----------------------|--------------------|-----------------------|-----------|
| FULL_TIME             | 100                | 100                   | 0         |
| RETIRED               | 80                 | 85                    | +5        |
| PART_TIME             | 70                 | 75                    | +5        |
| SELF_EMPLOYED         | 65                 | 70                    | +5        |
| CONTRACT              | 60                 | 65                    | +5        |
| STUDENT               | 40                 | 50                    | +10       |
| UNEMPLOYED            | 10                 | 20                    | +10       |
| (null/other)          | 50                 | 60                    | +10       |

---

### 3.34 BR-034: Aggressive Strategy — Recommendation Rules

```
SWITCH riskCategory:
    EXCELLENT, GOOD → "APPROVE"
    FAIR → "APPROVE_WITH_CONDITIONS"
    POOR → "REFER_TO_UNDERWRITER"
    VERY_POOR → "DECLINE"
```

> Most lenient strategy — both EXCELLENT and GOOD auto-approve regardless of amount/income ratio.

---

### 3.35 BR-035: Aggressive Strategy — Affordability Rating

```
IF debtServiceRatio < 0.35 THEN "COMFORTABLE"
ELSE IF debtServiceRatio < 0.45 THEN "MANAGEABLE"
ELSE IF debtServiceRatio < 0.55 THEN "STRETCHED"
ELSE "OVEREXTENDED"
```

> Thresholds 7-11 points higher than Standard — more relaxed for unsecured products.

---

### 3.36 BR-036: Risk Factor — Low Credit Score

| **Attribute**            | **Details**                                              |
|--------------------------|----------------------------------------------------------|
| **Rule ID**              | BR-036                                                   |
| **Rule Name**            | Risk Factor Identification — Low Credit Score            |
| **Category**             | Constraint                                               |
| **Severity**             | Major                                                    |
| **Source Location**      | `src/main/java/.../processor/RiskScoreCalculator.java`   |
| **Source Line(s)**       | L149-L152                                                |
| **Input(s)**             | `creditDetail.getBureauScore()`                          |
| **Output(s)/Action**     | Adds "LOW_CREDIT_SCORE" to riskFactors list              |

```
IF bureauScore < 650 THEN ADD "LOW_CREDIT_SCORE"
```

---

### 3.37 BR-037: Risk Factor — High Credit Utilization

```
IF utilizationRate > 0.50 THEN ADD "HIGH_CREDIT_UTILIZATION"
```

Source: RiskScoreCalculator.java L153-L155

---

### 3.38 BR-038: Risk Factor — Past Delinquencies

```
IF delinquencyCount > 0 THEN ADD "PAST_DELINQUENCIES"
```

Source: RiskScoreCalculator.java L156-L158

---

### 3.39 BR-039: Risk Factor — Excessive Credit Inquiries

```
IF inquiryCount > 5 THEN ADD "EXCESSIVE_CREDIT_INQUIRIES"
```

Source: RiskScoreCalculator.java L159-L161

---

### 3.40 BR-040: Risk Factor — High Debt-to-Income

```
IF debtToIncomeRatio > 0.40 THEN ADD "HIGH_DEBT_TO_INCOME"
```

Source: RiskScoreCalculator.java L162-L164

---

### 3.41 BR-041: Risk Factor — No Employment Income

```
IF employmentStatus = "UNEMPLOYED" THEN ADD "NO_EMPLOYMENT_INCOME"
```

Source: RiskScoreCalculator.java L165-L167

---

### 3.42 BR-042: WS-Security Authentication

| **Attribute**            | **Details**                                              |
|--------------------------|----------------------------------------------------------|
| **Rule ID**              | BR-042                                                   |
| **Rule Name**            | SOAP Endpoint WS-Security Enforcement                    |
| **Category**             | Authorization                                            |
| **Severity**             | Critical                                                 |
| **Source Location**      | `src/main/resources/OSGI-INF/blueprint/blueprint.xml`    |
| **Input(s)**             | SOAP WS-Security header: UsernameToken (PasswordText)    |
| **Output(s)/Action**     | Allow or reject request with SOAP fault                  |
| **Client Validated**     | 🟡 Pending                                               |

> Applied only to `creditRiskSoapEndpoint`; the gateway endpoint (`creditRiskGwEndpoint`) has **no** security.

---

### 3.43 BR-043: Gateway Header Enrichment

| **Attribute**            | **Details**                                              |
|--------------------------|----------------------------------------------------------|
| **Rule ID**              | BR-043                                                   |
| **Rule Name**            | Gateway Request Pre-Processing — Default Headers         |
| **Category**             | Workflow                                                 |
| **Severity**             | Major                                                    |
| **Source Location**      | `src/main/java/.../processor/GatewayRequestPreProcessor.java` |
| **Source Line(s)**       | L15-L30                                                  |

#### Rule Logic (Pseudocode)

```
IF request.requestHeader IS NULL THEN
    CREATE RequestHeader
    SET sourceSystem = "GATEWAY"
    SET transactionId = UUID.randomUUID()
    SET timestamp = current time (yyyy-MM-dd'T'HH:mm:ss)
END IF
IF request.requestChannel IS NULL THEN
    SET requestChannel = "API"
END IF
```

---

### 3.44 BR-044: Error Response — Validation Error

| **Attribute**            | **Details**                                              |
|--------------------------|----------------------------------------------------------|
| **Rule ID**              | BR-044                                                   |
| **Rule Name**            | Validation Error Response — HTTP 400                     |
| **Category**             | Workflow                                                 |
| **Severity**             | Major                                                    |
| **Source Location**      | `src/main/java/.../processor/ErrorProcessor.java`        |
| **Source Line(s)**       | L25-L35                                                  |

```
IF exception IS ValidationException THEN
    statusCode = exception.getErrorCode()
    statusMessage = exception.getMessage()
    httpStatus = 400
END IF
```

---

### 3.45 BR-045: Error Response — System Error

| **Attribute**            | **Details**                                              |
|--------------------------|----------------------------------------------------------|
| **Rule ID**              | BR-045                                                   |
| **Rule Name**            | System Error Response — HTTP 500                         |
| **Category**             | Workflow                                                 |
| **Source Location**      | `src/main/java/.../processor/ErrorProcessor.java`        |
| **Source Line(s)**       | L37-L48                                                  |

```
IF exception IS NOT ValidationException THEN
    statusCode = "CR-500"
    statusMessage = "Internal service error"
    httpStatus = 500
END IF
```

> ⚠️ **GAP**: All non-validation exceptions collapse to generic CR-500. No distinction between timeout, parsing, or database errors.

---

## 4. Validation Rules Summary

| **Rule ID** | **Rule Name**        | **Field**             | **Validation Logic**                                | **Validation Exists** | **Error Code** |
|-------------|----------------------|-----------------------|-----------------------------------------------------|-----------------------|----------------|
| BR-008      | Null Request Body    | request body          | Must not be null                                    | ✅ Yes                | CR-000         |
| BR-001      | Applicant ID         | applicantId           | Required, non-blank                                 | ✅ Yes                | CR-001         |
| BR-002      | First Name           | firstName             | Required, non-blank                                 | ✅ Yes                | CR-002         |
| BR-003      | Last Name            | lastName              | Required, non-blank                                 | ✅ Yes                | CR-003         |
| BR-004      | Date of Birth        | dateOfBirth           | Required, YYYY-MM-DD format                         | ✅ Yes                | CR-004         |
| BR-005      | SIN                  | socialInsuranceNumber | Required, NNN-NNN-NNN format                        | ✅ Yes                | CR-005         |
| BR-006      | Province             | province              | Must be valid enum (13 values)                      | ✅ Yes                | CR-006         |
| BR-007      | Postal Code          | postalCode            | Canadian format (if provided)                       | ✅ Yes                | CR-007         |
| —           | DOB Age Check        | dateOfBirth           | Minimum age 18, not future date                     | ❌ No (Gap)           | —              |
| —           | SIN Checksum         | socialInsuranceNumber | Luhn algorithm checksum                             | ❌ No (Gap)           | —              |
| —           | Postal-Province      | postalCode + province | Cross-validation                                    | ❌ No (Gap)           | —              |

---

## 5. Computation Rules Summary

| **Rule ID** | **Rule Name**                  | **Formula/Logic**                                  | **Inputs**                        | **Output**       | **Hardcoded Override**                |
|-------------|--------------------------------|----------------------------------------------------|-----------------------------------|------------------|---------------------------------------|
| BR-011      | Utilization Rate               | totalBalance / totalCreditLimit                    | totalBalance, totalCreditLimit    | utilizationRate  | None                                  |
| BR-015      | Monthly Debt                   | totalBalance × 0.03                                | totalBalance                      | monthlyDebt      | **0.03 hardcoded**                    |
| BR-016      | Debt-to-Income                 | monthlyDebt / monthlyIncome                        | monthlyDebt, annualIncome         | DTI              | None                                  |
| BR-019      | Request Monthly Payment        | requestedAmount × 0.006                            | requestedAmount                   | requestMonthly   | **0.006 hardcoded**                   |
| BR-020      | Total Debt Service Ratio       | (monthlyDebt + requestMonthly) / monthlyIncome     | monthlyDebt, requestMonthly, income | TDSR           | None                                  |
| BR-022      | Standard Overall Score         | Weighted: 40/30/20/10 bureau/DTI/util/emp          | 4 normalized factors              | 0-100 score      | None                                  |
| BR-027      | Conservative Overall Score     | Weighted: 35/35/15/15 bureau/DTI/util/emp          | 4 normalized factors (stricter)   | 0-100 score      | None                                  |
| BR-032      | Aggressive Overall Score       | Weighted: 50/25/15/10 bureau/DTI/util/emp          | 4 normalized factors (relaxed)    | 0-100 score      | None                                  |

---

## 6. Decision Rules (Decision Tables)

### 6.1 Decision Table: Strategy Selection

**Rule ID:** BR-009

| **Product Type** | **Config Default** | **Strategy Selected**         |
|------------------|--------------------|-------------------------------|
| MORTGAGE         | Any                | ConservativeScoringStrategy   |
| AUTO_LOAN        | Any                | ConservativeScoringStrategy   |
| CREDIT_CARD      | Any                | AggressiveScoringStrategy     |
| LINE_OF_CREDIT   | Any                | AggressiveScoringStrategy     |
| (unknown)        | conservative       | ConservativeScoringStrategy   |
| (unknown)        | standard/other     | StandardScoringStrategy       |

### 6.2 Decision Table: Standard Strategy Recommendation

**Rule IDs:** BR-024

| **Risk Category** | **Amount/Income Ratio** | **Recommendation**         |
|--------------------|------------------------|----------------------------|
| EXCELLENT          | Any                    | APPROVE                    |
| GOOD               | ≤ 5.0                  | APPROVE                    |
| GOOD               | > 5.0                  | APPROVE_WITH_CONDITIONS    |
| FAIR               | ≤ 3.0                  | APPROVE_WITH_CONDITIONS    |
| FAIR               | > 3.0                  | REFER_TO_UNDERWRITER       |
| POOR               | Any                    | REFER_TO_UNDERWRITER       |
| VERY_POOR          | Any                    | DECLINE                    |

### 6.3 Decision Table: Conservative Strategy Recommendation

**Rule IDs:** BR-029

| **Risk Category** | **Amount/Income Ratio** | **Recommendation**         |
|--------------------|------------------------|----------------------------|
| EXCELLENT          | ≤ 5.0                  | APPROVE                    |
| EXCELLENT          | > 5.0                  | APPROVE_WITH_CONDITIONS    |
| GOOD               | ≤ 4.0                  | APPROVE_WITH_CONDITIONS    |
| GOOD               | > 4.0                  | REFER_TO_UNDERWRITER       |
| FAIR               | Any                    | REFER_TO_UNDERWRITER       |
| POOR               | Any                    | DECLINE                    |
| VERY_POOR          | Any                    | DECLINE                    |

### 6.4 Decision Table: Aggressive Strategy Recommendation

**Rule IDs:** BR-034

| **Risk Category** | **Amount/Income Ratio** | **Recommendation**         |
|--------------------|------------------------|----------------------------|
| EXCELLENT          | Any                    | APPROVE                    |
| GOOD               | Any                    | APPROVE                    |
| FAIR               | Any                    | APPROVE_WITH_CONDITIONS    |
| POOR               | Any                    | REFER_TO_UNDERWRITER       |
| VERY_POOR          | Any                    | DECLINE                    |

### 6.5 Decision Table: Risk Categorization Thresholds Comparison

| **Category** | **Standard** | **Conservative** | **Aggressive** |
|--------------|-------------|------------------|----------------|
| EXCELLENT    | ≥750, DTI<0.30, util<0.30 | ≥780, DTI<0.25, util<0.25 | ≥720, DTI<0.40 |
| GOOD         | ≥680         | ≥720, DTI<0.35   | ≥650           |
| FAIR         | ≥620         | ≥660              | ≥580           |
| POOR         | ≥560         | ≥600              | ≥520           |
| VERY_POOR    | <560         | <600              | <520           |

### 6.6 Decision Table: Affordability Rating Comparison

| **Rating**     | **Standard** | **Conservative** | **Aggressive** |
|----------------|-------------|------------------|----------------|
| COMFORTABLE    | DSR < 0.28  | DSR < 0.25       | DSR < 0.35     |
| MANAGEABLE     | DSR < 0.36  | DSR < 0.32       | DSR < 0.45     |
| STRETCHED      | DSR < 0.44  | DSR < 0.40       | DSR < 0.55     |
| OVEREXTENDED   | DSR ≥ 0.44  | DSR ≥ 0.40       | DSR ≥ 0.55     |

---

## 7. Workflow / State Transition Rules

### 7.1 State Machine: Credit Risk Assessment Request

**Related Rule IDs:** BR-008 through BR-045

| **Current State**    | **Event/Trigger**          | **Condition(s)**                   | **Next State**        | **Action(s)**                        |
|----------------------|----------------------------|------------------------------------|-----------------------|--------------------------------------|
| RECEIVED             | Validation start           | —                                  | VALIDATING            | Execute BR-001..BR-008               |
| VALIDATING           | Validation pass            | All fields valid                   | STRATEGY_SELECTED     | Execute BR-009                       |
| VALIDATING           | Validation fail            | Any field invalid                  | ERROR                 | Execute BR-044                       |
| STRATEGY_SELECTED    | Bureau request built       | —                                  | BUREAU_INQUIRY        | Build BureauInquiryRequest           |
| BUREAU_INQUIRY       | Bureau response received   | Response valid                     | SCORING               | Execute BR-010..BR-013               |
| BUREAU_INQUIRY       | Bureau error               | Null/error response                | INDETERMINATE         | Execute BR-014                       |
| SCORING              | Score calculated           | —                                  | ASSESSED              | Execute BR-015..BR-041               |
| INDETERMINATE        | —                          | —                                  | RESPONSE_BUILT        | Return W215E response                |
| ASSESSED             | —                          | —                                  | RESPONSE_BUILT        | Build full response                  |
| RESPONSE_BUILT       | Wire tap                   | —                                  | LOGGED                | Async MongoDB insert                 |
| ERROR                | —                          | —                                  | LOGGED                | Error response + async log           |

```
RECEIVED ──[validate]──▶ VALIDATING ──[pass]──▶ STRATEGY_SELECTED ──▶ BUREAU_INQUIRY
     │                       │                                              │
     │                   [fail]                                        [error/null]
     │                       ▼                                              ▼
     │                    ERROR ──▶ LOGGED                           INDETERMINATE
     │                                                                      │
     │                                                             [respond]│
     │              SCORING ◀── [response OK] ── BUREAU_INQUIRY            │
     │                  │                                                   │
     │             [calculated]                                             │
     │                  ▼                                                   │
     │              ASSESSED ──▶ RESPONSE_BUILT ◀───────────────────────────┘
     │                                  │
     │                             [wire tap]
     │                                  ▼
     └─────────────────────────────▶ LOGGED
```

---

## 8. Authorization Rules

| **Rule ID** | **Resource/Action**              | **Required Credentials**            | **Additional Conditions**                     |
|-------------|----------------------------------|-------------------------------------|-----------------------------------------------|
| BR-042      | SOAP endpoint access             | WSS4J UsernameToken (PasswordText)  | Only for `creditRiskSoapEndpoint`             |
| —           | REST endpoint access             | None                                | ⚠️ No authentication on REST endpoint         |
| —           | Gateway endpoint access          | None                                | ⚠️ No authentication on Gateway endpoint      |

---

## 9. Unresolved / Ambiguous Rules

| **Rule ID** | **Description**                                       | **Ambiguity**                                    | **Recommended Action**                  |
|-------------|-------------------------------------------------------|--------------------------------------------------|-----------------------------------------|
| BR-015      | Monthly debt = totalBalance × 0.03                    | Why 3%? Is this minimum payment? APR-based?      | SME review — validate with business     |
| BR-019      | Request monthly = requestedAmount × 0.006             | Why 0.6%? No term/rate considered                | SME review — should be amortization-based |
| BR-017      | Income always "SELF_REPORTED"                          | Is this intentional or incomplete implementation? | Confirm with business team              |
| BR-006      | All 13 provinces accepted but config only lists 4     | SUPPORTED_PROVINCE_LIST=ON,BC,AB,QC is unused    | Clarify if enum or config should govern |

---

## 10. Notes & Observations

| **#** | **Observation**                                                                     | **Related Rules**         | **Impact**  |
|-------|------------------------------------------------------------------------------------|---------------------------|-------------|
| 1     | All 3 strategies share same algorithm structure with different constants            | BR-021..BR-035            | Medium — code duplication risk |
| 2     | Risk factor thresholds (BR-036..BR-041) use the same fixed values across all strategies | BR-036..BR-041       | Medium — may need strategy-specific thresholds |
| 3     | Province is validated but never used in scoring                                     | BR-006                    | High — regulatory differences ignored |
| 4     | RequestChannelType includes BATCH but no batch processing exists                   | —                         | Low — dead enum value |
| 5     | Scoring model version hardcoded as strategy name + "-v1.0"                         | —                         | Medium — no A/B testing support |

---

## 11. Potentially Missing Business Rules

| **#** | **Expected Rule Description**                  | **Business Domain**    | **Expected Category** | **Found?** | **Evidence of Absence**                                          | **Severity** | **Recommended Action**              | **Linked Gap** |
|-------|-------------------------------------------------|------------------------|-----------------------|------------|------------------------------------------------------------------|-------------|--------------------------------------|----------------|
| 1     | Minimum age check (18+ years)                   | Validation             | Validation            | ❌ No      | DOB validated for format only; no age calculation                | High        | Implement age check                  | GAP-010        |
| 2     | SIN checksum validation (Luhn)                  | Validation             | Validation            | ❌ No      | SIN format checked via regex; no checksum                        | Medium      | Implement Luhn algorithm             | GAP-011        |
| 3     | Postal code to province cross-validation        | Validation             | Validation            | ❌ No      | Both validated independently                                     | Medium      | Implement cross-check                | GAP-012        |
| 4     | Loan-to-Value (LTV) calculation                 | Risk Assessment        | Computation           | ❌ No      | No collateral/property value fields or calculations              | High        | Add for mortgage/auto products       | GAP-005        |
| 5     | Employment duration / stability check           | Risk Assessment        | Decision              | ❌ No      | Model has yearsEmployed field but never populated/used           | Medium      | Populate and add threshold rules     | GAP-006        |
| 6     | Province-specific risk adjustments              | Risk Assessment        | Decision              | ❌ No      | Province captured but scoring ignores it entirely                | High        | Add regulatory/risk province factors | GAP-014        |
| 7     | Rate limiting per applicant/per day             | Security               | Constraint            | ❌ No      | No throttling on any endpoint                                    | High        | Implement rate limiting              | GAP-019        |
| 8     | Credit history age analysis                     | Risk Assessment        | Computation           | ❌ No      | Only snapshot data used; no time-series                          | High        | Add credit age factors               | GAP-007        |

---

## 12. Rule Execution Order & Conflicts

### 12.1 Rule Execution Chains

| **Trigger / Input Context**              | **Rule Execution Order**                                                    | **Notes**                    |
|------------------------------------------|-----------------------------------------------------------------------------|------------------------------|
| REST/SOAP/GW request received            | BR-008 → BR-001 → BR-002 → BR-003 → BR-004 → BR-005 → BR-006 → BR-007    | Sequential; stops on first failure |
| After validation passes                  | BR-009 (strategy selection)                                                 | Single decision              |
| After bureau response received           | BR-012/BR-013 → BR-010 → BR-011                                           | Error check first, then mapping |
| If bureau error                          | BR-014                                                                      | Short-circuit to INDETERMINATE |
| If bureau success                        | BR-015 → BR-016 → BR-017 → BR-018 → BR-019 → BR-020                      | Income/debt computations     |
| Scoring via selected strategy            | BR-021/026/031 → BR-022/027/032 → BR-024/029/034 → BR-025/030/035          | Strategy-specific chain      |
| Risk factor identification               | BR-036 → BR-037 → BR-038 → BR-039 → BR-040 → BR-041                      | Independent; all evaluated   |

### 12.2 Rule Conflicts

| **Rule A** | **Rule B** | **Conflict Description**                                          | **Resolution**                                     | **Status**        |
|------------|------------|-------------------------------------------------------------------|----------------------------------------------------|-------------------|
| BR-006     | Config     | ProvinceType enum accepts 13 provinces; SUPPORTED_PROVINCE_LIST has 4 | Enum governs validation; config list appears unused | Open — needs clarification |

---

## 13. Appendices

### Appendix A: Rule ID Naming Convention

Format: `BR-[NNN]` where NNN is sequential.

| **Range**   | **Domain**                |
|-------------|---------------------------|
| BR-001..008 | Validation rules          |
| BR-009      | Strategy selection        |
| BR-010..013 | Bureau response handling   |
| BR-014      | Bureau error fallback     |
| BR-015..020 | Income/debt computations  |
| BR-021..025 | Standard strategy rules   |
| BR-026..030 | Conservative strategy rules |
| BR-031..035 | Aggressive strategy rules |
| BR-036..041 | Risk factor constraints   |
| BR-042      | Authorization             |
| BR-043..045 | Workflow/error handling    |

### Appendix B: Glossary

| **Term**           | **Definition**                                                    |
|--------------------|-------------------------------------------------------------------|
| DTI                | Debt-to-Income ratio — monthly debt divided by monthly income      |
| DSR                | Debt Service Ratio — ratio of total debt service to income         |
| TDSR               | Total Debt Service Ratio — includes proposed new debt              |
| LTV                | Loan-to-Value ratio — loan amount vs. asset value (not implemented)|
| SIN                | Social Insurance Number — Canadian unique identifier               |
| Bureau Score       | Credit score from external credit bureau (300-900 range)           |
| Utilization Rate   | Total credit balance divided by total credit limit                 |

---

> **Document Control:**
> | Version | Date | Author | Changes |
> |---------|------|--------|---------|
> | 1.0 | 31-Mar-2026 | RE Automation (Local) | Initial draft — 45 business rules cataloged across 7 categories |
