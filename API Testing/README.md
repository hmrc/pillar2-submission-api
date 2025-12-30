# Bruno API Testing

This directory contains [Bruno](https://www.usebruno.com/) collections for the Pillar 2 ecosystem.

> **Important**: These collections are intended as developer and tester aids during active development. They provide example API calls and simple tests for manual verification. These tests are **not included in CI/CD** and may become out of sync with the codebase if not manually updated. The plan is to remove these collections before handover to live services.

## Directory Structure

- **`pillar2-submission-api/`**: Example calls and simple tests for the submission service API endpoints.
- **`pillar2/`**: Example calls and simple tests for the backend Pillar 2 service.
- **`pillar2-external-test-stub/`**: Example calls and simple tests for the external stub service.
- **`environments/`**: Configuration for `local`, `development`, `qa`, and `externaltest`.

## Running Tests

### Local Environment Only

**Tests should be run locally.** Running the full test suites against deployed environments is not recommended because:

- The `pillar2` and `pillar2-external-test-stub` services are in protected environments and cannot be accessed directly
- Running tests against deployed environments will likely hit API Platform rate limits as the tests execute too quickly
- Only individual requests for manual verification should be run against deployed environments

### Using the Bruno GUI

Most of the team uses the [Bruno desktop application](https://www.usebruno.com/downloads) to run these collections:

1. Download and install Bruno from https://www.usebruno.com/downloads
2. Open Bruno and select "Open Collection"
3. Navigate to the `API Testing` folder and open any of the collection folders
4. Select the `local` environment from the environment dropdown
5. Run individual requests or use "Run Folder" to execute multiple tests

### Using the Bruno CLI

Alternatively, you can use the Bruno CLI:

1. Install the CLI globally:
   ```bash
   npm install -g @usebruno/cli
   ```

2. Run tests from the `API Testing` directory:
   ```bash
   cd "API Testing"

   # Run pillar2-submission-api tests
   bru run pillar2-submission-api --env local -r

   # Run pillar2 tests
   bru run pillar2 --env local -r

   # Run pillar2-external-test-stub tests
   bru run pillar2-external-test-stub --env local -r
   ```

The `-r` flag runs tests recursively through all subfolders.

### Prerequisites for Local Testing

Ensure the following services are running locally:

- `pillar2-submission-api` on port 10054
- `pillar2-external-test-stub` on port 10055

### Test Execution Order

Bruno runs tests in sequence order defined by the `seq` field in each `.bru` file. The collections are structured so that:

1. **Authentication** (`01-auth/`): Obtains bearer tokens for authenticated requests
2. **Setup** (`02-setup/` or inline): Creates test organisations and data
3. **Functional Tests** (`03-*/` or `99-qa/`): Tests actual API endpoints
4. **Cleanup**: Deletes test data

When running the full collection recursively, earlier tests set up data that subsequent tests depend on.

### Running Specific Folders

To run tests in a specific folder:

```bash
# Run only UKTR submit tests
bru run pillar2-submission-api/99-qa/uktr/submit --env local -r

# Run only ORN tests
bru run pillar2-submission-api/99-qa/orn --env local -r
```

### Environment Variables

The `environments/local.bru` file defines:

| Variable | Value | Description |
|----------|-------|-------------|
| `apiUrl` | `http://localhost:10054/pillar2/submission` | Submission API base URL |
| `pillar2Url` | `http://localhost:10055/RESTAdapter/plr` | Pillar 2 backend URL |
| `externalTestStubUrl` | `http://localhost:10055` | External test stub URL |
| `authUrl` | `http://localhost:10054/pillar2/test/auth-login-stub` | Local auth endpoint |
| `testPlrId` | `XEPLR0000000000` | Test Pillar 2 ID |
| `bearer_token` | (set by auth script) | Authentication token |

## Test Collections

### pillar2-submission-api

Example calls and tests for the main submission API service:

- **`01-auth/`**: Authentication scripts
  - `local/`: Local auth via stub (sets `bearer_token` automatically)
  - `envs/`: OAuth flow for deployed environments
- **`99-qa/`**: QA test scenarios
  - `uktr/submit/`: UKTR submission tests
  - `uktr/amend/`: UKTR amendment tests
  - `orn/`: Overseas Return Notification tests
  - `oas/`: Obligation Accounting Statement tests
  - `btn/`: Below Threshold Notification tests

### pillar2

Example calls and tests for the upstream Pillar 2 backend service:

- **`01-setup/`**: Organisation setup
- **`99-qa/`**: Validation and error handling tests

### pillar2-external-test-stub

Example calls and tests for the external test stub service:

- **`01-setup/`**: Organisation management
- **`99-qa/`**: API response tests

## Authentication Workflows

### Local Environment

For local testing, authentication is simplified:

1. Run the full collection from the root - the auth script runs first automatically
2. Or manually run the `01-auth/local/create-bearer-token` request

The `create-bearer-token` script uses `auth-login-stub` to generate a token and saves it to the `bearer_token` environment variable.

### Deployed Environments (Development, QA, External Test)

> **Note**: Only run individual requests against deployed environments to avoid rate limiting. Do not run full test suites.

For deployed environments, you need to perform the full OAuth flow:

#### Step 1: Create or Obtain a User

| Environment | Method |
|-------------|--------|
| **Development** | Run `01-auth/envs/Create Test User` |
| **External Test** | Run `01-auth/envs/Create Test User` |
| **QA** | Create a Government Gateway user manually via the QA frontend and register for Pillar 2 |

#### Step 2: Get Authorization Code

1. Open `01-auth/envs/Get Auth Code`
2. Copy the URL and paste it into your browser
3. Log in with Government Gateway credentials and grant authority
4. Copy the `code` parameter from the callback URL

#### Step 3: Exchange Code for Token

1. Open `01-auth/envs/Exchange auth code`
2. Paste the code into the request body
3. Run the request - the script saves `bearer_token` and `refresh_token` to environment variables

#### Step 4: Refresh Token (when expired)

Run `01-auth/envs/Create Refresh Token` to generate a new `bearer_token` using the saved `refresh_token`.

## Expected Local Test Results

When all local services are running:

| Collection | Requests | Tests |
|------------|----------|-------|
| pillar2-submission-api | 107+ passed | 147+ passed |
| pillar2 | 68 passed | 82 passed |
| pillar2-external-test-stub | 75 passed | 97 passed |

Note: The `01-auth/envs/` requests in `pillar2-submission-api` will fail when running with `--env local` as they target deployed environment URLs. This is expected.
