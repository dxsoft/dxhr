import fs from "node:fs";
import path from "node:path";
import { fileURLToPath } from "node:url";
import { parseHTML } from "linkedom";
import fetch from "node-fetch";

const __dirname = path.dirname(fileURLToPath(import.meta.url));
const root = path.resolve(__dirname, "..");
const html = fs.readFileSync(path.join(root, "src/main/resources/static/index.html"), "utf8");
const js = fs.readFileSync(path.join(root, "src/main/resources/static/app.js"), "utf8");

const { document, window, customElements } = parseHTML(html);
const errors = [];
const domReadyHandlers = [];
const originalDocAdd = document.addEventListener.bind(document);
document.addEventListener = (type, handler, options) => {
    if (type === "DOMContentLoaded") {
        domReadyHandlers.push(handler);
        return;
    }
    return originalDocAdd(type, handler, options);
};
window.location = { href: "", assign() {}, replace() {} };
window.history = { pushState() {}, replaceState() {} };
window.fetch = fetch;
window.Intl = Intl;
window.URL = URL;
window.FormData = class FormData {};
window.Blob = class Blob {};
window.alert = () => {};
window.confirm = () => true;
window.print = () => {};
window.requestAnimationFrame = (cb) => setTimeout(cb, 0);
window.getComputedStyle = () => ({ getPropertyValue: () => "" });

globalThis.window = window;
globalThis.document = document;
globalThis.customElements = customElements;
globalThis.location = window.location;
globalThis.history = window.history;
globalThis.fetch = window.fetch;
globalThis.Intl = window.Intl;
globalThis.URL = window.URL;
globalThis.FormData = window.FormData;
globalThis.Blob = window.Blob;
globalThis.alert = window.alert;
globalThis.confirm = window.confirm;
globalThis.print = window.print;
globalThis.requestAnimationFrame = window.requestAnimationFrame;
globalThis.getComputedStyle = window.getComputedStyle;

try {
    eval(js);
} catch (error) {
    errors.push(`eval: ${error.stack || error.message}`);
}

for (const handler of domReadyHandlers) {
    try {
        handler();
    } catch (error) {
        errors.push(`DOMContentLoaded: ${error.stack || error.message}`);
    }
}

await new Promise((resolve) => setTimeout(resolve, 0));

console.log(JSON.stringify({
    domContentLoadedErrors: errors,
    currentUser: document.getElementById("current-user")?.textContent,
    navLinks: document.querySelectorAll("#main-nav a").length,
    quickLinks: document.querySelectorAll("#dashboard-quick-links a").length,
    groups: document.querySelectorAll("#dashboard-group-links section").length,
}, null, 2));
