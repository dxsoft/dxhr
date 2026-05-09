const state = {
    selectedPersonnel: null,
    currentUser: null,
    menus: [],
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
    document.getElementById("annual-assessments-form").addEventListener("submit", onAnnualAssessmentsSearch);
    document.getElementById("assessment-summary-form").addEventListener("submit", onAssessmentSummarySearch);
    document.getElementById("changed-personnel-form").addEventListener("submit", onChangedPersonnelSearch);
    document.getElementById("position-history-form").addEventListener("submit", onPositionHistorySearch);
    document.getElementById("education-history-form").addEventListener("submit", onEducationHistorySearch);
    document.getElementById("organization-maintenance-form").addEventListener("submit", onOrganizationMaintenanceSearch);
    document.getElementById("dictionary-maintenance-form").addEventListener("submit", onDictionarySearch);
    document.getElementById("local-policy-form").addEventListener("submit", onLocalPolicySearch);
    document.getElementById("audit-form").addEventListener("submit", onAudit);
    document.getElementById("payroll-history-form").addEventListener("submit", onPayrollHistorySearch);
    document.getElementById("teaching-allowance-form").addEventListener("submit", onTeachingAllowanceSearch);
    document.getElementById("normal-promotion-form").addEventListener("submit", onNormalPromotionSearch);
    document.getElementById("level-promotion-form").addEventListener("submit", onLevelPromotionSearch);
    document.getElementById("position-change-promotion-form").addEventListener("submit", onPositionChangePromotionSearch);
    document.getElementById("basic-standards-form").addEventListener("submit", onBasicStandardsSearch);
    document.getElementById("allowance-standards-form").addEventListener("submit", onAllowanceStandardsSearch);
    document.getElementById("rank-allowance-standards-form").addEventListener("submit", onRankAllowanceStandardsSearch);
    document.getElementById("retained-allowance-standards-form").addEventListener("submit", onRetainedAllowanceStandardsSearch);
    document.getElementById("year-allowance-standards-form").addEventListener("submit", onYearAllowanceStandardsSearch);
    document.getElementById("intern-salary-standards-form").addEventListener("submit", onInternSalaryStandardsSearch);
    document.getElementById("wage-reform-standards-form").addEventListener("submit", onWageReformStandardsSearch);
    document.getElementById("other-allowance-standards-form").addEventListener("submit", onOtherAllowanceStandardsSearch);
    document.getElementById("create-user-form").addEventListener("submit", onCreateUser);
    document.getElementById("create-role-form").addEventListener("submit", onCreateRole);
    document.getElementById("create-menu-form").addEventListener("submit", onCreateMenu);
    document.getElementById("change-password-form").addEventListener("submit", onChangePassword);
    ["security-user-filter", "security-role-filter", "security-organization-filter", "security-menu-filter", "security-audit-filter"].forEach(id => {
        document.getElementById(id).addEventListener("input", debounceSecurityReload);
    });
    ["security-user-page", "security-role-page", "security-menu-page", "security-audit-page", "security-page-size"].forEach(id => {
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
        const menus = await getJson("/api/auth/menus");
        state.currentUser = user;
        state.menus = menus;
        document.getElementById("current-user").textContent = `${user.displayName} (${user.username})`;
        renderMenus();
        if (hasMenu("SECURITY")) {
            await loadSecurityAdmin();
        }
        if (hasMenu("PERSONNEL")) {
            await loadPersonnel();
        }
        if (hasMenu("ANNUAL_ASSESSMENTS")) {
            await loadAnnualAssessments();
        }
        if (hasMenu("ASSESSMENT_SUMMARY")) {
            await loadAssessmentSummary();
        }
        if (hasMenu("CHANGED_PERSONNEL")) {
            await loadChangedPersonnel();
        }
        if (hasMenu("POSITION_HISTORY")) {
            await loadPositionHistory();
        }
        if (hasMenu("EDUCATION_HISTORY")) {
            await loadEducationHistory();
        }
        if (hasMenu("ORGANIZATION_MAINTENANCE")) {
            await loadOrganizationMaintenance();
        }
        if (hasMenu("DICTIONARY_MAINTENANCE")) {
            await loadDictionaries();
        }
        if (hasMenu("LOCAL_POLICY_CONFIG")) {
            await loadLocalPolicies();
        }
        if (hasMenu("PAYROLL_HISTORY")) {
            await loadPayrollHistory();
        }
        if (hasMenu("TEACHING_ALLOWANCE_ADJUSTMENT")) {
            await loadTeachingAllowanceAdjustments();
        }
        if (hasMenu("NORMAL_PROMOTION")) {
            await loadNormalPromotions();
        }
        if (hasMenu("LEVEL_PROMOTION")) {
            await loadLevelPromotions();
        }
        if (hasMenu("POSITION_CHANGE_PROMOTION")) {
            await loadPositionChangePromotions();
        }
        if (hasMenu("BASIC_STANDARDS")) {
            await loadBasicStandards();
        }
        if (hasMenu("ALLOWANCE_STANDARDS")) {
            await loadAllowanceStandards();
        }
        if (hasMenu("RANK_ALLOWANCE_STANDARDS")) {
            await loadRankAllowanceStandards();
        }
        if (hasMenu("RETAINED_ALLOWANCE_STANDARDS")) {
            await loadRetainedAllowanceStandards();
        }
        if (hasMenu("YEAR_ALLOWANCE_STANDARDS")) {
            await loadYearAllowanceStandards();
        }
        if (hasMenu("INTERN_SALARY_STANDARDS")) {
            await loadInternSalaryStandards();
        }
        if (hasMenu("WAGE_REFORM_STANDARDS")) {
            await loadWageReformStandards();
        }
        if (hasMenu("OTHER_ALLOWANCE_STANDARDS")) {
            await loadOtherAllowanceStandards();
        }
    } catch (error) {
        window.location.href = "/login.html";
    }
}

function renderMenus() {
    const nav = document.getElementById("main-nav");
    nav.innerHTML = state.menus.map(menu => `
        <a href="${escapeHtml(menu.path)}" data-menu-link="${escapeHtml(menu.code)}">${escapeHtml(menu.title)}</a>
    `).join("");
    document.querySelectorAll("[data-menu-code]").forEach(section => {
        section.classList.toggle("hidden", !hasMenu(section.dataset.menuCode));
    });
}

function hasMenu(code) {
    return state.menus.some(menu => menu.code === code);
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

async function onCreateMenu(event) {
    event.preventDefault();
    await postJson("/api/security/menus", {
        code: document.getElementById("new-menu-code").value.trim(),
        title: document.getElementById("new-menu-title").value.trim(),
        path: document.getElementById("new-menu-path").value.trim(),
        permissionCode: document.getElementById("new-menu-permission").value.trim(),
        sortOrder: Number(document.getElementById("new-menu-sort").value || 0),
        enabled: true,
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

async function onAnnualAssessmentsSearch(event) {
    event.preventDefault();
    document.getElementById("assessment-page").value = "0";
    await loadAnnualAssessments();
}

async function onAssessmentSummarySearch(event) {
    event.preventDefault();
    document.getElementById("assessment-summary-page").value = "0";
    await loadAssessmentSummary();
}

async function onChangedPersonnelSearch(event) {
    event.preventDefault();
    document.getElementById("changed-personnel-page").value = "0";
    await loadChangedPersonnel();
}

async function onPositionHistorySearch(event) {
    event.preventDefault();
    document.getElementById("position-history-page").value = "0";
    await loadPositionHistory();
}

async function onEducationHistorySearch(event) {
    event.preventDefault();
    document.getElementById("education-history-page").value = "0";
    await loadEducationHistory();
}

async function onOrganizationMaintenanceSearch(event) {
    event.preventDefault();
    document.getElementById("organization-maintenance-page").value = "0";
    await loadOrganizationMaintenance();
}

async function onDictionarySearch(event) {
    event.preventDefault();
    document.getElementById("dictionary-page").value = "0";
    await loadDictionaries();
}

async function onLocalPolicySearch(event) {
    event.preventDefault();
    document.getElementById("local-policy-page").value = "0";
    await loadLocalPolicies();
}

async function onAudit(event) {
    event.preventDefault();
    await loadAudit();
}

async function onPayrollHistorySearch(event) {
    event.preventDefault();
    document.getElementById("payroll-history-page").value = "0";
    await loadPayrollHistory();
}

async function onTeachingAllowanceSearch(event) {
    event.preventDefault();
    document.getElementById("teaching-allowance-page").value = "0";
    await loadTeachingAllowanceAdjustments();
}

async function onNormalPromotionSearch(event) {
    event.preventDefault();
    document.getElementById("normal-promotion-page").value = "0";
    await loadNormalPromotions();
}

async function onLevelPromotionSearch(event) {
    event.preventDefault();
    document.getElementById("level-promotion-page").value = "0";
    await loadLevelPromotions();
}

async function onPositionChangePromotionSearch(event) {
    event.preventDefault();
    document.getElementById("position-change-page").value = "0";
    await loadPositionChangePromotions();
}

async function onBasicStandardsSearch(event) {
    event.preventDefault();
    document.getElementById("basic-standard-page").value = "0";
    await loadBasicStandards();
}

async function onAllowanceStandardsSearch(event) {
    event.preventDefault();
    document.getElementById("allowance-standard-page").value = "0";
    await loadAllowanceStandards();
}

async function onRankAllowanceStandardsSearch(event) {
    event.preventDefault();
    document.getElementById("rank-standard-page").value = "0";
    await loadRankAllowanceStandards();
}

async function onRetainedAllowanceStandardsSearch(event) {
    event.preventDefault();
    document.getElementById("retained-standard-page").value = "0";
    await loadRetainedAllowanceStandards();
}

async function onYearAllowanceStandardsSearch(event) {
    event.preventDefault();
    document.getElementById("year-standard-page").value = "0";
    await loadYearAllowanceStandards();
}

async function onInternSalaryStandardsSearch(event) {
    event.preventDefault();
    document.getElementById("intern-standard-page").value = "0";
    await loadInternSalaryStandards();
}

async function onWageReformStandardsSearch(event) {
    event.preventDefault();
    document.getElementById("wage-reform-page").value = "0";
    await loadWageReformStandards();
}

async function onOtherAllowanceStandardsSearch(event) {
    event.preventDefault();
    document.getElementById("other-allowance-page").value = "0";
    await loadOtherAllowanceStandards();
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

async function loadAnnualAssessments() {
    const organizationCode = document.getElementById("assessment-organization-code").value.trim();
    const year = document.getElementById("assessment-year").value.trim();
    const keyword = document.getElementById("assessment-keyword").value.trim();
    const page = document.getElementById("assessment-page").value || "0";
    const size = document.getElementById("assessment-size").value || "20";
    const params = new URLSearchParams({ page, size });
    if (organizationCode) {
        params.set("organizationCode", organizationCode);
    }
    if (year) {
        params.set("year", year);
    }
    if (keyword) {
        params.set("keyword", keyword);
    }

    const status = document.getElementById("assessment-status");
    const rows = document.getElementById("assessment-rows");
    status.className = "status";
    status.textContent = "正在查询年度考核结果...";
    rows.innerHTML = "";

    try {
        const result = await getJson(`/api/personnel/assessments?${params}`);
        rows.innerHTML = (result.content || []).map(row => `
            <tr>
                <td>${escapeHtml(row.id)}</td>
                <td>${escapeHtml(row.organizationCode)} ${escapeHtml(row.organizationName || "")}</td>
                <td>${escapeHtml(row.personCode)}</td>
                <td>${escapeHtml(row.name || "")}</td>
                <td>${escapeHtml(row.year)}</td>
                <td>${escapeHtml(row.result)}</td>
            </tr>
        `).join("");
        status.textContent = `第 ${result.page + 1} / ${Math.max(result.totalPages, 1)} 页，共 ${result.totalElements} 条考核记录`;
    } catch (error) {
        showError(status, error);
    }
}

async function loadAssessmentSummary() {
    const organizationCode = document.getElementById("assessment-summary-organization-code").value.trim();
    const year = document.getElementById("assessment-summary-year").value.trim();
    const resultFilter = document.getElementById("assessment-summary-result").value.trim();
    const page = document.getElementById("assessment-summary-page").value || "0";
    const size = document.getElementById("assessment-summary-size").value || "20";
    const params = new URLSearchParams({ page, size });
    if (organizationCode) {
        params.set("organizationCode", organizationCode);
    }
    if (year) {
        params.set("year", year);
    }
    if (resultFilter) {
        params.set("result", resultFilter);
    }

    const status = document.getElementById("assessment-summary-status");
    const rows = document.getElementById("assessment-summary-rows");
    status.className = "status";
    status.textContent = "正在查询年度考核统计...";
    rows.innerHTML = "";

    try {
        const result = await getJson(`/api/personnel/assessment-summary?${params}`);
        rows.innerHTML = (result.content || []).map(row => `
            <tr>
                <td>${escapeHtml(row.year)}</td>
                <td>${escapeHtml(row.organizationCode)} ${escapeHtml(row.organizationName || "")}</td>
                <td>${escapeHtml(row.result)}</td>
                <td>${escapeHtml(row.personnelCount)}</td>
            </tr>
        `).join("");
        status.textContent = `第 ${result.page + 1} / ${Math.max(result.totalPages, 1)} 页，共 ${result.totalElements} 组统计`;
    } catch (error) {
        showError(status, error);
    }
}

async function loadChangedPersonnel() {
    const organizationCode = document.getElementById("changed-personnel-organization-code").value.trim();
    const period = document.getElementById("changed-personnel-period").value.trim();
    const keyword = document.getElementById("changed-personnel-keyword").value.trim();
    const page = document.getElementById("changed-personnel-page").value || "0";
    const size = document.getElementById("changed-personnel-size").value || "20";
    const params = new URLSearchParams({ page, size });
    if (organizationCode) {
        params.set("organizationCode", organizationCode);
    }
    if (period) {
        params.set("period", period);
    }
    if (keyword) {
        params.set("keyword", keyword);
    }

    const status = document.getElementById("changed-personnel-status");
    const rows = document.getElementById("changed-personnel-rows");
    status.className = "status";
    status.textContent = "正在查询变动人员信息...";
    rows.innerHTML = "";

    try {
        const result = await getJson(`/api/personnel/changed?${params}`);
        rows.innerHTML = (result.content || []).map(row => `
            <tr>
                <td>${escapeHtml(row.organizationCode)} ${escapeHtml(row.organizationName || "")}</td>
                <td>${escapeHtml(row.personCode)}</td>
                <td>${escapeHtml(row.name)}</td>
                <td>${escapeHtml(row.idCard || "")}</td>
                <td>${escapeHtml(row.gender || "")}</td>
                <td>${escapeHtml(row.birthYearMonth || "")}</td>
                <td>${escapeHtml(row.personnelCategory || "")}</td>
                <td>${escapeHtml(row.organizationType || "")}</td>
                <td>${escapeHtml(row.changeYear || "")}${escapeHtml(row.changeMonth || "")}</td>
                <td>${escapeHtml(row.changeType || "")}</td>
                <td>${escapeHtml(row.newPositionCode || "")}</td>
                <td>${escapeHtml(row.newPositionName || "")}</td>
                <td>${money(row.newTotalAmount)}</td>
                <td>${escapeHtml(row.salaryStandardYearMonth || "")}</td>
                <td>${escapeHtml(row.allowanceStandardYearMonth || "")}</td>
                <td>${escapeHtml(row.remark || "")}</td>
            </tr>
        `).join("");
        status.textContent = `第 ${result.page + 1} / ${Math.max(result.totalPages, 1)} 页，共 ${result.totalElements} 条变动人员记录`;
    } catch (error) {
        showError(status, error);
    }
}

async function loadPositionHistory() {
    const organizationCode = document.getElementById("position-history-organization-code").value.trim();
    const keyword = document.getElementById("position-history-keyword").value.trim();
    const page = document.getElementById("position-history-page").value || "0";
    const size = document.getElementById("position-history-size").value || "20";
    const params = new URLSearchParams({ page, size });
    if (organizationCode) {
        params.set("organizationCode", organizationCode);
    }
    if (keyword) {
        params.set("keyword", keyword);
    }

    const status = document.getElementById("position-history-status");
    const rows = document.getElementById("position-history-rows");
    status.className = "status";
    status.textContent = "正在查询任职岗位信息...";
    rows.innerHTML = "";

    try {
        const result = await getJson(`/api/personnel/positions?${params}`);
        rows.innerHTML = (result.content || []).map(row => `
            <tr>
                <td>${escapeHtml(row.id)}</td>
                <td>${escapeHtml(row.organizationCode)} ${escapeHtml(row.organizationName || "")}</td>
                <td>${escapeHtml(row.personCode)}</td>
                <td>${escapeHtml(row.name || "")}</td>
                <td>${escapeHtml(row.currentPositionCode)}</td>
                <td>${escapeHtml(row.currentPosition)}</td>
                <td>${escapeHtml(row.positionLevel)}</td>
                <td>${escapeHtml(row.rankCode)}</td>
                <td>${escapeHtml(row.positionCode)}</td>
                <td>${escapeHtml(row.positionName)}</td>
                <td>${escapeHtml(row.positionType)}</td>
                <td>${escapeHtml(row.startYearMonth)}</td>
                <td>${escapeHtml(row.intervalYears)}</td>
                <td>${escapeHtml(row.activeFlag)}</td>
                <td>${escapeHtml(row.calculationStandard)}</td>
            </tr>
        `).join("");
        status.textContent = `第 ${result.page + 1} / ${Math.max(result.totalPages, 1)} 页，共 ${result.totalElements} 条任职记录`;
    } catch (error) {
        showError(status, error);
    }
}

async function loadEducationHistory() {
    const organizationCode = document.getElementById("education-history-organization-code").value.trim();
    const keyword = document.getElementById("education-history-keyword").value.trim();
    const page = document.getElementById("education-history-page").value || "0";
    const size = document.getElementById("education-history-size").value || "20";
    const params = new URLSearchParams({ page, size });
    if (organizationCode) {
        params.set("organizationCode", organizationCode);
    }
    if (keyword) {
        params.set("keyword", keyword);
    }

    const status = document.getElementById("education-history-status");
    const rows = document.getElementById("education-history-rows");
    status.className = "status";
    status.textContent = "正在查询学历信息...";
    rows.innerHTML = "";

    try {
        const result = await getJson(`/api/personnel/education?${params}`);
        rows.innerHTML = (result.content || []).map(row => `
            <tr>
                <td>${escapeHtml(row.id)}</td>
                <td>${escapeHtml(row.organizationCode)} ${escapeHtml(row.organizationName || "")}</td>
                <td>${escapeHtml(row.personCode)}</td>
                <td>${escapeHtml(row.name || "")}</td>
                <td>${escapeHtml(row.educationCode)}</td>
                <td>${escapeHtml(row.educationName)}</td>
                <td>${escapeHtml(row.school)}</td>
                <td>${escapeHtml(row.enrollmentDate)}</td>
                <td>${escapeHtml(row.graduationDate)}</td>
                <td>${escapeHtml(row.studyYears)}</td>
                <td>${escapeHtml(row.educationType)}</td>
                <td>${escapeHtml(row.remark)}</td>
            </tr>
        `).join("");
        status.textContent = `第 ${result.page + 1} / ${Math.max(result.totalPages, 1)} 页，共 ${result.totalElements} 条学历记录`;
    } catch (error) {
        showError(status, error);
    }
}

async function loadOrganizationMaintenance() {
    const keyword = document.getElementById("organization-maintenance-keyword").value.trim();
    const page = document.getElementById("organization-maintenance-page").value || "0";
    const size = document.getElementById("organization-maintenance-size").value || "20";
    const params = new URLSearchParams({ page, size });
    if (keyword) {
        params.set("keyword", keyword);
    }

    const status = document.getElementById("organization-maintenance-status");
    const rows = document.getElementById("organization-maintenance-rows");
    status.className = "status";
    status.textContent = "正在查询单位信息...";
    rows.innerHTML = "";

    try {
        const result = await getJson(`/api/organizations/maintenance?${params}`);
        rows.innerHTML = (result.content || []).map(row => `
            <tr>
                <td>${escapeHtml(row.id)}</td>
                <td>${escapeHtml(row.organizationCode)}</td>
                <td>${escapeHtml(row.name)}</td>
                <td>${escapeHtml(row.shortName)}</td>
                <td>${escapeHtml(row.category)}</td>
                <td>${escapeHtml(row.payrollCategory)}</td>
                <td>${escapeHtml(row.allowanceStandard)}</td>
                <td>${escapeHtml(row.personnelQuota)}</td>
                <td>${escapeHtml(row.establishmentCount)}</td>
                <td>${escapeHtml(row.actualCount)}</td>
                <td>${escapeHtml(row.activePersonnelCount)}</td>
                <td>${escapeHtml(row.performanceAllowanceEnabled)}</td>
                <td>${escapeHtml(row.performanceCategory)}</td>
                <td>${escapeHtml(row.yearAllowanceCategory)}</td>
                <td>${escapeHtml(row.financeSource)}</td>
                <td>${escapeHtml(row.housingFundWithheld)}</td>
                <td>${escapeHtml(row.pensionWithheld)}</td>
            </tr>
        `).join("");
        status.textContent = `第 ${result.page + 1} / ${Math.max(result.totalPages, 1)} 页，共 ${result.totalElements} 个单位`;
    } catch (error) {
        showError(status, error);
    }
}

async function loadDictionaries() {
    const prefix = document.getElementById("dictionary-prefix").value.trim();
    const keyword = document.getElementById("dictionary-keyword").value.trim();
    const page = document.getElementById("dictionary-page").value || "0";
    const size = document.getElementById("dictionary-size").value || "20";
    const params = new URLSearchParams({ page, size });
    if (prefix) {
        params.set("prefix", prefix);
    }
    if (keyword) {
        params.set("keyword", keyword);
    }

    const status = document.getElementById("dictionary-status");
    const rows = document.getElementById("dictionary-rows");
    status.className = "status";
    status.textContent = "正在查询字典...";
    rows.innerHTML = "";

    try {
        const result = await getJson(`/api/dictionaries?${params}`);
        rows.innerHTML = (result.content || []).map(row => `
            <tr>
                <td>${escapeHtml(row.code)}</td>
                <td>${escapeHtml(row.name)}</td>
                <td>${escapeHtml(row.parentCode)}</td>
                <td>${escapeHtml(row.systemFlag)}</td>
                <td>${escapeHtml(row.enabledFlag)}</td>
            </tr>
        `).join("");
        status.textContent = `第 ${result.page + 1} / ${Math.max(result.totalPages, 1)} 页，共 ${result.totalElements} 条字典`;
    } catch (error) {
        showError(status, error);
    }
}

async function loadLocalPolicies() {
    const keyword = document.getElementById("local-policy-keyword").value.trim();
    const page = document.getElementById("local-policy-page").value || "0";
    const size = document.getElementById("local-policy-size").value || "20";
    const params = new URLSearchParams({ page, size });
    if (keyword) {
        params.set("keyword", keyword);
    }

    const status = document.getElementById("local-policy-status");
    const rows = document.getElementById("local-policy-rows");
    const optionRows = document.getElementById("system-option-rows");
    status.className = "status";
    status.textContent = "正在查询本地工资政策...";
    rows.innerHTML = "";
    optionRows.innerHTML = "";

    try {
        const [policies, options] = await Promise.all([
            getJson(`/api/system-config/local-policies?${params}`),
            getJson("/api/system-config/options"),
        ]);
        rows.innerHTML = (policies.content || []).map(row => `
            <tr>
                <td>${escapeHtml(row.id)}</td>
                <td>${escapeHtml(row.organizationCode)}</td>
                <td>${escapeHtml(row.organizationName)}</td>
                <td>${escapeHtml(row.city)}</td>
                <td>${escapeHtml(row.approvedAt)}</td>
                <td>${escapeHtml(row.payrollTitle)}</td>
                <td>${escapeHtml(row.roundingMode)}</td>
                <td>${escapeHtml(row.roundToInteger)}</td>
                <td>${escapeHtml(row.policeAllowanceCaption)}</td>
                <td>${escapeHtml(row.subsidyCaption)}</td>
                <td>${escapeHtml(row.approvalMode)}</td>
                <td>${escapeHtml(row.unitApprovalCategory)}</td>
                <td>${escapeHtml(row.internSalaryMode)}</td>
                <td>${escapeHtml(row.bonusBalanceMode)}</td>
                <td>${escapeHtml(row.floatingSalaryMode)}</td>
                <td>${escapeHtml(row.payGradeRetentionMode)}</td>
                <td>${escapeHtml(row.autoBackup)}</td>
                <td>${escapeHtml(row.checkUpdate)}</td>
            </tr>
        `).join("");
        optionRows.innerHTML = (options || []).map(row => `
            <tr>
                <td>${escapeHtml(row.enterpriseTransferRaise)}</td>
                <td>${escapeHtml(row.gradeStepEducationLink)}</td>
                <td>${escapeHtml(row.decimalPlaces)}</td>
                <td>${escapeHtml(row.policeRankAllowance)}</td>
                <td>${escapeHtml(row.reformBonusBalance)}</td>
                <td>${escapeHtml(row.floatingSalary)}</td>
            </tr>
        `).join("");
        status.textContent = `第 ${policies.page + 1} / ${Math.max(policies.totalPages, 1)} 页，共 ${policies.totalElements} 条政策配置`;
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

async function loadPayrollHistory() {
    const organizationCode = document.getElementById("payroll-history-organization-code").value.trim();
    const period = document.getElementById("payroll-history-period").value.trim();
    const keyword = document.getElementById("payroll-history-keyword").value.trim();
    const page = document.getElementById("payroll-history-page").value || "0";
    const size = document.getElementById("payroll-history-size").value || "20";
    const params = new URLSearchParams({ page, size });
    if (organizationCode) {
        params.set("organizationCode", organizationCode);
    }
    if (period) {
        params.set("period", period);
    }
    if (keyword) {
        params.set("keyword", keyword);
    }

    const status = document.getElementById("payroll-history-status");
    const rows = document.getElementById("payroll-history-rows");
    status.className = "status";
    status.textContent = "正在查询工资变动历史...";
    rows.innerHTML = "";

    try {
        const result = await getJson(`/api/payroll/histories?${params}`);
        rows.innerHTML = (result.content || []).map(row => `
            <tr>
                <td>${escapeHtml(row.id)}</td>
                <td>${row.currentPayroll ? "是" : "否"}</td>
                <td>${escapeHtml(row.successorId || "")}</td>
                <td>${escapeHtml(row.organizationCode)} ${escapeHtml(row.organizationName || "")}</td>
                <td>${escapeHtml(row.personCode)}</td>
                <td>${escapeHtml(row.name)}</td>
                <td>${escapeHtml(row.calculationYear)}${escapeHtml(row.calculationMonth)}</td>
                <td>${escapeHtml(row.changeType)}</td>
                <td>${escapeHtml(row.positionCode)}</td>
                <td>${escapeHtml(row.positionName)}</td>
                <td>${money(row.positionSalary)}</td>
                <td>${money(row.gradeSalary)}</td>
                <td>${money(row.technicalGradeSalary)}</td>
                <td>${money(row.performanceAllowance)}</td>
                <td>${money(row.retainedAllowance)}</td>
                <td>${money(row.rankAllowance)}</td>
                <td>${money(row.floatingSalary)}</td>
                <td>${money(row.bonusBalance)}</td>
                <td>${money(row.teachingAllowance)}</td>
                <td>${money(row.salaryIncrease)}</td>
                <td>${money(row.yearAllowance)}</td>
                <td>${money(row.payGradeRetention)}</td>
                <td>${money(row.totalAmount)}</td>
            </tr>
        `).join("");
        status.textContent = `第 ${result.page + 1} / ${Math.max(result.totalPages, 1)} 页，共 ${result.totalElements} 条工资历史`;
    } catch (error) {
        showError(status, error);
    }
}

async function loadTeachingAllowanceAdjustments() {
    const organizationCode = document.getElementById("teaching-allowance-organization-code").value.trim();
    const keyword = document.getElementById("teaching-allowance-keyword").value.trim();
    const page = document.getElementById("teaching-allowance-page").value || "0";
    const size = document.getElementById("teaching-allowance-size").value || "20";
    const params = new URLSearchParams({ page, size });
    if (organizationCode) {
        params.set("organizationCode", organizationCode);
    }
    if (keyword) {
        params.set("keyword", keyword);
    }

    const status = document.getElementById("teaching-allowance-status");
    const rows = document.getElementById("teaching-allowance-rows");
    status.className = "status";
    status.textContent = "正在查询教护龄津贴调整试算...";
    rows.innerHTML = "";

    try {
        const result = await getJson(`/api/payroll/teaching-allowance-adjustments?${params}`);
        rows.innerHTML = (result.content || []).map(row => `
            <tr>
                <td>${escapeHtml(row.organizationCode)} ${escapeHtml(row.organizationName || "")}</td>
                <td>${escapeHtml(row.personCode)}</td>
                <td>${escapeHtml(row.name)}</td>
                <td>${escapeHtml(row.calculationPeriod)}</td>
                <td>${escapeHtml(row.positionCode)}</td>
                <td>${escapeHtml(row.positionName)}</td>
                <td>${escapeHtml(row.teachingStartYearMonth)}</td>
                <td>${escapeHtml(row.interruptedYears)}</td>
                <td>${escapeHtml(row.teachingYears)}</td>
                <td>${money(row.storedAmount)}</td>
                <td>${money(row.calculatedAmount)}</td>
                <td class="${Number(row.differenceAmount) === 0 ? "difference-ok" : "difference-bad"}">${money(row.differenceAmount)}</td>
                <td>${row.eligible ? "是" : "否"}</td>
            </tr>
        `).join("");
        status.textContent = `第 ${result.page + 1} / ${Math.max(result.totalPages, 1)} 页，共 ${result.totalElements} 条试算记录`;
    } catch (error) {
        showError(status, error);
    }
}

async function loadNormalPromotions() {
    const organizationCode = document.getElementById("normal-promotion-organization-code").value.trim();
    const keyword = document.getElementById("normal-promotion-keyword").value.trim();
    const page = document.getElementById("normal-promotion-page").value || "0";
    const size = document.getElementById("normal-promotion-size").value || "20";
    const params = new URLSearchParams({ page, size });
    if (organizationCode) {
        params.set("organizationCode", organizationCode);
    }
    if (keyword) {
        params.set("keyword", keyword);
    }

    const status = document.getElementById("normal-promotion-status");
    const rows = document.getElementById("normal-promotion-rows");
    status.className = "status";
    status.textContent = "正在查询正常档次/薪级晋升试算...";
    rows.innerHTML = "";

    try {
        const result = await getJson(`/api/payroll/normal-promotions?${params}`);
        rows.innerHTML = (result.content || []).map(row => `
            <tr>
                <td>${escapeHtml(row.organizationCode)}</td>
                <td>${escapeHtml(row.personCode)}</td>
                <td>${escapeHtml(row.name)}</td>
                <td>${escapeHtml(row.calculationPeriod)}</td>
                <td>${escapeHtml(row.changeType)}</td>
                <td>${escapeHtml(row.positionCode)}</td>
                <td>${escapeHtml(row.positionName)}</td>
                <td>${escapeHtml(row.salaryStandardYearMonth)}</td>
                <td>${escapeHtml(row.currentGradeOrLevel)}</td>
                <td>${escapeHtml(row.promotedGradeOrLevel)}</td>
                <td>${escapeHtml(row.gradeSalaryLevel || "")}</td>
                <td>${escapeHtml(row.gradeSalaryStep || "")}</td>
                <td>${money(row.currentBaseSalary)}</td>
                <td>${money(row.promotedBaseSalary)}</td>
                <td>${money(row.increaseAmount)}</td>
                <td>${escapeHtml(baseSalarySourceName(row.baseSalarySource))}</td>
            </tr>
        `).join("");
        status.textContent = `第 ${result.page + 1} / ${Math.max(result.totalPages, 1)} 页，共 ${result.totalElements} 条试算记录`;
    } catch (error) {
        showError(status, error);
    }
}

async function loadLevelPromotions() {
    const organizationCode = document.getElementById("level-promotion-organization-code").value.trim();
    const keyword = document.getElementById("level-promotion-keyword").value.trim();
    const dueOnly = document.getElementById("level-promotion-due-only").checked;
    const page = document.getElementById("level-promotion-page").value || "0";
    const size = document.getElementById("level-promotion-size").value || "20";
    const params = new URLSearchParams({ page, size, dueOnly });
    if (organizationCode) {
        params.set("organizationCode", organizationCode);
    }
    if (keyword) {
        params.set("keyword", keyword);
    }

    const status = document.getElementById("level-promotion-status");
    const rows = document.getElementById("level-promotion-rows");
    status.className = "status";
    status.textContent = "正在查询级别晋升试算...";
    rows.innerHTML = "";

    try {
        const result = await getJson(`/api/payroll/level-promotions?${params}`);
        rows.innerHTML = (result.content || []).map(row => `
            <tr>
                <td>${escapeHtml(row.organizationCode)}</td>
                <td>${escapeHtml(row.personCode)}</td>
                <td>${escapeHtml(row.name)}</td>
                <td>${escapeHtml(row.calculationPeriod)}</td>
                <td>${escapeHtml(row.changeType)}</td>
                <td>${escapeHtml(row.positionCode)}</td>
                <td>${escapeHtml(row.positionName)}</td>
                <td>${escapeHtml(row.salaryStandardYearMonth)}</td>
                <td>${escapeHtml(row.currentLevel || "")}</td>
                <td>${escapeHtml(row.currentStep || "")}</td>
                <td>${escapeHtml(row.promotedLevel || "")}</td>
                <td>${escapeHtml(row.promotedStep || "")}</td>
                <td>${escapeHtml(row.levelAssessmentStartYear || "")}</td>
                <td>${escapeHtml(row.stepAssessmentStartYear || "")}</td>
                <td>${escapeHtml(row.nextLevelAssessmentStartYear || "")}</td>
                <td>${escapeHtml(row.nextStepAssessmentStartYear || "")}</td>
                <td>${escapeHtml(row.qualifiedYearsForLevel)}</td>
                <td>${escapeHtml(row.qualifiedYearsForStep)}</td>
                <td>${row.levelPromotionDue ? "是" : "否"}</td>
                <td>${row.stepPromotionDue ? "是" : "否"}</td>
                <td>${row.gradeIncreaseExceedsStepDifference ? "是" : "否"}</td>
                <td>${money(row.currentGradeSalary)}</td>
                <td>${money(row.promotedGradeSalary)}</td>
                <td>${money(row.increaseAmount)}</td>
                <td>${row.eligible ? "是" : "否"}</td>
                <td>${escapeHtml(row.note || "")}</td>
            </tr>
        `).join("");
        status.textContent = `第 ${result.page + 1} / ${Math.max(result.totalPages, 1)} 页，共 ${result.totalElements} 条试算记录`;
    } catch (error) {
        showError(status, error);
    }
}

async function loadPositionChangePromotions() {
    const organizationCode = document.getElementById("position-change-organization-code").value.trim();
    const keyword = document.getElementById("position-change-keyword").value.trim();
    const page = document.getElementById("position-change-page").value || "0";
    const size = document.getElementById("position-change-size").value || "20";
    const params = new URLSearchParams({ page, size });
    if (organizationCode) {
        params.set("organizationCode", organizationCode);
    }
    if (keyword) {
        params.set("keyword", keyword);
    }

    const status = document.getElementById("position-change-status");
    const rows = document.getElementById("position-change-rows");
    status.className = "status";
    status.textContent = "正在查询职务变化晋升试算...";
    rows.innerHTML = "";

    try {
        const result = await getJson(`/api/payroll/position-change-promotions?${params}`);
        rows.innerHTML = (result.content || []).map(row => `
            <tr>
                <td>${escapeHtml(row.organizationCode)}</td>
                <td>${escapeHtml(row.personCode)}</td>
                <td>${escapeHtml(row.name)}</td>
                <td>${escapeHtml(row.currentPositionCode || "")}</td>
                <td>${escapeHtml(row.currentPositionName || "")}</td>
                <td>${escapeHtml(row.newPositionCode || "")}</td>
                <td>${escapeHtml(row.newPositionName || "")}</td>
                <td>${escapeHtml(row.positionStartYearMonth || "")}</td>
                <td>${escapeHtml(row.effectivePeriod || "")}</td>
                <td>${escapeHtml(row.currentLevel || "")}</td>
                <td>${escapeHtml(row.currentStep || "")}</td>
                <td>${escapeHtml(row.newPositionMinimumLevel || "")}</td>
                <td>${escapeHtml(row.newPositionMaximumLevel || "")}</td>
                <td>${escapeHtml(row.promotedLevel || "")}</td>
                <td>${escapeHtml(row.promotedStep || "")}</td>
                <td>${money(row.currentPositionSalary)}</td>
                <td>${money(row.newPositionSalary)}</td>
                <td>${money(row.currentGradeSalary)}</td>
                <td>${money(row.promotedGradeSalary)}</td>
                <td>${money(row.positionSalaryIncrease)}</td>
                <td>${money(row.gradeSalaryIncrease)}</td>
                <td>${money(row.totalIncrease)}</td>
                <td>${escapeHtml(row.nextLevelAssessmentStartYear || "")}</td>
                <td>${escapeHtml(row.nextStepAssessmentStartYear || "")}</td>
                <td>${row.gradeIncreaseExceedsStepDifference ? "是" : "否"}</td>
                <td>${row.eligible ? "是" : "否"}</td>
                <td>${escapeHtml(row.note || "")}</td>
            </tr>
        `).join("");
        status.textContent = `第 ${result.page + 1} / ${Math.max(result.totalPages, 1)} 页，共 ${result.totalElements} 条试算记录`;
    } catch (error) {
        showError(status, error);
    }
}

function baseSalarySourceName(source) {
    return {
        GRADE: "级别工资",
        SALARY_LEVEL: "薪级工资",
        TECHNICAL_GRADE: "技术等级工资",
    }[source] || source || "";
}

async function loadBasicStandards() {
    const standardType = document.getElementById("basic-standard-type").value;
    const standardYearMonth = document.getElementById("basic-standard-year-month").value.trim();
    const code = document.getElementById("basic-standard-code").value.trim();
    const page = document.getElementById("basic-standard-page").value || "0";
    const size = document.getElementById("basic-standard-size").value || "20";
    const params = new URLSearchParams({ standardType, page, size });
    if (standardYearMonth) {
        params.set("standardYearMonth", standardYearMonth);
    }
    if (code) {
        params.set("code", code);
    }
    const status = document.getElementById("basic-standards-status");
    status.className = "status";
    status.textContent = "正在查询工资标准...";
    document.getElementById("basic-standards-head").innerHTML = "";
    document.getElementById("basic-standards-rows").innerHTML = "";
    try {
        const result = await getJson(`/api/payroll/basic-standards?${params}`);
        renderBasicStandards(result.content || []);
        status.textContent = `第 ${result.page + 1} / ${Math.max(result.totalPages, 1)} 页，共 ${result.totalElements} 条`;
    } catch (error) {
        showError(status, error);
    }
}

function renderBasicStandards(records) {
    const head = document.getElementById("basic-standards-head");
    const body = document.getElementById("basic-standards-rows");
    if (!records.length) {
        head.innerHTML = "<tr><th>结果</th></tr>";
        body.innerHTML = "<tr><td>没有查询到标准数据</td></tr>";
        return;
    }
    const columns = Object.keys(records[0].values);
    head.innerHTML = `<tr>${columns.map(column => `<th>${escapeHtml(column)}</th>`).join("")}</tr>`;
    body.innerHTML = records.map(record => `
        <tr>${columns.map(column => `<td>${escapeHtml(record.values[column] ?? "")}</td>`).join("")}</tr>
    `).join("");
}

async function loadAllowanceStandards() {
    const standardYearMonth = document.getElementById("allowance-standard-year-month").value.trim();
    const item = document.getElementById("allowance-standard-item").value.trim();
    const positionCode = document.getElementById("allowance-standard-position-code").value.trim();
    const page = document.getElementById("allowance-standard-page").value || "0";
    const size = document.getElementById("allowance-standard-size").value || "20";
    const params = new URLSearchParams({ page, size });
    if (standardYearMonth) {
        params.set("standardYearMonth", standardYearMonth);
    }
    if (item) {
        params.set("item", item);
    }
    if (positionCode) {
        params.set("positionCode", positionCode);
    }
    const status = document.getElementById("allowance-standards-status");
    const rows = document.getElementById("allowance-standards-rows");
    status.className = "status";
    status.textContent = "正在查询津贴补贴标准...";
    rows.innerHTML = "";
    try {
        const result = await getJson(`/api/payroll/allowance-standards?${params}`);
        rows.innerHTML = (result.content || []).map(row => `
            <tr>
                <td>${escapeHtml(row.id)}</td>
                <td>${escapeHtml(row.standardYearMonth)}</td>
                <td>${escapeHtml(row.item)}</td>
                <td>${escapeHtml(row.positionCode)}</td>
                <td>${escapeHtml(row.name)}</td>
                <td>${escapeHtml(row.workYearsLower)}</td>
                <td>${escapeHtml(row.workYearsUpper)}</td>
                <td>${money(row.amount)}</td>
                <td>${escapeHtml(row.performanceCategory)}</td>
            </tr>
        `).join("");
        status.textContent = `第 ${result.page + 1} / ${Math.max(result.totalPages, 1)} 页，共 ${result.totalElements} 条`;
    } catch (error) {
        showError(status, error);
    }
}

async function loadRankAllowanceStandards() {
    const standardYearMonth = document.getElementById("rank-standard-year-month").value.trim();
    const rankName = document.getElementById("rank-standard-name").value.trim();
    const category = document.getElementById("rank-standard-category").value.trim();
    const page = document.getElementById("rank-standard-page").value || "0";
    const size = document.getElementById("rank-standard-size").value || "20";
    const params = new URLSearchParams({ page, size });
    if (standardYearMonth) {
        params.set("standardYearMonth", standardYearMonth);
    }
    if (rankName) {
        params.set("rankName", rankName);
    }
    if (category) {
        params.set("category", category);
    }
    const status = document.getElementById("rank-standards-status");
    const rows = document.getElementById("rank-standards-rows");
    status.className = "status";
    status.textContent = "正在查询警衔津贴标准...";
    rows.innerHTML = "";
    try {
        const result = await getJson(`/api/payroll/rank-allowance-standards?${params}`);
        rows.innerHTML = (result.content || []).map(row => `
            <tr>
                <td>${escapeHtml(row.id)}</td>
                <td>${escapeHtml(row.standardYearMonth)}</td>
                <td>${escapeHtml(row.rankCode)}</td>
                <td>${escapeHtml(row.rankName)}</td>
                <td>${money(row.amount)}</td>
                <td>${escapeHtml(row.category)}</td>
            </tr>
        `).join("");
        status.textContent = `第 ${result.page + 1} / ${Math.max(result.totalPages, 1)} 页，共 ${result.totalElements} 条`;
    } catch (error) {
        showError(status, error);
    }
}

async function loadRetainedAllowanceStandards() {
    const keyword = document.getElementById("retained-standard-keyword").value.trim();
    const page = document.getElementById("retained-standard-page").value || "0";
    const size = document.getElementById("retained-standard-size").value || "20";
    const params = new URLSearchParams({ page, size });
    if (keyword) {
        params.set("keyword", keyword);
    }
    const status = document.getElementById("retained-standards-status");
    const rows = document.getElementById("retained-standards-rows");
    status.className = "status";
    status.textContent = "正在查询保留福补标准...";
    rows.innerHTML = "";
    try {
        const result = await getJson(`/api/payroll/retained-allowance-standards?${params}`);
        rows.innerHTML = (result.content || []).map(row => `
            <tr>
                <td>${escapeHtml(row.positionCode)}</td>
                <td>${escapeHtml(row.name)}</td>
                <td>${money(row.amount)}</td>
            </tr>
        `).join("");
        status.textContent = `第 ${result.page + 1} / ${Math.max(result.totalPages, 1)} 页，共 ${result.totalElements} 条`;
    } catch (error) {
        showError(status, error);
    }
}

async function loadYearAllowanceStandards() {
    const standardYearMonth = document.getElementById("year-standard-year-month").value.trim();
    const page = document.getElementById("year-standard-page").value || "0";
    const size = document.getElementById("year-standard-size").value || "20";
    const params = new URLSearchParams({ page, size });
    if (standardYearMonth) {
        params.set("standardYearMonth", standardYearMonth);
    }
    const status = document.getElementById("year-standards-status");
    const rows = document.getElementById("year-standards-rows");
    status.className = "status";
    status.textContent = "正在查询年补贴标准...";
    rows.innerHTML = "";
    try {
        const result = await getJson(`/api/payroll/year-allowance-standards?${params}`);
        rows.innerHTML = (result.content || []).map(row => `
            <tr>
                <td>${escapeHtml(row.standardYearMonth)}</td>
                <td>${money(row.categoryOneAmount)}</td>
                <td>${money(row.categoryTwoAmount)}</td>
                <td>${money(row.categoryThreeAmount)}</td>
                <td>${money(row.categoryFourAmount)}</td>
            </tr>
        `).join("");
        status.textContent = `第 ${result.page + 1} / ${Math.max(result.totalPages, 1)} 页，共 ${result.totalElements} 条`;
    } catch (error) {
        showError(status, error);
    }
}

async function loadInternSalaryStandards() {
    const standardYearMonth = document.getElementById("intern-standard-year-month").value.trim();
    const keyword = document.getElementById("intern-standard-keyword").value.trim();
    const page = document.getElementById("intern-standard-page").value || "0";
    const size = document.getElementById("intern-standard-size").value || "20";
    const params = new URLSearchParams({ page, size });
    if (standardYearMonth) {
        params.set("standardYearMonth", standardYearMonth);
    }
    if (keyword) {
        params.set("keyword", keyword);
    }
    const status = document.getElementById("intern-standards-status");
    const rows = document.getElementById("intern-standards-rows");
    status.className = "status";
    status.textContent = "正在查询见习工资标准...";
    rows.innerHTML = "";
    try {
        const result = await getJson(`/api/payroll/intern-salary-standards?${params}`);
        rows.innerHTML = (result.content || []).map(row => `
            <tr>
                <td>${escapeHtml(row.standardYearMonth)}</td>
                <td>${escapeHtml(row.educationCode)}</td>
                <td>${escapeHtml(row.educationName)}</td>
                <td>${escapeHtml(row.regularPositionCode)}</td>
                <td>${escapeHtml(row.regularPositionName)}</td>
                <td>${escapeHtml(row.regularGradeStep)}</td>
                <td>${escapeHtml(row.regularLevel)}</td>
                <td>${money(row.firstYearAmount)}</td>
                <td>${money(row.secondYearAmount)}</td>
            </tr>
        `).join("");
        status.textContent = `第 ${result.page + 1} / ${Math.max(result.totalPages, 1)} 页，共 ${result.totalElements} 条`;
    } catch (error) {
        showError(status, error);
    }
}

async function loadWageReformStandards() {
    const positionCode = document.getElementById("wage-reform-position-code").value.trim();
    const page = document.getElementById("wage-reform-page").value || "0";
    const size = document.getElementById("wage-reform-size").value || "20";
    const params = new URLSearchParams({ page, size });
    if (positionCode) {
        params.set("positionCode", positionCode);
    }
    const status = document.getElementById("wage-reform-standards-status");
    const rows = document.getElementById("wage-reform-standards-rows");
    status.className = "status";
    status.textContent = "正在查询2006套改标准...";
    rows.innerHTML = "";
    try {
        const result = await getJson(`/api/payroll/wage-reform-standards?${params}`);
        rows.innerHTML = (result.content || []).map(row => `
            <tr>
                <td>${escapeHtml(row.positionCode)}</td>
                <td>${escapeHtml(row.appointmentYearsLower)}</td>
                <td>${escapeHtml(row.appointmentYearsUpper)}</td>
                <td>${escapeHtml(row.reformYearsLower)}</td>
                <td>${escapeHtml(row.reformYearsUpper)}</td>
                <td>${escapeHtml(row.convertedLevel)}</td>
                <td>${escapeHtml(row.convertedStep)}</td>
            </tr>
        `).join("");
        status.textContent = `第 ${result.page + 1} / ${Math.max(result.totalPages, 1)} 页，共 ${result.totalElements} 条`;
    } catch (error) {
        showError(status, error);
    }
}

async function loadOtherAllowanceStandards() {
    const standardType = document.getElementById("other-allowance-standard-type").value;
    const standardYearMonth = document.getElementById("other-allowance-standard-year-month").value.trim();
    const code = document.getElementById("other-allowance-code").value.trim();
    const page = document.getElementById("other-allowance-page").value || "0";
    const size = document.getElementById("other-allowance-size").value || "20";
    const params = new URLSearchParams({ standardType, page, size });
    if (standardYearMonth) {
        params.set("standardYearMonth", standardYearMonth);
    }
    if (code) {
        params.set("code", code);
    }
    const status = document.getElementById("other-allowance-status");
    const rows = document.getElementById("other-allowance-rows");
    status.className = "status";
    status.textContent = "正在查询其他补贴标准...";
    rows.innerHTML = "";
    try {
        const result = await getJson(`/api/payroll/other-allowance-standards?${params}`);
        rows.innerHTML = (result.content || []).map(row => `
            <tr>
                <td>${escapeHtml(otherAllowanceTypeName(row.standardType))}</td>
                <td>${escapeHtml(row.standardYearMonth || "")}</td>
                <td>${escapeHtml(row.code || "")}</td>
                <td>${escapeHtml(row.name || "")}</td>
                <td>${money(row.amount)}</td>
                <td>${money(row.averageAmount)}</td>
                <td>${escapeHtml(row.multiplier ?? "")}</td>
            </tr>
        `).join("");
        status.textContent = `第 ${result.page + 1} / ${Math.max(result.totalPages, 1)} 页，共 ${result.totalElements} 条`;
    } catch (error) {
        showError(status, error);
    }
}

function otherAllowanceTypeName(type) {
    return {
        property: "物业补贴",
        communication: "通信补贴",
        civilized: "文明奖",
        assessment: "平时考核奖",
    }[type] || type || "";
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
        const menuParams = new URLSearchParams({
            keyword: document.getElementById("security-menu-filter").value.trim(),
            page: document.getElementById("security-menu-page").value || "0",
            size: pageSize,
        });
        const [users, roles, permissions, organizations, menus, auditLogs] = await Promise.all([
            getJson(`/api/security/users-page?${userParams}`),
            getJson(`/api/security/roles-page?${roleParams}`),
            getJson("/api/security/permissions"),
            getJson("/api/organizations?size=200"),
            getJson(`/api/security/menus-page?${menuParams}`),
            getJson(`/api/security/audit-logs-page?${auditParams}`),
        ]);
        state.security = {
            users: users.content || [],
            userPage: users,
            roles: roles.content || [],
            rolePage: roles,
            permissions,
            organizations: organizations.content || [],
            menus: menus.content || [],
            menuPage: menus,
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
    const menus = state.security.menus || [];
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

    document.getElementById("security-menu-rows").innerHTML = menus.map(menu => `
        <tr>
            <td>${escapeHtml(menu.id)}</td>
            <td>${escapeHtml(menu.code)}</td>
            <td><input class="inline-input" id="menu-title-${menu.id}" value="${escapeHtml(menu.title)}"></td>
            <td><input class="inline-input" id="menu-path-${menu.id}" value="${escapeHtml(menu.path)}"></td>
            <td><input class="inline-input" id="menu-permission-${menu.id}" value="${escapeHtml(menu.permissionCode)}"></td>
            <td><input class="inline-number" id="menu-sort-${menu.id}" type="number" value="${escapeHtml(menu.sortOrder)}"></td>
            <td><input id="menu-enabled-${menu.id}" type="checkbox" ${menu.enabled ? "checked" : ""}></td>
            <td><button class="row-action" data-menu-save="${menu.id}">保存菜单</button></td>
        </tr>
    `).join("");
    renderPageInfo("security-menu-page-info", state.security.menuPage);

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
    document.querySelectorAll("[data-menu-save]").forEach(button => {
        button.addEventListener("click", async () => {
            const id = button.dataset.menuSave;
            await putJson(`/api/security/menus/${id}`, {
                title: document.getElementById(`menu-title-${id}`).value.trim(),
                path: document.getElementById(`menu-path-${id}`).value.trim(),
                permissionCode: document.getElementById(`menu-permission-${id}`).value.trim(),
                sortOrder: Number(document.getElementById(`menu-sort-${id}`).value || 0),
                enabled: document.getElementById(`menu-enabled-${id}`).checked,
            });
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
        document.getElementById("security-menu-page").value = "0";
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
