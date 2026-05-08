const state = {
    selectedPersonnel: null,
};

const yuanFormatter = new Intl.NumberFormat("zh-CN", {
    minimumFractionDigits: 0,
    maximumFractionDigits: 2,
});

document.addEventListener("DOMContentLoaded", () => {
    document.getElementById("personnel-search").addEventListener("submit", onPersonnelSearch);
    document.getElementById("audit-form").addEventListener("submit", onAudit);
    document.getElementById("logout-button").addEventListener("click", () => {
        window.location.href = "/logout";
    });
    initializeAuth();
});

async function initializeAuth() {
    try {
        const user = await getJson("/api/auth/me");
        document.getElementById("current-user").textContent = `${user.displayName} (${user.username})`;
        await loadPersonnel();
    } catch (error) {
        window.location.href = "/login.html";
    }
}

async function onPersonnelSearch(event) {
    event.preventDefault();
    await loadPersonnel();
}

async function onAudit(event) {
    event.preventDefault();
    await loadAudit();
}

async function loadPersonnel() {
    const organizationCode = document.getElementById("organization-code").value.trim();
    const keyword = document.getElementById("keyword").value.trim();
    const size = document.getElementById("page-size").value || "10";
    const params = new URLSearchParams({ page: "0", size });
    if (organizationCode) {
        params.set("organizationCode", organizationCode);
    }
    if (keyword) {
        params.set("keyword", keyword);
    }

    const status = document.getElementById("personnel-status");
    const rows = document.getElementById("personnel-rows");
    status.className = "status";
    status.textContent = "正在查询人员...";
    rows.innerHTML = "";

    try {
        const page = await getJson(`/api/personnel?${params}`);
        status.textContent = `共 ${page.totalElements} 人，当前显示 ${page.content.length} 人`;
        rows.innerHTML = page.content.map(person => `
            <tr>
                <td>${escapeHtml(person.uid)}</td>
                <td>${escapeHtml(person.organizationCode)} ${escapeHtml(person.organizationName || "")}</td>
                <td>${escapeHtml(person.personCode)}</td>
                <td>${escapeHtml(person.name)}</td>
                <td>${escapeHtml(person.idCard || "")}</td>
                <td>${escapeHtml(person.currentPosition || "")}</td>
                <td><button class="row-action" data-uid="${person.uid}">工资试算</button></td>
            </tr>
        `).join("");
        rows.querySelectorAll("button[data-uid]").forEach(button => {
            button.addEventListener("click", () => loadPreview(button.dataset.uid));
        });
    } catch (error) {
        showError(status, error);
    }
}

async function loadPreview(uid) {
    const status = document.getElementById("preview-status");
    const empty = document.getElementById("preview-empty");
    const content = document.getElementById("preview-content");
    status.className = "status";
    status.textContent = "正在加载工资试算...";
    empty.classList.add("hidden");
    content.classList.add("hidden");

    try {
        const preview = await getJson(`/api/payroll/personnel/${uid}/calculation-preview`);
        state.selectedPersonnel = preview;
        document.getElementById("selected-person").textContent =
            `${preview.name} / ${preview.organizationCode}-${preview.personCode}`;
        document.getElementById("preview-period").textContent = preview.calculationPeriod || "-";
        document.getElementById("preview-total").textContent = money(preview.recalculatedKnownTotal);
        document.getElementById("stored-total").textContent = money(preview.storedTotal);
        const diff = document.getElementById("total-difference");
        diff.textContent = money(preview.totalDifference);
        diff.className = Number(preview.totalDifference) === 0 ? "difference-ok" : "difference-bad";

        document.getElementById("component-rows").innerHTML = preview.calculatedComponents.map(component => `
            <tr>
                <td>${escapeHtml(component.fieldName)}</td>
                <td>${escapeHtml(component.caption)}</td>
                <td>${money(component.amount)}</td>
                <td>${escapeHtml(component.source)}</td>
            </tr>
        `).join("");

        document.getElementById("excluded-rows").innerHTML = preview.excludedComponents.map(component => `
            <tr>
                <td>${escapeHtml(component.fieldName)}</td>
                <td>${escapeHtml(component.caption)}</td>
                <td>${money(component.storedAmount)}</td>
                <td>${escapeHtml(component.reason)}</td>
            </tr>
        `).join("");

        const pgbc = preview.pgbcComparison;
        document.getElementById("pgbc-card").innerHTML = `
            <strong>处理方式：</strong>${escapeHtml(pgbc.treatment)}<br>
            <strong>旧值：</strong>${money(pgbc.storedAmount)}
            <strong>对账值：</strong>${money(pgbc.comparisonAmount)}<br>
            <span>${escapeHtml(pgbc.note)}</span>
        `;

        status.textContent = "工资试算加载完成";
        content.classList.remove("hidden");
        document.getElementById("payroll").scrollIntoView({ behavior: "smooth", block: "start" });
    } catch (error) {
        empty.classList.remove("hidden");
        showError(status, error);
    }
}

async function loadAudit() {
    const organizationCode = document.getElementById("audit-organization-code").value.trim();
    const page = document.getElementById("audit-page").value || "0";
    const size = document.getElementById("audit-size").value || "5";
    const params = new URLSearchParams({ page, size });
    if (organizationCode) {
        params.set("organizationCode", organizationCode);
    }

    const status = document.getElementById("audit-status");
    const rows = document.getElementById("audit-rows");
    status.className = "status";
    status.textContent = "正在执行批量对账，远程数据库可能需要等待...";
    rows.innerHTML = "";

    try {
        const summary = await getJson(`/api/payroll/calculation-audit-summary?${params}`);
        document.getElementById("audit-total").textContent = summary.totalPersonnelWithHistory;
        document.getElementById("audit-compared").textContent = summary.comparedPersonnel;
        document.getElementById("audit-difference-count").textContent = summary.differenceCount;
        document.getElementById("audit-max-difference").textContent = money(summary.maxAbsoluteDifference);
        rows.innerHTML = summary.differences.map(item => `
            <tr>
                <td>${escapeHtml(item.uid)}</td>
                <td>${escapeHtml(item.name)}</td>
                <td>${escapeHtml(item.calculationPeriod)}</td>
                <td>${money(item.storedTotal)}</td>
                <td>${money(item.recalculatedKnownTotal)}</td>
                <td class="${Number(item.totalDifference) === 0 ? "difference-ok" : "difference-bad"}">${money(item.totalDifference)}</td>
                <td>${item.componentDifferences.map(diff => `${escapeHtml(diff.fieldName)}(${money(diff.difference)})`).join("，")}</td>
            </tr>
        `).join("");
        status.textContent = `已比较 ${summary.comparedPersonnel} 人，差异 ${summary.differenceCount} 人`;
    } catch (error) {
        showError(status, error);
    }
}

async function getJson(url) {
    const response = await fetch(url, { headers: { Accept: "application/json" } });
    if (response.redirected && response.url.includes("/login.html")) {
        window.location.href = "/login.html";
        throw new Error("需要登录");
    }
    if (!response.ok) {
        const text = await response.text();
        throw new Error(text || `HTTP ${response.status}`);
    }
    const contentType = response.headers.get("content-type") || "";
    if (!contentType.includes("application/json")) {
        window.location.href = "/login.html";
        throw new Error("需要登录");
    }
    return response.json();
}

function money(value) {
    if (value === null || value === undefined || value === "") {
        return "-";
    }
    const number = Number(value);
    if (!Number.isFinite(number)) {
        return String(value);
    }
    return yuanFormatter.format(number);
}

function showError(element, error) {
    element.className = "status error";
    element.textContent = `出错：${error.message || error}`;
}

function escapeHtml(value) {
    return String(value ?? "")
        .replaceAll("&", "&amp;")
        .replaceAll("<", "&lt;")
        .replaceAll(">", "&gt;")
        .replaceAll('"', "&quot;")
        .replaceAll("'", "&#039;");
}
