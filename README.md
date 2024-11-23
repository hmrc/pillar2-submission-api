# pillar2-submission-api
Backend microservice for Pillar 2  project. Pillar 2 refers to the Global Minimum Tax being introduced by the Organisation for Economic Cooperation and Development (OECD).

The Pillar 2 Tax will ensure that global Multinational Enterprises (MNEs) with a turnover of >€750m are subject to a minimum Effective Tax Rate of 15%, i.e. a top-up tax for Medium to Large MNEs.

This microservice provides APIs for third-party applications to manage Pillar2 tax on behalf of corporations
## Running the service locally

```shell
sbt run
```

Test-only route:

```shell
sbt 'run -Dplay.http.router=testOnlyDoNotUseInAppConf.Routes'
```

To run locally:

***Not sure if this works!!!!***

Navigate to http://localhost:9949/auth-login-stub/gg-sign-in which redirects to auth-login-stub page.


## Redirect URL:
    http://localhost:10050/report-pillar2-top-up-taxes

## Affinity Group:
    Organisation

### To check test coverage:

```shell
sbt scalafmt test:scalafmt it:test::scalafmt coverage test it/test coverageReport`
```

### Integration and unit tests

To run unit tests:
```shell
sbt test
```
To run Integration tests:
```shell
sbt it/test
```

### Using Service Manager

You can use service manage to run all dependent microservices using the command below
```shell
sm2 --start PILLAR2_ALL
```
To stop services:
```shell
sm2 --stop PILLAR2_ALL
```

## Generation, Validation and Publishing
To ensure API documentation alignment with code, we generate the OpenAPI specification (OAS) directly from the route definitions using an SBT task, reducing risk and manual effort.

### Generation
To generate the YAML OAS, run:
```shell
sbt routesToYamlOas
```
The generated OAS will include definitions based on the application's routes. The output folder is 'target/swagger' by default.

### Validation
To run basic validation on the generated OAS, run: 
```shell
sbt validateOas
```
This validates the generated specification (from `sbt routesToYamlOas`) against OpenAPI standards to ensure compliance and detect any structural errors. It is, however, the API Platform team's advice that the OAS is also validated using [Swagged Editor](https://editor.swagger.io/) before publishing.

### Publishing
Generating the OAS does not automatically publish it. If the new changes warrant publication e.g. endpoints introduced/deprecated, the validated OAS needs to replace the application.yaml file in 'resources/public/api/conf/1.0'. The API Platform will detect the new changes and process the file for publication on Developer Hub.

## UKTR Validation Overview
This section of the document contains detailed test scenarios for submitting a UK Tax Return (Uktr) to the `/RESTAdapter/PLR/UKTaxReturn` endpoint. Each test includes a description, a curl example, and a key reference table.

## Key Description
The following table summarises the test cases:

| **Case**                     | **Field**                 | **Issue**                                        | **Expected Result**             |
|------------------------------|---------------------------|--------------------------------------------------|---------------------------------|
| **Valid Input (Happy Path)** | All                       | All fields valid and provided.                   | `201 Created`                   |
| **Missing Field**            | `electionDTTSingleMember` | Field is missing entirely.                       | `400 Bad Request` + Field Error |
| **Null Field**               | `electionDTTSingleMember` | Field is explicitly set to `null`.               | `400 Bad Request` + Field Error |
| **Negative Integer**         | `numberSubGroupDTT`       | Field has a negative value.                      | `400 Bad Request` + Field Error |
| **Empty Array**              | `liableEntities`          | Array is empty.                                  | `400 Bad Request` + Field Error |
| **Negative BigDecimal**      | `totalLiability`          | Field has a negative decimal value.              | `400 Bad Request` + Field Error |
| **Empty String**             | `ukChargeableEntityName`  | Field is an empty string for one `liableEntity`. | `400 Bad Request` + Field Error |

---

## Test Scenarios and Curl Commands

### 1. Valid Input (Happy Path)
**Description:**  
All fields are valid. Expected result is `201 Created`.

```bash
curl -X POST \
  http://localhost:10054/RESTAdapter/PLR/UKTaxReturn \
  -H "Content-Type: application/json" \
  -d '{
    "accountingPeriodFrom": "2024-08-14",
    "accountingPeriodTo": "2024-12-14",
    "obligationMTT": true,
    "electionUKGAAP": true,
    "liabilities": {
      "electionDTTSingleMember": true,
      "electionUTPRSingleMember": false,
      "numberSubGroupDTT": 1,
      "numberSubGroupUTPR": 2,
      "totalLiability": 10000.99,
      "totalLiabilityDTT": 5000.50,
      "totalLiabilityIIR": 4000.00,
      "totalLiabilityUTPR": 2000.75,
      "liableEntities": [
        {
          "ukChargeableEntityName": "Entity 1",
          "idType": "CRN",
          "idValue": "12345678",
          "amountOwedDTT": 1500.25,
          "amountOwedIIR": 1200.75,
          "amountOwedUTPR": 800.00
        }
      ]
    }
  }'
```

---

### 2. Missing Field
**Description:**  
The field `electionDTTSingleMember` is missing entirely. Expected result is `400 Bad Request`.

```bash
curl -X POST \
  http://localhost:10054/RESTAdapter/PLR/UKTaxReturn \
  -H "Content-Type: application/json" \
  -d '{
  "accountingPeriodFrom": "2024-08-14",
  "accountingPeriodTo": "2024-12-14",
  "obligationMTT": true,
  "electionUKGAAP": true,
  "liabilities": {
    "electionUTPRSingleMember": false,
    "numberSubGroupDTT": 1,
    "numberSubGroupUTPR": 2,
    "totalLiability": 10000.99,
    "totalLiabilityDTT": 5000.50,
    "totalLiabilityIIR": 4000.00,
    "totalLiabilityUTPR": 2000.75,
    "liableEntities": [
      {
        "ukChargeableEntityName": "Entity 1",
        "idType": "CRN",
        "idValue": "12345678",
        "amountOwedDTT": 1500.25,
        "amountOwedIIR": 1200.75,
        "amountOwedUTPR": 800.00
      }
    ]
  }
}'
```

```
Expected Response:
{
  "message": "Invalid JSON format",
  "details": [
    "Path: /liabilities/electionDTTSingleMember, Errors: error.path.missing"
  ]
}
```

---

### 3. Null Field
**Description:**  
The field `electionDTTSingleMember` is explicitly set to `null`. Expected result is `400 Bad Request`.

```bash
curl -X POST \
  http://localhost:10054/RESTAdapter/PLR/UKTaxReturn \
  -H "Content-Type: application/json" \
  -d '{
    "accountingPeriodFrom": "2024-08-14",
    "accountingPeriodTo": "2024-12-14",
    "obligationMTT": true,
    "electionUKGAAP": true,
    "liabilities": {
      "electionDTTSingleMember": null,
      "electionUTPRSingleMember": false,
      "numberSubGroupDTT": 1,
      "numberSubGroupUTPR": 2,
      "totalLiability": 10000.99,
      "totalLiabilityDTT": 5000.50,
      "totalLiabilityIIR": 4000.00,
      "totalLiabilityUTPR": 2000.75,
      "liableEntities": [
        {
          "ukChargeableEntityName": "Entity 1",
          "idType": "CRN",
          "idValue": "12345678",
          "amountOwedDTT": 1500.25,
          "amountOwedIIR": 1200.75,
          "amountOwedUTPR": 800.00
        }
      ]
    }
  }'
```

```  
  Expected Response:
{
  "message": "Invalid JSON format",
  "details": [
    "Path: /liabilities/electionDTTSingleMember, Errors: error.expected.jsboolean"
  ]
}
```

---


### Scenario 4: Invalid Input - Negative Integer

**Description:**  
The field `numberSubGroupDTT` has a negative value, which is not valid. This scenario tests validation for non-negative integer inputs.

**Curl Example:**
```bash
curl -X POST \
  http://localhost:10054/RESTAdapter/PLR/UKTaxReturn \
  -H "Content-Type: application/json" \
  -d '{
    "accountingPeriodFrom": "2024-08-14",
    "accountingPeriodTo": "2024-12-14",
    "obligationMTT": true,
    "electionUKGAAP": true,
    "liabilities": {
      "electionDTTSingleMember": true,
      "electionUTPRSingleMember": false,
      "numberSubGroupDTT": -1,
      "numberSubGroupUTPR": 2,
      "totalLiability": 10000.99,
      "totalLiabilityDTT": 5000.50,
      "totalLiabilityIIR": 4000.00,
      "totalLiabilityUTPR": 2000.75,
      "liableEntities": [
        {
          "ukChargeableEntityName": "Entity 1",
          "idType": "CRN",
          "idValue": "12345678",
          "amountOwedDTT": 1500.25,
          "amountOwedIIR": 1200.75,
          "amountOwedUTPR": 800.00
        }
      ]
    }
  }'
```
```
  Expected Response:
{
  "message": "Invalid JSON format",
  "details": [
    "numberSubGroupDTT: numberSubGroupDTT must be non-negative"
  ]
}
```

---

### Scenario 5: Invalid Input - Empty Liable Entities

**Description:**  
The `liableEntities` array is empty, violating the requirement for at least one entity.

**Curl Example:**
```bash
curl -X POST \
  http://localhost:10054/RESTAdapter/PLR/UKTaxReturn \
  -H "Content-Type: application/json" \
  -d '{
    "accountingPeriodFrom": "2024-08-14",
    "accountingPeriodTo": "2024-12-14",
    "obligationMTT": true,
    "electionUKGAAP": true,
    "liabilities": {
      "electionDTTSingleMember": true,
      "electionUTPRSingleMember": false,
      "numberSubGroupDTT": 1,
      "numberSubGroupUTPR": 2,
      "totalLiability": 10000.99,
      "totalLiabilityDTT": 5000.50,
      "totalLiabilityIIR": 4000.00,
      "totalLiabilityUTPR": 2000.75,
      "liableEntities": []
    }
  }'
```
```
  Expected Response:
{
  "message": "Invalid JSON format",
  "details": [
    "liableEntities: liableEntities must not be empty"
  ]
}

```

---

### Scenario 6: Invalid Input - Negative BigDecimal

**Description:**  
The field `totalLiability` has a negative value, which is not valid.

**Curl Example:**
```bash
curl -X POST \
  http://localhost:10054/RESTAdapter/PLR/UKTaxReturn \
  -H "Content-Type: application/json" \
  -d '{
    "accountingPeriodFrom": "2024-08-14",
    "accountingPeriodTo": "2024-12-14",
    "obligationMTT": true,
    "electionUKGAAP": true,
    "liabilities": {
      "electionDTTSingleMember": true,
      "electionUTPRSingleMember": false,
      "numberSubGroupDTT": 1,
      "numberSubGroupUTPR": 2,
      "totalLiability": -10000.99,
      "totalLiabilityDTT": 5000.50,
      "totalLiabilityIIR": 4000.00,
      "totalLiabilityUTPR": 2000.75,
      "liableEntities": [
        {
          "ukChargeableEntityName": "Entity 1",
          "idType": "CRN",
          "idValue": "12345678",
          "amountOwedDTT": 1500.25,
          "amountOwedIIR": 1200.75,
          "amountOwedUTPR": 800.00
        }
      ]
    }
  }'
```
```
  Expected Response:
{
  "message": "Invalid JSON format",
  "details": [
    "totalLiability: totalLiability must be a positive number"
  ]
}
```

---

### Scenario 7: Invalid Input - Empty `ukChargeableEntityName`

**Description:**  
The field `ukChargeableEntityName` in the `liableEntities` array is empty.

**Curl Example:**
```bash
curl -X POST \
  http://localhost:10054/RESTAdapter/PLR/UKTaxReturn \
  -H "Content-Type: application/json" \
  -d '{
    "accountingPeriodFrom": "2024-08-14",
    "accountingPeriodTo": "2024-12-14",
    "obligationMTT": true,
    "electionUKGAAP": true,
    "liabilities": {
      "electionDTTSingleMember": true,
      "electionUTPRSingleMember": false,
      "numberSubGroupDTT": 1,
      "numberSubGroupUTPR": 2,
      "totalLiability": 10000.99,
      "totalLiabilityDTT": 5000.50,
      "totalLiabilityIIR": 4000.00,
      "totalLiabilityUTPR": 2000.75,
      "liableEntities": [
        {
          "ukChargeableEntityName": "",
          "idType": "CRN",
          "idValue": "12345678",
          "amountOwedDTT": 1500.25,
          "amountOwedIIR": 1200.75,
          "amountOwedUTPR": 800.00
        }
      ]
    }
  }'
```
```
  Expected Response:
{
  "message": "Invalid JSON format",
  "details": [
    "liableEntities[0].ukChargeableEntityName: ukChargeableEntityName is missing or empty"
  ]
}

```

---

## Key Table for Scenarios 4 to 7

| Scenario | Field/Description                       | Example Value |
|----------|-----------------------------------------|---------------|
| 4        | `numberSubGroupDTT` (Negative Integer)  | `-1`          |
| 5        | `liableEntities` (Empty Array)          | `[]`          |
| 6        | `totalLiability` (Negative BigDecimal)  | `-10000.99`   |
| 7        | `ukChargeableEntityName` (Empty String) | `""`          |


### License

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html").