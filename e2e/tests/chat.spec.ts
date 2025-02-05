import { test, expect, Page } from '@playwright/test';

test('Chat functionality between two users', async ({ browser }) => {
    // Create separate browser contexts + pages for two users
    const userRoseContext = await browser.newContext();
    const userChrisContext = await browser.newContext();
    const pageRose = await userRoseContext.newPage();
    const pageChris = await userChrisContext.newPage();

    // Log in both users
    await pageRose.goto('/login');
    await pageRose.fill('input[formControlName="email"]', 'rose.g@example.com');
    await pageRose.fill('input[formControlName="password"]', 'Password123!');
    await pageRose.getByRole('button', { name: 'Login' }).click();
    await expect(pageRose.locator('#toast-container')).toContainText('Login successful!');

    await pageChris.goto('/login');
    await pageChris.fill('input[formControlName="email"]', 'christian-sebastian.m@example.com');
    await pageChris.fill('input[formControlName="password"]', 'Password123!');
    await pageChris.getByRole('button', { name: 'Login' }).click();
    await expect(pageChris.locator('#toast-container')).toContainText('Login successful!');

    // Rose starts Barter Chat with Christian
    await pageRose.locator('a', { hasText: 'Explore' }).click();
    await expect(pageRose.locator('.skill-item')).toHaveCount(12);
    await pageRose.locator('#link-offer--834').click();
    await expect(pageRose).toHaveURL('/offers/-834');
    await expect(pageRose.locator('h2')).toHaveText('Professional Creative & Design Creative');
    await pageRose.getByRole('button', { name: 'Barter' }).click();
    await expect(pageRose.locator('#toast-container')).toContainText('Exchange was initiated successfully');
    await expect(pageRose.url()).toContain('/chat/');
    await expect(pageRose.locator('.chat-message')).toContainText(
        'Hi, I would like to barter with you! To accept the exchange please reply to this message!'
    );

    // Christian reads the message & replies
    await pageChris.locator('a', { hasText: 'Messages' }).click();
    await expect(pageChris).toHaveURL('/chat');
    const chatWithRose = await pageChris.locator('.chat-item').first();
    await expect(chatWithRose.locator('h2')).toContainText('Rose Gomez');
    await expect(chatWithRose.locator('p')).toContainText('Hi, I would'); // beginning of most recent message
    await chatWithRose.click();
    await expect(pageChris.locator('.chat-message')).toContainText(
        'Hi, I would like to barter with you! To accept the exchange please reply to this message!'
    );
    await pageChris.fill('textarea[id="message-input"]', 'Lets do it!');
    await pageChris.locator('#send-button').click();
    const messagesChris = await pageChris.locator('.chat-message');
    await expect(messagesChris).toHaveCount(2);
    await expect(messagesChris.nth(0)).toContainText(
        'Hi, I would like to barter with you! To accept the exchange please reply to this message!'
    );
    await expect(messagesChris.nth(1)).toContainText(
        'Lets do it!'
    );

    // Rose reads the message
    await expect(pageRose.url()).toContain('/chat/');
    const messagesRose = await pageRose.locator('.chat-message');
    await expect(messagesRose).toHaveCount(2);
    await expect(messagesRose.nth(0)).toContainText(
        'Hi, I would like to barter with you! To accept the exchange please reply to this message!'
    );
    await expect(messagesRose.nth(1)).toContainText(
        'Lets do it!'
    );

    await pageRose.locator('.toggler').click();
    await pageRose.getByRole('button', { name: 'Rate Exchange' }).click();
    await expect(pageRose.locator('#toast-container')).toContainText(
        'This exchange is not ratable yet. Please wait three days or until all participants have marked it as completed.'
    );

    // Close the contexts
    await userRoseContext.close();
    await userChrisContext.close();
});

test('Rate exchange chat that\'s older than 3 days', async ({ page }) => {
    await loginAsRoseG(page);

    await page.locator('a', { hasText: 'Messages' }).click();
    await expect(page).toHaveURL('/chat');
    await page.locator('.chat-item', { hasText: 'Andrew Ward' }).click();
    await expect(page).toHaveURL('/chat/00000000-0000-4000-8000-000000001498');
    await expect(page.locator('h1')).toContainText('Andrew Ward');
    await page.locator('.toggler').click();
    await page.getByRole('button', { name: 'Rate Exchange' }).click();

    await page.locator('.star').nth(4).click();
    await page.fill('input[formControlName="title"]', 'Nice guy');
    await page.fill('textarea[formControlName="review"]', 'Nice experience, friendly and chatty.');
    await page.getByRole('button', { name: 'Submit Review' }).click(); 
    await expect(page.locator('#toast-container')).toContainText('Rating created successfully');

    await page.locator('h1').click();
    await expect(page).toHaveURL('/profile/andrew.w');
    await page.locator('#more-reviews').click();
    await expect(page.locator('.review h3').nth(5)).toContainText('Nice guy');
});

test('Report a user', async ({ page }) => {
    await loginAsRoseG(page);

    await page.locator('a', { hasText: 'Messages' }).click();
    await expect(page).toHaveURL('/chat');
    await page.locator('.chat-item', { hasText: 'Leah John' }).click();
    await expect(page).toHaveURL('/chat/00000000-0000-4000-8000-000000001386');
    await expect(page.locator('h1')).toContainText('Leah John');
    await page.locator('.toggler').click();
    await page.getByRole('button', { name: 'Report User' }).click();

    await page.fill('textarea[id="confirm-dialog-input"]', 'The user was rude to me through chat for no reason, I only asked to barter with them.');
    await page.getByRole('button', { name: 'Yes, Report' }).click(); 
    await expect(page.locator('#toast-container')).toContainText('User reported successfully');
});

test('Report a skill', async ({ page }) => {
    await loginAsRoseG(page);

    await expect(page.locator('.skill-item')).toHaveCount(12);
    await page.locator('#link-offer--876').click();
    await expect(page).toHaveURL('/offers/-876');
    await page.getByTitle('Report Skill').first().click();
    await page.fill('textarea[id="confirm-dialog-input"]', 'Weird description. Sounds too suggestive and inappropriate');
    await page.getByRole('button', { name: 'Yes, Report' }).click();
    await expect(page.locator('#toast-container')).toContainText('Skill reported successfully');
});

async function loginAsRoseG(page: Page) {
    await page.goto('/login');
    await page.fill('input[formControlName="email"]', 'rose.g@example.com');
    await page.fill('input[formControlName="password"]', 'Password123!');
    await page.getByRole('button', { name: 'Login' }).click();
    await expect(page.locator('#toast-container')).toContainText('Login successful!');
    await expect(page).toHaveURL('/explore');
}
