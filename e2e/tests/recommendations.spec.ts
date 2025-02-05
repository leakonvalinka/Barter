import { expect, request } from '@playwright/test';
import { test } from '../fixtures/test-with-login-setup';
  
// only passes on first run, since matched demands are removed from first page on 'For You'
test('Finds recommendations for demand and matches one', async ({ authenticatedPage }) => {
    await authenticatedPage.locator('a', { hasText: 'For You' }).click();
    const ownDemands = await authenticatedPage.locator('.demand-title:visible');
    await expect(ownDemands).toHaveCount(5);

    await authenticatedPage.locator('.demand-title:visible', { hasText: 'Need Personal Services Help' }).click();
    const matchingOffers = await authenticatedPage.locator('.offer-title:visible');
    await expect(matchingOffers).toHaveCount(4);
    await authenticatedPage.locator('.offer-title:visible', { hasText: 'Professional Personal Services Personal' }).click();
    await authenticatedPage.waitForTimeout(750); //wait for exchange creation
    await expect(authenticatedPage.locator('h2')).toContainText('Perfect Match!');
    await expect(authenticatedPage.getByRole('button', { name: 'Continue Browsing' })).toBeVisible();
    await expect(authenticatedPage.getByRole('button', { name: 'Start Chat' })).toBeVisible();
    
    await authenticatedPage.getByRole('button', { name: 'Start Chat' }).click();
    await expect(authenticatedPage.locator('h1')).toContainText('Dominic Diaz');
    await expect(authenticatedPage.locator('.chat-message')).toContainText(
        'Hello! I would like to take your offer from your post Professional Personal Services Personal for my demand Need Personal Services Help. Would you want to help me? :D'
    );
});