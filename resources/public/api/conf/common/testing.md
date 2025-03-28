API testing is performed in the [HMRC developer hub](https://developer.service.hmrc.gov.uk/api-documentation) “sandbox” environment. Once you’ve [registered for an account](https://developer.service.hmrc.gov.uk/developer/registration) you can conduct your own testing. The Pillar 2 API belongs to the "Corporation Tax" category. 

Work through the instructions on the [getting started](https://developer.service.hmrc.gov.uk/api-documentation/docs/using-the-hub) page to create an application, then locate and subscribe to the Pillar 2 API.

The next steps are outlined on the Open API Specification (**OAS**) page under "setup". Access to the OAS page is currently restricted, so please use the link in the "Endpoints" section to contact support and join the private trial.

- Create a test user with a Pillar 2 Subscription. 
- Create a bearer token for the test user.
- Set up a test organisation.
- Run a SubmitUKTR request.

We’ve included some information to help you test your application here.

**Header Parameters**

Header parameters are listed on the OAS page for each endpoint. Agents will need to include the organisation *Pillar 2 ID* in the header. 

**SubmitUKTR**

Use the SubmitUKTR section in the OAS page to check you have included the correct header and body parameters.

**AmendUKTR**

Use the AmendUKTR section in the OAS page to check you have the correct header and body parameters for the request. 

**SubmitBTN**

Use the SubmitBTN section in the OAS page to check you have the correct header and body parameters for the request. 

If you have a specific testing need that is not supported in the sandbox, please contact our [support team](https://developer.service.hmrc.gov.uk/developer/support).


