# Reverse Engineering — NexGen Credit Risk Gateway

## What is Reverse Engineering?

**Reverse Engineering (RE)** in the context of this project means systematically analyzing the existing JBoss Fuse 6.3 legacy application to extract, document, and catalog all:

- **Business logic** — Validation rules, scoring algorithms, risk factor identification
- **Data flows** — How requests traverse from REST/SOAP endpoints through Camel routes to the external bureau and back
- **Integration patterns** — SOAP provider/consumer endpoints, wire-tap logging, error handling
- **Technical gaps** — Security vulnerabilities, missing features, dead code, hardcoded values
- **Test coverage** — What tests exist (none) and what tests are needed

The goal is to create a **comprehensive knowledge base** that serves as the foundation for Forward Engineering — ensuring nothing is lost or misunderstood during migration.

---

## Legacy System Overview

| Aspect | Details |
|---|---|
| **Platform** | JBoss Fuse 6.3.0.redhat-187 |
| **Frameworks** | Apache Camel 2.17, Apache CXF 3.1.5 |
| **Java Version** | 8 |
| **Packaging** | OSGi Bundle (deployed on Apache Karaf) |
| **Database** | MongoDB 3.4 (transaction logging) |
| **Codebase** | 33 Java classes across 7 packages |

### Architecture
```
Client → [REST or SOAP Endpoint] → Camel Route → [Validate → Score → Bureau → Calculate] → Response
                                                                                    ↓
                                                                              [MongoDB Log]
```

### Package Structure
| Package | Classes | Purpose |
|---|---|---|
| `com.nexgen.esb.creditrisk.service` | 4 | REST + SOAP endpoint facades |
| `com.nexgen.esb.creditrisk.processor` | 8 | Camel processors (business logic) |
| `com.nexgen.esb.creditrisk.model` | 10 | JAXB data models + enums |
| `com.nexgen.esb.creditrisk.scoring` | 4 | Strategy pattern scoring |
| `com.nexgen.esb.creditrisk.generated` | 5 | Bureau SOAP client classes |
| `com.nexgen.esb.creditrisk.logging` | 2 | MongoDB transaction logger |

---

## RE Deliverables

10 comprehensive documents were produced:

| # | Document | Size | Description |
|---|---|---|---|
| RE-001 | [Discovery Report](../docs/re/01-discovery-report.md) | 71 KB | High-level system overview, technology inventory, dependencies |
| RE-002 | [Component Catalog](../docs/re/02-component-catalog.md) | 78 KB | All 33 classes cataloged with interfaces, dependencies, responsibilities |
| RE-003 | [Sequence Diagrams](../docs/re/03-sequence-diagrams.md) | 88 KB | 6 sequence diagrams covering all 4 Camel routes |
| RE-004 | [Business Rules Catalog](../docs/re/04-business-rules-catalog.md) | 74 KB | 45 business rules across 7 categories |
| RE-005 | [Data Dictionary](../docs/re/05-data-dictionary.md) | 108 KB | All fields, types, constraints, and mappings |
| RE-006 | [Data Flow Diagrams](../docs/re/06-data-flow-diagrams.md) | 89 KB | System and component-level data flows |
| RE-007 | [BDD Feature Specs](../docs/re/07-bdd-feature-specs.md) | 89 KB | Behavior-driven specifications for all features |
| RE-008 | [Test Case Inventory](../docs/re/08-test-case-inventory.md) | 65 KB | 198 test cases across 24 categories |
| RE-009 | [Gap Report](../docs/re/09-gap-report.md) | 59 KB | 33 gaps: 6 Critical, 12 High, 13 Medium, 2 Low |
| RE-010 | [Field-to-Field Mapping](../docs/re/10-field-to-field-mapping.md) | 69 KB | Source-to-target field mapping for migration |

---

## Key Findings

### Business Logic
- **3 Scoring Strategies** (Strategy Pattern): Standard, Conservative, Aggressive
  - Product type determines strategy: MORTGAGE/AUTO_LOAN → Conservative, CREDIT_CARD → Aggressive
- **7 Validation Rules** (CR-001 through CR-007): Required fields, SIN format, DOB format, province enum, postal code
- **6 Risk Factors** identified: LOW_CREDIT_SCORE, HIGH_CREDIT_UTILIZATION, PAST_DELINQUENCIES, EXCESSIVE_CREDIT_INQUIRIES, HIGH_DEBT_TO_INCOME, NO_EMPLOYMENT_INCOME

### Critical Gaps (from RE-009)
| Severity | Count | Examples |
|---|---|---|
| **Critical** | 6 | Zero test coverage, PII in logs, no TLS enforcement |
| **High** | 12 | No income verification, no LTV, no rate limiting |
| **Medium** | 13 | Hardcoded factors, duplicate code, magic numbers |
| **Low** | 2 | Dead code stubs, unused batch enum |

### Test Coverage
- **Current: 0%** — No test files exist
- **Needed: 198 test cases** documented in RE-008
