import puppeteer from "puppeteer-core";

const browser = await puppeteer.launch({
    executablePath: "C:\\Program Files\\Google\\Chrome\\Application\\chrome.exe",
    headless: true,
    args: ["--no-sandbox"],
});
const page = await browser.newPage();
const errors = [];
page.on("pageerror", (error) => errors.push(`pageerror: ${error.message}`));
page.on("console", (message) => {
    if (message.type() === "error") {
        errors.push(`console: ${message.text()}`);
    }
});

await page.goto("http://127.0.0.1:8081/login", { waitUntil: "networkidle0" });
await page.type("input[name=username]", "admin");
await page.type("input[name=password]", "admin123");
await Promise.all([
    page.waitForNavigation({ waitUntil: "networkidle0" }),
    page.click("button[type=submit]"),
]);
await page.waitForTimeout(3000);

const result = {
    url: page.url(),
    currentUser: await page.$eval("#current-user", (el) => el.textContent),
    navLinks: await page.$$eval("#main-nav a", (links) => links.length),
    quickLinks: await page.$$eval("#dashboard-quick-links a", (links) => links.length),
    groups: await page.$$eval("#dashboard-group-links section", (sections) => sections.length),
    totalFunctions: await page.$eval("#dashboard-total-functions", (el) => el.textContent).catch(() => null),
    errors,
};

console.log(JSON.stringify(result, null, 2));
await browser.close();
