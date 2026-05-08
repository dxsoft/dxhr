const state = {
    selectedPersonnel: null,
    currentUser: null,
    security: {
        users: [],
        roles: [],
        permissions: [],
        organizations: [],
        auditLogs: [],
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
    document.getElementById("change-password-form").addEventListener("submit", onChangePassword);
    ["security-user-filter", "security-role-filter", "security-organization-filter", "security-audit-filter"].forEach(id => {
        document.getElementById(id).addEventListener("input", debounceSecurityReload);
    });
    ["security-user-page", "security-role-page", "security-audit-page", "security-page-size"].forEach(id => {
        document.getElementById(id).addEventListener("change", loadSecurityAdmin);
    });
    document.getElementById("security-refresh-button").addEventListener("click", loadSecurityAdmin);
    document.getElementById("change-password-button").addEventListener("click", () => {
        document.getElementById("password-panel").classList.toggle("hidden");
    });
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

async function onChangePassword(event) {
    event.preventDefault();
    const status = document.getElementById("password-status");
    const currentPassword = document.getElementById("current-password").value;
    const newPassword = document.getElementById("new-own-password").value;
    const confirmPassword = document.getElementById("confirm-own-password").value;
    status.className = "status";
    if (newPassword !== confirmPassword) {
        showError(status, new Error("两次输入的新密码不一致"));
        return;
    }
    if (newPassword.length < 8) {
        showError(status, new Error("新密码长度至少 8 位"));
        return;
    }
    try {
        await putJson("/api/auth/password", { currentPassword, newPassword });
        event.target.reset();
        status.textContent = "密码修改成功，请妥善保存新密码。";
    } catch (error) {
        showError(status, error);
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

async function loadSecurityAdmin() {
    const status = document.getElementById("security-status");
    status.className = "status";
    status.textContent = "正在加载权限配置...";
    try {
        const pageSize = document.getElementById("security-page-size").value || "20";
        const userParams = new URLSearchParams({
            keyword: document.getElementById("security-user-filter").value.trim(),
            page: document.getElementById("security-user-page").value || "0",
            size: pageSize,
        });
        const roleParams = new URLSearchParams({
            keyword: document.getElementById("security-role-filter").value.trim(),
            page: document.getElementById("security-role-page").value || "0",
            size: pageSize,
        });
        const auditParams = new URLSearchParams({
            keyword: document.getElementById("security-audit-filter").value.trim(),
            page: document.getElementById("security-audit-page").value || "0",
            size: pageSize,
        });
        const [users, roles, permissions, organizations, auditLogs] = await Promise.all([
            getJson(`/api/security/users-page?${userParams}`),
            getJson(`/api/security/roles-page?${roleParams}`),
            getJson("/api/security/permissions"),
            getJson("/api/organizations?size=200"),
            getJson(`/api/security/audit-logs-page?${auditParams}`),
        ]);
        state.security = {
            users: users.content || [],
            userPage: users,
            roles: roles.content || [],
            rolePage: roles,
            permissions,
            organizations: organizations.content || [],
            auditLogs: auditLogs.content || [],
            auditPage: auditLogs,
        };
        renderSecurityAdmin();
        status.textContent = "权限配置已加载";
    } catch (error) {
        showError(status, error);
    }
}

function renderSecurityAdmin() {
    const userFilter = normalizedFilter("security-user-filter");
    const roleFilter = normalizedFilter("security-role-filter");
    const users = state.security.users.filter(user =>
        matchesFilter(userFilter, user.username, user.displayName, (user.roleCodes || []).join(",")));
    const roles = state.security.roles.filter(role =>
        matchesFilter(roleFilter, role.code, role.name, role.dataScope));
    const auditLogs = state.security.auditLogs || [];

    document.getElementById("security-user-rows").innerHTML = users.map(user => `
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
    renderPageInfo("security-user-page-info", state.security.userPage);

    document.getElementById("security-role-rows").innerHTML = roles.map(role => `
        <tr>
            <td>${escapeHtml(role.id)}</td>
            <td>${escapeHtml(role.code)}</td>
            <td>${escapeHtml(role.name)}</td>
            <td>${escapeHtml(role.dataScope)}</td>
            <td>${renderPermissionChoices(role)}</td>
            <td>${renderOrganizationChoices(role)}</td>
            <td>
                <button class="row-action" data-role-permissions="${role.id}">保存权限</button>
                <button class="row-action" data-role-organizations="${role.id}">保存单位</button>
            </td>
        </tr>
    `).join("");
    renderPageInfo("security-role-page-info", state.security.rolePage);

    document.getElementById("permission-list").innerHTML = state.security.permissions.map(permission => `
        <span><strong>${escapeHtml(permission.code)}</strong>${escapeHtml(permission.name)}</span>
    `).join("");

    document.getElementById("security-audit-rows").innerHTML = auditLogs.map(log => `
        <tr>
            <td>${escapeHtml(log.id)}</td>
            <td>${escapeHtml(log.actorUsername)}</td>
            <td>${escapeHtml(log.action)}</td>
            <td>${escapeHtml(log.targetType)}:${escapeHtml(log.targetId)}</td>
            <td>${escapeHtml(log.summary)}</td>
            <td>${escapeHtml(log.createdAt)}</td>
        </tr>
    `).join("");
    renderPageInfo("security-audit-page-info", state.security.auditPage);

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
            await putJson(`/api/security/roles/${id}/permissions`, { codes: checkedValues(`role-permissions-${id}`) });
            await loadSecurityAdmin();
        });
    });
    document.querySelectorAll("[data-role-organizations]").forEach(button => {
        button.addEventListener("click", async () => {
            const id = button.dataset.roleOrganizations;
            await putJson(`/api/security/roles/${id}/organizations`, { codes: checkedValues(`role-organizations-${id}`) });
            await loadSecurityAdmin();
        });
    });
}

let securityReloadTimer = null;

function debounceSecurityReload() {
    clearTimeout(securityReloadTimer);
    securityReloadTimer = setTimeout(() => {
        document.getElementById("security-user-page").value = "0";
        document.getElementById("security-role-page").value = "0";
        document.getElementById("security-audit-page").value = "0";
        loadSecurityAdmin();
    }, 350);
}

function renderPageInfo(elementId, page) {
    const element = document.getElementById(elementId);
    if (!page) {
        element.textContent = "";
        return;
    }
    element.textContent = `第 ${page.page + 1} / ${Math.max(page.totalPages, 1)} 页，共 ${page.totalElements} 条`;
}

function renderPermissionChoices(role) {
    const selected = new Set(role.permissionCodes || []);
    return `<div class="checkbox-grid" id="role-permissions-${role.id}">
        ${state.security.permissions.map(permission => `
            <label class="checkbox-item" title="${escapeHtml(permission.name)}">
                <input type="checkbox" value="${escapeHtml(permission.code)}" ${selected.has(permission.code) ? "checked" : ""}>
                <span>${escapeHtml(permission.code)}</span>
            </label>
        `).join("")}
    </div>`;
}

function renderOrganizationChoices(role) {
    if (role.dataScope === "ALL") {
        return `<div class="scope-all">全部单位</div>`;
    }
    const selected = new Set(role.organizationCodes || []);
    const filter = normalizedFilter("security-organization-filter");
    const organizations = state.security.organizations.filter(org =>
        selected.has(org.organizationCode) || matchesFilter(filter, org.organizationCode, org.name));
    return `<div class="checkbox-grid organization-grid" id="role-organizations-${role.id}">
        ${organizations.map(org => `
            <label class="checkbox-item" title="${escapeHtml(org.name || "")}">
                <input type="checkbox" value="${escapeHtml(org.organizationCode)}" ${selected.has(org.organizationCode) ? "checked" : ""}>
                <span>${escapeHtml(org.organizationCode)} ${escapeHtml(org.name || "")}</span>
            </label>
        `).join("")}
    </div>`;
}

function normalizedFilter(inputId) {
    const element = document.getElementById(inputId);
    return element ? element.value.trim().toLowerCase() : "";
}

function matchesFilter(filter, ...values) {
    if (!filter) {
        return true;
    }
    return values.some(value => String(value || "").toLowerCase().includes(filter));
}

function checkedValues(containerId) {
    const container = document.getElementById(containerId);
    if (!container) {
        return [];
    }
    return Array.from(container.querySelectorAll("input[type='checkbox']:checked"))
        .map(input => input.value);
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
