import { expect, request } from '@playwright/test';
import { test } from '../fixtures/test-with-login-setup';
  
test('Displays user\'s own demands', async ({ authenticatedPage }) => {
    await authenticatedPage.locator('a', { hasText: 'My Demands' }).click();
    await expect(authenticatedPage.locator('h3')).toHaveCount(5);
});

test('Dispalys detailed view for demand', async ({ authenticatedPage }) => {
    const id = '-824';
    await authenticatedPage.locator('a', { hasText: 'My Demands' }).click();
    await expect(authenticatedPage.locator('h3')).toHaveCount(5);

    await authenticatedPage.locator(`#link-demand-${id}`).click();
    await expect(authenticatedPage).toHaveURL(`/demands/${id}`);

    await expect(authenticatedPage.locator('h2')).toHaveText('Need Home & Property Services Help');
    await expect(authenticatedPage.locator('#profile-link')).toHaveText('Rose Gomez');
    await expect(authenticatedPage.locator('#skill-description'))
        .toHaveText('Hello! I have some trouble with Comprehensive home services including construction, repairs, cleaning, maintenance, and organization Maybe you can help me?');
    await expect(authenticatedPage.locator('#skill-category')).toContainText('Home & Property Services');
    await expect(authenticatedPage.locator('#skill-urgency')).toContainText('MEDIUM');    
});

test('Edits demand for Personal Services Help', async ({ authenticatedPage }) => {
    const id = '-821';
    const oldDemandData = {
        title: 'Need Personal Services Help',
        description: 'Hello! I have some trouble with Individual care services including childcare, pet care, and personal assistance Maybe you can help me?',
        category: {id: '-10', name: 'Personal Services'},
        urgency: 'CRITICAL'
    };
    const newDemandData = {
        title: 'Personal Services Request',
        description: 'I need some help with watering my garden',
        category: {id: '-3', name: 'Home & Property Services'},
        urgency: 'HIGH'
    };

    await authenticatedPage.locator('a', { hasText: 'My Demands' }).click();
    await expect(authenticatedPage.locator('h3')).toHaveCount(5);

    await authenticatedPage.locator(`#link-demand-${id}`).click();
    await expect(authenticatedPage).toHaveURL(`/demands/${id}`);
    await expect(authenticatedPage.locator('h2')).toHaveText(oldDemandData.title);
    await expect(authenticatedPage.locator('#skill-description')).toHaveText(oldDemandData.description);
    await expect(authenticatedPage.locator('#skill-category')).toContainText(oldDemandData.category.name);
    await expect(authenticatedPage.locator('#skill-urgency')).toContainText(oldDemandData.urgency);  

    await authenticatedPage.getByRole('button', { name: 'Edit Details' }).click();
    await expect(authenticatedPage).toHaveURL(`/demands/${id}/edit`);

    await authenticatedPage.fill('input[formControlName="title"]', newDemandData.title);
    await authenticatedPage.fill('textarea[formControlName="description"]', newDemandData.description);
    await authenticatedPage.selectOption('#category', newDemandData.category.id);
    await authenticatedPage.getByRole('button', { name: newDemandData.urgency }).click();
    await authenticatedPage.getByRole('button', { name: 'Save Changes' }).click();
    
    await expect(authenticatedPage).toHaveURL(`/demands/${id}`);
    await expect(authenticatedPage.locator('h2')).toHaveText(newDemandData.title);
    await expect(authenticatedPage.locator('#skill-description')).toHaveText(newDemandData.description);
    await expect(authenticatedPage.locator('#skill-category')).toContainText(newDemandData.category.name);
    await expect(authenticatedPage.locator('#skill-urgency')).toContainText(newDemandData.urgency);

    // undo updates via api call
    const authToken = await authenticatedPage.evaluate(() => {
        return localStorage.getItem('authToken');
    });
    const apiRequest = await request.newContext();
    const response = await apiRequest.put(`http://localhost:8080/skills/demand/${id}`, {
        headers: {
            Authorization: `Bearer ${authToken}`,
        },
        data: {
            title: oldDemandData.title,
            description: oldDemandData.description,
            category: {id: oldDemandData.category.id},
            urgency: oldDemandData.urgency
        }
    });
    await expect(response.ok()).toBeTruthy();
});

test('Creates a demand and deletes it again', async ({ authenticatedPage }) => {
    const newDemand = {
        title: 'My new demand',
        description: 'Demand description',
        category: {id: '-1', name: 'Technology & Digital'},
        urgency: {label: 'LOW', value: '1'}
    };

    await authenticatedPage.locator('a', { hasText: 'My Demands' }).click();
    await authenticatedPage.getByRole('button', { name: 'New' }).click();

    await authenticatedPage.fill('input[formControlName="title"]', newDemand.title);
    await authenticatedPage.fill('textarea[formControlName="description"]', newDemand.description);
    await authenticatedPage.getByLabel('Category').selectOption(newDemand.category.id);
    await authenticatedPage.getByRole('button', { name: 'LOW' }).click();
    await authenticatedPage.getByRole('button', { name: 'Create' }).click();

    await expect(authenticatedPage.locator('#toast-container')).toContainText('Demand created successfully!');
    await expect(authenticatedPage.locator('h2')).toHaveText(newDemand.title);
    await expect(authenticatedPage.locator('#skill-description')).toHaveText(newDemand.description);
    await expect(authenticatedPage.locator('#skill-category')).toContainText(newDemand.category.name);
    await expect(authenticatedPage.locator('#skill-urgency')).toContainText(newDemand.urgency.label);  
     
    await authenticatedPage.getByRole('button', { name: 'Edit Details' }).click();
    await expect(authenticatedPage.url()).toContain('/edit');
    
    await authenticatedPage.getByRole('button', { name: 'Delete' }).click();
    await authenticatedPage.getByRole('button', { name: 'Delete', class: 'btn btn-danger' }).click(); // modal button
    await expect(authenticatedPage).toHaveURL('/demands');
    await expect(authenticatedPage.locator('#toast-container')).toContainText('Demand deleted successfully!');
});

[
    {title: 'a', description: 'The description', category: '-1', urgency: 'LOW', reason: 'title too short'},
    {title: 'The title', description: 'a', category: '-1', urgency: 'MEDIUM', reason: 'description too short'},
    //TODO fails as the category id is 0 even when no category is set:
    //{title: 'The title', description: 'The description', category: 'none', urgency: '2', reason: 'no category set'},
].forEach(({ title, description, category, urgency, reason }) => {
    test(`Fails to create a new demand because ${reason}`, async ({ authenticatedPage }) => {
        await authenticatedPage.locator('a', { hasText: 'My Demands' }).click();
        await authenticatedPage.getByRole('button', { name: 'New' }).click();

        await authenticatedPage.fill('input[formControlName="title"]', title);
        await authenticatedPage.fill('textarea[formControlName="description"]', description);
        if (category != 'none') await authenticatedPage.getByLabel('Category').selectOption(category);
        await authenticatedPage.getByRole('button', { name: urgency }).click();

        await expect(authenticatedPage.getByRole('button', { name: 'Create' })).toBeDisabled();
    });
});