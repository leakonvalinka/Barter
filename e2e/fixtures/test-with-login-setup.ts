import { test as base, expect } from '@playwright/test';

export const test = base.extend({
    authenticatedPage: async ({ page }, use) => {
      await page.goto('/login');
      await page.fill('input[formControlName="email"]', 'rose.g@example.com');
      await page.fill('input[formControlName="password"]', 'Password123!');
      await page.getByRole('button', { name: 'Login' }).click();

      await expect(page).toHaveURL('/explore');
      await use(page);
    }
  });