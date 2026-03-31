# Design of Experiments — Forward Engineering

---

| **Field** | **Value** |
|---|---|
| **Project** | NexGen Credit Risk Gateway — Forward Engineering |
| **Source** | JBoss Fuse 6.3 / Camel 2.17 / CXF 3.1.5 / Java 8 |
| **Target** | Spring Boot 3.3.x / Java 21 / CXF 4.x / Spring Data MongoDB |
| **Date** | 31-Mar-2026 |
| **Status** | Approved |

---

## 1. Executive Summary

This Design of Experiments defines the experimental approaches for the 6 key technical decisions in the migration from JBoss Fuse 6.3 to Spring Boot 3.3.x. The source system consists of 33 Java classes across 7 packages, 4 Camel XML DSL routes, 3 CXF endpoints, MongoDB wire-tap logging, and 3 scoring strategies — all running as an OSGi bundle on Apache Karaf.

Each experiment validates a hypothesis about the optimal migration path for a specific component, ensuring we select the approach that minimizes risk, preserves functional equivalence, and produces a build-successful Spring Boot application deployed on AWS EC2.

---

## 2. Migration Strategy Analysis

### 2.1 Approach Comparison

| **Approach** | **Description** | **Pros** | **Cons** | **Verdict** |
|---|---|---|---|---|
| **Big Bang Rewrite** | Rewrite entire app from scratch in Spring Boot | Clean architecture, no legacy debt | High risk, long timeline, no incremental value | Not recommended |
| **Strangler Fig** | Run old + new in parallel, migrate route by route | Low risk, incremental, rollback-able | Requires routing layer, operational complexity | Viable for large apps |
| **Lift-and-Shift + Refactor** | Port existing code to Spring Boot, then modernize | Preserves business logic, fast start | Carries some legacy patterns initially | **Recommended** |
| **Branch by Abstraction** | Abstract interfaces, swap implementations | Clean but complex abstraction layers | Over-engineering for this codebase size | Not recommended |

### 2.2 Recommended Approach: Lift-and-Shift + Refactor

**Rationale**: The codebase is small (33 classes) with well-separated concerns (Strategy pattern, discrete processors). A lift-and-shift preserves proven business logic while modernizing infrastructure. The existing processor chain maps naturally to Spring `@Service` beans.

**Execution Plan**:
1. **Phase 1 — Scaffold**: Spring Boot 3.3.x project with Java 21, CXF 4.x, Spring Data MongoDB, Spring Security 6
2. **Phase 2 — Port**: Migrate models → services → orchestration → config (bottom-up)
3. **Phase 3 — Modernize**: Replace Camel routes with `@Service` chain, CXF JAX-RS with Spring MVC, add tests
4. **Phase 4 — Harden**: Security, CI/CD, EC2 deployment config
5. **Phase 5 — Validate**: Unit + integration tests, build verification

---

## 3. Experiments

### EXP-001: Orchestration — Camel 4.x vs Pure Spring @Service

| Attribute | Details |
|---|---|
| **Hypothesis** | Pure Spring `@Service` chain is simpler for this codebase size (8 processors) and eliminates the Camel dependency entirely. |
| **Variable A** | Apache Camel 4.x Java DSL `RouteBuilder` — direct port of XML routes |
| **Variable B** | Spring `@Service` orchestration — one service calls another |
| **Metrics** | Lines of code, dependency count, test setup complexity, debuggability |
| **Decision** | **Variable B (Pure Spring)** — 33 classes is too small for Camel overhead. Spring `@Service` gives direct method calls, standard breakpoints, zero Camel learning curve for maintainers. |

### EXP-002: REST Endpoint — Camel REST DSL vs Spring MVC

| Attribute | Details |
|---|---|
| **Hypothesis** | Spring MVC `@RestController` is the natural choice for a Spring Boot app and provides superior OpenAPI integration via SpringDoc. |
| **Variable A** | Camel REST DSL with Spring Boot |
| **Variable B** | Spring MVC `@RestController` with SpringDoc OpenAPI |
| **Metrics** | OpenAPI generation quality, input validation integration, test setup |
| **Decision** | **Variable B (Spring MVC)** — Native Spring Boot, auto-generates OpenAPI, Bean Validation annotations on params, MockMvc for testing. |

### EXP-003: SOAP Provider — Spring Web Services vs CXF 4.x Starter

| Attribute | Details |
|---|---|
| **Hypothesis** | CXF 4.x Spring Boot Starter provides the path of least resistance since the legacy already uses CXF, and WSS4J configuration is directly reusable. |
| **Variable A** | Spring Web Services (`@Endpoint` + `PayloadRootAnnotationMethodEndpointMapping`) |
| **Variable B** | CXF 4.x Spring Boot Starter (`EndpointImpl` + `WSS4JInInterceptor`) |
| **Metrics** | WSDL compatibility, WS-Security setup effort, Spring Boot integration |
| **Decision** | **Variable B (CXF 4.x)** — Reuses WSS4J interceptor config, WSDL hosting is familiar, `@WebService` annotations are the same (just `javax` → `jakarta`). Spring WS would require rewriting the security layer. |

### EXP-004: MongoDB Integration — Spring Data Repository vs MongoTemplate

| Attribute | Details |
|---|---|
| **Hypothesis** | `MongoTemplate` is better for the audit logging use case (flexible schema, one collection, write-only) while `MongoRepository` is better for CRUD. |
| **Variable A** | Spring Data Repository with `@Document` entity |
| **Variable B** | `MongoTemplate` with raw BSON `Document` |
| **Metrics** | Code simplicity, flexibility for audit schema, testability |
| **Decision** | **Variable B (MongoTemplate)** — The transaction log is write-only with a flat document structure. Repository pattern adds unnecessary abstraction. MongoTemplate maps 1:1 to the legacy raw driver approach. |

### EXP-005: Async Logging — @Async vs Spring Events vs CompletableFuture

| Attribute | Details |
|---|---|
| **Hypothesis** | Spring `@Async` with a thread pool is the simplest replacement for Camel's wire-tap pattern. |
| **Variable A** | `@Async` annotation with `ThreadPoolTaskExecutor` |
| **Variable B** | `ApplicationEventPublisher` with `@EventListener` |
| **Variable C** | `CompletableFuture.runAsync()` |
| **Metrics** | Simplicity, error isolation, testability |
| **Decision** | **Variable A (@Async)** — Simplest, most Spring-idiomatic. Error isolation already handled by try/catch in the log service (same as legacy). Thread pool configurable via `AsyncConfig`. |

### EXP-006: Security Layer — API Key vs Basic Auth vs OAuth2

| Attribute | Details |
|---|---|
| **Hypothesis** | Basic Auth is the right balance of security and simplicity for Phase 1 on EC2, with a clear migration path to OAuth2/JWT later. |
| **Variable A** | API Key header |
| **Variable B** | HTTP Basic Auth (Spring Security 6) |
| **Variable C** | OAuth2/JWT with Spring Authorization Server |
| **Metrics** | Setup complexity, AWS integration, migration path |
| **Decision** | **Variable B (Basic Auth)** — Built into Spring Security, works with any HTTP client, credentials configurable via env vars on EC2. OAuth2 adds infrastructure (token server) not needed for Phase 1. |

---

## 4. Technology Decision Summary

### 4.1 Core Framework

| **Decision** | **Option A** | **Option B** | **Selected** | **Rationale** |
|---|---|---|---|---|
| Application Framework | Spring Boot 3.3.x | Quarkus 3.x | **Spring Boot 3.3.x** | Larger ecosystem, team familiarity |
| Java Version | Java 17 (LTS) | Java 21 (LTS) | **Java 21** | Latest LTS, virtual threads, pattern matching |
| Build Tool | Maven | Gradle | **Maven** | Continuity from source project |

### 4.2 Integration Layer

| **Decision** | **Option A** | **Option B** | **Selected** | **Rationale** |
|---|---|---|---|---|
| Route Engine | Apache Camel 4.x | Pure Spring @Service | **Pure Spring** | No Camel overhead for 8 processors (EXP-001) |
| REST Endpoint | Spring MVC @RestController | CXF JAX-RS | **Spring MVC** | Native Spring Boot, OpenAPI via SpringDoc (EXP-002) |
| SOAP Endpoint | Spring WS | CXF 4.x Spring Boot Starter | **CXF 4.x** | WSS4J reuse, minimal annotation changes (EXP-003) |
| Bureau Client | Stubbed (mock response) | Real SOAP call | **Stubbed** | No real bureau available |
| API Documentation | SpringDoc OpenAPI 3 | Swagger 2 (springfox) | **SpringDoc** | Active project, OpenAPI 3.0 standard |

### 4.3 Data & Security

| **Decision** | **Option A** | **Option B** | **Selected** | **Rationale** |
|---|---|---|---|---|
| MongoDB | MongoTemplate | Spring Data Repository | **MongoTemplate** | Write-only audit, flexible schema (EXP-004) |
| REST Security | Basic Auth | OAuth2/JWT | **Basic Auth** | Simple, configurable, upgradeable (EXP-006) |
| SOAP Security | WS-Security UsernameToken (WSS4J) | mTLS | **WSS4J** | Backward compatible with legacy clients |
| Secret Management | Spring Vault | Environment variables | **Env vars** | Simplicity for EC2 |
| Logging | Logback + SLF4J | Log4j2 | **Logback** | Spring Boot default |

### 4.4 Testing & Deployment

| **Decision** | **Option A** | **Option B** | **Selected** | **Rationale** |
|---|---|---|---|---|
| Unit Testing | JUnit 5 + Mockito | TestNG | **JUnit 5** | Spring Boot default, industry standard |
| API Testing | MockMvc | REST Assured | **MockMvc** | In-process, faster, CI-friendly |
| CI/CD | GitHub Actions | Jenkins | **GitHub Actions** | Native to repo platform |
| Deployment | AWS EC2 (executable JAR) | Docker on ECS | **EC2** | Simpler infrastructure, systemd |

---

## 5. Task Decomposition & Dependency Graph

### 5.1 Work Breakdown Structure (16 Issues)

```
FE-001 ─ Project Scaffolding (Spring Boot 3.3.x + Java 21)
  │
FE-002 ─ Configuration Migration (Blueprint → application.yml + profiles)
  │
  ├── FE-003 ─ Model Layer Migration (javax.xml.bind → jakarta.xml.bind)
  │     │
  │     ├── FE-004 ─ WSDL Generation (CreditRisk + Bureau WSDLs)
  │     │
  │     ├── FE-005 ─ REST API (@RestController + 11 query params)
  │     │
  │     ├── FE-006 ─ SOAP Endpoints (CXF 4.x, 2 providers, WSS4J)
  │     │
  │     ├── FE-007 ─ Validation Service (7 rules CR-001..CR-007)
  │     │
  │     ├── FE-008 ─ Scoring Strategy Migration (3 strategies + resolver)
  │     │
  │     ├── FE-009 ─ Bureau Integration (stubbed mock)
  │     │
  │     └── FE-010 ─ Risk Calculation Service (RiskScoreCalculator)
  │
  ├── FE-011 ─ Orchestration Service (replaces all 4 Camel routes)
  │
  ├── FE-012 ─ MongoDB Transaction Logging (MongoTemplate + @Async)
  │
  └── FE-013 ─ Security (Basic Auth + WS-Security UsernameToken)
        │
FE-014 ─ Error Handling & Logging (Logback + @ControllerAdvice)
  │
FE-015 ─ Unit & Integration Tests (JUnit 5 + Mockito, ≥80%)
  │
FE-016 ─ CI/CD Pipeline (GitHub Actions + EC2 deploy)
```

### 5.2 Issue Dependency Graph

```
                    FE-001 (Scaffolding)
                       │
                    FE-002 (Config)
                       │
          ┌────────────┼────────────┐
          │            │            │
       FE-003       FE-012       FE-014
      (Models)    (MongoDB)   (ErrorHandler)
          │
    ┌─────┼─────┬─────┬─────┬─────┬─────┐
    │     │     │     │     │     │     │
  FE-004 FE-005 FE-006 FE-007 FE-008 FE-009 FE-010
  (WSDL) (REST) (SOAP) (Valid)(Score)(Bureau)(Risk)
    │     │     │     │     │     │     │
    └─────┴─────┴─────┴─────┴─────┴─────┘
                       │
                    FE-011 (Orchestration)
                       │
                    FE-013 (Security)
                       │
                    FE-015 (Tests)
                       │
                    FE-016 (CI/CD)
```

### 5.3 Wave-Based Execution Plan

| **Wave** | **Issues (Parallel)** | **Prerequisite** |
|---|---|---|
| Wave 1 | FE-001, FE-002 | None |
| Wave 2 | FE-003, FE-004, FE-012, FE-014 | FE-002 |
| Wave 3 | FE-005, FE-006, FE-007, FE-008, FE-009, FE-010 | FE-003 |
| Wave 4 | FE-011, FE-013 | Wave 3 |
| Wave 5 | FE-015, FE-016 | Wave 4 |

---

## 6. Risk Assessment

| **Risk** | **Probability** | **Impact** | **Mitigation** |
|---|---|---|---|
| CXF 4.x + Spring Boot 3.x integration issues | Medium | High | Test SOAP endpoint early (FE-006) |
| WSDL generation from annotations may miss edge cases | Medium | Medium | Validate against SOAP UI |
| `javax` → `jakarta` namespace issues in JAXB | Low | Medium | Compile early (FE-003) |
| Spring Security + CXF URL path conflicts | Medium | Medium | Permit `/ws/**` in SecurityConfig |
| MongoDB connection on EC2 (network/auth) | Low | Medium | Externalize via env vars |
| `@Async` thread pool exhaustion under load | Low | Low | Configure pool size, queue capacity |
| Bureau stub may mask real integration issues | Medium | Low | Document stub boundary clearly |

---

## 7. Success Criteria

| **Criteria** | **Target** |
|---|---|
| `mvn clean verify` | 0 failures |
| Application startup | Spring Boot banner + port 8080 |
| REST API | Same 11 params, same response structure |
| SOAP API | WSDL-compatible, same 2 operations |
| Scoring accuracy | Identical results for same inputs |
| Test coverage | ≥80% on business logic |
| CI/CD | GitHub Actions green on main |

---

## 8. Source → Target Component Mapping

| **Source (JBoss Fuse)** | **Target (Spring Boot)** | **FE Issue** |
|---|---|---|
| `pom.xml` (OSGi bundle) | `pom.xml` (Spring Boot starter parent) | FE-001 |
| `blueprint.xml` (beans + routes + CXF) | `application.yml` + `@Configuration` classes | FE-002 |
| Model classes (`javax.xml.bind`) | DTOs (`jakarta.xml.bind` + Jakarta Validation) | FE-003 |
| WSDL (implicit from annotations) | `src/main/resources/wsdl/*.wsdl` (generated) | FE-004 |
| `CreditRiskRestSvc` (JAX-RS + CXF) | `CreditRiskController` (`@RestController`) | FE-005 |
| `CreditRiskSoapSvc` + Gateway (CXF JAXWS) | CXF 4.x `@WebService` endpoints | FE-006 |
| `CreditRiskRequestValidator` (Processor) | `ValidationService` (`@Service`) | FE-007 |
| 3 Scoring Strategies + `ScoringStrategyService` | 3 `@Component` strategies + `@Service` resolver | FE-008 |
| Bureau CXF SOAP client | Stubbed `BureauService` (`@Service`) | FE-009 |
| `RiskScoreCalculator` (Processor) | `RiskCalculationService` (`@Service`) | FE-010 |
| 4 Camel XML routes | `CreditRiskOrchestrationService` (`@Service`) | FE-011 |
| MongoDB (raw driver + wire-tap) | `MongoTemplate` + `@Async` | FE-012 |
| WSS4J WS-Security | Spring Security 6 + WSS4J | FE-013 |
| Log4j + wire-tap | Logback + `@ControllerAdvice` | FE-014 |
| No tests (0%) | JUnit 5 + Mockito (≥80%) | FE-015 |
| No CI/CD | GitHub Actions + EC2 deploy | FE-016 |

---

## 9. Appendix: Target Directory Structure

```
forward-engineering/
├── src/main/java/com/nexgen/sb/creditrisk/
│   ├── CreditRiskApplication.java
│   ├── config/
│   │   ├── CxfConfig.java
│   │   ├── SecurityConfig.java
│   │   ├── MongoConfig.java
│   │   ├── AsyncConfig.java
│   │   └── properties/
│   │       ├── BureauProperties.java
│   │       ├── ScoringProperties.java
│   │       └── NexgenProperties.java
│   ├── controller/
│   │   └── CreditRiskController.java
│   ├── ws/
│   │   ├── CreditRiskSoapEndpoint.java
│   │   └── GatewaySoapEndpoint.java
│   ├── service/
│   │   ├── ValidationService.java
│   │   ├── ScoringStrategyService.java
│   │   ├── BureauService.java
│   │   ├── RiskCalculationService.java
│   │   ├── CreditRiskOrchestrationService.java
│   │   ├── GatewayPreProcessService.java
│   │   └── TransactionLogService.java
│   ├── scoring/
│   │   ├── ScoringStrategy.java
│   │   ├── StandardScoringStrategy.java
│   │   ├── ConservativeScoringStrategy.java
│   │   └── AggressiveScoringStrategy.java
│   ├── model/
│   │   ├── CreditRiskRequest.java
│   │   ├── CreditRiskResponse.java
│   │   ├── CreditScoreDetail.java
│   │   ├── RiskFactor.java
│   │   ├── BureauResult.java
│   │   └── enums/
│   │       ├── RequestChannelType.java
│   │       └── ProvinceType.java
│   └── exception/
│       ├── CreditRiskServiceException.java
│       ├── ValidationException.java
│       └── GlobalExceptionHandler.java
├── src/main/resources/
│   ├── application.yml
│   ├── application-dev.yml
│   ├── application-qa.yml
│   ├── application-prod.yml
│   ├── wsdl/
│   │   ├── CreditRiskService.wsdl
│   │   └── ExternalBureauService.wsdl
│   └── logback-spring.xml
├── src/test/java/com/nexgen/sb/creditrisk/
│   ├── service/
│   ├── scoring/
│   ├── controller/
│   └── ws/
├── .github/workflows/
│   └── ci.yml
├── deploy/
│   ├── deploy.sh
│   ├── creditrisk-gateway.service
│   └── env.conf.template
└── pom.xml
```

---

## 10. Appendix: Spring Boot Starter Dependencies (Planned)

```xml
spring-boot-starter-web
spring-boot-starter-data-mongodb
spring-boot-starter-security
spring-boot-starter-actuator
spring-boot-starter-validation
spring-boot-starter-test
cxf-spring-boot-starter-jaxws          <!-- CXF 4.x SOAP -->
cxf-rt-ws-security                      <!-- WSS4J -->
springdoc-openapi-starter-webmvc-ui     <!-- OpenAPI 3 -->
```
