## RE-005: Data Dictionary — All Entities, Fields, Types

### Instructions for Copilot Agent

You are generating a **completed** Data Dictionary for the `nexgen-creditrisk-gateway` JBoss Fuse service. Follow the template structure exactly. Catalog every data entity, every field, every type, every enum value.

**Template:** [`docs/re/templates/05-DATA-DICTIONARY-TEMPLATE_Version4.md`](docs/re/templates/05-DATA-DICTIONARY-TEMPLATE_Version4.md)
**Output file to create:** `docs/re/05-data-dictionary.md`

### Data Sources

| Source | Type | Technology | Purpose |
|--------|------|-----------|---------|
| MongoDB | NoSQL (Document) | mongo-java-driver 3.4.2 | Transaction logging (Wire Tap) |
| In-Memory | Java Objects | POJOs | Request/response processing |
| External Config | Properties File | `app_config.properties` | 17 externalized configuration properties |

### All Model Classes (10 classes in `model/` package)

| Class | Fields | Purpose |
|-------|--------|---------|
| `CreditRiskReqType` | 13 fields: applicantId, firstName, lastName, dateOfBirth, sinNumber, province, postalCode, productType, loanAmount, annualIncome, employmentStatus, monthlyDebtPayment, requestChannel | Inbound request |
| `CreditRiskResType` | 12 fields: responseCode, responseMessage, applicantId, creditScore, riskCategory, overallScore, recommendation, affordabilityRating, debtServiceRatio, riskFactors, scoreDetail, header | Outbound response |
| `RequestHeader` | 4 fields: requestId, timestamp, sourceSystem, channelType | Request metadata |
| `ResponseHeader` | 4 fields: responseId, timestamp, statusCode, statusMessage | Response metadata |
| `CreditScoreDetail` | 8 fields: bureauScore, scoreCategory, numberOfInquiries, totalAccounts, delinquentAccounts, creditUtilization, oldestAccountAge, recentInquiryDate | Bureau score details |
| `DebtServiceDetail` | 5 fields: monthlyDebtPayment, annualIncome, debtServiceRatio, monthlyIncome, availableIncome | Debt calculations |
| `EmploymentRiskDetail` | 4 fields: employmentStatus, employmentRiskScore, employmentStability, yearsEmployed | Employment assessment |
| `IncomeVerificationDetail` | 5 fields: annualIncome, verifiedIncome, incomeVariance, incomeVerificationStatus, incomeSource | Income assessment |

### Enum Types (2 enums)

| Enum | Values |
|------|--------|
| `ProvinceType` | ON, BC, AB, QC, MB, SK, NS, NB, PE, NL, NT, YT, NU (13 provinces/territories) |
| `RequestChannelType` | REST, SOAP, GATEWAY, MOBILE, WEB, BATCH, API (7 channels) |

### Generated Classes (5 classes in `generated/` package — from Bureau WSDL)

| Class | Fields | Purpose |
|-------|--------|---------|
| `BureauInquiryRequest` | Maps from CreditRiskReqType | Outbound bureau SOAP request |
| `BureauInquiryResponse` | 10 fields | Inbound bureau SOAP response |
| `BureauScoreService` | JAX-WS interface | Bureau SOAP service contract |
| `BureauSubscriber` | subscriberCode, subscriberName | Bureau credential info |
| `BureauSubject` | firstName, lastName, dateOfBirth, sinNumber | Bureau inquiry subject |

### Configuration Properties (app_config.properties — 17 properties)

| Property | Type | Default | Purpose |
|----------|------|---------|---------|
| BUREAU_ENDPOINT_URL | String | (external) | Bureau SOAP endpoint URL |
| BUREAU_REQUEST_ID_PREFIX | String | (prefix) | Request ID generation prefix |
| BUREAU_SUBSCRIBER_CODE | String | (code) | Bureau subscriber identification |
| BUREAU_SUBSCRIBER_NAME | String | (name) | Bureau subscriber name |
| BUREAU_CONNECTION_TIMEOUT | Integer | 30000 | Bureau connection timeout ms |
| BUREAU_RECEIVE_TIMEOUT | Integer | 30000 | Bureau response timeout ms |
| SCORING_DEFAULT_STRATEGY | String | standard | Default scoring strategy |
| MONGO_HOST | String | (host) | MongoDB server host |
| MONGO_PORT | Integer | (port) | MongoDB server port |
| MONGO_DB | String | (db) | MongoDB database name |
| MONGO_COLLECTION | String | (collection) | MongoDB collection name |
| LDAP_LOGIN | String | LDAPLogin | JAAS login context name |
| SUPPORTED_PROVINCE_LIST | String | ON,BC,AB,QC | Comma-separated valid provinces |

### MongoDB Transaction Log Document Structure

From `TransactionLogger.java` and `LoggerConstants.java` (17 field constants):
- Document fields to catalog from LoggerConstants

### Scope — All Template Sections

1. **Data Sources** — MongoDB, in-memory, config file
2. **Entity Summary** — All 10 model classes + 5 generated classes + 2 enums
3. **Entity Details** — Per-class field inventory with data types, nullability, constraints, usage status, hardcoded values, mapping references
4. **Entity Relationship Summary** — How models relate (req→scoring→response, req→bureau mapping→bureau response)
5. **Enumerated Values** — Complete ProvinceType and RequestChannelType value lists with used-in-source status
6. **API Data Structures** — REST request params, REST response JSON, SOAP request/response XML structures
7. **Configuration Data** — All 17 properties
8. **Data Quality Observations** — Missing constraints, type safety concerns
9. **Validation Rules Matrix** — Existing validations (7 rules) and missing validations by field
10. **NoSQL / Document Store Field Catalog** — MongoDB transaction log collection schema with all fields from LoggerConstants
11. **Data Type Mapping** — Java type ↔ API type ↔ DB type mappings

### Acceptance Criteria

- [ ] All 10 model classes have ENT-XXX entries with every field documented
- [ ] All 5 generated classes are documented
- [ ] Both enums have every value listed with usage status
- [ ] REST endpoint parameters (11 query params) are documented as API data structures
- [ ] MongoDB collection schema is fully documented from LoggerConstants
- [ ] All 17 config properties are cataloged
- [ ] Validation rules matrix shows existing AND missing validations per field
- [ ] Entity relationships are diagrammed
- [ ] Output file is created at `docs/re/05-data-dictionary.md`
