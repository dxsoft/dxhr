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
    dictionaryFieldConfigs: {},
    activeDictionaryTarget: null,
    activeDictionaryNodes: [],
    dictionaryExpandedCodes: new Set(),
    organizationNodes: [],
    organizationExpandedCodes: new Set(),
    activeOrganizationTarget: "maintenance",
    pendingPersonnelChange: null,
    activePersonnelMaintenance: null,
    activeSubrecordEditor: null,
};

const personnelChangeTypes = [
    { type: "退休", description: "退休" },
    { type: "调动", description: "调往本地其他单位" },
    { type: "调出", description: "调往外地" },
    { type: "辞职", description: "辞职" },
    { type: "辞退", description: "辞退" },
    { type: "开除", description: "开除" },
    { type: "死亡", description: "死亡" },
];

const subrecordEditors = {
    education: {
        title: "学历信息",
        endpoint: uid => `/api/personnel/${uid}/education`,
        fields: [
            ["educationCode", "学历编码", "text", { readonly: true }],
            ["educationName", "学历", "text", { dictionaryPrefixField: "zgxl", dictionaryPrefix: "002", linkedCodeField: "educationCode" }],
            ["school", "学校"],
            ["enrollmentDate", "入学时间", "month"], ["graduationDate", "毕业时间", "month"],
            ["studyYears", "学制", "number"], ["educationType", "学历类别"], ["remark", "备注"],
        ],
    },
    position: {
        title: "职务变化信息",
        endpoint: uid => `/api/personnel/${uid}/positions`,
        fields: [
            ["currentPositionCode", "任职编码"], ["currentPosition", "任职职务"], ["positionLevel", "职务级别"],
            ["rankCode", "职级编码"], ["positionCode", "岗位编码"], ["positionName", "岗位名称"],
            ["positionType", "岗位类型"], ["startYearMonth", "任职年月", "month"], ["intervalYears", "间隔年限", "number"],
            ["activeFlag", "现任标志"], ["calculationStandard", "计算标准"],
        ],
    },
    assessment: {
        title: "年度考核信息",
        endpoint: uid => `/api/personnel/${uid}/assessments`,
        fields: [["year", "年度"], ["result", "考核结果"]],
    },
    payroll: {
        title: "历次调资信息",
        endpoint: uid => `/api/payroll/personnel/${uid}/histories`,
        updateEndpoint: id => `/api/payroll/histories/${id}`,
        fields: [
            ["calculationYear", "年度"], ["calculationMonth", "月份"], ["changeType", "变动类别"],
            ["positionCode", "岗位编码"], ["positionName", "岗位名称"], ["positionSalary", "职务工资", "number"],
            ["gradeSalary", "级别/薪级工资", "number"], ["technicalGradeSalary", "技术等级工资", "number"],
            ["performanceAllowance", "绩效/生活补贴", "number"], ["retainedAllowance", "保留福补", "number"],
            ["totalAmount", "合计", "number"],
        ],
    },
};

const yuanFormatter = new Intl.NumberFormat("zh-CN", {
    minimumFractionDigits: 0,
    maximumFractionDigits: 2,
});

const menuGroups = [
    { title: "工作台", codes: ["DASHBOARD"] },
    { title: "信息维护", codes: ["PERSONNEL", "PERSONNEL_MAINTENANCE", "ANNUAL_ASSESSMENTS", "CHANGED_PERSONNEL", "ASSESSMENT_SUMMARY", "POSITION_HISTORY", "EDUCATION_HISTORY", "ORGANIZATION_MAINTENANCE"] },
    { title: "工资变动", codes: ["PAYROLL", "PAYROLL_HISTORY", "TEACHING_ALLOWANCE_ADJUSTMENT", "NORMAL_PROMOTION", "LEVEL_PROMOTION", "POSITION_CHANGE_PROMOTION", "EDUCATION_PROMOTION", "REGULARIZATION", "AUDIT"] },
    { title: "标准维护", codes: ["BASIC_STANDARDS", "ALLOWANCE_STANDARDS", "INTERN_SALARY_STANDARDS", "RANK_ALLOWANCE_STANDARDS", "RETAINED_ALLOWANCE_STANDARDS", "YEAR_ALLOWANCE_STANDARDS", "WAGE_REFORM_STANDARDS", "OTHER_ALLOWANCE_STANDARDS"] },
    { title: "系统管理", codes: ["LOCAL_POLICY_CONFIG", "DICTIONARY_MAINTENANCE", "SECURITY"] },
];

document.addEventListener("DOMContentLoaded", () => {
    document.getElementById("personnel-search").addEventListener("submit", onPersonnelSearch);
    document.getElementById("personnel-maintenance-form").addEventListener("submit", onPersonnelMaintenanceSave);
    document.getElementById("personnel-maintenance-search").addEventListener("submit", onPersonnelMaintenanceSearch);
    document.getElementById("personnel-maintenance-reset").addEventListener("click", resetPersonnelMaintenanceForm);
    document.getElementById("personnel-maintenance-new").addEventListener("click", openNewPersonnelMaintenance);
    document.getElementById("personnel-maintenance-close").addEventListener("click", closePersonnelMaintenanceModal);
    document.getElementById("subrecord-editor-close").addEventListener("click", closeSubrecordEditor);
    document.getElementById("subrecord-editor-form").addEventListener("submit", onSubrecordSave);
    document.getElementById("add-education-record").addEventListener("click", () => openSubrecordEditor("education"));
    document.getElementById("add-position-record").addEventListener("click", () => openSubrecordEditor("position"));
    document.getElementById("add-payroll-record").addEventListener("click", () => openSubrecordEditor("payroll"));
    document.getElementById("add-assessment-record").addEventListener("click", () => openSubrecordEditor("assessment"));
    document.getElementById("dictionary-picker-close").addEventListener("click", closeDictionaryPicker);
    document.getElementById("organization-picker-close").addEventListener("click", closeOrganizationPicker);
    document.getElementById("organization-picker-filter").addEventListener("input", renderOrganizationPickerTree);
    initializeOrganizationPickerInput();
    document.querySelectorAll("[data-personnel-tab]").forEach(button => {
        button.addEventListener("click", () => showPersonnelTab(button.dataset.personnelTab));
    });
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
    document.getElementById("payroll-change-close").addEventListener("click", closePayrollChangeModal);
    document.getElementById("teaching-allowance-form").addEventListener("submit", onTeachingAllowanceSearch);
    document.getElementById("normal-promotion-form").addEventListener("submit", onNormalPromotionSearch);
    document.getElementById("normal-promotion-due-only").addEventListener("change", loadNormalPromotions);
    document.getElementById("normal-promotion-batch-apply").addEventListener("click", applySelectedNormalPromotions);
    document.getElementById("normal-promotion-select-all").addEventListener("change", event => {
        document.querySelectorAll("[data-normal-select]").forEach(checkbox => {
            checkbox.checked = event.target.checked;
        });
    });
    document.getElementById("level-promotion-form").addEventListener("submit", onLevelPromotionSearch);
    document.getElementById("position-change-promotion-form").addEventListener("submit", onPositionChangePromotionSearch);
    document.getElementById("education-promotion-form").addEventListener("submit", onEducationPromotionSearch);
    document.getElementById("regularization-form").addEventListener("submit", onRegularizationSearch);
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
    document.addEventListener("click", event => {
        if (!event.target.closest(".personnel-change-menu") && !event.target.closest("[data-maint-change]")) {
            closePersonnelChangeMenu();
        }
    });
    document.getElementById("logout-button").addEventListener("click", () => {
        window.location.href = "/logout";
    });
    window.addEventListener("hashchange", applyRoute);
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
        await initializeDictionaryPickers();
        renderDashboard();
        applyRoute();
        if (hasMenu("SECURITY")) {
            await loadSecurityAdmin();
        }
        if (hasMenu("PERSONNEL")) {
            await loadPersonnel();
        }
        if (hasMenu("PERSONNEL_MAINTENANCE")) {
            await loadPersonnelMaintenance();
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
        if (hasMenu("EDUCATION_PROMOTION")) {
            await loadEducationPromotions();
        }
        if (hasMenu("REGULARIZATION")) {
            await loadRegularizations();
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
    const menuByCode = new Map([{ code: "DASHBOARD", title: "工作台", path: "#dashboard", permissionCode: "" }, ...state.menus].map(menu => [menu.code, menu]));
    nav.innerHTML = menuGroups.map(group => {
        const links = group.codes
            .map(code => menuByCode.get(code))
            .filter(Boolean)
            .map(menu => `
                <a href="${escapeHtml(menu.path)}" data-menu-link="${escapeHtml(menu.code)}">${escapeHtml(menu.title)}</a>
            `).join("");
        if (!links) {
            return "";
        }
        return `
            <div class="nav-group">
                <div class="nav-group-title">${escapeHtml(group.title)}</div>
                ${links}
            </div>
        `;
    }).join("");
    document.querySelectorAll("[data-menu-code]").forEach(section => {
        section.classList.toggle("unavailable", !hasMenu(section.dataset.menuCode));
    });
}

function hasMenu(code) {
    if (code === "DASHBOARD") {
        return true;
    }
    return state.menus.some(menu => menu.code === code);
}

function applyRoute() {
    const availableMenus = [{ code: "DASHBOARD", title: "工作台", path: "#dashboard" }, ...state.menus];
    const requestedHash = window.location.hash || "#dashboard";
    const selectedMenu = availableMenus.find(menu => menu.path === requestedHash) || availableMenus[0];
    const selectedId = (selectedMenu.path || "#dashboard").replace("#", "");
    document.querySelectorAll("main > section.panel").forEach(section => {
        const isPasswordPanel = section.id === "password-panel";
        const isActive = section.id === selectedId;
        section.classList.toggle("hidden", !isActive || section.classList.contains("unavailable") || isPasswordPanel);
    });
    if (selectedMenu.path && window.location.hash !== selectedMenu.path) {
        history.replaceState(null, "", selectedMenu.path);
    }
    document.querySelectorAll("[data-menu-link]").forEach(link => {
        link.classList.toggle("active", link.getAttribute("href") === selectedMenu.path);
    });
    document.getElementById("workspace-title").textContent = selectedMenu.title || "工作台";
    document.getElementById("breadcrumb").textContent = menuGroupTitle(selectedMenu.code) + " / " + (selectedMenu.title || "工作台");
}

function menuGroupTitle(code) {
    const group = menuGroups.find(item => item.codes.includes(code));
    return group ? group.title : "工作台";
}

function renderDashboard() {
    const counts = {
        "dashboard-personnel-count": state.menus.filter(menu => menuGroupTitle(menu.code) === "信息维护").length,
        "dashboard-payroll-count": state.menus.filter(menu => menuGroupTitle(menu.code) === "工资变动").length,
        "dashboard-standard-count": state.menus.filter(menu => menuGroupTitle(menu.code) === "标准维护").length,
        "dashboard-system-count": state.menus.filter(menu => menuGroupTitle(menu.code) === "系统管理").length,
    };
    Object.entries(counts).forEach(([id, value]) => {
        document.getElementById(id).textContent = value;
    });
    document.getElementById("dashboard-quick-links").innerHTML = state.menus.slice(0, 12).map(menu => `
        <a class="quick-link" href="${escapeHtml(menu.path)}">
            <strong>${escapeHtml(menu.title)}</strong>
            <span>${escapeHtml(menuGroupTitle(menu.code))}</span>
        </a>
    `).join("");
}

async function initializeDictionaryPickers() {
    if (!hasMenu("PERSONNEL_MAINTENANCE")) {
        return;
    }
    initializeOrganizationPickerInput();
    try {
        const configs = await getJson("/api/dictionaries/field-configs?tableName=dryjbxx");
        state.dictionaryFieldConfigs = Object.fromEntries((configs || []).map(config => [String(config.fieldName || "").toLowerCase(), config]));
        const fieldBindings = {
            csny: "maint-birth-year-month",
            xb: "maint-gender",
            ryfl: "maint-personnel-category",
            gwfl: "maint-post-category",
            cjgzny: "maint-work-start",
            zzny: "maint-regularization",
            zgxl: "maint-highest-education",
            zwjb: "maint-position-level",
            xrzw: "maint-current-position",
            srny: "maint-position-start",
            mz: "maint-ethnicity",
            zzmm: "maint-political-status",
        };
        const fallbackPrefixes = {
            xb: "003",
            ryfl: "014",
            gwfl: "008",
            zgxl: "002",
            zwjb: "051",
            xrzw: "025",
            mz: "011",
            zzmm: "012",
            dwsx: "008",
        };
        Object.entries(fieldBindings).forEach(([fieldName, inputId]) => {
            const configured = state.dictionaryFieldConfigs[fieldName];
            const config = configured || {
                fieldName,
                caption: fieldName,
                dictionaryPrefix: fallbackPrefixes[fieldName],
                fieldType: null,
            };
            const input = document.getElementById(inputId);
            if (!input) {
                return;
            }
            if (String(config.fieldType || "").toUpperCase() === "D") {
                input.type = "month";
                input.placeholder = "";
                input.dataset.monthField = "true";
            }
            if (!config || !config.dictionaryPrefix) {
                return;
            }
            input.dataset.dictionaryPrefix = config.dictionaryPrefix;
            input.dataset.dictionaryField = fieldName;
            const wrapper = input.closest("label");
            if (wrapper && !wrapper.querySelector(".dict-input-combo")) {
                const combo = document.createElement("div");
                combo.className = "dict-input-combo";
                wrapper.insertBefore(combo, input);
                combo.appendChild(input);
                const button = document.createElement("button");
                button.type = "button";
                button.className = "dict-picker-button";
                button.setAttribute("aria-label", "展开选项");
                button.textContent = "⌄";
                button.addEventListener("click", () => openDictionaryPicker(inputId, config));
                combo.appendChild(button);
            }
        });
    } catch (error) {
        console.warn("字典字段配置加载失败", error);
    }
}

function initializeOrganizationPickerInput() {
    bindOrganizationPickerInput("maint-organization-name", "maint-organization-picker-button", "maintenance");
    bindOrganizationPickerInput("maint-search-organization-code", "maint-search-organization-picker-button", "maintenanceSearch");
}

function bindOrganizationPickerInput(inputId, buttonId, target) {
    const input = document.getElementById(inputId);
    if (!input) {
        return;
    }
    const wrapper = input.closest("label");
    let button = document.getElementById(buttonId) || wrapper.querySelector(".organization-picker-button");
    if (!button) {
        const combo = document.createElement("div");
        combo.className = "dict-input-combo";
        wrapper.insertBefore(combo, input);
        combo.appendChild(input);
        button = document.createElement("button");
        button.type = "button";
        button.id = buttonId;
        button.className = "dict-picker-button organization-picker-button";
        button.setAttribute("aria-label", "选择单位");
        button.textContent = "⌄";
        combo.appendChild(button);
    }
    if (!button.dataset.pickerBound) {
        button.addEventListener("click", () => openOrganizationPicker(target));
        button.dataset.pickerBound = "true";
    }
    if (!input.dataset.pickerBound) {
        input.addEventListener("click", () => openOrganizationPicker(target));
        input.addEventListener("focus", () => openOrganizationPicker(target));
        input.addEventListener("keydown", event => {
            if (target === "maintenanceSearch" && (event.key === "Backspace" || event.key === "Delete")) {
                input.value = "";
                event.preventDefault();
            }
        });
        input.dataset.pickerBound = "true";
    }
}

async function openOrganizationPicker(target = "maintenance") {
    state.activeOrganizationTarget = target;
    document.getElementById("organization-picker-title").textContent = target === "personnelTransfer" ? "选择调往单位" : "选择单位";
    document.getElementById("organization-picker-subtitle").textContent = target === "personnelTransfer"
        ? "从单位树中选择调往本地其他单位，支持按单位名称或编码搜索。"
        : "从单位树中选择人员所属单位，支持按单位名称或编码搜索。";
    document.getElementById("organization-picker-filter").value = "";
    document.getElementById("organization-picker-tree").innerHTML = "正在加载单位...";
    document.getElementById("organization-picker-modal").classList.remove("hidden");
    try {
        state.organizationNodes = await getJson("/api/organizations/tree");
        state.organizationExpandedCodes = new Set(rootOrganizationNodes(state.organizationNodes).map(node => node.code));
        renderOrganizationPickerTree();
    } catch (error) {
        document.getElementById("organization-picker-tree").innerHTML = `<div class="status error">${escapeHtml(error.message)}</div>`;
    }
}

function closeOrganizationPicker() {
    document.getElementById("organization-picker-modal").classList.add("hidden");
}

function renderOrganizationPickerTree() {
    const container = document.getElementById("organization-picker-tree");
    const filter = document.getElementById("organization-picker-filter").value.trim().toLowerCase();
    const allNodes = state.organizationNodes || [];
    const childrenByParent = organizationChildrenByParent(allNodes);
    const roots = rootOrganizationNodes(allNodes);
    const visibleNodes = [];
    const appendVisibleNodes = (node, depth) => {
        const children = childrenByParent.get(node.code) || [];
        const descendantMatches = children.some(child => organizationNodeMatchesFilter(child, filter, childrenByParent));
        const selfMatches = organizationNodeTextMatches(node, filter);
        if (!filter || selfMatches || descendantMatches) {
            visibleNodes.push({ node, depth, hasChildren: children.length > 0 });
            const expanded = filter || state.organizationExpandedCodes.has(node.code);
            if (expanded) {
                children.forEach(child => appendVisibleNodes(child, depth + 1));
            }
        }
    };
    roots.forEach(root => appendVisibleNodes(root, 0));
    if (!visibleNodes.length) {
        container.innerHTML = "<div class='empty-state'>没有匹配的单位</div>";
        return;
    }
    container.innerHTML = visibleNodes.map(({ node, depth, hasChildren }) => {
        const expanded = filter || state.organizationExpandedCodes.has(node.code);
        return `
            <button type="button" class="dictionary-node organization-node ${hasChildren ? "branch" : "leaf"}" style="--depth:${depth}" data-org-code="${escapeHtml(node.code)}" data-org-name="${escapeHtml(node.name || node.shortName || "")}" data-has-children="${hasChildren}">
                <em>${hasChildren ? (expanded ? "▾" : "▸") : "•"}</em>
                <strong>${escapeHtml(node.name || node.shortName || "")}</strong>
                <span>${escapeHtml(node.code)}</span>
            </button>
        `;
    }).join("");
    container.querySelectorAll(".dictionary-node").forEach(button => {
        button.addEventListener("click", () => {
            if (button.dataset.hasChildren === "true") {
                toggleOrganizationNode(button.dataset.orgCode);
                return;
            }
            selectOrganizationNode(button.dataset.orgCode || "", button.dataset.orgName || button.dataset.orgCode || "");
            closeOrganizationPicker();
        });
    });
}

function selectOrganizationNode(code, name) {
    if (state.activeOrganizationTarget === "personnelTransfer") {
        const pending = state.pendingPersonnelChange;
        state.pendingPersonnelChange = null;
        if (pending) {
            continuePersonnelChangeMaintenance(pending.uid, pending.name, pending.changeType, pending.changeDescription, { code, name });
        }
        return;
    }
    if (state.activeOrganizationTarget === "maintenanceSearch") {
        document.getElementById("maint-search-organization-code").value = name || code;
        return;
    }
    document.getElementById("maint-organization-code").value = code;
    document.getElementById("maint-organization-name").value = name || code;
}

function organizationChildrenByParent(nodes) {
    const map = new Map();
    nodes.forEach(node => {
        const key = node.parentCode || "__ROOT__";
        if (!map.has(key)) {
            map.set(key, []);
        }
        map.get(key).push(node);
    });
    return map;
}

function rootOrganizationNodes(nodes) {
    const codes = new Set((nodes || []).map(node => node.code));
    return (nodes || []).filter(node => !node.parentCode || !codes.has(node.parentCode));
}

function organizationNodeTextMatches(node, filter) {
    if (!filter) {
        return true;
    }
    return String(node.code || "").toLowerCase().includes(filter)
        || String(node.name || "").toLowerCase().includes(filter)
        || String(node.shortName || "").toLowerCase().includes(filter);
}

function organizationNodeMatchesFilter(node, filter, childrenByParent) {
    if (organizationNodeTextMatches(node, filter)) {
        return true;
    }
    return (childrenByParent.get(node.code) || []).some(child => organizationNodeMatchesFilter(child, filter, childrenByParent));
}

function toggleOrganizationNode(code) {
    if (state.organizationExpandedCodes.has(code)) {
        state.organizationExpandedCodes.delete(code);
    } else {
        state.organizationExpandedCodes.add(code);
    }
    renderOrganizationPickerTree();
}

async function openDictionaryPicker(inputId, config) {
    state.activeDictionaryTarget = { inputId, config };
    document.getElementById("dictionary-picker-title").textContent = `选择${config.caption || config.fieldName}`;
    document.getElementById("dictionary-picker-tree").innerHTML = "正在加载选项...";
    document.getElementById("dictionary-picker-modal").classList.remove("hidden");
    try {
        state.activeDictionaryNodes = await getJson(`/api/dictionaries/tree?prefix=${encodeURIComponent(config.dictionaryPrefix)}`);
        state.dictionaryExpandedCodes = new Set(rootDictionaryNodes(state.activeDictionaryNodes).map(node => node.code));
        renderDictionaryPickerTree();
    } catch (error) {
        document.getElementById("dictionary-picker-tree").innerHTML = `<div class="status error">${escapeHtml(error.message)}</div>`;
    }
}

function closeDictionaryPicker() {
    document.getElementById("dictionary-picker-modal").classList.add("hidden");
}

function renderDictionaryPickerTree() {
    const container = document.getElementById("dictionary-picker-tree");
    const filter = "";
    const allNodes = state.activeDictionaryNodes || [];
    const childrenByParent = dictionaryChildrenByParent(allNodes);
    const roots = rootDictionaryNodes(allNodes);
    const visibleNodes = [];
    const appendVisibleNodes = (node, depth) => {
        const children = childrenByParent.get(node.code) || [];
        const descendantMatches = children.some(child => dictionaryNodeMatchesFilter(child, filter, childrenByParent));
        const selfMatches = dictionaryNodeTextMatches(node, filter);
        if (!filter || selfMatches || descendantMatches) {
            visibleNodes.push({ node, depth, hasChildren: children.length > 0 });
            const expanded = filter || state.dictionaryExpandedCodes.has(node.code);
            if (expanded) {
                children.forEach(child => appendVisibleNodes(child, depth + 1));
            }
        }
    };
    roots.forEach(root => appendVisibleNodes(root, 0));
    if (!visibleNodes.length) {
        container.innerHTML = "<div class='empty-state'>没有匹配的选项</div>";
        return;
    }
    container.innerHTML = visibleNodes.map(({ node, depth, hasChildren }) => {
        const expanded = filter || state.dictionaryExpandedCodes.has(node.code);
        return `
            <button type="button" class="dictionary-node ${hasChildren ? "branch" : "leaf"}" style="--depth:${depth}" data-dict-code="${escapeHtml(node.code)}" data-dict-value="${escapeHtml(node.value || "")}" data-dict-name="${escapeHtml(node.name || "")}" data-has-children="${hasChildren}">
                <em>${hasChildren ? (expanded ? "▾" : "▸") : "•"}</em>
                <span>${escapeHtml(node.code)}</span>
                <strong>${escapeHtml(node.name)}</strong>
            </button>
        `;
    }).join("");
    container.querySelectorAll(".dictionary-node").forEach(button => {
        button.addEventListener("click", () => {
            if (button.dataset.hasChildren === "true") {
                toggleDictionaryNode(button.dataset.dictCode);
                return;
            }
            selectDictionaryNode(button.dataset.dictValue, button.dataset.dictName);
        });
    });
}

function dictionaryChildrenByParent(nodes) {
    const map = new Map();
    nodes.forEach(node => {
        const key = node.parentCode || "__ROOT__";
        if (!map.has(key)) {
            map.set(key, []);
        }
        map.get(key).push(node);
    });
    return map;
}

function rootDictionaryNodes(nodes) {
    const codes = new Set((nodes || []).map(node => node.code));
    return (nodes || []).filter(node => !node.parentCode || !codes.has(node.parentCode));
}

function dictionaryNodeTextMatches(node, filter) {
    if (!filter) {
        return true;
    }
    return String(node.code || "").toLowerCase().includes(filter)
        || String(node.value || "").toLowerCase().includes(filter)
        || String(node.name || "").toLowerCase().includes(filter);
}

function dictionaryNodeMatchesFilter(node, filter, childrenByParent) {
    if (dictionaryNodeTextMatches(node, filter)) {
        return true;
    }
    return (childrenByParent.get(node.code) || []).some(child => dictionaryNodeMatchesFilter(child, filter, childrenByParent));
}

function toggleDictionaryNode(code) {
    if (state.dictionaryExpandedCodes.has(code)) {
        state.dictionaryExpandedCodes.delete(code);
    } else {
        state.dictionaryExpandedCodes.add(code);
    }
    renderDictionaryPickerTree();
}

function selectDictionaryNode(value, name) {
    const target = state.activeDictionaryTarget;
    if (!target) {
        return;
    }
    const input = document.getElementById(target.inputId);
    if (input) {
        input.value = target.inputId === "maint-education-code" || target.inputId === "maint-rank-code"
            ? value
            : name;
    }
    if (target.config?.linkedCodeInputId) {
        const linkedInput = document.getElementById(target.config.linkedCodeInputId);
        if (linkedInput) {
            linkedInput.value = value || "";
        }
    }
    closeDictionaryPicker();
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

async function onPersonnelMaintenanceSearch(event) {
    event.preventDefault();
    await loadPersonnelMaintenance();
}

async function onPersonnelMaintenanceSave(event) {
    event.preventDefault();
    const uid = document.getElementById("personnel-maintenance-uid").value;
    const payload = personnelMaintenancePayload();
    const status = document.getElementById("personnel-maintenance-status");
    status.className = "status";
    status.textContent = "正在保存人员信息...";
    try {
        const saved = uid ? await putJson(`/api/personnel/${uid}`, payload) : await postJson("/api/personnel", payload);
        status.textContent = `保存成功：${saved.name}（${saved.organizationCode}-${saved.personCode}）`;
        fillPersonnelMaintenanceForm(saved);
        await loadPersonnelSubrecords(saved.uid, saved.organizationCode, saved.personCode);
        await loadPersonnelMaintenance();
    } catch (error) {
        showError(status, error);
    }
}

function openNewPersonnelMaintenance() {
    resetPersonnelMaintenanceForm();
    openPersonnelMaintenanceModal("新增人员", "填写人员基本信息后保存；保存成功后可继续维护多条附属记录。");
}

function openPersonnelMaintenanceModal(title, subtitle) {
    document.getElementById("personnel-maintenance-modal-title").textContent = title;
    document.getElementById("personnel-maintenance-modal-subtitle").textContent = subtitle;
    document.getElementById("personnel-maintenance-modal").classList.remove("hidden");
    showPersonnelTab("basic");
}

function closePersonnelMaintenanceModal() {
    document.getElementById("personnel-maintenance-modal").classList.add("hidden");
}

function showPersonnelTab(tabName) {
    document.querySelectorAll("[data-personnel-tab]").forEach(button => {
        button.classList.toggle("active", button.dataset.personnelTab === tabName);
    });
    ["basic", "education", "position", "current-payroll", "payroll", "assessment", "award", "rank-level", "wage-reform", "pre-reform"].forEach(name => {
        document.getElementById(`personnel-tab-${name}`).classList.toggle("hidden", name !== tabName);
    });
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

async function onEducationPromotionSearch(event) {
    event.preventDefault();
    document.getElementById("education-promotion-page").value = "0";
    await loadEducationPromotions();
}

async function onRegularizationSearch(event) {
    event.preventDefault();
    document.getElementById("regularization-page").value = "0";
    await loadRegularizations();
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

async function loadPersonnelMaintenance() {
    const organizationCode = document.getElementById("maint-search-organization-code").value.trim();
    const keyword = document.getElementById("maint-search-keyword").value.trim();
    const size = document.getElementById("maint-search-size").value || "20";
    const params = new URLSearchParams({ page: "0", size });
    if (organizationCode) {
        params.set("organizationCode", organizationCode);
    }
    if (keyword) {
        params.set("keyword", keyword);
    }
    const rows = document.getElementById("personnel-maintenance-rows");
    const status = document.getElementById("personnel-maintenance-status");
    status.className = "status";
    status.textContent = "正在查询人员维护列表...";
    rows.innerHTML = "";
    try {
        const page = await getJson(`/api/personnel?${params}`);
        rows.innerHTML = (page.content || []).map(person => `
            <tr>
                <td>${escapeHtml(person.uid)}</td>
                <td>${escapeHtml(person.organizationCode)} ${escapeHtml(person.organizationName || "")}</td>
                <td>${escapeHtml(person.personCode)}</td>
                <td>${escapeHtml(person.name)}</td>
                <td>${escapeHtml(person.idCard || "")}</td>
                <td>${escapeHtml(person.currentPosition || "")}</td>
                <td>
                    <button class="row-action" data-maint-edit="${person.uid}" type="button">编辑</button>
                    <button class="row-action" data-maint-change="${person.uid}" data-person-name="${escapeHtml(person.name)}" type="button">变动</button>
                    <button class="row-action danger-button" data-maint-delete="${person.uid}" type="button">删除</button>
                </td>
            </tr>
        `).join("");
        rows.querySelectorAll("button[data-maint-edit]").forEach(button => {
            button.addEventListener("click", () => editPersonnelMaintenance(button.dataset.maintEdit));
        });
        rows.querySelectorAll("button[data-maint-delete]").forEach(button => {
            button.addEventListener("click", () => deletePersonnelMaintenance(button.dataset.maintDelete));
        });
        rows.querySelectorAll("button[data-maint-change]").forEach(button => {
            button.addEventListener("click", event => {
                event.stopPropagation();
                openPersonnelChangeMenu(button);
            });
        });
        status.textContent = `共 ${page.totalElements} 人，当前显示 ${page.content.length} 人`;
    } catch (error) {
        showError(status, error);
    }
}

function openPersonnelChangeMenu(button) {
    closePersonnelChangeMenu();
    const menu = document.createElement("div");
    menu.className = "personnel-change-menu";
    menu.innerHTML = personnelChangeTypes.map(item => `
        <button type="button" data-change-type="${escapeHtml(item.type)}" data-change-description="${escapeHtml(item.description)}">
            <strong>${escapeHtml(item.type)}</strong>
            <span>${escapeHtml(item.description)}</span>
        </button>
    `).join("");
    document.body.appendChild(menu);
    const rect = button.getBoundingClientRect();
    menu.style.left = `${Math.min(rect.left, window.innerWidth - 220)}px`;
    menu.style.top = `${rect.bottom + 6}px`;
    menu.querySelectorAll("button[data-change-type]").forEach(itemButton => {
        itemButton.addEventListener("click", event => {
            event.stopPropagation();
            closePersonnelChangeMenu();
            changePersonnelMaintenance(
                button.dataset.maintChange,
                button.dataset.personName || "",
                itemButton.dataset.changeType,
                itemButton.dataset.changeDescription);
        });
    });
}

function closePersonnelChangeMenu() {
    document.querySelectorAll(".personnel-change-menu").forEach(menu => menu.remove());
}

async function changePersonnelMaintenance(uid, name, changeType, changeDescription) {
    if (changeType === "调动") {
        state.pendingPersonnelChange = { uid, name, changeType, changeDescription };
        await openOrganizationPicker("personnelTransfer");
        return;
    }
    await continuePersonnelChangeMaintenance(uid, name, changeType, changeDescription, null);
}

async function continuePersonnelChangeMaintenance(uid, name, changeType, changeDescription, targetOrganization) {
    const remark = prompt("请输入备注（可留空）：", "");
    if (remark === null) {
        return;
    }
    const defaultRemark = changeDescription && changeDescription !== changeType ? changeDescription : "";
    const transferRemark = targetOrganization ? `调往单位：${targetOrganization.name || ""}（${targetOrganization.code || ""}）` : "";
    const finalRemark = [defaultRemark, transferRemark, remark.trim()].filter(Boolean).join("；");
    if (!confirm(`确认将 ${name || "该人员"} 办理为“${changeType.trim()}”？该人员将转入变动人员信息。`)) {
        return;
    }
    const status = document.getElementById("personnel-maintenance-status");
    status.className = "status";
    status.textContent = "正在办理人员变动...";
    try {
        const result = await postJson(`/api/personnel/${encodeURIComponent(uid)}/change`, {
            changeType: changeType.trim(),
            effectivePeriod: "",
            remark: finalRemark,
        });
        status.textContent = result.message || "人员变动处理完成";
        resetPersonnelMaintenanceForm();
        await loadPersonnelMaintenance();
        if (hasMenu("CHANGED_PERSONNEL")) {
            await loadChangedPersonnel();
        }
    } catch (error) {
        showError(status, error);
    }
}

async function editPersonnelMaintenance(uid) {
    const status = document.getElementById("personnel-maintenance-status");
    status.className = "status";
    status.textContent = "正在加载人员详情...";
    try {
        const record = await getJson(`/api/personnel/${uid}/maintenance`);
        fillPersonnelMaintenanceForm(record);
        openPersonnelMaintenanceModal("编辑人员", `${record.organizationCode}-${record.personCode} ${record.name}`);
        await loadPersonnelSubrecords(record.uid, record.organizationCode, record.personCode);
        status.textContent = `正在编辑：${record.name}`;
    } catch (error) {
        showError(status, error);
    }
}

async function deletePersonnelMaintenance(uid) {
    if (!confirm("确认删除该人员基本信息？该操作不可恢复。")) {
        return;
    }
    const status = document.getElementById("personnel-maintenance-status");
    status.className = "status";
    status.textContent = "正在删除人员...";
    try {
        await deleteJson(`/api/personnel/${uid}`);
        status.textContent = "删除成功";
        resetPersonnelMaintenanceForm();
        await loadPersonnelMaintenance();
    } catch (error) {
        showError(status, error);
    }
}

function personnelMaintenancePayload() {
    return {
        organizationCode: document.getElementById("maint-organization-code").value.trim(),
        personCode: document.getElementById("maint-person-code").value.trim(),
        name: document.getElementById("maint-name").value.trim(),
        idCard: document.getElementById("maint-id-card").value.trim(),
        gender: document.getElementById("maint-gender").value.trim(),
        birthYearMonth: monthPayloadValue("maint-birth-year-month"),
        personnelCategory: document.getElementById("maint-personnel-category").value.trim(),
        organizationType: document.getElementById("maint-organization-type").value.trim(),
        postCategory: document.getElementById("maint-post-category").value.trim(),
        workStartYearMonth: monthPayloadValue("maint-work-start"),
        regularizationYearMonth: monthPayloadValue("maint-regularization"),
        salaryYears: Number(document.getElementById("maint-salary-years").value || 0),
        educationCode: document.getElementById("maint-education-code").value.trim(),
        highestEducation: document.getElementById("maint-highest-education").value.trim(),
        currentPositionLevel: document.getElementById("maint-position-level").value.trim(),
        currentRankCode: document.getElementById("maint-rank-code").value.trim(),
        currentPosition: document.getElementById("maint-current-position").value.trim(),
        currentPositionStartYearMonth: monthPayloadValue("maint-position-start"),
        ethnicity: document.getElementById("maint-ethnicity").value.trim(),
        politicalStatus: document.getElementById("maint-political-status").value.trim(),
        archiveNumber: document.getElementById("maint-archive-number").value.trim(),
    };
}

function fillPersonnelMaintenanceForm(record) {
    state.activePersonnelMaintenance = record;
    document.getElementById("personnel-maintenance-uid").value = record.uid || "";
    document.getElementById("maint-organization-code").value = record.organizationCode || "";
    document.getElementById("maint-organization-name").value = record.organizationName || record.organizationCode || "";
    document.getElementById("maint-person-code").value = record.personCode || "";
    document.getElementById("maint-name").value = record.name || "";
    document.getElementById("maint-id-card").value = record.idCard || "";
    document.getElementById("maint-gender").value = record.gender || "";
    setMonthInputValue("maint-birth-year-month", record.birthYearMonth);
    document.getElementById("maint-personnel-category").value = record.personnelCategory || "";
    document.getElementById("maint-organization-type").value = record.organizationType || "";
    document.getElementById("maint-post-category").value = record.postCategory || "";
    setMonthInputValue("maint-work-start", record.workStartYearMonth);
    setMonthInputValue("maint-regularization", record.regularizationYearMonth);
    document.getElementById("maint-salary-years").value = record.salaryYears || 0;
    document.getElementById("maint-education-code").value = record.educationCode || "";
    document.getElementById("maint-highest-education").value = record.highestEducation || "";
    document.getElementById("maint-position-level").value = record.currentPositionLevel || "";
    document.getElementById("maint-rank-code").value = record.currentRankCode || "";
    document.getElementById("maint-current-position").value = record.currentPosition || "";
    setMonthInputValue("maint-position-start", record.currentPositionStartYearMonth);
    document.getElementById("maint-ethnicity").value = record.ethnicity || "";
    document.getElementById("maint-political-status").value = record.politicalStatus || "";
    document.getElementById("maint-archive-number").value = record.archiveNumber || "";
}

function resetPersonnelMaintenanceForm() {
    state.activePersonnelMaintenance = null;
    document.getElementById("personnel-maintenance-form").reset();
    document.getElementById("personnel-maintenance-uid").value = "";
    document.getElementById("maint-salary-years").value = "0";
    [
        "maint-education-rows", "maint-position-rows", "maint-payroll-rows", "maint-assessment-rows",
        "maint-current-payroll-rows", "maint-award-rows", "maint-rank-rows",
        "maint-wage-reform-rows", "maint-pre-reform-rows", "maint-pension-base-rows",
    ].forEach(id => {
        document.getElementById(id).innerHTML = "<tr><td colspan='8'>保存或选择人员后加载记录</td></tr>";
    });
}

function openSubrecordEditor(type, record = null) {
    const person = state.activePersonnelMaintenance;
    if (!person || !person.uid) {
        alert("请先保存或选择一个人员。");
        return;
    }
    const config = subrecordEditors[type];
    state.activeSubrecordEditor = { type, record };
    document.getElementById("subrecord-editor-title").textContent = `${record ? "编辑" : "新增"}${config.title}`;
    document.getElementById("subrecord-editor-form").innerHTML = config.fields.map(([name, label, inputType, options]) => `
        <label>${escapeHtml(label)}
            <input id="subrecord-field-${escapeHtml(name)}" data-subrecord-field="${escapeHtml(name)}" type="${inputType === "number" ? "number" : inputType === "month" ? "month" : "text"}" value="${escapeHtml(subrecordInputValue(record?.[name], inputType))}" ${options?.readonly ? "readonly" : ""}>
        </label>
    `).join("") + `<div class="form-actions"><button type="submit">保存记录</button></div>`;
    enhanceSubrecordEditorInputs(config);
    document.getElementById("subrecord-editor-status").textContent = "";
    document.getElementById("subrecord-editor-status").className = "status";
    document.getElementById("subrecord-editor-modal").classList.remove("hidden");
}

function enhanceSubrecordEditorInputs(config) {
    config.fields.forEach(([name, label, , options]) => {
        const dictionaryPrefix = subrecordDictionaryPrefix(options);
        if (!dictionaryPrefix) {
            return;
        }
        const input = document.getElementById(`subrecord-field-${name}`);
        const wrapper = input?.closest("label");
        if (!input || !wrapper || wrapper.querySelector(".dict-input-combo")) {
            return;
        }
        const combo = document.createElement("div");
        combo.className = "dict-input-combo";
        wrapper.insertBefore(combo, input);
        combo.appendChild(input);
        const button = document.createElement("button");
        button.type = "button";
        button.className = "dict-picker-button";
        button.setAttribute("aria-label", `选择${label}`);
        button.textContent = "⌄";
        button.addEventListener("click", () => openDictionaryPicker(input.id, {
            fieldName: name,
            caption: label,
            dictionaryPrefix,
            linkedCodeInputId: options.linkedCodeField ? `subrecord-field-${options.linkedCodeField}` : null,
        }));
        combo.appendChild(button);
    });
}

function subrecordDictionaryPrefix(options) {
    if (!options) {
        return null;
    }
    if (options.dictionaryPrefixField) {
        const configured = state.dictionaryFieldConfigs?.[String(options.dictionaryPrefixField).toLowerCase()];
        if (configured?.dictionaryPrefix) {
            return configured.dictionaryPrefix;
        }
    }
    return options.dictionaryPrefix || null;
}

function closeSubrecordEditor() {
    document.getElementById("subrecord-editor-modal").classList.add("hidden");
}

async function onSubrecordSave(event) {
    event.preventDefault();
    const person = state.activePersonnelMaintenance;
    const editor = state.activeSubrecordEditor;
    const status = document.getElementById("subrecord-editor-status");
    if (!person || !editor) {
        return;
    }
    const config = subrecordEditors[editor.type];
    const payload = {};
    config.fields.forEach(([name, , inputType]) => {
        const input = document.querySelector(`[data-subrecord-field="${name}"]`);
        payload[name] = inputType === "number" ? Number(input.value || 0) : inputType === "month" ? input.value.replace("-", ".") : input.value.trim();
    });
    status.textContent = "正在保存记录...";
    try {
        const url = editor.record
            ? (config.updateEndpoint ? config.updateEndpoint(editor.record.id) : `${config.endpoint(person.uid)}/${editor.record.id}`)
            : config.endpoint(person.uid);
        const rows = editor.record ? await putJson(url, payload) : await postJson(url, payload);
        status.textContent = "保存成功";
        closeSubrecordEditor();
        await loadPersonnelSubrecords(person.uid, person.organizationCode, person.personCode);
    } catch (error) {
        showError(status, error);
    }
}

async function deleteSubrecord(type, id) {
    const person = state.activePersonnelMaintenance;
    if (!person || !confirm("确认删除该记录？")) {
        return;
    }
    const config = subrecordEditors[type];
    const url = config.updateEndpoint ? config.updateEndpoint(id) : `${config.endpoint(person.uid)}/${id}`;
    await deleteJson(url);
    await loadPersonnelSubrecords(person.uid, person.organizationCode, person.personCode);
}

function subrecordInputValue(value, inputType) {
    if (inputType === "month") {
        return String(value || "").replace(".", "-").slice(0, 7);
    }
    return value ?? "";
}

function monthPayloadValue(inputId) {
    const value = document.getElementById(inputId).value.trim();
    return value ? value.replace("-", ".") : "";
}

function setMonthInputValue(inputId, value) {
    const input = document.getElementById(inputId);
    const raw = String(value || "").trim();
    input.value = raw ? raw.replace(".", "-").slice(0, 7) : "";
}

async function loadPersonnelSubrecords(uid, organizationCode, personCode) {
    const [education, positions, assessments, payrollHistory, relatedRecords] = await Promise.all([
        getJson(`/api/personnel/${uid}/education`),
        getJson(`/api/personnel/${uid}/positions`),
        getJson(`/api/personnel/${uid}/assessments`),
        getJson(`/api/payroll/histories?organizationCode=${encodeURIComponent(organizationCode)}&keyword=${encodeURIComponent(personCode)}&size=50`),
        getJson(`/api/personnel/${uid}/related-records`),
    ]);
    document.getElementById("maint-education-rows").innerHTML = education.length ? education.map(row => `
        <tr>
            <td>${escapeHtml(row.id)} ${row.appCreated ? "<span class='new-badge'>新</span>" : ""}</td>
            <td>${escapeHtml(row.educationCode)}</td>
            <td>${escapeHtml(row.educationName)}</td>
            <td>${escapeHtml(row.school)}</td>
            <td>${escapeHtml(row.enrollmentDate)}</td>
            <td>${escapeHtml(row.graduationDate)}</td>
            <td>${escapeHtml(row.educationType)}</td>
            <td>${escapeHtml(row.remark)} <button class="row-action" type="button" data-edit-education="${row.id}">编辑</button> <button class="row-action danger-button" type="button" data-delete-education="${row.id}">删除</button></td>
        </tr>
    `).join("") : "<tr><td colspan='8'>暂无学历记录</td></tr>";
    document.getElementById("maint-position-rows").innerHTML = positions.length ? positions.map(row => `
        <tr>
            <td>${escapeHtml(row.id)} ${row.appCreated ? "<span class='new-badge'>新</span>" : ""}</td>
            <td>${escapeHtml(row.currentPositionCode)}</td>
            <td>${escapeHtml(row.currentPosition)}</td>
            <td>${escapeHtml(row.rankCode)}</td>
            <td>${escapeHtml(row.positionCode)}</td>
            <td>${escapeHtml(row.positionName)}</td>
            <td>${escapeHtml(row.startYearMonth)}</td>
            <td>${escapeHtml(row.activeFlag)} <button class="row-action" type="button" data-edit-position="${row.id}">编辑</button> <button class="row-action danger-button" type="button" data-delete-position="${row.id}">删除</button></td>
        </tr>
    `).join("") : "<tr><td colspan='8'>暂无任职记录</td></tr>";
    document.getElementById("maint-assessment-rows").innerHTML = assessments.length ? assessments.map(row => `
        <tr>
            <td>${escapeHtml(row.id)} ${row.appCreated ? "<span class='new-badge'>新</span>" : ""}</td>
            <td>${escapeHtml(row.year)}</td>
            <td>${escapeHtml(row.result)} <button class="row-action" type="button" data-edit-assessment="${row.id}">编辑</button> <button class="row-action danger-button" type="button" data-delete-assessment="${row.id}">删除</button></td>
        </tr>
    `).join("") : "<tr><td colspan='3'>暂无考核记录</td></tr>";
    const histories = payrollHistory.content || [];
    document.getElementById("maint-payroll-rows").innerHTML = histories.length ? histories.map(row => `
        <tr>
            <td>${escapeHtml(row.calculationYear)}${escapeHtml(row.calculationMonth)} ${row.appCreated ? "<span class='new-badge'>新</span>" : ""}</td>
            <td>${escapeHtml(row.changeType)}</td>
            <td>${escapeHtml(row.positionName)}</td>
            <td>${escapeHtml(row.gradeSalaryLevel || "")}</td>
            <td>${escapeHtml(row.positionSalaryGrade || "")}</td>
            <td>${money(row.positionSalary)}</td>
            <td>${money(row.gradeSalary)}</td>
            <td>${money(row.totalAmount)}</td>
            <td>${row.currentPayroll ? "是" : "否"} <button class="row-action" type="button" data-edit-payroll="${row.id}">编辑</button> <button class="row-action danger-button" type="button" data-delete-payroll="${row.id}">删除</button></td>
        </tr>
    `).join("") : "<tr><td colspan='9'>暂无调资记录</td></tr>";
    renderPersonnelRelatedRecords(relatedRecords || {});
    bindSubrecordActions("education", education);
    bindSubrecordActions("position", positions);
    bindSubrecordActions("assessment", assessments);
    bindSubrecordActions("payroll", histories);
}

function bindSubrecordActions(type, rows) {
    rows.forEach(row => {
        const edit = document.querySelector(`[data-edit-${type}="${row.id}"]`);
        const del = document.querySelector(`[data-delete-${type}="${row.id}"]`);
        if (edit) {
            edit.addEventListener("click", () => openSubrecordEditor(type, row));
        }
        if (del) {
            del.addEventListener("click", () => deleteSubrecord(type, row.id));
        }
    });
}

function renderPersonnelRelatedRecords(records) {
    const current = records.currentPayroll || {};
    document.getElementById("maint-current-payroll-rows").innerHTML = Object.keys(current).length ? `
        <tr>
            <td>${escapeHtml(textField(current, "jsnf"))}${escapeHtml(textField(current, "jsyf"))}</td>
            <td>${escapeHtml(textField(current, "jslb"))}</td>
            <td>${escapeHtml(textField(current, "zwbm2"))} ${escapeHtml(textField(current, "zwgw2"))}</td>
            <td>${escapeHtml(textField(current, "jbgzjb2"))}</td>
            <td>${escapeHtml(textField(current, "zwgzdc2"))}</td>
            <td>${money(numberField(current, "zwgzse2"))}</td>
            <td>${money(numberField(current, "jbgzse2"))}</td>
            <td>${money(numberField(current, "jsdjgz2"))}</td>
            <td>${money(numberField(current, "dfbt2"))}</td>
            <td>${money(numberField(current, "blfb2"))}</td>
            <td>${money(numberField(current, "jxjt"))}</td>
            <td>${money(numberField(current, "njbt"))}</td>
            <td>${money(numberField(current, "hj2"))}</td>
        </tr>
    ` : "<tr><td colspan='13'>暂无当前工资记录</td></tr>";

    document.getElementById("maint-award-rows").innerHTML = tableRows(records.awards, row => `
        <tr><td>${escapeHtml(textField(row, "hjmc"))}</td><td>${escapeHtml(textField(row, "sjdw"))}</td><td>${escapeHtml(textField(row, "jllx"))}</td><td>${escapeHtml(textField(row, "hjsj"))}</td><td>${escapeHtml(textField(row, "tqyjjssj"))}</td><td>${escapeHtml(textField(row, "jljb"))}</td><td>${escapeHtml(textField(row, "jldc"))}</td><td>${escapeHtml(textField(row, "qtqk"))}</td></tr>
    `, 8, "暂无获奖记录");
    document.getElementById("maint-rank-rows").innerHTML = tableRows(records.rankRecords, row => `
        <tr><td>${escapeHtml(rankRecordType(row))}</td><td>${escapeHtml(textField(row, "jx"))}</td><td>${escapeHtml(textField(row, "sysj"))}</td><td>${escapeHtml(textField(row, "syyy"))}</td><td>${escapeHtml(textField(row, "rmwh"))}</td><td>${truthyField(row, "xrjxbz") ? "是" : "否"}</td><td>${escapeHtml(textField(row, "lb"))}</td></tr>
    `, 7, "暂无警衔/等级记录");
    document.getElementById("maint-wage-reform-rows").innerHTML = tableRows(records.wageReform, row => `
        <tr><td>${escapeHtml(textField(row, "cjgzny"))}</td><td>${escapeHtml(textField(row, "tgnx"))}</td><td>${escapeHtml(textField(row, "zwmc"))}</td><td>${escapeHtml(textField(row, "rzsj"))}</td><td>${escapeHtml(textField(row, "rznx"))}</td><td>${escapeHtml(textField(row, "xl"))}</td><td>${escapeHtml(textField(row, "tgzw"))}</td><td>${escapeHtml(textField(row, "tgjb"))}</td><td>${escapeHtml(textField(row, "tgdc"))}</td><td>${escapeHtml(textField(row, "remark"))}</td></tr>
    `, 10, "暂无套改记录");
    document.getElementById("maint-pre-reform-rows").innerHTML = tableRows(records.preReformSalary, row => `
        <tr><td>${escapeHtml(textField(row, "jsnf") || textField(row, "nd") || textField(row, "tbnd"))}${escapeHtml(textField(row, "jsyf"))}</td><td>${escapeHtml(textField(row, "zwbm2"))} ${escapeHtml(textField(row, "zwgw2"))}</td><td>${escapeHtml(textField(row, "jbgzjb2"))}</td><td>${escapeHtml(textField(row, "zwgzdc2"))}</td><td>${money(numberField(row, "zwgzse2"))}</td><td>${money(numberField(row, "jbgzse2"))}</td><td>${money(numberField(row, "jsdjgz2"))}</td><td>${escapeHtml(textField(row, "bz") || textField(row, "remark"))}</td></tr>
    `, 8, "暂无套改前工资记录");
    document.getElementById("maint-pension-base-rows").innerHTML = tableRows(records.pensionBase, row => `
        <tr><td>${escapeHtml(textField(row, "nd"))}</td><td>${escapeHtml(textField(row, "zwbm2"))} ${escapeHtml(textField(row, "zwgw2"))}</td><td>${escapeHtml(textField(row, "jbgzjb2"))}</td><td>${escapeHtml(textField(row, "zwgzdc2"))}</td><td>${money(numberField(row, "zwgzse2"))}</td><td>${money(numberField(row, "jbgzse2"))}</td><td>${money(numberField(row, "js"))}</td><td>${escapeHtml(textField(row, "bz"))}</td></tr>
    `, 8, "暂无养老缴费基数记录");
}

function tableRows(rows, render, colspan, emptyText) {
    return (rows || []).length ? rows.map(render).join("") : `<tr><td colspan="${colspan}">${emptyText}</td></tr>`;
}

function textField(row, fieldName) {
    return row?.[fieldName] ?? row?.[fieldName.toUpperCase()] ?? "";
}

function numberField(row, fieldName) {
    return Number(textField(row, fieldName) || 0);
}

function truthyField(row, fieldName) {
    const value = textField(row, fieldName);
    return value === true || value === 1 || value === "1" || value === "true";
}

function rankRecordType(row) {
    const value = String(textField(row, "jx") || "");
    if (value.includes("检察")) {
        return "检察官等级";
    }
    if (value.includes("法官")) {
        return "法官等级";
    }
    if (value.includes("监察")) {
        return "监察官等级";
    }
    return "警衔";
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
                <td><button class="row-action" data-restore-org="${escapeHtml(row.organizationCode)}" data-restore-person="${escapeHtml(row.personCode)}" data-restore-name="${escapeHtml(row.name)}" type="button">恢复在册</button></td>
            </tr>
        `).join("");
        rows.querySelectorAll("button[data-restore-org]").forEach(button => {
            button.addEventListener("click", () => restoreChangedPersonnel(
                button.dataset.restoreOrg,
                button.dataset.restorePerson,
                button.dataset.restoreName || ""));
        });
        status.textContent = `第 ${result.page + 1} / ${Math.max(result.totalPages, 1)} 页，共 ${result.totalElements} 条变动人员记录`;
    } catch (error) {
        showError(status, error);
    }
}

async function restoreChangedPersonnel(organizationCode, personCode, name) {
    if (!confirm(`确认将 ${name || personCode} 恢复到在册人员信息？`)) {
        return;
    }
    const status = document.getElementById("changed-personnel-status");
    status.className = "status";
    status.textContent = "正在恢复人员...";
    try {
        const result = await postJson("/api/personnel/changed/restore", { organizationCode, personCode });
        status.textContent = result.message || "人员已恢复到在册";
        await loadChangedPersonnel();
        if (hasMenu("PERSONNEL_MAINTENANCE")) {
            await loadPersonnelMaintenance();
        }
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
                <td><button class="row-action" data-payroll-change="${escapeHtml(row.id)}" type="button">变动情况</button></td>
            </tr>
        `).join("");
        rows.querySelectorAll("button[data-payroll-change]").forEach(button => {
            button.addEventListener("click", () => openPayrollChangeModal(button.dataset.payrollChange));
        });
        status.textContent = `第 ${result.page + 1} / ${Math.max(result.totalPages, 1)} 页，共 ${result.totalElements} 条工资历史`;
    } catch (error) {
        showError(status, error);
    }
}

async function openPayrollChangeModal(payrollHistoryId) {
    const modal = document.getElementById("payroll-change-modal");
    const status = document.getElementById("payroll-change-status");
    const rows = document.getElementById("payroll-change-rows");
    const summary = document.getElementById("payroll-change-summary");
    modal.classList.remove("hidden");
    status.className = "status";
    status.textContent = "正在加载工资变动情况...";
    summary.textContent = "查看变动前后各工资项目对比。";
    rows.innerHTML = "";
    try {
        const comparison = await getJson(`/api/payroll/histories/${encodeURIComponent(payrollHistoryId)}/change-comparison`);
        summary.textContent = `${comparison.organizationCode}-${comparison.personCode} ${comparison.name} / ${comparison.calculationPeriod || ""} ${comparison.changeType || ""}`
            + (comparison.previousPayrollHistoryId ? `（前次：${comparison.previousCalculationPeriod || ""} ${comparison.previousChangeType || ""}）` : "（无前次工资记录）");
        rows.innerHTML = (comparison.components || []).map(component => `
            <tr class="${Number(component.difference || 0) !== 0 ? "highlight-row" : ""}">
                <td>${escapeHtml(component.caption || "")}</td>
                <td>${escapeHtml(component.fieldName || "")}</td>
                <td>${money(component.beforeAmount)}</td>
                <td>${money(component.afterAmount)}</td>
                <td>${money(component.difference)}</td>
            </tr>
        `).join("");
        status.textContent = `共 ${(comparison.components || []).length} 个工资项目`;
    } catch (error) {
        showError(status, error);
    }
}

function closePayrollChangeModal() {
    document.getElementById("payroll-change-modal").classList.add("hidden");
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
    const dueOnly = document.getElementById("normal-promotion-due-only").checked;
    const page = document.getElementById("normal-promotion-page").value || "0";
    const size = document.getElementById("normal-promotion-size").value || "20";
    const params = new URLSearchParams({ page, size, dueOnly });
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
        document.getElementById("normal-promotion-select-all").checked = false;
        rows.innerHTML = (result.content || []).map(row => `
            <tr>
                <td><input type="checkbox" data-normal-select="${escapeHtml(row.payrollHistoryId)}" data-normal-eligible="${row.eligible ? "true" : "false"}" ${row.eligible ? "" : "disabled"} aria-label="选择${escapeHtml(row.name)}"></td>
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
                <td>${escapeHtml(row.levelAssessmentStartYear || "")}</td>
                <td>${escapeHtml(row.stepAssessmentStartYear || "")}</td>
                <td>${escapeHtml(row.qualifiedYears ?? "")}</td>
                <td>${escapeHtml(row.requiredYears ?? "")}</td>
                <td>${row.eligible ? "是" : "否"}</td>
                <td>${money(row.currentBaseSalary)}</td>
                <td>${money(row.promotedBaseSalary)}</td>
                <td>${money(row.increaseAmount)}</td>
                <td>${escapeHtml(baseSalarySourceName(row.baseSalarySource))}</td>
                <td>
                    <button class="row-action" data-normal-apply="${escapeHtml(row.payrollHistoryId)}" type="button">处理</button>
                    <button class="row-action danger-button" data-normal-rollback="${escapeHtml(row.payrollHistoryId)}" type="button">还原</button>
                </td>
            </tr>
        `).join("");
        rows.querySelectorAll("button[data-normal-apply]").forEach(button => {
            button.addEventListener("click", () => applyPromotionAction("normal", button.dataset.normalApply));
        });
        rows.querySelectorAll("button[data-normal-rollback]").forEach(button => {
            button.addEventListener("click", () => rollbackPromotionAction("normal", button.dataset.normalRollback));
        });
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
                <td>
                    <button class="row-action" data-level-apply="${escapeHtml(row.payrollHistoryId)}" type="button">处理</button>
                    <button class="row-action danger-button" data-level-rollback="${escapeHtml(row.payrollHistoryId)}" type="button">还原</button>
                </td>
            </tr>
        `).join("");
        rows.querySelectorAll("button[data-level-apply]").forEach(button => {
            button.addEventListener("click", () => applyPromotionAction("level", button.dataset.levelApply));
        });
        rows.querySelectorAll("button[data-level-rollback]").forEach(button => {
            button.addEventListener("click", () => rollbackPromotionAction("level", button.dataset.levelRollback));
        });
        status.textContent = `第 ${result.page + 1} / ${Math.max(result.totalPages, 1)} 页，共 ${result.totalElements} 条试算记录`;
    } catch (error) {
        showError(status, error);
    }
}

async function applyPromotionAction(type, payrollHistoryId) {
    const moduleName = type === "normal" ? "正常档次/薪级晋升" : "级别晋升";
    if (!confirm(`确认按当前试算结果处理${moduleName}？系统会新增一条当前工资变动记录，并将原当前记录转为历史记录。`)) {
        return;
    }
    const status = document.getElementById(type === "normal" ? "normal-promotion-status" : "level-promotion-status");
    status.className = "status";
    status.textContent = `正在处理${moduleName}...`;
    try {
        const path = type === "normal" ? "normal-promotions" : "level-promotions";
        const result = await postJson(`/api/payroll/${path}/${encodeURIComponent(payrollHistoryId)}/apply`, {});
        status.textContent = result.message || `${moduleName}处理完成`;
        if (type === "normal") {
            await loadNormalPromotions();
        } else {
            await loadLevelPromotions();
        }
    } catch (error) {
        showError(status, error);
    }
}

async function applySelectedNormalPromotions() {
    const selectedIds = Array.from(document.querySelectorAll("[data-normal-select]:checked"))
        .filter(checkbox => checkbox.dataset.normalEligible === "true")
        .map(checkbox => checkbox.dataset.normalSelect)
        .filter(Boolean);
    const status = document.getElementById("normal-promotion-status");
    if (!selectedIds.length) {
        status.className = "status error";
        status.textContent = "请先勾选需要处理的正常档次/薪级晋升记录。";
        return;
    }
    if (!confirm(`确认批量处理 ${selectedIds.length} 条正常档次/薪级晋升记录？`)) {
        return;
    }
    status.className = "status";
    status.textContent = `正在批量处理 0 / ${selectedIds.length}...`;
    let successCount = 0;
    const failures = [];
    for (const id of selectedIds) {
        try {
            await postJson(`/api/payroll/normal-promotions/${encodeURIComponent(id)}/apply`, {});
            successCount++;
            status.textContent = `正在批量处理 ${successCount} / ${selectedIds.length}...`;
        } catch (error) {
            failures.push(error.message);
        }
    }
    status.className = failures.length ? "status error" : "status";
    status.textContent = failures.length
        ? `批量处理完成：成功 ${successCount} 条，失败 ${failures.length} 条。${failures[0] || ""}`
        : `批量处理完成：成功 ${successCount} 条。`;
    await loadNormalPromotions();
}

async function rollbackPromotionAction(type, payrollHistoryId) {
    const moduleName = type === "normal" ? "正常档次/薪级晋升" : "级别晋升";
    if (!confirm(`确认还原当前${moduleName}工资变动？系统会删除当前链头记录，并恢复上一条工资记录为当前执行工资。`)) {
        return;
    }
    const status = document.getElementById(type === "normal" ? "normal-promotion-status" : "level-promotion-status");
    status.className = "status";
    status.textContent = `正在还原${moduleName}...`;
    try {
        const path = type === "normal" ? "normal-promotions" : "level-promotions";
        const result = await postJson(`/api/payroll/${path}/${encodeURIComponent(payrollHistoryId)}/rollback`, {});
        status.textContent = result.message || `${moduleName}已还原`;
        if (type === "normal") {
            await loadNormalPromotions();
        } else {
            await loadLevelPromotions();
        }
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
                <td>${escapeHtml(row.currentPositionPrefix || "")}</td>
                <td>${escapeHtml(row.newPositionPrefix || "")}</td>
                <td>${escapeHtml(row.changeType || "")}</td>
                <td>${row.sequenceConversion ? "是" : "否"}</td>
                <td>${row.policeOfficerConversion ? "是" : "否"}</td>
                <td>${row.judicialConversion ? "是" : "否"}</td>
                <td>${escapeHtml(row.positionStartYearMonth || "")}</td>
                <td>${escapeHtml(row.effectivePeriod || "")}</td>
                <td>${escapeHtml(row.currentLevel || "")}</td>
                <td>${escapeHtml(row.currentStep || "")}</td>
                <td>${escapeHtml(row.newPositionMinimumLevel || "")}</td>
                <td>${escapeHtml(row.newPositionMaximumLevel || "")}</td>
                <td>${escapeHtml(row.policeSameRankLevel || "")}</td>
                <td>${escapeHtml(row.policeSameRankStep || "")}</td>
                <td>${row.policeHighPositionPromotion ? "是" : "否"}</td>
                <td>${escapeHtml(row.judicialConversionStep || "")}</td>
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

async function loadEducationPromotions() {
    const organizationCode = document.getElementById("education-promotion-organization-code").value.trim();
    const keyword = document.getElementById("education-promotion-keyword").value.trim();
    const page = document.getElementById("education-promotion-page").value || "0";
    const size = document.getElementById("education-promotion-size").value || "20";
    const params = new URLSearchParams({ page, size });
    if (organizationCode) {
        params.set("organizationCode", organizationCode);
    }
    if (keyword) {
        params.set("keyword", keyword);
    }

    const status = document.getElementById("education-promotion-status");
    const rows = document.getElementById("education-promotion-rows");
    status.className = "status";
    status.textContent = "正在查询学历晋升试算...";
    rows.innerHTML = "";

    try {
        const result = await getJson(`/api/payroll/education-promotions?${params}`);
        rows.innerHTML = (result.content || []).map(row => `
            <tr>
                <td>${escapeHtml(row.organizationCode)}</td>
                <td>${escapeHtml(row.personCode)}</td>
                <td>${escapeHtml(row.name)}</td>
                <td>${escapeHtml(row.calculationPeriod)}</td>
                <td>${escapeHtml(row.currentPositionCode || "")}</td>
                <td>${escapeHtml(row.currentPositionName || "")}</td>
                <td>${escapeHtml(row.educationCode || "")}</td>
                <td>${escapeHtml(row.educationName || "")}</td>
                <td>${escapeHtml(row.graduationDate || "")}</td>
                <td>${escapeHtml(row.standardPositionCode || "")}</td>
                <td>${escapeHtml(row.standardPositionName || "")}</td>
                <td>${escapeHtml(row.standardLevel || "")}</td>
                <td>${escapeHtml(row.standardStep || "")}</td>
                <td>${escapeHtml(row.promotedPositionCode || "")}</td>
                <td>${escapeHtml(row.promotedLevel || "")}</td>
                <td>${escapeHtml(row.promotedStep || "")}</td>
                <td>${money(row.currentPositionSalary)}</td>
                <td>${money(row.promotedPositionSalary)}</td>
                <td>${money(row.currentGradeSalary)}</td>
                <td>${money(row.promotedGradeSalary)}</td>
                <td>${money(row.positionSalaryIncrease)}</td>
                <td>${money(row.gradeSalaryIncrease)}</td>
                <td>${money(row.totalIncrease)}</td>
                <td>${row.eligible ? "是" : "否"}</td>
                <td>${escapeHtml(row.note || "")}</td>
            </tr>
        `).join("");
        status.textContent = `第 ${result.page + 1} / ${Math.max(result.totalPages, 1)} 页，共 ${result.totalElements} 条试算记录`;
    } catch (error) {
        showError(status, error);
    }
}

async function loadRegularizations() {
    const organizationCode = document.getElementById("regularization-organization-code").value.trim();
    const keyword = document.getElementById("regularization-keyword").value.trim();
    const page = document.getElementById("regularization-page").value || "0";
    const size = document.getElementById("regularization-size").value || "20";
    const params = new URLSearchParams({ page, size });
    if (organizationCode) {
        params.set("organizationCode", organizationCode);
    }
    if (keyword) {
        params.set("keyword", keyword);
    }

    const status = document.getElementById("regularization-status");
    const rows = document.getElementById("regularization-rows");
    status.className = "status";
    status.textContent = "正在查询转正定级试算...";
    rows.innerHTML = "";

    try {
        const result = await getJson(`/api/payroll/regularizations?${params}`);
        rows.innerHTML = (result.content || []).map(row => `
            <tr>
                <td>${escapeHtml(row.organizationCode)}</td>
                <td>${escapeHtml(row.personCode)}</td>
                <td>${escapeHtml(row.name)}</td>
                <td>${escapeHtml(row.calculationPeriod)}</td>
                <td>${escapeHtml(row.probationPositionCode || "")}</td>
                <td>${escapeHtml(row.probationPositionName || "")}</td>
                <td>${escapeHtml(row.educationCode || "")}</td>
                <td>${escapeHtml(row.educationName || "")}</td>
                <td>${escapeHtml(row.graduationDate || "")}</td>
                <td>${escapeHtml(row.regularPositionCode || "")}</td>
                <td>${escapeHtml(row.regularPositionName || "")}</td>
                <td>${escapeHtml(row.regularLevel || "")}</td>
                <td>${escapeHtml(row.regularStep || "")}</td>
                <td>${money(row.currentSalary)}</td>
                <td>${money(row.regularPositionSalary)}</td>
                <td>${money(row.regularBaseSalary)}</td>
                <td>${money(row.totalRegularSalary)}</td>
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

async function deleteJson(url) {
    const response = await fetch(url, { method: "DELETE", headers: { Accept: "application/json" } });
    if (!response.ok) {
        const text = await response.text();
        throw new Error(text || `HTTP ${response.status}`);
    }
    return null;
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
