import { test, expect, request } from '@playwright/test';

test('Logs in successfully', async ({ page, context }) => {
  await page.goto('/login');
  await page.fill('input[formControlName="email"]', 'rose.g@example.com');
  await page.fill('input[formControlName="password"]', 'Password123!');
  await page.getByRole('button', { name: 'Login' }).click();

  await expect(page).toHaveURL('/explore');
});

[
  {email: 'rose.g@example.com', password: 'Wrong123!', reason: 'invalid password'},
  {email: 'unverifiedUser@example.com', password: 'Password123!', reason: 'unconfirmed email'},
  {email: 'nonexistent@example.com', password: 'Password123!', reason: 'non-existent email'},
].forEach(({ email, password, reason }) => {
  test(`Login fails for ${reason}`, async ({ page, context }) => {
    await page.goto('/login');
    await page.fill('input[formControlName="email"]', email);
    await page.fill('input[formControlName="password"]', password);
    await page.getByRole('button', { name: 'Login' }).click();
  
    await expect(page.locator('#toast-container')).toContainText('Login failed.');
  });
});

test('Resets password successfully', async ({ page, context }) => {
  const credentials = {
    email: 'resetPasswordUser@example.com',
    oldPassword: 'Password123!',
    newPassword: 'Test.123'
  };
  await page.goto('/login');
  await page.locator('a', { hasText: 'Forgot password?'}).click();
  await page.fill('input[formControlName="email"]', credentials.email);
  await page.getByRole('button', { name: 'Send Reset Link'}).click();
  await page.waitForTimeout(1500); // wait for mail to send

  // get reset link from mailpit
  const apiRequestContext = await request.newContext();
  const response = await apiRequestContext.get('http://localhost:8025/q/mailpit/api/v1/message/latest');
  await expect(response.ok()).toBeTruthy();
  const emailData = await response.json();
  const match = await emailData.Text.match(/http:\/\/localhost:4200\/reset-password\?[^"\s]+/);
  const resetLink = await match ? match[0] : null;

  console.log(resetLink);
  await page.goto(resetLink);

  await page.fill('input[formControlName="password"]', credentials.newPassword);
  await page.fill('input[formControlName="confirmPassword"]', credentials.newPassword);
  await page.getByRole('button', { name: 'Reset Password'}).click();

  await expect(page).toHaveURL('/login');
  await page.fill('input[formControlName="email"]', credentials.email);
  await page.fill('input[formControlName="password"]', credentials.oldPassword);
  await page.getByRole('button', { name: 'Login' }).click();
  await expect(page.locator('#toast-container')).toContainText('Login failed.');
  await page.fill('input[formControlName="email"]', credentials.email);
  await page.fill('input[formControlName="password"]', credentials.newPassword);
  await page.getByRole('button', { name: 'Login' }).click();
  await expect(page.locator('#toast-container')).toContainText('Login successful');
});
