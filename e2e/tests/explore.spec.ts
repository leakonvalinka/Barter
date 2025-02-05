import { expect } from '@playwright/test';
import { test } from '../fixtures/test-with-login-setup';
  
test('Filters for offers', async ({ authenticatedPage }) => {
    await authenticatedPage.locator('a', { hasText: 'Explore' }).click();

    await expect(authenticatedPage.locator('.skill-item')).toHaveCount(12);
    await expect(authenticatedPage.locator('.pagination-item').last()).toContainText('24');
    await expect(authenticatedPage.locator('google-map')).toBeVisible();
    const mapCircles = await authenticatedPage.locator('map-circle');
    await expect(mapCircles).toHaveCount(53);

    //select filter
    await authenticatedPage.locator('pill').filter({ hasText: 'Technology & Digital' }).click();
    await expect(authenticatedPage.locator('.skill-item')).toHaveCount(12);
    await expect(authenticatedPage.locator('.pagination-item').last()).toContainText('3');
    await authenticatedPage.locator('.pagination-item').filter({ hasText: '3' }).click();
    await expect(authenticatedPage.locator('.skill-item')).toHaveCount(8);
    // unselect filter
    await authenticatedPage.locator('pill').filter({ hasText: 'Technology & Digital' }).click();
    await expect(authenticatedPage.locator('.skill-item')).toHaveCount(12);
    await expect(authenticatedPage.locator('.pagination-item').last()).toContainText('24');
});

test('Filters for demands', async ({ authenticatedPage }) => {
    await authenticatedPage.locator('a', { hasText: 'Explore' }).click();

    await authenticatedPage.getByRole('switch').click();

    await expect(authenticatedPage.locator('.skill-item')).toHaveCount(12);
    await expect(authenticatedPage.locator('.pagination-item').last()).toContainText('15');
    await expect(authenticatedPage.locator('google-map')).toBeVisible();
    const mapCircles = await authenticatedPage.locator('map-circle');
    await expect(mapCircles).toHaveCount(53);

    //select filter
    await authenticatedPage.locator('pill').filter({ hasText: 'Events & Entertainment' }).click();
    await expect(authenticatedPage.locator('.skill-item')).toHaveCount(12);
    await expect(authenticatedPage.locator('.pagination-item').last()).toContainText('2');
    await authenticatedPage.locator('.pagination-item').filter({ hasText: '2' }).click();
    await expect(authenticatedPage.locator('.skill-item')).toHaveCount(2);
    // unselect filter
    await authenticatedPage.locator('pill').filter({ hasText: 'Events & Entertainment' }).click();
    await expect(authenticatedPage.locator('.skill-item')).toHaveCount(12);
    await expect(authenticatedPage.locator('.pagination-item').last()).toContainText('15');
});