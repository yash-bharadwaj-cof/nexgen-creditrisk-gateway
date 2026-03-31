# Component Catalog

---

| **Field**            | **Details**                                          |
|----------------------|------------------------------------------------------|
| **Project Name**     | NexGen ESB Modernisation                             |
| **Application Name** | nexgen-creditrisk-gateway                            |
| **Version**          | 1.0.0-SNAPSHOT                                       |
| **Date**             | 31-Mar-2026                                          |
| **Prepared By**      | Reverse Engineering Agent                            |
| **Reviewed By**      | _[Pending Client Review]_                            |
| **Status**           | Draft                                                |

---

## 1. Overview

This component catalog provides a complete inventory of all software components identified during the reverse engineering of `nexgen-creditrisk-gateway`. The service is a JBoss Fuse 6.3 OSGi bundle containing 33 Java types across 7 packages, wired through an OSGi Blueprint XML descriptor with 14 beans, 4 CXF endpoints, and 4 Apache Camel routes. This catalog maps every class, bean, dependency, and endpoint to support the planned migration.

---

## 2. Component Summary

| **Total Components** | **Modules** | **Services** | **Libraries** | **Shared Utilities** |
|----------------------|-------------|--------------|---------------|----------------------|
| 33                   | 7 packages  | 4 endpoints  | 20 deps       | 2 (logging)          |

### 2.1 Component Health Summary

| **Status**                       | **Count** | **Percentage** |
|----------------------------------|-----------|----------------|
| ✅ Active                         | 25        | 76%            |
| ⚠️ Partially Used                | 3         | 9%             |
| 🔴 Dead Code                     | 3         | 9%             |
| ⚪ Contains Hardcoded Values      | 7         | 21%            |

---

## 3. Component Inventory

### 3.1 Component: CreditRiskRestSvc

| **Attribute**            | **Details**                                                   |
|--------------------------|---------------------------------------------------------------|
| **Component ID**         | COMP-001                                                      |
| **Component Name**       | CreditRiskRestSvc                                             |
| **Type**                 | Service                                                       |
| **Layer**                | Presentation / Integration                                    |
| **Package/Namespace**    | com.nexgen.esb.creditrisk.service                             |
| **Source Path**          | src/main/java/com/nexgen/esb/creditrisk/service/              |
| **Language**             | Java 8                                                        |
| **Framework**            | Apache CXF JAX-RS, Swagger 1.5                               |
| **Framework Version**    | CXF 3.1.5.redhat-630187                                      |
| **Description**          | JAX-RS REST endpoint class. Defines `GET /assess` with Swagger annotations. Method body returns `null`; actual processing is handled by Camel route `in_rest_creditRiskRouter`. |
| **Business Capability**  | REST API entry point for credit risk assessment               |
| **Owner/Team**           | NexGen ESB Team                                               |
| **Status**               | Partially Used (annotations only — body is dead code)         |
| **Migration Risk**       | 🟡 Medium                                                    |
| **Hardcoded Values**     | No                                                            |
| **Dead Code Present**    | Yes — `assessCreditRisk()` body always returns `null`         |

#### Dependencies

| **Depends On**          | **Dependency Type** | **Version**       | **Support Status** | **Migration Risk** | **Notes**                     |
|-------------------------|---------------------|-------------------|--------------------|---------------------|-------------------------------|
| CreditRiskResType       | Compile             | —                 | ✅ Internal         | 🟢 Low              | Return type model             |
| io.swagger.annotations  | Compile             | 1.5.16            | 🔴 EOL              | 🟡 Med              | Swagger 1.x → OpenAPI 3      |
| javax.ws.rs             | Compile             | JAX-RS 2.0        | ⚠️ Nearing EOL     | 🟡 Med              | Jakarta namespace migration   |

#### Exposed Interfaces

| **Interface**           | **Type** | **Auth Mechanism** | **Rate Limit** | **Description**                                   |
|-------------------------|----------|--------------------|----------------|---------------------------------------------------|
| `GET /service/rest/creditrisk/assess` | REST | None              | No             | Credit risk assessment with query parameters      |

#### Key Classes / Files

| **Class/File Name**     | **Responsibility**                            | **LOC** |
|-------------------------|-----------------------------------------------|---------|
| CreditRiskRestSvc.java  | JAX-RS endpoint definition + Swagger metadata | ~35     |

---

### 3.2 Component: CreditRiskSoapSvc (Interface)

| **Attribute**            | **Details**                                                   |
|--------------------------|---------------------------------------------------------------|
| **Component ID**         | COMP-002                                                      |
| **Component Name**       | CreditRiskSoapSvc                                             |
| **Type**                 | Service (Interface)                                           |
| **Layer**                | Presentation / Integration                                    |
| **Package/Namespace**    | com.nexgen.esb.creditrisk.service                             |
| **Source Path**          | src/main/java/com/nexgen/esb/creditrisk/service/              |
| **Language**             | Java 8                                                        |
| **Framework**            | Apache CXF JAX-WS                                            |
| **Framework Version**    | CXF 3.1.5.redhat-630187                                      |
| **Description**          | JAX-WS interface defining the SOAP contract for `assessCreditRisk`. Used by both SOAP and Gateway CXF endpoints in blueprint.xml. |
| **Business Capability**  | SOAP contract for credit risk assessment                      |
| **Owner/Team**           | NexGen ESB Team                                               |
| **Status**               | Active                                                        |
| **Migration Risk**       | 🔴 High                                                      |
| **Hardcoded Values**     | No                                                            |
| **Dead Code Present**    | No                                                            |

#### Exposed Interfaces

| **Interface**           | **Type** | **Auth Mechanism**                    | **Rate Limit** | **Description**                   |
|-------------------------|----------|---------------------------------------|----------------|-----------------------------------|
| `assessCreditRisk`      | SOAP     | WS-Security WSS4J UsernameToken       | No             | SOAP operation for assessment     |

---

### 3.3 Component: CreditRiskSoapSvcImpl

| **Attribute**            | **Details**                                                   |
|--------------------------|---------------------------------------------------------------|
| **Component ID**         | COMP-003                                                      |
| **Component Name**       | CreditRiskSoapSvcImpl                                         |
| **Type**                 | Service                                                       |
| **Layer**                | Presentation / Integration                                    |
| **Package/Namespace**    | com.nexgen.esb.creditrisk.service                             |
| **Source Path**          | src/main/java/com/nexgen/esb/creditrisk/service/              |
| **Language**             | Java 8                                                        |
| **Framework**            | Apache CXF JAX-WS                                            |
| **Framework Version**    | CXF 3.1.5.redhat-630187                                      |
| **Description**          | JAX-WS implementation. Method `assessCreditRisk()` always returns `null`. Camel intercepts the CXF endpoint before this method is reached — this is a dead code stub registered as a blueprint bean. |
| **Business Capability**  | SOAP endpoint implementation (stub only)                      |
| **Owner/Team**           | NexGen ESB Team                                               |
| **Status**               | 🔴 Dead Code                                                 |
| **Migration Risk**       | 🟢 Low                                                       |
| **Hardcoded Values**     | No                                                            |
| **Dead Code Present**    | Yes — entire method body is dead                              |

---

### 3.4 Component: CreditRiskServiceException

| **Attribute**            | **Details**                                                   |
|--------------------------|---------------------------------------------------------------|
| **Component ID**         | COMP-004                                                      |
| **Component Name**       | CreditRiskServiceException                                    |
| **Type**                 | Utility                                                       |
| **Layer**                | Cross-Cutting                                                 |
| **Package/Namespace**    | com.nexgen.esb.creditrisk.service                             |
| **Source Path**          | src/main/java/com/nexgen/esb/creditrisk/service/              |
| **Language**             | Java 8                                                        |
| **Framework**            | None (plain Java)                                             |
| **Framework Version**    | —                                                             |
| **Description**          | Custom checked exception used in the SOAP service interface declaration. Contains inner `DetailException` class. |
| **Business Capability**  | SOAP fault mapping                                            |
| **Owner/Team**           | NexGen ESB Team                                               |
| **Status**               | Active                                                        |
| **Migration Risk**       | 🟢 Low                                                       |
| **Hardcoded Values**     | No                                                            |
| **Dead Code Present**    | No                                                            |

---

### 3.5 Component: CreditRiskRequestValidator

| **Attribute**            | **Details**                                                   |
|--------------------------|---------------------------------------------------------------|
| **Component ID**         | COMP-005                                                      |
| **Component Name**       | CreditRiskRequestValidator                                    |
| **Type**                 | Module (Camel Processor)                                      |
| **Layer**                | Business Logic                                                |
| **Package/Namespace**    | com.nexgen.esb.creditrisk.processor                           |
| **Source Path**          | src/main/java/com/nexgen/esb/creditrisk/processor/            |
| **Language**             | Java 8                                                        |
| **Framework**            | Apache Camel Processor                                        |
| **Framework Version**    | Camel 2.17.0.redhat-630187                                    |
| **Description**          | Validates incoming requests against 7 business rules (CR-001 through CR-007). Validates applicantId, firstName, lastName, DOB format, SIN format, province code, and postal code. Populates exchange logging properties. Contains inner `ValidationException` class. |
| **Business Capability**  | Request input validation                                      |
| **Owner/Team**           | NexGen ESB Team                                               |
| **Status**               | Active                                                        |
| **Migration Risk**       | 🟡 Medium                                                    |
| **Hardcoded Values**     | Yes — SIN_PATTERN, DOB_PATTERN, POSTAL_CODE_PATTERN regexes  |
| **Dead Code Present**    | No                                                            |

#### Dependencies

| **Depends On**          | **Dependency Type** | **Version**        | **Support Status** | **Migration Risk** | **Notes**                     |
|-------------------------|---------------------|--------------------|---------------------|---------------------|-------------------------------|
| camel-core (Processor)  | Compile             | 2.17.0.redhat      | 🔴 EOL              | 🔴 High             | Camel 3/4 API changes         |
| commons-lang3           | Compile             | 3.4                | ✅ Supported         | 🟢 Low              | StringUtils null checks       |
| ProvinceType enum       | Compile             | —                  | ✅ Internal          | 🟢 Low              | Province validation           |

---

### 3.6 Component: ScoringStrategyProcessor

| **Attribute**            | **Details**                                                   |
|--------------------------|---------------------------------------------------------------|
| **Component ID**         | COMP-006                                                      |
| **Component Name**       | ScoringStrategyProcessor                                      |
| **Type**                 | Module (Camel Processor)                                      |
| **Layer**                | Business Logic                                                |
| **Package/Namespace**    | com.nexgen.esb.creditrisk.processor                           |
| **Source Path**          | src/main/java/com/nexgen/esb/creditrisk/processor/            |
| **Language**             | Java 8                                                        |
| **Framework**            | Apache Camel Processor                                        |
| **Framework Version**    | Camel 2.17.0.redhat-630187                                    |
| **Description**          | Strategy Pattern implementation. Resolves the appropriate `ScoringStrategy` based on product type: MORTGAGE/AUTO_LOAN → Conservative, CREDIT_CARD/LINE_OF_CREDIT → Aggressive, default → Standard. Has blueprint property `defaultStrategy` injected from config. |
| **Business Capability**  | Scoring algorithm selection                                   |
| **Owner/Team**           | NexGen ESB Team                                               |
| **Status**               | Active                                                        |
| **Migration Risk**       | 🟡 Medium                                                    |
| **Hardcoded Values**     | Yes — product type strings: "MORTGAGE", "AUTO_LOAN", "CREDIT_CARD", "LINE_OF_CREDIT"; also "conservative" default check |
| **Dead Code Present**    | Yes — `"aggressive"` default strategy branch is missing; falls through to Standard |

---

### 3.7 Component: BureauRequestBuilder

| **Attribute**            | **Details**                                                   |
|--------------------------|---------------------------------------------------------------|
| **Component ID**         | COMP-007                                                      |
| **Component Name**       | BureauRequestBuilder                                          |
| **Type**                 | Module (Camel Processor)                                      |
| **Layer**                | Integration                                                   |
| **Package/Namespace**    | com.nexgen.esb.creditrisk.processor                           |
| **Source Path**          | src/main/java/com/nexgen/esb/creditrisk/processor/            |
| **Language**             | Java 8                                                        |
| **Framework**            | Apache Camel Processor                                        |
| **Framework Version**    | Camel 2.17.0.redhat-630187                                    |
| **Description**          | Builds the outbound `BureauInquiryRequest` SOAP payload. Maps applicant data to bureau format, generates unique request ID, sets CXF operation headers. Blueprint-injected properties: requestIdPrefix, subscriberCode, subscriberName. |
| **Business Capability**  | External credit bureau integration — outbound request         |
| **Owner/Team**           | NexGen ESB Team                                               |
| **Status**               | Active                                                        |
| **Migration Risk**       | 🔴 High                                                      |
| **Hardcoded Values**     | Yes — TIMESTAMP_FORMAT, operationNamespace URI `"http://ws.esb.nexgen.com/bureau/v1"` |
| **Dead Code Present**    | No                                                            |

---

### 3.8 Component: BureauResponseMapper

| **Attribute**            | **Details**                                                   |
|--------------------------|---------------------------------------------------------------|
| **Component ID**         | COMP-008                                                      |
| **Component Name**       | BureauResponseMapper                                          |
| **Type**                 | Module (Camel Processor)                                      |
| **Layer**                | Integration                                                   |
| **Package/Namespace**    | com.nexgen.esb.creditrisk.processor                           |
| **Source Path**          | src/main/java/com/nexgen/esb/creditrisk/processor/            |
| **Language**             | Java 8                                                        |
| **Framework**            | Apache Camel Processor                                        |
| **Framework Version**    | Camel 2.17.0.redhat-630187                                    |
| **Description**          | Maps external `BureauInquiryResponse` to internal `CreditScoreDetail`. Handles null/error responses by setting BUREAU_ERROR exchange property. Calculates utilization rate. Maps bureau score to range bands (EXCEPTIONAL/VERY_GOOD/GOOD/FAIR/POOR). |
| **Business Capability**  | External credit bureau integration — response mapping         |
| **Owner/Team**           | NexGen ESB Team                                               |
| **Status**               | Active                                                        |
| **Migration Risk**       | 🟡 Medium                                                    |
| **Hardcoded Values**     | Yes — score range thresholds: 800, 740, 670, 580              |
| **Dead Code Present**    | No                                                            |

---

### 3.9 Component: RiskScoreCalculator

| **Attribute**            | **Details**                                                   |
|--------------------------|---------------------------------------------------------------|
| **Component ID**         | COMP-009                                                      |
| **Component Name**       | RiskScoreCalculator                                           |
| **Type**                 | Module (Camel Processor)                                      |
| **Layer**                | Business Logic                                                |
| **Package/Namespace**    | com.nexgen.esb.creditrisk.processor                           |
| **Source Path**          | src/main/java/com/nexgen/esb/creditrisk/processor/            |
| **Language**             | Java 8                                                        |
| **Framework**            | Apache Camel Processor                                        |
| **Framework Version**    | Camel 2.17.0.redhat-630187                                    |
| **Description**          | Core risk assessment processor. Builds complete `CreditRiskResType` response including income verification, employment risk, debt service details, risk categorization, overall score, recommendation, and risk factors. Delegates scoring to the Strategy set by `ScoringStrategyProcessor`. Handles bureau error fallback (INDETERMINATE/REFER_TO_UNDERWRITER). |
| **Business Capability**  | Risk score calculation and assessment                         |
| **Owner/Team**           | NexGen ESB Team                                               |
| **Status**               | Active                                                        |
| **Migration Risk**       | 🔴 High                                                      |
| **Hardcoded Values**     | Yes — monthly debt factor 0.03, monthly payment factor 0.006, risk factor thresholds (650, 0.50, 5, 0.40), status codes ("W215E", "SUCCESS"), accuracy codes ("LM", "FD") |
| **Dead Code Present**    | Yes — `scoringStrategy` field injected via blueprint but never read in `process()` |

---

### 3.10 Component: GatewayRequestPreProcessor

| **Attribute**            | **Details**                                                   |
|--------------------------|---------------------------------------------------------------|
| **Component ID**         | COMP-010                                                      |
| **Component Name**       | GatewayRequestPreProcessor                                    |
| **Type**                 | Module (Camel Processor)                                      |
| **Layer**                | Integration                                                   |
| **Package/Namespace**    | com.nexgen.esb.creditrisk.processor                           |
| **Source Path**          | src/main/java/com/nexgen/esb/creditrisk/processor/            |
| **Language**             | Java 8                                                        |
| **Framework**            | Apache Camel Processor                                        |
| **Framework Version**    | Camel 2.17.0.redhat-630187                                    |
| **Description**          | Pre-processes requests from the Gateway SOAP endpoint. Injects default `RequestHeader` with source system "GATEWAY" and generates a transaction ID. Sets default request channel to "API" if missing. |
| **Business Capability**  | Gateway integration pre-processing                            |
| **Owner/Team**           | NexGen ESB Team                                               |
| **Status**               | Active                                                        |
| **Migration Risk**       | 🟡 Medium                                                    |
| **Hardcoded Values**     | Yes — `"GATEWAY"` source system, `"API"` request channel      |
| **Dead Code Present**    | No                                                            |

---

### 3.11 Component: RestResponseBuilder

| **Attribute**            | **Details**                                                   |
|--------------------------|---------------------------------------------------------------|
| **Component ID**         | COMP-011                                                      |
| **Component Name**       | RestResponseBuilder                                           |
| **Type**                 | Module (Camel Processor)                                      |
| **Layer**                | Presentation                                                  |
| **Package/Namespace**    | com.nexgen.esb.creditrisk.processor                           |
| **Source Path**          | src/main/java/com/nexgen/esb/creditrisk/processor/            |
| **Language**             | Java 8                                                        |
| **Framework**            | Apache Camel Processor                                        |
| **Framework Version**    | Camel 2.17.0.redhat-630187                                    |
| **Description**          | Builds REST-specific response format. Retrieves the `RISK_RESPONSE` exchange property, sets HTTP 200 status code and `application/xml` content type. Used only in the REST route. |
| **Business Capability**  | REST response formatting                                      |
| **Owner/Team**           | NexGen ESB Team                                               |
| **Status**               | Active                                                        |
| **Migration Risk**       | 🟢 Low                                                       |
| **Hardcoded Values**     | No                                                            |
| **Dead Code Present**    | No                                                            |

---

### 3.12 Component: ErrorProcessor

| **Attribute**            | **Details**                                                   |
|--------------------------|---------------------------------------------------------------|
| **Component ID**         | COMP-012                                                      |
| **Component Name**       | ErrorProcessor                                                |
| **Type**                 | Module (Camel Processor)                                      |
| **Layer**                | Cross-Cutting                                                 |
| **Package/Namespace**    | com.nexgen.esb.creditrisk.processor                           |
| **Source Path**          | src/main/java/com/nexgen/esb/creditrisk/processor/            |
| **Language**             | Java 8                                                        |
| **Framework**            | Apache Camel Processor                                        |
| **Framework Version**    | Camel 2.17.0.redhat-630187                                    |
| **Description**          | Centralized error handler. Maps `ValidationException` to HTTP 400 with specific error code. Maps all other exceptions to HTTP 500 with generic `CR-500` code. Sets `ERROR` risk category and `REFER_TO_UNDERWRITER` recommendation on error responses. |
| **Business Capability**  | Error response generation                                     |
| **Owner/Team**           | NexGen ESB Team                                               |
| **Status**               | Active                                                        |
| **Migration Risk**       | 🟡 Medium                                                    |
| **Hardcoded Values**     | Yes — error code `"CR-500"`, generic message                  |
| **Dead Code Present**    | No                                                            |

---

### 3.13 Component: ScoringStrategy (Interface)

| **Attribute**            | **Details**                                                   |
|--------------------------|---------------------------------------------------------------|
| **Component ID**         | COMP-013                                                      |
| **Component Name**       | ScoringStrategy                                               |
| **Type**                 | Service (Interface)                                           |
| **Layer**                | Business Logic                                                |
| **Package/Namespace**    | com.nexgen.esb.creditrisk.scoring                             |
| **Source Path**          | src/main/java/com/nexgen/esb/creditrisk/scoring/              |
| **Language**             | Java 8                                                        |
| **Framework**            | None (Strategy Pattern)                                       |
| **Framework Version**    | —                                                             |
| **Description**          | Strategy interface defining 5 methods: `getStrategyName()`, `categorizeRisk()`, `calculateOverallScore()`, `determineRecommendation()`, `determineAffordabilityRating()`. Three implementations: Standard, Conservative, Aggressive. |
| **Business Capability**  | Scoring algorithm contract                                    |
| **Owner/Team**           | NexGen ESB Team                                               |
| **Status**               | Active                                                        |
| **Migration Risk**       | 🟢 Low                                                       |
| **Hardcoded Values**     | No                                                            |
| **Dead Code Present**    | No                                                            |

---

### 3.14 Component: StandardScoringStrategy

| **Attribute**            | **Details**                                                   |
|--------------------------|---------------------------------------------------------------|
| **Component ID**         | COMP-014                                                      |
| **Component Name**       | StandardScoringStrategy                                       |
| **Type**                 | Module                                                        |
| **Layer**                | Business Logic                                                |
| **Package/Namespace**    | com.nexgen.esb.creditrisk.scoring                             |
| **Source Path**          | src/main/java/com/nexgen/esb/creditrisk/scoring/              |
| **Language**             | Java 8                                                        |
| **Framework**            | None (Strategy Pattern)                                       |
| **Framework Version**    | —                                                             |
| **Description**          | Standard/balanced scoring strategy. Weights: Bureau 40%, DTI 30%, Utilization 20%, Employment 10%. Thresholds: EXCELLENT ≥750 + DTI<30% + util<30%, GOOD ≥680, FAIR ≥620, POOR ≥560, VERY_POOR <560. Also used as default in `RiskScoreCalculator.buildDebtService()` for affordability. |
| **Business Capability**  | Default credit risk scoring algorithm                         |
| **Owner/Team**           | NexGen ESB Team                                               |
| **Status**               | Active                                                        |
| **Migration Risk**       | 🟡 Medium                                                    |
| **Hardcoded Values**     | Yes — weights (40/30/20/10), thresholds (750/680/620/560), normalization factors, employment scores, affordability thresholds (0.28/0.36/0.44) |
| **Dead Code Present**    | No                                                            |

---

### 3.15 Component: ConservativeScoringStrategy

| **Attribute**            | **Details**                                                   |
|--------------------------|---------------------------------------------------------------|
| **Component ID**         | COMP-015                                                      |
| **Component Name**       | ConservativeScoringStrategy                                   |
| **Type**                 | Module                                                        |
| **Layer**                | Business Logic                                                |
| **Package/Namespace**    | com.nexgen.esb.creditrisk.scoring                             |
| **Source Path**          | src/main/java/com/nexgen/esb/creditrisk/scoring/              |
| **Language**             | Java 8                                                        |
| **Framework**            | None (Strategy Pattern)                                       |
| **Framework Version**    | —                                                             |
| **Description**          | Conservative scoring for secured products (MORTGAGE, AUTO_LOAN). Tighter thresholds: EXCELLENT ≥780 + DTI<25% + util<25%, GOOD ≥720 + DTI<35%, FAIR ≥660, POOR ≥600. Weights: Bureau 35%, DTI 35%, Util 15%, Employment 15%. |
| **Business Capability**  | Secured product credit risk scoring                           |
| **Owner/Team**           | NexGen ESB Team                                               |
| **Status**               | Active                                                        |
| **Migration Risk**       | 🟡 Medium                                                    |
| **Hardcoded Values**     | Yes — weights (35/35/15/15), thresholds (780/720/660/600), affordability thresholds (0.25/0.32/0.40) |
| **Dead Code Present**    | No                                                            |

---

### 3.16 Component: AggressiveScoringStrategy

| **Attribute**            | **Details**                                                   |
|--------------------------|---------------------------------------------------------------|
| **Component ID**         | COMP-016                                                      |
| **Component Name**       | AggressiveScoringStrategy                                     |
| **Type**                 | Module                                                        |
| **Layer**                | Business Logic                                                |
| **Package/Namespace**    | com.nexgen.esb.creditrisk.scoring                             |
| **Source Path**          | src/main/java/com/nexgen/esb/creditrisk/scoring/              |
| **Language**             | Java 8                                                        |
| **Framework**            | None (Strategy Pattern)                                       |
| **Framework Version**    | —                                                             |
| **Description**          | Aggressive/relaxed scoring for unsecured products (CREDIT_CARD, LINE_OF_CREDIT). Thresholds: EXCELLENT ≥720 + DTI<40%, GOOD ≥650, FAIR ≥580, POOR ≥520. Weights: Bureau 50%, DTI 25%, Util 15%, Employment 10%. |
| **Business Capability**  | Unsecured product credit risk scoring                         |
| **Owner/Team**           | NexGen ESB Team                                               |
| **Status**               | Active                                                        |
| **Migration Risk**       | 🟡 Medium                                                    |
| **Hardcoded Values**     | Yes — weights (50/25/15/10), thresholds (720/650/580/520), affordability thresholds (0.35/0.45/0.55) |
| **Dead Code Present**    | No                                                            |

---

### 3.17 Component: CreditRiskReqType

| **Attribute**            | **Details**                                                   |
|--------------------------|---------------------------------------------------------------|
| **Component ID**         | COMP-017                                                      |
| **Component Name**       | CreditRiskReqType                                             |
| **Type**                 | Module (Data Model)                                           |
| **Layer**                | Data                                                          |
| **Package/Namespace**    | com.nexgen.esb.creditrisk.model                               |
| **Source Path**          | src/main/java/com/nexgen/esb/creditrisk/model/                |
| **Language**             | Java 8                                                        |
| **Framework**            | JAXB                                                          |
| **Description**          | Request data model. Fields: applicantId, firstName, lastName, dateOfBirth, socialInsuranceNumber, employmentStatus, annualIncome, province, postalCode, productType, requestedAmount, requestChannel, requestHeader. |
| **Status**               | Active                                                        |
| **Migration Risk**       | 🟢 Low                                                       |
| **Hardcoded Values**     | No                                                            |
| **Dead Code Present**    | No                                                            |

---

### 3.18 Component: CreditRiskResType

| **Attribute**            | **Details**                                                   |
|--------------------------|---------------------------------------------------------------|
| **Component ID**         | COMP-018                                                      |
| **Component Name**       | CreditRiskResType                                             |
| **Type**                 | Module (Data Model)                                           |
| **Layer**                | Data                                                          |
| **Package/Namespace**    | com.nexgen.esb.creditrisk.model                               |
| **Source Path**          | src/main/java/com/nexgen/esb/creditrisk/model/                |
| **Language**             | Java 8                                                        |
| **Framework**            | JAXB                                                          |
| **Description**          | Response data model. Fields: responseHeader, applicantId, riskCategory, overallScore, recommendation, scoringModelVersion, accuracyCode, creditScoreDetail, incomeVerification, employmentRisk, debtService, riskFactors. |
| **Status**               | Active                                                        |
| **Migration Risk**       | 🟢 Low                                                       |
| **Hardcoded Values**     | No                                                            |
| **Dead Code Present**    | No                                                            |

---

### 3.19 Component: RequestHeader

| **Attribute**            | **Details**                                                   |
|--------------------------|---------------------------------------------------------------|
| **Component ID**         | COMP-019                                                      |
| **Component Name**       | RequestHeader                                                 |
| **Type**                 | Module (Data Model)                                           |
| **Layer**                | Data                                                          |
| **Package/Namespace**    | com.nexgen.esb.creditrisk.model                               |
| **Description**          | Request header model. Fields: transactionId, sourceSystem, timestamp, userId. |
| **Status**               | Partially Used                                                |
| **Migration Risk**       | 🟢 Low                                                       |
| **Dead Code Present**    | Yes — `userId` field never set in any processor               |

---

### 3.20 Component: ResponseHeader

| **Attribute**            | **Details**                                                   |
|--------------------------|---------------------------------------------------------------|
| **Component ID**         | COMP-020                                                      |
| **Component Name**       | ResponseHeader                                                |
| **Type**                 | Module (Data Model)                                           |
| **Layer**                | Data                                                          |
| **Package/Namespace**    | com.nexgen.esb.creditrisk.model                               |
| **Description**          | Response header model. Fields: transactionId, timestamp, statusCode, statusMessage. |
| **Status**               | Active                                                        |
| **Migration Risk**       | 🟢 Low                                                       |

---

### 3.21 Component: CreditScoreDetail

| **Attribute**            | **Details**                                                   |
|--------------------------|---------------------------------------------------------------|
| **Component ID**         | COMP-021                                                      |
| **Component Name**       | CreditScoreDetail                                             |
| **Type**                 | Module (Data Model)                                           |
| **Layer**                | Data                                                          |
| **Package/Namespace**    | com.nexgen.esb.creditrisk.model                               |
| **Description**          | Credit score detail model. Fields: bureauScore, bureauScoreRange, delinquencyCount, inquiryCount, openAccountCount, totalCreditLimit, totalBalance, utilizationRate. |
| **Status**               | Active                                                        |
| **Migration Risk**       | 🟢 Low                                                       |

---

### 3.22 Component: DebtServiceDetail

| **Attribute**            | **Details**                                                   |
|--------------------------|---------------------------------------------------------------|
| **Component ID**         | COMP-022                                                      |
| **Component Name**       | DebtServiceDetail                                             |
| **Type**                 | Module (Data Model)                                           |
| **Layer**                | Data                                                          |
| **Package/Namespace**    | com.nexgen.esb.creditrisk.model                               |
| **Description**          | Debt service detail model. Fields: totalMonthlyIncome, totalMonthlyDebt, debtServiceRatio, totalDebtServiceRatio, affordabilityRating. |
| **Status**               | Active                                                        |
| **Migration Risk**       | 🟢 Low                                                       |

---

### 3.23 Component: EmploymentRiskDetail

| **Attribute**            | **Details**                                                   |
|--------------------------|---------------------------------------------------------------|
| **Component ID**         | COMP-023                                                      |
| **Component Name**       | EmploymentRiskDetail                                          |
| **Type**                 | Module (Data Model)                                           |
| **Layer**                | Data                                                          |
| **Package/Namespace**    | com.nexgen.esb.creditrisk.model                               |
| **Description**          | Employment risk detail model. Fields: employmentType, riskLevel, yearsEmployed, industryCategory. |
| **Status**               | Partially Used                                                |
| **Migration Risk**       | 🟢 Low                                                       |
| **Dead Code Present**    | Yes — `yearsEmployed` and `industryCategory` fields never populated |

---

### 3.24 Component: IncomeVerificationDetail

| **Attribute**            | **Details**                                                   |
|--------------------------|---------------------------------------------------------------|
| **Component ID**         | COMP-024                                                      |
| **Component Name**       | IncomeVerificationDetail                                      |
| **Type**                 | Module (Data Model)                                           |
| **Layer**                | Data                                                          |
| **Package/Namespace**    | com.nexgen.esb.creditrisk.model                               |
| **Description**          | Income verification detail model. Fields: reportedIncome, verifiedIncome, verificationStatus, incomeSource, debtToIncomeRatio. |
| **Status**               | Active                                                        |
| **Migration Risk**       | 🟢 Low                                                       |

---

### 3.25 Component: ProvinceType (Enum)

| **Attribute**            | **Details**                                                   |
|--------------------------|---------------------------------------------------------------|
| **Component ID**         | COMP-025                                                      |
| **Component Name**       | ProvinceType                                                  |
| **Type**                 | Module (Enum)                                                 |
| **Layer**                | Data                                                          |
| **Package/Namespace**    | com.nexgen.esb.creditrisk.model                               |
| **Description**          | Canadian province/territory code enumeration. Used by `CreditRiskRequestValidator` for province validation. Note: validator uses this enum instead of the `SUPPORTED_PROVINCE_LIST` config property. |
| **Status**               | Active                                                        |
| **Migration Risk**       | 🟢 Low                                                       |

---

### 3.26 Component: RequestChannelType (Enum)

| **Attribute**            | **Details**                                                   |
|--------------------------|---------------------------------------------------------------|
| **Component ID**         | COMP-026                                                      |
| **Component Name**       | RequestChannelType                                            |
| **Type**                 | Module (Enum)                                                 |
| **Layer**                | Data                                                          |
| **Package/Namespace**    | com.nexgen.esb.creditrisk.model                               |
| **Description**          | Request channel enumeration (REST, SOAP, API, GATEWAY, etc.). |
| **Status**               | Active                                                        |
| **Migration Risk**       | 🟢 Low                                                       |

---

### 3.27 Component: BureauInquiryRequest

| **Attribute**            | **Details**                                                   |
|--------------------------|---------------------------------------------------------------|
| **Component ID**         | COMP-027                                                      |
| **Component Name**       | BureauInquiryRequest                                          |
| **Type**                 | Module (CXF Generated)                                        |
| **Layer**                | Integration                                                   |
| **Package/Namespace**    | com.nexgen.esb.creditrisk.generated                           |
| **Description**          | CXF-generated outbound request type for the external credit bureau SOAP service. Fields: requestId, timestamp, productType, subscriber, subject. |
| **Status**               | Active                                                        |
| **Migration Risk**       | 🟡 Medium (WSDL dependency)                                  |

---

### 3.28 Component: BureauInquiryResponse

| **Attribute**            | **Details**                                                   |
|--------------------------|---------------------------------------------------------------|
| **Component ID**         | COMP-028                                                      |
| **Component Name**       | BureauInquiryResponse                                         |
| **Type**                 | Module (CXF Generated)                                        |
| **Layer**                | Integration                                                   |
| **Package/Namespace**    | com.nexgen.esb.creditrisk.generated                           |
| **Description**          | CXF-generated inbound response type from the external credit bureau. Fields: creditScore, delinquencyCount, inquiryCount, openTradelineCount, totalCreditLimit, totalBalance, errorCode, errorMessage. |
| **Status**               | Active                                                        |
| **Migration Risk**       | 🟡 Medium (WSDL dependency)                                  |

---

### 3.29 Component: BureauScoreService (Interface)

| **Attribute**            | **Details**                                                   |
|--------------------------|---------------------------------------------------------------|
| **Component ID**         | COMP-029                                                      |
| **Component Name**       | BureauScoreService                                            |
| **Type**                 | Service (Interface / CXF Generated)                           |
| **Layer**                | Integration                                                   |
| **Package/Namespace**    | com.nexgen.esb.creditrisk.generated                           |
| **Description**          | CXF-generated service interface for the external bureau SOAP endpoint. Referenced in blueprint.xml as the `serviceClass` for `bureauEndpoint`. |
| **Status**               | Active                                                        |
| **Migration Risk**       | 🔴 High (external WSDL contract)                             |

---

### 3.30 Component: BureauSubject

| **Attribute**            | **Details**                                                   |
|--------------------------|---------------------------------------------------------------|
| **Component ID**         | COMP-030                                                      |
| **Component Name**       | BureauSubject                                                 |
| **Type**                 | Module (CXF Generated)                                        |
| **Layer**                | Integration                                                   |
| **Package/Namespace**    | com.nexgen.esb.creditrisk.generated                           |
| **Description**          | Bureau applicant subject model. Fields: firstName, lastName, dateOfBirth, socialInsuranceNumber, province, postalCode. |
| **Status**               | Active                                                        |
| **Migration Risk**       | 🟡 Medium                                                    |

---

### 3.31 Component: BureauSubscriber

| **Attribute**            | **Details**                                                   |
|--------------------------|---------------------------------------------------------------|
| **Component ID**         | COMP-031                                                      |
| **Component Name**       | BureauSubscriber                                              |
| **Type**                 | Module (CXF Generated)                                        |
| **Layer**                | Integration                                                   |
| **Package/Namespace**    | com.nexgen.esb.creditrisk.generated                           |
| **Description**          | Bureau subscriber identification model. Fields: subscriberCode, subscriberName. |
| **Status**               | Active                                                        |
| **Migration Risk**       | 🟢 Low                                                       |

---

### 3.32 Component: TransactionLogger

| **Attribute**            | **Details**                                                   |
|--------------------------|---------------------------------------------------------------|
| **Component ID**         | COMP-032                                                      |
| **Component Name**       | TransactionLogger                                             |
| **Type**                 | Module                                                        |
| **Layer**                | Cross-Cutting                                                 |
| **Package/Namespace**    | com.nexgen.esb.creditrisk.logging                             |
| **Source Path**          | src/main/java/com/nexgen/esb/creditrisk/logging/              |
| **Language**             | Java 8                                                        |
| **Framework**            | MongoDB Java Driver 3.4.2                                     |
| **Framework Version**    | 3.4.2                                                         |
| **Description**          | Wire Tap transaction logger. Creates `MongoClient` per invocation (no connection pooling), builds a BSON `Document` with transaction details + risk assessment results, inserts into configured MongoDB collection. Logs errors but does not propagate them (fire-and-forget pattern). Blueprint-injected: mongoHost, mongoPort, mongoDb, mongoCollection. |
| **Business Capability**  | Transaction audit logging                                     |
| **Owner/Team**           | NexGen ESB Team                                               |
| **Status**               | Active                                                        |
| **Migration Risk**       | 🔴 High                                                      |
| **Hardcoded Values**     | No (all config injected)                                      |
| **Dead Code Present**    | No                                                            |

---

### 3.33 Component: LoggerConstants (Interface)

| **Attribute**            | **Details**                                                   |
|--------------------------|---------------------------------------------------------------|
| **Component ID**         | COMP-033                                                      |
| **Component Name**       | LoggerConstants                                               |
| **Type**                 | Utility (Constants Interface)                                 |
| **Layer**                | Cross-Cutting                                                 |
| **Package/Namespace**    | com.nexgen.esb.creditrisk.logging                             |
| **Description**          | Defines constant keys for Camel exchange properties used in transaction logging: LOG_TRANSACTION_ID, LOG_APPLICANT_ID, LOG_PROVINCE, LOG_PRODUCT_TYPE, LOG_REQUEST_CHANNEL, LOG_SOURCE_SYSTEM. |
| **Status**               | Active                                                        |
| **Migration Risk**       | 🟢 Low                                                       |

---

## 4. Component Dependency Map

### 4.1 Dependency Matrix

| **Component ↓ / Depends On →** | COMP-005 | COMP-006 | COMP-007 | COMP-008 | COMP-009 | COMP-010 | COMP-011 | COMP-012 | COMP-013 | COMP-032 |
|---------------------------------|----------|----------|----------|----------|----------|----------|----------|----------|----------|----------|
| **Route 1 (REST)**              | ✓        | ✓        | ✓        | ✓        | ✓        |          | ✓        | ✓        |          | ✓        |
| **Route 2 (SOAP)**              | ✓        | ✓        | ✓        | ✓        | ✓        |          |          | ✓        |          | ✓        |
| **Route 3 (Gateway)**           | ✓        | ✓        | ✓        | ✓        | ✓        | ✓        |          | ✓        |          | ✓        |
| **Route 4 (Logging)**           |          |          |          |          |          |          |          |          |          | ✓        |
| **COMP-006 (ScoringProc)**      |          |          |          |          |          |          |          |          | ✓        |          |
| **COMP-009 (RiskCalc)**         |          |          |          |          |          |          |          |          | ✓        |          |

### 4.2 Dependency Diagram

```
┌────────────────────────────────────────────────────────────────────────────────┐
│                              INBOUND ENDPOINTS                                 │
│  ┌──────────────┐   ┌──────────────────┐   ┌─────────────────────┐            │
│  │ REST (COMP-1)│   │ SOAP (COMP-2/3)  │   │ Gateway (COMP-2/3)  │            │
│  │ /rest/credit │   │ /soap/creditrisk │   │ /creditriskapi      │            │
│  │ (No Auth)    │   │ (WSS4J)          │   │ (WSS4J)             │            │
│  └──────┬───────┘   └────────┬─────────┘   └──────────┬──────────┘            │
│         │                    │                    ┌────▼─────────┐             │
│         │                    │                    │ GwPreProc    │             │
│         │                    │                    │ (COMP-010)   │             │
│         │                    │                    └────┬─────────┘             │
│         ▼                    ▼                         ▼                       │
│  ┌──────────────────────────────────────────────────────────┐                 │
│  │              SHARED PIPELINE (Camel Processors)           │                 │
│  │                                                           │                 │
│  │  ┌─────────────┐   ┌──────────────┐   ┌───────────────┐ │                 │
│  │  │ Validator   │──▶│ ScoringStrat │──▶│ BureauReqBldr │ │                 │
│  │  │ (COMP-005)  │   │ (COMP-006)   │   │ (COMP-007)    │ │                 │
│  │  └─────────────┘   └──────────────┘   └──────┬────────┘ │                 │
│  │                                                │          │                 │
│  │         ┌─────────────────────────────────────▼────┐     │                 │
│  │         │        Bureau CXF Client (external)      │     │                 │
│  │         │        bureauEndpoint (COMP-029)         │     │                 │
│  │         └─────────────────────────────┬────────────┘     │                 │
│  │                                        │                  │                 │
│  │  ┌───────────────┐   ┌──────────────┐ │                  │                 │
│  │  │ RiskScoreCalc │◀──│ BureauResMap │◀┘                  │                 │
│  │  │ (COMP-009)    │   │ (COMP-008)   │                    │                 │
│  │  └──────┬────────┘   └──────────────┘                    │                 │
│  │         │                                                 │                 │
│  │  ┌──────▼────────┐                                       │                 │
│  │  │ RestResBldr   │ (REST route only)                     │                 │
│  │  │ (COMP-011)    │                                       │                 │
│  │  └───────────────┘                                       │                 │
│  └──────────────────────────────────────────────────────────┘                 │
│         │                                                                      │
│         ▼ Wire Tap (async)                                                     │
│  ┌──────────────────┐   ┌──────────────────┐                                  │
│  │ TransactionLogger│──▶│ MongoDB          │                                  │
│  │ (COMP-032)       │   │ (Audit Log)      │                                  │
│  └──────────────────┘   └──────────────────┘                                  │
│                                                                                │
│  ┌──────────────┐  (catch all routes)                                          │
│  │ ErrorProc    │                                                              │
│  │ (COMP-012)   │                                                              │
│  └──────────────┘                                                              │
└────────────────────────────────────────────────────────────────────────────────┘
```

---

## 5. Layered Component View

### 5.1 Presentation Layer

| **Component ID** | **Component Name**        | **Technology**      | **Description**                          |
|------------------|---------------------------|---------------------|------------------------------------------|
| COMP-001         | CreditRiskRestSvc         | CXF JAX-RS          | REST endpoint with Swagger annotations   |
| COMP-011         | RestResponseBuilder       | Camel Processor      | REST response formatting                 |

### 5.2 Business Logic Layer

| **Component ID** | **Component Name**              | **Technology**      | **Description**                          |
|------------------|---------------------------------|---------------------|------------------------------------------|
| COMP-005         | CreditRiskRequestValidator      | Camel Processor      | Input validation (7 rules)               |
| COMP-006         | ScoringStrategyProcessor        | Camel Processor      | Strategy selection                       |
| COMP-009         | RiskScoreCalculator             | Camel Processor      | Core risk assessment                     |
| COMP-013         | ScoringStrategy (Interface)     | Java                 | Strategy contract                        |
| COMP-014         | StandardScoringStrategy         | Java                 | Balanced scoring                         |
| COMP-015         | ConservativeScoringStrategy     | Java                 | Secured product scoring                  |
| COMP-016         | AggressiveScoringStrategy       | Java                 | Unsecured product scoring                |

### 5.3 Data Layer

| **Component ID** | **Component Name**              | **Technology** | **Description**                          |
|------------------|---------------------------------|---------------|------------------------------------------|
| COMP-017         | CreditRiskReqType               | JAXB           | Request model                            |
| COMP-018         | CreditRiskResType               | JAXB           | Response model                           |
| COMP-019         | RequestHeader                   | JAXB           | Request header model                     |
| COMP-020         | ResponseHeader                  | JAXB           | Response header model                    |
| COMP-021         | CreditScoreDetail               | JAXB           | Credit score detail                      |
| COMP-022         | DebtServiceDetail               | JAXB           | Debt service detail                      |
| COMP-023         | EmploymentRiskDetail            | JAXB           | Employment risk detail                   |
| COMP-024         | IncomeVerificationDetail        | JAXB           | Income verification                      |
| COMP-025         | ProvinceType                    | Java Enum      | Province codes                           |
| COMP-026         | RequestChannelType              | Java Enum      | Channel types                            |

### 5.4 Integration Layer

| **Component ID** | **Component Name**              | **Technology**      | **Description**                          |
|------------------|---------------------------------|---------------------|------------------------------------------|
| COMP-002         | CreditRiskSoapSvc               | CXF JAX-WS          | SOAP contract interface                  |
| COMP-003         | CreditRiskSoapSvcImpl           | CXF JAX-WS          | SOAP stub implementation (dead code)     |
| COMP-007         | BureauRequestBuilder            | Camel Processor      | Outbound bureau request                  |
| COMP-008         | BureauResponseMapper            | Camel Processor      | Inbound bureau response                  |
| COMP-010         | GatewayRequestPreProcessor      | Camel Processor      | Gateway pre-processing                   |
| COMP-027         | BureauInquiryRequest            | CXF Generated        | Bureau request model                     |
| COMP-028         | BureauInquiryResponse           | CXF Generated        | Bureau response model                    |
| COMP-029         | BureauScoreService              | CXF Generated        | Bureau service interface                 |
| COMP-030         | BureauSubject                   | CXF Generated        | Bureau subject model                     |
| COMP-031         | BureauSubscriber                | CXF Generated        | Bureau subscriber model                  |

### 5.5 Cross-Cutting Concerns

| **Component ID** | **Component Name**   | **Concern Type**       | **Description**                          |
|------------------|----------------------|------------------------|------------------------------------------|
| COMP-004         | CreditRiskServiceException | Error Handling    | Custom SOAP fault exception              |
| COMP-012         | ErrorProcessor       | Error Handling          | Centralized error handler                |
| COMP-032         | TransactionLogger    | Logging / Audit         | MongoDB transaction logging              |
| COMP-033         | LoggerConstants      | Logging                 | Exchange property key constants           |

---

## 6. Third-Party Libraries & Frameworks

| **Library/Framework**            | **Version**              | **License**    | **Used By**                | **Support Status** | **Migration Risk** | **Purpose**                                |
|----------------------------------|--------------------------|----------------|----------------------------|--------------------|---------------------|--------------------------------------------|
| JBoss Fuse / Apache Karaf BOM    | 6.3.0.redhat-187         | Apache 2.0     | All                        | 🔴 EOL              | 🔴 High             | OSGi runtime container + BOM               |
| Apache Camel                     | 2.17.0.redhat-630187     | Apache 2.0     | All processors             | 🔴 EOL              | 🔴 High             | Integration engine, route DSL              |
| Apache CXF                       | 3.1.5.redhat-630187      | Apache 2.0     | All endpoints              | 🔴 EOL              | 🔴 High             | JAX-RS / JAX-WS exposures                  |
| cxf-rt-ws-security (WSS4J)       | 3.1.5.redhat-630187      | Apache 2.0     | SOAP + GW endpoints        | 🔴 EOL              | 🔴 High             | WS-Security UsernameToken                  |
| Jackson                          | 2.4.3                    | Apache 2.0     | REST endpoint              | 🔴 EOL              | 🟡 Med              | JSON provider for JAX-RS                   |
| MongoDB Java Driver              | 3.4.2                    | Apache 2.0     | COMP-032                   | 🔴 EOL              | 🟡 Med              | Transaction logging to MongoDB             |
| Swagger (io.swagger)             | 1.5.16                   | Apache 2.0     | COMP-001                   | 🔴 EOL              | 🟡 Med              | REST API annotations                       |
| SLF4J + Log4j 1.x               | Bundled via BOM          | MIT/Apache     | All                        | 🔴 EOL (Log4j 1)   | 🟡 Med              | Logging facade + appender                  |
| Apache Commons Lang              | 3.4                      | Apache 2.0     | COMP-005                   | ✅ Supported         | 🟢 Low              | StringUtils null/blank checks              |
| camel-jasypt                     | 2.17.0.redhat-630187     | Apache 2.0     | Property placeholders      | 🔴 EOL              | 🟡 Med              | Encrypted config properties                |

---

## 7. Deprecated / Dead Code Components

| **Component ID** | **Component Name**              | **Reason**                                     | **Evidence Type**                 | **Lines Affected** | **Recommendation**     |
|------------------|---------------------------------|------------------------------------------------|-----------------------------------|--------------------|------------------------|
| COMP-003         | CreditRiskSoapSvcImpl           | Method returns `null`; Camel handles processing| Unreachable method body           | L14–L16            | Remove body or document as Camel-delegated stub |
| COMP-001 (body)  | CreditRiskRestSvc.assessCreditRisk | Method returns `null`; annotations decoration only | Unreachable method body        | L20–L32            | Remove body; keep annotations for CXF wiring |
| COMP-023 (fields)| EmploymentRiskDetail.yearsEmployed, industryCategory | Fields never populated anywhere | Never invoked / never set      | L14, L18           | Remove or populate from request data |
| COMP-019 (field) | RequestHeader.userId            | Field never set in any processor               | Never invoked / never set         | L16                | Confirm business need or remove |
| COMP-009 (field) | RiskScoreCalculator.scoringStrategy | Blueprint-injected but never read in process() | Never invoked in logic           | L21, L173–L176     | Remove unused field or implement fallback |
| COMP-006 (branch)| ScoringStrategyProcessor        | "aggressive" default strategy not handled      | Unreachable branch                | L38–L40            | Add `"aggressive"` branch or confirm intent |

---

## 8. Notes & Observations

| **#** | **Observation**                                                                     | **Severity** | **Recommendation**                       |
|-------|-------------------------------------------------------------------------------------|--------------|------------------------------------------|
| 1     | REST endpoint has **no authentication** — SOAP/Gateway have WSS4J but REST is open | Critical     | Add API key, OAuth, or JWT for REST      |
| 2     | `TransactionLogger` creates a new `MongoClient` per invocation (no connection pool) | Warning      | Use singleton connection pool            |
| 3     | Missing WSDLs: `CreditRiskService.wsdl` and `ExternalBureauService.wsdl` not in repo | Critical   | Locate and add to `src/main/resources/wsdl/` |
| 4     | Blueprint creates new Strategy instances per request (not singleton)                | Info         | Consider singleton beans in blueprint    |
| 5     | `SimpleDateFormat` used as static field (not thread-safe)                           | Warning      | Replace with `DateTimeFormatter` (Java 8+) |
| 6     | Province validation uses enum instead of config property `SUPPORTED_PROVINCE_LIST`  | Info         | Align validation source with config      |

---

## 10. Hardcoded Values per Component

| **Component ID** | **File/Class**                   | **Field/Variable**                          | **Hardcoded Value**                              | **Line(s)** | **Expected Behavior**                           | **Recommendation**                              |
|------------------|----------------------------------|---------------------------------------------|--------------------------------------------------|-------------|--------------------------------------------------|-------------------------------------------------|
| COMP-005         | CreditRiskRequestValidator       | SIN_PATTERN                                 | `"^\\d{3}-?\\d{3}-?\\d{3}$"`                    | L28         | Acceptable as constant                           | Keep; document CR-005 rule                      |
| COMP-005         | CreditRiskRequestValidator       | DOB_PATTERN                                 | `"^\\d{4}-\\d{2}-\\d{2}$"`                       | L29         | Acceptable as constant                           | Keep                                             |
| COMP-005         | CreditRiskRequestValidator       | POSTAL_CODE_PATTERN                         | `"^[A-Za-z]\\d[A-Za-z]\\s?\\d[A-Za-z]\\d$"`     | L30         | Acceptable as constant                           | Keep; document CR-007 rule                      |
| COMP-006         | ScoringStrategyProcessor         | Product type strings                        | "MORTGAGE", "AUTO_LOAN", "CREDIT_CARD", "LINE_OF_CREDIT" | L41–L48 | Should be data-driven                     | Externalize mapping to config                   |
| COMP-007         | BureauRequestBuilder             | operationNamespace                          | `"http://ws.esb.nexgen.com/bureau/v1"`           | L68         | Changes when bureau WSDL changes                 | Externalize to config                           |
| COMP-008         | BureauResponseMapper             | Score range thresholds                      | 800, 740, 670, 580                               | L58–L63     | Bureau score models change                       | Externalize to config                           |
| COMP-009         | RiskScoreCalculator              | Monthly debt factor                         | 0.03                                             | L104        | Actuarial assumption                             | Externalize                                      |
| COMP-009         | RiskScoreCalculator              | Monthly payment factor                      | 0.006                                            | L115        | Actuarial assumption                             | Externalize                                      |
| COMP-010         | GatewayRequestPreProcessor       | Source system literal                       | `"GATEWAY"`                                      | L27         | Should be configurable                           | Externalize to config                           |
| COMP-010         | GatewayRequestPreProcessor       | Request channel literal                     | `"API"`                                          | L34         | Should be configurable                           | Externalize to config                           |
| COMP-014         | StandardScoringStrategy          | Weights                                     | 40, 30, 20, 10                                   | L14–L17     | Should be configurable per product               | Externalize to scoring config                   |
| COMP-015         | ConservativeScoringStrategy      | Weights                                     | 35, 35, 15, 15                                   | L39–L42     | Should be configurable per product               | Externalize to scoring config                   |
| COMP-016         | AggressiveScoringStrategy        | Weights                                     | 50, 25, 15, 10                                   | L33–L36     | Should be configurable per product               | Externalize to scoring config                   |

---

## 11. Component-to-Endpoint Mapping

| **Endpoint**                             | **HTTP Method** | **Route Component**       | **Processor(s)**                                                              | **Transformer(s)** | **Data Access** | **External Calls**     |
|------------------------------------------|-----------------|---------------------------|-------------------------------------------------------------------------------|---------------------|-----------------|------------------------|
| `/service/rest/creditrisk/assess`        | GET             | in_rest_creditRiskRouter  | COMP-005 → COMP-006 → COMP-007 → COMP-008 → COMP-009 → COMP-011             | COMP-011            | COMP-032 (async)| bureauEndpoint (SOAP)  |
| `/service/soap/creditrisk`               | POST (SOAP)     | in_soap_creditRiskRouter  | COMP-005 → COMP-006 → COMP-007 → COMP-008 → COMP-009                         | —                   | COMP-032 (async)| bureauEndpoint (SOAP)  |
| `/service/creditriskapi`                 | POST (SOAP)     | in_gw_creditRiskRouter    | COMP-010 → COMP-005 → COMP-006 → COMP-007 → COMP-008 → COMP-009             | —                   | COMP-032 (async)| bureauEndpoint (SOAP)  |
| `direct:logTransaction`                  | Internal        | creditRisk_logging_route  | COMP-032                                                                      | —                   | MongoDB         | None                   |

---

> **Document Control:**
> | Version | Date | Author | Changes |
> |---------|------|--------|---------|
> | 0.1 | 31-Mar-2026 | RE Agent | Initial draft — all 33 classes inventoried, 14 blueprint beans mapped, 20 dependencies listed |
