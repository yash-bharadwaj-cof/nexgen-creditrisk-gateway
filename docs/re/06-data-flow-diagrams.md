# Data Flow Diagram

---

| **Field**            | **Details**                                         |
|----------------------|-----------------------------------------------------|
| **Project Name**     | NexGen Credit Risk Gateway                         |
| **Application Name** | nexgen-creditrisk-gateway                          |
| **Version**          | 1.0.0-SNAPSHOT                                     |
| **Date**             | 31-Mar-2026                                        |
| **Prepared By**      | Copilot Reverse Engineering Agent                  |
| **Reviewed By**      | —                                                  |
| **Status**           | Draft                                              |

---

## 1. Overview

This document presents the Data Flow Diagrams (DFDs) identified during reverse engineering of the `nexgen-creditrisk-gateway` JBoss Fuse 6.3 service. DFDs illustrate how data moves through the system — from external entities through processes to data stores and back. The service exposes three entry points (REST, SOAP Connectivity, SOAP Gateway/Guidewire) and integrates with an external Credit Bureau via CXF SOAP, logging every transaction asynchronously to MongoDB via the Camel Wire Tap pattern.

Diagrams are organized at three levels of abstraction (Context → Level 1 → Level 2) and are accompanied by catalogs, transformation rules, and target-state proposals.

### DFD Notation

| **Symbol**              | **Meaning**                                       |
|-------------------------|---------------------------------------------------|
| ▭ (Rectangle)           | External Entity — source or destination of data   |
| ○ (Circle/Rounded rect) | Process — transforms or routes data               |
| ═══ (Open rectangle)    | Data Store — where data is persisted              |
| → (Arrow)               | Data Flow — movement of data between elements     |

### Annotation Notation

| **Annotation**       | **Meaning**                                                         |
|----------------------|---------------------------------------------------------------------|
| `⚠️ NO VALIDATION`   | Data enters or exits this point without any validation              |
| `⚪ HARDCODED`        | Data at this point is hardcoded/static, not derived from processing |
| `⚫ DEAD FLOW`        | Data flow path exists in code but is never triggered                |
| `🔒 PII`              | Data flow contains Personally Identifiable Information              |
| `🔒 SENSITIVE`        | Data flow contains sensitive business data                          |
| `📝 NOTE`             | General observation                                                 |

---

## 2. DFD Index

| **Diagram ID** | **Level** | **Title**                          | **Parent Process** | **Description**                                                   |
|----------------|-----------|------------------------------------|--------------------|-------------------------------------------------------------------|
| DFD-0          | 0         | Context Diagram                    | —                  | Entire service as single process; all 5 external entities         |
| DFD-1          | 1         | Credit Risk Assessment — L1        | DFD-0              | Ten internal processes (P1–P10), data stores, all data flows      |
| DFD-1.1        | 2         | Request Validation Decomposition   | DFD-1 / P1         | Seven validation rules (CR-001 → CR-007)                         |
| DFD-1.2        | 2         | Scoring Decomposition              | DFD-1 / P2, P6     | Strategy selection, weight application, score computation         |
| DFD-1.3        | 2         | Bureau Integration Decomposition   | DFD-1 / P3, P4, P5 | Bureau request mapping, SOAP call, response mapping, error path   |

---

## 3. Context Diagram (Level 0)

**Diagram ID:** DFD-0  
**Description:** Shows the entire `nexgen-creditrisk-gateway` application as a single process with all external entities and data flows.

### External Entities

| **Entity ID** | **Entity Name**    | **Type**        | **Description**                                                                                    |
|---------------|--------------------|-----------------|----------------------------------------------------------------------------------------------------|
| EE-001        | REST Client        | External Actor  | Invokes JAX-RS GET `/service/rest/creditrisk/assess` with 11 query parameters                      |
| EE-002        | SOAP Client        | External Actor  | Invokes CXF SOAP endpoint `/service/soap/creditrisk`; secured with WSS4J UsernameToken             |
| EE-003        | Guidewire Gateway  | External System | Invokes CXF SOAP endpoint `/service/creditriskapi`; secured with WSS4J UsernameToken               |
| EE-004        | Credit Bureau      | External System | CXF SOAP service at `BUREAU_ENDPOINT_URL`; operation `inquire` on namespace `http://ws.esb.nexgen.com/bureau/v1` |
| EE-005        | MongoDB            | Data Store      | Transaction log store; accessed via mongo-java-driver 3.4.2; populated by async Wire Tap           |

### Context Diagram

```
                          ┌─────────────────────────────────────────┐
  [DF-01 REST Request]    │                                         │  [DF-07 Bureau SOAP Req]
▭ EE-001 ───────────────▶│                                         │────────────────────────▶ ▭ EE-004
  🔒PII                   │                                         │  🔒PII                    Credit
  [DF-11 REST Response]   │         CREDIT RISK ASSESSMENT          │  [DF-08 Bureau SOAP Resp]  Bureau
▭ EE-001 ◀───────────────│             SERVICE                     │◀──────────────────────── ▭ EE-004
  🔒SENSITIVE             │               (P0)                      │  🔒SENSITIVE
                          │                                         │
  [DF-02 SOAP Request]    │                                         │  [DF-13 Log Insert]
▭ EE-002 ───────────────▶│                                         │────────────────────────▶ ═══════════
  🔒PII                   │                                         │  Non-Sensitive            EE-005
  [DF-02r SOAP Response]  │                                         │                           MongoDB
▭ EE-002 ◀───────────────│                                         │
  🔒SENSITIVE             │                                         │
                          │                                         │
  [DF-03 GW SOAP Request] │                                         │
▭ EE-003 ───────────────▶│                                         │
  🔒PII                   │                                         │
  [DF-03r GW Response]    │                                         │
▭ EE-003 ◀───────────────│                                         │
  🔒SENSITIVE             └─────────────────────────────────────────┘
  Guidewire
  Gateway
```

### Data Flows (Level 0)

| **Flow ID** | **From**          | **To**            | **Data Description**                                                                                       | **Format/Protocol** | **Data Sensitivity** | **Annotations**                             |
|-------------|-------------------|-------------------|------------------------------------------------------------------------------------------------------------|---------------------|----------------------|---------------------------------------------|
| DF-01       | EE-001 (REST)     | System (P0)       | 11 query params: applicantId, firstName, lastName, DOB, SIN, province, postalCode, productType, loanAmount, income, employment | REST/HTTP GET / URL params | 🔒 PII              | ⚠️ NO VALIDATION at transport layer         |
| DF-02       | EE-002 (SOAP)     | System (P0)       | SOAP XML body with same fields; WSS4J UsernameToken authentication header                                   | SOAP/XML + WS-Security     | 🔒 PII              | None                                        |
| DF-03       | EE-003 (Gateway)  | System (P0)       | SOAP XML body; defaults injected (channel=GATEWAY, source=API)                                              | SOAP/XML + WS-Security     | 🔒 PII              | ⚪ HARDCODED defaults added by P8           |
| DF-07       | System (P0)       | EE-004 (Bureau)   | BureauInquiryRequest with subscriber info, applicant demographics                                           | SOAP/XML                   | 🔒 PII              | ⚠️ NO VALIDATION on bureau schema (schema-validation-enabled=false) |
| DF-08       | EE-004 (Bureau)   | System (P0)       | BureauInquiryResponse with credit score, delinquency/inquiry counts, tradeline data, or error               | SOAP/XML                   | 🔒 SENSITIVE        | None                                        |
| DF-11       | System (P0)       | EE-001 (REST)     | HTTP 200 JSON/XML response with CreditRiskResType (risk category, score, recommendation)                    | REST/HTTP 200 + XML        | 🔒 SENSITIVE        | 📝 NOTE: content-type set to application/xml despite REST endpoint |
| DF-02r      | System (P0)       | EE-002 (SOAP)     | SOAP XML response with CreditRiskResType                                                                    | SOAP/XML                   | 🔒 SENSITIVE        | None                                        |
| DF-03r      | System (P0)       | EE-003 (Gateway)  | SOAP XML response with CreditRiskResType                                                                    | SOAP/XML                   | 🔒 SENSITIVE        | None                                        |
| DF-13       | System (P0)       | EE-005 (MongoDB)  | Transaction log document (non-PII subset): transactionId, province, productType, riskCategory, overallScore, recommendation, status | MongoDB Wire Protocol | Non-Sensitive     | 📝 NOTE: Async via Wire Tap; no PII in log   |
| DF-14       | System (P0)       | All Clients       | Error response: HTTP 400 (ValidationException) or 500 (all other exceptions)                                | REST/HTTP or SOAP/XML      | Non-Sensitive       | None                                        |

---

## 4. Level 1 Data Flow Diagram

**Diagram ID:** DFD-1  
**Description:** Decomposes the Credit Risk Assessment Service into 10 internal processes, showing internal data stores and all data flows.

### Processes

| **Process ID** | **Process Name**           | **Description**                                                                                                | **Component(s)**                           |
|----------------|----------------------------|----------------------------------------------------------------------------------------------------------------|--------------------------------------------|
| P1             | Request Validation         | Validates 7 mandatory fields (CR-001→CR-007); throws `ValidationException` on failure                         | `CreditRiskRequestValidator`               |
| P2             | Strategy Selection         | Maps product type to scoring strategy (Standard / Conservative / Aggressive)                                   | `ScoringStrategyProcessor`                 |
| P3             | Bureau Request Building    | Maps `CreditRiskReqType` → `BureauInquiryRequest`; sets subscriber info, requestId, timestamp                 | `BureauRequestBuilder`                     |
| P4             | Bureau Service Call        | Invokes external Credit Bureau via CXF SOAP client; operation `inquire`                                        | Camel CXF `<to uri="cxf:bean:bureauEndpoint"/>` |
| P5             | Bureau Response Mapping    | Maps `BureauInquiryResponse` → `CreditScoreDetail`; categorizes score range; handles bureau errors            | `BureauResponseMapper`                     |
| P6             | Risk Score Calculation     | Applies scoring strategy to produce `CreditRiskResType` (overall score, risk category, recommendation, affordability) | `RiskScoreCalculator`                 |
| P7             | Response Formatting        | Sets HTTP 200, content-type application/xml for REST route only                                                | `RestResponseBuilder`                      |
| P8             | Gateway Preprocessing      | Injects defaults: sourceSystem=GATEWAY, requestChannel=API, transactionId=UUID, timestamp=now                 | `GatewayRequestPreProcessor`               |
| P9             | Transaction Logging        | Async Wire Tap → MongoDB document insert; logs non-PII transaction metadata                                    | `TransactionLogger`                        |
| P10            | Error Handling             | Catches all exceptions; maps `ValidationException`→HTTP 400, all others→HTTP 500                              | `ErrorProcessor`                           |

### Data Stores

| **Store ID** | **Store Name**              | **Type**     | **Technology**            | **Data Retention**  | **Failure Behavior**         | **Description**                                                                  |
|--------------|-----------------------------|--------------|---------------------------|---------------------|------------------------------|----------------------------------------------------------------------------------|
| DS-001       | MongoDB Transaction Log     | Database     | MongoDB 3.4.2 (Wire Tap)  | Configurable (TTL not configured — Permanent by default) | Main flow continues silently | Transaction metadata (non-PII): transactionId, province, productType, riskCategory, overallScore, recommendation, status, errorCode, errorMessage |
| DS-002       | In-Memory Exchange Model    | In-Memory    | Apache Camel Exchange     | Per-request lifetime | Main flow fails              | Working model: `CreditRiskReqType`, `BureauInquiryRequest`, `BureauInquiryResponse`, `CreditScoreDetail`, `CreditRiskResType` |

### Level 1 Diagram

```
                                    ┌──────────────────────────────────────────────────────────────────────────────┐
                                    │                    NEXGEN-CREDITRISK-GATEWAY (DFD-1)                         │
                                    │                                                                              │
  ▭ EE-001 ──[DF-01]──────────────▶│P1: Request Validation (CreditRiskRequestValidator)                           │
  REST Client                       │  CR-001…CR-007                                                               │
                                    │       │ DF-04 (Validated CreditRiskReqType)                                  │
  ▭ EE-002 ──[DF-02]──────────────▶│       ▼                                                                      │
  SOAP Client                       │P2: Strategy Selection (ScoringStrategyProcessor)                             │
                                    │  productType → strategy                                                      │
  ▭ EE-003 ──[DF-03]──▶ P8 ───────▶│       │ DF-05 (CreditRiskReqType + strategy)                                 │
  Guidewire                         │       ▼                                                                      │
  Gateway                           │P3: Bureau Request Building (BureauRequestBuilder)                            │
                                    │  CreditRiskReqType → BureauInquiryRequest                                    │
                                    │       │ DF-06 (BureauInquiryRequest)                                         │
                                    │       ▼                                                                      │
                                    │P4: Bureau Service Call (CXF cxf:bean:bureauEndpoint)                         │──[DF-07]──▶ ▭ EE-004
                                    │  SOAP invocation                                                             │              Credit Bureau
                                    │       │ DF-08 (BureauInquiryResponse)              ◀────────────────────────│◀─[DF-08]──── ▭ EE-004
                                    │       ▼                                                                      │
                                    │P5: Bureau Response Mapping (BureauResponseMapper)                            │
                                    │  BureauInquiryResponse → CreditScoreDetail                                   │
                                    │       │ DF-09 (CreditScoreDetail)                                            │
                                    │       ▼                                                                      │
                                    │P6: Risk Score Calculation (RiskScoreCalculator)                              │
                                    │  CreditScoreDetail + strategy → CreditRiskResType                            │
                                    │       │ DF-10 (CreditRiskResType)        DF-12 (Wire Tap) ─────────────────▶ │
                                    │       ▼                                        ▼                             │
                                    │P7: Response Formatting (RestResponseBuilder)  P9: Transaction Logging        │──[DF-13]──▶ ═══════════
                                    │  REST route only                               (TransactionLogger)           │              DS-001
                                    │       │ DF-11                                                                │              MongoDB
                                    │       ▼                                                                      │
  ▭ EE-001 ◀──────────────────────│  HTTP 200 response                                                            │
  REST Client                       │                                                                              │
                                    │  ← ← ← ← ← ← ← ← ← ← ← ← ← ← ERROR PATH ← ← ← ← ← ← ← ← ← ← ← ← ←    │
                                    │P10: Error Handling (ErrorProcessor)                                          │
                                    │  Exception → HTTP 400/500 response                          [DF-14]         │──▶ All Clients
                                    └──────────────────────────────────────────────────────────────────────────────┘
```

**Simplified routing view (3 entry routes + 1 logging route):**

```
  ┌────────────────────────────────────────────────────────────────────────┐
  │  Route 1: in_rest_creditRiskRouter                                     │
  │  jaxrs:bean:creditRiskRestEndpoint                                     │
  │  ──▶ P1 ──▶ P2 ──▶ P3 ──▶ P4 ──▶ P5 ──▶ P6 ──▶ P7 ──wireTap──▶ P9   │
  │  (error) ──▶ P10                                                       │
  └────────────────────────────────────────────────────────────────────────┘

  ┌────────────────────────────────────────────────────────────────────────┐
  │  Route 2: in_soap_creditRiskRouter                                     │
  │  cxf:bean:creditRiskSoapEndpoint                                       │
  │  ──▶ P1 ──▶ P2 ──▶ P3 ──▶ P4 ──▶ P5 ──▶ P6 ──wireTap──▶ P9          │
  │  (error) ──▶ P10                                                       │
  └────────────────────────────────────────────────────────────────────────┘

  ┌────────────────────────────────────────────────────────────────────────┐
  │  Route 3: in_gw_creditRiskRouter                                       │
  │  cxf:bean:creditRiskGwEndpoint                                         │
  │  ──▶ P8 ──▶ P1 ──▶ P2 ──▶ P3 ──▶ P4 ──▶ P5 ──▶ P6 ──wireTap──▶ P9  │
  │  (error) ──▶ P10                                                       │
  └────────────────────────────────────────────────────────────────────────┘

  ┌──────────────────────────────────────────────────┐
  │  Route 4: creditRisk_logging_route               │
  │  direct:logTransaction                           │
  │  ──▶ transactionLogger.logTransaction()          │
  │  ──▶ DS-001 (MongoDB)                            │
  └──────────────────────────────────────────────────┘
```

### Data Flows (Level 1)

| **Flow ID** | **From**          | **To**             | **Data Elements**                                                                                           | **Trigger**                     | **Volume/Frequency** | **Data Sensitivity** | **Annotations**                                              |
|-------------|-------------------|--------------------|-------------------------------------------------------------------------------------------------------------|---------------------------------|----------------------|----------------------|--------------------------------------------------------------|
| DF-01       | EE-001 (REST)     | P1                 | applicantId, firstName, lastName, dateOfBirth, SIN, employmentStatus, annualIncome, province, postalCode, productType, requestedAmount | HTTP GET request                | Per request          | 🔒 PII              | ⚠️ NO VALIDATION before P1                                  |
| DF-02       | EE-002 (SOAP)     | P1                 | CreditRiskReqType XML (same fields); WSS4J UsernameToken in SOAP header                                     | SOAP request                    | Per request          | 🔒 PII              | None                                                         |
| DF-03       | EE-003 (Gateway)  | P8                 | CreditRiskReqType XML (same fields); WSS4J UsernameToken                                                    | SOAP request                    | Per request          | 🔒 PII              | None                                                         |
| DF-04       | P1                | P2                 | Validated `CreditRiskReqType` (all fields pass CR-001→CR-007)                                               | Successful validation           | Per request          | 🔒 PII              | None                                                         |
| DF-05       | P2                | P3                 | `CreditRiskReqType` + selected `ScoringStrategy` instance stored in exchange property                       | Strategy resolved               | Per request          | 🔒 PII              | ⚪ HARDCODED: `defaultStrategy` fallback in ScoringStrategyProcessor |
| DF-06       | P3                | P4                 | `BureauInquiryRequest` (requestId, timestamp, subscriber, subject with PII, productType)                    | Request built                   | Per request          | 🔒 PII              | ⚪ HARDCODED: subscriberCode, subscriberName from config; operationName hardcoded as `"inquire"` |
| DF-07       | P4                | EE-004 (Bureau)    | SOAP envelope with `BureauInquiryRequest`                                                                   | CXF client invocation           | Per request          | 🔒 PII              | ⚠️ NO VALIDATION: schema-validation-enabled=false on bureauEndpoint |
| DF-08       | EE-004 (Bureau)   | P5                 | SOAP envelope with `BureauInquiryResponse` (creditScore, delinquencyCount, inquiryCount, openTradelineCount, totalCreditLimit, totalBalance) or error (errorCode, errorMessage) | Bureau response received | Per request | 🔒 SENSITIVE | None |
| DF-09       | P5                | P6                 | `CreditScoreDetail` (bureauScore, bureauScoreRange, delinquencyCount, inquiryCount, openAccountCount, totalCreditLimit, totalBalance, utilizationRate) | Response mapped | Per request | 🔒 SENSITIVE | None |
| DF-10       | P6                | P7 (REST) / output | `CreditRiskResType` (applicantId, riskCategory, overallScore, creditScoreDetail, incomeVerification, employmentRisk, debtService, riskFactors, recommendation) | Score calculated | Per request | 🔒 SENSITIVE | None |
| DF-11       | P7                | EE-001 (REST)      | HTTP 200 response with CreditRiskResType as application/xml                                                 | REST route only                 | Per request          | 🔒 SENSITIVE        | 📝 NOTE: content-type is application/xml even on REST endpoint |
| DF-12       | P6                | P9 (Wire Tap)      | Transaction data: transactionId, applicantId, province, productType, requestChannel, sourceSystem, timestamp, riskCategory, overallScore, recommendation | Wire Tap async | Per request | Non-Sensitive | 📝 NOTE: Async — does not block main flow |
| DF-13       | P9                | DS-001 (MongoDB)   | MongoDB document insert with transaction metadata                                                           | Wire Tap route triggered        | Per request          | Non-Sensitive        | ⚠️ NO VALIDATION: no schema enforcement on MongoDB document  |
| DF-14       | P10               | All Clients        | Error response: ValidationException → HTTP 400 with error code/message; other → HTTP 500 with CR-500        | Exception caught in doTry/doCatch | Per error          | Non-Sensitive        | None                                                         |

---

## 5. Level 2 Data Flow Diagrams

### 5.1 DFD-1.1: Request Validation Decomposition

**Diagram ID:** DFD-1.1  
**Parent Process:** P1 — Request Validation  
**Description:** Detailed decomposition of `CreditRiskRequestValidator`. Incoming request fields are validated against seven business rules in sequence. Any failure throws a `ValidationException` which is caught by P10.

#### Sub-Processes

| **Process ID** | **Process Name**             | **Description**                                                              |
|----------------|------------------------------|------------------------------------------------------------------------------|
| P1.1           | Validate ApplicantId         | CR-001: applicantId must not be null or blank                                |
| P1.2           | Validate FirstName           | CR-002: firstName must not be null or blank                                  |
| P1.3           | Validate LastName            | CR-003: lastName must not be null or blank                                   |
| P1.4           | Validate DateOfBirth         | CR-004: dateOfBirth must not be null; format must match `^\d{4}-\d{2}-\d{2}$` |
| P1.5           | Validate SocialInsuranceNum  | CR-005: SIN must not be null; format must match `^\d{3}-?\d{3}-?\d{3}$`     |
| P1.6           | Validate Province            | CR-006: province must be valid Canadian province code (see ProvinceType enum) |
| P1.7           | Validate PostalCode          | CR-007: postalCode must match `^[A-Za-z]\d[A-Za-z]\s?\d[A-Za-z]\d$`         |

#### Diagram

```
  [DF-01/02/03]
  CreditRiskReqType
       │
       ▼
  ┌─────────┐  FAIL    ┌──────────────────────────────────────────────┐
  │  P1.1   │─────────▶│  ValidationException (CR-001)                │──▶ P10
  │ Appl.Id │          └──────────────────────────────────────────────┘
  └────┬────┘
       │ PASS
       ▼
  ┌─────────┐  FAIL    ┌──────────────────────────────────────────────┐
  │  P1.2   │─────────▶│  ValidationException (CR-002)                │──▶ P10
  │ First   │          └──────────────────────────────────────────────┘
  │ Name    │
  └────┬────┘
       │ PASS
       ▼
  ┌─────────┐  FAIL    ┌──────────────────────────────────────────────┐
  │  P1.3   │─────────▶│  ValidationException (CR-003)                │──▶ P10
  │ Last    │          └──────────────────────────────────────────────┘
  │ Name    │
  └────┬────┘
       │ PASS
       ▼
  ┌─────────┐  FAIL    ┌──────────────────────────────────────────────┐
  │  P1.4   │─────────▶│  ValidationException (CR-004)                │──▶ P10
  │  DOB    │          └──────────────────────────────────────────────┘
  │ format  │
  └────┬────┘
       │ PASS
       ▼
  ┌─────────┐  FAIL    ┌──────────────────────────────────────────────┐
  │  P1.5   │─────────▶│  ValidationException (CR-005)                │──▶ P10
  │   SIN   │          └──────────────────────────────────────────────┘
  │ format  │
  └────┬────┘
       │ PASS
       ▼
  ┌─────────┐  FAIL    ┌──────────────────────────────────────────────┐
  │  P1.6   │─────────▶│  ValidationException (CR-006)                │──▶ P10
  │Province │          └──────────────────────────────────────────────┘
  └────┬────┘
       │ PASS
       ▼
  ┌─────────┐  FAIL    ┌──────────────────────────────────────────────┐
  │  P1.7   │─────────▶│  ValidationException (CR-007)                │──▶ P10
  │ Postal  │          └──────────────────────────────────────────────┘
  │  Code   │
  └────┬────┘
       │ PASS (all 7 rules)
       ▼
  [DF-04] Validated CreditRiskReqType ──▶ P2
```

#### Data Flows (Level 2)

| **Flow ID** | **From**       | **To**       | **Data Elements**                                                              | **Notes**                                                   |
|-------------|----------------|--------------|--------------------------------------------------------------------------------|-------------------------------------------------------------|
| DF-1.1-01   | P1 (entry)     | P1.1         | Raw `CreditRiskReqType.applicantId` (String)                                   | Null/blank check                                            |
| DF-1.1-02   | P1.1           | P1.2         | applicantId validated (pass)                                                   | Chain continues if valid                                    |
| DF-1.1-03   | P1.2           | P1.3         | firstName validated (pass)                                                     | Chain continues if valid                                    |
| DF-1.1-04   | P1.3           | P1.4         | lastName validated (pass)                                                      | Chain continues if valid                                    |
| DF-1.1-05   | P1.4           | P1.5         | dateOfBirth validated against `^\d{4}-\d{2}-\d{2}$` (pass)                    | Regex pattern match                                         |
| DF-1.1-06   | P1.5           | P1.6         | SIN validated against `^\d{3}-?\d{3}-?\d{3}$` (pass)                          | Regex pattern match; 🔒 PII                                 |
| DF-1.1-07   | P1.6           | P1.7         | province validated against ProvinceType enum (pass)                            | Enum value check                                            |
| DF-1.1-08   | P1.7           | P2           | postalCode validated against `^[A-Za-z]\d[A-Za-z]\s?\d[A-Za-z]\d$` (pass)     | All 7 rules passed → DF-04                                  |
| DF-1.1-ERR  | P1.1–P1.7      | P10          | `ValidationException` with error code (CR-001…CR-007) and message              | Any rule failure stops chain and routes to error handler    |

---

### 5.2 DFD-1.2: Scoring Decomposition

**Diagram ID:** DFD-1.2  
**Parent Process:** P2 (Strategy Selection) and P6 (Risk Score Calculation)  
**Description:** Detailed decomposition of strategy selection and risk score computation, including weight application, risk categorization, recommendation derivation, and affordability rating.

#### Sub-Processes

| **Process ID** | **Process Name**            | **Description**                                                                                             |
|----------------|-----------------------------|-------------------------------------------------------------------------------------------------------------|
| P2.1           | Resolve Strategy             | Maps productType to strategy: MORTGAGE/AUTO_LOAN→Conservative; CREDIT_CARD/LINE_OF_CREDIT→Aggressive; else→Standard (or config default) |
| P6.1           | Build Income Verification    | Calculates monthlyIncome=annualIncome/12, monthlyDebt=totalBalance×0.03, debtToIncomeRatio=monthlyDebt/monthlyIncome |
| P6.2           | Classify Employment Risk     | Maps employmentStatus → riskLevel: FULL_TIME→LOW; PART_TIME/RETIRED→MODERATE; SELF_EMPLOYED/CONTRACT→MODERATE_HIGH; UNEMPLOYED→HIGH |
| P6.3           | Build Debt Service Detail    | Calculates debtServiceRatio, totalDebtServiceRatio (with requested amount); derives affordabilityRating from strategy thresholds |
| P6.4           | Categorize Risk              | Applies strategy weights (bureau/DTI/utilization/employment) to produce riskCategory (EXCELLENT/GOOD/FAIR/POOR/VERY_POOR) |
| P6.5           | Calculate Overall Score      | Computes weighted overall score (0–100) from bureau score, DTI ratio, utilization rate, employment score      |
| P6.6           | Identify Risk Factors        | Flags: LOW_CREDIT_SCORE (<650), HIGH_CREDIT_UTILIZATION (>50%), PAST_DELINQUENCIES (>0), EXCESSIVE_CREDIT_INQUIRIES (>5), HIGH_DEBT_TO_INCOME (>40%), NO_EMPLOYMENT_INCOME (UNEMPLOYED) |
| P6.7           | Determine Recommendation     | Maps riskCategory+requestedAmount+annualIncome → APPROVE / APPROVE_WITH_CONDITIONS / REFER_TO_UNDERWRITER / DECLINE |

#### Diagram

```
  [DF-04] Validated CreditRiskReqType
       │
       ▼
  ┌────────────────────────────────────────────────────────────────────┐
  │  P2: Strategy Selection                                            │
  │                                                                    │
  │  ┌─────────┐   productType                                        │
  │  │  P2.1   │   MORTGAGE/AUTO_LOAN ──────▶ ConservativeScoringStrategy │
  │  │ Resolve │   CREDIT_CARD/LINE_OF_CREDIT ▶ AggressiveScoringStrategy │
  │  │Strategy │   else ────────────────────▶ StandardScoringStrategy │
  │  └────┬────┘                          ⚪ HARDCODED defaultStrategy  │
  └───────┼────────────────────────────────────────────────────────────┘
          │ DF-05 (CreditRiskReqType + ScoringStrategy)
          ▼
  [P3 → P4 → P5: Bureau call — see DFD-1.3]
          │ DF-09 (CreditScoreDetail)
          ▼
  ┌────────────────────────────────────────────────────────────────────┐
  │  P6: Risk Score Calculation                                        │
  │                                                                    │
  │  ┌─────────┐          ┌─────────┐          ┌─────────┐           │
  │  │  P6.1   │          │  P6.2   │          │  P6.3   │           │
  │  │ Income  │          │ Employ  │          │  Debt   │           │
  │  │ Verif.  │          │  Risk   │          │ Service │           │
  │  └────┬────┘          └────┬────┘          └────┬────┘           │
  │       │                   │                     │                 │
  │       └───────────────────┴─────────────────────┘                │
  │                           │                                       │
  │                    ┌──────▼──────┐                               │
  │                    │    P6.4     │                               │
  │                    │ Categorize  │                               │
  │                    │    Risk     │                               │
  │                    └──────┬──────┘                               │
  │                           │                                       │
  │                    ┌──────▼──────┐                               │
  │                    │    P6.5     │                               │
  │                    │  Calculate  │                               │
  │                    │   Overall   │                               │
  │                    │    Score    │                               │
  │                    └──────┬──────┘                               │
  │                           │                                       │
  │                    ┌──────▼──────┐                               │
  │                    │    P6.6     │                               │
  │                    │  Identify   │                               │
  │                    │    Risk     │                               │
  │                    │   Factors   │                               │
  │                    └──────┬──────┘                               │
  │                           │                                       │
  │                    ┌──────▼──────┐                               │
  │                    │    P6.7     │                               │
  │                    │ Determine   │                               │
  │                    │Recommenda-  │                               │
  │                    │   tion      │                               │
  │                    └──────┬──────┘                               │
  └───────────────────────────┼────────────────────────────────────────┘
                              │ DF-10 (CreditRiskResType)
                              ▼
                    [P7 REST formatting / direct response]
```

#### Data Flows (Level 2)

| **Flow ID** | **From** | **To**  | **Data Elements**                                                                                              | **Notes**                                                         |
|-------------|----------|---------|----------------------------------------------------------------------------------------------------------------|-------------------------------------------------------------------|
| DF-1.2-01   | P2 entry | P2.1    | productType (String from CreditRiskReqType)                                                                    | Case-insensitive matching                                         |
| DF-1.2-02   | P2.1     | P3      | Selected `ScoringStrategy` instance stored in exchange; CreditRiskReqType passed through                       | ⚪ HARDCODED: defaultStrategy config key `SCORING_DEFAULT_STRATEGY` |
| DF-1.2-03   | P5 → P6  | P6.1    | annualIncome (Double), totalBalance (from CreditScoreDetail)                                                   | monthlyDebt = totalBalance × 0.03 (hardcoded factor)              |
| DF-1.2-04   | P5 → P6  | P6.2    | employmentStatus (String)                                                                                      | ⚪ HARDCODED risk level mapping in code                           |
| DF-1.2-05   | P5 → P6  | P6.3    | totalMonthlyDebt, totalMonthlyIncome, requestedAmount                                                          | Affordability thresholds differ per strategy                      |
| DF-1.2-06   | P6.1–P6.3 | P6.4   | debtToIncomeRatio, utilizationRate, bureauScore, employmentRiskLevel                                           | Strategy-specific weight application                              |
| DF-1.2-07   | P6.4     | P6.5    | riskCategory, bureauScore, DTI ratio, utilization rate, employment score                                       | Weighted sum formula applied                                      |
| DF-1.2-08   | P6.5     | P6.6    | overallScore (0–100), bureauScore, utilizationRate, delinquencyCount, inquiryCount, DTI, employmentStatus      | Six risk factor flags evaluated                                   |
| DF-1.2-09   | P6.6     | P6.7    | riskCategory, requestedAmount, annualIncome, riskFactors list                                                  | Four possible recommendations                                     |
| DF-1.2-10   | P6.7     | P7/out  | Complete `CreditRiskResType`                                                                                   | Includes all sub-detail objects                                   |

---

### 5.3 DFD-1.3: Bureau Integration Decomposition

**Diagram ID:** DFD-1.3  
**Parent Process:** P3 (Bureau Request Building), P4 (Bureau Service Call), P5 (Bureau Response Mapping)  
**Description:** Detailed flow of bureau request construction, SOAP call, response mapping, and error handling paths.

#### Sub-Processes

| **Process ID** | **Process Name**            | **Description**                                                                                  |
|----------------|-----------------------------|--------------------------------------------------------------------------------------------------|
| P3.1           | Map Subscriber Info         | Sets hardcoded subscriberCode and subscriberName from configuration into BureauSubscriber        |
| P3.2           | Map Subject Info            | Copies applicant PII (firstName, lastName, DOB, SIN, province, postalCode) into BureauSubject    |
| P3.3           | Generate RequestId          | Generates requestId = `{requestIdPrefix}-{UUID}` and ISO 8601 timestamp                         |
| P4.1           | Set CXF Headers             | Sets Camel headers: operationName=`inquire`, operationNamespace=`http://ws.esb.nexgen.com/bureau/v1` |
| P4.2           | Invoke Bureau SOAP           | Sends SOAP request to `BUREAU_ENDPOINT_URL` via CXF client; schema-validation-enabled=false      |
| P5.1           | Check Bureau Error           | Inspects response: null response → CR-301; errorCode present → CR-302; sets BUREAU_ERROR flag    |
| P5.2           | Map Score Data               | Maps creditScore, delinquencyCount, inquiryCount, openTradelineCount→openAccountCount, totalCreditLimit, totalBalance |
| P5.3           | Categorize Score Range       | Maps bureauScore to range: EXCEPTIONAL≥800, VERY_GOOD≥740, GOOD≥670, FAIR≥580, POOR<580         |
| P5.4           | Calculate Utilization Rate   | utilizationRate = totalBalance / totalCreditLimit (guarded: returns 0 if totalCreditLimit = 0)   |

#### Diagram

```
  [DF-05] CreditRiskReqType + ScoringStrategy
       │
       ▼
  ┌────────────────────────────────────────────────────────────────────┐
  │  P3: Bureau Request Building (BureauRequestBuilder)                │
  │                                                                    │
  │  ┌─────────┐    ┌─────────┐    ┌──────────┐                      │
  │  │  P3.1   │    │  P3.2   │    │   P3.3   │                      │
  │  │Subscrib │    │ Subject │    │ RequestId│                      │
  │  │  Info   │    │  Info   │    │ + Stamp  │                      │
  │  │⚪HARDCODED│  │ 🔒PII  │    │⚪HARDCODED│                     │
  │  └────┬────┘    └────┬────┘    └────┬─────┘                      │
  │       └─────────────┴──────────────┘                              │
  │                      │ DF-06 (BureauInquiryRequest)               │
  └──────────────────────┼─────────────────────────────────────────────┘
                         │
                         ▼
  ┌────────────────────────────────────────────────────────────────────┐
  │  P4: Bureau Service Call                                           │
  │                                                                    │
  │  ┌─────────┐           ┌─────────┐                               │
  │  │  P4.1   │           │  P4.2   │                               │
  │  │Set CXF  │──────────▶│ Invoke  │──[DF-07]──▶ ▭ EE-004         │
  │  │ Headers │           │  SOAP   │◀─[DF-08]──── ▭ EE-004         │
  │  │⚪HARDCODED│         │⚠️ NO    │                               │
  │  │operNm   │           │SCHEMA   │                               │
  │  └─────────┘           │VALID.   │                               │
  │                        └────┬────┘                               │
  └─────────────────────────────┼──────────────────────────────────────┘
                                │ BureauInquiryResponse (or null/error)
                                ▼
  ┌────────────────────────────────────────────────────────────────────┐
  │  P5: Bureau Response Mapping (BureauResponseMapper)                │
  │                                                                    │
  │  ┌─────────┐  ERROR     ┌──────────────────────────────────────┐  │
  │  │  P5.1   │──────────▶│ BUREAU_ERROR flag set                │  │
  │  │  Check  │            │ CR-301 (null) / CR-302 (errorCode)   │──┼──▶ P10
  │  │  Error  │            └──────────────────────────────────────┘  │
  │  └────┬────┘                                                       │
  │       │ OK                                                         │
  │       ▼                                                            │
  │  ┌─────────┐    ┌─────────┐    ┌──────────┐                      │
  │  │  P5.2   │    │  P5.3   │    │   P5.4   │                      │
  │  │  Map    │    │Categorize│   │Calculate │                      │
  │  │ Score   │    │  Range  │    │Utilization│                     │
  │  │  Data   │    │         │    │   Rate   │                      │
  │  └────┬────┘    └────┬────┘    └────┬─────┘                      │
  │       └─────────────┴──────────────┘                              │
  │                      │ DF-09 (CreditScoreDetail)                  │
  └──────────────────────┼─────────────────────────────────────────────┘
                         │
                         ▼
                    [P6 Risk Score Calculation]
```

#### Data Flows (Level 2)

| **Flow ID** | **From** | **To**   | **Data Elements**                                                                                    | **Notes**                                                                       |
|-------------|----------|----------|------------------------------------------------------------------------------------------------------|---------------------------------------------------------------------------------|
| DF-1.3-01   | P3 entry | P3.1     | Nothing (reads from config: subscriberCode, subscriberName)                                          | ⚪ HARDCODED: config values `BUREAU_SUBSCRIBER_CODE`, `BUREAU_SUBSCRIBER_NAME`  |
| DF-1.3-02   | P3 entry | P3.2     | firstName, lastName, dateOfBirth, SIN, province, postalCode from CreditRiskReqType                   | 🔒 PII fields copied directly                                                   |
| DF-1.3-03   | P3 entry | P3.3     | requestIdPrefix from config; UUID generated at runtime; system timestamp                              | ⚪ HARDCODED: prefix from `BUREAU_REQUEST_ID_PREFIX` config                     |
| DF-1.3-04   | P3.1–P3.3 | P4      | Complete `BureauInquiryRequest` object (DF-06)                                                       | Assembled from three sub-processes                                               |
| DF-1.3-05   | P4 entry | P4.1     | BureauInquiryRequest in exchange body                                                                 | ⚪ HARDCODED: operationName=`"inquire"`, operationNamespace hardcoded            |
| DF-1.3-06   | P4.1     | P4.2     | BureauInquiryRequest + CXF headers set                                                               | None                                                                             |
| DF-1.3-07   | P4.2     | EE-004   | SOAP envelope: BureauInquiryRequest (DF-07)                                                          | ⚠️ NO VALIDATION: schema-validation-enabled=false on bureauEndpoint             |
| DF-1.3-08   | EE-004   | P5.1     | SOAP envelope: BureauInquiryResponse or null (DF-08)                                                 | Network timeout/error possible                                                   |
| DF-1.3-09   | P5.1     | P10      | BUREAU_ERROR flag + error code CR-301/CR-302 (on error path)                                         | Error routes to ErrorProcessor                                                   |
| DF-1.3-10   | P5.1     | P5.2     | Valid BureauInquiryResponse body                                                                     | Null/error check passed                                                          |
| DF-1.3-11   | P5.2     | P5.3     | bureauScore (Integer), delinquencyCount, inquiryCount, openTradelineCount, totalCreditLimit, totalBalance | Raw numeric fields from bureau                                               |
| DF-1.3-12   | P5.3     | P5.4     | bureauScoreRange (String: EXCEPTIONAL/VERY_GOOD/GOOD/FAIR/POOR)                                      | Categorization thresholds: ≥800/≥740/≥670/≥580/<580                            |
| DF-1.3-13   | P5.4     | P6       | Complete `CreditScoreDetail` with utilizationRate (DF-09)                                            | utilizationRate=totalBalance/totalCreditLimit; guard: 0 if limit=0              |

---

## 6. Data Flow Catalog

Complete reference of all data flows across all DFD levels.

| **Flow ID** | **Level** | **Source**          | **Destination**       | **Data Elements**                                                                 | **Direction** | **Protocol/Format**       | **Frequency** | **Data Sensitivity** |
|-------------|-----------|---------------------|-----------------------|-----------------------------------------------------------------------------------|---------------|---------------------------|---------------|----------------------|
| DF-01       | 0, 1      | EE-001 (REST)       | P1                    | 11 query params (applicantId, firstName, lastName, DOB, SIN, province, postalCode, productType, loanAmount, income, employment) | In | REST/HTTP GET URL params | Per request | 🔒 PII |
| DF-02       | 0, 1      | EE-002 (SOAP)       | P1                    | CreditRiskReqType XML + WSS4J UsernameToken                                        | In            | SOAP/XML + WS-Security    | Per request   | 🔒 PII               |
| DF-03       | 0, 1      | EE-003 (Gateway)    | P8                    | CreditRiskReqType XML + WSS4J UsernameToken                                        | In            | SOAP/XML + WS-Security    | Per request   | 🔒 PII               |
| DF-04       | 1         | P1                  | P2                    | Validated `CreditRiskReqType`                                                      | Internal      | Java Object               | Per request   | 🔒 PII               |
| DF-05       | 1         | P2                  | P3                    | `CreditRiskReqType` + selected `ScoringStrategy`                                  | Internal      | Java Object               | Per request   | 🔒 PII               |
| DF-06       | 1         | P3                  | P4                    | `BureauInquiryRequest` (requestId, timestamp, subscriber, subject, productType)    | Internal      | Java Object               | Per request   | 🔒 PII               |
| DF-07       | 0, 1      | P4                  | EE-004 (Bureau)       | SOAP envelope with BureauInquiryRequest                                            | Out           | SOAP/XML                  | Per request   | 🔒 PII               |
| DF-08       | 0, 1      | EE-004 (Bureau)     | P5                    | SOAP envelope with BureauInquiryResponse (or error)                               | In            | SOAP/XML                  | Per request   | 🔒 SENSITIVE          |
| DF-09       | 1         | P5                  | P6                    | `CreditScoreDetail` (bureauScore, bureauScoreRange, delinquencyCount, inquiryCount, openAccountCount, totalCreditLimit, totalBalance, utilizationRate) | Internal | Java Object | Per request | 🔒 SENSITIVE |
| DF-10       | 1         | P6                  | P7 / output           | `CreditRiskResType` (applicantId, riskCategory, overallScore, creditScoreDetail, incomeVerification, employmentRisk, debtService, riskFactors, recommendation) | Internal | Java Object | Per request | 🔒 SENSITIVE |
| DF-11       | 0, 1      | P7                  | EE-001 (REST)         | HTTP 200 + CreditRiskResType as application/xml                                    | Out           | REST/HTTP 200 + XML       | Per request   | 🔒 SENSITIVE          |
| DF-12       | 1         | P6                  | P9 (Wire Tap)         | Transaction metadata subset (transactionId, applicantId, province, productType, requestChannel, sourceSystem, timestamp, riskCategory, overallScore, recommendation) | Internal | Java Object (async) | Per request | Non-Sensitive |
| DF-13       | 0, 1      | P9                  | DS-001 (MongoDB)      | MongoDB BSON document (transaction log)                                            | Out           | MongoDB Wire Protocol     | Per request   | Non-Sensitive         |
| DF-14       | 0, 1      | P10                 | All Clients           | Error response (HTTP 400 or 500)                                                   | Out           | REST/HTTP or SOAP/XML     | Per error     | Non-Sensitive         |
| DF-1.1-01–08 | 2        | P1 sub-processes    | P1 sub-processes      | Individual field validations through CR-001→CR-007 rules                           | Internal      | Java Object (chained)     | Per request   | 🔒 PII               |
| DF-1.2-01–10 | 2        | P2/P6 sub-processes | P2/P6 sub-processes   | Strategy resolution, weight application, score calculation, recommendation          | Internal      | Java Object               | Per request   | 🔒 SENSITIVE          |
| DF-1.3-01–13 | 2        | P3/P4/P5 sub-procs  | P3/P4/P5 sub-procs    | Bureau request building, SOAP call, response mapping                               | Internal/Ext  | Java Object / SOAP/XML    | Per request   | 🔒 PII / 🔒 SENSITIVE |

---

## 7. Data Store Catalog

Complete reference of all data stores.

| **Store ID** | **Store Name**           | **Type**    | **Used By Processes** | **Key Data Elements**                                                                           | **Entity Ref**                     |
|--------------|--------------------------|-------------|------------------------|--------------------------------------------------------------------------------------------------|------------------------------------|
| DS-001       | MongoDB Transaction Log  | Database    | P9                     | transactionId, applicantId, province, productType, requestChannel, sourceSystem, timestamp, riskCategory, overallScore, recommendation, status, errorCode, errorMessage | `TransactionLogger.logTransaction()` |
| DS-002       | Camel Exchange (Memory)  | In-Memory   | P1, P2, P3, P4, P5, P6, P7, P8, P9, P10 | `CreditRiskReqType`, `BureauInquiryRequest`, `BureauInquiryResponse`, `CreditScoreDetail`, `CreditRiskResType`, selected `ScoringStrategy` | All processor classes |

### DS-001: MongoDB Document Schema

```json
{
  "transactionId":   "<String — UUID from RequestHeader>",
  "applicantId":     "<String>",
  "province":        "<String — Canadian province code>",
  "productType":     "<String>",
  "requestChannel":  "<String — ONLINE/MOBILE/BRANCH/BROKER/CALL_CENTER/API/BATCH>",
  "sourceSystem":    "<String>",
  "timestamp":       "<String — ISO 8601>",
  "riskCategory":    "<String — EXCELLENT/GOOD/FAIR/POOR/VERY_POOR/ERROR>",
  "overallScore":    "<Integer — 0..100>",
  "recommendation":  "<String — APPROVE/APPROVE_WITH_CONDITIONS/REFER_TO_UNDERWRITER/DECLINE>",
  "status":          "<String — success/error>",
  "errorCode":       "<String — null on success>",
  "errorMessage":    "<String — null on success>"
}
```

**📝 NOTE:** PII fields (firstName, lastName, DOB, SIN, postalCode) are intentionally **excluded** from the MongoDB log document. The logged `applicantId` is an internal identifier. No TTL index is configured — documents are retained permanently unless manually purged.

### DS-002: In-Memory Model Objects

| **Class**               | **Key Fields**                                                                                                               |
|-------------------------|------------------------------------------------------------------------------------------------------------------------------|
| `CreditRiskReqType`     | requestHeader, applicantId, firstName, lastName, dateOfBirth, socialInsuranceNumber, employmentStatus, annualIncome, province, postalCode, requestChannel, productType, requestedAmount |
| `BureauInquiryRequest`  | requestId, timestamp, subscriber (subscriberCode, subscriberName), subject (firstName, lastName, DOB, SIN, province, postalCode), productType |
| `BureauInquiryResponse` | requestId, responseId, creditScore, delinquencyCount, inquiryCount, openTradelineCount, totalCreditLimit, totalBalance, errorCode, errorMessage |
| `CreditScoreDetail`     | bureauScore, bureauScoreRange, delinquencyCount, inquiryCount, openAccountCount, totalCreditLimit, totalBalance, utilizationRate |
| `CreditRiskResType`     | responseHeader, applicantId, riskCategory, overallScore, creditScoreDetail, incomeVerification, employmentRisk, debtService, riskFactors, recommendation, accuracyCode, scoringModelVersion |

---

## 8. Data Transformation Rules

All significant data mappings that occur within processes.

| **Process** | **Input Data**                                              | **Transformation**                                                                                        | **Output Data**                               | **Hardcoded Override**                                                          | **Business Rule**   |
|-------------|-------------------------------------------------------------|-----------------------------------------------------------------------------------------------------------|-----------------------------------------------|---------------------------------------------------------------------------------|---------------------|
| P1          | Raw request fields (String)                                 | Null/blank check; regex pattern match for DOB, SIN, postal code; enum lookup for province                 | Validated `CreditRiskReqType`                 | None                                                                            | CR-001→CR-007       |
| P2          | `productType` (String)                                      | Case-insensitive match: MORTGAGE/AUTO_LOAN→Conservative; CREDIT_CARD/LINE_OF_CREDIT→Aggressive; else→Standard | Selected `ScoringStrategy` instance        | `defaultStrategy` config `SCORING_DEFAULT_STRATEGY` (falls back to STANDARD)   | None                |
| P3          | `CreditRiskReqType`                                         | Field copy to `BureauSubject`; subscriber from config; requestId=prefix+UUID; timestamp=ISO8601 now       | `BureauInquiryRequest`                        | subscriberCode=`BUREAU_SUBSCRIBER_CODE`; subscriberName=`BUREAU_SUBSCRIBER_NAME`; requestIdPrefix=`BUREAU_REQUEST_ID_PREFIX` | None |
| P4          | `BureauInquiryRequest`                                      | Wraps in SOAP envelope; CXF header: operationName=`inquire`, ns=`http://ws.esb.nexgen.com/bureau/v1`      | SOAP request to bureau                        | operationName `"inquire"` hardcoded in `BureauRequestBuilder`                  | None                |
| P5 (score)  | `BureauInquiryResponse.creditScore` (Integer)               | Score range categorization: ≥800→EXCEPTIONAL, ≥740→VERY_GOOD, ≥670→GOOD, ≥580→FAIR, <580→POOR           | `CreditScoreDetail.bureauScoreRange` (String) | None                                                                            | None                |
| P5 (util.)  | `totalBalance`, `totalCreditLimit` (Double)                 | utilizationRate = totalBalance / totalCreditLimit; guard: 0.0 if totalCreditLimit = 0                     | `CreditScoreDetail.utilizationRate` (Double 0–1+) | None                                                                        | None                |
| P5 (field)  | `openTradelineCount` (Integer)                              | Direct field rename                                                                                       | `openAccountCount` (Integer)                  | None                                                                            | None                |
| P6 (income) | `annualIncome` (Double), `totalBalance` (Double)            | monthlyIncome=annualIncome/12.0; monthlyDebt=totalBalance×0.03; DTI=monthlyDebt/monthlyIncome             | `IncomeVerificationDetail.debtToIncomeRatio` (Double) | 0.03 factor hardcoded (assumed 3% of balance as monthly obligation)      | None                |
| P6 (employ) | `employmentStatus` (String)                                 | FULL_TIME→LOW; PART_TIME/RETIRED→MODERATE; SELF_EMPLOYED/CONTRACT→MODERATE_HIGH; UNEMPLOYED→HIGH         | `EmploymentRiskDetail.riskLevel` (String)     | Risk level mapping hardcoded in `RiskScoreCalculator`                           | None                |
| P6 (afford) | `debtServiceRatio` (Double) — varies by strategy            | Standard: <28%=COMFORTABLE, <36%=MANAGEABLE, <44%=STRETCHED, ≥44%=OVEREXTENDED                           | `DebtServiceDetail.affordabilityRating`       | Thresholds hardcoded per strategy class                                         | None                |
| P6 (score)  | bureauScore, DTI ratio, utilization rate, employment score  | Standard weights: bureau×40%+DTI×30%+utilization×20%+employment×10%; Conservative: 35/35/15/15; Aggressive: 50/25/15/10 | `CreditRiskResType.overallScore` (0–100) | Weight values hardcoded in each ScoringStrategy implementation             | None                |
| P6 (rec.)   | riskCategory, requestedAmount, annualIncome                 | EXCELLENT/GOOD→APPROVE; FAIR+conditions→APPROVE_WITH_CONDITIONS; threshold→REFER_TO_UNDERWRITER; POOR/VERY_POOR→DECLINE | `recommendation` (String)             | Threshold values hardcoded in strategy                                          | None                |
| P7          | `CreditRiskResType` Java object                             | Sets HTTP response code=200; content-type=application/xml                                                 | HTTP 200 response with XML body               | ⚪ HARDCODED HTTP 200 and content-type                                          | None                |
| P8          | Incoming Gateway SOAP request                               | sourceSystem defaulted to "GATEWAY"; requestChannel defaulted to "API"; transactionId=UUID; timestamp=now | `CreditRiskReqType` with headers set          | ⚪ HARDCODED: sourceSystem="GATEWAY", requestChannel="API"                      | None                |
| P10         | `ValidationException`                                       | Maps to HTTP 400 + validation error code/message                                                          | Error response                                | ⚪ HARDCODED HTTP 400 for validation; 500 for all other exceptions; CR-500 code | None                |

---

## 9. Notes & Observations

| **#** | **Observation**                                                                                                                                      | **Related DFD** | **Impact**    |
|-------|------------------------------------------------------------------------------------------------------------------------------------------------------|-----------------|---------------|
| 1     | `RestResponseBuilder` sets content-type to `application/xml` despite the service being exposed at a REST endpoint. JSON serialization is not used on the main response path. | DFD-1 (P7)     | Medium — REST clients may expect JSON |
| 2     | Bureau CXF endpoint has `schema-validation-enabled=false`, meaning malformed bureau responses are accepted without schema enforcement.                | DFD-1.3 (P4.2)  | High — silent data corruption risk |
| 3     | Wire Tap logging (P9 → DS-001) does not retry on MongoDB failure. If MongoDB is unavailable, the transaction log entry is lost with no alert.          | DFD-1 (P9)      | Medium — audit trail gaps possible |
| 4     | MongoDB documents have no TTL index configured. Data accumulates indefinitely.                                                                        | DS-001           | Low — operational risk over time   |
| 5     | The `defaultStrategy` configuration key `SCORING_DEFAULT_STRATEGY` is injected into both `ScoringStrategyProcessor` and `RiskScoreCalculator` separately. These could become inconsistent. | DFD-1 (P2, P6) | Medium — inconsistent scoring risk |
| 6     | The `monthlyDebt` calculation uses a hardcoded factor of 0.03 (3% of total balance), which may not reflect actual minimum payment obligations.         | DFD-1.2 (P6.1)  | Medium — inaccurate affordability   |
| 7     | No input sanitization is applied before regex validation in P1. While ValidationException is thrown, the raw input is logged, creating potential log injection risk. | DFD-1.1         | Medium — security concern          |
| 8     | `CreditRiskRestSvc.assessCreditRisk()` has 11 query parameters but the REST query parameter `loanAmount` maps to `requestedAmount` in the model — naming inconsistency. | DFD-0 (DF-01) | Low — documentation gap            |
| 9     | The Gateway route (Route 3) and SOAP route (Route 2) are nearly identical except for P8 preprocessing. There is no deduplication of the common processing chain. | DFD-1           | Low — maintainability concern      |
| 10    | No circuit breaker or retry pattern is configured for the external bureau CXF call (P4). A single bureau timeout will propagate directly to the client. | DFD-1.3 (P4.2)  | High — resilience gap              |

---

## 10. Dead Data Flow Paths

Data flow paths that exist in the source code but are never triggered in production.

| **Flow ID**  | **DFD Level** | **From**                | **To**                 | **Description**                                                                                                          | **Evidence**                                                                                       | **Recommendation**                                       |
|--------------|---------------|-------------------------|------------------------|--------------------------------------------------------------------------------------------------------------------------|----------------------------------------------------------------------------------------------------|----------------------------------------------------------|
| DF-DEAD-01   | 1             | P6 (RiskScoreCalculator) | Response (accuracyCode) | `accuracyCode` and `scoringModelVersion` fields exist in `CreditRiskResType` but are never populated by any processor.   | `CreditRiskResType.accuracyCode` and `scoringModelVersion` are declared but no setter is called in `RiskScoreCalculator` | Remove fields or implement model version tracking  |
| DF-DEAD-02   | 1             | P6 (RiskScoreCalculator) | IncomeVerificationDetail | `verifiedIncome` field in `IncomeVerificationDetail` is never populated — only `reportedIncome` is set from `annualIncome` | `IncomeVerificationDetail.verifiedIncome` always null; `verificationStatus` hardcoded to "SELF_REPORTED" | Remove `verifiedIncome` or implement verification logic |
| DF-DEAD-03   | 1             | P6 (EmploymentRiskDetail) | Response               | `yearsEmployed` and `industryCategory` fields in `EmploymentRiskDetail` are never populated.                             | No source populates these fields; always null in response                                           | Remove fields or source them from request                |
| DF-DEAD-04   | 2             | P2 (ScoringStrategyProcessor) | ConservativeScoringStrategy | The `defaultStrategy="conservative"` branch in `resolveStrategy()` is unreachable: it only triggers if productType doesn't match AND config=conservative, but MORTGAGE/AUTO_LOAN already route to Conservative. | Code logic in `ScoringStrategyProcessor.resolveStrategy()` | Simplify conditional logic                          |

---

## 11. Transaction Logging Data Flow Detail

### 11.1 Logging Flow Overview

| **Attribute**                | **Details**                                                                                                  |
|------------------------------|--------------------------------------------------------------------------------------------------------------|
| Log Data Store               | MongoDB — collection configurable via `MONGO_COLLECTION` (default unknown; must be set at deployment)        |
| Logging Trigger              | After every successful risk score calculation (DF-12); also on error path via P10 setting status=error       |
| Synchronous / Asynchronous   | **Async** — Camel Wire Tap pattern (`<wireTap uri="direct:logTransaction"/>`)                                |
| Data Logged                  | Transaction metadata only (no PII): transactionId, applicantId, province, productType, requestChannel, sourceSystem, riskCategory, overallScore, recommendation, status, errorCode, errorMessage |
| Failure Impact on Main Flow  | **Main flow continues silently** — Wire Tap runs on a separate thread; MongoDB failures do not affect response |
| Data Retention               | **Permanent** — no TTL index configured; manual purge required                                               |

### 11.2 Logging Data Elements

| **#** | **Data Element**  | **Source**                          | **Logged As**       | **Sensitivity**  | **Notes**                                                    |
|-------|-------------------|-------------------------------------|---------------------|------------------|--------------------------------------------------------------|
| 1     | transactionId     | `RequestHeader.transactionId`       | `transactionId`     | Non-Sensitive    | UUID generated by client or by P8 (Gateway route)            |
| 2     | applicantId       | `CreditRiskReqType.applicantId`     | `applicantId`       | Non-Sensitive    | Internal identifier; not PII per se but links to applicant   |
| 3     | province          | `CreditRiskReqType.province`        | `province`          | Non-Sensitive    | Canadian province code                                        |
| 4     | productType       | `CreditRiskReqType.productType`     | `productType`       | Non-Sensitive    | Product category                                             |
| 5     | requestChannel    | `CreditRiskReqType.requestChannel`  | `requestChannel`    | Non-Sensitive    | Defaults to API for Gateway route                            |
| 6     | sourceSystem      | `RequestHeader.sourceSystem`        | `sourceSystem`      | Non-Sensitive    | Defaults to GATEWAY for Gateway route                        |
| 7     | timestamp         | `RequestHeader.timestamp`           | `timestamp`         | Non-Sensitive    | ISO 8601 format                                              |
| 8     | riskCategory      | `CreditRiskResType.riskCategory`    | `riskCategory`      | 🔒 SENSITIVE     | Business-sensitive risk classification                        |
| 9     | overallScore      | `CreditRiskResType.overallScore`    | `overallScore`      | 🔒 SENSITIVE     | Proprietary scoring result                                    |
| 10    | recommendation    | `CreditRiskResType.recommendation`  | `recommendation`    | 🔒 SENSITIVE     | Credit decision recommendation                               |
| 11    | status            | Set by `TransactionLogger`          | `status`            | Non-Sensitive    | "success" or "error"                                         |
| 12    | errorCode         | Exchange property (on error)        | `errorCode`         | Non-Sensitive    | Null on success                                              |
| 13    | errorMessage      | Exchange property (on error)        | `errorMessage`      | Non-Sensitive    | Null on success                                              |

### 11.3 Wire Tap Async Flow

```
  P6 (RiskScoreCalculator) completes
       │
       ├──[DF-10]──▶ P7 (RestResponseBuilder) or direct SOAP response
       │
       └──[DF-12 WIRE TAP — async]──▶ Route 4: creditRisk_logging_route
                                              │
                                              ▼
                                    TransactionLogger.logTransaction()
                                              │
                                              ▼
                                    MongoDB MongoClient
                                    db.getCollection(MONGO_COLLECTION)
                                    collection.insertOne(document)
                                              │
                                              ▼
                                    ═══════════════════════
                                    ║  DS-001 MongoDB     ║
                                    ║  Transaction Log    ║
                                    ═══════════════════════
                                    (failure silently ignored)
```

---

## 12. Target State Data Flow (Proposed)

### 12.1 Key Changes from Current State

| **Change #** | **Description of Change**                                                                       | **Current DFD** | **Reason**                                                    |
|--------------|--------------------------------------------------------------------------------------------------|-----------------|---------------------------------------------------------------|
| 1            | Add API Gateway (e.g., AWS API Gateway / Kong) as entry point for all external REST/SOAP flows   | DFD-0           | Centralized AuthN/Z, rate limiting, TLS termination           |
| 2            | Add input sanitization before P1 validation to prevent log injection                             | DFD-1.1         | Addresses observation #7 (security concern)                   |
| 3            | Enable schema validation on bureau CXF endpoint (`schema-validation-enabled=true`)              | DFD-1.3 (P4.2)  | Addresses observation #2 (silent data corruption)             |
| 4            | Add retry + circuit breaker on bureau call (P4)                                                  | DFD-1.3 (P4.2)  | Addresses observation #10 (resilience gap)                    |
| 5            | Add MongoDB TTL index on Transaction Log (DS-001)                                                | DS-001          | Addresses observation #4 (unbounded data growth)              |
| 6            | Add MongoDB write retry / dead-letter queue in logging route (Route 4)                           | DFD-1 (P9)      | Addresses observation #3 (silent audit trail gaps)            |
| 7            | Remove dead fields: accuracyCode, scoringModelVersion, verifiedIncome, yearsEmployed, industryCategory | DFD-DEAD-01–03 | Addresses dead data flow paths                               |
| 8            | Consolidate REST and SOAP response formatting (content-type negotiation)                         | DFD-1 (P7)      | Addresses observation #1 (JSON vs XML content-type)           |

### 12.2 Target State Context Diagram (Proposed)

```
                                   ┌──────────────────────┐
                                   │     API GATEWAY      │
                                   │  (AuthN/Z, Rate Limit│
                                   │   TLS Termination)   │
                                   └──────────┬───────────┘
                                              │
                    ┌─────────────────────────┼─────────────────────────┐
  [REST Request]    │                         │                         │  [Bureau SOAP]
▭ EE-001 ─────────▶│                         │                         │──────────────▶ ▭ EE-004
  REST Client       │   CREDIT RISK           │                         │◀────────────── Credit Bureau
  [REST Response]   │   ASSESSMENT            │  [+ Input Sanitization] │
▭ EE-001 ◀─────────│   SERVICE               │  [+ Schema Validation]  │
                    │   (P0-TARGET)           │  [+ Circuit Breaker]    │  [Log Insert + Retry]
  [SOAP Request]    │                         │                         │──────────────▶ ═══════════
▭ EE-002 ─────────▶│                         │                         │               DS-001
  SOAP Client       │                         │                         │               MongoDB
  [SOAP Response]   │                         │                         │               (+ TTL Index)
▭ EE-002 ◀─────────│                         │                         │
                    │                         │                         │
  [GW Request]      │                         │                         │
▭ EE-003 ─────────▶│                         │                         │
  Guidewire         │                         │                         │
  [GW Response]     │                         │                         │
▭ EE-003 ◀─────────└─────────────────────────┴─────────────────────────┘
```

---

## 13. Appendices

### Appendix A: DFD Leveling Rules

- **Level 0 (Context):** Single process representing the entire system — `nexgen-creditrisk-gateway`
- **Level 1:** Ten functional processes (P1–P10) covering all four Camel routes
- **Level 2:** Sub-process decompositions for Request Validation (P1), Scoring (P2+P6), Bureau Integration (P3+P4+P5)
- Each level has no more than 7±2 processes per the Yourdon/DeMarco DFD leveling rule (Level 2 diagrams stay within this bound)

### Appendix B: Glossary

| **Term**                    | **Definition**                                                                                                 |
|-----------------------------|----------------------------------------------------------------------------------------------------------------|
| Camel Exchange              | The message container used by Apache Camel to carry request/response data through a route                      |
| CXF                         | Apache CXF — the framework used for SOAP and REST endpoint implementation                                      |
| DTI                         | Debt-to-Income Ratio — monthly debt obligations divided by monthly income                                      |
| Wire Tap                    | Apache Camel EIP pattern that asynchronously sends a copy of the exchange to a secondary endpoint             |
| WSS4J                       | Apache WSS4J — WS-Security implementation used for UsernameToken authentication on SOAP endpoints             |
| BureauInquiryRequest        | JAXB-generated request class for the external credit bureau SOAP service                                       |
| BureauInquiryResponse       | JAXB-generated response class for the external credit bureau SOAP service                                      |
| CreditRiskReqType           | Internal request model for credit risk assessment; populated from REST query params or SOAP body               |
| CreditRiskResType           | Internal response model containing the full risk assessment result                                             |
| CreditScoreDetail           | Intermediate model containing mapped bureau data and derived utilization rate                                   |
| ScoringStrategy             | Interface defining the Strategy Pattern for credit scoring; three implementations (Standard, Conservative, Aggressive) |
| ValidationException         | Custom exception thrown by `CreditRiskRequestValidator` when any of CR-001→CR-007 rules fail                  |
| BUREAU_ERROR                | Exchange flag set by `BureauResponseMapper` when bureau returns null or an error response                      |

### Appendix C: Data Sensitivity Classification

| **Classification** | **Definition**                                                               | **Handling Requirement**                                   |
|--------------------|------------------------------------------------------------------------------|------------------------------------------------------------|
| 🔒 **PII**          | Personally Identifiable Information: firstName, lastName, DOB, SIN, postalCode | Must be encrypted at rest and in transit; access logged  |
| 🔒 **Sensitive**    | Sensitive business data: credit scores, risk categories, recommendations, affordability ratings | Restricted access; audit trail required         |
| **Non-Sensitive**   | Operational/technical data: transaction IDs, timestamps, status codes, error codes | Standard handling                                   |

---

> **Document Control:**
> | Version | Date | Author | Changes |
> |---------|------|--------|---------|
> | 0.1 | 31-Mar-2026 | Copilot RE Agent | Initial draft — Context (L0), Level 1, Level 2 (×3), Data Flow Catalog, Data Store Catalog, Transformation Rules, Dead Flow Paths, Transaction Logging Detail, Target State, Annotations |
