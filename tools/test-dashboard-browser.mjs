import { chromium } from "playwright";

const browser = await chromium.launch();
const page = await browser.newPage();
const errors = [];
page.on("pageerror", (error) => errors.push(`pageerror: ${error.message}`));
page.on("console", (message) => {
    if (message.type() === "error") {
        errors.push(`console: ${message.text()}`);
    }
});

await page.goto("http://127.0.0.1:8081/login");
await page.fill("input[name=username]", "admin");
await page.fill("input[name=password]", "admin123");
await page.click("button[type=submit]");
await page.waitForURL("**/");
await page.waitForTimeout(3000);

const result = {
    currentUser: await page.textContent("#current-user"),
    navLinks: await page.locator("#main-nav a").count(),
    quickLinks: await page.locator("#dashboard-quick-links a").count(),
    groups: await page.locator("#dashboard-group-links section").count(),
    totalFunctions: await page.textContent("#dashboard-total-functions"),
    errors,
};

console.log(JSON.stringify(result, null, 2));
await browser.close();
