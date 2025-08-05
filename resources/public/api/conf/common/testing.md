API testing is performed in the [HMRC developer hub](https://developer.service.hmrc.gov.uk/api-documentation) “sandbox” environment. Once you’ve [registered for an account](https://developer.service.hmrc.gov.uk/developer/registration) you can conduct your own testing. The Pillar 2 API belongs to the *Corporation Tax* category. 

Work through the instructions on the [getting started](https://developer.service.hmrc.gov.uk/api-documentation/docs/using-the-hub) page to create an application, then locate and subscribe to the Pillar 2 API.

**Important**: The *Pillar 2 ID* must be included in the request header or an error will be returned when the request is sent. 

The next steps are outlined on the Open API Specification (**OAS**) page under *setup*. Access to the OAS page is currently restricted, so please use the link in the *Endpoints* section to contact support and submit an access request.

- [Create a test user](https://developer.service.hmrc.gov.uk/api-documentation/docs/api/service/api-platform-test-user/1.0) with a Pillar 2 subscription. 
- Create a bearer token for the test user.
- Set up a test organisation.
- Run a *Submit UK Tax Return* request.

The [service guide](https://developer.service.hmrc.gov.uk/guides/pillar2-service-guide/) contains more detailed information on testing for each endpoint.

If you have a specific testing need that is not supported in the sandbox, please contact our [support team](https://developer.service.hmrc.gov.uk/developer/support).


