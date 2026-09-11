import { expect, Page, test } from "@playwright/test";

const allProfiles = [
  "am.alex",
  "po.petra",
  "ia.ines",
  "ao.aaron",
  "finance.fran",
  "provider.pavel",
  "cnu.clara",
  "cnu.niko",
  "attendee.aria",
  "admin.taylor",
];
const activityManagers = ["am.alex", "po.petra", "ia.ines", "ao.aaron"];
const readOnlyProfiles = [
  "finance.fran",
  "provider.pavel",
  "cnu.clara",
  "cnu.niko",
  "attendee.aria",
  "admin.taylor",
];

async function signIn(page: Page, username: string) {
  await page.goto("/");
  await page.getByLabel("Test profile").selectOption(username);
  await page.getByLabel("Password").fill("demo-password");
  await page.getByRole("button", { name: "Sign in" }).click();
  await expect(page.getByRole("heading", { name: "Activities" })).toBeVisible();
  await expect(page.getByText("Activity Management Suite")).toBeVisible();
}

async function signOut(page: Page, username: string) {
  await page
    .getByRole("button", { name: `Open account menu for ${username}` })
    .click();
  await page.getByRole("button", { name: "Sign out" }).click();
  await expect(
    page.getByRole("heading", { name: "Event Operations" }),
  ).toBeVisible();
}

test.describe("synthetic profile journeys", () => {
  for (const username of allProfiles) {
    test(`${username} can sign in and sign out`, async ({ page }) => {
      await signIn(page, username);
      await signOut(page, username);
    });
  }

  test("invalid credentials are rejected", async ({ page }) => {
    await page.goto("/");
    await page.getByLabel("Password").fill("not-the-demo-password");
    await page.getByRole("button", { name: "Sign in" }).click();
    await expect(page.getByRole("alert")).toContainText(
      "Invalid username or password",
    );
  });
});

test.describe("activity workspace", () => {
  test("IA can order and paginate assigned activities", async ({ page }) => {
    await signIn(page, "ia.ines");
    await expect(page.getByText("2026-DEM-01")).toBeVisible();
    await page.getByLabel("Activities per page").selectOption("1");
    await expect(page.getByText("Page 1 of 2")).toBeVisible();
    await expect(page.getByText("2026-DEM-02")).not.toBeVisible();
    await page.getByRole("button", { name: "Next" }).click();
    await expect(page.getByText("Page 2 of 2")).toBeVisible();
    await expect(page.getByText("2026-DEM-02")).toBeVisible();
    await page.getByLabel("Order by").selectOption("date-desc");
    await expect(page.getByText("Page 1 of 2")).toBeVisible();
  });

  for (const username of activityManagers) {
    test(`${username} can access activity setup and editing controls`, async ({
      page,
    }) => {
      await signIn(page, username);
      await expect(
        page.getByRole("button", { name: "Add activity" }),
      ).toBeVisible();
      await page
        .getByRole("list", { name: "Activities" })
        .getByRole("button")
        .first()
        .click();
      await expect(
        page.getByRole("button", { name: "Edit activity" }),
      ).toBeVisible();
    });
  }

  for (const username of readOnlyProfiles) {
    test(`${username} cannot access activity setup or editing controls`, async ({
      page,
    }) => {
      await signIn(page, username);
      await expect(
        page.getByRole("button", { name: "Add activity" }),
      ).toHaveCount(0);
      const activities = page.getByRole("list", { name: "Activities" });
      if (await activities.count()) {
        await activities.getByRole("button").first().click();
        await expect(
          page.getByRole("button", { name: "Edit activity" }),
        ).toHaveCount(0);
      }
    });
  }

  test("AM creates, views, edits and invites CNUs for an activity", async ({
    page,
  }) => {
    await signIn(page, "am.alex");
    await page.getByRole("button", { name: "Add activity" }).click();
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
    await page.getByLabel("Default CPL reference (all orders)").fill("CPL-E2E");
    await page.getByLabel("Travel CPL").fill("CPL-TRAVEL");
    await page.getByLabel("Curricula file").setInputFiles({
      name: "curriculum.pdf",
      mimeType: "application/pdf",
      buffer: Buffer.from("synthetic curriculum"),
    });
    await page.getByRole("button", { name: "Create activity" }).click();
    await expect(page.getByText(code)).toBeVisible();

    const activity = page.getByRole("listitem").filter({ hasText: code });
    await activity.getByRole("button").click();
    await expect(
      page.getByRole("heading", { name: "E2E activity" }),
    ).toBeVisible();
    await expect(page.getByText("curriculum.pdf")).toBeVisible();
    await page.getByRole("button", { name: "Edit activity" }).click();
    await page.getByLabel("Title").fill("Edited E2E activity");
    await page.getByRole("button", { name: "Save changes" }).click();
    await expect(
      page.getByRole("heading", { name: "Edited E2E activity" }),
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
});
