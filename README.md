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

### License

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html").