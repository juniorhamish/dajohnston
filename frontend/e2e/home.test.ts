import { expect, test } from "@playwright/test";

test("has title", async ({ page }) => {
  await page.goto("/");

  // Expect a title "to contain" a substring.
  await expect(page).toHaveTitle(/Multi-App Portal/);
});

test("login button exists", async ({ page }) => {
  await page.goto("/");

  // Expect to see a login link or button
  const loginLink = page.getByRole("link", { name: /login|log in/i });
  await expect(loginLink).toBeVisible();
});
