# Forward Engineering — NexGen Credit Risk Gateway

## What is Forward Engineering?

**Forward Engineering (FE)** in this project means taking the knowledge captured during Reverse Engineering and using it to build a **new, modern Spring Boot application** that is functionally equivalent to the legacy JBoss Fuse system. The goal is:

- **Functional equivalence** — Same inputs produce same outputs
- **Modern stack** — Spring Boot 3.x, Java 21, no legacy dependencies
- **Production-ready** — Tests, CI/CD, AWS EC2 deployment
- **No Camel** — The orchestration engine is replaced with pure Spring `@Service` chains

Forward Engineering does NOT fix the gaps identified in RE-009. Those are deferred to a future phase.

---

## Migration Summary

| Aspect | Legacy (JBoss Fuse) | Target (Spring Boot) |
|---|---|---|
| **Container** | JBoss Fuse 6.3 / OSGi / Karaf | Spring Boot 3.3.x executable JAR |
| **Java** | 8 | 21 (LTS) |
| **Orchestration** | Apache Camel 2.17 (4 XML DSL routes) | Spring `@Service` chain (no Camel) |
| **REST** | CXF JAX-RS (`@Path`, `@GET`) | Spring MVC `@RestController` |
| **SOAP (Provider)** | CXF JAX-WS (Blueprint XML) | CXF 4.x Spring Boot Starter |
| **SOAP (Bureau Client)** | CXF SOAP client | **Stubbed** — mock response |
| **MongoDB** | Raw driver 3.4.2 | Spring Data MongoTemplate |
| **Security (REST)** | None | Basic Auth (Spring Security 6) |
| **Security (SOAP)** | WS-Security UsernameToken | Same — WSS4J via CXF 4.x |
| **Logging** | Log4j (PII exposed) | Logback + PII masking |
| **Testing** | None (0%) | JUnit 5 + Mockito (≥80% coverage) |
| **CI/CD** | None | GitHub Actions |
| **Deployment** | Karaf container | AWS EC2 (executable JAR) |

---

## Architecture Change

### Legacy: Camel Route Orchestration
```
REST/SOAP Endpoint
  → Camel Route (XML DSL)
    → Processor Chain (via Exchange)
      → requestValidator
      → scoringStrategyProcessor
      → bureauRequestBuilder
      → [CXF SOAP call to bureau]
      → bureauResponseMapper
      → riskScoreCalculator
      → restResponseBuilder
      → wireTap(logTransaction)
```

### Target: Spring Service Orchestration
```
Controller / SOAP Endpoint
  → CreditRiskOrchestrationService
    → ValidationService.validate()
    → ScoringStrategyService.resolveStrategy()
    → BureauService.buildAndCallBureau()  [STUBBED]
    → RiskCalculationService.calculate()
    → TransactionLogService.logAsync()    [@Async]
  ← Response
```

---

## Package Mapping

| Legacy Package | Target Package |
|---|---|
| `com.nexgen.esb.creditrisk.service` | `com.nexgen.sb.creditrisk.service` |
| `com.nexgen.esb.creditrisk.processor` | `com.nexgen.sb.creditrisk.service` (merged) |
| `com.nexgen.esb.creditrisk.model` | `com.nexgen.sb.creditrisk.model` |
| `com.nexgen.esb.creditrisk.scoring` | `com.nexgen.sb.creditrisk.scoring` |
| `com.nexgen.esb.creditrisk.generated` | `com.nexgen.sb.creditrisk.generated` |
| `com.nexgen.esb.creditrisk.logging` | `com.nexgen.sb.creditrisk.logging` |
| _(new)_ | `com.nexgen.sb.creditrisk.config` |
| _(new)_ | `com.nexgen.sb.creditrisk.controller` |
| _(new)_ | `com.nexgen.sb.creditrisk.endpoint` |
| _(new)_ | `com.nexgen.sb.creditrisk.exception` |

---

## FE Issue Tracker

16 issues filed (#36 — #51), organized in 5 waves:

### Wave 1 — Foundation
| Issue | Title | Priority |
|---|---|---|
| FE-001 (#36) | Project Scaffolding — Spring Boot 3.x + Java 21 | P0 |
| FE-002 (#37) | Configuration Migration — application.yml profiles | P0 |

### Wave 2 — Models & Contracts
| Issue | Title | Priority |
|---|---|---|
| FE-003 (#38) | Model Layer Migration — javax → jakarta | P0 |
| FE-004 (#39) | WSDL Generation | P0 |

### Wave 3 — Core Services
| Issue | Title | Priority |
|---|---|---|
| FE-005 (#40) | REST API — @RestController | P1 |
| FE-006 (#41) | SOAP Endpoints — CXF 4.x | P1 |
| FE-007 (#42) | Validation Service | P1 |
| FE-008 (#43) | Scoring Strategies | P1 |
| FE-009 (#44) | Bureau Service (Stubbed) | P1 |
| FE-010 (#45) | Risk Calculation Service | P1 |
| FE-012 (#47) | MongoDB Logging | P1 |
| FE-013 (#48) | Security (Basic Auth + WS-Security) | P1 |
| FE-014 (#49) | Error Handling & Logging | P1 |

### Wave 4 — Orchestration
| Issue | Title | Priority |
|---|---|---|
| FE-011 (#46) | Orchestration Service — replaces all Camel routes | P0 |

### Wave 5 — Quality & Delivery
| Issue | Title | Priority |
|---|---|---|
| FE-015 (#50) | Unit & Integration Tests | P1 |
| FE-016 (#51) | CI/CD Pipeline + EC2 Deployment | P2 |

---

## Dependency Graph
```
FE-001 (Scaffold) → FE-002 (Config) → FE-003 (Models) → [FE-005..FE-010, FE-012..FE-014]
                                     → FE-004 (WSDLs) → FE-006 (SOAP)
All services → FE-011 (Orchestration) → FE-015 (Tests) → FE-016 (CI/CD)
```

---

## Acceptance Criteria (Global)

- `mvn clean verify` passes with 0 failures
- Application starts on port 8080 with Spring Boot banner
- REST endpoint: `GET /nexgen/api/v1/creditrisk/assess?applicantId=...`
- SOAP WSDL: `/nexgen/ws/soap/creditrisk?wsdl`
- Gateway WSDL: `/nexgen/ws/creditriskapi?wsdl`
- Bureau call stubbed — returns mock successful response
- MongoDB logging works with PII masking
- Basic Auth on REST, WS-Security on SOAP
- Actuator health at `/nexgen/actuator/health`
- ≥80% test coverage on business logic
- GitHub Actions CI green
