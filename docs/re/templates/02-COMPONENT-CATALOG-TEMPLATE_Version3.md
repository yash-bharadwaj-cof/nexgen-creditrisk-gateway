# Component Catalog

---

| **Field**            | **Details**                        |
|----------------------|------------------------------------|
| **Project Name**     | _[Project Name]_                   |
| **Application Name** | _[Application Name]_               |
| **Version**          | _[Version Number]_                 |
| **Date**             | _[DD-MMM-YYYY]_                    |
| **Prepared By**      | _[Author Name]_                    |
| **Reviewed By**      | _[Reviewer Name]_                  |
| **Status**           | _Draft / In Review / Approved_     |

---

## 1. Overview

_Provide a brief description of the component catalog's purpose and how it relates to the overall application architecture. This catalog enumerates all software components, modules, services, and libraries identified during reverse engineering._

---

## 2. Component Summary

| **Total Components** | **Modules** | **Services** | **Libraries** | **Shared Utilities** |
|----------------------|-------------|--------------|---------------|----------------------|
| _[Count]_            | _[Count]_   | _[Count]_    | _[Count]_     | _[Count]_            |

<!-- ✅ NEW: Section 2.1 — Component Health Summary -->

### 2.1 Component Health Summary

| **Status**                       | **Count** | **Percentage** |
|----------------------------------|-----------|----------------|
| ✅ Active                         | _[#]_     | _[%]_          |
| ⚠️ Partially Used                | _[#]_     | _[%]_          |
| 🔴 Dead Code                     | _[#]_     | _[%]_          |
| ⚪ Contains Hardcoded Values      | _[#]_     | _[%]_          |

---

## 3. Component Inventory

### 3.1 Component: _[Component Name]_

<!-- ✅ NEW: Added "Framework Version", "Migration Risk", "Hardcoded Values", "Dead Code Present" attributes -->

| **Attribute**            | **Details**                                      |
|--------------------------|--------------------------------------------------|
| **Component ID**         | _COMP-001_                                       |
| **Component Name**       | _[Name]_                                         |
| **Type**                 | _Module / Service / Library / Utility / UI_      |
| **Layer**                | _Presentation / Business / Data / Integration_   |
| **Package/Namespace**    | _[e.g., com.company.module.submodule]_           |
| **Source Path**          | _[e.g., src/main/java/com/company/module/]_      |
| **Language**             | _[e.g., Java, C#, Python]_                       |
| **Framework**            | _[e.g., Spring MVC, Hibernate]_                  |
| **Framework Version**    | _[e.g., 5.3.x]_                                 |
| **Description**          | _[What this component does]_                     |
| **Business Capability**  | _[Business function it supports]_                |
| **Owner/Team**           | _[Responsible team]_                             |
| **Status**               | _Active / Deprecated / Unused / Partially Used_  |
| **Migration Risk**       | _🟢 Low / 🟡 Medium / 🔴 High_                  |
| **Hardcoded Values**     | _Yes / No — If Yes, see Section 10_              |
| **Dead Code Present**    | _Yes / No — If Yes, see Section 7_               |

#### Dependencies

<!-- ✅ NEW: Added "Version", "Support Status", and "Migration Risk" columns -->

| **Depends On**          | **Dependency Type**       | **Version**   | **Support Status**                        | **Migration Risk**             | **Notes**                    |
|-------------------------|---------------------------|---------------|-------------------------------------------|--------------------------------|------------------------------|
| _[Component/Library]_   | _Compile / Runtime / API_ | _[Version]_   | _✅ Supported / ⚠️ Nearing EOL / 🔴 EOL_ | _🟢 Low / 🟡 Med / 🔴 High_  | _[Any relevant notes]_       |

#### Exposed Interfaces

<!-- ✅ NEW: Added "Auth Mechanism" and "Rate Limit" columns -->

| **Interface**           | **Type**        | **Auth Mechanism**                  | **Rate Limit**         | **Description**                     |
|-------------------------|-----------------|-------------------------------------|------------------------|-------------------------------------|
| _[API/Method/Event]_    | _REST/SOAP/RPC_ | _[API Key / OAuth / mTLS / None]_   | _[Yes/No — details]_   | _[Brief description]_               |

#### Key Classes / Files

| **Class/File Name**     | **Responsibility**               | **LOC** |
|-------------------------|----------------------------------|---------|
| _[ClassName.java]_      | _[What this class does]_         | _[#]_   |

---

### 3.2 Component: _[Component Name]_

_Repeat Section 3.1 structure for each component._

---

## 4. Component Dependency Map

_Provide a visual or tabular representation of inter-component dependencies._

### 4.1 Dependency Matrix

| **Component ↓ / Depends On →** | COMP-001 | COMP-002 | COMP-003 | COMP-004 |
|---------------------------------|----------|----------|----------|----------|
| **COMP-001**                    | —        | ✓        |          |          |
| **COMP-002**                    |          | —        | ✓        |          |
| **COMP-003**                    |          |          | —        | ✓        |
| **COMP-004**                    | ✓        |          |          | —        |

### 4.2 Dependency Diagram

```
┌─────────────┐     ┌─────────────┐
│  COMP-001   │────▶│  COMP-002   │
│ [Name]      │     │ [Name]      │
└─────────────┘     └──────┬──────┘
                           │
                           ▼
                    ┌─────────────┐
                    │  COMP-003   │
                    │ [Name]      │
                    └─────────────┘
```

---

## 5. Layered Component View

### 5.1 Presentation Layer

| **Component ID** | **Component Name**   | **Technology**    | **Description**           |
|------------------|----------------------|-------------------|---------------------------|
| _COMP-XXX_       | _[Name]_             | _[Tech]_          | _[Description]_           |

### 5.2 Business Logic Layer

| **Component ID** | **Component Name**   | **Technology**    | **Description**           |
|------------------|----------------------|-------------------|---------------------------|
| _COMP-XXX_       | _[Name]_             | _[Tech]_          | _[Description]_           |

### 5.3 Data Access Layer

| **Component ID** | **Component Name**   | **Technology**    | **Description**           |
|------------------|----------------------|-------------------|---------------------------|
| _COMP-XXX_       | _[Name]_             | _[Tech]_          | _[Description]_           |

### 5.4 Integration Layer

| **Component ID** | **Component Name**   | **Technology**    | **Description**           |
|------------------|----------------------|-------------------|---------------------------|
| _COMP-XXX_       | _[Name]_             | _[Tech]_          | _[Description]_           |

### 5.5 Cross-Cutting Concerns

| **Component ID** | **Component Name**   | **Concern Type**       | **Description**           |
|------------------|----------------------|------------------------|---------------------------|
| _COMP-XXX_       | _[Name]_             | _Logging/Security/etc_ | _[Description]_           |

---

## 6. Third-Party Libraries & Frameworks

<!-- ✅ NEW: Added "Support Status" and "Migration Risk" columns -->

| **Library/Framework** | **Version** | **License**    | **Used By**       | **Support Status**                        | **Migration Risk**             | **Purpose**                |
|-----------------------|-------------|----------------|-------------------|-------------------------------------------|--------------------------------|----------------------------|
| _[Name]_              | _[Ver]_     | _[License]_    | _[COMP-XXX]_      | _✅ Supported / ⚠️ Nearing EOL / 🔴 EOL_ | _🟢 Low / 🟡 Med / 🔴 High_  | _[Why it's used]_          |

---

## 7. Deprecated / Dead Code Components

<!-- ✅ NEW: Added "Evidence Type" and "Lines Affected" columns -->

| **Component ID** | **Component Name** | **Reason**                   | **Evidence Type**                                                           | **Lines Affected** | **Last Modified** | **Recommendation**    |
|------------------|--------------------|------------------------------|-----------------------------------------------------------------------------|--------------------|-------------------|-----------------------|
| _COMP-XXX_       | _[Name]_           | _[Why it's deprecated/dead]_ | _[Unreachable code / Never invoked / Commented out / Feature flag off]_     | _[L##-L##]_        | _[Date]_          | _Remove / Refactor_   |

---

## 8. Notes & Observations

| **#** | **Observation**                                | **Severity**            | **Recommendation**          |
|-------|------------------------------------------------|-------------------------|-----------------------------|
| 1     | _[Observation]_                                | _Info/Warning/Critical_ | _[Recommendation]_          |

---

## 9. Appendices

### Appendix A: Component ID Reference

_Naming convention: COMP-[NNN] where NNN is a sequential number._

### Appendix B: Glossary

| **Term**     | **Definition**                                |
|--------------|-----------------------------------------------|
| _[Term]_     | _[Definition]_                                |

---

<!-- ✅ NEW: Section 10 — Hardcoded Values per Component -->

## 10. Hardcoded Values per Component

_This section lists all hardcoded/static values found within components that should be externalized to configuration or replaced with dynamic logic._

| **Component ID** | **File/Class**    | **Field/Variable** | **Hardcoded Value** | **Line(s)** | **Expected Behavior**                     | **Impact if Not Addressed**                       | **Recommendation**                              |
|------------------|-------------------|--------------------|---------------------|-------------|-------------------------------------------|---------------------------------------------------|-------------------------------------------------|
| _COMP-XXX_       | _[File path]_     | _[Field name]_     | _[Value]_           | _[L##]_     | _[Should be config-driven / dynamic]_     | _[Business logic bypassed / Incorrect results]_   | _Externalize / Implement dynamic logic_         |

---

<!-- ✅ NEW: Section 11 — Component-to-Endpoint Mapping -->

## 11. Component-to-Endpoint Mapping

_Cross-reference showing which components serve which endpoints for traceability during migration._

| **Endpoint**             | **HTTP Method** | **Route Component** | **Processor(s)** | **Transformer(s)** | **Data Access** | **External Calls**    |
|--------------------------|-----------------|---------------------|-------------------|---------------------|-----------------|-----------------------|
| _[/api/v1/endpoint]_     | _GET/POST/..._  | _[COMP-XXX]_        | _[COMP-XXX]_      | _[COMP-XXX]_        | _[COMP-XXX]_    | _[COMP-XXX / None]_   |

---

> **Document Control:**
> | Version | Date | Author | Changes |
> |---------|------|--------|---------|
> | 0.1 | _[Date]_ | _[Author]_ | Initial draft |
> | 0.2 | _[Date]_ | _[Author]_ | Added: Component health summary (2.1), Migration risk & hardcoded/dead code flags (3.1), Dependency version & risk (3.1), Interface auth details (3.1), Library support status (6), Dead code evidence (7), Hardcoded values section (10), Component-to-endpoint mapping (11) |