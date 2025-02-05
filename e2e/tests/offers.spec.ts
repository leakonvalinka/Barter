import { expect, request } from '@playwright/test';
import { test } from '../fixtures/test-with-login-setup';
  
test('Displays user\'s own offers', async ({ authenticatedPage }) => {
    await authenticatedPage.locator('a', { hasText: 'My Offers' }).click();
    await expect(authenticatedPage.locator('h3')).toHaveCount(9);
});

test('Dispalys detailed view for offer', async ({ authenticatedPage }) => {
    const id = '-2144';
    await authenticatedPage.locator('a', { hasText: 'My Offers' }).click();
    await expect(authenticatedPage.locator('h3')).toHaveCount(9);

    await authenticatedPage.locator(`#link-offer-${id}`).click();
    await expect(authenticatedPage).toHaveURL(`/offers/${id}`);

    await expect(authenticatedPage.locator('h2')).toHaveText('Technology & Digital Assistance');
    await expect(authenticatedPage.locator('#profile-link')).toHaveText('Rose Gomez');
    await expect(authenticatedPage.locator('#skill-description'))
        .toHaveText('Hi! I offer help in Technology & Digital. If you struggle with Digital services including web/mobile development, data analysis, and IT solutions, do not hesitate to write me!');
    await expect(authenticatedPage.locator('#skill-category')).toContainText('Technology & Digital');
    await expect(authenticatedPage.locator('#skill-availability')).toContainText('tuesday morning');
});

test('Edits offer for Fitness Training', async ({ authenticatedPage }) => {
    const id = '-2143';
    const oldOfferData = {
        title: 'Creative & Design Assistance',
        description: 'Hi! I offer help in Creative & Design. If you struggle with Visual and creative services including graphic design, photography, video, animation, do not hesitate to write me!',
        category: {id: '-2', name: 'Creative & Design'},
        schedule: 'saturday evening'
    };
    const newOfferData = {
        title: 'Design Support',
        description: 'I offer you help with all things Design',
        category: {id: '-4', name: 'Education & Training'},
        schedule: 'in the mornings'
    };

    await authenticatedPage.locator('a', { hasText: 'My Offers' }).click();
    await expect(authenticatedPage.locator('h3')).toHaveCount(9);

    await authenticatedPage.locator(`#link-offer-${id}`).click();
    await expect(authenticatedPage).toHaveURL(`/offers/${id}`);
    await expect(authenticatedPage.locator('h2')).toHaveText(oldOfferData.title);
    await expect(authenticatedPage.locator('#skill-description')).toHaveText(oldOfferData.description);
    await expect(authenticatedPage.locator('#skill-category')).toContainText(oldOfferData.category.name);
    await expect(authenticatedPage.locator('#skill-availability')).toContainText(oldOfferData.schedule);  

    await authenticatedPage.getByRole('button', { name: 'Edit Details' }).click();
    await expect(authenticatedPage).toHaveURL(`/offers/${id}/edit`);

    await authenticatedPage.fill('input[formControlName="title"]', newOfferData.title);
    await authenticatedPage.fill('textarea[formControlName="description"]', newOfferData.description);
    await authenticatedPage.selectOption('#category', newOfferData.category.id);
    await authenticatedPage.fill('input[formControlName="schedule"]', newOfferData.schedule);
    await authenticatedPage.getByRole('button', { name: 'Save Changes' }).click();
    
    await expect(authenticatedPage).toHaveURL(`/offers/${id}`);
    await expect(authenticatedPage.locator('h2')).toHaveText(newOfferData.title);
    await expect(authenticatedPage.locator('#skill-description')).toHaveText(newOfferData.description);
    await expect(authenticatedPage.locator('#skill-category')).toContainText(newOfferData.category.name);
    await expect(authenticatedPage.locator('#skill-availability')).toContainText(newOfferData.schedule);

    // undo updates via api call
    const authToken = await authenticatedPage.evaluate(() => {
        return localStorage.getItem('authToken');
      });
    const apiRequest = await request.newContext();
    const response = await apiRequest.put(`http://localhost:8080/skills/offer/${id}`, {
        headers: {
            Authorization: `Bearer ${authToken}`,
        },
        data: {
            title: oldOfferData.title,
            description: oldOfferData.description,
            category: {id: oldOfferData.category.id},
            schedule: oldOfferData.schedule
        }
    });
    await expect(response.ok()).toBeTruthy();
});

test('Creates an offer and deletes it again', async ({ authenticatedPage }) => {
    const newOffer = {
        title: 'My new offer',
        description: 'Offer description',
        category: {id: '-1', name: 'Technology & Digital'},
        schedule: 'mornings'
    };

    await authenticatedPage.locator('a', { hasText: 'My Offers' }).click();
    await authenticatedPage.getByRole('button', { name: 'New' }).click();

    await authenticatedPage.fill('input[formControlName="title"]', newOffer.title);
    await authenticatedPage.fill('textarea[formControlName="description"]', newOffer.description);
    await authenticatedPage.selectOption('#category', newOffer.category.id);
    await authenticatedPage.fill('input[formControlName="schedule"]', newOffer.schedule);
    await authenticatedPage.getByRole('button', { name: 'Create' }).click();

    await expect(authenticatedPage.locator('#toast-container')).toContainText('Offer created successfully!');
    await expect(authenticatedPage.locator('h2')).toHaveText(newOffer.title);
    await expect(authenticatedPage.locator('#skill-description')).toHaveText(newOffer.description);
    await expect(authenticatedPage.locator('#skill-category')).toContainText(newOffer.category.name);
    await expect(authenticatedPage.locator('#skill-availability')).toContainText(newOffer.schedule);  
     
    await authenticatedPage.getByRole('button', { name: 'Edit Details' }).click();
    await expect(authenticatedPage.url()).toContain('/edit');
    
    await authenticatedPage.getByRole('button', { name: 'Delete' }).click();
    await authenticatedPage.getByRole('button', { name: 'Delete', class: 'btn btn-danger' }).click(); // modal button
    await expect(authenticatedPage).toHaveURL('/offers');
    await expect(authenticatedPage.locator('#toast-container')).toContainText('Offer deleted successfully!');
});

[
    {title: 'a', description: 'The description', category: '-1', schedule: 'monday', reason: 'title too short'},
    {title: 'The title', description: 'a', category: '-1', schedule: 'monday', reason: 'description too short'}
].forEach(({ title, description, category, schedule, reason }) => {
    test(`Fails to create a new offer because ${reason}`, async ({ authenticatedPage }) => {
        await authenticatedPage.locator('a', { hasText: 'My Offers' }).click();
        await authenticatedPage.getByRole('button', { name: 'New' }).click();

        await authenticatedPage.fill('input[formControlName="title"]', title);
        await authenticatedPage.fill('textarea[formControlName="description"]', description);
        if (category != 'none') await authenticatedPage.getByLabel('Category').selectOption(category);
        await authenticatedPage.fill('input[formControlName="schedule"]', schedule);

        await expect(authenticatedPage.getByRole('button', { name: 'Create' })).toBeDisabled();
    });
});