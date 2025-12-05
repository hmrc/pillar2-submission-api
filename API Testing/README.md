# Bruno API Testing

This directory contains [Bruno](https://www.usebruno.com/) collections to test the Pillar 2 ecosystem.

## Directory Structure

- **`pillar2-submission-api`**: Tests for the submission service.
- **`pillar2`**: Tests for the backend service (upstream dependency).
- **`pillar2-external-test-stub`**: Tests for the external stub service.
- **`environments`**: Configuration for `local`, `development`, `qa`, and `externaltest`.

## Authentication Workflows

> **Note**: These workflows and scripts are specifically for the **`pillar2-submission-api`** collection.

Authentication involves two stages: **User Creation** and **OAuth Flow**.

### 1. User Creation

How you obtain a user to log in with depends on the environment:

| Environment | Method | Steps |
|---|---|---|
| **Local** | Script | Run **`01-auth / local / create-bearer-token`**. This shortcuts the entire flow using `auth-login-stub`. |
| **Development** | Script | Run **`01-auth / envs / Create Test User`**. This hits an endpoint to auto-generate a test user. |
| **External Test** | Script | Run **`01-auth / envs / Create Test User`**. |
| **QA** | **Manual** | 1. Create a Government Gateway user manually via the QA frontend.<br>2. Register for Pillar 2 to get a valid Pillar 2 ID. |

### 2. OAuth Flow (Deployed Environments)

For **Development**, **External Test**, and **QA**, once you have a user (either auto-generated or manually created), you must perform the OAuth flow to get a bearer token.

**Scripts Location**: `pillar2-submission-api/01-auth/envs`

#### Step 1: Get Authorization Code
1. Open **`Get Auth Code`**.
2. Copy the URL from the request (or run it to get the redirect URL).
3. Paste the URL into your browser.
4. Log in with your Government Gateway credentials (the ones you created/generated above) and grant authority.
5. Copy the `code` parameter from the final callback URL (e.g., `urn:ietf:wg:oauth:2.0:oob?code=AUTHORIZATION_CODE`).

#### Step 2: Exchange Code for Token
1. Open **`Exchange auth code`**.
2. Paste the code into the `code` field in the request body.
3. Run the request.
4. *Result*: The script automatically saves the `bearer_token` and `refresh_token` to your environment variables.

#### Step 3: Refresh Token (Optional)
If your token expires, run **`Create Refresh Token`**. It uses the saved `refresh_token` to generate a new `bearer_token`.

## Running Tests

Once authenticated (token is saved to env vars), you can run functional tests in:
- **`02-setup`**: Create/Update organisations.
- **`03-uktr`**: Submit/Amend UK Tax Returns.
- **`03-orn`**: Manage Overseas Return Notifications.
- **`03-btn`**: Submit Below Threshold Notifications.

> **Note**: To test deployed environments, you must have valid Developer Hub credentials and a registered application.
