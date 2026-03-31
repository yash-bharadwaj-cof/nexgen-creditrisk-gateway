# Sequence Diagrams

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

This document contains sequence diagrams capturing the runtime interaction patterns between components, services, and actors in the nexgen-creditrisk-gateway application. Each diagram represents a specific Camel route or cross-cutting workflow identified during reverse engineering of the JBoss Fuse 6.3 / Apache Camel 2.17 codebase.

### Diagram Notation

| **Symbol**   | **Meaning**                                           |
|--------------|-------------------------------------------------------|
| `──▶`        | Synchronous request                                   |
| `──▷`        | Asynchronous request                                  |
| `◀──`        | Synchronous response                                  |
| `◁──`        | Asynchronous response / callback                      |
| `──X`        | Message lost / failure                                |
| `[alt]`      | Alternative (conditional) flow                        |
| `[loop]`     | Repeated execution                                    |
| `[opt]`      | Optional execution                                    |

### Annotation Notation

| **Annotation**             | **Meaning**                                                       |
|----------------------------|-------------------------------------------------------------------|
| `⚠️ GAP`                   | Missing validation or functionality at this step                  |
| `⚪ HARDCODED`             | Step returns a hardcoded/static value instead of dynamic logic    |
| `⚫ DEAD CODE`             | Flow path exists in code but is never executed                    |
| `🔴 CRITICAL`              | Critical finding that must be addressed before migration          |
| `📝 NOTE`                  | General observation or clarification                              |

---

## 2. Sequence Diagram Index

| **Diagram ID** | **Title**                                        | **Use Case / Workflow**                | **Actors/Components**                                                                 | **Priority** |
|-----------------|--------------------------------------------------|----------------------------------------|---------------------------------------------------------------------------------------|-------------|
| SD-001          | REST Credit Risk Assessment                      | REST API request → full scoring flow   | REST Client, CXF JAX-RS, Validator, ScoringStrategyProcessor, BureauRequestBuilder, Bureau SOAP, BureauResponseMapper, RiskScoreCalculator, RestResponseBuilder, TransactionLogger | High |
| SD-002          | SOAP Credit Risk Assessment                      | SOAP request → full scoring flow       | SOAP Client, CXF JAX-WS, WSS4J, Validator, ScoringStrategyProcessor, BureauRequestBuilder, Bureau SOAP, BureauResponseMapper, RiskScoreCalculator, TransactionLogger | High |
| SD-003          | Gateway Credit Risk Assessment                   | Guidewire gateway → full scoring flow  | Gateway Client, CXF JAX-WS, GatewayRequestPreProcessor, Validator, ScoringStrategyProcessor, BureauRequestBuilder, Bureau SOAP, BureauResponseMapper, RiskScoreCalculator, TransactionLogger | High |
| SD-004          | Transaction Logging (Wire Tap)                   | Async logging to MongoDB               | Camel Wire Tap, TransactionLogger, MongoDB                                           | Medium |
| SD-005          | Error Handling Flow                              | Validation / system error handling     | Any Client, ErrorProcessor, TransactionLogger                                        | High |
| SD-006          | Scoring Strategy Resolution                      | Strategy Pattern selection             | ScoringStrategyProcessor, Standard/Conservative/Aggressive Strategy                  | Medium |

---

## 3. Sequence Diagrams

### 3.1 SD-001: REST Credit Risk Assessment

**Use Case:** Assess credit risk via RESTful HTTP GET endpoint
**Description:** External client sends a GET request with query parameters to `/service/rest/creditrisk/assess`. The request flows through validation, strategy selection, bureau inquiry, response mapping, risk calculation, and response building. A wire tap asynchronously logs the transaction to MongoDB.
**Trigger:** HTTP GET request to `/service/rest/creditrisk/assess`
**Pre-conditions:** REST endpoint is deployed on JBoss Fuse / Karaf; bureau SOAP endpoint is reachable
**Post-conditions:** Client receives XML response with risk assessment; transaction logged to MongoDB

#### Participants

| **Participant**            | **Type**    | **Component ID** | **Description**                                    |
|----------------------------|-------------|-------------------|----------------------------------------------------|
| REST Client                | Actor       | —                 | External system or user initiating the request     |
| CXF JAX-RS Endpoint        | Service     | COMP-001          | `creditRiskRestEndpoint` — REST front door         |
| CreditRiskRequestValidator | Processor   | COMP-005          | Validates all required fields and formats          |
| ScoringStrategyProcessor   | Processor   | COMP-006          | Selects scoring strategy based on product type     |
| BureauRequestBuilder       | Processor   | COMP-007          | Transforms request into BureauInquiryRequest       |
| Bureau SOAP Endpoint       | External    | EXT-001           | External credit bureau SOAP service                |
| BureauResponseMapper       | Processor   | COMP-008          | Maps bureau response to CreditScoreDetail          |
| RiskScoreCalculator        | Processor   | COMP-009          | Calculates risk score, category, recommendation    |
| RestResponseBuilder        | Processor   | COMP-010          | Sets HTTP headers and response body                |
| TransactionLogger          | Bean        | COMP-011          | Logs transaction to MongoDB (async via wire tap)   |
| MongoDB                    | Data Store  | DS-001            | `nexgen_creditrisk.transactions` collection        |

#### Sequence Diagram (ASCII)

```
┌───────────┐  ┌─────────┐  ┌───────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌─────────┐
│REST Client│  │CXF REST │  │ Validator │  │ Strategy │  │ Bureau   │  │ Bureau   │  │ Bureau   │  │ Risk     │  │ Rest Resp│  │ Txn      │  │MongoDB  │
│           │  │Endpoint │  │           │  │Processor │  │ReqBuilder│  │SOAP Ext  │  │RespMapper│  │Calculator│  │ Builder  │  │ Logger   │  │         │
└─────┬─────┘  └────┬────┘  └─────┬─────┘  └────┬─────┘  └────┬─────┘  └────┬─────┘  └────┬─────┘  └────┬─────┘  └────┬─────┘  └────┬─────┘  └────┬────┘
      │              │             │              │             │             │             │             │             │             │             │
      │ GET /assess  │             │              │             │             │             │             │             │             │             │
      │  ?params...  │             │              │             │             │             │             │             │             │             │
      │─────────────▶│             │              │             │             │             │             │             │             │             │
      │              │             │              │             │             │             │             │             │             │             │
      │              │ process()   │              │             │             │             │             │             │             │             │
      │              │────────────▶│              │             │             │             │             │             │             │             │
      │              │             │              │             │             │             │             │             │             │             │
      │              │             │ validate     │             │             │             │             │             │             │             │
      │              │             │ fields+fmt   │             │             │             │             │             │             │             │
      │              │             │ set props    │             │             │             │             │             │             │             │
      │              │             │──┐           │             │             │             │             │             │             │             │
      │              │             │  │ CR-001..7  │             │             │             │             │             │             │             │
      │              │             │◀─┘           │             │             │             │             │             │             │             │
      │              │             │              │             │             │             │             │             │             │             │
      │              │  VALIDATED  │              │             │             │             │             │             │             │             │
      │              │◀────────────│              │             │             │             │             │             │             │             │
      │              │             │              │             │             │             │             │             │             │             │
      │              │ process()   │              │             │             │             │             │             │             │             │
      │              │─────────────────────────▶│             │             │             │             │             │             │             │
      │              │             │              │             │             │             │             │             │             │             │
      │              │             │   resolve    │             │             │             │             │             │             │             │
      │              │             │   Strategy   │             │             │             │             │             │             │             │
      │              │             │              │──┐          │             │             │             │             │             │             │
      │              │             │              │  │ product  │             │             │             │             │             │             │
      │              │             │              │◀─┘ type     │             │             │             │             │             │             │
      │              │             │              │             │             │             │             │             │             │             │
      │              │ SCORING_STRATEGY set       │             │             │             │             │             │             │             │
      │              │◀───────────────────────────│             │             │             │             │             │             │             │
      │              │             │              │             │             │             │             │             │             │             │
      │              │ process()   │              │             │             │             │             │             │             │             │
      │              │────────────────────────────────────────▶│             │             │             │             │             │             │
      │              │             │              │             │             │             │             │             │             │             │
      │              │             │              │  build req  │             │             │             │             │             │             │
      │              │             │              │             │──┐          │             │             │             │             │             │
      │              │             │              │             │  │ map flds │             │             │             │             │             │
      │              │             │              │             │◀─┘          │             │             │             │             │             │
      │              │             │              │             │             │             │             │             │             │             │
      │              │ BureauInquiryRequest       │             │             │             │             │             │             │             │
      │              │◀────────────────────────────────────────│             │             │             │             │             │             │
      │              │             │              │             │             │             │             │             │             │             │
      │              │ SOAP inquire()             │             │             │             │             │             │             │             │
      │              │───────────────────────────────────────────────────────▶│             │             │             │             │             │
      │              │             │              │             │             │             │             │             │             │             │
      │              │             │              │             │  BureauInquiryResponse    │             │             │             │             │
      │              │◀──────────────────────────────────────────────────────│             │             │             │             │             │
      │              │             │              │             │             │             │             │             │             │             │
      │              │ process()   │              │             │             │             │             │             │             │             │
      │              │──────────────────────────────────────────────────────────────────▶│             │             │             │             │
      │              │             │              │             │             │             │             │             │             │             │
      │              │             │              │             │             │   map resp  │             │             │             │             │
      │              │             │              │             │             │             │──┐          │             │             │             │
      │              │             │              │             │             │             │  │score rng │             │             │             │
      │              │             │              │             │             │             │◀─┘util calc │             │             │             │
      │              │             │              │             │             │             │             │             │             │             │
      │              │ CREDIT_SCORE_DETAIL set    │             │             │             │             │             │             │             │
      │              │◀─────────────────────────────────────────────────────────────────│             │             │             │             │
      │              │             │              │             │             │             │             │             │             │             │
      │              │ process()   │              │             │             │             │             │             │             │             │
      │              │─────────────────────────────────────────────────────────────────────────────▶│             │             │             │
      │              │             │              │             │             │             │             │             │             │             │
      │              │             │              │             │             │             │  calc score │             │             │             │
      │              │             │              │             │             │             │  income,DTI │             │             │             │
      │              │             │              │             │             │             │  risk factors             │             │             │
      │              │             │              │             │             │             │             │──┐          │             │             │
      │              │             │              │             │             │             │             │  │strategy  │             │             │
      │              │             │              │             │             │             │             │◀─┘calls     │             │             │
      │              │             │              │             │             │             │             │             │             │             │
      │              │ CreditRiskResType          │             │             │             │             │             │             │             │
      │              │◀────────────────────────────────────────────────────────────────────────────│             │             │             │
      │              │             │              │             │             │             │             │             │             │             │
      │              │ process()   │              │             │             │             │             │             │             │             │
      │              │────────────────────────────────────────────────────────────────────────────────────────▶│             │             │
      │              │             │              │             │             │             │             │             │             │             │
      │              │  HTTP 200   │              │             │             │             │             │  set headers│             │             │
      │              │  XML body   │              │             │             │             │             │  app/xml    │             │             │
      │              │◀────────────────────────────────────────────────────────────────────────────────────────│             │             │
      │              │             │              │             │             │             │             │             │             │             │
      │              │ [Wire Tap — async]         │             │             │             │             │             │             │             │
      │              │─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─▷│             │
      │              │             │              │             │             │             │             │             │  logTxn()   │             │
      │              │             │              │             │             │             │             │             │────────────▶│             │
      │              │             │              │             │             │             │             │             │             │  insert()   │
      │              │             │              │             │             │             │             │             │             │────────────▶│
      │              │             │              │             │             │             │             │             │             │     ack     │
      │              │             │              │             │             │             │             │             │             │◀────────────│
      │              │             │              │             │             │             │             │             │             │             │
      │ HTTP 200     │             │              │             │             │             │             │             │             │             │
      │ XML Response │             │              │             │             │             │             │             │             │             │
      │◀─────────────│             │              │             │             │             │             │             │             │             │
      │              │             │              │             │             │             │             │             │             │             │
┌─────┴─────┐  ┌────┴────┐  ┌─────┴─────┐  ┌────┴─────┐  ┌────┴─────┐  ┌────┴─────┐  ┌────┴─────┐  ┌────┴─────┐  ┌────┴─────┐  ┌────┴─────┐  ┌────┴────┐
│REST Client│  │CXF REST │  │ Validator │  │ Strategy │  │ Bureau   │  │ Bureau   │  │ Bureau   │  │ Risk     │  │ Rest Resp│  │ Txn      │  │MongoDB  │
│           │  │Endpoint │  │           │  │Processor │  │ReqBuilder│  │SOAP Ext  │  │RespMapper│  │Calculator│  │ Builder  │  │ Logger   │  │         │
└───────────┘  └─────────┘  └───────────┘  └──────────┘  └──────────┘  └──────────┘  └──────────┘  └──────────┘  └──────────┘  └──────────┘  └─────────┘
```

#### Step-by-Step Description

| **Step** | **From**                 | **To**                   | **Message/Action**                                     | **Type**   | **Annotations**  | **Notes**                                                     |
|----------|--------------------------|--------------------------|--------------------------------------------------------|------------|------------------|---------------------------------------------------------------|
| 1        | REST Client              | CXF JAX-RS Endpoint      | GET `/assess?applicantId=...&firstName=...`            | Sync       | None             | 11 query parameters bound to CreditRiskReqType                |
| 2        | CXF JAX-RS Endpoint      | CreditRiskRequestValidator| `process(Exchange)`                                   | Sync       | ⚠️ GAP           | No DOB age check, no SIN checksum, no postal-province cross-validation |
| 3        | CreditRiskRequestValidator| (self)                   | Validate 7 fields, set logging properties              | Internal   | None             | Regex: DOB, SIN, postal code; enum check: province            |
| 4        | CXF JAX-RS Endpoint      | ScoringStrategyProcessor  | `process(Exchange)` — resolve strategy                | Sync       | None             | Based on productType → Conservative/Aggressive/Standard       |
| 5        | ScoringStrategyProcessor  | (self)                   | `resolveStrategy(request)` — select implementation    | Internal   | None             | Falls back to config default if product type unknown          |
| 6        | CXF JAX-RS Endpoint      | BureauRequestBuilder      | `process(Exchange)` — build SOAP request              | Sync       | None             | Maps applicant fields → BureauInquiryRequest                  |
| 7        | BureauRequestBuilder      | (self)                   | Build BureauSubject, BureauSubscriber, set headers    | Internal   | None             | requestId = prefix + UUID; timestamp formatted                |
| 8        | CXF JAX-RS Endpoint      | Bureau SOAP Endpoint      | SOAP `inquire()` — send BureauInquiryRequest          | Sync       | ⚠️ GAP           | No circuit breaker, no retry, single endpoint, no fallback    |
| 9        | Bureau SOAP Endpoint      | CXF JAX-RS Endpoint      | BureauInquiryResponse (score, limits, delinquencies)  | Response   | None             | 30s connection + 30s receive timeout                          |
| 10       | CXF JAX-RS Endpoint      | BureauResponseMapper      | `process(Exchange)` — map bureau response             | Sync       | None             | Null response → CR-301; error code → CR-302                   |
| 11       | BureauResponseMapper      | (self)                   | Map score range, calculate utilization rate            | Internal   | None             | 5 ranges: EXCEPTIONAL/VERY_GOOD/GOOD/FAIR/POOR               |
| 12       | CXF JAX-RS Endpoint      | RiskScoreCalculator       | `process(Exchange)` — calculate full assessment       | Sync       | ⚪ HARDCODED     | monthlyDebt = balance × 0.03; requestMonthly = amount × 0.006 |
| 13       | RiskScoreCalculator       | (self)                   | Build income, employment, debt, risk factors          | Internal   | ⚪ HARDCODED     | verificationStatus always "SELF_REPORTED"; no actual verification |
| 14       | RiskScoreCalculator       | ScoringStrategy           | `categorizeRisk()`, `calculateOverallScore()`, etc.   | Sync       | None             | Delegates to selected strategy implementation                 |
| 15       | CXF JAX-RS Endpoint      | RestResponseBuilder       | `process(Exchange)` — set HTTP headers + body         | Sync       | None             | Content-Type: application/xml, HTTP 200                       |
| 16       | CXF JAX-RS Endpoint      | TransactionLogger         | Wire Tap `direct:logTransaction` (async)              | Async      | ⚠️ GAP           | Silent failure if MongoDB down; PII logged unencrypted        |
| 17       | TransactionLogger         | MongoDB                  | `insert(document)` to `transactions` collection       | Sync       | 🔴 CRITICAL      | PII (applicantId, province) stored in plain text              |
| 18       | CXF JAX-RS Endpoint      | REST Client              | HTTP 200 + CreditRiskResType XML                      | Response   | None             | Full assessment response returned                             |

#### Error / Alternative Flows

| **Condition**                    | **Alternative Flow**                                                                 |
|----------------------------------|--------------------------------------------------------------------------------------|
| Validation failure (CR-001..007) | ErrorProcessor → HTTP 400 + error XML with specific error code and message           |
| Bureau response null             | BureauResponseMapper sets BUREAU_ERROR=true, CR-301 → RiskScoreCalculator returns INDETERMINATE with W215E |
| Bureau returns errorCode         | BureauResponseMapper sets BUREAU_ERROR=true, CR-302 → RiskScoreCalculator returns INDETERMINATE with W215E |
| Bureau SOAP timeout (30s)        | doCatch(Exception) → ErrorProcessor → HTTP 500 + CR-500                              |
| Unexpected exception             | doCatch(Exception) → ErrorProcessor → HTTP 500 + CR-500                              |
| MongoDB logging failure          | TransactionLogger catches exception, logs error, continues silently                  |

#### Annotations Summary

| **Step** | **Annotation Type** | **Description**                                                       | **Linked Gap/Finding** |
|----------|---------------------|-----------------------------------------------------------------------|------------------------|
| 2        | ⚠️ GAP              | No DOB age check (min 18); no SIN checksum; no postal-province cross-validation | GAP-009, GAP-010, GAP-011, GAP-012 |
| 8        | ⚠️ GAP              | No circuit breaker, retry, or fallback for bureau SOAP call           | GAP-023                |
| 12       | ⚪ HARDCODED         | Monthly debt = totalBalance × 0.03 (hardcoded 3%)                    | GAP-003                |
| 13       | ⚪ HARDCODED         | Income verification always "SELF_REPORTED"; no actual verification    | GAP-002                |
| 16       | ⚠️ GAP              | Silent failure on MongoDB error; no dead letter queue                 | GAP-022                |
| 17       | 🔴 CRITICAL          | PII stored unencrypted in MongoDB (applicantId, province)             | GAP-017                |

---

### 3.2 SD-002: SOAP Credit Risk Assessment

**Use Case:** Assess credit risk via SOAP endpoint with WS-Security
**Description:** External system sends a SOAP request to `/service/soap/creditrisk` with WSS4J UsernameToken authentication. After security validation, the processing chain is identical to the REST route.
**Trigger:** SOAP request to `cxf:bean:creditRiskSoapEndpoint`
**Pre-conditions:** Client has valid WS-Security UsernameToken credentials; schema validation passes
**Post-conditions:** Client receives SOAP response with risk assessment; transaction logged

#### Participants

| **Participant**            | **Type**    | **Component ID** | **Description**                                 |
|----------------------------|-------------|-------------------|-------------------------------------------------|
| SOAP Client                | Actor       | —                 | External system with WS-Security credentials    |
| CXF JAX-WS Endpoint        | Service     | COMP-002          | `creditRiskSoapEndpoint` with WSS4J interceptor |
| WSS4J Interceptor           | Security    | COMP-012          | Validates UsernameToken (PasswordText)          |
| CreditRiskRequestValidator | Processor   | COMP-005          | Same as SD-001                                  |
| ScoringStrategyProcessor   | Processor   | COMP-006          | Same as SD-001                                  |
| BureauRequestBuilder       | Processor   | COMP-007          | Same as SD-001                                  |
| Bureau SOAP Endpoint       | External    | EXT-001           | Same as SD-001                                  |
| BureauResponseMapper       | Processor   | COMP-008          | Same as SD-001                                  |
| RiskScoreCalculator        | Processor   | COMP-009          | Same as SD-001                                  |
| TransactionLogger          | Bean        | COMP-011          | Same as SD-001                                  |
| MongoDB                    | Data Store  | DS-001            | Same as SD-001                                  |

#### Sequence Diagram (ASCII)

```
┌───────────┐  ┌─────────┐  ┌──────────┐  ┌───────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌─────────┐
│SOAP Client│  │CXF SOAP │  │  WSS4J   │  │ Validator │  │ Strategy │  │ Bureau   │  │ Bureau   │  │ Bureau   │  │ Risk     │  │ Txn      │  │MongoDB  │
│           │  │Endpoint │  │Interceptor│  │           │  │Processor │  │ReqBuilder│  │SOAP Ext  │  │RespMapper│  │Calculator│  │ Logger   │  │         │
└─────┬─────┘  └────┬────┘  └────┬─────┘  └─────┬─────┘  └────┬─────┘  └────┬─────┘  └────┬─────┘  └────┬─────┘  └────┬─────┘  └────┬─────┘  └────┬────┘
      │              │            │              │              │             │             │             │             │             │             │
      │ SOAP Request │            │              │              │             │             │             │             │             │             │
      │ +WS-Security │            │              │              │             │             │             │             │             │             │
      │─────────────▶│            │              │              │             │             │             │             │             │             │
      │              │            │              │              │             │             │             │             │             │             │
      │              │ validate   │              │              │             │             │             │             │             │             │
      │              │ UsernameToken             │              │             │             │             │             │             │             │
      │              │───────────▶│              │              │             │             │             │             │             │             │
      │              │            │              │              │             │             │             │             │             │             │
      │              │  token OK  │              │              │             │             │             │             │             │             │
      │              │◀───────────│              │              │             │             │             │             │             │             │
      │              │            │              │              │             │             │             │             │             │             │
      │              │ schema validation         │              │             │             │             │             │             │             │
      │              │──┐         │              │              │             │             │             │             │             │             │
      │              │  │ XSD     │              │              │             │             │             │             │             │             │
      │              │◀─┘         │              │              │             │             │             │             │             │             │
      │              │            │              │              │             │             │             │             │             │             │
      │              │ process()  │              │              │             │             │             │             │             │             │
      │              │───────────────────────▶│              │             │             │             │             │             │             │
      │              │            │              │              │             │             │             │             │             │             │
      │              │            │    [Same processing chain as SD-001, Steps 2-17]       │             │             │             │             │
      │              │            │              │              │             │             │             │             │             │             │
      │ SOAP Response│            │              │              │             │             │             │             │             │             │
      │ (native)     │            │              │              │             │             │             │             │             │             │
      │◀─────────────│            │              │              │             │             │             │             │             │             │
      │              │            │              │              │             │             │             │             │             │             │
┌─────┴─────┐  ┌────┴────┐  ┌────┴─────┐  ┌─────┴─────┐  ┌────┴─────┐  ┌────┴─────┐  ┌────┴─────┐  ┌────┴─────┐  ┌────┴─────┐  ┌────┴─────┐  ┌────┴────┐
│SOAP Client│  │CXF SOAP │  │  WSS4J   │  │ Validator │  │ Strategy │  │ Bureau   │  │ Bureau   │  │ Bureau   │  │ Risk     │  │ Txn      │  │MongoDB  │
│           │  │Endpoint │  │Interceptor│  │           │  │Processor │  │ReqBuilder│  │SOAP Ext  │  │RespMapper│  │Calculator│  │ Logger   │  │         │
└───────────┘  └─────────┘  └──────────┘  └───────────┘  └──────────┘  └──────────┘  └──────────┘  └──────────┘  └──────────┘  └──────────┘  └─────────┘
```

#### Step-by-Step Description

| **Step** | **From**            | **To**               | **Message/Action**                                           | **Type**   | **Annotations** | **Notes**                                              |
|----------|---------------------|----------------------|--------------------------------------------------------------|------------|-----------------|--------------------------------------------------------|
| 1        | SOAP Client         | CXF JAX-WS Endpoint  | SOAP Envelope + WS-Security UsernameToken header            | Sync       | None            | CreditRiskReqType in SOAP body                         |
| 2        | CXF JAX-WS Endpoint | WSS4J Interceptor     | Validate UsernameToken (PasswordText)                       | Sync       | ⚠️ GAP          | Credentials in config file; no vault integration       |
| 3        | CXF JAX-WS Endpoint | (self)               | XML Schema validation                                        | Internal   | None            | Schema enforcement enabled on this endpoint            |
| 4-18     | —                   | —                     | Same processing chain as SD-001 Steps 2-17                  | —          | —               | Validator → Strategy → Bureau → Mapper → Calculator → Logger |
| 19       | CXF JAX-WS Endpoint | SOAP Client           | SOAP Response (native CXF; no RestResponseBuilder)          | Response   | None            | Response wrapped in SOAP envelope automatically        |

#### Error / Alternative Flows

| **Condition**                  | **Alternative Flow**                                              |
|--------------------------------|-------------------------------------------------------------------|
| Invalid WS-Security token      | SOAP fault returned by CXF before reaching Camel route           |
| Schema validation failure      | SOAP fault with schema violation details                         |
| All other errors               | Same as SD-001 error flows                                       |

#### Annotations Summary

| **Step** | **Annotation Type** | **Description**                                        | **Linked Gap/Finding** |
|----------|---------------------|--------------------------------------------------------|------------------------|
| 2        | ⚠️ GAP              | WS-Security credentials stored in plain text config    | GAP-020                |

---

### 3.3 SD-003: Gateway Credit Risk Assessment

**Use Case:** Assess credit risk via Guidewire-style gateway endpoint
**Description:** Gateway client sends a SOAP request to `/service/creditriskapi`. A pre-processor enriches the request with default headers if missing, then the standard processing chain executes.
**Trigger:** SOAP request to `cxf:bean:creditRiskGwEndpoint`
**Pre-conditions:** Gateway endpoint deployed; no WS-Security required on this endpoint
**Post-conditions:** Client receives SOAP response; transaction logged

#### Participants

| **Participant**              | **Type**    | **Component ID** | **Description**                                |
|------------------------------|-------------|-------------------|------------------------------------------------|
| Gateway Client               | Actor       | —                 | Guidewire or internal system                   |
| CXF Gateway Endpoint         | Service     | COMP-003          | `creditRiskGwEndpoint` — no WS-Security        |
| GatewayRequestPreProcessor   | Processor   | COMP-004          | Enriches missing requestHeader and channel     |
| (Remaining processors)       | —           | COMP-005..011     | Same as SD-001                                 |

#### Sequence Diagram (ASCII)

```
┌───────────┐  ┌─────────┐  ┌──────────────┐  ┌───────────┐  ┌──────────┐
│GW Client  │  │CXF GW   │  │GW PreProcess │  │ Validator │  │[SD-001   │
│           │  │Endpoint  │  │              │  │           │  │Steps 4+] │
└─────┬─────┘  └────┬────┘  └──────┬───────┘  └─────┬─────┘  └────┬─────┘
      │              │              │                 │             │
      │ SOAP Request │              │                 │             │
      │ (may lack    │              │                 │             │
      │  headers)    │              │                 │             │
      │─────────────▶│              │                 │             │
      │              │              │                 │             │
      │              │ process()    │                 │             │
      │              │─────────────▶│                 │             │
      │              │              │                 │             │
      │              │              │ [opt] header null             │
      │              │              │──┐              │             │
      │              │              │  │ create hdr   │             │
      │              │              │  │ src=GATEWAY   │             │
      │              │              │  │ txnId=UUID    │             │
      │              │              │◀─┘              │             │
      │              │              │                 │             │
      │              │              │ [opt] channel null            │
      │              │              │──┐              │             │
      │              │              │  │ set API      │             │
      │              │              │◀─┘              │             │
      │              │              │                 │             │
      │              │ enriched req │                 │             │
      │              │◀─────────────│                 │             │
      │              │              │                 │             │
      │              │ process()    │                 │             │
      │              │──────────────────────────────▶│             │
      │              │              │                 │             │
      │              │              │   [Same as SD-001 Steps 2-17]│
      │              │              │                 │             │
      │ SOAP Response│              │                 │             │
      │◀─────────────│              │                 │             │
      │              │              │                 │             │
┌─────┴─────┐  ┌────┴────┐  ┌──────┴───────┐  ┌─────┴─────┐  ┌────┴─────┐
│GW Client  │  │CXF GW   │  │GW PreProcess │  │ Validator │  │[SD-001   │
│           │  │Endpoint  │  │              │  │           │  │Steps 4+] │
└───────────┘  └─────────┘  └──────────────┘  └───────────┘  └──────────┘
```

#### Step-by-Step Description

| **Step** | **From**               | **To**                      | **Message/Action**                                | **Type**   | **Annotations** | **Notes**                                      |
|----------|------------------------|-----------------------------|---------------------------------------------------|------------|-----------------|------------------------------------------------|
| 1        | Gateway Client         | CXF Gateway Endpoint        | SOAP request (may lack requestHeader/channel)     | Sync       | None            | No WS-Security on this endpoint                |
| 2        | CXF Gateway Endpoint   | GatewayRequestPreProcessor  | `process(Exchange)` — enrich missing fields       | Sync       | None            | Only acts if header or channel is null          |
| 3        | GatewayRequestPreProcessor | (self)                  | Create RequestHeader: src=GATEWAY, txnId=UUID     | Internal   | None            | Timestamp format: `yyyy-MM-dd'T'HH:mm:ss`      |
| 4        | GatewayRequestPreProcessor | (self)                  | Set requestChannel=API if null                    | Internal   | None            | Default channel for gateway requests           |
| 5-19     | —                      | —                            | Same as SD-001 Steps 2-17                        | —          | —               | Full processing chain                          |
| 20       | CXF Gateway Endpoint   | Gateway Client              | SOAP Response (native CXF)                        | Response   | None            | No RestResponseBuilder needed                  |

#### Error / Alternative Flows

| **Condition**          | **Alternative Flow**                                    |
|------------------------|----------------------------------------------------------|
| Header already present | Pre-processor skips enrichment; proceeds normally        |
| All other errors       | Same as SD-001 error flows                               |

---

### 3.4 SD-004: Transaction Logging (Wire Tap)

**Use Case:** Asynchronous transaction audit logging
**Description:** After each assessment (success or error), a Camel wire tap asynchronously sends exchange properties to the `TransactionLogger` bean, which inserts a BSON document into MongoDB.
**Trigger:** Wire tap from `direct:logTransaction` (called from Routes 1-3)
**Pre-conditions:** MongoDB is running at configured host:port
**Post-conditions:** Transaction record inserted into `nexgen_creditrisk.transactions`

#### Participants

| **Participant**    | **Type**    | **Component ID** | **Description**                          |
|--------------------|-------------|-------------------|------------------------------------------|
| Camel Route        | Caller      | —                 | Any of the 3 assessment routes           |
| TransactionLogger  | Bean        | COMP-011          | Builds BSON document from exchange props |
| MongoDB            | Data Store  | DS-001            | `nexgen_creditrisk.transactions`         |

#### Sequence Diagram (ASCII)

```
┌────────────┐          ┌─────────────────┐          ┌─────────────┐
│Camel Route │          │TransactionLogger│          │   MongoDB   │
│(Wire Tap)  │          │                 │          │             │
└─────┬──────┘          └────────┬────────┘          └──────┬──────┘
      │                          │                          │
      │ direct:logTransaction    │                          │
      │ (async — wire tap)       │                          │
      │─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─▷│                          │
      │                          │                          │
      │                          │ build BSON Document      │
      │                          │──┐                       │
      │                          │  │ txnId, applicantId    │
      │                          │  │ province, product     │
      │                          │  │ channel, source       │
      │                          │  │ timestamp, status     │
      │                          │  │ risk/error fields     │
      │                          │◀─┘                       │
      │                          │                          │
      │                          │ insert(document)         │
      │                          │─────────────────────────▶│
      │                          │                          │
      │                          │       acknowledgment     │
      │                          │◀─────────────────────────│
      │                          │                          │
      │   ┌──────────────────────┴───────────────────────┐  │
      │   │ [alt] MongoDB Unavailable                    │  │
      │   │ catch(Exception) → LOG.error(...)            │  │
      │   │ ⚠️ Silent failure — no alert, no DLQ         │  │
      │   └──────────────────────┬───────────────────────┘  │
      │                          │                          │
┌─────┴──────┐          ┌────────┴────────┐          ┌──────┴──────┐
│Camel Route │          │TransactionLogger│          │   MongoDB   │
│(Wire Tap)  │          │                 │          │             │
└────────────┘          └─────────────────┘          └─────────────┘
```

#### Step-by-Step Description

| **Step** | **From**          | **To**               | **Message/Action**                              | **Type** | **Annotations** | **Notes**                                  |
|----------|-------------------|----------------------|-------------------------------------------------|----------|-----------------|---------------------------------------------|
| 1        | Camel Route       | TransactionLogger    | `logTransaction(Exchange)` via wire tap         | Async    | None            | Non-blocking — route continues immediately  |
| 2        | TransactionLogger | (self)               | Build BSON from exchange properties             | Internal | 🔴 CRITICAL     | PII fields stored unencrypted               |
| 3        | TransactionLogger | MongoDB              | `collection.insertOne(document)`                | Sync     | None            | DB: nexgen_creditrisk, Collection: transactions |
| 4        | MongoDB           | TransactionLogger    | Insert acknowledgment                           | Response | None            |                                             |
| 5 (alt)  | TransactionLogger | (self)               | catch Exception → log error → continue          | Internal | ⚠️ GAP          | Silent failure; no DLQ or alert             |

#### Annotations Summary

| **Step** | **Annotation Type** | **Description**                                    | **Linked Gap/Finding** |
|----------|---------------------|----------------------------------------------------|------------------------|
| 2        | 🔴 CRITICAL          | PII (applicantId, province) stored unencrypted     | GAP-017                |
| 5        | ⚠️ GAP              | Silent failure on MongoDB error; no dead letter queue | GAP-022             |

---

### 3.5 SD-005: Error Handling Flow

**Use Case:** Centralized error handling for validation and system errors
**Description:** When any processor throws an exception, the route's `doCatch(Exception)` block delegates to `ErrorProcessor`, which builds an error response with appropriate HTTP status and error codes.
**Trigger:** Exception thrown during any processing step
**Pre-conditions:** An exception occurred in the Camel route
**Post-conditions:** Error response returned to client with appropriate status code

#### Participants

| **Participant**   | **Type**    | **Component ID** | **Description**                          |
|-------------------|-------------|-------------------|------------------------------------------|
| Failed Processor  | Processor   | —                 | Any processor that threw an exception    |
| ErrorProcessor    | Processor   | COMP-013          | Maps exception to error response         |
| Client            | Actor       | —                 | REST or SOAP client                      |

#### Sequence Diagram (ASCII)

```
┌──────────────┐          ┌────────────────┐          ┌────────────────┐
│   Failed     │          │ Error          │          │    Client      │
│  Processor   │          │ Processor      │          │                │
└──────┬───────┘          └───────┬────────┘          └───────┬────────┘
       │                          │                           │
       │ throw Exception          │                           │
       │─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─▶│                           │
       │                          │                           │
       │   ┌──────────────────────┴────────────────────────┐  │
       │   │ [alt] ValidationException                     │  │
       │   │   statusCode = ex.getErrorCode() (CR-001..7)  │  │
       │   │   httpStatus = 400                            │  │
       │   │   statusMessage = ex.getMessage()             │  │
       │   └──────────────────────┬────────────────────────┘  │
       │                          │                           │
       │   ┌──────────────────────┴────────────────────────┐  │
       │   │ [else] Other Exception                        │  │
       │   │   statusCode = CR-500                         │  │
       │   │   httpStatus = 500                            │  │
       │   │   statusMessage = "Internal service error"    │  │
       │   └──────────────────────┬────────────────────────┘  │
       │                          │                           │
       │                          │ build CreditRiskResType   │
       │                          │ with error details        │
       │                          │──┐                        │
       │                          │  │ set responseHeader     │
       │                          │  │ set HTTP headers       │
       │                          │◀─┘                        │
       │                          │                           │
       │                          │ Error Response            │
       │                          │──────────────────────────▶│
       │                          │                           │
┌──────┴───────┐          ┌───────┴────────┐          ┌───────┴────────┐
│   Failed     │          │ Error          │          │    Client      │
│  Processor   │          │ Processor      │          │                │
└──────────────┘          └────────────────┘          └────────────────┘
```

#### Step-by-Step Description

| **Step** | **From**          | **To**           | **Message/Action**                                       | **Type**   | **Annotations** | **Notes**                                    |
|----------|-------------------|------------------|---------------------------------------------------------|------------|-----------------|----------------------------------------------|
| 1        | Failed Processor  | Route doCatch    | Exception thrown                                         | Exception  | None            | Camel doCatch(java.lang.Exception)           |
| 2        | Route doCatch     | ErrorProcessor   | `process(Exchange)` with caught exception               | Sync       | None            | Exception available via exchange property     |
| 3        | ErrorProcessor    | (self)           | Check exception type: ValidationException vs other      | Internal   | ⚠️ GAP          | Only 2 categories; no granularity for CR-500 |
| 4        | ErrorProcessor    | (self)           | Build error CreditRiskResType with timestamp            | Internal   | None            | Error response follows same XML schema        |
| 5        | ErrorProcessor    | Client           | HTTP 400 (validation) or 500 (other) + XML error body  | Response   | None            |                                               |

#### Annotations Summary

| **Step** | **Annotation Type** | **Description**                                              | **Linked Gap/Finding** |
|----------|---------------------|--------------------------------------------------------------|------------------------|
| 3        | ⚠️ GAP              | All non-validation errors mapped to generic CR-500           | GAP-024                |

---

### 3.6 SD-006: Scoring Strategy Resolution

**Use Case:** Strategy Pattern — select scoring implementation based on product type
**Description:** The `ScoringStrategyProcessor` examines the request's product type and optionally the default strategy config to instantiate the appropriate `ScoringStrategy` implementation.
**Trigger:** `process(Exchange)` call from Camel route
**Pre-conditions:** VALIDATED_REQUEST property set on exchange
**Post-conditions:** SCORING_STRATEGY property set on exchange

#### Sequence Diagram (ASCII)

```
┌───────────────┐  ┌──────────────────┐  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐
│ Camel Route   │  │ScoringStrategy   │  │Conservative │  │ Aggressive  │  │  Standard   │
│               │  │Processor         │  │Strategy     │  │Strategy     │  │Strategy     │
└───────┬───────┘  └────────┬─────────┘  └──────┬──────┘  └──────┬──────┘  └──────┬──────┘
        │                   │                    │               │               │
        │ process(exchange) │                    │               │               │
        │──────────────────▶│                    │               │               │
        │                   │                    │               │               │
        │                   │ get productType    │               │               │
        │                   │──┐                 │               │               │
        │                   │◀─┘                 │               │               │
        │                   │                    │               │               │
        │  ┌────────────────┴─────────────────────────────────────────────────┐ │
        │  │ [alt] MORTGAGE or AUTO_LOAN                                     │ │
        │  └────────────────┬─────────────────────────────────────────────────┘ │
        │                   │ new()              │               │               │
        │                   │───────────────────▶│               │               │
        │                   │  instance          │               │               │
        │                   │◀───────────────────│               │               │
        │                   │                    │               │               │
        │  ┌────────────────┴─────────────────────────────────────────────────┐ │
        │  │ [alt] CREDIT_CARD or LINE_OF_CREDIT                             │ │
        │  └────────────────┬─────────────────────────────────────────────────┘ │
        │                   │ new()              │               │               │
        │                   │──────────────────────────────────▶│               │
        │                   │  instance          │               │               │
        │                   │◀──────────────────────────────────│               │
        │                   │                    │               │               │
        │  ┌────────────────┴─────────────────────────────────────────────────┐ │
        │  │ [else] Unknown product OR default config                        │ │
        │  └────────────────┬─────────────────────────────────────────────────┘ │
        │                   │ new()              │               │               │
        │                   │─────────────────────────────────────────────────▶│
        │                   │  instance          │               │               │
        │                   │◀─────────────────────────────────────────────────│
        │                   │                    │               │               │
        │  SCORING_STRATEGY │                    │               │               │
        │  property set     │                    │               │               │
        │◀──────────────────│                    │               │               │
        │                   │                    │               │               │
┌───────┴───────┐  ┌────────┴─────────┐  ┌──────┴──────┐  ┌──────┴──────┐  ┌──────┴──────┐
│ Camel Route   │  │ScoringStrategy   │  │Conservative │  │ Aggressive  │  │  Standard   │
│               │  │Processor         │  │Strategy     │  │Strategy     │  │Strategy     │
└───────────────┘  └──────────────────┘  └─────────────┘  └─────────────┘  └─────────────┘
```

#### Step-by-Step Description

| **Step** | **From**           | **To**                      | **Message/Action**                               | **Type**   | **Annotations** | **Notes**                                       |
|----------|--------------------|-----------------------------|--------------------------------------------------|------------|-----------------|--------------------------------------------------|
| 1        | Camel Route        | ScoringStrategyProcessor     | `process(Exchange)`                             | Sync       | None            | Reads VALIDATED_REQUEST property                 |
| 2        | ScoringStrategyProcessor | (self)                | Extract productType from request                 | Internal   | None            | Case-insensitive comparison                      |
| 3a       | ScoringStrategyProcessor | ConservativeScoringStrategy | `new()` if MORTGAGE or AUTO_LOAN            | Sync       | None            | Stricter thresholds for secured products         |
| 3b       | ScoringStrategyProcessor | AggressiveScoringStrategy   | `new()` if CREDIT_CARD or LINE_OF_CREDIT    | Sync       | None            | Relaxed thresholds for unsecured products        |
| 3c       | ScoringStrategyProcessor | StandardScoringStrategy     | `new()` if unknown or default config         | Sync       | None            | Fallback reads `SCORING_DEFAULT_STRATEGY` config |
| 4        | ScoringStrategyProcessor | Camel Route                 | Sets SCORING_STRATEGY exchange property         | Response   | None            | Strategy used by RiskScoreCalculator downstream  |

---

## 4. Cross-Cutting Sequence Patterns

### 4.1 Authentication Flow Pattern (SOAP Only)

```
┌────────┐          ┌──────────────────┐          ┌─────────────────┐
│ Client │          │ CXF SOAP Endpoint│          │ WSS4J Interceptor│
└───┬────┘          └────────┬─────────┘          └────────┬────────┘
    │                        │                             │
    │ SOAP Request           │                             │
    │ + <wsse:Security>      │                             │
    │   <UsernameToken>      │                             │
    │─────────────────────▶│                             │
    │                        │                             │
    │                        │ validate(UsernameToken)      │
    │                        │────────────────────────────▶│
    │                        │                             │
    │                        │                             │
    │    ┌───────────────────┴──────────────────────────┐  │
    │    │ [alt] Valid credentials                      │  │
    │    └───────────────────┬──────────────────────────┘  │
    │                        │                             │
    │                        │       OK — proceed          │
    │                        │◀────────────────────────────│
    │                        │                             │
    │    ┌───────────────────┴──────────────────────────┐  │
    │    │ [alt] Invalid credentials                    │  │
    │    └───────────────────┬──────────────────────────┘  │
    │                        │                             │
    │                        │ SOAP Fault (unauthorized)   │
    │                        │◀────────────────────────────│
    │                        │                             │
    │  SOAP Fault / proceed  │                             │
    │◀───────────────────────│                             │
    │                        │                             │
┌───┴────┐          ┌────────┴─────────┐          ┌────────┴────────┐
│ Client │          │ CXF SOAP Endpoint│          │ WSS4J Interceptor│
└────────┘          └──────────────────┘          └─────────────────┘
```

### 4.2 Error Handling Pattern

```
┌────────┐          ┌─────────┐          ┌───────────────────┐          ┌────────┐
│ Caller │          │ Processor│          │  ErrorProcessor   │          │ Logger │
└───┬────┘          └────┬────┘          └─────────┬─────────┘          └───┬────┘
    │                    │                         │                        │
    │ process()          │                         │                        │
    │───────────────────▶│                         │                        │
    │                    │                         │                        │
    │                    │ [processing fails]       │                        │
    │                    │─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─▶│                        │
    │                    │                         │                        │
    │                    │ throw Exception          │                        │
    │                    │────────────────────────▶│                        │
    │                    │                         │                        │
    │                    │                         │ LOG.error(...)         │
    │                    │                         │───────────────────────▶│
    │                    │                         │                        │
    │      Error Response (400 or 500)             │                        │
    │◀─────────────────────────────────────────────│                        │
    │                    │                         │                        │
┌───┴────┐          ┌────┴────┐          ┌─────────┴─────────┐          ┌───┴────┐
│ Caller │          │ Processor│          │  ErrorProcessor   │          │ Logger │
└────────┘          └─────────┘          └───────────────────┘          └────────┘
```

### 4.3 Transaction Logging Pattern

```
┌─────────┐          ┌────────────────────┐          ┌────────────────┐
│ Service │          │ TransactionLogger  │          │    MongoDB     │
└────┬────┘          └─────────┬──────────┘          └───────┬────────┘
     │                         │                             │
     │ wire tap (async)        │                             │
     │─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─▷│                             │
     │                         │                             │
     │                         │ build BSON Document          │
     │                         │──┐                          │
     │                         │  │ txnId, applicantId       │
     │                         │  │ status, risk/error       │
     │                         │◀─┘                          │
     │                         │                             │
     │    ┌────────────────────┴──────────────────────────┐  │
     │    │ [alt] MongoDB Available                       │  │
     │    └────────────────────┬──────────────────────────┘  │
     │                         │                             │
     │                         │ insertOne(document)         │
     │                         │────────────────────────────▶│
     │                         │                             │
     │                         │       acknowledgment        │
     │                         │◀────────────────────────────│
     │                         │                             │
     │    ┌────────────────────┴──────────────────────────┐  │
     │    │ [else] MongoDB Unavailable                    │  │
     │    │ catch(Exception) → LOG.error(); // silent     │  │
     │    └────────────────────┬──────────────────────────┘  │
     │                         │                             │
┌────┴────┐          ┌─────────┴──────────┐          ┌───────┴────────┐
│ Service │          │ TransactionLogger  │          │    MongoDB     │
└─────────┘          └────────────────────┘          └────────────────┘
```

---

## 5. Interaction Summary Matrix

| **Component ↓ / Calls →**         | Validator | Strategy Proc | Bureau Builder | Bureau SOAP | Bureau Mapper | Risk Calculator | Rest Builder | GW PreProc | Error Proc | Txn Logger | MongoDB |
|------------------------------------|-----------|---------------|----------------|-------------|---------------|-----------------|--------------|------------|------------|------------|---------|
| **CXF REST Endpoint (Route 1)**    | ✓         | ✓             | ✓              | ✓           | ✓             | ✓               | ✓            |            | ✓ (error)  | ✓ (async)  |         |
| **CXF SOAP Endpoint (Route 2)**    | ✓         | ✓             | ✓              | ✓           | ✓             | ✓               |              |            | ✓ (error)  | ✓ (async)  |         |
| **CXF GW Endpoint (Route 3)**      | ✓         | ✓             | ✓              | ✓           | ✓             | ✓               |              | ✓          | ✓ (error)  | ✓ (async)  |         |
| **Transaction Logger**             |           |               |                |             |               |                 |              |            |            |            | ✓       |
| **Risk Score Calculator**          |           |               |                |             |               |                 |              |            |            |            |         |
| **Scoring Strategy (delegated)**   |           |               |                |             |               | ← called by     |              |            |            |            |         |

---

## 6. Notes & Observations

| **#** | **Observation**                                                                 | **Diagram(s)**     | **Impact**    |
|-------|---------------------------------------------------------------------------------|--------------------|---------------|
| 1     | All 3 routes share identical processor chain after initial entry/pre-processing | SD-001, SD-002, SD-003 | Moderate — DRY principle applied via same bean references |
| 2     | REST route adds RestResponseBuilder; SOAP routes use native CXF serialization   | SD-001 vs SD-002   | Low           |
| 3     | Gateway route is only route with pre-processor for header enrichment            | SD-003             | Low           |
| 4     | Wire tap logging is non-blocking but has no failure alerting                    | SD-004             | High          |
| 5     | Strategy Pattern cleanly separates scoring logic but creates code duplication   | SD-006             | Medium        |
| 6     | Bureau call is synchronous with 30s timeout; no fallback or circuit breaker     | SD-001 Step 8      | High          |
| 7     | Service interface implementations (CreditRiskRestSvc, CreditRiskSoapSvcImpl) return null — all logic in processors | SD-001, SD-002 | Low — by design in Camel |

---

## 7. Dead Code Flow Paths

| **Diagram ID** | **Step(s)** | **Flow Path Description**                                          | **Evidence**                                                           | **Recommendation**                     |
|-----------------|-------------|--------------------------------------------------------------------|------------------------------------------------------------------------|----------------------------------------|
| SD-001          | —           | CreditRiskRestSvc.assessCreditRisk() method body returns null      | Method defined but Camel route handles all logic; method never called directly | Confirm with team; remove or add delegation |
| SD-002          | —           | CreditRiskSoapSvcImpl.assessCreditRisk() method body returns null  | Same pattern — SOAP endpoint delegates via Camel, not via implementation | Confirm with team; remove or add delegation |

---

## 8. Target State Sequence Diagrams (Proposed)

### 8.1 SD-001-TARGET: REST Credit Risk Assessment (Target State)

**Corresponding Current State:** SD-001
**Key Changes from Current State:**

| **Change #** | **Description of Change**                                          | **Reason**                               |
|--------------|--------------------------------------------------------------------|------------------------------------------|
| 1            | Add input validation: DOB age check, SIN checksum, postal-province | Addresses GAP-009, GAP-010, GAP-011, GAP-012 |
| 2            | Add circuit breaker around bureau SOAP call with fallback scoring  | Addresses GAP-023                        |
| 3            | Replace hardcoded monthly debt (3%) with configurable parameter    | Addresses GAP-003                        |
| 4            | Add actual income verification step (external call)                | Addresses GAP-002                        |
| 5            | Encrypt PII in MongoDB audit trail                                 | Addresses GAP-017                        |
| 6            | Add dead letter queue for failed MongoDB writes                    | Addresses GAP-022                        |
| 7            | Add granular error codes beyond CR-500                             | Addresses GAP-024                        |
| 8            | Add rate limiting at API gateway layer                             | Addresses GAP-019                        |

---

## 9. Appendices

### Appendix A: Route Configuration Reference

| **Route ID**                    | **From Endpoint**                         | **Blueprint Line** | **Entry Type** |
|---------------------------------|-------------------------------------------|--------------------|----------------|
| `in_rest_creditRiskRouter`      | `jaxrs:bean:creditRiskRestEndpoint`       | Line 165           | REST           |
| `in_soap_creditRiskRouter`      | `cxf:bean:creditRiskSoapEndpoint`         | Line 186           | SOAP + WSS4J   |
| `in_gw_creditRiskRouter`        | `cxf:bean:creditRiskGwEndpoint`           | Line 204           | SOAP (Gateway) |
| `creditRisk_logging_route`      | `direct:logTransaction`                   | Line 223           | Internal (Wire Tap) |

### Appendix B: CXF Endpoint Configuration

| **Endpoint Bean ID**          | **Address**                    | **Service Class**           | **Security**     |
|-------------------------------|--------------------------------|-----------------------------|------------------|
| `creditRiskRestEndpoint`      | `/service/rest/creditrisk`     | CreditRiskRestSvc           | None             |
| `creditRiskSoapEndpoint`      | `/service/soap/creditrisk`     | CreditRiskSoapSvc           | WSS4J UsernameToken |
| `creditRiskGwEndpoint`        | `/service/creditriskapi`       | CreditRiskSoapSvc           | None             |
| `bureauEndpoint`              | `{{BUREAU_ENDPOINT_URL}}`      | BureauScoreService          | None (client)    |

---

> **Document Control:**
> | Version | Date | Author | Changes |
> |---------|------|--------|---------|
> | 1.0 | 31-Mar-2026 | RE Automation (Local) | Initial draft — 6 sequence diagrams, 4 routes, cross-cutting patterns |
