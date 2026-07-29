const getChromium = () => {
  try {
    return require("playwright").chromium;
  } catch (e) {
    throw new Error("Missing dependency: playwright.\nRun 'npm install' in the collection folder.");
  }
};

const buildAuthorizeUrl = ({authCodeUrl, clientId}) =>
    `${authCodeUrl}/oauth/authorize` +
    `?client_id=${clientId}` +
    `&redirect_uri=urn:ietf:wg:oauth:2.0:oob` +
    `&scope=write:pillar2 read:pillar2` +
    `&response_type=code`;

const getAuthCode = async ({authCodeUrl, clientId, userId, password, accessCode, headless = true}) => {
    const url = buildAuthorizeUrl({authCodeUrl, clientId});
    console.log('Authorisation URL:\n', url);

    const browser = await getChromium().launch({ headless });
    try {
        const page = await browser.newPage();
        await page.goto(url);

        // Page 1: Click on "Continue" button
        if (page.url().includes('/oauth/start')) {
            await page.locator('a.govuk-button').click();
            await page.waitForLoadState('networkidle');
        }

        // Page 2: Click on the "Sign in to the HMRC online service" link
        if (page.url().includes('/oauth/whatYouWillNeed')) {
            await page.locator('a#signIn').click();
            await page.waitForLoadState('networkidle');
        }

        // Page 2a (QA only): Select "Government Gateway" login
        if (page.url().includes('/sign-in-to-hmrc-online-services/identity/sign-in/')) {
            await page.locator('#signInType').check();
            await page.locator('#continue').click({timeout: 15000});
            await page.waitForLoadState('networkidle');
        }

        // Page 3 (QA only): Fillin in the Government Gateway credentials form
        if (page.url().includes('/login/signin/creds')) {
            await page.locator('#user_id').fill(userId);
            await page.locator('#password').fill(password);
            await page.locator('#continue').click();
            await page.waitForLoadState('networkidle');
        }

        // Page 3 (other envs): Fill in the test credentials form
        if (page.url().includes('/api-test-login/sign-in')) {
            await page.locator('#userId').fill(userId);
            await page.locator('#password').fill(password);
            await page.locator('#submit').click();
            await page.waitForLoadState('networkidle');
        }

        // Page 3a (QA only): Provide the 2FA code
        if (page.url().includes('/multi-factor/challenge/')) {
            await page.locator('#oneTimePassword').fill(accessCode);
            await page.locator('#continue').click();
            await page.waitForLoadState('networkidle');
        }

        // Page 4: Click on the "Give permission" button
        if (page.url().includes('/oauth/grantscope')) {
            await page.locator('button#givePermission').click();
            await page.waitForLoadState('networkidle');
        }

        // Final page: Get the authorisation code
        const code = await page.locator('#authorisation-code').innerText();
        return code.trim();
    } finally {
        await browser.close();
    }
};

module.exports = {
    buildAuthorizeUrl,
    getAuthCode
};
