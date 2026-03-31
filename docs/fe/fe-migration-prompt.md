# Forward Engineering Migration Prompt

## NexGen Credit Risk Gateway — JBoss Fuse → Spring Boot

---

## 1. Migration Overview

| **Field** | **Value** |
|---|---|
| **Source Platform** | JBoss Fuse 6.3 / Camel 2.17 / CXF 3.1.5 / Java 8 / OSGi Bundle |
| **Target Platform** | Spring Boot 3.x / Java 21 / CXF 4.x / Spring Data MongoDB |
| **Deployment Target** | AWS EC2 (runnable JAR) |
| **Approach** | Lift-and-Shift + Refactor — functional equivalence, no gap fixes |
| **Orchestration** | Pure Spring `@Service` chain (Camel removed entirely) |
| **Output Location** | `forward-engineering/` directory at repo root |

---

## 2. Finalized Technology Decisions

### 2.1 Core Stack

| Decision | Selected | Rationale |
|---|---|---|
| Framework | Spring Boot 3.3.x | LTS, Camel-free, mature ecosystem |
| Java | 21 (LTS) | Latest LTS, virtual threads available |
| Build Tool | Maven | Continuity from source project |
| REST | Spring MVC `@RestController` | Native Spring Boot, OpenAPI support |
| SOAP Provider | Apache CXF 4.x Spring Boot Starter | Closest migration from legacy CXF, WSS4J reuse |
| SOAP Bureau Client | **Stubbed** — returns mock data | No real bureau to call |
| Orchestration | Spring `@Service` beans | Replaces all 4 Camel XML DSL routes |
| MongoDB | Spring Data MongoDB (`MongoTemplate`) | Audit logging, preserves schema |
| API Docs | SpringDoc OpenAPI 3 | Active project, OpenAPI 3.0 |

### 2.2 Security

| Decision | Selected |
|---|---|
| REST Auth | Basic Auth (Spring Security 6) |
| SOAP Auth | WS-Security UsernameToken (WSS4J via CXF) |
| PII in Logs | Masked from day one |

### 2.3 Testing & CI/CD

| Decision | Selected |
|---|---|
| Unit Testing | JUnit 5 + Mockito |
| Integration Testing | Spring Boot Test + embedded MongoDB |
| CI/CD | GitHub Actions |
| Docker | Not required (EC2 JAR deployment) |
| Profiles | `dev`, `qa`, `prod` |

### 2.4 Deployment (AWS EC2)

| Decision | Selected |
|---|---|
| Packaging | Executable JAR (`spring-boot-maven-plugin`) |
| Health Check | Spring Boot Actuator (`/actuator/health`) |
| Config | Externalized via env vars + `application-{profile}.yml` |
| Startup | systemd service unit file |

---

## 3. Package Mapping

| Legacy Package | Target Package |
|---|---|
| `com.nexgen.esb.creditrisk.service` | `com.nexgen.sb.creditrisk.service` |
| `com.nexgen.esb.creditrisk.processor` | `com.nexgen.sb.creditrisk.service` (merged into service layer) |
| `com.nexgen.esb.creditrisk.model` | `com.nexgen.sb.creditrisk.model` |
| `com.nexgen.esb.creditrisk.scoring` | `com.nexgen.sb.creditrisk.scoring` |
| `com.nexgen.esb.creditrisk.logging` | `com.nexgen.sb.creditrisk.logging` |
| `com.nexgen.esb.creditrisk.generated` | `com.nexgen.sb.creditrisk.generated` |
| _(new)_ | `com.nexgen.sb.creditrisk.config` |
| _(new)_ | `com.nexgen.sb.creditrisk.controller` |
| _(new)_ | `com.nexgen.sb.creditrisk.endpoint` |
| _(new)_ | `com.nexgen.sb.creditrisk.exception` |

---

## 4. Architecture Mapping: Camel Routes → Spring Services

### Legacy Camel Route Chain (4 XML routes, 8 processors):
```
from(REST/SOAP/Gateway endpoint)
  → requestValidator
  → scoringStrategyProcessor
  → bureauRequestBuilder
  → to(bureauEndpoint)         // external SOAP call
  → bureauResponseMapper
  → riskScoreCalculator
  → restResponseBuilder        // REST only
  → wireTap(logTransaction)    // async
  doCatch → errorProcessor
```

### Target Spring Service Chain:
```
Controller / SOAP Endpoint
  → ValidationService.validate(request)
  → ScoringStrategyService.resolveStrategy(request)
  → BureauService.buildAndCallBureau(request)     // STUBBED
  → RiskCalculationService.calculate(request, bureauResponse, strategy)
  → TransactionLogService.logAsync(...)            // @Async
  → GlobalExceptionHandler                         // @ControllerAdvice
```

### Service Mapping Table:

| Legacy Processor | Target Spring Service | Method |
|---|---|---|
| `CreditRiskRequestValidator` | `ValidationService` | `validate(CreditRiskReqType)` |
| `ScoringStrategyProcessor` | `ScoringStrategyService` | `resolveStrategy(CreditRiskReqType)` |
| `BureauRequestBuilder` | `BureauService` | `buildRequest(CreditRiskReqType)` |
| Bureau CXF SOAP call | `BureauService` | `callBureau(BureauInquiryRequest)` **[STUBBED]** |
| `BureauResponseMapper` | `BureauService` | `mapResponse(BureauInquiryResponse)` |
| `RiskScoreCalculator` | `RiskCalculationService` | `calculate(req, creditDetail, strategy)` |
| `GatewayRequestPreProcessor` | `GatewayPreProcessService` | `preProcess(CreditRiskReqType)` |
| `RestResponseBuilder` | _(handled by controller)_ | Direct return |
| `ErrorProcessor` | `GlobalExceptionHandler` | `@ControllerAdvice` |
| `TransactionLogger` | `TransactionLogService` | `logAsync(exchange properties)` `@Async` |
| `CreditRiskRestSvc` | `CreditRiskController` | `@RestController` |
| `CreditRiskSoapSvc/Impl` | `CreditRiskSoapEndpointConfig` | CXF `@Configuration` + endpoint bean |

---

## 5. Endpoint Mapping

### REST Endpoint

| Aspect | Legacy | Target |
|---|---|---|
| Path | `/service/rest/creditrisk/assess` | `/api/v1/creditrisk/assess` |
| Method | GET with 11 query params | GET with 11 query params (identical) |
| Response | XML (`application/xml`) | XML + JSON (content negotiation) |
| Auth | None | Basic Auth |

### SOAP Endpoints (2 — both kept)

| Aspect | Legacy | Target |
|---|---|---|
| Main SOAP | `/service/soap/creditrisk` | `/ws/soap/creditrisk` |
| Gateway SOAP | `/service/creditriskapi` | `/ws/creditriskapi` |
| WSDL | `classpath:wsdl/CreditRiskService.wsdl` | Same — hosted from `src/main/resources/wsdl/` |
| Security | WS-Security UsernameToken | Same — WSS4J via CXF 4.x |
| Schema Validation | Enabled | Enabled |

### Bureau Client (STUBBED)

| Aspect | Legacy | Target |
|---|---|---|
| Call | CXF SOAP to external bureau | **Stubbed** — returns hardcoded successful response |
| Interface | `BureauScoreService.inquire()` | Same interface, mock implementation |
| Config | `BUREAU_ENDPOINT_URL` property | Stub flag in `application.yml` |

---

## 6. Target Directory Structure

```
forward-engineering/
├── pom.xml
├── src/
│   ├── main/
│   │   ├── java/com/nexgen/sb/creditrisk/
│   │   │   ├── CreditRiskApplication.java
│   │   │   ├── config/
│   │   │   │   ├── SecurityConfig.java
│   │   │   │   ├── CxfConfig.java
│   │   │   │   ├── MongoConfig.java
│   │   │   │   ├── AsyncConfig.java
│   │   │   │   └── OpenApiConfig.java
│   │   │   ├── controller/
│   │   │   │   └── CreditRiskController.java
│   │   │   ├── endpoint/
│   │   │   │   └── CreditRiskSoapEndpoint.java
│   │   │   ├── service/
│   │   │   │   ├── ValidationService.java
│   │   │   │   ├── ScoringStrategyService.java
│   │   │   │   ├── BureauService.java
│   │   │   │   ├── RiskCalculationService.java
│   │   │   │   ├── GatewayPreProcessService.java
│   │   │   │   ├── CreditRiskOrchestrationService.java
│   │   │   │   └── TransactionLogService.java
│   │   │   ├── scoring/
│   │   │   │   ├── ScoringStrategy.java
│   │   │   │   ├── StandardScoringStrategy.java
│   │   │   │   ├── ConservativeScoringStrategy.java
│   │   │   │   └── AggressiveScoringStrategy.java
│   │   │   ├── model/
│   │   │   │   ├── CreditRiskReqType.java
│   │   │   │   ├── CreditRiskResType.java
│   │   │   │   ├── CreditScoreDetail.java
│   │   │   │   ├── DebtServiceDetail.java
│   │   │   │   ├── EmploymentRiskDetail.java
│   │   │   │   ├── IncomeVerificationDetail.java
│   │   │   │   ├── RequestHeader.java
│   │   │   │   ├── ResponseHeader.java
│   │   │   │   ├── RequestChannelType.java
│   │   │   │   └── ProvinceType.java
│   │   │   ├── generated/
│   │   │   │   ├── BureauInquiryRequest.java
│   │   │   │   ├── BureauInquiryResponse.java
│   │   │   │   ├── BureauScoreService.java
│   │   │   │   ├── BureauSubject.java
│   │   │   │   └── BureauSubscriber.java
│   │   │   ├── logging/
│   │   │   │   ├── LoggerConstants.java
│   │   │   │   └── TransactionLogService.java
│   │   │   └── exception/
│   │   │       ├── CreditRiskServiceException.java
│   │   │       ├── ValidationException.java
│   │   │       └── GlobalExceptionHandler.java
│   │   └── resources/
│   │       ├── application.yml
│   │       ├── application-dev.yml
│   │       ├── application-qa.yml
│   │       ├── application-prod.yml
│   │       ├── logback-spring.xml
│   │       └── wsdl/
│   │           ├── CreditRiskService.wsdl
│   │           └── ExternalBureauService.wsdl
│   └── test/java/com/nexgen/sb/creditrisk/
│       ├── service/
│       ├── scoring/
│       ├── controller/
│       └── integration/
├── .github/
│   └── workflows/
│       └── ci.yml
└── ec2/
    ├── nexgen-creditrisk.service    (systemd unit)
    └── deploy.sh                     (deployment script)
```

---

## 7. Configuration Mapping

### Legacy `app_config.properties` → Target `application.yml`

```yaml
# application.yml
server:
  port: 8080
  servlet:
    context-path: /nexgen

spring:
  data:
    mongodb:
      uri: ${MONGO_URI:mongodb://localhost:27017/nexgen_creditrisk}
      database: ${MONGO_DB:nexgen_creditrisk}
  security:
    user:
      name: ${BASIC_AUTH_USER:nexgen-admin}
      password: ${BASIC_AUTH_PASSWORD:changeme}

nexgen:
  bureau:
    endpoint-url: ${BUREAU_ENDPOINT_URL:https://qa.nexgenservices.com:9443/services/bureau/creditcheck}
    request-id-prefix: ${BUREAU_REQUEST_ID_PREFIX:dev.nexgen.com-}
    subscriber-code: ${BUREAU_SUBSCRIBER_CODE:NEXGEN-001}
    subscriber-name: ${BUREAU_SUBSCRIBER_NAME:NexGen Financial}
    connection-timeout: ${BUREAU_CONNECTION_TIMEOUT:30000}
    receive-timeout: ${BUREAU_RECEIVE_TIMEOUT:30000}
    stub-enabled: ${BUREAU_STUB_ENABLED:true}
  scoring:
    default-strategy: ${SCORING_DEFAULT_STRATEGY:standard}
  mongo:
    collection: ${MONGO_COLLECTION:transactions}
  supported-provinces: ${SUPPORTED_PROVINCE_LIST:ON,BC,AB,QC}

management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics
  endpoint:
    health:
      show-details: when-authorized
```

---

## 8. Key Migration Rules

1. **All `javax.*` → `jakarta.*`** — JAXB, JAX-WS, validation annotations
2. **All Camel `Exchange` / `Processor` → Spring `@Service`** — No Camel dependency at all
3. **Wire-tap async logging → `@Async` Spring** — `TransactionLogService.logAsync()`
4. **Blueprint XML beans → `@Configuration` / `@Component`** — Spring DI
5. **CXF endpoint declarations → `CxfConfig.java` `@Bean`** — CXF Spring Boot style
6. **Property placeholders `{{KEY}}` → `@Value("${nexgen.key}")` or `@ConfigurationProperties`**
7. **`exchange.getProperty()` / `exchange.setProperty()` → method params/returns** — Direct Java
8. **`exchange.getIn().setBody()` → return value** from service method
9. **Scoring strategies stay as-is** — 3 separate classes, same thresholds, same weights
10. **Business logic identical** — same validation rules CR-001..CR-007, same hardcoded factors (0.03, 0.006), same risk factor thresholds

---

## 9. Issue Dependency Graph

```
FE-001 (Scaffolding)
  ├──→ FE-002 (Config)
  │      ├──→ FE-003 (Models)
  │      │      ├──→ FE-005 (REST Controller)
  │      │      ├──→ FE-006 (SOAP Endpoints + WSDL)
  │      │      ├──→ FE-007 (Validation Service)
  │      │      │      └──→ FE-011 (Orchestration)
  │      │      ├──→ FE-008 (Scoring Strategies)
  │      │      │      └──→ FE-011
  │      │      ├──→ FE-009 (Bureau Service - Stub)
  │      │      │      └──→ FE-011
  │      │      └──→ FE-010 (Risk Calculation)
  │      │             └──→ FE-011
  │      ├──→ FE-012 (MongoDB Logging)
  │      │      └──→ FE-011
  │      └──→ FE-013 (Security)
  │
  ├──→ FE-004 (WSDL Generation)
  │      └──→ FE-006
  │
  └──→ FE-014 (Error Handling + Logging)
         └──→ FE-011

FE-011 (Orchestration) ──→ FE-015 (Tests) ──→ FE-016 (CI/CD + EC2)
```

---

## 10. Acceptance Criteria (Global)

- [ ] `mvn clean verify` passes with 0 failures
- [ ] Application starts: `java -jar target/*.jar` → Spring Boot banner + port 8080
- [ ] REST: `GET /nexgen/api/v1/creditrisk/assess?applicantId=...` returns XML response
- [ ] SOAP: WSDL accessible at `/nexgen/ws/soap/creditrisk?wsdl`
- [ ] SOAP: `assessCreditRisk` operation returns same response structure
- [ ] Gateway SOAP: Same behavior at `/nexgen/ws/creditriskapi`
- [ ] Bureau call is stubbed — returns mock successful response
- [ ] MongoDB logging works — documents appear in `transactions` collection
- [ ] Basic Auth required for REST endpoints
- [ ] WS-Security required for SOAP endpoints
- [ ] Actuator health endpoint at `/nexgen/actuator/health`
- [ ] All 3 scoring strategies produce identical results for same inputs
- [ ] Validation rules CR-001 through CR-007 enforced identically
- [ ] JUnit 5 tests pass with ≥80% coverage on business logic
- [ ] GitHub Actions CI pipeline green on push
