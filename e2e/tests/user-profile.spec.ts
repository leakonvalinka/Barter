import { expect, request } from '@playwright/test';
import { test } from '../fixtures/test-with-login-setup';
  
test('Displays a user\'s profile page', async ({ authenticatedPage }) => {
    await authenticatedPage.locator('a', { hasText: 'Profile' }).click();
    // overview section
    await expect(authenticatedPage.locator('#displayName')).toContainText('Rose Gomez');
    await expect(authenticatedPage.locator('#username')).toContainText('@rose.g');
    await expect(authenticatedPage.locator('#bio')).toContainText('Professional graphic designer');
    await expect(authenticatedPage.locator('#address')).toContainText('Linz 4020, Austria');
    await expect(authenticatedPage.locator('#rating')).toContainText('3.5/5 (3 reviews)');
    await expect(authenticatedPage.getByRole('button', { name: 'Edit' })).toHaveCount(1);

    // skills section
    await expect(authenticatedPage.locator('.skill')).toHaveCount(9);
    await authenticatedPage.locator('button[ngpswitch]').click();
    await expect(authenticatedPage.locator('.skill')).toHaveCount(5);
});

test('Edits a user\'s profile', async ({ authenticatedPage }) => {
    await authenticatedPage.locator('a', { hasText: 'Profile' }).click();

    await expect(authenticatedPage.locator('#displayName')).toContainText('Rose Gomez');
    await expect(authenticatedPage.locator('#username')).toContainText('@rose.g');
    await expect(authenticatedPage.locator('#bio')).toContainText('Professional graphic designer');
    await expect(authenticatedPage.locator('#address')).toContainText('Linz 4020, Austria');
    
    await authenticatedPage.getByRole('button', { name: 'Edit' }).click();
    await expect(authenticatedPage).toHaveURL('/profile/edit/rose.g');

    await authenticatedPage.fill('input[formControlName="displayName"]', 'Rose :)');
    await authenticatedPage.fill('textarea[formControlName="bio"]', 'Just moved, excited to meet new bartering friends here!');
    await authenticatedPage.fill('input[formControlName="addressInput"]', '');
    await expect(authenticatedPage.getByRole('button', { name: 'Save' })).toBeDisabled();
    await authenticatedPage.fill('input[formControlName="addressInput"]', 'Neubaugasse 2');
    await authenticatedPage.locator('.address-suggestion').first().click();
    await expect(authenticatedPage.locator('#toast-container')).toContainText('Location found');
    await authenticatedPage.getByRole('button', { name: 'Save' }).click();

    await expect(authenticatedPage).toHaveURL('/profile/rose.g');
    await expect(authenticatedPage.locator('#displayName')).toContainText('Rose :)');
    await expect(authenticatedPage.locator('#address')).toContainText('Wien 1070, Austria');
    await expect(authenticatedPage.locator('#bio')).toContainText('Just moved, excited to meet new bartering friends here!');

    // undo updates via api call
    const authToken = await authenticatedPage.evaluate(() => {
        return localStorage.getItem('authToken');
    });
    const apiRequest = await request.newContext();
    const response = await apiRequest.put(`http://localhost:8080/users`, {
        headers: {
            Authorization: `Bearer ${authToken}`,
        },
        data: {
            displayName: 'Rose Gomez',
            bio: 'Professional graphic designer',
            location: {
                street: 'Hanriederstraße',
                streetNumber: '5',
                city: 'Linz',
                postalCode: 4020,
                country: 'Austria',
                homeLocation: {
                    type: 'Point',
                    coordinates: [48.287933126778, 14.284053453885214]
                }
            }
        }
    });
    await expect(response.ok()).toBeTruthy();
});
