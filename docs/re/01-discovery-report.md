# Discovery Report

---

| **Field**            | **Details**                                          |
|----------------------|------------------------------------------------------|
| **Project Name**     | NexGen ESB Modernisation                             |
| **Application Name** | nexgen-creditrisk-gateway                            |
| **Version**          | 1.0.0-SNAPSHOT                                       |
| **Date**             | 31-Mar-2026                                          |
| **Prepared By**      | Copilot Reverse Engineering Agent                    |
| **Reviewed By**      | _[Pending Client Review]_                            |
| **Status**           | Draft                                                |

---

## 1. Executive Summary

> **Purpose:** This discovery was initiated to perform a full reverse engineering analysis of the `nexgen-creditrisk-gateway` JBoss Fuse service as the first step in a planned modernisation programme. The goal is to capture a complete technical baseline — code structure, integration topology, data landscape, security posture, and migration risk — before any re-platforming work begins.
>
> **Scope:** All source artefacts in the repository are in scope: 38 source files across 33 Java types and 7 packages, the OSGi Blueprint wiring descriptor, the Maven build descriptor, the externalised configuration file, and the Karaf features descriptor. The live runtime environment and any connected external systems (credit bureau, Guidewire) are out of scope for this analysis; findings are based solely on static code review.
>
> **Key Findings:**
> 1. **EOL runtime** — JBoss Fuse 6.3.0.redhat-187 reached end of life in December 2019. Apache Camel 2.17 and CXF 3.1.5 are also well past community end-of-life. Java 8 exits extended support in March 2025 (Oracle) / September 2026 (Azul). All four layers carry critical migration urgency.
> 2. **Solid internal design** — The service uses the Strategy pattern for scoring, well-separated Camel processor pipeline, and clean model/service/processor layering. Business logic is largely self-contained and well-commented, which will assist migration.
> 3. **Zero automated test coverage** — No JUnit tests exist in `src/test/java`. The only test artefact is a sample `app_config.properties`. This is the single biggest risk to a safe migration.
> 4. **Unresolved WSDL references** — `blueprint.xml` references `classpath:wsdl/CreditRiskService.wsdl` and `classpath:wsdl/ExternalBureauService.wsdl`; neither file exists in the repository. The service cannot start without them.
> 5. **Dead service stubs** — `CreditRiskRestSvc.assessCreditRisk()` and `CreditRiskSoapSvcImpl.assessCreditRisk()` both return `null`. Actual processing happens inside the Camel routes, making the JAX-RS/JAX-WS annotations decoration only.
> 6. **Several hardcoded values** — Scoring weights, calculation factors, and namespace URIs are embedded in source code rather than externalised to configuration.
> 7. **Thin configuration** — The `app_config.properties` file exposes 13 runtime properties. The `LDAP_LOGIN` and `SUPPORTED_PROVINCE_LIST` entries are present in config but referenced nowhere in source code; `BUREAU_CONNECTION_TIMEOUT` and `BUREAU_RECEIVE_TIMEOUT` are likewise unused in code.

---

## 2. Application Overview

### 2.1 Application Description

The `nexgen-creditrisk-gateway` is a credit risk assessment mediation service deployed as an OSGi bundle inside an Apache Karaf container (JBoss Fuse 6.3). It receives credit risk assessment requests from three inbound channels — a JAX-RS REST endpoint, a JAX-WS SOAP endpoint, and a Guidewire-style Gateway SOAP endpoint — validates the request, selects a product-appropriate scoring strategy, calls an external credit bureau SOAP web service, maps the bureau response, calculates a composite risk score and recommendation, and returns a structured XML response to the caller. All transactions are asynchronously logged to a MongoDB collection via a Camel Wire Tap.

| **Attribute**           | **Details**                                                              |
|-------------------------|--------------------------------------------------------------------------|
| Application Type        | API / ESB Mediation Service                                              |
| Primary Language        | Java 8                                                                   |
| Framework(s)            | Apache Camel 2.17 (Blueprint DSL), Apache CXF 3.1.5, OSGi Blueprint     |
| Database(s)             | MongoDB 3.4.2 (transaction audit log only; no persistent domain data)   |
| Deployment Platform     | JBoss Fuse 6.3.0 / Apache Karaf (OSGi container), on-premises           |
| Authentication Method   | WS-Security WSS4J UsernameToken (SOAP), JAAS/LDAP (LDAPLogin config)   |

### 2.2 Technology Stack

| **Layer**            | **Technology**                    | **Version**                   | **Support Status**     | **Migration Risk**  | **Notes**                                                              |
|----------------------|-----------------------------------|-------------------------------|------------------------|---------------------|------------------------------------------------------------------------|
| Runtime Container    | JBoss Fuse / Apache Karaf         | 6.3.0.redhat-187              | 🔴 EOL (Dec 2019)      | 🔴 High             | No patches; CVEs accumulate silently                                   |
| Integration Engine   | Apache Camel                      | 2.17.0.redhat-630187          | 🔴 EOL                 | 🔴 High             | Camel 3/4 are not drop-in replacements; DSL migration needed           |
| Web Services         | Apache CXF                        | 3.1.5.redhat-630187           | 🔴 EOL                 | 🔴 High             | JAX-RS / JAX-WS contracts must be re-validated on newer CXF            |
| Language Runtime     | Java                              | 8 (source/target 1.8)         | ⚠️ Nearing EOL         | 🟡 Med              | Oracle extended support ends Mar 2025; migrate to Java 17/21           |
| API Documentation    | Swagger (Springfox / io.swagger)  | 1.5.16                        | 🔴 EOL                 | 🟡 Med              | Replaced by OpenAPI 3 / Swagger 2.x — UI/spec annotations must migrate |
| Database Driver      | MongoDB Java Driver               | 3.4.2                         | 🔴 EOL                 | 🟡 Med              | Current driver is 5.x; breaking changes in async/reactive model        |
| JSON Binding         | Jackson                           | 2.4.3                         | 🔴 EOL                 | 🟡 Med              | Several CVEs in 2.4.x; upgrade to 2.17+ required                       |
| Security             | WSS4J (via CXF)                   | Bundled with CXF 3.1.5        | 🔴 EOL                 | 🔴 High             | Old UsernameToken plain-text; no transport encryption evidenced         |
| Build                | Apache Maven + maven-bundle-plugin | Maven 3.x / bundle 3.2.0     | ✅ Supported            | 🟢 Low              | OSGi packaging must be replaced for non-Karaf targets                  |
| Logging              | SLF4J + Log4j 1.x                 | SLF4J bundled; Log4j 1.x      | 🔴 EOL (Log4j 1 EOL)   | 🟡 Med              | Log4j 1 reached EOL August 2015; migrate to Logback / Log4j 2          |
| Commons              | Apache Commons Lang               | 3.4                           | ✅ Supported            | 🟢 Low              | Minor upgrade to 3.14+ recommended                                      |

### 2.3 Architecture Pattern

The service implements a **Service-Oriented Architecture (SOA) / Enterprise Service Bus (ESB)** pattern, deployed as a single OSGi bundle. Within the bundle the architecture follows a **pipeline (chain-of-responsibility)** pattern: each Camel processor in a route handles one discrete responsibility (validation → strategy selection → bureau call → response mapping → score calculation → response building). The **Strategy** design pattern is used for scoring algorithm selection. There is no inbound message queue; all three routes are synchronous request/reply. The Wire Tap to MongoDB provides asynchronous audit logging without blocking the main flow.

---

## 3. Codebase Analysis

### 3.1 Repository Structure

```
nexgen-creditrisk-gateway/
├── pom.xml                                    # Maven build: OSGi bundle, dependencies, compiler settings
├── docs/
│   └── re/
│       └── templates/                         # RE document templates (10 templates)
└── src/
    ├── main/
    │   ├── java/
    │   │   └── com/nexgen/esb/creditrisk/
    │   │       ├── generated/                 # 5 CXF-generated bureau SOAP client stubs
    │   │       ├── logging/                   # TransactionLogger (MongoDB) + LoggerConstants
    │   │       ├── model/                     # 10 JAXB model types (request, response, enums, headers, details)
    │   │       ├── processor/                 # 8 Camel processors (validation, scoring, bureau, risk, REST, error)
    │   │       ├── scoring/                   # Strategy interface + 3 strategy implementations
    │   │       └── service/                   # JAX-RS endpoint, JAX-WS interface + impl, exception
    │   └── resources/
    │       ├── OSGI-INF/blueprint/
    │       │   └── blueprint.xml              # 14 Spring-Blueprint beans, 4 CXF endpoints, 4 Camel routes
    │       ├── features.xml                   # Karaf feature descriptor
    │       └── log4j.properties               # Log4j 1.x logging configuration
    └── test/
        └── resources/
            └── app_config.properties          # Sample runtime configuration (13 properties)
```

> **Note:** `src/main/resources/wsdl/` is referenced in `blueprint.xml` for both `CreditRiskService.wsdl` and `ExternalBureauService.wsdl` but neither file exists in the repository. This is a **critical gap** — the bundle will fail to deploy without them.

### 3.2 Code Metrics

| **Metric**                    | **Value**                                  |
|-------------------------------|--------------------------------------------|
| Total Lines of Code (LOC)     | 1,714 (Java source only)                   |
| Number of Source Files        | 38 (34 Java + 3 XML/properties + 1 pom)    |
| Number of Modules/Packages    | 7                                          |
| Number of Classes/Types       | 33 top-level (26 classes, 3 interfaces, 2 enums, 2 inner classes) |
| Number of Test Files          | 0 (no test Java sources)                   |
| Test Coverage                 | 0% — no automated tests exist              |
| Cyclomatic Complexity (Est.)  | Low–Medium; highest in `RiskScoreCalculator` (~12) and `ScoringStrategyProcessor` (~8) |

**Class inventory by package:**

| **Package**                                       | **Types** | **Classes**                                                                                                                  |
|---------------------------------------------------|-----------|------------------------------------------------------------------------------------------------------------------------------|
| `com.nexgen.esb.creditrisk.generated`             | 5         | `BureauInquiryRequest`, `BureauInquiryResponse`, `BureauScoreService` (iface), `BureauSubject`, `BureauSubscriber`           |
| `com.nexgen.esb.creditrisk.logging`               | 2         | `LoggerConstants` (iface), `TransactionLogger`                                                                               |
| `com.nexgen.esb.creditrisk.model`                 | 10        | `CreditRiskReqType`, `CreditRiskResType`, `RequestHeader`, `ResponseHeader`, `CreditScoreDetail`, `DebtServiceDetail`, `EmploymentRiskDetail`, `IncomeVerificationDetail`, `ProvinceType` (enum), `RequestChannelType` (enum) |
| `com.nexgen.esb.creditrisk.processor`             | 8         | `BureauRequestBuilder`, `BureauResponseMapper`, `CreditRiskRequestValidator`, `ErrorProcessor`, `GatewayRequestPreProcessor`, `RestResponseBuilder`, `RiskScoreCalculator`, `ScoringStrategyProcessor` |
| `com.nexgen.esb.creditrisk.scoring`               | 4         | `ScoringStrategy` (iface), `StandardScoringStrategy`, `ConservativeScoringStrategy`, `AggressiveScoringStrategy`             |
| `com.nexgen.esb.creditrisk.service`               | 4         | `CreditRiskRestSvc`, `CreditRiskSoapSvc` (iface), `CreditRiskSoapSvcImpl`, `CreditRiskServiceException`                     |
| _(inner classes)_                                 | 2         | `CreditRiskRequestValidator.ValidationException`, `CreditRiskServiceException.DetailException`                               |
| **Total**                                         | **35**    |                                                                                                                              |

### 3.3 Dependency Analysis

| **Dependency**                         | **Version**              | **Purpose**                                           | **Risk Level** |
|----------------------------------------|--------------------------|-------------------------------------------------------|----------------|
| `org.jboss.fuse.bom:jboss-fuse-parent` | 6.3.0.redhat-187         | BOM: pins all Fuse/Camel/CXF transitive versions      | High           |
| `camel-core`                           | 2.17.0.redhat-630187     | Camel exchange model, processor interfaces, routing   | High           |
| `camel-blueprint`                      | 2.17.0.redhat-630187     | OSGi Blueprint DSL for Camel                          | High           |
| `camel-cxf`                            | 2.17.0.redhat-630187     | CXF transport component for Camel routes              | High           |
| `camel-jackson`                        | 2.17.0.redhat-630187     | JSON data format support                              | High           |
| `camel-jasypt`                         | 2.17.0.redhat-630187     | Encrypted property placeholders                       | High           |
| `cxf-rt-frontend-jaxrs`               | 3.1.5.redhat-630187      | JAX-RS REST endpoint exposure                         | High           |
| `cxf-rt-frontend-jaxws`               | 3.1.5.redhat-630187      | JAX-WS SOAP endpoint exposure and client              | High           |
| `cxf-rt-transports-http`              | 3.1.5.redhat-630187      | HTTP transport for CXF                                | High           |
| `cxf-rt-ws-security`                  | 3.1.5.redhat-630187      | WS-Security (WSS4J) for SOAP endpoints                | High           |
| `io.swagger:swagger-jaxrs`            | 1.5.16                   | Swagger 1.x annotations on REST endpoint              | Medium         |
| `jackson-databind`                    | 2.4.3                    | JSON serialisation/deserialisation                    | High           |
| `jackson-jaxrs-json-provider`         | 2.4.3                    | Jackson JAX-RS integration                            | High           |
| `org.mongodb:mongo-java-driver`       | 3.4.2                    | MongoDB transaction logging                           | Medium         |
| `slf4j-api`                           | Bundled via BOM          | Logging facade                                        | Low            |
| `slf4j-log4j12`                       | Bundled via BOM          | SLF4J binding to Log4j 1.x                            | Medium         |
| `log4j:log4j`                         | Bundled via BOM          | Log4j 1.x appenders (EOL Aug 2015)                   | High           |
| `commons-lang3`                       | 3.4                      | `StringUtils` for null/blank checks in validator      | Low            |
| `junit`                               | Bundled via BOM (test)   | Unit test framework (no tests written)                | Low            |
| `camel-test-blueprint`                | Bundled via BOM (test)   | Blueprint container for Camel integration tests       | Low            |

### 3.4 Dead Code & Hardcoded Values Summary

#### 3.4.1 Dead Code Inventory

| **#** | **File/Class**                              | **Method/Block**                      | **Lines**  | **Evidence**                                                                                        | **Recommendation**                                                   |
|-------|---------------------------------------------|---------------------------------------|------------|-----------------------------------------------------------------------------------------------------|----------------------------------------------------------------------|
| 1     | `service/CreditRiskRestSvc.java`            | `assessCreditRisk()`                  | L20–L32    | Method always returns `null`; actual logic lives in the Camel route; method body is never executed  | Remove method body or add delegation comment; JAX-RS annotations are used only for CXF wiring |
| 2     | `service/CreditRiskSoapSvcImpl.java`        | `assessCreditRisk()`                  | L14–L16    | Method always returns `null`; Camel handles all processing; this impl is never invoked at runtime   | Document as Camel-delegated stub; add explicit UnsupportedOperationException or comment |
| 3     | `model/EmploymentRiskDetail.java`           | Fields `yearsEmployed`, `industryCategory` | L14, L18 | Fields declared with getters/setters but never populated anywhere in `RiskScoreCalculator` or other processors | Either populate from request data or remove if not needed for migration |
| 4     | `model/RequestHeader.java`                  | Field `userId`                        | L16        | Field declared with getter/setter but never set in any processor; `GatewayRequestPreProcessor` creates headers without `userId` | Confirm with business whether user ID propagation is required        |
| 5     | `processor/RiskScoreCalculator.java`        | Field `scoringStrategy` (String)      | L21, L173–L176 | Spring-injected via `blueprint.xml` (`{{SCORING_DEFAULT_STRATEGY}}`) but never read in `process()` — the strategy is fetched from exchange property set by `ScoringStrategyProcessor` | Remove the unused field and its getter/setter, or confirm it is intended as a future fallback |
| 6     | `processor/ScoringStrategyProcessor.java`   | Aggressive strategy `"conservative"` branch | L38–L40 | `defaultStrategy` is checked for `"conservative"` only; `"aggressive"` default is never handled — resolves to `StandardScoringStrategy` | Confirm expected behaviour and add an `"aggressive"` branch if needed |
| 7     | Config: `app_config.properties`             | `LDAP_LOGIN=LDAPLogin`                | —          | Property defined in config file but no Java code references this key; `JAAS` login module name is presumably used at Karaf container level but not from application code | Document that this is a Karaf-container-level config, not application-code-level |
| 8     | Config: `app_config.properties`             | `SUPPORTED_PROVINCE_LIST=ON,BC,AB,QC` | —          | Property defined but never read by any class; `CreditRiskRequestValidator` validates against the hardcoded `ProvinceType` enum instead | Either wire this config into `CreditRiskRequestValidator` or remove the property |
| 9     | Config: `app_config.properties`             | `BUREAU_CONNECTION_TIMEOUT`, `BUREAU_RECEIVE_TIMEOUT` | — | Defined in config but no Java code or blueprint XML reads these keys to configure CXF client timeouts | Wire into `bureauEndpoint` CXF configuration or remove from config   |

#### 3.4.2 Hardcoded Values Inventory

| **#** | **File/Class**                              | **Field/Variable**               | **Hardcoded Value**                              | **Lines**    | **Expected Behavior**                               | **Recommendation**                                         |
|-------|---------------------------------------------|----------------------------------|--------------------------------------------------|--------------|-----------------------------------------------------|------------------------------------------------------------|
| 1     | `processor/BureauRequestBuilder.java`       | `TIMESTAMP_FORMAT`               | `"yyyy-MM-dd'T'HH:mm:ss.SSSZ"`                  | L21          | Timestamp format should be consistent across service | Acceptable as constant; confirm UTC vs local time          |
| 2     | `processor/BureauRequestBuilder.java`       | `operationNamespace` header      | `"http://ws.esb.nexgen.com/bureau/v1"`           | L68          | SOAP namespace for bureau operation                  | Externalise to config; changes when bureau WSDL changes    |
| 3     | `processor/CreditRiskRequestValidator.java` | `SIN_PATTERN`                    | `"^\\d{3}-?\\d{3}-?\\d{3}$"`                    | L23          | Regex for Canadian SIN format validation             | Acceptable as constant; document CR-005 rule explicitly    |
| 4     | `processor/CreditRiskRequestValidator.java` | `DOB_PATTERN`                    | `"^\\d{4}-\\d{2}-\\d{2}$"`                       | L24          | Regex for date format                                | Acceptable as constant                                     |
| 5     | `processor/CreditRiskRequestValidator.java` | `POSTAL_CODE_PATTERN`            | `"^[A-Za-z]\\d[A-Za-z]\\s?\\d[A-Za-z]\\d$"`     | L25          | Regex for Canadian postal code                       | Acceptable as constant; document CR-007 rule               |
| 6     | `scoring/StandardScoringStrategy.java`      | `WEIGHT_BUREAU`, `WEIGHT_DTI`, `WEIGHT_UTILIZATION`, `WEIGHT_EMPLOYMENT` | 40, 30, 20, 10 | L14–L17 | Scoring weights should be configurable per product | Externalise to config or a scoring-weights properties file |
| 7     | `scoring/ConservativeScoringStrategy.java`  | Inline weights                   | 35, 35, 15, 15                                   | L39–L42      | Conservative weights should be configurable          | Externalise to config                                      |
| 8     | `scoring/AggressiveScoringStrategy.java`    | Inline weights                   | 50, 25, 15, 10                                   | L33–L36      | Aggressive weights should be configurable            | Externalise to config                                      |
| 9     | `processor/ScoringStrategyProcessor.java`   | Product type strings             | `"MORTGAGE"`, `"AUTO_LOAN"`, `"CREDIT_CARD"`, `"LINE_OF_CREDIT"` | L33–L40 | Product-to-strategy mapping should be data-driven | Externalise product-type-to-strategy mapping to config     |
| 10    | `processor/RiskScoreCalculator.java`        | Monthly debt factor              | `0.03` (3% of total balance)                     | L104         | Minimum payment estimation factor                    | Externalise — actuarial assumption that changes over time  |
| 11    | `processor/RiskScoreCalculator.java`        | Monthly payment factor           | `0.006` (0.6% of requested amount)               | L115         | Payment estimation factor for new credit             | Externalise — same reason                                  |
| 12    | `processor/BureauResponseMapper.java`       | Score range thresholds           | 800, 740, 670, 580                               | L58–L63      | Credit score band boundaries                         | Externalise to config; bureau score models change          |
| 13    | `processor/GatewayRequestPreProcessor.java` | Literal `"GATEWAY"` source system | `"GATEWAY"`                                     | L27          | Source system name for gateway channel               | Externalise to config property                             |
| 14    | `processor/GatewayRequestPreProcessor.java` | Literal `"API"` request channel  | `"API"`                                          | L34          | Default channel when not provided by caller          | Externalise to config property                             |

---

## 4. Integration Points

### 4.1 External Integrations

| **#** | **System/Service**         | **Protocol** | **Direction** | **Auth Mechanism**                                      | **Timeout / Retry Config**                                             | **Fallback Behavior**                                                   | **Description**                                                        |
|-------|----------------------------|--------------|---------------|---------------------------------------------------------|------------------------------------------------------------------------|-------------------------------------------------------------------------|------------------------------------------------------------------------|
| 1     | External Credit Bureau     | SOAP/HTTPS   | Outbound      | None evidenced in code (bearer credentials in WSDL TBD) | `BUREAU_CONNECTION_TIMEOUT=30000 ms`, `BUREAU_RECEIVE_TIMEOUT=30000 ms` (config values, **not wired into CXF client**) | `BureauResponseMapper` sets `BUREAU_ERROR=true`; `RiskScoreCalculator` returns `INDETERMINATE` / `REFER_TO_UNDERWRITER` | CXF client call via `bureauEndpoint`; URL: `{{BUREAU_ENDPOINT_URL}}` (QA: `https://qa.nexgenservices.com:9443/services/bureau/creditcheck`) |
| 2     | Guidewire / Policy Gateway | SOAP/HTTPS   | Inbound       | WS-Security WSS4J UsernameToken (PasswordText)          | N/A (inbound)                                                          | `ErrorProcessor` returns structured XML error response                  | Dedicated `creditRiskGwEndpoint` at `/service/creditriskapi`; pre-processed by `GatewayRequestPreProcessor` which injects source system `"GATEWAY"` |

### 4.2 Internal Integrations

| **#** | **Module/Service**              | **Communication**             | **Description**                                                                                  |
|-------|----------------------------------|-------------------------------|--------------------------------------------------------------------------------------------------|
| 1     | MongoDB (`nexgen_creditrisk`)    | Wire Tap → direct:logTransaction | Asynchronous fire-and-forget transaction logging; decoupled from main processing route; errors are caught and logged to SLF4J only |
| 2     | Camel Exchange Properties        | In-memory exchange properties | Processors communicate via named exchange properties: `VALIDATED_REQUEST`, `SCORING_STRATEGY`, `BUREAU_REQUEST_ID`, `CREDIT_SCORE_DETAIL`, `RISK_RESPONSE`, `BUREAU_ERROR`, `ERROR_CODE`, `ERROR_MESSAGE`, `TRANSACTION_ID`, `APPLICANT_ID`, `PROVINCE`, `PRODUCT_TYPE`, `REQUEST_CHANNEL`, `SOURCE_SYSTEM` |
| 3     | OSGi Blueprint Bean Registry     | Spring-style DI               | All beans wired by Blueprint XML; processors receive config values via property injection from externalised config |

### 4.3 API Documentation (Swagger / OpenAPI)

#### 4.3.1 Swagger Specification Overview

| **Attribute**                | **Details**                                                                    |
|------------------------------|--------------------------------------------------------------------------------|
| Swagger/OpenAPI Version      | Swagger 1.5.16 (io.swagger annotations; not a specification file)             |
| Specification File Location  | No generated or committed specification file found in repository               |
| Specification Format         | N/A — annotations only, runtime-generated spec if Swagger servlet is configured |
| API Title                    | `"Credit Risk Assessment REST API"` (from `@Api` annotation)                  |
| API Version                  | Not declared                                                                   |
| Base Path / Server URL       | `/service/rest/creditrisk`                                                     |
| Auto-Generated               | Yes — Swagger 1.x runtime generation via CXF-Swagger integration if servlet is registered |
| Last Updated                 | Unknown                                                                        |
| Spec Completeness            | Partial — only the REST endpoint is annotated; SOAP endpoints have no equivalent API doc |

#### 4.3.2 API Endpoints Inventory

| **#** | **HTTP Method** | **Endpoint Path**                   | **Operation ID**     | **Summary/Description**                          | **Request Body** | **Response Codes** | **Auth Required** |
|-------|-----------------|-------------------------------------|----------------------|--------------------------------------------------|------------------|--------------------|-------------------|
| 1     | GET             | `/service/rest/creditrisk/assess`   | `assessCreditRisk`   | Assess credit risk for an applicant              | No               | 200 (XML), 400, 500 | No (REST endpoint has no auth interceptor in blueprint) |
| 2     | POST            | `/service/soap/creditrisk`          | `assessCreditRisk`   | SOAP credit risk assessment (internal systems)   | Yes (SOAP envelope) | 200, SOAP fault  | Yes (WS-Security UsernameToken) |
| 3     | POST            | `/service/creditriskapi`            | `assessCreditRisk`   | Gateway SOAP endpoint (Guidewire integration)    | Yes (SOAP envelope) | 200, SOAP fault  | Yes (WS-Security UsernameToken) |

**REST Query Parameters (`GET /service/rest/creditrisk/assess`):**

| **#** | **Parameter**     | **Type**  | **Required** | **Description**                        |
|-------|-------------------|-----------|--------------|----------------------------------------|
| 1     | `applicantId`     | String    | Yes          | Unique applicant identifier            |
| 2     | `firstName`       | String    | Yes          | Applicant first name                   |
| 3     | `lastName`        | String    | Yes          | Applicant last name                    |
| 4     | `dateOfBirth`     | String    | Yes          | Date of birth in `YYYY-MM-DD` format   |
| 5     | `sin`             | String    | Yes          | Social Insurance Number (NNN-NNN-NNN)  |
| 6     | `employmentStatus`| String    | No           | Employment status code                 |
| 7     | `annualIncome`    | Double    | No           | Annual income in CAD                   |
| 8     | `province`        | String    | No           | Canadian province code (2 letters)     |
| 9     | `postalCode`      | String    | No           | Canadian postal code                   |
| 10    | `productType`     | String    | No           | Product being applied for              |
| 11    | `requestedAmount` | Double    | No           | Loan/credit amount requested           |

#### 4.3.3 API Models / Schemas

| **Schema Name**             | **Type** | **Properties Count** | **Used In Endpoints**               | **Notes**                                                   |
|-----------------------------|----------|----------------------|-------------------------------------|-------------------------------------------------------------|
| `CreditRiskReqType`         | Object   | 13                   | SOAP (all 2 SOAP endpoints)         | `requestHeader`, `applicantId`, `firstName`, `lastName`, `dateOfBirth`, `socialInsuranceNumber`, `employmentStatus`, `annualIncome`, `province`, `postalCode`, `requestChannel`, `productType`, `requestedAmount` |
| `CreditRiskResType`         | Object   | 12                   | All 3 endpoints (return type)       | `responseHeader`, `applicantId`, `riskCategory`, `overallScore`, `creditScoreDetail`, `incomeVerification`, `employmentRisk`, `debtService`, `riskFactors`, `recommendation`, `accuracyCode`, `scoringModelVersion` |
| `RequestHeader`             | Object   | 4                    | Embedded in `CreditRiskReqType`     | `transactionId`, `timestamp`, `sourceSystem`, `userId`      |
| `ResponseHeader`            | Object   | 4                    | Embedded in `CreditRiskResType`     | `transactionId`, `timestamp`, `statusCode`, `statusMessage` |
| `CreditScoreDetail`         | Object   | 8                    | Embedded in `CreditRiskResType`     | `bureauScore`, `bureauScoreRange`, `delinquencyCount`, `inquiryCount`, `openAccountCount`, `totalCreditLimit`, `totalBalance`, `utilizationRate` |
| `IncomeVerificationDetail`  | Object   | 5                    | Embedded in `CreditRiskResType`     | `verificationStatus`, `reportedIncome`, `verifiedIncome`, `incomeSource`, `debtToIncomeRatio` |
| `EmploymentRiskDetail`      | Object   | 4                    | Embedded in `CreditRiskResType`     | `employmentType`, `yearsEmployed`, `industryCategory`, `riskLevel` |
| `DebtServiceDetail`         | Object   | 5                    | Embedded in `CreditRiskResType`     | `totalMonthlyDebt`, `totalMonthlyIncome`, `debtServiceRatio`, `totalDebtServiceRatio`, `affordabilityRating` |
| `ProvinceType`              | Enum     | 13 values            | `CreditRiskReqType.province`        | ON, QC, BC, AB, MB, SK, NS, NB, NL, PE, NT, YT, NU        |
| `RequestChannelType`        | Enum     | 7 values             | `CreditRiskReqType.requestChannel`  | ONLINE, MOBILE, BRANCH, BROKER, CALL_CENTER, API, BATCH     |
| `BureauInquiryRequest`      | Object   | 5                    | Outbound bureau SOAP call           | `requestId`, `timestamp`, `subscriber`, `subject`, `productType` |
| `BureauInquiryResponse`     | Object   | 10                   | Inbound bureau SOAP response        | `requestId`, `responseId`, `creditScore`, `delinquencyCount`, `inquiryCount`, `openTradelineCount`, `totalCreditLimit`, `totalBalance`, `errorCode`, `errorMessage` |

#### 4.3.4 API Security Definitions

| **Security Scheme**       | **Type**        | **Details**                                                                                                    |
|---------------------------|-----------------|----------------------------------------------------------------------------------------------------------------|
| WS-Security UsernameToken | WS-Security     | Applied to SOAP (`/service/soap/creditrisk`) and Gateway (`/service/creditriskapi`) endpoints via WSS4J inbound interceptor; `action=UsernameToken`, `passwordType=PasswordText` (cleartext — risk) |
| REST (no auth)            | None            | The JAX-RS REST endpoint (`/service/rest/creditrisk`) has no authentication interceptor configured in blueprint |
| JAAS/LDAP                 | JAAS            | `LDAP_LOGIN=LDAPLogin` references a Karaf JAAS login module; used for LDAP-backed credential validation — wired at container level, not application level |

#### 4.3.5 API Tags / Groupings

| **Tag Name**                         | **Description**                         | **Endpoints Count** |
|--------------------------------------|-----------------------------------------|---------------------|
| `Credit Risk Assessment REST API`    | All REST credit risk operations         | 1                   |
| _(SOAP — no Swagger tag)_            | SOAP endpoints not tagged               | 2                   |

#### 4.3.6 Swagger/OpenAPI Gaps & Observations

| **#** | **Observation**                                                                           | **Severity** | **Recommendation**                                                        |
|-------|-------------------------------------------------------------------------------------------|--------------|---------------------------------------------------------------------------|
| 1     | No committed Swagger/OpenAPI specification file; spec would be generated at runtime only  | High         | Generate and commit an OpenAPI 3.0 spec during migration; version-control it |
| 2     | SOAP endpoints have no API documentation equivalent (no WSDL in repo)                    | High         | Recover or regenerate WSDL files; document in OpenAPI 3.0 with appropriate notes |
| 3     | REST endpoint has no security definition despite requiring matching auth in practice       | High         | Add `securitySchemes` to the OpenAPI spec to reflect actual auth requirements |
| 4     | `@ApiParam` annotations describe fields but no `@ApiResponse` annotations exist           | Medium       | Add response schema documentation to all endpoint methods                 |
| 5     | Swagger 1.5.16 is end-of-life; incompatible with OpenAPI 3.0 tooling                     | High         | Migrate to OpenAPI 3 annotations (e.g., MicroProfile OpenAPI or SpringDoc) |

---

## 5. Data Landscape

### 5.1 Data Stores

| **Data Store**             | **Type** | **Size**  | **Collections**         | **Notes**                                                                                |
|----------------------------|----------|-----------|-------------------------|------------------------------------------------------------------------------------------|
| MongoDB (`nexgen_creditrisk`) | NoSQL (Document) | Unknown  | 1 (`transactions`)  | Write-only transaction audit log; no queries from application code; direct `MongoClient` instantiated per transaction (no connection pooling) |

**MongoDB Transaction Document Schema (inferred from `TransactionLogger.java`):**

| **Field**          | **Type**  | **Source**                                     | **Notes**                              |
|--------------------|-----------|------------------------------------------------|----------------------------------------|
| `transactionId`    | String    | `LoggerConstants.LOG_TRANSACTION_ID` (exchange property) | UUID; set in `RequestHeader` or auto-generated |
| `applicantId`      | String    | `LoggerConstants.LOG_APPLICANT_ID`             | From request                           |
| `province`         | String    | `LoggerConstants.LOG_PROVINCE`                 | 2-letter province code                 |
| `productType`      | String    | `LoggerConstants.LOG_PRODUCT_TYPE`             | Product type string                    |
| `requestChannel`   | String    | `LoggerConstants.LOG_REQUEST_CHANNEL`          | Channel enum value                     |
| `sourceSystem`     | String    | `LoggerConstants.LOG_SOURCE_SYSTEM`            | Originating system                     |
| `timestamp`        | String    | Generated at log time                          | ISO-8601 format with milliseconds      |
| `riskCategory`     | String    | From `RISK_RESPONSE` exchange property         | EXCELLENT / GOOD / FAIR / POOR / VERY_POOR / INDETERMINATE / ERROR |
| `overallScore`     | Integer   | From `RISK_RESPONSE`                           | 0–100                                  |
| `recommendation`   | String    | From `RISK_RESPONSE`                           | APPROVE / APPROVE_WITH_CONDITIONS / REFER_TO_UNDERWRITER / DECLINE |
| `status`           | String    | Derived: `"success"` or `"error"`              | Presence of `RISK_RESPONSE` determines this |
| `errorCode`        | String    | `ERROR_CODE` exchange property (error path)    | CR-001 through CR-500 range            |
| `errorMessage`     | String    | `ERROR_MESSAGE` exchange property (error path) | Human-readable error text              |

### 5.2 Data Flows Summary

The service processes synchronous request/reply flows with an asynchronous audit branch:

1. **Inbound** — REST/SOAP/Gateway endpoint receives request (query params for REST, XML body for SOAP)
2. **Validation** — `CreditRiskRequestValidator` validates 7 business rules (CR-001 → CR-007); exchanges `VALIDATED_REQUEST` property
3. **Strategy Selection** — `ScoringStrategyProcessor` inspects product type; selects Standard / Conservative / Aggressive strategy; stores in `SCORING_STRATEGY` exchange property
4. **Bureau Enrichment** — `BureauRequestBuilder` maps to `BureauInquiryRequest`; Camel CXF sends to external bureau; `BureauResponseMapper` maps response to `CreditScoreDetail`
5. **Risk Calculation** — `RiskScoreCalculator` applies strategy; builds `CreditRiskResType` with all sub-details; stores in `RISK_RESPONSE` exchange property
6. **Response Building** — `RestResponseBuilder` (REST route only) sets HTTP headers; body is returned to caller
7. **Audit Log (async)** — Wire Tap forks the exchange to `direct:logTransaction`; `TransactionLogger` writes a document to MongoDB `transactions` collection; errors are silently swallowed

_Detailed Data Flow Diagrams: see Doc-06 (to be produced)._

---

## 6. Security Assessment

| **Area**                        | **Current State**                                                                                                     | **Risk**  |
|---------------------------------|-----------------------------------------------------------------------------------------------------------------------|-----------|
| Authentication (SOAP)           | WS-Security WSS4J `UsernameToken` with `passwordType=PasswordText` — password transmitted in cleartext in SOAP header | High      |
| Authentication (REST)           | No authentication mechanism on the JAX-RS REST endpoint (`/service/rest/creditrisk`); open to unauthenticated access  | High      |
| Authentication (LDAP)           | JAAS `LDAPLogin` module referenced in config; wired at Karaf container level — application code does not validate tokens | Medium  |
| Authorization                   | No role-based access control evidenced in code; all authenticated requests receive the same processing                | Medium    |
| Data Encryption (At Rest)       | No encryption of MongoDB documents; SIN and applicant PII stored in plaintext in `transactions` collection            | High      |
| Data Encryption (In Transit)    | SOAP endpoints use HTTPS (bureau QA URL is `https://`); no TLS client certificate evidenced; REST transport security not configured | High |
| Input Validation                | 7 business rules (CR-001 → CR-007) cover required fields, SIN format, DOB format, province code, postal code; no SQL/NoSQL injection risk (no dynamic queries); no XSS risk (XML-only) | Low |
| Secrets Management              | `BUREAU_SUBSCRIBER_CODE`, `BUREAU_SUBSCRIBER_NAME`, and SOAP credentials are in plaintext config; `camel-jasypt` is a dependency but no encrypted properties (`ENC(...)`) are evidenced | Medium |
| Known Dependency Vulnerabilities | Jackson 2.4.3 has multiple published CVEs (deserialization); Log4j 1.x has multiple CVEs; JBoss Fuse 6.3 accumulates unfixed CVEs since Dec 2019 | High |
| SIN PII Handling                | Social Insurance Number is passed through the processing chain, included in the bureau request, and may be logged at DEBUG level | High |

---

## 7. Environment & Infrastructure

| **Environment** | **URL/Endpoint**                                                  | **Purpose**            | **Notes**                                                         |
|-----------------|-------------------------------------------------------------------|------------------------|-------------------------------------------------------------------|
| Development     | Karaf console, local deploy                                       | Developer testing      | No CI/CD pipeline evidenced in the repository                     |
| QA              | `https://qa.nexgenservices.com:9443/services/bureau/creditcheck`  | Bureau QA endpoint     | Hardcoded in `test/resources/app_config.properties`               |
| Production      | Not documented in codebase                                        | Live environment        | Externalised via `${NEXGEN_CONFIG_PATH}/creditrisk/app_config.properties` |

**Karaf/OSGi Container Configuration:**

| **Item**                    | **Value**                                             |
|-----------------------------|-------------------------------------------------------|
| Container                   | Apache Karaf (bundled in JBoss Fuse 6.3)              |
| Bundle Symbolic Name        | `nexgen-creditrisk-gateway`                           |
| Bundle Name                 | `NexGen Credit Risk Gateway [nexgen-creditrisk-gateway]` |
| Config Path                 | `${NEXGEN_CONFIG_PATH}/creditrisk/app_config.properties` |
| `ignoreMissingLocation`     | `true` — service starts even if config file is absent (uses defaults/nulls) |
| Features Descriptor         | `src/main/resources/features.xml`                     |
| Supported Province Codes    | ON, QC, BC, AB, MB, SK, NS, NB, NL, PE, NT, YT, NU (from `ProvinceType` enum — all 13 Canadian provinces/territories) |
| Note on provinces in config | `SUPPORTED_PROVINCE_LIST=ON,BC,AB,QC` in test config contradicts the full 13-province enum — actual runtime restriction is code-driven |

**Externalised Configuration Properties (`app_config.properties`):**

| **#** | **Property**                  | **Default (test)**                                           | **Used By**                        |
|-------|-------------------------------|--------------------------------------------------------------|------------------------------------|
| 1     | `BUREAU_ENDPOINT_URL`         | `https://qa.nexgenservices.com:9443/services/bureau/creditcheck` | `bureauEndpoint` (blueprint)   |
| 2     | `BUREAU_REQUEST_ID_PREFIX`    | `dev.nexgen.com-`                                            | `bureauRequestBuilder` (blueprint) |
| 3     | `BUREAU_SUBSCRIBER_CODE`      | `NEXGEN-001`                                                 | `bureauRequestBuilder` (blueprint) |
| 4     | `BUREAU_SUBSCRIBER_NAME`      | `NexGen Financial`                                           | `bureauRequestBuilder` (blueprint) |
| 5     | `BUREAU_CONNECTION_TIMEOUT`   | `30000`                                                      | **Unused in code** — not wired to CXF client |
| 6     | `BUREAU_RECEIVE_TIMEOUT`      | `30000`                                                      | **Unused in code** — not wired to CXF client |
| 7     | `SCORING_DEFAULT_STRATEGY`    | `standard`                                                   | `scoringStrategyProcessor`, `riskScoreCalculator` (blueprint) |
| 8     | `MONGO_HOST`                  | `localhost`                                                  | `transactionLogger` (blueprint)    |
| 9     | `MONGO_PORT`                  | `27017`                                                      | `transactionLogger` (blueprint)    |
| 10    | `MONGO_DB`                    | `nexgen_creditrisk`                                          | `transactionLogger` (blueprint)    |
| 11    | `MONGO_COLLECTION`            | `transactions`                                               | `transactionLogger` (blueprint)    |
| 12    | `LDAP_LOGIN`                  | `LDAPLogin`                                                  | **Unused in code** — Karaf container level |
| 13    | `SUPPORTED_PROVINCE_LIST`     | `ON,BC,AB,QC`                                                | **Unused in code** — validator uses enum |

---

## 8. Key Risks & Challenges

| **#** | **Risk/Challenge**                                                                          | **Impact** | **Mitigation**                                                                                              |
|-------|---------------------------------------------------------------------------------------------|------------|-------------------------------------------------------------------------------------------------------------|
| 1     | JBoss Fuse 6.3 / Camel 2.17 EOL — no security patches since December 2019                  | High       | Migration to Spring Boot + Camel 4 (or Quarkus/Camel) is the primary driver; treat as a compliance issue    |
| 2     | Java 8 — approaching end of free extended support                                           | High       | Migrate to Java 17 (LTS) as the baseline for the target platform                                            |
| 3     | Zero automated test coverage — no safety net for migration                                  | High       | Write unit tests for all 8 processors, 3 scoring strategies, and validators **before** any migration begins |
| 4     | Missing WSDL files — bundle cannot start without `CreditRiskService.wsdl` and `ExternalBureauService.wsdl` | Critical | Recover WSDLs from the credit bureau or Guidewire; regenerate if unavailable; add to repository immediately |
| 5     | WS-Security PasswordText — password in cleartext in SOAP header                             | High       | Upgrade to PasswordDigest or replace with OAuth 2.0 / mTLS in the target architecture                      |
| 6     | REST endpoint has no authentication                                                         | High       | Add authentication (API key, OAuth 2.0) to the REST endpoint before production exposure                     |
| 7     | SIN (PII) logged to MongoDB in plaintext                                                    | High       | Mask or tokenise SIN before logging; review compliance with PIPEDA / FINTRAC requirements                   |
| 8     | Jackson 2.4.3 — multiple deserialization CVEs                                               | High       | Upgrade to Jackson 2.17.x as an immediate patch before migration                                            |
| 9     | Tight OSGi/Blueprint coupling — all wiring is Blueprint-specific                            | Medium     | Re-platform wiring to Spring Boot `@Configuration` or Quarkus CDI during migration                          |
| 10    | Dead service method stubs returning `null` — misleading Camel/CXF contract                 | Medium     | Document intent clearly; add `UnsupportedOperationException` or delegation logic; ensures no confusion for migration team |
| 11    | `TransactionLogger` creates a new `MongoClient` per transaction — no connection pooling    | Medium     | Refactor to use a shared `MongoClient` bean with a connection pool (already a bean in blueprint — pass it in) |
| 12    | Scoring weights and calculation factors are hardcoded — cannot be adjusted without a code change and redeployment | Medium | Externalise to a properties file or a rules engine                                             |
| 13    | Log4j 1.x EOL (August 2015) with known CVEs                                                | Medium     | Migrate to Logback or Log4j 2 as part of runtime upgrade                                                    |
| 14    | `SimpleDateFormat` is not thread-safe — declared as `static final` in `BureauRequestBuilder` and `RiskScoreCalculator` | Low | Replace with `DateTimeFormatter` (Java 8+ thread-safe) or synchronise access                              |

---

## 9. Recommendations

| **#** | **Recommendation**                                                                                   | **Priority** | **Rationale**                                                                                                    |
|-------|------------------------------------------------------------------------------------------------------|--------------|------------------------------------------------------------------------------------------------------------------|
| 1     | **Recover and commit missing WSDL files** (`CreditRiskService.wsdl`, `ExternalBureauService.wsdl`)  | High         | The bundle cannot deploy without them; they are the contractual interface to the bureau and gateway              |
| 2     | **Write unit tests for all processors and scoring strategies before migration**                      | High         | 0% test coverage is the biggest migration risk; tests provide a regression safety net                            |
| 3     | **Migrate to Spring Boot + Apache Camel 4 (or Quarkus + Camel)**                                    | High         | Eliminates Fuse/Karaf/OSGi EOL dependency; aligns with modern cloud deployment targets                           |
| 4     | **Upgrade Java runtime to Java 17 LTS**                                                              | High         | Required for modern frameworks; resolves Java 8 EOL risk                                                         |
| 5     | **Upgrade Jackson to 2.17.x immediately** (short-term patch)                                         | High         | Multiple deserialization CVEs in 2.4.3; upgrade is backward-compatible                                           |
| 6     | **Add authentication to the REST endpoint**                                                          | High         | Currently unauthenticated; PII is returned in responses                                                          |
| 7     | **Replace WS-Security PasswordText with PasswordDigest or OAuth 2.0**                                | High         | Cleartext passwords in SOAP headers are a serious security risk                                                  |
| 8     | **Mask or tokenise SIN before MongoDB logging**                                                      | High         | PII in audit logs violates data minimisation principles under PIPEDA                                             |
| 9     | **Wire CXF client timeouts** (`BUREAU_CONNECTION_TIMEOUT`, `BUREAU_RECEIVE_TIMEOUT`) to the `bureauEndpoint` | Medium | Currently these config values have no effect; the bureau call may hang indefinitely                              |
| 10    | **Externalise scoring weights and calculation factors**                                              | Medium       | Business will need to adjust scoring thresholds without code deployments                                         |
| 11    | **Replace `SimpleDateFormat` static fields with `DateTimeFormatter`**                                | Medium       | Concurrency safety; `SimpleDateFormat` is not thread-safe as a static field                                      |
| 12    | **Refactor `TransactionLogger` to use a shared `MongoClient` bean**                                  | Medium       | Creating a new client per transaction is expensive and does not benefit from connection pooling                   |
| 13    | **Replace Log4j 1.x with Logback or Log4j 2**                                                        | Medium       | Log4j 1 EOL since 2015; known CVEs                                                                               |
| 14    | **Migrate Swagger 1.x annotations to OpenAPI 3.0**                                                   | Medium       | Required for modern API gateway tooling; Swagger 1.x is not supported by current toolchains                     |
| 15    | **Populate or remove `EmploymentRiskDetail.yearsEmployed` and `industryCategory`**                   | Low          | Fields exist in the model but are never populated; they will appear as `null` in all responses                   |

---

## 10. Migration Readiness Assessment

### 10.1 Readiness Scorecard

| **Dimension**                  | **Score (1–5)** | **Assessment**                                                                                                  |
|--------------------------------|-----------------|-----------------------------------------------------------------------------------------------------------------|
| Technology Currency            | 1               | JBoss Fuse 6.3 (EOL 2019), Camel 2.17 (EOL), CXF 3.1.5 (EOL), Java 8 (near EOL), Jackson 2.4.3 (CVEs), Log4j 1.x (EOL) — all core layers are past end of life |
| Code Quality & Maintainability | 3               | Clean processor pipeline, good Strategy pattern usage, meaningful class and method names; offset by null-returning stubs, dead fields, and non-thread-safe statics |
| Test Coverage                  | 1               | Zero automated tests; no integration test harness; the only test artefact is a sample config file              |
| Dependency Health              | 1               | Nearly every dependency is EOL or has known CVEs; no lock on dependency versions beyond the EOL Fuse BOM       |
| Integration Complexity         | 3               | Three inbound channels (REST, SOAP, GW), one outbound SOAP client, one async MongoDB logger; moderate; outbound contract (WSDL) is missing |
| Data Migration Complexity      | 4               | MongoDB audit log only — no domain data to migrate; schema is simple and write-only; minimal risk              |
| Documentation Completeness     | 2               | No external documentation in repo beyond Javadoc comments; missing WSDLs; no Swagger spec file; no architecture diagram |
| Business Logic Clarity         | 3               | Scoring rules and validation rules are well-commented in code; however scoring weights are hardcoded and product-strategy mapping is implicit |
| **Overall Readiness Score**    | **2.25**        | **High migration risk; major groundwork (tests, WSDL recovery, dependency upgrades, auth hardening) required before migration can proceed safely** |

> **Scoring Guide:** 1 = Critical risk, not ready | 2 = High risk, major work needed | 3 = Moderate, manageable with effort | 4 = Good, minor adjustments needed | 5 = Excellent, ready for migration

### 10.2 Migration Approach Recommendation

| **Approach**            | **Suitability** | **Rationale**                                                                                                                     |
|-------------------------|-----------------|-----------------------------------------------------------------------------------------------------------------------------------|
| Lift & Shift            | ❌              | Not viable — target platforms do not support Karaf/OSGi; Fuse 6.3 is EOL; no container image path exists                         |
| Re-platform             | ✅              | **Recommended** — the business logic (processors, scoring strategies, model classes) is clean and reusable; re-platform by replacing Fuse/Camel/CXF with Spring Boot + Camel 4 + Spring WS; no logic rewrite needed for core rules |
| Re-architect / Re-write | ⚠️              | Could be considered if the target architecture moves to microservices / event-driven; the service is small enough to rewrite, but re-platforming preserves existing business logic with lower risk |
| Hybrid                  | ⚠️              | Possible as a transition strategy — deploy the re-platformed service alongside the old one with traffic shadowing for validation |

### 10.3 Critical Blockers for Migration

| **#** | **Blocker Description**                                                                        | **Category** | **Severity** | **Resolution Required Before Migration** |
|-------|-----------------------------------------------------------------------------------------------|--------------|--------------|------------------------------------------|
| 1     | Missing WSDL files — bundle cannot deploy; integration contracts unverifiable                  | Technical    | Critical     | Yes                                      |
| 2     | Zero automated test coverage — no regression baseline exists                                   | Quality      | Critical     | Yes                                      |
| 3     | WS-Security PasswordText — cleartext credentials in SOAP headers                              | Security     | High         | Yes                                      |
| 4     | Unauthenticated REST endpoint exposing PII                                                    | Security     | High         | Yes                                      |
| 5     | SIN (PII) in MongoDB audit log without masking                                                | Compliance   | High         | Yes                                      |
| 6     | Jackson 2.4.3 deserialization CVEs                                                            | Security     | High         | Yes                                      |
| 7     | CXF client timeouts not wired — bureau call can hang indefinitely                             | Functional   | Medium       | Yes                                      |
| 8     | `TransactionLogger` creates new `MongoClient` per invocation — will exhaust connections under load | Technical | Medium      | Yes (before production load)             |

---

## 11. API Gateway Integration Considerations

### 11.1 Current API Routing

| **Attribute**                | **Details**                                                                       |
|------------------------------|-----------------------------------------------------------------------------------|
| Current Routing Mechanism    | Apache Camel Blueprint route DSL inside JBoss Fuse/Karaf OSGi container           |
| Base URL Pattern             | `/service/rest/creditrisk` (REST), `/service/soap/creditrisk` (SOAP), `/service/creditriskapi` (GW SOAP) |
| Endpoint Count               | 3 inbound endpoints; 1 outbound SOAP client                                       |
| Authentication at Entry      | WSS4J UsernameToken on SOAP/GW endpoints; REST endpoint has no authentication     |
| Rate Limiting                | None — no rate limiting mechanism evidenced in code or blueprint                  |
| Request/Response Logging     | Wire Tap to MongoDB for transaction audit; application-level SLF4J/Log4j for debug/error |

### 11.2 Target API Gateway Requirements

| **Requirement**              | **Details**                                                             | **Status**  |
|------------------------------|-------------------------------------------------------------------------|-------------|
| Gateway Platform             | TBD (AWS API Gateway, Kong, or Azure API Management are candidates)     | Pending     |
| Authentication Offloading    | REST endpoint auth must be added; SOAP auth could be offloaded to gateway | Pending   |
| Rate Limiting / Throttling   | Per-client rate limits should be defined; currently unlimited            | Pending     |
| Request Transformation       | REST query params → SOAP/internal model mapping (currently Camel-handled) | Pending   |
| Response Transformation      | XML response could be transformed to JSON at gateway level for REST consumers | Pending |
| Versioning Strategy          | No versioning currently; recommend URI versioning (`/v1/`) for target   | Pending     |
| CORS Policy                  | Not applicable in current on-premises SOAP context; review for REST     | Pending     |

### 11.3 Open Questions for API Gateway Discussion

| **#** | **Question**                                                                                        | **Directed To**   | **Status** | **Resolution** |
|-------|-----------------------------------------------------------------------------------------------------|-------------------|------------|----------------|
| 1     | Will the API Gateway handle WS-Security authentication or will the migrated service retain it?       | Architecture team | 🟡 Open    |                |
| 2     | Will the REST endpoint be replaced by a proper REST API with JSON bodies, or will XML query-param approach be retained? | API team      | 🟡 Open    |                |
| 3     | What is the target URL pattern for the migrated service (URI versioning, subdomain, etc.)?          | Architecture team | 🟡 Open    |                |
| 4     | Is the Guidewire Gateway SOAP endpoint still required, or will Guidewire integration be modernised separately? | Guidewire integration team | 🟡 Open |         |
| 5     | Will MongoDB transaction logging be retained as-is or replaced with a structured event log (e.g., Kafka, CloudWatch)? | Architecture team | 🟡 Open |              |
| 6     | Is the `SUPPORTED_PROVINCE_LIST` config (ON, BC, AB, QC) the actual production-allowed list, or should all 13 provinces in the enum be allowed? | Business team | 🟡 Open | |

---

## 12. Client Feedback & Additional Findings

_This section is reserved for observations and feedback raised during client review sessions. No review has been conducted at the time of this initial draft._

### 12.1 Client-Raised Observations

| **#** | **Observation**              | **Raised By** | **Date Raised** | **Category** | **Impact** | **Action Required**           | **Status** |
|-------|------------------------------|---------------|-----------------|--------------|------------|-------------------------------|------------|
| 1     | _Pending client review_      | —             | —               | —            | —          | Schedule discovery walkthrough | Open       |

### 12.2 Validated vs. Unvalidated Findings

| **Finding from Discovery**                                   | **Client Validation Status**  | **Client Comments** |
|--------------------------------------------------------------|-------------------------------|---------------------|
| Missing WSDL files in repository                             | 🟡 Pending validation         |                     |
| REST endpoint has no authentication                          | 🟡 Pending validation         |                     |
| `SUPPORTED_PROVINCE_LIST` (4 provinces) vs. enum (13)        | 🟡 Pending validation         |                     |
| CXF client timeouts defined in config but not wired to code  | 🟡 Pending validation         |                     |
| SIN stored in MongoDB plaintext                              | 🟡 Pending validation         |                     |

---

## 13. Appendices

### Appendix A: Glossary

| **Term**            | **Definition**                                                                                              |
|---------------------|-------------------------------------------------------------------------------------------------------------|
| OSGi                | Open Services Gateway initiative — a component framework for Java applications used by Apache Karaf/JBoss Fuse |
| Blueprint XML       | OSGi Blueprint specification — declarative dependency injection for OSGi bundles (analogous to Spring XML)  |
| Camel               | Apache Camel — integration framework implementing Enterprise Integration Patterns                            |
| CXF                 | Apache CXF — web services framework for JAX-RS and JAX-WS endpoints                                        |
| Wire Tap            | Camel EIP — sends a copy of the message to a secondary endpoint without interrupting the main flow          |
| WSS4J               | Apache WSS4J — WS-Security implementation used by CXF for SOAP message security                            |
| DTI                 | Debt-to-Income ratio — monthly debt obligations divided by monthly gross income                             |
| TDS                 | Total Debt Service ratio — all debt payments including new requested payment vs. income                     |
| SIN                 | Social Insurance Number — Canadian government-issued personal identifier (equivalent to SSN in USA)         |
| EOL                 | End of Life — a vendor has stopped providing security patches and support for the product                   |
| BOM                 | Bill of Materials — a Maven POM that defines dependency version alignment across a project suite            |
| JAAS                | Java Authentication and Authorisation Service — Java standard API for authentication                        |
| LDAP                | Lightweight Directory Access Protocol — directory service used for credential validation                    |
| PIPEDA              | Personal Information Protection and Electronic Documents Act — Canadian federal privacy law                 |
| Bureau              | Credit Bureau — external service (e.g., Equifax, TransUnion) that provides credit history data             |
| Strategy Pattern    | GoF design pattern — defines a family of algorithms and makes them interchangeable at runtime               |

### Appendix B: References

| **#** | **Document/Resource**                              | **Location/Link**                                                    |
|-------|----------------------------------------------------|----------------------------------------------------------------------|
| 1     | JBoss Fuse 6.3 Release Notes                       | https://access.redhat.com/documentation/en-us/red_hat_fuse/6.3      |
| 2     | Apache Camel 2.17 Documentation                    | https://camel.apache.org/camel-2x/camel-217x/index.html             |
| 3     | Apache CXF 3.1.x Documentation                    | https://cxf.apache.org/docs/index.html                               |
| 4     | OSGi Blueprint Specification                       | https://docs.osgi.org/specification/osgi.cmpn/7.0.0/service.blueprint.html |
| 5     | OWASP WS-Security Cheat Sheet                     | https://cheatsheetseries.owasp.org/cheatsheets/Web_Service_Security_Cheat_Sheet.html |
| 6     | PIPEDA — Office of the Privacy Commissioner       | https://www.priv.gc.ca/en/privacy-topics/privacy-laws-in-canada/the-personal-information-protection-and-electronic-documents-act-pipeda/ |
| 7     | Jackson CVE List                                   | https://github.com/FasterXML/jackson-databind/blob/master/release-notes/GHSA-list.md |
| 8     | Maven Bundle Plugin Documentation                  | https://felix.apache.org/documentation/subprojects/apache-felix-maven-bundle-plugin-bnd.html |
| 9     | `pom.xml`                                         | `nexgen-creditrisk-gateway/pom.xml`                                  |
| 10    | `blueprint.xml`                                   | `nexgen-creditrisk-gateway/src/main/resources/OSGI-INF/blueprint/blueprint.xml` |
| 11    | `app_config.properties` (test/sample)             | `nexgen-creditrisk-gateway/src/test/resources/app_config.properties` |

### Appendix C: Cross-Reference to Other RE Artifacts

| **Artifact**                    | **Document #** | **Relationship to Discovery Report**                                               |
|---------------------------------|----------------|------------------------------------------------------------------------------------|
| Component Catalog               | Doc-02         | Detailed breakdown of all 33 components summarised in Section 3                    |
| Sequence Diagrams               | Doc-03         | Runtime message flows for the 4 Camel routes described in Sections 4 and 5.2      |
| Business Rules Catalog          | Doc-04         | Full specification of the 7 validation rules (CR-001→CR-007) and scoring rules     |
| Data Dictionary                 | Doc-05         | Detailed field-level definitions for all 12 model types in Section 5               |
| Data Flow Diagram               | Doc-06         | Visual DFD for the data flow summarised in Section 5.2                             |
| BDD Feature Specs               | Doc-07         | Behavioural specs for credit risk assessment scenarios                             |
| Test Case Inventory             | Doc-08         | Test cases required to establish baseline coverage before migration                |
| Gap Report                      | Doc-09         | Gaps derived from Section 3.4 (dead code, hardcoded values) and Section 8 (risks)  |
| Field-to-Field Mapping          | Doc-10         | Mapping of `CreditRiskReqType` / `CreditRiskResType` fields to target service model |

---

> **Document Control:**
> | Version | Date         | Author                          | Changes                                                                                   |
> |---------|--------------|---------------------------------|-------------------------------------------------------------------------------------------|
> | 0.1     | 31-Mar-2026  | Copilot Reverse Engineering Agent | Initial draft — all 13 sections populated from static codebase analysis                 |
