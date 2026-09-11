import { expect, test } from "@playwright/test";

test.describe("activity setup journey", () => {
  test("AM can sign in, view active activities and create an activity", async ({
    page,
  }) => {
    await page.goto("/");
    await expect(
      page.getByRole("heading", { name: "Event Operations" }),
    ).toBeVisible();
    await page.getByLabel("Test profile").selectOption("am.alex");
    await page.getByLabel("Password").fill("demo-password");
    await page.getByRole("button", { name: "Sign in" }).click();
    await expect(
      page.getByRole("heading", { name: "Activities" }),
    ).toBeVisible();
    await page.getByRole("button", { name: "Add activity" }).first().click();
    await expect(
      page.getByRole("heading", { name: "Set up an activity" }),
    ).toBeVisible();
    const code = `E2E-${Date.now()}`;
    await page.getByLabel("Course code").fill(code);
    await page.getByLabel("Title").fill("E2E activity");
    await page
      .getByLabel("Description")
      .fill("Created by the supported setup journey");
    await page.getByLabel("Country code").fill("HU");
    await page.getByLabel("Venue").fill("Budapest");
    await page.getByLabel("Time zone").fill("Europe/Budapest");
    await page.getByLabel("Expected participants").fill("12");
    await page.getByLabel("Starts").fill("2027-01-10");
    await page.getByLabel("Ends").fill("2027-01-12");
    await page.getByLabel("Funding regime").fill("STANDARD");
    await page.getByRole("button", { name: "Create activity" }).click();
    await expect(
      page.getByRole("heading", { name: "Activities" }),
    ).toBeVisible();
    await expect(page.getByText(code)).toBeVisible();

    const activityCard = page.locator("li").filter({ hasText: code });
    await activityCard.getByRole("button").click();
    await expect(
      page.getByRole("heading", { name: "E2E activity" }),
    ).toBeVisible();
    const invitationSection = page
      .getByRole("heading", { name: "Nomination invitations" })
      .locator("..");
    await invitationSection.locator('input[type="checkbox"]').first().check();
    await invitationSection
      .getByRole("button", { name: "Send nomination invitation" })
      .click();
    await expect(invitationSection).toContainText(
      "invitation(s) recorded for nomination",
    );
  });

  test("invalid credentials are rejected", async ({ page }) => {
    await page.goto("/");
    await page.getByLabel("Password").fill("not-the-demo-password");
    await page.getByRole("button", { name: "Sign in" }).click();
    await expect(page.getByRole("alert")).toContainText(
      "Invalid username or password",
    );
  });
});
