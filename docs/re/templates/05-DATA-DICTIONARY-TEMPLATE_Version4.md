# Data Dictionary

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

_This data dictionary provides a comprehensive catalog of all data entities, attributes, data types, relationships, and constraints identified in the application's data layer during reverse engineering. It covers database tables, configuration stores, API payloads, and in-memory data structures._

---

## 2. Data Sources

| **Data Source**     | **Type**                       | **Technology**        | **Schema/Database** | **Notes**            |
|---------------------|--------------------------------|-----------------------|---------------------|----------------------|
| _[Source Name]_     | _RDBMS / NoSQL / File / Cache_ | _[e.g., PostgreSQL]_  | _[Schema name]_     | _[Notes]_            |

---

## 3. Entity Summary

| **Entity ID** | **Entity Name**     | **Data Source**   | **Type**                    | **Record Count** | **Description**         |
|----------------|---------------------|-------------------|-----------------------------|------------------|-------------------------|
| ENT-001        | _[Table/Entity]_    | _[Source Name]_   | _Table / View / Collection_ | _[Approx #]_    | _[Brief description]_   |

---

## 4. Entity Details

### 4.1 ENT-001: _[Entity/Table Name]_

| **Attribute**         | **Details**                                    |
|-----------------------|------------------------------------------------|
| **Entity ID**         | ENT-001                                        |
| **Entity Name**       | _[Table/Entity Name]_                          |
| **Schema**            | _[Schema Name]_                                |
| **Type**              | _Table / View / Materialized View / Collection_|
| **Description**       | _[Business purpose of this entity]_            |
| **Primary Key**       | _[Column(s)]_                                  |
| **Estimated Rows**    | _[Approximate count]_                          |
| **Source File/Class** | _[Corresponding ORM class or DAO]_             |

#### Attributes / Columns

<!-- ✅ NEW: Added "Usage Status", "Default/Hardcoded Value", and "Mapping Reference" columns -->

| **#** | **Column Name**     | **Data Type**    | **Length/Precision** | **Nullable** | **Default**   | **Constraints**       | **Usage Status**                             | **Default/Hardcoded Value**                      | **Mapping Reference**  | **Description**                |
|-------|---------------------|------------------|----------------------|--------------|---------------|-----------------------|----------------------------------------------|--------------------------------------------------|------------------------|--------------------------------|
| 1     | _[column_name]_     | _[VARCHAR/INT/…]_| _[50/10,2/…]_       | _Yes/No_     | _[Default]_   | _PK / FK / UQ / CHK_  | _Active / Logging Only / Dead / Deprecated_  | _[None / Hardcoded value — file:line ref]_       | _[F2F Doc Section ref]_ | _[Business meaning]_          |
| 2     | _[column_name]_     | _[Data Type]_    | _[Length]_           | _Yes/No_     | _[Default]_   | _[Constraints]_        | _Active / Logging Only / Dead / Deprecated_  | _[None / Hardcoded value]_                       | _[F2F Doc Section ref]_ | _[Business meaning]_          |
| 3     | _[column_name]_     | _[Data Type]_    | _[Length]_           | _Yes/No_     | _[Default]_   | _[Constraints]_        | _Active / Logging Only / Dead / Deprecated_  | _[None / Hardcoded value]_                       | _[F2F Doc Section ref]_ | _[Business meaning]_          |

#### Indexes

| **Index Name**          | **Columns**              | **Type**         | **Unique** | **Notes**            |
|-------------------------|--------------------------|------------------|------------|----------------------|
| _[idx_name]_            | _[col1, col2]_           | _B-Tree/Hash/…_  | _Yes/No_   | _[Purpose]_          |

#### Foreign Key Relationships

| **FK Name**         | **Column(s)**     | **References**              | **On Delete**                   | **On Update**          |
|---------------------|-------------------|-----------------------------|----------------------------------|------------------------|
| _[fk_name]_         | _[column]_        | _[parent_table(column)]_    | _CASCADE/SET NULL/RESTRICT_      | _CASCADE/NO ACTION_    |

#### Check Constraints

| **Constraint Name** | **Column(s)**     | **Expression**                     |
|---------------------|-------------------|------------------------------------|
| _[chk_name]_        | _[column]_        | _[e.g., status IN ('A','I','D')]_  |

#### Triggers

| **Trigger Name**    | **Event**              | **Timing**       | **Description**                  |
|---------------------|------------------------|------------------|----------------------------------|
| _[trg_name]_        | _INSERT/UPDATE/DELETE_  | _BEFORE/AFTER_   | _[What the trigger does]_        |

---

### 4.2 ENT-002: _[Entity/Table Name]_

_Repeat Section 4.1 structure for each entity._

---

## 5. Entity Relationship Summary

### 5.1 Relationship Matrix

| **Parent Entity** | **Child Entity**  | **Relationship**  | **Cardinality**    | **FK Column(s)**     |
|--------------------|-------------------|-------------------|--------------------|----------------------|
| _[Entity A]_       | _[Entity B]_      | _[Description]_   | _1:N / 1:1 / M:N_  | _[FK columns]_       |

### 5.2 ER Diagram (Text Representation)

```
┌──────────────┐    1    ┌──────────────┐
│  [Entity A]  │────────▶│  [Entity B]  │
│              │    N    │              │
│  PK: id      │         │  PK: id      │
│  name        │         │  FK: a_id    │
│  ...         │         │  ...         │
└──────────────┘         └──────────────┘
        │
        │ 1
        ▼ N
┌──────────────┐
│  [Entity C]  │
│              │
│  PK: id      │
│  FK: a_id    │
│  ...         │
└──────────────┘
```

---

## 6. Enumerated Values / Reference Data

<!-- ✅ NEW: Added "Used in Source" column -->

| **Entity/Column**           | **Code** | **Display Value**     | **Description**             | **Used in Source** | **Active** |
|-----------------------------|----------|-----------------------|-----------------------------|---------------------|------------|
| _[Entity.column]_           | _[Code]_ | _[Display text]_      | _[Meaning]_                 | _✅ Yes / ❌ No_    | _Yes/No_   |

---

## 7. API Data Structures

### 7.1 _[API/DTO Name]_

| **Attribute**        | **Details**                               |
|----------------------|-------------------------------------------|
| **Structure Name**   | _[DTO/Request/Response Name]_             |
| **Used In**          | _[API endpoint(s)]_                       |
| **Format**           | _JSON / XML / Protobuf_                   |

<!-- ✅ NEW: Added "Usage Status", "Default/Hardcoded Value", and "Mapping Reference" columns -->

| **Field Name**       | **Data Type**                    | **Required** | **Validation**           | **Usage Status**                            | **Default/Hardcoded Value**          | **Mapping Reference**   | **Description**          |
|----------------------|----------------------------------|--------------|--------------------------|---------------------------------------------|--------------------------------------|-------------------------|--------------------------|
| _[fieldName]_        | _String/Number/Boolean/Array/Object_ | _Yes/No_ | _[Validation rules]_     | _Active / Dead / Deprecated_                | _[None / Hardcoded value]_           | _[F2F Doc Section ref]_ | _[Description]_          |

#### Sample Payload

```json
{
  "fieldName1": "value",
  "fieldName2": 123,
  "nestedObject": {
    "nestedField": "value"
  }
}
```

---

## 8. Configuration Data

| **Key/Property**           | **Data Type**     | **Default Value**  | **Description**                    | **Source File**        |
|----------------------------|-------------------|--------------------|------------------------------------|------------------------|
| _[config.key.name]_        | _String/Int/Bool_ | _[Default]_        | _[What this config controls]_      | _[application.yml]_    |

---

## 9. Data Quality Observations

| **#** | **Entity**        | **Issue**                           | **Severity**   | **Recommendation**            |
|-------|-------------------|-------------------------------------|----------------|-------------------------------|
| 1     | _[Entity Name]_   | _[e.g., No FK constraint defined]_  | _High/Med/Low_ | _[Recommendation]_            |

---

<!-- ✅ NEW: Section 10 — Validation Rules Matrix -->

## 10. Validation Rules Matrix

_This section provides a consolidated view of all validation rules (present and missing) across every data field in the system._

### 10.1 Existing Validations

| **Entity / DTO**   | **Field Name**     | **Validation Type**                        | **Validation Logic**                    | **Source Location**       | **Notes**                |
|---------------------|--------------------|--------------------------------------------|------------------------------------------|---------------------------|--------------------------|
| _[Entity/DTO]_      | _[Field]_          | _Required / Format / Range / Length / Regex_ | _[e.g., Not null, Max 50 chars]_         | _[File:Line]_             | _[Notes]_                |

### 10.2 Missing Validations (Gaps)

| **Entity / DTO**   | **Field Name**     | **Expected Validation**                     | **Risk if Not Validated**               | **Severity**   | **Linked Gap** |
|---------------------|--------------------|--------------------------------------------|------------------------------------------|----------------|----------------|
| _[Entity/DTO]_      | _[Field]_          | _[e.g., Format check, range check]_        | _[e.g., Invalid data accepted, security risk]_ | _High/Med/Low_ | _GAP-XXX_  |

---

<!-- ✅ NEW: Section 11 — NoSQL / Document Store Field Catalog -->

## 11. NoSQL / Document Store Field Catalog

_This section specifically catalogs fields stored in NoSQL/document databases (e.g., MongoDB collections) used by the application._

### 11.1 Collection: _[Collection Name]_

| **Attribute**         | **Details**                                    |
|-----------------------|------------------------------------------------|
| **Collection Name**   | _[Collection Name]_                            |
| **Database**          | _[Database Name]_                              |
| **Purpose**           | _[e.g., Transaction logging, session store]_   |
| **Estimated Documents** | _[Approximate count]_                        |
| **TTL / Expiry**      | _[None / X days / X hours]_                    |

#### Document Fields

| **#** | **Field Path**       | **BSON Type**    | **Indexed** | **Required** | **Sample Value**          | **Description**              | **Mapping Reference**  |
|-------|----------------------|------------------|-------------|--------------|---------------------------|------------------------------|------------------------|
| 1     | _[field.path]_       | _String/ObjectId/Date/Object/Array_ | _Yes/No_ | _Yes/No_ | _[Sanitized sample]_  | _[Business meaning]_         | _[F2F Doc Section ref]_ |

#### Indexes

| **Index Name**      | **Fields**           | **Type**              | **Unique** | **TTL**   | **Notes**            |
|---------------------|----------------------|-----------------------|------------|-----------|----------------------|
| _[idx_name]_        | _[field1, field2]_   | _Single / Compound / Text / Geo_ | _Yes/No_ | _[Seconds / None]_ | _[Purpose]_ |

---

## 12. Appendices

### Appendix A: Data Type Mapping

| **Database Type**   | **Java Type**     | **API Type**      | **Notes**                  |
|---------------------|-------------------|-------------------|----------------------------|
| VARCHAR(n)          | String            | string            |                            |
| INTEGER             | Integer / int     | number            |                            |
| TIMESTAMP           | LocalDateTime     | string (ISO-8601) |                            |
| BOOLEAN             | Boolean / boolean | boolean           |                            |

### Appendix B: Glossary

| **Term**     | **Definition**                                |
|--------------|-----------------------------------------------|
| _[Term]_     | _[Definition]_                                |

<!-- ✅ NEW: Appendix C — Field Usage Status Definitions -->

### Appendix C: Field Usage Status Definitions

| **Status**       | **Definition**                                                          |
|------------------|-------------------------------------------------------------------------|
| **Active**       | Field is actively used in processing, business logic, and/or response   |
| **Logging Only** | Field is only written to the log/transaction data store                 |
| **Dead**         | Field exists in the model/DTO/schema but is never populated or consumed |
| **Deprecated**   | Field is marked or planned for removal                                  |

---

> **Document Control:**
> | Version | Date | Author | Changes |
> |---------|------|--------|---------|
> | 0.1 | _[Date]_ | _[Author]_ | Initial draft |
> | 0.2 | _[Date]_ | _[Author]_ | Added: Usage status, default/hardcoded, mapping ref columns (4.1, 7.1), Enum usage check (6), Validation rules matrix (10), NoSQL field catalog (11), Field usage definitions (Appendix C) |