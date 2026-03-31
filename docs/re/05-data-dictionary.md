# Data Dictionary

---

| **Field**            | **Details**                                    |
|----------------------|------------------------------------------------|
| **Project Name**     | NexGen ESB Credit Risk Gateway                 |
| **Application Name** | nexgen-creditrisk-gateway                      |
| **Version**          | 1.0.0                                          |
| **Date**             | 31-Mar-2026                                    |
| **Prepared By**      | Copilot Reverse Engineering Agent              |
| **Reviewed By**      | —                                              |
| **Status**           | Draft                                          |

---

## 1. Overview

This data dictionary provides a comprehensive catalog of all data entities, attributes, data types, relationships, and constraints identified in the `nexgen-creditrisk-gateway` JBoss Fuse / Apache Camel service. The application is a credit risk assessment gateway that receives requests via REST or SOAP, calls an external credit bureau SOAP service, applies configurable scoring strategies, and returns a structured risk assessment response. Transaction data is persisted to MongoDB via a Wire Tap logging pattern.

The dictionary covers:
- **In-memory Java model classes** (request/response POJOs and scoring sub-models)
- **Generated JAXB/JAX-WS classes** from the Bureau WSDL
- **Enumerated types** for province codes and request channels
- **REST/SOAP API data structures**
- **MongoDB transaction log collection**
- **External configuration properties**

---

## 2. Data Sources

| **Data Source**          | **Type**            | **Technology**                     | **Schema/Database**        | **Notes**                                           |
|--------------------------|---------------------|------------------------------------|----------------------------|-----------------------------------------------------|
| MongoDB                  | NoSQL (Document)    | mongo-java-driver 3.4.2            | nexgen_creditrisk           | Wire Tap transaction logging; collection: transactions |
| In-Memory (Java Objects) | Java POJOs          | JVM heap / Apache Camel Exchange   | N/A                        | Request/response processing; no persistence         |
| External Config File     | Properties File     | OSGi Blueprint / Camel property placeholder | N/A           | `app_config.properties`; 17 externalized properties |
| External Bureau Service  | SOAP Web Service    | Apache CXF / JAX-WS                | N/A                        | Outbound call to credit bureau; no local schema     |

---

## 3. Entity Summary

| **Entity ID** | **Entity Name**             | **Data Source**   | **Type**              | **Record Count** | **Description**                                              |
|---------------|-----------------------------|-------------------|-----------------------|------------------|--------------------------------------------------------------|
| ENT-001       | CreditRiskReqType           | In-Memory         | Java POJO / JAXB      | N/A              | Inbound credit risk assessment request (REST & SOAP)         |
| ENT-002       | CreditRiskResType           | In-Memory         | Java POJO / JAXB      | N/A              | Outbound credit risk assessment response                     |
| ENT-003       | RequestHeader               | In-Memory         | Java POJO / JAXB      | N/A              | Request metadata (transaction ID, timestamp, source system)  |
| ENT-004       | ResponseHeader              | In-Memory         | Java POJO / JAXB      | N/A              | Response metadata (status code, message, transaction ID)     |
| ENT-005       | CreditScoreDetail           | In-Memory         | Java POJO / JAXB      | N/A              | Bureau-sourced credit score and trade-line details           |
| ENT-006       | DebtServiceDetail           | In-Memory         | Java POJO / JAXB      | N/A              | Calculated debt-service and affordability metrics            |
| ENT-007       | EmploymentRiskDetail        | In-Memory         | Java POJO / JAXB      | N/A              | Employment-based risk classification                         |
| ENT-008       | IncomeVerificationDetail    | In-Memory         | Java POJO / JAXB      | N/A              | Income verification and debt-to-income ratio                 |
| ENT-009       | ProvinceType (enum)         | In-Memory         | Java Enum             | 13 values        | Valid Canadian province/territory codes                      |
| ENT-010       | RequestChannelType (enum)   | In-Memory         | Java Enum             | 7 values         | Valid inbound request channel codes                          |
| ENT-011       | BureauInquiryRequest        | In-Memory         | Generated JAXB POJO   | N/A              | Outbound SOAP request to external credit bureau              |
| ENT-012       | BureauInquiryResponse       | In-Memory         | Generated JAXB POJO   | N/A              | Inbound SOAP response from external credit bureau            |
| ENT-013       | BureauScoreService          | External          | JAX-WS Interface      | N/A              | Bureau SOAP service contract (single `inquire` operation)    |
| ENT-014       | BureauSubscriber            | In-Memory         | Generated JAXB POJO   | N/A              | Bureau subscriber credentials (code + name)                  |
| ENT-015       | BureauSubject               | In-Memory         | Generated JAXB POJO   | N/A              | Applicant identity data forwarded to bureau                  |
| ENT-016       | TransactionLog (MongoDB)    | MongoDB           | Document Collection   | Unbounded         | Wire Tap audit log of every processed transaction            |

---

## 4. Entity Details

### 4.1 ENT-001: CreditRiskReqType

| **Attribute**         | **Details**                                                                 |
|-----------------------|-----------------------------------------------------------------------------|
| **Entity ID**         | ENT-001                                                                     |
| **Entity Name**       | CreditRiskReqType                                                           |
| **Schema**            | com.nexgen.esb.creditrisk.model                                             |
| **Type**              | Java POJO / JAXB root element                                               |
| **Description**       | Top-level inbound request for credit risk assessment; consumed by REST and SOAP routes |
| **Primary Key**       | applicantId (logical; no DB PK)                                             |
| **Estimated Rows**    | N/A — in-memory per-request                                                 |
| **Source File/Class** | `src/main/java/com/nexgen/esb/creditrisk/model/CreditRiskReqType.java`     |

#### Attributes / Columns

| **#** | **Column Name**         | **Data Type** | **Length/Precision** | **Nullable** | **Default** | **Constraints**                   | **Usage Status** | **Default/Hardcoded Value**                        | **Mapping Reference**       | **Description**                                                 |
|-------|-------------------------|---------------|----------------------|--------------|-------------|-----------------------------------|------------------|----------------------------------------------------|-----------------------------|-----------------------------------------------------------------|
| 1     | requestHeader           | RequestHeader | —                    | No           | —           | `@XmlElement(required=true)`      | Active           | None                                               | ENT-003; F2F §2.1           | Nested request metadata (transaction ID, timestamp, source)     |
| 2     | applicantId             | String        | —                    | No           | —           | `@XmlElement(required=true)`; CR-001 not blank | Active | None                                       | F2F §2.1; REST param        | Unique applicant identifier                                     |
| 3     | firstName               | String        | —                    | No           | —           | `@XmlElement(required=true)`; CR-002 not blank | Active | None                                       | ENT-015.firstName; F2F §2.1 | Applicant first name                                            |
| 4     | lastName                | String        | —                    | No           | —           | `@XmlElement(required=true)`; CR-003 not blank | Active | None                                       | ENT-015.lastName; F2F §2.1  | Applicant last name                                             |
| 5     | dateOfBirth             | String        | YYYY-MM-DD           | No           | —           | `@XmlElement(required=true)`; CR-004 regex `^\d{4}-\d{2}-\d{2}$` | Active | None              | ENT-015.dateOfBirth; F2F §2.1 | Date of birth in ISO format                                   |
| 6     | socialInsuranceNumber   | String        | NNN-NNN-NNN          | No           | —           | `@XmlElement(required=true)`; CR-005 regex `^\d{3}-?\d{3}-?\d{3}$` | Active | None         | ENT-015.socialInsuranceNumber; F2F §2.1 | Canadian SIN                                         |
| 7     | employmentStatus        | String        | —                    | Yes          | —           | None                              | Active           | None                                               | ENT-007.employmentType; F2F §2.1 | e.g. FULL_TIME, PART_TIME, SELF_EMPLOYED, UNEMPLOYED        |
| 8     | annualIncome            | Double        | —                    | Yes          | —           | None                              | Active           | None                                               | ENT-008.reportedIncome; F2F §2.1 | Applicant self-reported annual income                       |
| 9     | province                | String        | 2-char               | Yes          | —           | CR-006 must be valid ProvinceType | Active           | None                                               | ENT-015.province; F2F §2.1  | Canadian province/territory code (EN-009)                       |
| 10    | postalCode              | String        | A1A 1A1              | Yes          | —           | CR-007 regex `^[A-Za-z]\d[A-Za-z]\s?\d[A-Za-z]\d$` | Active | None                     | ENT-015.postalCode; F2F §2.1 | Canadian postal code                                           |
| 11    | requestChannel          | String        | —                    | Yes          | —           | None (no enum validation applied) | Active           | None                                               | F2F §2.1; MongoDB log       | Inbound channel (see ENT-010)                                   |
| 12    | productType             | String        | —                    | Yes          | —           | None                              | Active           | None                                               | ENT-011.productType; F2F §2.1 | Credit product type (e.g. MORTGAGE, AUTO)                     |
| 13    | requestedAmount         | Double        | —                    | Yes          | —           | None                              | Active           | None                                               | F2F §2.1                    | Loan/credit amount requested                                    |

#### Indexes
_None — in-memory POJO._

#### Foreign Key Relationships
_None — in-memory POJO._

#### Check Constraints
_None defined beyond JAXB `required` annotations._

#### Triggers
_None._

---

### 4.2 ENT-002: CreditRiskResType

| **Attribute**         | **Details**                                                                  |
|-----------------------|------------------------------------------------------------------------------|
| **Entity ID**         | ENT-002                                                                      |
| **Entity Name**       | CreditRiskResType                                                            |
| **Schema**            | com.nexgen.esb.creditrisk.model                                              |
| **Type**              | Java POJO / JAXB root element                                                |
| **Description**       | Top-level outbound credit risk assessment response returned to caller        |
| **Primary Key**       | applicantId (logical)                                                        |
| **Estimated Rows**    | N/A — in-memory per-request                                                  |
| **Source File/Class** | `src/main/java/com/nexgen/esb/creditrisk/model/CreditRiskResType.java`      |

#### Attributes / Columns

| **#** | **Column Name**      | **Data Type**             | **Length/Precision** | **Nullable** | **Default** | **Constraints**              | **Usage Status** | **Default/Hardcoded Value**                                              | **Mapping Reference**       | **Description**                                      |
|-------|----------------------|---------------------------|----------------------|--------------|-------------|------------------------------|------------------|--------------------------------------------------------------------------|-----------------------------|------------------------------------------------------|
| 1     | responseHeader       | ResponseHeader            | —                    | No           | —           | `@XmlElement(required=true)` | Active           | statusCode="W215E" / "SUCCESS" — `RiskScoreCalculator.java:48,55`       | ENT-004; F2F §2.2           | Nested response metadata                             |
| 2     | applicantId          | String                    | —                    | Yes          | —           | None                         | Active           | None                                                                     | ENT-001.applicantId; F2F §2.2 | Echoed applicant identifier                        |
| 3     | riskCategory         | String                    | —                    | Yes          | —           | None                         | Active           | "INDETERMINATE" — `RiskScoreCalculator.java:51` (bureau error path)     | F2F §2.2; MongoDB log        | e.g. LOW, MODERATE, HIGH, INDETERMINATE             |
| 4     | overallScore         | Integer                   | —                    | Yes          | —           | None                         | Active           | 0 — `RiskScoreCalculator.java:52` (bureau error path)                   | F2F §2.2; MongoDB log        | Composite risk score (0–1000)                       |
| 5     | creditScoreDetail    | CreditScoreDetail         | —                    | Yes          | —           | None                         | Active           | None (null on bureau error)                                              | ENT-005; F2F §2.2            | Bureau score sub-model                              |
| 6     | incomeVerification   | IncomeVerificationDetail  | —                    | Yes          | —           | None                         | Active           | verificationStatus="SELF_REPORTED" — `RiskScoreCalculator.java:107`     | ENT-008; F2F §2.2            | Income verification sub-model                       |
| 7     | employmentRisk       | EmploymentRiskDetail      | —                    | Yes          | —           | None                         | Active           | None                                                                     | ENT-007; F2F §2.2            | Employment risk sub-model                           |
| 8     | debtService          | DebtServiceDetail         | —                    | Yes          | —           | None                         | Active           | None                                                                     | ENT-006; F2F §2.2            | Debt service calculations sub-model                 |
| 9     | riskFactors          | List\<String\>            | —                    | Yes          | —           | `@XmlElementWrapper`         | Active           | None                                                                     | F2F §2.2                     | List of risk factor codes (e.g. LOW_CREDIT_SCORE)   |
| 10    | recommendation       | String                    | —                    | Yes          | —           | None                         | Active           | "REFER_TO_UNDERWRITER" — `RiskScoreCalculator.java:53` (bureau error)   | F2F §2.2; MongoDB log        | Decisioning recommendation                          |
| 11    | accuracyCode         | String                    | —                    | Yes          | —           | None                         | Active           | "LM" (limited) / "FD" (full data) — `RiskScoreCalculator.java:53,93`   | F2F §2.2                     | Assessment accuracy code                            |
| 12    | scoringModelVersion  | String                    | —                    | Yes          | —           | None                         | Active           | `<strategyName>-v1.0` — `RiskScoreCalculator.java:45`                   | F2F §2.2                     | Identifies the scoring strategy version used        |

#### Indexes
_None — in-memory POJO._

#### Foreign Key Relationships
_None — in-memory POJO._

#### Check Constraints
_None._

#### Triggers
_None._

---

### 4.3 ENT-003: RequestHeader

| **Attribute**         | **Details**                                                                 |
|-----------------------|-----------------------------------------------------------------------------|
| **Entity ID**         | ENT-003                                                                     |
| **Entity Name**       | RequestHeader                                                               |
| **Schema**            | com.nexgen.esb.creditrisk.model                                             |
| **Type**              | Java POJO / JAXB element                                                    |
| **Description**       | Metadata embedded in every inbound credit risk request                      |
| **Primary Key**       | transactionId (logical)                                                     |
| **Estimated Rows**    | N/A — in-memory per-request                                                 |
| **Source File/Class** | `src/main/java/com/nexgen/esb/creditrisk/model/RequestHeader.java`         |

#### Attributes / Columns

| **#** | **Column Name** | **Data Type** | **Length/Precision** | **Nullable** | **Default** | **Constraints**              | **Usage Status** | **Default/Hardcoded Value** | **Mapping Reference**           | **Description**                            |
|-------|-----------------|---------------|----------------------|--------------|-------------|------------------------------|------------------|-----------------------------|----------------------------------|--------------------------------------------|
| 1     | transactionId   | String        | —                    | No           | —           | `@XmlElement(required=true)` | Active           | None                        | MongoDB LOG_TRANSACTION_ID; F2F §2.1 | Unique transaction identifier         |
| 2     | timestamp       | String        | ISO-8601             | No           | —           | `@XmlElement(required=true)` | Active           | None                        | F2F §2.1                         | Request timestamp                          |
| 3     | sourceSystem    | String        | —                    | Yes          | —           | None                         | Active           | None                        | MongoDB LOG_SOURCE_SYSTEM; F2F §2.1 | Originating system identifier           |
| 4     | userId          | String        | —                    | Yes          | —           | None                         | Dead             | None                        | None                             | User identifier; never read in any processor |

#### Indexes
_None._

#### Foreign Key Relationships
_None._

#### Check Constraints
_None._

#### Triggers
_None._

---

### 4.4 ENT-004: ResponseHeader

| **Attribute**         | **Details**                                                                 |
|-----------------------|-----------------------------------------------------------------------------|
| **Entity ID**         | ENT-004                                                                     |
| **Entity Name**       | ResponseHeader                                                              |
| **Schema**            | com.nexgen.esb.creditrisk.model                                             |
| **Type**              | Java POJO / JAXB element                                                    |
| **Description**       | Metadata embedded in every outbound credit risk response                    |
| **Primary Key**       | transactionId (logical; echoed from request)                                |
| **Estimated Rows**    | N/A — in-memory per-request                                                 |
| **Source File/Class** | `src/main/java/com/nexgen/esb/creditrisk/model/ResponseHeader.java`        |

#### Attributes / Columns

| **#** | **Column Name** | **Data Type** | **Length/Precision** | **Nullable** | **Default** | **Constraints** | **Usage Status** | **Default/Hardcoded Value**                                                        | **Mapping Reference** | **Description**                    |
|-------|-----------------|---------------|----------------------|--------------|-------------|-----------------|------------------|------------------------------------------------------------------------------------|-----------------------|------------------------------------|
| 1     | transactionId   | String        | —                    | Yes          | —           | None            | Active           | None                                                                               | ENT-003.transactionId; F2F §2.2 | Echoed transaction identifier |
| 2     | timestamp       | String        | ISO-8601             | Yes          | —           | None            | Active           | Current time — `RiskScoreCalculator.java:41`                                      | F2F §2.2              | Response generation timestamp      |
| 3     | statusCode      | String        | —                    | Yes          | —           | None            | Active           | "SUCCESS" or "W215E" — `RiskScoreCalculator.java:48,55`                           | F2F §2.2              | Processing status code             |
| 4     | statusMessage   | String        | —                    | Yes          | —           | None            | Active           | "Assessment completed successfully" / "Bureau data unavailable…" — `RiskScoreCalculator.java:49,56` | F2F §2.2 | Human-readable status message |

#### Indexes
_None._

#### Foreign Key Relationships
_None._

#### Check Constraints
_None._

#### Triggers
_None._

---

### 4.5 ENT-005: CreditScoreDetail

| **Attribute**         | **Details**                                                                  |
|-----------------------|------------------------------------------------------------------------------|
| **Entity ID**         | ENT-005                                                                      |
| **Entity Name**       | CreditScoreDetail                                                            |
| **Schema**            | com.nexgen.esb.creditrisk.model                                              |
| **Type**              | Java POJO / JAXB element                                                     |
| **Description**       | Bureau-sourced credit score and trade-line metrics; mapped from BureauInquiryResponse |
| **Primary Key**       | N/A                                                                          |
| **Estimated Rows**    | N/A — in-memory per-request                                                  |
| **Source File/Class** | `src/main/java/com/nexgen/esb/creditrisk/model/CreditScoreDetail.java`      |

#### Attributes / Columns

| **#** | **Column Name**    | **Data Type** | **Length/Precision** | **Nullable** | **Default** | **Constraints** | **Usage Status** | **Default/Hardcoded Value**                                     | **Mapping Reference**                  | **Description**                                        |
|-------|--------------------|---------------|----------------------|--------------|-------------|-----------------|------------------|-----------------------------------------------------------------|----------------------------------------|--------------------------------------------------------|
| 1     | bureauScore        | Integer       | —                    | Yes          | —           | None            | Active           | None                                                            | ENT-012.creditScore; F2F §3.1          | Numeric credit score from bureau (300–900)            |
| 2     | bureauScoreRange   | String        | —                    | Yes          | —           | None            | Active           | Derived: EXCEPTIONAL/VERY_GOOD/GOOD/FAIR/POOR — `BureauResponseMapper.java:60` | F2F §3.1          | Score range classification                            |
| 3     | delinquencyCount   | Integer       | —                    | Yes          | —           | None            | Active           | None                                                            | ENT-012.delinquencyCount; F2F §3.1     | Number of delinquent accounts                         |
| 4     | inquiryCount       | Integer       | —                    | Yes          | —           | None            | Active           | None                                                            | ENT-012.inquiryCount; F2F §3.1         | Number of recent credit inquiries                     |
| 5     | openAccountCount   | Integer       | —                    | Yes          | —           | None            | Active           | None                                                            | ENT-012.openTradelineCount; F2F §3.1   | Number of open trade lines                            |
| 6     | totalCreditLimit   | Double        | —                    | Yes          | —           | None            | Active           | None                                                            | ENT-012.totalCreditLimit; F2F §3.1     | Sum of all credit limits                              |
| 7     | totalBalance       | Double        | —                    | Yes          | —           | None            | Active           | None                                                            | ENT-012.totalBalance; F2F §3.1         | Sum of all outstanding balances                       |
| 8     | utilizationRate    | Double        | 0.0–1.0              | Yes          | 0.0         | None            | Active           | 0.0 — `BureauResponseMapper.java:51` (when limit=0)            | F2F §3.1                               | totalBalance / totalCreditLimit; key risk factor      |

#### Indexes
_None._

#### Foreign Key Relationships
_None._

#### Check Constraints
_None._

#### Triggers
_None._

---

### 4.6 ENT-006: DebtServiceDetail

| **Attribute**         | **Details**                                                                 |
|-----------------------|-----------------------------------------------------------------------------|
| **Entity ID**         | ENT-006                                                                     |
| **Entity Name**       | DebtServiceDetail                                                           |
| **Schema**            | com.nexgen.esb.creditrisk.model                                             |
| **Type**              | Java POJO / JAXB element                                                    |
| **Description**       | Calculated debt-service ratios and affordability rating                     |
| **Primary Key**       | N/A                                                                         |
| **Estimated Rows**    | N/A — in-memory per-request                                                 |
| **Source File/Class** | `src/main/java/com/nexgen/esb/creditrisk/model/DebtServiceDetail.java`     |

#### Attributes / Columns

| **#** | **Column Name**      | **Data Type** | **Length/Precision** | **Nullable** | **Default** | **Constraints** | **Usage Status** | **Default/Hardcoded Value**                           | **Mapping Reference** | **Description**                                          |
|-------|----------------------|---------------|----------------------|--------------|-------------|-----------------|------------------|-------------------------------------------------------|-----------------------|----------------------------------------------------------|
| 1     | totalMonthlyDebt     | Double        | —                    | Yes          | —           | None            | Active           | Calculated: totalBalance × 0.03 — `RiskScoreCalculator.java:138` | F2F §3.2   | Estimated monthly debt obligation                       |
| 2     | totalMonthlyIncome   | Double        | —                    | Yes          | —           | None            | Active           | Calculated: annualIncome / 12 — `RiskScoreCalculator.java:137`   | F2F §3.2   | Monthly income derived from annual                      |
| 3     | debtServiceRatio     | Double        | 0.0–1.0+             | Yes          | —           | None            | Active           | 1.0 — `RiskScoreCalculator.java:142` (when income=0)  | F2F §3.2              | GDS ratio: monthlyDebt / monthlyIncome                  |
| 4     | totalDebtServiceRatio| Double        | 0.0–1.0+             | Yes          | —           | None            | Active           | 1.0 — `RiskScoreCalculator.java:145` (when income=0)  | F2F §3.2              | TDS ratio: (monthlyDebt + requestedPayment) / income    |
| 5     | affordabilityRating  | String        | —                    | Yes          | —           | None            | Active           | Derived by StandardScoringStrategy — `RiskScoreCalculator.java:148` | F2F §3.2 | e.g. AFFORDABLE, MARGINAL, OVER_EXTENDED              |

#### Indexes
_None._

#### Foreign Key Relationships
_None._

#### Check Constraints
_None._

#### Triggers
_None._

---

### 4.7 ENT-007: EmploymentRiskDetail

| **Attribute**         | **Details**                                                                  |
|-----------------------|------------------------------------------------------------------------------|
| **Entity ID**         | ENT-007                                                                      |
| **Entity Name**       | EmploymentRiskDetail                                                         |
| **Schema**            | com.nexgen.esb.creditrisk.model                                              |
| **Type**              | Java POJO / JAXB element                                                     |
| **Description**       | Employment-based risk classification derived from applicant employment status |
| **Primary Key**       | N/A                                                                          |
| **Estimated Rows**    | N/A — in-memory per-request                                                  |
| **Source File/Class** | `src/main/java/com/nexgen/esb/creditrisk/model/EmploymentRiskDetail.java`   |

#### Attributes / Columns

| **#** | **Column Name**   | **Data Type** | **Length/Precision** | **Nullable** | **Default** | **Constraints** | **Usage Status** | **Default/Hardcoded Value**                                                          | **Mapping Reference**         | **Description**                                   |
|-------|-------------------|---------------|----------------------|--------------|-------------|-----------------|------------------|--------------------------------------------------------------------------------------|-------------------------------|---------------------------------------------------|
| 1     | employmentType    | String        | —                    | Yes          | —           | None            | Active           | Copied from `CreditRiskReqType.employmentStatus` — `RiskScoreCalculator.java:119`   | ENT-001.employmentStatus; F2F §3.3 | Employment type/status label                 |
| 2     | yearsEmployed     | Integer       | —                    | Yes          | —           | None            | Dead             | None                                                                                 | None                          | Never populated by any processor                  |
| 3     | industryCategory  | String        | —                    | Yes          | —           | None            | Dead             | None                                                                                 | None                          | Never populated by any processor                  |
| 4     | riskLevel         | String        | —                    | Yes          | —           | None            | Active           | LOW/MODERATE/MODERATE_HIGH/HIGH — `RiskScoreCalculator.java:123-129`                | F2F §3.3                      | Derived employment risk level                     |

#### Indexes
_None._

#### Foreign Key Relationships
_None._

#### Check Constraints
_None._

#### Triggers
_None._

---

### 4.8 ENT-008: IncomeVerificationDetail

| **Attribute**         | **Details**                                                                  |
|-----------------------|------------------------------------------------------------------------------|
| **Entity ID**         | ENT-008                                                                      |
| **Entity Name**       | IncomeVerificationDetail                                                     |
| **Schema**            | com.nexgen.esb.creditrisk.model                                              |
| **Type**              | Java POJO / JAXB element                                                     |
| **Description**       | Income verification assessment; all values are self-reported (no third-party verification) |
| **Primary Key**       | N/A                                                                          |
| **Estimated Rows**    | N/A — in-memory per-request                                                  |
| **Source File/Class** | `src/main/java/com/nexgen/esb/creditrisk/model/IncomeVerificationDetail.java` |

#### Attributes / Columns

| **#** | **Column Name**      | **Data Type** | **Length/Precision** | **Nullable** | **Default** | **Constraints** | **Usage Status** | **Default/Hardcoded Value**                                              | **Mapping Reference**          | **Description**                                           |
|-------|----------------------|---------------|----------------------|--------------|-------------|-----------------|------------------|--------------------------------------------------------------------------|--------------------------------|-----------------------------------------------------------|
| 1     | verificationStatus   | String        | —                    | Yes          | —           | None            | Active           | "SELF_REPORTED" — `RiskScoreCalculator.java:107`                        | F2F §3.4                       | Status of income verification                            |
| 2     | reportedIncome       | Double        | —                    | Yes          | —           | None            | Active           | Copied from `CreditRiskReqType.annualIncome` — `RiskScoreCalculator.java:105` | ENT-001.annualIncome; F2F §3.4 | Annual income as reported by applicant              |
| 3     | verifiedIncome       | Double        | —                    | Yes          | —           | None            | Active           | Set equal to reportedIncome — `RiskScoreCalculator.java:106`            | F2F §3.4                       | Verified income (currently mirrors reported; no external source) |
| 4     | incomeSource         | String        | —                    | Yes          | —           | None            | Active           | Copied from `CreditRiskReqType.employmentStatus` — `RiskScoreCalculator.java:108` | ENT-001.employmentStatus; F2F §3.4 | Source of income (employment type)         |
| 5     | debtToIncomeRatio    | Double        | 0.0–1.0+             | Yes          | —           | None            | Active           | 1.0 — `RiskScoreCalculator.java:112` (when income=0)                   | F2F §3.4                       | Monthly debt / monthly income ratio                      |

#### Indexes
_None._

#### Foreign Key Relationships
_None._

#### Check Constraints
_None._

#### Triggers
_None._

---

### 4.9 ENT-009: ProvinceType (Enum)

| **Attribute**         | **Details**                                                              |
|-----------------------|--------------------------------------------------------------------------|
| **Entity ID**         | ENT-009                                                                  |
| **Entity Name**       | ProvinceType                                                             |
| **Schema**            | com.nexgen.esb.creditrisk.model                                          |
| **Type**              | Java Enum                                                                |
| **Description**       | Enumeration of valid Canadian province and territory codes               |
| **Primary Key**       | N/A                                                                      |
| **Estimated Rows**    | 13 constant values                                                       |
| **Source File/Class** | `src/main/java/com/nexgen/esb/creditrisk/model/ProvinceType.java`       |

_See Section 6 for full enumerated value listing._

---

### 4.10 ENT-010: RequestChannelType (Enum)

| **Attribute**         | **Details**                                                               |
|-----------------------|---------------------------------------------------------------------------|
| **Entity ID**         | ENT-010                                                                   |
| **Entity Name**       | RequestChannelType                                                        |
| **Schema**            | com.nexgen.esb.creditrisk.model                                           |
| **Type**              | Java Enum                                                                 |
| **Description**       | Enumeration of valid inbound request channel identifiers                  |
| **Primary Key**       | N/A                                                                       |
| **Estimated Rows**    | 7 constant values                                                         |
| **Source File/Class** | `src/main/java/com/nexgen/esb/creditrisk/model/RequestChannelType.java`  |

_See Section 6 for full enumerated value listing._

---

### 4.11 ENT-011: BureauInquiryRequest

| **Attribute**         | **Details**                                                                   |
|-----------------------|-------------------------------------------------------------------------------|
| **Entity ID**         | ENT-011                                                                       |
| **Entity Name**       | BureauInquiryRequest                                                          |
| **Schema**            | com.nexgen.esb.creditrisk.generated                                           |
| **Type**              | Generated JAXB POJO / SOAP message                                            |
| **Description**       | Outbound SOAP request sent to the external credit bureau service              |
| **Primary Key**       | requestId (logical)                                                           |
| **Estimated Rows**    | N/A — in-memory per-request                                                   |
| **Source File/Class** | `src/main/java/com/nexgen/esb/creditrisk/generated/BureauInquiryRequest.java` |

#### Attributes / Columns

| **#** | **Column Name** | **Data Type**     | **Length/Precision** | **Nullable** | **Default** | **Constraints** | **Usage Status** | **Default/Hardcoded Value**                                      | **Mapping Reference**           | **Description**                               |
|-------|-----------------|-------------------|----------------------|--------------|-------------|-----------------|------------------|------------------------------------------------------------------|---------------------------------|-----------------------------------------------|
| 1     | requestId       | String            | —                    | Yes          | —           | None            | Active           | `<prefix>+UUID` — `BureauRequestBuilder.java:54`                | F2F §4.1                        | Unique bureau request identifier              |
| 2     | timestamp       | String            | ISO-8601             | Yes          | —           | None            | Active           | Current time — `BureauRequestBuilder.java:56`                   | F2F §4.1                        | Request generation timestamp                  |
| 3     | subscriber      | BureauSubscriber  | —                    | Yes          | —           | None            | Active           | From config: BUREAU_SUBSCRIBER_CODE, BUREAU_SUBSCRIBER_NAME     | ENT-014; F2F §4.1               | Bureau subscriber credentials                 |
| 4     | subject         | BureauSubject     | —                    | Yes          | —           | None            | Active           | Mapped from ENT-001 fields                                      | ENT-015; F2F §4.1               | Applicant identity for bureau lookup          |
| 5     | productType     | String            | —                    | Yes          | —           | None            | Active           | Copied from ENT-001.productType — `BureauRequestBuilder.java:57` | ENT-001.productType; F2F §4.1  | Credit product type for bureau inquiry        |

#### Indexes
_None._

#### Foreign Key Relationships
_None._

#### Check Constraints
_None._

#### Triggers
_None._

---

### 4.12 ENT-012: BureauInquiryResponse

| **Attribute**         | **Details**                                                                    |
|-----------------------|--------------------------------------------------------------------------------|
| **Entity ID**         | ENT-012                                                                        |
| **Entity Name**       | BureauInquiryResponse                                                          |
| **Schema**            | com.nexgen.esb.creditrisk.generated                                            |
| **Type**              | Generated JAXB POJO / SOAP message                                             |
| **Description**       | Inbound SOAP response received from the external credit bureau service         |
| **Primary Key**       | responseId (logical)                                                           |
| **Estimated Rows**    | N/A — in-memory per-request                                                    |
| **Source File/Class** | `src/main/java/com/nexgen/esb/creditrisk/generated/BureauInquiryResponse.java` |

#### Attributes / Columns

| **#** | **Column Name**       | **Data Type** | **Length/Precision** | **Nullable** | **Default** | **Constraints** | **Usage Status** | **Default/Hardcoded Value** | **Mapping Reference**              | **Description**                         |
|-------|-----------------------|---------------|----------------------|--------------|-------------|-----------------|------------------|-----------------------------|------------------------------------|-----------------------------------------|
| 1     | requestId             | String        | —                    | Yes          | —           | None            | Logging Only     | None                        | ENT-011.requestId; F2F §4.2        | Echoed bureau request ID               |
| 2     | responseId            | String        | —                    | Yes          | —           | None            | Logging Only     | None                        | F2F §4.2                           | Bureau-assigned response ID            |
| 3     | creditScore           | Integer       | 300–900              | Yes          | —           | None            | Active           | None                        | ENT-005.bureauScore; F2F §4.2      | Credit bureau numeric score            |
| 4     | delinquencyCount      | Integer       | —                    | Yes          | —           | None            | Active           | None                        | ENT-005.delinquencyCount; F2F §4.2 | Count of delinquent accounts           |
| 5     | inquiryCount          | Integer       | —                    | Yes          | —           | None            | Active           | None                        | ENT-005.inquiryCount; F2F §4.2     | Count of credit inquiries              |
| 6     | openTradelineCount    | Integer       | —                    | Yes          | —           | None            | Active           | None                        | ENT-005.openAccountCount; F2F §4.2 | Count of open trade lines              |
| 7     | totalCreditLimit      | Double        | —                    | Yes          | —           | None            | Active           | None                        | ENT-005.totalCreditLimit; F2F §4.2 | Total available credit limit           |
| 8     | totalBalance          | Double        | —                    | Yes          | —           | None            | Active           | None                        | ENT-005.totalBalance; F2F §4.2     | Total outstanding balance              |
| 9     | errorCode             | String        | —                    | Yes          | —           | None            | Active           | None                        | F2F §4.2                           | Bureau error code (null on success)    |
| 10    | errorMessage          | String        | —                    | Yes          | —           | None            | Active           | None                        | F2F §4.2                           | Bureau error message (null on success) |

#### Indexes
_None._

#### Foreign Key Relationships
_None._

#### Check Constraints
_None._

#### Triggers
_None._

---

### 4.13 ENT-013: BureauScoreService

| **Attribute**         | **Details**                                                                  |
|-----------------------|------------------------------------------------------------------------------|
| **Entity ID**         | ENT-013                                                                      |
| **Entity Name**       | BureauScoreService                                                           |
| **Schema**            | com.nexgen.esb.creditrisk.generated                                          |
| **Type**              | JAX-WS Service Interface                                                     |
| **Description**       | SOAP service contract for the external credit bureau; single `inquire` operation |
| **Primary Key**       | N/A — interface only                                                         |
| **Estimated Rows**    | N/A                                                                          |
| **Source File/Class** | `src/main/java/com/nexgen/esb/creditrisk/generated/BureauScoreService.java` |

_No field-level attributes; defines one operation: `inquire(BureauInquiryRequest) → BureauInquiryResponse`._

---

### 4.14 ENT-014: BureauSubscriber

| **Attribute**         | **Details**                                                                 |
|-----------------------|-----------------------------------------------------------------------------|
| **Entity ID**         | ENT-014                                                                     |
| **Entity Name**       | BureauSubscriber                                                            |
| **Schema**            | com.nexgen.esb.creditrisk.generated                                         |
| **Type**              | Generated JAXB POJO                                                         |
| **Description**       | Bureau subscriber credentials embedded in every outbound bureau request     |
| **Primary Key**       | subscriberCode (logical)                                                    |
| **Estimated Rows**    | N/A — in-memory per-request                                                 |
| **Source File/Class** | `src/main/java/com/nexgen/esb/creditrisk/generated/BureauSubscriber.java`  |

#### Attributes / Columns

| **#** | **Column Name**  | **Data Type** | **Length/Precision** | **Nullable** | **Default** | **Constraints** | **Usage Status** | **Default/Hardcoded Value**                       | **Mapping Reference** | **Description**                          |
|-------|------------------|---------------|----------------------|--------------|-------------|-----------------|------------------|---------------------------------------------------|-----------------------|------------------------------------------|
| 1     | subscriberCode   | String        | —                    | Yes          | —           | None            | Active           | From config: `BUREAU_SUBSCRIBER_CODE`             | F2F §4.1              | Bureau subscriber identification code   |
| 2     | subscriberName   | String        | —                    | Yes          | —           | None            | Active           | From config: `BUREAU_SUBSCRIBER_NAME`             | F2F §4.1              | Bureau subscriber display name          |

#### Indexes
_None._

#### Foreign Key Relationships
_None._

#### Check Constraints
_None._

#### Triggers
_None._

---

### 4.15 ENT-015: BureauSubject

| **Attribute**         | **Details**                                                                |
|-----------------------|----------------------------------------------------------------------------|
| **Entity ID**         | ENT-015                                                                    |
| **Entity Name**       | BureauSubject                                                              |
| **Schema**            | com.nexgen.esb.creditrisk.generated                                        |
| **Type**              | Generated JAXB POJO                                                        |
| **Description**       | Applicant identity data forwarded to the credit bureau for inquiry         |
| **Primary Key**       | socialInsuranceNumber (logical)                                            |
| **Estimated Rows**    | N/A — in-memory per-request                                                |
| **Source File/Class** | `src/main/java/com/nexgen/esb/creditrisk/generated/BureauSubject.java`    |

#### Attributes / Columns

| **#** | **Column Name**       | **Data Type** | **Length/Precision** | **Nullable** | **Default** | **Constraints** | **Usage Status** | **Default/Hardcoded Value** | **Mapping Reference**                   | **Description**                      |
|-------|-----------------------|---------------|----------------------|--------------|-------------|-----------------|------------------|-----------------------------|-----------------------------------------|--------------------------------------|
| 1     | firstName             | String        | —                    | Yes          | —           | None            | Active           | None                        | ENT-001.firstName; F2F §4.1             | Applicant first name                |
| 2     | lastName              | String        | —                    | Yes          | —           | None            | Active           | None                        | ENT-001.lastName; F2F §4.1              | Applicant last name                 |
| 3     | dateOfBirth           | String        | YYYY-MM-DD           | Yes          | —           | None            | Active           | None                        | ENT-001.dateOfBirth; F2F §4.1           | Applicant date of birth             |
| 4     | socialInsuranceNumber | String        | NNN-NNN-NNN          | Yes          | —           | None            | Active           | None                        | ENT-001.socialInsuranceNumber; F2F §4.1 | Canadian SIN for bureau lookup      |
| 5     | province              | String        | 2-char               | Yes          | —           | None            | Active           | None                        | ENT-001.province; F2F §4.1              | Province code for bureau lookup     |
| 6     | postalCode            | String        | A1A 1A1              | Yes          | —           | None            | Active           | None                        | ENT-001.postalCode; F2F §4.1            | Postal code for bureau lookup       |

#### Indexes
_None._

#### Foreign Key Relationships
_None._

#### Check Constraints
_None._

#### Triggers
_None._

---

## 5. Entity Relationship Summary

### 5.1 Relationship Matrix

| **Parent Entity**        | **Child Entity**            | **Relationship**                              | **Cardinality** | **FK Column(s)**                    |
|--------------------------|-----------------------------|-----------------------------------------------|-----------------|-------------------------------------|
| CreditRiskReqType        | RequestHeader               | Contains request metadata                     | 1:1             | requestHeader (embedded)            |
| CreditRiskReqType        | BureauInquiryRequest        | Mapped-to for bureau SOAP call                | 1:1             | firstName, lastName, DOB, SIN, etc. |
| BureauInquiryRequest     | BureauSubscriber            | Contains subscriber credentials               | 1:1             | subscriber (embedded)               |
| BureauInquiryRequest     | BureauSubject               | Contains applicant identity for bureau        | 1:1             | subject (embedded)                  |
| BureauInquiryResponse    | CreditScoreDetail           | Mapped-to internal score model                | 1:1             | creditScore, delinquencyCount, etc. |
| CreditRiskResType        | ResponseHeader              | Contains response metadata                    | 1:1             | responseHeader (embedded)           |
| CreditRiskResType        | CreditScoreDetail           | Contains bureau score details                 | 1:1             | creditScoreDetail (embedded)        |
| CreditRiskResType        | IncomeVerificationDetail    | Contains income verification results          | 1:1             | incomeVerification (embedded)       |
| CreditRiskResType        | EmploymentRiskDetail        | Contains employment risk results              | 1:1             | employmentRisk (embedded)           |
| CreditRiskResType        | DebtServiceDetail           | Contains debt service calculations            | 1:1             | debtService (embedded)              |
| CreditRiskReqType        | TransactionLog (MongoDB)    | Request fields logged via Wire Tap            | 1:1             | applicantId, province, etc.         |
| CreditRiskResType        | TransactionLog (MongoDB)    | Response fields logged via Wire Tap           | 1:1             | riskCategory, overallScore, etc.    |

### 5.2 ER Diagram (Text Representation)

```
┌─────────────────────────┐         ┌───────────────────┐
│   CreditRiskReqType     │ 1──────▶│   RequestHeader   │
│   (ENT-001)             │ (embed) │   (ENT-003)       │
│                         │         │ transactionId     │
│ applicantId             │         │ timestamp         │
│ firstName               │         │ sourceSystem      │
│ lastName                │         │ userId [Dead]     │
│ dateOfBirth             │         └───────────────────┘
│ socialInsuranceNumber   │
│ employmentStatus        │         ┌──────────────────────┐
│ annualIncome            │ 1──────▶│ BureauInquiryRequest │
│ province                │ (map)   │ (ENT-011)            │
│ postalCode              │         │ requestId            │
│ requestChannel          │         │ timestamp            │
│ productType             │         │ subscriber ─────────▶│ BureauSubscriber (ENT-014)
│ requestedAmount         │         │ subject  ──────────▶ │ BureauSubject    (ENT-015)
└─────────────────────────┘         │ productType          │
         │                          └──────────────────────┘
         │ Wire Tap                           │ SOAP call
         ▼                                   ▼
┌─────────────────────────┐         ┌──────────────────────┐
│  TransactionLog MongoDB │         │ BureauInquiryResponse│
│  (ENT-016)              │◀────────│ (ENT-012)            │
│  transactionId          │ (log)   │ creditScore          │
│  applicantId            │         │ delinquencyCount     │
│  riskCategory           │         │ inquiryCount         │
│  overallScore           │         │ openTradelineCount   │
│  ...                    │         │ totalCreditLimit     │
└─────────────────────────┘         │ totalBalance         │
                                    │ errorCode            │
                                    └──────────────────────┘
                                             │ map
                                             ▼
┌─────────────────────────┐         ┌──────────────────────┐
│   CreditRiskResType     │◀────────│   CreditScoreDetail  │
│   (ENT-002)             │ (embed) │   (ENT-005)          │
│                         │         └──────────────────────┘
│ applicantId             │
│ riskCategory            │         ┌──────────────────────────┐
│ overallScore            │ 1──────▶│ IncomeVerificationDetail │
│ recommendation          │ (embed) │ (ENT-008)                │
│ accuracyCode            │         └──────────────────────────┘
│ scoringModelVersion     │
│ riskFactors             │         ┌──────────────────────┐
│ responseHeader ────────▶│ 1──────▶│ EmploymentRiskDetail │
│                         │ (embed) │ (ENT-007)            │
│                         │         └──────────────────────┘
│                         │
│                         │         ┌──────────────────────┐
│                         │ 1──────▶│ DebtServiceDetail    │
│                         │ (embed) │ (ENT-006)            │
│                         │         └──────────────────────┘
└─────────────────────────┘
       │ ResponseHeader (ENT-004)
       ▼
┌──────────────────┐
│  ResponseHeader  │
│  (ENT-004)       │
│  transactionId   │
│  timestamp       │
│  statusCode      │
│  statusMessage   │
└──────────────────┘
```

---

## 6. Enumerated Values / Reference Data

### 6.1 ProvinceType (ENT-009)

| **Entity/Column**               | **Code**   | **Display Value**              | **Description**                             | **Used in Source**                                      | **Active** |
|---------------------------------|------------|--------------------------------|---------------------------------------------|---------------------------------------------------------|------------|
| CreditRiskReqType.province      | ON         | Ontario                        | Province of Ontario                         | ✅ Yes — `CreditRiskRequestValidator.java:72`           | Yes        |
| CreditRiskReqType.province      | QC         | Quebec                         | Province of Quebec                          | ✅ Yes — `CreditRiskRequestValidator.java:72`           | Yes        |
| CreditRiskReqType.province      | BC         | British Columbia               | Province of British Columbia                | ✅ Yes — `CreditRiskRequestValidator.java:72`           | Yes        |
| CreditRiskReqType.province      | AB         | Alberta                        | Province of Alberta                         | ✅ Yes — `CreditRiskRequestValidator.java:72`           | Yes        |
| CreditRiskReqType.province      | MB         | Manitoba                       | Province of Manitoba                        | ✅ Yes — enum defined; ❌ not in SUPPORTED_PROVINCE_LIST | Yes        |
| CreditRiskReqType.province      | SK         | Saskatchewan                   | Province of Saskatchewan                    | ✅ Yes — enum defined; ❌ not in SUPPORTED_PROVINCE_LIST | Yes        |
| CreditRiskReqType.province      | NS         | Nova Scotia                    | Province of Nova Scotia                     | ✅ Yes — enum defined; ❌ not in SUPPORTED_PROVINCE_LIST | Yes        |
| CreditRiskReqType.province      | NB         | New Brunswick                  | Province of New Brunswick                   | ✅ Yes — enum defined; ❌ not in SUPPORTED_PROVINCE_LIST | Yes        |
| CreditRiskReqType.province      | NL         | Newfoundland and Labrador      | Province of Newfoundland and Labrador       | ✅ Yes — enum defined; ❌ not in SUPPORTED_PROVINCE_LIST | Yes        |
| CreditRiskReqType.province      | PE         | Prince Edward Island           | Province of Prince Edward Island            | ✅ Yes — enum defined; ❌ not in SUPPORTED_PROVINCE_LIST | Yes        |
| CreditRiskReqType.province      | NT         | Northwest Territories          | Territory of Northwest Territories          | ✅ Yes — enum defined; ❌ not in SUPPORTED_PROVINCE_LIST | Yes        |
| CreditRiskReqType.province      | YT         | Yukon                          | Territory of Yukon                          | ✅ Yes — enum defined; ❌ not in SUPPORTED_PROVINCE_LIST | Yes        |
| CreditRiskReqType.province      | NU         | Nunavut                        | Territory of Nunavut                        | ✅ Yes — enum defined; ❌ not in SUPPORTED_PROVINCE_LIST | Yes        |

> **Note:** `SUPPORTED_PROVINCE_LIST=ON,BC,AB,QC` in `app_config.properties` implies only 4 provinces are actively supported at runtime, despite all 13 being defined in the enum. The validator uses `ProvinceType.fromValue()` (accepts all 13); the `SUPPORTED_PROVINCE_LIST` property is not wired to the validator in any observed processor.

### 6.2 RequestChannelType (ENT-010)

| **Entity/Column**                  | **Code**    | **Display Value** | **Description**                               | **Used in Source**                                                          | **Active** |
|------------------------------------|-------------|-------------------|-----------------------------------------------|-----------------------------------------------------------------------------|------------|
| CreditRiskReqType.requestChannel   | ONLINE      | Online            | Web browser / online portal channel           | ❌ No — enum defined; field stored as String; no enum validation applied     | Yes        |
| CreditRiskReqType.requestChannel   | MOBILE      | Mobile            | Mobile application channel                    | ❌ No — enum defined; field stored as String; no enum validation applied     | Yes        |
| CreditRiskReqType.requestChannel   | BRANCH      | Branch            | Physical branch channel                       | ❌ No — enum defined; field stored as String; no enum validation applied     | Yes        |
| CreditRiskReqType.requestChannel   | BROKER      | Broker            | Broker / intermediary channel                 | ❌ No — enum defined; field stored as String; no enum validation applied     | Yes        |
| CreditRiskReqType.requestChannel   | CALL_CENTER | Call Center       | Telephone / call center channel               | ❌ No — enum defined; field stored as String; no enum validation applied     | Yes        |
| CreditRiskReqType.requestChannel   | API         | API               | Direct API integration channel                | ❌ No — enum defined; field stored as String; no enum validation applied     | Yes        |
| CreditRiskReqType.requestChannel   | BATCH       | Batch             | Automated batch processing channel            | ❌ No — enum defined; field stored as String; no enum validation applied     | Yes        |

> **Note:** `RequestChannelType` enum exists in the model package but `CreditRiskReqType.requestChannel` is declared as `String`. No validator enforces enum membership. The enum itself is currently unused in processing logic.

---

## 7. API Data Structures

### 7.1 REST Endpoint — Credit Risk Assessment Request

| **Attribute**        | **Details**                                                             |
|----------------------|-------------------------------------------------------------------------|
| **Structure Name**   | CreditRisk REST Query Parameters                                        |
| **Used In**          | `GET /service/rest/creditrisk/assess`                                   |
| **Format**           | HTTP Query Parameters (URL-encoded)                                     |

| **Field Name**     | **Data Type** | **Required** | **Validation**                                           | **Usage Status** | **Default/Hardcoded Value** | **Mapping Reference**              | **Description**                          |
|--------------------|---------------|--------------|----------------------------------------------------------|------------------|-----------------------------|------------------------------------|------------------------------------------|
| applicantId        | String        | Yes          | CR-001: not blank                                        | Active           | None                        | ENT-001.applicantId                | Unique applicant identifier              |
| firstName          | String        | Yes          | CR-002: not blank                                        | Active           | None                        | ENT-001.firstName                  | Applicant first name                     |
| lastName           | String        | Yes          | CR-003: not blank                                        | Active           | None                        | ENT-001.lastName                   | Applicant last name                      |
| dateOfBirth        | String        | Yes          | CR-004: not blank; format `YYYY-MM-DD`                   | Active           | None                        | ENT-001.dateOfBirth                | Date of birth (ISO format)               |
| sin                | String        | Yes          | CR-005: not blank; format `NNN-NNN-NNN`                  | Active           | None                        | ENT-001.socialInsuranceNumber      | Canadian Social Insurance Number         |
| employmentStatus   | String        | No           | None                                                     | Active           | None                        | ENT-001.employmentStatus           | Employment type (FULL_TIME, etc.)        |
| annualIncome       | Double        | No           | None (no range check)                                    | Active           | None                        | ENT-001.annualIncome               | Annual income in CAD                     |
| province           | String        | No           | CR-006: must match ProvinceType enum if provided         | Active           | None                        | ENT-001.province                   | 2-char Canadian province/territory code  |
| postalCode         | String        | No           | CR-007: format `A1A 1A1` if provided                     | Active           | None                        | ENT-001.postalCode                 | Canadian postal code                     |
| productType        | String        | No           | None                                                     | Active           | None                        | ENT-001.productType                | Credit product type                      |
| requestedAmount    | Double        | No           | None (no range check)                                    | Active           | None                        | ENT-001.requestedAmount            | Requested loan / credit amount in CAD    |

#### Sample REST Request
```
GET /service/rest/creditrisk/assess
    ?applicantId=APP-20240001
    &firstName=John
    &lastName=Smith
    &dateOfBirth=1985-06-15
    &sin=123-456-789
    &employmentStatus=FULL_TIME
    &annualIncome=75000.00
    &province=ON
    &postalCode=M5V%201J1
    &productType=MORTGAGE
    &requestedAmount=350000.00
```

---

### 7.2 REST Endpoint — Credit Risk Assessment Response

| **Attribute**        | **Details**                                           |
|----------------------|-------------------------------------------------------|
| **Structure Name**   | CreditRiskResType (XML serialized)                    |
| **Used In**          | `GET /service/rest/creditrisk/assess` response        |
| **Format**           | `application/xml` (JAXB marshalled)                   |

| **Field Name**                              | **Data Type** | **Required** | **Validation** | **Usage Status** | **Default/Hardcoded Value**           | **Mapping Reference**   | **Description**                              |
|---------------------------------------------|---------------|--------------|----------------|------------------|---------------------------------------|-------------------------|----------------------------------------------|
| responseHeader.transactionId                | String        | No           | None           | Active           | None                                  | ENT-004.transactionId   | Echoed transaction ID                        |
| responseHeader.timestamp                    | String        | No           | None           | Active           | Current timestamp                     | ENT-004.timestamp       | Response generation time                     |
| responseHeader.statusCode                   | String        | No           | None           | Active           | "SUCCESS" or "W215E"                  | ENT-004.statusCode      | Processing outcome code                      |
| responseHeader.statusMessage                | String        | No           | None           | Active           | "Assessment completed successfully"   | ENT-004.statusMessage   | Human-readable outcome message               |
| applicantId                                 | String        | No           | None           | Active           | None                                  | ENT-002.applicantId     | Echoed applicant ID                          |
| riskCategory                                | String        | No           | None           | Active           | "INDETERMINATE" (bureau error)        | ENT-002.riskCategory    | Risk category (LOW/MODERATE/HIGH/INDETERMINATE) |
| overallScore                                | Integer       | No           | None           | Active           | 0 (bureau error)                      | ENT-002.overallScore    | Composite risk score 0–1000                  |
| recommendation                              | String        | No           | None           | Active           | "REFER_TO_UNDERWRITER" (bureau error) | ENT-002.recommendation  | Decisioning recommendation                   |
| accuracyCode                                | String        | No           | None           | Active           | "LM" or "FD"                          | ENT-002.accuracyCode    | Assessment accuracy code                     |
| scoringModelVersion                         | String        | No           | None           | Active           | `<strategy>-v1.0`                     | ENT-002.scoringModelVersion | Scoring model identifier                 |
| creditScoreDetail.bureauScore               | Integer       | No           | None           | Active           | None                                  | ENT-005.bureauScore     | Raw bureau credit score                      |
| creditScoreDetail.bureauScoreRange          | String        | No           | None           | Active           | Derived label                         | ENT-005.bureauScoreRange | Score range category                        |
| creditScoreDetail.delinquencyCount          | Integer       | No           | None           | Active           | None                                  | ENT-005.delinquencyCount | Number of delinquencies                     |
| creditScoreDetail.inquiryCount              | Integer       | No           | None           | Active           | None                                  | ENT-005.inquiryCount    | Number of credit inquiries                   |
| creditScoreDetail.openAccountCount          | Integer       | No           | None           | Active           | None                                  | ENT-005.openAccountCount | Open trade line count                        |
| creditScoreDetail.totalCreditLimit          | Double        | No           | None           | Active           | None                                  | ENT-005.totalCreditLimit | Sum of all credit limits                    |
| creditScoreDetail.totalBalance              | Double        | No           | None           | Active           | None                                  | ENT-005.totalBalance    | Sum of outstanding balances                  |
| creditScoreDetail.utilizationRate           | Double        | No           | None           | Active           | None                                  | ENT-005.utilizationRate | Credit utilization ratio                     |
| incomeVerification.verificationStatus       | String        | No           | None           | Active           | "SELF_REPORTED"                       | ENT-008.verificationStatus | Income verification status               |
| incomeVerification.reportedIncome           | Double        | No           | None           | Active           | None                                  | ENT-008.reportedIncome  | Reported annual income                       |
| incomeVerification.verifiedIncome           | Double        | No           | None           | Active           | Mirrors reportedIncome                | ENT-008.verifiedIncome  | Verified income (same as reported)           |
| incomeVerification.incomeSource             | String        | No           | None           | Active           | From employmentStatus                 | ENT-008.incomeSource    | Income source description                    |
| incomeVerification.debtToIncomeRatio        | Double        | No           | None           | Active           | None                                  | ENT-008.debtToIncomeRatio | Debt-to-income ratio                       |
| employmentRisk.employmentType               | String        | No           | None           | Active           | From employmentStatus                 | ENT-007.employmentType  | Employment type label                        |
| employmentRisk.yearsEmployed                | Integer       | No           | None           | Dead             | Never set                             | ENT-007.yearsEmployed   | Years employed — never populated             |
| employmentRisk.industryCategory             | String        | No           | None           | Dead             | Never set                             | ENT-007.industryCategory | Industry category — never populated         |
| employmentRisk.riskLevel                    | String        | No           | None           | Active           | Derived (LOW/MODERATE/etc.)           | ENT-007.riskLevel       | Employment risk level                        |
| debtService.totalMonthlyDebt                | Double        | No           | None           | Active           | Calculated                            | ENT-006.totalMonthlyDebt | Estimated monthly debt                      |
| debtService.totalMonthlyIncome              | Double        | No           | None           | Active           | Calculated                            | ENT-006.totalMonthlyIncome | Monthly income derived                     |
| debtService.debtServiceRatio                | Double        | No           | None           | Active           | Calculated                            | ENT-006.debtServiceRatio | GDS ratio                                   |
| debtService.totalDebtServiceRatio           | Double        | No           | None           | Active           | Calculated                            | ENT-006.totalDebtServiceRatio | TDS ratio                              |
| debtService.affordabilityRating             | String        | No           | None           | Active           | Derived                               | ENT-006.affordabilityRating | Affordability rating                     |
| riskFactors.factor                          | String[]      | No           | None           | Active           | None                                  | ENT-002.riskFactors     | List of risk factor codes                    |

#### Sample Response Payload
```xml
<CreditRiskResponse>
  <responseHeader>
    <transactionId>TXN-20240001</transactionId>
    <timestamp>2024-03-31T09:51:00.000+0000</timestamp>
    <statusCode>SUCCESS</statusCode>
    <statusMessage>Assessment completed successfully</statusMessage>
  </responseHeader>
  <applicantId>APP-20240001</applicantId>
  <riskCategory>MODERATE</riskCategory>
  <overallScore>685</overallScore>
  <recommendation>APPROVE_WITH_CONDITIONS</recommendation>
  <accuracyCode>FD</accuracyCode>
  <scoringModelVersion>standard-v1.0</scoringModelVersion>
  <creditScoreDetail>
    <bureauScore>710</bureauScore>
    <bureauScoreRange>GOOD</bureauScoreRange>
    <delinquencyCount>0</delinquencyCount>
    <inquiryCount>2</inquiryCount>
    <openAccountCount>5</openAccountCount>
    <totalCreditLimit>35000.00</totalCreditLimit>
    <totalBalance>14000.00</totalBalance>
    <utilizationRate>0.40</utilizationRate>
  </creditScoreDetail>
  <incomeVerification>
    <verificationStatus>SELF_REPORTED</verificationStatus>
    <reportedIncome>75000.00</reportedIncome>
    <verifiedIncome>75000.00</verifiedIncome>
    <incomeSource>FULL_TIME</incomeSource>
    <debtToIncomeRatio>0.067</debtToIncomeRatio>
  </incomeVerification>
  <employmentRisk>
    <employmentType>FULL_TIME</employmentType>
    <riskLevel>LOW</riskLevel>
  </employmentRisk>
  <debtService>
    <totalMonthlyDebt>420.00</totalMonthlyDebt>
    <totalMonthlyIncome>6250.00</totalMonthlyIncome>
    <debtServiceRatio>0.067</debtServiceRatio>
    <totalDebtServiceRatio>0.38</totalDebtServiceRatio>
    <affordabilityRating>AFFORDABLE</affordabilityRating>
  </debtService>
  <riskFactors>
    <factor>HIGH_CREDIT_UTILIZATION</factor>
  </riskFactors>
</CreditRiskResponse>
```

---

### 7.3 SOAP Endpoint — Bureau Inquiry Request

| **Attribute**        | **Details**                                                                      |
|----------------------|----------------------------------------------------------------------------------|
| **Structure Name**   | BureauInquiryRequest                                                             |
| **Used In**          | `POST http://ws.esb.nexgen.com/bureau/v1` — operation: `inquire`                 |
| **Format**           | SOAP/XML — namespace: `http://ws.esb.nexgen.com/bureau/v1`                       |

#### Sample SOAP Request
```xml
<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"
                  xmlns:bur="http://ws.esb.nexgen.com/bureau/v1">
  <soapenv:Header/>
  <soapenv:Body>
    <bur:BureauInquiryRequest>
      <requestId>dev.nexgen.com-a1b2c3d4-e5f6-...</requestId>
      <timestamp>2024-03-31T09:51:00.000+0000</timestamp>
      <subscriber>
        <subscriberCode>NEXGEN-001</subscriberCode>
        <subscriberName>NexGen Financial</subscriberName>
      </subscriber>
      <subject>
        <firstName>John</firstName>
        <lastName>Smith</lastName>
        <dateOfBirth>1985-06-15</dateOfBirth>
        <socialInsuranceNumber>123-456-789</socialInsuranceNumber>
        <province>ON</province>
        <postalCode>M5V 1J1</postalCode>
      </subject>
      <productType>MORTGAGE</productType>
    </bur:BureauInquiryRequest>
  </soapenv:Body>
</soapenv:Envelope>
```

---

### 7.4 SOAP Endpoint — Bureau Inquiry Response

| **Attribute**        | **Details**                                                                     |
|----------------------|---------------------------------------------------------------------------------|
| **Structure Name**   | BureauInquiryResponse                                                           |
| **Used In**          | Response from `inquire` operation on `BureauScoreService`                       |
| **Format**           | SOAP/XML — namespace: `http://ws.esb.nexgen.com/bureau/v1`                      |

#### Sample SOAP Response
```xml
<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/">
  <soapenv:Body>
    <BureauInquiryResponse>
      <requestId>dev.nexgen.com-a1b2c3d4-e5f6-...</requestId>
      <responseId>BUR-RESP-20240001</responseId>
      <creditScore>710</creditScore>
      <delinquencyCount>0</delinquencyCount>
      <inquiryCount>2</inquiryCount>
      <openTradelineCount>5</openTradelineCount>
      <totalCreditLimit>35000.00</totalCreditLimit>
      <totalBalance>14000.00</totalBalance>
      <errorCode/>
      <errorMessage/>
    </BureauInquiryResponse>
  </soapenv:Body>
</soapenv:Envelope>
```

---

## 8. Configuration Data

All properties are loaded from `app_config.properties` at path `${NEXGEN_CONFIG_PATH}/creditrisk/app_config.properties` via OSGi Blueprint Camel property placeholder.

| **Key/Property**            | **Data Type** | **Default Value**                                        | **Description**                                                         | **Source File**         |
|-----------------------------|---------------|----------------------------------------------------------|-------------------------------------------------------------------------|-------------------------|
| BUREAU_ENDPOINT_URL         | String        | `https://qa.nexgenservices.com:9443/services/bureau/creditcheck` | External credit bureau SOAP endpoint URL                      | app_config.properties   |
| BUREAU_REQUEST_ID_PREFIX    | String        | `dev.nexgen.com-`                                        | Prefix prepended to UUID to form bureau request IDs                     | app_config.properties   |
| BUREAU_SUBSCRIBER_CODE      | String        | `NEXGEN-001`                                             | Bureau subscriber identification code                                   | app_config.properties   |
| BUREAU_SUBSCRIBER_NAME      | String        | `NexGen Financial`                                       | Bureau subscriber display name                                          | app_config.properties   |
| BUREAU_CONNECTION_TIMEOUT   | Integer       | `30000`                                                  | Bureau SOAP connection timeout in milliseconds                          | app_config.properties   |
| BUREAU_RECEIVE_TIMEOUT      | Integer       | `30000`                                                  | Bureau SOAP response receive timeout in milliseconds                    | app_config.properties   |
| SCORING_DEFAULT_STRATEGY    | String        | `standard`                                               | Default scoring strategy; used by ScoringStrategyProcessor              | app_config.properties   |
| MONGO_HOST                  | String        | `localhost`                                              | MongoDB server hostname                                                 | app_config.properties   |
| MONGO_PORT                  | Integer       | `27017`                                                  | MongoDB server port                                                     | app_config.properties   |
| MONGO_DB                    | String        | `nexgen_creditrisk`                                      | MongoDB database name for transaction logging                           | app_config.properties   |
| MONGO_COLLECTION            | String        | `transactions`                                           | MongoDB collection name for transaction log documents                   | app_config.properties   |
| LDAP_LOGIN                  | String        | `LDAPLogin`                                              | JAAS login context name for LDAP/security authentication                | app_config.properties   |
| SUPPORTED_PROVINCE_LIST     | String        | `ON,BC,AB,QC`                                            | Comma-separated list of currently supported province codes              | app_config.properties   |

> **Note:** Only 13 properties are present in the discovered `app_config.properties`. The issue brief cites 17 properties; 5 of the 13 discovered properties (`BUREAU_CONNECTION_TIMEOUT`, `BUREAU_RECEIVE_TIMEOUT`, `BUREAU_REQUEST_ID_PREFIX`, `BUREAU_SUBSCRIBER_CODE`, and `BUREAU_SUBSCRIBER_NAME`) are present in config but not all are wired as Blueprint placeholders. The blueprint explicitly references `{{BUREAU_REQUEST_ID_PREFIX}}`, `{{BUREAU_SUBSCRIBER_CODE}}`, and `{{BUREAU_SUBSCRIBER_NAME}}` alongside `{{BUREAU_ENDPOINT_URL}}`; however, the timeout properties (`BUREAU_CONNECTION_TIMEOUT`, `BUREAU_RECEIVE_TIMEOUT`) appear in config but are not referenced in any discovered Blueprint or processor code — gap noted in Section 9. The remaining 4 properties cited in the issue brief (beyond the 13 found) were not present in the discovered `app_config.properties`.

---

## 9. Data Quality Observations

| **#** | **Entity**                  | **Issue**                                                                                          | **Severity** | **Recommendation**                                                                             |
|-------|-----------------------------|----------------------------------------------------------------------------------------------------|--------------|-------------------------------------------------------------------------------------------------|
| 1     | CreditRiskReqType           | `requestChannel` is declared as `String` but `RequestChannelType` enum exists; no validation applied | Medium     | Validate `requestChannel` against `RequestChannelType` enum values in `CreditRiskRequestValidator` |
| 2     | CreditRiskReqType           | `annualIncome` and `requestedAmount` have no range or non-negative checks                         | Medium       | Add range validation: non-negative, realistic maximum (e.g., < 10,000,000)                     |
| 3     | CreditRiskReqType           | `employmentStatus` accepts any string; no allowed-value set enforced                               | Medium       | Define an `EmploymentStatusType` enum or add a whitelist validation                            |
| 4     | CreditRiskReqType           | `productType` accepts any string; no allowed-value set enforced                                    | Low          | Define a `ProductType` enum or add a validation step                                           |
| 5     | EmploymentRiskDetail        | `yearsEmployed` and `industryCategory` fields are never populated (Dead fields)                   | Medium       | Populate from request or remove from model to avoid confusion                                  |
| 6     | IncomeVerificationDetail    | `verifiedIncome` is always set equal to `reportedIncome`; no actual verification occurs            | High         | Document this limitation explicitly; implement third-party verification integration or rename field |
| 7     | RequestHeader               | `userId` field is never populated or read by any processor                                        | Low          | Remove or implement population from security context                                           |
| 8     | RequestChannelType          | Enum values differ from the issue brief (ONLINE/MOBILE/BRANCH/BROKER/CALL_CENTER/API/BATCH vs. REST/SOAP/GATEWAY/MOBILE/WEB/BATCH/API) | Low | Reconcile enum values with actual integration channels |
| 9     | Config: BUREAU_CONNECTION_TIMEOUT | Property exists in config but is not wired to any CXF client endpoint configuration         | Medium       | Wire to `cxf:cxfEndpoint` bureau client properties for proper timeout enforcement               |
| 10    | Config: BUREAU_RECEIVE_TIMEOUT    | Property exists in config but is not wired to any CXF client endpoint configuration         | Medium       | Wire to `cxf:cxfEndpoint` bureau client properties                                             |
| 11    | Config: SUPPORTED_PROVINCE_LIST   | Defines 4 supported provinces (ON,BC,AB,QC) but validator accepts all 13 enum values        | Medium       | Wire `SUPPORTED_PROVINCE_LIST` to `CreditRiskRequestValidator` to enforce runtime restrictions  |
| 12    | CreditRiskResType           | `riskCategory`, `overallScore` have no defined value enumerations or constraints in model          | Low          | Document allowed values; consider enum for `riskCategory`                                      |
| 13    | BureauInquiryResponse       | `requestId` and `responseId` fields are received but never logged or correlated                   | Low          | Log `requestId`/`responseId` to MongoDB for full transaction correlation                        |

---

## 10. Validation Rules Matrix

### 10.1 Existing Validations

| **Entity / DTO**         | **Field Name**           | **Validation Type** | **Validation Logic**                                              | **Source Location**                          | **Notes**                                          |
|--------------------------|--------------------------|---------------------|-------------------------------------------------------------------|----------------------------------------------|----------------------------------------------------|
| CreditRiskReqType        | (entire request)         | Required            | Request body must not be null                                     | `CreditRiskRequestValidator.java:36`         | Rule CR-000                                        |
| CreditRiskReqType        | applicantId              | Required            | Must not be blank (`StringUtils.isBlank`)                         | `CreditRiskRequestValidator.java:42`         | Rule CR-001                                        |
| CreditRiskReqType        | firstName                | Required            | Must not be blank                                                 | `CreditRiskRequestValidator.java:46`         | Rule CR-002                                        |
| CreditRiskReqType        | lastName                 | Required            | Must not be blank                                                 | `CreditRiskRequestValidator.java:50`         | Rule CR-003                                        |
| CreditRiskReqType        | dateOfBirth              | Required + Format   | Not blank; must match `^\d{4}-\d{2}-\d{2}$`                      | `CreditRiskRequestValidator.java:54-59`      | Rule CR-004; format only, no semantic date check   |
| CreditRiskReqType        | socialInsuranceNumber    | Required + Format   | Not blank; must match `^\d{3}-?\d{3}-?\d{3}$`                    | `CreditRiskRequestValidator.java:62-67`      | Rule CR-005; format only, no checksum validation   |
| CreditRiskReqType        | province                 | Format / Enum       | If present, must match `ProvinceType.fromValue()` (all 13 codes)  | `CreditRiskRequestValidator.java:70-75`      | Rule CR-006; optional field                        |
| CreditRiskReqType        | postalCode               | Format              | If present, must match `^[A-Za-z]\d[A-Za-z]\s?\d[A-Za-z]\d$`    | `CreditRiskRequestValidator.java:78-80`      | Rule CR-007; optional field                        |

### 10.2 Missing Validations (Gaps)

| **Entity / DTO**         | **Field Name**        | **Expected Validation**                                      | **Risk if Not Validated**                                         | **Severity** | **Linked Gap**  |
|--------------------------|-----------------------|--------------------------------------------------------------|-------------------------------------------------------------------|--------------|-----------------|
| CreditRiskReqType        | dateOfBirth           | Semantic date validity (real calendar date); age ≥ 18        | Invalid dates accepted (e.g. 1985-13-45); underage applicants    | High         | GAP-001         |
| CreditRiskReqType        | socialInsuranceNumber | Luhn/checksum algorithm validation for Canadian SIN          | Invalid SINs pass format check; fraudulent SINs not rejected     | High         | GAP-002         |
| CreditRiskReqType        | annualIncome          | Non-negative; reasonable upper bound (e.g. ≤ 10,000,000)    | Negative income causes negative ratio calculations               | Medium       | GAP-003         |
| CreditRiskReqType        | requestedAmount       | Non-negative; reasonable upper bound                         | Negative requested amount causes TDS miscalculation              | Medium       | GAP-004         |
| CreditRiskReqType        | requestChannel        | Must match `RequestChannelType` enum values                  | Invalid channel values reach MongoDB log unchecked               | Medium       | GAP-005         |
| CreditRiskReqType        | employmentStatus      | Whitelist (FULL_TIME, PART_TIME, SELF_EMPLOYED, etc.)        | Unknown employment status maps to HIGH risk by default           | Medium       | GAP-006         |
| CreditRiskReqType        | productType           | Whitelist of supported product types                         | Invalid product type forwarded to bureau unvalidated             | Low          | GAP-007         |
| CreditRiskReqType        | province              | Constrain to `SUPPORTED_PROVINCE_LIST` (not all 13 enum values) | Provinces outside supported list accepted at validation layer  | Medium       | GAP-008         |
| CreditScoreDetail        | bureauScore           | Range check: 300–900                                         | Out-of-range scores cause incorrect risk categorization          | Medium       | GAP-009         |
| CreditScoreDetail        | utilizationRate       | Range check: 0.0–1.0                                         | Values >1.0 possible if bureau data is inconsistent              | Low          | GAP-010         |
| RequestHeader            | transactionId         | Required; not blank                                          | Null transaction ID causes MongoDB log entry with no correlation | Medium       | GAP-011         |
| RequestHeader            | timestamp             | Format check: ISO-8601                                       | Malformed timestamps cause processing errors downstream          | Low          | GAP-012         |

---

## 11. NoSQL / Document Store Field Catalog

### 11.1 Collection: transactions

| **Attribute**           | **Details**                                                                          |
|-------------------------|--------------------------------------------------------------------------------------|
| **Collection Name**     | transactions                                                                         |
| **Database**            | nexgen_creditrisk                                                                    |
| **Purpose**             | Wire Tap audit log of every processed credit risk transaction (both successful and error paths) |
| **Estimated Documents** | Unbounded; one document per transaction                                              |
| **TTL / Expiry**        | None defined (no TTL index configured in discovered code)                            |

#### Document Fields

| **#** | **Field Path**     | **BSON Type** | **Indexed** | **Required** | **Sample Value**                       | **Description**                                                      | **Mapping Reference**          |
|-------|--------------------|---------------|-------------|--------------|----------------------------------------|----------------------------------------------------------------------|--------------------------------|
| 1     | transactionId      | String        | No          | No           | `TXN-20240001`                         | Unique transaction identifier from RequestHeader; LoggerConstants: `LOG_TRANSACTION_ID` | ENT-003.transactionId          |
| 2     | applicantId        | String        | No          | No           | `APP-20240001`                         | Applicant identifier; LoggerConstants: `LOG_APPLICANT_ID`           | ENT-001.applicantId            |
| 3     | province           | String        | No          | No           | `ON`                                   | Province code; LoggerConstants: `LOG_PROVINCE`                      | ENT-001.province               |
| 4     | productType        | String        | No          | No           | `MORTGAGE`                             | Credit product type; LoggerConstants: `LOG_PRODUCT_TYPE`            | ENT-001.productType            |
| 5     | requestChannel     | String        | No          | No           | `ONLINE`                               | Request channel; LoggerConstants: `LOG_REQUEST_CHANNEL`             | ENT-001.requestChannel         |
| 6     | sourceSystem       | String        | No          | No           | `GUIDEWIRE-PORTAL`                     | Originating system; LoggerConstants: `LOG_SOURCE_SYSTEM`            | ENT-003.sourceSystem           |
| 7     | timestamp          | String        | No          | Yes          | `2024-03-31T09:51:00.000+0000`         | Log insertion timestamp (ISO-8601); generated at log time; LoggerConstants: `LOG_TIMESTAMP` | TransactionLogger.java:39 |
| 8     | riskCategory       | String        | No          | No           | `MODERATE`                             | Risk category result; present on success path; LoggerConstants: `LOG_RISK_CATEGORY` | ENT-002.riskCategory    |
| 9     | overallScore       | Integer       | No          | No           | `685`                                  | Overall risk score; present on success path; LoggerConstants: `LOG_OVERALL_SCORE` | ENT-002.overallScore      |
| 10    | recommendation     | String        | No          | No           | `APPROVE_WITH_CONDITIONS`              | Decisioning recommendation; present on success path; LoggerConstants: `LOG_RECOMMENDATION` | ENT-002.recommendation |
| 11    | status             | String        | No          | Yes          | `success` or `error`                   | Transaction processing outcome; hardcoded: "success"/"error" — `TransactionLogger.java:46,48` | None (hardcoded) |
| 12    | errorCode          | String        | No          | No           | `CR-301`                               | Error code; present on error path only; LoggerConstants: `LOG_ERROR_CODE` | ErrorProcessor / exchange property |
| 13    | errorMessage       | String        | No          | No           | `Bureau data unavailable`              | Error message; present on error path only; LoggerConstants: `LOG_ERROR_MESSAGE` | exchange property |
| 14    | bureauScore        | Integer       | No          | No           | `710`                                  | Bureau credit score — NOTE: **not actually populated** in `TransactionLogger.java`; defined in LoggerConstants: `LOG_BUREAU_SCORE` but missing from logDoc construction | ENT-005.bureauScore — GAP |
| 15    | application        | String        | No          | No           | `nexgen-creditrisk-gateway`            | Application name — NOTE: **not actually populated** in `TransactionLogger.java`; defined in LoggerConstants: `LOG_APPLICATION` but missing from logDoc construction | Hardcoded — GAP |
| 16    | requestPayload     | String        | No          | No           | `<CreditRiskRequest>...</CreditRiskRequest>` | Serialized request payload — NOTE: **not actually populated**; defined in LoggerConstants: `LOG_REQUEST_PAYLOAD` but missing from logDoc | ENT-001 — GAP |
| 17    | responsePayload    | String        | No          | No           | `<CreditRiskResponse>...</CreditRiskResponse>` | Serialized response payload — NOTE: **not actually populated**; defined in LoggerConstants: `LOG_RESPONSE_PAYLOAD` but missing from logDoc | ENT-002 — GAP |

> **Data Quality Note:** LoggerConstants defines 17 field constants (`LOG_TRANSACTION_ID`, `LOG_APPLICANT_ID`, `LOG_PROVINCE`, `LOG_PRODUCT_TYPE`, `LOG_REQUEST_CHANNEL`, `LOG_SOURCE_SYSTEM`, `LOG_RISK_CATEGORY`, `LOG_OVERALL_SCORE`, `LOG_RECOMMENDATION`, `LOG_BUREAU_SCORE`, `LOG_STATUS`, `LOG_ERROR_CODE`, `LOG_ERROR_MESSAGE`, `LOG_REQUEST_PAYLOAD`, `LOG_RESPONSE_PAYLOAD`, `LOG_TIMESTAMP`, `LOG_APPLICATION`). However, `TransactionLogger.java` only populates **13** of these fields. The 4 missing fields — `LOG_BUREAU_SCORE`, `LOG_APPLICATION`, `LOG_REQUEST_PAYLOAD`, and `LOG_RESPONSE_PAYLOAD` — are declared as constants but never used as MongoDB document keys. This is a data completeness gap.

#### Indexes

| **Index Name**  | **Fields**     | **Type** | **Unique** | **TTL**  | **Notes**                                    |
|-----------------|----------------|----------|------------|----------|----------------------------------------------|
| (none defined)  | —              | —        | —          | None     | No indexes configured; query performance risk for large collections |

---

## 12. Appendices

### Appendix A: Data Type Mapping

| **Java Type**     | **JAXB/XML Type**      | **BSON Type (MongoDB)** | **REST/JSON Type** | **Notes**                                                    |
|-------------------|------------------------|-------------------------|--------------------|--------------------------------------------------------------|
| String            | xs:string              | String                  | string             |                                                              |
| Integer / int     | xs:int                 | Int32                   | number (integer)   |                                                              |
| Double / double   | xs:double              | Double                  | number (float)     | Monetary values — no BigDecimal used; rounding risk          |
| Boolean / boolean | xs:boolean             | Boolean                 | boolean            |                                                              |
| List\<String\>    | xs:sequence (element)  | Array                   | array of string    | Used for `riskFactors`                                       |
| Enum              | xs:string (XmlEnumValue) | String                | string             | Java enum serialized as its `value` string via JAXB         |
| Date (formatted)  | xs:string (ISO-8601)   | String                  | string             | Timestamps stored as formatted strings, not native Date type |

### Appendix B: Glossary

| **Term**                  | **Definition**                                                                                           |
|---------------------------|----------------------------------------------------------------------------------------------------------|
| SIN                       | Social Insurance Number — 9-digit Canadian government identification number                              |
| DSR / GDS                 | Gross Debt Service Ratio — monthly housing costs divided by monthly income                               |
| TDS                       | Total Debt Service Ratio — all monthly debt payments divided by monthly income                           |
| DTI                       | Debt-to-Income Ratio — total debt divided by gross income                                                |
| Bureau Score              | Numerical credit score provided by a credit bureau (e.g. Equifax, TransUnion); range typically 300–900  |
| Wire Tap                  | Apache Camel EIP pattern; sends a copy of a message to a secondary route without stopping the main flow  |
| JAXB                      | Java Architecture for XML Binding — maps Java classes to XML schema                                      |
| JAX-WS                    | Java API for XML Web Services — framework for SOAP web services in Java                                  |
| OSGI Blueprint             | Dependency injection framework for OSGi containers (JBoss Fuse)                                         |
| ProvinceType              | Enumeration of the 13 Canadian provinces and territories used for request validation                     |
| RequestChannelType        | Enumeration of 7 valid inbound channel identifiers; currently not enforced at validation                 |
| Scoring Strategy          | Configurable algorithm (standard/aggressive/conservative) applied to produce the overall risk score      |
| Risk Category             | Categorical risk assessment output: LOW, MODERATE, HIGH, INDETERMINATE                                   |
| Accuracy Code             | FD = Full Data assessment; LM = Limited/partial assessment (bureau data unavailable)                     |
| SUPPORTED_PROVINCE_LIST   | Runtime configuration property listing only the 4 provinces actively supported (ON, BC, AB, QC)          |
| Wire Tap Route            | Camel route `direct:logTransaction` that asynchronously logs transaction data to MongoDB                  |

### Appendix C: Field Usage Status Definitions

| **Status**       | **Definition**                                                          |
|------------------|-------------------------------------------------------------------------|
| **Active**       | Field is actively used in processing, business logic, and/or response   |
| **Logging Only** | Field is only written to the log/transaction data store                 |
| **Dead**         | Field exists in the model/DTO/schema but is never populated or consumed |
| **Deprecated**   | Field is marked or planned for removal                                  |

---

> **Document Control:**
> | Version | Date        | Author                          | Changes                                                                                          |
> |---------|-------------|----------------------------------|--------------------------------------------------------------------------------------------------|
> | 0.1     | 31-Mar-2026 | Copilot Reverse Engineering Agent | Initial draft — full catalog from source code: 10 model classes, 5 generated classes, 2 enums, REST/SOAP API structures, MongoDB collection, 13 config properties, validation rules matrix, data quality observations |
