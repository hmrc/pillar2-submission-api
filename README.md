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

## Testing in Development

Follow the guide to [Test the Beta API in Development](https://docs.tax.service.gov.uk/mdtp-handbook/documentation/create-and-manage-a-developer-hub-api/test-the-beta-api-in-dev.html). This API is a user-restricted resource. You will also need the following:

    <context>  : organisations/pillar-two
    <resource> : RESTAdapter/plr/<endpoint>
    <endpoint> : the endpoint being tested

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

### License

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html").