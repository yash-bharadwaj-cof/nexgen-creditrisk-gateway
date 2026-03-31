# Sequence Diagrams

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

_This document contains sequence diagrams capturing the runtime interaction patterns between components, services, and actors in the application. Each diagram represents a specific use case or workflow identified during reverse engineering._

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

<!-- ✅ NEW: Annotation Notation -->

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

| **Diagram ID** | **Title**                        | **Use Case / Workflow**    | **Actors/Components**        | **Priority**  |
|-----------------|----------------------------------|----------------------------|------------------------------|---------------|
| SD-001          | _[Diagram Title]_                | _[Use Case Reference]_     | _[List of participants]_     | _High/Med/Low_|
| SD-002          | _[Diagram Title]_                | _[Use Case Reference]_     | _[List of participants]_     | _High/Med/Low_|
| SD-003          | _[Diagram Title]_                | _[Use Case Reference]_     | _[List of participants]_     | _High/Med/Low_|

---

## 3. Sequence Diagrams

### 3.1 SD-001: _[Diagram Title]_

**Use Case:** _[Use Case ID / Name]_
**Description:** _[Brief description of the interaction being modeled]_
**Trigger:** _[What initiates this sequence]_
**Pre-conditions:** _[Conditions that must be true before this sequence starts]_
**Post-conditions:** _[State of the system after the sequence completes]_

#### Participants

| **Participant**   | **Type**                       | **Component ID** | **Description**            |
|-------------------|--------------------------------|-------------------|----------------------------|
| _[Actor/System]_  | _Actor / Service / DB / Queue_ | _COMP-XXX_        | _[Role in this sequence]_  |

#### Sequence Diagram (ASCII)

```
┌──────┐          ┌──────────┐          ┌────────────┐          ┌─────────┐          ┌──────────┐
│ User │          │ UI Layer │          │ Controller │          │ Service │          │ Database │
└──┬───┘          └────┬─────┘          └─────┬──────┘          └────┬────┘          └────┬─────┘
   │                   │                      │                      │                    │
   │ [Action Desc]     │                      │                      │                    │
   │──────────────────▶│                      │                      │                    │
   │                   │                      │                      │                    │
   │                   │ [Request Method]     │                      │                    │
   │                   │─────────────────────▶│                      │                    │
   │                   │                      │                      │                    │
   │                   │                      │ [Service Method]     │                    │
   │                   │                      │─────────────────────▶│                    │
   │                   │                      │                      │                    │
   │                   │                      │                      │ [Query/Command]    │
   │                   │                      │                      │───────────────────▶│
   │                   │                      │                      │                    │
   │                   │                      │                      │     [Result]       │
   │                   │                      │                      │◀───────────────────│
   │                   │                      │                      │                    │
   │                   │                      │  [Response Object]   │                    │
   │                   │                      │◀─────────────────────│                    │
   │                   │                      │                      │                    │
   │                   │  [HTTP Response]     │                      │                    │
   │                   │◀─────────────────────│                      │                    │
   │                   │                      │                      │                    │
   │ [Display Result]  │                      │                      │                    │
   │◀──────────────────│                      │                      │                    │
   │                   │                      │                      │                    │
┌──┴───┐          ┌────┴─────┐          ┌─────┴──────┐          ┌────┴────┐          ┌────┴─────┐
│ User │          │ UI Layer │          │ Controller │          │ Service │          │ Database │
└──────┘          └──────────┘          └────────────┘          └─────────┘          └──────────┘
```

#### Step-by-Step Description

<!-- ✅ NEW: Added "Annotations" column -->

| **Step** | **From**       | **To**         | **Message/Action**            | **Type**   | **Annotations**                                  | **Notes**                   |
|----------|----------------|----------------|-------------------------------|------------|--------------------------------------------------|-----------------------------|
| 1        | _User_         | _UI Layer_     | _[Action description]_        | _Sync_     | _[⚠️ GAP / ⚪ HARDCODED / ⚫ DEAD CODE / None]_  | _[Notes]_                   |
| 2        | _UI Layer_     | _Controller_   | _[Method/endpoint]_           | _Sync_     | _[Annotation or None]_                           | _[Notes]_                   |
| 3        | _Controller_   | _Service_      | _[Method call]_               | _Sync_     | _[Annotation or None]_                           | _[Notes]_                   |
| 4        | _Service_      | _Database_     | _[Query/command]_             | _Sync_     | _[Annotation or None]_                           | _[Notes]_                   |
| 5        | _Database_     | _Service_      | _[Return result]_             | _Response_ | _[Annotation or None]_                           | _[Notes]_                   |
| 6        | _Service_      | _Controller_   | _[Return DTO]_                | _Response_ | _[Annotation or None]_                           | _[Notes]_                   |
| 7        | _Controller_   | _UI Layer_     | _[HTTP Response]_             | _Response_ | _[Annotation or None]_                           | _[Notes]_                   |
| 8        | _UI Layer_     | _User_         | _[Display]_                   | _Response_ | _[Annotation or None]_                           | _[Notes]_                   |

#### Error / Alternative Flows

| **Condition**               | **Alternative Flow**                              |
|-----------------------------|---------------------------------------------------|
| _[e.g., Invalid Input]_     | _[What happens in this case]_                     |
| _[e.g., DB Connection Fail]_| _[Error handling behavior]_                       |
| _[e.g., Unauthorized]_      | _[Redirect / Error response]_                     |

<!-- ✅ NEW: Annotations Summary sub-section per diagram -->

#### Annotations Summary

| **Step** | **Annotation Type** | **Description**                                          | **Linked Gap/Finding** |
|----------|---------------------|----------------------------------------------------------|------------------------|
| _[#]_    | _⚠️ GAP_            | _[e.g., No input validation at this step]_               | _GAP-XXX_              |
| _[#]_    | _⚪ HARDCODED_       | _[e.g., Response field always returns static value]_     | _GAP-XXX_              |
| _[#]_    | _⚫ DEAD CODE_       | _[e.g., This branch is never reached in production]_     | _GAP-XXX_              |

---

### 3.2 SD-002: _[Diagram Title]_

_Repeat Section 3.1 structure for each sequence diagram._

---

### 3.3 SD-003: _[Diagram Title]_

_Repeat Section 3.1 structure for each sequence diagram._

---

## 4. Cross-Cutting Sequence Patterns

_Document recurring interaction patterns observed across multiple sequences._

### 4.1 Authentication Flow Pattern

```
┌────────┐          ┌─────────────┐          ┌──────────────┐          ┌────────────┐
│ Client │          │ Auth Filter │          │ Auth Service │          │ User Store │
└───┬────┘          └──────┬──────┘          └──────┬───────┘          └─────┬──────┘
    │                      │                        │                        │
    │ Request + credentials│                        │                        │
    │─────────────────────▶│                        │                        │
    │                      │                        │                        │
    │                      │ validate(credentials)  │                        │
    │                      │───────────────────────▶│                        │
    │                      │                        │                        │
    │                      │                        │ lookupUser(username)   │
    │                      │                        │───────────────────────▶│
    │                      │                        │                        │
    │                      │                        │      User record       │
    │                      │                        │◀───────────────────────│
    │                      │                        │                        │
    │                      │  Auth token / reject   │                        │
    │                      │◀───────────────────────│                        │
    │                      │                        │                        │
    │ Auth response / 401  │                        │                        │
    │◀─────────────────────│                        │                        │
    │                      │                        │                        │
┌───┴────┐          ┌──────┴──────┐          ┌──────┴───────┐          ┌─────┴──────┐
│ Client │          │ Auth Filter │          │ Auth Service │          │ User Store │
└────────┘          └─────────────┘          └──────────────┘          └────────────┘
```

### 4.2 Error Handling Pattern

```
┌────────┐          ┌─────────┐          ┌───────────────────┐          ┌────────┐
│ Caller │          │ Service │          │ Exception Handler │          │ Logger │
└───┬────┘          └────┬────┘          └─────────┬─────────┘          └───┬────┘
    │                    │                         │                        │
    │ serviceCall()      │                         │                        │
    │───────────────────▶│                         │                        │
    │                    │                         │                        │
    │                    │ [processing fails]      │                        │
    │                    │─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ▶│                        │
    │                    │                         │                        │
    │                    │ throw Exception         │                        │
    │                    │────────────────────────▶│                        │
    │                    │                         │                        │
    │                    │                         │ log(error)             │
    │                    │                         │───────────────────────▶│
    │                    │                         │                        │
    │                    │                         │                        │
    │      Error Response (code, message)          │                        │
    │◀─────────────────────────────────────────────│                        │
    │                    │                         │                        │
┌───┴────┐          ┌────┴────┐          ┌─────────┴─────────┐          ┌───┴────┐
│ Caller │          │ Service │          │ Exception Handler │          │ Logger │
└────────┘          └─────────┘          └───────────────────┘          └────────┘
```

<!-- ✅ NEW: Section 4.3 — Transaction Logging Pattern -->

### 4.3 Transaction Logging Pattern

_Captures how transaction logging to the data store occurs during request processing._

```
┌─────────┐          ┌────────────────────┐          ┌────────────────┐
│ Service │          │ Transaction Logger │          │ Log Data Store │
└────┬────┘          └─────────┬──────────┘          └───────┬────────┘
     │                         │                             │
     │ logTransaction(req,res) │                             │
     │────────────────────────▶│                             │
     │                         │                             │
     │                         │                             │
     │    ┌────────────────────┴──────────────────────────┐  │
     │    │ [alt] Log Store Available                     │  │
     │    └────────────────────┬──────────────────────────┘  │
     │                         │                             │
     │                         │ insert(transactionRecord)   │
     │                         │────────────────────────────▶│
     │                         │                             │
     │                         │       acknowledgment        │
     │                         │◀────────────────────────────│
     │                         │                             │
     │    logging success      │                             │
     │◀────────────────────────│                             │
     │                         │                             │
     │    ┌────────────────────┴──────────────────────────┐  │
     │    │ [else] Log Store Unavailable                  │  │
     │    │ NOTE: Document behavior - fail or continue?   │  │
     │    └────────────────────┬──────────────────────────┘  │
     │                         │                             │
     │                         │ handle failure (internal)   │
     │                         │─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─▶│
     │                         │                             │
     │  logging failure        │                             │
     │◀────────────────────────│                             │
     │                         │                             │
┌────┴────┐          ┌─────────┴──────────┐          ┌───────┴────────┐
│ Service │          │ Transaction Logger │          │ Log Data Store │
└─────────┘          └────────────────────┘          └────────────────┘
```

---

## 5. Interaction Summary Matrix

_Quick reference showing which components interact with each other._

| **Component ↓ / Calls →** | Controller | Service | Repository | External API | Message Queue |
|----------------------------|------------|---------|------------|--------------|---------------|
| **UI / Client**            | ✓          |         |            |              |               |
| **Controller**             |            | ✓       |            |              |               |
| **Service**                |            |         | ✓          | ✓            | ✓             |
| **Repository**             |            |         |            |              |               |

---

## 6. Notes & Observations

| **#** | **Observation**                                    | **Diagram(s)** | **Impact**         |
|-------|----------------------------------------------------|----------------|--------------------|
| 1     | _[Pattern or concern observed]_                    | _SD-XXX_       | _[Impact level]_   |

---

<!-- ✅ NEW: Section 7 — Dead Code Flow Paths -->

## 7. Dead Code Flow Paths

_This section consolidates all flow paths across sequence diagrams that have been identified as dead code._

| **Diagram ID** | **Step(s)**  | **Flow Path Description**                    | **Evidence**                                          | **Recommendation**                     |
|-----------------|--------------|----------------------------------------------|-------------------------------------------------------|----------------------------------------|
| _SD-XXX_        | _[Step #-#]_ | _[Description of the dead code flow path]_   | _[e.g., Feature flag always off, unreachable branch]_ | _Remove / Confirm with business team_  |

---

<!-- ✅ NEW: Section 8 — Target State Sequence Diagrams -->

## 8. Target State Sequence Diagrams (Proposed)

_This section contains proposed target-state sequence diagrams showing how the flow should look after migration._

### 8.1 SD-001-TARGET: _[Diagram Title]_ (Target State)

**Corresponding Current State:** SD-001
**Key Changes from Current State:**

| **Change #** | **Description of Change**                                    | **Reason**                     |
|--------------|--------------------------------------------------------------|--------------------------------|
| 1            | _[e.g., Added input validation step at entry point]_         | _Addresses GAP-XXX_           |
| 2            | _[e.g., Replaced hardcoded value with dynamic calculation]_  | _Addresses GAP-XXX_           |
| 3            | _[e.g., Added API Gateway as entry point]_                   | _New architecture requirement_ |
| 4            | _[e.g., Removed dead code branch]_                           | _Dead code cleanup_           |

#### Target State Diagram (ASCII)

```
┌──────┐          ┌──────────┐          ┌────────────┐          ┌─────────┐          ┌──────────┐
│ User │          │ UI Layer │          │ Controller │          │ Service │          │ Database │
└──┬───┘          └────┬─────┘          └─────┬──────┘          └────┬────┘          └────┬─────┘
   │                   │                      │                      │                    │
   │                   │                      │                      │                    │
   │  [Place the proposed target state sequence diagram here]       │                    │
   │                   │                      │                      │                    │
   │                   │                      │                      │                    │
┌──┴───┐          ┌────┴─────┐          ┌─────┴──────┐          ┌────┴────┐          ┌────┴─────┐
│ User │          │ UI Layer │          │ Controller │          │ Service │          │ Database │
└──────┘          └──────────┘          └────────────┘          └─────────┘          └──────────┘
```

> _Repeat for each sequence diagram that has significant changes in the target state._

---

## 9. Appendices

### Appendix A: ASCII Diagram Guidelines

_ASCII sequence diagrams are rendered directly in any text editor or Markdown viewer. Use the following conventions:_

| **Element**              | **ASCII Representation**                           |
|--------------------------|----------------------------------------------------|
| Participant box          | `┌──────┐` / `└──────┘`                            |
| Lifeline                 | `│` (vertical bar)                                 |
| Sync request (→)         | `───────────────────▶`                             |
| Sync response (←)        | `◀───────────────────`                             |
| Async request            | `─ ─ ─ ─ ─ ─ ─ ─ ─ ─▶`                             |
| Alt/Opt block            | `┌─────────────────┐` with `[alt]` or `[opt]` label|
| Self-call                | Arrow looping back to same lifeline                |

_Tools for creating ASCII diagrams: asciiflow.com, Monodraw (Mac), or manual text editing._

### Appendix B: Glossary

| **Term**     | **Definition**                                |
|--------------|-----------------------------------------------|
| _[Term]_     | _[Definition]_                                |

---

> **Document Control:**
> | Version | Date | Author | Changes |
> |---------|------|--------|---------|
> | 0.1 | _[Date]_ | _[Author]_ | Initial draft |
> | 0.2 | _[Date]_ | _[Author]_ | Added: Annotation notation (1), Step annotations column (3.x), Annotations summary per diagram (3.x), Transaction logging pattern (4.3), Dead code flow paths (7), Target state diagrams (8) |
> | 0.3 | _[Date]_ | _[Author]_ | Changed: Replaced all PlantUML diagrams with ASCII format for universal rendering |