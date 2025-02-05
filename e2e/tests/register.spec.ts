import { test, expect, request } from '@playwright/test';

test('Registers successfully with verification code', async ({ page, context, browserName }) => {
    await page.goto('/register');

    const email = `${browserName}WithCode@example.com`;
    const password = 'Password123!';

    await page.fill('input[formControlName="email"]', email);
    await page.fill('input[formControlName="password"]', password);
    await page.fill('input[formControlName="passwordRepeat"]', password);
    await page.getByRole('button', { name: 'Join the Community' }).click();
    
    await expect(page).toHaveURL(`/verify?email=${email}`);
    await expect(page.locator('#toast-container')).toContainText('Please verify your account.');

    // get verification code from mailpit
    const apiRequestContext = await request.newContext();
    const response = await apiRequestContext.get('http://localhost:8025/q/mailpit/api/v1/message/latest');
    await expect(response.ok()).toBeTruthy();
    const emailData = await response.json();
    const codeMatch = emailData.Text.match(/\b\d{6}\b/);
    const code = codeMatch ? codeMatch[0] : null;

    await page.fill('input[formControlName="verificationCode"]', code);
    await page.getByRole('button', { name: 'Verify account' }).click();

    await expect(page).toHaveURL('/onboarding?step=profile');
    await expect(page.locator('#toast-container')).toContainText('Verification successful!');
    await expect(page.locator('.overlay-message')).toContainText('Hi, nice to meet you!');
    await page.mouse.click(10,10);
    await page.fill('input[formControlName="displayName"]', 'Lisa');
    await page.fill('input[formControlName="addressInput"]', 'Neubaugasse 2');
    await page.locator('.address-suggestion').first().click();
    await page.fill('textarea[formControlName="bio"]', 'I am new on Barter!');
    await page.getByRole('button', { name: 'Next' }).click();

    await expect(page).toHaveURL('/onboarding?step=offers');
    await expect(page.locator('.overlay-message')).toContainText('Almost done!');
    await page.mouse.click(10,10);
    await page.getByRole('button', { name: 'New' }).click();
    await page.fill('input[formControlName="title"]', 'Math Tutoring');
    await page.fill('textarea[formControlName="description"]', 'I can tutor students in math related subjects');
    await page.selectOption('#category', '-4');
    await page.fill('input[formControlName="schedule"]', 'weekends');
    await page.getByRole('button', { name: 'Create' }).click();

    await expect(page.locator('h3')).toContainText('Math Tutoring');
    await page.getByRole('button', { name: 'Finish' }).click();

    await expect(page).toHaveURL('/explore');

    // deleting user via api call
    await deleteUser(email, password);
});

test('Registers successfully via verfication link', async ({ page, context, browserName }) => {
    await page.goto('/register');

    const email = `${browserName}WithLink@example.com`;
    const password = 'Password123!';

    await page.fill('input[formControlName="email"]', email);
    await page.fill('input[formControlName="password"]', password);
    await page.fill('input[formControlName="passwordRepeat"]', password);
    await page.getByRole('button', { name: 'Join the Community' }).click();
    
    await expect(page).toHaveURL(`/verify?email=${email}`);
    await expect(page.locator('#toast-container')).toContainText('Please verify your account.');

    // get verification link from mailpit
    const apiRequestContext = await request.newContext();
    const response = await apiRequestContext.get('http://localhost:8025/q/mailpit/api/v1/message/latest');
    await expect(response.ok()).toBeTruthy();
    const emailData = await response.json();
    const match = emailData.Text.match(/\/verify\?verificationCode=\d+&email=[^\s)]+/);
    const verificationLink = match ? match[0] : null;

    await page.goto(verificationLink);

    await expect(page).toHaveURL('/onboarding?step=profile');
    await expect(page.locator('#toast-container')).toContainText('Verification successful!');
    await expect(page.locator('.overlay-message')).toContainText('Hi, nice to meet you!');
    await page.mouse.click(10,10);
    await page.fill('input[formControlName="displayName"]', 'Lukas');
    await page.fill('input[formControlName="addressInput"]', 'Stiegengasse 7');
    await page.locator('.address-suggestion').first().click();
    await page.fill('textarea[formControlName="bio"]', 'New here on Barter!');
    await page.getByRole('button', { name: 'Next' }).click();

    await expect(page).toHaveURL('/onboarding?step=offers');
    await expect(page.locator('.overlay-message')).toContainText('Almost done!');
    await page.mouse.click(10,10);
    await page.getByRole('button', { name: 'New' }).click();
    await page.fill('input[formControlName="title"]', 'Website Creation');
    await page.fill('textarea[formControlName="description"]', 'I can help you create a website for yourself!');
    await page.selectOption('#category', '-1');
    await page.fill('input[formControlName="schedule"]', 'evenings');
    await page.getByRole('button', { name: 'Create' }).click();

    await expect(page.locator('h3')).toContainText('Website Creation');
    await page.getByRole('button', { name: 'Finish' }).click();

    await expect(page).toHaveURL('/explore');
    // deleting user via api call    
    await deleteUser(email, password);
});

// only passes on first run as there is no way to delete the user with no verified account
test('Registering fails for invalid verfication code', async ({ page, context, browserName }) => {
    await page.goto('/register');

    const email = `${browserName}InvalidCode@example.com`;
    const password = 'Password123!';
  
    await page.fill('input[formControlName="email"]', email);
    await page.fill('input[formControlName="password"]', password);
    await page.fill('input[formControlName="passwordRepeat"]', password);
    await page.getByRole('button', { name: 'Join the Community' }).click();
    
    await expect(page).toHaveURL(`/verify?email=${email}`);
    await expect(page.locator('#toast-container')).toContainText('Please verify your account.');
    
    await page.fill('input[formControlName="verificationCode"]', '000000'); // yes that's flakey but we'll take our chances ok
    await page.getByRole('button', { name: 'Verify account' }).click();
    await expect(page).toHaveURL(`/verify?email=${email}`);
    await expect(page.locator('#toast-container')).toContainText('Registration failed.');
});

test('Registering fails for existing email', async ({ page, context }) => {
    await page.goto('/register');
    
    const email = 'user@example.com';
    const password = 'Password123!';
  
    await page.fill('input[formControlName="email"]', email);
    await page.fill('input[formControlName="password"]', password);
    await page.fill('input[formControlName="passwordRepeat"]', password);
    await page.getByRole('button', { name: 'Join the Community' }).click();

    await expect(page).toHaveURL('/register');
    await expect(page.locator('#toast-container')).toContainText('Registration failed.');
    });

async function deleteUser(email: string, password: string) {
    const apiRequestContextLogin = await request.newContext();
    const responseLogin = await apiRequestContextLogin.post('http://localhost:8080/auth/login', {
        data: {
            emailOrUsername: email,
            password: password
        },
    });
    await expect(responseLogin.ok()).toBeTruthy();
    const token = (await responseLogin.json())['jwt'];

    const apiRequestContextDelete = await request.newContext({
        extraHTTPHeaders: {
            Authorization: `Bearer ${token}`,
        },
    });
    const responseDelete = await apiRequestContextDelete.delete('http://localhost:8080/users');
    await expect(responseDelete.ok()).toBeTruthy();
}