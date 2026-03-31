# Data Flow Diagram

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

_This document presents the Data Flow Diagrams (DFDs) identified during reverse engineering of the application. DFDs illustrate how data moves through the system — from external entities through processes to data stores and back. Diagrams are organized by level of abstraction._

### DFD Notation

| **Symbol**                | **Meaning**                                       |
|---------------------------|---------------------------------------------------|
| ▭ (Rectangle)             | External Entity — source or destination of data   |
| ○ (Circle/Rounded rect)   | Process — transforms or routes data               |
| ═══ (Open rectangle)      | Data Store — where data is persisted              |
| → (Arrow)                 | Data Flow — movement of data between elements     |

<!-- ✅ NEW: Annotation Notation for DFDs -->

### Annotation Notation

| **Annotation**             | **Meaning**                                                       |
|----------------------------|-------------------------------------------------------------------|
| `⚠️ NO VALIDATION`         | Data enters or exits this point without any validation            |
| `⚪ HARDCODED`             | Data at this point is hardcoded/static, not derived from processing |
| `⚫ DEAD FLOW`             | Data flow path exists in code but is never triggered              |
| `🔒 PII`                   | Data flow contains Personally Identifiable Information            |
| `🔒 SENSITIVE`             | Data flow contains sensitive business data                        |
| `📝 NOTE`                  | General observation                                               |

---

## 2. DFD Index

| **Diagram ID** | **Level** | **Title**                    | **Parent Process** | **Description**                    |
|-----------------|-----------|------------------------------|--------------------|------------------------------------|
| DFD-0           | 0         | _Context Diagram_            | —                  | _Highest level system overview_    |
| DFD-1           | 1         | _[Level 1 Title]_           | DFD-0              | _[Description]_                    |
| DFD-1.1         | 2         | _[Level 2 Title]_           | DFD-1 / Process X  | _[Description]_                    |

---

## 3. Context Diagram (Level 0)

**Diagram ID:** DFD-0
**Description:** _Shows the entire application as a single process with all external entities and data flows._

### External Entities

| **Entity ID** | **Entity Name**     | **Type**                       | **Description**                     |
|----------------|---------------------|--------------------------------|-------------------------------------|
| EE-001         | _[Entity Name]_     | _User / External System / API_ | _[Description of the entity]_       |
| EE-002         | _[Entity Name]_     | _User / External System / API_ | _[Description of the entity]_       |

### Context Diagram

```
                    ┌──────────────┐
    [Data Flow A]   │              │   [Data Flow B]
 ▭ EE-001 ────────▶│              │◀──────── ▭ EE-002
                    │              │
    [Data Flow C]   │   SYSTEM     │   [Data Flow D]
 ▭ EE-001 ◀────────│   (P0)       │────────▶ ▭ EE-003
                    │              │
                    │              │   [Data Flow E]
                    │              │────────▶ ▭ EE-004
                    └──────────────┘
```

### Data Flows (Level 0)

<!-- ✅ NEW: Added "Data Sensitivity" and "Annotations" columns -->

| **Flow ID** | **From**       | **To**         | **Data Description**                  | **Format/Protocol** | **Data Sensitivity**                | **Annotations**                         |
|--------------|----------------|----------------|---------------------------------------|---------------------|-------------------------------------|-----------------------------------------|
| DF-001       | _EE-001_       | _System (P0)_  | _[Description of data flowing in]_    | _REST/JSON/CSV_     | _🔒 PII / 🔒 SENSITIVE / Non-Sensitive_ | _[⚠️ NO VALIDATION / ⚪ HARDCODED / None]_ |
| DF-002       | _System (P0)_  | _EE-002_       | _[Description of data flowing out]_   | _REST/JSON/CSV_     | _🔒 PII / 🔒 SENSITIVE / Non-Sensitive_ | _[Annotation or None]_                  |

---

## 4. Level 1 Data Flow Diagram

**Diagram ID:** DFD-1
**Description:** _Decomposes the system into major processes, showing internal data stores and flows._

### Processes

| **Process ID** | **Process Name**        | **Description**                              | **Component(s)**      |
|----------------|-------------------------|----------------------------------------------|-----------------------|
| P1             | _[Process Name]_        | _[What this process does]_                   | _[COMP-XXX]_          |
| P2             | _[Process Name]_        | _[What this process does]_                   | _[COMP-XXX]_          |
| P3             | _[Process Name]_        | _[What this process does]_                   | _[COMP-XXX]_          |

### Data Stores

<!-- ✅ NEW: Added "Data Retention" and "Failure Behavior" columns -->

| **Store ID** | **Store Name**        | **Type**                          | **Technology**      | **Data Retention**                  | **Failure Behavior**                                | **Description**                    |
|--------------|-----------------------|-----------------------------------|---------------------|-------------------------------------|-----------------------------------------------------|------------------------------------|
| DS-001       | _[Store Name]_        | _Database / File / Cache / Queue_ | _[Technology]_      | _[Permanent / X days TTL / None]_   | _[Main flow continues / Main flow fails / Unknown]_ | _[What data is stored]_            |
| DS-002       | _[Store Name]_        | _Database / File / Cache / Queue_ | _[Technology]_      | _[Permanent / X days TTL / None]_   | _[Main flow continues / Main flow fails / Unknown]_ | _[What data is stored]_            |

### Level 1 Diagram

```
                ┌──────────┐            ┌──────────┐
 ▭ EE-001 ────▶│   P1     │───DF-X───▶│   P2     │
                │[Process] │            │[Process] │
                └────┬─────┘            └────┬─────┘
                     │                       │
                  DF-Y                    DF-Z
                     │                       │
                     ▼                       ▼
              ══════════════          ══════════════
              ║  DS-001    ║          ║  DS-002    ║
              ║ [Store]    ║          ║ [Store]    ║
              ══════════════          ══════════════
                     │
                  DF-W
                     │
                     ▼
                ┌──────────┐
                │   P3     │───DF-V───▶ ▭ EE-002
                │[Process] │
                └──────────┘
```

### Data Flows (Level 1)

<!-- ✅ NEW: Added "Data Sensitivity" and "Annotations" columns -->

| **Flow ID** | **From**       | **To**         | **Data Elements**                   | **Trigger**              | **Volume/Frequency**    | **Data Sensitivity**                     | **Annotations**                          |
|--------------|----------------|----------------|-------------------------------------|--------------------------|-------------------------|------------------------------------------|------------------------------------------|
| DF-XXX       | _[Source]_     | _[Destination]_| _[List of data elements]_           | _[What triggers flow]_   | _[e.g., 100/day]_      | _🔒 PII / 🔒 SENSITIVE / Non-Sensitive_  | _[⚠️ NO VALIDATION / ⚪ HARDCODED / None]_ |

---

## 5. Level 2 Data Flow Diagrams

### 5.1 DFD-1.1: _[Process Name]_ Decomposition

**Diagram ID:** DFD-1.1
**Parent Process:** P1 — _[Process Name]_
**Description:** _Detailed decomposition of process P1._

#### Sub-Processes

| **Process ID** | **Process Name**        | **Description**                              |
|----------------|-------------------------|----------------------------------------------|
| P1.1           | _[Sub-process Name]_    | _[What this sub-process does]_               |
| P1.2           | _[Sub-process Name]_    | _[What this sub-process does]_               |

#### Diagram

```
             ┌──────────┐         ┌──────────┐
  DF-A ────▶│  P1.1    │──DF-B─▶│  P1.2    │──DF-C──▶
             │[Sub-proc]│         │[Sub-proc]│
             └────┬─────┘         └──────────┘
                  │
               DF-D
                  ▼
           ══════════════
           ║  DS-001    ║
           ══════════════
```

#### Data Flows (Level 2)

| **Flow ID** | **From**   | **To**     | **Data Elements**             | **Notes**             |
|--------------|------------|------------|-------------------------------|-----------------------|
| DF-XXX       | _[Source]_ | _[Dest]_   | _[Data elements]_             | _[Notes]_             |

---

### 5.2 DFD-1.2: _[Process Name]_ Decomposition

_Repeat Section 5.1 structure for each Level 2 decomposition._

---

## 6. Data Flow Catalog

_Complete reference of all data flows across all levels._

<!-- ✅ NEW: Added "Data Sensitivity" column -->

| **Flow ID** | **Level** | **Source**     | **Destination** | **Data Elements**     | **Direction**  | **Protocol/Format** | **Frequency**    | **Data Sensitivity**                     |
|--------------|-----------|----------------|-----------------|------------------------|----------------|---------------------|------------------|------------------------------------------|
| DF-001       | 0         | _[Source]_     | _[Destination]_ | _[Elements]_           | _In/Out/Both_  | _[Protocol]_        | _[Frequency]_    | _🔒 PII / 🔒 SENSITIVE / Non-Sensitive_  |
| DF-002       | 1         | _[Source]_     | _[Destination]_ | _[Elements]_           | _In/Out/Both_  | _[Protocol]_        | _[Frequency]_    | _🔒 PII / 🔒 SENSITIVE / Non-Sensitive_  |

---

## 7. Data Store Catalog

_Complete reference of all data stores._

| **Store ID** | **Store Name**  | **Type**          | **Used By Processes** | **Key Data Elements**      | **Entity Ref** |
|--------------|-----------------|-------------------|-----------------------|----------------------------|----------------|
| DS-001       | _[Name]_        | _[Type]_          | _P1, P3_              | _[Key fields/entities]_    | _ENT-XXX_      |

---

## 8. Data Transformation Rules

_Document significant data transformations that occur within processes._

<!-- ✅ NEW: Added "Hardcoded Override" column -->

| **Process** | **Input Data**          | **Transformation**                     | **Output Data**          | **Hardcoded Override**                        | **Business Rule**  |
|-------------|-------------------------|----------------------------------------|--------------------------|-----------------------------------------------|--------------------|
| _P1_        | _[Input description]_   | _[What happens to the data]_           | _[Output description]_   | _[None / Hardcoded value bypasses transform]_ | _BR-XXX_           |

---

## 9. Notes & Observations

| **#** | **Observation**                                    | **Related DFD** | **Impact**         |
|-------|----------------------------------------------------|------------------|--------------------|
| 1     | _[Observation about data flow patterns/concerns]_  | _DFD-X_          | _[Impact level]_   |

---

<!-- ✅ NEW: Section 10 — Dead Data Flow Paths -->

## 10. Dead Data Flow Paths

_Data flow paths that exist in the source code but are never triggered in production._

| **Flow ID** | **DFD Level** | **From**       | **To**         | **Description**                          | **Evidence**                                          | **Recommendation**                     |
|--------------|---------------|----------------|----------------|------------------------------------------|-------------------------------------------------------|----------------------------------------|
| _DF-XXX_     | _[Level]_     | _[Source]_     | _[Destination]_| _[Description of dead data flow]_        | _[e.g., Feature flag off, unreachable condition]_     | _Remove / Confirm with business team_  |

---

<!-- ✅ NEW: Section 11 — Transaction Logging Data Flow Detail -->

## 11. Transaction Logging Data Flow Detail

_Dedicated section for the data flow to/from the transaction logging data store, given its significance as a key dependency._

### 11.1 Logging Flow Overview

| **Attribute**                | **Details**                                                  |
|------------------------------|--------------------------------------------------------------|
| Log Data Store               | _[e.g., MongoDB collection name]_                            |
| Logging Trigger              | _[e.g., After every request/response cycle]_                 |
| Synchronous / Asynchronous   | _[Sync / Async / Fire-and-forget]_                           |
| Data Logged                  | _[e.g., Full request payload, full response payload, metadata]_ |
| Failure Impact on Main Flow  | _[Main flow continues silently / Main flow fails / Alert generated]_ |
| Data Retention               | _[Permanent / X days TTL / Configurable]_                    |

### 11.2 Logging Data Elements

| **#** | **Data Element**       | **Source**                  | **Logged As**            | **Sensitivity**                    | **Notes**                |
|-------|------------------------|----------------------------|--------------------------|------------------------------------|--------------------------|
| 1     | _[Element name]_       | _[Request / Response / System]_ | _[Field name in log store]_ | _🔒 PII / 🔒 SENSITIVE / Non-Sensitive_ | _[Notes]_           |

---

<!-- ✅ NEW: Section 12 — Target State Data Flow (Proposed) -->

## 12. Target State Data Flow (Proposed)

_Proposed target-state data flow showing changes after migration, including API Gateway, new validations, and updated data stores._

### 12.1 Key Changes from Current State

| **Change #** | **Description of Change**                                        | **Current DFD**  | **Reason**                     |
|--------------|------------------------------------------------------------------|------------------|--------------------------------|
| 1            | _[e.g., API Gateway added as entry point for all external flows]_ | _DFD-0_         | _New architecture requirement_ |
| 2            | _[e.g., Input validation added before processing]_               | _DFD-1_         | _Addresses GAP-XXX_           |
| 3            | _[e.g., Transaction logging moved to async pattern]_             | _DFD-1_         | _Performance improvement_      |
| 4            | _[e.g., Dead data flow path removed]_                            | _DFD-1.1_       | _Dead code cleanup_           |

### 12.2 Target State Context Diagram (Proposed)

```
                         ┌────────────────┐
                         │  API GATEWAY   │
                         └───────┬────────┘
                                 │
                    ┌────────────┴────────────┐
    [Data Flow A]   │                         │   [Data Flow B]
 ▭ EE-001 ────────▶│   MIGRATED SYSTEM       │◀──────── ▭ EE-002
                    │   (P0-TARGET)           │
    [Data Flow C]   │                         │   [Data Flow D]
 ▭ EE-001 ◀────────│   [+ Validations]       │────────▶ ▭ EE-003
                    │   [+ Dynamic Logic]     │
                    └─────────────────────────┘
```

---

## 13. Appendices

### Appendix A: DFD Leveling Rules

- **Level 0 (Context):** Single process representing the entire system
- **Level 1:** Major functional areas / subsystems
- **Level 2:** Detailed sub-processes within each Level 1 process
- Each level should have no more than 7±2 processes

### Appendix B: Glossary

| **Term**     | **Definition**                                |
|--------------|-----------------------------------------------|
| _[Term]_     | _[Definition]_                                |

<!-- ✅ NEW: Appendix C — Data Sensitivity Classification -->

### Appendix C: Data Sensitivity Classification

| **Classification**       | **Definition**                                                          | **Handling Requirement**                              |
|--------------------------|-------------------------------------------------------------------------|-------------------------------------------------------|
| 🔒 **PII**               | Personally Identifiable Information (name, address, email, etc.)        | Must be encrypted at rest and in transit; access logged |
| 🔒 **Sensitive**          | Sensitive business data (financial, scoring, risk data)                  | Restricted access; audit trail required                |
| **Non-Sensitive**         | Operational/technical data with no privacy or business sensitivity      | Standard handling                                     |

---

> **Document Control:**
> | Version | Date | Author | Changes |
> |---------|------|--------|---------|
> | 0.1 | _[Date]_ | _[Author]_ | Initial draft |
> | 0.2 | _[Date]_ | _[Author]_ | Added: Annotation notation (1), Data sensitivity & annotations columns (3, 4, 6), Data store retention & failure behavior (4), Hardcoded override in transforms (8), Dead data flow paths (10), Transaction logging detail (11), Target state DFD (12), Sensitivity classification (Appendix C) |