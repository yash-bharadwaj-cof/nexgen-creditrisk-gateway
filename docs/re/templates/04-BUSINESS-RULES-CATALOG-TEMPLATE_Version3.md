# Business Rules Catalog

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

_This catalog documents all business rules identified during reverse engineering of the application. Business rules define the constraints, computations, validations, and decision logic that govern business operations within the system._

### Rule Classification

| **Category**       | **Description**                                                    |
|--------------------|--------------------------------------------------------------------|
| **Validation**     | Input validation, data format checks, required field enforcement    |
| **Computation**    | Calculations, formulas, derived values                              |
| **Decision**       | Conditional logic, branching, routing decisions                     |
| **Authorization**  | Access control, permission checks, role-based rules                 |
| **Workflow**       | Process flow, state transitions, sequencing rules                   |
| **Constraint**     | Data integrity constraints, business limits, thresholds             |
| **Derivation**     | Rules that derive or infer data from other data                     |
| **Notification**   | Trigger conditions for alerts, emails, or notifications             |

---

## 2. Business Rules Summary

| **Category**       | **Count** | **Critical** | **Major** | **Minor** |
|--------------------|-----------|--------------|-----------|-----------|
| Validation         | _[#]_     | _[#]_        | _[#]_     | _[#]_     |
| Computation        | _[#]_     | _[#]_        | _[#]_     | _[#]_     |
| Decision           | _[#]_     | _[#]_        | _[#]_     | _[#]_     |
| Authorization      | _[#]_     | _[#]_        | _[#]_     | _[#]_     |
| Workflow           | _[#]_     | _[#]_        | _[#]_     | _[#]_     |
| Constraint         | _[#]_     | _[#]_        | _[#]_     | _[#]_     |
| Derivation         | _[#]_     | _[#]_        | _[#]_     | _[#]_     |
| Notification       | _[#]_     | _[#]_        | _[#]_     | _[#]_     |
| **Total**          | _[#]_     | _[#]_        | _[#]_     | _[#]_     |

<!-- ✅ NEW: Section 2.1 — Rule Health Summary -->

### 2.1 Rule Health Summary

| **Status**                            | **Count** | **Percentage** |
|---------------------------------------|-----------|----------------|
| ✅ Active & Validated by Client        | _[#]_     | _[%]_          |
| 🟡 Active & Pending Client Validation | _[#]_     | _[%]_          |
| ❌ Client Identified as Incorrect      | _[#]_     | _[%]_          |
| ⚫ Dead Code Rule                      | _[#]_     | _[%]_          |
| ⚪ Hardcoded / Bypassed Rule           | _[#]_     | _[%]_          |

---

## 3. Business Rules Detail

### 3.1 BR-001: _[Rule Name]_

<!-- ✅ NEW: Added "Source Line(s)", "Client Validated", "Client Comments", "Dead Code Flag", "Hardcoded Flag", "Execution Priority" attributes -->

| **Attribute**            | **Details**                                              |
|--------------------------|----------------------------------------------------------|
| **Rule ID**              | BR-001                                                   |
| **Rule Name**            | _[Descriptive name]_                                     |
| **Category**             | _Validation / Computation / Decision / Authorization / Workflow / Constraint / Derivation / Notification_ |
| **Severity**             | _Critical / Major / Minor_                               |
| **Business Domain**      | _[e.g., Order Processing, User Management]_              |
| **Description**          | _[Plain English description of the business rule]_       |
| **Source Location**      | _[File path, class, method where rule is implemented]_   |
| **Source Line(s)**       | _[e.g., L85-L102]_                                      |
| **Trigger/Condition**    | _[When/What triggers this rule]_                         |
| **Input(s)**             | _[Data inputs the rule evaluates]_                       |
| **Output(s)/Action**     | _[Result or action taken when rule executes]_            |
| **Exception Handling**   | _[What happens if the rule fails or exception occurs]_   |
| **Client Validated**     | _✅ Yes / ❌ No / 🟡 Pending_                            |
| **Client Comments**      | _[Any feedback or corrections from client]_              |
| **Dead Code Flag**       | _Yes / No — [If Yes, explain why it's dead code]_        |
| **Hardcoded Flag**       | _Yes / No — [If Yes, specify the hardcoded value]_       |
| **Execution Priority**   | _[Execution order if multiple rules apply to same input]_|

#### Rule Logic (Pseudocode)

```
IF [condition1] AND [condition2] THEN
    [action/result]
ELSE IF [alternative condition] THEN
    [alternative action/result]
ELSE
    [default action/result]
END IF
```

#### Rule Logic (Source Code Reference)

```java
// File: [path/to/file.java]
// Method: [methodName()]
// Lines: [start-end]
[Relevant code snippet demonstrating the rule implementation]
```

#### Related Entities

| **Entity**           | **Relationship**                              |
|----------------------|-----------------------------------------------|
| _[Table/Class/API]_  | _[How this entity is involved in the rule]_   |

#### Traceability

<!-- ✅ NEW: Added "Gap Report Item" and "Field-to-Field Mapping" rows -->

| **Linked To**             | **Reference**                                 |
|---------------------------|-----------------------------------------------|
| Use Case                  | _[UC-XXX]_                                    |
| BDD Scenario              | _[Feature/Scenario reference]_                |
| Test Case                 | _[TC-XXX]_                                    |
| Component                 | _[COMP-XXX]_                                  |
| Gap Report Item           | _[GAP-XXX (if applicable)]_                   |
| Field-to-Field Mapping    | _[F2F Section reference (if applicable)]_     |

---

### 3.2 BR-002: _[Rule Name]_

_Repeat Section 3.1 structure for each business rule._

---

## 4. Validation Rules

_Subset of rules specifically related to input/data validation._

<!-- ✅ NEW: Added "Validation Exists in Source" column -->

| **Rule ID** | **Rule Name**        | **Field/Entity**  | **Validation Logic**              | **Validation Exists in Source** | **Error Message**          |
|-------------|----------------------|-------------------|-----------------------------------|---------------------------------|----------------------------|
| BR-XXX      | _[Name]_             | _[Field]_         | _[e.g., Required, Max length 50]_ | _✅ Yes / ❌ No (Gap)_          | _[Error message shown]_    |
| BR-XXX      | _[Name]_             | _[Field]_         | _[e.g., Must be >= 0]_           | _✅ Yes / ❌ No (Gap)_          | _[Error message shown]_    |
| BR-XXX      | _[Name]_             | _[Field]_         | _[e.g., Valid email format]_      | _✅ Yes / ❌ No (Gap)_          | _[Error message shown]_    |

---

## 5. Computation Rules

_Subset of rules involving calculations or formulas._

<!-- ✅ NEW: Added "Hardcoded Override" column -->

| **Rule ID** | **Rule Name**   | **Formula/Logic**                           | **Inputs**          | **Output**  | **Hardcoded Override**                          |
|-------------|-----------------|---------------------------------------------|---------------------|-------------|-------------------------------------------------|
| BR-XXX      | _[Name]_        | _[e.g., total = qty × price × (1 - disc)]_ | _qty, price, disc_  | _total_     | _None / [Hardcoded value that overrides this]_  |

---

## 6. Decision Rules (Decision Tables)

_Complex decision logic presented as decision tables._

### 6.1 Decision Table: _[Decision Name]_

**Rule ID:** BR-XXX
**Description:** _[What decision this table represents]_

| **Condition 1** | **Condition 2** | **Condition 3** | **Action/Result**         |
|-----------------|-----------------|-----------------|---------------------------|
| Yes             | Yes             | Any             | _[Action A]_              |
| Yes             | No              | Yes             | _[Action B]_              |
| Yes             | No              | No              | _[Action C]_              |
| No              | Any             | Any             | _[Action D]_              |

---

## 7. Workflow / State Transition Rules

### 7.1 State Machine: _[Entity Name]_

**Related Rule IDs:** BR-XXX, BR-XXX

| **Current State** | **Event/Trigger**       | **Condition(s)**        | **Next State**     | **Action(s)**            |
|--------------------|-------------------------|-------------------------|--------------------|--------------------------|
| _[State A]_        | _[Event]_               | _[Guard condition]_     | _[State B]_        | _[Side effects]_         |
| _[State B]_        | _[Event]_               | _[Guard condition]_     | _[State C]_        | _[Side effects]_         |

```
[State A] ──[Event/Condition]──▶ [State B] ──[Event/Condition]──▶ [State C]
     │                                │
     ▼                                ▼
 [State D]                        [State E]
```

---

## 8. Authorization Rules

| **Rule ID** | **Resource/Action**     | **Required Role(s)**    | **Additional Conditions**     |
|-------------|-------------------------|-------------------------|-------------------------------|
| BR-XXX      | _[e.g., Delete Order]_  | _[e.g., Admin, Manager]_| _[e.g., Only if status=Draft]_|

---

## 9. Unresolved / Ambiguous Rules

_Rules that could not be fully determined during reverse engineering._

| **Rule ID** | **Description**                     | **Ambiguity**                        | **Recommended Action**        |
|-------------|-------------------------------------|--------------------------------------|-------------------------------|
| BR-XXX      | _[Partial rule description]_        | _[What is unclear or conflicting]_   | _[SME review / Testing]_     |

---

## 10. Notes & Observations

| **#** | **Observation**                                    | **Related Rules** | **Impact**       |
|-------|----------------------------------------------------|-------------------|------------------|
| 1     | _[Observation about rule patterns or concerns]_    | _BR-XXX_          | _[Impact level]_ |

---

<!-- ✅ NEW: Section 11 — Potentially Missing Business Rules -->

## 11. Potentially Missing Business Rules

_Business rules expected based on the application's domain but NOT found in source code._

| **#** | **Expected Rule Description**    | **Business Domain** | **Expected Category**                     | **Found in Code?** | **Evidence of Absence**                                           | **Severity**                   | **Recommended Action**                    | **Linked Gap** |
|-------|----------------------------------|---------------------|-------------------------------------------|--------------------|-------------------------------------------------------------------|--------------------------------|-------------------------------------------|----------------|
| 1     | _[Description of expected rule]_ | _[Domain]_          | _Validation / Computation / Decision / etc._ | _❌ No_          | _[e.g., No validation found in any controller/processor]_         | _Critical / High / Medium / Low_ | _[Implement in target / Confirm with client]_ | _GAP-XXX_  |

---

<!-- ✅ NEW: Section 12 — Rule Execution Order & Conflicts -->

## 12. Rule Execution Order & Conflicts

_When multiple rules can apply to the same input, this section documents execution order and conflicts._

### 12.1 Rule Execution Chains

| **Trigger / Input Context**      | **Rule Execution Order**             | **Notes**                               |
|----------------------------------|--------------------------------------|-----------------------------------------|
| _[e.g., Incoming score request]_ | _BR-001 → BR-003 → BR-007 → BR-012_ | _[Sequential / Parallel / Conditional]_ |

### 12.2 Rule Conflicts

| **Rule A** | **Rule B** | **Conflict Description**                                  | **Resolution**                                     | **Status**        |
|------------|------------|-----------------------------------------------------------|----------------------------------------------------|-------------------|
| _BR-XXX_   | _BR-XXX_   | _[e.g., Both rules attempt to set the same output field]_ | _[e.g., Rule A takes priority per business logic]_ | _Resolved / Open_ |

---

## 13. Appendices

### Appendix A: Rule ID Naming Convention

_Format: BR-[NNN] where NNN is a sequential number._

### Appendix B: Glossary

| **Term**     | **Definition**                                |
|--------------|-----------------------------------------------|
| _[Term]_     | _[Definition]_                                |

---

> **Document Control:**
> | Version | Date | Author | Changes |
> |---------|------|--------|---------|
> | 0.1 | _[Date]_ | _[Author]_ | Initial draft |
> | 0.2 | _[Date]_ | _[Author]_ | Added: Rule health summary (2.1), Client validation/dead code/hardcoded flags (3.1), Source line refs (3.1), Execution priority (3.1), Validation existence check (4), Hardcoded override (5), Missing business rules (11), Rule execution order & conflicts (12) |