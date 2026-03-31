# Gap Report

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

## 1. Executive Summary

_Provide a high-level overview of the gap analysis findings. Summarize the total number of gaps identified, their severity distribution, and the overall risk to the modernization/migration effort._

> **Total Gaps Identified:** _[Number]_
>
> **Critical:** _[Number]_ | **High:** _[Number]_ | **Medium:** _[Number]_ | **Low:** _[Number]_
>
> **Overall Risk Assessment:** _[High / Medium / Low]_
>
> **Key Finding:** _[One-sentence summary of the most important finding]_

---

## 2. Scope & Methodology

### 2.1 Analysis Scope

_Define what was compared and what was included/excluded from the gap analysis._

| **Dimension**              | **Details**                                     |
|----------------------------|-------------------------------------------------|
| Source System               | _[Current/legacy system description]_           |
| Target System               | _[Target/modernized system description]_        |
| Modules Analyzed            | _[List of modules in scope]_                    |
| Modules Excluded            | _[List of modules out of scope and why]_        |
| Analysis Period             | _[Date range]_                                  |

### 2.2 Gap Categories

| **Category**               | **Description**                                                  |
|----------------------------|------------------------------------------------------------------|
| **Functional**             | Missing or incomplete business functionality                      |
| **Technical**              | Technology, architecture, or infrastructure gaps                  |
| **Data**                   | Data model, migration, or integration data gaps                   |
| **Integration**            | Missing or incompatible integration points                        |
| **Security**               | Security controls, compliance, or vulnerability gaps              |
| **Performance**            | Performance, scalability, or reliability gaps                     |
| **Testing**                | Test coverage, automation, or environment gaps                    |
| **Documentation**          | Missing or outdated documentation                                 |
| **Process/Operational**    | Operational procedures, monitoring, or support gaps               |

<!-- ✅ NEW: Added "Dead Code" and "Hardcoded Values" categories -->

| **Dead Code**              | Dead code paths, unused components, or unreachable branches       |
| **Hardcoded Values**       | Hardcoded/static values that bypass dynamic business logic        |

### 2.3 Severity Definitions

| **Severity** | **Definition**                                                              | **Action Required**        |
|--------------|-----------------------------------------------------------------------------|----------------------------|
| **Critical** | Blocks migration/go-live; no workaround available                           | Must resolve before go-live|
| **High**     | Significant impact on functionality or operations; workaround may exist     | Resolve before go-live     |
| **Medium**   | Moderate impact; workaround available                                       | Plan resolution post-go-live if needed |
| **Low**      | Minor impact; cosmetic or nice-to-have                                      | Address in future iteration|

---

## 3. Gap Summary Dashboard

### 3.1 Gaps by Category

<!-- ✅ NEW: Added "Dead Code" and "Hardcoded Values" rows -->

| **Category**       | **Critical** | **High** | **Medium** | **Low** | **Total** |
|--------------------|--------------|----------|------------|---------|-----------|
| Functional         | _[#]_        | _[#]_    | _[#]_      | _[#]_   | _[#]_     |
| Technical          | _[#]_        | _[#]_    | _[#]_      | _[#]_   | _[#]_     |
| Data               | _[#]_        | _[#]_    | _[#]_      | _[#]_   | _[#]_     |
| Integration        | _[#]_        | _[#]_    | _[#]_      | _[#]_   | _[#]_     |
| Security           | _[#]_        | _[#]_    | _[#]_      | _[#]_   | _[#]_     |
| Performance        | _[#]_        | _[#]_    | _[#]_      | _[#]_   | _[#]_     |
| Testing            | _[#]_        | _[#]_    | _[#]_      | _[#]_   | _[#]_     |
| Documentation      | _[#]_        | _[#]_    | _[#]_      | _[#]_   | _[#]_     |
| Process/Operational| _[#]_        | _[#]_    | _[#]_      | _[#]_   | _[#]_     |
| Dead Code          | _[#]_        | _[#]_    | _[#]_      | _[#]_   | _[#]_     |
| Hardcoded Values   | _[#]_        | _[#]_    | _[#]_      | _[#]_   | _[#]_     |
| **Total**          | _[#]_        | _[#]_    | _[#]_      | _[#]_   | **[#]**   |

### 3.2 Gaps by Module/Component

| **Module/Component** | **Critical** | **High** | **Medium** | **Low** | **Total** |
|----------------------|--------------|----------|------------|---------|-----------|
| _[Module Name]_      | _[#]_        | _[#]_    | _[#]_      | _[#]_   | _[#]_     |

### 3.3 Resolution Status

<!-- ✅ NEW: Added "Assigned To" column -->

| **Status**              | **Count** | **Percentage** | **Assigned To**       |
|-------------------------|-----------|----------------|------------------------|
| Open                    | _[#]_     | _[%]_          | _[Person/Team list]_   |
| In Progress             | _[#]_     | _[%]_          | _[Person/Team list]_   |
| Resolved                | _[#]_     | _[%]_          | _[Person/Team list]_   |
| Accepted (Won't Fix)    | _[#]_     | _[%]_          | _[Person/Team list]_   |
| Deferred                | _[#]_     | _[%]_          | _[Person/Team list]_   |

---

## 4. Detailed Gap Analysis

### 4.1 GAP-001: _[Gap Title]_

<!-- ✅ NEW: Added "Assigned To", "Target Resolution Date", "Source Code Reference", "Resolution Notes" attributes -->

| **Attribute**             | **Details**                                              |
|---------------------------|----------------------------------------------------------|
| **Gap ID**                | GAP-001                                                  |
| **Gap Title**             | _[Short descriptive title]_                              |
| **Category**              | _Functional / Technical / Data / Integration / Security / Performance / Testing / Documentation / Process / Dead Code / Hardcoded Values_ |
| **Severity**              | _Critical / High / Medium / Low_                         |
| **Status**                | _Open / In Progress / Resolved / Accepted / Deferred_    |
| **Module/Component**      | _[COMP-XXX / Module Name]_                               |
| **Source Code Reference** | _[File path : Line numbers]_                             |
| **Discovered During**     | _[Which analysis phase — e.g., Code Review, Data Analysis, Client Review]_ |
| **Discovered By**         | _[Person who identified the gap]_                        |
| **Assigned To**           | _[Person/Team responsible for resolution]_               |
| **Target Resolution Date**| _[Date]_                                                 |
| **Resolution Notes**      | _[How it was resolved, or current progress]_             |

#### Current State (As-Is)

_[Describe the current system's behavior, capability, or implementation]_

#### Expected/Target State (To-Be)

_[Describe the desired behavior, capability, or implementation in the target system]_

#### Gap Description

_[Detailed description of what is missing, different, or incompatible between current and target states]_

#### Impact Analysis

| **Impact Area**       | **Description**                                          |
|-----------------------|----------------------------------------------------------|
| Business Impact       | _[How this gap affects business operations]_             |
| Technical Impact      | _[How this gap affects the technical architecture]_      |
| User Impact           | _[How end-users are affected]_                           |
| Migration Impact      | _[How this gap affects the migration timeline/approach]_ |

#### Root Cause

_[Why this gap exists — e.g., technology difference, missing feature, design decision]_

#### Recommended Resolution

| **Option** | **Description**                    | **Effort**      | **Risk**        | **Recommended** |
|------------|------------------------------------|-----------------|-----------------|-----------------|
| Option A   | _[Resolution approach A]_          | _[S/M/L/XL]_   | _[Low/Med/High]_| _Yes / No_      |
| Option B   | _[Resolution approach B]_          | _[S/M/L/XL]_   | _[Low/Med/High]_| _Yes / No_      |

#### Workaround

_[If a temporary workaround exists, describe it here. If none, state "No workaround available."]_

#### Related Artifacts

| **Artifact Type**     | **Reference**                     |
|-----------------------|-----------------------------------|
| Business Rule         | _BR-XXX_                          |
| Feature Spec          | _FEAT-XXX_                        |
| Test Case             | _TC-XXX_                          |
| Component             | _COMP-XXX_                        |
| Data Entity           | _ENT-XXX_                         |
| Field-to-Field Mapping| _F2F Section X, Row Y_            |

---

### 4.2 GAP-002: _[Gap Title]_

_Repeat Section 4.1 structure for each gap._

---

### 4.3 GAP-003: _[Gap Title]_

_Repeat Section 4.1 structure for each gap._

---

## 5. Functional Gaps

_Summary of all functional gaps — missing or incomplete business capabilities._

| **Gap ID** | **Title**                    | **Severity** | **Module**   | **Resolution**           | **Assigned To** | **Status**  |
|------------|------------------------------|--------------|--------------|--------------------------|-----------------|-------------|
| GAP-XXX    | _[Missing feature/function]_ | _[Severity]_ | _[Module]_   | _[Recommended approach]_ | _[Person]_      | _[Status]_  |

---

## 6. Technical Gaps

_Summary of technology, architecture, and infrastructure gaps._

| **Gap ID** | **Title**                      | **Severity** | **Area**             | **Resolution**           | **Assigned To** | **Status**  |
|------------|--------------------------------|--------------|----------------------|--------------------------|-----------------|-------------|
| GAP-XXX    | _[Technology incompatibility]_ | _[Severity]_ | _[Architecture/Infra]_| _[Recommended approach]_| _[Person]_      | _[Status]_  |

---

## 7. Data Gaps

_Summary of data model, data migration, and data quality gaps._

| **Gap ID** | **Title**              | **Severity** | **Entity/Table** | **Resolution**           | **Assigned To** | **Status**  |
|------------|------------------------|--------------|------------------|--------------------------|-----------------|-------------|
| GAP-XXX    | _[Data mapping issue]_ | _[Severity]_ | _[Entity]_       | _[Recommended approach]_ | _[Person]_      | _[Status]_  |

---

## 8. Integration Gaps

_Summary of missing or incompatible integration points._

| **Gap ID** | **Title**                       | **Severity** | **Integration**  | **Resolution**           | **Assigned To** | **Status**  |
|------------|---------------------------------|--------------|------------------|--------------------------|-----------------|-------------|
| GAP-XXX    | _[Integration incompatibility]_ | _[Severity]_ | _[System/API]_   | _[Recommended approach]_ | _[Person]_      | _[Status]_  |

---

## 9. Security Gaps

_Summary of security controls and compliance gaps._

| **Gap ID** | **Title**                    | **Severity** | **Security Area**      | **Resolution**           | **Assigned To** | **Status**  |
|------------|------------------------------|--------------|------------------------|--------------------------|-----------------|-------------|
| GAP-XXX    | _[Security control missing]_ | _[Severity]_ | _[Auth/Encryption/etc]_| _[Recommended approach]_ | _[Person]_      | _[Status]_  |

---

## 10. Testing Gaps

_Summary of test coverage, automation, and environment gaps._

| **Gap ID** | **Title**                  | **Severity** | **Test Area** | **Resolution**           | **Assigned To** | **Status**  |
|------------|----------------------------|--------------|---------------|--------------------------|-----------------|-------------|
| GAP-XXX    | _[Missing test coverage]_  | _[Severity]_ | _[Area]_      | _[Recommended approach]_ | _[Person]_      | _[Status]_  |

---

<!-- ✅ NEW: Section 11 — Dead Code Gaps -->

## 11. Dead Code Gaps

_Summary of all dead code paths, unused components, and unreachable branches identified during reverse engineering._

| **Gap ID** | **Title**                          | **Severity** | **Component**  | **Source Location**       | **Evidence**                                                  | **Risk if Not Removed**                            | **Resolution**         | **Assigned To** | **Status**  |
|------------|------------------------------------|--------------|----------------|---------------------------|---------------------------------------------------------------|-----------------------------------------------------|------------------------|-----------------|-------------|
| GAP-DC-XXX | _[Dead code description]_         | _[Severity]_ | _[COMP-XXX]_   | _[File:Line]_             | _[e.g., Unreachable branch, never invoked, feature flag off]_ | _[e.g., Confusion during migration, wasted effort]_ | _Remove / Confirm_     | _[Person]_      | _[Status]_  |

---

<!-- ✅ NEW: Section 12 — Hardcoded Value Gaps -->

## 12. Hardcoded Value Gaps

_Summary of all hardcoded/static values that bypass dynamic business logic._

| **Gap ID** | **Title**                            | **Severity** | **Component**  | **Field**       | **Hardcoded Value** | **Source Location** | **Expected Dynamic Behavior**                       | **Resolution**                          | **Assigned To** | **Status**  |
|------------|--------------------------------------|--------------|----------------|-----------------|---------------------|---------------------|------------------------------------------------------|-----------------------------------------|-----------------|-------------|
| GAP-HC-XXX | _[Hardcoded value description]_     | _[Severity]_ | _[COMP-XXX]_   | _[Field name]_  | _[Static value]_    | _[File:Line]_       | _[Should be calculated by BR-XXX / read from config]_| _Implement dynamic logic / Externalize_ | _[Person]_      | _[Status]_  |

---

<!-- ✅ NEW: Section 13 — Missing Business Logic Gaps -->

## 13. Missing Business Logic Gaps

_Gaps where expected business logic was NOT found in the source code. These were identified by cross-referencing the Business Rules Catalog (Doc-04, Section 11) and client feedback._

| **Gap ID** | **Title**                              | **Severity** | **Expected Rule**                        | **Business Domain**  | **Identified By**               | **Resolution**                          | **Assigned To** | **Status**  |
|------------|----------------------------------------|--------------|------------------------------------------|----------------------|---------------------------------|-----------------------------------------|-----------------|-------------|
| GAP-BL-XXX | _[Missing logic description]_         | _[Severity]_ | _[Description of expected business rule]_| _[Domain]_           | _[RE analysis / Client review]_ | _[Implement in target / Confirm with client]_ | _[Person]_ | _[Status]_  |

---

<!-- ✅ NEW: Section 14 — Client-Raised Gaps -->

## 14. Client-Raised Gaps

_Gaps identified by the client or stakeholders during review sessions, separate from gaps found during reverse engineering._

| **Gap ID** | **Title**                  | **Severity** | **Raised By**     | **Date Raised** | **Category**                                  | **Description**                          | **Resolution**           | **Assigned To** | **Status**  |
|------------|----------------------------|--------------|-------------------|-----------------|-----------------------------------------------|------------------------------------------|--------------------------|-----------------|-------------|
| GAP-CL-XXX | _[Client-raised gap title]_| _[Severity]_ | _[Person name]_   | _[Date]_        | _Functional / Technical / Data / Other_       | _[Detailed description from client]_     | _[Recommended approach]_ | _[Person]_      | _[Status]_  |

---

## 15. Resolution Roadmap

### 15.1 Priority Matrix

| **Priority** | **Gap IDs**                          | **Target Phase**            | **Effort Estimate** |
|--------------|--------------------------------------|-----------------------------|---------------------|
| Must Have    | _GAP-001, GAP-003, ..._             | _Pre-Migration_             | _[X person-days]_   |
| Should Have  | _GAP-002, GAP-005, ..._             | _During Migration_          | _[X person-days]_   |
| Could Have   | _GAP-004, GAP-006, ..._             | _Post-Migration Phase 1_    | _[X person-days]_   |
| Won't Have   | _GAP-007, ..._                       | _Future / Accepted Risk_    | _N/A_               |

### 15.2 Resolution Timeline

| **Phase**            | **Gap IDs**               | **Start Date** | **End Date**   | **Owner**     |
|----------------------|---------------------------|-----------------|----------------|---------------|
| Pre-Migration        | _GAP-XXX, GAP-XXX_       | _[Date]_        | _[Date]_       | _[Team]_      |
| Migration Sprint 1   | _GAP-XXX, GAP-XXX_       | _[Date]_        | _[Date]_       | _[Team]_      |
| Migration Sprint 2   | _GAP-XXX, GAP-XXX_       | _[Date]_        | _[Date]_       | _[Team]_      |
| Post-Migration       | _GAP-XXX, GAP-XXX_       | _[Date]_        | _[Date]_       | _[Team]_      |

### 15.3 Effort Summary

| **Category**         | **Critical/High Gaps** | **Estimated Effort**   | **Resources Needed**   |
|----------------------|------------------------|------------------------|------------------------|
| Functional           | _[#]_                  | _[X person-days]_      | _[# developers]_       |
| Technical            | _[#]_                  | _[X person-days]_      | _[# developers]_       |
| Data                 | _[#]_                  | _[X person-days]_      | _[# developers]_       |
| Integration          | _[#]_                  | _[X person-days]_      | _[# developers]_       |
| Security             | _[#]_                  | _[X person-days]_      | _[# developers]_       |
| Dead Code            | _[#]_                  | _[X person-days]_      | _[# developers]_       |
| Hardcoded Values     | _[#]_                  | _[X person-days]_      | _[# developers]_       |
| Missing Business Logic | _[#]_                | _[X person-days]_      | _[# developers]_       |
| Client-Raised        | _[#]_                  | _[X person-days]_      | _[# developers]_       |
| **Total**            | _[#]_                  | _[X person-days]_      | _[# developers]_       |

---

## 16. Risk Assessment

| **#** | **Risk**                                      | **Related Gap(s)** | **Probability** | **Impact**     | **Mitigation**                     |
|-------|-----------------------------------------------|--------------------|-----------------|----------------|------------------------------------|
| 1     | _[Risk description]_                          | _GAP-XXX_          | _High/Med/Low_  | _High/Med/Low_ | _[Mitigation strategy]_            |

---

## 17. Recommendations

| **#** | **Recommendation**                             | **Priority** | **Related Gap(s)** | **Rationale**                      |
|-------|------------------------------------------------|--------------|--------------------|------------------------------------|
| 1     | _[Recommendation]_                             | _High_       | _GAP-XXX_          | _[Why this is recommended]_        |

---

## 18. Appendices

### Appendix A: Gap ID Naming Convention

_Format: GAP-[NNN] where NNN is a sequential number._

<!-- ✅ NEW: Extended naming convention for new gap categories -->

_Extended Gap ID Formats:_
- `GAP-[NNN]` ��� General gaps
- `GAP-DC-[NNN]` — Dead Code gaps
- `GAP-HC-[NNN]` — Hardcoded Value gaps
- `GAP-BL-[NNN]` — Missing Business Logic gaps
- `GAP-CL-[NNN]` — Client-Raised gaps

### Appendix B: Comparison with Discovery Report

_Cross-reference key discovery findings with identified gaps._

| **Discovery Finding**        | **Related Gap(s)**    | **Status**     |
|------------------------------|-----------------------|----------------|
| _[Finding from Doc 01]_      | _GAP-XXX_             | _[Status]_     |

<!-- ✅ NEW: Appendix C — Cross-Reference to All RE Artifacts -->

### Appendix C: Cross-Reference to All RE Artifacts

| **Artifact**                    | **Document #** | **How Gaps Were Derived**                                           |
|---------------------------------|----------------|---------------------------------------------------------------------|
| Discovery Report                | Doc-01         | Technology risks, integration findings, security assessment          |
| Component Catalog               | Doc-02         | Dead code components, deprecated libraries, version risks           |
| Sequence Diagrams               | Doc-03         | Hardcoded steps, missing validation points, dead flow paths          |
| Business Rules Catalog          | Doc-04         | Missing rules, bypassed rules, hardcoded rules                       |
| Data Dictionary                 | Doc-05         | Missing validations, dead fields, schema inconsistencies             |
| Data Flow Diagram               | Doc-06         | Dead data flows, missing sensitivity handling, hardcoded transforms  |
| BDD Feature Specs               | Doc-07         | Gap-exposing scenarios, uncovered business rules                     |
| Test Case Inventory             | Doc-08         | Uncovered test areas, missing migration tests                        |
| Field-to-Field Mapping          | Doc-10         | Unmapped fields, new target fields, transformation gaps              |

### Appendix D: Glossary

| **Term**     | **Definition**                                |
|--------------|-----------------------------------------------|
| _[Term]_     | _[Definition]_                                |

---

> **Document Control:**
> | Version | Date | Author | Changes |
> |---------|------|--------|---------|
> | 0.1 | _[Date]_ | _[Author]_ | Initial draft |
> | 0.2 | _[Date]_ | _[Author]_ | Added: Dead Code & Hardcoded Values gap categories (2.2, 3.1), Resolution tracking fields (3.3, 4.1), Dead code gaps section (11), Hardcoded value gaps section (12), Missing business logic gaps (13), Client-raised gaps (14), Extended effort summary (15.3), Extended gap naming convention (Appendix A), Cross-reference to all RE artifacts (Appendix C) |