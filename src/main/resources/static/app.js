const state = {
    selectedPersonnel: null,
    currentUser: null,
    security: {
        users: [],
        roles: [],
        permissions: [],
    },
};

const yuanFormatter = new Intl.NumberFormat("zh-CN", {
    minimumFractionDigits: 0,
    maximumFractionDigits: 2,
});

document.addEventListener("DOMContentLoaded", () => {
    document.getElementById("personnel-search").addEventListener("submit", onPersonnelSearch);
    document.getElementById("audit-form").addEventListener("submit", onAudit);
    document.getElementById("create-user-form").addEventListener("submit", onCreateUser);
    document.getElementById("create-role-form").addEventListener("submit", onCreateRole);
    document.getElementById("logout-button").addEventListener("click", () => {
        window.location.href = "/logout";
    });
    initializeAuth();
});

async function initializeAuth() {
    try {
        const user = await getJson("/api/auth/me");
        state.currentUser = user;
        document.getElementById("current-user").textContent = `${user.displayName} (${user.username})`;
        if (user.permissions.includes("SECURITY_ADMIN")) {
            document.getElementById("security-nav").classList.remove("hidden");
            document.getElementById("security").classList.remove("hidden");
            await loadSecurityAdmin();
        }
        await loadPersonnel();
    } catch (error) {
        window.location.href = "/login.html";
    }
}

async function onCreateUser(event) {
    event.preventDefault();
    await postJson("/api/security/users", {
        username: document.getElementById("new-username").value.trim(),
        displayName: document.getElementById("new-display-name").value.trim(),
        password: document.getElementById("new-password").value,
        enabled: true,
    });
    event.target.reset();
    await loadSecurityAdmin();
}

async function onCreateRole(event) {
    event.preventDefault();
    await postJson("/api/security/roles", {
        code: document.getElementById("new-role-code").value.trim(),
        name: document.getElementById("new-role-name").value.trim(),
        dataScope: document.getElementById("new-role-scope").value,
    });
    event.target.reset();
    await loadSecurityAdmin();
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

async function loadSecurityAdmin() {
    const status = document.getElementById("security-status");
    status.className = "status";
    status.textContent = "正在加载权限配置...";
    try {
        const [users, roles, permissions] = await Promise.all([
            getJson("/api/security/users"),
            getJson("/api/security/roles"),
            getJson("/api/security/permissions"),
        ]);
        state.security = { users, roles, permissions };
        renderSecurityAdmin();
        status.textContent = "权限配置已加载";
    } catch (error) {
        showError(status, error);
    }
}

function renderSecurityAdmin() {
    document.getElementById("security-user-rows").innerHTML = state.security.users.map(user => `
        <tr>
            <td>${escapeHtml(user.id)}</td>
            <td>${escapeHtml(user.username)}</td>
            <td>${escapeHtml(user.displayName)}</td>
            <td>${user.enabled ? "是" : "否"}</td>
            <td><input class="inline-input" id="user-roles-${user.id}" value="${escapeHtml((user.roleCodes || []).join(","))}"></td>
            <td>
                <button class="row-action" data-user-save="${user.id}">保存角色</button>
                <button class="row-action" data-user-toggle="${user.id}" data-enabled="${!user.enabled}">${user.enabled ? "停用" : "启用"}</button>
            </td>
        </tr>
    `).join("");

    document.getElementById("security-role-rows").innerHTML = state.security.roles.map(role => `
        <tr>
            <td>${escapeHtml(role.id)}</td>
            <td>${escapeHtml(role.code)}</td>
            <td>${escapeHtml(role.name)}</td>
            <td>${escapeHtml(role.dataScope)}</td>
            <td><input class="inline-input" id="role-permissions-${role.id}" value="${escapeHtml((role.permissionCodes || []).join(","))}"></td>
            <td><input class="inline-input" id="role-organizations-${role.id}" value="${escapeHtml((role.organizationCodes || []).join(","))}" placeholder="ALL 时可留空"></td>
            <td>
                <button class="row-action" data-role-permissions="${role.id}">保存权限</button>
                <button class="row-action" data-role-organizations="${role.id}">保存单位</button>
            </td>
        </tr>
    `).join("");

    document.getElementById("permission-list").innerHTML = state.security.permissions.map(permission => `
        <span><strong>${escapeHtml(permission.code)}</strong>${escapeHtml(permission.name)}</span>
    `).join("");

    document.querySelectorAll("[data-user-save]").forEach(button => {
        button.addEventListener("click", async () => {
            const id = button.dataset.userSave;
            await putJson(`/api/security/users/${id}/roles`, { codes: splitCodes(document.getElementById(`user-roles-${id}`).value) });
            await loadSecurityAdmin();
        });
    });
    document.querySelectorAll("[data-user-toggle]").forEach(button => {
        button.addEventListener("click", async () => {
            await putJson(`/api/security/users/${button.dataset.userToggle}/enabled`, { enabled: button.dataset.enabled === "true" });
            await loadSecurityAdmin();
        });
    });
    document.querySelectorAll("[data-role-permissions]").forEach(button => {
        button.addEventListener("click", async () => {
            const id = button.dataset.rolePermissions;
            await putJson(`/api/security/roles/${id}/permissions`, { codes: splitCodes(document.getElementById(`role-permissions-${id}`).value) });
            await loadSecurityAdmin();
        });
    });
    document.querySelectorAll("[data-role-organizations]").forEach(button => {
        button.addEventListener("click", async () => {
            const id = button.dataset.roleOrganizations;
            await putJson(`/api/security/roles/${id}/organizations`, { codes: splitCodes(document.getElementById(`role-organizations-${id}`).value) });
            await loadSecurityAdmin();
        });
    });
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

async function postJson(url, body) {
    return writeJson("POST", url, body);
}

async function putJson(url, body) {
    return writeJson("PUT", url, body);
}

async function writeJson(method, url, body) {
    const response = await fetch(url, {
        method,
        headers: {
            Accept: "application/json",
            "Content-Type": "application/json",
        },
        body: JSON.stringify(body),
    });
    if (!response.ok) {
        const text = await response.text();
        throw new Error(text || `HTTP ${response.status}`);
    }
    const contentType = response.headers.get("content-type") || "";
    return contentType.includes("application/json") ? response.json() : null;
}

function splitCodes(value) {
    return String(value || "")
        .split(",")
        .map(item => item.trim())
        .filter(Boolean);
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
