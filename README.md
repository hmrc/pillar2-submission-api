
# pillar2-submission-api

Backend microservice for the Pillar 2 project. Pillar 2 refers to the Global Minimum Tax being introduced by the Organisation for Economic Cooperation and Development (OECD).

The Pillar 2 Tax ensures that global Multinational Enterprises (MNEs) with a turnover of >€750m are subject to a minimum Effective Tax Rate of 15% (i.e., a top-up tax for Medium to Large MNEs).

This microservice provides APIs for third-party applications to manage Pillar 2 tax obligations and submissions on behalf of corporations.

## Running the Service

The service runs on port `10054` by default.

### Local

To run the application locally:

```bash
sbt run
```

### Service Manager

You can use [Service Manager](https://github.com/hmrc/service-manager) to run this service along with its dependencies:

```bash
sm2 --start PILLAR2_ALL
```

To stop the services:

```bash
sm2 --stop PILLAR2_ALL
```

## Testing

### Unit Tests

Run unit tests:

```bash
sbt test
```

### Integration Tests

Run integration tests:

```bash
sbt it/test
```

### Test Coverage

Generate a test coverage report:

```bash
sbt 'coverage; test; it/test; coverageReport'
```

## Code Style & Linting

This project uses `scalafmt` and `scalafix` to ensure code consistency.

To run linting checks:

```bash
sbt lint
```

To run pre-PR checks (includes formatting checks):

```bash
sbt prePrChecks
```

## API Documentation

The API is documented using encryption-agnostic standard OpenAPI 3.0 specification.

- **Source**: Route definitions and manual overrides in `conf/swagger.yml`.
- **Generated Spec**: `resources/public/api/conf/1.0/application.yaml`.

### Generating the Spec

To generate the OpenAPI Specification (OAS) from the code:

```bash
sbt createOpenAPISpec
```

This command runs `routesToYamlOas` and validates the result. The spec is generated into the **`target/swagger`** directory.

You will need to manually copy the generated file to replace the relevant configuration file in `resources/public/api/conf/1.0`.

### Spec Versions

The service maintains two versions of the OpenAPI specification:

1. **Test Only Spec**: 
   - **Path**: `resources/public/api/conf/1.0/testOnly/application.yaml`
   - **Target**: QA Developer Hub (Development and QA environments)
   - Contains new endpoints or changes under development that are not yet ready for the public Production Developer Hub.

2. **Production Spec**: 
   - **Path**: `resources/public/api/conf/1.0/application.yaml`
   - **Target**: Production Developer Hub (Sandbox and Production environments)
   - Contains the public-facing API definition.

### Validating the Spec

To validate the generated OAS against OpenAPI standards (this is also run as part of `createOpenAPISpec`):

```bash
sbt validateOas
```

### Publishing

If you have made changes to the API that need to be published to the Developer Hub, ensure the generated `application.yaml` is moved from the target directory to the appropriate location (Test Only or Production path above), reviewed, and merged into the main branch. The API Platform will adhere to its own release cycle to pick up changes.

## Bruno API Testing

The `API Testing` directory contains [Bruno](https://www.usebruno.com/) collections to test the ecosystem.

For detailed instructions on setup, authentication, and OAuth flows for all environments (Local, Dev, QA, etc.), please read the **[API Testing README](API%20Testing/README.md)**.

## License

This code is open source software licensed under the [Apache 2.0 License](http://www.apache.org/licenses/LICENSE-2.0.html).