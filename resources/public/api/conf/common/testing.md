API testing is performed in the [HMRC developer hub](https://developer.service.hmrc.gov.uk/api-documentation) sandbox environment. Once you’ve registered for an account you can conduct your own testing. The Pillar 2 API belongs to the *Corporation Tax* category.

To start, use the developer hub to create an application in the test environment (sandbox), then locate and subscribe to the API.

The Pillar 2 API will use a dynamic stub system for testing, where the developer creates a test organisation. Organisation details are stored in Mongo DB as an external test stub and used as test data for a maximum of 28 days. The developer can then query multiple endpoints without having to new test data values, and the data can be cleared once testing is complete.

If you have a specific testing need that is not supported in the sandbox, contact our [support team](https://developer.service.hmrc.gov.uk/developer/support). 

