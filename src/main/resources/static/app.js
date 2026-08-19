const state = {
    selectedPersonnel: null,
    currentUser: null,
    licenseStatus: null,
    menus: [],
    security: {
        activeTab: "users",
        users: [],
        userPage: null,
        userPageIndex: 0,
        roles: [],
        rolePage: null,
        rolePageIndex: 0,
        allRoles: [],
        permissions: [],
        organizations: [],
        organizationNodes: [],
        organizationExpandedCodes: new Set(),
        roleOrgSelectedCodes: new Set(),
        menus: [],
        menuPage: null,
        menuPageIndex: 0,
        menuDraft: [],
        menuExpandedIds: new Set(),
        auditLogs: [],
        auditPage: null,
        auditPageIndex: 0,
    },
    dictionaryFieldConfigs: {},
    dictionaryRows: [],
    activeDictionaryTarget: null,
    activeDictionaryNodes: [],
    dictionaryExpandedCodes: new Set(),
    organizationNodes: [],
    organizationExpandedCodes: new Set(),
    organizationMaintenanceNodes: [],
    organizationMaintenanceExpandedCodes: new Set(),
    organizationMaintenanceSelectedCode: "",
    organizationFieldOptions: null,
    activeOrganizationTarget: "maintenance",
    pendingPersonnelChange: null,
    activePersonnelMaintenance: null,
    maintPayrollHistories: [],
    activeSubrecordEditor: null,
    dataExchangeDispatchOrganizations: [],
    dataExchangeDispatchRows: [],
    dataExchangeSubmissionOrganizations: [],
    dataExchangeSubmissionRows: [],
    dataExchangeSubmissionPayrollTables: [],
    dataExchangeApprovalOrganizations: [],
    dataExchangeApprovalRows: [],
    dataExchangeApprovalPayrollTables: [],
    dataExchangeReceiveRows: [],
    dataExchangePersonnelRows: [],
    dataExchangePersonnelImportRows: [],
    assessmentBatchRows: [],
    assessmentBatchMeta: null,
    auditSummaryCache: null,
    auditPersonnelRows: [],
    auditPersonnelMeta: null,
    auditResultsByUid: {},
    auditSelectedUids: {},
    levelPromotionRowsById: {},
    levelPromotionListMeta: null,
    levelPromotionPage: 0,
    levelPromotionTotalPages: 1,
    normalPromotionRowsById: {},
    normalPromotionListMeta: null,
    normalPromotionPage: 0,
    normalPromotionTotalPages: 1,
    positionChangePage: 0,
    positionChangeTotalPages: 1,
    regularizationRowsById: {},
    educationPromotionRowsById: {},
    newPersonnelSalaryRowsByUid: {},
    basicSalaryStandardAdjustmentRowsById: {},
    basicSalaryStandardAdjustmentListMeta: null,
    basicSalaryStandardAdjustmentPage: 0,
    basicSalaryStandardAdjustmentTotalPages: 1,
    basicSalaryStandardAdjustmentMidChainExports: [],
    policeRankChangePage: 0,
    policeRankChangeTotalPages: 1,
    policeRankChangeMidChainExports: [],
    prosecutionRankChangePage: 0,
    prosecutionRankChangeTotalPages: 1,
    prosecutionRankChangeMidChainExports: [],
    judicialRankChangePage: 0,
    judicialRankChangeTotalPages: 1,
    judicialRankChangeMidChainExports: [],
    supervisionRankChangePage: 0,
    supervisionRankChangeTotalPages: 1,
    supervisionRankChangeMidChainExports: [],
    activePanelId: null,
    personnelPage: 0,
    personnelTotalPages: 1,
    personnelSort: "",
    personnelDirection: "asc",
    personnelStatisticsRows: [],
    personnelStatisticsPage: 0,
    personnelStatisticsTotalPages: 1,
    personnelMaintenanceReadonly: false,
    otherPayrollChangePage: 0,
    otherPayrollChangeTotalPages: 1,
    payrollHistoryPage: 0,
    payrollHistoryTotalPages: 1,
    payrollHistoryPageSize: 20,
    payrollChangeApprovalPage: 0,
    payrollChangeApprovalTotalPages: 1,
    payrollChangeApprovalTotalCount: 0,
    payrollChangeRegisterPage: 0,
    payrollChangeRegisterTotalPages: 1,
    payrollChangeRegisterTotalCount: 0,
    newPersonnelSalaryPage: 0,
    newPersonnelSalaryTotalPages: 1,
    regularizationHighGradePage: 0,
    regularizationHighGradeTotalPages: 1,
    retirementDueQueryPage: 0,
    retirementDueQueryTotalPages: 1,
    personnelComprehensiveQueryPage: 0,
    personnelComprehensiveQueryTotalPages: 1,
    positionHistoryPage: 0,
    positionHistoryTotalPages: 1,
    changedPersonnelPage: 0,
    changedPersonnelTotalPages: 1,
    educationHistoryPage: 0,
    educationHistoryTotalPages: 1,
    retirementProcessingPage: 0,
    retirementProcessingTotalPages: 1,
    retireePersonnelPage: 0,
    retireePersonnelTotalPages: 1,
    currentRetireeId: null,
    appDomain: null,
};

const RANK_CHANGE_MODULES = {
    police: {
        idPrefix: "police-rank-change",
        apiPrefix: "police-rank-change-promotions",
        moduleName: "警衔变化晋升",
        writePermission: "POLICE_RANK_CHANGE_PROMOTION_WRITE",
    },
    prosecution: {
        idPrefix: "prosecution-rank-change",
        apiPrefix: "prosecution-rank-change-promotions",
        moduleName: "检察官等级变化晋升",
        writePermission: "PROSECUTION_RANK_CHANGE_PROMOTION_WRITE",
    },
    judicial: {
        idPrefix: "judicial-rank-change",
        apiPrefix: "judicial-rank-change-promotions",
        moduleName: "法官等级变化晋升",
        writePermission: "JUDICIAL_RANK_CHANGE_PROMOTION_WRITE",
    },
    supervision: {
        idPrefix: "supervision-rank-change",
        apiPrefix: "supervision-rank-change-promotions",
        moduleName: "监察等级变化晋升",
        writePermission: "SUPERVISION_RANK_CHANGE_PROMOTION_WRITE",
    },
};

const PAYROLL_API_WRITE_PERMISSIONS = {
    "normal-promotions": "NORMAL_PROMOTION_WRITE",
    "level-promotions": "LEVEL_PROMOTION_WRITE",
    "position-change-promotions": "POSITION_CHANGE_PROMOTION_WRITE",
    "regularizations": "REGULARIZATION_WRITE",
    "new-personnel-salary-determinations": "NEW_PERSONNEL_SALARY_WRITE",
    "basic-salary-standard-adjustments": "BASIC_SALARY_STANDARD_ADJUSTMENT_WRITE",
    "education-promotions": "EDUCATION_PROMOTION_WRITE",
    "teaching-allowance-adjustments": "TEACHING_ALLOWANCE_ADJUSTMENT_WRITE",
    "floating-to-fixed-conversions": "FLOATING_TO_FIXED_WRITE",
    "other-payroll-changes": "OTHER_PAYROLL_CHANGE_WRITE",
    "regularization-high-grades": "REGULARIZATION_HIGH_GRADE_WRITE",
    "intern-salary-changes": "INTERN_SALARY_CHANGE_WRITE",
    "police-rank-change-promotions": "POLICE_RANK_CHANGE_PROMOTION_WRITE",
    "prosecution-rank-change-promotions": "PROSECUTION_RANK_CHANGE_PROMOTION_WRITE",
    "judicial-rank-change-promotions": "JUDICIAL_RANK_CHANGE_PROMOTION_WRITE",
    "supervision-rank-change-promotions": "SUPERVISION_RANK_CHANGE_PROMOTION_WRITE",
};

const PAYROLL_FEATURE_WRITE_UI = [
    {
        writePermission: "NORMAL_PROMOTION_WRITE",
        batchButtonIds: ["normal-promotion-batch-apply", "normal-promotion-apply-all", "normal-promotion-rollback-all"],
        tableSelector: ".normal-promotion-table .col-select",
        selectAllId: "normal-promotion-select-all",
    },
    {
        writePermission: "LEVEL_PROMOTION_WRITE",
        batchButtonIds: ["level-promotion-batch-apply", "level-promotion-apply-all", "level-promotion-batch-rollback", "level-promotion-rollback-all"],
        tableSelector: ".level-promotion-table .col-select",
        selectAllId: "level-promotion-select-all",
    },
    {
        writePermission: "POSITION_CHANGE_PROMOTION_WRITE",
        batchButtonIds: ["position-change-batch-apply", "position-change-batch-rollback"],
        tableSelector: "#position-change .col-select",
        selectAllId: "position-change-select-all",
    },
    {
        writePermission: "REGULARIZATION_WRITE",
        batchButtonIds: ["regularization-batch-apply", "regularization-batch-rollback"],
        tableSelector: "#regularization .col-select",
        selectAllId: "regularization-select-all",
    },
    {
        writePermission: "NEW_PERSONNEL_SALARY_WRITE",
        batchButtonIds: ["new-personnel-salary-batch-apply", "new-personnel-salary-batch-rollback"],
        tableSelector: "#new-personnel-salary .col-select",
        selectAllId: "new-personnel-salary-select-all",
    },
    {
        writePermission: "BASIC_SALARY_STANDARD_ADJUSTMENT_WRITE",
        batchButtonIds: [
            "basic-salary-standard-adjustment-batch-apply",
            "basic-salary-standard-adjustment-apply-all",
            "basic-salary-standard-adjustment-batch-rollback",
            "basic-salary-standard-adjustment-rollback-all",
            "basic-salary-standard-adjustment-export-mid-chain",
        ],
        tableSelector: ".basic-salary-standard-adjustment-table .col-select",
        selectAllId: "basic-salary-standard-adjustment-select-all",
    },
];

const personnelChangeTypes = [
    { type: "退休", description: "退休" },
    { type: "调动", description: "调往本地其他单位（仍在职，写入调动履历）" },
    { type: "调出", description: "调往外地（转入变动人员）" },
    { type: "辞职", description: "辞职" },
    { type: "辞退", description: "辞退" },
    { type: "开除", description: "开除" },
    { type: "死亡", description: "死亡" },
];

const assessmentResultOptions = {
    administrative: [
        "优秀",
        "称职",
        "基本称职",
        "不称职",
        "暂缓确定",
        "未定等次(试用期)",
        "未定等次(处分期)",
        "未定等次(其它)",
        "未参加考核",
        "未考核(中断年限)",
    ],
    institution: [
        "优秀",
        "合格",
        "基本合格",
        "不合格",
        "暂缓确定",
        "未定等次(见习期)",
        "未定等次(处分期)",
        "未定等次(其它)",
        "未参加考核",
        "未考核(中断年限)",
    ],
};

const positionDictionaryFallbacks = {
    xrzw: "051",
    xrzwbm: "051",
    zwjb: "026",
    zjbm: "058",
    xzzw: "051",
    zwbm: "051",
};

const dictionaryFilterFields = new Set([
    "ryfl", "gwfl", "xrzw", "zwjb", "xzzw", "zjdj", "zwgw1", "zwgw2",
]);

const subrecordEditors = {
    education: {
        title: "学历信息",
        endpoint: uid => `/api/personnel/${uid}/education`,
        fields: [
            ["educationCode", "学历编码", "text", { readonly: true }],
            ["educationName", "学历", "text", { dictionaryPrefixField: "zgxl", dictionaryPrefix: "002", linkedCodeField: "educationCode" }],
            ["school", "学校"],
            ["enrollmentDate", "入学时间", "month"], ["graduationDate", "毕业时间", "month"],
            ["studyYears", "学制", "number"],
            ["educationType", "学历类别", "select", { optionsProvider: "educationTypes" }],
            ["remark", "备注"],
        ],
    },
    position: {
        title: "职务变化信息",
        wideModal: true,
        modalClass: "position-subrecord-modal",
        endpoint: uid => `/api/personnel/${uid}/positions`,
        fields: [
            ["currentPositionCode", "现任职务编码", "text", { hidden: true }],
            ["currentPosition", "现任职务", "text", {
                dictionaryPrefixField: "xrzw",
                dictionaryPrefix: positionDictionaryFallbacks.xrzw,
                linkedCodeField: "currentPositionCode",
                useFullDictionaryCode: true,
                codeMaxLength: 4,
            }],
            ["positionLevel", "现任职务层次", "text", {
                dictionaryPrefixField: "zwjb",
                dictionaryPrefix: positionDictionaryFallbacks.zwjb,
            }],
            ["positionCode", "执行工资职务编码", "text", { hidden: true }],
            ["positionName", "执行工资职务层次", "text", {
                dictionaryPrefixField: "xzzw",
                dictionaryPrefix: positionDictionaryFallbacks.xzzw,
                linkedCodeField: "positionCode",
                useFullDictionaryCode: true,
                codeMaxLength: 4,
            }],
            ["startYearMonth", "任职时间", "month"],
            ["intervalYears", "扣减年限", "number"],
            ["activeFlag", "现任职", "select", { optionsProvider: "activeFlags" }],
        ],
    },
    assessment: {
        title: "年度考核信息",
        endpoint: uid => `/api/personnel/${uid}/assessments`,
        fields: [
            ["year", "年度"],
            ["result", "考核结果", "select", { optionsProvider: "assessmentResults" }],
        ],
    },
    payroll: {
        title: "历次调资信息",
        wideModal: true,
        modalClass: "payroll-history-modal",
        endpoint: uid => `/api/payroll/personnel/${uid}/histories`,
        updateEndpoint: id => `/api/payroll/histories/${id}`,
        sections: [
            {
                title: "基本情况",
                fields: [
                    ["calculationYear", "年度"],
                    ["calculationMonth", "月份"],
                    ["changeType", "变动类别"],
                    ["positionCode", "岗位编码"],
                    ["positionName", "岗位名称"],
                ],
            },
            {
                title: "执行信息",
                fields: [
                    ["gradeSalaryLevel", "级别"],
                    ["positionSalaryGrade", "档次"],
                    ["salaryStandardYearMonth", "执行工资标准"],
                    ["allowanceStandardYearMonth", "执行津补贴标准"],
                    ["teachingIncreaseRatio", "教护提高比例"],
                    ["rankName", "警衔、法检、监察等级"],
                    ["rankAllowanceStandardYearMonth", "警衔法检监察津贴标准"],
                    ["floatingStep", "浮动档次"],
                ],
            },
            {
                title: "工资项目",
                fields: [
                    ["positionSalary", "职务(岗位)工资", "number"],
                    ["gradeSalary", "级别工资", "number"],
                    ["technicalGradeSalary", "技术等级工资", "number"],
                    ["internSalary", "试用期工资", "number"],
                    ["salaryIncrease", "教护提高部分", "number"],
                    ["teachingAllowance", "教护龄津贴", "number"],
                    ["floatingSalary", "农林水一线浮动工资", "number"],
                    ["subsidyAllowance", "工作性津贴", "number"],
                    ["performanceAllowance", "生活性补贴", "number"],
                    ["payGradeRetention", "保留职务工资", "number"],
                    ["rankAllowance", "警衔、法检、监察津贴", "number"],
                    ["retainedReformAllowance", "工改保留津贴", "number"],
                    ["overtimeAllowance", "加班补贴", "number"],
                    ["retainedSpecialPostAllowance", "特岗保留部分", "number"],
                    ["hygieneAllowance", "岗位津贴", "number"],
                    ["retainedAllowance", "保留副补", "number"],
                    ["bonusBalance", "保留奖金", "number"],
                    ["specialPostAllowance", "特殊岗位津贴", "number"],
                    ["otherAllowance", "其它补贴", "number"],
                    ["yearAllowance", "农村学校教师补贴", "number"],
                    ["totalAmount", "月工资合计", "number"],
                ],
            },
        ],
    },
};

const yuanFormatter = new Intl.NumberFormat("zh-CN", {
    minimumFractionDigits: 0,
    maximumFractionDigits: 2,
});

const rankAllowanceMenuCodes = [
    "POLICE_ALLOWANCE_ADJUSTMENT",
    "PROSECUTION_ALLOWANCE_ADJUSTMENT",
    "JUDICIAL_ALLOWANCE_ADJUSTMENT",
    "SUPERVISION_ALLOWANCE_ADJUSTMENT",
    "POLICE_RANK_CHANGE_PROMOTION",
    "PROSECUTION_RANK_CHANGE_PROMOTION",
    "JUDICIAL_RANK_CHANGE_PROMOTION",
    "SUPERVISION_RANK_CHANGE_PROMOTION",
];

const standardAdjustmentMenuCodes = [
    "BASIC_SALARY_STANDARD_ADJUSTMENT",
    "CIVIL_ALLOWANCE_STANDARD_ADJUSTMENT",
    "PERFORMANCE_STANDARD_ADJUSTMENT",
    "PERFORMANCE_RATIO_ADJUSTMENT",
];

const DOMAIN_STORAGE_KEY = "rsgzgl.appDomain";
const DOMAIN_ACTIVE = "active";
const DOMAIN_RETIREMENT = "retirement";

const activeMenuGroups = [
    { title: "工作台", codes: ["DASHBOARD"] },
    {
        title: "信息维护",
        codes: [
            "PERSONNEL", "PERSONNEL_STATISTICS", "RETIREMENT_DUE_QUERY", "PERSONNEL_COMPREHENSIVE_QUERY",
            "ANNUAL_ASSESSMENT_MANAGEMENT", "CHANGED_PERSONNEL",
            "POSITION_HISTORY", "EDUCATION_HISTORY", "ORGANIZATION_MAINTENANCE",
        ],
    },
    {
        title: "工资变动",
        sections: [
            {
                codes: [
                    "LEVEL_PROMOTION",
                    "NORMAL_PROMOTION",
                    "POSITION_CHANGE_PROMOTION",
                    "NEW_PERSONNEL_SALARY",
                    "REGULARIZATION",
                    "EDUCATION_PROMOTION",
                    "TEACHING_ALLOWANCE_ADJUSTMENT",
                    "FLOATING_TO_FIXED",
                    "OTHER_PAYROLL_CHANGE",
                    "REGULARIZATION_HIGH_GRADE",
                    "WAGE_REFORM_2006",
                    "PAYROLL_HISTORY",
                    "AUDIT",
                ],
            },
            { title: "调整标准", codes: standardAdjustmentMenuCodes },
            { title: "警衔法检监", codes: rankAllowanceMenuCodes },
        ],
    },
    {
        title: "养老接口",
        codes: ["MONTHLY_AVERAGE_SALARY"],
    },
    {
        title: "报表打印",
        codes: [
            "PAYROLL_CHANGE_REGISTER_REPORT", "PAYROLL_CHANGE_APPROVAL_REPORT",
            "WAGE_REFORM_2006_PUBLIC_NOTICE_REPORT", "PERSONNEL_INFORMATION_COLLECTION_REPORT",
            "PERSONNEL_INFORMATION_REGISTRATION_REPORT", "DATA_EXCHANGE",
        ],
    },
    {
        title: "标准维护",
        codes: [
            "BASIC_STANDARDS", "INTERN_SALARY_STANDARDS", "ALLOWANCE_STANDARDS",
            "RANK_ALLOWANCE_STANDARDS",
            "RETAINED_ALLOWANCE_STANDARDS", "RURAL_TEACHER_ALLOWANCE_STANDARDS",
            "WAGE_REFORM_STANDARDS", "OTHER_ALLOWANCE_STANDARDS",
        ],
    },
];

/** 跨业务域公共菜单：未选域 / 在职 / 离退均可进入 */
const commonMenuGroups = [
    {
        title: "系统管理",
        codes: [
            "LOCAL_POLICY_CONFIG", "DICTIONARY_MAINTENANCE", "SECURITY",
            "OPERATION_LOG", "DATA_MAINTENANCE", "SYSTEM_HELP", "SYSTEM_SETUP", "LICENSE_IMPORT",
        ],
    },
];

const retirementMenuGroups = [
    { title: "工作台", codes: ["DASHBOARD"] },
    {
        title: "信息维护",
        codes: ["RETIREE_PERSONNEL"],
    },
    {
        title: "政策标准",
        codes: ["RETIREMENT_RATIO_STANDARDS"],
    },
    {
        title: "报表打印",
        codes: ["RETIREMENT_APPROVAL_REPORT"],
    },
    {
        title: "数据交换",
        codes: ["RETIREMENT_DATA_EXCHANGE"],
    },
];

/** @deprecated use menuGroupsForDomain */
const menuGroups = activeMenuGroups;

function menuGroupsForDomain(domain) {
    return domain === DOMAIN_RETIREMENT ? retirementMenuGroups : activeMenuGroups;
}

function currentMenuGroups() {
    return menuGroupsForDomain(state.appDomain || DOMAIN_ACTIVE);
}

function visibleMenuGroups() {
    const domainGroups = state.appDomain
        ? menuGroupsForDomain(state.appDomain)
        : [{ title: "工作台", codes: ["DASHBOARD"] }];
    return [...domainGroups, ...commonMenuGroups];
}

function isCommonMenuCode(code) {
    return commonMenuGroups.some(group => menuGroupCodes(group).includes(code));
}

function commonSystemMenuGroup() {
    return commonMenuGroups.find(group => group.title === "系统管理") || commonMenuGroups[0];
}

function readStoredAppDomain() {
    try {
        const value = sessionStorage.getItem(DOMAIN_STORAGE_KEY);
        if (value === DOMAIN_ACTIVE || value === DOMAIN_RETIREMENT) {
            return value;
        }
    } catch (_error) {
        // ignore
    }
    return null;
}

function writeStoredAppDomain(domain) {
    try {
        if (domain) {
            sessionStorage.setItem(DOMAIN_STORAGE_KEY, domain);
        } else {
            sessionStorage.removeItem(DOMAIN_STORAGE_KEY);
        }
    } catch (_error) {
        // ignore
    }
}

function domainLabel(domain) {
    return domain === DOMAIN_RETIREMENT ? "离退休管理" : "在职人事工资";
}

function setAppDomain(domain, options = {}) {
    const navigateHome = options.navigateHome !== false;
    state.appDomain = domain === DOMAIN_RETIREMENT ? DOMAIN_RETIREMENT : domain === DOMAIN_ACTIVE ? DOMAIN_ACTIVE : null;
    writeStoredAppDomain(state.appDomain);
    updateDomainChrome();
    renderMenus();
    renderDashboard();
    if (navigateHome) {
        location.hash = "#dashboard";
        showPanel();
    }
}

function clearAppDomain() {
    setAppDomain(null, { navigateHome: true });
}

function ensureDomainForMenuCode(code) {
    if (!code || code === "DASHBOARD" || isCommonMenuCode(code)) {
        return;
    }
    const inRetirement = retirementMenuGroups.some(group => menuGroupCodes(group).includes(code));
    const inActive = activeMenuGroups.some(group => menuGroupCodes(group).includes(code));
    if (inRetirement && !inActive && state.appDomain !== DOMAIN_RETIREMENT) {
        state.appDomain = DOMAIN_RETIREMENT;
        writeStoredAppDomain(DOMAIN_RETIREMENT);
        updateDomainChrome();
        renderMenus();
    } else if (inActive && !inRetirement && state.appDomain !== DOMAIN_ACTIVE) {
        state.appDomain = DOMAIN_ACTIVE;
        writeStoredAppDomain(DOMAIN_ACTIVE);
        updateDomainChrome();
        renderMenus();
    }
}

function updateDomainChrome() {
    const badge = document.getElementById("domain-badge");
    const switchBtn = document.getElementById("domain-switch-button");
    const brand = document.getElementById("brand-subtitle");
    const gate = document.getElementById("domain-gate");
    const home = document.getElementById("domain-home");
    const domain = state.appDomain;
    if (badge) {
        badge.classList.toggle("hidden", !domain);
        badge.textContent = domain ? domainLabel(domain) : "";
    }
    if (switchBtn) {
        switchBtn.classList.toggle("hidden", !domain);
    }
    if (brand) {
        brand.textContent = domain
            ? `VFP 迁移版 · ${domainLabel(domain)}`
            : "VFP 迁移版 · Spring Boot";
    }
    document.body.classList.toggle("domain-active", domain === DOMAIN_ACTIVE);
    document.body.classList.toggle("domain-retirement", domain === DOMAIN_RETIREMENT);
    document.body.classList.toggle("domain-unset", !domain);
    if (gate && home) {
        gate.classList.toggle("hidden", Boolean(domain));
        home.classList.toggle("hidden", !domain);
    }
}

function menuGroupCodes(group) {
    if (group.sections?.length) {
        return group.sections.flatMap(section => section.codes);
    }
    return group.codes || [];
}

function findMenuGroupByCode(code) {
    const groups = [
        ...commonMenuGroups,
        ...activeMenuGroups,
        ...retirementMenuGroups,
    ];
    return groups.find(group => menuGroupCodes(group).includes(code));
}

function renderMenuLinks(codes, menuByCode) {
    const fallbackIndex = new Map(codes.map((code, index) => [code, index]));
    return codes
        .map(menuCode => menuByCode.get(menuCode))
        .filter(Boolean)
        .sort((left, right) => {
            const leftOrder = left.sortOrder;
            const rightOrder = right.sortOrder;
            if (leftOrder != null && rightOrder != null && leftOrder !== rightOrder) {
                return leftOrder - rightOrder;
            }
            return (fallbackIndex.get(left.code) ?? 0) - (fallbackIndex.get(right.code) ?? 0);
        })
        .map(menu => `
            <a href="${escapeHtml(menu.path)}" data-menu-link="${escapeHtml(menu.code)}">${escapeHtml(menu.title)}</a>
        `).join("");
}

function renderMenuGroupContent(group, menuByCode) {
    if (group.sections?.length) {
        return group.sections.map(section => {
            const links = renderMenuLinks(section.codes, menuByCode);
            if (!links) {
                return "";
            }
            const title = section.title
                ? `<div class="nav-subgroup-title">${escapeHtml(section.title)}</div>`
                : "";
            return `${title}${links}`;
        }).join("");
    }
    return renderMenuLinks(group.codes || [], menuByCode);
}

const dashboardQuickActions = [
    { code: "PERSONNEL", label: "人员管理", desc: "查询、维护及附属信息管理", domain: DOMAIN_ACTIVE },
    { code: "PAYROLL_HISTORY", label: "工资变动历史", desc: "查看历次调资与变动记录", domain: DOMAIN_ACTIVE },
    { code: "POSITION_CHANGE_PROMOTION", label: "职务变化晋升", desc: "筛选待处理职务变动人员", domain: DOMAIN_ACTIVE },
    { code: "AUDIT", label: "工资推算对账", desc: "批量重放推算并比对差异", domain: DOMAIN_ACTIVE },
    { code: "PAYROLL_CHANGE_REGISTER_REPORT", label: "工资变动花名册", desc: "生成与打印变动花名册", domain: DOMAIN_ACTIVE },
    { code: "BASIC_STANDARDS", label: "基本工资标准", desc: "查询职务与级别工资标准", domain: DOMAIN_ACTIVE },
    { code: "SECURITY", label: "权限管理", desc: "维护用户、角色与单位范围" },
    { code: "LICENSE_IMPORT", label: "单位授权", desc: "查看签约主体并导入授权包" },
    { code: "RETIREE_PERSONNEL", label: "离退休人员", desc: "待办办理、维护与审批通过", domain: DOMAIN_RETIREMENT },
    { code: "RETIREMENT_APPROVAL_REPORT", label: "退休审批表", desc: "2006/2021/2025 三样式打印", domain: DOMAIN_RETIREMENT },
    { code: "RETIREMENT_DATA_EXCHANGE", label: "离退数据交换", desc: "离退专用上下级交换", domain: DOMAIN_RETIREMENT },
];

const menuDescriptions = {
    RETIREMENT_DUE_QUERY: "已达退休年龄人员查询",
    RETIREMENT_PROCESSING: "已并入离退休人员",
    RETIREE_PERSONNEL: "待办办理 / 主档维护 / 审批通过",
    RETIREMENT_RATIO_STANDARDS: "套改后折算比例 zsbl06",
    RETIREMENT_APPROVAL_REPORT: "退休审批表（2006/2021/2025）",
    RETIREMENT_DATA_EXCHANGE: "离退专用数据交换",
    PERSONNEL: "查询在册人员，支持工资试算与信息维护",
    PERSONNEL_STATISTICS: "人员基本情况与工资变动统计",
    ANNUAL_ASSESSMENT_MANAGEMENT: "考核结果查询录入与汇总统计",
    CHANGED_PERSONNEL: "办理变动人员恢复与查询",
    POSITION_HISTORY: "任职变化历史查询",
    EDUCATION_HISTORY: "学历变化历史查询",
    ORGANIZATION_MAINTENANCE: "单位树维护与调整",
    PAYROLL: "工资项对账说明与规则入口",
    PAYROLL_HISTORY: "hisbase 工资变动历史",
    TEACHING_ALLOWANCE_ADJUSTMENT: "教护龄津贴调整试算",
    POLICE_ALLOWANCE_ADJUSTMENT: "警衔津贴调整试算",
    PROSECUTION_ALLOWANCE_ADJUSTMENT: "检察津贴调整试算",
    JUDICIAL_ALLOWANCE_ADJUSTMENT: "审判津贴调整试算",
    SUPERVISION_ALLOWANCE_ADJUSTMENT: "监察津贴调整试算",
    POLICE_RANK_CHANGE_PROMOTION: "警衔变化晋升试算与处理",
    PROSECUTION_RANK_CHANGE_PROMOTION: "检察官等级变化晋升",
    JUDICIAL_RANK_CHANGE_PROMOTION: "法官等级变化晋升",
    SUPERVISION_RANK_CHANGE_PROMOTION: "监察等级变化晋升",
    FLOATING_TO_FIXED: "浮动固定试算",
    NEW_PERSONNEL_SALARY: "新进定资试算",
    OTHER_PAYROLL_CHANGE: "其它情况工资变动试算",
    REGULARIZATION_HIGH_GRADE: "转正高定档次薪级试算",
    MONTHLY_AVERAGE_SALARY: "月平均工资试算",
    WAGE_REFORM_2006: "2006年工资套改试算",
    INTERN_SALARY_CHANGE: "见习工资变动试算",
    SALARY_STANDARD_ADJUSTMENT: "2024.07 调标试算",
    BASIC_SALARY_STANDARD_ADJUSTMENT: "调整基本工资标准试算",
    PERFORMANCE_RATIO_ADJUSTMENT: "调整绩效比例试算",
    NORMAL_PROMOTION: "正常档次/薪级晋升试算",
    LEVEL_PROMOTION: "级别晋升（含套改级别滚动）试算",
    POSITION_CHANGE_PROMOTION: "职务变化晋升试算与处理",
    EDUCATION_PROMOTION: "学历晋升定级试算",
    REGULARIZATION: "见习人员转正定级试算",
    AUDIT: "逐人工资推算对账与导出",
    BASIC_STANDARDS: "职务工资与级别工资标准",
    ALLOWANCE_STANDARDS: "津补贴标准维护",
    INTERN_SALARY_STANDARDS: "见习期工资标准",
    RANK_ALLOWANCE_STANDARDS: "警衔/检察/审判/监察津贴标准",
    RETAINED_ALLOWANCE_STANDARDS: "保留福补标准",
    YEAR_ALLOWANCE_STANDARDS: "年终一次性奖金标准",
    WAGE_REFORM_STANDARDS: "2006 套改对照标准",
    OTHER_ALLOWANCE_STANDARDS: "其他津补贴标准",
    PAYROLL_CHANGE_REGISTER_REPORT: "工资变动花名册报表",
    PAYROLL_CHANGE_APPROVAL_REPORT: "工资变动审批表报表",
    DATA_EXCHANGE: "与外部系统数据交换",
    LOCAL_POLICY_CONFIG: "本地工资政策参数",
    DICTIONARY_MAINTENANCE: "代码字典维护",
    SECURITY: "用户、角色与权限管理",
};

document.addEventListener("DOMContentLoaded", () => {
    // Personnel maintenance overlays live inside #personnel in HTML; mount to body so they
    // remain visible when opened from other panels (e.g. 变动人员信息 → 查看).
    mountModalOverlaysToBody([
        "personnel-maintenance-modal",
        "dictionary-picker-modal",
        "subrecord-editor-modal",
        "personnel-change-remark-modal",
        "missing-assessment-fill-modal",
    ]);
    if (window.MonthPicker) {
        MonthPicker.enhanceAll(document);
    }
    syncBasicSalaryStandardAdjustmentTableHeader();
    const currentYear = String(new Date().getFullYear());
    ["report-payroll-change-year", "report-approval-year"].forEach(id => {
        const input = document.getElementById(id);
        if (input) {
            input.value = currentYear;
        }
    });
    fillPersonnelStatisticsYearOptions();
    bindPersonnelStatisticsYearClickDefault();
    initializeAuth();
    initGradeStandardStepsGrid();
    document.getElementById("personnel-search").addEventListener("submit", onPersonnelSearch);
    document.getElementById("page-size").addEventListener("change", () => { state.personnelPage = 0; loadPersonnel(); });
    document.getElementById("personnel-first").addEventListener("click", () => gotoPersonnelPage(0));
    document.getElementById("personnel-prev").addEventListener("click", () => gotoPersonnelPage(state.personnelPage - 1));
    document.getElementById("personnel-next").addEventListener("click", () => gotoPersonnelPage(state.personnelPage + 1));
    document.getElementById("personnel-last").addEventListener("click", () => gotoPersonnelPage(state.personnelTotalPages - 1));
    document.getElementById("personnel-page-input").addEventListener("change", event => {
        const value = parseInt(event.target.value, 10);
        if (!Number.isNaN(value)) {
            gotoPersonnelPage(value - 1);
        }
    });
    document.querySelectorAll(".personnel-table th.sortable").forEach(th => {
        th.addEventListener("click", () => onPersonnelSort(th.dataset.sort));
    });
    document.getElementById("personnel-maintenance-form").addEventListener("submit", onPersonnelMaintenanceSave);
    document.getElementById("maint-wage-projection-form").addEventListener("submit", onPersonnelWageProjectionSearch);
    document.getElementById("personnel-maintenance-reset").addEventListener("click", resetPersonnelMaintenanceForm);
    document.getElementById("personnel-maintenance-new").addEventListener("click", openNewPersonnelMaintenance);
    document.getElementById("personnel-maintenance-close").addEventListener("click", closePersonnelMaintenanceModal);
    document.getElementById("subrecord-editor-close").addEventListener("click", closeSubrecordEditor);
    document.getElementById("subrecord-editor-form").addEventListener("submit", onSubrecordSave);
    document.getElementById("add-education-record").addEventListener("click", () => openSubrecordEditor("education"));
    document.getElementById("add-position-record").addEventListener("click", () => openSubrecordEditor("position"));
    document.getElementById("add-payroll-record").addEventListener("click", () => openSubrecordEditor("payroll"));
    document.getElementById("add-assessment-record").addEventListener("click", () => openSubrecordEditor("assessment"));
    document.getElementById("auto-fill-missing-assessments").addEventListener("click", autoFillMissingAssessments);
    document.getElementById("dictionary-picker-close").addEventListener("click", closeDictionaryPicker);
    document.getElementById("organization-picker-close").addEventListener("click", closeOrganizationPicker);
    document.getElementById("organization-picker-filter").addEventListener("input", renderOrganizationPickerTree);
    initializeOrganizationPickerInput();
    initializeSidebar();
    document.querySelectorAll("[data-personnel-tab]").forEach(button => {
        button.addEventListener("click", () => showPersonnelTab(button.dataset.personnelTab));
    });
    document.getElementById("annual-assessment-batch-form").addEventListener("submit", onAssessmentBatchSearch);
    document.getElementById("assessment-batch-year")?.addEventListener("change", () => {
        void loadAssessmentBatch();
    });
    document.getElementById("assessment-batch-result-filter")?.addEventListener("change", renderAssessmentBatchRows);
    document.getElementById("assessment-batch-apply-selected").addEventListener("click", applyAssessmentBatchBulkResult);
    document.getElementById("assessment-batch-fill-defaults").addEventListener("click", fillAssessmentBatchDefaults);
    document.getElementById("assessment-batch-save").addEventListener("click", saveAssessmentBatch);
    document.getElementById("assessment-batch-select-all").addEventListener("change", event => {
        document.querySelectorAll("[data-assessment-batch-select]").forEach(checkbox => {
            if (!checkbox.closest("tr")?.classList.contains("hidden-row")) {
                checkbox.checked = event.target.checked;
            }
        });
    });
    document.getElementById("assessment-summary-form").addEventListener("submit", onAssessmentSummarySearch);
    document.getElementById("assessment-summary-year")?.addEventListener("change", () => {
        void loadAssessmentSummary();
    });
    document.getElementById("assessment-tab-entry").addEventListener("click", () => switchAssessmentTab("entry"));
    document.getElementById("assessment-tab-summary").addEventListener("click", () => switchAssessmentTab("summary"));
    document.getElementById("changed-personnel-form").addEventListener("submit", onChangedPersonnelSearch);
    document.getElementById("changed-personnel-first")?.addEventListener("click", () => gotoChangedPersonnelPage(0));
    document.getElementById("changed-personnel-prev")?.addEventListener("click", () => gotoChangedPersonnelPage(state.changedPersonnelPage - 1));
    document.getElementById("changed-personnel-next")?.addEventListener("click", () => gotoChangedPersonnelPage(state.changedPersonnelPage + 1));
    document.getElementById("changed-personnel-last")?.addEventListener("click", () => gotoChangedPersonnelPage(state.changedPersonnelTotalPages - 1));
    document.getElementById("changed-personnel-page-input")?.addEventListener("change", event => {
        const value = parseInt(event.target.value, 10);
        if (!Number.isNaN(value)) {
            gotoChangedPersonnelPage(value - 1);
        }
    });
    document.getElementById("position-history-form").addEventListener("submit", onPositionHistorySearch);
    document.getElementById("position-history-first")?.addEventListener("click", () => gotoPositionHistoryPage(0));
    document.getElementById("position-history-prev")?.addEventListener("click", () => gotoPositionHistoryPage(state.positionHistoryPage - 1));
    document.getElementById("position-history-next")?.addEventListener("click", () => gotoPositionHistoryPage(state.positionHistoryPage + 1));
    document.getElementById("position-history-last")?.addEventListener("click", () => gotoPositionHistoryPage(state.positionHistoryTotalPages - 1));
    document.getElementById("position-history-page-input")?.addEventListener("change", event => {
        const value = parseInt(event.target.value, 10);
        if (!Number.isNaN(value)) {
            gotoPositionHistoryPage(value - 1);
        }
    });
    document.getElementById("education-history-form").addEventListener("submit", onEducationHistorySearch);
    document.getElementById("education-history-first")?.addEventListener("click", () => gotoEducationHistoryPage(0));
    document.getElementById("education-history-prev")?.addEventListener("click", () => gotoEducationHistoryPage(state.educationHistoryPage - 1));
    document.getElementById("education-history-next")?.addEventListener("click", () => gotoEducationHistoryPage(state.educationHistoryPage + 1));
    document.getElementById("education-history-last")?.addEventListener("click", () => gotoEducationHistoryPage(state.educationHistoryTotalPages - 1));
    document.getElementById("education-history-page-input")?.addEventListener("change", event => {
        const value = parseInt(event.target.value, 10);
        if (!Number.isNaN(value)) {
            gotoEducationHistoryPage(value - 1);
        }
    });
    document.getElementById("organization-maintenance-form").addEventListener("submit", onOrganizationMaintenanceSearch);
    document.getElementById("organization-detail-form")?.addEventListener("submit", onOrganizationDetailSave);
    document.getElementById("organization-add-root-button")?.addEventListener("click", onOrganizationAddRoot);
    document.getElementById("organization-add-child-button")?.addEventListener("click", onOrganizationAddChild);
    document.getElementById("organization-delete-button")?.addEventListener("click", onOrganizationDelete);
    document.getElementById("organization-detail-reset")?.addEventListener("click", () => clearOrganizationDetailForm({ keepSelection: false }));
    document.getElementById("personnel-statistics-form").addEventListener("submit", onPersonnelStatisticsSearch);
    document.getElementById("personnel-comprehensive-query-form")?.addEventListener("submit", onPersonnelComprehensiveQuerySearch);
    document.getElementById("personnel-comprehensive-query-size")?.addEventListener("change", () => {
        state.personnelComprehensiveQueryPage = 0;
        const pageInput = document.getElementById("personnel-comprehensive-query-page");
        if (pageInput) {
            pageInput.value = "0";
        }
        void loadPersonnelComprehensiveQueries();
    });
    document.getElementById("personnel-comprehensive-query-first")?.addEventListener("click", () => gotoPersonnelComprehensiveQueryPage(0));
    document.getElementById("personnel-comprehensive-query-prev")?.addEventListener("click", () => gotoPersonnelComprehensiveQueryPage(state.personnelComprehensiveQueryPage - 1));
    document.getElementById("personnel-comprehensive-query-next")?.addEventListener("click", () => gotoPersonnelComprehensiveQueryPage(state.personnelComprehensiveQueryPage + 1));
    document.getElementById("personnel-comprehensive-query-last")?.addEventListener("click", () => gotoPersonnelComprehensiveQueryPage(state.personnelComprehensiveQueryTotalPages - 1));
    document.getElementById("personnel-comprehensive-query-page-input")?.addEventListener("change", event => {
        const value = parseInt(event.target.value, 10);
        if (!Number.isNaN(value)) {
            gotoPersonnelComprehensiveQueryPage(value - 1);
        }
    });
    document.getElementById("retirement-due-query-form")?.addEventListener("submit", onRetirementDueQuerySearch);
    document.getElementById("retirement-due-query-size")?.addEventListener("change", () => {
        state.retirementDueQueryPage = 0;
        void loadRetirementDuePersonnel();
    });
    document.getElementById("retirement-due-query-first")?.addEventListener("click", () => gotoRetirementDueQueryPage(0));
    document.getElementById("retirement-due-query-prev")?.addEventListener("click", () => gotoRetirementDueQueryPage(state.retirementDueQueryPage - 1));
    document.getElementById("retirement-due-query-next")?.addEventListener("click", () => gotoRetirementDueQueryPage(state.retirementDueQueryPage + 1));
    document.getElementById("retirement-due-query-last")?.addEventListener("click", () => gotoRetirementDueQueryPage(state.retirementDueQueryTotalPages - 1));
    document.getElementById("retirement-due-query-page-input")?.addEventListener("change", event => {
        const value = parseInt(event.target.value, 10);
        if (!Number.isNaN(value)) {
            gotoRetirementDueQueryPage(value - 1);
        }
    });
    document.getElementById("personnel-statistics-page-size").addEventListener("change", () => {
        state.personnelStatisticsPage = 0;
        renderPersonnelStatisticsPage();
    });
    document.getElementById("personnel-statistics-first").addEventListener("click", () => gotoPersonnelStatisticsPage(0));
    document.getElementById("personnel-statistics-prev").addEventListener("click", () => gotoPersonnelStatisticsPage(state.personnelStatisticsPage - 1));
    document.getElementById("personnel-statistics-next").addEventListener("click", () => gotoPersonnelStatisticsPage(state.personnelStatisticsPage + 1));
    document.getElementById("personnel-statistics-last").addEventListener("click", () => gotoPersonnelStatisticsPage(state.personnelStatisticsTotalPages - 1));
    document.getElementById("personnel-statistics-page-input").addEventListener("change", event => {
        const value = parseInt(event.target.value, 10);
        if (!Number.isNaN(value)) {
            gotoPersonnelStatisticsPage(value - 1);
        }
    });
    ["personnel-statistics-metric-change-count", "personnel-statistics-metric-personnel-count"].forEach(id => {
        document.getElementById(id)?.addEventListener("change", () => renderPersonnelStatisticsPage());
    });
    document.getElementById("dictionary-maintenance-form").addEventListener("submit", onDictionarySearch);
    document.getElementById("dictionary-category").addEventListener("change", () => {
        void loadDictionaries();
    });
    document.getElementById("dictionary-add-category-button")?.addEventListener("click", () => openDictionaryMaintenanceModal("create-category"));
    document.getElementById("dictionary-add-option-button")?.addEventListener("click", () => openDictionaryMaintenanceModal("create-option"));
    document.getElementById("dictionary-maintenance-modal-form")?.addEventListener("submit", onDictionaryMaintenanceModalSubmit);
    document.getElementById("dictionary-maintenance-modal-close")?.addEventListener("click", closeDictionaryMaintenanceModal);
    document.getElementById("dictionary-maintenance-modal-cancel")?.addEventListener("click", closeDictionaryMaintenanceModal);
    document.getElementById("dictionary-maintenance-modal-disable")?.addEventListener("click", onDictionaryMaintenanceDisable);
    document.getElementById("dictionary-maintenance-modal")?.addEventListener("click", event => {
        if (event.target.id === "dictionary-maintenance-modal") {
            closeDictionaryMaintenanceModal();
        }
    });
    document.getElementById("local-policy-form").addEventListener("submit", onLocalPolicySearch);
    document.getElementById("audit-form").addEventListener("submit", onAudit);
    document.getElementById("audit-run-selected").addEventListener("click", () => runAuditForSelected());
    document.getElementById("audit-run-page").addEventListener("click", () => runAuditForCurrentPage());
    document.getElementById("audit-select-all").addEventListener("change", onAuditSelectAllChanged);
    document.getElementById("audit-show-mismatch-only")?.addEventListener("change", () => renderAuditRows());
    document.getElementById("audit-detail-close").addEventListener("click", closeAuditDetail);
    document.getElementById("audit-export-excel").addEventListener("click", () => downloadProjectionAuditExport("xlsx"));
    document.getElementById("audit-export-csv").addEventListener("click", () => downloadProjectionAuditExport("csv"));
    document.getElementById("payroll-change-register-report-form").addEventListener("submit", onPayrollChangeRegisterReportSearch);
    document.getElementById("payroll-change-register-print").addEventListener("click", generateAndPrintSelectedPayrollChangeRegister);
    document.getElementById("payroll-change-register-print-all")?.addEventListener("click", generateAndPrintAllPayrollChangeRegister);
    document.getElementById("payroll-change-register-export-excel").addEventListener("click", exportSelectedPayrollChangeRegisterExcel);
    document.getElementById("report-payroll-change-select-all").addEventListener("change", event => {
        document.querySelectorAll("#report-payroll-change-rows [data-register-select]").forEach(checkbox => {
            checkbox.checked = event.target.checked;
        });
    });
    document.getElementById("report-payroll-change-first")?.addEventListener("click", () => gotoPayrollChangeRegisterPage(0));
    document.getElementById("report-payroll-change-prev")?.addEventListener("click", () => gotoPayrollChangeRegisterPage(state.payrollChangeRegisterPage - 1));
    document.getElementById("report-payroll-change-next")?.addEventListener("click", () => gotoPayrollChangeRegisterPage(state.payrollChangeRegisterPage + 1));
    document.getElementById("report-payroll-change-last")?.addEventListener("click", () => gotoPayrollChangeRegisterPage(state.payrollChangeRegisterTotalPages - 1));
    document.getElementById("report-payroll-change-page-input")?.addEventListener("change", event => {
        const value = parseInt(event.target.value, 10);
        if (!Number.isNaN(value)) {
            gotoPayrollChangeRegisterPage(value - 1);
        }
    });
    document.getElementById("payroll-change-approval-report-form").addEventListener("submit", onPayrollChangeApprovalReportSearch);
    document.getElementById("payroll-change-approval-print").addEventListener("click", generateAndPrintSelectedPayrollChangeApprovals);
    document.getElementById("payroll-change-approval-print-all")?.addEventListener("click", generateAndPrintAllPayrollChangeApprovals);
    document.getElementById("payroll-change-approval-export-excel").addEventListener("click", exportSelectedPayrollChangeApprovalsExcel);
    document.getElementById("report-approval-select-all").addEventListener("change", event => {
        document.querySelectorAll("#report-approval-select-rows [data-approval-select]").forEach(checkbox => {
            checkbox.checked = event.target.checked;
        });
    });
    document.getElementById("report-approval-select-rows").addEventListener("click", onPayrollChangeApprovalRowClick);
    document.getElementById("report-approval-first")?.addEventListener("click", () => gotoPayrollChangeApprovalPage(0));
    document.getElementById("report-approval-prev")?.addEventListener("click", () => gotoPayrollChangeApprovalPage(state.payrollChangeApprovalPage - 1));
    document.getElementById("report-approval-next")?.addEventListener("click", () => gotoPayrollChangeApprovalPage(state.payrollChangeApprovalPage + 1));
    document.getElementById("report-approval-last")?.addEventListener("click", () => gotoPayrollChangeApprovalPage(state.payrollChangeApprovalTotalPages - 1));
    document.getElementById("report-approval-page-input")?.addEventListener("change", event => {
        const value = parseInt(event.target.value, 10);
        if (!Number.isNaN(value)) {
            gotoPayrollChangeApprovalPage(value - 1);
        }
    });
    document.getElementById("data-exchange-personnel-form").addEventListener("submit", onDataExchangePersonnelSearch);
    document.getElementById("data-exchange-personnel-download").addEventListener("click", downloadPersonnelCsv);
    document.getElementById("data-exchange-personnel-package-download").addEventListener("click", downloadPersonnelPackage);
    document.getElementById("data-exchange-personnel-select-all").addEventListener("change", event => {
        document.querySelectorAll("[data-personnel-export-select]").forEach(checkbox => {
            checkbox.checked = event.target.checked;
        });
    });
    document.getElementById("data-exchange-personnel-import-file").addEventListener("change", onDataExchangePersonnelImportFileSelected);
    document.getElementById("data-exchange-personnel-import-form").addEventListener("submit", onDataExchangePersonnelImportPreview);
    document.getElementById("data-exchange-personnel-import-apply").addEventListener("click", confirmDataExchangePersonnelImport);
    document.getElementById("data-exchange-personnel-import-select-all").addEventListener("change", event => {
        document.querySelectorAll("[data-personnel-import-select]").forEach(checkbox => {
            checkbox.checked = event.target.checked;
        });
    });
    document.getElementById("data-exchange-annual-form").addEventListener("submit", onDataExchangeAnnualSearch);
    document.getElementById("data-exchange-annual-download").addEventListener("click", downloadAnnualCsv);
    document.getElementById("data-exchange-annual-excel-download").addEventListener("click", downloadAnnualExcel);
    document.getElementById("data-exchange-submission-export-form").addEventListener("submit", onDataExchangeSubmissionSearch);
    document.getElementById("data-exchange-submission-download").addEventListener("click", downloadSubmissionPackage);
    document.getElementById("data-exchange-submission-select-all").addEventListener("change", event => {
        document.querySelectorAll("[data-submission-select]").forEach(checkbox => checkbox.checked = event.target.checked);
    });
    document.getElementById("data-exchange-submission-review-file").addEventListener("change", onDataExchangeSubmissionReviewFileSelected);
    document.getElementById("data-exchange-submission-review-form").addEventListener("submit", onDataExchangeSubmissionReviewPreview);
    document.getElementById("data-exchange-submission-review-approve").addEventListener("click", () => applyDataExchangeSubmissionReview("APPROVE", false));
    document.getElementById("data-exchange-submission-review-select-all").addEventListener("change", event => {
        document.querySelectorAll("[data-submission-review-select]").forEach(checkbox => checkbox.checked = event.target.checked);
    });
    document.getElementById("data-exchange-approval-dispatch-form").addEventListener("submit", onDataExchangeApprovalSearch);
    document.getElementById("data-exchange-approval-download").addEventListener("click", downloadApprovalPackage);
    document.getElementById("data-exchange-approval-revert").addEventListener("click", revertDispatchedApprovalPackage);
    document.getElementById("data-exchange-approval-select-all").addEventListener("change", event => {
        document.querySelectorAll("[data-approval-select]").forEach(checkbox => checkbox.checked = event.target.checked);
    });
    document.getElementById("data-exchange-approval-receive-file").addEventListener("change", onDataExchangeApprovalReceiveFileSelected);
    document.getElementById("data-exchange-approval-receive-form").addEventListener("submit", onDataExchangeApprovalReceivePreview);
    document.getElementById("data-exchange-approval-receive-apply").addEventListener("click", () => applyDataExchangeApprovalReceive(false));
    document.getElementById("data-exchange-approval-receive-select-all").addEventListener("change", event => {
        document.querySelectorAll("[data-approval-receive-select]").forEach(checkbox => checkbox.checked = event.target.checked);
    });
    document.querySelectorAll("[data-exchange-group]").forEach(button => {
        button.addEventListener("click", () => showDataExchangeGroup(button.dataset.exchangeGroup));
    });
    document.querySelectorAll("[data-exchange-tab]").forEach(button => {
        button.addEventListener("click", () => showDataExchangeTab(button.dataset.exchangeTab));
    });
    document.getElementById("payroll-history-form").addEventListener("submit", onPayrollHistorySearch);
    document.getElementById("payroll-history-first")?.addEventListener("click", () => gotoPayrollHistoryPage(0));
    document.getElementById("payroll-history-prev")?.addEventListener("click", () => gotoPayrollHistoryPage(state.payrollHistoryPage - 1));
    document.getElementById("payroll-history-next")?.addEventListener("click", () => gotoPayrollHistoryPage(state.payrollHistoryPage + 1));
    document.getElementById("payroll-history-last")?.addEventListener("click", () => gotoPayrollHistoryPage(state.payrollHistoryTotalPages - 1));
    document.getElementById("payroll-history-page-input")?.addEventListener("change", event => {
        const value = parseInt(event.target.value, 10);
        if (!Number.isNaN(value)) {
            gotoPayrollHistoryPage(value - 1);
        }
    });
    document.getElementById("payroll-change-close").addEventListener("click", closePayrollChangeModal);
    document.getElementById("teaching-allowance-form").addEventListener("submit", onTeachingAllowanceSearch);
    document.getElementById("police-rank-change-form")?.addEventListener("submit", event => onRankChangeSearch(event, RANK_CHANGE_MODULES.police));
    document.getElementById("prosecution-rank-change-form")?.addEventListener("submit", event => onRankChangeSearch(event, RANK_CHANGE_MODULES.prosecution));
    document.getElementById("judicial-rank-change-form")?.addEventListener("submit", event => onRankChangeSearch(event, RANK_CHANGE_MODULES.judicial));
    document.getElementById("supervision-rank-change-form")?.addEventListener("submit", event => onRankChangeSearch(event, RANK_CHANGE_MODULES.supervision));
    Object.values(RANK_CHANGE_MODULES).forEach(config => bindRankChangeModuleListeners(config));
    document.getElementById("rank-change-detail-close")?.addEventListener("click", closeRankChangeDetailModal);
    document.getElementById("rank-change-detail-modal")?.addEventListener("click", event => {
        if (event.target.id === "rank-change-detail-modal") {
            closeRankChangeDetailModal();
        }
    });
    document.getElementById("basic-salary-standard-adjustment-form")?.addEventListener("submit", onBasicSalaryStandardAdjustmentSearch);
    document.getElementById("basic-salary-standard-adjustment-include-apply")?.addEventListener("change", () => {
        state.basicSalaryStandardAdjustmentPage = 0;
        void loadBasicSalaryStandardAdjustments();
    });
    document.getElementById("basic-salary-standard-adjustment-include-processed")?.addEventListener("change", () => {
        state.basicSalaryStandardAdjustmentPage = 0;
        void loadBasicSalaryStandardAdjustments();
    });
    document.getElementById("basic-salary-standard-adjustment-later-period-mode")?.addEventListener("change", () => {
        state.basicSalaryStandardAdjustmentPage = 0;
        void loadBasicSalaryStandardAdjustments();
    });
    document.getElementById("basic-salary-standard-adjustment-batch-apply")?.addEventListener("click", applySelectedBasicSalaryStandardAdjustments);
    document.getElementById("basic-salary-standard-adjustment-apply-all")?.addEventListener("click", applyAllEligibleBasicSalaryStandardAdjustments);
    document.getElementById("basic-salary-standard-adjustment-export-mid-chain")?.addEventListener("click", exportBasicSalaryStandardAdjustmentMidChain);
    document.getElementById("basic-salary-standard-adjustment-batch-rollback")?.addEventListener("click", rollbackSelectedBasicSalaryStandardAdjustments);
    document.getElementById("basic-salary-standard-adjustment-rollback-all")?.addEventListener("click", rollbackAllProcessedBasicSalaryStandardAdjustments);
    document.getElementById("basic-salary-standard-adjustment-detail-close")?.addEventListener("click", closeBasicSalaryStandardAdjustmentDetailModal);
    document.getElementById("basic-salary-standard-adjustment-select-all")?.addEventListener("change", event => {
        document.querySelectorAll("[data-basic-adj-select]").forEach(checkbox => {
            checkbox.checked = event.target.checked;
        });
    });
    document.getElementById("basic-salary-standard-adjustment-first")?.addEventListener("click", () => gotoBasicSalaryStandardAdjustmentPage(0));
    document.getElementById("basic-salary-standard-adjustment-prev")?.addEventListener("click", () => {
        gotoBasicSalaryStandardAdjustmentPage((state.basicSalaryStandardAdjustmentPage || 0) - 1);
    });
    document.getElementById("basic-salary-standard-adjustment-next")?.addEventListener("click", () => {
        gotoBasicSalaryStandardAdjustmentPage((state.basicSalaryStandardAdjustmentPage || 0) + 1);
    });
    document.getElementById("basic-salary-standard-adjustment-last")?.addEventListener("click", () => {
        gotoBasicSalaryStandardAdjustmentPage((state.basicSalaryStandardAdjustmentTotalPages || 1) - 1);
    });
    document.getElementById("basic-salary-standard-adjustment-page-input")?.addEventListener("change", event => {
        const page = Number(event.target.value || "1") - 1;
        gotoBasicSalaryStandardAdjustmentPage(Number.isFinite(page) ? page : 0);
    });
    document.getElementById("basic-salary-standard-adjustment-page-size")?.addEventListener("change", () => {
        state.basicSalaryStandardAdjustmentPage = 0;
        void loadBasicSalaryStandardAdjustments();
    });
    document.getElementById("normal-promotion-form").addEventListener("submit", onNormalPromotionSearch);
    document.getElementById("normal-promotion-include-apply")?.addEventListener("change", () => {
        state.normalPromotionPage = 0;
        void loadNormalPromotions();
    });
    document.getElementById("normal-promotion-include-processed")?.addEventListener("change", () => {
        state.normalPromotionPage = 0;
        void loadNormalPromotions();
    });
    document.getElementById("normal-promotion-batch-apply").addEventListener("click", applySelectedNormalPromotions);
    document.getElementById("normal-promotion-apply-all").addEventListener("click", applyAllEligibleNormalPromotions);
    document.getElementById("normal-promotion-rollback-all").addEventListener("click", rollbackAllProcessedNormalPromotions);
    document.getElementById("normal-promotion-first")?.addEventListener("click", () => gotoNormalPromotionPage(0));
    document.getElementById("normal-promotion-prev")?.addEventListener("click", () => gotoNormalPromotionPage(state.normalPromotionPage - 1));
    document.getElementById("normal-promotion-next")?.addEventListener("click", () => gotoNormalPromotionPage(state.normalPromotionPage + 1));
    document.getElementById("normal-promotion-last")?.addEventListener("click", () => gotoNormalPromotionPage(state.normalPromotionTotalPages - 1));
    document.getElementById("normal-promotion-page-input")?.addEventListener("change", event => {
        const page = Number(event.target.value || "1") - 1;
        gotoNormalPromotionPage(Number.isFinite(page) ? page : 0);
    });
    document.getElementById("normal-promotion-select-all").addEventListener("change", event => {
        document.querySelectorAll("[data-normal-select]:not(:disabled)").forEach(checkbox => {
            checkbox.checked = event.target.checked;
        });
    });
    document.getElementById("normal-promotion-detail-close")?.addEventListener("click", closeNormalPromotionDetailModal);
    document.getElementById("level-promotion-form").addEventListener("submit", onLevelPromotionSearch);
    document.getElementById("level-promotion-batch-apply").addEventListener("click", applySelectedLevelPromotions);
    document.getElementById("level-promotion-apply-all")?.addEventListener("click", applyAllEligibleLevelPromotions);
    document.getElementById("level-promotion-batch-rollback")?.addEventListener("click", rollbackSelectedLevelPromotions);
    document.getElementById("level-promotion-rollback-all").addEventListener("click", rollbackAllProcessedLevelPromotions);
    document.getElementById("level-promotion-first")?.addEventListener("click", () => gotoLevelPromotionPage(0));
    document.getElementById("level-promotion-prev")?.addEventListener("click", () => gotoLevelPromotionPage(state.levelPromotionPage - 1));
    document.getElementById("level-promotion-next")?.addEventListener("click", () => gotoLevelPromotionPage(state.levelPromotionPage + 1));
    document.getElementById("level-promotion-last")?.addEventListener("click", () => gotoLevelPromotionPage(state.levelPromotionTotalPages - 1));
    document.getElementById("level-promotion-page-input")?.addEventListener("change", event => {
        const value = parseInt(event.target.value, 10);
        if (!Number.isNaN(value)) {
            gotoLevelPromotionPage(value - 1);
        }
    });
    document.getElementById("level-promotion-select-all").addEventListener("change", event => {
        document.querySelectorAll("[data-level-select]:not(:disabled)").forEach(checkbox => {
            checkbox.checked = event.target.checked;
        });
    });
    document.getElementById("level-promotion-detail-close")?.addEventListener("click", closeLevelPromotionDetailModal);
    document.getElementById("position-change-promotion-form").addEventListener("submit", onPositionChangePromotionSearch);
    document.getElementById("position-change-include-apply")?.addEventListener("change", () => {
        state.positionChangePage = 0;
        void loadPositionChangePromotions();
    });
    document.getElementById("position-change-include-processed")?.addEventListener("change", () => {
        state.positionChangePage = 0;
        void loadPositionChangePromotions();
    });
    document.getElementById("position-change-batch-apply").addEventListener("click", applySelectedPositionChanges);
    document.getElementById("position-change-batch-rollback")?.addEventListener("click", rollbackSelectedPositionChanges);
    document.getElementById("position-change-first")?.addEventListener("click", () => gotoPositionChangePage(0));
    document.getElementById("position-change-prev")?.addEventListener("click", () => gotoPositionChangePage(state.positionChangePage - 1));
    document.getElementById("position-change-next")?.addEventListener("click", () => gotoPositionChangePage(state.positionChangePage + 1));
    document.getElementById("position-change-last")?.addEventListener("click", () => gotoPositionChangePage(state.positionChangeTotalPages - 1));
    document.getElementById("position-change-page-input")?.addEventListener("change", event => {
        const page = Number(event.target.value || "1") - 1;
        gotoPositionChangePage(Number.isFinite(page) ? page : 0);
    });
    document.getElementById("position-change-select-all").addEventListener("change", event => {
        document.querySelectorAll("[data-position-change-select]:not(:disabled)").forEach(checkbox => {
            checkbox.checked = event.target.checked;
        });
    });
    document.getElementById("position-change-detail-close").addEventListener("click", closePositionChangeDetailModal);
    document.getElementById("payroll-preview-modal-close").addEventListener("click", closePayrollPreviewModal);
    document.getElementById("payroll-preview-modal").addEventListener("click", event => {
        if (event.target.id === "payroll-preview-modal") {
            closePayrollPreviewModal();
        }
    });
    document.getElementById("education-promotion-form").addEventListener("submit", onEducationPromotionSearch);
    document.getElementById("education-promotion-include-apply")?.addEventListener("change", () => {
        void loadEducationPromotions();
    });
    document.getElementById("education-promotion-include-processed")?.addEventListener("change", () => {
        void loadEducationPromotions();
    });
    document.getElementById("education-promotion-detail-close")?.addEventListener("click", closeEducationPromotionDetailModal);
    document.getElementById("education-promotion-detail-modal")?.addEventListener("click", event => {
        if (event.target.id === "education-promotion-detail-modal") {
            closeEducationPromotionDetailModal();
        }
    });
    document.getElementById("regularization-form").addEventListener("submit", onRegularizationSearch);
    document.getElementById("regularization-include-apply")?.addEventListener("change", () => {
        void loadRegularizations();
    });
    document.getElementById("regularization-include-processed")?.addEventListener("change", () => {
        void loadRegularizations();
    });
    document.getElementById("regularization-batch-apply")?.addEventListener("click", applySelectedRegularizations);
    document.getElementById("regularization-batch-rollback")?.addEventListener("click", rollbackSelectedRegularizations);
    document.getElementById("regularization-select-all")?.addEventListener("change", event => {
        const checked = event.target.checked;
        document.querySelectorAll("[data-regularization-select]:not(:disabled)").forEach(cb => {
            cb.checked = checked;
        });
    });
    document.getElementById("regularization-detail-close")?.addEventListener("click", closeRegularizationDetailModal);
    document.getElementById("regularization-detail-modal")?.addEventListener("click", event => {
        if (event.target.id === "regularization-detail-modal") {
            closeRegularizationDetailModal();
        }
    });
    document.getElementById("regularization-high-grade-form")?.addEventListener("submit", onRegularizationHighGradeSearch);
    document.getElementById("regularization-high-grade-size")?.addEventListener("change", () => {
        state.regularizationHighGradePage = 0;
        void loadRegularizationHighGrades();
    });
    document.getElementById("regularization-high-grade-first")?.addEventListener("click", () => gotoRegularizationHighGradePage(0));
    document.getElementById("regularization-high-grade-prev")?.addEventListener("click", () => gotoRegularizationHighGradePage(state.regularizationHighGradePage - 1));
    document.getElementById("regularization-high-grade-next")?.addEventListener("click", () => gotoRegularizationHighGradePage(state.regularizationHighGradePage + 1));
    document.getElementById("regularization-high-grade-last")?.addEventListener("click", () => gotoRegularizationHighGradePage(state.regularizationHighGradeTotalPages - 1));
    document.getElementById("regularization-high-grade-page-input")?.addEventListener("change", event => {
        const value = parseInt(event.target.value, 10);
        if (!Number.isNaN(value)) {
            gotoRegularizationHighGradePage(value - 1);
        }
    });
    document.getElementById("floating-to-fixed-form").addEventListener("submit", onFloatingToFixedSearch);
    document.getElementById("intern-salary-change-form").addEventListener("submit", onInternSalaryChangeSearch);
    document.getElementById("new-personnel-salary-form").addEventListener("submit", onNewPersonnelSalarySearch);
    document.getElementById("new-personnel-salary-include-apply")?.addEventListener("change", () => {
        state.newPersonnelSalaryPage = 0;
        void loadNewPersonnelSalaryDeterminations();
    });
    document.getElementById("new-personnel-salary-include-processed")?.addEventListener("change", () => {
        state.newPersonnelSalaryPage = 0;
        void loadNewPersonnelSalaryDeterminations();
    });
    document.getElementById("new-personnel-salary-batch-apply")?.addEventListener("click", applySelectedNewPersonnelSalaries);
    document.getElementById("new-personnel-salary-batch-rollback")?.addEventListener("click", rollbackSelectedNewPersonnelSalaries);
    document.getElementById("new-personnel-salary-select-all")?.addEventListener("change", event => {
        const checked = event.target.checked;
        document.querySelectorAll("[data-new-personnel-select]:not(:disabled)").forEach(cb => {
            cb.checked = checked;
        });
    });
    document.getElementById("new-personnel-salary-first")?.addEventListener("click", () => gotoNewPersonnelSalaryPage(0));
    document.getElementById("new-personnel-salary-prev")?.addEventListener("click", () => gotoNewPersonnelSalaryPage(state.newPersonnelSalaryPage - 1));
    document.getElementById("new-personnel-salary-next")?.addEventListener("click", () => gotoNewPersonnelSalaryPage(state.newPersonnelSalaryPage + 1));
    document.getElementById("new-personnel-salary-last")?.addEventListener("click", () => gotoNewPersonnelSalaryPage(state.newPersonnelSalaryTotalPages - 1));
    document.getElementById("new-personnel-salary-page-input")?.addEventListener("change", event => {
        const value = Number(event.target.value || 1);
        gotoNewPersonnelSalaryPage(value - 1);
    });
    document.getElementById("new-personnel-salary-detail-close")?.addEventListener("click", closeNewPersonnelSalaryDetailModal);
    document.getElementById("other-payroll-change-form").addEventListener("submit", onOtherPayrollChangeSearch);
    document.getElementById("other-payroll-change-size")?.addEventListener("change", () => {
        state.otherPayrollChangePage = 0;
        void loadOtherPayrollChanges();
    });
    document.getElementById("other-payroll-change-first")?.addEventListener("click", () => gotoOtherPayrollChangePage(0));
    document.getElementById("other-payroll-change-prev")?.addEventListener("click", () => gotoOtherPayrollChangePage(state.otherPayrollChangePage - 1));
    document.getElementById("other-payroll-change-next")?.addEventListener("click", () => gotoOtherPayrollChangePage(state.otherPayrollChangePage + 1));
    document.getElementById("other-payroll-change-last")?.addEventListener("click", () => gotoOtherPayrollChangePage(state.otherPayrollChangeTotalPages - 1));
    document.getElementById("other-payroll-change-page-input")?.addEventListener("change", event => {
        const value = parseInt(event.target.value, 10);
        if (!Number.isNaN(value)) {
            gotoOtherPayrollChangePage(value - 1);
        }
    });
    document.getElementById("other-payroll-change-apply-form").addEventListener("submit", onOtherPayrollChangeApply);
    document.getElementById("other-payroll-change-modal-close").addEventListener("click", closeOtherPayrollChangeModal);
    document.getElementById("other-payroll-change-cancel").addEventListener("click", closeOtherPayrollChangeModal);
    document.getElementById("other-payroll-change-modal").addEventListener("click", event => {
        if (event.target.id === "other-payroll-change-modal") {
            closeOtherPayrollChangeModal();
        }
    });
    document.getElementById("basic-standards-form").addEventListener("submit", onBasicStandardsSearch);
    document.getElementById("basic-standard-type").addEventListener("change", () => {
        updateBasicStandardCreateButton();
        updateBasicStandardPositionCategoryVisibility();
        void refreshBasicStandardPeriods()
            .then(() => refreshBasicStandardPositionCategories())
            .then(() => loadBasicStandards());
    });
    document.getElementById("basic-standard-year-month").addEventListener("change", () => {
        void refreshBasicStandardPositionCategories().then(() => loadBasicStandards());
    });
    document.getElementById("basic-standard-position-category").addEventListener("change", () => {
        void loadBasicStandards();
    });
    document.getElementById("basic-standard-create").addEventListener("click", onBasicStandardCreate);
    document.getElementById("grade-standard-form").addEventListener("submit", onGradeStandardFormSubmit);
    document.getElementById("grade-standard-modal-close").addEventListener("click", closeGradeStandardModal);
    document.getElementById("grade-standard-modal").addEventListener("click", event => {
        if (event.target.id === "grade-standard-modal") {
            closeGradeStandardModal();
        }
    });
    document.getElementById("salary-level-standard-form").addEventListener("submit", onSalaryLevelStandardFormSubmit);
    document.getElementById("salary-level-standard-modal-close").addEventListener("click", closeSalaryLevelStandardModal);
    document.getElementById("salary-level-standard-modal").addEventListener("click", event => {
        if (event.target.id === "salary-level-standard-modal") {
            closeSalaryLevelStandardModal();
        }
    });
    document.getElementById("allowance-standards-form").addEventListener("submit", onAllowanceStandardsSearch);
    document.getElementById("allowance-standard-create").addEventListener("click", createAllowanceStandard);
    document.getElementById("allowance-standard-year-month").addEventListener("change", () => {
        void refreshAllowanceStandardCategories()
            .then(() => refreshAllowanceStandardPositionCategories())
            .then(() => loadAllowanceStandards());
    });
    document.getElementById("allowance-standard-category").addEventListener("change", () => {
        void refreshAllowanceStandardPositionCategories().then(() => loadAllowanceStandards());
    });
    document.getElementById("allowance-standard-position-category").addEventListener("change", () => {
        void loadAllowanceStandards();
    });
    document.getElementById("rank-allowance-standards-form").addEventListener("submit", onRankAllowanceStandardsSearch);
    document.getElementById("rank-standard-create").addEventListener("click", createRankAllowanceStandard);
    document.getElementById("rank-standard-category").addEventListener("change", () => {
        void refreshRankAllowanceStandardPeriods().then(() => loadRankAllowanceStandards());
    });
    document.getElementById("rank-standard-year-month").addEventListener("change", () => {
        void loadRankAllowanceStandards();
    });
    document.getElementById("retained-allowance-standards-form").addEventListener("submit", onRetainedAllowanceStandardsSearch);
    document.getElementById("retained-standard-create").addEventListener("click", createRetainedAllowanceStandard);
    document.getElementById("retained-standard-position-category").addEventListener("change", () => {
        void loadRetainedAllowanceStandards();
    });
    document.getElementById("year-allowance-standards-form").addEventListener("submit", onYearAllowanceStandardsSearch);
    document.getElementById("year-standard-create").addEventListener("click", createYearAllowanceStandard);
    document.getElementById("intern-standard-create").addEventListener("click", () => openInternSalaryStandardModal("create"));
    document.getElementById("wage-reform-create").addEventListener("click", () => openWageReformStandardModal("create"));
    document.getElementById("other-allowance-create").addEventListener("click", () => openOtherAllowanceStandardModal("create"));
    document.getElementById("intern-salary-standard-form").addEventListener("submit", onInternSalaryStandardFormSubmit);
    document.getElementById("intern-salary-standard-modal-close").addEventListener("click", closeInternSalaryStandardModal);
    document.getElementById("intern-salary-standard-modal").addEventListener("click", event => {
        if (event.target.id === "intern-salary-standard-modal") {
            closeInternSalaryStandardModal();
        }
    });
    document.getElementById("wage-reform-standard-form").addEventListener("submit", onWageReformStandardFormSubmit);
    document.getElementById("wage-reform-standard-modal-close").addEventListener("click", closeWageReformStandardModal);
    document.getElementById("wage-reform-standard-modal").addEventListener("click", event => {
        if (event.target.id === "wage-reform-standard-modal") {
            closeWageReformStandardModal();
        }
    });
    document.getElementById("other-allowance-standard-form").addEventListener("submit", onOtherAllowanceStandardFormSubmit);
    document.getElementById("other-allowance-standard-modal-close").addEventListener("click", closeOtherAllowanceStandardModal);
    document.getElementById("other-allowance-standard-modal").addEventListener("click", event => {
        if (event.target.id === "other-allowance-standard-modal") {
            closeOtherAllowanceStandardModal();
        }
    });
    document.getElementById("other-allowance-standard-modal-type").addEventListener("change", updateOtherAllowanceStandardModalFields);
    document.getElementById("intern-salary-standards-form").addEventListener("submit", onInternSalaryStandardsSearch);
    document.getElementById("wage-reform-standards-form").addEventListener("submit", onWageReformStandardsSearch);
    document.getElementById("wage-reform-position-category").addEventListener("change", () => {
        void refreshWageReformStandardPositions().then(() => loadWageReformStandards());
    });
    document.getElementById("wage-reform-position").addEventListener("change", () => {
        void loadWageReformStandards();
    });
    document.getElementById("other-allowance-standards-form").addEventListener("submit", onOtherAllowanceStandardsSearch);
    document.getElementById("other-allowance-standard-type").addEventListener("change", () => {
        void refreshOtherAllowanceStandardPeriods()
            .then(() => refreshOtherAllowanceStandardPositionCategories())
            .then(() => loadOtherAllowanceStandards());
    });
    document.getElementById("other-allowance-filter-year-month").addEventListener("change", () => {
        void refreshOtherAllowanceStandardPositionCategories().then(() => loadOtherAllowanceStandards());
    });
    document.getElementById("other-allowance-position-category").addEventListener("change", () => {
        void loadOtherAllowanceStandards();
    });
    document.getElementById("create-user-form").addEventListener("submit", onCreateUser);
    document.getElementById("create-role-form").addEventListener("submit", onCreateRole);
    document.getElementById("create-menu-form").addEventListener("submit", onCreateMenu);
    document.getElementById("change-password-form").addEventListener("submit", onChangePassword);
    wireSecurityAdminUi();
    document.getElementById("operation-log-form")?.addEventListener("submit", onOperationLogSearch);
    document.getElementById("change-password-button").addEventListener("click", () => {
        document.getElementById("password-panel").classList.toggle("hidden");
    });
    document.addEventListener("click", event => {
        if (!event.target.closest(".personnel-change-menu") && !event.target.closest("[data-maint-change]")) {
            closePersonnelChangeMenu();
        }
    });
    document.getElementById("data-maintenance-refresh")?.addEventListener("click", loadDataMaintenanceDiagnostics);
    document.getElementById("data-maintenance-purge-logs")?.addEventListener("click", purgeDataMaintenanceAuditLogs);
    document.getElementById("data-maintenance-purge-markers")?.addEventListener("click", purgeDataMaintenanceOrphanMarkers);
    document.getElementById("data-backup-export")?.addEventListener("click", exportDataBackup);
    document.getElementById("data-backup-inspect")?.addEventListener("click", inspectDataBackup);
    document.getElementById("data-backup-restore")?.addEventListener("click", restoreDataBackup);
    wireBackupScopeExclusiveChecks();
    document.getElementById("license-status-refresh")?.addEventListener("click", loadLicenseStatus);
    document.getElementById("license-import-execute")?.addEventListener("click", importLicensePackage);
    document.getElementById("license-orgs-export")?.addEventListener("click", exportLicenseOrgsForOps);
    document.getElementById("ukey-bind-import-execute")?.addEventListener("click", importUkeyBindingsPackage);
    syncLicenseIssueScopeControls();
    document.getElementById("system-setup-import-template")?.addEventListener("click", downloadExcelImportTemplate);
    document.getElementById("system-setup-import-preview")?.addEventListener("click", previewExcelImport);
    document.getElementById("system-setup-import-execute")?.addEventListener("click", executeExcelImport);
    document.getElementById("system-setup-init-preview")?.addEventListener("click", previewSystemInitialization);
    document.getElementById("system-setup-init-execute")?.addEventListener("click", executeSystemInitialization);
    document.getElementById("system-setup-init-clear-orgs")?.addEventListener("change", () => {
        void previewSystemInitialization();
    });
    document.getElementById("logout-button").addEventListener("click", () => {
        window.location.href = "/logout";
    });
    document.getElementById("domain-switch-button")?.addEventListener("click", () => {
        clearAppDomain();
    });
    document.querySelectorAll("[data-enter-domain]").forEach(button => {
        button.addEventListener("click", () => {
            setAppDomain(button.dataset.enterDomain, { navigateHome: true });
        });
    });
    document.getElementById("dashboard-grid")?.addEventListener("click", event => {
        const card = event.target.closest("[data-domain-reset='1']");
        if (!card) {
            return;
        }
        event.preventDefault();
        clearAppDomain();
    });
    document.getElementById("retirement-processing-form")?.addEventListener("submit", event => {
        event.preventDefault();
        state.retirementProcessingPage = 0;
        void loadRetirementProcessingCandidates();
    });
    document.getElementById("retirement-processing-size")?.addEventListener("change", () => {
        state.retirementProcessingPage = 0;
        void loadRetirementProcessingCandidates();
    });
    document.getElementById("retirement-processing-first")?.addEventListener("click", () => gotoRetirementProcessingPage(0));
    document.getElementById("retirement-processing-prev")?.addEventListener("click", () => gotoRetirementProcessingPage(state.retirementProcessingPage - 1));
    document.getElementById("retirement-processing-next")?.addEventListener("click", () => gotoRetirementProcessingPage(state.retirementProcessingPage + 1));
    document.getElementById("retirement-processing-last")?.addEventListener("click", () => gotoRetirementProcessingPage(state.retirementProcessingTotalPages - 1));
    document.getElementById("retirement-processing-page-input")?.addEventListener("change", event => {
        const page = Math.max(parseInt(event.target.value, 10) || 1, 1) - 1;
        gotoRetirementProcessingPage(page);
    });
    document.getElementById("retirement-processing-rows")?.addEventListener("click", event => {
        const button = event.target.closest("[data-retirement-apply]");
        if (!button) {
            return;
        }
        void applyRetirementProcessing(Number(button.dataset.retirementApply), button.dataset.name || "");
    });
    document.getElementById("retiree-personnel-form")?.addEventListener("submit", event => {
        event.preventDefault();
        state.retireePersonnelPage = 0;
        void loadRetireePersonnel();
    });
    document.getElementById("retiree-personnel-size")?.addEventListener("change", () => {
        state.retireePersonnelPage = 0;
        void loadRetireePersonnel();
    });
    document.getElementById("retiree-personnel-pending-only")?.addEventListener("change", () => {
        state.retireePersonnelPage = 0;
        void loadRetireePersonnel();
    });
    document.getElementById("retiree-personnel-include-descendants")?.addEventListener("change", () => {
        state.retireePersonnelPage = 0;
        void loadRetireePersonnel();
    });
    document.getElementById("retiree-personnel-first")?.addEventListener("click", () => gotoRetireePersonnelPage(0));
    document.getElementById("retiree-personnel-prev")?.addEventListener("click", () => gotoRetireePersonnelPage(state.retireePersonnelPage - 1));
    document.getElementById("retiree-personnel-next")?.addEventListener("click", () => gotoRetireePersonnelPage(state.retireePersonnelPage + 1));
    document.getElementById("retiree-personnel-last")?.addEventListener("click", () => gotoRetireePersonnelPage(state.retireePersonnelTotalPages - 1));
    document.getElementById("retiree-personnel-page-input")?.addEventListener("change", event => {
        const page = Math.max(parseInt(event.target.value, 10) || 1, 1) - 1;
        gotoRetireePersonnelPage(page);
    });
    document.getElementById("retiree-personnel-rows")?.addEventListener("click", event => {
        const maintain = event.target.closest("[data-retiree-maintain]");
        if (maintain) {
            void openRetireeMaintenance(Number(maintain.dataset.retireeMaintain));
            return;
        }
        const approve = event.target.closest("[data-retiree-approve]");
        if (approve) {
            void approveRetireeFromList(Number(approve.dataset.retireeApprove), approve.dataset.name || "");
        }
    });
    document.getElementById("retiree-maintenance-close")?.addEventListener("click", closeRetireeMaintenance);
    document.getElementById("retiree-maint-cancel")?.addEventListener("click", closeRetireeMaintenance);
    document.getElementById("retiree-maintenance-form")?.addEventListener("submit", event => {
        event.preventDefault();
        void saveRetireeMaintenance();
    });
    document.getElementById("retiree-maint-approve")?.addEventListener("click", () => {
        void approveRetireeFromModal();
    });
    document.getElementById("retiree-maint-cancel-approve")?.addEventListener("click", () => {
        void cancelRetireeApprovalFromModal();
    });
    document.getElementById("retirement-ratio-standards-refresh")?.addEventListener("click", () => {
        void loadRetirementRatioStandards();
    });
    document.getElementById("retirement-approval-select-all")?.addEventListener("change", event => {
        document.querySelectorAll("#retirement-approval-rows [data-retirement-approval-select]")
            .forEach(input => {
                input.checked = event.target.checked;
            });
    });
    document.getElementById("retirement-approval-report-form")?.addEventListener("submit", event => {
        event.preventDefault();
        void onRetirementApprovalReportSearch();
    });
    document.getElementById("retirement-approval-style")?.addEventListener("change", () => {
        void refreshRetirementApprovalTemplateName();
    });
    document.getElementById("retirement-approval-org-nature")?.addEventListener("change", () => {
        void refreshRetirementApprovalTemplateName();
    });
    document.getElementById("retirement-approval-print")?.addEventListener("click", () => {
        void generateAndPrintRetirementApprovalReports();
    });
    window.addEventListener("hashchange", applyRoute);
    initializeNormalPromotionPage();
    initializeLevelPromotionPage();
});

async function initializeAuth() {
    try {
        const user = await getJson("/api/auth/me");
        const menus = await getJson("/api/auth/menus");
        state.currentUser = user;
        state.menus = menus;
        state.appDomain = readStoredAppDomain();
        document.getElementById("current-user").textContent = `${user.displayName} (${user.username})`;
        updateDomainChrome();
        renderMenus();
        updateStandardWriteUi();
        updateOrgWriteUi();
        renderDashboard();
        applyRoute();
        void refreshLicenseBanner();
        if (hasMenu("ANNUAL_ASSESSMENT_MANAGEMENT")) {
            updateAssessmentBatchWriteUi();
            initializeAssessmentBatchPage();
        }
        if (hasMenu("NORMAL_PROMOTION")) {
            initializeNormalPromotionPage();
        }
        if (hasMenu("LEVEL_PROMOTION")) {
            initializeLevelPromotionPage();
        }
        updateAllPayrollFeatureWriteUi();
        if (hasPersonnelAccess()) {
            refreshPersonnelPanelActions();
        }
        try {
            await initializeDictionaryPickers();
        } catch (error) {
            console.warn("字典选择器初始化失败", error);
        }
    } catch (error) {
        if (isAuthFailure(error)) {
            window.location.href = "/login.html";
            return;
        }
        console.error("工作台初始化失败", error);
        const currentUser = document.getElementById("current-user");
        if (currentUser && currentUser.textContent === "正在验证登录...") {
            currentUser.textContent = "登录状态异常，请重新登录";
        }
    }
}

function isAuthFailure(error) {
    const message = String(error?.message || error || "").toLowerCase();
    return message.includes("需要登录")
            || message.includes("登录已失效")
            || message.includes("authentication required")
            || message.includes("full authentication is required")
            || message.includes("access is denied")
            || message.includes("access denied")
            || message.includes("http 401")
            || message.includes("http 403");
}

function renderMenus() {
    const nav = document.getElementById("main-nav");
    const menuByCode = new Map([{ code: "DASHBOARD", title: "工作台", path: "#dashboard", permissionCode: "" }, ...state.menus].map(menu => [menu.code, menu]));
    const groups = visibleMenuGroups();
    nav.innerHTML = groups.map(group => {
        const links = renderMenuGroupContent(group, menuByCode);
        if (!links) {
            return "";
        }
        const commonClass = commonMenuGroups.includes(group) ? " nav-group-common" : "";
        return `
            <div class="nav-group${commonClass}">
                <div class="nav-group-title">${escapeHtml(group.title)}</div>
                ${links}
            </div>
        `;
    }).join("");
    document.querySelectorAll("[data-menu-code]").forEach(section => {
        section.classList.toggle("unavailable", !isPanelAvailable(section.dataset.menuCode));
    });
    refreshPersonnelPanelActions();
}

function hasMenu(code) {
    if (code === "DASHBOARD") {
        return true;
    }
    return state.menus.some(menu => menu.code === code);
}

function hasPermission(code) {
    return state.currentUser?.permissions?.includes(code) ?? false;
}

function hasRetirementWrite() {
    return hasPermission("RETIREMENT_WRITE");
}

function hasPersonnelWrite() {
    return hasPermission("PERSONNEL_WRITE");
}

function hasPersonnelAccess() {
    return hasMenu("PERSONNEL") || hasPersonnelWrite();
}

function hasPayrollRead() {
    return hasPermission("PAYROLL_READ");
}

function hasPayrollFeatureWrite(writePermission) {
    return hasPermission(writePermission);
}

function payrollWritePermissionForApi(apiPrefix) {
    return PAYROLL_API_WRITE_PERMISSIONS[apiPrefix] || null;
}

function ensurePayrollFeatureWrite(writePermission, moduleName) {
    if (!hasPayrollFeatureWrite(writePermission)) {
        window.alert(`当前账号没有${moduleName}办理权限。`);
        return false;
    }
    return true;
}

function ensurePayrollApiWrite(apiPrefix, moduleName) {
    const writePermission = payrollWritePermissionForApi(apiPrefix);
    if (!writePermission) {
        return true;
    }
    return ensurePayrollFeatureWrite(writePermission, moduleName);
}

function updatePayrollFeatureWriteUi(config) {
    const canWrite = hasPayrollFeatureWrite(config.writePermission);
    (config.batchButtonIds || []).forEach(id => document.getElementById(id)?.classList.toggle("hidden", !canWrite));
    if (config.tableSelector) {
        document.querySelectorAll(config.tableSelector).forEach(cell => {
            cell.classList.toggle("hidden", !canWrite);
        });
    }
    if (config.selectAllId) {
        document.getElementById(config.selectAllId)?.classList.toggle("hidden", !canWrite);
    }
}

function updateRankChangeWriteUi(config) {
    const canWrite = hasPayrollFeatureWrite(config.writePermission);
    const prefix = config.idPrefix;
    [
        `${prefix}-batch-apply`,
        `${prefix}-apply-all`,
        `${prefix}-batch-rollback`,
        `${prefix}-rollback-all`,
        `${prefix}-export-mid-chain`,
    ].forEach(id => document.getElementById(id)?.classList.toggle("hidden", !canWrite));
    document.querySelectorAll(`#${prefix} .col-select`).forEach(cell => {
        cell.classList.toggle("hidden", !canWrite);
    });
    document.getElementById(`${prefix}-select-all`)?.classList.toggle("hidden", !canWrite);
}

function updateAllPayrollFeatureWriteUi() {
    PAYROLL_FEATURE_WRITE_UI.forEach(updatePayrollFeatureWriteUi);
    Object.values(RANK_CHANGE_MODULES).forEach(updateRankChangeWriteUi);
}

function hasLevelPromotionRead() {
    return hasPermission("LEVEL_PROMOTION_READ") || hasPermission("PAYROLL_READ");
}

function hasLevelPromotionWrite() {
    return hasPayrollFeatureWrite("LEVEL_PROMOTION_WRITE");
}

function updateLevelPromotionWriteUi() {
    updatePayrollFeatureWriteUi(PAYROLL_FEATURE_WRITE_UI.find(config => config.writePermission === "LEVEL_PROMOTION_WRITE"));
}

function hasSystemConfigWrite() {
    return hasPermission("SYSTEM_CONFIG");
}

function updateDictionaryWriteUi() {
    const visible = hasSystemConfigWrite();
    document.querySelectorAll("#dictionary-maintenance .system-config-write-col").forEach(element => {
        element.classList.toggle("hidden", !visible);
    });
    const addOption = document.getElementById("dictionary-add-option-button");
    if (addOption) {
        const hasCategory = Boolean(document.getElementById("dictionary-category")?.value.trim());
        addOption.disabled = !visible || !hasCategory;
        addOption.title = hasCategory ? "" : "请先选择分类";
    }
}

function hasOrgWrite() {
    return hasPermission("ORG_WRITE");
}

function updateOrgWriteUi() {
    const visible = hasOrgWrite();
    document.querySelectorAll(".org-write-col").forEach(element => {
        element.classList.toggle("hidden", !visible);
    });
    setOrganizationDetailFormEditable(visible);
}

function setOrganizationDetailFormEditable(editable) {
    const form = document.getElementById("organization-detail-form");
    if (!form) {
        return;
    }
    const mode = document.getElementById("organization-detail-mode")?.value || "view";
    form.querySelectorAll("input, select, textarea").forEach(input => {
        if (input.type === "hidden") {
            return;
        }
        if (input.tagName === "SELECT") {
            input.disabled = !editable;
            return;
        }
        if (input.id === "organization-modal-code") {
            input.readOnly = !editable || mode === "edit";
            return;
        }
        input.readOnly = !editable;
        input.disabled = false;
    });
}

function hasStandardWrite() {
    return hasPermission("STANDARD_WRITE");
}

function updateStandardWriteUi() {
    const visible = hasStandardWrite();
    document.querySelectorAll(".standard-write-col").forEach(element => {
        element.classList.toggle("hidden", !visible);
    });
    ["allowance-standard-create", "rank-standard-create", "retained-standard-create", "year-standard-create",
        "intern-standard-create", "wage-reform-create", "other-allowance-create"].forEach(id => {
        const button = document.getElementById(id);
        if (button) {
            button.classList.toggle("hidden", !visible);
        }
    });
    updateBasicStandardCreateButton();
}

function updateBasicStandardCreateButton() {
    const button = document.getElementById("basic-standard-create");
    if (!button) {
        return;
    }
    const type = document.getElementById("basic-standard-type").value;
    const maintainable = hasStandardWrite() && ["position", "grade", "position-grade", "salary-level"].includes(type);
    button.classList.toggle("hidden", !maintainable);
    const labels = {
        position: "新增职务工资",
        grade: "新增级别工资",
        "position-grade": "新增岗位档次工资",
        "salary-level": "新增薪级工资",
    };
    button.textContent = labels[type] || "新增";
}

function initGradeStandardStepsGrid() {
    const grid = document.getElementById("grade-standard-steps-grid");
    if (!grid || grid.dataset.initialized === "true") {
        return;
    }
    grid.innerHTML = Array.from({ length: 20 }, (_, index) => `
        <label>
            档次${index + 1}
            <input type="number" data-grade-step="${index + 1}" value="0">
        </label>
    `).join("");
    grid.dataset.initialized = "true";
}

function basicStandardMaintainable(standardType) {
    return hasStandardWrite() && ["position", "grade", "position-grade", "salary-level"].includes(standardType);
}

function extractGradeSteps(values) {
    return Array.from({ length: 20 }, (_, index) => Number(values[`dc${index + 1}`] ?? 0));
}

function onBasicStandardCreate() {
    const type = document.getElementById("basic-standard-type").value;
    if (type === "position") {
        createPositionSalaryStandard();
        return;
    }
    if (type === "grade" || type === "position-grade") {
        openGradeStandardModal("create", type);
        return;
    }
    if (type === "salary-level") {
        openSalaryLevelStandardModal("create");
    }
}

function refreshPersonnelPanelActions() {
    const newButton = document.getElementById("personnel-maintenance-new");
    if (newButton) {
        newButton.classList.toggle("hidden", !hasPersonnelWrite());
    }
}

function isPanelAvailable(menuCode) {
    if (menuCode === "PERSONNEL") {
        return hasPersonnelAccess();
    }
    return hasMenu(menuCode);
}

function initializeSidebar() {
    const toggle = document.getElementById("sidebar-toggle");
    if (!toggle) {
        return;
    }
    toggle.addEventListener("click", (event) => {
        event.stopPropagation();
        document.body.classList.toggle("sidebar-open");
    });
    document.querySelectorAll("[data-menu-link]").forEach(link => {
        link.addEventListener("click", () => document.body.classList.remove("sidebar-open"));
    });
    document.addEventListener("click", (event) => {
        if (event.target.closest(".dashboard-card, .dashboard-group-link")) {
            document.body.classList.remove("sidebar-open");
        }
    });
    document.addEventListener("click", (event) => {
        if (!document.body.classList.contains("sidebar-open")) {
            return;
        }
        const sidebar = document.querySelector(".app-sidebar");
        if (sidebar && !sidebar.contains(event.target) && event.target !== toggle) {
            document.body.classList.remove("sidebar-open");
        }
    });
}

function applyRoute() {
    applyRouteNow();
}

function applyRouteNow() {
    if (window.location.hash === "#personnel-maintenance") {
        window.location.replace("#personnel");
        return;
    }
    if (window.location.hash === "#reform-level-rolling") {
        window.location.replace("#level-promotion");
        return;
    }
    let requestedHash = window.location.hash || "#dashboard";
    if (requestedHash === "#annual-assessments" || requestedHash === "#annual-assessment-batch") {
        requestedHash = "#annual-assessment-management";
    }
    if (requestedHash === "#reform-level-rolling") {
        requestedHash = "#level-promotion";
    }
    // 旧版拆分菜单：警衔/检察/审判津贴标准统一到同一页面，并按路径预选类别
    const rankAllowanceLegacy = requestedHash.match(/^#rank-allowance-standards\/(jx|jc|sp|mt)$/);
    if (rankAllowanceLegacy) {
        const categoryInput = document.getElementById("rank-standard-category");
        if (categoryInput) {
            categoryInput.value = rankAllowanceLegacy[1];
        }
        requestedHash = "#rank-allowance-standards";
        history.replaceState(null, "", requestedHash);
    }
    const availableMenus = [{ code: "DASHBOARD", title: "工作台", path: "#dashboard" }, ...state.menus];
    const selectedMenu = availableMenus.find(menu => menu.path === requestedHash) || availableMenus[0];
    ensureDomainForMenuCode(selectedMenu.code);
    const selectedId = (selectedMenu.path || "#dashboard").replace("#", "");
    const previousPanelId = state.activePanelId;
    const panelChanged = previousPanelId !== selectedId;
    state.activePanelId = selectedId;
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
    updateAllPayrollFeatureWriteUi();
    if (selectedId === "normal-promotion") {
        initializeNormalPromotionPage();
    }
    if (selectedId === "level-promotion") {
        initializeLevelPromotionPage();
        updateAllPayrollFeatureWriteUi();
        if (panelChanged && hasMenu("LEVEL_PROMOTION")) {
            void loadLevelPromotions();
        }
    }
    if (selectedId === "annual-assessment-management") {
        updateAssessmentBatchWriteUi();
        initializeAssessmentBatchPage();
    }
    if (selectedId === "payroll-change-approval-report" || selectedId === "payroll-change-register-report") {
        void loadReportTypes();
    }
    if (selectedId === "new-personnel-salary" && panelChanged && hasMenu("NEW_PERSONNEL_SALARY")) {
        void loadNewPersonnelSalaryDeterminations();
    }
    if (selectedId === "other-payroll-change" && panelChanged && hasMenu("OTHER_PAYROLL_CHANGE")) {
        void loadOtherPayrollChanges();
    }
    if (selectedId === "regularization-high-grade" && panelChanged && hasMenu("REGULARIZATION_HIGH_GRADE")) {
        void loadRegularizationHighGrades();
    }
    if (selectedId === "retirement-due-query" && panelChanged && hasMenu("RETIREMENT_DUE_QUERY")) {
        ensureRetirementDueReferencePeriod();
        void loadRetirementDuePersonnel();
    }
    if (selectedId === "personnel-comprehensive-query" && panelChanged && hasMenu("PERSONNEL_COMPREHENSIVE_QUERY")) {
        void ensurePersonnelComprehensiveQueryOptions().then(() => loadPersonnelComprehensiveQueries());
    }
    if (selectedId === "position-history" && panelChanged && hasMenu("POSITION_HISTORY")) {
        void ensurePositionHistoryOptions();
    }
    if (selectedId === "education-history" && panelChanged && hasMenu("EDUCATION_HISTORY")) {
        void ensureEducationHistoryOptions();
    }
    if (selectedId === "police-rank-change-promotion" && panelChanged && hasMenu("POLICE_RANK_CHANGE_PROMOTION")) {
        void loadRankChangePromotions(RANK_CHANGE_MODULES.police);
    }
    if (selectedId === "prosecution-rank-change-promotion" && panelChanged && hasMenu("PROSECUTION_RANK_CHANGE_PROMOTION")) {
        void loadRankChangePromotions(RANK_CHANGE_MODULES.prosecution);
    }
    if (selectedId === "judicial-rank-change-promotion" && panelChanged && hasMenu("JUDICIAL_RANK_CHANGE_PROMOTION")) {
        void loadRankChangePromotions(RANK_CHANGE_MODULES.judicial);
    }
    if (selectedId === "supervision-rank-change-promotion" && panelChanged && hasMenu("SUPERVISION_RANK_CHANGE_PROMOTION")) {
        void loadRankChangePromotions(RANK_CHANGE_MODULES.supervision);
    }
    if (selectedId === "retirement-processing") {
        selectedId = "retiree-personnel";
        if (location.hash !== "#retiree-personnel") {
            location.hash = "#retiree-personnel";
        }
    }
    if (selectedId === "retiree-personnel" && panelChanged && hasMenu("RETIREE_PERSONNEL")) {
        void loadRetireePersonnel();
    }
    if (selectedId === "retirement-ratio-standards" && panelChanged && hasMenu("RETIREMENT_RATIO_STANDARDS")) {
        void loadRetirementRatioStandards();
    }
    if (selectedId === "retirement-approval-report" && panelChanged && hasMenu("RETIREMENT_APPROVAL_REPORT")) {
        void refreshRetirementApprovalTemplateName();
    }
    if (selectedId === "basic-standards" && panelChanged && hasMenu("BASIC_STANDARDS")) {
        updateBasicStandardPositionCategoryVisibility();
        void refreshBasicStandardPeriods()
            .then(() => refreshBasicStandardPositionCategories())
            .then(() => loadBasicStandards());
    }
    if (selectedId === "basic-salary-standard-adjustment" && panelChanged && hasMenu("BASIC_SALARY_STANDARD_ADJUSTMENT")) {
        void refreshBasicSalaryStandardAdjustmentPeriods();
    }
    if (selectedId === "allowance-standards" && panelChanged && hasMenu("ALLOWANCE_STANDARDS")) {
        void refreshAllowanceStandardPeriods()
            .then(() => refreshAllowanceStandardCategories())
            .then(() => refreshAllowanceStandardPositionCategories())
            .then(() => loadAllowanceStandards());
    }
    if (selectedId === "rank-allowance-standards" && panelChanged && hasMenu("RANK_ALLOWANCE_STANDARDS")) {
        void refreshRankAllowanceStandardPeriods().then(() => loadRankAllowanceStandards());
    }
    if (selectedId === "retained-allowance-standards" && panelChanged && hasMenu("RETAINED_ALLOWANCE_STANDARDS")) {
        void refreshRetainedAllowanceStandardPositionCategories().then(() => loadRetainedAllowanceStandards());
    }
    if (selectedId === "wage-reform-standards" && panelChanged && hasMenu("WAGE_REFORM_STANDARDS")) {
        void refreshWageReformStandardPositionCategories()
            .then(() => refreshWageReformStandardPositions())
            .then(() => loadWageReformStandards());
    }
    if (selectedId === "other-allowance-standards" && panelChanged && hasMenu("OTHER_ALLOWANCE_STANDARDS")) {
        void refreshOtherAllowanceStandardPeriods()
            .then(() => refreshOtherAllowanceStandardPositionCategories())
            .then(() => loadOtherAllowanceStandards());
    }
    if (selectedId === "dictionary-maintenance" && panelChanged && hasMenu("DICTIONARY_MAINTENANCE")) {
        updateDictionaryWriteUi();
        void refreshDictionaryCategories().then(() => loadDictionaries());
    }
    if (selectedId === "security" && panelChanged && hasMenu("SECURITY")) {
        void loadSecurityAdmin();
    }
    if (selectedId === "operation-log" && panelChanged && hasMenu("OPERATION_LOG")) {
        void loadOperationLogs();
    }
    if (selectedId === "organization-maintenance") {
        updateOrgWriteUi();
        if (panelChanged && hasMenu("ORGANIZATION_MAINTENANCE")) {
            void loadOrganizationMaintenance();
        }
    }
    if (selectedId === "data-maintenance" && panelChanged && hasMenu("DATA_MAINTENANCE")) {
        void loadDataMaintenanceDiagnostics();
    }
    if (selectedId === "license-import" && panelChanged && hasMenu("LICENSE_IMPORT")) {
        void loadLicenseStatus();
    }
}

function menuGroupTitle(code) {
    const group = findMenuGroupByCode(code);
    return group ? group.title : "工作台";
}

function renderDashboard() {
    updateDomainChrome();
    const user = state.currentUser;
    const gateGreeting = document.getElementById("domain-gate-greeting");
    if (gateGreeting) {
        gateGreeting.textContent = dashboardGreeting();
    }
    const greetingEl = document.getElementById("dashboard-greeting");
    if (greetingEl) {
        greetingEl.textContent = dashboardGreeting();
    }
    const nameEl = document.getElementById("dashboard-user-name");
    if (nameEl) {
        nameEl.textContent = user?.displayName || user?.username || "工作台";
    }
    const subtitleEl = document.getElementById("dashboard-subtitle");
    if (subtitleEl) {
        subtitleEl.textContent = state.appDomain === DOMAIN_RETIREMENT
            ? "离退业务与数据交换独立于在职域；可办理退休、维护离退休人员并打印审批表。"
            : "按业务分组进入各模块，下方提供常用快捷入口。";
    }
    const scopeEl = document.getElementById("dashboard-scope");
    if (scopeEl) {
        scopeEl.textContent = dashboardScopeText(user);
    }
    void refreshDashboardLicenseSubject();
    const dateEl = document.getElementById("dashboard-date");
    if (dateEl) {
        const now = new Date();
        dateEl.textContent = now.toLocaleDateString("zh-CN", {
            year: "numeric",
            month: "long",
            day: "numeric",
            weekday: "long",
        });
    }
    const domainBadge = document.getElementById("dashboard-domain-badge");
    if (domainBadge) {
        domainBadge.textContent = state.appDomain ? domainLabel(state.appDomain) : "VFP 迁移版";
    }
    if (!state.appDomain) {
        return;
    }
    const groups = currentMenuGroups();
    const domainMenuCodes = new Set(groups.flatMap(group => menuGroupCodes(group)).filter(code => code !== "DASHBOARD"));
    const domainMenus = state.menus.filter(menu => domainMenuCodes.has(menu.code));
    const totalEl = document.getElementById("dashboard-total-functions");
    if (totalEl) {
        totalEl.textContent = `共 ${domainMenus.length} 项可用功能`;
    }
    const menuByCode = new Map(state.menus.map(menu => [menu.code, menu]));
    const groupCounts = {};
    groups.forEach(group => {
        if (group.title === "工作台") {
            return;
        }
        groupCounts[group.title] = menuGroupCodes(group).filter(code => menuByCode.has(code)).length;
    });
    const systemMenuGroup = commonSystemMenuGroup();
    const systemMenuCount = menuGroupCodes(systemMenuGroup).filter(code => menuByCode.has(code)).length;
    const counts = {
        "dashboard-personnel-count": groupCounts["信息维护"] ?? 0,
        "dashboard-payroll-count": groupCounts["工资变动"] ?? groupCounts["政策标准"] ?? 0,
        "dashboard-standard-count": groupCounts["标准维护"] ?? groupCounts["政策标准"] ?? 0,
        "dashboard-report-count": groupCounts["报表打印"] ?? 0,
        "dashboard-system-count": state.appDomain === DOMAIN_RETIREMENT
            ? (groupCounts["数据交换"] ?? 0)
            : systemMenuCount,
    };
    Object.entries(counts).forEach(([id, value]) => {
        const el = document.getElementById(id);
        if (el) {
            el.textContent = value;
        }
    });
    const cardMap = state.appDomain === DOMAIN_RETIREMENT
        ? {
            "dashboard-card-personnel": { title: "信息维护", desc: "离退休待办办理与人员主档。" },
            "dashboard-card-payroll": { title: "政策标准", desc: "折算比例等离退政策标准。" },
            "dashboard-card-report": { title: "报表打印", desc: "退休审批表（三样式）。" },
            "dashboard-card-standard": { title: "数据交换", desc: "离退专用上下级交换。" },
            "dashboard-card-system": { title: "返回选择", desc: "切换回业务域入口。" },
        }
        : {
            "dashboard-card-personnel": { title: "信息维护", desc: "人员查询、维护、任职、学历与考核。" },
            "dashboard-card-payroll": { title: "工资变动", desc: "试算、晋升、职务变化、转正与对账。" },
            "dashboard-card-report": { title: "报表打印", desc: "花名册、审批表与数据交换。" },
            "dashboard-card-standard": { title: "标准维护", desc: "基本工资、津补贴、警衔与套改标准。" },
            "dashboard-card-system": { title: "系统管理", desc: "单位授权、权限、字典与数据维护。" },
        };
    const cardTargets = state.appDomain === DOMAIN_RETIREMENT
        ? {
            "dashboard-card-personnel": firstMenuPath(menuByCode, menuGroupCodes(groups.find(g => g.title === "信息维护") || {})),
            "dashboard-card-payroll": firstMenuPath(menuByCode, menuGroupCodes(groups.find(g => g.title === "政策标准") || {})),
            "dashboard-card-report": firstMenuPath(menuByCode, menuGroupCodes(groups.find(g => g.title === "报表打印") || {})),
            "dashboard-card-standard": firstMenuPath(menuByCode, menuGroupCodes(groups.find(g => g.title === "数据交换") || {})),
            "dashboard-card-system": "#dashboard",
        }
        : {
            "dashboard-card-personnel": firstMenuPath(menuByCode, menuGroupCodes(groups.find(g => g.title === "信息维护") || {})),
            "dashboard-card-payroll": firstMenuPath(menuByCode, menuGroupCodes(groups.find(g => g.title === "工资变动") || {})),
            "dashboard-card-standard": firstMenuPath(menuByCode, menuGroupCodes(groups.find(g => g.title === "标准维护") || {})),
            "dashboard-card-report": firstMenuPath(menuByCode, menuGroupCodes(groups.find(g => g.title === "报表打印") || {})),
            "dashboard-card-system": firstMenuPath(menuByCode, menuGroupCodes(systemMenuGroup)),
        };
    Object.entries(cardMap).forEach(([id, meta]) => {
        const card = document.getElementById(id);
        if (!card) {
            return;
        }
        const titleEl = card.querySelector(".dashboard-card-body > span");
        const descEl = card.querySelector(".dashboard-card-body > p");
        if (titleEl) {
            titleEl.textContent = meta.title;
        }
        if (descEl) {
            descEl.textContent = meta.desc;
        }
    });
    Object.entries(cardTargets).forEach(([id, path]) => {
        const card = document.getElementById(id);
        if (!card) {
            return;
        }
        if (id === "dashboard-card-system" && state.appDomain === DOMAIN_RETIREMENT) {
            card.href = "#dashboard";
            card.dataset.domainReset = "1";
            card.classList.remove("dashboard-card-disabled");
            return;
        }
        delete card.dataset.domainReset;
        if (path) {
            card.href = path;
            card.classList.remove("dashboard-card-disabled");
        } else {
            card.href = "#dashboard";
            card.classList.add("dashboard-card-disabled");
        }
    });
    const quickLinks = document.getElementById("dashboard-quick-links");
    if (quickLinks) {
        const actions = dashboardQuickActions
            .filter(action => !action.domain || action.domain === state.appDomain)
            .map(action => {
                const menu = menuByCode.get(action.code);
                return menu ? { ...action, path: menu.path } : null;
            })
            .filter(Boolean);
        quickLinks.innerHTML = actions.length
            ? actions.map(action => `
                <a class="quick-link" href="${escapeHtml(action.path)}">
                    <strong>${escapeHtml(action.label)}</strong>
                    <span>${escapeHtml(action.desc)}</span>
                </a>
            `).join("")
            : `<div class="dashboard-empty-hint">当前账号在本业务域暂无快捷入口，请联系管理员分配权限。</div>`;
    }
    const groupsContainer = document.getElementById("dashboard-group-links");
    if (groupsContainer) {
        groupsContainer.innerHTML = groups
            .filter(group => group.title !== "工作台")
            .map(group => {
                const links = menuGroupCodes(group)
                    .map(code => menuByCode.get(code))
                    .filter(Boolean);
                if (!links.length) {
                    return "";
                }
                return `
                    <section class="dashboard-group">
                        <div class="dashboard-group-header">
                            <h3>${escapeHtml(group.title)}</h3>
                            <span>${links.length} 项</span>
                        </div>
                        <div class="dashboard-group-links">
                            ${links.map(menu => `
                                <a class="dashboard-group-link" href="${escapeHtml(menu.path)}">
                                    <strong>${escapeHtml(menu.title)}</strong>
                                    <span>${escapeHtml(menuDescriptions[menu.code] || "进入功能页面")}</span>
                                </a>
                            `).join("")}
                        </div>
                    </section>
                `;
            }).join("") || `<div class="dashboard-empty-hint">当前业务域暂无可用功能。</div>`;
    }
}

function dashboardGreeting() {
    const hour = new Date().getHours();
    if (hour < 6) {
        return "夜深了";
    }
    if (hour < 12) {
        return "早上好";
    }
    if (hour < 14) {
        return "中午好";
    }
    if (hour < 18) {
        return "下午好";
    }
    return "晚上好";
}

function dashboardScopeText(user) {
    if (!user) {
        return "";
    }
    if (user.allOrganizations) {
        return `登录账号：${user.username} · 数据范围：全部单位`;
    }
    if (user.homeOrganizationCode) {
        return `登录账号：${user.username} · 数据范围：${user.homeOrganizationCode} 及下属单位`;
    }
    const count = user.organizationCodes?.length || 0;
    if (count === 0) {
        return `登录账号：${user.username} · 数据范围：未配置单位，可能无法查询数据`;
    }
    if (count <= 3) {
        return `登录账号：${user.username} · 数据范围：${Array.from(user.organizationCodes).join("、")}`;
    }
    return `登录账号：${user.username} · 数据范围：${count} 个单位`;
}

function firstMenuPath(menuByCode, codes) {
    for (const code of codes) {
        const menu = menuByCode.get(code);
        if (menu?.path) {
            return menu.path;
        }
    }
    return null;
}

async function initializeDictionaryPickers() {
    if (!hasPersonnelWrite() && !hasRetirementWrite() && !hasRetirementRead()) {
        return;
    }
    if (hasPersonnelWrite()) {
        initializeOrganizationPickerInput();
    }
    try {
        await loadDictionaryFieldConfigs();
        if (hasPersonnelWrite()) {
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
                gwfl: "051",
                zgxl: "002",
                zwjb: "026",
                xrzw: "051",
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
                    input.type = "text";
                    input.placeholder = input.placeholder || "例如 1980-01";
                    input.dataset.monthField = "true";
                    if (window.MonthPicker) {
                        MonthPicker.enhance(input);
                    }
                    // Date fields use month picker, not dictionary tree.
                    return;
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
                    button.addEventListener("click", () => openDictionaryPicker(inputId, {
                        ...config,
                        dictionaryFieldKey: fieldName,
                    }));
                    combo.appendChild(button);
                }
            });
        }
        initializeRetireeDictionaryPickers();
        ensureRetireeGradeSelectOptions();
    } catch (error) {
        console.warn("字典字段配置加载失败", error);
    }
}

function hasRetirementRead() {
    return hasPermission("RETIREMENT_READ");
}

function initializeRetireeDictionaryPickers() {
    const bindings = [
        { fieldName: "gwfl", inputId: "retiree-maint-post-category", buttonId: "retiree-maint-post-category-picker", caption: "岗位分类", fallbackPrefix: "051" },
        {
            fieldName: "xrzw",
            inputId: "retiree-maint-position-name",
            buttonId: "retiree-maint-position-picker",
            caption: "职务岗位",
            fallbackPrefix: "051",
            linkedCodeInputId: "retiree-maint-position-code",
            useFullDictionaryCode: true,
        },
    ];
    bindings.forEach(({ fieldName, inputId, buttonId, caption, fallbackPrefix, linkedCodeInputId, useFullDictionaryCode }) => {
        const input = document.getElementById(inputId);
        const button = document.getElementById(buttonId);
        if (!input || !button || button.dataset.pickerBound) {
            return;
        }
        const configured = state.dictionaryFieldConfigs?.[fieldName];
        const config = {
            fieldName,
            caption: configured?.caption || caption,
            dictionaryPrefix: configured?.dictionaryPrefix || fallbackPrefix,
            dictionaryFieldKey: fieldName,
            linkedCodeInputId: linkedCodeInputId || null,
            useFullDictionaryCode: !!useFullDictionaryCode,
        };
        input.dataset.dictionaryPrefix = config.dictionaryPrefix;
        input.dataset.dictionaryField = fieldName;
        button.dataset.pickerBound = "1";
        button.addEventListener("click", () => openDictionaryPicker(inputId, config));
    });
}

const ensureRetireeDictionaryPickers = initializeRetireeDictionaryPickers;

const RETIREE_FIXED_SALARY_STANDARD = "2006.07";
const RETIREE_FIXED_ALLOWANCE_STANDARD = "2014.01";

function parseYearMonthParts(value) {
    const digits = String(value || "").replace(/\D/g, "");
    if (digits.length < 6) {
        return null;
    }
    const year = Number(digits.slice(0, 4));
    const month = Number(digits.slice(4, 6));
    if (!Number.isFinite(year) || !Number.isFinite(month) || month < 1 || month > 12) {
        return null;
    }
    return { year, month };
}

function calcRetireeSalaryYears(workStart, retirementDate, interruptedYears) {
    const start = parseYearMonthParts(workStart);
    const end = parseYearMonthParts(retirementDate);
    if (!start || !end) {
        return 0;
    }
    const months = (end.year - start.year) * 12 + (end.month - start.month);
    const years = Math.max(0, Math.floor(Math.max(0, months) / 12));
    return Math.max(0, years - Math.max(0, Number(interruptedYears || 0)));
}

function refreshRetireeSalaryYears() {
    const workStart = document.getElementById("retiree-maint-work-start")?.value || "";
    const retirementDate = document.getElementById("retiree-maint-retirement-date")?.value || "";
    const interrupted = document.getElementById("retiree-maint-interrupted")?.value || 0;
    const salaryYears = document.getElementById("retiree-maint-salary-years");
    if (!salaryYears) {
        return;
    }
    salaryYears.value = String(calcRetireeSalaryYears(workStart, retirementDate, interrupted));
}

function bindRetireeSalaryYearsAutoCalc() {
    ["retiree-maint-work-start", "retiree-maint-retirement-date", "retiree-maint-interrupted"].forEach(id => {
        const el = document.getElementById(id);
        if (!el || el.dataset.salaryYearsBound) {
            return;
        }
        el.dataset.salaryYearsBound = "1";
        el.addEventListener("change", refreshRetireeSalaryYears);
        el.addEventListener("input", refreshRetireeSalaryYears);
    });
}

function ensureRetireeGradeSelectOptions(minLevel = 1, maxLevel = 27, currentLevel = "", currentStep = "") {
    const levelSelect = document.getElementById("retiree-maint-grade-level");
    const stepSelect = document.getElementById("retiree-maint-grade-step");
    if (levelSelect) {
        if (minLevel == null || maxLevel == null || !Number.isFinite(Number(minLevel)) || !Number.isFinite(Number(maxLevel))) {
            levelSelect.innerHTML = `<option value="">请选择</option>`;
            levelSelect.value = "";
        } else {
            fillNumberSelect(levelSelect, minLevel, maxLevel, currentLevel);
        }
    }
    if (stepSelect) {
        const keepStep = currentStep || stepSelect.value || "";
        fillNumberSelect(stepSelect, 1, 65, keepStep);
    }
}

async function refreshRetireeLevelOptionsByPosition(options = {}) {
    const keepCurrent = options.keepCurrent !== false;
    const levelSelect = document.getElementById("retiree-maint-grade-level");
    const positionCode = document.getElementById("retiree-maint-position-code")?.value?.trim() || "";
    const postCategory = document.getElementById("retiree-maint-post-category")?.value || "";
    const currentLevel = keepCurrent ? (levelSelect?.value || "") : "";
    const canWrite = state.currentRetireeDetail?.editable && hasRetirementWrite();

    if (!levelSelect) {
        return;
    }

    // 旧系统：仅行政公务员有级别；工人/事业无级别
    if (!isRetireeCivilServantLevelApplicable(postCategory)) {
        levelSelect.innerHTML = `<option value="">不适用</option>`;
        levelSelect.value = "";
        levelSelect.disabled = true;
        return;
    }

    if (!positionCode) {
        levelSelect.innerHTML = `<option value="">请先选择职务</option>`;
        levelSelect.value = "";
        levelSelect.disabled = true;
        return;
    }

    try {
        const range = await getJson(
            `/api/retirement/position-level-range?positionCode=${encodeURIComponent(positionCode)}`);
        if (!range?.applicable || range.minimumLevel == null || range.maximumLevel == null) {
            levelSelect.innerHTML = `<option value="">该职务无级别范围</option>`;
            levelSelect.value = "";
            levelSelect.disabled = true;
            return;
        }
        ensureRetireeGradeSelectOptions(
            range.minimumLevel,
            range.maximumLevel,
            currentLevel,
            document.getElementById("retiree-maint-grade-step")?.value || "");
        // 当前级别若不在新范围内，清空以便重选
        if (currentLevel) {
            const min = Number(range.minimumLevel);
            const max = Number(range.maximumLevel);
            const cur = Number(currentLevel);
            if (!Number.isFinite(cur) || cur < Math.min(min, max) || cur > Math.max(min, max)) {
                levelSelect.value = "";
            }
        }
        levelSelect.disabled = !canWrite;
    } catch (error) {
        console.warn("加载职务级别范围失败", error);
        levelSelect.innerHTML = `<option value="">级别范围加载失败</option>`;
        levelSelect.value = "";
        levelSelect.disabled = true;
    }
}

function isRetireeWorkerPostCategory(value) {
    const text = String(value || "").trim();
    return ["机关技术工人", "机关普通工人", "事业技术工人", "事业普通工人", "技术工岗位", "普通工岗位"]
        .includes(text);
}

function isRetireeCivilServantLevelApplicable(postCategory) {
    const text = String(postCategory || "").trim();
    if (!text) {
        return true; // 未选岗位分类时，仍按职务级别范围判断
    }
    if (isRetireeWorkerPostCategory(text)) {
        return false;
    }
    if (text.includes("事业")) {
        return false;
    }
    return true;
}

function refreshRetireeLevelEnabled() {
    void refreshRetireeLevelOptionsByPosition({ keepCurrent: true });
}

function bindRetireePostCategoryLevelRule() {
    const postCategory = document.getElementById("retiree-maint-post-category");
    const positionCode = document.getElementById("retiree-maint-position-code");
    if (postCategory && !postCategory.dataset.levelRuleBound) {
        postCategory.dataset.levelRuleBound = "1";
        postCategory.addEventListener("change", () => {
            void refreshRetireeLevelOptionsByPosition({ keepCurrent: false });
        });
    }
    if (positionCode && !positionCode.dataset.levelRuleBound) {
        positionCode.dataset.levelRuleBound = "1";
        positionCode.addEventListener("change", () => {
            void refreshRetireeLevelOptionsByPosition({ keepCurrent: false });
        });
    }
}

function fillNumberSelect(select, min, max, currentValue) {
    const current = String(currentValue == null ? "" : currentValue).trim();
    const start = Number.isFinite(Number(min)) ? Number(min) : 1;
    const end = Number.isFinite(Number(max)) ? Number(max) : start;
    const lo = Math.min(start, end);
    const hi = Math.max(start, end);
    const values = new Set();
    for (let i = lo; i <= hi; i += 1) {
        values.add(String(i));
    }
    if (current && !values.has(current)) {
        values.add(current);
    }
    const sorted = [...values].sort((a, b) => Number(a) - Number(b));
    select.innerHTML = `<option value="">请选择</option>${sorted.map(v =>
        `<option value="${escapeHtml(v)}">${escapeHtml(v)}</option>`).join("")}`;
    select.value = current;
}

function toMonthControlValue(value) {
    const raw = String(value || "").trim();
    if (!raw) {
        return "";
    }
    const digits = raw.replace(/\D/g, "");
    if (digits.length >= 6) {
        return `${digits.slice(0, 4)}-${digits.slice(4, 6)}`;
    }
    const match = raw.match(/^(\d{4})[.\-/](\d{1,2})/);
    if (match) {
        return `${match[1]}-${String(match[2]).padStart(2, "0")}`;
    }
    return "";
}

function fromMonthControlValue(value) {
    const raw = String(value || "").trim();
    return raw ? raw.replace("-", ".") : "";
}

function ensureSelectHasValue(selectId, value) {
    const select = document.getElementById(selectId);
    if (!select) {
        return;
    }
    const text = String(value || "").trim();
    if (text && ![...select.options].some(opt => opt.value === text)) {
        const option = document.createElement("option");
        option.value = text;
        option.textContent = text;
        select.appendChild(option);
    }
    select.value = text;
}

async function loadDictionaryFieldConfigs() {
    const [basicConfigs, positionConfigs] = await Promise.all([
        getJson("/api/dictionaries/field-configs?tableName=dryjbxx"),
        getJson("/api/dictionaries/field-configs?tableName=dryzwbh"),
    ]);
    state.dictionaryFieldConfigs = Object.fromEntries(
        [...(basicConfigs || []), ...(positionConfigs || [])]
            .map(config => [String(config.fieldName || "").toLowerCase(), config]),
    );
}

function initializeOrganizationPickerInput() {
    // 特殊 target 必须先绑；其余单位选择器按页面上的 .organization-picker-button 自动发现，避免漏绑。
    const pickerConfigs = [
        { inputId: "maint-organization-name", buttonId: "maint-organization-picker-button", target: "maintenance" },
        { inputId: "data-exchange-submission-organization", buttonId: "data-exchange-submission-organization-picker-button", target: "dataExchangeSubmission" },
        { inputId: "data-exchange-approval-organization", buttonId: "data-exchange-approval-organization-picker-button", target: "dataExchangeApproval" },
        { inputId: "organization-code", buttonId: "organization-code-picker-button" },
        { inputId: "system-setup-import-organization", buttonId: "system-setup-import-organization-picker-button" },
    ];
    pickerConfigs.forEach(config => bindOrganizationPickerInput(config.inputId, config.buttonId, config.target || config.inputId));
    document.querySelectorAll("button.organization-picker-button").forEach(button => {
        if (button.dataset.pickerBound) {
            return;
        }
        const combo = button.closest(".dict-input-combo");
        const input = combo?.querySelector("input[id]");
        if (!input) {
            return;
        }
        bindOrganizationPickerInput(input.id, button.id || "", input.id);
    });
}

function bindOrganizationPickerInput(inputId, buttonId, target) {
    const input = document.getElementById(inputId);
    if (!input) {
        return;
    }
    input.readOnly = true;
    if (!input.placeholder) {
        input.placeholder = "请选择单位或留空";
    }
    const combo = input.closest(".dict-input-combo");
    const wrapper = combo || input.closest("label") || input.parentElement;
    let button = document.getElementById(buttonId) || combo?.querySelector(".organization-picker-button") || wrapper?.querySelector(".organization-picker-button");
    if (!button && wrapper) {
        const comboWrap = document.createElement("div");
        comboWrap.className = "dict-input-combo";
        if (input.parentElement === wrapper) {
            wrapper.insertBefore(comboWrap, input);
            comboWrap.appendChild(input);
        } else {
            input.parentElement?.replaceWith(comboWrap);
            comboWrap.appendChild(input);
        }
        button = document.createElement("button");
        button.type = "button";
        button.id = buttonId;
        button.className = "dict-picker-button organization-picker-button";
        button.setAttribute("aria-label", "选择单位");
        button.textContent = "⌄";
        comboWrap.appendChild(button);
    }
    const pickerTarget = target || inputId;
    if (button && !button.dataset.pickerBound) {
        button.addEventListener("click", () => openOrganizationPicker(pickerTarget));
        button.dataset.pickerBound = "true";
    }
    if (!input.dataset.pickerBound) {
        input.addEventListener("click", () => openOrganizationPicker(pickerTarget));
        input.addEventListener("focus", () => openOrganizationPicker(pickerTarget));
        input.addEventListener("keydown", event => {
            if (event.key === "Backspace" || event.key === "Delete") {
                clearOrganizationInput(input);
                if (input.id === "organization-code") {
                    state.personnelPage = 0;
                    void loadPersonnel();
                }
                event.preventDefault();
            }
        });
        input.dataset.pickerBound = "true";
    }
}

function clearOrganizationInput(input) {
    if (!input) {
        return;
    }
    input.value = "";
    delete input.dataset.organizationCode;
    input.title = "";
}

async function openOrganizationPicker(target = "maintenance") {
    state.activeOrganizationTarget = target;
    const modal = document.getElementById("organization-picker-modal");
    if (modal.parentElement !== document.body) {
        document.body.appendChild(modal);
    }
    document.getElementById("organization-picker-title").textContent = target === "personnelTransfer" ? "选择调往单位" : "选择单位";
    document.getElementById("organization-picker-subtitle").textContent = target === "personnelTransfer"
        ? "从单位树中选择调往本地其他单位，支持按单位名称或编码搜索。"
        : target === "dataExchangeDispatch"
            ? "从单位树中选择一个或多个下发单位，支持按单位名称或编码搜索。"
            : target === "dataExchangeSubmission"
                ? "从单位树中选择一个或多个申报单位，支持按单位名称或编码搜索。"
                : target === "dataExchangeApproval"
                    ? "从单位树中选择一个或多个审批下发单位，支持按单位名称或编码搜索。"
                    : target === "licenseIssue"
                        ? "可选：从单位库带入签约主体编码/名称；也可直接手填地区或客户名。"
                        : "从单位树中选择单位，支持按单位名称或编码搜索；留空表示不限单位。";
    document.getElementById("organization-picker-filter").value = "";
    document.getElementById("organization-picker-tree").innerHTML = "正在加载单位...";
    modal.classList.remove("hidden");
    try {
        state.organizationNodes = await getJson("/api/organizations/tree");
        state.organizationExpandedCodes = new Set();
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
        button.addEventListener("click", event => {
            if (button.dataset.hasChildren === "true" && event.target.tagName === "EM") {
                toggleOrganizationNode(button.dataset.orgCode);
                return;
            }
            selectOrganizationNode(button.dataset.orgCode || "", button.dataset.orgName || button.dataset.orgCode || "");
            closeOrganizationPicker();
        });
    });
}

function selectOrganizationNode(code, name) {
    const target = state.activeOrganizationTarget;
    if (target === "personnelTransfer") {
        const pending = state.pendingPersonnelChange;
        state.pendingPersonnelChange = null;
        if (pending) {
            continuePersonnelChangeMaintenance(pending.uid, pending.name, pending.changeType, pending.changeDescription, { code, name });
        }
        return;
    }
    if (target === "maintenance") {
        document.getElementById("maint-organization-code").value = code;
        document.getElementById("maint-organization-name").value = name || code;
        return;
    }
    if (target === "dataExchangeDispatch") {
        if (!state.dataExchangeDispatchOrganizations.some(item => item.code === code)) {
            state.dataExchangeDispatchOrganizations.push({ code, name: name || code });
            renderDataExchangeDispatchOrganizations();
        }
        clearOrganizationInput(document.getElementById("data-exchange-dispatch-organization"));
        return;
    }
    if (target === "dataExchangeSubmission") {
        if (!state.dataExchangeSubmissionOrganizations.some(item => item.code === code)) {
            state.dataExchangeSubmissionOrganizations.push({ code, name: name || code });
            renderDataExchangeSubmissionOrganizations();
        }
        clearOrganizationInput(document.getElementById("data-exchange-submission-organization"));
        return;
    }
    if (target === "dataExchangeApproval") {
        if (!state.dataExchangeApprovalOrganizations.some(item => item.code === code)) {
            state.dataExchangeApprovalOrganizations.push({ code, name: name || code });
            renderDataExchangeApprovalOrganizations();
        }
        clearOrganizationInput(document.getElementById("data-exchange-approval-organization"));
        return;
    }
    if (target === "licenseIssue") {
        setOrganizationInput("license-issue-org", code, name || code);
        document.getElementById("license-issue-code").value = code || "";
        document.getElementById("license-issue-name").value = name || code || "";
        void refreshLicenseIssuePreview();
        return;
    }
    const input = document.getElementById(target);
    if (input) {
        input.value = name || code;
        input.dataset.organizationCode = code;
        input.title = name ? `${name} (${code})` : code;
        if (target === "assessment-batch-organization-code") {
            void loadAssessmentBatch();
        }
        if (target === "assessment-summary-organization-code") {
            void loadAssessmentSummary();
        }
        if (target === "organization-code") {
            state.personnelPage = 0;
            void loadPersonnel();
        }
    }
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
        state.activeDictionaryNodes = await getJson(dictionaryTreeRequestUrl(config));
        state.dictionaryExpandedCodes = new Set();
        renderDictionaryPickerTree();
    } catch (error) {
        document.getElementById("dictionary-picker-tree").innerHTML = `<div class="status error">${escapeHtml(error.message)}</div>`;
    }
}

function dictionaryPickerContext() {
    const person = state.activePersonnelMaintenance;
    return {
        unitCategory: person?.organizationCategory || "",
        organizationProperty: person?.organizationType
            || document.getElementById("maint-organization-type")?.value?.trim()
            || "",
        organizationCode: person?.organizationCode
            || document.getElementById("maint-organization-code")?.value?.trim()
            || "",
    };
}

function dictionaryTreeRequestUrl(config) {
    const fieldKey = String(config.dictionaryFieldKey || config.fieldName || "").toLowerCase();
    const prefix = config.dictionaryPrefix || null;
    if (dictionaryFilterFields.has(fieldKey)) {
        const context = dictionaryPickerContext();
        const params = new URLSearchParams({
            field: fieldKey,
            unitCategory: context.unitCategory,
            organizationProperty: context.organizationProperty,
            organizationCode: context.organizationCode,
        });
        if (prefix) {
            params.set("prefix", prefix);
        }
        return `/api/dictionaries/tree?${params}`;
    }
    if (!prefix) {
        throw new Error("缺少字典前缀");
    }
    return `/api/dictionaries/tree?prefix=${encodeURIComponent(prefix)}`;
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
            selectDictionaryNode({
                code: button.dataset.dictCode,
                value: button.dataset.dictValue,
                name: button.dataset.dictName,
            });
        });
    });
}

function resolveDictionaryCode(node, config) {
    const fullCode = String(node.code || "").trim();
    if (config?.useFullDictionaryCode) {
        const maxLen = config.codeMaxLength;
        if (maxLen && fullCode.length > maxLen) {
            return fullCode.slice(-maxLen);
        }
        return fullCode;
    }
    return String(node.value || "").trim();
}

function selectDictionaryNode(node) {
    const target = state.activeDictionaryTarget;
    if (!target) {
        return;
    }
    const config = target.config || {};
    const code = resolveDictionaryCode(node, config);
    const name = node.name || "";
    const input = document.getElementById(target.inputId);
    if (input) {
        if (config.codeTarget) {
            input.value = code;
        } else if (target.inputId === "maint-education-code" || target.inputId === "maint-rank-code") {
            input.value = node.value || code;
        } else {
            input.value = name;
        }
    }
    if (config.linkedCodeField) {
        const linkedInput = document.getElementById(`subrecord-field-${config.linkedCodeField}`)
            || document.getElementById(config.linkedCodeInputId);
        if (linkedInput) {
            linkedInput.value = code;
            linkedInput.dispatchEvent(new Event("change", { bubbles: true }));
        }
    } else if (config.linkedCodeInputId) {
        const linkedInput = document.getElementById(config.linkedCodeInputId);
        if (linkedInput) {
            linkedInput.value = code || node.value || "";
            linkedInput.dispatchEvent(new Event("change", { bubbles: true }));
        }
    }
    input.dispatchEvent(new Event("change", { bubbles: true }));
    closeDictionaryPicker();
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

async function onCreateUser(event) {
    event.preventDefault();
    const status = document.getElementById("security-create-user-status");
    status.className = "status";
    status.textContent = "正在创建...";
    try {
        await postJson("/api/security/users", {
            username: document.getElementById("new-username").value.trim(),
            displayName: document.getElementById("new-display-name").value.trim(),
            password: document.getElementById("new-password").value,
            enabled: true,
        });
        event.target.reset();
        closeSecurityModal("security-create-user-modal");
        state.security.userPageIndex = 0;
        await loadSecurityAdmin({ statusMessage: "用户创建成功" });
    } catch (error) {
        showError(status, error);
    }
}

async function onCreateRole(event) {
    event.preventDefault();
    const status = document.getElementById("security-create-role-status");
    status.className = "status";
    status.textContent = "正在创建...";
    try {
        await postJson("/api/security/roles", {
            code: document.getElementById("new-role-code").value.trim(),
            name: document.getElementById("new-role-name").value.trim(),
        });
        event.target.reset();
        closeSecurityModal("security-create-role-modal");
        state.security.rolePageIndex = 0;
        state.security.allRoles = [];
        await loadSecurityAdmin({ statusMessage: "角色创建成功" });
    } catch (error) {
        showError(status, error);
    }
}

async function onCreateMenu(event) {
    event.preventDefault();
    const status = document.getElementById("security-create-menu-status");
    status.className = "status";
    status.textContent = "正在创建...";
    try {
        await postJson("/api/security/menus", {
            code: document.getElementById("new-menu-code").value.trim(),
            title: document.getElementById("new-menu-title").value.trim(),
            path: document.getElementById("new-menu-path").value.trim(),
            permissionCode: document.getElementById("new-menu-permission").value.trim(),
            parentId: (() => {
                const raw = document.getElementById("new-menu-parent")?.value;
                return raw ? Number(raw) : null;
            })(),
            sortOrder: Number(document.getElementById("new-menu-sort").value || 0),
            enabled: true,
        });
        event.target.reset();
        closeSecurityModal("security-create-menu-modal");
        state.security.menuPageIndex = 0;
        await loadSecurityAdmin({ statusMessage: "菜单创建成功" });
    } catch (error) {
        showError(status, error);
    }
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
    state.personnelPage = 0;
    await loadPersonnel();
}

async function onPersonnelMaintenanceSave(event) {
    event.preventDefault();
    const uid = document.getElementById("personnel-maintenance-uid").value;
    const payload = personnelMaintenancePayload();
    const status = document.getElementById("personnel-status");
    status.className = "status";
    status.textContent = "正在保存人员信息...";
    try {
        const saved = uid ? await putJson(`/api/personnel/${uid}`, payload) : await postJson("/api/personnel", payload);
        status.textContent = `保存成功：${saved.name}（${saved.organizationCode}-${saved.personCode}）`;
        showAppToast(status.textContent);
        fillPersonnelMaintenanceForm(saved);
        await loadPersonnelSubrecords(saved.uid, saved.organizationCode, saved.personCode);
        await loadPersonnel();
    } catch (error) {
        showError(status, error);
    }
}

async function onPersonnelWageProjectionSearch(event) {
    event.preventDefault();
    const person = state.activePersonnelMaintenance;
    if (!person?.uid) {
        alert("请先选择或保存人员。");
        return;
    }
    const submitButton = document.getElementById("maint-wage-projection-submit");
    const originalLabel = submitButton?.textContent || "执行工资推算";
    if (submitButton) {
        submitButton.disabled = true;
        submitButton.textContent = "推算中…";
    }
    try {
        await loadPersonnelWageProjection(person.uid);
    } finally {
        if (submitButton) {
            submitButton.disabled = false;
            submitButton.textContent = originalLabel;
        }
    }
}

function openNewPersonnelMaintenance() {
    resetPersonnelMaintenanceForm();
    openPersonnelMaintenanceModal("新增人员", "填写人员基本信息后保存；保存成功后可继续维护多条附属记录。");
}

function mountModalOverlaysToBody(ids) {
    ids.forEach(id => {
        const el = document.getElementById(id);
        if (el && el.parentElement !== document.body) {
            document.body.appendChild(el);
        }
    });
}

function openPersonnelMaintenanceModal(title, subtitle, readonly = false) {
    mountModalOverlaysToBody([
        "personnel-maintenance-modal",
        "dictionary-picker-modal",
        "subrecord-editor-modal",
        "personnel-change-remark-modal",
        "missing-assessment-fill-modal",
    ]);
    setPersonnelMaintenanceReadonly(readonly);
    document.getElementById("personnel-maintenance-modal-title").textContent = title;
    document.getElementById("personnel-maintenance-modal-subtitle").textContent = subtitle;
    document.getElementById("personnel-maintenance-modal").classList.remove("hidden");
    showPersonnelTab("basic");
}

function closePersonnelMaintenanceModal() {
    document.getElementById("personnel-maintenance-modal").classList.add("hidden");
    setPersonnelMaintenanceReadonly(false);
}

function setPersonnelMaintenanceReadonly(readonly) {
    state.personnelMaintenanceReadonly = !!readonly;
    const modal = document.getElementById("personnel-maintenance-modal");
    modal?.classList.toggle("personnel-maintenance-readonly", !!readonly);
    modal?.querySelectorAll("#personnel-maintenance-form input, #personnel-maintenance-form select, #personnel-maintenance-form textarea")
        .forEach(el => {
            if (el.id === "personnel-maintenance-uid" || el.id === "maint-organization-code") {
                return;
            }
            if (readonly) {
                el.setAttribute("readonly", "readonly");
                el.setAttribute("disabled", "disabled");
            } else if (el.id !== "maint-organization-name") {
                el.removeAttribute("readonly");
                el.removeAttribute("disabled");
            }
            if (window.MonthPicker && (el.dataset.monthField === "true" || el.dataset.monthPickerEnhanced === "1")) {
                MonthPicker.syncToggleState(el);
            }
        });
    const orgPicker = document.getElementById("maint-organization-picker-button");
    if (orgPicker) {
        orgPicker.disabled = !!readonly;
    }
    if (window.MonthPicker) {
        MonthPicker.enhanceAll(document.getElementById("personnel-maintenance-modal"));
    }
}

function showPersonnelTab(tabName) {
    document.querySelectorAll("[data-personnel-tab]").forEach(button => {
        button.classList.toggle("active", button.dataset.personnelTab === tabName);
    });
    ["basic", "projection", "education", "position", "transfer", "current-payroll", "payroll", "assessment", "award", "rank-level", "wage-reform", "pre-reform"].forEach(name => {
        document.getElementById(`personnel-tab-${name}`).classList.toggle("hidden", name !== tabName);
    });
    const tabBody = document.querySelector("#personnel-maintenance-modal .personnel-tab-body");
    if (tabBody) {
        tabBody.scrollTop = 0;
    }
}

async function onAssessmentSummarySearch(event) {
    event.preventDefault();
    document.getElementById("assessment-summary-page").value = "0";
    await loadAssessmentSummary();
}

function syncAssessmentSummaryFiltersFromEntry() {
    const batchOrg = document.getElementById("assessment-batch-organization-code");
    const summaryOrg = document.getElementById("assessment-summary-organization-code");
    const batchYear = document.getElementById("assessment-batch-year");
    const summaryYear = document.getElementById("assessment-summary-year");
    const batchInclude = document.getElementById("assessment-batch-include-descendants");
    const summaryInclude = document.getElementById("assessment-summary-include-descendants");
    if (summaryOrg && batchOrg) {
        const code = (batchOrg.dataset.organizationCode || "").trim();
        const name = batchOrg.value.trim();
        if (code) {
            setOrganizationInput("assessment-summary-organization-code", code, name || code);
        } else if (name) {
            setOrganizationInput("assessment-summary-organization-code", name, name);
        }
    }
    if (summaryYear && batchYear?.value.trim()) {
        summaryYear.value = batchYear.value.trim();
    }
    if (summaryInclude && batchInclude) {
        summaryInclude.checked = batchInclude.checked;
    }
}

function switchAssessmentTab(view) {
    const showSummary = view === "summary";
    document.getElementById("assessment-entry-view").classList.toggle("hidden", showSummary);
    document.getElementById("assessment-summary-view").classList.toggle("hidden", !showSummary);
    document.getElementById("assessment-tab-entry").classList.toggle("active", !showSummary);
    document.getElementById("assessment-tab-summary").classList.toggle("active", showSummary);
    if (showSummary) {
        syncAssessmentSummaryFiltersFromEntry();
        void loadAssessmentSummary();
    }
}

async function onChangedPersonnelSearch(event) {
    event.preventDefault();
    state.changedPersonnelPage = 0;
    await loadChangedPersonnel();
}

function gotoChangedPersonnelPage(page) {
    const totalPages = Math.max(state.changedPersonnelTotalPages || 1, 1);
    const target = Math.min(Math.max(page, 0), totalPages - 1);
    if (target === state.changedPersonnelPage) {
        return;
    }
    state.changedPersonnelPage = target;
    void loadChangedPersonnel();
}

function renderChangedPersonnelPagination(totalElements, totalPages) {
    const bar = document.getElementById("changed-personnel-pagination");
    if (!bar) {
        return;
    }
    const pages = Math.max(totalPages || 1, 1);
    state.changedPersonnelTotalPages = pages;
    const current = state.changedPersonnelPage;
    bar.classList.toggle("hidden", !totalElements);
    const totalPagesEl = document.getElementById("changed-personnel-total-pages");
    const totalCountEl = document.getElementById("changed-personnel-total-count");
    const pageInput = document.getElementById("changed-personnel-page-input");
    if (totalPagesEl) {
        totalPagesEl.textContent = String(pages);
    }
    if (totalCountEl) {
        totalCountEl.textContent = String(totalElements || 0);
    }
    if (pageInput) {
        pageInput.value = String(current + 1);
        pageInput.max = String(pages);
        pageInput.disabled = !totalElements;
    }
    const noData = !totalElements;
    document.getElementById("changed-personnel-first").disabled = noData || current <= 0;
    document.getElementById("changed-personnel-prev").disabled = noData || current <= 0;
    document.getElementById("changed-personnel-next").disabled = noData || current >= pages - 1;
    document.getElementById("changed-personnel-last").disabled = noData || current >= pages - 1;
}

async function onPositionHistorySearch(event) {
    event.preventDefault();
    state.positionHistoryPage = 0;
    await loadPositionHistory();
}

function gotoPositionHistoryPage(page) {
    const totalPages = Math.max(state.positionHistoryTotalPages || 1, 1);
    const target = Math.min(Math.max(page, 0), totalPages - 1);
    if (target === state.positionHistoryPage) {
        return;
    }
    state.positionHistoryPage = target;
    void loadPositionHistory();
}

function renderPositionHistoryPagination(totalElements, totalPages) {
    const bar = document.getElementById("position-history-pagination");
    if (!bar) {
        return;
    }
    const pages = Math.max(totalPages || 1, 1);
    state.positionHistoryTotalPages = pages;
    const current = state.positionHistoryPage;
    bar.classList.toggle("hidden", !totalElements);
    const totalPagesEl = document.getElementById("position-history-total-pages");
    const totalCountEl = document.getElementById("position-history-total-count");
    const pageInput = document.getElementById("position-history-page-input");
    if (totalPagesEl) {
        totalPagesEl.textContent = String(pages);
    }
    if (totalCountEl) {
        totalCountEl.textContent = String(totalElements || 0);
    }
    if (pageInput) {
        pageInput.value = String(current + 1);
        pageInput.max = String(pages);
        pageInput.disabled = !totalElements;
    }
    const noData = !totalElements;
    document.getElementById("position-history-first").disabled = noData || current <= 0;
    document.getElementById("position-history-prev").disabled = noData || current <= 0;
    document.getElementById("position-history-next").disabled = noData || current >= pages - 1;
    document.getElementById("position-history-last").disabled = noData || current >= pages - 1;
}

let positionHistoryOptionsLoaded = false;

async function ensurePositionHistoryOptions() {
    if (positionHistoryOptionsLoaded) {
        return;
    }
    try {
        const options = await getJson("/api/personnel/comprehensive-query-options");
        const positions = [...(options.positions || [])].sort((a, b) =>
            String(a?.code ?? "").localeCompare(String(b?.code ?? ""), "zh-CN", { numeric: true, sensitivity: "base" }));
        fillPersonnelComprehensiveSelect("position-history-position-code", positions);
        positionHistoryOptionsLoaded = true;
    } catch (error) {
        console.warn("加载任职岗位下拉选项失败", error);
    }
}

async function onEducationHistorySearch(event) {
    event.preventDefault();
    state.educationHistoryPage = 0;
    await loadEducationHistory();
}

function gotoEducationHistoryPage(page) {
    const totalPages = Math.max(state.educationHistoryTotalPages || 1, 1);
    const target = Math.min(Math.max(page, 0), totalPages - 1);
    if (target === state.educationHistoryPage) {
        return;
    }
    state.educationHistoryPage = target;
    void loadEducationHistory();
}

function renderEducationHistoryPagination(totalElements, totalPages) {
    const bar = document.getElementById("education-history-pagination");
    if (!bar) {
        return;
    }
    const pages = Math.max(totalPages || 1, 1);
    state.educationHistoryTotalPages = pages;
    const current = state.educationHistoryPage;
    bar.classList.toggle("hidden", !totalElements);
    const totalPagesEl = document.getElementById("education-history-total-pages");
    const totalCountEl = document.getElementById("education-history-total-count");
    const pageInput = document.getElementById("education-history-page-input");
    if (totalPagesEl) {
        totalPagesEl.textContent = String(pages);
    }
    if (totalCountEl) {
        totalCountEl.textContent = String(totalElements || 0);
    }
    if (pageInput) {
        pageInput.value = String(current + 1);
        pageInput.max = String(pages);
        pageInput.disabled = !totalElements;
    }
    const noData = !totalElements;
    document.getElementById("education-history-first").disabled = noData || current <= 0;
    document.getElementById("education-history-prev").disabled = noData || current <= 0;
    document.getElementById("education-history-next").disabled = noData || current >= pages - 1;
    document.getElementById("education-history-last").disabled = noData || current >= pages - 1;
}

let educationHistoryOptionsLoaded = false;

async function ensureEducationHistoryOptions() {
    if (educationHistoryOptionsLoaded) {
        return;
    }
    try {
        const options = await getJson("/api/personnel/comprehensive-query-options");
        const educations = [...(options.educations || [])].sort((a, b) =>
            String(a?.code ?? "").localeCompare(String(b?.code ?? ""), "zh-CN", { numeric: true, sensitivity: "base" }));
        fillPersonnelComprehensiveSelect("education-history-education-code", educations);
        educationHistoryOptionsLoaded = true;
    } catch (error) {
        console.warn("加载学历下拉选项失败", error);
    }
}

async function onOrganizationMaintenanceSearch(event) {
    event.preventDefault();
    await loadOrganizationMaintenance();
}

async function onPersonnelStatisticsSearch(event) {
    event.preventDefault();
    state.personnelStatisticsPage = 0;
    await loadPersonnelStatistics();
}

async function onPersonnelComprehensiveQuerySearch(event) {
    event.preventDefault();
    state.personnelComprehensiveQueryPage = 0;
    const pageInput = document.getElementById("personnel-comprehensive-query-page");
    if (pageInput) {
        pageInput.value = "0";
    }
    await loadPersonnelComprehensiveQueries();
}

let personnelComprehensiveQueryOptionsLoaded = false;

function fillPersonnelComprehensiveSelect(selectId, options, formatter) {
    const select = document.getElementById(selectId);
    if (!select) {
        return;
    }
    const previous = select.value;
    const rows = options || [];
    const html = [`<option value="">全部</option>`].concat(rows.map(item => {
        const code = String(item?.code ?? "").trim();
        const name = String(item?.name ?? "").trim();
        const value = code || name;
        const label = formatter ? formatter(code, name) : (code && name && code !== name ? `${code} ${name}` : (name || code));
        return `<option value="${escapeHtml(value)}">${escapeHtml(label)}</option>`;
    }));
    select.innerHTML = html.join("");
    if (previous && rows.some(item => String(item?.code ?? "").trim() === previous || String(item?.name ?? "").trim() === previous)) {
        select.value = previous;
    } else {
        select.value = "";
    }
}

async function ensurePersonnelComprehensiveQueryOptions() {
    if (personnelComprehensiveQueryOptionsLoaded) {
        return;
    }
    try {
        const options = await getJson("/api/personnel/comprehensive-query-options");
        fillPersonnelComprehensiveSelect("personnel-comprehensive-query-personnel-category", options.personnelCategories, (_code, name) => name);
        fillPersonnelComprehensiveSelect("personnel-comprehensive-query-organization-type", options.organizationTypes);
        fillPersonnelComprehensiveSelect("personnel-comprehensive-query-post-category", options.postCategories, (_code, name) => name);
        fillPersonnelComprehensiveSelect("personnel-comprehensive-query-education-code", options.educations);
        const positions = [...(options.positions || [])].sort((a, b) =>
            String(a?.code ?? "").localeCompare(String(b?.code ?? ""), "zh-CN", { numeric: true, sensitivity: "base" }));
        fillPersonnelComprehensiveSelect("personnel-comprehensive-query-position-code", positions);
        personnelComprehensiveQueryOptionsLoaded = true;
    } catch (error) {
        console.warn("加载综合查询下拉选项失败", error);
    }
}

function gotoPersonnelComprehensiveQueryPage(page) {
    const totalPages = Math.max(state.personnelComprehensiveQueryTotalPages || 1, 1);
    const target = Math.min(Math.max(page, 0), totalPages - 1);
    if (target === state.personnelComprehensiveQueryPage) {
        return;
    }
    state.personnelComprehensiveQueryPage = target;
    const pageInput = document.getElementById("personnel-comprehensive-query-page");
    if (pageInput) {
        pageInput.value = String(target);
    }
    void loadPersonnelComprehensiveQueries();
}

function renderPersonnelComprehensiveQueryPagination(totalElements, totalPages) {
    const bar = document.getElementById("personnel-comprehensive-query-pagination");
    if (!bar) {
        return;
    }
    const pages = Math.max(totalPages || 1, 1);
    state.personnelComprehensiveQueryTotalPages = pages;
    const current = state.personnelComprehensiveQueryPage;
    bar.classList.toggle("hidden", !totalElements);
    const totalPagesEl = document.getElementById("personnel-comprehensive-query-total-pages");
    const totalCountEl = document.getElementById("personnel-comprehensive-query-total-count");
    const pageInput = document.getElementById("personnel-comprehensive-query-page-input");
    if (totalPagesEl) {
        totalPagesEl.textContent = String(pages);
    }
    if (totalCountEl) {
        totalCountEl.textContent = String(totalElements || 0);
    }
    if (pageInput) {
        pageInput.value = String(current + 1);
        pageInput.max = String(pages);
        pageInput.disabled = !totalElements;
    }
    const noData = !totalElements;
    document.getElementById("personnel-comprehensive-query-first").disabled = noData || current <= 0;
    document.getElementById("personnel-comprehensive-query-prev").disabled = noData || current <= 0;
    document.getElementById("personnel-comprehensive-query-next").disabled = noData || current >= pages - 1;
    document.getElementById("personnel-comprehensive-query-last").disabled = noData || current >= pages - 1;
}

async function loadPersonnelComprehensiveQueries() {
    await ensurePersonnelComprehensiveQueryOptions();
    const status = document.getElementById("personnel-comprehensive-query-status");
    const rows = document.getElementById("personnel-comprehensive-query-rows");
    if (!status || !rows) {
        return;
    }
    const organizationCode = selectedOrganizationCode("personnel-comprehensive-query-organization-code");
    const page = String(state.personnelComprehensiveQueryPage || 0);
    const size = document.getElementById("personnel-comprehensive-query-size")?.value || "20";
    const hiddenPage = document.getElementById("personnel-comprehensive-query-page");
    if (hiddenPage) {
        hiddenPage.value = page;
    }
    const params = new URLSearchParams({ page, size });
    const fields = [
        ["organizationCode", organizationCode],
        ["keyword", document.getElementById("personnel-comprehensive-query-keyword")?.value?.trim()],
        ["gender", document.getElementById("personnel-comprehensive-query-gender")?.value?.trim()],
        ["personnelCategory", document.getElementById("personnel-comprehensive-query-personnel-category")?.value?.trim()],
        ["organizationType", document.getElementById("personnel-comprehensive-query-organization-type")?.value?.trim()],
        ["postCategory", document.getElementById("personnel-comprehensive-query-post-category")?.value?.trim()],
        ["educationCode", document.getElementById("personnel-comprehensive-query-education-code")?.value?.trim()],
        ["birthYearMonthFrom", document.getElementById("personnel-comprehensive-query-birth-from")?.value?.trim()],
        ["birthYearMonthTo", document.getElementById("personnel-comprehensive-query-birth-to")?.value?.trim()],
        ["workStartYearMonthFrom", document.getElementById("personnel-comprehensive-query-work-from")?.value?.trim()],
        ["workStartYearMonthTo", document.getElementById("personnel-comprehensive-query-work-to")?.value?.trim()],
        ["regularizationYearMonthFrom", document.getElementById("personnel-comprehensive-query-regularization-from")?.value?.trim()],
        ["regularizationYearMonthTo", document.getElementById("personnel-comprehensive-query-regularization-to")?.value?.trim()],
        ["positionCode", document.getElementById("personnel-comprehensive-query-position-code")?.value?.trim()],
        ["gradeLevelFrom", document.getElementById("personnel-comprehensive-query-grade-from")?.value?.trim()],
        ["gradeLevelTo", document.getElementById("personnel-comprehensive-query-grade-to")?.value?.trim()],
    ];
    fields.forEach(([key, value]) => {
        if (value) {
            params.set(key, value);
        }
    });
    status.className = "status";
    status.textContent = "正在查询人员...";
    rows.innerHTML = "";
    try {
        const result = await getJson(`/api/personnel/comprehensive-queries?${params}`);
        const content = result.content || [];
        const total = result.totalElements || 0;
        const totalPages = Math.max(result.totalPages || 1, 1);
        if ((result.page || 0) > totalPages - 1 && totalPages > 0 && total > 0) {
            state.personnelComprehensiveQueryPage = Math.max(totalPages - 1, 0);
            return loadPersonnelComprehensiveQueries();
        }
        state.personnelComprehensiveQueryPage = result.page || 0;
        state.personnelComprehensiveQueryTotalPages = totalPages;
        status.textContent = total === 0
            ? "未查询到符合条件的人员"
            : `共 ${total} 人，第 ${state.personnelComprehensiveQueryPage + 1} / ${totalPages} 页`;
        rows.innerHTML = content.length ? content.map(row => `
            <tr>
                <td class="col-org" title="${escapeHtml(row.organizationName || row.organizationCode || "")}">${escapeHtml(row.organizationName || row.organizationCode || "")}</td>
                <td class="col-code">${escapeHtml(row.personCode || "")}</td>
                <td class="col-name">${escapeHtml(row.name || "")}</td>
                <td class="col-gender">${escapeHtml(row.gender || "")}</td>
                <td class="col-period">${escapeHtml(row.birthYearMonth || "")}</td>
                <td class="col-category">${escapeHtml(row.personnelCategory || "")}</td>
                <td class="col-period">${escapeHtml(row.workStartYearMonth || "")}</td>
                <td class="col-period">${escapeHtml(row.regularizationYearMonth || "")}</td>
                <td class="col-position">${escapeHtml(row.appointmentPositionName || row.appointmentPositionCode || "")}</td>
                <td class="col-period">${escapeHtml(row.payrollPeriod || "")}</td>
                <td class="col-position">${escapeHtml(row.payrollPositionName || row.payrollPositionCode || "")}</td>
                <td class="col-level">${escapeHtml([row.gradeLevel, row.gradeStep].filter(Boolean).join("/"))}</td>
                <td class="col-amount">${row.totalSalary == null ? "" : escapeHtml(String(row.totalSalary))}</td>
                <td class="col-actions">
                    ${row.uid ? `<button type="button" class="row-action" data-comprehensive-edit="${escapeHtml(String(row.uid))}">维护</button>` : ""}
                </td>
            </tr>
        `).join("") : `<tr><td colspan="14" class="empty-row">无数据</td></tr>`;
        rows.querySelectorAll("button[data-comprehensive-edit]").forEach(button => {
            button.addEventListener("click", () => editPersonnelMaintenance(button.dataset.comprehensiveEdit));
        });
        renderPersonnelComprehensiveQueryPagination(total, totalPages);
    } catch (error) {
        renderPersonnelComprehensiveQueryPagination(0, 1);
        showError(status, error);
    }
}

async function onRetirementDueQuerySearch(event) {
    event.preventDefault();
    state.retirementDueQueryPage = 0;
    await loadRetirementDuePersonnel();
}

function ensureRetirementDueReferencePeriod() {
    const input = document.getElementById("retirement-due-query-reference-period");
    if (!input || input.value.trim()) {
        return;
    }
    const now = new Date();
    const month = String(now.getMonth() + 1).padStart(2, "0");
    input.value = `${now.getFullYear()}.${month}`;
}

function gotoRetirementDueQueryPage(page) {
    const totalPages = Math.max(state.retirementDueQueryTotalPages || 1, 1);
    const target = Math.min(Math.max(page, 0), totalPages - 1);
    if (target === state.retirementDueQueryPage) {
        return;
    }
    state.retirementDueQueryPage = target;
    void loadRetirementDuePersonnel();
}

function renderRetirementDueQueryPagination(totalElements, totalPages) {
    const bar = document.getElementById("retirement-due-query-pagination");
    if (!bar) {
        return;
    }
    const pages = Math.max(totalPages || 1, 1);
    state.retirementDueQueryTotalPages = pages;
    const current = state.retirementDueQueryPage || 0;
    bar.classList.toggle("hidden", !totalElements);
    const totalPagesEl = document.getElementById("retirement-due-query-total-pages");
    const totalCountEl = document.getElementById("retirement-due-query-total-count");
    const pageInput = document.getElementById("retirement-due-query-page-input");
    if (totalPagesEl) {
        totalPagesEl.textContent = String(pages);
    }
    if (totalCountEl) {
        totalCountEl.textContent = String(totalElements || 0);
    }
    if (pageInput) {
        pageInput.value = String(current + 1);
        pageInput.max = String(pages);
        pageInput.disabled = !totalElements;
    }
    const noData = !totalElements;
    document.getElementById("retirement-due-query-first").disabled = noData || current <= 0;
    document.getElementById("retirement-due-query-prev").disabled = noData || current <= 0;
    document.getElementById("retirement-due-query-next").disabled = noData || current >= pages - 1;
    document.getElementById("retirement-due-query-last").disabled = noData || current >= pages - 1;
}

async function loadRetirementDuePersonnel() {
    ensureRetirementDueReferencePeriod();
    const organizationCode = selectedOrganizationCode("retirement-due-query-organization-code");
    const referencePeriod = document.getElementById("retirement-due-query-reference-period").value.trim();
    const keyword = document.getElementById("retirement-due-query-keyword").value.trim();
    const page = String(state.retirementDueQueryPage || 0);
    const size = document.getElementById("retirement-due-query-size").value || "20";
    const params = new URLSearchParams({ page, size });
    if (organizationCode) {
        params.set("organizationCode", organizationCode);
    }
    if (referencePeriod) {
        params.set("referencePeriod", referencePeriod);
    }
    if (keyword) {
        params.set("keyword", keyword);
    }
    const status = document.getElementById("retirement-due-query-status");
    const rows = document.getElementById("retirement-due-query-rows");
    status.className = "status";
    status.textContent = "正在查询已达退休年龄人员...";
    rows.innerHTML = "";
    try {
        const result = await getJson(`/api/statistics/retirement-due-personnel?${params}`);
        const content = result.content || [];
        const totalPages = Math.max(result.totalPages || 1, 1);
        if ((result.page || 0) > totalPages - 1 && totalPages > 0) {
            state.retirementDueQueryPage = Math.max(totalPages - 1, 0);
            return loadRetirementDuePersonnel();
        }
        state.retirementDueQueryPage = result.page || 0;
        state.retirementDueQueryTotalPages = totalPages;
        rows.innerHTML = content.length ? content.map(row => `
            <tr>
                <td class="col-org-code">${escapeHtml(row.organizationCode || "")}</td>
                <td class="col-org" title="${escapeHtml(row.organizationName || row.organizationCode || "")}">${escapeHtml(row.organizationName || row.organizationCode || "")}</td>
                <td class="col-code">${escapeHtml(row.personCode || "")}</td>
                <td class="col-name">${escapeHtml(row.name || "")}</td>
                <td class="col-gender">${escapeHtml(row.gender || "")}</td>
                <td class="col-period">${escapeHtml(row.birthYearMonth || "")}</td>
                <td class="col-position" title="${escapeHtml(row.positionName || row.positionCode || "")}">${escapeHtml(row.positionName || row.positionCode || "")}</td>
                <td class="col-category">${escapeHtml(row.retirementCategory || "")}</td>
                <td class="col-delay">${row.delayMonths == null ? "" : escapeHtml(String(row.delayMonths))}</td>
                <td class="col-period">${escapeHtml(row.calculatedRetirementMonth || "")}</td>
                <td class="col-period">${escapeHtml(row.storedRetirementMonth || "")}</td>
            </tr>
        `).join("") : `<tr><td colspan="11">截至参照年月暂无已达退休年龄人员</td></tr>`;
        renderRetirementDueQueryPagination(result.totalElements || 0, totalPages);
        status.textContent = `查询完成，共 ${result.totalElements || 0} 人` +
            (referencePeriod ? `（参照 ${referencePeriod}）` : "");
    } catch (error) {
        rows.innerHTML = `<tr><td colspan="11">查询失败</td></tr>`;
        renderRetirementDueQueryPagination(0, 1);
        showError(status, error);
    }
}

function ensureRetirementProcessingReferencePeriod() {
    const input = document.getElementById("retirement-processing-reference-period");
    if (!input || input.value.trim()) {
        return;
    }
    const now = new Date();
    const month = String(now.getMonth() + 1).padStart(2, "0");
    input.value = `${now.getFullYear()}.${month}`;
}

function gotoRetirementProcessingPage(page) {
    const totalPages = Math.max(state.retirementProcessingTotalPages || 1, 1);
    const target = Math.min(Math.max(page, 0), totalPages - 1);
    if (target === state.retirementProcessingPage) {
        return;
    }
    state.retirementProcessingPage = target;
    void loadRetirementProcessingCandidates();
}

function renderRetirementProcessingPagination(totalElements, totalPages) {
    const bar = document.getElementById("retirement-processing-pagination");
    if (!bar) {
        return;
    }
    const pages = Math.max(totalPages || 1, 1);
    state.retirementProcessingTotalPages = pages;
    const current = state.retirementProcessingPage || 0;
    bar.classList.toggle("hidden", !totalElements);
    const totalPagesEl = document.getElementById("retirement-processing-total-pages");
    const totalCountEl = document.getElementById("retirement-processing-total-count");
    const pageInput = document.getElementById("retirement-processing-page-input");
    if (totalPagesEl) {
        totalPagesEl.textContent = String(pages);
    }
    if (totalCountEl) {
        totalCountEl.textContent = String(totalElements || 0);
    }
    if (pageInput) {
        pageInput.value = String(current + 1);
        pageInput.max = String(pages);
        pageInput.disabled = !totalElements;
    }
    const noData = !totalElements;
    document.getElementById("retirement-processing-first").disabled = noData || current <= 0;
    document.getElementById("retirement-processing-prev").disabled = noData || current <= 0;
    document.getElementById("retirement-processing-next").disabled = noData || current >= pages - 1;
    document.getElementById("retirement-processing-last").disabled = noData || current >= pages - 1;
}

async function loadRetirementProcessingCandidates() {
    ensureRetirementProcessingReferencePeriod();
    const organizationCode = selectedOrganizationCode("retirement-processing-organization-code");
    const referencePeriod = document.getElementById("retirement-processing-reference-period")?.value.trim() || "";
    const keyword = document.getElementById("retirement-processing-keyword")?.value.trim() || "";
    const includeDescendants = !!document.getElementById("retirement-processing-include-descendants")?.checked;
    const page = String(state.retirementProcessingPage || 0);
    const size = document.getElementById("retirement-processing-size")?.value || "20";
    const params = new URLSearchParams({ page, size, includeDescendants: String(includeDescendants) });
    if (organizationCode) {
        params.set("organizationCode", organizationCode);
    }
    if (referencePeriod) {
        params.set("referencePeriod", referencePeriod);
    }
    if (keyword) {
        params.set("keyword", keyword);
    }
    const status = document.getElementById("retirement-processing-status");
    const rows = document.getElementById("retirement-processing-rows");
    status.className = "status";
    status.textContent = "正在查询待办退休人员...";
    rows.innerHTML = "";
    try {
        const result = await getJson(`/api/retirement/processing/candidates?${params}`);
        const content = result.content || [];
        const totalPages = Math.max(result.totalPages || 1, 1);
        if ((result.page || 0) > totalPages - 1 && totalPages > 0) {
            state.retirementProcessingPage = Math.max(totalPages - 1, 0);
            return loadRetirementProcessingCandidates();
        }
        state.retirementProcessingPage = result.page || 0;
        state.retirementProcessingTotalPages = totalPages;
        rows.innerHTML = content.length ? content.map(row => {
            const seeded = !!row.alreadySeeded;
            const action = seeded
                ? `<span class="muted">已建档</span>`
                : `<button type="button" class="row-action" data-retirement-apply="${row.uid}" data-name="${escapeHtml(row.name || "")}">办理</button>`;
            return `
            <tr>
                <td class="col-org" title="${escapeHtml(row.organizationName || row.organizationCode || "")}">${escapeHtml(row.organizationName || row.organizationCode || "")}</td>
                <td class="col-code">${escapeHtml(row.personCode || "")}</td>
                <td class="col-name">${escapeHtml(row.name || "")}</td>
                <td class="col-gender">${escapeHtml(row.gender || "")}</td>
                <td class="col-period">${escapeHtml(row.birthYearMonth || "")}</td>
                <td class="col-position" title="${escapeHtml(row.positionName || row.positionCode || "")}">${escapeHtml(row.positionName || row.positionCode || "")}</td>
                <td>${row.salaryYears == null ? "" : escapeHtml(String(row.salaryYears))}</td>
                <td class="col-period">${escapeHtml(row.calculatedRetirementMonth || "")}</td>
                <td>${row.currentTotal == null ? "" : escapeHtml(String(row.currentTotal))}</td>
                <td title="${escapeHtml(row.note || "")}">${seeded ? "已建档" : "可办理"}</td>
                <td>${action}</td>
            </tr>`;
        }).join("") : `<tr><td colspan="11">截至参照年月暂无待办退休人员</td></tr>`;
        renderRetirementProcessingPagination(result.totalElements || 0, totalPages);
        status.textContent = `查询完成，共 ${result.totalElements || 0} 人` +
            (referencePeriod ? `（参照 ${referencePeriod}）` : "");
    } catch (error) {
        rows.innerHTML = `<tr><td colspan="11">查询失败</td></tr>`;
        renderRetirementProcessingPagination(0, 1);
        showError(status, error);
    }
}

async function applyRetirementProcessing(uid, name) {
    if (!uid) {
        return;
    }
    const status = document.getElementById("retirement-processing-status");
    status.className = "status";
    status.textContent = `正在试算 ${name || uid} ...`;
    try {
        const preview = await getJson(`/api/retirement/processing/${encodeURIComponent(uid)}/preview`);
        if (!preview.applyEligible) {
            alert(preview.note || "当前人员不满足退休办理条件。");
            status.textContent = preview.note || "不可办理";
            return;
        }
        const confirmText = [
            `确认办理退休并归档在职记录？`,
            `${preview.name || name || ""}（在职 ${preview.sourcePersonCode || ""} → 离退 ${preview.retireePersonCode || ""}）`,
            `退休时间 ${preview.retirementDate || ""} / ${preview.retirementCategory || "退休"} / ${preview.retirementReason || ""}`,
            `折算比例 ${preview.conversionRatio == null ? "-" : preview.conversionRatio}%`
                + (preview.increaseRatio ? ` + 提高 ${preview.increaseRatio}%` : "")
                + ` → 有效 ${preview.effectiveRatio == null ? "-" : preview.effectiveRatio}%`,
            `工资基数 ${preview.wageBase == null ? "-" : preview.wageBase} → 折算 ${preview.convertedWageBase == null ? "-" : preview.convertedWageBase}`
                + `，jbldxf ${preview.basicRetirementFee == null ? "-" : preview.basicRetirementFee}`,
            `试算合计 ${preview.estimatedTotal == null ? "-" : preview.estimatedTotal}`
                + `（津补贴 ${preview.allowanceTotal == null ? "-" : preview.allowanceTotal}）`,
            preview.note || "",
        ].filter(Boolean).join("\n");
        if (!confirm(confirmText)) {
            status.textContent = "已取消办理";
            return;
        }
        status.textContent = "正在办理退休建档...";
        const result = await postJson(`/api/retirement/processing/${encodeURIComponent(uid)}/apply`, {
            retirementDate: preview.retirementDate,
            retirementCategory: preview.retirementCategory,
            retirementReason: preview.retirementReason,
            remark: "",
        });
        status.textContent = result.message
            || `办理完成：离退编码 ${result.retireePersonCode || ""}，试算合计 ${result.estimatedTotal == null ? "" : result.estimatedTotal}`;
        await loadRetirementProcessingCandidates();
    } catch (error) {
        showError(status, error);
    }
}

async function refreshRetirementApprovalTemplateName() {
    const style = document.getElementById("retirement-approval-style")?.value || "2025";
    const organizationNature = document.getElementById("retirement-approval-org-nature")?.value || "事业";
    const nameEl = document.getElementById("retirement-approval-template-name");
    const natureWrap = document.getElementById("retirement-approval-org-nature-wrap");
    if (natureWrap) {
        natureWrap.classList.toggle("hidden", style === "2025" || style === "2021");
    }
    if (!nameEl) {
        return;
    }
    try {
        const params = new URLSearchParams({ style, organizationNature });
        const resolved = await getJson(`/api/retirement/approval-report/resolve-template?${params}`);
        nameEl.textContent = resolved.template || "-";
    } catch (_error) {
        const agency = organizationNature.includes("行政");
        const map = {
            "2006": agency ? "txspbxz" : "txspbsy",
            "2021": "txspb21",
            "2025": "txspb25",
        };
        nameEl.textContent = map[style] || "txspb25";
    }
}

async function onRetirementApprovalReportSearch() {
    await refreshRetirementApprovalTemplateName();
    const organizationCode = selectedOrganizationCode("retirement-approval-organization-code");
    const keyword = document.getElementById("retirement-approval-keyword")?.value.trim() || "";
    const params = new URLSearchParams({ page: "0", size: "100" });
    if (organizationCode) {
        params.set("organizationCode", organizationCode);
    }
    if (keyword) {
        params.set("keyword", keyword);
    }
    const status = document.getElementById("retirement-approval-status");
    const rows = document.getElementById("retirement-approval-rows");
    const selectAll = document.getElementById("retirement-approval-select-all");
    const style = document.getElementById("retirement-approval-style")?.value || "2025";
    const template = document.getElementById("retirement-approval-template-name")?.textContent || "";
    status.className = "status";
    status.textContent = "正在查询离退休人员...";
    rows.innerHTML = "";
    if (selectAll) {
        selectAll.checked = false;
    }
    try {
        const result = await getJson(`/api/retirement/approval-report/candidates?${params}`);
        const content = result.content || [];
        rows.innerHTML = content.length ? content.map(row => `
            <tr>
                <td><input type="checkbox" data-retirement-approval-select value="${escapeHtml(String(row.id || ""))}"></td>
                <td class="col-org" title="${escapeHtml(row.organizationName || row.organizationCode || "")}">${escapeHtml(row.organizationName || row.organizationCode || "")}</td>
                <td class="col-code">${escapeHtml(row.personCode || "")}</td>
                <td class="col-name">${escapeHtml(row.name || "")}</td>
                <td class="col-period">${escapeHtml(row.retirementDate || "")}</td>
                <td class="col-position" title="${escapeHtml(row.positionName || row.positionCode || "")}">${escapeHtml(row.positionName || row.positionCode || "")}</td>
                <td>${row.totalAmount == null ? "" : escapeHtml(String(row.totalAmount))}</td>
            </tr>
        `).join("") : `<tr><td colspan="7">暂无套改后离退休人员</td></tr>`;
        status.textContent = `已选择 ${style} / ${template}，共 ${result.totalElements || 0} 人可勾选打印`;
    } catch (error) {
        rows.innerHTML = `<tr><td colspan="7">查询失败</td></tr>`;
        showError(status, error);
    }
}

async function generateAndPrintRetirementApprovalReports() {
    const selectedIds = Array.from(document.querySelectorAll("#retirement-approval-rows [data-retirement-approval-select]:checked"))
        .map(input => Number(input.value))
        .filter(id => Number.isFinite(id) && id > 0);
    const status = document.getElementById("retirement-approval-status");
    const triggerButton = document.getElementById("retirement-approval-print");
    if (!selectedIds.length) {
        status.className = "status error";
        status.textContent = "请先勾选要打印的离退休人员。";
        return;
    }
    const style = document.getElementById("retirement-approval-style")?.value || "2025";
    const organizationNature = document.getElementById("retirement-approval-org-nature")?.value || "事业";
    const originalLabel = triggerButton?.textContent || "生成并打印";
    if (triggerButton) {
        triggerButton.disabled = true;
        triggerButton.textContent = `正在生成 ${selectedIds.length} 份...`;
    }
    status.className = "status";
    status.textContent = `正在生成 ${selectedIds.length} 份退休审批表 PDF...`;
    try {
        const startedAt = performance.now();
        const response = await fetch("/api/retirement/approval-report/pdf", {
            method: "POST",
            headers: { "Content-Type": "application/json", Accept: "application/pdf, application/json" },
            credentials: "same-origin",
            body: JSON.stringify({
                retireeIds: selectedIds,
                style,
                organizationNature,
            }),
        });
        await ensureAuthenticatedApiResponse(response, "生成退休审批表失败");
        const blob = await response.blob();
        const elapsedMs = Math.max(1, Math.round(performance.now() - startedAt));
        status.className = "status success";
        status.textContent = `已生成 ${selectedIds.length} 份 PDF（${elapsedMs} ms），正在打开打印窗口...`;
        await openPdfBlobForPrint(blob);
        status.textContent = `已生成并送打 ${selectedIds.length} 份退休审批表（${elapsedMs} ms）`;
    } catch (error) {
        showError(status, error);
    } finally {
        if (triggerButton) {
            triggerButton.disabled = false;
            triggerButton.textContent = originalLabel;
        }
    }
}

function gotoRetireePersonnelPage(page) {
    const totalPages = Math.max(state.retireePersonnelTotalPages || 1, 1);
    const target = Math.min(Math.max(page, 0), totalPages - 1);
    if (target === state.retireePersonnelPage) {
        return;
    }
    state.retireePersonnelPage = target;
    void loadRetireePersonnel();
}

function renderRetireePersonnelPagination(totalElements, totalPages) {
    const bar = document.getElementById("retiree-personnel-pagination");
    if (!bar) {
        return;
    }
    const pages = Math.max(totalPages || 1, 1);
    state.retireePersonnelTotalPages = pages;
    const current = state.retireePersonnelPage || 0;
    bar.classList.toggle("hidden", !totalElements);
    const totalPagesEl = document.getElementById("retiree-personnel-total-pages");
    const totalCountEl = document.getElementById("retiree-personnel-total-count");
    const pageInput = document.getElementById("retiree-personnel-page-input");
    if (totalPagesEl) {
        totalPagesEl.textContent = String(pages);
    }
    if (totalCountEl) {
        totalCountEl.textContent = String(totalElements || 0);
    }
    if (pageInput) {
        pageInput.value = String(current + 1);
        pageInput.max = String(pages);
        pageInput.disabled = !totalElements;
    }
    const noData = !totalElements;
    document.getElementById("retiree-personnel-first").disabled = noData || current <= 0;
    document.getElementById("retiree-personnel-prev").disabled = noData || current <= 0;
    document.getElementById("retiree-personnel-next").disabled = noData || current >= pages - 1;
    document.getElementById("retiree-personnel-last").disabled = noData || current >= pages - 1;
}

async function loadRetireePersonnel() {
    const organizationCode = selectedOrganizationCode("retiree-personnel-organization-code");
    const keyword = document.getElementById("retiree-personnel-keyword")?.value.trim() || "";
    const includeDescendants = !!document.getElementById("retiree-personnel-include-descendants")?.checked;
    const pendingOnly = !!document.getElementById("retiree-personnel-pending-only")?.checked;
    const page = String(state.retireePersonnelPage || 0);
    const size = document.getElementById("retiree-personnel-size")?.value || "20";
    const params = new URLSearchParams({
        page,
        size,
        includeDescendants: String(includeDescendants),
        pendingOnly: String(pendingOnly)
    });
    if (organizationCode) {
        params.set("organizationCode", organizationCode);
    }
    if (keyword) {
        params.set("keyword", keyword);
    }
    const status = document.getElementById("retiree-personnel-status");
    const rows = document.getElementById("retiree-personnel-rows");
    status.className = "status";
    status.textContent = pendingOnly ? "正在查询待办退休人员..." : "正在查询离退休人员...";
    rows.innerHTML = "";
    try {
        const result = await getJson(`/api/retirement/retirees?${params}`);
        const content = result.content || [];
        const totalPages = Math.max(result.totalPages || 1, 1);
        if ((result.page || 0) > totalPages - 1 && totalPages > 0) {
            state.retireePersonnelPage = Math.max(totalPages - 1, 0);
            return loadRetireePersonnel();
        }
        state.retireePersonnelPage = result.page || 0;
        state.retireePersonnelTotalPages = totalPages;
        const canWrite = hasRetirementWrite();
        rows.innerHTML = content.length ? content.map(row => {
            const statusText = row.approvalStatus || "";
            const pending = statusText === "待办退休";
            const approved = statusText === "审批通过";
            const actions = [];
            if (pending) {
                actions.push(`<button type="button" class="row-action" data-retiree-maintain="${row.id}">办理</button>`);
            } else {
                actions.push(`<button type="button" class="row-action" data-retiree-maintain="${row.id}">维护</button>`);
            }
            if (canWrite && !approved && (pending || statusText === "建库未核" || statusText === "申报" || !statusText)) {
                actions.push(`<button type="button" class="row-action" data-retiree-approve="${row.id}" data-name="${escapeHtml(row.name || "")}">审批通过</button>`);
            }
            return `
            <tr>
                <td class="col-org" title="${escapeHtml(row.organizationName || row.organizationCode || "")}">${escapeHtml(row.organizationName || row.organizationCode || "")}</td>
                <td class="col-code">${escapeHtml(row.personCode || "")}</td>
                <td class="col-name">${escapeHtml(row.name || "")}</td>
                <td>${escapeHtml(row.retirementCategory || "")}</td>
                <td class="col-period">${escapeHtml(row.retirementDate || "")}</td>
                <td class="col-position" title="${escapeHtml(row.positionName || row.positionCode || "")}">${escapeHtml(row.positionName || row.positionCode || "")}</td>
                <td>${row.totalAmount == null ? "" : escapeHtml(String(row.totalAmount))}</td>
                <td>${escapeHtml(statusText)}</td>
                <td class="col-actions">${actions.join(" ")}</td>
            </tr>`;
        }).join("") : `<tr><td colspan="9">${pendingOnly ? "暂无待办退休人员" : "暂无套改后离退休人员"}</td></tr>`;
        renderRetireePersonnelPagination(result.totalElements || 0, totalPages);
        status.textContent = `查询完成，共 ${result.totalElements || 0} 人`
            + (pendingOnly ? "（待办）" : "");
    } catch (error) {
        rows.innerHTML = `<tr><td colspan="9">查询失败</td></tr>`;
        renderRetireePersonnelPagination(0, 1);
        showError(status, error);
    }
}

function closeRetireeMaintenance() {
    const modal = document.getElementById("retiree-maintenance-modal");
    if (modal) {
        modal.classList.add("hidden");
    }
    state.currentRetireeId = null;
}

function setRetireeMaintenanceEditable(editable) {
    const form = document.getElementById("retiree-maintenance-form");
    if (!form) {
        return;
    }
    const canWrite = editable && hasRetirementWrite();
    form.querySelectorAll("input, select, textarea, button").forEach(el => {
        if (el.id === "retiree-maint-cancel" || el.id === "retiree-maint-id"
            || el.id === "retiree-maint-organization" || el.id === "retiree-maint-person-code"
            || el.id === "retiree-maint-approval-status"
            || el.id === "retiree-maint-approve" || el.id === "retiree-maint-cancel-approve"
            || el.id === "retiree-maint-position-code"
            || el.id === "retiree-maint-nation"
            || el.id === "retiree-maint-education"
            || el.id === "retiree-maint-bank-account"
            || el.id === "retiree-maint-salary-standard"
            || el.id === "retiree-maint-allowance-standard") {
            return;
        }
        if (el.id === "retiree-maint-save") {
            el.disabled = !canWrite;
            el.classList.toggle("hidden", !hasRetirementWrite());
            return;
        }
        if (el.id === "retiree-maint-salary-years") {
            el.readOnly = true;
            el.disabled = false;
            return;
        }
        if (el.classList.contains("dict-picker-button")) {
            el.disabled = !canWrite;
            return;
        }
        if (el.id === "retiree-maint-post-category" || el.id === "retiree-maint-position-name") {
            el.readOnly = true;
            el.disabled = false;
            return;
        }
        if (el.id === "retiree-maint-grade-level") {
            // 由 refreshRetireeLevelOptionsByPosition 按职务范围单独控制
            return;
        }
        if (el.tagName === "SELECT" || el.type === "month" || el.dataset.monthField === "true" || el.dataset.monthPickerEnhanced === "1" || el.type === "number" || el.type === "checkbox") {
            el.disabled = !canWrite;
            if (window.MonthPicker && (el.dataset.monthField === "true" || el.dataset.monthPickerEnhanced === "1")) {
                MonthPicker.syncToggleState(el);
            }
            return;
        }
        el.readOnly = !canWrite;
        el.disabled = false;
    });
    refreshRetireeLevelEnabled();
}

function moneyText(value) {
    return value == null || value === "" ? "-" : String(value);
}

function fillRetireeMaintenance(detail) {
    state.currentRetireeId = detail.id;
    state.currentRetireeDetail = detail;
    ensureRetireeDictionaryPickers();
    bindRetireeSalaryYearsAutoCalc();
    bindRetireePostCategoryLevelRule();
    ensureRetireeGradeSelectOptions(null, null, "", detail.gradeStep || "");
    document.getElementById("retiree-maint-id").value = detail.id ?? "";
    document.getElementById("retiree-maint-organization").value =
        `${detail.organizationName || ""} (${detail.organizationCode || ""})`.trim();
    document.getElementById("retiree-maint-person-code").value = detail.personCode || "";
    document.getElementById("retiree-maint-approval-status").value = detail.approvalStatus || "";
    const statusBadge = document.getElementById("retiree-maint-status-badge");
    if (statusBadge) {
        statusBadge.textContent = detail.approvalStatus ? `（${detail.approvalStatus}）` : "";
    }
    ensureSelectHasValue("retiree-maint-approval-org", detail.approvalOrganization || "");
    document.getElementById("retiree-maint-name").value = detail.name || "";
    ensureSelectHasValue("retiree-maint-gender", detail.gender || "");
    document.getElementById("retiree-maint-id-card").value = detail.idCard || "";
    document.getElementById("retiree-maint-nation").value = detail.nation || "";
    document.getElementById("retiree-maint-birth").value = toMonthControlValue(detail.birthYearMonth);
    document.getElementById("retiree-maint-work-start").value = toMonthControlValue(detail.workStartYearMonth);
    document.getElementById("retiree-maint-interrupted").value = detail.interruptedYears ?? 0;
    document.getElementById("retiree-maint-education").value = detail.education || "";
    ensureSelectHasValue("retiree-maint-category", detail.retirementCategory || "");
    document.getElementById("retiree-maint-retirement-date").value = toMonthControlValue(detail.retirementDate);
    ensureSelectHasValue("retiree-maint-reason", detail.retirementReason || "");
    document.getElementById("retiree-maint-post-category").value = detail.postCategory || "";
    document.getElementById("retiree-maint-position-name").value = detail.positionName || "";
    document.getElementById("retiree-maint-position-code").value = detail.positionCode || "";
    // 先记下当前级别，待职务级别范围刷新后再回填
    state.pendingRetireeGradeLevel = detail.gradeLevel || "";
    document.getElementById("retiree-maint-salary-standard").value = toMonthControlValue(RETIREE_FIXED_SALARY_STANDARD);
    document.getElementById("retiree-maint-allowance-standard").value = toMonthControlValue(RETIREE_FIXED_ALLOWANCE_STANDARD);
    const teachingRaise = Number(detail.teachingRaisePercentage ?? 0);
    ensureSelectHasValue("retiree-maint-teaching-raise", teachingRaise >= 10 ? "10" : "0");
    document.getElementById("retiree-maint-teaching-years").value = detail.teachingYears ?? 0;
    document.getElementById("retiree-maint-bank-account").value = detail.bankAccount || "";
    ensureSelectHasValue("retiree-maint-increase-reason", detail.increaseReason || "");
    document.getElementById("retiree-maint-increase-ratio").value = detail.increaseRatio ?? 0;
    document.getElementById("retiree-maint-approval-doc").value = detail.approvalDocumentNumber || "";
    document.getElementById("retiree-maint-interrupted-note").value = detail.interruptedNote || "";
    document.getElementById("retiree-maint-interrupted-months").value = detail.interruptedMonths || "";
    const workStartValue = document.getElementById("retiree-maint-work-start").value;
    const retirementValue = document.getElementById("retiree-maint-retirement-date").value;
    if (workStartValue && retirementValue) {
        refreshRetireeSalaryYears();
    } else {
        document.getElementById("retiree-maint-salary-years").value = detail.salaryYears ?? 0;
    }

    document.getElementById("retiree-fee-position").textContent = moneyText(detail.positionSalary);
    document.getElementById("retiree-fee-grade").textContent = moneyText(detail.gradeSalary);
    document.getElementById("retiree-fee-technical").textContent = moneyText(detail.technicalSalary);
    document.getElementById("retiree-fee-teaching").value = String(detail.teachingRaise ?? 0);
    document.getElementById("retiree-fee-rank").value = String(detail.rankAllowance ?? 0);
    document.getElementById("retiree-fee-converted-base").textContent = moneyText(detail.convertedWageBase);
    document.getElementById("retiree-fee-conversion-ratio").textContent = moneyText(detail.conversionRatio);
    document.getElementById("retiree-fee-increase-ratio-view").textContent = moneyText(detail.increaseRatio);
    document.getElementById("retiree-fee-effective-ratio").textContent = moneyText(detail.effectiveRatio);
    document.getElementById("retiree-fee-converted-amount").textContent = moneyText(detail.convertedAmount);
    document.getElementById("retiree-fee-basic").textContent = moneyText(detail.basicRetirementFee);
    document.getElementById("retiree-fee-increase").textContent = moneyText(detail.cumulativeIncrease);

    document.getElementById("retiree-before-retained").textContent = moneyText(detail.beforeRetainedAllowance);
    document.getElementById("retiree-after-retained").textContent = moneyText(detail.retainedAllowance);
    document.getElementById("retiree-before-local").textContent = moneyText(detail.beforeLocalAllowance);
    document.getElementById("retiree-after-local").textContent = moneyText(detail.localAllowance);
    document.getElementById("retiree-before-post").textContent = moneyText(detail.beforePostAllowance);
    document.getElementById("retiree-after-post").textContent = moneyText(detail.postAllowance);
    document.getElementById("retiree-before-floating").textContent = moneyText(detail.beforeFloatingSalary);
    document.getElementById("retiree-after-floating").textContent = moneyText(detail.floatingSalary);
    document.getElementById("retiree-before-bonus").textContent = moneyText(detail.beforeBonusBalance);
    document.getElementById("retiree-after-bonus").value = String(detail.bonusBalance ?? 0);
    document.getElementById("retiree-before-living").textContent = moneyText(detail.beforeLivingAllowance);
    document.getElementById("retiree-after-living").textContent = moneyText(detail.livingAllowance);
    document.getElementById("retiree-before-special").textContent = moneyText(detail.beforeSpecialPostAllowance);
    document.getElementById("retiree-after-special").textContent = moneyText(detail.specialPostAllowance);
    document.getElementById("retiree-before-position").textContent = moneyText(detail.beforePositionAllowance);
    document.getElementById("retiree-after-position").textContent = moneyText(detail.positionAllowance);
    document.getElementById("retiree-before-other").textContent = moneyText(detail.beforeOtherAllowance);
    document.getElementById("retiree-after-other").textContent = moneyText(detail.otherAllowance);
    document.getElementById("retiree-before-allowance-total").textContent = moneyText(detail.beforeAllowanceTotal);
    document.getElementById("retiree-after-allowance-total").textContent = moneyText(detail.afterAllowanceTotal);
    document.getElementById("retiree-fee-before").textContent = moneyText(detail.beforeTotal);
    document.getElementById("retiree-fee-total").textContent = moneyText(detail.totalAmount);
    bindRetireeFeePreview();
    refreshRetireeFeePreview();

    const editable = !!detail.editable && hasRetirementWrite();
    setRetireeMaintenanceEditable(editable);
    void refreshRetireeLevelOptionsByPosition({ keepCurrent: true }).then(() => {
        const pending = state.pendingRetireeGradeLevel || "";
        state.pendingRetireeGradeLevel = "";
        const levelSelect = document.getElementById("retiree-maint-grade-level");
        if (pending && levelSelect && !levelSelect.disabled) {
            ensureSelectHasValue("retiree-maint-grade-level", pending);
        }
    });
    const approveBtn = document.getElementById("retiree-maint-approve");
    if (approveBtn) {
        const showApprove = hasRetirementWrite() && !!detail.approvable;
        approveBtn.classList.toggle("hidden", !showApprove);
        approveBtn.disabled = !showApprove;
    }
    const cancelApproveBtn = document.getElementById("retiree-maint-cancel-approve");
    if (cancelApproveBtn) {
        const showCancel = hasRetirementWrite() && !!detail.cancellable;
        cancelApproveBtn.classList.toggle("hidden", !showCancel);
        cancelApproveBtn.disabled = !showCancel;
    }
    const pending = (detail.approvalStatus || "") === "待办退休";
    const title = document.getElementById("retiree-maintenance-modal-title");
    if (title) {
        title.textContent = pending ? "退休办理" : "离退休人员维护";
    }
    const subtitle = document.getElementById("retiree-maintenance-modal-subtitle");
    if (subtitle) {
        if (!editable) {
            subtitle.textContent = "审批通过已锁定；可用「取消审核」退回待办。";
        } else if (pending) {
            subtitle.textContent = "依次为基本信息、2014.09.30 时信息、基本离退休费及津补贴；确认后点「审核通过」。";
        } else {
            subtitle.textContent = "依次为基本信息、2014.09.30 时信息、基本离退休费及津补贴。";
        }
    }
}

async function openRetireeMaintenance(id) {
    const modal = document.getElementById("retiree-maintenance-modal");
    const status = document.getElementById("retiree-maintenance-status");
    if (!modal || !id) {
        return;
    }
    modal.classList.remove("hidden");
    status.className = "status";
    status.textContent = "正在加载...";
    try {
        const detail = await getJson(`/api/retirement/retirees/${id}`);
        fillRetireeMaintenance(detail);
        status.textContent = "";
    } catch (error) {
        showError(status, error);
    }
}

function bindRetireeFeePreview() {
    ["retiree-fee-teaching", "retiree-fee-rank", "retiree-maint-increase-ratio", "retiree-maint-teaching-raise", "retiree-after-bonus"]
        .forEach(id => {
            const el = document.getElementById(id);
            if (!el || el.dataset.feePreviewBound) {
                return;
            }
            el.dataset.feePreviewBound = "1";
            el.addEventListener("input", () => {
                if (id === "retiree-fee-teaching" || id === "retiree-fee-rank" || id === "retiree-after-bonus") {
                    sanitizeRetireeDigitsInput(el);
                }
                refreshRetireeFeePreview();
            });
            el.addEventListener("change", () => {
                if (id === "retiree-maint-increase-ratio") {
                    normalizeRetireeIncreaseRatio();
                }
                if (id === "retiree-maint-teaching-raise") {
                    syncRetireeTeachingRaiseAmount();
                }
                if (id === "retiree-fee-teaching" || id === "retiree-fee-rank" || id === "retiree-after-bonus") {
                    sanitizeRetireeDigitsInput(el, true);
                }
                refreshRetireeFeePreview();
            });
            el.addEventListener("blur", () => {
                if (id === "retiree-fee-teaching" || id === "retiree-fee-rank" || id === "retiree-after-bonus") {
                    sanitizeRetireeDigitsInput(el, true);
                    refreshRetireeFeePreview();
                }
            });
        });
}

function sanitizeRetireeDigitsInput(el, normalizeEmpty) {
    if (!el) {
        return;
    }
    const digits = String(el.value || "").replace(/\D/g, "");
    if (!digits) {
        el.value = normalizeEmpty ? "0" : "";
        return;
    }
    el.value = String(Number(digits));
}

function normalizeRetireeIncreaseRatio() {
    const input = document.getElementById("retiree-maint-increase-ratio");
    if (!input) {
        return;
    }
    let value = Number(input.value || 0);
    if (!Number.isFinite(value) || value < 0) {
        value = 0;
    }
    value = Math.min(100, Math.round(value / 5) * 5);
    input.value = String(value);
}

function syncRetireeTeachingRaiseAmount() {
    const pct = Number(document.getElementById("retiree-maint-teaching-raise")?.value || 0);
    const position = Number(String(document.getElementById("retiree-fee-position")?.textContent || "0").replace(/[^\d.-]/g, "") || 0);
    const grade = Number(String(document.getElementById("retiree-fee-grade")?.textContent || "0").replace(/[^\d.-]/g, "") || 0);
    const teaching = document.getElementById("retiree-fee-teaching");
    if (teaching) {
        teaching.value = String(Math.round((position + grade) * Math.max(pct, 0) / 100));
    }
}

function retireeMoneyFromElement(id) {
    const el = document.getElementById(id);
    if (!el) {
        return 0;
    }
    const raw = el.tagName === "INPUT" || el.tagName === "SELECT" || el.tagName === "TEXTAREA"
        ? el.value
        : el.textContent;
    const value = Number(String(raw ?? "0").replace(/[^\d.-]/g, ""));
    return Number.isFinite(value) ? value : 0;
}

function refreshRetireeFeePreview() {
    const position = retireeMoneyFromElement("retiree-fee-position");
    const grade = retireeMoneyFromElement("retiree-fee-grade");
    const technical = retireeMoneyFromElement("retiree-fee-technical");
    const teaching = Math.max(0, retireeMoneyFromElement("retiree-fee-teaching"));
    const rank = Math.max(0, retireeMoneyFromElement("retiree-fee-rank"));
    const conversion = Math.max(0, retireeMoneyFromElement("retiree-fee-conversion-ratio"));
    const increase = Math.max(0, retireeMoneyFromElement("retiree-maint-increase-ratio"));
    const cumulativeIncrease = Math.max(0, retireeMoneyFromElement("retiree-fee-increase"));
    const afterAllowanceTotal = Math.max(0,
        retireeMoneyFromElement("retiree-after-retained")
        + retireeMoneyFromElement("retiree-after-local")
        + retireeMoneyFromElement("retiree-after-post")
        + retireeMoneyFromElement("retiree-after-floating")
        + retireeMoneyFromElement("retiree-after-bonus")
        + retireeMoneyFromElement("retiree-after-living")
        + retireeMoneyFromElement("retiree-after-special")
        + retireeMoneyFromElement("retiree-after-position")
        + retireeMoneyFromElement("retiree-after-other"));
    const wageBase = position + grade + technical + teaching + rank;
    const effective = Math.min(100, conversion + increase);
    const convertedAmount = Math.round(wageBase * effective / 100);
    const basic = convertedAmount;
    // 月离退休费合计 = 基本离退休费 + 津补贴合计 + 累计增资
    const monthlyTotal = basic + afterAllowanceTotal + cumulativeIncrease;
    const setText = (id, value) => {
        const el = document.getElementById(id);
        if (el) {
            el.textContent = moneyText(value);
        }
    };
    setText("retiree-fee-converted-base", wageBase);
    setText("retiree-fee-increase-ratio-view", increase);
    setText("retiree-fee-effective-ratio", effective);
    setText("retiree-fee-converted-amount", convertedAmount);
    setText("retiree-fee-basic", basic);
    setText("retiree-after-allowance-total", afterAllowanceTotal);
    setText("retiree-fee-total", monthlyTotal);
}

function collectRetireeMaintenancePayload() {
    refreshRetireeSalaryYears();
    normalizeRetireeIncreaseRatio();
    const increaseRatio = Number(document.getElementById("retiree-maint-increase-ratio").value || 0);
    if (increaseRatio % 5 !== 0) {
        throw new Error("提高比例须为 5 的倍数。");
    }
    return {
        name: document.getElementById("retiree-maint-name").value.trim(),
        gender: document.getElementById("retiree-maint-gender").value.trim(),
        idCard: document.getElementById("retiree-maint-id-card").value.trim(),
        nation: document.getElementById("retiree-maint-nation").value.trim(),
        birthYearMonth: fromMonthControlValue(document.getElementById("retiree-maint-birth").value),
        workStartYearMonth: fromMonthControlValue(document.getElementById("retiree-maint-work-start").value),
        interruptedYears: Number(document.getElementById("retiree-maint-interrupted").value || 0),
        salaryYears: Number(document.getElementById("retiree-maint-salary-years").value || 0),
        education: document.getElementById("retiree-maint-education").value.trim(),
        retirementCategory: document.getElementById("retiree-maint-category").value.trim(),
        retirementDate: fromMonthControlValue(document.getElementById("retiree-maint-retirement-date").value),
        retirementReason: document.getElementById("retiree-maint-reason").value.trim(),
        postCategory: document.getElementById("retiree-maint-post-category").value.trim(),
        positionCode: document.getElementById("retiree-maint-position-code").value.trim(),
        positionName: document.getElementById("retiree-maint-position-name").value.trim(),
        gradeLevel: document.getElementById("retiree-maint-grade-level").value.trim(),
        gradeStep: document.getElementById("retiree-maint-grade-step").value.trim(),
        salaryStandardYearMonth: RETIREE_FIXED_SALARY_STANDARD,
        allowanceStandardYearMonth: RETIREE_FIXED_ALLOWANCE_STANDARD,
        approvalOrganization: document.getElementById("retiree-maint-approval-org").value.trim(),
        teachingRaisePercentage: Number(document.getElementById("retiree-maint-teaching-raise").value || 0),
        teachingYears: Number(document.getElementById("retiree-maint-teaching-years").value || 0),
        increaseRatio,
        increaseReason: document.getElementById("retiree-maint-increase-reason").value.trim(),
        approvalDocumentNumber: document.getElementById("retiree-maint-approval-doc").value.trim(),
        interruptedNote: document.getElementById("retiree-maint-interrupted-note").value.trim(),
        interruptedMonths: document.getElementById("retiree-maint-interrupted-months").value.trim(),
        bankAccount: document.getElementById("retiree-maint-bank-account").value.trim(),
        teachingRaise: Number(String(document.getElementById("retiree-fee-teaching").value || "0").replace(/\D/g, "") || 0),
        rankAllowance: Number(String(document.getElementById("retiree-fee-rank").value || "0").replace(/\D/g, "") || 0),
        bonusBalance: Number(String(document.getElementById("retiree-after-bonus").value || "0").replace(/\D/g, "") || 0)
    };
}

async function saveRetireeMaintenance() {
    const id = state.currentRetireeId || Number(document.getElementById("retiree-maint-id").value);
    const status = document.getElementById("retiree-maintenance-status");
    if (!id) {
        return;
    }
    status.className = "status";
    status.textContent = "正在保存...";
    try {
        const detail = await putJson(`/api/retirement/retirees/${id}`, collectRetireeMaintenancePayload());
        fillRetireeMaintenance(detail);
        showSuccess(status, "保存成功");
        void loadRetireePersonnel();
    } catch (error) {
        showError(status, error);
    }
}

async function approveRetireeFromModal() {
    const id = state.currentRetireeId || Number(document.getElementById("retiree-maint-id").value);
    const status = document.getElementById("retiree-maintenance-status");
    if (!id) {
        return;
    }
    if (!window.confirm("确认将该人员标记为审核通过？通过后将锁定编辑。")) {
        return;
    }
    status.className = "status";
    status.textContent = "正在审核...";
    try {
        const detail = await postJson(`/api/retirement/retirees/${id}/approve`, {});
        fillRetireeMaintenance(detail);
        status.className = "status success";
        status.textContent = "已审核通过";
        void loadRetireePersonnel();
    } catch (error) {
        showError(status, error);
    }
}

async function cancelRetireeApprovalFromModal() {
    const id = state.currentRetireeId || Number(document.getElementById("retiree-maint-id").value);
    const status = document.getElementById("retiree-maintenance-status");
    if (!id) {
        return;
    }
    if (!window.confirm("确认取消审核并退回待办退休？")) {
        return;
    }
    status.className = "status";
    status.textContent = "正在取消审核...";
    try {
        const detail = await postJson(`/api/retirement/retirees/${id}/cancel-approval`, {});
        fillRetireeMaintenance(detail);
        status.className = "status success";
        status.textContent = "已取消审核，状态为待办退休";
        void loadRetireePersonnel();
    } catch (error) {
        showError(status, error);
    }
}

async function approveRetireeFromList(id, name) {
    if (!id) {
        return;
    }
    if (!window.confirm(`确认将「${name || id}」标记为审批通过？`)) {
        return;
    }
    const status = document.getElementById("retiree-personnel-status");
    status.className = "status";
    status.textContent = "正在审批...";
    try {
        await postJson(`/api/retirement/retirees/${id}/approve`, {});
        status.className = "status success";
        status.textContent = "已审批通过";
        void loadRetireePersonnel();
    } catch (error) {
        showError(status, error);
    }
}

async function loadRetirementRatioStandards() {
    const status = document.getElementById("retirement-ratio-standards-status");
    const rows = document.getElementById("retirement-ratio-standards-rows");
    status.className = "status";
    status.textContent = "正在加载折算比例标准...";
    rows.innerHTML = "";
    try {
        const result = await getJson("/api/retirement/ratio-standards");
        const content = Array.isArray(result) ? result : [];
        rows.innerHTML = content.length ? content.map(row => `
            <tr>
                <td>${escapeHtml(row.category || "")}</td>
                <td>${escapeHtml(row.yearBand1 || "")}</td>
                <td>${row.rate1 == null ? "" : escapeHtml(String(row.rate1))}</td>
                <td>${escapeHtml(row.yearBand2 || "")}</td>
                <td>${row.rate2 == null ? "" : escapeHtml(String(row.rate2))}</td>
                <td>${escapeHtml(row.yearBand3 || "")}</td>
                <td>${row.rate3 == null ? "" : escapeHtml(String(row.rate3))}</td>
                <td>${escapeHtml(row.yearBand4 || "")}</td>
                <td>${row.rate4 == null ? "" : escapeHtml(String(row.rate4))}</td>
                <td>${escapeHtml(row.yearBand5 || "")}</td>
                <td>${row.rate5 == null ? "" : escapeHtml(String(row.rate5))}</td>
            </tr>
        `).join("") : `<tr><td colspan="11">暂无 zsbl06 标准数据</td></tr>`;
        status.textContent = `已加载 ${content.length} 类折算比例`;
    } catch (error) {
        rows.innerHTML = `<tr><td colspan="11">加载失败</td></tr>`;
        showError(status, error);
    }
}

async function onDictionarySearch(event) {
    event.preventDefault();
    await loadDictionaries();
}

async function refreshDictionaryCategories() {
    const select = document.getElementById("dictionary-category");
    if (!select) {
        return;
    }
    const previous = select.value;
    try {
        const categories = await getJson("/api/dictionaries/categories");
        const options = [`<option value="">请选择</option>`]
            .concat((categories || []).map(item => {
                const code = item.code || "";
                const name = item.name || "";
                const label = name && name !== code ? `${code} ${name}` : code;
                return `<option value="${escapeHtml(code)}">${escapeHtml(label)}</option>`;
            }));
        select.innerHTML = options.join("");
        if (previous && (categories || []).some(item => String(item.code) === String(previous))) {
            select.value = previous;
        } else if ((categories || []).length) {
            select.value = categories[0].code || "";
        } else {
            select.value = "";
        }
    } catch (error) {
        select.innerHTML = `<option value="">请选择</option>`;
        select.value = "";
        console.warn("加载字典分类失败", error);
    }
}

async function onLocalPolicySearch(event) {
    event.preventDefault();
    document.getElementById("local-policy-page").value = "0";
    await loadLocalPolicies();
}

async function onAudit(event) {
    event.preventDefault();
    await loadAuditPersonnel();
}

async function onPayrollHistorySearch(event) {
    event.preventDefault();
    state.payrollHistoryPage = 0;
    await loadPayrollHistory();
}

function gotoPayrollHistoryPage(page) {
    const totalPages = Math.max(state.payrollHistoryTotalPages || 1, 1);
    const target = Math.min(Math.max(page, 0), totalPages - 1);
    if (target === state.payrollHistoryPage) {
        return;
    }
    state.payrollHistoryPage = target;
    void loadPayrollHistory();
}

function renderPayrollHistoryPagination(totalElements, totalPages) {
    const bar = document.getElementById("payroll-history-pagination");
    if (!bar) {
        return;
    }
    const pages = Math.max(totalPages || 1, 1);
    state.payrollHistoryTotalPages = pages;
    const current = state.payrollHistoryPage;
    bar.classList.toggle("hidden", !totalElements);
    const totalPagesEl = document.getElementById("payroll-history-total-pages");
    const totalCountEl = document.getElementById("payroll-history-total-count");
    const pageInput = document.getElementById("payroll-history-page-input");
    if (totalPagesEl) {
        totalPagesEl.textContent = String(pages);
    }
    if (totalCountEl) {
        totalCountEl.textContent = String(totalElements || 0);
    }
    if (pageInput) {
        pageInput.value = String(current + 1);
        pageInput.max = String(pages);
        pageInput.disabled = !totalElements;
    }
    const noData = !totalElements;
    document.getElementById("payroll-history-first").disabled = noData || current <= 0;
    document.getElementById("payroll-history-prev").disabled = noData || current <= 0;
    document.getElementById("payroll-history-next").disabled = noData || current >= pages - 1;
    document.getElementById("payroll-history-last").disabled = noData || current >= pages - 1;
}

function formatPayrollHistoryLevelStep(row) {
    const level = String(row.gradeSalaryLevel || "").trim();
    const step = String(row.positionSalaryGrade || "").trim();
    if (level && step) {
        return `${level}-${step}`;
    }
    return level || step || "-";
}

async function onTeachingAllowanceSearch(event) {
    event.preventDefault();
    document.getElementById("teaching-allowance-page").value = "0";
    await loadTeachingAllowanceAdjustments();
}

async function onRankChangeSearch(event, config) {
    event.preventDefault();
    const base = rankChangeStateBase(config.idPrefix);
    state[`${base}Page`] = 0;
    await loadRankChangePromotions(config);
}

function gotoRankChangePage(config, page) {
    const base = rankChangeStateBase(config.idPrefix);
    const totalPages = Math.max(state[`${base}TotalPages`] || 1, 1);
    const target = Math.min(Math.max(page, 0), totalPages - 1);
    if (target === state[`${base}Page`]) {
        return;
    }
    state[`${base}Page`] = target;
    void loadRankChangePromotions(config);
}

function bindRankChangePagination(config) {
    const prefix = config.idPrefix;
    document.getElementById(`${prefix}-first`)?.addEventListener("click", () => gotoRankChangePage(config, 0));
    document.getElementById(`${prefix}-prev`)?.addEventListener("click", () => gotoRankChangePage(config, state[`${rankChangeStateBase(prefix)}Page`] - 1));
    document.getElementById(`${prefix}-next`)?.addEventListener("click", () => gotoRankChangePage(config, state[`${rankChangeStateBase(prefix)}Page`] + 1));
    document.getElementById(`${prefix}-last`)?.addEventListener("click", () => gotoRankChangePage(config, state[`${rankChangeStateBase(prefix)}TotalPages`] - 1));
    document.getElementById(`${prefix}-page-input`)?.addEventListener("change", event => {
        const value = Number.parseInt(event.target.value, 10);
        if (!Number.isFinite(value) || value < 1) {
            return;
        }
        gotoRankChangePage(config, value - 1);
    });
}

function renderRankChangePagination(config, totalElements, totalPages) {
    const prefix = config.idPrefix;
    const base = rankChangeStateBase(prefix);
    const bar = document.getElementById(`${prefix}-pagination`);
    if (!bar) {
        return;
    }
    const pages = Math.max(totalPages || 1, 1);
    state[`${base}TotalPages`] = pages;
    const current = state[`${base}Page`];
    bar.classList.toggle("hidden", !totalElements);
    const totalPagesEl = document.getElementById(`${prefix}-total-pages`);
    const totalCountEl = document.getElementById(`${prefix}-total-count`);
    const pageInput = document.getElementById(`${prefix}-page-input`);
    if (totalPagesEl) {
        totalPagesEl.textContent = String(pages);
    }
    if (totalCountEl) {
        totalCountEl.textContent = String(totalElements || 0);
    }
    if (pageInput) {
        pageInput.value = String(current + 1);
        pageInput.max = String(pages);
        pageInput.disabled = !totalElements;
    }
    const noData = !totalElements;
    document.getElementById(`${prefix}-first`).disabled = noData || current <= 0;
    document.getElementById(`${prefix}-prev`).disabled = noData || current <= 0;
    document.getElementById(`${prefix}-next`).disabled = noData || current >= pages - 1;
    document.getElementById(`${prefix}-last`).disabled = noData || current >= pages - 1;
}

async function onPoliceRankChangeSearch(event) {
    return onRankChangeSearch(event, RANK_CHANGE_MODULES.police);
}

function gotoPoliceRankChangePage(page) {
    return gotoRankChangePage(RANK_CHANGE_MODULES.police, page);
}

function renderPoliceRankChangePagination(totalElements, totalPages) {
    return renderRankChangePagination(RANK_CHANGE_MODULES.police, totalElements, totalPages);
}

async function onNormalPromotionSearch(event) {
    event.preventDefault();
    state.normalPromotionPage = 0;
    await loadNormalPromotions();
}

function gotoNormalPromotionPage(page) {
    const totalPages = Math.max(state.normalPromotionTotalPages || 1, 1);
    const target = Math.min(Math.max(page, 0), totalPages - 1);
    if (target === state.normalPromotionPage) {
        return;
    }
    state.normalPromotionPage = target;
    void loadNormalPromotions();
}

function renderNormalPromotionPagination(totalElements, totalPages) {
    const bar = document.getElementById("normal-promotion-pagination");
    if (!bar) {
        return;
    }
    const pages = Math.max(totalPages || 1, 1);
    state.normalPromotionTotalPages = pages;
    const current = state.normalPromotionPage;
    bar.classList.toggle("hidden", !totalElements);
    const totalPagesEl = document.getElementById("normal-promotion-total-pages");
    const totalCountEl = document.getElementById("normal-promotion-total-count");
    const pageInput = document.getElementById("normal-promotion-page-input");
    if (totalPagesEl) {
        totalPagesEl.textContent = String(pages);
    }
    if (totalCountEl) {
        totalCountEl.textContent = String(totalElements || 0);
    }
    if (pageInput) {
        pageInput.value = String(current + 1);
        pageInput.max = String(pages);
        pageInput.disabled = !totalElements;
    }
    const noData = !totalElements;
    document.getElementById("normal-promotion-first").disabled = noData || current <= 0;
    document.getElementById("normal-promotion-prev").disabled = noData || current <= 0;
    document.getElementById("normal-promotion-next").disabled = noData || current >= pages - 1;
    document.getElementById("normal-promotion-last").disabled = noData || current >= pages - 1;
}

function initializeNormalPromotionPage() {
    const yearInput = document.getElementById("normal-promotion-year");
    if (yearInput && !yearInput.value) {
        yearInput.value = String(new Date().getFullYear());
    }
    updateAllPayrollFeatureWriteUi();
}

function currentNormalPromotionYear() {
    const yearInput = document.getElementById("normal-promotion-year");
    const year = yearInput?.value?.trim();
    if (year) {
        return year;
    }
    const currentYear = String(new Date().getFullYear());
    if (yearInput) {
        yearInput.value = currentYear;
    }
    return currentYear;
}

function normalPromotionYearParam() {
    const params = new URLSearchParams({ year: currentNormalPromotionYear() });
    const laterPeriodMode = normalPromotionLaterPeriodMode();
    if (laterPeriodMode) {
        params.set("laterPeriodMode", laterPeriodMode);
    }
    return params.toString();
}

function normalPromotionLaterPeriodMode() {
    return document.getElementById("normal-promotion-later-period-mode")?.value?.trim() || "block";
}

function initializeLevelPromotionPage() {
    const yearInput = document.getElementById("level-promotion-year");
    if (yearInput && !yearInput.value) {
        yearInput.value = String(new Date().getFullYear());
    }
    updateAllPayrollFeatureWriteUi();
}

function currentLevelPromotionYear() {
    const yearInput = document.getElementById("level-promotion-year");
    const year = yearInput?.value?.trim();
    if (year) {
        return year;
    }
    const currentYear = String(new Date().getFullYear());
    if (yearInput) {
        yearInput.value = currentYear;
    }
    return currentYear;
}

function levelPromotionYearParam() {
    const params = new URLSearchParams({ year: currentLevelPromotionYear() });
    const laterPeriodMode = levelPromotionLaterPeriodMode();
    if (laterPeriodMode) {
        params.set("laterPeriodMode", laterPeriodMode);
    }
    return params.toString();
}

function levelPromotionLaterPeriodMode() {
    return document.getElementById("level-promotion-later-period-mode")?.value?.trim() || "block";
}

async function onLevelPromotionSearch(event) {
    event.preventDefault();
    state.levelPromotionPage = 0;
    await loadLevelPromotions();
}

function gotoLevelPromotionPage(page) {
    const totalPages = Math.max(state.levelPromotionTotalPages || 1, 1);
    const target = Math.min(Math.max(page, 0), totalPages - 1);
    if (target === state.levelPromotionPage) {
        return;
    }
    state.levelPromotionPage = target;
    void loadLevelPromotions();
}

function renderLevelPromotionPagination(totalElements, totalPages) {
    const bar = document.getElementById("level-promotion-pagination");
    if (!bar) {
        return;
    }
    const pages = Math.max(totalPages || 1, 1);
    state.levelPromotionTotalPages = pages;
    const current = state.levelPromotionPage;
    bar.classList.toggle("hidden", !totalElements);
    const totalPagesEl = document.getElementById("level-promotion-total-pages");
    const totalCountEl = document.getElementById("level-promotion-total-count");
    const pageInput = document.getElementById("level-promotion-page-input");
    if (totalPagesEl) {
        totalPagesEl.textContent = String(pages);
    }
    if (totalCountEl) {
        totalCountEl.textContent = String(totalElements || 0);
    }
    if (pageInput) {
        pageInput.value = String(current + 1);
        pageInput.max = String(pages);
        pageInput.disabled = !totalElements;
    }
    const noData = !totalElements;
    document.getElementById("level-promotion-first").disabled = noData || current <= 0;
    document.getElementById("level-promotion-prev").disabled = noData || current <= 0;
    document.getElementById("level-promotion-next").disabled = noData || current >= pages - 1;
    document.getElementById("level-promotion-last").disabled = noData || current >= pages - 1;
}

async function onPositionChangePromotionSearch(event) {
    event.preventDefault();
    state.positionChangePage = 0;
    await loadPositionChangePromotions();
}

function gotoPositionChangePage(page) {
    const totalPages = Math.max(state.positionChangeTotalPages || 1, 1);
    const target = Math.min(Math.max(page, 0), totalPages - 1);
    if (target === state.positionChangePage) {
        return;
    }
    state.positionChangePage = target;
    void loadPositionChangePromotions();
}

function renderPositionChangePagination(totalElements, totalPages) {
    const bar = document.getElementById("position-change-pagination");
    if (!bar) {
        return;
    }
    const pages = Math.max(totalPages || 1, 1);
    state.positionChangeTotalPages = pages;
    const current = state.positionChangePage;
    bar.classList.toggle("hidden", !totalElements);
    const totalPagesEl = document.getElementById("position-change-total-pages");
    const totalCountEl = document.getElementById("position-change-total-count");
    const pageInput = document.getElementById("position-change-page-input");
    if (totalPagesEl) {
        totalPagesEl.textContent = String(pages);
    }
    if (totalCountEl) {
        totalCountEl.textContent = String(totalElements || 0);
    }
    if (pageInput) {
        pageInput.value = String(current + 1);
        pageInput.max = String(pages);
        pageInput.disabled = !totalElements;
    }
    const noData = !totalElements;
    document.getElementById("position-change-first").disabled = noData || current <= 0;
    document.getElementById("position-change-prev").disabled = noData || current <= 0;
    document.getElementById("position-change-next").disabled = noData || current >= pages - 1;
    document.getElementById("position-change-last").disabled = noData || current >= pages - 1;
}

async function onEducationPromotionSearch(event) {
    event.preventDefault();
    await loadEducationPromotions();
}

async function onRegularizationSearch(event) {
    event.preventDefault();
    await loadRegularizations();
}

async function onRegularizationHighGradeSearch(event) {
    event.preventDefault();
    state.regularizationHighGradePage = 0;
    await loadRegularizationHighGrades();
}

function gotoRegularizationHighGradePage(page) {
    const totalPages = Math.max(state.regularizationHighGradeTotalPages || 1, 1);
    const target = Math.min(Math.max(page, 0), totalPages - 1);
    if (target === state.regularizationHighGradePage) {
        return;
    }
    state.regularizationHighGradePage = target;
    void loadRegularizationHighGrades();
}

function renderRegularizationHighGradePagination(totalElements, totalPages) {
    const bar = document.getElementById("regularization-high-grade-pagination");
    if (!bar) {
        return;
    }
    const pages = Math.max(totalPages || 1, 1);
    state.regularizationHighGradeTotalPages = pages;
    const current = state.regularizationHighGradePage;
    bar.classList.toggle("hidden", !totalElements);
    const totalPagesEl = document.getElementById("regularization-high-grade-total-pages");
    const totalCountEl = document.getElementById("regularization-high-grade-total-count");
    const pageInput = document.getElementById("regularization-high-grade-page-input");
    if (totalPagesEl) {
        totalPagesEl.textContent = String(pages);
    }
    if (totalCountEl) {
        totalCountEl.textContent = String(totalElements || 0);
    }
    if (pageInput) {
        pageInput.value = String(current + 1);
        pageInput.max = String(pages);
        pageInput.disabled = !totalElements;
    }
    const noData = !totalElements;
    document.getElementById("regularization-high-grade-first").disabled = noData || current <= 0;
    document.getElementById("regularization-high-grade-prev").disabled = noData || current <= 0;
    document.getElementById("regularization-high-grade-next").disabled = noData || current >= pages - 1;
    document.getElementById("regularization-high-grade-last").disabled = noData || current >= pages - 1;
}

async function onFloatingToFixedSearch(event) {
    event.preventDefault();
    document.getElementById("floating-to-fixed-page").value = "0";
    await loadFloatingToFixedConversions();
}

async function onInternSalaryChangeSearch(event) {
    event.preventDefault();
    document.getElementById("intern-salary-change-page").value = "0";
    await loadInternSalaryChanges();
}

async function onNewPersonnelSalarySearch(event) {
    event.preventDefault();
    state.newPersonnelSalaryPage = 0;
    await loadNewPersonnelSalaryDeterminations();
}

function gotoNewPersonnelSalaryPage(page) {
    const totalPages = Math.max(state.newPersonnelSalaryTotalPages || 1, 1);
    const target = Math.min(Math.max(page, 0), totalPages - 1);
    if (target === state.newPersonnelSalaryPage) {
        return;
    }
    state.newPersonnelSalaryPage = target;
    void loadNewPersonnelSalaryDeterminations();
}

function renderNewPersonnelSalaryPagination(totalElements, totalPages) {
    const bar = document.getElementById("new-personnel-salary-pagination");
    if (!bar) {
        return;
    }
    const pages = Math.max(totalPages || 1, 1);
    state.newPersonnelSalaryTotalPages = pages;
    const current = state.newPersonnelSalaryPage;
    bar.classList.toggle("hidden", !totalElements);
    const totalPagesEl = document.getElementById("new-personnel-salary-total-pages");
    const totalCountEl = document.getElementById("new-personnel-salary-total-count");
    const pageInput = document.getElementById("new-personnel-salary-page-input");
    if (totalPagesEl) {
        totalPagesEl.textContent = String(pages);
    }
    if (totalCountEl) {
        totalCountEl.textContent = String(totalElements || 0);
    }
    if (pageInput) {
        pageInput.value = String(current + 1);
        pageInput.max = String(pages);
        pageInput.disabled = !totalElements;
    }
    const noData = !totalElements;
    document.getElementById("new-personnel-salary-first").disabled = noData || current <= 0;
    document.getElementById("new-personnel-salary-prev").disabled = noData || current <= 0;
    document.getElementById("new-personnel-salary-next").disabled = noData || current >= pages - 1;
    document.getElementById("new-personnel-salary-last").disabled = noData || current >= pages - 1;
}

async function onBasicStandardsSearch(event) {
    event.preventDefault();
    await loadBasicStandards();
}

async function onAllowanceStandardsSearch(event) {
    event.preventDefault();
    await loadAllowanceStandards();
}

function allowancePerformanceCategoryLabel(code) {
    const value = Number(code);
    switch (value) {
        case 1:
            return "1 机关";
        case 2:
            return "2 义务教育";
        case 3:
            return "3 公共卫生";
        case 5:
            return "5 其他事业";
        default:
            return String(code ?? "");
    }
}

/** 机关(jxlb=1)=公务员口径；其余=事业口径。全部时按事业默认（可随首行再校正）。 */
function allowanceStandardColumnCaptions(performanceCategory) {
    const civil = String(performanceCategory ?? "") === "1";
    if (civil) {
        return {
            dfbt2: "生活性补贴",
            sdbt: "工作性津贴",
            editDfbt2: "改生活性",
            editSdbt: "改工作性",
        };
    }
    return {
        dfbt2: "基础性绩效",
        sdbt: "工作性补贴",
        editDfbt2: "改基础绩效",
        editSdbt: "改工作性补贴",
    };
}

function updateAllowanceStandardColumnHeaders(performanceCategory, rows) {
    let category = performanceCategory;
    if (category === "" || category == null) {
        const hasCivil = (rows || []).some(row => Number(row.performanceCategory) === 1);
        const hasInstitution = (rows || []).some(row => Number(row.performanceCategory) !== 1);
        if (hasCivil && !hasInstitution) {
            category = "1";
        } else if (hasCivil && hasInstitution) {
            const dfbt2Header = document.getElementById("allowance-standard-dfbt2-header");
            const sdbtHeader = document.getElementById("allowance-standard-sdbt-header");
            if (dfbt2Header) {
                dfbt2Header.textContent = "生活性补贴/基础性绩效";
            }
            if (sdbtHeader) {
                sdbtHeader.textContent = "工作性津贴/补贴";
            }
            return {
                dfbt2: "生活性补贴/基础性绩效",
                sdbt: "工作性津贴/补贴",
                editDfbt2: "改DFBT2",
                editSdbt: "改SDBT",
            };
        }
    }
    const captions = allowanceStandardColumnCaptions(category);
    const dfbt2Header = document.getElementById("allowance-standard-dfbt2-header");
    const sdbtHeader = document.getElementById("allowance-standard-sdbt-header");
    if (dfbt2Header) {
        dfbt2Header.textContent = captions.dfbt2;
    }
    if (sdbtHeader) {
        sdbtHeader.textContent = captions.sdbt;
    }
    return captions;
}

async function refreshAllowanceStandardPeriods() {
    const select = document.getElementById("allowance-standard-year-month");
    if (!select) {
        return;
    }
    const previous = select.value;
    try {
        const periods = await getJson("/api/payroll/allowance-standards/periods");
        const options = [`<option value="">请选择</option>`]
            .concat((periods || []).map(period =>
                `<option value="${escapeHtml(period)}">${escapeHtml(period)}</option>`));
        select.innerHTML = options.join("");
        if (previous && (periods || []).includes(previous)) {
            select.value = previous;
        } else if ((periods || []).length > 0) {
            select.value = periods[0];
        } else {
            select.value = "";
        }
    } catch (error) {
        select.innerHTML = `<option value="">请选择</option>`;
        select.value = "";
        console.warn("加载津补贴标准年月失败", error);
    }
}

async function refreshAllowanceStandardCategories() {
    const select = document.getElementById("allowance-standard-category");
    const yearMonth = document.getElementById("allowance-standard-year-month")?.value?.trim() || "";
    if (!select) {
        return;
    }
    const previous = select.value;
    if (!yearMonth) {
        select.innerHTML = `<option value="">全部</option>`;
        select.value = "";
        return;
    }
    try {
        const params = new URLSearchParams({ standardYearMonth: yearMonth });
        const categories = await getJson(`/api/payroll/allowance-standards/categories?${params}`);
        const options = [`<option value="">全部</option>`]
            .concat((categories || []).map(code =>
                `<option value="${escapeHtml(code)}">${escapeHtml(allowancePerformanceCategoryLabel(code))}</option>`));
        select.innerHTML = options.join("");
        if (previous !== "" && (categories || []).map(String).includes(String(previous))) {
            select.value = previous;
        } else {
            select.value = "";
        }
    } catch (error) {
        select.innerHTML = `<option value="">全部</option>`;
        select.value = "";
        console.warn("加载津补贴绩效类别失败", error);
    }
}

async function refreshAllowanceStandardPositionCategories() {
    const select = document.getElementById("allowance-standard-position-category");
    const yearMonth = document.getElementById("allowance-standard-year-month")?.value?.trim() || "";
    const performanceCategory = document.getElementById("allowance-standard-category")?.value?.trim() || "";
    if (!select) {
        return;
    }
    const previous = select.value;
    if (!yearMonth) {
        select.innerHTML = `<option value="">全部</option>`;
        select.value = "";
        return;
    }
    try {
        const params = new URLSearchParams({ standardYearMonth: yearMonth });
        if (performanceCategory !== "") {
            params.set("performanceCategory", performanceCategory);
        }
        const categories = await getJson(`/api/payroll/allowance-standards/position-categories?${params}`);
        const options = [`<option value="">全部</option>`]
            .concat((categories || []).map(row => {
                const code = row.positionCode || "";
                const name = row.name || "";
                const label = name ? `${code} ${name}` : code;
                return `<option value="${escapeHtml(code)}">${escapeHtml(label)}</option>`;
            }));
        select.innerHTML = options.join("");
        if (previous && (categories || []).some(row => String(row.positionCode) === String(previous))) {
            select.value = previous;
        } else {
            select.value = "";
        }
    } catch (error) {
        select.innerHTML = `<option value="">全部</option>`;
        select.value = "";
        console.warn("加载职务岗位类别失败", error);
    }
}

async function onRankAllowanceStandardsSearch(event) {
    event.preventDefault();
    await loadRankAllowanceStandards();
}

async function refreshRankAllowanceStandardPeriods() {
    const select = document.getElementById("rank-standard-year-month");
    const category = document.getElementById("rank-standard-category")?.value?.trim() || "";
    if (!select) {
        return;
    }
    const previous = select.value;
    const params = new URLSearchParams();
    if (category) {
        params.set("category", category);
    }
    try {
        const periods = await getJson(`/api/payroll/rank-allowance-standards/periods${params.toString() ? `?${params}` : ""}`);
        const options = [`<option value="">全部</option>`]
            .concat((periods || []).map(period =>
                `<option value="${escapeHtml(period)}">${escapeHtml(period)}</option>`));
        select.innerHTML = options.join("");
        if (previous && (periods || []).includes(previous)) {
            select.value = previous;
        } else {
            select.value = "";
        }
    } catch (error) {
        select.innerHTML = `<option value="">全部</option>`;
        select.value = "";
        console.warn("加载津贴标准年月失败", error);
    }
}

async function onRetainedAllowanceStandardsSearch(event) {
    event.preventDefault();
    await loadRetainedAllowanceStandards();
}

async function refreshRetainedAllowanceStandardPositionCategories() {
    const select = document.getElementById("retained-standard-position-category");
    if (!select) {
        return;
    }
    const previous = select.value;
    try {
        const categories = await getJson("/api/payroll/retained-allowance-standards/position-categories");
        const options = [`<option value="">全部</option>`]
            .concat((categories || []).map(item => {
                const code = item.positionCode || "";
                const name = item.name || "";
                const label = name && name !== code ? `${code} ${name}` : code;
                return `<option value="${escapeHtml(code)}">${escapeHtml(label)}</option>`;
            }));
        select.innerHTML = options.join("");
        if (previous && (categories || []).some(item => String(item.positionCode) === String(previous))) {
            select.value = previous;
        } else {
            select.value = "";
        }
    } catch (error) {
        select.innerHTML = `<option value="">全部</option>`;
        select.value = "";
        console.warn("加载保留福补职务岗位类别失败", error);
    }
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
    await loadWageReformStandards();
}

async function refreshWageReformStandardPositionCategories() {
    const select = document.getElementById("wage-reform-position-category");
    if (!select) {
        return;
    }
    const previous = select.value;
    try {
        const categories = await getJson("/api/payroll/wage-reform-standards/position-categories");
        const options = [`<option value="">全部</option>`]
            .concat((categories || []).map(item => {
                const code = item.positionCode || "";
                const name = item.name || "";
                const label = name && name !== code ? `${code} ${name}` : code;
                return `<option value="${escapeHtml(code)}">${escapeHtml(label)}</option>`;
            }));
        select.innerHTML = options.join("");
        if (previous && (categories || []).some(item => String(item.positionCode) === String(previous))) {
            select.value = previous;
        } else if ((categories || []).length) {
            select.value = categories[0].positionCode || "";
        } else {
            select.value = "";
        }
    } catch (error) {
        select.innerHTML = `<option value="">全部</option>`;
        select.value = "";
        console.warn("加载套改标准职务岗位类别失败", error);
    }
}

async function refreshWageReformStandardPositions() {
    const select = document.getElementById("wage-reform-position");
    if (!select) {
        return;
    }
    const positionPrefix = document.getElementById("wage-reform-position-category")?.value.trim() || "";
    const previous = select.value;
    try {
        const params = new URLSearchParams();
        if (positionPrefix) {
            params.set("positionPrefix", positionPrefix);
        }
        const positions = await getJson(`/api/payroll/wage-reform-standards/positions?${params}`);
        const options = [`<option value="">全部</option>`]
            .concat((positions || []).map(item => {
                const code = item.positionCode || "";
                const name = item.name || "";
                const label = name && name !== code ? `${code} ${name}` : code;
                return `<option value="${escapeHtml(code)}">${escapeHtml(label)}</option>`;
            }));
        select.innerHTML = options.join("");
        if (previous && (positions || []).some(item => String(item.positionCode) === String(previous))) {
            select.value = previous;
        } else {
            select.value = "";
        }
    } catch (error) {
        select.innerHTML = `<option value="">全部</option>`;
        select.value = "";
        console.warn("加载套改标准职务岗位失败", error);
    }
}

async function onOtherAllowanceStandardsSearch(event) {
    event.preventDefault();
    await loadOtherAllowanceStandards();
}

async function refreshOtherAllowanceStandardPeriods() {
    const typeEl = document.getElementById("other-allowance-standard-type");
    const select = document.getElementById("other-allowance-filter-year-month");
    const wrap = document.getElementById("other-allowance-year-month-wrap");
    if (!typeEl || !select) {
        return;
    }
    const standardType = typeEl.value;
    if (wrap) {
        wrap.classList.toggle("hidden", standardType === "civilized");
    }
    if (standardType === "civilized") {
        select.innerHTML = `<option value="">全部</option>`;
        select.value = "";
        return;
    }
    const previous = select.value;
    try {
        const periods = await getJson(`/api/payroll/other-allowance-standards/periods?standardType=${encodeURIComponent(standardType)}`);
        const options = [`<option value="">全部</option>`]
            .concat((periods || []).map(period =>
                `<option value="${escapeHtml(period)}">${escapeHtml(period)}</option>`));
        select.innerHTML = options.join("");
        if (previous && (periods || []).includes(previous)) {
            select.value = previous;
        } else if ((periods || []).length) {
            select.value = periods[0];
        } else {
            select.value = "";
        }
    } catch (error) {
        select.innerHTML = `<option value="">全部</option>`;
        select.value = "";
        console.warn("加载其他补贴标准年月失败", error);
    }
}

function otherAllowanceUsesPositionCategory(standardType) {
    return standardType === "property" || standardType === "communication";
}

async function refreshOtherAllowanceStandardPositionCategories() {
    const typeEl = document.getElementById("other-allowance-standard-type");
    const select = document.getElementById("other-allowance-position-category");
    const wrap = document.getElementById("other-allowance-position-category-wrap");
    if (!typeEl || !select) {
        return;
    }
    const standardType = typeEl.value;
    const standardYearMonth = document.getElementById("other-allowance-filter-year-month")?.value.trim() || "";
    if (wrap) {
        wrap.classList.toggle("hidden", !otherAllowanceUsesPositionCategory(standardType));
    }
    if (!otherAllowanceUsesPositionCategory(standardType)) {
        select.innerHTML = `<option value="">全部</option>`;
        select.value = "";
        return;
    }
    const previous = select.value;
    try {
        const params = new URLSearchParams({ standardType });
        if (standardYearMonth) {
            params.set("standardYearMonth", standardYearMonth);
        }
        const categories = await getJson(`/api/payroll/other-allowance-standards/position-categories?${params}`);
        const options = [`<option value="">全部</option>`]
            .concat((categories || []).map(item => {
                const code = item.positionCode || "";
                const name = item.name || "";
                const label = name && name !== code ? `${code} ${name}` : code;
                return `<option value="${escapeHtml(code)}">${escapeHtml(label)}</option>`;
            }));
        select.innerHTML = options.join("");
        if (previous && (categories || []).some(item => String(item.positionCode) === String(previous))) {
            select.value = previous;
        } else {
            select.value = "";
        }
    } catch (error) {
        select.innerHTML = `<option value="">全部</option>`;
        select.value = "";
        console.warn("加载其他补贴职务岗位类别失败", error);
    }
}

async function loadPersonnel() {
    const organizationCode = selectedOrganizationCode("organization-code");
    const keyword = document.getElementById("keyword").value.trim();
    const size = document.getElementById("page-size").value || "20";
    const params = new URLSearchParams({ page: String(state.personnelPage), size });
    if (organizationCode) {
        params.set("organizationCode", organizationCode);
    }
    if (keyword) {
        params.set("keyword", keyword);
    }
    if (state.personnelSort) {
        params.set("sort", state.personnelSort);
        params.set("direction", state.personnelDirection);
    }

    const status = document.getElementById("personnel-status");
    const rows = document.getElementById("personnel-rows");
    status.className = "status";
    status.textContent = "正在查询人员...";
    rows.innerHTML = "";

    try {
        const page = await getJson(`/api/personnel?${params}`);
        const totalPages = Math.max(page.totalPages || 0, 1);
        state.personnelTotalPages = totalPages;
        if (state.personnelPage > totalPages - 1) {
            state.personnelPage = Math.max(totalPages - 1, 0);
        }
        const startIndex = page.totalElements === 0 ? 0 : state.personnelPage * (parseInt(size, 10) || 20) + 1;
        const endIndex = startIndex === 0 ? 0 : startIndex + (page.content || []).length - 1;
        status.textContent = `共 ${page.totalElements} 人，第 ${startIndex}-${endIndex} 人（第 ${state.personnelPage + 1} / ${totalPages} 页）`;
        rows.innerHTML = (page.content || []).map(person => `
            <tr>
                <td class="col-org">${escapeHtml(person.organizationCode)} ${escapeHtml(person.organizationName || "")}</td>
                <td>${escapeHtml(person.personCode)}</td>
                <td>${escapeHtml(person.name)}</td>
                <td>${escapeHtml(person.idCard || "")}</td>
                <td>${escapeHtml(person.gender || "")}</td>
                <td>${escapeHtml(person.birthYearMonth || "")}</td>
                <td>${escapeHtml(person.currentPosition || "")}</td>
                <td>${escapeHtml(person.appointmentPosition || "")}</td>
                <td class="col-actions">${renderPersonnelActions(person)}</td>
            </tr>
        `).join("");
        rows.querySelectorAll("button[data-payroll-preview]").forEach(button => {
            button.addEventListener("click", () => openPayrollPreviewModal(button.dataset.payrollPreview));
        });
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
        renderPersonnelPagination(page.totalElements || 0);
        updatePersonnelSortIndicators();
    } catch (error) {
        showError(status, error);
    }
}

function renderPersonnelPagination(totalElements) {
    const bar = document.getElementById("personnel-pagination");
    if (!bar) {
        return;
    }
    const totalPages = state.personnelTotalPages;
    bar.classList.toggle("hidden", totalElements === 0);
    const current = state.personnelPage;
    document.getElementById("personnel-total-pages").textContent = String(totalPages);
    const pageInput = document.getElementById("personnel-page-input");
    pageInput.value = String(current + 1);
    pageInput.max = String(totalPages);
    document.getElementById("personnel-first").disabled = current <= 0;
    document.getElementById("personnel-prev").disabled = current <= 0;
    document.getElementById("personnel-next").disabled = current >= totalPages - 1;
    document.getElementById("personnel-last").disabled = current >= totalPages - 1;
}

function gotoPersonnelPage(page) {
    const target = Math.min(Math.max(page, 0), state.personnelTotalPages - 1);
    if (target === state.personnelPage) {
        return;
    }
    state.personnelPage = target;
    loadPersonnel();
}

function onPersonnelSort(column) {
    if (state.personnelSort === column) {
        state.personnelDirection = state.personnelDirection === "asc" ? "desc" : "asc";
    } else {
        state.personnelSort = column;
        state.personnelDirection = "asc";
    }
    state.personnelPage = 0;
    loadPersonnel();
}

function updatePersonnelSortIndicators() {
    document.querySelectorAll(".personnel-table th.sortable").forEach(th => {
        th.classList.remove("sort-asc", "sort-desc");
        if (th.dataset.sort === state.personnelSort) {
            th.classList.add(state.personnelDirection === "desc" ? "sort-desc" : "sort-asc");
        }
    });
}

function renderPersonnelActions(person) {
    const actions = [];
    if (hasPayrollRead()) {
        actions.push(`<button class="row-action" data-payroll-preview="${person.uid}">工资试算</button>`);
    }
    if (hasPersonnelWrite()) {
        actions.push(`<button class="row-action" data-maint-edit="${person.uid}" type="button">编辑</button>`);
        actions.push(`<button class="row-action" data-maint-change="${person.uid}" data-person-name="${escapeHtml(person.name)}" type="button">变动</button>`);
        actions.push(`<button class="row-action danger-button" data-maint-delete="${person.uid}" type="button">删除</button>`);
    }
    return actions.join(" ");
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
    if (changeType === "调动" && (!targetOrganization || !targetOrganization.code)) {
        alert("系统内调动必须选择调往单位。");
        return;
    }
    const remark = prompt("请输入备注（可留空）：", "");
    if (remark === null) {
        return;
    }
    const defaultRemark = changeDescription && changeDescription !== changeType ? changeDescription : "";
    const transferRemark = targetOrganization
        ? `调往单位：${targetOrganization.name || ""}（${targetOrganization.code || ""}）`
        : "";
    const finalRemark = [defaultRemark, transferRemark, remark.trim()].filter(Boolean).join("；");
    const confirmText = changeType === "调动"
        ? `确认将 ${name || "该人员"} 系统内调动至“${targetOrganization.name || targetOrganization.code}”？\n人员将仍保留在职，不进入变动人员库，调动信息写入调动履历。`
        : `确认将 ${name || "该人员"} 办理为“${changeType.trim()}”？该人员将转入变动人员信息。`;
    if (!confirm(confirmText)) {
        return;
    }
    const status = document.getElementById("personnel-status");
    status.className = "status";
    status.textContent = changeType === "调动" ? "正在办理系统内调动..." : "正在办理人员变动...";
    try {
        const payload = {
            changeType: changeType.trim(),
            effectivePeriod: "",
            remark: finalRemark,
        };
        if (changeType === "调动" && targetOrganization) {
            payload.targetOrganizationCode = targetOrganization.code;
            payload.targetOrganizationName = targetOrganization.name || targetOrganization.code;
        }
        const result = await postJson(`/api/personnel/${encodeURIComponent(uid)}/change`, payload);
        status.textContent = result.message || (changeType === "调动" ? "系统内调动完成" : "人员变动处理完成");
        resetPersonnelMaintenanceForm();
        await loadPersonnel();
        if (hasMenu("CHANGED_PERSONNEL")) {
            await loadChangedPersonnel();
        }
    } catch (error) {
        showError(status, error);
    }
}

async function editPersonnelMaintenance(uid) {
    const status = document.getElementById("personnel-status");
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
    const status = document.getElementById("personnel-status");
    status.className = "status";
    status.textContent = "正在删除人员...";
    try {
        await deleteJson(`/api/personnel/${uid}`);
        status.textContent = "删除成功";
        resetPersonnelMaintenanceForm();
        await loadPersonnel();
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
        joinYearMonth: monthPayloadValue("maint-join-year-month"),
        joinType: document.getElementById("maint-join-type").value.trim(),
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
    setMonthInputValue("maint-join-year-month", record.joinYearMonth);
    document.getElementById("maint-join-type").value = record.joinType || "";
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
    state.maintPayrollHistories = [];
    document.getElementById("personnel-maintenance-form").reset();
    document.getElementById("personnel-maintenance-uid").value = "";
    document.getElementById("maint-salary-years").value = "0";
    [
        "maint-education-rows", "maint-position-rows", "maint-payroll-rows", "maint-assessment-rows",
        "maint-award-rows", "maint-rank-rows",
    ].forEach(id => {
        document.getElementById(id).innerHTML = "<tr><td colspan='8'>保存或选择人员后加载记录</td></tr>";
    });
    ["maint-wage-reform-view", "maint-pre-reform-view", "maint-current-payroll-view"].forEach(id => {
        const host = document.getElementById(id);
        if (host) {
            host.innerHTML = `<div class="note-card note-card-muted">保存或选择人员后加载记录</div>`;
        }
    });
    ["maint-projection-period", "maint-projection-total", "maint-projection-stored-total", "maint-projection-difference"].forEach(id => {
        document.getElementById(id).textContent = "-";
    });
    document.getElementById("maint-wage-projection-period").value = "";
    resetProjectionOverview();
    document.getElementById("maint-wage-projection-result").textContent = "暂无推算说明，请选择人员后执行工资推算。";
    document.getElementById("maint-wage-projection-steps").innerHTML = "暂无分步明细，请先执行工资推算。";
}

function resetProjectionOverview() {
    const overview = document.getElementById("maint-wage-projection-overview");
    if (!overview) {
        return;
    }
    overview.innerHTML = `
        <div class="projection-stat"><span>目标年月</span><strong>-</strong></div>
        <div class="projection-stat"><span>推算起点</span><strong>-</strong></div>
        <div class="projection-stat accent"><span>岗位 / 级别</span><strong>-</strong></div>
        <div class="projection-stat"><span>分步合计</span><strong>-</strong></div>
    `;
}

function subrecordEditorFields(config) {
    if (config.sections?.length) {
        return config.sections.flatMap(section => section.fields || []);
    }
    return config.fields || [];
}

function isSalaryLevelPosition(positionCode) {
    const prefix = String(positionCode || "").trim().substring(0, 2);
    if (prefix.length < 2) {
        return isInstitutionPersonnel(state.activePersonnelMaintenance);
    }
    return !["01", "02", "04", "21", "22", "23", "24", "25", "26", "27", "28"].includes(prefix);
}

function isInstitutionPositionCode(positionCode) {
    const prefix = String(positionCode || "").trim().substring(0, 2);
    return ["07", "08", "09", "10", "11"].includes(prefix);
}

function isGovernmentWorkerPositionCode(positionCode) {
    const prefix = String(positionCode || "").trim().substring(0, 2);
    return ["05", "06"].includes(prefix);
}

/** 事业 zwgzdc2 称薪级；机关工勤及其他非级别序列称档次。 */
function salaryStepCaption(positionCode) {
    return isInstitutionPositionCode(positionCode) ? "薪级" : "档次";
}

function salaryStepSalaryCaption(positionCode) {
    if (isInstitutionPositionCode(positionCode)) {
        return "薪级工资";
    }
    if (isGovernmentWorkerPositionCode(positionCode)) {
        return "档次工资";
    }
    return "级别工资";
}

function normalStepChangeTypeLabel(positionCode, baseSalarySource) {
    if (isInstitutionPositionCode(positionCode)) {
        return "正常薪级";
    }
    if (baseSalarySource === "SALARY_LEVEL" || baseSalarySource === "WORKER_GRADE" || isGovernmentWorkerPositionCode(positionCode)) {
        return "正常档次";
    }
    return "正常档次";
}

function payrollHistoryEditorContext(afterRecord, beforeRecord = null) {
    const positionCode = String(afterRecord?.positionCode || beforeRecord?.positionCode
        || state.activePersonnelMaintenance?.currentPositionCode || "").trim();
    return {
        payrollHistory: true,
        positionCode,
        isSalaryLevel: isSalaryLevelPosition(positionCode),
        beforeRecord,
        afterRecord,
    };
}

function resolvePayrollHistoryBeforeRecord(afterRecord) {
    const histories = state.maintPayrollHistories || [];
    if (afterRecord?.id) {
        const afterId = String(afterRecord.id).trim();
        const bySuccessor = histories.find(row => String(row.successorId || "").trim() === afterId);
        if (bySuccessor) {
            return bySuccessor;
        }
    }
    if (!afterRecord?.id) {
        return histories.find(row => row.currentPayroll)
            || histories.find(row => !String(row.successorId || "").trim())
            || null;
    }
    return null;
}

function payrollHistoryAfterDefaults(beforeRecord) {
    if (!beforeRecord) {
        return {};
    }
    const defaults = { ...beforeRecord };
    delete defaults.id;
    delete defaults.successorId;
    delete defaults.currentPayroll;
    delete defaults.appCreated;
    return defaults;
}

function resolvePayrollHistoryFieldMeta(name, defaultLabel, editorContext) {
    if (!editorContext?.payrollHistory) {
        return { label: defaultLabel, hidden: false, emphasis: false };
    }
    if (name === "gradeSalaryLevel" && editorContext.isSalaryLevel) {
        return { label: defaultLabel, hidden: true, emphasis: false };
    }
    if (name === "positionSalaryGrade") {
        return {
            label: salaryStepCaption(editorContext.positionCode),
            hidden: false,
            emphasis: false,
        };
    }
    if (name === "gradeSalary") {
        return {
            label: salaryStepSalaryCaption(editorContext.positionCode),
            hidden: false,
            emphasis: false,
        };
    }
    if (name === "totalAmount") {
        return { label: defaultLabel, hidden: false, emphasis: true };
    }
    return { label: defaultLabel, hidden: false, emphasis: false };
}

function subrecordEditorRenderContext(config, record, beforeRecord = null) {
    if (config.modalClass === "payroll-history-modal") {
        return payrollHistoryEditorContext(record, beforeRecord);
    }
    return null;
}

function formatPayrollCompareBeforeValue(value, inputType) {
    if (value == null || value === "") {
        return "—";
    }
    if (inputType === "number") {
        return money(value);
    }
    const text = String(value).trim();
    return text || "—";
}

function formatPayrollCompareDiff(beforeValue, afterValue, inputType) {
    if (inputType === "number") {
        const beforeNum = Number(beforeValue ?? 0);
        const afterNum = Number(afterValue ?? 0);
        const diff = afterNum - beforeNum;
        if (!Number.isFinite(diff) || diff === 0) {
            return "";
        }
        const sign = diff > 0 ? "+" : "";
        return `${sign}${money(diff)}`;
    }
    const beforeText = String(beforeValue ?? "").trim();
    const afterText = String(afterValue ?? "").trim();
    if (beforeText === afterText) {
        return "";
    }
    return "变";
}

function payrollHistoryCompareFieldMap() {
    return {
        ZWGZSE2: "positionSalary",
        JBGZSE2: "gradeSalary",
        JSDJGZ2: "technicalGradeSalary",
        JXGZ: "internSalary",
        JSFSZWTG2: "salaryIncrease",
        JHLJT: "teachingAllowance",
        FDGZ2: "floatingSalary",
        SDBT: "subsidyAllowance",
        DFBT2: "performanceAllowance",
        PGBC: "payGradeRetention",
        JXJT: "rankAllowance",
        ZWJT: "retainedReformAllowance",
        JZMCBT: "overtimeAllowance",
        TGBLBF: "retainedSpecialPostAllowance",
        NZGWSF: "hygieneAllowance",
        BLFB2: "retainedAllowance",
        JJJY2: "bonusBalance",
        GWJT2: "specialPostAllowance",
        QTBT: "otherAllowance",
        NJBT: "yearAllowance",
        HJ2: "totalAmount",
    };
}

function beforeRecordFromChangeComparison(comparison) {
    if (!comparison) {
        return null;
    }
    const period = String(comparison.previousCalculationPeriod || "").trim();
    const before = {
        calculationYear: period.slice(0, 4),
        calculationMonth: period.length >= 6 ? period.slice(4, 6) : period.slice(4),
        changeType: comparison.previousChangeType || "",
        positionName: comparison.previousPositionName || "",
        gradeSalaryLevel: comparison.previousGradeLevel || "",
        positionSalaryGrade: comparison.previousStepOrSalaryLevel || "",
    };
    const fieldMap = payrollHistoryCompareFieldMap();
    (comparison.components || []).forEach(component => {
        const fieldName = fieldMap[String(component.fieldName || "").toUpperCase()];
        if (fieldName) {
            before[fieldName] = component.beforeAmount;
        }
    });
    return before;
}

async function resolvePayrollHistoryBeforeRecordAsync(afterRecord) {
    const local = resolvePayrollHistoryBeforeRecord(afterRecord);
    if (local || !afterRecord?.id) {
        return local;
    }
    try {
        const comparison = await getJson(`/api/payroll/histories/${encodeURIComponent(afterRecord.id)}/change-comparison`);
        return beforeRecordFromChangeComparison(comparison);
    } catch (error) {
        console.warn("加载变动前对照失败", error);
        return null;
    }
}

function renderPayrollHistoryCompareField([name, label, inputType, options], afterRecord, editorContext) {
    const fieldMeta = resolvePayrollHistoryFieldMeta(name, label, editorContext);
    const afterValue = subrecordInputValue(afterRecord?.[name], inputType);
    const beforeRaw = editorContext?.beforeRecord?.[name];
    const fieldOptions = { ...(options || {}) };
    if (fieldMeta.hidden) {
        fieldOptions.hidden = true;
    }
    if (fieldOptions.hidden) {
        return `<input type="hidden" id="subrecord-field-${escapeHtml(name)}" data-subrecord-field="${escapeHtml(name)}" value="${escapeHtml(afterValue)}">`;
    }
    const beforeDisplay = formatPayrollCompareBeforeValue(beforeRaw, inputType);
    const diffText = formatPayrollCompareDiff(beforeRaw, afterRecord?.[name], inputType);
    const changed = Boolean(diffText);
    const beforeAttr = escapeHtml(beforeRaw == null ? "" : beforeRaw);
    const afterControl = inputType === "select"
        ? (() => {
            const choices = subrecordSelectChoices(fieldOptions);
            const allChoices = choices.some(choice => choice.value === afterValue) || !afterValue
                ? choices
                : [{ value: afterValue, label: afterValue, code: "" }, ...choices];
            return `
                <select id="subrecord-field-${escapeHtml(name)}" data-subrecord-field="${escapeHtml(name)}" data-compare-input="${escapeHtml(name)}" data-compare-type="text" data-compare-before="${beforeAttr}">
                    <option value="">请选择${escapeHtml(fieldMeta.label)}</option>
                    ${allChoices.map(choice => `<option value="${escapeHtml(choice.value)}" ${choice.value === afterValue ? "selected" : ""}>${escapeHtml(choice.label)}</option>`).join("")}
                </select>
            `;
        })()
        : `<input id="subrecord-field-${escapeHtml(name)}" data-subrecord-field="${escapeHtml(name)}" data-compare-input="${escapeHtml(name)}" data-compare-type="${inputType === "number" ? "number" : "text"}" data-compare-before="${beforeAttr}" type="${inputType === "number" ? "number" : "text"}" value="${escapeHtml(afterValue)}" ${inputType === "month" ? 'data-month-picker' : ""} ${fieldOptions.readonly ? "readonly" : ""}>`;
    return `
        <div class="payroll-compare-row${changed ? " is-changed" : ""}${fieldMeta.emphasis ? " is-emphasis" : ""}" data-compare-row="${escapeHtml(name)}">
            <div class="payroll-compare-label">${escapeHtml(fieldMeta.label)}</div>
            <div class="payroll-compare-before">${escapeHtml(beforeDisplay)}</div>
            <div class="payroll-compare-after">${afterControl}</div>
            <div class="payroll-compare-diff" data-compare-diff="${escapeHtml(name)}">${escapeHtml(diffText)}</div>
        </div>
    `;
}

function renderPayrollHistoryCompareForm(config, afterRecord, beforeRecord) {
    const editorContext = payrollHistoryEditorContext(afterRecord, beforeRecord);
    const beforePeriod = beforeRecord
        ? `${beforeRecord.calculationYear || ""}${beforeRecord.calculationMonth || ""} ${beforeRecord.changeType || ""}`.trim()
        : "";
    const summary = beforeRecord
        ? `对照前次：${beforePeriod || "—"}（变动前只读，变动后可编辑）`
        : "无前次工资记录，仅编辑变动后内容。";
    const formActions = `<div class="form-actions"><button type="submit">保存记录</button></div>`;
    const sectionsHtml = (config.sections || []).map(section => `
        <section class="subrecord-form-section payroll-compare-section">
            <h4 class="subrecord-form-section-title">${escapeHtml(section.title)}</h4>
            <div class="payroll-compare-head">
                <span>项目</span>
                <span>变动前</span>
                <span>变动后</span>
                <span>差额</span>
            </div>
            <div class="payroll-compare-body">
                ${(section.fields || []).map(field => renderPayrollHistoryCompareField(field, afterRecord, editorContext)).join("")}
            </div>
        </section>
    `).join("");
    return `
        <div class="payroll-compare-summary">${escapeHtml(summary)}</div>
        ${sectionsHtml}
        ${formActions}
    `;
}

function bindPayrollHistoryCompareDiffs() {
    const form = document.getElementById("subrecord-editor-form");
    if (!form) {
        return;
    }
    form.querySelectorAll("[data-compare-input]").forEach(input => {
        const refresh = () => {
            const name = input.dataset.compareInput;
            const row = form.querySelector(`[data-compare-row="${name}"]`);
            const diffHost = form.querySelector(`[data-compare-diff="${name}"]`);
            if (!row || !diffHost) {
                return;
            }
            const inputType = input.dataset.compareType === "number" ? "number" : "text";
            const beforeRaw = input.dataset.compareBefore ?? "";
            const afterRaw = inputType === "number" ? Number(input.value || 0) : input.value;
            const beforeValue = inputType === "number" ? Number(beforeRaw || 0) : beforeRaw;
            const diffText = formatPayrollCompareDiff(beforeValue, afterRaw, inputType);
            diffHost.textContent = diffText;
            row.classList.toggle("is-changed", Boolean(diffText));
        };
        input.addEventListener("input", refresh);
        input.addEventListener("change", refresh);
    });
}

function renderSubrecordEditorForm(config, record, beforeRecord = null) {
    if (config.modalClass === "payroll-history-modal") {
        return renderPayrollHistoryCompareForm(config, record || {}, beforeRecord);
    }
    const editorContext = subrecordEditorRenderContext(config, record, beforeRecord);
    const formActions = `<div class="form-actions"><button type="submit">保存记录</button></div>`;
    if (config.sections?.length) {
        return config.sections.map(section => `
            <section class="subrecord-form-section">
                <h4 class="subrecord-form-section-title">${escapeHtml(section.title)}</h4>
                <div class="subrecord-form-section-grid">
                    ${section.fields.map(field => renderSubrecordEditorField(field, record, editorContext)).join("")}
                </div>
            </section>
        `).join("") + formActions;
    }
    const visibleFields = subrecordEditorFields(config).filter(([, , , options]) => !options?.hidden);
    const hiddenFields = subrecordEditorFields(config).filter(([, , , options]) => options?.hidden);
    if (config.wideModal) {
        return `<div class="subrecord-form-section-grid position-entry-grid">
            ${visibleFields.map(field => renderSubrecordEditorField(field, record, editorContext)).join("")}
            ${hiddenFields.map(field => renderSubrecordEditorField(field, record, editorContext)).join("")}
            ${formActions}
        </div>`;
    }
    return visibleFields.map(field => renderSubrecordEditorField(field, record, editorContext)).join("")
        + hiddenFields.map(field => renderSubrecordEditorField(field, record, editorContext)).join("")
        + formActions;
}

async function openSubrecordEditor(type, record = null) {
    const person = state.activePersonnelMaintenance;
    if (!person || !person.uid) {
        alert("请先保存或选择一个人员。");
        return;
    }
    const config = subrecordEditors[type];
    let beforeRecord = null;
    let afterRecord = record;
    if (type === "payroll") {
        beforeRecord = await resolvePayrollHistoryBeforeRecordAsync(record);
        if (!record) {
            afterRecord = payrollHistoryAfterDefaults(beforeRecord);
        }
    }
    state.activeSubrecordEditor = { type, record, beforeRecord };
    document.getElementById("subrecord-editor-title").textContent = `${record ? "编辑" : "新增"}${config.title}`;
    const modalClasses = ["modal-card"];
    if (config.wideModal && config.modalClass !== "position-subrecord-modal") {
        modalClasses.push("wide-modal");
    }
    if (config.modalClass) {
        modalClasses.push(config.modalClass);
    } else if (config.wideModal) {
        modalClasses.push("position-subrecord-modal");
    }
    document.getElementById("subrecord-editor-card").className = modalClasses.join(" ");
    document.getElementById("subrecord-editor-modal").classList.remove("hidden");
    document.getElementById("subrecord-editor-status").className = "status";
    document.getElementById("subrecord-editor-status").textContent = "";
    document.getElementById("subrecord-editor-form").innerHTML = renderSubrecordEditorForm(config, afterRecord, beforeRecord);
    if (window.MonthPicker) {
        MonthPicker.enhanceAll(document.getElementById("subrecord-editor-form"));
    }
    if (type === "payroll") {
        bindPayrollHistoryCompareDiffs();
    }
    loadDictionaryFieldConfigs()
        .catch(error => console.warn("字典字段配置加载失败", error))
        .finally(() => {
            enhanceSubrecordEditorInputs(config);
            if (type === "position" && !record) {
                const activeInput = document.getElementById("subrecord-field-activeFlag");
                if (activeInput) {
                    activeInput.value = "1";
                }
            }
            if (type === "education" && !record) {
                const typeInput = document.getElementById("subrecord-field-educationType");
                if (typeInput && !typeInput.value) {
                    typeInput.value = "普通全日制";
                }
            }
        });
}

function renderSubrecordEditorField([name, label, inputType, options], record, editorContext = null) {
    const fieldMeta = resolvePayrollHistoryFieldMeta(name, label, editorContext);
    const value = subrecordInputValue(record?.[name], inputType);
    const fieldOptions = { ...(options || {}) };
    if (fieldMeta.hidden) {
        fieldOptions.hidden = true;
    }
    if (fieldOptions.hidden) {
        return `<input type="hidden" id="subrecord-field-${escapeHtml(name)}" data-subrecord-field="${escapeHtml(name)}" value="${escapeHtml(value)}">`;
    }
    const labelClass = fieldMeta.emphasis ? "subrecord-field-emphasis" : "";
    if (inputType === "select") {
        const choices = subrecordSelectChoices(fieldOptions);
        const allChoices = choices.some(choice => choice.value === value) || !value
            ? choices
            : [{ value, label: value, code: "" }, ...choices];
        return `
            <label class="${labelClass}">${escapeHtml(fieldMeta.label)}
                <select id="subrecord-field-${escapeHtml(name)}" data-subrecord-field="${escapeHtml(name)}">
                    <option value="">请选择${escapeHtml(fieldMeta.label)}</option>
                    ${allChoices.map(choice => `<option value="${escapeHtml(choice.value)}" ${choice.value === value ? "selected" : ""}>${escapeHtml(choice.label)}</option>`).join("")}
                </select>
            </label>
        `;
    }
    return `
        <label class="${labelClass}">${escapeHtml(fieldMeta.label)}
            <input id="subrecord-field-${escapeHtml(name)}" data-subrecord-field="${escapeHtml(name)}" type="${inputType === "number" ? "number" : "text"}" value="${escapeHtml(value)}" ${inputType === "month" ? "data-month-picker" : ""} ${fieldOptions.readonly ? "readonly" : ""}>
        </label>
    `;
}

function formatPromotionFlag(value) {
    const normalized = String(value ?? "").trim();
    return normalized === "1" ? "已处理" : "未处理";
}

function formatActiveFlag(value) {
    const normalized = String(value ?? "").trim();
    return normalized === "1" ? "现任" : "非现任";
}

function positionMaintenancePayload(row, overrides = {}) {
    return {
        currentPositionCode: row.currentPositionCode || "",
        currentPosition: row.currentPosition || "",
        positionLevel: row.positionLevel || "",
        rankCode: row.rankCode || "",
        positionCode: row.positionCode || "",
        positionName: row.positionName || "",
        startYearMonth: row.startYearMonth || "",
        intervalYears: row.intervalYears ?? 0,
        activeFlag: row.activeFlag || "",
        promotionFlag: row.promotionFlag || "",
        ...overrides,
    };
}

function renderMaintPositionRows(positions, readonly = false) {
    return positions.length ? positions.map(row => {
        const isCurrent = String(row.activeFlag ?? "").trim() === "1";
        const codeHint = [row.currentPositionCode, row.positionCode].filter(Boolean).join(" / ");
        const actions = readonly
            ? "—"
            : `${isCurrent ? "" : `<button class="row-action" type="button" data-set-current-position="${row.id}">设为现任</button>`}
                    <button class="row-action" type="button" data-edit-position="${row.id}">编辑</button>
                    <button class="row-action danger-button" type="button" data-delete-position="${row.id}">删除</button>`;
        return `
            <tr class="${isCurrent ? "highlight-row" : ""}">
                <td class="col-period">${escapeHtml(row.startYearMonth || "")}${row.appCreated ? " <span class='new-badge'>新</span>" : ""}</td>
                <td class="col-position" title="${escapeHtml(row.currentPositionCode || "")}">${escapeHtml(row.currentPosition || "")}</td>
                <td class="col-level">${escapeHtml(row.positionLevel || "")}</td>
                <td class="col-position" title="${escapeHtml(row.positionCode || "")}">${escapeHtml(row.positionName || "")}</td>
                <td class="col-years">${escapeHtml(row.intervalYears ?? "")}</td>
                <td class="col-flag"><span class="assessment-batch-status ${isCurrent ? "" : "status-missing"}">${formatActiveFlag(row.activeFlag)}</span></td>
                <td class="col-code" title="${escapeHtml(codeHint)}">${escapeHtml(codeHint || "-")}</td>
                <td class="col-action">${actions}</td>
            </tr>
        `;
    }).join("") : "<tr><td colspan='8'>暂无任职记录</td></tr>";
}

const educationTypeOptions = [
    "普通全日制",
    "成人教育",
    "自考",
    "后取",
    "其它",
];

function subrecordSelectChoices(options) {
    if (options?.optionsProvider === "assessmentResults") {
        const results = usesInstitutionAssessmentResults(state.activePersonnelMaintenance)
            ? assessmentResultOptions.institution
            : assessmentResultOptions.administrative;
        return results.map(result => ({ value: result, label: result }));
    }
    if (options?.optionsProvider === "promotionFlags") {
        return [
            { value: "", label: "未处理" },
            { value: "1", label: "已处理" },
        ];
    }
    if (options?.optionsProvider === "activeFlags") {
        return [
            { value: "1", label: "现任" },
            { value: "0", label: "非现任" },
        ];
    }
    if (options?.optionsProvider === "educationTypes") {
        return educationTypeOptions.map(result => ({ value: result, label: result }));
    }
    return [];
}

function isInstitutionPersonnel(person) {
    const text = `${person?.personnelCategory || ""} ${person?.organizationType || ""}`;
    return text.includes("事业");
}

/** 年度考核：仅公务员用「称职」等，其余人员用「合格」等。 */
function isCivilServantPersonnelCategory(personOrRow) {
    return String(personOrRow?.personnelCategory || "").includes("公务员");
}

function usesInstitutionAssessmentResults(personOrRow) {
    return !isCivilServantPersonnelCategory(personOrRow);
}

function enhanceSubrecordEditorInputs(config) {
    subrecordEditorFields(config).forEach(([name, label, , options]) => {
        if (options?.hidden) {
            return;
        }
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
            dictionaryFieldKey: options?.dictionaryPrefixField || name,
            linkedCodeInputId: options?.linkedCodeField ? `subrecord-field-${options.linkedCodeField}` : null,
            linkedCodeField: options?.linkedCodeField || null,
            useFullDictionaryCode: options?.useFullDictionaryCode || false,
            codeTarget: options?.codeTarget || false,
            codeMaxLength: options?.codeMaxLength || null,
        }));
        combo.appendChild(button);
    });
}

function subrecordDictionaryPrefix(options) {
    if (!options) {
        return null;
    }
    if (options.dictionaryPrefixField) {
        const fieldKey = String(options.dictionaryPrefixField).toLowerCase();
        const configured = state.dictionaryFieldConfigs?.[fieldKey];
        if (configured?.dictionaryPrefix) {
            return configured.dictionaryPrefix;
        }
        if (positionDictionaryFallbacks[fieldKey]) {
            return positionDictionaryFallbacks[fieldKey];
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
    subrecordEditorFields(config).forEach(([name, , inputType]) => {
        const input = document.querySelector(`[data-subrecord-field="${name}"]`);
        payload[name] = inputType === "number"
            ? Number(input.value || 0)
            : inputType === "month"
                ? (() => {
                    const normalized = window.MonthPicker
                        ? MonthPicker.normalizeToYm(input.value)
                        : String(input.value || "").replace(".", "-").slice(0, 7);
                    return normalized ? normalized.replace("-", ".") : String(input.value || "").trim().replace("-", ".");
                })()
                : input.value.trim();
    });
    if (editor.type === "position") {
        payload.rankCode = editor.record?.rankCode || "";
        payload.promotionFlag = editor.record?.promotionFlag || "";
    }
    status.textContent = "正在保存记录...";
    try {
        const url = editor.record
            ? (config.updateEndpoint ? config.updateEndpoint(editor.record.id) : `${config.endpoint(person.uid)}/${editor.record.id}`)
            : config.endpoint(person.uid);
        const rows = editor.record ? await putJson(url, payload) : await postJson(url, payload);
        status.textContent = "保存成功";
        showAppToast("保存成功");
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
    const el = document.getElementById(inputId);
    if (!el) {
        return "";
    }
    const raw = el.value.trim();
    if (!raw) {
        return "";
    }
    const normalized = window.MonthPicker
        ? MonthPicker.normalizeToYm(raw)
        : raw.replace(".", "-").slice(0, 7);
    return normalized ? normalized.replace("-", ".") : raw.replace("-", ".");
}

function setMonthInputValue(inputId, value) {
    const input = document.getElementById(inputId);
    if (!input) {
        return;
    }
    const raw = String(value || "").trim();
    const normalized = window.MonthPicker
        ? MonthPicker.normalizeToYm(raw)
        : (raw ? raw.replace(".", "-").slice(0, 7) : "");
    input.value = normalized || (raw ? raw.replace(".", "-").slice(0, 7) : "");
    if (window.MonthPicker && (input.dataset.monthField === "true" || input.hasAttribute("data-month-picker") || input.type === "month")) {
        MonthPicker.enhance(input);
        MonthPicker.syncToggleState(input);
    }
}

async function loadPersonnelSubrecords(uid, organizationCode, personCode) {
    const [education, positions, assessments, payrollHistory, relatedRecords, projection] = await Promise.all([
        getJson(`/api/personnel/${uid}/education`),
        getJson(`/api/personnel/${uid}/positions`),
        getJson(`/api/personnel/${uid}/assessments`),
        getJson(`/api/payroll/histories?organizationCode=${encodeURIComponent(organizationCode)}&keyword=${encodeURIComponent(personCode)}&size=50`),
        getJson(`/api/personnel/${uid}/related-records`),
        getJson(`/api/payroll/personnel/${uid}/calculation-preview`),
    ]);
    renderPersonnelProjection(projection);
    renderWageProjection(await getJson(`/api/payroll/personnel/${uid}/wage-projection`));
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
    document.getElementById("maint-position-rows").innerHTML = renderMaintPositionRows(positions);
    document.getElementById("maint-assessment-rows").innerHTML = assessments.length ? assessments.map(row => `
        <tr class="${row.appCreated ? "highlight-row" : ""}">
            <td class="col-year">${escapeHtml(row.year)}${row.appCreated ? " <span class='new-badge'>新</span>" : ""}</td>
            <td class="col-result"><span class="${assessmentResultTagClass(row.result)}">${escapeHtml(row.result || "—")}</span></td>
            <td class="col-action">
                <button class="row-action" type="button" data-edit-assessment="${row.id}">编辑</button>
                <button class="row-action danger-button" type="button" data-delete-assessment="${row.id}">删除</button>
            </td>
        </tr>
    `).join("") : "<tr><td colspan='3'>暂无考核记录</td></tr>";
    const histories = payrollHistory.content || [];
    state.maintPayrollHistories = histories;
    document.getElementById("maint-payroll-rows").innerHTML = histories.length ? histories.map(row => `
        <tr>
            <td>${escapeHtml(row.calculationYear)}${escapeHtml(row.calculationMonth)} ${row.appCreated ? "<span class='new-badge'>新</span>" : ""}</td>
            <td>${escapeHtml(row.changeType)}</td>
            <td>${escapeHtml(row.positionName)}</td>
            <td>${escapeHtml(row.gradeSalaryLevel || "")}</td>
            <td>${escapeHtml(row.positionSalaryGrade || "")}</td>
            <td>${escapeHtml(row.levelAssessmentStartYear || "")}</td>
            <td>${escapeHtml(row.stepAssessmentStartYear || "")}</td>
            <td>${money(row.positionSalary)}</td>
            <td>${money(row.gradeSalary)}</td>
            <td>${money(row.totalAmount)}</td>
            <td>${row.currentPayroll ? "是" : "否"} <button class="row-action" type="button" data-edit-payroll="${row.id}">编辑</button> <button class="row-action danger-button" type="button" data-delete-payroll="${row.id}">删除</button></td>
        </tr>
    `).join("") : "<tr><td colspan='11'>暂无调资记录</td></tr>";
    renderPersonnelRelatedRecords(relatedRecords || {});
    bindSubrecordActions("education", education);
    bindSubrecordActions("position", positions);
    bindSubrecordActions("assessment", assessments);
    bindSubrecordActions("payroll", histories);
}

async function autoFillMissingAssessments() {
    const person = state.activePersonnelMaintenance;
    if (!person || !person.uid) {
        alert("请先选择人员。");
        return;
    }
    const preview = await loadMissingAssessmentPreview(person.uid);
    const years = preview.years || [];
    if (!years.length) {
        alert("未检测到缺失年度考核。");
        return;
    }
    const defaultResult = preview.defaultResult || defaultAssessmentResultForCurrentPerson();
    const result = prompt(`将补录 ${years.join("、")} 年度考核，默认结果：${defaultResult}。可在下方编辑。\\n如需改默认结果，请输入：`, defaultResult);
    if (result === null) {
        return;
    }
    const status = document.getElementById("personnel-status");
    status.className = "status";
    status.textContent = `正在补录 ${years.length} 条年度考核...`;
    try {
        const normalizedResult = result.trim() || defaultResult;
        for (const year of years) {
            await postJson(`/api/personnel/${person.uid}/assessments`, { year, result: normalizedResult });
        }
        await loadPersonnelSubrecords(person.uid, person.organizationCode, person.personCode);
        status.textContent = `已补录 ${years.length} 条年度考核，补录记录已高亮显示。`;
    } catch (error) {
        showError(status, error);
    }
}

function defaultAssessmentResultForCurrentPerson() {
    const category = document.getElementById("maint-personnel-category").value || "";
    const organizationType = document.getElementById("maint-organization-type").value || "";
    return usesInstitutionAssessmentResults({ personnelCategory: category, organizationType })
        ? "合格"
        : "称职";
}

function assessmentResultTagClass(result) {
    const text = String(result || "").trim();
    if (!text || text === "—" || text === "合格" || text === "称职") {
        return "assessment-result-tag is-normal";
    }
    if (text === "优秀") {
        return "assessment-result-tag is-excellent";
    }
    if (text === "不合格" || text === "不称职") {
        return "assessment-result-tag is-danger";
    }
    if (text.startsWith("基本")) {
        return "assessment-result-tag is-warning";
    }
    return "assessment-result-tag is-special";
}

async function loadMissingAssessmentPreview(uid) {
    const period = document.getElementById("maint-wage-projection-period").value.trim();
    const params = new URLSearchParams();
    if (period) {
        params.set("targetPeriod", period.replace("-", ""));
    }
    const suffix = params.toString() ? `?${params}` : "";
    return getJson(`/api/personnel/${uid}/assessments/missing${suffix}`);
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
        if (type === "position") {
            const setCurrent = document.querySelector(`[data-set-current-position="${row.id}"]`);
            if (setCurrent) {
                setCurrent.addEventListener("click", () => setPositionAsCurrent(row));
            }
        }
    });
}

async function setPositionAsCurrent(row) {
    const person = state.activePersonnelMaintenance;
    if (!person?.uid) {
        return;
    }
    const status = document.getElementById("personnel-status");
    status.className = "status";
    status.textContent = "正在设为现任...";
    try {
        await putJson(`/api/personnel/${person.uid}/positions/${row.id}`, positionMaintenancePayload(row, { activeFlag: "1" }));
        status.textContent = "已设为现任职务。";
        await loadPersonnelSubrecords(person.uid, person.organizationCode, person.personCode);
    } catch (error) {
        showError(status, error);
    }
}

function renderPersonnelProjection(preview) {
    document.getElementById("maint-projection-period").textContent = preview.calculationPeriod || "-";
    document.getElementById("maint-projection-total").textContent = money(preview.recalculatedKnownTotal);
    document.getElementById("maint-projection-stored-total").textContent = money(preview.storedTotal);
    const difference = document.getElementById("maint-projection-difference");
    difference.textContent = money(preview.totalDifference);
    difference.className = Number(preview.totalDifference) === 0 ? "difference-ok" : "difference-bad";
}

async function loadPersonnelWageProjection(uid) {
    const period = document.getElementById("maint-wage-projection-period").value.trim();
    const params = new URLSearchParams();
    if (period) {
        params.set("period", period);
    }
    const suffix = params.toString() ? `?${params}` : "";
    const [wageProjection, calculationPreview] = await Promise.all([
        getJson(`/api/payroll/personnel/${uid}/wage-projection${suffix}`),
        getJson(`/api/payroll/personnel/${uid}/calculation-preview${suffix}`),
    ]);
    renderWageProjection(wageProjection);
    renderPersonnelProjection(calculationPreview);
}

function renderWageProjection(projection) {
    document.getElementById("maint-wage-projection-period").value = projection.targetPeriod
        ? `${projection.targetPeriod.slice(0, 4)}-${projection.targetPeriod.slice(4, 6)}`
        : "";
    const steps = projection.stepDetails || [];
    const projectedTotal = steps.length ? steps[steps.length - 1].total : null;
    const levelText = projection.levelStepDisplay
        || gradeStepText(projection.level, projection.stepOrSalaryLevel)
        || "-";
    const overview = document.getElementById("maint-wage-projection-overview");
    if (overview) {
        overview.innerHTML = `
            <div class="projection-stat"><span>目标年月</span><strong>${escapeHtml(formatProjectionPeriod(projection.targetPeriod))}</strong></div>
            <div class="projection-stat"><span>推算起点</span><strong>${escapeHtml(formatProjectionPeriod(projection.basePeriod))}</strong></div>
            <div class="projection-stat accent"><span>岗位 / 级别</span><strong>${escapeHtml(positionDisplay(projection.positionCode, projection.positionName))}<br>${escapeHtml(levelText)} · ${escapeHtml(baseSalarySourceName(projection.baseSalarySource))}</strong></div>
            <div class="projection-stat"><span>分步合计</span><strong>${projectedTotal == null ? "-" : money(projectedTotal)}</strong></div>
        `;
    }
    const lines = projection.explanationLines || [];
    const metaParts = [
        projection.regularizationYearMonth ? `转正 ${escapeHtml(projection.regularizationYearMonth)}` : "",
        projection.levelAssessmentStartYear ? `xckhndjb ${escapeHtml(projection.levelAssessmentStartYear)}` : "",
        projection.stepAssessmentStartYear ? `xckhndzw ${escapeHtml(projection.stepAssessmentStartYear)}` : "",
    ].filter(Boolean);
    document.getElementById("maint-wage-projection-result").innerHTML = lines.length ? `
        ${metaParts.length ? `<div class="projection-meta-line">${metaParts.join(" · ")}</div>` : ""}
        <details class="projection-timeline"${lines.length <= 6 ? " open" : ""}>
            <summary>推算说明（${lines.length} 条）</summary>
            <ol>${lines.map(line => `<li>${escapeHtml(line)}</li>`).join("")}</ol>
        </details>
    ` : `<div class="projection-empty-note">暂无可展示的推算说明。</div>`;
    renderWageProjectionSteps(steps, document.getElementById("maint-wage-projection-steps"), steps.length ? {} : {
        reason: (projection.explanationLines || []).length
            ? "逐年推算不合格或未能生成分步明细："
            : "暂无分步明细。",
        explanationLines: projection.explanationLines || [],
    });
}

function renderWageProjectionSteps(steps, container = document.getElementById("maint-wage-projection-steps"), options = {}) {
    if (!container) {
        return;
    }
    const list = Array.isArray(steps) ? steps : [];
    if (!list.length) {
        const reason = String(options.reason || "").trim();
        const lines = (options.explanationLines || [])
            .map(line => String(line || "").trim())
            .filter(Boolean);
        if (!reason && !lines.length) {
            container.innerHTML = `<div class="projection-empty-note">暂无分步明细。</div>`;
            return;
        }
        const title = reason || "暂无分步明细。";
        container.innerHTML = `
            <div class="projection-empty-note projection-empty-reason">
                <p class="projection-empty-title">${escapeHtml(title)}</p>
                ${lines.length ? `<ul class="projection-empty-lines">${lines.map(line => `<li>${escapeHtml(line)}</li>`).join("")}</ul>` : ""}
            </div>
        `;
        return;
    }
    container.innerHTML = list.map((step, index) => {
        const period = formatProjectionPeriod(step.period);
        const componentRows = (step.components || []).map(component => `
            <tr>
                <td>${escapeHtml(component.fieldName)}</td>
                <td>${escapeHtml(component.caption)}</td>
                <td>${money(component.amount)}</td>
            </tr>
        `).join("");
        const category = String(step.changeCategory || "").trim();
        const positionText = positionDisplay(step.positionCode, step.positionName);
        const hideLevel = ["SALARY_LEVEL", "WORKER_GRADE"].includes(String(step.baseSalarySource || ""));
        const levelText = String(step.level || "").trim() || "-";
        const stepText = String(step.step || "").trim() || "-";
        const stepLabel = salaryStepCaption(step.positionCode);
        return `
            <details class="projection-step-card"${index === steps.length - 1 ? " open" : ""}>
                <summary>
                    <span class="projection-step-index">第 ${index + 1} 步</span>
                    <span class="projection-step-period">${escapeHtml(period)}</span>
                    ${category ? `<span class="projection-step-category">${escapeHtml(category)}</span>` : ""}
                    <span class="projection-step-position" title="变动后职务">${escapeHtml(positionText)}</span>
                    ${hideLevel ? "" : `<span class="projection-step-level">级别 ${escapeHtml(levelText)}</span>`}
                    <span class="projection-step-level">${stepLabel} ${escapeHtml(stepText)}</span>
                    <span class="projection-step-total">合计 ${money(step.total)}</span>
                </summary>
                <div class="projection-step-body">
                    <p class="projection-step-description">${escapeHtml(step.description || "")}</p>
                    <p class="projection-step-meta">
                        <strong>岗位：</strong>${escapeHtml(positionDisplay(step.positionCode, step.positionName))}
                        <strong>工资标准：</strong>${escapeHtml(step.salaryStandardYearMonth || "-")}
                        <strong>津补贴标准：</strong>${escapeHtml(step.allowanceStandardYearMonth || "-")}
                    </p>
                    <div class="table-wrap">
                        <table>
                            <thead><tr><th>字段</th><th>项目</th><th>金额</th></tr></thead>
                            <tbody>${componentRows || "<tr><td colspan='3'>暂无工资项</td></tr>"}</tbody>
                        </table>
                    </div>
                </div>
            </details>
        `;
    }).join("");
}

function formatProjectionPeriod(period) {
    const normalized = String(period || "").trim();
    if (normalized.length === 6) {
        return `${normalized.slice(0, 4)}-${normalized.slice(4, 6)}`;
    }
    if (normalized.length === 4) {
        return `${normalized}年`;
    }
    return normalized || "-";
}

function positionDisplay(code, name) {
    const normalizedCode = String(code || "").trim();
    const normalizedName = String(name || "").trim();
    if (normalizedCode && normalizedName) {
        return `${normalizedCode} ${normalizedName}`;
    }
    return normalizedName || normalizedCode || "-";
}

function regularizationProjectionLine(projection) {
    const regularization = String(projection.regularizationYearMonth || "").replace(".", "");
    if (regularization && regularization >= "200607") {
        return `<strong>转正时间：</strong>${escapeHtml(projection.regularizationYearMonth)}<br>`;
    }
    return "";
}

function renderPersonnelRelatedRecords(records) {
    renderCurrentPayrollDetail(records.currentPayroll || {});
    renderPersonnelTransferRows(records.transfers || []);

    document.getElementById("maint-award-rows").innerHTML = tableRows(records.awards, row => `
        <tr><td>${escapeHtml(textField(row, "hjmc"))}</td><td>${escapeHtml(textField(row, "sjdw"))}</td><td>${escapeHtml(textField(row, "jllx"))}</td><td>${escapeHtml(textField(row, "hjsj"))}</td><td>${escapeHtml(textField(row, "tqyjjssj"))}</td><td>${escapeHtml(textField(row, "jljb"))}</td><td>${escapeHtml(textField(row, "jldc"))}</td><td>${escapeHtml(textField(row, "qtqk"))}</td></tr>
    `, 8, "暂无获奖记录");
    document.getElementById("maint-rank-rows").innerHTML = tableRows(records.rankRecords, row => `
        <tr><td>${escapeHtml(rankRecordType(row))}</td><td>${escapeHtml(textField(row, "jx"))}</td><td>${escapeHtml(textField(row, "sysj"))}</td><td>${escapeHtml(textField(row, "syyy"))}</td><td>${escapeHtml(textField(row, "rmwh"))}</td><td>${truthyField(row, "xrjxbz") ? "是" : "否"}</td><td>${escapeHtml(textField(row, "lb"))}</td></tr>
    `, 7, "暂无警衔/等级记录");
    renderWageReformDetail(records.wageReform);
    renderPreReformSalaryDetail(records.preReformSalary);
}

function renderPersonnelTransferRows(transfers) {
    const host = document.getElementById("maint-transfer-rows");
    if (!host) {
        return;
    }
    const rows = Array.isArray(transfers) ? transfers : [];
    host.innerHTML = rows.length ? rows.map(row => {
        const sourceOrg = [row.sourceOrganizationName, row.sourceOrganizationCode].filter(Boolean).join(" / ");
        const targetOrg = [row.targetOrganizationName, row.targetOrganizationCode].filter(Boolean).join(" / ");
        return `
        <tr>
            <td>${escapeHtml(row.transferPeriod || "")}</td>
            <td>${escapeHtml(sourceOrg || "—")}</td>
            <td>${escapeHtml(row.sourcePersonCode || "")}</td>
            <td>${escapeHtml(targetOrg || "—")}</td>
            <td>${escapeHtml(row.targetPersonCode || "")}</td>
            <td>${escapeHtml(row.changeType || "调动")}</td>
            <td>${escapeHtml(row.remark || "")}</td>
            <td>${escapeHtml(row.createdAt || "")}</td>
        </tr>`;
    }).join("") : "<tr><td colspan='8'>暂无调动履历</td></tr>";
}

let currentPayrollFieldConfigPromise = null;

async function loadCurrentPayrollFieldConfig() {
    if (!currentPayrollFieldConfigPromise) {
        currentPayrollFieldConfigPromise = getJson("/api/payroll/fields?enabledIn2006Policy=true&size=200")
            .then(result => (result.content || []).map(field => ({
                fieldName: String(field.fieldName || "").trim(),
                caption: String(field.caption || field.fieldName || "").trim(),
                fieldType: String(field.fieldType || "").trim().toUpperCase(),
            })).filter(field => field.fieldName))
            .catch(error => {
                currentPayrollFieldConfigPromise = null;
                throw error;
            });
    }
    return currentPayrollFieldConfigPromise;
}

function renderCurrentPayrollDetail(current) {
    const host = document.getElementById("maint-current-payroll-view");
    if (!host) {
        return;
    }
    if (!current || !Object.keys(current).length) {
        host.innerHTML = `<div class="note-card note-card-muted">暂无当前工资记录</div>`;
        return;
    }
    host.innerHTML = `<div class="note-card note-card-muted">正在按旧系统工资项目配置加载...</div>`;
    loadCurrentPayrollFieldConfig()
        .then(fields => {
            host.innerHTML = buildCurrentPayrollSheet(current, fields);
        })
        .catch(error => {
            console.warn("当前工资字段配置加载失败", error);
            host.innerHTML = buildCurrentPayrollSheet(current, []);
        });
}

function buildCurrentPayrollSheet(current, fields) {
    const configured = (fields && fields.length) ? fields : currentPayrollFallbackFields();
    const metaFields = configured.filter(field => field.fieldType !== "N" && !isSalaryTotalFieldName(field.fieldName));
    const amountFields = configured.filter(field => field.fieldType === "N" && !isSalaryTotalFieldName(field.fieldName));
    const totalField = configured.find(field => isSalaryTotalFieldName(field.fieldName))
        || { fieldName: "HJ2", caption: "月工资合计", fieldType: "N" };
    const period = `${textField(current, "jsnf") || ""}${textField(current, "jsyf") || ""}`.trim() || "—";
    return `
        <div class="wage-reform-sheet">
            <h4 class="personnel-form-section-title">基本情况</h4>
            <div class="personnel-form-meta-grid">
                <label><span>结算年月</span><strong>${escapeHtml(period)}</strong></label>
                <label><span>变动类别</span><strong>${wageReformDisplay(textField(current, "jslb"))}</strong></label>
            </div>
            <h4 class="personnel-form-section-title">执行信息</h4>
            <div class="personnel-form-meta-grid">
                ${metaFields.map(field => `
                    <label>
                        <span>${escapeHtml(field.caption)}</span>
                        <strong>${escapeHtml(currentPayrollFieldDisplay(current, field))}</strong>
                    </label>
                `).join("")}
            </div>
            <h4 class="personnel-form-section-title">工资项目</h4>
            <div class="personnel-form-items-grid">
                ${amountFields.map(field => `
                    <label>
                        <span>${escapeHtml(field.caption)}</span>
                        <strong>${money(numberField(current, field.fieldName))}</strong>
                    </label>
                `).join("")}
            </div>
            <div class="personnel-form-total">
                <span>${escapeHtml(totalField.caption || "月工资合计")}</span>
                <strong>${money(numberField(current, totalField.fieldName || "hj2"))}</strong>
            </div>
        </div>
    `;
}

function isSalaryTotalFieldName(fieldName) {
    const name = String(fieldName || "").trim().toUpperCase();
    return name === "HJ2" || name === "HJ";
}

function currentPayrollFallbackFields() {
    return [
        { fieldName: "ZWGW2", caption: "执行工资职务层次", fieldType: "C" },
        { fieldName: "JBGZJB2", caption: "级别", fieldType: "C" },
        { fieldName: "ZWGZDC2", caption: "档次", fieldType: "C" },
        { fieldName: "TBND", caption: "执行工资标准", fieldType: "C" },
        { fieldName: "JBTBZ", caption: "执行津补贴标准", fieldType: "C" },
        { fieldName: "ZWGZSE2", caption: "职务(岗位)工资", fieldType: "N" },
        { fieldName: "JBGZSE2", caption: "级别工资", fieldType: "N" },
        { fieldName: "JSDJGZ2", caption: "技术等级工资", fieldType: "N" },
        { fieldName: "DFBT2", caption: "生活性补贴", fieldType: "N" },
        { fieldName: "SDBT", caption: "工作性津贴", fieldType: "N" },
        { fieldName: "BLFB2", caption: "保留副补", fieldType: "N" },
        { fieldName: "JXJT", caption: "警衔、法检、监察津贴", fieldType: "N" },
        { fieldName: "NJBT", caption: "农村学校教师补贴", fieldType: "N" },
        { fieldName: "HJ2", caption: "月工资合计", fieldType: "N" },
    ];
}

function currentPayrollFieldDisplay(current, field) {
    const name = String(field.fieldName || "").trim().toUpperCase();
    if (name === "ZWGW2") {
        const code = String(textField(current, "zwbm2") || "").trim();
        const label = String(textField(current, "zwgw2") || "").trim();
        if (code && label) {
            return `${code} ${label}`;
        }
        return label || code || "—";
    }
    const value = String(textField(current, field.fieldName) || "").trim();
    return value || "—";
}

function tableRows(rows, render, colspan, emptyText) {
    return (rows || []).length ? rows.map(render).join("") : `<tr><td colspan="${colspan}">${emptyText}</td></tr>`;
}

function wageReformDisplay(value) {
    const text = value == null ? "" : String(value).trim();
    return text === "" ? "—" : escapeHtml(text);
}

function wageReformField(row, fieldName) {
    return wageReformDisplay(textField(row, fieldName));
}

function renderWageReformDetail(rows) {
    const host = document.getElementById("maint-wage-reform-view");
    if (!host) {
        return;
    }
    const records = rows || [];
    if (!records.length) {
        host.innerHTML = `<div class="note-card note-card-muted">暂无套改记录</div>`;
        return;
    }
    host.innerHTML = records.map((row, index) => `
        <div class="wage-reform-sheet${index > 0 ? " wage-reform-sheet-follow" : ""}">
            ${records.length > 1 ? `<div class="wage-reform-sheet-index">套改记录 ${index + 1} / ${records.length}</div>` : ""}
            <div class="wage-reform-grid wage-reform-grid-top">
                <label><span>参加工作年月</span><strong>${wageReformField(row, "cjgzny")}</strong></label>
                <label><span>学制</span><strong>${wageReformField(row, "xlnx")}</strong></label>
            </div>
            <div class="wage-reform-grid">
                <div class="wage-reform-col">
                    <label><span>中断工作年限</span><strong>${wageReformField(row, "zdgznx")}</strong></label>
                    <label><span>扣减套改年限</span><strong>${wageReformField(row, "kjnx")}</strong></label>
                    <label><span>套改年限</span><strong>${wageReformField(row, "tgnx")}</strong></label>
                    <label><span>套改时学历</span><strong>${wageReformField(row, "xl")}</strong></label>
                    <label><span>套改时职务</span><strong>${wageReformField(row, "zwmc")}</strong></label>
                    <label><span>任职时间</span><strong>${wageReformField(row, "rzsj")}</strong></label>
                    <label><span>任职年限</span><strong>${wageReformField(row, "rznx")}</strong></label>
                    <label><span>扣减年限</span><strong>${wageReformField(row, "zwkjnx")}</strong></label>
                </div>
                <div class="wage-reform-col">
                    <label><span>原任低一级职务</span><strong>${wageReformField(row, "zwmc1")}</strong></label>
                    <label><span>任职时间</span><strong>${wageReformField(row, "rzsj1")}</strong></label>
                    <label><span>任职年限</span><strong>${wageReformField(row, "rznx1")}</strong></label>
                    <label><span>扣减年限</span><strong>${wageReformField(row, "zwkjnx1")}</strong></label>
                    <label><span>套改职务</span><strong>${wageReformField(row, "tgzw")}</strong></label>
                    <label><span>套改级别</span><strong>${wageReformField(row, "tgjb")}</strong></label>
                    <label class="wage-reform-remark"><span>套改说明</span><strong>${wageReformField(row, "remark")}</strong></label>
                </div>
                <div class="wage-reform-col wage-reform-col-grade">
                    <label><span>套改档次</span><strong>${wageReformField(row, "tgdc")}</strong></label>
                    <label><span>高定档次</span><strong>${wageReformField(row, "gddc")}</strong></label>
                    <label><span>低定档次</span><strong>${wageReformField(row, "dddc")}</strong></label>
                    <label><span>高定级别</span><strong>${wageReformField(row, "gdjb")}</strong></label>
                    <label><span>低定级别</span><strong>${wageReformField(row, "ddjb")}</strong></label>
                </div>
            </div>
        </div>
    `).join("");
}

const PRE_REFORM_META_FIELDS = [
    { label: "结算年月", fields: ["jsnf", "jsyf"], join: "" },
    { label: "变动类别", field: "jslb" },
    { label: "套改标准年月", field: "tbnd" },
    { label: "工资年限", field: "gznx" },
    { label: "套改比例", field: "tgbl" },
    { label: "津贴比例", field: "jtbl" },
    { label: "浮动档次", field: "fddc" },
    { label: "津补贴标准年月", field: "jbtbz" },
    { label: "警衔津贴标准", field: "jxjtbz" },
];

const PRE_REFORM_POSITION_FIELDS = [
    [
        { label: "岗位编码", field: "zwbm2" },
        { label: "岗位名称", field: "zwgw2" },
        { label: "职务工资档次", field: "zwgzdc2" },
        { label: "级别", field: "jbgzjb2" },
        { label: "档次", field: "djc2" },
    ],
    [
        { label: "现任职务", field: "xrzw" },
        { label: "职务级别", field: "zwjb" },
        { label: "任职年月", field: "srny" },
        { label: "警衔", field: "jx" },
        { label: "职级编码", field: "zjbm" },
    ],
    [
        { label: "人员类别", field: "ryfl" },
        { label: "岗位分类", field: "gwfl" },
        { label: "最高学历", field: "zgxl" },
        { label: "学历类别", field: "xllb" },
        { label: "参加工作年月", field: "cjgzny" },
    ],
];

const PRE_REFORM_SALARY_ITEMS = [
    { field: "zwgzse2", label: "职务工资" },
    { field: "jbgzse2", label: "级别/薪级工资" },
    { field: "jcgz2", label: "基础工资" },
    { field: "glgz2", label: "工龄工资" },
    { field: "jsdjgz2", label: "技术等级工资" },
    { field: "grjj2", label: "岗位津贴" },
    { field: "dfbt2", label: "绩效/生活补贴" },
    { field: "sdbt", label: "工作性/生活性补贴" },
    { field: "blfb2", label: "保留福补" },
    { field: "jxjt", label: "警衔/检察/审判/监察津贴" },
    { field: "jhljt", label: "教护龄津贴" },
    { field: "jsfszwtg2", label: "教护提高部分" },
    { field: "jt2", label: "津贴" },
    { field: "fdgz2", label: "浮动工资" },
    { field: "jjjy2", label: "保留奖金" },
    { field: "gwjt2", label: "岗位津贴(事业)" },
    { field: "jxgz", label: "见习工资" },
    { field: "zzbc", label: "转正补差" },
    { field: "zwjt", label: "职务津贴" },
    { field: "zfbt", label: "住房补贴" },
    { field: "dsznf", label: "独生子女费" },
    { field: "nzgwsf", label: "女职工卫生费" },
    { field: "jzmcbt", label: "驻地津贴" },
    { field: "qtbt", label: "其他补贴" },
    { field: "pgbc", label: "工改保留职务工资" },
];

function personnelFormField(row, fieldName, options = {}) {
    if (options.money) {
        const amount = numberField(row, fieldName);
        return amount ? money(amount) : wageReformDisplay("");
    }
    return wageReformField(row, fieldName);
}

function personnelFormCombinedField(row, spec) {
    if (spec.field) {
        return personnelFormField(row, spec.field, spec);
    }
    const parts = (spec.fields || []).map(name => textField(row, name)).map(value => String(value || "").trim()).filter(Boolean);
    return wageReformDisplay(parts.join(spec.join ?? ""));
}

function renderPersonnelFormFields(columnSpecs, row) {
    return columnSpecs.map(column => `
        <div class="wage-reform-col">
            ${column.map(spec => `
                <label>
                    <span>${escapeHtml(spec.label)}</span>
                    <strong>${personnelFormCombinedField(row, spec)}</strong>
                </label>
            `).join("")}
        </div>
    `).join("");
}

function renderPersonnelFormItems(items, row) {
    return items.map(item => `
        <label>
            <span>${escapeHtml(item.label)}</span>
            <strong>${personnelFormField(row, item.field, item)}</strong>
        </label>
    `).join("");
}

function renderPreReformSalaryDetail(rows) {
    const host = document.getElementById("maint-pre-reform-view");
    if (!host) {
        return;
    }
    const records = rows || [];
    if (!records.length) {
        host.innerHTML = `<div class="note-card note-card-muted">暂无套改前工资记录</div>`;
        return;
    }
    host.innerHTML = records.map((row, index) => `
        <div class="wage-reform-sheet${index > 0 ? " wage-reform-sheet-follow" : ""}">
            ${records.length > 1 ? `<div class="wage-reform-sheet-index">套改前工资 ${index + 1} / ${records.length}</div>` : ""}
            <h4 class="personnel-form-section-title">基本情况</h4>
            <div class="personnel-form-meta-grid">
                ${PRE_REFORM_META_FIELDS.map(spec => `
                    <label>
                        <span>${escapeHtml(spec.label)}</span>
                        <strong>${personnelFormCombinedField(row, spec)}</strong>
                    </label>
                `).join("")}
            </div>
            <h4 class="personnel-form-section-title">岗位与级别</h4>
            <div class="wage-reform-grid">
                ${renderPersonnelFormFields(PRE_REFORM_POSITION_FIELDS, row)}
            </div>
            <h4 class="personnel-form-section-title">工资项目</h4>
            <div class="personnel-form-items-grid">
                ${renderPersonnelFormItems(PRE_REFORM_SALARY_ITEMS, row)}
            </div>
            <div class="personnel-form-total">
                <span>工资合计</span>
                <strong>${personnelFormField(row, "hj2", { money: true })}</strong>
            </div>
        </div>
    `).join("");
}

function textField(row, fieldName) {
    if (!row || fieldName == null || fieldName === "") {
        return "";
    }
    const key = String(fieldName);
    const value = row[key] ?? row[key.toUpperCase()] ?? row[key.toLowerCase()];
    return value == null ? "" : value;
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
        return "检察";
    }
    if (value.includes("法官")) {
        return "审判";
    }
    if (value.includes("监察")) {
        return "监察";
    }
    return "警衔";
}

function initializeAssessmentBatchPage() {
    const yearInput = document.getElementById("assessment-batch-year");
    if (yearInput && !yearInput.value) {
        yearInput.value = String(new Date().getFullYear() - 1);
    }
    populateAssessmentBatchBulkSelect();
}

function updateAssessmentBatchWriteUi() {
    const writable = hasPersonnelWrite();
    const panel = document.getElementById("annual-assessment-management");
    if (panel) {
        panel.classList.toggle("assessment-management-readonly", !writable);
    }
    document.querySelectorAll(".assessment-batch-write-only").forEach(element => {
        element.classList.toggle("hidden", !writable);
    });
}

function assessmentBatchVisibleColumnCount() {
    const meta = state.assessmentBatchMeta;
    const hideOrg = Boolean(meta?.organizationCode) && !meta?.includeDescendants;
    let count = 7;
    if (hideOrg) {
        count -= 1;
    }
    if (hasPersonnelWrite()) {
        count += 1;
    }
    return count;
}

function populateAssessmentBatchResultFilter() {
    const select = document.getElementById("assessment-batch-result-filter");
    if (!select) {
        return;
    }
    const previous = select.value;
    const rows = state.assessmentBatchRows || [];
    const values = [...new Set(rows.map(row => (row.result || "").trim()).filter(Boolean))].sort();
    select.innerHTML = [
        `<option value="">全部</option>`,
        `<option value="__missing__">未录入</option>`,
        ...values.map(value => `<option value="${escapeHtml(value)}">${escapeHtml(value)}</option>`),
    ].join("");
    if (previous && [...select.options].some(option => option.value === previous)) {
        select.value = previous;
    }
}

function assessmentBatchRowMatchesFilter(row) {
    const resultFilter = document.getElementById("assessment-batch-result-filter")?.value || "";
    const status = assessmentBatchRowStatus(row);
    const result = (row.result || "").trim();
    if (!resultFilter) {
        return true;
    }
    if (resultFilter === "__missing__") {
        return status === "未录入";
    }
    return result === resultFilter;
}

function populateAssessmentBatchBulkSelect() {
    const select = document.getElementById("assessment-batch-bulk-result");
    if (!select || select.options.length) {
        return;
    }
    const options = [...new Set([
        ...assessmentResultOptions.administrative,
        ...assessmentResultOptions.institution,
    ])];
    select.innerHTML = options.map(option => `<option value="${escapeHtml(option)}">${escapeHtml(option)}</option>`).join("");
}

function assessmentResultOptionsForRow(row) {
    return usesInstitutionAssessmentResults(row) ? assessmentResultOptions.institution : assessmentResultOptions.administrative;
}

function isInstitutionAssessmentRow(row) {
    return usesInstitutionAssessmentResults(row);
}

function assessmentBatchRowStatus(row) {
    const current = (row.result || "").trim();
    const original = (row.originalResult || "").trim();
    if (!current && !original) {
        return "未录入";
    }
    if (current === original) {
        return "已录入";
    }
    return "已修改";
}

async function onAssessmentBatchSearch(event) {
    event.preventDefault();
    await loadAssessmentBatch();
}

async function loadAssessmentBatch() {
    const organizationCode = selectedOrganizationCode("assessment-batch-organization-code");
    const year = document.getElementById("assessment-batch-year").value.trim();
    const keyword = document.getElementById("assessment-batch-keyword").value.trim();
    const includeDescendants = document.getElementById("assessment-batch-include-descendants").checked;
    const status = document.getElementById("assessment-batch-status");
    if (!year) {
        status.className = "status error";
        status.textContent = "请填写考核年度。";
        return;
    }
    status.className = "status";
    status.textContent = "正在查询考核结果...";
    document.getElementById("assessment-batch-rows").innerHTML = "";
    try {
        const params = new URLSearchParams({ year, includeDescendants: String(includeDescendants) });
        if (organizationCode) {
            params.set("organizationCode", organizationCode);
        }
        if (keyword) {
            params.set("keyword", keyword);
        }
        const preview = await getJson(`/api/personnel/assessments/batch-entry?${params}`);
        state.assessmentBatchRows = (preview.rows || []).map(row => ({
            ...row,
            originalResult: row.result || "",
            result: row.result || "",
        }));
        state.assessmentBatchMeta = {
            organizationCode: organizationCode || preview.organizationCode || "",
            year: preview.year,
            includeDescendants,
        };
        const table = document.getElementById("assessment-batch-table");
        if (table) {
            table.classList.toggle("hide-org-column", Boolean(organizationCode) && !includeDescendants);
            table.classList.add("hide-year-column");
        }
        const selectAll = document.getElementById("assessment-batch-select-all");
        if (selectAll) {
            selectAll.checked = false;
        }
        populateAssessmentBatchResultFilter();
        renderAssessmentBatchRows();
        updateAssessmentBatchCounts(preview);
        status.textContent = preview.totalPersonnel ? "" : "未找到符合条件的人员。";
    } catch (error) {
        showError(status, error);
    }
}

function updateAssessmentBatchCounts(preview) {
    const rows = state.assessmentBatchRows || [];
    const entered = rows.filter(row => (row.result || "").trim()).length;
    const totalEl = document.getElementById("assessment-batch-total-count");
    const enteredEl = document.getElementById("assessment-batch-entered-count");
    const missingEl = document.getElementById("assessment-batch-missing-count");
    if (totalEl) {
        totalEl.textContent = preview?.totalPersonnel ?? rows.length;
    }
    if (enteredEl) {
        enteredEl.textContent = preview?.enteredCount ?? entered;
    }
    if (missingEl) {
        missingEl.textContent = preview?.missingCount ?? (rows.length - entered);
    }
    updateAssessmentBatchResultBreakdown(rows);
    updateAssessmentBatchVisibleCount(rows.filter(assessmentBatchRowMatchesFilter).length, rows.length);
}

function updateAssessmentBatchResultBreakdown(rows) {
    const el = document.getElementById("assessment-batch-result-breakdown");
    if (!el) {
        return;
    }
    const counts = new Map();
    for (const row of rows || []) {
        const result = (row.result || "").trim();
        if (!result) {
            continue;
        }
        counts.set(result, (counts.get(result) || 0) + 1);
    }
    if (!counts.size) {
        el.textContent = "";
        return;
    }
    const knownOrder = [...new Set([
        ...assessmentResultOptions.administrative,
        ...assessmentResultOptions.institution,
    ])];
    const labels = [...counts.keys()].sort((a, b) => {
        const indexA = knownOrder.indexOf(a);
        const indexB = knownOrder.indexOf(b);
        if (indexA >= 0 || indexB >= 0) {
            if (indexA < 0) {
                return 1;
            }
            if (indexB < 0) {
                return -1;
            }
            return indexA - indexB;
        }
        return a.localeCompare(b, "zh");
    });
    el.innerHTML = " · " + labels
        .map(label => `${escapeHtml(label)} <strong>${counts.get(label)}</strong>`)
        .join(" · ");
}

function updateAssessmentBatchVisibleCount(visibleCount, totalCount) {
    const hint = document.getElementById("assessment-batch-visible-hint");
    if (!hint) {
        return;
    }
    if (!totalCount || visibleCount === totalCount) {
        hint.textContent = "";
        return;
    }
    hint.textContent = ` · 显示 ${visibleCount} 人`;
}

function renderAssessmentBatchRows() {
    const tbody = document.getElementById("assessment-batch-rows");
    const rows = state.assessmentBatchRows || [];
    const writable = hasPersonnelWrite();
    const visibleColumns = assessmentBatchVisibleColumnCount();
    const filteredRows = rows.filter(assessmentBatchRowMatchesFilter);
    updateAssessmentBatchVisibleCount(filteredRows.length, rows.length);
    if (!rows.length) {
        tbody.innerHTML = `<tr><td colspan="${visibleColumns}">请选择单位和年度后查询</td></tr>`;
        return;
    }
    if (!filteredRows.length) {
        tbody.innerHTML = `<tr><td colspan="${visibleColumns}">当前筛选条件下无匹配人员</td></tr>`;
        return;
    }
    tbody.innerHTML = rows.map((row, index) => {
        const hidden = !assessmentBatchRowMatchesFilter(row);
        const baseOptions = assessmentResultOptionsForRow(row);
        const currentResult = (row.result || "").trim();
        const options = currentResult && !baseOptions.includes(currentResult)
            ? [...baseOptions, currentResult]
            : baseOptions;
        const statusText = assessmentBatchRowStatus(row);
        const statusClass = statusText === "未录入" ? "status-missing" : statusText === "已修改" ? "status-modified" : "";
        const resultCell = writable
            ? `<select data-assessment-batch-result="${index}">
                    <option value="">— 请选择 —</option>
                    ${options.map(option => `
                        <option value="${escapeHtml(option)}" ${option === (row.result || "") ? "selected" : ""}>${escapeHtml(option)}</option>
                    `).join("")}
                </select>`
            : escapeHtml(row.result || "—");
        return `
            <tr class="${hidden ? "hidden-row" : ""}" data-assessment-batch-index="${index}">
                ${writable ? `<td class="col-select"><input type="checkbox" data-assessment-batch-select="${index}"></td>` : ""}
                <td class="col-org">${escapeHtml(row.organizationName || row.organizationCode)}</td>
                <td class="col-code">${escapeHtml(row.personCode)}</td>
                <td class="col-name">${escapeHtml(row.name || "")}</td>
                <td class="col-position">${escapeHtml(row.currentPosition || "")}</td>
                <td class="col-year">${escapeHtml(row.year || state.assessmentBatchMeta?.year || "")}</td>
                <td class="col-result">${resultCell}</td>
                <td class="col-status"><span class="assessment-batch-status ${statusClass}">${statusText}</span></td>
            </tr>
        `;
    }).join("");
    if (!writable) {
        return;
    }
    tbody.querySelectorAll("[data-assessment-batch-result]").forEach(select => {
        select.addEventListener("change", event => {
            const index = Number(event.target.dataset.assessmentBatchResult);
            state.assessmentBatchRows[index].result = event.target.value;
            const statusCell = event.target.closest("tr")?.querySelector(".assessment-batch-status");
            if (statusCell) {
                const statusText = assessmentBatchRowStatus(state.assessmentBatchRows[index]);
                statusCell.textContent = statusText;
                statusCell.className = `assessment-batch-status ${statusText === "未录入" ? "status-missing" : statusText === "已修改" ? "status-modified" : ""}`;
            }
            updateAssessmentBatchCounts();
            populateAssessmentBatchResultFilter();
            const rowEl = event.target.closest("tr");
            if (rowEl) {
                rowEl.classList.toggle("hidden-row", !assessmentBatchRowMatchesFilter(state.assessmentBatchRows[index]));
            }
        });
    });
}

function applyAssessmentBatchBulkResult() {
    const bulkResult = document.getElementById("assessment-batch-bulk-result").value;
    if (!bulkResult) {
        alert("请先选择要批量设置的结果。");
        return;
    }
    document.querySelectorAll("[data-assessment-batch-select]:checked").forEach(checkbox => {
        const index = Number(checkbox.dataset.assessmentBatchSelect);
        state.assessmentBatchRows[index].result = bulkResult;
    });
    populateAssessmentBatchResultFilter();
    renderAssessmentBatchRows();
    updateAssessmentBatchCounts();
}

function fillAssessmentBatchDefaults() {
    (state.assessmentBatchRows || []).forEach(row => {
        if (!(row.result || "").trim()) {
            row.result = row.defaultResult || defaultAssessmentResultForRow(row);
        }
    });
    populateAssessmentBatchResultFilter();
    renderAssessmentBatchRows();
    updateAssessmentBatchCounts();
}

function defaultAssessmentResultForRow(row) {
    return usesInstitutionAssessmentResults(row) ? "合格" : "称职";
}

async function saveAssessmentBatch() {
    const meta = state.assessmentBatchMeta;
    if (!meta?.year) {
        alert("请先加载人员列表。");
        return;
    }
    const records = (state.assessmentBatchRows || [])
        .filter(row => (row.result || "").trim())
        .map(row => ({
            organizationCode: row.organizationCode,
            personCode: row.personCode,
            result: row.result.trim(),
        }));
    if (!records.length) {
        alert("没有可保存的考核结果，请先填写或使用“未录入填默认”。");
        return;
    }
    const dirtyCount = state.assessmentBatchRows.filter(row => assessmentBatchRowStatus(row) !== "已录入").length;
    const message = dirtyCount
        ? `将保存 ${records.length} 条考核记录（含新增或修改 ${dirtyCount} 条），是否继续？`
        : `将保存 ${records.length} 条考核记录，是否继续？`;
    if (!confirm(message)) {
        return;
    }
    const status = document.getElementById("assessment-batch-status");
    status.className = "status";
    status.textContent = "正在保存考核结果...";
    try {
        const payload = {
            year: meta.year,
            includeDescendants: meta.includeDescendants,
            records,
        };
        if (meta.organizationCode) {
            payload.organizationCode = meta.organizationCode;
        }
        const result = await postJson("/api/personnel/assessments/batch-entry", payload);
        const failureText = (result.failures || []).slice(0, 3).map(item => `${item.personCode} ${item.name}: ${item.message}`).join("；");
        status.textContent = `保存完成：新增 ${result.inserted} 条，更新 ${result.updated} 条，跳过 ${result.skipped} 条。${failureText || ""}`;
        if (result.skipped > 0) {
            status.className = "status error";
        }
        await loadAssessmentBatch();
    } catch (error) {
        showError(status, error);
    }
}

async function loadAssessmentSummary() {
    const organizationCode = selectedOrganizationCode("assessment-summary-organization-code");
    const year = document.getElementById("assessment-summary-year").value.trim();
    const resultFilter = document.getElementById("assessment-summary-result").value.trim();
    const includeDescendants = document.getElementById("assessment-summary-include-descendants").checked;
    const page = document.getElementById("assessment-summary-page").value || "0";
    const size = document.getElementById("assessment-summary-size").value || "50";
    const params = new URLSearchParams({ page, size, includeDescendants: String(includeDescendants) });
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
    const organizationCode = selectedOrganizationCode("changed-personnel-organization-code");
    const period = document.getElementById("changed-personnel-period")?.value?.trim() || "";
    const keyword = document.getElementById("changed-personnel-keyword")?.value?.trim() || "";
    const page = String(state.changedPersonnelPage || 0);
    const size = "20";
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
    if (!status || !rows) {
        return;
    }
    status.className = "status";
    status.textContent = "正在查询变动人员信息...";
    rows.innerHTML = "";

    try {
        const result = await getJson(`/api/personnel/changed?${params}`);
        const totalPages = Math.max(result.totalPages || 1, 1);
        if ((result.page || 0) >= totalPages && totalPages > 0 && (result.totalElements || 0) > 0) {
            state.changedPersonnelPage = Math.max(totalPages - 1, 0);
            return loadChangedPersonnel();
        }
        state.changedPersonnelPage = result.page || 0;
        rows.innerHTML = (result.content || []).map(row => `
            <tr>
                <td class="col-org" title="${escapeHtml(row.organizationCode || "")}">${escapeHtml(row.organizationName || row.organizationCode || "")}</td>
                <td class="col-code">${escapeHtml(row.personCode || "")}</td>
                <td class="col-name">${escapeHtml(row.name || "")}</td>
                <td class="col-id">${escapeHtml(row.idCard || "")}</td>
                <td class="col-gender">${escapeHtml(row.gender || "")}</td>
                <td class="col-position" title="${escapeHtml(row.newPositionName || row.newPositionCode || "")}">${escapeHtml(row.newPositionName || row.newPositionCode || "")}</td>
                <td class="col-remark" title="${escapeHtml(row.remark || "")}">${escapeHtml(row.remark || "")}</td>
                <td class="col-actions">
                    ${row.uid ? `<button class="row-action" data-view-changed-uid="${escapeHtml(row.uid)}" type="button">查看</button>` : ""}
                    <button class="row-action" data-restore-org="${escapeHtml(row.organizationCode)}" data-restore-person="${escapeHtml(row.personCode)}" data-restore-name="${escapeHtml(row.name)}" type="button">恢复在册</button>
                </td>
            </tr>`).join("") || "<tr><td colspan='8'>暂无变动人员</td></tr>";
        rows.querySelectorAll("button[data-view-changed-uid]").forEach(button => {
            button.addEventListener("click", () => viewChangedPersonnelDetail(button.dataset.viewChangedUid));
        });
        rows.querySelectorAll("button[data-restore-org]").forEach(button => {
            button.addEventListener("click", () => restoreChangedPersonnel(
                button.dataset.restoreOrg,
                button.dataset.restorePerson,
                button.dataset.restoreName || ""));
        });
        const total = result.totalElements || 0;
        status.textContent = total
            ? `共 ${total} 人，第 ${state.changedPersonnelPage + 1} / ${totalPages} 页`
            : "未查询到变动人员";
        renderChangedPersonnelPagination(total, totalPages);
    } catch (error) {
        renderChangedPersonnelPagination(0, 1);
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
        if (hasPersonnelAccess()) {
            await loadPersonnel();
        }
    } catch (error) {
        showError(status, error);
    }
}

async function viewChangedPersonnelDetail(uid) {
    const status = document.getElementById("changed-personnel-status");
    status.className = "status";
    status.textContent = "正在加载变动人员详情...";
    try {
        const detail = await getJson(`/api/personnel/changed/${encodeURIComponent(uid)}/detail`);
        const record = detail.basic || {};
        fillPersonnelMaintenanceForm(record);
        openPersonnelMaintenanceModal(
            "变动人员详情",
            `${record.organizationCode || ""}-${record.personCode || ""} ${record.name || ""}（只读）`,
            true);
        renderChangedPersonnelDetailTabs(detail);
        status.textContent = `正在查看：${record.name || record.personCode || uid}`;
    } catch (error) {
        showError(status, error);
    }
}

function renderChangedPersonnelDetailTabs(detail) {
    const education = detail.education || [];
    const positions = detail.positions || [];
    const assessments = detail.assessments || [];
    const histories = detail.payrollHistories || [];
    state.maintPayrollHistories = histories;
    document.getElementById("maint-education-rows").innerHTML = education.length ? education.map(row => `
        <tr>
            <td>${escapeHtml(row.id)}</td>
            <td>${escapeHtml(row.educationCode)}</td>
            <td>${escapeHtml(row.educationName)}</td>
            <td>${escapeHtml(row.school)}</td>
            <td>${escapeHtml(row.enrollmentDate)}</td>
            <td>${escapeHtml(row.graduationDate)}</td>
            <td>${escapeHtml(row.educationType)}</td>
            <td>${escapeHtml(row.remark || "")}</td>
        </tr>
    `).join("") : "<tr><td colspan='8'>暂无学历记录</td></tr>";
    document.getElementById("maint-position-rows").innerHTML = renderMaintPositionRows(positions, true);
    document.getElementById("maint-assessment-rows").innerHTML = assessments.length ? assessments.map(row => `
        <tr>
            <td class="col-year">${escapeHtml(row.year)}</td>
            <td class="col-result"><span class="${assessmentResultTagClass(row.result)}">${escapeHtml(row.result || "—")}</span></td>
            <td class="col-action">—</td>
        </tr>
    `).join("") : "<tr><td colspan='3'>暂无考核记录</td></tr>";
    document.getElementById("maint-payroll-rows").innerHTML = histories.length ? histories.map(row => `
        <tr>
            <td>${escapeHtml(row.calculationYear || "")}${escapeHtml(row.calculationMonth || "")}</td>
            <td>${escapeHtml(row.changeType || "")}</td>
            <td>${escapeHtml(row.positionName || "")}</td>
            <td>${escapeHtml(row.gradeSalaryLevel || "")}</td>
            <td>${escapeHtml(row.positionSalaryGrade || "")}</td>
            <td>${escapeHtml(row.levelAssessmentStartYear || "")}</td>
            <td>${escapeHtml(row.stepAssessmentStartYear || "")}</td>
            <td>${money(row.positionSalary)}</td>
            <td>${money(row.gradeSalary)}</td>
            <td>${money(row.totalAmount)}</td>
            <td>${row.currentPayroll ? "是" : "否"}</td>
        </tr>
    `).join("") : "<tr><td colspan='11'>暂无调资记录</td></tr>";
    renderPersonnelRelatedRecords(detail.relatedRecords || {});
    document.getElementById("maint-wage-projection-result").textContent = "变动人员为只读查看，不支持工资推算。";
    document.getElementById("maint-wage-projection-steps").innerHTML = "—";
    resetProjectionOverview();
}

async function loadPositionHistory() {
    await ensurePositionHistoryOptions();
    const organizationCode = selectedOrganizationCode("position-history-organization-code");
    const keyword = document.getElementById("position-history-keyword")?.value?.trim() || "";
    const positionCode = document.getElementById("position-history-position-code")?.value?.trim() || "";
    const page = String(state.positionHistoryPage || 0);
    const size = "20";
    const params = new URLSearchParams({ page, size });
    if (organizationCode) {
        params.set("organizationCode", organizationCode);
    }
    if (keyword) {
        params.set("keyword", keyword);
    }
    if (positionCode) {
        params.set("positionCode", positionCode);
    }

    const status = document.getElementById("position-history-status");
    const rows = document.getElementById("position-history-rows");
    if (!status || !rows) {
        return;
    }
    status.className = "status";
    status.textContent = "正在查询任职岗位信息...";
    rows.innerHTML = "";

    try {
        const result = await getJson(`/api/personnel/positions?${params}`);
        const totalPages = Math.max(result.totalPages || 1, 1);
        if ((result.page || 0) >= totalPages && totalPages > 0 && (result.totalElements || 0) > 0) {
            state.positionHistoryPage = Math.max(totalPages - 1, 0);
            return loadPositionHistory();
        }
        state.positionHistoryPage = result.page || 0;
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
                <td>${escapeHtml(row.startYearMonth)}</td>
                <td>${escapeHtml(row.intervalYears)}</td>
                <td>${escapeHtml(row.activeFlag)}</td>
                <td>${formatPromotionFlag(row.promotionFlag)}</td>
            </tr>
        `).join("") || "<tr><td colspan='14'>暂无任职记录</td></tr>";
        const total = result.totalElements || 0;
        status.textContent = total
            ? `共 ${total} 条，第 ${state.positionHistoryPage + 1} / ${totalPages} 页`
            : "未查询到任职记录";
        renderPositionHistoryPagination(total, totalPages);
    } catch (error) {
        renderPositionHistoryPagination(0, 1);
        showError(status, error);
    }
}

async function loadEducationHistory() {
    await ensureEducationHistoryOptions();
    const organizationCode = selectedOrganizationCode("education-history-organization-code");
    const keyword = document.getElementById("education-history-keyword")?.value?.trim() || "";
    const educationCode = document.getElementById("education-history-education-code")?.value?.trim() || "";
    const page = String(state.educationHistoryPage || 0);
    const size = "20";
    const params = new URLSearchParams({ page, size });
    if (organizationCode) {
        params.set("organizationCode", organizationCode);
    }
    if (keyword) {
        params.set("keyword", keyword);
    }
    if (educationCode) {
        params.set("educationCode", educationCode);
    }

    const status = document.getElementById("education-history-status");
    const rows = document.getElementById("education-history-rows");
    if (!status || !rows) {
        return;
    }
    status.className = "status";
    status.textContent = "正在查询学历信息...";
    rows.innerHTML = "";

    try {
        const result = await getJson(`/api/personnel/education?${params}`);
        const totalPages = Math.max(result.totalPages || 1, 1);
        if ((result.page || 0) >= totalPages && totalPages > 0 && (result.totalElements || 0) > 0) {
            state.educationHistoryPage = Math.max(totalPages - 1, 0);
            return loadEducationHistory();
        }
        state.educationHistoryPage = result.page || 0;
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
        `).join("") || "<tr><td colspan='12'>暂无学历记录</td></tr>";
        const total = result.totalElements || 0;
        status.textContent = total
            ? `共 ${total} 条，第 ${state.educationHistoryPage + 1} / ${totalPages} 页`
            : "未查询到学历记录";
        renderEducationHistoryPagination(total, totalPages);
    } catch (error) {
        renderEducationHistoryPagination(0, 1);
        showError(status, error);
    }
}

function fillPersonnelStatisticsYearOptions() {
    const select = document.getElementById("personnel-statistics-year");
    if (!select) {
        return;
    }
    const currentYear = new Date().getFullYear();
    const previous = select.value;
    const years = [];
    for (let year = currentYear + 1; year >= currentYear - 30; year -= 1) {
        years.push(String(year));
    }
    select.innerHTML = `<option value="">全部</option>${years.map(year =>
        `<option value="${year}">${year}</option>`
    ).join("")}`;
    if (previous && years.includes(previous)) {
        select.value = previous;
    } else {
        select.value = "";
    }
}

function bindPersonnelStatisticsYearClickDefault() {
    const select = document.getElementById("personnel-statistics-year");
    if (!select || select.dataset.currentYearClickBound === "1") {
        return;
    }
    select.dataset.currentYearClickBound = "1";
    const applyCurrentYearIfAll = () => {
        fillPersonnelStatisticsYearOptions();
        if (select.value) {
            return;
        }
        const currentYear = String(new Date().getFullYear());
        if ([...select.options].some(option => option.value === currentYear)) {
            select.value = currentYear;
        }
    };
    select.addEventListener("pointerdown", applyCurrentYearIfAll);
    select.addEventListener("focus", applyCurrentYearIfAll);
}

function selectedPersonnelStatisticsChangeTypes() {
    const select = document.getElementById("personnel-statistics-change-type");
    const value = select?.value?.trim() || "";
    return value ? [value] : [];
}

function personnelStatisticsMetricFlags() {
    return {
        changeCount: document.getElementById("personnel-statistics-metric-change-count")?.checked !== false,
        personnelCount: document.getElementById("personnel-statistics-metric-personnel-count")?.checked !== false,
    };
}

function formatStatisticsPeriod(period) {
    const value = String(period || "").trim();
    if (/^\d{6}$/.test(value)) {
        return `${value.slice(0, 4)}.${value.slice(4)}`;
    }
    return value;
}

function personnelStatisticsPageSize() {
    const size = parseInt(document.getElementById("personnel-statistics-page-size")?.value || "20", 10);
    return Number.isNaN(size) || size < 1 ? 20 : size;
}

function renderPersonnelStatisticsTable(payrollChanges) {
    state.personnelStatisticsRows = Array.isArray(payrollChanges) ? payrollChanges : [];
    const total = state.personnelStatisticsRows.length;
    const size = personnelStatisticsPageSize();
    state.personnelStatisticsTotalPages = Math.max(1, Math.ceil(total / size) || 1);
    if (state.personnelStatisticsPage > state.personnelStatisticsTotalPages - 1) {
        state.personnelStatisticsPage = Math.max(state.personnelStatisticsTotalPages - 1, 0);
    }
    renderPersonnelStatisticsPage();
}

function renderPersonnelStatisticsPage() {
    const metrics = personnelStatisticsMetricFlags();
    if (!metrics.changeCount && !metrics.personnelCount) {
        metrics.changeCount = true;
        metrics.personnelCount = true;
        const changeCountInput = document.getElementById("personnel-statistics-metric-change-count");
        const personnelCountInput = document.getElementById("personnel-statistics-metric-personnel-count");
        if (changeCountInput) {
            changeCountInput.checked = true;
        }
        if (personnelCountInput) {
            personnelCountInput.checked = true;
        }
    }
    const allRows = state.personnelStatisticsRows || [];
    const size = personnelStatisticsPageSize();
    const total = allRows.length;
    state.personnelStatisticsTotalPages = Math.max(1, Math.ceil(total / size) || 1);
    if (state.personnelStatisticsPage > state.personnelStatisticsTotalPages - 1) {
        state.personnelStatisticsPage = Math.max(state.personnelStatisticsTotalPages - 1, 0);
    }
    const start = state.personnelStatisticsPage * size;
    const pageRows = allRows.slice(start, start + size);
    const head = document.getElementById("personnel-statistics-head");
    const rows = document.getElementById("personnel-statistics-rows");
    const metricHeaders = [
        metrics.changeCount ? "<th>变动条数</th>" : "",
        metrics.personnelCount ? "<th>涉及人数</th>" : "",
    ].join("");
    head.innerHTML = `<tr><th>变动类别</th><th>变动年月</th>${metricHeaders}</tr>`;
    const colSpan = 2 + (metrics.changeCount ? 1 : 0) + (metrics.personnelCount ? 1 : 0);
    rows.innerHTML = pageRows.map(row => `
        <tr>
            <td>${escapeHtml(row.changeType || "")}</td>
            <td>${escapeHtml(formatStatisticsPeriod(row.period))}</td>
            ${metrics.changeCount ? `<td>${escapeHtml(row.changeCount)}</td>` : ""}
            ${metrics.personnelCount ? `<td>${escapeHtml(row.personnelCount)}</td>` : ""}
        </tr>
    `).join("") || `<tr><td colspan="${colSpan}">暂无工资变动统计数据</td></tr>`;
    renderPersonnelStatisticsPagination(total);
}

function renderPersonnelStatisticsPagination(totalElements) {
    const bar = document.getElementById("personnel-statistics-pagination");
    if (!bar) {
        return;
    }
    const totalPages = state.personnelStatisticsTotalPages;
    const current = state.personnelStatisticsPage;
    bar.classList.remove("hidden");
    document.getElementById("personnel-statistics-total-pages").textContent = String(totalPages);
    document.getElementById("personnel-statistics-total-count").textContent = String(totalElements);
    const pageInput = document.getElementById("personnel-statistics-page-input");
    pageInput.value = String(current + 1);
    pageInput.max = String(totalPages);
    const noData = totalElements === 0;
    document.getElementById("personnel-statistics-first").disabled = noData || current <= 0;
    document.getElementById("personnel-statistics-prev").disabled = noData || current <= 0;
    document.getElementById("personnel-statistics-next").disabled = noData || current >= totalPages - 1;
    document.getElementById("personnel-statistics-last").disabled = noData || current >= totalPages - 1;
    pageInput.disabled = noData;
}

function gotoPersonnelStatisticsPage(page) {
    const target = Math.min(Math.max(page, 0), state.personnelStatisticsTotalPages - 1);
    if (target === state.personnelStatisticsPage) {
        return;
    }
    state.personnelStatisticsPage = target;
    renderPersonnelStatisticsPage();
}

async function loadPersonnelStatistics() {
    fillPersonnelStatisticsYearOptions();
    bindPersonnelStatisticsYearClickDefault();
    const organizationCode = selectedOrganizationCode("personnel-statistics-organization-code");
    const year = document.getElementById("personnel-statistics-year")?.value.trim() || "";
    const month = document.getElementById("personnel-statistics-month")?.value.trim() || "";
    const changeTypeSelect = document.getElementById("personnel-statistics-change-type");
    const selectedTypes = selectedPersonnelStatisticsChangeTypes();
    const params = new URLSearchParams();
    if (organizationCode) {
        params.set("organizationCode", organizationCode);
    }
    if (year) {
        params.set("year", year);
    }
    if (month) {
        params.set("month", month);
    }
    selectedTypes.forEach(type => params.append("changeTypes", type));
    const status = document.getElementById("personnel-statistics-status");
    status.className = "status";
    status.textContent = "正在加载统计数据...";
    try {
        const filterParams = new URLSearchParams();
        if (organizationCode) {
            filterParams.set("organizationCode", organizationCode);
        }
        if (year) {
            filterParams.set("year", year);
        }
        if (month) {
            filterParams.set("month", month);
        }
        const summaryParams = new URLSearchParams();
        if (organizationCode) {
            summaryParams.set("organizationCode", organizationCode);
        }
        const [summary, payrollChanges, changeTypes] = await Promise.all([
            getJson(`/api/statistics/personnel-summary?${summaryParams}`),
            getJson(`/api/statistics/payroll-change-summary?${params}`),
            getJson(`/api/statistics/payroll-change-types?${filterParams}`),
        ]);
        if (changeTypeSelect) {
            const previous = selectedTypes[0] || "";
            changeTypeSelect.innerHTML = `<option value="">全部</option>${(changeTypes || []).map(type =>
                `<option value="${escapeHtml(type)}"${type === previous ? " selected" : ""}>${escapeHtml(type)}</option>`
            ).join("")}`;
            if (previous && !(changeTypes || []).includes(previous)) {
                changeTypeSelect.value = "";
            }
        }
        document.getElementById("stat-org-count").textContent = summary.organizationCount ?? "-";
        document.getElementById("stat-active-count").textContent = summary.activePersonnelCount ?? "-";
        document.getElementById("stat-changed-count").textContent = summary.changedPersonnelCount ?? "-";
        document.getElementById("stat-probation-count").textContent = summary.probationPersonnelCount ?? "-";
        renderPersonnelStatisticsTable(payrollChanges);
        const total = (payrollChanges || []).length;
        const size = personnelStatisticsPageSize();
        const page = state.personnelStatisticsPage + 1;
        const totalPages = Math.max(1, Math.ceil(total / size) || 1);
        status.textContent = total
            ? `统计加载完成，共 ${total} 组（第 ${page} / ${totalPages} 页）`
            : "统计加载完成，暂无工资变动数据";
    } catch (error) {
        showError(status, error);
    }
}

async function editDictionaryEntry(code) {
    const row = (state.dictionaryRows || []).find(item => String(item.code) === String(code));
    openDictionaryMaintenanceModal("edit", row || { code });
}

function suggestNextDictionaryCategoryCode(categories) {
    let max = 0;
    (categories || []).forEach(item => {
        const code = String(item.code || "").trim();
        if (/^\d{3}$/.test(code)) {
            max = Math.max(max, Number(code));
        }
    });
    return String(Math.min(max + 1, 999)).padStart(3, "0");
}

function suggestNextDictionaryOptionCode(category, rows) {
    const prefix = String(category || "").trim();
    if (!prefix) {
        return "";
    }
    let max = 0;
    let width = 2;
    (rows || []).forEach(row => {
        const code = String(row.code || "").trim();
        if (!code.startsWith(prefix) || code.length <= prefix.length) {
            return;
        }
        const suffix = code.slice(prefix.length);
        if (!/^\d+$/.test(suffix)) {
            return;
        }
        if (suffix.length === 2 || (max === 0 && suffix.length >= 2)) {
            width = Math.max(width, Math.min(suffix.length, 4));
            max = Math.max(max, Number(suffix));
        }
    });
    return prefix + String(max + 1).padStart(width, "0");
}

function openDictionaryMaintenanceModal(mode, record = {}) {
    if (!hasSystemConfigWrite()) {
        return;
    }
    const title = document.getElementById("dictionary-maintenance-modal-title");
    const modeInput = document.getElementById("dictionary-modal-mode");
    const codeInput = document.getElementById("dictionary-modal-code");
    const nameInput = document.getElementById("dictionary-modal-name");
    const parentInput = document.getElementById("dictionary-modal-parent-code");
    const systemInput = document.getElementById("dictionary-modal-system-flag");
    const enabledInput = document.getElementById("dictionary-modal-enabled-flag");
    const disableButton = document.getElementById("dictionary-maintenance-modal-disable");
    const status = document.getElementById("dictionary-maintenance-modal-status");
    if (!modeInput || !codeInput) {
        return;
    }
    modeInput.value = mode;
    status.className = "status";
    status.textContent = "";
    if (mode === "create-category") {
        title.textContent = "增加分类";
        const categories = [...document.getElementById("dictionary-category").options]
            .filter(opt => opt.value)
            .map(opt => ({ code: opt.value }));
        codeInput.value = suggestNextDictionaryCategoryCode(categories);
        codeInput.readOnly = false;
        nameInput.value = "";
        parentInput.value = "";
        systemInput.value = "1";
        enabledInput.value = "1";
        disableButton.classList.add("hidden");
    } else if (mode === "create-option") {
        const category = document.getElementById("dictionary-category")?.value.trim() || "";
        if (!category) {
            showError(document.getElementById("dictionary-status"), new Error("请先选择分类"));
            return;
        }
        title.textContent = `为分类 ${category} 增加选项`;
        codeInput.value = suggestNextDictionaryOptionCode(category, state.dictionaryRows);
        codeInput.readOnly = false;
        nameInput.value = "";
        parentInput.value = "";
        systemInput.value = "1";
        enabledInput.value = "1";
        disableButton.classList.add("hidden");
    } else {
        title.textContent = "编辑字典";
        codeInput.value = record.code || "";
        codeInput.readOnly = true;
        nameInput.value = record.name || "";
        parentInput.value = record.parentCode || "";
        systemInput.value = record.systemFlag ?? 1;
        enabledInput.value = record.enabledFlag ?? 1;
        disableButton.classList.toggle("hidden", Number(record.enabledFlag) === 0);
    }
    document.getElementById("dictionary-maintenance-modal").classList.remove("hidden");
    nameInput.focus();
}

function closeDictionaryMaintenanceModal() {
    document.getElementById("dictionary-maintenance-modal")?.classList.add("hidden");
}

async function onDictionaryMaintenanceModalSubmit(event) {
    event.preventDefault();
    const mode = document.getElementById("dictionary-modal-mode").value;
    const code = document.getElementById("dictionary-modal-code").value.trim();
    const name = document.getElementById("dictionary-modal-name").value.trim();
    const parentCode = document.getElementById("dictionary-modal-parent-code").value.trim();
    const systemFlag = Number(document.getElementById("dictionary-modal-system-flag").value || 0);
    const enabledFlag = Number(document.getElementById("dictionary-modal-enabled-flag").value || 1);
    const status = document.getElementById("dictionary-maintenance-modal-status");
    status.className = "status";
    if (!code || !name) {
        status.className = "status error";
        status.textContent = "编码和名称不能为空";
        return;
    }
    if (mode === "create-category" && !/^\d{3}$/.test(code)) {
        status.className = "status error";
        status.textContent = "分类编码须为 3 位数字";
        return;
    }
    if (mode === "create-option") {
        const category = document.getElementById("dictionary-category")?.value.trim() || "";
        if (!category || !code.startsWith(category) || code.length <= category.length) {
            status.className = "status error";
            status.textContent = `选项编码须以当前分类 ${category} 开头，且长于分类编码`;
            return;
        }
    }
    const payload = { code, name, parentCode, systemFlag, enabledFlag };
    status.textContent = "正在保存...";
    try {
        if (mode === "edit") {
            await putJson(`/api/dictionaries/${encodeURIComponent(code)}`, payload);
        } else {
            await postJson("/api/dictionaries", payload);
        }
        closeDictionaryMaintenanceModal();
        if (mode === "create-category") {
            await refreshDictionaryCategories();
            const select = document.getElementById("dictionary-category");
            if (select) {
                select.value = code;
            }
        }
        await loadDictionaries({ statusMessage: "保存成功" });
    } catch (error) {
        showError(status, error);
    }
}

async function onDictionaryMaintenanceDisable() {
    const code = document.getElementById("dictionary-modal-code").value.trim();
    if (!code || !confirm(`确认停用字典 ${code}？`)) {
        return;
    }
    const status = document.getElementById("dictionary-maintenance-modal-status");
    status.className = "status";
    status.textContent = "正在停用...";
    try {
        await deleteJson(`/api/dictionaries/${encodeURIComponent(code)}`);
        closeDictionaryMaintenanceModal();
        await loadDictionaries({ statusMessage: "已停用" });
    } catch (error) {
        showError(status, error);
    }
}

async function editLocalPolicy(id) {
    const organizationName = prompt("政策单位名称：");
    if (organizationName === null) {
        return;
    }
    try {
        await putJson(`/api/system-config/local-policies/${id}`, {
            organizationCode: "",
            organizationName,
        });
        await loadLocalPolicies({ statusMessage: "保存成功" });
    } catch (error) {
        showError(document.getElementById("local-policy-status"), error);
    }
}

async function editSystemOptions(current) {
    const decimalPlaces = prompt("小数位数：", current?.decimalPlaces || "0");
    if (decimalPlaces === null) {
        return;
    }
    try {
        await putJson("/api/system-config/options", {
            enterpriseTransferRaise: current?.enterpriseTransferRaise || "",
            gradeStepEducationLink: current?.gradeStepEducationLink || "",
            decimalPlaces,
            policeRankAllowance: current?.policeRankAllowance || "",
            reformBonusBalance: current?.reformBonusBalance || "",
            floatingSalary: current?.floatingSalary || "",
        });
        await loadLocalPolicies({ statusMessage: "系统选项保存成功" });
    } catch (error) {
        showError(document.getElementById("local-policy-status"), error);
    }
}

async function editAllowanceStandardAmount(id, item, positionCode, name, performanceCategory, currentAmount) {
    const captions = allowanceStandardColumnCaptions(performanceCategory);
    const itemLabel = item === "SDBT" ? captions.sdbt : captions.dfbt2;
    if (!id) {
        alert(`该职务没有 ${itemLabel} 标准行`);
        return;
    }
    const amount = prompt(`${itemLabel} 金额：`, currentAmount == null ? "0" : String(currentAmount));
    if (amount === null) {
        return;
    }
    const standardYearMonth = document.getElementById("allowance-standard-year-month")?.value?.trim() || "";
    if (!standardYearMonth) {
        alert("请先选择标准年月");
        return;
    }
    try {
        const existingList = await getJson(
            `/api/payroll/allowance-standards?${new URLSearchParams({
                standardYearMonth,
                item,
                positionCode,
                page: "0",
                size: "50",
            })}`);
        const existing = (existingList.content || []).find(row => Number(row.id) === Number(id))
            || (existingList.content || [])[0];
        if (!existing) {
            throw new Error("未找到该津贴标准记录");
        }
        await putJson(`/api/standards/allowances/${id}`, {
            standardYearMonth: existing.standardYearMonth,
            item: existing.item,
            positionCode: existing.positionCode,
            name: existing.name || name || "",
            workYearsLower: existing.workYearsLower ?? 0,
            workYearsUpper: existing.workYearsUpper ?? 99,
            amount: Number(amount),
            performanceCategory: existing.performanceCategory ?? performanceCategory ?? 0,
        });
        await loadAllowanceStandards();
    } catch (error) {
        showError(document.getElementById("allowance-standards-status"), error);
    }
}

async function createAllowanceStandard() {
    const standardYearMonth = prompt(
        "标准年月：",
        document.getElementById("allowance-standard-year-month")?.value?.trim() || "202407");
    if (standardYearMonth === null) {
        return;
    }
    const item = prompt("项目编码：", "DFBT2");
    if (item === null) {
        return;
    }
    const positionCode = prompt("职务编码：", "");
    if (positionCode === null) {
        return;
    }
    const name = prompt("名称：", "");
    if (name === null) {
        return;
    }
    const amount = prompt("金额：", "0");
    if (amount === null) {
        return;
    }
    const categoryDefault = document.getElementById("allowance-standard-category")?.value?.trim() || "1";
    const performanceCategory = prompt("绩效类别(jxlb)：", categoryDefault);
    if (performanceCategory === null) {
        return;
    }
    try {
        await postJson("/api/standards/allowances", {
            standardYearMonth,
            item,
            positionCode,
            name,
            workYearsLower: 0,
            workYearsUpper: 99,
            amount: Number(amount),
            performanceCategory: Number(performanceCategory) || 0,
        });
        const yearSelect = document.getElementById("allowance-standard-year-month");
        if (yearSelect) {
            yearSelect.value = standardYearMonth;
        }
        await refreshAllowanceStandardPeriods();
        if (yearSelect) {
            yearSelect.value = standardYearMonth;
        }
        await refreshAllowanceStandardCategories();
        await refreshAllowanceStandardPositionCategories();
        await loadAllowanceStandards();
    } catch (error) {
        showError(document.getElementById("allowance-standards-status"), error);
    }
}

async function editRankAllowanceStandard(id) {
    try {
        const existing = (await getJson(`/api/payroll/rank-allowance-standards?page=0&size=1000`)).content.find(row => row.id === id);
        if (!existing) {
            throw new Error("未找到该津贴标准");
        }
        const amount = prompt("标准金额：", existing.amount);
        if (amount === null) {
            return;
        }
        await putJson(`/api/standards/ranks/${id}`, {
            standardYearMonth: existing.standardYearMonth,
            rankCode: existing.rankCode,
            rankName: existing.rankName,
            amount: Number(amount),
            category: existing.category,
        });
        await refreshRankAllowanceStandardPeriods();
        await loadRankAllowanceStandards();
    } catch (error) {
        showError(document.getElementById("rank-standards-status"), error);
    }
}

function rankAllowanceCategoryLabel(category) {
    const map = { jx: "警衔津贴", jc: "检察津贴", sp: "审判津贴", mt: "监察津贴" };
    const key = String(category || "").trim().toLowerCase();
    return map[key] ? `${map[key]}（${key}）` : (category || "");
}

async function createRankAllowanceStandard() {
    const standardYearMonth = prompt("标准年月：", document.getElementById("rank-standard-year-month").value.trim() || "202407");
    if (standardYearMonth === null) {
        return;
    }
    const rankCode = prompt("编码：", "");
    if (rankCode === null) {
        return;
    }
    const rankName = prompt("名称：", "");
    if (rankName === null) {
        return;
    }
    const amount = prompt("金额：", "0");
    if (amount === null) {
        return;
    }
    const category = prompt("类别（jx/jc/sp/mt）：", document.getElementById("rank-standard-category").value.trim() || "jx");
    if (category === null) {
        return;
    }
    try {
        await postJson("/api/standards/ranks", {
            standardYearMonth,
            rankCode,
            rankName,
            amount: Number(amount),
            category,
        });
        const categorySelect = document.getElementById("rank-standard-category");
        if (categorySelect && category) {
            categorySelect.value = category;
        }
        await refreshRankAllowanceStandardPeriods();
        const yearSelect = document.getElementById("rank-standard-year-month");
        if (yearSelect && standardYearMonth) {
            yearSelect.value = standardYearMonth;
        }
        await loadRankAllowanceStandards();
    } catch (error) {
        showError(document.getElementById("rank-standards-status"), error);
    }
}

async function deleteRankAllowanceStandard(id) {
    if (!confirm("确认删除该津贴标准？")) {
        return;
    }
    try {
        await deleteJson(`/api/standards/ranks/${id}`);
        await refreshRankAllowanceStandardPeriods();
        await loadRankAllowanceStandards();
    } catch (error) {
        showError(document.getElementById("rank-standards-status"), error);
    }
}

async function editRetainedAllowanceStandard(positionCode) {
    try {
        const existing = (await getJson(`/api/payroll/retained-allowance-standards?page=0&size=1000`)).content
            .find(row => row.positionCode === positionCode);
        if (!existing) {
            throw new Error("未找到该保留福补标准");
        }
        const name = prompt("名称：", existing.name);
        if (name === null) {
            return;
        }
        const amount = prompt("金额：", existing.amount);
        if (amount === null) {
            return;
        }
        await putJson(`/api/standards/retained/${encodeURIComponent(positionCode)}`, {
            positionCode,
            name,
            amount: Number(amount),
        });
        await loadRetainedAllowanceStandards();
    } catch (error) {
        showError(document.getElementById("retained-standards-status"), error);
    }
}

async function createRetainedAllowanceStandard() {
    const positionCode = prompt("职务编码：", "");
    if (positionCode === null) {
        return;
    }
    const name = prompt("名称：", "");
    if (name === null) {
        return;
    }
    const amount = prompt("金额：", "0");
    if (amount === null) {
        return;
    }
    try {
        await postJson("/api/standards/retained", {
            positionCode,
            name,
            amount: Number(amount),
        });
        await loadRetainedAllowanceStandards();
    } catch (error) {
        showError(document.getElementById("retained-standards-status"), error);
    }
}

async function deleteRetainedAllowanceStandard(positionCode) {
    if (!confirm(`确认删除职务 ${positionCode} 的保留福补标准？`)) {
        return;
    }
    try {
        await deleteJson(`/api/standards/retained/${encodeURIComponent(positionCode)}`);
        await loadRetainedAllowanceStandards();
    } catch (error) {
        showError(document.getElementById("retained-standards-status"), error);
    }
}

async function editYearAllowanceStandard(standardYearMonth) {
    try {
        const existing = (await getJson(`/api/payroll/year-allowance-standards?page=0&size=1000`)).content
            .find(row => row.standardYearMonth === standardYearMonth);
        if (!existing) {
            throw new Error("未找到该年补贴标准");
        }
        const categoryOneAmount = prompt("A1 金额：", existing.categoryOneAmount);
        if (categoryOneAmount === null) {
            return;
        }
        const categoryTwoAmount = prompt("A2 金额：", existing.categoryTwoAmount);
        if (categoryTwoAmount === null) {
            return;
        }
        const categoryThreeAmount = prompt("A3 金额：", existing.categoryThreeAmount);
        if (categoryThreeAmount === null) {
            return;
        }
        const categoryFourAmount = prompt("A4 金额：", existing.categoryFourAmount);
        if (categoryFourAmount === null) {
            return;
        }
        await putJson(`/api/standards/year-allowances/${encodeURIComponent(standardYearMonth)}`, {
            standardYearMonth,
            categoryOneAmount: Number(categoryOneAmount),
            categoryTwoAmount: Number(categoryTwoAmount),
            categoryThreeAmount: Number(categoryThreeAmount),
            categoryFourAmount: Number(categoryFourAmount),
        });
        await loadYearAllowanceStandards();
    } catch (error) {
        showError(document.getElementById("year-standards-status"), error);
    }
}

async function createYearAllowanceStandard() {
    const standardYearMonth = prompt("标准年月：", document.getElementById("year-standard-year-month").value.trim() || "202407");
    if (standardYearMonth === null) {
        return;
    }
    const categoryOneAmount = prompt("A1 金额：", "0");
    if (categoryOneAmount === null) {
        return;
    }
    const categoryTwoAmount = prompt("A2 金额：", "0");
    if (categoryTwoAmount === null) {
        return;
    }
    const categoryThreeAmount = prompt("A3 金额：", "0");
    if (categoryThreeAmount === null) {
        return;
    }
    const categoryFourAmount = prompt("A4 金额：", "0");
    if (categoryFourAmount === null) {
        return;
    }
    try {
        await postJson("/api/standards/year-allowances", {
            standardYearMonth,
            categoryOneAmount: Number(categoryOneAmount),
            categoryTwoAmount: Number(categoryTwoAmount),
            categoryThreeAmount: Number(categoryThreeAmount),
            categoryFourAmount: Number(categoryFourAmount),
        });
        await loadYearAllowanceStandards();
    } catch (error) {
        showError(document.getElementById("year-standards-status"), error);
    }
}

async function deleteYearAllowanceStandard(standardYearMonth) {
    if (!confirm(`确认删除 ${standardYearMonth} 的年补贴标准？`)) {
        return;
    }
    try {
        await deleteJson(`/api/standards/year-allowances/${encodeURIComponent(standardYearMonth)}`);
        await loadYearAllowanceStandards();
    } catch (error) {
        showError(document.getElementById("year-standards-status"), error);
    }
}

async function editPositionSalaryStandard(standardYearMonth, positionCode, currentAmount) {
    const amount = prompt("职务工资金额：", currentAmount);
    if (amount === null) {
        return;
    }
    try {
        await putJson(`/api/standards/position-salaries/${encodeURIComponent(standardYearMonth)}/${encodeURIComponent(positionCode)}`, {
            standardYearMonth,
            positionCode,
            amount: Number(amount),
        });
        await loadBasicStandards();
    } catch (error) {
        showError(document.getElementById("basic-standards-status"), error);
    }
}

async function createPositionSalaryStandard() {
    const standardYearMonth = prompt("标准年月：", document.getElementById("basic-standard-year-month").value.trim() || "202407");
    if (standardYearMonth === null) {
        return;
    }
    const positionCode = prompt("职务编码：", "");
    if (positionCode === null) {
        return;
    }
    const amount = prompt("金额：", "0");
    if (amount === null) {
        return;
    }
    try {
        await postJson("/api/standards/position-salaries", {
            standardYearMonth,
            positionCode,
            amount: Number(amount),
        });
        document.getElementById("basic-standard-type").value = "position";
        await refreshBasicStandardPeriods();
        document.getElementById("basic-standard-year-month").value = standardYearMonth;
        await refreshBasicStandardPositionCategories();
        const categoryEl = document.getElementById("basic-standard-position-category");
        if (categoryEl && positionCode.trim().length >= 2) {
            categoryEl.value = positionCode.trim().slice(0, 2);
        }
        await loadBasicStandards();
    } catch (error) {
        showError(document.getElementById("basic-standards-status"), error);
    }
}

async function deletePositionSalaryStandard(standardYearMonth, positionCode) {
    if (!confirm(`确认删除 ${standardYearMonth} / ${positionCode} 的职务工资标准？`)) {
        return;
    }
    try {
        await deleteJson(`/api/standards/position-salaries/${encodeURIComponent(standardYearMonth)}/${encodeURIComponent(positionCode)}`);
        await loadBasicStandards();
    } catch (error) {
        showError(document.getElementById("basic-standards-status"), error);
    }
}

function openGradeStandardModal(mode, kind, record = {}) {
    initGradeStandardStepsGrid();
    document.getElementById("grade-standard-mode").value = mode;
    document.getElementById("grade-standard-kind").value = kind;
    const isPositionGrade = kind === "position-grade";
    document.getElementById("grade-standard-modal-title").textContent = isPositionGrade ? "岗位档次工资标准" : "级别工资标准";
    document.getElementById("grade-standard-code-label").textContent = isPositionGrade ? "职务编码" : "级别";
    document.getElementById("grade-standard-jsdjgz-wrap").classList.toggle("hidden", !isPositionGrade);
    const yearInput = document.getElementById("grade-standard-year-month");
    const codeInput = document.getElementById("grade-standard-code");
    yearInput.value = record.standardYearMonth
        || document.getElementById("basic-standard-year-month").value.trim()
        || "202407";
    codeInput.value = record.code || "";
    yearInput.readOnly = mode === "edit";
    codeInput.readOnly = mode === "edit";
    document.getElementById("grade-standard-jsdjgz").value = record.technicalGradeSalary ?? 0;
    const steps = record.gradeSteps || Array(20).fill(0);
    document.querySelectorAll("#grade-standard-steps-grid [data-grade-step]").forEach(input => {
        input.value = steps[Number(input.dataset.gradeStep) - 1] ?? 0;
    });
    document.getElementById("grade-standard-status").textContent = "";
    document.getElementById("grade-standard-modal").classList.remove("hidden");
}

function closeGradeStandardModal() {
    document.getElementById("grade-standard-modal").classList.add("hidden");
}

async function onGradeStandardFormSubmit(event) {
    event.preventDefault();
    const mode = document.getElementById("grade-standard-mode").value;
    const kind = document.getElementById("grade-standard-kind").value;
    const standardYearMonth = document.getElementById("grade-standard-year-month").value.trim();
    const code = document.getElementById("grade-standard-code").value.trim();
    const gradeSteps = Array.from(document.querySelectorAll("#grade-standard-steps-grid [data-grade-step]"))
        .sort((left, right) => Number(left.dataset.gradeStep) - Number(right.dataset.gradeStep))
        .map(input => Number(input.value || 0));
    const status = document.getElementById("grade-standard-status");
    status.className = "status";
    status.textContent = "正在保存...";
    try {
        if (kind === "grade") {
            const payload = { standardYearMonth, gradeLevel: code, gradeSteps };
            if (mode === "create") {
                await postJson("/api/standards/grade-salaries", payload);
            } else {
                await putJson(`/api/standards/grade-salaries/${encodeURIComponent(standardYearMonth)}/${encodeURIComponent(code)}`, payload);
            }
        } else {
            const payload = {
                standardYearMonth,
                positionCode: code,
                technicalGradeSalary: Number(document.getElementById("grade-standard-jsdjgz").value || 0),
                gradeSteps,
            };
            if (mode === "create") {
                await postJson("/api/standards/position-grade-salaries", payload);
            } else {
                await putJson(`/api/standards/position-grade-salaries/${encodeURIComponent(standardYearMonth)}/${encodeURIComponent(code)}`, payload);
            }
        }
        closeGradeStandardModal();
        document.getElementById("basic-standard-type").value = kind;
        updateBasicStandardPositionCategoryVisibility();
        await refreshBasicStandardPeriods();
        document.getElementById("basic-standard-year-month").value = standardYearMonth;
        await refreshBasicStandardPositionCategories();
        const categoryEl = document.getElementById("basic-standard-position-category");
        if (categoryEl && kind === "position-grade" && code.length >= 2) {
            categoryEl.value = code.slice(0, 2);
        }
        await loadBasicStandards({ statusMessage: "保存成功" });
    } catch (error) {
        showError(status, error);
    }
}

async function deleteGradeSalaryStandard(standardYearMonth, gradeLevel) {
    if (!confirm(`确认删除 ${standardYearMonth} / ${gradeLevel} 的级别工资标准？`)) {
        return;
    }
    try {
        await deleteJson(`/api/standards/grade-salaries/${encodeURIComponent(standardYearMonth)}/${encodeURIComponent(gradeLevel)}`);
        await loadBasicStandards();
    } catch (error) {
        showError(document.getElementById("basic-standards-status"), error);
    }
}

async function deletePositionGradeSalaryStandard(standardYearMonth, positionCode) {
    if (!confirm(`确认删除 ${standardYearMonth} / ${positionCode} 的岗位档次工资标准？`)) {
        return;
    }
    try {
        await deleteJson(`/api/standards/position-grade-salaries/${encodeURIComponent(standardYearMonth)}/${encodeURIComponent(positionCode)}`);
        await loadBasicStandards();
    } catch (error) {
        showError(document.getElementById("basic-standards-status"), error);
    }
}

function openSalaryLevelStandardModal(mode, record = {}) {
    document.getElementById("salary-level-standard-mode").value = mode;
    const yearInput = document.getElementById("salary-level-standard-year-month");
    const jobCategoryInput = document.getElementById("salary-level-standard-job-category");
    const levelInput = document.getElementById("salary-level-standard-level");
    yearInput.value = record.standardYearMonth
        || document.getElementById("basic-standard-year-month").value.trim()
        || "202407";
    jobCategoryInput.value = record.jobCategoryCode || "";
    levelInput.value = record.salaryLevel || "";
    yearInput.readOnly = mode === "edit";
    jobCategoryInput.readOnly = mode === "edit";
    levelInput.readOnly = mode === "edit";
    document.getElementById("salary-level-standard-amount").value = record.amount ?? 0;
    document.getElementById("salary-level-standard-base-amount").value = record.baseAmount ?? 0;
    document.getElementById("salary-level-standard-base-extra").value = record.baseAmountExtra ?? 0;
    document.getElementById("salary-level-standard-status").textContent = "";
    document.getElementById("salary-level-standard-modal").classList.remove("hidden");
}

function closeSalaryLevelStandardModal() {
    document.getElementById("salary-level-standard-modal").classList.add("hidden");
}

async function onSalaryLevelStandardFormSubmit(event) {
    event.preventDefault();
    const mode = document.getElementById("salary-level-standard-mode").value;
    const standardYearMonth = document.getElementById("salary-level-standard-year-month").value.trim();
    const jobCategoryCode = document.getElementById("salary-level-standard-job-category").value.trim();
    const salaryLevel = document.getElementById("salary-level-standard-level").value.trim();
    const payload = {
        standardYearMonth,
        jobCategoryCode,
        salaryLevel,
        amount: Number(document.getElementById("salary-level-standard-amount").value || 0),
        baseAmount: Number(document.getElementById("salary-level-standard-base-amount").value || 0),
        baseAmountExtra: Number(document.getElementById("salary-level-standard-base-extra").value || 0),
    };
    const status = document.getElementById("salary-level-standard-status");
    status.className = "status";
    status.textContent = "正在保存...";
    try {
        if (mode === "create") {
            await postJson("/api/standards/salary-levels", payload);
        } else {
            await putJson(`/api/standards/salary-levels/${encodeURIComponent(standardYearMonth)}/${encodeURIComponent(jobCategoryCode)}/${encodeURIComponent(salaryLevel)}`, payload);
        }
        closeSalaryLevelStandardModal();
        document.getElementById("basic-standard-type").value = "salary-level";
        updateBasicStandardPositionCategoryVisibility();
        await refreshBasicStandardPeriods();
        document.getElementById("basic-standard-year-month").value = standardYearMonth;
        await refreshBasicStandardPositionCategories();
        await loadBasicStandards({ statusMessage: "保存成功" });
    } catch (error) {
        showError(status, error);
    }
}

async function deleteSalaryLevelStandard(standardYearMonth, jobCategoryCode, salaryLevel) {
    if (!confirm(`确认删除 ${standardYearMonth} / ${jobCategoryCode} / ${salaryLevel} 的薪级工资标准？`)) {
        return;
    }
    try {
        await deleteJson(`/api/standards/salary-levels/${encodeURIComponent(standardYearMonth)}/${encodeURIComponent(jobCategoryCode)}/${encodeURIComponent(salaryLevel)}`);
        await loadBasicStandards();
    } catch (error) {
        showError(document.getElementById("basic-standards-status"), error);
    }
}

function openInternSalaryStandardModal(mode, record = {}) {
    document.getElementById("intern-salary-standard-mode").value = mode;
    const yearInput = document.getElementById("intern-salary-standard-year-month");
    const educationCodeInput = document.getElementById("intern-salary-standard-education-code");
    const positionCodeInput = document.getElementById("intern-salary-standard-position-code");
    yearInput.value = record.standardYearMonth || document.getElementById("intern-standard-year-month").value.trim() || "201807";
    educationCodeInput.value = record.educationCode || "";
    document.getElementById("intern-salary-standard-education-name").value = record.educationName || "";
    positionCodeInput.value = record.regularPositionCode || "";
    document.getElementById("intern-salary-standard-position-name").value = record.regularPositionName || "";
    document.getElementById("intern-salary-standard-grade-step").value = record.regularGradeStep || "";
    document.getElementById("intern-salary-standard-level").value = record.regularLevel || "";
    document.getElementById("intern-salary-standard-first-year").value = record.firstYearAmount ?? 0;
    document.getElementById("intern-salary-standard-second-year").value = record.secondYearAmount ?? 0;
    yearInput.readOnly = mode === "edit";
    educationCodeInput.readOnly = mode === "edit";
    positionCodeInput.readOnly = mode === "edit";
    document.getElementById("intern-salary-standard-status").textContent = "";
    document.getElementById("intern-salary-standard-modal").classList.remove("hidden");
}

function closeInternSalaryStandardModal() {
    document.getElementById("intern-salary-standard-modal").classList.add("hidden");
}

async function onInternSalaryStandardFormSubmit(event) {
    event.preventDefault();
    const mode = document.getElementById("intern-salary-standard-mode").value;
    const payload = {
        standardYearMonth: document.getElementById("intern-salary-standard-year-month").value.trim(),
        educationCode: document.getElementById("intern-salary-standard-education-code").value.trim(),
        educationName: document.getElementById("intern-salary-standard-education-name").value.trim(),
        regularPositionCode: document.getElementById("intern-salary-standard-position-code").value.trim(),
        regularPositionName: document.getElementById("intern-salary-standard-position-name").value.trim(),
        regularGradeStep: document.getElementById("intern-salary-standard-grade-step").value.trim(),
        regularLevel: document.getElementById("intern-salary-standard-level").value.trim(),
        firstYearAmount: Number(document.getElementById("intern-salary-standard-first-year").value || 0),
        secondYearAmount: Number(document.getElementById("intern-salary-standard-second-year").value || 0),
    };
    const status = document.getElementById("intern-salary-standard-status");
    status.className = "status";
    status.textContent = "正在保存...";
    try {
        if (mode === "create") {
            await postJson("/api/standards/intern-salaries", payload);
        } else {
            await putJson(`/api/standards/intern-salaries/${encodeURIComponent(payload.standardYearMonth)}/${encodeURIComponent(payload.educationCode)}/${encodeURIComponent(payload.regularPositionCode)}`, payload);
        }
        closeInternSalaryStandardModal();
        await loadInternSalaryStandards({ statusMessage: "保存成功" });
    } catch (error) {
        showError(status, error);
    }
}

async function deleteInternSalaryStandard(standardYearMonth, educationCode, regularPositionCode) {
    if (!confirm(`确认删除 ${standardYearMonth} / ${educationCode} / ${regularPositionCode} 的见习工资标准？`)) {
        return;
    }
    try {
        await deleteJson(`/api/standards/intern-salaries/${encodeURIComponent(standardYearMonth)}/${encodeURIComponent(educationCode)}/${encodeURIComponent(regularPositionCode)}`);
        await loadInternSalaryStandards();
    } catch (error) {
        showError(document.getElementById("intern-standards-status"), error);
    }
}

function openWageReformStandardModal(mode, record = {}) {
    document.getElementById("wage-reform-standard-mode").value = mode;
    const positionInput = document.getElementById("wage-reform-standard-position-code");
    const appointmentLowerInput = document.getElementById("wage-reform-standard-appointment-lower");
    const appointmentUpperInput = document.getElementById("wage-reform-standard-appointment-upper");
    const reformLowerInput = document.getElementById("wage-reform-standard-reform-lower");
    const reformUpperInput = document.getElementById("wage-reform-standard-reform-upper");
    positionInput.value = record.positionCode
        || document.getElementById("wage-reform-position")?.value.trim()
        || document.getElementById("wage-reform-position-category")?.value.trim()
        || "";
    appointmentLowerInput.value = record.appointmentYearsLower ?? 0;
    appointmentUpperInput.value = record.appointmentYearsUpper ?? 0;
    reformLowerInput.value = record.reformYearsLower ?? 0;
    reformUpperInput.value = record.reformYearsUpper ?? 0;
    document.getElementById("wage-reform-standard-level").value = record.convertedLevel || "";
    document.getElementById("wage-reform-standard-step").value = record.convertedStep || "";
    const readOnly = mode === "edit";
    positionInput.readOnly = readOnly;
    appointmentLowerInput.readOnly = readOnly;
    appointmentUpperInput.readOnly = readOnly;
    reformLowerInput.readOnly = readOnly;
    reformUpperInput.readOnly = readOnly;
    document.getElementById("wage-reform-standard-status").textContent = "";
    document.getElementById("wage-reform-standard-modal").classList.remove("hidden");
}

function closeWageReformStandardModal() {
    document.getElementById("wage-reform-standard-modal").classList.add("hidden");
}

function wageReformStandardApiPath(record) {
    return `/api/standards/wage-reforms/${encodeURIComponent(record.positionCode)}/${record.appointmentYearsLower}/${record.appointmentYearsUpper}/${record.reformYearsLower}/${record.reformYearsUpper}`;
}

async function onWageReformStandardFormSubmit(event) {
    event.preventDefault();
    const mode = document.getElementById("wage-reform-standard-mode").value;
    const payload = {
        positionCode: document.getElementById("wage-reform-standard-position-code").value.trim(),
        appointmentYearsLower: Number(document.getElementById("wage-reform-standard-appointment-lower").value || 0),
        appointmentYearsUpper: Number(document.getElementById("wage-reform-standard-appointment-upper").value || 0),
        reformYearsLower: Number(document.getElementById("wage-reform-standard-reform-lower").value || 0),
        reformYearsUpper: Number(document.getElementById("wage-reform-standard-reform-upper").value || 0),
        convertedLevel: document.getElementById("wage-reform-standard-level").value.trim(),
        convertedStep: document.getElementById("wage-reform-standard-step").value.trim(),
    };
    const status = document.getElementById("wage-reform-standard-status");
    status.className = "status";
    status.textContent = "正在保存...";
    try {
        if (mode === "create") {
            await postJson("/api/standards/wage-reforms", payload);
        } else {
            await putJson(wageReformStandardApiPath(payload), payload);
        }
        closeWageReformStandardModal();
        await loadWageReformStandards({ statusMessage: "保存成功" });
    } catch (error) {
        showError(status, error);
    }
}

async function deleteWageReformStandard(record) {
    if (!confirm(`确认删除 ${record.positionCode} 的套改标准记录？`)) {
        return;
    }
    try {
        await deleteJson(wageReformStandardApiPath(record));
        await loadWageReformStandards();
    } catch (error) {
        showError(document.getElementById("wage-reform-standards-status"), error);
    }
}

function updateOtherAllowanceStandardModalFields() {
    const type = document.getElementById("other-allowance-standard-modal-type").value;
    const mode = document.getElementById("other-allowance-standard-mode").value;
    document.getElementById("other-allowance-standard-year-wrap").classList.toggle("hidden", type === "civilized");
    document.getElementById("other-allowance-standard-average-wrap").classList.toggle("hidden", type !== "assessment");
    document.getElementById("other-allowance-standard-multiplier-wrap").classList.toggle("hidden", type !== "civilized");
    document.getElementById("other-allowance-standard-name-wrap").classList.add("hidden");
    document.getElementById("other-allowance-standard-modal-type").disabled = mode === "edit";
}

function otherAllowanceStandardApiPath(standardType, standardYearMonth, code) {
    if (standardType === "civilized") {
        return `/api/standards/other-allowances/${standardType}/${encodeURIComponent(code)}`;
    }
    return `/api/standards/other-allowances/${standardType}/${encodeURIComponent(standardYearMonth)}/${encodeURIComponent(code)}`;
}

function openOtherAllowanceStandardModal(mode, record = {}) {
    document.getElementById("other-allowance-standard-mode").value = mode;
    const typeSelect = document.getElementById("other-allowance-standard-modal-type");
    typeSelect.value = record.standardType || document.getElementById("other-allowance-standard-type").value;
    document.getElementById("other-allowance-standard-year-month").value = record.standardYearMonth
        || document.getElementById("other-allowance-filter-year-month")?.value.trim()
        || "";
    const codeInput = document.getElementById("other-allowance-standard-code");
    codeInput.value = record.code || "";
    document.getElementById("other-allowance-standard-name").value = record.name || "";
    document.getElementById("other-allowance-standard-amount").value = record.amount ?? 0;
    document.getElementById("other-allowance-standard-average").value = record.averageAmount ?? 0;
    document.getElementById("other-allowance-standard-multiplier").value = record.multiplier ?? 0;
    codeInput.readOnly = mode === "edit";
    document.getElementById("other-allowance-standard-year-month").readOnly = mode === "edit";
    updateOtherAllowanceStandardModalFields();
    document.getElementById("other-allowance-standard-status").textContent = "";
    document.getElementById("other-allowance-standard-modal").classList.remove("hidden");
}

function closeOtherAllowanceStandardModal() {
    document.getElementById("other-allowance-standard-modal").classList.add("hidden");
}

async function onOtherAllowanceStandardFormSubmit(event) {
    event.preventDefault();
    const mode = document.getElementById("other-allowance-standard-mode").value;
    const standardType = document.getElementById("other-allowance-standard-modal-type").value;
    const payload = {
        standardType,
        standardYearMonth: document.getElementById("other-allowance-standard-year-month").value.trim(),
        code: document.getElementById("other-allowance-standard-code").value.trim(),
        name: document.getElementById("other-allowance-standard-name").value.trim(),
        amount: Number(document.getElementById("other-allowance-standard-amount").value || 0),
        averageAmount: Number(document.getElementById("other-allowance-standard-average").value || 0),
        multiplier: Number(document.getElementById("other-allowance-standard-multiplier").value || 0),
    };
    const status = document.getElementById("other-allowance-standard-status");
    status.className = "status";
    status.textContent = "正在保存...";
    try {
        if (mode === "create") {
            await postJson("/api/standards/other-allowances", payload);
        } else {
            await putJson(otherAllowanceStandardApiPath(standardType, payload.standardYearMonth, payload.code), payload);
        }
        closeOtherAllowanceStandardModal();
        document.getElementById("other-allowance-standard-type").value = standardType;
        await refreshOtherAllowanceStandardPeriods();
        if (payload.standardYearMonth) {
            const filterYear = document.getElementById("other-allowance-filter-year-month");
            if (filterYear && [...filterYear.options].some(opt => opt.value === payload.standardYearMonth)) {
                filterYear.value = payload.standardYearMonth;
            }
        }
        await loadOtherAllowanceStandards({ statusMessage: "保存成功" });
    } catch (error) {
        showError(status, error);
    }
}

async function deleteOtherAllowanceStandardRecord(record) {
    if (!confirm(`确认删除 ${otherAllowanceTypeName(record.standardType)} / ${record.code} 的标准？`)) {
        return;
    }
    try {
        await deleteJson(otherAllowanceStandardApiPath(record.standardType, record.standardYearMonth, record.code));
        await loadOtherAllowanceStandards();
    } catch (error) {
        showError(document.getElementById("other-allowance-status"), error);
    }
}

async function loadOrganizationMaintenance() {
    const keyword = document.getElementById("organization-maintenance-keyword").value.trim();
    const params = new URLSearchParams();
    if (keyword) {
        params.set("keyword", keyword);
    }
    const status = document.getElementById("organization-maintenance-status");
    const tree = document.getElementById("organization-maintenance-tree");
    status.className = "status";
    status.textContent = "正在加载单位树...";
    if (tree) {
        tree.innerHTML = "正在加载单位...";
    }
    try {
        await ensureOrganizationFieldOptions();
        const url = params.toString() ? `/api/organizations/tree?${params}` : "/api/organizations/tree";
        state.organizationMaintenanceNodes = await getJson(url);
        renderOrganizationMaintenanceTree();
        const selected = state.organizationMaintenanceSelectedCode;
        if (selected && (state.organizationMaintenanceNodes || []).some(node => node.code === selected)) {
            await selectOrganizationMaintenanceNode(selected, { reloadDetail: true });
        } else {
            clearOrganizationDetailForm({ keepSelection: false });
        }
        status.textContent = `共 ${(state.organizationMaintenanceNodes || []).length} 个单位`;
        updateOrgWriteUi();
    } catch (error) {
        showError(status, error);
    }
}

async function ensureOrganizationFieldOptions() {
    if (state.organizationFieldOptions) {
        applyOrganizationFieldOptions(state.organizationFieldOptions);
        return state.organizationFieldOptions;
    }
    const options = await getJson("/api/organizations/field-options");
    state.organizationFieldOptions = options;
    applyOrganizationFieldOptions(options);
    return options;
}

function applyOrganizationFieldOptions(options) {
    if (!options) {
        return;
    }
    fillOrganizationSelect("organization-modal-property", options.properties, { blankLabel: "请选择" });
    fillOrganizationSelect("organization-modal-category", options.categories, { blankLabel: "请选择" });
    fillOrganizationSelect("organization-modal-organization-level", options.organizationLevels, { blankLabel: "请选择" });
    fillOrganizationSelect("organization-modal-payroll-category", options.payrollCategories, { blankLabel: "请选择" });
    fillOrganizationSelect("organization-modal-allowance-standard", options.allowanceStandards, { blankLabel: "请选择" });
    fillOrganizationSelect("organization-modal-performance-enabled", options.performanceEnabled, { blankLabel: null });
    fillOrganizationSelect("organization-modal-performance-category", options.performanceCategories, { blankLabel: null });
    fillOrganizationSelect("organization-modal-year-allowance-category", options.yearAllowanceCategories, { blankLabel: null });
    fillOrganizationSelect("organization-modal-finance-source", options.financeSources, { blankLabel: "请选择" });
    fillOrganizationSelect("organization-modal-housing-fund", options.housingFundWithheld, { blankLabel: "请选择" });
    fillOrganizationSelect("organization-modal-pension", options.pensionWithheld, { blankLabel: "请选择" });
}

function fillOrganizationSelect(selectId, options, config = {}) {
    const select = document.getElementById(selectId);
    if (!select) {
        return;
    }
    const previous = select.value;
    const rows = Array.isArray(options) ? options : [];
    const parts = [];
    if (config.blankLabel != null) {
        parts.push(`<option value="">${escapeHtml(config.blankLabel)}</option>`);
    }
    rows.forEach(option => {
        const value = option?.value == null ? "" : String(option.value);
        const label = option?.label == null || option.label === "" ? value : String(option.label);
        parts.push(`<option value="${escapeHtml(value)}">${escapeHtml(label)}</option>`);
    });
    select.innerHTML = parts.join("");
    if (previous) {
        setOrganizationSelectValue(selectId, previous);
    }
}

function setOrganizationSelectValue(selectId, value) {
    const select = document.getElementById(selectId);
    if (!select) {
        return;
    }
    const text = value == null ? "" : String(value);
    if (text !== "" && ![...select.options].some(option => option.value === text)) {
        const option = document.createElement("option");
        option.value = text;
        option.textContent = text;
        select.appendChild(option);
    }
    select.value = text;
}

function renderOrganizationMaintenanceTree() {
    const container = document.getElementById("organization-maintenance-tree");
    if (!container) {
        return;
    }
    const filter = document.getElementById("organization-maintenance-keyword").value.trim().toLowerCase();
    const allNodes = state.organizationMaintenanceNodes || [];
    const childrenByParent = organizationChildrenByParent(allNodes);
    const roots = rootOrganizationNodes(allNodes);
    const visibleNodes = [];
    const appendVisibleNodes = (node, depth) => {
        const children = childrenByParent.get(node.code) || [];
        const descendantMatches = children.some(child => organizationNodeMatchesFilter(child, filter, childrenByParent));
        const selfMatches = organizationNodeTextMatches(node, filter);
        if (!filter || selfMatches || descendantMatches) {
            visibleNodes.push({ node, depth, hasChildren: children.length > 0 });
            const expanded = filter || state.organizationMaintenanceExpandedCodes.has(node.code);
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
    const selected = state.organizationMaintenanceSelectedCode;
    container.innerHTML = visibleNodes.map(({ node, depth, hasChildren }) => {
        const expanded = filter || state.organizationMaintenanceExpandedCodes.has(node.code);
        const active = node.code === selected ? " active" : "";
        return `
            <button type="button" class="dictionary-node organization-node${active} ${hasChildren ? "branch" : "leaf"}" style="--depth:${depth}" data-org-maint-code="${escapeHtml(node.code)}" data-has-children="${hasChildren}">
                <em>${hasChildren ? (expanded ? "▾" : "▸") : "•"}</em>
                <strong>${escapeHtml(node.name || node.shortName || "")}</strong>
                <span>${escapeHtml(node.code)}</span>
            </button>
        `;
    }).join("");
    container.querySelectorAll("[data-org-maint-code]").forEach(button => {
        button.addEventListener("click", event => {
            if (button.dataset.hasChildren === "true" && event.target.tagName === "EM") {
                toggleOrganizationMaintenanceNode(button.dataset.orgMaintCode);
                return;
            }
            void selectOrganizationMaintenanceNode(button.dataset.orgMaintCode || "");
        });
    });
}

function toggleOrganizationMaintenanceNode(code) {
    if (state.organizationMaintenanceExpandedCodes.has(code)) {
        state.organizationMaintenanceExpandedCodes.delete(code);
    } else {
        state.organizationMaintenanceExpandedCodes.add(code);
    }
    renderOrganizationMaintenanceTree();
}

async function selectOrganizationMaintenanceNode(code, options = {}) {
    const reloadDetail = options.reloadDetail !== false;
    state.organizationMaintenanceSelectedCode = code || "";
    renderOrganizationMaintenanceTree();
    if (!code) {
        clearOrganizationDetailForm({ keepSelection: false });
        return;
    }
    if (!reloadDetail) {
        return;
    }
    const status = document.getElementById("organization-maintenance-status");
    status.className = "status";
    status.textContent = "正在加载单位详情...";
    try {
        const record = await getJson(`/api/organizations/by-code/${encodeURIComponent(code)}`);
        fillOrganizationDetailForm(record, "edit");
        status.textContent = `已选中：${record.name || ""}（${record.organizationCode || code}）`;
        updateOrgWriteUi();
    } catch (error) {
        showError(status, error);
    }
}

function fillOrganizationDetailForm(record, mode = "edit") {
    document.getElementById("organization-detail-mode").value = mode;
    document.getElementById("organization-detail-id").value = record?.id ?? "";
    document.getElementById("organization-modal-code").value = record?.organizationCode || "";
    document.getElementById("organization-modal-name").value = record?.name || "";
    document.getElementById("organization-modal-short-name").value = record?.shortName || "";
    setOrganizationSelectValue("organization-modal-property", record?.property || "");
    setOrganizationSelectValue("organization-modal-category", record?.category || "");
    setOrganizationSelectValue("organization-modal-organization-level", record?.organizationLevel || "");
    document.getElementById("organization-modal-personnel-quota").value = record?.personnelQuota ?? 0;
    document.getElementById("organization-modal-establishment-count").value = record?.establishmentCount ?? 0;
    document.getElementById("organization-modal-actual-count").value = record?.actualCount ?? 0;
    document.getElementById("organization-detail-active-count").textContent =
        record?.activePersonnelCount == null ? "—" : String(record.activePersonnelCount);
    setOrganizationSelectValue("organization-modal-payroll-category", record?.payrollCategory || "");
    setOrganizationSelectValue("organization-modal-allowance-standard", record?.allowanceStandard || "");
    setOrganizationSelectValue("organization-modal-performance-enabled", record?.performanceAllowanceEnabled ?? 0);
    setOrganizationSelectValue("organization-modal-performance-category", record?.performanceCategory ?? 0);
    setOrganizationSelectValue("organization-modal-year-allowance-category", record?.yearAllowanceCategory ?? 0);
    setOrganizationSelectValue("organization-modal-finance-source", record?.financeSource || "");
    setOrganizationSelectValue("organization-modal-housing-fund", record?.housingFundWithheld || "");
    setOrganizationSelectValue("organization-modal-pension", record?.pensionWithheld || "");
    updateOrgWriteUi();
}

function clearOrganizationDetailForm(options = {}) {
    if (!options.keepSelection) {
        state.organizationMaintenanceSelectedCode = "";
        renderOrganizationMaintenanceTree();
    }
    document.getElementById("organization-detail-mode").value = "create";
    document.getElementById("organization-detail-id").value = "";
    document.getElementById("organization-modal-code").value = "";
    document.getElementById("organization-modal-name").value = "";
    document.getElementById("organization-modal-short-name").value = "";
    setOrganizationSelectValue("organization-modal-property", "");
    setOrganizationSelectValue("organization-modal-category", "");
    setOrganizationSelectValue("organization-modal-organization-level", "");
    document.getElementById("organization-modal-personnel-quota").value = "0";
    document.getElementById("organization-modal-establishment-count").value = "0";
    document.getElementById("organization-modal-actual-count").value = "0";
    document.getElementById("organization-detail-active-count").textContent = "—";
    setOrganizationSelectValue("organization-modal-payroll-category", "");
    setOrganizationSelectValue("organization-modal-allowance-standard", "");
    setOrganizationSelectValue("organization-modal-performance-enabled", "0");
    setOrganizationSelectValue("organization-modal-performance-category", "0");
    setOrganizationSelectValue("organization-modal-year-allowance-category", "0");
    setOrganizationSelectValue("organization-modal-finance-source", "");
    setOrganizationSelectValue("organization-modal-housing-fund", "");
    setOrganizationSelectValue("organization-modal-pension", "");
    updateOrgWriteUi();
}

function readOrganizationDetailPayload() {
    const intValue = (id, fallback = 0) => {
        const raw = document.getElementById(id).value;
        if (raw === "" || raw == null) {
            return fallback;
        }
        const value = Number(raw);
        return Number.isFinite(value) ? value : fallback;
    };
    return {
        organizationCode: document.getElementById("organization-modal-code").value.trim(),
        name: document.getElementById("organization-modal-name").value.trim(),
        shortName: document.getElementById("organization-modal-short-name").value.trim(),
        property: document.getElementById("organization-modal-property").value.trim(),
        category: document.getElementById("organization-modal-category").value.trim(),
        payrollCategory: document.getElementById("organization-modal-payroll-category").value.trim(),
        allowanceStandard: document.getElementById("organization-modal-allowance-standard").value.trim(),
        personnelQuota: intValue("organization-modal-personnel-quota"),
        establishmentCount: intValue("organization-modal-establishment-count"),
        actualCount: intValue("organization-modal-actual-count"),
        organizationLevel: document.getElementById("organization-modal-organization-level").value.trim(),
        performanceAllowanceEnabled: intValue("organization-modal-performance-enabled"),
        performanceCategory: intValue("organization-modal-performance-category"),
        yearAllowanceCategory: intValue("organization-modal-year-allowance-category"),
        financeSource: document.getElementById("organization-modal-finance-source").value.trim(),
        housingFundWithheld: document.getElementById("organization-modal-housing-fund").value.trim(),
        pensionWithheld: document.getElementById("organization-modal-pension").value.trim(),
    };
}

async function onOrganizationDetailSave(event) {
    event.preventDefault();
    if (!hasOrgWrite()) {
        return;
    }
    const status = document.getElementById("organization-maintenance-status");
    const mode = document.getElementById("organization-detail-mode").value || "view";
    const id = document.getElementById("organization-detail-id").value;
    const payload = readOrganizationDetailPayload();
    if (!payload.name) {
        status.className = "status error";
        status.textContent = "单位名称不能为空。";
        return;
    }
    if (mode === "create" && !payload.organizationCode) {
        status.className = "status error";
        status.textContent = "单位编码不能为空。";
        return;
    }
    status.className = "status";
    status.textContent = "正在保存单位...";
    try {
        let saved;
        if (mode === "create" || !id) {
            saved = await postJson("/api/organizations", payload);
        } else {
            const { organizationCode, ...updatePayload } = payload;
            saved = await putJson(`/api/organizations/${id}`, updatePayload);
        }
        state.organizationMaintenanceSelectedCode = saved.organizationCode || payload.organizationCode;
        await loadOrganizationMaintenance();
        status.textContent = `保存成功：${saved.name || ""}（${saved.organizationCode || ""}）`;
        showAppToast(status.textContent);
    } catch (error) {
        showError(status, error);
    }
}

async function onOrganizationAddRoot() {
    if (!hasOrgWrite()) {
        return;
    }
    const status = document.getElementById("organization-maintenance-status");
    status.className = "status";
    status.textContent = "正在生成一级单位编码...";
    try {
        const suggestion = await postJson("/api/organizations/next-root-code", {});
        clearOrganizationDetailForm({ keepSelection: false });
        document.getElementById("organization-detail-mode").value = "create";
        document.getElementById("organization-modal-code").value = suggestion.organizationCode || "";
        updateOrgWriteUi();
        document.getElementById("organization-modal-name").focus();
        status.textContent = `已预填一级单位编码 ${suggestion.organizationCode || ""}，请填写后保存。`;
    } catch (error) {
        showError(status, error);
    }
}

async function onOrganizationAddChild() {
    if (!hasOrgWrite()) {
        return;
    }
    const status = document.getElementById("organization-maintenance-status");
    const parentCode = state.organizationMaintenanceSelectedCode;
    if (!parentCode) {
        status.className = "status error";
        status.textContent = "请先在左侧选择上级单位。";
        return;
    }
    status.className = "status";
    status.textContent = "正在生成下辖单位编码...";
    try {
        const suggestion = await postJson(`/api/organizations/${encodeURIComponent(parentCode)}/next-child-code`, {});
        clearOrganizationDetailForm({ keepSelection: true });
        document.getElementById("organization-detail-mode").value = "create";
        document.getElementById("organization-modal-code").value = suggestion.organizationCode || "";
        updateOrgWriteUi();
        document.getElementById("organization-modal-name").focus();
        status.textContent = `已预填下辖单位编码 ${suggestion.organizationCode || ""}（上级 ${parentCode}），请填写后保存。`;
    } catch (error) {
        showError(status, error);
    }
}

async function onOrganizationDelete() {
    if (!hasOrgWrite()) {
        return;
    }
    const status = document.getElementById("organization-maintenance-status");
    const id = document.getElementById("organization-detail-id").value;
    const code = document.getElementById("organization-modal-code").value.trim();
    const name = document.getElementById("organization-modal-name").value.trim();
    if (!id) {
        status.className = "status error";
        status.textContent = "请先选择要删除的单位。";
        return;
    }
    if (!confirm(`确认删除单位 ${name || code}（${code}）？`)) {
        return;
    }
    status.className = "status";
    status.textContent = "正在删除单位...";
    try {
        await deleteJson(`/api/organizations/${id}`);
        state.organizationMaintenanceSelectedCode = "";
        clearOrganizationDetailForm({ keepSelection: false });
        await loadOrganizationMaintenance();
        status.textContent = `已删除单位 ${code}`;
    } catch (error) {
        showError(status, error);
    }
}

async function loadDictionaries(options = {}) {
    const prefix = document.getElementById("dictionary-category")?.value.trim() || "";
    const params = new URLSearchParams();
    if (prefix) {
        params.set("prefix", prefix);
    }
    const status = document.getElementById("dictionary-status");
    const rows = document.getElementById("dictionary-rows");
    status.className = "status";
    status.textContent = "正在查询字典...";
    rows.innerHTML = "";
    updateDictionaryWriteUi();
    if (!prefix) {
        state.dictionaryRows = [];
        status.textContent = "请选择分类";
        return;
    }

    try {
        const result = await getJson(`/api/dictionaries?${params}`);
        const content = result.content || [];
        state.dictionaryRows = content;
        rows.innerHTML = content.map(row => `
            <tr>
                <td>${escapeHtml(row.code)}</td>
                <td>${escapeHtml(row.name)}</td>
                <td>${escapeHtml(row.parentCode)}</td>
                <td>${escapeHtml(row.systemFlag)}</td>
                <td>${escapeHtml(row.enabledFlag)}</td>
                ${hasSystemConfigWrite() ? `<td><button class="row-action" type="button" data-dict-edit="${escapeHtml(row.code)}">编辑</button></td>` : ""}
            </tr>
        `).join("");
        rows.querySelectorAll("button[data-dict-edit]").forEach(button => {
            button.addEventListener("click", () => editDictionaryEntry(button.dataset.dictEdit));
        });
        if (options.statusMessage) {
            showSuccess(status, options.statusMessage);
        } else {
            const total = result.totalElements ?? content.length;
            status.textContent = content.length < total
                ? `共 ${total} 条字典，当前显示 ${content.length} 条`
                : `共 ${total} 条字典`;
        }
    } catch (error) {
        state.dictionaryRows = [];
        showError(status, error);
    }
}

async function loadLocalPolicies(options = {}) {
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
        const [policies, optionsData] = await Promise.all([
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
                ${hasSystemConfigWrite() ? `<td><button class="row-action" type="button" data-policy-edit="${row.id}">编辑</button></td>` : ""}
            </tr>
        `).join("");
        rows.querySelectorAll("button[data-policy-edit]").forEach(button => {
            button.addEventListener("click", () => editLocalPolicy(Number(button.dataset.policyEdit)));
        });
        optionRows.innerHTML = (optionsData || []).map(row => `
            <tr>
                <td>${escapeHtml(row.enterpriseTransferRaise)}</td>
                <td>${escapeHtml(row.gradeStepEducationLink)}</td>
                <td>${escapeHtml(row.decimalPlaces)}</td>
                <td>${escapeHtml(row.policeRankAllowance)}</td>
                <td>${escapeHtml(row.reformBonusBalance)}</td>
                <td>${escapeHtml(row.floatingSalary)}</td>
                ${hasSystemConfigWrite() ? `<td><button class="row-action" type="button" id="system-options-edit">编辑</button></td>` : ""}
            </tr>
        `).join("");
        document.getElementById("system-options-edit")?.addEventListener("click", () => editSystemOptions(optionsData?.[0]));
        await loadPayrollFieldConfig({ silent: true });
        if (options.statusMessage) {
            showSuccess(status, options.statusMessage);
        } else {
            status.textContent = `第 ${policies.page + 1} / ${Math.max(policies.totalPages, 1)} 页，共 ${policies.totalElements} 条政策配置`;
        }
    } catch (error) {
        showError(status, error);
    }
}

function isPayrollFieldEnabled(field) {
    const flag = field?.enabledIn2006Policy;
    return flag === "√" || flag === true || String(flag || "").trim() === "√";
}

function renderPayrollFieldConfigRows(fields) {
    const rows = document.getElementById("payroll-field-config-rows");
    const actions = document.getElementById("payroll-field-config-actions");
    if (!rows) {
        return;
    }
    const canWrite = hasSystemConfigWrite();
    rows.innerHTML = (fields || []).map(field => `
        <tr>
            <td>${escapeHtml(field.sequence ?? "")}</td>
            <td>${escapeHtml(field.fieldName || "")}</td>
            <td>${escapeHtml(field.caption || field.shortCaption || field.simpleCaption || "")}</td>
            <td>${field.counted ? "是" : "否"}</td>
            <td>
                <input type="checkbox"
                    data-field-id="${escapeHtml(field.id)}"
                    ${isPayrollFieldEnabled(field) ? "checked" : ""}
                    ${canWrite ? "" : "disabled"}>
            </td>
        </tr>
    `).join("") || `<tr><td colspan="5">暂无数值型工资项目</td></tr>`;
    if (actions) {
        actions.innerHTML = canWrite
            ? `<button type="button" id="payroll-field-config-save">保存工资项目</button>`
            : "";
        document.getElementById("payroll-field-config-save")?.addEventListener("click", savePayrollFieldConfig);
    }
}

async function loadPayrollFieldConfig(options = {}) {
    const status = document.getElementById("payroll-field-config-status");
    const rows = document.getElementById("payroll-field-config-rows");
    if (!rows) {
        return;
    }
    if (status && !options.silent) {
        status.className = "status";
        status.textContent = "正在加载工资项目...";
    }
    try {
        const fields = await getJson("/api/payroll/field-config");
        renderPayrollFieldConfigRows(fields || []);
        if (status) {
            if (options.statusMessage) {
                showSuccess(status, options.statusMessage);
            } else if (!options.silent) {
                status.textContent = `共 ${(fields || []).length} 个数值型工资项目`;
            } else {
                status.textContent = "";
            }
        }
    } catch (error) {
        if (status) {
            showError(status, error);
        }
    }
}

async function savePayrollFieldConfig() {
    const status = document.getElementById("payroll-field-config-status");
    if (!hasSystemConfigWrite()) {
        if (status) {
            status.className = "status error";
            status.textContent = "无 SYSTEM_CONFIG 权限，无法保存。";
        }
        return;
    }
    const enabledIds = Array.from(document.querySelectorAll("#payroll-field-config-rows input[type='checkbox']:checked"))
        .map(input => Number(input.dataset.fieldId))
        .filter(id => Number.isFinite(id));
    if (status) {
        status.className = "status";
        status.textContent = "正在保存工资项目勾选...";
    }
    try {
        const fields = await putJson("/api/payroll/field-config", { enabledIds });
        renderPayrollFieldConfigRows(fields || []);
        if (status) {
            showSuccess(status, `工资项目已保存，已启用 ${enabledIds.length} 项`);
        }
    } catch (error) {
        if (status) {
            showError(status, error);
        }
    }
}

const payrollPreviewPanelIds = {
    person: "selected-person",
    period: "preview-period",
    total: "preview-total",
    storedTotal: "stored-total",
    totalDifference: "total-difference",
    componentRows: "component-rows",
};

const payrollPreviewModalIds = {
    person: "payroll-preview-modal-person",
    period: "payroll-preview-modal-period",
    total: "payroll-preview-modal-total",
    storedTotal: "payroll-preview-modal-stored-total",
    totalDifference: "payroll-preview-modal-total-difference",
    componentRows: "payroll-preview-modal-component-rows",
};

function renderPayrollPreview(preview, ids) {
    document.getElementById(ids.person).textContent =
        `${preview.name} / ${preview.organizationCode}-${preview.personCode}`;
    document.getElementById(ids.period).textContent = preview.calculationPeriod || "-";
    document.getElementById(ids.total).textContent = money(preview.recalculatedKnownTotal);
    document.getElementById(ids.storedTotal).textContent = money(preview.storedTotal);
    const diff = document.getElementById(ids.totalDifference);
    diff.textContent = money(preview.totalDifference);
    diff.className = Number(preview.totalDifference) === 0 ? "difference-ok" : "difference-bad";

    const salaryItems = preview.salaryItems && preview.salaryItems.length
        ? preview.salaryItems
        : preview.calculatedComponents;
    document.getElementById(ids.componentRows).innerHTML = salaryItems.map(component => `
        <tr>
            <td>${escapeHtml(component.fieldName)}</td>
            <td>${escapeHtml(component.caption)}</td>
            <td>${money(component.amount)}</td>
        </tr>
    `).join("");
}

async function openPayrollPreviewModal(uid) {
    const modal = document.getElementById("payroll-preview-modal");
    const status = document.getElementById("payroll-preview-modal-status");
    const content = document.getElementById("payroll-preview-modal-content");
    const stepsContainer = document.getElementById("payroll-preview-modal-steps");
    const projectionSummary = document.getElementById("payroll-preview-modal-projection-summary");
    modal.classList.remove("hidden");
    status.className = "status";
    status.textContent = "正在加载工资试算...";
    content.classList.add("hidden");
    if (projectionSummary) {
        projectionSummary.classList.add("hidden");
    }

    try {
        const preview = await getJson(`/api/payroll/personnel/${uid}/calculation-preview`);
        state.selectedPersonnel = preview;
        renderPayrollPreview(preview, payrollPreviewModalIds);

        let projection = null;
        let projectionError = null;
        try {
            projection = await getJson(`/api/payroll/personnel/${uid}/wage-projection`);
        } catch (error) {
            projectionError = error;
        }
        const steps = projection?.stepDetails || [];
        const explanationLines = projection?.explanationLines || [];
        renderPayrollPreviewProjectionSummary(preview, steps);
        if (steps.length) {
            renderWageProjectionSteps(steps, stepsContainer);
        } else if (projectionError) {
            renderWageProjectionSteps([], stepsContainer, {
                reason: `逐年推算失败：${projectionError.message || projectionError}`,
            });
        } else if (explanationLines.length) {
            renderWageProjectionSteps([], stepsContainer, {
                reason: "逐年推算不合格或未能生成分步明细：",
                explanationLines,
            });
        } else {
            renderWageProjectionSteps([], stepsContainer, {
                reason: "暂无分步明细（接口未返回推算步骤或说明）。",
            });
        }

        status.textContent = projectionError
            ? "工资试算加载完成（逐年推算失败，见下方原因）"
            : "工资试算加载完成";
        content.classList.remove("hidden");
    } catch (error) {
        showError(status, error);
    }
}

function renderPayrollPreviewProjectionSummary(preview, steps) {
    const summary = document.getElementById("payroll-preview-modal-projection-summary");
    if (!summary) {
        return;
    }
    const list = Array.isArray(steps) ? steps : [];
    if (!list.length) {
        summary.classList.add("hidden");
        return;
    }
    const lastStep = list[list.length - 1] || {};
    const projectionTotal = Number(lastStep.total);
    const storedTotal = preview?.storedTotal == null
        ? Number(preview?.recalculatedKnownTotal)
        : Number(preview.storedTotal);
    const difference = Number.isFinite(projectionTotal) && Number.isFinite(storedTotal)
        ? projectionTotal - storedTotal
        : null;
    document.getElementById("payroll-preview-modal-projection-total").textContent = money(projectionTotal);
    document.getElementById("payroll-preview-modal-projection-stored").textContent = money(storedTotal);
    const diffEl = document.getElementById("payroll-preview-modal-projection-difference");
    diffEl.textContent = money(difference);
    diffEl.className = Number(difference) === 0 ? "difference-ok" : "difference-bad";
    summary.classList.remove("hidden");
}

function closePayrollPreviewModal() {
    document.getElementById("payroll-preview-modal").classList.add("hidden");
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
        renderPayrollPreview(preview, payrollPreviewPanelIds);
        status.textContent = "工资试算加载完成";
        content.classList.remove("hidden");
    } catch (error) {
        empty.classList.remove("hidden");
        showError(status, error);
    }
}

async function loadAudit() {
    await loadAuditPersonnel();
}

function auditSelectedUidList() {
    return Object.keys(state.auditSelectedUids || {}).filter(uid => state.auditSelectedUids[uid]);
}

function updateAuditSelectedButtonLabel() {
    const button = document.getElementById("audit-run-selected");
    if (button) {
        button.textContent = `对账勾选(${auditSelectedUidList().length})`;
    }
}

function onAuditSelectAllChanged(event) {
    const checked = Boolean(event.target.checked);
    (state.auditPersonnelRows || []).forEach(row => {
        if (row?.uid != null) {
            state.auditSelectedUids[String(row.uid)] = checked;
        }
    });
    renderAuditRows();
    updateAuditSelectedButtonLabel();
}

function onAuditRowSelectionChanged(uid, checked) {
    if (uid == null) {
        return;
    }
    state.auditSelectedUids[String(uid)] = checked;
    const selectAll = document.getElementById("audit-select-all");
    const rows = state.auditPersonnelRows || [];
    if (selectAll) {
        selectAll.checked = rows.length > 0 && rows.every(row => state.auditSelectedUids[String(row.uid)]);
    }
    updateAuditSelectedButtonLabel();
}

function mergeAuditResults(audits) {
    const merged = { ...(state.auditResultsByUid || {}) };
    (audits || []).forEach(item => {
        if (item?.uid != null) {
            merged[String(item.uid)] = item;
        }
    });
    state.auditResultsByUid = merged;
}

function renderAuditSummaryStats(summary) {
    document.getElementById("audit-total").textContent = summary?.totalPersonnelWithHistory ?? state.auditPersonnelMeta?.totalElements ?? "-";
    document.getElementById("audit-compared").textContent = summary?.comparedPersonnel ?? "-";
    document.getElementById("audit-difference-count").textContent = summary?.latestDifferenceCount ?? "-";
    document.getElementById("audit-history-person-count").textContent = summary?.historyMismatchPersonCount ?? "-";
    document.getElementById("audit-history-record-count").textContent = summary?.totalHistoryRecordMismatches ?? "-";
    document.getElementById("audit-max-difference").textContent = summary ? money(summary.maxAbsoluteDifference) : "-";
}

function auditRowDisplayData(row) {
    const audit = state.auditResultsByUid?.[String(row.uid)];
    return {
        uid: row.uid,
        personCode: row.personCode,
        name: row.name,
        latestPeriod: audit?.latestPeriod || row.latestPeriod || "-",
        storedTotal: audit?.storedTotal ?? row.storedTotal,
        projectedTotal: audit?.projectedTotal,
        latestProjectionEligible: audit?.latestProjectionEligible,
        latestTotalDifference: audit?.latestTotalDifference,
        latestMatched: audit?.latestMatched,
        latestNote: audit?.latestNote,
        historyMismatchCount: audit?.historyMismatchCount,
        historyRecordCount: audit?.historyRecordCount,
        audited: Boolean(audit),
        audit,
    };
}

function shouldShowAuditRow(display) {
    const mismatchOnly = document.getElementById("audit-show-mismatch-only")?.checked;
    if (!mismatchOnly) {
        return true;
    }
    if (!display.audited) {
        return false;
    }
    return !display.latestMatched || (display.historyMismatchCount || 0) > 0;
}

function renderAuditRows() {
    const rows = document.getElementById("audit-rows");
    if (!rows) {
        return;
    }
    const pageRows = (state.auditPersonnelRows || []).map(auditRowDisplayData).filter(shouldShowAuditRow);
    rows.innerHTML = pageRows.map(display => `
        <tr>
            <td class="col-select">
                <input type="checkbox" data-audit-select="${display.uid}" ${state.auditSelectedUids[String(display.uid)] ? "checked" : ""}>
            </td>
            <td>${escapeHtml(display.uid)}</td>
            <td>${escapeHtml(display.personCode || "-")}</td>
            <td>${escapeHtml(display.name || "-")}</td>
            <td>${escapeHtml(formatProjectionPeriod(display.latestPeriod))}</td>
            <td>${money(display.storedTotal)}</td>
            <td>${display.audited && display.latestProjectionEligible ? money(display.projectedTotal) : "-"}</td>
            <td class="${display.audited && Number(display.latestTotalDifference || 0) === 0 && display.latestMatched ? "difference-ok" : display.audited ? "difference-bad" : ""}">${display.audited ? (display.latestProjectionEligible ? money(display.latestTotalDifference) : escapeHtml(display.latestNote || "不可推算")) : "-"}</td>
            <td>${display.audited ? `${escapeHtml(display.historyMismatchCount)} / ${escapeHtml(display.historyRecordCount)}` : "-"}</td>
            <td>${display.audited ? formatProjectionAuditSummary(display.audit) : "-"}</td>
            <td>${display.audited ? `<button class="row-action" type="button" data-audit-detail="${display.uid}" data-audit-name="${display.name || ""}">查看</button>` : "-"}</td>
        </tr>
    `).join("");
    rows.querySelectorAll("[data-audit-select]").forEach(checkbox => {
        checkbox.addEventListener("change", () => onAuditRowSelectionChanged(checkbox.dataset.auditSelect, checkbox.checked));
    });
    rows.querySelectorAll("[data-audit-detail]").forEach(button => {
        button.addEventListener("click", () => openAuditDetail(button.dataset.auditDetail, button.dataset.auditName));
    });
    const selectAll = document.getElementById("audit-select-all");
    const sourceRows = state.auditPersonnelRows || [];
    if (selectAll) {
        selectAll.checked = sourceRows.length > 0 && sourceRows.every(row => state.auditSelectedUids[String(row.uid)]);
    }
}

async function loadAuditPersonnel() {
    const organizationCode = selectedOrganizationCode("audit-organization-code");
    const keyword = document.getElementById("audit-keyword")?.value?.trim() || "";
    const page = document.getElementById("audit-page").value || "0";
    const size = document.getElementById("audit-size").value || "20";
    const params = new URLSearchParams({ page, size });
    if (organizationCode) {
        params.set("organizationCode", organizationCode);
    }
    if (keyword) {
        params.set("keyword", keyword);
    }

    const status = document.getElementById("audit-status");
    status.className = "status";
    status.textContent = "正在查询人员列表...";
    closeAuditDetail();

    try {
        const pageData = await getJson(`/api/payroll/projection-audit-personnel?${params}`);
        state.auditPersonnelRows = pageData.content || [];
        state.auditPersonnelMeta = pageData;
        renderAuditSummaryStats({ totalPersonnelWithHistory: pageData.totalElements });
        document.getElementById("audit-compared").textContent = "-";
        document.getElementById("audit-difference-count").textContent = "-";
        document.getElementById("audit-history-person-count").textContent = "-";
        document.getElementById("audit-history-record-count").textContent = "-";
        document.getElementById("audit-max-difference").textContent = "-";
        renderAuditRows();
        updateAuditSelectedButtonLabel();
        status.textContent = `已加载 ${state.auditPersonnelRows.length} 人（共 ${pageData.totalElements || 0} 人），请勾选后执行对账。`;
    } catch (error) {
        showError(status, error);
    }
}

async function runAuditForSelected() {
    const uids = auditSelectedUidList().map(uid => Number.parseInt(uid, 10)).filter(Number.isFinite);
    if (!uids.length) {
        const status = document.getElementById("audit-status");
        status.className = "status error";
        status.textContent = "请先勾选需要推算对账的人员。";
        return;
    }
    await runAuditForUids(uids);
}

async function runAuditForCurrentPage() {
    const uids = (state.auditPersonnelRows || []).map(row => row.uid).filter(uid => uid != null);
    if (!uids.length) {
        const status = document.getElementById("audit-status");
        status.className = "status error";
        status.textContent = "请先查询人员。";
        return;
    }
    uids.forEach(uid => {
        state.auditSelectedUids[String(uid)] = true;
    });
    updateAuditSelectedButtonLabel();
    await runAuditForUids(uids);
}

async function runAuditForUids(uids) {
    const status = document.getElementById("audit-status");
    status.className = "status";
    status.textContent = `正在对 ${uids.length} 人执行推算对账，请稍候...`;
    closeAuditDetail();

    try {
        const summary = await postJson("/api/payroll/projection-audit-run", { uids });
        state.auditSummaryCache = summary;
        mergeAuditResults(summary.audits || summary.differences || []);
        renderAuditSummaryStats(summary);
        renderAuditRows();
        updateAuditSelectedButtonLabel();
        status.textContent = `已对账 ${summary.comparedPersonnel} 人，当前工资差异 ${summary.latestDifferenceCount} 人，历次调资差异 ${summary.totalHistoryRecordMismatches} 条`;
    } catch (error) {
        showError(status, error);
    }
}

function formatProjectionAuditSummary(item) {
    const parts = [];
    if (!item.latestMatched) {
        parts.push("当前工资不一致");
    }
    if ((item.historyMismatchCount || 0) > 0) {
        const first = (item.historyMismatches || [])[0];
        if (first) {
            parts.push(`${first.calculationPeriod}${first.changeType ? `(${first.changeType})` : ""}`);
        }
    }
    return escapeHtml(parts.join("；") || "一致");
}

function renderAuditDetailItems(mismatches, { stepDetailsPending = false } = {}) {
    const items = document.getElementById("audit-detail-items");
    if (!mismatches.length) {
        items.innerHTML = "<p class=\"projection-step-description\">未发现差异记录。</p>";
        return;
    }
    items.innerHTML = mismatches.map((item, index) => `
        <article class="audit-detail-item">
            <div class="audit-detail-summary">
                <p><strong>调资年月：</strong>${escapeHtml(formatProjectionPeriod(item.calculationPeriod))}
                    <strong>变动类别：</strong>${escapeHtml(item.changeType || "-")}
                    <strong>可推算：</strong>${item.projectionEligible ? "是" : "否"}</p>
                <p><strong>库中合计：</strong>${money(item.storedTotal)}
                    <strong>推算合计：</strong>${item.projectionEligible ? money(item.projectedTotal) : "-"}
                    <strong>差额：</strong><span class="${Number(item.totalDifference || 0) === 0 ? "difference-ok" : "difference-bad"}">${item.projectionEligible ? money(item.totalDifference) : escapeHtml(item.note || "-")}</span></p>
                <p><strong>结构差异：</strong>${escapeHtml((item.structureMismatches || []).join("；") || "-")}</p>
                <p><strong>金额差异：</strong>${(item.componentDifferences || []).map(diff =>
                    `${escapeHtml(diff.caption)}：库 ${money(diff.storedAmount)} / 算 ${money(diff.calculatedAmount)} / 差 ${money(diff.difference)}`
                ).join("；") || "-"}</p>
            </div>
            <h4 class="audit-detail-steps-title">分步工资明细</h4>
            <div id="audit-projection-steps-${index}" class="projection-steps">${stepDetailsPending ? "<p class=\"projection-step-description\">正在加载分步明细...</p>" : ""}</div>
        </article>
    `).join("");
    if (!stepDetailsPending) {
        mismatches.forEach((item, index) => {
            const stepContainer = document.getElementById(`audit-projection-steps-${index}`);
            renderWageProjectionSteps(item.stepDetails || [], stepContainer);
        });
    }
}

function renderAuditDetailStepDetails(mismatches) {
    mismatches.forEach((item, index) => {
        const stepContainer = document.getElementById(`audit-projection-steps-${index}`);
        if (stepContainer) {
            renderWageProjectionSteps(item.stepDetails || [], stepContainer);
        }
    });
}

async function openAuditDetail(uid, name) {
    const panel = document.getElementById("audit-detail-panel");
    const items = document.getElementById("audit-detail-items");
    const status = document.getElementById("audit-detail-status");
    document.getElementById("audit-detail-title").textContent = `${name || ""} 历次调资差异明细`;
    panel.classList.remove("hidden");
    items.innerHTML = "";
    status.className = "status";

    const cachedPerson = (state.auditSummaryCache?.audits || state.auditSummaryCache?.differences || [])
        .find(item => String(item.uid) === String(uid))
        || state.auditResultsByUid?.[String(uid)];
    const cachedMismatches = (cachedPerson?.historyMismatches || []).filter(item => !item.matched);
    if (cachedPerson) {
        status.textContent = cachedMismatches.length
            ? `共 ${cachedMismatches.length} 条调资记录存在差异，正在加载分步明细...`
            : "该人员历次调资与推算结果一致。";
        renderAuditDetailItems(cachedMismatches, { stepDetailsPending: cachedMismatches.length > 0 });
        if (!cachedMismatches.length) {
            return;
        }
    } else {
        status.textContent = "正在加载对账结果与分步工资明细，请稍候...";
    }

    try {
        const audits = await getJson(`/api/payroll/personnel/${uid}/projection-history-audits?includeStepDetails=true`);
        const mismatches = (audits || []).filter(item => !item.matched);
        status.textContent = mismatches.length
            ? `共 ${mismatches.length} 条调资记录存在差异，可展开查看推算分步明细。`
            : "该人员历次调资与推算结果一致。";
        if (!cachedPerson) {
            renderAuditDetailItems(mismatches);
            return;
        }
        if (!mismatches.length) {
            renderAuditDetailItems(mismatches);
            return;
        }
        if (cachedMismatches.length === mismatches.length) {
            const needsSummaryRefresh = cachedMismatches.some((item, index) => {
                const cachedDiffs = item.componentDifferences || [];
                const loadedDiffs = mismatches[index]?.componentDifferences || [];
                return cachedDiffs.length === 0 && loadedDiffs.length > 0;
            });
            if (needsSummaryRefresh) {
                renderAuditDetailItems(mismatches);
            } else {
                renderAuditDetailStepDetails(mismatches);
            }
            return;
        }
        renderAuditDetailItems(mismatches);
    } catch (error) {
        if (!cachedPerson) {
            status.textContent = "";
            showError(status, error);
            items.innerHTML = `<p class="projection-step-description">${escapeHtml(error.message || "加载失败")}</p>`;
            return;
        }
        status.textContent = "差异摘要已展示，分步明细加载失败，请稍后重试。";
        showError(status, error);
    }
}

function closeAuditDetail() {
    document.getElementById("audit-detail-panel").classList.add("hidden");
    document.getElementById("audit-detail-items").innerHTML = "";
    document.getElementById("audit-detail-status").textContent = "";
}

async function downloadProjectionAuditExport(format) {
    const organizationCode = selectedOrganizationCode("audit-organization-code");
    const keyword = document.getElementById("audit-keyword")?.value?.trim() || "";
    const mismatchesOnly = document.getElementById("audit-export-mismatches-only")?.checked;
    const params = new URLSearchParams();
    if (organizationCode) {
        params.set("organizationCode", organizationCode);
    }
    if (keyword) {
        params.set("keyword", keyword);
    }
    if (mismatchesOnly) {
        params.set("mismatchesOnly", "true");
    }
    const status = document.getElementById("audit-status");
    status.className = "status";
    status.textContent = keyword
        ? "正在按关键词筛选人员执行工资推算对账并生成文件，请勿关闭页面..."
        : "正在对全库人员执行工资推算对账并生成文件，人数较多时可能需要数十分钟，请勿关闭页面...";
    const suffix = format === "csv" ? "csv" : "xlsx";
    const url = `/api/payroll/projection-audit-export.${suffix}?${params}`;
    try {
        const response = await fetch(url, { credentials: "same-origin" });
        if (!response.ok) {
            const message = await response.text();
            throw new Error(message || `导出失败（${response.status}）`);
        }
        const blob = await response.blob();
        const disposition = response.headers.get("Content-Disposition") || "";
        const match = disposition.match(/filename="?([^";]+)"?/i);
        const filename = match ? match[1] : `projection_audit.${suffix === "csv" ? "zip" : "xlsx"}`;
        const link = document.createElement("a");
        link.href = URL.createObjectURL(blob);
        link.download = filename;
        link.click();
        URL.revokeObjectURL(link.href);
        status.textContent = `全库推算对账导出完成：${filename}`;
    } catch (error) {
        showError(status, error);
    }
}

async function onPayrollChangeRegisterReportSearch(event) {
    event.preventDefault();
    state.payrollChangeRegisterPage = 0;
    await loadPayrollChangeRegisterReport();
}

function gotoPayrollChangeRegisterPage(page) {
    const totalPages = Math.max(state.payrollChangeRegisterTotalPages || 1, 1);
    const target = Math.min(Math.max(page, 0), totalPages - 1);
    if (target === state.payrollChangeRegisterPage) {
        return;
    }
    state.payrollChangeRegisterPage = target;
    void loadPayrollChangeRegisterReport();
}

function renderPayrollChangeRegisterPagination(totalElements, totalPages) {
    const bar = document.getElementById("report-payroll-change-pagination");
    if (!bar) {
        return;
    }
    const pages = Math.max(totalPages || 1, 1);
    state.payrollChangeRegisterTotalPages = pages;
    state.payrollChangeRegisterTotalCount = totalElements || 0;
    const current = state.payrollChangeRegisterPage;
    bar.classList.toggle("hidden", !totalElements);
    const totalPagesEl = document.getElementById("report-payroll-change-total-pages");
    const totalCountEl = document.getElementById("report-payroll-change-page-total-count");
    const pageInput = document.getElementById("report-payroll-change-page-input");
    if (totalPagesEl) {
        totalPagesEl.textContent = String(pages);
    }
    if (totalCountEl) {
        totalCountEl.textContent = String(totalElements || 0);
    }
    if (pageInput) {
        pageInput.value = String(current + 1);
        pageInput.max = String(pages);
        pageInput.disabled = !totalElements;
    }
    const noData = !totalElements;
    document.getElementById("report-payroll-change-first").disabled = noData || current <= 0;
    document.getElementById("report-payroll-change-prev").disabled = noData || current <= 0;
    document.getElementById("report-payroll-change-next").disabled = noData || current >= pages - 1;
    document.getElementById("report-payroll-change-last").disabled = noData || current >= pages - 1;
}

function buildPayrollChangeRegisterSearchParams(page, size) {
    const organizationCode = selectedOrganizationCode("report-payroll-change-organization-code");
    const reportTypeCode = document.getElementById("report-payroll-change-type-select").value.trim();
    const year = document.getElementById("report-payroll-change-year").value.trim();
    const keyword = document.getElementById("report-payroll-change-keyword").value.trim();
    const params = new URLSearchParams({
        page: String(page || 0),
        size: String(size || 50),
    });
    if (organizationCode) {
        params.set("organizationCode", organizationCode);
    }
    if (reportTypeCode) {
        params.set("reportTypeCode", reportTypeCode);
    }
    if (year) {
        params.set("year", year);
    }
    if (keyword) {
        params.set("keyword", keyword);
    }
    return params;
}

async function loadPayrollChangeRegisterReport() {
    const size = document.getElementById("report-payroll-change-size").value || "50";
    const params = buildPayrollChangeRegisterSearchParams(state.payrollChangeRegisterPage || 0, size);
    const status = document.getElementById("report-payroll-change-status");
    const rows = document.getElementById("report-payroll-change-rows");
    status.className = "status";
    status.textContent = "正在加载工资变动花名册...";
    rows.innerHTML = "";
    document.getElementById("report-payroll-change-preview").classList.add("hidden");
    document.getElementById("report-payroll-change-preview").innerHTML = "";
    document.getElementById("report-payroll-change-select-all").checked = false;
    try {
        const result = await getJson(`/api/reports/payroll-change-candidates?${params}`);
        const totalElements = result.totalElements || 0;
        const totalPages = Math.max(result.totalPages || 1, 1);
        if (totalElements > 0 && (state.payrollChangeRegisterPage || 0) >= totalPages) {
            state.payrollChangeRegisterPage = Math.max(totalPages - 1, 0);
            return loadPayrollChangeRegisterReport();
        }
        state.payrollChangeRegisterPage = result.page || 0;
        state.payrollChangeRegisterCandidates = result.content || [];
        document.getElementById("report-payroll-change-total-count").textContent = totalElements;
        rows.innerHTML = (result.content || []).map(row => `
            <tr>
                <td><input type="checkbox" data-register-select value="${escapeHtml(row.payrollHistoryId)}"></td>
                <td>${escapeHtml(row.organizationCode)} ${escapeHtml(row.organizationName || "")}</td>
                <td>${escapeHtml(row.personCode)}</td>
                <td>${escapeHtml(row.name)}</td>
                <td>${escapeHtml(row.calculationYear)}${escapeHtml(row.calculationMonth)}</td>
                <td>${escapeHtml(row.changeType)}</td>
                <td>${escapeHtml(row.positionCode)} ${escapeHtml(row.positionName || "")}</td>
                <td>${escapeHtml(row.gradeLevel || "")}</td>
                <td>${escapeHtml(row.stepOrSalaryLevel || "")}</td>
                <td>${money(row.positionSalary)}</td>
                <td>${money(row.gradeSalary)}</td>
                <td>${money(row.technicalGradeSalary)}</td>
                <td>${money(row.performanceAllowance)}</td>
                <td>${money(row.retainedAllowance)}</td>
                <td>${money(row.rankAllowance)}</td>
                <td>${money(row.yearAllowance)}</td>
                <td>${money(row.pgbc)}</td>
                <td>${money(row.totalAmount)}</td>
            </tr>
        `).join("");
        renderPayrollChangeRegisterPagination(totalElements, totalPages);
        status.textContent = totalElements
            ? `第 ${state.payrollChangeRegisterPage + 1} / ${totalPages} 页，共 ${totalElements} 条`
            : "未找到符合条件的记录";
    } catch (error) {
        renderPayrollChangeRegisterPagination(0, 1);
        showError(status, error);
    }
}

async function generateAndPrintSelectedPayrollChangeRegister() {
    const selectedIds = Array.from(document.querySelectorAll("#report-payroll-change-rows [data-register-select]:checked"))
        .map(input => input.value)
        .filter(Boolean);
    const status = document.getElementById("report-payroll-change-status");
    if (selectedIds.length === 0) {
        status.className = "status error";
        status.textContent = "请先勾选需要打印的人员。";
        return;
    }
    await generateAndPrintPayrollChangeRegister(
        selectedIds,
        document.getElementById("payroll-change-register-print"));
}

async function fetchAllPayrollChangeRegisterIds(statusEl) {
    const pageSize = 200;
    const ids = [];
    let page = 0;
    let totalPages = 1;
    do {
        if (statusEl) {
            statusEl.className = "status";
            statusEl.textContent = `正在收集待打印记录... 第 ${page + 1} / ${Math.max(totalPages, 1)} 页`;
        }
        const result = await getJson(
            `/api/reports/payroll-change-candidates?${buildPayrollChangeRegisterSearchParams(page, pageSize)}`);
        totalPages = Math.max(result.totalPages || 1, 1);
        for (const row of result.content || []) {
            const id = String(row.payrollHistoryId || "").trim();
            if (id) {
                ids.push(id);
            }
        }
        if (!(result.content || []).length) {
            break;
        }
        page += 1;
    } while (page < totalPages);
    return ids;
}

async function generateAndPrintAllPayrollChangeRegister() {
    const status = document.getElementById("report-payroll-change-status");
    const triggerButton = document.getElementById("payroll-change-register-print-all");
    const knownTotal = state.payrollChangeRegisterTotalCount || 0;
    if (knownTotal <= 0) {
        status.className = "status error";
        status.textContent = "当前没有可打印的记录，请先查询。";
        return;
    }
    if (knownTotal > 20
        && !window.confirm(`将按当前筛选条件生成并打印全部 ${knownTotal} 人花名册，数量较大，确认继续？`)) {
        return;
    }
    const originalLabel = triggerButton?.textContent || "生成并打印全部";
    if (triggerButton) {
        triggerButton.disabled = true;
        triggerButton.textContent = "正在收集...";
    }
    try {
        const allIds = await fetchAllPayrollChangeRegisterIds(status);
        if (allIds.length === 0) {
            status.className = "status error";
            status.textContent = "当前筛选条件下没有可打印的记录。";
            return;
        }
        if (triggerButton) {
            triggerButton.textContent = `正在生成 ${allIds.length} 人...`;
        }
        await generateAndPrintPayrollChangeRegister(allIds, null);
    } catch (error) {
        showError(status, error);
    } finally {
        if (triggerButton) {
            triggerButton.disabled = false;
            triggerButton.textContent = originalLabel;
        }
    }
}

async function generateAndPrintPayrollChangeRegister(selectedIds, triggerButton) {
    const ids = (selectedIds || []).map(id => String(id || "").trim()).filter(Boolean);
    const status = document.getElementById("report-payroll-change-status");
    if (ids.length === 0) {
        status.className = "status error";
        status.textContent = "请先勾选需要打印的人员。";
        return;
    }
    const defaultLabel = triggerButton?.id === "payroll-change-register-print-all"
        ? "生成并打印全部"
        : "生成并打印花名册";
    const originalLabel = triggerButton?.textContent || defaultLabel;
    if (triggerButton) {
        triggerButton.disabled = true;
        triggerButton.textContent = `正在生成 ${ids.length} 人...`;
    }
    status.className = "status";
    status.textContent = `正在生成 ${ids.length} 人花名册 PDF...`;
    try {
        const startedAt = performance.now();
        const blob = await downloadPayrollChangeReportExport(
            "/api/reports/payroll-change-register/pdf",
            buildPayrollChangeExportRequest(ids, "report-payroll-change-type-select"),
            status);
        const elapsedMs = Math.max(1, Math.round(performance.now() - startedAt));
        status.className = "status success";
        status.textContent = `已生成 ${ids.length} 人花名册 PDF（${elapsedMs} ms），正在打开打印窗口...`;
        await openPdfBlobForPrint(blob);
        status.textContent = `已生成并送打 ${ids.length} 人花名册 PDF（${elapsedMs} ms）`;
    } catch (error) {
        showError(status, error);
    } finally {
        if (triggerButton) {
            triggerButton.disabled = false;
            triggerButton.textContent = originalLabel;
        }
    }
}

async function onPayrollChangeApprovalReportSearch(event) {
    event.preventDefault();
    state.payrollChangeApprovalPage = 0;
    await loadPayrollChangeApprovalReport();
}

function gotoPayrollChangeApprovalPage(page) {
    const totalPages = Math.max(state.payrollChangeApprovalTotalPages || 1, 1);
    const target = Math.min(Math.max(page, 0), totalPages - 1);
    if (target === state.payrollChangeApprovalPage) {
        return;
    }
    state.payrollChangeApprovalPage = target;
    void loadPayrollChangeApprovalReport();
}

function renderPayrollChangeApprovalPagination(totalElements, totalPages) {
    const bar = document.getElementById("report-approval-pagination");
    if (!bar) {
        return;
    }
    const pages = Math.max(totalPages || 1, 1);
    state.payrollChangeApprovalTotalPages = pages;
    state.payrollChangeApprovalTotalCount = totalElements || 0;
    const current = state.payrollChangeApprovalPage;
    bar.classList.toggle("hidden", !totalElements);
    const totalPagesEl = document.getElementById("report-approval-total-pages");
    const totalCountEl = document.getElementById("report-approval-page-total-count");
    const pageInput = document.getElementById("report-approval-page-input");
    if (totalPagesEl) {
        totalPagesEl.textContent = String(pages);
    }
    if (totalCountEl) {
        totalCountEl.textContent = String(totalElements || 0);
    }
    if (pageInput) {
        pageInput.value = String(current + 1);
        pageInput.max = String(pages);
        pageInput.disabled = !totalElements;
    }
    const noData = !totalElements;
    document.getElementById("report-approval-first").disabled = noData || current <= 0;
    document.getElementById("report-approval-prev").disabled = noData || current <= 0;
    document.getElementById("report-approval-next").disabled = noData || current >= pages - 1;
    document.getElementById("report-approval-last").disabled = noData || current >= pages - 1;
}

function buildPayrollChangeApprovalSearchParams(page, size) {
    const organizationCode = selectedOrganizationCode("report-approval-organization-code");
    const reportTypeCode = document.getElementById("report-approval-type-select").value.trim();
    const year = document.getElementById("report-approval-year").value.trim();
    const keyword = document.getElementById("report-approval-keyword").value.trim();
    const params = new URLSearchParams({
        page: String(page || 0),
        size: String(size || 50),
    });
    if (organizationCode) {
        params.set("organizationCode", organizationCode);
    }
    if (reportTypeCode) {
        params.set("reportTypeCode", reportTypeCode);
    }
    if (year) {
        params.set("year", year);
    }
    if (keyword) {
        params.set("keyword", keyword);
    }
    return params;
}

async function loadReportTypes() {
    const approvalSelect = document.getElementById("report-approval-type-select");
    const registerSelect = document.getElementById("report-payroll-change-type-select");
    if (!approvalSelect && !registerSelect) {
        return;
    }
    const approvalReady = approvalSelect && approvalSelect.options.length > 0 && approvalSelect.dataset.loaded === "1";
    const registerReady = registerSelect && registerSelect.options.length > 0 && registerSelect.dataset.loaded === "1";
    if ((!approvalSelect || approvalReady) && (!registerSelect || registerReady)) {
        return;
    }
    try {
        const [approvalResult, registerResult, allResult] = await Promise.all([
            getJson("/api/reports/types?category=" + encodeURIComponent("审批表")
                + "&reportType=" + encodeURIComponent("在职") + "&size=200"),
            getJson("/api/reports/types?category=" + encodeURIComponent("花名册") + "&size=200"),
            getJson("/api/reports/types?size=200"),
        ]);
        const allTypes = sortReportTypesByLbbm(allResult.content || []);
        const approvalTypes = sortReportTypesByLbbm((approvalResult.content || []).length
            ? approvalResult.content
            : allTypes.filter(type => isApprovalReportType(type) && String(type.reportType || "").trim() === "在职"));
        const registerTypes = sortReportTypesByLbbm((registerResult.content || []).length
            ? registerResult.content
            : allTypes.filter(type => isRegisterReportType(type)));
        if (approvalSelect) {
            fillReportTypeSelect(approvalSelect, approvalTypes, "工资变动审批表");
            approvalSelect.dataset.loaded = "1";
        }
        if (registerSelect) {
            fillReportTypeSelect(registerSelect, registerTypes, "工资变动花名册");
            registerSelect.dataset.loaded = "1";
        }
    } catch (error) {
        console.warn("加载报表类别失败", error);
        if (approvalSelect && approvalSelect.options.length === 0) {
            fillReportTypeSelect(approvalSelect, [], "工资变动审批表");
        }
        if (registerSelect && registerSelect.options.length === 0) {
            fillReportTypeSelect(registerSelect, [], "工资变动花名册");
        }
    }
}

function isApprovalReportType(type) {
    const text = [type.category, type.printCategory, type.name, type.title, type.code].join(" ");
    return /审批|spb/i.test(text);
}

function isRegisterReportType(type) {
    const text = [type.category, type.printCategory, type.name, type.title, type.code].join(" ");
    return /名册|hmc/i.test(text);
}

function sortReportTypesByLbbm(types) {
    return [...(types || [])].sort((a, b) => {
        const left = String(a?.code ?? "").trim();
        const right = String(b?.code ?? "").trim();
        const byCode = left.localeCompare(right, "zh-CN", { numeric: true, sensitivity: "base" });
        if (byCode !== 0) {
            return byCode;
        }
        return String(a?.name ?? "").localeCompare(String(b?.name ?? ""), "zh-CN");
    });
}

function fillReportTypeSelect(select, types, fallbackTitle) {
    const options = (types || []).map(type => `
            <option value="${escapeHtml(type.code || "")}" data-title="${escapeHtml(type.title || type.name || "")}">
                ${escapeHtml(type.name || type.title || type.code)}
            </option>
        `).join("");
    select.innerHTML = options || `<option value="">${escapeHtml(fallbackTitle)}</option>`;
}

async function loadPayrollChangeApprovalReport() {
    const size = document.getElementById("report-approval-size").value || "50";
    const params = buildPayrollChangeApprovalSearchParams(state.payrollChangeApprovalPage || 0, size);
    const status = document.getElementById("report-approval-status");
    const rows = document.getElementById("report-approval-select-rows");
    status.className = "status";
    status.textContent = "正在加载工资变动记录...";
    rows.innerHTML = "";
    document.getElementById("report-approval-preview").classList.add("hidden");
    document.getElementById("report-approval-preview").innerHTML = "";
    document.getElementById("report-approval-select-all").checked = false;
    try {
        const result = await getJson(`/api/reports/payroll-change-candidates?${params}`);
        const totalElements = result.totalElements || 0;
        const totalPages = Math.max(result.totalPages || 1, 1);
        if (totalElements > 0 && (state.payrollChangeApprovalPage || 0) >= totalPages) {
            state.payrollChangeApprovalPage = Math.max(totalPages - 1, 0);
            return loadPayrollChangeApprovalReport();
        }
        state.payrollChangeApprovalPage = result.page || 0;
        document.getElementById("report-approval-total-count").textContent = totalElements;
        rows.innerHTML = (result.content || []).map(row => `
            <tr>
                <td><input type="checkbox" data-approval-select value="${escapeHtml(row.payrollHistoryId)}"></td>
                <td>${escapeHtml(row.organizationCode)} ${escapeHtml(row.organizationName || "")}</td>
                <td>${escapeHtml(row.personCode)}</td>
                <td>${escapeHtml(row.name)}</td>
                <td>${escapeHtml(row.calculationYear)}${escapeHtml(row.calculationMonth)}</td>
                <td>${escapeHtml(row.changeType)}</td>
                <td>${escapeHtml([row.beforePositionCode, row.beforePositionName].filter(Boolean).join(" "))}</td>
                <td>${escapeHtml(row.positionCode)} ${escapeHtml(row.positionName || "")}</td>
                <td>${money(row.totalAmount)}</td>
                <td><button class="row-action" data-approval-generate="${escapeHtml(row.payrollHistoryId)}" type="button">生成并打印</button></td>
            </tr>
        `).join("");
        renderPayrollChangeApprovalPagination(totalElements, totalPages);
        status.textContent = totalElements
            ? `第 ${state.payrollChangeApprovalPage + 1} / ${totalPages} 页，共 ${totalElements} 条`
            : "未找到符合条件的记录";
    } catch (error) {
        renderPayrollChangeApprovalPagination(0, 1);
        showError(status, error);
    }
}

function onPayrollChangeApprovalRowClick(event) {
    const button = event.target.closest("button[data-approval-generate]");
    if (!button || button.disabled) {
        return;
    }
    const payrollHistoryId = (button.getAttribute("data-approval-generate") || "").trim();
    if (!payrollHistoryId) {
        return;
    }
    event.preventDefault();
    void generateAndPrintPayrollChangeApprovals([payrollHistoryId], button);
}

async function maybePreviewBeforePayrollChangeExport(selectedIds, titleSelectId, kind) {
    if (selectedIds.length > PAYROLL_CHANGE_PREVIEW_BEFORE_EXPORT_LIMIT) {
        return;
    }
    if (kind === "register") {
        await renderPayrollChangeRegisterFromServer(selectedIds, titleSelectId);
        return;
    }
    await renderPayrollChangeApprovalsFromServer(selectedIds, titleSelectId);
}

async function generateAndPrintSelectedPayrollChangeApprovals() {
    const selectedIds = Array.from(document.querySelectorAll("#report-approval-select-rows [data-approval-select]:checked"))
        .map(input => input.value)
        .filter(Boolean);
    const status = document.getElementById("report-approval-status");
    if (selectedIds.length === 0) {
        status.className = "status error";
        status.textContent = "请先勾选需要打印的人员。";
        return;
    }
    await generateAndPrintPayrollChangeApprovals(
        selectedIds,
        document.getElementById("payroll-change-approval-print"));
}

async function fetchAllPayrollChangeApprovalIds(statusEl) {
    const pageSize = 200;
    const ids = [];
    let page = 0;
    let totalPages = 1;
    do {
        if (statusEl) {
            statusEl.className = "status";
            statusEl.textContent = `正在收集待打印记录... 第 ${page + 1} / ${Math.max(totalPages, 1)} 页`;
        }
        const result = await getJson(
            `/api/reports/payroll-change-candidates?${buildPayrollChangeApprovalSearchParams(page, pageSize)}`);
        totalPages = Math.max(result.totalPages || 1, 1);
        for (const row of result.content || []) {
            const id = String(row.payrollHistoryId || "").trim();
            if (id) {
                ids.push(id);
            }
        }
        if (!(result.content || []).length) {
            break;
        }
        page += 1;
    } while (page < totalPages);
    return ids;
}

async function generateAndPrintAllPayrollChangeApprovals() {
    const status = document.getElementById("report-approval-status");
    const triggerButton = document.getElementById("payroll-change-approval-print-all");
    const knownTotal = state.payrollChangeApprovalTotalCount || 0;
    if (knownTotal <= 0) {
        status.className = "status error";
        status.textContent = "当前没有可打印的记录，请先查询。";
        return;
    }
    if (knownTotal > 20
        && !window.confirm(`将按当前筛选条件生成并打印全部 ${knownTotal} 份审批表，数量较大，确认继续？`)) {
        return;
    }
    const originalLabel = triggerButton?.textContent || "生成并打印全部";
    if (triggerButton) {
        triggerButton.disabled = true;
        triggerButton.textContent = "正在收集...";
    }
    try {
        const allIds = await fetchAllPayrollChangeApprovalIds(status);
        if (allIds.length === 0) {
            status.className = "status error";
            status.textContent = "当前筛选条件下没有可打印的记录。";
            return;
        }
        if (triggerButton) {
            triggerButton.textContent = `正在生成 ${allIds.length} 份...`;
        }
        // 由本函数统一恢复按钮文案，避免与共用打印函数互相覆盖
        await generateAndPrintPayrollChangeApprovals(allIds, null);
    } catch (error) {
        showError(status, error);
    } finally {
        if (triggerButton) {
            triggerButton.disabled = false;
            triggerButton.textContent = originalLabel;
        }
    }
}

async function generateAndPrintPayrollChangeApprovals(selectedIds, triggerButton) {
    const ids = (selectedIds || []).map(id => String(id || "").trim()).filter(Boolean);
    const status = document.getElementById("report-approval-status");
    if (ids.length === 0) {
        status.className = "status error";
        status.textContent = "请先勾选需要打印的人员。";
        return;
    }
    const defaultLabel = triggerButton?.id === "payroll-change-approval-print-all"
        ? "生成并打印全部"
        : triggerButton?.id === "payroll-change-approval-print"
            ? "生成并打印审批表"
            : "生成并打印";
    const originalLabel = triggerButton?.textContent || defaultLabel;
    if (triggerButton) {
        triggerButton.disabled = true;
        triggerButton.textContent = `正在生成 ${ids.length} 份...`;
    }
    status.className = "status";
    status.textContent = `正在生成 ${ids.length} 份审批表 PDF...`;
    try {
        const startedAt = performance.now();
        // 直接生成 PDF，不再先走 preview（会重复查库与排版，单份常多耗十余秒）
        const blob = await downloadPayrollChangeReportExport(
            "/api/reports/payroll-change-approvals/pdf",
            buildPayrollChangeExportRequest(ids, "report-approval-type-select"),
            status);
        const elapsedMs = Math.max(1, Math.round(performance.now() - startedAt));
        status.className = "status success";
        status.textContent = `已生成 ${ids.length} 份审批表 PDF（${elapsedMs} ms），正在打开打印窗口...`;
        await openPdfBlobForPrint(blob);
        status.textContent = `已生成并送打 ${ids.length} 份审批表 PDF（${elapsedMs} ms）`;
    } catch (error) {
        showError(status, error);
    } finally {
        if (triggerButton) {
            triggerButton.disabled = false;
            triggerButton.textContent = originalLabel;
        }
    }
}

async function exportSelectedPayrollChangeApprovalsExcel() {
    const selectedIds = Array.from(document.querySelectorAll("#report-approval-select-rows [data-approval-select]:checked"))
        .map(input => input.value)
        .filter(Boolean);
    const status = document.getElementById("report-approval-status");
    if (selectedIds.length === 0) {
        status.className = "status error";
        status.textContent = "请先勾选需要导出的人员。";
        return;
    }
    status.className = "status";
    status.textContent = `正在导出 ${selectedIds.length} 份审批表 Excel 备份...`;
    try {
        await downloadPayrollChangeReportExportFile(
            "/api/reports/payroll-change-approvals/excel",
            buildPayrollChangeExportRequest(selectedIds, "report-approval-type-select"),
            "payroll_change_approval.xlsx",
            status);
        status.className = "status success";
        status.textContent = `已导出 ${selectedIds.length} 份审批表 Excel 备份`;
    } catch (error) {
        showError(status, error);
    }
}

async function exportSelectedPayrollChangeRegisterExcel() {
    const selectedIds = Array.from(document.querySelectorAll("#report-payroll-change-rows [data-register-select]:checked"))
        .map(input => input.value)
        .filter(Boolean);
    const status = document.getElementById("report-payroll-change-status");
    if (selectedIds.length === 0) {
        status.className = "status error";
        status.textContent = "请先勾选需要导出的人员。";
        return;
    }
    status.className = "status";
    status.textContent = `正在导出 ${selectedIds.length} 人花名册 Excel 备份...`;
    try {
        await downloadPayrollChangeReportExportFile(
            "/api/reports/payroll-change-register/excel",
            buildPayrollChangeExportRequest(selectedIds, "report-payroll-change-type-select"),
            "payroll_change_register.xlsx",
            status);
        status.className = "status success";
        status.textContent = `已导出 ${selectedIds.length} 人花名册 Excel 备份`;
    } catch (error) {
        showError(status, error);
    }
}

function selectedReportTitle(selectId) {
    const select = document.getElementById(selectId);
    const selected = select?.selectedOptions?.[0];
    return selected?.dataset?.title || selected?.textContent?.trim() || "";
}

function gradeStepText(grade, step) {
    const normalizedGrade = String(grade ?? "").trim();
    const normalizedStep = String(step ?? "").trim();
    if (normalizedGrade && normalizedStep) {
        return `${normalizedGrade}-${normalizedStep}`;
    }
    return normalizedGrade || normalizedStep || "";
}

function buildPayrollChangeExportRequest(selectedIds, titleSelectId) {
    const title = selectedReportTitle(titleSelectId);
    return {
        payrollHistoryIds: selectedIds,
        reportTitle: title || null,
        institution: null,
    };
}

async function fetchPayrollChangePreviewHtml(url, requestBody) {
    const response = await fetch(url, {
        method: "POST",
        headers: { "Content-Type": "application/json", Accept: "text/html, application/json" },
        credentials: "same-origin",
        body: JSON.stringify(requestBody || {}),
    });
    await ensureAuthenticatedApiResponse(response, "预览生成失败");
    return response.text();
}

async function renderPayrollChangeApprovalsFromServer(selectedIds, titleSelectId) {
    const preview = document.getElementById("report-approval-preview");
    preview.innerHTML = await fetchPayrollChangePreviewHtml(
        "/api/reports/payroll-change-approvals/preview",
        buildPayrollChangeExportRequest(selectedIds, titleSelectId));
    preview.classList.remove("hidden");
    preview.scrollIntoView({ behavior: "smooth", block: "start" });
}

async function renderPayrollChangeRegisterFromServer(selectedIds, titleSelectId) {
    const preview = document.getElementById("report-payroll-change-preview");
    preview.innerHTML = await fetchPayrollChangePreviewHtml(
        "/api/reports/payroll-change-register/preview",
        buildPayrollChangeExportRequest(selectedIds, titleSelectId));
    preview.classList.remove("hidden");
    preview.scrollIntoView({ behavior: "smooth", block: "start" });
}

const PAYROLL_CHANGE_ASYNC_EXPORT_THRESHOLD = 20;
const PAYROLL_CHANGE_PREVIEW_BEFORE_EXPORT_LIMIT = 5;

function payrollChangeExportTargetFromUrl(url) {
    const path = String(url || "");
    if (path.includes("payroll-change-approvals/pdf")) {
        return "APPROVAL_PDF";
    }
    if (path.includes("payroll-change-approvals/excel")) {
        return "APPROVAL_EXCEL";
    }
    if (path.includes("payroll-change-register/pdf")) {
        return "REGISTER_PDF";
    }
    if (path.includes("payroll-change-register/excel")) {
        return "REGISTER_EXCEL";
    }
    throw new Error("不支持的导出地址");
}

function shouldUsePayrollChangeAsyncExport(requestBody) {
    const count = requestBody?.payrollHistoryIds?.length || 0;
    return count >= PAYROLL_CHANGE_ASYNC_EXPORT_THRESHOLD;
}

async function submitPayrollChangeExportJob(target, requestBody) {
    return postJson("/api/reports/payroll-change-export-jobs", {
        target,
        exportRequest: requestBody,
    });
}

async function waitForPayrollChangeExportJob(jobId, accessToken, onProgress) {
    const startedAt = performance.now();
    for (;;) {
        const job = await getJson(
            `/api/reports/payroll-change-export-jobs/${encodeURIComponent(jobId)}?accessToken=${encodeURIComponent(accessToken)}`);
        if (onProgress) {
            onProgress(job, Math.max(1, Math.round(performance.now() - startedAt)));
        }
        if (job.status === "SUCCEEDED" || job.status === "FAILED") {
            return job;
        }
        await new Promise(resolve => setTimeout(resolve, 1000));
    }
}

async function downloadPayrollChangeExportJobBlob(jobId, accessToken) {
    const response = await fetch(
        `/api/reports/payroll-change-export-jobs/${encodeURIComponent(jobId)}/download?accessToken=${encodeURIComponent(accessToken)}`,
        {
            credentials: "same-origin",
            headers: { Accept: "application/octet-stream, application/json" },
        });
    await ensureAuthenticatedApiResponse(response, "下载导出结果失败");
    const blob = await response.blob();
    const disposition = response.headers.get("Content-Disposition") || "";
    const match = disposition.match(/filename="?([^";]+)"?/i);
    return { blob, filename: match ? match[1] : "export.bin" };
}

async function runPayrollChangeExport(url, requestBody, statusEl) {
    if (shouldUsePayrollChangeAsyncExport(requestBody)) {
        const target = payrollChangeExportTargetFromUrl(url);
        const count = requestBody.payrollHistoryIds.length;
        if (statusEl) {
            statusEl.textContent = `已提交 ${count} 条后台导出任务，正在排队...`;
        }
        const submitted = await submitPayrollChangeExportJob(target, requestBody);
        const accessToken = submitted.accessToken;
        if (!accessToken) {
            throw new Error("后台导出任务未返回下载凭证，请刷新后重试");
        }
        const job = await waitForPayrollChangeExportJob(submitted.jobId, accessToken, (current, elapsedMs) => {
            if (!statusEl) {
                return;
            }
            if (current.status === "RUNNING") {
                statusEl.textContent = `后台导出进行中（${count} 条，已等待 ${elapsedMs} ms）...`;
            } else if (current.status === "PENDING") {
                statusEl.textContent = `后台导出排队中（${count} 条）...`;
            }
        });
        if (job.status !== "SUCCEEDED") {
            throw new Error(job.errorMessage || "后台导出失败");
        }
        if (statusEl) {
            statusEl.textContent = `后台导出完成（${count} 条），正在下载...`;
        }
        const downloaded = await downloadPayrollChangeExportJobBlob(job.jobId, accessToken);
        return { blob: downloaded.blob, filename: job.fileName || downloaded.filename };
    }

    const response = await fetch(url, {
        method: "POST",
        headers: { "Content-Type": "application/json", Accept: "application/octet-stream, application/json" },
        credentials: "same-origin",
        body: JSON.stringify(requestBody || {}),
    });
    await ensureAuthenticatedApiResponse(response, "导出失败");
    const blob = await response.blob();
    const disposition = response.headers.get("Content-Disposition") || "";
    const match = disposition.match(/filename="?([^";]+)"?/i);
    return { blob, filename: match ? match[1] : "export.bin" };
}

async function downloadPayrollChangeReportExport(url, requestBody, statusEl) {
    const { blob } = await runPayrollChangeExport(url, requestBody, statusEl);
    return blob;
}

async function downloadPayrollChangeReportExportFile(url, requestBody, fallbackFilename, statusEl) {
    const { blob, filename } = await runPayrollChangeExport(url, requestBody, statusEl);
    const link = document.createElement("a");
    link.href = URL.createObjectURL(blob);
    link.download = filename || fallbackFilename;
    link.click();
    URL.revokeObjectURL(link.href);
}

function openPdfBlobForPrint(blob) {
    return new Promise((resolve, reject) => {
        const url = URL.createObjectURL(blob);
        let frame = document.getElementById("approval-print-frame");
        if (!frame) {
            frame = document.createElement("iframe");
            frame.id = "approval-print-frame";
            frame.setAttribute("aria-hidden", "true");
            frame.style.cssText = "position:fixed;right:0;bottom:0;width:0;height:0;border:0;opacity:0;pointer-events:none;";
            document.body.appendChild(frame);
        }
        const cleanup = () => {
            URL.revokeObjectURL(url);
            resolve();
        };
        frame.onerror = () => {
            cleanup();
            reject(new Error("PDF 预览失败"));
        };
        frame.onload = () => {
            setTimeout(() => {
                try {
                    frame.contentWindow?.focus();
                    frame.contentWindow?.print();
                } catch (error) {
                    reject(error);
                    return;
                }
                setTimeout(cleanup, 800);
            }, 200);
        };
        frame.src = url;
    });
}

function printApprovalSheets(html) {
    const content = String(html || "").trim();
    if (!content) {
        window.print();
        return;
    }
    let frame = document.getElementById("approval-print-frame");
    if (!frame) {
        frame = document.createElement("iframe");
        frame.id = "approval-print-frame";
        frame.setAttribute("aria-hidden", "true");
        frame.style.cssText = "position:fixed;right:0;bottom:0;width:0;height:0;border:0;opacity:0;pointer-events:none;";
        document.body.appendChild(frame);
    }
    const cssHref = document.querySelector('link[href*="app.css"]')?.href || "/app.css";
    const doc = frame.contentDocument;
    doc.open();
    doc.write(`<!DOCTYPE html>
<html lang="zh-CN">
<head>
<meta charset="utf-8">
<title></title>
<link rel="stylesheet" href="${cssHref}">
<style>
@page { size: A4 portrait; margin: 0; }
html, body {
    margin: 0;
    padding: 0;
    background: #fff;
}
body {
    -webkit-print-color-adjust: exact;
    print-color-adjust: exact;
}
.approval-sheet {
    max-width: none !important;
    width: 210mm !important;
    height: 297mm !important;
    min-height: 297mm !important;
    margin: 0 !important;
    padding: 15mm 12mm 12mm !important;
    box-sizing: border-box !important;
    box-shadow: none !important;
    page-break-after: always;
    break-after: page;
    display: flex !important;
    flex-direction: column !important;
}
.approval-sheet:last-child {
    page-break-after: auto;
    break-after: auto;
}
.approval-topline,
.approval-sheet-header {
    flex: 0 0 auto !important;
}
.approval-frame-table {
    flex: 1 1 auto !important;
    width: 100% !important;
    height: 100% !important;
    min-height: 230mm !important;
}
.approval-col-label { width: 9% !important; }
.approval-col-value { width: 18% !important; }
.approval-col-edu-label { width: 9% !important; }
.approval-col-edu-value { width: 10% !important; }
.approval-body-side {
    overflow: hidden !important;
}
.approval-basis-text {
    word-break: break-all !important;
}
.approval-body-row > td {
    height: 168mm !important;
    vertical-align: top !important;
}
.approval-component-table,
.approval-basis-panel {
    height: 100% !important;
}
.approval-meta-table th,
.approval-meta-table td,
.approval-component-table th,
.approval-component-table td {
    padding: 7px 5px !important;
    line-height: 1.4 !important;
}
.approval-signature-cell,
.approval-signature-table,
.approval-signature-label,
.approval-signature-box {
    height: 48mm !important;
    min-height: 48mm !important;
}
</style>
</head>
<body>${content}</body>
</html>`);
    doc.close();
    const triggerPrint = () => {
        try {
            frame.contentWindow.focus();
            frame.contentWindow.print();
        } catch (error) {
            console.warn("审批表打印失败，回退到当前页打印", error);
            window.print();
        }
    };
    if (frame.contentDocument.readyState === "complete") {
        setTimeout(triggerPrint, 120);
    } else {
        frame.onload = () => setTimeout(triggerPrint, 120);
    }
}

async function loadPayrollChangeApprovalReports(payrollHistoryIds) {
    const ids = (payrollHistoryIds || []).map(id => String(id || "").trim()).filter(Boolean);
    if (ids.length === 0) {
        return [];
    }
    return postJson("/api/reports/payroll-change-approvals", ids);
}


async function loadPayrollHistory() {
    const organizationCode = selectedOrganizationCode("payroll-history-organization-code");
    const period = document.getElementById("payroll-history-period").value.trim();
    const keyword = document.getElementById("payroll-history-keyword").value.trim();
    const page = String(state.payrollHistoryPage || 0);
    const size = String(state.payrollHistoryPageSize || 20);
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
        const content = result.content || [];
        const totalPages = Math.max(result.totalPages || 1, 1);
        if ((result.page || 0) > totalPages - 1 && totalPages > 0 && (result.totalElements || 0) > 0) {
            state.payrollHistoryPage = Math.max(totalPages - 1, 0);
            return loadPayrollHistory();
        }
        state.payrollHistoryPage = result.page || 0;
        state.payrollHistoryTotalPages = totalPages;
        rows.innerHTML = content.map(row => {
            const orgText = `${row.organizationCode || ""} ${row.organizationName || ""}`.trim();
            const positionText = `${row.positionCode || ""} ${row.positionName || ""}`.trim();
            return `
            <tr>
                <td class="col-current">${row.currentPayroll ? "是" : "否"}</td>
                <td class="col-org" title="${escapeHtml(orgText)}">${escapeHtml(orgText)}</td>
                <td class="col-code">${escapeHtml(row.personCode || "")}</td>
                <td class="col-name">${escapeHtml(row.name || "")}</td>
                <td class="col-period">${escapeHtml(row.calculationYear || "")}${escapeHtml(row.calculationMonth || "")}</td>
                <td class="col-type">${escapeHtml(row.changeType || "")}</td>
                <td class="col-position" title="${escapeHtml(positionText)}">${escapeHtml(positionText || "-")}</td>
                <td class="col-level">${escapeHtml(formatPayrollHistoryLevelStep(row))}</td>
                <td class="col-year">${escapeHtml(row.levelAssessmentStartYear || "-")}</td>
                <td class="col-year">${escapeHtml(row.stepAssessmentStartYear || "-")}</td>
                <td class="col-amount">${money(row.totalAmount)}</td>
                <td class="col-actions"><button class="row-action" data-payroll-change="${escapeHtml(row.id)}" type="button">变动情况</button></td>
            </tr>`;
        }).join("");
        rows.querySelectorAll("button[data-payroll-change]").forEach(button => {
            button.addEventListener("click", () => openPayrollChangeModal(button.dataset.payrollChange));
        });
        renderPayrollHistoryPagination(result.totalElements || 0, totalPages);
        status.textContent = content.length
            ? `第 ${state.payrollHistoryPage + 1} / ${totalPages} 页，共 ${result.totalElements || 0} 条工资历史`
            : "未查询到工资变动历史";
    } catch (error) {
        renderPayrollHistoryPagination(0, 1);
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

function simplePromotionStatusId(apiPrefix) {
    if (apiPrefix.includes("education")) {
        return "education-promotion-status";
    }
    if (apiPrefix.includes("regularization-high")) {
        return "regularization-high-grade-status";
    }
    if (apiPrefix.includes("regularization")) {
        return "regularization-status";
    }
    if (apiPrefix.includes("floating-to-fixed")) {
        return "floating-to-fixed-status";
    }
    if (apiPrefix.includes("intern-salary-changes")) {
        return "intern-salary-change-status";
    }
    if (apiPrefix.includes("police-rank-change")) {
        return "police-rank-change-status";
    }
    return "teaching-allowance-status";
}

async function applySimplePromotionAction(apiPrefix, payrollHistoryId, moduleName, reloadFn) {
    if (!ensurePayrollApiWrite(apiPrefix, moduleName)) {
        return;
    }
    if (!confirm(`确认按当前试算结果处理${moduleName}？系统会新增一条当前工资变动记录，并将原当前记录转为历史记录。`)) {
        return;
    }
    const status = document.getElementById(simplePromotionStatusId(apiPrefix));
    status.className = "status";
    status.textContent = `正在处理${moduleName}...`;
    try {
        const result = await postJson(`/api/payroll/${apiPrefix}/${encodeURIComponent(payrollHistoryId)}/apply`, {});
        status.textContent = (result && result.message) || `${moduleName}处理完成`;
        await reloadFn();
    } catch (error) {
        showError(status, error);
    }
}

async function rollbackSimplePromotionAction(apiPrefix, payrollHistoryId, moduleName, reloadFn) {
    if (!ensurePayrollApiWrite(apiPrefix, moduleName)) {
        return;
    }
    if (!confirm(`确认还原当前${moduleName}工资变动？`)) {
        return;
    }
    const status = document.getElementById(simplePromotionStatusId(apiPrefix));
    status.className = "status";
    status.textContent = `正在还原${moduleName}...`;
    try {
        const result = await postJson(`/api/payroll/${apiPrefix}/${encodeURIComponent(payrollHistoryId)}/rollback`, {});
        status.textContent = (result && result.message) || `${moduleName}已还原`;
        await reloadFn();
    } catch (error) {
        showError(status, error);
    }
}

function renderSimplePromotionActions(row, apiPrefix, moduleName, reloadFn) {
    const canApply = Boolean(row.applyEligible);
    const canRollback = Boolean(row.rollbackEligible);
    const canWrite = hasPayrollFeatureWrite(payrollWritePermissionForApi(apiPrefix));
    if (!canWrite || (!canApply && !canRollback)) {
        return "-";
    }
    const parts = [];
    if (canApply) {
        parts.push(`<button class="row-action" type="button" data-simple-apply="${escapeHtml(row.payrollHistoryId)}" data-simple-api="${escapeHtml(apiPrefix)}" data-simple-name="${escapeHtml(moduleName)}">处理</button>`);
    }
    if (canRollback) {
        parts.push(`<button class="row-action danger-button" type="button" data-simple-rollback="${escapeHtml(row.payrollHistoryId)}" data-simple-api="${escapeHtml(apiPrefix)}" data-simple-name="${escapeHtml(moduleName)}">还原</button>`);
    }
    return parts.join(" ");
}

function bindSimplePromotionActions(container, reloadFn) {
    container.querySelectorAll("button[data-simple-apply]").forEach(button => {
        button.addEventListener("click", () => applySimplePromotionAction(
            button.dataset.simpleApi,
            button.dataset.simpleApply,
            button.dataset.simpleName,
            reloadFn));
    });
    container.querySelectorAll("button[data-simple-rollback]").forEach(button => {
        button.addEventListener("click", () => rollbackSimplePromotionAction(
            button.dataset.simpleApi,
            button.dataset.simpleRollback,
            button.dataset.simpleName,
            reloadFn));
    });
}

function rankChangeStateBase(idPrefix) {
    return idPrefix.split("-").map((part, index) => index === 0 ? part : part.charAt(0).toUpperCase() + part.slice(1)).join("");
}

function rankChangeLaterPeriodMode(config) {
    return document.getElementById(`${config.idPrefix}-later-period-mode`)?.value?.trim() || "block";
}

function rankChangeApplyParams(config) {
    const params = new URLSearchParams();
    const laterPeriodMode = rankChangeLaterPeriodMode(config);
    if (laterPeriodMode) {
        params.set("laterPeriodMode", laterPeriodMode);
    }
    return params;
}

function confirmRankChangeApply(config, row) {
    if (!row?.midChainApply) {
        return confirm(`确认按当前试算结果处理${config.moduleName}？系统会新增一条当前工资变动记录，并将原当前记录转为历史记录。`);
    }
    const successorCount = Number(row.laterPeriodSuccessorCount || 0);
    return confirm(
        `确认处理${config.moduleName}？该人员为后变动人员：`
        + `将在 ${row.calculationPeriod || ""} 中段插入${config.moduleName}，并重算后继 ${successorCount} 条记录（仅改等级/jxjt 与合计）。`
        + " 办理后可导出后变动重算清单。");
}

function accumulateRankChangeMidChainExports(config, exports) {
    const base = rankChangeStateBase(config.idPrefix);
    const key = `${base}MidChainExports`;
    const incoming = (exports || []).filter(Boolean);
    if (!incoming.length) {
        return;
    }
    state[key] = [...(state[key] || []), ...incoming];
}

function downloadRankChangeMidChainExportsCsv(exports, filename) {
    const rows = exports || [];
    const lines = ["单位编码,单位名称,人员编码,姓名,执行年月,变动记录ID,后继变动年月,后继变动类别,原等级,新等级,原jxjt,新jxjt,原合计,新合计,差额"];
    for (const item of rows) {
        for (const successor of item.successors || []) {
            lines.push([
                item.organizationCode || "",
                item.organizationName || "",
                item.personCode || "",
                item.name || "",
                item.executionPeriod || "",
                item.adjustmentHistoryId || "",
                successor.period || "",
                successor.changeType || "",
                successor.oldRankName || "",
                successor.newRankName || "",
                successor.oldRankAllowance ?? "",
                successor.newRankAllowance ?? "",
                successor.oldTotal ?? "",
                successor.newTotal ?? "",
                successor.difference ?? "",
            ].map(value => {
                const text = String(value).replace(/"/g, '""');
                return /[",\n]/.test(text) ? `"${text}"` : text;
            }).join(","));
        }
    }
    const blob = new Blob(["\ufeff" + lines.join("\n")], { type: "text/csv;charset=utf-8" });
    const url = window.URL.createObjectURL(blob);
    const anchor = document.createElement("a");
    anchor.href = url;
    anchor.download = filename || `rank-change-mid-chain-${new Date().toISOString().slice(0, 10)}.csv`;
    document.body.appendChild(anchor);
    anchor.click();
    window.URL.revokeObjectURL(url);
    document.body.removeChild(anchor);
}

async function exportRankChangeMidChain(config) {
    const prefix = config.idPrefix;
    const status = document.getElementById(`${prefix}-status`);
    const base = rankChangeStateBase(prefix);
    const accumulated = state[`${base}MidChainExports`] || [];
    if (accumulated.length) {
        downloadRankChangeMidChainExportsCsv(accumulated, `${prefix}-mid-chain-session.csv`);
        status.className = "status";
        status.textContent = `已下载本次会话中段重算清单（${accumulated.length} 人）。`;
        return;
    }
    const organizationCode = selectedOrganizationCode(`${prefix}-organization-code`);
    const keyword = document.getElementById(`${prefix}-keyword`)?.value?.trim() || "";
    status.className = "status";
    status.textContent = "正在导出已办理的中段重算清单...";
    try {
        const params = new URLSearchParams();
        if (organizationCode) {
            params.set("organizationCode", organizationCode);
        }
        if (keyword) {
            params.set("keyword", keyword);
        }
        const response = await fetch(`/api/payroll/${config.apiPrefix}/mid-chain-export?${params}`, {
            headers: { Accept: "text/csv" },
        });
        if (!response.ok) {
            throw new Error(`HTTP ${response.status}`);
        }
        const blob = await response.blob();
        const url = window.URL.createObjectURL(blob);
        const anchor = document.createElement("a");
        anchor.href = url;
        anchor.download = `${prefix}-mid-chain-export.csv`;
        document.body.appendChild(anchor);
        anchor.click();
        window.URL.revokeObjectURL(url);
        document.body.removeChild(anchor);
        status.textContent = "后变动重算清单导出完成。";
    } catch (error) {
        showError(status, error);
    }
}

async function applyRankChangePromotion(config, payrollHistoryId, row, reloadFn) {
    if (!ensurePayrollFeatureWrite(config.writePermission, config.moduleName)) {
        return;
    }
    if (!confirmRankChangeApply(config, row || {})) {
        return;
    }
    const prefix = config.idPrefix;
    const status = document.getElementById(`${prefix}-status`);
    status.className = "status";
    status.textContent = `正在处理${config.moduleName}...`;
    try {
        const params = rankChangeApplyParams(config);
        const result = await postJson(
            `/api/payroll/${config.apiPrefix}/${encodeURIComponent(payrollHistoryId)}/apply?${params}`,
            {});
        accumulateRankChangeMidChainExports(config, result?.midChainExport ? [result.midChainExport] : []);
        const exportHint = result?.midChainExport ? " 可点击「导出后变动重算清单」下载。" : "";
        status.textContent = `${(result && result.message) || `${config.moduleName}处理完成`}${exportHint}`;
        await reloadFn();
    } catch (error) {
        showError(status, error);
    }
}

function bindRankChangeModuleListeners(config) {
    const prefix = config.idPrefix;
    document.getElementById(`${prefix}-export-mid-chain`)?.addEventListener("click", () => exportRankChangeMidChain(config));
    document.getElementById(`${prefix}-batch-apply`)?.addEventListener("click", () => applySelectedRankChangePromotions(config));
    document.getElementById(`${prefix}-apply-all`)?.addEventListener("click", () => applyAllEligibleRankChangePromotions(config));
    document.getElementById(`${prefix}-batch-rollback`)?.addEventListener("click", () => rollbackSelectedRankChangePromotions(config));
    document.getElementById(`${prefix}-rollback-all`)?.addEventListener("click", () => rollbackAllProcessedRankChangePromotions(config));
    document.getElementById(`${prefix}-select-all`)?.addEventListener("change", event => {
        document.querySelectorAll(`[data-rank-change-select][data-rank-change-module="${prefix}"]`).forEach(checkbox => {
            checkbox.checked = event.target.checked;
        });
    });
    bindRankChangePagination(config);
}

function rankChangeSelectAttr(config, row) {
    const canWrite = hasPayrollFeatureWrite(config.writePermission);
    if (!canWrite) {
        return "";
    }
    const canApply = Boolean(row.applyEligible);
    const canRollback = Boolean(row.rollbackEligible);
    const canSelect = canApply || canRollback;
    if (!canSelect) {
        return "";
    }
    const selectAction = canApply ? "apply" : "rollback";
    return `<input type="checkbox" data-rank-change-select="${escapeHtml(row.payrollHistoryId)}" data-rank-change-module="${escapeHtml(config.idPrefix)}" data-rank-change-action="${selectAction}" aria-label="选择 ${escapeHtml(row.name || row.personCode || "")}">`;
}

function confirmRankChangeBatchApply(config, rows, scopeText) {
    const eligibleRows = (rows || []).filter(Boolean);
    if (!eligibleRows.length) {
        return false;
    }
    const midChainRows = eligibleRows.filter(row => row.midChainApply);
    if (!midChainRows.length) {
        return confirm(`确认${scopeText} ${eligibleRows.length} 条${config.moduleName}记录？系统会新增工资变动记录。`);
    }
    const totalSuccessors = midChainRows.reduce((sum, row) => sum + Number(row.laterPeriodSuccessorCount || 0), 0);
    return confirm(
        `确认${scopeText} ${eligibleRows.length} 条${config.moduleName}记录？`
        + `其中 ${midChainRows.length} 人为后变动人员，将中段插入并重算后继约 ${totalSuccessors} 条记录。`
        + " 办理后可导出后变动重算清单。");
}

async function postRankChangeBatchChunks(config, url, items, status, progressLabel) {
    const chunkSize = 200;
    let successCount = 0;
    let failureCount = 0;
    const failures = [];
    const midChainExports = [];
    for (let from = 0; from < items.length; from += chunkSize) {
        const chunk = items.slice(from, Math.min(from + chunkSize, items.length));
        if (status) {
            status.textContent = `${progressLabel} ${Math.min(from + chunk.length, items.length)} / ${items.length}...`;
        }
        const result = await postJson(url, { items: chunk });
        successCount += Number(result?.successCount || 0);
        failureCount += Number(result?.failureCount || 0);
        if (Array.isArray(result?.failures) && result.failures.length) {
            failures.push(...result.failures);
        }
        if (Array.isArray(result?.midChainExports) && result.midChainExports.length) {
            midChainExports.push(...result.midChainExports);
        }
    }
    accumulateRankChangeMidChainExports(config, midChainExports);
    return { successCount, failureCount, failures, midChainExports };
}

async function applySelectedRankChangePromotions(config) {
    if (!ensurePayrollFeatureWrite(config.writePermission, config.moduleName)) {
        return;
    }
    const prefix = config.idPrefix;
    const base = rankChangeStateBase(prefix);
    const status = document.getElementById(`${prefix}-status`);
    const selectedIds = Array.from(document.querySelectorAll(`[data-rank-change-select][data-rank-change-module="${prefix}"]:checked`))
        .filter(checkbox => checkbox.dataset.rankChangeAction === "apply")
        .map(checkbox => checkbox.dataset.rankChangeSelect)
        .filter(Boolean);
    if (!selectedIds.length) {
        status.className = "status error";
        status.textContent = "请先勾选需要办理的试算记录。";
        return;
    }
    const rows = selectedIds.map(id => (state[`${base}RowsById`] || {})[id]).filter(Boolean);
    if (!confirmRankChangeBatchApply(config, rows, `批量办理 ${selectedIds.length} 条`)) {
        return;
    }
    const items = selectedIds.map(payrollHistoryId => ({ payrollHistoryId }));
    status.className = "status";
    status.textContent = `正在批量处理 ${items.length} 条...`;
    try {
        const params = rankChangeApplyParams(config);
        const result = await postRankChangeBatchChunks(
            config,
            `/api/payroll/${config.apiPrefix}/batch-apply?${params}`,
            items,
            status,
            "正在批量处理");
        status.className = result.failureCount ? "status error" : "status";
        const exportHint = (result.midChainExports || []).length
            ? ` 本次中段重算 ${result.midChainExports.length} 人，可点击「导出后变动重算清单」下载。`
            : "";
        status.textContent = `批量处理完成：成功 ${result.successCount} 条，失败 ${result.failureCount} 条。${exportHint}正在刷新列表...`;
        state[`${base}Page`] = 0;
        await loadRankChangePromotions(config);
    } catch (error) {
        showError(status, error);
    }
}

async function rollbackSelectedRankChangePromotions(config) {
    if (!ensurePayrollFeatureWrite(config.writePermission, config.moduleName)) {
        return;
    }
    const prefix = config.idPrefix;
    const base = rankChangeStateBase(prefix);
    const status = document.getElementById(`${prefix}-status`);
    const selectedIds = Array.from(document.querySelectorAll(`[data-rank-change-select][data-rank-change-module="${prefix}"]:checked`))
        .filter(checkbox => checkbox.dataset.rankChangeAction === "rollback")
        .map(checkbox => checkbox.dataset.rankChangeSelect)
        .filter(Boolean);
    if (!selectedIds.length) {
        status.className = "status error";
        status.textContent = "请先勾选需要还原的记录。";
        return;
    }
    if (!confirm(`确认批量还原 ${selectedIds.length} 条${config.moduleName}记录？系统会删除对应链头记录并恢复上一条工资记录。`)) {
        return;
    }
    const items = [];
    for (const id of selectedIds) {
        const row = (state[`${base}RowsById`] || {})[id];
        if (!row?.payrollHistoryId) {
            status.className = "status error";
            status.textContent = "部分勾选记录缺少标识，请重新查询后再还原。";
            return;
        }
        items.push({
            payrollHistoryId: row.payrollHistoryId,
            organizationCode: row.organizationCode || "",
            personCode: row.personCode || "",
        });
    }
    status.className = "status";
    status.textContent = `正在批量还原 ${items.length} 条...`;
    try {
        const result = await postRankChangeBatchChunks(
            config,
            `/api/payroll/${config.apiPrefix}/batch-rollback`,
            items,
            status,
            "正在批量还原");
        status.className = result.failureCount ? "status error" : "status";
        status.textContent = `批量还原完成：成功 ${result.successCount} 条，失败 ${result.failureCount} 条。正在刷新列表...`;
        state[`${base}Page`] = 0;
        await loadRankChangePromotions(config);
    } catch (error) {
        showError(status, error);
    }
}

async function applyAllEligibleRankChangePromotions(config) {
    if (!ensurePayrollFeatureWrite(config.writePermission, config.moduleName)) {
        return;
    }
    const prefix = config.idPrefix;
    const base = rankChangeStateBase(prefix);
    const status = document.getElementById(`${prefix}-status`);
    const organizationCode = selectedOrganizationCode(`${prefix}-organization-code`);
    const keyword = document.getElementById(`${prefix}-keyword`)?.value?.trim() || "";
    status.className = "status";
    status.textContent = "正在获取全部待办理人员...";

    let items = [];
    let eligibleRows = [];
    try {
        const baseParams = { laterPeriodMode: rankChangeLaterPeriodMode(config) };
        if (organizationCode) {
            baseParams.organizationCode = organizationCode;
        }
        if (keyword) {
            baseParams.keyword = keyword;
        }
        const rows = await collectAllPromotionRows(
            `/api/payroll/${config.apiPrefix}`,
            baseParams,
            status,
            "正在获取待办理人员");
        eligibleRows = rows.filter(row => row.applyEligible && row.payrollHistoryId);
        items = eligibleRows.map(row => ({ payrollHistoryId: row.payrollHistoryId }));
    } catch (error) {
        showError(status, error);
        return;
    }

    if (!items.length) {
        status.className = "status";
        status.textContent = `没有可办理的${config.moduleName}记录。`;
        return;
    }
    const scopeText = organizationCode ? `单位 ${organizationCode}` : "全部可见单位";
    if (!confirmRankChangeBatchApply(config, eligibleRows, `办理${scopeText} 全部待办理 ${items.length} 条`)) {
        status.className = "status";
        status.textContent = "已取消批量处理全部。";
        return;
    }

    status.className = "status";
    status.textContent = `正在批量处理全部 ${items.length} 条...`;
    try {
        const params = rankChangeApplyParams(config);
        const result = await postRankChangeBatchChunks(
            config,
            `/api/payroll/${config.apiPrefix}/batch-apply?${params}`,
            items,
            status,
            "正在批量处理全部");
        status.className = result.failureCount ? "status error" : "status";
        const exportHint = (result.midChainExports || []).length
            ? ` 本次中段重算 ${result.midChainExports.length} 人，可点击「导出后变动重算清单」下载。`
            : "";
        status.textContent = `批量处理全部完成：成功 ${result.successCount} 条，失败 ${result.failureCount} 条。${exportHint}正在刷新列表...`;
        state[`${base}Page`] = 0;
        await loadRankChangePromotions(config);
    } catch (error) {
        showError(status, error);
    }
}

async function rollbackAllProcessedRankChangePromotions(config) {
    if (!ensurePayrollFeatureWrite(config.writePermission, config.moduleName)) {
        return;
    }
    const prefix = config.idPrefix;
    const base = rankChangeStateBase(prefix);
    const status = document.getElementById(`${prefix}-status`);
    const organizationCode = selectedOrganizationCode(`${prefix}-organization-code`);
    const keyword = document.getElementById(`${prefix}-keyword`)?.value?.trim() || "";
    status.className = "status";
    status.textContent = "正在获取全部已办理人员...";

    let items = [];
    try {
        const baseParams = { laterPeriodMode: rankChangeLaterPeriodMode(config) };
        if (organizationCode) {
            baseParams.organizationCode = organizationCode;
        }
        if (keyword) {
            baseParams.keyword = keyword;
        }
        const rows = await collectAllPromotionRows(
            `/api/payroll/${config.apiPrefix}`,
            baseParams,
            status,
            "正在获取已办理人员");
        items = rows
            .filter(row => row.rollbackEligible && row.payrollHistoryId)
            .map(row => ({
                payrollHistoryId: row.payrollHistoryId,
                organizationCode: row.organizationCode || "",
                personCode: row.personCode || "",
            }));
    } catch (error) {
        showError(status, error);
        return;
    }

    if (!items.length) {
        status.className = "status";
        status.textContent = `没有可还原的${config.moduleName}记录。`;
        return;
    }
    const scopeText = organizationCode ? `单位 ${organizationCode}` : "全部可见单位";
    if (!confirm(`确认还原${scopeText} 全部已办理 ${items.length} 条${config.moduleName}记录？系统会删除对应链头记录并恢复上一条工资记录。`)) {
        status.className = "status";
        status.textContent = "已取消批量还原全部。";
        return;
    }

    status.className = "status";
    status.textContent = `正在批量还原全部 ${items.length} 条...`;
    try {
        const result = await postRankChangeBatchChunks(
            config,
            `/api/payroll/${config.apiPrefix}/batch-rollback`,
            items,
            status,
            "正在批量还原全部");
        status.className = result.failureCount ? "status error" : "status";
        status.textContent = `批量还原全部完成：成功 ${result.successCount} 条，失败 ${result.failureCount} 条。正在刷新列表...`;
        state[`${base}Page`] = 0;
        await loadRankChangePromotions(config);
    } catch (error) {
        showError(status, error);
    }
}

function closeRankChangeDetailModal() {
    document.getElementById("rank-change-detail-modal")?.classList.add("hidden");
}

function renderRankChangeDetailContent(config, row) {
    const title = document.getElementById("rank-change-detail-title");
    const summary = document.getElementById("rank-change-detail-summary");
    const content = document.getElementById("rank-change-detail-content");
    if (!summary || !content) {
        return;
    }
    if (title) {
        title.textContent = `${config.moduleName}明细`;
    }
    const statusText = row.rollbackEligible
        ? "（已处理，可还原）"
        : row.applyEligible
            ? "（待处理）"
            : "";
    summary.textContent =
        `${row.organizationName || row.organizationCode || ""}-${row.personCode || ""} ${row.name || ""}`
        + ` / 执行 ${row.calculationPeriod || "-"}${statusText}`;
    const positionText = `${row.positionCode || ""} ${row.positionName || ""}`.trim() || "-";
    content.innerHTML = `
        <table class="detail-table">
            <tbody>
                ${positionChangeDetailCompareRow("岗位", positionText, positionText)}
                ${positionChangeDetailCompareRow("执行年月", row.calculationPeriod || "-", row.calculationPeriod || "-")}
                ${positionChangeDetailCompareRow("授衔/变动年月", row.rankChangeYearMonth || "-", row.rankChangeYearMonth || "-")}
                ${positionChangeDetailCompareRow("旧等级", row.storedRankName || "-", row.targetRankName || "-")}
                ${positionChangeDetailMoneyRow("jxjt", row.storedRankAllowance, row.calculatedRankAllowance, row.differenceAmount)}
                <tr><td>说明</td><td colspan="3">${escapeHtml(row.standardNote || "-")}${row.midChainApply ? "【中段】" : ""}${row.laterPeriodSuccessorCount ? `（后继${row.laterPeriodSuccessorCount}条）` : ""}</td></tr>
            </tbody>
        </table>`;
}

function openRankChangeDetailModal(config, payrollHistoryId) {
    const base = rankChangeStateBase(config.idPrefix);
    const row = (state[`${base}RowsById`] || {})[payrollHistoryId];
    const modal = document.getElementById("rank-change-detail-modal");
    const content = document.getElementById("rank-change-detail-content");
    const summary = document.getElementById("rank-change-detail-summary");
    if (!modal || !content) {
        return;
    }
    if (!row) {
        if (summary) {
            summary.textContent = "未找到试算结果，请重新查询。";
        }
        content.innerHTML = "";
        modal.classList.remove("hidden");
        return;
    }
    renderRankChangeDetailContent(config, row);
    modal.classList.remove("hidden");
}

function bindRankChangeActions(config, container, reloadFn) {
    const base = rankChangeStateBase(config.idPrefix);
    container.querySelectorAll("button[data-rank-change-detail]").forEach(button => {
        button.addEventListener("click", () => openRankChangeDetailModal(config, button.dataset.rankChangeDetail));
    });
    container.querySelectorAll("button[data-rank-change-apply]").forEach(button => {
        button.addEventListener("click", () => {
            const rowId = button.dataset.rankChangeApply;
            const row = (state[`${base}RowsById`] || {})[rowId] || {};
            applyRankChangePromotion(config, rowId, row, reloadFn);
        });
    });
    container.querySelectorAll("button[data-rank-change-rollback]").forEach(button => {
        button.addEventListener("click", () => rollbackSimplePromotionAction(
            config.apiPrefix,
            button.dataset.rankChangeRollback,
            config.moduleName,
            reloadFn));
    });
}

function renderRankChangeActions(config, row) {
    const canWrite = hasPayrollFeatureWrite(config.writePermission);
    const canApply = Boolean(row.applyEligible);
    const canRollback = Boolean(row.rollbackEligible);
    const parts = [
        `<button class="row-action" type="button" data-rank-change-detail="${escapeHtml(row.payrollHistoryId)}">明细</button>`,
    ];
    if (canWrite && canApply) {
        parts.push(`<button class="row-action" type="button" data-rank-change-apply="${escapeHtml(row.payrollHistoryId)}">处理</button>`);
    }
    if (canWrite && canRollback) {
        parts.push(`<button class="row-action danger-button" type="button" data-rank-change-rollback="${escapeHtml(row.payrollHistoryId)}">还原</button>`);
    }
    return parts.join(" ");
}

async function loadRankChangePromotions(config) {
    const prefix = config.idPrefix;
    const base = rankChangeStateBase(prefix);
    const organizationCode = selectedOrganizationCode(`${prefix}-organization-code`);
    const keyword = document.getElementById(`${prefix}-keyword`)?.value?.trim() || "";
    const page = String(state[`${base}Page`] || 0);
    const size = "20";
    const params = new URLSearchParams({ page, size });
    if (organizationCode) {
        params.set("organizationCode", organizationCode);
    }
    if (keyword) {
        params.set("keyword", keyword);
    }
    const laterPeriodMode = rankChangeLaterPeriodMode(config);
    if (laterPeriodMode) {
        params.set("laterPeriodMode", laterPeriodMode);
    }

    const status = document.getElementById(`${prefix}-status`);
    const rows = document.getElementById(`${prefix}-rows`);
    status.className = "status";
    status.textContent = `正在查询${config.moduleName}试算...`;
    rows.innerHTML = "";

    const reloadFn = () => loadRankChangePromotions(config);
    try {
        const result = await getJson(`/api/payroll/${config.apiPrefix}?${params}`);
        const total = result.totalElements || 0;
        const totalPages = Math.max(result.totalPages || 1, 1);
        if (total > 0 && state[`${base}Page`] >= totalPages) {
            state[`${base}Page`] = Math.max(totalPages - 1, 0);
            await reloadFn();
            return;
        }
        state[`${base}Page`] = result.page || 0;
        const byId = {};
        (result.content || []).forEach(row => {
            if (row?.payrollHistoryId) {
                byId[row.payrollHistoryId] = row;
            }
        });
        state[`${base}RowsById`] = byId;
        const selectAll = document.getElementById(`${prefix}-select-all`);
        if (selectAll) {
            selectAll.checked = false;
        }
        rows.innerHTML = (result.content || []).map(row => `
            <tr class="${row.rollbackEligible ? "highlight-row" : ""}">
                <td class="col-select">${rankChangeSelectAttr(config, row)}</td>
                <td class="col-org">${escapeHtml(row.organizationCode)} ${escapeHtml(row.organizationName || "")}</td>
                <td class="col-code">${escapeHtml(row.personCode)}</td>
                <td class="col-name">${escapeHtml(row.name)}</td>
                <td class="col-period">${escapeHtml(row.calculationPeriod || "")}</td>
                <td class="col-position" title="${escapeHtml(row.positionCode || "")}">${escapeHtml(row.positionName || "")}</td>
                <td class="col-period">${escapeHtml(row.rankChangeYearMonth || "")}</td>
                <td class="col-grade">${escapeHtml(row.storedRankName || "")}</td>
                <td class="col-grade">${escapeHtml(row.targetRankName || "")}</td>
                <td class="col-money">${money(row.storedRankAllowance)}</td>
                <td class="col-money">${money(row.calculatedRankAllowance)}</td>
                <td class="col-money ${Number(row.differenceAmount) === 0 ? "difference-ok" : "difference-bad"}">${money(row.differenceAmount)}</td>
                <td class="col-note">${escapeHtml(row.standardNote || "")}${row.midChainApply ? "【中段】" : ""}${row.laterPeriodSuccessorCount ? `（后继${row.laterPeriodSuccessorCount}条）` : ""}</td>
                <td class="col-action">${renderRankChangeActions(config, row)}</td>
            </tr>
        `).join("");
        bindRankChangeActions(config, rows, reloadFn);
        renderRankChangePagination(config, total, totalPages);
        status.textContent = total
            ? `共 ${total} 条试算记录，第 ${state[`${base}Page`] + 1} / ${totalPages} 页`
            : "未查询到试算记录";
    } catch (error) {
        renderRankChangePagination(config, 0, 1);
        showError(status, error);
    }
}

function policeRankChangeLaterPeriodMode() {
    return rankChangeLaterPeriodMode(RANK_CHANGE_MODULES.police);
}

function policeRankChangeApplyParams() {
    return rankChangeApplyParams(RANK_CHANGE_MODULES.police);
}

function confirmPoliceRankChangeApply(row) {
    return confirmRankChangeApply(RANK_CHANGE_MODULES.police, row);
}

function accumulatePoliceRankChangeMidChainExports(exports) {
    accumulateRankChangeMidChainExports(RANK_CHANGE_MODULES.police, exports);
}

function downloadPoliceRankChangeMidChainExportsCsv(exports, filename) {
    downloadRankChangeMidChainExportsCsv(exports, filename);
}

async function exportPoliceRankChangeMidChain() {
    return exportRankChangeMidChain(RANK_CHANGE_MODULES.police);
}

async function applyPoliceRankChangePromotion(payrollHistoryId, row, reloadFn) {
    return applyRankChangePromotion(RANK_CHANGE_MODULES.police, payrollHistoryId, row, reloadFn);
}

function bindPoliceRankChangeActions(container, reloadFn) {
    bindRankChangeActions(RANK_CHANGE_MODULES.police, container, reloadFn);
}

function renderPoliceRankChangeActions(row) {
    return renderRankChangeActions(RANK_CHANGE_MODULES.police, row);
}

async function loadPoliceRankChangePromotions() {
    return loadRankChangePromotions(RANK_CHANGE_MODULES.police);
}

async function loadProsecutionRankChangePromotions() {
    return loadRankChangePromotions(RANK_CHANGE_MODULES.prosecution);
}

async function loadJudicialRankChangePromotions() {
    return loadRankChangePromotions(RANK_CHANGE_MODULES.judicial);
}

async function loadSupervisionRankChangePromotions() {
    return loadRankChangePromotions(RANK_CHANGE_MODULES.supervision);
}

async function loadTeachingAllowanceAdjustments() {
    const organizationCode = selectedOrganizationCode("teaching-allowance-organization-code");
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
                <td>${renderSimplePromotionActions(row, "teaching-allowance-adjustments", "教护龄津贴调整", loadTeachingAllowanceAdjustments)}</td>
            </tr>
        `).join("");
        bindSimplePromotionActions(rows, loadTeachingAllowanceAdjustments);
        status.textContent = `第 ${result.page + 1} / ${Math.max(result.totalPages, 1)} 页，共 ${result.totalElements} 条试算记录`;
    } catch (error) {
        showError(status, error);
    }
}

async function loadNormalPromotions() {
    const organizationCode = selectedOrganizationCode("normal-promotion-organization-code");
    const keyword = document.getElementById("normal-promotion-keyword").value.trim();
    const page = String(state.normalPromotionPage || 0);
    const size = "20";
    const year = currentNormalPromotionYear();
    const params = new URLSearchParams({ page, size, year });
    if (!document.getElementById("normal-promotion-include-apply")?.checked) {
        params.set("includeApply", "false");
    }
    if (!document.getElementById("normal-promotion-include-processed")?.checked) {
        params.set("includeProcessed", "false");
    }
    if (organizationCode) {
        params.set("organizationCode", organizationCode);
    }
    if (keyword) {
        params.set("keyword", keyword);
    }
    const laterPeriodMode = normalPromotionLaterPeriodMode();
    if (laterPeriodMode) {
        params.set("laterPeriodMode", laterPeriodMode);
    }

    const status = document.getElementById("normal-promotion-status");
    const rows = document.getElementById("normal-promotion-rows");
    status.className = "status";
    status.textContent = "正在查询正常档次/薪级晋升试算...";
    rows.innerHTML = "";

    try {
        const result = await getJson(`/api/payroll/normal-promotions?${params}`);
        const total = result.totalElements || 0;
        const totalPages = Math.max(result.totalPages || 1, 1);
        if (total > 0 && state.normalPromotionPage >= totalPages) {
            state.normalPromotionPage = Math.max(totalPages - 1, 0);
            await loadNormalPromotions();
            return;
        }
        const byId = {};
        (result.content || []).forEach(row => {
            if (row?.payrollHistoryId) {
                byId[row.payrollHistoryId] = row;
            }
        });
        state.normalPromotionRowsById = byId;
        state.normalPromotionPage = result.page || 0;
        state.normalPromotionListMeta = {
            page: result.page,
            totalPages: result.totalPages,
            totalElements: result.totalElements,
            year,
        };
        renderNormalPromotionTableRows(result.content || []);
        updateAllPayrollFeatureWriteUi();
        renderNormalPromotionPagination(total, totalPages);
        status.textContent = total
            ? `共 ${total} 条试算记录（${year} 年），第 ${state.normalPromotionPage + 1} / ${totalPages} 页`
            : `未查询到试算记录（${year} 年）`;
    } catch (error) {
        renderNormalPromotionPagination(0, 1);
        showError(status, error);
    }
}

/** 正常档次/薪级：事业人员无级别起算，不展示该列。 */
function isNormalPromotionInstitutionRow(row) {
    const source = String(row?.baseSalarySource || "").toUpperCase();
    if (source === "SALARY_LEVEL") {
        return true;
    }
    return isInstitutionPositionCode(row?.positionCode || row?.currentPositionCode);
}

function shouldHideNormalPromotionLevelStartColumn(rows) {
    const list = rows || [];
    return list.length > 0 && list.every(isNormalPromotionInstitutionRow);
}

function updateNormalPromotionLevelStartColumnVisibility(rows) {
    const table = document.querySelector("#normal-promotion .normal-promotion-table");
    if (!table) {
        return;
    }
    table.classList.toggle("hide-level-start-column", shouldHideNormalPromotionLevelStartColumn(rows));
}

function renderNormalPromotionTableRows(content) {
    const rows = document.getElementById("normal-promotion-rows");
    if (!rows) {
        return;
    }
    const canWrite = hasPayrollFeatureWrite("NORMAL_PROMOTION_WRITE");
    updateNormalPromotionLevelStartColumnVisibility(content);
    document.getElementById("normal-promotion-select-all").checked = false;
    rows.innerHTML = (content || []).map(row => {
        const canProcess = Boolean(row.applyEligible);
        const canPromptOverdue = Boolean(row.overdueFromLastYear);
        const levelRequiredFirst = Boolean(row.levelPromotionRequiredFirst);
        const canClickApply = canWrite && (canProcess || canPromptOverdue || levelRequiredFirst);
        const canRollback = Boolean(row.rollbackEligible);
        const hideLevelStart = isNormalPromotionInstitutionRow(row);
        const applyButton = canWrite
            ? `<button class="row-action" data-normal-apply="${escapeHtml(row.payrollHistoryId)}" data-normal-overdue="${canPromptOverdue ? "true" : "false"}" data-normal-level-first="${levelRequiredFirst ? "true" : "false"}" type="button" ${canClickApply ? "" : "disabled"}>办理</button>`
            : "";
        const rollbackButton = canWrite
            ? `<button class="row-action danger-button" data-normal-rollback="${escapeHtml(row.payrollHistoryId)}" type="button" ${canRollback ? "" : "disabled"}>还原</button>`
            : "";
        return `
            <tr class="${canRollback ? "highlight-row" : ""}">
                <td class="col-select${canWrite ? "" : " hidden"}"><input type="checkbox" data-normal-select="${escapeHtml(row.payrollHistoryId)}" data-normal-eligible="${canProcess ? "true" : "false"}" data-normal-rollback="${canRollback ? "true" : "false"}" ${canWrite && (canProcess || canRollback) ? "" : "disabled"} aria-label="选择${escapeHtml(row.name)}"></td>
                <td class="col-org">${escapeHtml(row.organizationCode)}</td>
                <td class="col-code">${escapeHtml(row.personCode)}</td>
                <td class="col-name">${escapeHtml(row.name)}</td>
                <td class="col-period">${escapeHtml(row.calculationPeriod)}</td>
                <td class="col-position">${escapeHtml(row.positionName)}</td>
                <td class="col-grade">${escapeHtml(formatNormalPromotionGradeDisplay(row, false))}</td>
                <td class="col-grade">${escapeHtml(formatNormalPromotionGradeDisplay(row, true))}</td>
                <td class="col-tech col-level-start">${hideLevelStart ? "" : escapeHtml(row.levelAssessmentStartYear || "")}</td>
                <td class="col-tech">${escapeHtml(row.stepAssessmentStartYear || "")}</td>
                <td class="col-tech">${escapeHtml(row.nextStepAssessmentStartYear || "")}</td>
                <td class="col-years">${escapeHtml(row.qualifiedYears ?? "")}</td>
                <td class="col-years">${escapeHtml(row.requiredYears ?? "")}</td>
                <td class="col-money">${money(row.currentTotal)}</td>
                <td class="col-money">${money(row.promotedTotal)}</td>
                <td class="col-money difference-bad">${money(row.totalIncrease ?? row.increaseAmount)}</td>
                <td class="col-flag">${canRollback ? "已处理" : "待处理"}</td>
                <td class="col-note">${escapeHtml(row.note || "")}</td>
                <td class="col-action">
                    <button class="row-action" data-normal-detail="${escapeHtml(row.payrollHistoryId)}" type="button">明细</button>
                    ${applyButton}
                    ${rollbackButton}
                </td>
            </tr>
        `;
    }).join("");
    rows.querySelectorAll("button[data-normal-detail]").forEach(button => {
        button.addEventListener("click", () => openNormalPromotionDetailModal(button.dataset.normalDetail));
    });
    rows.querySelectorAll("button[data-normal-apply]").forEach(button => {
        button.addEventListener("click", () => applyPromotionAction(
            "normal",
            button.dataset.normalApply,
            button.dataset.normalOverdue === "true",
            button.dataset.normalLevelFirst === "true"));
    });
    rows.querySelectorAll("button[data-normal-rollback]").forEach(button => {
        button.addEventListener("click", () => rollbackPromotionAction("normal", button.dataset.normalRollback));
    });
}

function normalPromotionApplyPayload(row) {
    if (!row) {
        return null;
    }
    return {
        organizationCode: row.organizationCode || "",
        personCode: row.personCode || "",
        calculationPeriod: row.calculationPeriod || "",
        promotedGradeOrLevel: row.promotedGradeOrLevel || "",
        gradeSalaryLevel: row.gradeSalaryLevel || "",
        promotedBaseSalary: row.promotedBaseSalary,
        increaseAmount: row.increaseAmount,
        baseSalarySource: row.baseSalarySource || "",
    };
}

function normalPromotionRollbackPayload(row) {
    if (!row) {
        return null;
    }
    return {
        organizationCode: row.organizationCode || "",
        personCode: row.personCode || "",
    };
}

/** 列表本地重绘时按单位编码+人员编码排序（与后端 dwbm+grbm 一致）。 */
function sortRowsByOrgPerson(rows) {
    return (rows || []).slice().sort((a, b) => {
        const org = String(a?.organizationCode || "").localeCompare(String(b?.organizationCode || ""), "zh");
        if (org !== 0) {
            return org;
        }
        return String(a?.personCode || "").localeCompare(String(b?.personCode || ""), "zh");
    });
}

/** 办理成功后本地更新行状态，避免立刻全量重算列表。 */
function markNormalPromotionsProcessedLocally(successItems) {
    const items = (successItems || []).filter(item => item && (item.previousPayrollHistoryId || item.payrollHistoryId));
    if (!items.length) {
        return;
    }
    const includeApply = document.getElementById("normal-promotion-include-apply")?.checked ?? true;
    const includeProcessed = document.getElementById("normal-promotion-include-processed")?.checked ?? true;
    const nextById = { ...(state.normalPromotionRowsById || {}) };
    for (const item of items) {
        const previousId = item.previousPayrollHistoryId || item.payrollHistoryId;
        const newId = item.payrollHistoryId || previousId;
        const row = nextById[previousId];
        if (!row) {
            continue;
        }
        delete nextById[previousId];
        const changeType = item.changeType
            || normalStepChangeTypeLabel(row.currentPositionCode || row.positionCode, row.baseSalarySource);
        const nextStartYear = (row.calculationPeriod || "").length >= 4
            ? String(row.calculationPeriod).slice(0, 4)
            : (row.nextStepAssessmentStartYear || "");
        nextById[newId] = {
            ...row,
            payrollHistoryId: newId,
            changeType,
            applyEligible: false,
            overdueFromLastYear: false,
            levelPromotionRequiredFirst: false,
            eligible: false,
            rollbackEligible: true,
            nextStepAssessmentStartYear: nextStartYear,
            note: row.note || "",
        };
    }
    state.normalPromotionRowsById = nextById;
    let visible = sortRowsByOrgPerson(Object.values(nextById));
    if (!includeApply) {
        visible = visible.filter(row => row.rollbackEligible);
    }
    if (!includeProcessed) {
        visible = visible.filter(row => !row.rollbackEligible);
    }
    renderNormalPromotionTableRows(visible);
}

function markNormalPromotionsRolledBackLocally(successItems) {
    const items = (successItems || []).filter(item => item && (item.previousPayrollHistoryId || item.payrollHistoryId));
    if (!items.length) {
        return;
    }
    const includeApply = document.getElementById("normal-promotion-include-apply")?.checked ?? true;
    const includeProcessed = document.getElementById("normal-promotion-include-processed")?.checked ?? true;
    const nextById = { ...(state.normalPromotionRowsById || {}) };
    for (const item of items) {
        const processedId = item.previousPayrollHistoryId || item.payrollHistoryId;
        const restoredId = item.payrollHistoryId || processedId;
        const row = nextById[processedId];
        if (!row) {
            continue;
        }
        delete nextById[processedId];
        nextById[restoredId] = {
            ...row,
            payrollHistoryId: restoredId,
            changeType: "",
            applyEligible: true,
            overdueFromLastYear: false,
            levelPromotionRequiredFirst: false,
            eligible: true,
            rollbackEligible: false,
            note: row.note || "",
        };
    }
    state.normalPromotionRowsById = nextById;
    let visible = sortRowsByOrgPerson(Object.values(nextById));
    if (!includeApply) {
        visible = visible.filter(row => row.rollbackEligible);
    }
    if (!includeProcessed) {
        visible = visible.filter(row => !row.rollbackEligible);
    }
    renderNormalPromotionTableRows(visible);
}


function closeNormalPromotionDetailModal() {
    document.getElementById("normal-promotion-detail-modal")?.classList.add("hidden");
}

function renderNormalPromotionDetailContent(row) {
    const positionCode = row.positionCode || row.currentPositionCode;
    const gradeLabel = salaryStepCaption(positionCode);
    const baseSalaryLabel = row.baseSalarySource === "JUDICIAL_GRADE"
        ? "法检档次工资"
        : row.baseSalarySource === "WORKER_GRADE"
            ? "职务工资"
            : salaryStepSalaryCaption(positionCode);
    document.getElementById("normal-promotion-detail-summary").textContent =
        `${row.organizationCode || ""}-${row.personCode || ""} ${row.name || ""} / ${row.positionName || ""}`
        + (row.rollbackEligible ? "（已处理，可还原）" : row.applyEligible ? "（待处理）" : "");

    const metaRows = [
        positionChangeDetailCompareRow(
            "岗位",
            `${row.positionCode || ""} ${row.positionName || ""}`.trim() || "-",
            `${row.positionCode || ""} ${row.positionName || ""}`.trim() || "-"),
        positionChangeDetailCompareRow("级别", row.gradeSalaryLevel || "-", row.gradeSalaryLevel || "-"),
        positionChangeDetailCompareRow(gradeLabel, row.currentGradeOrLevel, row.promotedGradeOrLevel),
        positionChangeDetailCompareRow(
            gradeLabel === "薪级" ? "薪级考核起算年" : "档次考核起算年",
            row.stepAssessmentStartYear,
            row.nextStepAssessmentStartYear || row.stepAssessmentStartYear),
        isNormalPromotionInstitutionRow(row)
            ? ""
            : positionChangeDetailCompareRow("级别考核起算年", row.levelAssessmentStartYear || "-", row.levelAssessmentStartYear || "-"),
    ].join("");

    const salaryRows = (row.salaryComponents || []).length
        ? renderSalaryComponentCompareRows(row.salaryComponents)
        : [
            positionChangeDetailMoneyRow("职务工资", row.currentPositionSalary, row.promotedPositionSalary, null),
            positionChangeDetailMoneyRow(
                baseSalaryLabel,
                row.currentBaseSalary,
                row.promotedBaseSalary,
                null),
            positionChangeDetailMoneyRow(
                row.performanceAllowanceCaption || "生活性补贴",
                row.currentPerformanceAllowance,
                row.promotedPerformanceAllowance,
                null),
            row.showSubsidyAllowance !== false && row.subsidyAllowanceCaption
                ? positionChangeDetailMoneyRow(
                    row.subsidyAllowanceCaption || "工作性津贴",
                    row.currentSubsidyAllowance,
                    row.promotedSubsidyAllowance,
                    null)
                : "",
            positionChangeDetailMoneyRow("合计", row.currentTotal, row.promotedTotal, row.increaseAmount),
        ].filter(Boolean).join("");

    const processRows = [
        ...(row.explanationLines || []).map((line, index) => `
            <tr>
                <th>${index + 1}</th>
                <td>${escapeHtml(line)}</td>
            </tr>
        `),
        row.note ? `<tr><th>说明</th><td>${escapeHtml(row.note)}</td></tr>` : "",
        `<tr><th>考核</th><td>区间 ${escapeHtml(row.assessmentPeriod || "-")}，合格 ${escapeHtml(String(row.qualifiedYears ?? "-"))} 年 / 要求 ${escapeHtml(String(row.requiredYears ?? "-"))} 年，满足条件：${row.eligible ? "是" : "否"}</td></tr>`,
        `<tr><th>工资类型</th><td>${escapeHtml(baseSalarySourceName(row.baseSalarySource))}</td></tr>`,
    ].filter(Boolean).join("");

    document.getElementById("normal-promotion-detail-content").innerHTML = `
        <div class="detail-table-panel">
            <h4>档次/薪级与工资对照</h4>
            <table class="approval-component-table">
                <thead>
                    <tr>
                        <th class="approval-item-header">项目</th>
                        <th>变动前</th>
                        <th>变动后</th>
                        <th>增减</th>
                    </tr>
                </thead>
                <tbody>${metaRows}${salaryRows}</tbody>
            </table>
            <h4>试算过程</h4>
            <table class="approval-meta-table position-change-detail-steps">
                <thead>
                    <tr>
                        <th>步骤</th>
                        <th>说明</th>
                    </tr>
                </thead>
                <tbody>${processRows || `<tr><th>-</th><td>暂无试算说明</td></tr>`}</tbody>
            </table>
        </div>
    `;
}

async function openNormalPromotionDetailModal(payrollHistoryId) {
    const modal = document.getElementById("normal-promotion-detail-modal");
    const content = document.getElementById("normal-promotion-detail-content");
    const cached = state.normalPromotionRowsById?.[payrollHistoryId];
    if (cached) {
        renderNormalPromotionDetailContent(cached);
    } else {
        document.getElementById("normal-promotion-detail-summary").textContent = "正在加载明细...";
        content.innerHTML = `<div class="status">正在加载试算明细...</div>`;
    }
    modal.classList.remove("hidden");
    try {
        const yearQuery = normalPromotionYearParam();
        const row = await getJson(`/api/payroll/normal-promotions/${encodeURIComponent(payrollHistoryId)}${yearQuery ? `?${yearQuery}` : ""}`);
        renderNormalPromotionDetailContent(row);
    } catch (error) {
        if (!cached) {
            document.getElementById("normal-promotion-detail-summary").textContent = "加载明细失败";
            content.innerHTML = `<div class="status error">${escapeHtml(error.message || "加载失败")}</div>`;
        }
    }
}

async function loadLevelPromotions() {
    const organizationCode = selectedOrganizationCode("level-promotion-organization-code");
    const keyword = document.getElementById("level-promotion-keyword").value.trim();
    const page = String(state.levelPromotionPage || 0);
    const size = "20";
    const year = currentLevelPromotionYear();
    const params = new URLSearchParams({ page, size, year });
    if (organizationCode) {
        params.set("organizationCode", organizationCode);
    }
    if (keyword) {
        params.set("keyword", keyword);
    }
    if (!document.getElementById("level-promotion-include-apply").checked) {
        params.set("includeApply", "false");
    }
    if (!document.getElementById("level-promotion-include-processed").checked) {
        params.set("includeProcessed", "false");
    }
    const laterPeriodMode = levelPromotionLaterPeriodMode();
    if (laterPeriodMode) {
        params.set("laterPeriodMode", laterPeriodMode);
    }

    const status = document.getElementById("level-promotion-status");
    const rows = document.getElementById("level-promotion-rows");
    status.className = "status";
    status.textContent = "正在查询级别晋升试算...";
    rows.innerHTML = "";

    try {
        const result = await getJson(`/api/payroll/level-promotions?${params}`);
        const totalPages = Math.max(result.totalPages || 1, 1);
        if ((result.page || 0) >= totalPages && totalPages > 0 && (result.totalElements || 0) > 0) {
            state.levelPromotionPage = Math.max(totalPages - 1, 0);
            return loadLevelPromotions();
        }
        document.getElementById("level-promotion-select-all").checked = false;
        const byId = {};
        (result.content || []).forEach(row => {
            if (row?.payrollHistoryId) {
                byId[row.payrollHistoryId] = row;
            }
        });
        state.levelPromotionRowsById = byId;
        state.levelPromotionPage = result.page || 0;
        state.levelPromotionListMeta = {
            page: result.page,
            totalPages: result.totalPages,
            totalElements: result.totalElements,
            year,
        };
        renderLevelPromotionTableRows(result.content || []);
        const total = result.totalElements || 0;
        status.textContent = total
            ? `共 ${total} 条试算记录（${year} 年），第 ${state.levelPromotionPage + 1} / ${totalPages} 页`
            : `未查询到 ${year} 年试算记录`;
        renderLevelPromotionPagination(total, totalPages);
    } catch (error) {
        renderLevelPromotionPagination(0, 1);
        showError(status, error);
    }
}

function formatLevelStep(level, step) {
    const levelText = level == null || level === "" ? "" : String(level).trim();
    const stepText = step == null || step === "" ? "" : String(step).trim();
    if (!levelText && !stepText) {
        return "";
    }
    if (!levelText) {
        return stepText;
    }
    if (!stepText) {
        return levelText;
    }
    return `${levelText}-${stepText}`;
}

/** 正常档次/薪级：级别工资显示「级别-档次」，薪级/法检档次仅显示档次。 */
function formatNormalPromotionGradeDisplay(row, promoted) {
    const gradeOrLevel = promoted ? row?.promotedGradeOrLevel : row?.currentGradeOrLevel;
    const source = String(row?.baseSalarySource || "").toUpperCase();
    if (source === "SALARY_LEVEL" || source === "JUDICIAL_GRADE" || source === "WORKER_GRADE") {
        return gradeOrLevel == null ? "" : String(gradeOrLevel).trim();
    }
    return formatLevelStep(row?.gradeSalaryLevel, gradeOrLevel);
}

function renderLevelPromotionTableRows(content) {
    const rows = document.getElementById("level-promotion-rows");
    if (!rows) {
        return;
    }
    const canWrite = hasLevelPromotionWrite();
    document.getElementById("level-promotion-select-all").checked = false;
    rows.innerHTML = (content || []).map(row => {
        const canProcess = Boolean(row.applyEligible);
        const canRollback = Boolean(row.rollbackEligible);
        const canPromptOverdue = Boolean(row.overdueFromLastYear);
        const canPromptEarlierStep = Boolean(row.earlierStepPromotionPending);
        const canClickApply = canWrite && (canProcess || canPromptOverdue || canPromptEarlierStep);
        const canSelect = canWrite && (canProcess || canRollback);
        const applyButton = canWrite
            ? `<button class="row-action" data-level-apply="${escapeHtml(row.payrollHistoryId)}" data-level-overdue="${canPromptOverdue ? "true" : "false"}" data-level-earlier-step="${canPromptEarlierStep ? "true" : "false"}" type="button" ${canClickApply ? "" : "disabled"}>处理</button>`
            : "";
        const rollbackButton = canWrite
            ? `<button class="row-action danger-button" data-level-rollback="${escapeHtml(row.payrollHistoryId)}" type="button" ${row.rollbackEligible ? "" : "disabled"}>还原</button>`
            : "";
        return `
            <tr class="${canRollback ? "highlight-row" : ""}">
                <td class="col-select${canWrite ? "" : " hidden"}"><input type="checkbox" data-level-select="${escapeHtml(row.payrollHistoryId)}" data-level-eligible="${canProcess ? "true" : "false"}" data-level-rollback="${canRollback ? "true" : "false"}" ${canSelect ? "" : "disabled"} aria-label="选择${escapeHtml(row.name)}"></td>
                <td class="col-org">${escapeHtml(row.organizationCode)}</td>
                <td class="col-code">${escapeHtml(row.personCode)}</td>
                <td class="col-name">${escapeHtml(row.name)}</td>
                <td class="col-period">${escapeHtml(row.calculationPeriod)}</td>
                <td class="col-change">${escapeHtml(row.changeType)}</td>
                <td class="col-position">${escapeHtml(row.positionName)}</td>
                <td class="col-level">${escapeHtml(formatLevelStep(row.currentLevel, row.currentStep))}</td>
                <td class="col-level">${escapeHtml(formatLevelStep(row.promotedLevel, row.promotedStep))}</td>
                <td class="col-tech">${escapeHtml(row.levelAssessmentStartYear || "")}</td>
                <td class="col-tech">${escapeHtml(row.stepAssessmentStartYear || "")}</td>
                <td class="col-tech">${escapeHtml(row.nextLevelAssessmentStartYear || "")}</td>
                <td class="col-tech">${escapeHtml(row.nextStepAssessmentStartYear || "")}</td>
                <td class="col-years">${escapeHtml(row.qualifiedYearsForLevel ?? "")}</td>
                <td class="col-years">${escapeHtml(row.qualifiedYearsForStep ?? "")}</td>
                <td class="col-flag">${row.levelPromotionDue ? "是" : "否"}</td>
                <td class="col-flag">${row.reformLevelRollingDue ? "是" : "否"}</td>
                <td class="col-flag">${row.stepPromotionDue ? "是" : "否"}</td>
                <td class="col-flag">${row.gradeIncreaseExceedsStepDifference ? "是" : "否"}</td>
                <td class="col-money">${money(row.currentGradeSalary)}</td>
                <td class="col-money">${money(row.promotedGradeSalary)}</td>
                <td class="col-money">${money(row.increaseAmount)}</td>
                <td class="col-note">${escapeHtml(row.note || "")}</td>
                <td class="col-action">
                    <button class="row-action" data-level-detail="${escapeHtml(row.payrollHistoryId)}" type="button">明细</button>
                    ${applyButton}
                    ${rollbackButton}
                </td>
            </tr>
        `;
    }).join("");
    rows.querySelectorAll("button[data-level-detail]").forEach(button => {
        button.addEventListener("click", () => openLevelPromotionDetailModal(button.dataset.levelDetail));
    });
    rows.querySelectorAll("button[data-level-apply]").forEach(button => {
        button.addEventListener("click", () => applyPromotionAction(
            "level",
            button.dataset.levelApply,
            button.dataset.levelOverdue === "true",
            false,
            button.dataset.levelEarlierStep === "true"));
    });
    rows.querySelectorAll("button[data-level-rollback]").forEach(button => {
        button.addEventListener("click", () => rollbackPromotionAction("level", button.dataset.levelRollback));
    });
}

/** 办理成功后本地更新行状态，避免立刻全量重算列表。 */
function markLevelPromotionsProcessedLocally(successItems) {
    const items = (successItems || []).filter(item => item && (item.previousPayrollHistoryId || item.payrollHistoryId));
    if (!items.length) {
        return;
    }
    const includeApply = document.getElementById("level-promotion-include-apply")?.checked ?? true;
    const includeProcessed = document.getElementById("level-promotion-include-processed")?.checked ?? true;
    const nextById = { ...(state.levelPromotionRowsById || {}) };
    for (const item of items) {
        const previousId = item.previousPayrollHistoryId || item.payrollHistoryId;
        const newId = item.payrollHistoryId || previousId;
        const row = nextById[previousId];
        if (!row) {
            continue;
        }
        delete nextById[previousId];
        const changeType = item.changeType
            || (row.reformLevelRollingDue ? "级别滚动" : "正常级别");
        nextById[newId] = {
            ...row,
            payrollHistoryId: newId,
            changeType,
            applyEligible: false,
            overdueFromLastYear: false,
            rollbackEligible: true,
            note: row.note || "",
        };
    }
    state.levelPromotionRowsById = nextById;
    let visible = sortRowsByOrgPerson(Object.values(nextById));
    if (!includeApply) {
        visible = visible.filter(row => row.rollbackEligible);
    }
    if (!includeProcessed) {
        visible = visible.filter(row => !row.rollbackEligible);
    }
    renderLevelPromotionTableRows(visible);
}

function markLevelPromotionsRolledBackLocally(successItems) {
    const items = (successItems || []).filter(item => item && (item.previousPayrollHistoryId || item.payrollHistoryId));
    if (!items.length) {
        return;
    }
    const includeApply = document.getElementById("level-promotion-include-apply")?.checked ?? true;
    const includeProcessed = document.getElementById("level-promotion-include-processed")?.checked ?? true;
    const nextById = { ...(state.levelPromotionRowsById || {}) };
    for (const item of items) {
        const processedId = item.previousPayrollHistoryId || item.payrollHistoryId;
        const restoredId = item.payrollHistoryId || processedId;
        const row = nextById[processedId];
        if (!row) {
            continue;
        }
        delete nextById[processedId];
        nextById[restoredId] = {
            ...row,
            payrollHistoryId: restoredId,
            changeType: "",
            applyEligible: true,
            overdueFromLastYear: false,
            rollbackEligible: false,
            note: row.note || "",
        };
    }
    state.levelPromotionRowsById = nextById;
    let visible = sortRowsByOrgPerson(Object.values(nextById));
    if (!includeApply) {
        visible = visible.filter(row => row.rollbackEligible);
    }
    if (!includeProcessed) {
        visible = visible.filter(row => !row.rollbackEligible);
    }
    renderLevelPromotionTableRows(visible);
}

function levelPromotionRollbackPayload(row) {
    if (!row) {
        return null;
    }
    return {
        organizationCode: row.organizationCode || "",
        personCode: row.personCode || "",
    };
}

function closeLevelPromotionDetailModal() {
    document.getElementById("level-promotion-detail-modal")?.classList.add("hidden");
}

function renderLevelPromotionDetailContent(row) {
    document.getElementById("level-promotion-detail-summary").textContent =
        `${row.organizationCode || ""}-${row.personCode || ""} ${row.name || ""} / ${row.positionName || ""}`
        + (row.rollbackEligible ? "（已处理，可还原）" : row.applyEligible ? "（待处理）" : "");

    const metaRows = [
        positionChangeDetailCompareRow(
            "岗位",
            `${row.positionCode || ""} ${row.positionName || ""}`.trim() || "-",
            `${row.positionCode || ""} ${row.positionName || ""}`.trim() || "-"),
        positionChangeDetailCompareRow("级别", row.currentLevel, row.promotedLevel),
        positionChangeDetailCompareRow("档次", row.currentStep, row.promotedStep),
        positionChangeDetailCompareRow(
            "级别考核起算年",
            row.levelAssessmentStartYear,
            row.nextLevelAssessmentStartYear),
        positionChangeDetailCompareRow(
            "档次考核起算年",
            row.stepAssessmentStartYear,
            row.nextStepAssessmentStartYear),
    ].join("");

    const salaryRows = (row.salaryComponents || []).length
        ? renderSalaryComponentCompareRows(row.salaryComponents)
        : [
            positionChangeDetailMoneyRow("职务工资", row.currentPositionSalary, row.promotedPositionSalary, null),
            positionChangeDetailMoneyRow(
                "级别工资",
                row.currentGradeSalary,
                row.promotedGradeSalary,
                null),
            positionChangeDetailMoneyRow(
                row.performanceAllowanceCaption || "生活性补贴",
                row.currentPerformanceAllowance,
                row.promotedPerformanceAllowance,
                null),
            row.showSubsidyAllowance !== false && row.subsidyAllowanceCaption
                ? positionChangeDetailMoneyRow(
                    row.subsidyAllowanceCaption || "工作性津贴",
                    row.currentSubsidyAllowance,
                    row.promotedSubsidyAllowance,
                    null)
                : "",
            positionChangeDetailMoneyRow("合计", row.currentTotal, row.promotedTotal, row.increaseAmount),
        ].filter(Boolean).join("");

    const processRows = [
        ...(row.explanationLines || []).map((line, index) => `
            <tr>
                <th>${index + 1}</th>
                <td>${escapeHtml(line)}</td>
            </tr>
        `),
        row.note ? `<tr><th>说明</th><td>${escapeHtml(row.note)}</td></tr>` : "",
        `<tr><th>条件</th><td>晋级别 ${row.levelPromotionDue ? "是" : "否"}，级滚动 ${row.reformLevelRollingDue ? "是" : "否"}，晋档次 ${row.stepPromotionDue ? "是" : "否"}，超档差 ${row.gradeIncreaseExceedsStepDifference ? "是" : "否"}</td></tr>`,
        `<tr><th>考核</th><td>级别合格 ${escapeHtml(String(row.qualifiedYearsForLevel ?? "-"))} 年，档次合格 ${escapeHtml(String(row.qualifiedYearsForStep ?? "-"))} 年，适用：${row.eligible ? "是" : "否"}</td></tr>`,
    ].filter(Boolean).join("");

    document.getElementById("level-promotion-detail-content").innerHTML = `
        <div class="detail-table-panel">
            <h4>级别/档次与工资对照</h4>
            <table class="approval-component-table">
                <thead>
                    <tr>
                        <th class="approval-item-header">项目</th>
                        <th>变动前</th>
                        <th>变动后</th>
                        <th>增减</th>
                    </tr>
                </thead>
                <tbody>${metaRows}${salaryRows}</tbody>
            </table>
            <h4>试算过程</h4>
            <table class="approval-meta-table position-change-detail-steps">
                <thead>
                    <tr>
                        <th>步骤</th>
                        <th>说明</th>
                    </tr>
                </thead>
                <tbody>${processRows || `<tr><th>-</th><td>暂无试算说明</td></tr>`}</tbody>
            </table>
        </div>
    `;
}

async function openLevelPromotionDetailModal(payrollHistoryId) {
    const modal = document.getElementById("level-promotion-detail-modal");
    const content = document.getElementById("level-promotion-detail-content");
    document.getElementById("level-promotion-detail-summary").textContent = "正在加载明细...";
    content.innerHTML = `<div class="status">正在加载试算明细...</div>`;
    modal.classList.remove("hidden");
    try {
        const yearQuery = levelPromotionYearParam();
        const row = await getJson(`/api/payroll/level-promotions/${encodeURIComponent(payrollHistoryId)}${yearQuery ? `?${yearQuery}` : ""}`);
        renderLevelPromotionDetailContent(row);
    } catch (error) {
        document.getElementById("level-promotion-detail-summary").textContent = "加载明细失败";
        content.innerHTML = `<div class="status error">${escapeHtml(error.message || "加载失败")}</div>`;
    }
}

const OVERDUE_LEVEL_PROMOTION_MESSAGE = "上年符合级别晋升条件，请将晋升年度切换到上年后再办理。";
const OVERDUE_NORMAL_PROMOTION_MESSAGE = "上年符合档次/薪级晋升条件，请将晋升年度切换到上年后再办理。";
const EARLIER_STEP_BEFORE_LEVEL_MESSAGE = "上年符合档次晋升条件";
const LEVEL_PROMOTION_REQUIRED_FIRST_MESSAGE = "同年符合级别晋升条件，请先到级别晋升办理级别，再回本菜单办理档次/薪级。";

function warnOverduePromotionAction(type, overdueFromLastYear, earlierStepPromotionPending = false) {
    if (type === "level" && earlierStepPromotionPending) {
        window.alert(EARLIER_STEP_BEFORE_LEVEL_MESSAGE);
        return true;
    }
    if (!overdueFromLastYear) {
        return false;
    }
    window.alert(type === "normal" ? OVERDUE_NORMAL_PROMOTION_MESSAGE : OVERDUE_LEVEL_PROMOTION_MESSAGE);
    return true;
}

function levelPromotionApplyPayload(row) {
    if (!row) {
        return null;
    }
    return {
        organizationCode: row.organizationCode || "",
        personCode: row.personCode || "",
        reformLevelRollingDue: Boolean(row.reformLevelRollingDue),
        calculationPeriod: row.calculationPeriod || "",
        promotedLevel: row.promotedLevel || "",
        promotedStep: row.promotedStep || "",
        nextLevelAssessmentStartYear: row.nextLevelAssessmentStartYear || "",
        nextStepAssessmentStartYear: row.nextStepAssessmentStartYear || "",
        promotedGradeSalary: row.promotedGradeSalary,
        increaseAmount: row.increaseAmount,
    };
}

async function applyPromotionAction(type, payrollHistoryId, overdueFromLastYear = false, levelPromotionRequiredFirst = false, earlierStepPromotionPending = false) {
    const writeConfig = type === "normal"
        ? { permission: "NORMAL_PROMOTION_WRITE", name: "正常档次/薪级晋升" }
        : { permission: "LEVEL_PROMOTION_WRITE", name: "级别晋升" };
    if (!ensurePayrollFeatureWrite(writeConfig.permission, writeConfig.name)) {
        return;
    }
    if (levelPromotionRequiredFirst) {
        window.alert(LEVEL_PROMOTION_REQUIRED_FIRST_MESSAGE);
        return;
    }
    if (warnOverduePromotionAction(type, overdueFromLastYear, earlierStepPromotionPending)) {
        return;
    }
    const moduleName = type === "normal" ? "正常档次/薪级晋升" : "级别晋升";
    if (!confirm(`确认按当前试算结果处理${moduleName}？系统会新增一条当前工资变动记录，并将原当前记录转为历史记录。`)) {
        return;
    }
    const status = document.getElementById(type === "normal" ? "normal-promotion-status" : "level-promotion-status");
    status.className = "status";
    status.textContent = `正在处理${moduleName}...`;
    try {
        const path = type === "normal" ? "normal-promotions" : "level-promotions";
        const yearQuery = type === "normal" ? normalPromotionYearParam() : levelPromotionYearParam();
        const applyUrl = `/api/payroll/${path}/${encodeURIComponent(payrollHistoryId)}/apply${yearQuery ? `?${yearQuery}` : ""}`;
        let body = {};
        if (type === "level") {
            const row = state.levelPromotionRowsById?.[payrollHistoryId];
            body = levelPromotionApplyPayload(row);
            if (!body) {
                throw new Error("未找到试算结果，请重新查询后再办理。");
            }
        } else if (type === "normal") {
            const row = state.normalPromotionRowsById?.[payrollHistoryId];
            body = normalPromotionApplyPayload(row);
            if (!body) {
                throw new Error("未找到试算结果，请重新查询后再办理。");
            }
        }
        const writeStarted = performance.now();
        const result = await postJson(applyUrl, body);
        const writeMs = Math.round(performance.now() - writeStarted);
        const uiStarted = performance.now();
        if (type === "normal") {
            markNormalPromotionsProcessedLocally([{
                previousPayrollHistoryId: payrollHistoryId,
                payrollHistoryId: result?.payrollHistoryId || payrollHistoryId,
                changeType: result?.changeType,
            }]);
        } else {
            markLevelPromotionsProcessedLocally([{
                previousPayrollHistoryId: payrollHistoryId,
                payrollHistoryId: result?.payrollHistoryId || payrollHistoryId,
                changeType: result?.changeType,
            }]);
        }
        const uiMs = Math.round(performance.now() - uiStarted);
        status.textContent = `${(result && result.message) || `${moduleName}处理完成`}（写库 ${writeMs}ms，界面更新 ${uiMs}ms，未重算列表）`;
    } catch (error) {
        showError(status, error);
    }
}

async function applySelectedLevelPromotions() {
    if (!ensurePayrollFeatureWrite("LEVEL_PROMOTION_WRITE", "级别晋升")) {
        return;
    }
    const selectedIds = Array.from(document.querySelectorAll("[data-level-select]:checked"))
        .filter(checkbox => checkbox.dataset.levelEligible === "true")
        .map(checkbox => checkbox.dataset.levelSelect)
        .filter(Boolean);
    const status = document.getElementById("level-promotion-status");
    if (!selectedIds.length) {
        status.className = "status error";
        status.textContent = "请先勾选需要处理的级别晋升记录。";
        return;
    }
    if (!confirm(`确认批量处理勾选的 ${selectedIds.length} 条级别晋升记录？`)) {
        return;
    }
    const items = [];
    for (const id of selectedIds) {
        const row = state.levelPromotionRowsById?.[id];
        const payload = levelPromotionApplyPayload(row);
        if (!payload) {
            status.className = "status error";
            status.textContent = "部分勾选记录缺少试算结果，请重新查询后再办理。";
            return;
        }
        items.push({ payrollHistoryId: id, ...payload });
    }
    status.className = "status";
    status.textContent = `正在批量处理 ${items.length} 条...`;
    const writeStarted = performance.now();
    try {
        const yearQuery = levelPromotionYearParam();
        const result = await postJson(
            `/api/payroll/level-promotions/batch-apply${yearQuery ? `?${yearQuery}` : ""}`,
            { items });
        const writeMs = Math.round(performance.now() - writeStarted);
        const uiStarted = performance.now();
        const successItems = (result?.successes && result.successes.length)
            ? result.successes
            : (result?.successIds || []).map(id => ({
                previousPayrollHistoryId: id,
                payrollHistoryId: id,
            }));
        markLevelPromotionsProcessedLocally(successItems);
        const uiMs = Math.round(performance.now() - uiStarted);
        status.className = (result?.failureCount || 0) ? "status error" : "status";
        const baseMessage = result?.message
            || `批量处理完成：成功 ${result?.successCount ?? 0} 条，失败 ${result?.failureCount ?? 0} 条。`;
        status.textContent = `${baseMessage}（写库 ${writeMs}ms，界面更新 ${uiMs}ms，未重算列表）`;
        console.info("[level-promotion-batch]", { writeMs, uiMs, successCount: result?.successCount, failureCount: result?.failureCount });
    } catch (error) {
        showError(status, error);
    }
}

async function applyAllEligibleLevelPromotions() {
    if (!ensurePayrollFeatureWrite("LEVEL_PROMOTION_WRITE", "级别晋升")) {
        return;
    }
    const status = document.getElementById("level-promotion-status");
    const organizationCode = selectedOrganizationCode("level-promotion-organization-code");
    const keyword = document.getElementById("level-promotion-keyword").value.trim();
    const year = currentLevelPromotionYear();
    status.className = "status";
    status.textContent = "正在获取全部符合条件的人员...";

    let items = [];
    try {
        const baseParams = { year, includeApply: "true", includeProcessed: "false", laterPeriodMode: levelPromotionLaterPeriodMode() };
        if (organizationCode) {
            baseParams.organizationCode = organizationCode;
        }
        if (keyword) {
            baseParams.keyword = keyword;
        }
        const rows = await collectAllPromotionRows("/api/payroll/level-promotions", baseParams, status, "正在获取符合条件的人员");
        items = rows
            .filter(row => row.applyEligible && row.payrollHistoryId)
            .map(row => ({ payrollHistoryId: row.payrollHistoryId, ...levelPromotionApplyPayload(row) }))
            .filter(item => item.organizationCode && item.personCode);
    } catch (error) {
        showError(status, error);
        return;
    }

    if (!items.length) {
        status.className = "status";
        status.textContent = "没有符合条件的级别晋升记录。";
        return;
    }
    const scopeText = organizationCode ? `单位 ${organizationCode}` : "全部可见单位";
    if (!confirm(`确认处理${scopeText} ${year} 年全部符合条件的 ${items.length} 人？`)) {
        status.className = "status";
        status.textContent = "已取消批量处理全部。";
        return;
    }

    status.className = "status";
    status.textContent = `正在批量处理全部 ${items.length} 条...`;
    const writeStarted = performance.now();
    try {
        const yearQuery = levelPromotionYearParam();
        const result = await postJson(
            `/api/payroll/level-promotions/batch-apply${yearQuery ? `?${yearQuery}` : ""}`,
            { items });
        const writeMs = Math.round(performance.now() - writeStarted);
        const uiStarted = performance.now();
        const successItems = (result?.successes && result.successes.length)
            ? result.successes
            : (result?.successIds || []).map(id => ({
                previousPayrollHistoryId: id,
                payrollHistoryId: id,
            }));
        markLevelPromotionsProcessedLocally(successItems);
        const uiMs = Math.round(performance.now() - uiStarted);
        status.className = (result?.failureCount || 0) ? "status error" : "status";
        const baseMessage = result?.message
            || `批量处理全部完成：成功 ${result?.successCount ?? 0} 条，失败 ${result?.failureCount ?? 0} 条。`;
        status.textContent = `${baseMessage}（写库 ${writeMs}ms，界面更新 ${uiMs}ms，未重算列表）`;
        console.info("[level-promotion-batch-all]", { writeMs, uiMs, successCount: result?.successCount, failureCount: result?.failureCount });
    } catch (error) {
        showError(status, error);
    }
}

async function rollbackSelectedLevelPromotions() {
    if (!ensurePayrollFeatureWrite("LEVEL_PROMOTION_WRITE", "级别晋升")) {
        return;
    }
    const selectedIds = Array.from(document.querySelectorAll("[data-level-select]:checked"))
        .filter(checkbox => checkbox.dataset.levelRollback === "true")
        .map(checkbox => checkbox.dataset.levelSelect)
        .filter(Boolean);
    const status = document.getElementById("level-promotion-status");
    if (!selectedIds.length) {
        status.className = "status error";
        status.textContent = "请先勾选需要还原的已处理级别晋升记录。";
        return;
    }
    if (!confirm(`确认还原勾选的 ${selectedIds.length} 条级别晋升？系统会删除对应链头记录并恢复上一条工资记录。`)) {
        return;
    }
    const items = [];
    for (const id of selectedIds) {
        const row = state.levelPromotionRowsById?.[id];
        const payload = levelPromotionRollbackPayload(row);
        if (!payload || !payload.organizationCode || !payload.personCode) {
            status.className = "status error";
            status.textContent = "部分勾选记录缺少人员信息，请重新查询后再还原。";
            return;
        }
        items.push({ payrollHistoryId: id, ...payload });
    }
    status.className = "status";
    status.textContent = `正在还原勾选 ${items.length} 条...`;
    const writeStarted = performance.now();
    try {
        const result = await postJson("/api/payroll/level-promotions/batch-rollback", { items });
        const writeMs = Math.round(performance.now() - writeStarted);
        const uiStarted = performance.now();
        const successItems = (result?.successes && result.successes.length)
            ? result.successes
            : (result?.successIds || []).map(id => ({
                previousPayrollHistoryId: id,
                payrollHistoryId: id,
            }));
        markLevelPromotionsRolledBackLocally(successItems);
        const uiMs = Math.round(performance.now() - uiStarted);
        status.className = (result?.failureCount || 0) ? "status error" : "status";
        const baseMessage = result?.message
            || `还原勾选完成：成功 ${result?.successCount ?? 0} 条，失败 ${result?.failureCount ?? 0} 条。`;
        status.textContent = `${baseMessage}（写库 ${writeMs}ms，界面更新 ${uiMs}ms，未重算列表）`;
        console.info("[level-promotion-batch-rollback-selected]", {
            writeMs,
            uiMs,
            successCount: result?.successCount,
            failureCount: result?.failureCount,
        });
    } catch (error) {
        showError(status, error);
    }
}

async function rollbackAllProcessedLevelPromotions() {
    if (!ensurePayrollFeatureWrite("LEVEL_PROMOTION_WRITE", "级别晋升")) {
        return;
    }
    const status = document.getElementById("level-promotion-status");
    const organizationCode = selectedOrganizationCode("level-promotion-organization-code");
    const keyword = document.getElementById("level-promotion-keyword").value.trim();
    const year = currentLevelPromotionYear();
    status.className = "status";
    status.textContent = "正在获取全部已处理的人员...";

    let rollbackItems = [];
    try {
        const baseParams = { year, includeApply: "false", includeProcessed: "true" };
        if (organizationCode) {
            baseParams.organizationCode = organizationCode;
        }
        if (keyword) {
            baseParams.keyword = keyword;
        }
        const rows = await collectAllPromotionRows("/api/payroll/level-promotions", baseParams, status, "正在获取已处理的人员");
        rollbackItems = rows
            .filter(row => row.rollbackEligible && row.payrollHistoryId)
            .map(row => ({
                payrollHistoryId: row.payrollHistoryId,
                organizationCode: row.organizationCode || "",
                personCode: row.personCode || "",
            }));
    } catch (error) {
        showError(status, error);
        return;
    }

    if (!rollbackItems.length) {
        status.className = "status";
        status.textContent = `没有可还原的 ${year} 年级别晋升记录。`;
        return;
    }
    const scopeText = organizationCode ? `单位 ${organizationCode}` : "全部可见单位";
    if (!confirm(`确认还原${scopeText} ${year} 年全部已处理的 ${rollbackItems.length} 人？系统会删除对应链头记录并恢复上一条工资记录。`)) {
        status.className = "status";
        status.textContent = "已取消批量还原。";
        return;
    }

    status.className = "status";
    status.textContent = `正在批量还原 ${rollbackItems.length} 条...`;
    const writeStarted = performance.now();
    try {
        const result = await postJson("/api/payroll/level-promotions/batch-rollback", { items: rollbackItems });
        const writeMs = Math.round(performance.now() - writeStarted);
        const uiStarted = performance.now();
        const successItems = (result?.successes && result.successes.length)
            ? result.successes
            : (result?.successIds || []).map(id => ({
                previousPayrollHistoryId: id,
                payrollHistoryId: id,
            }));
        markLevelPromotionsRolledBackLocally(successItems);
        const uiMs = Math.round(performance.now() - uiStarted);
        status.className = (result?.failureCount || 0) ? "status error" : "status";
        const baseMessage = result?.message
            || `批量还原完成：成功 ${result?.successCount ?? 0} 条，失败 ${result?.failureCount ?? 0} 条。`;
        status.textContent = `${baseMessage}（写库 ${writeMs}ms，界面更新 ${uiMs}ms，未重算列表）`;
        console.info("[level-promotion-batch-rollback]", {
            writeMs,
            uiMs,
            successCount: result?.successCount,
            failureCount: result?.failureCount,
        });
    } catch (error) {
        showError(status, error);
    }
}

async function applySelectedNormalPromotions() {
    if (!ensurePayrollFeatureWrite("NORMAL_PROMOTION_WRITE", "正常档次/薪级晋升")) {
        return;
    }
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
    const items = [];
    for (const id of selectedIds) {
        const row = state.normalPromotionRowsById?.[id];
        const payload = normalPromotionApplyPayload(row);
        if (!payload) {
            status.className = "status error";
            status.textContent = "部分勾选记录缺少试算结果，请重新查询后再办理。";
            return;
        }
        items.push({ payrollHistoryId: id, ...payload });
    }
    status.className = "status";
    status.textContent = `正在批量处理 ${items.length} 条...`;
    const writeStarted = performance.now();
    try {
        const yearQuery = normalPromotionYearParam();
        const result = await postJson(
            `/api/payroll/normal-promotions/batch-apply${yearQuery ? `?${yearQuery}` : ""}`,
            { items });
        const writeMs = Math.round(performance.now() - writeStarted);
        const uiStarted = performance.now();
        const successItems = (result?.successes && result.successes.length)
            ? result.successes
            : (result?.successIds || []).map(id => ({
                previousPayrollHistoryId: id,
                payrollHistoryId: id,
            }));
        markNormalPromotionsProcessedLocally(successItems);
        const uiMs = Math.round(performance.now() - uiStarted);
        status.className = (result?.failureCount || 0) ? "status error" : "status";
        const baseMessage = result?.message
            || `批量处理完成：成功 ${result?.successCount ?? 0} 条，失败 ${result?.failureCount ?? 0} 条。`;
        status.textContent = `${baseMessage}（写库 ${writeMs}ms，界面更新 ${uiMs}ms，未重算列表）`;
        console.info("[normal-promotion-batch]", { writeMs, uiMs, successCount: result?.successCount, failureCount: result?.failureCount });
    } catch (error) {
        showError(status, error);
    }
}

async function collectAllPromotionRows(path, baseParams, status, progressLabel) {
    const rows = [];
    let page = 0;
    const size = 200;
    while (true) {
        const params = new URLSearchParams(baseParams);
        params.set("page", String(page));
        params.set("size", String(size));
        const result = await getJson(`${path}?${params}`);
        const content = result.content || [];
        rows.push(...content);
        const totalPages = Math.max(result.totalPages || 0, 1);
        if (status && progressLabel) {
            status.textContent = `${progressLabel} ${rows.length} / ${result.totalElements ?? rows.length}...`;
        }
        page++;
        if (content.length === 0 || page >= totalPages) {
            break;
        }
    }
    return rows;
}

async function applyAllEligibleNormalPromotions() {
    if (!ensurePayrollFeatureWrite("NORMAL_PROMOTION_WRITE", "正常档次/薪级晋升")) {
        return;
    }
    const status = document.getElementById("normal-promotion-status");
    const organizationCode = selectedOrganizationCode("normal-promotion-organization-code");
    const keyword = document.getElementById("normal-promotion-keyword").value.trim();
    const year = currentNormalPromotionYear();
    status.className = "status";
    status.textContent = "正在获取全部符合条件的人员...";

    let items = [];
    try {
        const baseParams = { year, laterPeriodMode: normalPromotionLaterPeriodMode() };
        if (organizationCode) {
            baseParams.organizationCode = organizationCode;
        }
        if (keyword) {
            baseParams.keyword = keyword;
        }
        baseParams.includeProcessed = "false";
        const rows = await collectAllPromotionRows("/api/payroll/normal-promotions", baseParams, status, "正在获取符合条件的人员");
        items = rows
            .filter(row => row.applyEligible && row.payrollHistoryId && Number(row.increaseAmount) > 0)
            .map(row => ({ payrollHistoryId: row.payrollHistoryId, ...normalPromotionApplyPayload(row) }))
            .filter(item => item.organizationCode && item.personCode);
    } catch (error) {
        showError(status, error);
        return;
    }

    if (!items.length) {
        status.className = "status";
        status.textContent = "没有符合条件的正常档次/薪级晋升记录。";
        return;
    }
    const scopeText = organizationCode ? `单位 ${organizationCode}` : "全部可见单位";
    if (!confirm(`确认处理${scopeText} ${year} 年全部符合条件的 ${items.length} 人？`)) {
        status.className = "status";
        status.textContent = "已取消批量处理。";
        return;
    }

    status.className = "status";
    status.textContent = `正在批量处理 ${items.length} 条...`;
    const writeStarted = performance.now();
    try {
        const yearQuery = normalPromotionYearParam();
        const result = await postJson(
            `/api/payroll/normal-promotions/batch-apply${yearQuery ? `?${yearQuery}` : ""}`,
            { items });
        const writeMs = Math.round(performance.now() - writeStarted);
        const uiStarted = performance.now();
        const successItems = (result?.successes && result.successes.length)
            ? result.successes
            : (result?.successIds || []).map(id => ({
                previousPayrollHistoryId: id,
                payrollHistoryId: id,
            }));
        markNormalPromotionsProcessedLocally(successItems);
        const uiMs = Math.round(performance.now() - uiStarted);
        status.className = (result?.failureCount || 0) ? "status error" : "status";
        const baseMessage = result?.message
            || `批量处理完成：成功 ${result?.successCount ?? 0} 条，失败 ${result?.failureCount ?? 0} 条。`;
        status.textContent = `${baseMessage}（写库 ${writeMs}ms，界面更新 ${uiMs}ms，未重算列表）`;
        console.info("[normal-promotion-batch-all]", { writeMs, uiMs, successCount: result?.successCount, failureCount: result?.failureCount });
    } catch (error) {
        showError(status, error);
    }
}

async function rollbackAllProcessedNormalPromotions() {
    if (!ensurePayrollFeatureWrite("NORMAL_PROMOTION_WRITE", "正常档次/薪级晋升")) {
        return;
    }
    const status = document.getElementById("normal-promotion-status");
    const organizationCode = selectedOrganizationCode("normal-promotion-organization-code");
    const keyword = document.getElementById("normal-promotion-keyword").value.trim();
    const year = currentNormalPromotionYear();
    status.className = "status";
    status.textContent = "正在获取全部已处理的人员...";

    let rollbackItems = [];
    try {
        const baseParams = { year };
        if (organizationCode) {
            baseParams.organizationCode = organizationCode;
        }
        if (keyword) {
            baseParams.keyword = keyword;
        }
        baseParams.includeApply = "false";
        const rows = await collectAllPromotionRows("/api/payroll/normal-promotions", baseParams, status, "正在获取已处理的人员");
        rollbackItems = rows
            .filter(row => row.rollbackEligible && row.payrollHistoryId)
            .map(row => ({
                payrollHistoryId: row.payrollHistoryId,
                organizationCode: row.organizationCode || "",
                personCode: row.personCode || "",
            }));
    } catch (error) {
        showError(status, error);
        return;
    }

    if (!rollbackItems.length) {
        status.className = "status";
        status.textContent = `没有可还原的 ${year} 年正常档次/薪级晋升记录。`;
        return;
    }
    const scopeText = organizationCode ? `单位 ${organizationCode}` : "全部可见单位";
    if (!confirm(`确认还原${scopeText} ${year} 年全部已处理的 ${rollbackItems.length} 人？系统会删除对应链头记录并恢复上一条工资记录。`)) {
        status.className = "status";
        status.textContent = "已取消批量还原。";
        return;
    }

    status.className = "status";
    status.textContent = `正在批量还原 ${rollbackItems.length} 条...`;
    const writeStarted = performance.now();
    try {
        const result = await postJson("/api/payroll/normal-promotions/batch-rollback", { items: rollbackItems });
        const writeMs = Math.round(performance.now() - writeStarted);
        const uiStarted = performance.now();
        const successItems = (result?.successes && result.successes.length)
            ? result.successes
            : (result?.successIds || []).map(id => ({
                previousPayrollHistoryId: id,
                payrollHistoryId: id,
            }));
        markNormalPromotionsRolledBackLocally(successItems);
        const uiMs = Math.round(performance.now() - uiStarted);
        status.className = (result?.failureCount || 0) ? "status error" : "status";
        const baseMessage = result?.message
            || `批量还原完成：成功 ${result?.successCount ?? 0} 条，失败 ${result?.failureCount ?? 0} 条。`;
        status.textContent = `${baseMessage}（写库 ${writeMs}ms，界面更新 ${uiMs}ms，未重算列表）`;
        console.info("[normal-promotion-batch-rollback]", {
            writeMs,
            uiMs,
            successCount: result?.successCount,
            failureCount: result?.failureCount,
        });
    } catch (error) {
        showError(status, error);
    }
}

async function rollbackPromotionAction(type, payrollHistoryId) {
    const writeConfig = type === "normal"
        ? { permission: "NORMAL_PROMOTION_WRITE", name: "正常档次/薪级晋升" }
        : { permission: "LEVEL_PROMOTION_WRITE", name: "级别晋升" };
    if (!ensurePayrollFeatureWrite(writeConfig.permission, writeConfig.name)) {
        return;
    }
    const moduleName = writeConfig.name;
    if (!confirm(`确认还原当前${moduleName}工资变动？系统会删除当前链头记录，并恢复上一条工资记录为当前执行工资。`)) {
        return;
    }
    const status = document.getElementById(type === "normal" ? "normal-promotion-status" : "level-promotion-status");
    status.className = "status";
    status.textContent = `正在还原${moduleName}...`;
    try {
        const path = type === "normal" ? "normal-promotions" : "level-promotions";
        let body = {};
        if (type === "level") {
            const row = state.levelPromotionRowsById?.[payrollHistoryId];
            body = levelPromotionRollbackPayload(row) || {};
        } else if (type === "normal") {
            const row = state.normalPromotionRowsById?.[payrollHistoryId];
            body = normalPromotionRollbackPayload(row) || {};
        }
        const writeStarted = performance.now();
        const result = await postJson(`/api/payroll/${path}/${encodeURIComponent(payrollHistoryId)}/rollback`, body);
        const writeMs = Math.round(performance.now() - writeStarted);
        const uiStarted = performance.now();
        if (type === "normal") {
            markNormalPromotionsRolledBackLocally([{
                previousPayrollHistoryId: payrollHistoryId,
                payrollHistoryId: result?.payrollHistoryId || payrollHistoryId,
                changeType: result?.changeType,
            }]);
        } else {
            markLevelPromotionsRolledBackLocally([{
                previousPayrollHistoryId: payrollHistoryId,
                payrollHistoryId: result?.payrollHistoryId || payrollHistoryId,
                changeType: result?.changeType,
            }]);
        }
        const uiMs = Math.round(performance.now() - uiStarted);
        status.textContent = `${(result && result.message) || `${moduleName}已还原`}（写库 ${writeMs}ms，界面更新 ${uiMs}ms，未重算列表）`;
    } catch (error) {
        showError(status, error);
    }
}

async function loadPositionChangePromotions() {
    const organizationCode = selectedOrganizationCode("position-change-organization-code");
    const keyword = document.getElementById("position-change-keyword").value.trim();
    const page = String(state.positionChangePage || 0);
    const size = "20";
    const params = new URLSearchParams({ page, size });
    if (!document.getElementById("position-change-include-apply")?.checked) {
        params.set("includeApply", "false");
    }
    if (!document.getElementById("position-change-include-processed")?.checked) {
        params.set("includeProcessed", "false");
    }
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
        const total = result.totalElements || 0;
        const totalPages = Math.max(result.totalPages || 1, 1);
        if (total > 0 && state.positionChangePage >= totalPages) {
            state.positionChangePage = Math.max(totalPages - 1, 0);
            await loadPositionChangePromotions();
            return;
        }
        state.positionChangePage = result.page || 0;
        const canWrite = hasPayrollFeatureWrite("POSITION_CHANGE_PROMOTION_WRITE");
        document.getElementById("position-change-select-all").checked = false;
        rows.innerHTML = (result.content || []).map(row => {
            const canApply = Boolean(row.applyEligible);
            const canRollback = Boolean(row.rollbackEligible);
            const applyButton = canWrite
                ? `<button class="row-action" data-position-change-apply="${escapeHtml(row.payrollHistoryId)}" type="button" ${canApply ? "" : "disabled"}>处理</button>`
                : "";
            const rollbackButton = canWrite
                ? `<button class="row-action danger-button" data-position-change-rollback="${escapeHtml(row.payrollHistoryId)}" type="button" ${canRollback ? "" : "disabled"}>还原</button>`
                : "";
            return `
            <tr class="${canRollback ? "highlight-row" : ""}">
                <td class="col-select${canWrite ? "" : " hidden"}"><input type="checkbox" data-position-change-select="${escapeHtml(row.payrollHistoryId)}" data-position-change-eligible="${canApply ? "true" : "false"}" data-position-change-rollback="${canRollback ? "true" : "false"}" ${canWrite && (canApply || canRollback) ? "" : "disabled"} aria-label="选择${escapeHtml(row.name)}"></td>
                <td class="col-org">${escapeHtml(row.organizationCode)}</td>
                <td class="col-code">${escapeHtml(row.personCode)}</td>
                <td class="col-name">${escapeHtml(row.name)}</td>
                <td class="col-position" title="${escapeHtml(row.currentPositionCode || "")}">${escapeHtml(row.currentPositionName || "")}</td>
                <td class="col-position" title="${escapeHtml(row.newPositionCode || "")}">${escapeHtml(row.newPositionName || "")}</td>
                <td class="col-change">${escapeHtml(row.changeType || "")}</td>
                <td class="col-change">${canRollback ? "已处理" : ""}</td>
                <td class="col-period">${escapeHtml(row.positionStartYearMonth || "")}</td>
                <td class="col-period">${escapeHtml(row.effectivePeriod || "")}</td>
                <td class="col-level">${escapeHtml(row.currentLevel || "")}</td>
                <td class="col-grade">${escapeHtml(row.currentStep || "")}</td>
                <td class="col-level">${escapeHtml(row.promotedLevel || "")}</td>
                <td class="col-grade">${escapeHtml(row.promotedStep || "")}</td>
                <td class="col-money">${money(row.netPositionSalaryIncrease ?? row.positionSalaryIncrease)}</td>
                <td class="col-money">${money(row.gradeSalaryIncrease)}</td>
                <td class="col-money">${money(row.totalIncrease)}</td>
                <td class="col-note">${escapeHtml(row.note || "")}</td>
                <td class="col-action">
                    <button class="row-action" data-position-change-detail="${escapeHtml(row.payrollHistoryId)}" type="button" ${canRollback ? "" : "disabled"}>明细</button>
                    ${applyButton}
                    ${rollbackButton}
                </td>
            </tr>
        `;
        }).join("");
        rows.querySelectorAll("button[data-position-change-detail]").forEach(button => {
            button.addEventListener("click", () => openPositionChangeDetailModal(button.dataset.positionChangeDetail));
        });
        rows.querySelectorAll("button[data-position-change-apply]").forEach(button => {
            button.addEventListener("click", () => applyPositionChangeAction(button.dataset.positionChangeApply));
        });
        rows.querySelectorAll("button[data-position-change-rollback]").forEach(button => {
            button.addEventListener("click", () => rollbackPositionChangeAction(button.dataset.positionChangeRollback));
        });
        renderPositionChangePagination(total, totalPages);
        status.textContent = total
            ? `共 ${total} 条试算记录，第 ${state.positionChangePage + 1} / ${totalPages} 页`
            : "未查询到试算记录";
    } catch (error) {
        renderPositionChangePagination(0, 1);
        showError(status, error);
    }
}

async function applySelectedPositionChanges() {
    if (!ensurePayrollFeatureWrite("POSITION_CHANGE_PROMOTION_WRITE", "职务变化晋升")) {
        return;
    }
    const selectedIds = Array.from(document.querySelectorAll("[data-position-change-select]:checked"))
        .filter(checkbox => checkbox.dataset.positionChangeEligible === "true")
        .map(checkbox => checkbox.dataset.positionChangeSelect)
        .filter(Boolean);
    const status = document.getElementById("position-change-status");
    if (!selectedIds.length) {
        status.className = "status error";
        status.textContent = "请先勾选需要处理的职务变化记录。";
        return;
    }
    if (!confirm(`确认批量处理勾选的 ${selectedIds.length} 条职务变化记录？`)) {
        return;
    }
    status.className = "status";
    status.textContent = `正在批量处理 0 / ${selectedIds.length}...`;
    let successCount = 0;
    const failures = [];
    for (const id of selectedIds) {
        try {
            await postJson(`/api/payroll/position-change-promotions/${encodeURIComponent(id)}/apply`, {});
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
    await loadPositionChangePromotions();
}

async function rollbackSelectedPositionChanges() {
    if (!ensurePayrollFeatureWrite("POSITION_CHANGE_PROMOTION_WRITE", "职务变化晋升")) {
        return;
    }
    const selectedIds = Array.from(document.querySelectorAll("[data-position-change-select]:checked"))
        .filter(checkbox => checkbox.dataset.positionChangeRollback === "true")
        .map(checkbox => checkbox.dataset.positionChangeSelect)
        .filter(Boolean);
    const status = document.getElementById("position-change-status");
    if (!selectedIds.length) {
        status.className = "status error";
        status.textContent = "请先勾选需要还原的已处理职务变化记录。";
        return;
    }
    if (!confirm(`确认批量还原勾选的 ${selectedIds.length} 条职务变化？系统会删除对应链头记录并恢复上一条工资记录。`)) {
        return;
    }
    status.className = "status";
    status.textContent = `正在批量还原 0 / ${selectedIds.length}...`;
    let successCount = 0;
    const failures = [];
    for (const id of selectedIds) {
        try {
            await postJson(`/api/payroll/position-change-promotions/${encodeURIComponent(id)}/rollback`, {});
            successCount++;
            status.textContent = `正在批量还原 ${successCount} / ${selectedIds.length}...`;
        } catch (error) {
            failures.push(error.message);
        }
    }
    status.className = failures.length ? "status error" : "status";
    status.textContent = failures.length
        ? `批量还原完成：成功 ${successCount} 条，失败 ${failures.length} 条。${failures[0] || ""}`
        : `批量还原完成：成功 ${successCount} 条。`;
    await loadPositionChangePromotions();
}

async function applyPositionChangeAction(payrollHistoryId) {
    if (!ensurePayrollFeatureWrite("POSITION_CHANGE_PROMOTION_WRITE", "职务变化晋升")) {
        return;
    }
    if (!confirm("确认按当前试算结果处理职务变化？系统会新增一条当前工资变动记录，并将原当前记录转为历史记录。")) {
        return;
    }
    const status = document.getElementById("position-change-status");
    status.className = "status";
    status.textContent = "正在处理职务变化...";
    try {
        const result = await postJson(`/api/payroll/position-change-promotions/${encodeURIComponent(payrollHistoryId)}/apply`, {});
        status.textContent = (result && result.message) || "职务变化处理完成";
        await loadPositionChangePromotions();
    } catch (error) {
        showError(status, error);
    }
}

async function rollbackPositionChangeAction(payrollHistoryId) {
    if (!ensurePayrollFeatureWrite("POSITION_CHANGE_PROMOTION_WRITE", "职务变化晋升")) {
        return;
    }
    if (!confirm("确认还原当前职务变化工资变动？系统会删除当前链头记录，并恢复上一条工资记录为当前执行工资。")) {
        return;
    }
    const status = document.getElementById("position-change-status");
    status.className = "status";
    status.textContent = "正在还原职务变化...";
    try {
        const result = await postJson(`/api/payroll/position-change-promotions/${encodeURIComponent(payrollHistoryId)}/rollback`, {});
        status.textContent = (result && result.message) || "职务变化已还原";
        await loadPositionChangePromotions();
    } catch (error) {
        showError(status, error);
    }
}

function positionChangeDetailMoneyRow(label, before, after, delta) {
    const beforeNum = before == null || before === "" ? null : Number(before);
    const afterNum = after == null || after === "" ? null : Number(after);
    const deltaNum = delta == null || delta === ""
        ? ((beforeNum == null || afterNum == null) ? null : afterNum - beforeNum)
        : Number(delta);
    const changed = deltaNum != null && Number.isFinite(deltaNum) && deltaNum !== 0;
    return `<tr class="${changed ? "highlight-row" : ""}"><td>${escapeHtml(label)}</td><td>${money(before)}</td><td>${money(after)}</td><td>${changed ? money(deltaNum) : "-"}</td></tr>`;
}

function renderSalaryComponentCompareRows(components) {
    if (!Array.isArray(components) || !components.length) {
        return "";
    }
    return components.map(component => positionChangeDetailMoneyRow(
        component.caption || component.fieldName,
        component.beforeAmount,
        component.afterAmount,
        component.difference)).join("");
}

function positionChangeDetailCompareRow(label, before, after) {
    const beforeText = before == null || before === "" ? "-" : String(before);
    const afterText = after == null || after === "" ? "-" : String(after);
    const changed = beforeText !== afterText;
    return `<tr class="${changed ? "highlight-row" : ""}"><td>${escapeHtml(label)}</td><td>${escapeHtml(beforeText)}</td><td>${escapeHtml(afterText)}</td><td>${changed ? "变化" : "-"}</td></tr>`;
}

function renderPositionChangeDetailContent(row) {
    document.getElementById("position-change-detail-summary").textContent =
        `${row.organizationCode}-${row.personCode} ${row.name} / ${row.currentPositionName || row.currentPositionCode || ""} → ${row.newPositionName || row.newPositionCode || ""} / ${row.changeType || ""}`
        + (row.rollbackEligible ? "（已处理，可还原）" : row.applyEligible ? "（待处理）" : "");

    const positionCode = row.newPositionCode || row.currentPositionCode;
    const hideLevelFields = isSalaryLevelPosition(positionCode);
    const stepLabel = salaryStepCaption(positionCode);
    const gradeSalaryLabel = salaryStepSalaryCaption(positionCode);
    const stepAssessmentLabel = isInstitutionPositionCode(positionCode) ? "薪级考核起算年" : "档次考核起算年";

    const metaRows = [
        positionChangeDetailCompareRow(
            "职务",
            `${row.currentPositionCode || ""} ${row.currentPositionName || ""}`.trim(),
            `${row.newPositionCode || ""} ${row.newPositionName || ""}`.trim()),
        hideLevelFields ? "" : positionChangeDetailCompareRow("级别", row.currentLevel, row.promotedLevel),
        positionChangeDetailCompareRow(stepLabel, row.currentStep, row.promotedStep),
        hideLevelFields
            ? ""
            : positionChangeDetailCompareRow("级别考核起算年", row.levelAssessmentStartYear, row.nextLevelAssessmentStartYear),
        positionChangeDetailCompareRow(
            stepAssessmentLabel,
            row.stepAssessmentStartYear,
            row.nextStepAssessmentStartYear),
    ].filter(Boolean).join("");

    const salaryRows = (row.salaryComponents || []).length
        ? renderSalaryComponentCompareRows(row.salaryComponents)
        : [
            positionChangeDetailMoneyRow(
                "职务工资",
                row.currentPositionSalary,
                row.newPositionSalary,
                row.netPositionSalaryIncrease ?? row.positionSalaryIncrease),
            positionChangeDetailMoneyRow(
                gradeSalaryLabel,
                row.currentGradeSalary,
                row.promotedGradeSalary,
                row.gradeSalaryIncrease),
            positionChangeDetailMoneyRow("合计增资", null, null, row.totalIncrease),
        ].join("");

    const processRows = [
        ...(row.explanationLines || []).map((line, index) => `
            <tr>
                <th>${index + 1}</th>
                <td>${escapeHtml(line)}</td>
            </tr>
        `),
        row.note ? `<tr><th>说明</th><td>${escapeHtml(row.note)}</td></tr>` : "",
    ].filter(Boolean).join("");

    document.getElementById("position-change-detail-content").innerHTML = `
        <div class="detail-table-panel">
            <h4>工资变动对照</h4>
            <table class="approval-component-table">
                <thead>
                    <tr>
                        <th class="approval-item-header">项目</th>
                        <th>变动前</th>
                        <th>变动后</th>
                        <th>增减</th>
                    </tr>
                </thead>
                <tbody>${metaRows}${salaryRows}</tbody>
            </table>
            <h4>试算过程</h4>
            <table class="approval-meta-table position-change-detail-steps">
                <thead>
                    <tr>
                        <th>步骤</th>
                        <th>说明</th>
                    </tr>
                </thead>
                <tbody>${processRows || `<tr><th>-</th><td>暂无试算说明</td></tr>`}</tbody>
            </table>
        </div>
    `;
}

async function openPositionChangeDetailModal(payrollHistoryId) {
    const modal = document.getElementById("position-change-detail-modal");
    const content = document.getElementById("position-change-detail-content");
    document.getElementById("position-change-detail-summary").textContent = "正在加载明细...";
    content.innerHTML = `<div class="status">正在加载试算明细...</div>`;
    modal.classList.remove("hidden");
    try {
        const row = await getJson(`/api/payroll/position-change-promotions/${encodeURIComponent(payrollHistoryId)}`);
        renderPositionChangeDetailContent(row);
    } catch (error) {
        document.getElementById("position-change-detail-summary").textContent = "加载明细失败";
        content.innerHTML = `<div class="status error">${escapeHtml(error.message || "加载失败")}</div>`;
    }
}

function closePositionChangeDetailModal() {
    document.getElementById("position-change-detail-modal").classList.add("hidden");
}

async function loadEducationPromotions() {
    const organizationCode = selectedOrganizationCode("education-promotion-organization-code");
    const keyword = document.getElementById("education-promotion-keyword").value.trim();
    const params = new URLSearchParams({ page: "0", size: "200" });
    if (!document.getElementById("education-promotion-include-apply")?.checked) {
        params.set("includeApply", "false");
    }
    if (!document.getElementById("education-promotion-include-processed")?.checked) {
        params.set("includeProcessed", "false");
    }
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
        const byId = {};
        (result.content || []).forEach(row => {
            if (row?.payrollHistoryId) {
                byId[row.payrollHistoryId] = row;
            }
        });
        state.educationPromotionRowsById = byId;
        rows.innerHTML = (result.content || []).map(row => {
            const canRollback = Boolean(row.rollbackEligible);
            const actionParts = [
                `<button class="row-action" type="button" data-education-detail="${escapeHtml(row.payrollHistoryId)}">明细</button>`,
                renderSimplePromotionActions(row, "education-promotions", "学历晋升", loadEducationPromotions),
            ].filter(part => part && part !== "-");
            return `
            <tr class="${canRollback ? "highlight-row" : ""}">
                <td>${escapeHtml(row.organizationCode)}</td>
                <td>${escapeHtml(row.personCode)}</td>
                <td>${escapeHtml(row.name)}</td>
                <td>${escapeHtml(row.calculationPeriod)}</td>
                <td>${canRollback ? "已处理" : ""}</td>
                <td title="${escapeHtml(row.currentPositionCode || "")}">${escapeHtml(row.currentPositionName || row.currentPositionCode || "")}</td>
                <td>${escapeHtml(formatEducationPromotionLevelStep(row.currentLevel, row.currentStep))}</td>
                <td title="${escapeHtml(row.educationCode || "")}">${escapeHtml(row.educationName || row.educationCode || "")}</td>
                <td>${escapeHtml(row.graduationDate || "")}</td>
                <td title="${escapeHtml(row.standardPositionCode || "")}">${escapeHtml([row.standardPositionName || row.standardPositionCode || "", formatEducationPromotionLevelStep(row.standardLevel, row.standardStep)].filter(Boolean).join(" "))}</td>
                <td title="${escapeHtml(row.promotedPositionCode || "")}">${escapeHtml(row.promotedPositionName || row.promotedPositionCode || "")}</td>
                <td>${escapeHtml(formatEducationPromotionLevelStep(row.promotedLevel, row.promotedStep, row.promotedGradeStepDifference))}</td>
                <td class="col-note" title="${escapeHtml(row.note || "")}">${escapeHtml(row.note || "")}</td>
                <td class="col-action">${actionParts.join(" ") || "-"}</td>
            </tr>`;
        }).join("");
        rows.querySelectorAll("button[data-education-detail]").forEach(button => {
            button.addEventListener("click", () => openEducationPromotionDetailModal(button.dataset.educationDetail));
        });
        bindSimplePromotionActions(rows, loadEducationPromotions);
        const total = result.totalElements ?? (result.content || []).length;
        status.textContent = `共 ${total} 条试算记录`;
    } catch (error) {
        showError(status, error);
    }
}

function formatEducationPromotionLevelStep(level, step, difference) {
    level = level || "";
    step = step || "";
    difference = difference || "0";
    if (!level && !step) {
        return "";
    }
    if (Number(difference) > 0) {
        return `${level}-${step}+${difference}`;
    }
    return level && step ? `${level}-${step}` : level || step;
}

function renderEducationPromotionDetailContent(row) {
    const content = document.getElementById("education-promotion-detail-content");
    const summary = document.getElementById("education-promotion-detail-summary");
    if (!content || !summary) {
        return;
    }
    const statusText = row.rollbackEligible
        ? "（已处理，可还原）"
        : row.applyEligible
            ? "（待处理）"
            : "";
    summary.textContent = `${row.organizationCode || ""}-${row.personCode || ""} ${row.name || ""}${statusText}`;
    const beforePosition = `${row.currentPositionCode || ""} ${row.currentPositionName || ""}`.trim() || "-";
    const afterPosition = `${row.promotedPositionCode || ""} ${row.promotedPositionName || ""}`.trim() || "-";
    const beforeLevelStep = formatEducationPromotionLevelStep(row.currentLevel, row.currentStep);
    const afterLevelStep = formatEducationPromotionLevelStep(row.promotedLevel, row.promotedStep, row.promotedGradeStepDifference);
    const positionCode = row.promotedPositionCode || row.currentPositionCode;
    const hideLevelFields = isSalaryLevelPosition(positionCode);
    const stepLabel = salaryStepCaption(positionCode);
    const gradeSalaryLabel = salaryStepSalaryCaption(positionCode);
    const metaRows = [
        `<tr><td>试算年月</td><td colspan="3">${escapeHtml(row.calculationPeriod || "-")}</td></tr>`,
        `<tr><td>学历</td><td colspan="3">${escapeHtml(row.educationName || row.educationCode || "-")}，毕业 ${escapeHtml(row.graduationDate || "-")}</td></tr>`,
        `<tr><td>定级标准</td><td colspan="3">${escapeHtml(row.standardPositionName || row.standardPositionCode || "-")} ${escapeHtml(formatEducationPromotionLevelStep(row.standardLevel, row.standardStep) || "")}</td></tr>`,
        positionChangeDetailCompareRow("岗位", beforePosition, afterPosition),
        hideLevelFields
            ? positionChangeDetailCompareRow(stepLabel, row.currentStep || "-", row.promotedStep || "-")
            : positionChangeDetailCompareRow("级别档次", beforeLevelStep || "-", afterLevelStep || "-"),
        `<tr><td>考核起算年</td><td colspan="3">级别 ${escapeHtml(row.nextLevelAssessmentStartYear || "-")} / ${stepLabel} ${escapeHtml(row.nextStepAssessmentStartYear || "-")}</td></tr>`,
    ].filter(Boolean).join("");
    const salaryRows = (row.salaryComponents || []).length
        ? renderSalaryComponentCompareRows(row.salaryComponents)
        : [
            positionChangeDetailMoneyRow("岗位工资", row.currentPositionSalary, row.promotedPositionSalary, row.positionSalaryIncrease),
            positionChangeDetailMoneyRow(gradeSalaryLabel, row.currentGradeSalary, row.promotedGradeSalary, row.gradeSalaryIncrease),
            positionChangeDetailMoneyRow("合计", null, null, row.totalIncrease),
        ].join("");
    content.innerHTML = `
        <div class="detail-table-panel">
            <h4>变动前后对照</h4>
            <table class="approval-component-table">
                <thead>
                    <tr>
                        <th class="approval-item-header">项目</th>
                        <th>变动前</th>
                        <th>变动后</th>
                        <th>增减</th>
                    </tr>
                </thead>
                <tbody>${metaRows}${salaryRows}</tbody>
            </table>
            <h4>说明</h4>
            <table class="approval-meta-table">
                <tbody>
                    <tr><th>说明</th><td>${escapeHtml(row.note || "-")}</td></tr>
                </tbody>
            </table>
        </div>
    `;
}

function openEducationPromotionDetailModal(payrollHistoryId) {
    const row = state.educationPromotionRowsById?.[payrollHistoryId];
    const modal = document.getElementById("education-promotion-detail-modal");
    const content = document.getElementById("education-promotion-detail-content");
    if (!modal || !content) {
        return;
    }
    if (!row) {
        document.getElementById("education-promotion-detail-summary").textContent = "未找到试算结果，请重新查询。";
        content.innerHTML = "";
        modal.classList.remove("hidden");
        return;
    }
    renderEducationPromotionDetailContent(row);
    modal.classList.remove("hidden");
}

function closeEducationPromotionDetailModal() {
    document.getElementById("education-promotion-detail-modal")?.classList.add("hidden");
}

async function loadRegularizations() {
    const organizationCode = selectedOrganizationCode("regularization-organization-code");
    const keyword = document.getElementById("regularization-keyword")?.value.trim() || "";
    const params = new URLSearchParams({ page: "0", size: "10000" });
    if (!document.getElementById("regularization-include-apply")?.checked) {
        params.set("includeApply", "false");
    }
    if (!document.getElementById("regularization-include-processed")?.checked) {
        params.set("includeProcessed", "false");
    }
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
    const startedAt = performance.now();

    try {
        const result = await getJson(`/api/payroll/regularizations?${params}`);
        const elapsedMs = Math.round(performance.now() - startedAt);
        const byId = {};
        (result.content || []).forEach(row => {
            if (row?.payrollHistoryId) {
                byId[row.payrollHistoryId] = row;
            }
        });
        state.regularizationRowsById = byId;
        renderRegularizationTableRows(result.content || []);
        status.textContent = `共 ${result.totalElements ?? (result.content || []).length} 条试算记录（${elapsedMs} ms）`;
    } catch (error) {
        showError(status, error);
    }
}

function renderRegularizationTableRows(content) {
    const rows = document.getElementById("regularization-rows");
    if (!rows) {
        return;
    }
    const canWrite = hasPayrollFeatureWrite("REGULARIZATION_WRITE");
    const selectAll = document.getElementById("regularization-select-all");
    if (selectAll) {
        selectAll.checked = false;
    }
    rows.innerHTML = (content || []).map(row => {
        const canApply = Boolean(row.applyEligible);
        const canRollback = Boolean(row.rollbackEligible);
        const canSelect = canWrite && (canApply || canRollback);
        const action = canApply ? "apply" : (canRollback ? "rollback" : "");
        const actions = [];
        actions.push(`<button class="row-action" type="button" data-regularization-detail="${escapeHtml(row.payrollHistoryId)}">明细</button>`);
        if (canWrite && canApply) {
            actions.push(`<button class="row-action" type="button" data-regularization-apply="${escapeHtml(row.payrollHistoryId)}">处理</button>`);
        }
        if (canWrite && canRollback) {
            actions.push(`<button class="row-action danger-button" type="button" data-regularization-rollback="${escapeHtml(row.payrollHistoryId)}">还原</button>`);
        }
        return `
            <tr class="${canRollback ? "highlight-row" : ""}">
                <td class="col-select${canWrite ? "" : " hidden"}"><input type="checkbox" data-regularization-select="${escapeHtml(row.payrollHistoryId)}" data-regularization-action="${escapeHtml(action)}" ${canSelect ? "" : "disabled"} aria-label="选择${escapeHtml(row.name)}"></td>
                <td>${escapeHtml(row.organizationCode)}</td>
                <td>${escapeHtml(row.personCode)}</td>
                <td>${escapeHtml(row.name)}</td>
                <td>${escapeHtml(row.calculationPeriod)}</td>
                <td>${canRollback ? "已处理" : ""}</td>
                <td>${escapeHtml(row.probationPositionName || "")}</td>
                <td>${escapeHtml(row.educationName || "")}</td>
                <td>${escapeHtml(row.graduationDate || "")}</td>
                <td>${escapeHtml(row.regularPositionCode || "")}</td>
                <td>${escapeHtml(row.regularPositionName || "")}</td>
                <td>${escapeHtml(row.regularLevel || "")}</td>
                <td>${escapeHtml(row.regularStep || "")}</td>
                <td>${money(row.increaseAmount)}</td>
                <td>${escapeHtml(row.note || "")}</td>
                <td class="col-action">${actions.join(" ")}</td>
            </tr>
        `;
    }).join("");
    rows.querySelectorAll("button[data-regularization-detail]").forEach(button => {
        button.addEventListener("click", () => openRegularizationDetailModal(button.dataset.regularizationDetail));
    });
    rows.querySelectorAll("button[data-regularization-apply]").forEach(button => {
        button.addEventListener("click", () => applyRegularizationAction(button.dataset.regularizationApply));
    });
    rows.querySelectorAll("button[data-regularization-rollback]").forEach(button => {
        button.addEventListener("click", () => rollbackRegularizationAction(button.dataset.regularizationRollback));
    });
}

function regularizationApplyPayload(row) {
    if (!row) {
        return null;
    }
    return {
        organizationCode: row.organizationCode || "",
        personCode: row.personCode || "",
        calculationPeriod: row.calculationPeriod || "",
        regularPositionCode: row.regularPositionCode || "",
        regularPositionName: row.regularPositionName || "",
        regularLevel: row.regularLevel || "",
        regularStep: row.regularStep || "",
        regularPositionSalary: row.regularPositionSalary,
        regularBaseSalary: row.regularBaseSalary,
        increaseAmount: row.increaseAmount,
    };
}

function regularizationRollbackPayload(row) {
    if (!row) {
        return null;
    }
    return {
        organizationCode: row.organizationCode || "",
        personCode: row.personCode || "",
    };
}

function markRegularizationsProcessedLocally(successItems) {
    const items = (successItems || []).filter(item => item && (item.previousPayrollHistoryId || item.payrollHistoryId));
    if (!items.length) {
        return;
    }
    const nextById = { ...(state.regularizationRowsById || {}) };
    for (const item of items) {
        const previousId = item.previousPayrollHistoryId || item.payrollHistoryId;
        const newId = item.payrollHistoryId || previousId;
        const row = nextById[previousId];
        if (!row) {
            continue;
        }
        delete nextById[previousId];
        nextById[newId] = {
            ...row,
            payrollHistoryId: newId,
            applyEligible: false,
            eligible: false,
            rollbackEligible: true,
            // 保留变动前/后对照字段，明细仍可查看增资
            note: "已办理转正定级，可还原。",
        };
    }
    state.regularizationRowsById = nextById;
    let visible = Object.values(nextById);
    const includeApply = document.getElementById("regularization-include-apply")?.checked ?? true;
    const includeProcessed = document.getElementById("regularization-include-processed")?.checked ?? true;
    if (!includeApply) {
        visible = visible.filter(row => row.rollbackEligible);
    }
    if (!includeProcessed) {
        visible = visible.filter(row => !row.rollbackEligible);
    }
    renderRegularizationTableRows(visible);
}

function markRegularizationsRolledBackLocally(successItems) {
    const items = (successItems || []).filter(item => item && (item.previousPayrollHistoryId || item.payrollHistoryId));
    if (!items.length) {
        return;
    }
    const nextById = { ...(state.regularizationRowsById || {}) };
    for (const item of items) {
        const processedId = item.previousPayrollHistoryId || item.payrollHistoryId;
        const restoredId = item.payrollHistoryId || processedId;
        const row = nextById[processedId];
        if (!row) {
            continue;
        }
        delete nextById[processedId];
        nextById[restoredId] = {
            ...row,
            payrollHistoryId: restoredId,
            applyEligible: true,
            eligible: true,
            rollbackEligible: false,
            note: row.note && String(row.note).includes("可还原") ? "符合转正定级条件，可办理。" : (row.note || ""),
        };
    }
    state.regularizationRowsById = nextById;
    let visible = Object.values(nextById);
    const includeApply = document.getElementById("regularization-include-apply")?.checked ?? true;
    const includeProcessed = document.getElementById("regularization-include-processed")?.checked ?? true;
    if (!includeApply) {
        visible = visible.filter(row => row.rollbackEligible);
    }
    if (!includeProcessed) {
        visible = visible.filter(row => !row.rollbackEligible);
    }
    renderRegularizationTableRows(visible);
}

function closeRegularizationDetailModal() {
    document.getElementById("regularization-detail-modal")?.classList.add("hidden");
}

function renderRegularizationDetailContent(row) {
    const statusText = row.rollbackEligible
        ? "（已处理，可还原）"
        : row.applyEligible
            ? "（待处理）"
            : "";
    document.getElementById("regularization-detail-summary").textContent =
        `${row.organizationCode || ""}-${row.personCode || ""} ${row.name || ""}${statusText}`;

    const beforePosition = `${row.probationPositionCode || ""} ${row.probationPositionName || ""}`.trim() || "-";
    const afterPosition = `${row.regularPositionCode || ""} ${row.regularPositionName || ""}`.trim() || "-";
    const positionCode = row.regularPositionCode || row.probationPositionCode;
    const hideLevelFields = isSalaryLevelPosition(positionCode);
    const stepLabel = salaryStepCaption(positionCode);
    const baseSalaryLabel = salaryStepSalaryCaption(positionCode);
    const performanceCaption = row.performanceAllowanceCaption || "生活性补贴";
    const subsidyCaption = row.subsidyAllowanceCaption || "工作性津贴";
    const showSubsidy = row.showSubsidyAllowance !== false && Boolean(subsidyCaption);
    const showTechnical = Number(row.currentTechnicalGradeSalary || 0) !== 0
        || Number(row.regularTechnicalGradeSalary || 0) !== 0;
    const showFloating = Number(row.currentFloatingSalary || 0) !== 0
        || Number(row.regularFloatingSalary || 0) !== 0;

    const metaRows = [
        `<tr><td>转正时间</td><td colspan="3">${escapeHtml(row.calculationPeriod || "-")}</td></tr>`,
        `<tr><td>学历</td><td colspan="3">${escapeHtml(row.educationName || row.educationCode || "-")}，毕业 ${escapeHtml(row.graduationDate || "-")}</td></tr>`,
        positionChangeDetailCompareRow("岗位", beforePosition, afterPosition),
        hideLevelFields
            ? ""
            : positionChangeDetailCompareRow("级别", row.currentLevel || "-", row.regularLevel || "-"),
        positionChangeDetailCompareRow(stepLabel, row.currentStep || "-", row.regularStep || "-"),
        (row.salaryComponents || []).length
            ? renderSalaryComponentCompareRows(row.salaryComponents)
            : [
                positionChangeDetailMoneyRow("职务工资", row.currentPositionSalary, row.regularPositionSalary, null),
                positionChangeDetailMoneyRow(baseSalaryLabel, row.currentGradeSalary, row.regularBaseSalary, null),
                showTechnical
                    ? positionChangeDetailMoneyRow(
                        "技术等级工资",
                        row.currentTechnicalGradeSalary,
                        row.regularTechnicalGradeSalary,
                        null)
                    : "",
                positionChangeDetailMoneyRow(
                    performanceCaption,
                    row.currentPerformanceAllowance,
                    row.regularPerformanceAllowance,
                    null),
                showSubsidy
                    ? positionChangeDetailMoneyRow(
                        subsidyCaption,
                        row.currentSubsidyAllowance,
                        row.regularSubsidyAllowance,
                        null)
                    : "",
                showFloating
                    ? positionChangeDetailMoneyRow(
                        "浮动工资",
                        row.currentFloatingSalary,
                        row.regularFloatingSalary,
                        null)
                    : "",
                positionChangeDetailMoneyRow(
                    "基本工资小计",
                    row.currentSalary,
                    row.totalRegularSalary,
                    null),
                positionChangeDetailMoneyRow(
                    "合计",
                    row.currentTotal,
                    row.regularTotal,
                    row.increaseAmount),
            ].filter(Boolean).join(""),
    ].filter(Boolean).join("");

    const processRows = row.note
        ? `<tr><th>说明</th><td>${escapeHtml(row.note)}</td></tr>`
        : `<tr><th>-</th><td>暂无试算说明</td></tr>`;

    document.getElementById("regularization-detail-content").innerHTML = `
        <div class="detail-table-panel">
            <h4>变动前后对照</h4>
            <table class="approval-component-table">
                <thead>
                    <tr>
                        <th class="approval-item-header">项目</th>
                        <th>变动前</th>
                        <th>变动后</th>
                        <th>差额/备注</th>
                    </tr>
                </thead>
                <tbody>${metaRows}</tbody>
            </table>
            <h4>试算说明</h4>
            <table class="approval-meta-table position-change-detail-steps">
                <thead>
                    <tr>
                        <th>项目</th>
                        <th>说明</th>
                    </tr>
                </thead>
                <tbody>${processRows}</tbody>
            </table>
        </div>
    `;
}

function openRegularizationDetailModal(payrollHistoryId) {
    const row = state.regularizationRowsById?.[payrollHistoryId];
    const modal = document.getElementById("regularization-detail-modal");
    const content = document.getElementById("regularization-detail-content");
    if (!modal || !content) {
        return;
    }
    if (!row) {
        document.getElementById("regularization-detail-summary").textContent = "未找到试算结果，请重新查询。";
        content.innerHTML = "";
        modal.classList.remove("hidden");
        return;
    }
    renderRegularizationDetailContent(row);
    modal.classList.remove("hidden");
}

async function applyRegularizationAction(payrollHistoryId) {
    if (!ensurePayrollFeatureWrite("REGULARIZATION_WRITE", "转正定级")) {
        return;
    }
    if (!confirm("确认按当前试算结果处理转正定级？系统会新增一条当前工资变动记录，并将原当前记录转为历史记录。")) {
        return;
    }
    const status = document.getElementById("regularization-status");
    const row = state.regularizationRowsById?.[payrollHistoryId];
    const body = regularizationApplyPayload(row);
    if (!body) {
        status.className = "status error";
        status.textContent = "未找到试算结果，请重新查询后再办理。";
        return;
    }
    status.className = "status";
    status.textContent = "正在处理转正定级...";
    const writeStarted = performance.now();
    try {
        const result = await postJson(`/api/payroll/regularizations/${encodeURIComponent(payrollHistoryId)}/apply`, body);
        const writeMs = Math.round(performance.now() - writeStarted);
        const uiStarted = performance.now();
        markRegularizationsProcessedLocally([{
            previousPayrollHistoryId: payrollHistoryId,
            payrollHistoryId: result?.payrollHistoryId || payrollHistoryId,
            changeType: result?.changeType,
        }]);
        const uiMs = Math.round(performance.now() - uiStarted);
        status.textContent = `${(result && result.message) || "转正定级处理完成"}（写库 ${writeMs}ms，界面更新 ${uiMs}ms，未重算列表）`;
    } catch (error) {
        showError(status, error);
    }
}

async function rollbackRegularizationAction(payrollHistoryId) {
    if (!ensurePayrollFeatureWrite("REGULARIZATION_WRITE", "转正定级")) {
        return;
    }
    if (!confirm("确认还原当前转正定级工资变动？系统会删除当前链头记录，并恢复上一条工资记录为当前执行工资。")) {
        return;
    }
    const status = document.getElementById("regularization-status");
    const row = state.regularizationRowsById?.[payrollHistoryId];
    const body = regularizationRollbackPayload(row) || {};
    status.className = "status";
    status.textContent = "正在还原转正定级...";
    const writeStarted = performance.now();
    try {
        const result = await postJson(`/api/payroll/regularizations/${encodeURIComponent(payrollHistoryId)}/rollback`, body);
        const writeMs = Math.round(performance.now() - writeStarted);
        const uiStarted = performance.now();
        markRegularizationsRolledBackLocally([{
            previousPayrollHistoryId: payrollHistoryId,
            payrollHistoryId: result?.payrollHistoryId || payrollHistoryId,
            changeType: result?.changeType,
        }]);
        const uiMs = Math.round(performance.now() - uiStarted);
        status.textContent = `${(result && result.message) || "转正定级已还原"}（写库 ${writeMs}ms，界面更新 ${uiMs}ms，未重算列表）`;
    } catch (error) {
        showError(status, error);
    }
}

async function applySelectedRegularizations() {
    if (!ensurePayrollFeatureWrite("REGULARIZATION_WRITE", "转正定级")) {
        return;
    }
    const selectedIds = Array.from(document.querySelectorAll("[data-regularization-select]:checked"))
        .filter(checkbox => checkbox.dataset.regularizationAction === "apply")
        .map(checkbox => checkbox.dataset.regularizationSelect)
        .filter(Boolean);
    const status = document.getElementById("regularization-status");
    if (!selectedIds.length) {
        status.className = "status error";
        status.textContent = "请先勾选需要办理的转正定级记录。";
        return;
    }
    if (!confirm(`确认批量处理 ${selectedIds.length} 条转正定级记录？`)) {
        return;
    }
    const items = [];
    for (const id of selectedIds) {
        const payload = regularizationApplyPayload(state.regularizationRowsById?.[id]);
        if (!payload) {
            status.className = "status error";
            status.textContent = "部分勾选记录缺少试算结果，请重新查询后再办理。";
            return;
        }
        items.push({ payrollHistoryId: id, ...payload });
    }
    status.className = "status";
    status.textContent = `正在批量处理 ${items.length} 条...`;
    const writeStarted = performance.now();
    try {
        const result = await postJson("/api/payroll/regularizations/batch-apply", { items });
        const writeMs = Math.round(performance.now() - writeStarted);
        const uiStarted = performance.now();
        const successItems = (result?.successes && result.successes.length)
            ? result.successes
            : (result?.successIds || []).map(id => ({
                previousPayrollHistoryId: id,
                payrollHistoryId: id,
            }));
        markRegularizationsProcessedLocally(successItems);
        const uiMs = Math.round(performance.now() - uiStarted);
        status.className = (result?.failureCount || 0) ? "status error" : "status";
        const baseMessage = result?.message
            || `批量处理完成：成功 ${result?.successCount ?? 0} 条，失败 ${result?.failureCount ?? 0} 条。`;
        status.textContent = `${baseMessage}（写库 ${writeMs}ms，界面更新 ${uiMs}ms，未重算列表）`;
        console.info("[regularization-batch]", { writeMs, uiMs, successCount: result?.successCount, failureCount: result?.failureCount });
    } catch (error) {
        showError(status, error);
    }
}

async function rollbackSelectedRegularizations() {
    if (!ensurePayrollFeatureWrite("REGULARIZATION_WRITE", "转正定级")) {
        return;
    }
    const selectedIds = Array.from(document.querySelectorAll("[data-regularization-select]:checked"))
        .filter(checkbox => checkbox.dataset.regularizationAction === "rollback")
        .map(checkbox => checkbox.dataset.regularizationSelect)
        .filter(Boolean);
    const status = document.getElementById("regularization-status");
    if (!selectedIds.length) {
        status.className = "status error";
        status.textContent = "请先勾选需要还原的转正定级记录。";
        return;
    }
    if (!confirm(`确认批量还原 ${selectedIds.length} 条转正定级记录？系统会删除对应链头记录并恢复上一条工资记录。`)) {
        return;
    }
    const items = [];
    for (const id of selectedIds) {
        const row = state.regularizationRowsById?.[id];
        if (!row?.payrollHistoryId) {
            status.className = "status error";
            status.textContent = "部分勾选记录缺少标识，请重新查询后再还原。";
            return;
        }
        items.push({
            payrollHistoryId: row.payrollHistoryId,
            organizationCode: row.organizationCode || "",
            personCode: row.personCode || "",
        });
    }
    status.className = "status";
    status.textContent = `正在批量还原 ${items.length} 条...`;
    const writeStarted = performance.now();
    try {
        const result = await postJson("/api/payroll/regularizations/batch-rollback", { items });
        const writeMs = Math.round(performance.now() - writeStarted);
        const uiStarted = performance.now();
        const successItems = (result?.successes && result.successes.length)
            ? result.successes
            : (result?.successIds || []).map(id => ({
                previousPayrollHistoryId: id,
                payrollHistoryId: id,
            }));
        markRegularizationsRolledBackLocally(successItems);
        const uiMs = Math.round(performance.now() - uiStarted);
        status.className = (result?.failureCount || 0) ? "status error" : "status";
        const baseMessage = result?.message
            || `批量还原完成：成功 ${result?.successCount ?? 0} 条，失败 ${result?.failureCount ?? 0} 条。`;
        status.textContent = `${baseMessage}（写库 ${writeMs}ms，界面更新 ${uiMs}ms，未重算列表）`;
        console.info("[regularization-batch-rollback]", { writeMs, uiMs, successCount: result?.successCount, failureCount: result?.failureCount });
    } catch (error) {
        showError(status, error);
    }
}

async function loadRegularizationHighGrades() {
    const organizationCode = selectedOrganizationCode("regularization-high-grade-organization-code");
    const keyword = document.getElementById("regularization-high-grade-keyword").value.trim();
    const page = String(state.regularizationHighGradePage || 0);
    const size = document.getElementById("regularization-high-grade-size").value || "20";
    const params = new URLSearchParams({ page, size });
    if (organizationCode) {
        params.set("organizationCode", organizationCode);
    }
    if (keyword) {
        params.set("keyword", keyword);
    }

    const status = document.getElementById("regularization-high-grade-status");
    const rows = document.getElementById("regularization-high-grade-rows");
    status.className = "status";
    status.textContent = "正在查询转正高定档次薪级试算...";
    rows.innerHTML = "";

    try {
        const result = await getJson(`/api/payroll/regularization-high-grades?${params}`);
        const content = result.content || [];
        const totalPages = Math.max(result.totalPages || 1, 1);
        if ((result.page || 0) > totalPages - 1 && totalPages > 0) {
            state.regularizationHighGradePage = Math.max(totalPages - 1, 0);
            return loadRegularizationHighGrades();
        }
        state.regularizationHighGradePage = result.page || 0;
        state.regularizationHighGradeTotalPages = totalPages;
        rows.innerHTML = content.length ? content.map(row => `
            <tr>
                <td class="col-org" title="${escapeHtml(row.organizationCode || "")}">${escapeHtml(row.organizationCode || "")}</td>
                <td class="col-code">${escapeHtml(row.personCode || "")}</td>
                <td class="col-name">${escapeHtml(row.name || "")}</td>
                <td class="col-period">${escapeHtml(row.regularizationPeriod || row.calculationPeriod || "")}</td>
                <td class="col-position" title="${escapeHtml(row.positionName || row.positionCode || "")}">${escapeHtml(row.positionName || row.positionCode || "")}</td>
                <td class="col-edu">${escapeHtml(row.educationName || row.educationCode || "")}</td>
                <td class="col-step">${escapeHtml(row.baseStep || "")}</td>
                <td class="col-inc">${escapeHtml(row.highGradeIncrement == null ? "" : row.highGradeIncrement)}</td>
                <td class="col-step">${escapeHtml(row.currentStep || "")}</td>
                <td class="col-step">${escapeHtml(row.targetStep || "")}</td>
                <td class="col-amount">${money(row.currentGradeSalary)}</td>
                <td class="col-amount">${money(row.targetGradeSalary)}</td>
                <td class="col-amount">${money(row.increaseAmount)}</td>
                <td class="col-note" title="${escapeHtml(row.note || "")}">${escapeHtml(row.note || "")}</td>
                <td class="col-actions">${renderSimplePromotionActions(row, "regularization-high-grades", "转正高定档次薪级", loadRegularizationHighGrades)}</td>
            </tr>
        `).join("") : `<tr><td colspan="15">暂无试算记录</td></tr>`;
        bindSimplePromotionActions(rows, loadRegularizationHighGrades);
        renderRegularizationHighGradePagination(result.totalElements || 0, totalPages);
        status.textContent = `第 ${state.regularizationHighGradePage + 1} / ${totalPages} 页，共 ${result.totalElements || 0} 条试算记录`;
    } catch (error) {
        renderRegularizationHighGradePagination(0, 1);
        showError(status, error);
    }
}

async function loadInternSalaryChanges() {
    const organizationCode = selectedOrganizationCode("intern-salary-change-organization-code");
    const keyword = document.getElementById("intern-salary-change-keyword").value.trim();
    const page = document.getElementById("intern-salary-change-page").value || "0";
    const size = document.getElementById("intern-salary-change-size").value || "20";
    const params = new URLSearchParams({ page, size });
    if (organizationCode) {
        params.set("organizationCode", organizationCode);
    }
    if (keyword) {
        params.set("keyword", keyword);
    }

    const status = document.getElementById("intern-salary-change-status");
    const rows = document.getElementById("intern-salary-change-rows");
    status.className = "status";
    status.textContent = "正在查询见习工资变动试算...";
    rows.innerHTML = "";

    try {
        const result = await getJson(`/api/payroll/intern-salary-changes?${params}`);
        rows.innerHTML = (result.content || []).map(row => `
            <tr>
                <td>${escapeHtml(row.organizationCode)} ${escapeHtml(row.organizationName || "")}</td>
                <td>${escapeHtml(row.personCode)}</td>
                <td>${escapeHtml(row.name)}</td>
                <td>${escapeHtml(row.calculationPeriod || "")}</td>
                <td>${escapeHtml(row.workStartYearMonth || "")}</td>
                <td>${escapeHtml(row.positionName || "")}</td>
                <td>${escapeHtml(row.salaryStandardYearMonth || "")}</td>
                <td>${money(row.storedInternSalary)}</td>
                <td>${money(row.calculatedInternSalary)}</td>
                <td>${money(row.nextTotal)}</td>
                <td>${money(row.differenceAmount)}</td>
                <td>${escapeHtml(row.standardNote || "")}</td>
                <td>${renderSimplePromotionActions(row, "intern-salary-changes", "见习工资变动", loadInternSalaryChanges)}</td>
            </tr>
        `).join("");
        bindSimplePromotionActions(rows, loadInternSalaryChanges);
        status.textContent = `第 ${result.page + 1} / ${Math.max(result.totalPages, 1)} 页，共 ${result.totalElements} 条试算记录`;
    } catch (error) {
        showError(status, error);
    }
}

async function loadNewPersonnelSalaryDeterminations() {
    const organizationCode = selectedOrganizationCode("new-personnel-salary-organization-code");
    const keyword = document.getElementById("new-personnel-salary-keyword").value.trim();
    const page = String(state.newPersonnelSalaryPage || 0);
    const size = "20";
    const params = new URLSearchParams({ page, size });
    if (!document.getElementById("new-personnel-salary-include-apply")?.checked) {
        params.set("includeApply", "false");
    }
    if (!document.getElementById("new-personnel-salary-include-processed")?.checked) {
        params.set("includeProcessed", "false");
    }
    if (organizationCode) {
        params.set("organizationCode", organizationCode);
    }
    if (keyword) {
        params.set("keyword", keyword);
    }

    const status = document.getElementById("new-personnel-salary-status");
    const rows = document.getElementById("new-personnel-salary-rows");
    status.className = "status";
    status.textContent = "正在查询新进定资...";
    rows.innerHTML = "";
    const startedAt = performance.now();

    try {
        const result = await getJson(`/api/payroll/new-personnel-salary-determinations?${params}`);
        const elapsedMs = Math.round(performance.now() - startedAt);
        const totalPages = Math.max(result.totalPages || 0, 1);
        if ((result.page || 0) >= totalPages && totalPages > 0 && (result.totalElements || 0) > 0) {
            state.newPersonnelSalaryPage = Math.max(totalPages - 1, 0);
            return loadNewPersonnelSalaryDeterminations();
        }
        state.newPersonnelSalaryPage = result.page || 0;
        state.newPersonnelSalaryTotalPages = totalPages;
        const byUid = {};
        (result.content || []).forEach(row => {
            if (row?.uid != null) {
                byUid[String(row.uid)] = row;
            }
        });
        state.newPersonnelSalaryRowsByUid = byUid;
        renderNewPersonnelSalaryTableRows(result.content || []);
        renderNewPersonnelSalaryPagination(result.totalElements || 0, totalPages);
        status.textContent = `第 ${state.newPersonnelSalaryPage + 1} / ${totalPages} 页，共 ${result.totalElements || 0} 条记录（${elapsedMs} ms）`;
    } catch (error) {
        renderNewPersonnelSalaryPagination(0, 1);
        showError(status, error);
    }
}

function renderNewPersonnelSalaryTableRows(content) {
    const rows = document.getElementById("new-personnel-salary-rows");
    if (!rows) {
        return;
    }
    const canWrite = hasPayrollFeatureWrite("NEW_PERSONNEL_SALARY_WRITE");
    const selectAll = document.getElementById("new-personnel-salary-select-all");
    if (selectAll) {
        selectAll.checked = false;
    }
    rows.innerHTML = (content || []).map(row => {
        const uid = String(row.uid || "");
        const canApply = Boolean(row.applyEligible);
        const canRollback = Boolean(row.rollbackEligible);
        const canSelect = canWrite && (canApply || canRollback);
        const action = canApply ? "apply" : (canRollback ? "rollback" : "");
        return `
            <tr class="${canRollback ? "highlight-row" : ""}">
                <td class="col-select${canWrite ? "" : " hidden"}"><input type="checkbox" data-new-personnel-select="${escapeHtml(uid)}" data-new-personnel-action="${escapeHtml(action)}" data-new-personnel-history="${escapeHtml(row.payrollHistoryId || "")}" ${canSelect ? "" : "disabled"} aria-label="选择${escapeHtml(row.name || "")}"></td>
                <td class="col-org" title="${escapeHtml(row.organizationName || row.organizationCode || "")}">${escapeHtml(row.organizationName || row.organizationCode || "")}</td>
                <td class="col-code">${escapeHtml(row.personCode)}</td>
                <td class="col-name">${escapeHtml(row.name)}</td>
                <td class="col-period">${escapeHtml(row.joinYearMonth || "")}</td>
                <td class="col-join">${escapeHtml(row.joinType || "")}</td>
                <td class="col-change">${canRollback ? "已处理" : ""}</td>
                <td class="col-period">${escapeHtml(row.calculationPeriod || "")}</td>
                <td class="col-position" title="${escapeHtml([row.positionCode, row.positionName].filter(Boolean).join(" "))}">${escapeHtml(row.positionCode || "")} ${escapeHtml(row.positionName || "")}</td>
                <td class="col-period">${escapeHtml(row.positionStartYearMonth || "")}</td>
                <td class="col-amount">${money(row.calculatedTotal)}</td>
                <td class="col-note" title="${escapeHtml(row.standardNote || "")}">${escapeHtml(row.standardNote || "")}</td>
                <td class="col-actions">${renderNewPersonnelSalaryActions(row)}</td>
            </tr>
        `;
    }).join("");
    bindNewPersonnelSalaryActions(rows);
}

function renderNewPersonnelSalaryActions(row) {
    const canWrite = hasPayrollFeatureWrite("NEW_PERSONNEL_SALARY_WRITE");
    const canApply = Boolean(row.applyEligible);
    const canRollback = Boolean(row.rollbackEligible);
    const parts = [
        `<button class="row-action" type="button" data-new-personnel-detail="${escapeHtml(String(row.uid || ""))}">明细</button>`
    ];
    if (canWrite && canApply) {
        parts.push(`<button class="row-action" type="button" data-new-personnel-apply="${escapeHtml(String(row.uid || ""))}">处理</button>`);
    }
    if (canWrite && canRollback) {
        parts.push(`<button class="row-action danger-button" type="button" data-new-personnel-rollback="${escapeHtml(row.payrollHistoryId || "")}">还原</button>`);
    }
    return parts.join(" ");
}

function markNewPersonnelSalariesProcessedLocally(successItems) {
    const items = (successItems || []).filter(item => item && (item.uid != null || item.previousPayrollHistoryId || item.payrollHistoryId));
    if (!items.length) {
        return;
    }
    const nextByUid = { ...(state.newPersonnelSalaryRowsByUid || {}) };
    for (const item of items) {
        const uidKey = item.uid != null ? String(item.uid) : String(item.previousPayrollHistoryId || "");
        const row = nextByUid[uidKey];
        if (!row) {
            continue;
        }
        const newId = item.payrollHistoryId || row.payrollHistoryId;
        const changeType = item.changeType || "";
        nextByUid[uidKey] = {
            ...row,
            payrollHistoryId: newId,
            applyEligible: false,
            eligible: true,
            rollbackEligible: true,
            standardNote: changeType
                ? `已定工资（${changeType}），可还原。`
                : "已定工资，可还原。",
        };
    }
    state.newPersonnelSalaryRowsByUid = nextByUid;
    let visible = Object.values(nextByUid);
    const includeApply = document.getElementById("new-personnel-salary-include-apply")?.checked ?? true;
    const includeProcessed = document.getElementById("new-personnel-salary-include-processed")?.checked ?? true;
    if (!includeApply) {
        visible = visible.filter(row => row.rollbackEligible);
    }
    if (!includeProcessed) {
        visible = visible.filter(row => !row.rollbackEligible);
    }
    renderNewPersonnelSalaryTableRows(visible);
}

function markNewPersonnelSalariesRolledBackLocally(successItems) {
    const items = (successItems || []).filter(item => item && (item.uid != null || item.previousPayrollHistoryId || item.payrollHistoryId));
    if (!items.length) {
        return;
    }
    const nextByUid = { ...(state.newPersonnelSalaryRowsByUid || {}) };
    for (const item of items) {
        let uidKey = item.uid != null ? String(item.uid) : null;
        let row = uidKey ? nextByUid[uidKey] : null;
        if (!row) {
            const historyId = item.previousPayrollHistoryId || item.payrollHistoryId;
            uidKey = Object.keys(nextByUid).find(key => nextByUid[key]?.payrollHistoryId === historyId) || null;
            row = uidKey ? nextByUid[uidKey] : null;
        }
        if (!row || !uidKey) {
            continue;
        }
        nextByUid[uidKey] = {
            ...row,
            payrollHistoryId: item.payrollHistoryId || row.payrollHistoryId,
            applyEligible: true,
            eligible: true,
            rollbackEligible: false,
            standardNote: row.standardNote && String(row.standardNote).includes("可还原")
                ? "未定工资，可按进入方式办理。"
                : (row.standardNote || ""),
        };
    }
    state.newPersonnelSalaryRowsByUid = nextByUid;
    let visible = Object.values(nextByUid);
    const includeApply = document.getElementById("new-personnel-salary-include-apply")?.checked ?? true;
    const includeProcessed = document.getElementById("new-personnel-salary-include-processed")?.checked ?? true;
    if (!includeApply) {
        visible = visible.filter(row => row.rollbackEligible);
    }
    if (!includeProcessed) {
        visible = visible.filter(row => !row.rollbackEligible);
    }
    renderNewPersonnelSalaryTableRows(visible);
}

function findNewPersonnelSalaryRowByHistoryId(payrollHistoryId) {
    if (!payrollHistoryId) {
        return null;
    }
    return Object.values(state.newPersonnelSalaryRowsByUid || {})
        .find(row => row?.payrollHistoryId === payrollHistoryId) || null;
}

async function applyNewPersonnelSalaryAction(uid) {
    if (!ensurePayrollFeatureWrite("NEW_PERSONNEL_SALARY_WRITE", "新进定资")) {
        return;
    }
    if (!uid || !confirm("确认办理新进定资？")) {
        return;
    }
    const status = document.getElementById("new-personnel-salary-status");
    status.className = "status";
    status.textContent = "正在处理新进定资...";
    const writeStarted = performance.now();
    try {
        const result = await postJson(`/api/payroll/new-personnel-salary-determinations/${encodeURIComponent(uid)}/apply`, {});
        const writeMs = Math.round(performance.now() - writeStarted);
        const uiStarted = performance.now();
        markNewPersonnelSalariesProcessedLocally([{
            uid: Number(uid) || uid,
            previousPayrollHistoryId: String(uid),
            payrollHistoryId: result?.payrollHistoryId,
            changeType: result?.changeType,
        }]);
        const uiMs = Math.round(performance.now() - uiStarted);
        status.textContent = `${(result && result.message) || "新进定资处理完成"}（写库 ${writeMs}ms，界面更新 ${uiMs}ms，未重算列表）`;
    } catch (error) {
        showError(status, error);
    }
}

async function rollbackNewPersonnelSalaryAction(payrollHistoryId) {
    if (!ensurePayrollFeatureWrite("NEW_PERSONNEL_SALARY_WRITE", "新进定资")) {
        return;
    }
    if (!payrollHistoryId || !confirm("确认还原新进定资？")) {
        return;
    }
    const status = document.getElementById("new-personnel-salary-status");
    const row = findNewPersonnelSalaryRowByHistoryId(payrollHistoryId);
    const body = {
        organizationCode: row?.organizationCode || "",
        personCode: row?.personCode || "",
    };
    status.className = "status";
    status.textContent = "正在还原新进定资...";
    const writeStarted = performance.now();
    try {
        const result = await postJson(`/api/payroll/new-personnel-salary-determinations/${encodeURIComponent(payrollHistoryId)}/rollback`, body);
        const writeMs = Math.round(performance.now() - writeStarted);
        const uiStarted = performance.now();
        markNewPersonnelSalariesRolledBackLocally([{
            uid: row?.uid,
            previousPayrollHistoryId: payrollHistoryId,
            payrollHistoryId: result?.payrollHistoryId || payrollHistoryId,
            changeType: result?.changeType,
        }]);
        const uiMs = Math.round(performance.now() - uiStarted);
        status.textContent = `${(result && result.message) || "新进定资已还原"}（写库 ${writeMs}ms，界面更新 ${uiMs}ms，未重算列表）`;
    } catch (error) {
        showError(status, error);
    }
}

function bindNewPersonnelSalaryActions(container) {
    container.querySelectorAll("button[data-new-personnel-detail]").forEach(button => {
        button.addEventListener("click", () => {
            const uid = button.dataset.newPersonnelDetail;
            if (uid) {
                openNewPersonnelSalaryDetailModal(uid);
            }
        });
    });
    container.querySelectorAll("button[data-new-personnel-apply]").forEach(button => {
        button.addEventListener("click", () => applyNewPersonnelSalaryAction(button.dataset.newPersonnelApply));
    });
    container.querySelectorAll("button[data-new-personnel-rollback]").forEach(button => {
        button.addEventListener("click", () => rollbackNewPersonnelSalaryAction(button.dataset.newPersonnelRollback));
    });
}

async function applySelectedNewPersonnelSalaries() {
    if (!ensurePayrollFeatureWrite("NEW_PERSONNEL_SALARY_WRITE", "新进定资")) {
        return;
    }
    const selected = Array.from(document.querySelectorAll("[data-new-personnel-select]:checked"))
        .filter(checkbox => checkbox.dataset.newPersonnelAction === "apply");
    const status = document.getElementById("new-personnel-salary-status");
    if (!selected.length) {
        status.className = "status error";
        status.textContent = "请先勾选需要办理的新进定资记录。";
        return;
    }
    if (!confirm(`确认批量处理 ${selected.length} 条新进定资记录？`)) {
        return;
    }
    const items = [];
    for (const checkbox of selected) {
        const uid = checkbox.dataset.newPersonnelSelect;
        const row = state.newPersonnelSalaryRowsByUid?.[uid];
        if (!row || row.uid == null) {
            status.className = "status error";
            status.textContent = "部分勾选记录缺少试算结果，请重新查询后再办理。";
            return;
        }
        items.push({
            uid: Number(row.uid),
            organizationCode: row.organizationCode || "",
            personCode: row.personCode || "",
        });
    }
    status.className = "status";
    status.textContent = `正在批量处理 ${items.length} 条...`;
    const writeStarted = performance.now();
    try {
        const result = await postJson("/api/payroll/new-personnel-salary-determinations/batch-apply", { items });
        const writeMs = Math.round(performance.now() - writeStarted);
        const uiStarted = performance.now();
        const successItems = (result?.successes && result.successes.length)
            ? result.successes
            : (result?.successIds || []).map(id => ({
                uid: Number(id),
                previousPayrollHistoryId: String(id),
                payrollHistoryId: String(id),
            }));
        markNewPersonnelSalariesProcessedLocally(successItems);
        const uiMs = Math.round(performance.now() - uiStarted);
        status.className = (result?.failureCount || 0) ? "status error" : "status";
        const baseMessage = result?.message
            || `批量处理完成：成功 ${result?.successCount ?? 0} 条，失败 ${result?.failureCount ?? 0} 条。`;
        status.textContent = `${baseMessage}（写库 ${writeMs}ms，界面更新 ${uiMs}ms，未重算列表）`;
        console.info("[new-personnel-salary-batch]", { writeMs, uiMs, successCount: result?.successCount, failureCount: result?.failureCount });
    } catch (error) {
        showError(status, error);
    }
}

async function rollbackSelectedNewPersonnelSalaries() {
    if (!ensurePayrollFeatureWrite("NEW_PERSONNEL_SALARY_WRITE", "新进定资")) {
        return;
    }
    const selected = Array.from(document.querySelectorAll("[data-new-personnel-select]:checked"))
        .filter(checkbox => checkbox.dataset.newPersonnelAction === "rollback");
    const status = document.getElementById("new-personnel-salary-status");
    if (!selected.length) {
        status.className = "status error";
        status.textContent = "请先勾选需要还原的新进定资记录。";
        return;
    }
    if (!confirm(`确认批量还原 ${selected.length} 条新进定资记录？系统会删除对应链头记录并恢复上一条工资记录。`)) {
        return;
    }
    const items = [];
    for (const checkbox of selected) {
        const uid = checkbox.dataset.newPersonnelSelect;
        const row = state.newPersonnelSalaryRowsByUid?.[uid];
        const historyId = checkbox.dataset.newPersonnelHistory || row?.payrollHistoryId;
        if (!historyId) {
            status.className = "status error";
            status.textContent = "部分勾选记录缺少标识，请重新查询后再还原。";
            return;
        }
        items.push({
            payrollHistoryId: historyId,
            organizationCode: row?.organizationCode || "",
            personCode: row?.personCode || "",
        });
    }
    status.className = "status";
    status.textContent = `正在批量还原 ${items.length} 条...`;
    const writeStarted = performance.now();
    try {
        const result = await postJson("/api/payroll/new-personnel-salary-determinations/batch-rollback", { items });
        const writeMs = Math.round(performance.now() - writeStarted);
        const uiStarted = performance.now();
        const successItems = (result?.successes && result.successes.length)
            ? result.successes
            : (result?.successIds || []).map(id => ({
                previousPayrollHistoryId: id,
                payrollHistoryId: id,
            }));
        markNewPersonnelSalariesRolledBackLocally(successItems);
        const uiMs = Math.round(performance.now() - uiStarted);
        status.className = (result?.failureCount || 0) ? "status error" : "status";
        const baseMessage = result?.message
            || `批量还原完成：成功 ${result?.successCount ?? 0} 条，失败 ${result?.failureCount ?? 0} 条。`;
        status.textContent = `${baseMessage}（写库 ${writeMs}ms，界面更新 ${uiMs}ms，未重算列表）`;
        console.info("[new-personnel-salary-batch-rollback]", { writeMs, uiMs, successCount: result?.successCount, failureCount: result?.failureCount });
    } catch (error) {
        showError(status, error);
    }
}

function closeNewPersonnelSalaryDetailModal() {
    document.getElementById("new-personnel-salary-detail-modal")?.classList.add("hidden");
}

function renderNewPersonnelSalaryDetailContent(row) {
    document.getElementById("new-personnel-salary-detail-summary").textContent =
        `${row.organizationCode || ""}-${row.personCode || ""} ${row.name || ""} / ${row.changeType || ""}`
        + (row.rollbackEligible ? "（已处理，可还原）" : row.applyEligible ? "（待处理）" : "");

    const tipLevel = row.tipLevel || "";
    const tipStep = row.tipStep || "";
    const tipBaseCleared = Boolean(row.tipBaseCleared) || !(String(tipLevel).trim() || String(tipStep).trim());
    const tipLevelStart = tipBaseCleared ? "-" : (row.tipLevelAssessmentStartYear || "-");
    const tipStepStart = tipBaseCleared ? "-" : (row.tipStepAssessmentStartYear || "-");
    const tipPosition = tipBaseCleared
        ? "-"
        : (`${row.tipPositionCode || ""} ${row.tipPositionName || ""}`.trim()
            || `${row.positionCode || ""} ${row.positionName || ""}`.trim()
            || "-");
    const executePosition = `${row.positionCode || ""} ${row.positionName || ""}`.trim() || "-";
    const performanceCaption = row.performanceAllowanceCaption || "生活性补贴";
    const subsidyCaption = row.subsidyAllowanceCaption || "工作性津贴";
    const showSubsidy = row.showSubsidyAllowance !== false && Boolean(subsidyCaption);

    const metaRows = [
        `<tr><td>进入方式</td><td colspan="3">${escapeHtml(row.joinType || "-")}，参工 ${escapeHtml(row.workStartYearMonth || "-")}，进入 ${escapeHtml(row.joinYearMonth || "-")}</td></tr>`,
        `<tr><td>办理路径</td><td colspan="3">${escapeHtml(row.pathNote || row.standardNote || "-")}</td></tr>`,
        `<tr><td>起薪年月</td><td colspan="3">${escapeHtml(row.calculationPeriod || "-")}</td></tr>`,
        positionChangeDetailCompareRow("职务", tipPosition, executePosition),
        positionChangeDetailCompareRow("推算级别", tipLevel || "-", row.projectedLevel || row.executeLevel || "-"),
        positionChangeDetailCompareRow("推算档次/薪级", tipStep || "-", row.projectedStep || row.executeStep || "-"),
        positionChangeDetailCompareRow("拟执行级别", tipLevel || "-", row.executeLevel || "-"),
        positionChangeDetailCompareRow("拟执行档次/薪级", tipStep || "-", row.executeStep || "-"),
        positionChangeDetailCompareRow("级别起算年", tipLevelStart, row.levelAssessmentStartYear || "-"),
        positionChangeDetailCompareRow("档次起算年", tipStepStart, row.stepAssessmentStartYear || "-"),
    ].join("");

    const salaryRows = [
        positionChangeDetailMoneyRow("职务工资", row.tipPositionSalary, row.positionSalary, null),
        positionChangeDetailMoneyRow("级别/薪级工资", row.tipGradeSalary, row.gradeSalary, null),
        positionChangeDetailMoneyRow(performanceCaption, row.tipPerformanceAllowance, row.performanceAllowance, null),
        showSubsidy
            ? positionChangeDetailMoneyRow(subsidyCaption, row.tipSubsidyAllowance, row.subsidyAllowance, null)
            : "",
        positionChangeDetailMoneyRow("试算合计", row.tipTotal, row.calculatedTotal, null),
    ].filter(Boolean).join("");

    const processRows = [
        ...(row.explanationLines || []).map((line, index) => `
            <tr>
                <th>${index + 1}</th>
                <td>${escapeHtml(line)}</td>
            </tr>
        `),
        row.standardNote ? `<tr><th>说明</th><td>${escapeHtml(row.standardNote)}</td></tr>` : "",
    ].filter(Boolean).join("");

    document.getElementById("new-personnel-salary-detail-content").innerHTML = `
        <div class="detail-table-panel">
            <h4>基本信息与级别档次</h4>
            <table class="approval-component-table">
                <thead>
                    <tr>
                        <th class="approval-item-header">项目</th>
                        <th>基础/原值</th>
                        <th>拟执行/推算</th>
                        <th>备注</th>
                    </tr>
                </thead>
                <tbody>${metaRows}${salaryRows}</tbody>
            </table>
            <h4>试算过程</h4>
            <table class="approval-meta-table position-change-detail-steps">
                <thead>
                    <tr>
                        <th>步骤</th>
                        <th>说明</th>
                    </tr>
                </thead>
                <tbody>${processRows || `<tr><th>-</th><td>暂无试算说明</td></tr>`}</tbody>
            </table>
        </div>
    `;
}

async function openNewPersonnelSalaryDetailModal(uid) {
    const modal = document.getElementById("new-personnel-salary-detail-modal");
    const content = document.getElementById("new-personnel-salary-detail-content");
    document.getElementById("new-personnel-salary-detail-summary").textContent = "正在加载明细...";
    content.innerHTML = `<div class="status">正在加载试算明细...</div>`;
    modal.classList.remove("hidden");
    try {
        const row = await getJson(`/api/payroll/new-personnel-salary-determinations/${encodeURIComponent(uid)}`);
        renderNewPersonnelSalaryDetailContent(row);
    } catch (error) {
        document.getElementById("new-personnel-salary-detail-summary").textContent = "加载明细失败";
        content.innerHTML = `<div class="status error">${escapeHtml(error.message || "加载失败")}</div>`;
    }
}

let otherPayrollChangeContext = null;
let otherPayrollChangePreviewTimer = null;

async function onOtherPayrollChangeSearch(event) {
    event.preventDefault();
    state.otherPayrollChangePage = 0;
    await loadOtherPayrollChanges();
}

function gotoOtherPayrollChangePage(page) {
    const totalPages = Math.max(state.otherPayrollChangeTotalPages || 1, 1);
    const target = Math.min(Math.max(page, 0), totalPages - 1);
    if (target === state.otherPayrollChangePage) {
        return;
    }
    state.otherPayrollChangePage = target;
    void loadOtherPayrollChanges();
}

function renderOtherPayrollChangePagination(totalElements, totalPages) {
    const bar = document.getElementById("other-payroll-change-pagination");
    if (!bar) {
        return;
    }
    const pages = Math.max(totalPages || 1, 1);
    state.otherPayrollChangeTotalPages = pages;
    const current = state.otherPayrollChangePage;
    bar.classList.toggle("hidden", !totalElements);
    const totalPagesEl = document.getElementById("other-payroll-change-total-pages");
    const totalCountEl = document.getElementById("other-payroll-change-total-count");
    const pageInput = document.getElementById("other-payroll-change-page-input");
    if (totalPagesEl) {
        totalPagesEl.textContent = String(pages);
    }
    if (totalCountEl) {
        totalCountEl.textContent = String(totalElements || 0);
    }
    if (pageInput) {
        pageInput.value = String(current + 1);
        pageInput.max = String(pages);
        pageInput.disabled = !totalElements;
    }
    const noData = !totalElements;
    document.getElementById("other-payroll-change-first").disabled = noData || current <= 0;
    document.getElementById("other-payroll-change-prev").disabled = noData || current <= 0;
    document.getElementById("other-payroll-change-next").disabled = noData || current >= pages - 1;
    document.getElementById("other-payroll-change-last").disabled = noData || current >= pages - 1;
}

function parseCalculationPeriod(period) {
    if (!period) {
        return { year: "", month: "" };
    }
    const digits = String(period).replace(/\D/g, "");
    if (digits.length >= 6) {
        return { year: digits.slice(0, 4), month: digits.slice(4, 6) };
    }
    const normalized = String(period).trim().replace(/-/g, ".");
    const parts = normalized.split(".");
    return {
        year: parts[0] || "",
        month: parts[1] ? String(parts[1]).padStart(2, "0") : "",
    };
}

function renderOtherPayrollChangeActions(row) {
    const canWrite = hasPayrollFeatureWrite("OTHER_PAYROLL_CHANGE_WRITE");
    const canApply = Boolean(row.applyEligible);
    const canRollback = Boolean(row.rollbackEligible);
    if (!canWrite || (!canApply && !canRollback)) {
        return "-";
    }
    const parts = [];
    if (canApply) {
        parts.push(`<button class="row-action" type="button" data-other-payroll-apply="${escapeHtml(row.payrollHistoryId)}">处理</button>`);
    }
    if (canRollback) {
        parts.push(`<button class="row-action" type="button" data-other-payroll-rollback="${escapeHtml(row.payrollHistoryId)}">还原</button>`);
    }
    return parts.join(" ");
}

function bindOtherPayrollChangeActions(container, rows) {
    container.querySelectorAll("[data-other-payroll-apply]").forEach(button => {
        button.addEventListener("click", () => {
            if (!ensurePayrollFeatureWrite("OTHER_PAYROLL_CHANGE_WRITE", "其它情况工资变动")) {
                return;
            }
            const payrollHistoryId = button.getAttribute("data-other-payroll-apply");
            openOtherPayrollChangeModal(payrollHistoryId, rows);
        });
    });
    container.querySelectorAll("[data-other-payroll-rollback]").forEach(button => {
        button.addEventListener("click", async () => {
            if (!ensurePayrollFeatureWrite("OTHER_PAYROLL_CHANGE_WRITE", "其它情况工资变动")) {
                return;
            }
            const payrollHistoryId = button.getAttribute("data-other-payroll-rollback");
            if (!confirm("确认还原该人员的其它情况工资变动？系统将删除本模块办理的最新记录并恢复上一条。")) {
                return;
            }
            const status = document.getElementById("other-payroll-change-status");
            status.className = "status";
            status.textContent = "正在还原其它情况工资变动...";
            try {
                const result = await postJson(`/api/payroll/other-payroll-changes/${encodeURIComponent(payrollHistoryId)}/rollback`, {});
                status.textContent = (result && result.message) || "其它情况工资变动已还原";
                await loadOtherPayrollChanges();
            } catch (error) {
                showError(status, error);
            }
        });
    });
}

function otherPayrollCompareRowHtml(field) {
    const beforeDisplay = field.number
        ? formatPayrollCompareBeforeValue(field.before, "number")
        : formatPayrollCompareBeforeValue(field.before, "text");
    const beforeAttr = escapeHtml(field.before == null ? "" : field.before);
    let afterControl;
    if (field.readonly) {
        afterControl = `<input id="${escapeHtml(field.id)}" data-other-field="${escapeHtml(field.key)}" data-compare-type="${field.number ? "number" : "text"}" data-compare-before="${beforeAttr}" type="${field.number ? "number" : "text"}" value="${escapeHtml(field.after == null ? "" : field.after)}" readonly>`;
    } else if (field.datalist) {
        afterControl = `
            <input id="${escapeHtml(field.id)}" list="${escapeHtml(field.datalist)}" data-other-field="${escapeHtml(field.key)}" data-compare-type="text" data-compare-before="${beforeAttr}" type="text" value="${escapeHtml(field.after == null ? "" : field.after)}" ${field.required ? "required" : ""} placeholder="${escapeHtml(field.placeholder || "")}">
        `;
    } else if (field.picker) {
        afterControl = `
            <div class="dict-input-combo">
                <input id="${escapeHtml(field.id)}" data-other-field="${escapeHtml(field.key)}" data-compare-type="text" data-compare-before="${beforeAttr}" type="text" value="${escapeHtml(field.after == null ? "" : field.after)}" ${field.required ? "required" : ""} ${field.readonlyName ? "readonly" : ""}>
                <button type="button" class="dict-picker-button" data-other-picker="${escapeHtml(field.key)}" aria-label="选择">⌄</button>
            </div>
        `;
    } else {
        afterControl = `<input id="${escapeHtml(field.id)}" data-other-field="${escapeHtml(field.key)}" data-compare-type="${field.number ? "number" : "text"}" data-compare-before="${beforeAttr}" type="${field.number ? "number" : "text"}" value="${escapeHtml(field.after == null ? "" : field.after)}" ${field.required ? "required" : ""}>`;
    }
    const diffText = field.number
        ? formatPayrollCompareDiff(field.before, field.after, "number")
        : formatPayrollCompareDiff(field.before, field.after, "text");
    return `
        <div class="payroll-compare-row${diffText ? " is-changed" : ""}${field.emphasis ? " is-emphasis" : ""}${field.hidden ? " hidden" : ""}" data-compare-row="${escapeHtml(field.key)}">
            <div class="payroll-compare-label">${escapeHtml(field.label)}</div>
            <div class="payroll-compare-before">${escapeHtml(beforeDisplay)}</div>
            <div class="payroll-compare-after">${afterControl}</div>
            <div class="payroll-compare-diff" data-compare-diff="${escapeHtml(field.key)}">${escapeHtml(diffText)}</div>
        </div>
    `;
}

function renderOtherPayrollChangeCompare(detail, calc) {
    const period = parseCalculationPeriod(detail.currentPeriod);
    const performanceCaption = (calc && calc.performanceAllowanceCaption)
        || detail.performanceAllowanceCaption
        || "绩效/生活补贴";
    const subsidyCaption = (calc && calc.subsidyAllowanceCaption)
        || detail.subsidyAllowanceCaption
        || "工作性津贴";
    const showSubsidy = calc ? Boolean(calc.showSubsidyAllowance) : Boolean(detail.showSubsidyAllowance);
    const after = calc || {
        changeType: "",
        positionCode: detail.currentPositionCode,
        positionName: detail.currentPositionName,
        gradeSalaryLevel: detail.currentLevel,
        positionSalaryGrade: detail.currentStep,
        salaryStandardYearMonth: detail.currentSalaryStandardYearMonth,
        allowanceStandardYearMonth: detail.currentAllowanceStandardYearMonth,
        positionSalary: detail.currentPositionSalary,
        gradeSalary: detail.currentGradeSalary,
        technicalGradeSalary: detail.currentTechnicalGradeSalary,
        performanceAllowance: detail.currentPerformanceAllowance,
        subsidyAllowance: detail.currentSubsidyAllowance,
        retainedAllowance: detail.currentRetainedAllowance,
        teachingAllowance: detail.currentTeachingAllowance,
        totalAmount: detail.currentTotal,
    };
    const changeTypeOptions = (detail.changeTypeOptions || [])
        .map(type => `<option value="${escapeHtml(type)}"></option>`)
        .join("");
    const fields = [
        { key: "year", id: "other-payroll-change-year", label: "变动年度", before: period.year, after: (calc && calc.calculationPeriod ? calc.calculationPeriod.slice(0, 4) : period.year), required: true },
        { key: "month", id: "other-payroll-change-month", label: "变动月份", before: period.month, after: (calc && calc.calculationPeriod ? calc.calculationPeriod.slice(4, 6) : period.month), required: true },
        { key: "changeType", id: "other-payroll-change-type", label: "变动类别", before: detail.currentChangeType, after: after.changeType || "", required: true, datalist: "other-payroll-change-type-options", placeholder: "如：奖惩、特殊调整" },
        { key: "positionName", id: "other-payroll-change-position-name", label: "岗位", before: detail.currentPositionName || detail.currentPositionCode, after: after.positionName || after.positionCode || "", picker: true, required: true },
        { key: "positionCode", id: "other-payroll-change-position-code", label: "岗位编码", before: detail.currentPositionCode, after: after.positionCode || "", required: true, hidden: true },
        { key: "level", id: "other-payroll-change-level", label: "级别", before: detail.currentLevel, after: after.gradeSalaryLevel || "" },
        { key: "step", id: "other-payroll-change-step", label: "档次/薪级", before: detail.currentStep, after: after.positionSalaryGrade || "" },
        { key: "salaryStandard", id: "other-payroll-change-salary-standard", label: "工资标准年月", before: detail.currentSalaryStandardYearMonth, after: after.salaryStandardYearMonth || "" },
        { key: "allowanceStandard", id: "other-payroll-change-allowance-standard", label: "津补贴标准年月", before: detail.currentAllowanceStandardYearMonth, after: after.allowanceStandardYearMonth || "" },
        { key: "positionSalary", id: "other-payroll-change-position-salary", label: "职务工资", before: detail.currentPositionSalary, after: after.positionSalary, number: true, readonly: true },
        { key: "gradeSalary", id: "other-payroll-change-grade-salary", label: "级别/薪级工资", before: detail.currentGradeSalary, after: after.gradeSalary, number: true, readonly: true },
        { key: "technicalSalary", id: "other-payroll-change-technical-salary", label: "技术等级工资", before: detail.currentTechnicalGradeSalary, after: after.technicalGradeSalary, number: true, readonly: true },
        { key: "performance", id: "other-payroll-change-performance-allowance", label: performanceCaption, before: detail.currentPerformanceAllowance, after: after.performanceAllowance, number: true, readonly: true },
        { key: "subsidy", id: "other-payroll-change-subsidy-allowance", label: subsidyCaption, before: detail.currentSubsidyAllowance, after: after.subsidyAllowance, number: true, readonly: true, hidden: !showSubsidy },
        { key: "retained", id: "other-payroll-change-retained-allowance", label: "保留福补", before: detail.currentRetainedAllowance, after: after.retainedAllowance, number: true, readonly: true },
        { key: "teaching", id: "other-payroll-change-teaching-allowance", label: "教护龄津贴", before: detail.currentTeachingAllowance, after: after.teachingAllowance, number: true, readonly: true },
        { key: "total", id: "other-payroll-change-total", label: "合计", before: detail.currentTotal, after: after.totalAmount, number: true, readonly: true, emphasis: true },
    ];
    const body = document.getElementById("other-payroll-change-compare-body");
    body.innerHTML = `
        <datalist id="other-payroll-change-type-options">${changeTypeOptions}</datalist>
        ${fields.map(otherPayrollCompareRowHtml).join("")}
    `;
    bindOtherPayrollChangeCompareInteractions();
}

function readOtherPayrollChangeDraft() {
    return {
        calculationYear: document.getElementById("other-payroll-change-year")?.value.trim() || "",
        calculationMonth: (document.getElementById("other-payroll-change-month")?.value.trim() || "").padStart(2, "0"),
        changeType: document.getElementById("other-payroll-change-type")?.value.trim() || "",
        positionCode: document.getElementById("other-payroll-change-position-code")?.value.trim() || "",
        positionName: document.getElementById("other-payroll-change-position-name")?.value.trim() || "",
        gradeSalaryLevel: document.getElementById("other-payroll-change-level")?.value.trim() || "",
        positionSalaryGrade: document.getElementById("other-payroll-change-step")?.value.trim() || "",
        salaryStandardYearMonth: document.getElementById("other-payroll-change-salary-standard")?.value.trim() || "",
        allowanceStandardYearMonth: document.getElementById("other-payroll-change-allowance-standard")?.value.trim() || "",
    };
}

function applyOtherPayrollCalcToReadonly(calc) {
    if (!calc) {
        return;
    }
    const setValue = (id, value) => {
        const input = document.getElementById(id);
        if (input) {
            input.value = value == null ? "" : value;
        }
    };
    setValue("other-payroll-change-position-salary", calc.positionSalary);
    setValue("other-payroll-change-grade-salary", calc.gradeSalary);
    setValue("other-payroll-change-technical-salary", calc.technicalGradeSalary);
    setValue("other-payroll-change-performance-allowance", calc.performanceAllowance);
    setValue("other-payroll-change-subsidy-allowance", calc.subsidyAllowance);
    setValue("other-payroll-change-retained-allowance", calc.retainedAllowance);
    setValue("other-payroll-change-teaching-allowance", calc.teachingAllowance);
    setValue("other-payroll-change-total", calc.totalAmount);
    const form = document.getElementById("other-payroll-change-apply-form");
    form.querySelectorAll("[data-other-field]").forEach(input => {
        const name = input.dataset.otherField;
        const row = form.querySelector(`[data-compare-row="${name}"]`);
        const diffHost = form.querySelector(`[data-compare-diff="${name}"]`);
        if (!row || !diffHost) {
            return;
        }
        const inputType = input.dataset.compareType === "number" ? "number" : "text";
        const beforeRaw = input.dataset.compareBefore ?? "";
        const afterRaw = inputType === "number" ? Number(input.value || 0) : input.value;
        const beforeValue = inputType === "number" ? Number(beforeRaw || 0) : beforeRaw;
        const diffText = formatPayrollCompareDiff(beforeValue, afterRaw, inputType);
        diffHost.textContent = diffText;
        row.classList.toggle("is-changed", Boolean(diffText));
    });
    const subsidyRow = form.querySelector(`[data-compare-row="subsidy"]`);
    if (subsidyRow) {
        subsidyRow.classList.toggle("hidden", !calc.showSubsidyAllowance);
    }
    const performanceLabel = form.querySelector(`[data-compare-row="performance"] .payroll-compare-label`);
    if (performanceLabel && calc.performanceAllowanceCaption) {
        performanceLabel.textContent = calc.performanceAllowanceCaption;
    }
    const subsidyLabel = form.querySelector(`[data-compare-row="subsidy"] .payroll-compare-label`);
    if (subsidyLabel && calc.subsidyAllowanceCaption) {
        subsidyLabel.textContent = calc.subsidyAllowanceCaption;
    }
    const summary = document.getElementById("other-payroll-change-modal-summary");
    if (summary && calc.note) {
        summary.textContent = calc.note;
    }
}

async function refreshOtherPayrollChangePreview() {
    if (!otherPayrollChangeContext?.detail) {
        return;
    }
    const draft = readOtherPayrollChangeDraft();
    if (!draft.calculationYear || !draft.calculationMonth || !draft.changeType || !draft.positionCode) {
        return;
    }
    const modalStatus = document.getElementById("other-payroll-change-modal-status");
    try {
        const calc = await postJson(
            `/api/payroll/other-payroll-changes/${encodeURIComponent(otherPayrollChangeContext.detail.payrollHistoryId)}/preview`,
            draft);
        otherPayrollChangeContext.calc = calc;
        applyOtherPayrollCalcToReadonly(calc);
        modalStatus.className = "status";
        modalStatus.textContent = "已按所选结构自动计算工资项。";
    } catch (error) {
        showError(modalStatus, error);
    }
}

function scheduleOtherPayrollChangePreview() {
    if (otherPayrollChangePreviewTimer) {
        clearTimeout(otherPayrollChangePreviewTimer);
    }
    otherPayrollChangePreviewTimer = setTimeout(() => {
        void refreshOtherPayrollChangePreview();
    }, 350);
}

function bindOtherPayrollChangeCompareInteractions() {
    const form = document.getElementById("other-payroll-change-apply-form");
    form.querySelectorAll("[data-other-field]").forEach(input => {
        if (input.readOnly) {
            return;
        }
        input.addEventListener("input", scheduleOtherPayrollChangePreview);
        input.addEventListener("change", scheduleOtherPayrollChangePreview);
    });
    form.querySelectorAll("[data-other-picker]").forEach(button => {
        button.addEventListener("click", () => {
            openDictionaryPicker("other-payroll-change-position-name", {
                fieldName: "xzzw",
                caption: "执行工资职务",
                dictionaryPrefix: "051",
                dictionaryFieldKey: "xzzw",
                linkedCodeInputId: "other-payroll-change-position-code",
                useFullDictionaryCode: true,
                codeMaxLength: 4,
            });
        });
    });
}

async function openOtherPayrollChangeModal(payrollHistoryId) {
    const modal = document.getElementById("other-payroll-change-modal");
    const modalStatus = document.getElementById("other-payroll-change-modal-status");
    const summary = document.getElementById("other-payroll-change-modal-summary");
    modalStatus.className = "status";
    modalStatus.textContent = "正在加载对账表...";
    summary.textContent = "对照当前执行工资录入变动信息；自动计算项只读带出。";
    document.getElementById("other-payroll-change-compare-body").innerHTML = `<div class="status">加载中...</div>`;
    modal.classList.remove("hidden");
    try {
        const detail = await getJson(`/api/payroll/other-payroll-changes/${encodeURIComponent(payrollHistoryId)}`);
        otherPayrollChangeContext = { detail, calc: null };
        document.getElementById("other-payroll-change-modal-title").textContent =
            `办理其它情况工资变动 — ${detail.name || ""}（${detail.personCode || ""}）`;
        renderOtherPayrollChangeCompare(detail, null);
        modalStatus.textContent = "请选择变动类别与结构项，系统将自动计算工资金额。";
        scheduleOtherPayrollChangePreview();
    } catch (error) {
        showError(modalStatus, error);
    }
}

function closeOtherPayrollChangeModal() {
    otherPayrollChangeContext = null;
    if (otherPayrollChangePreviewTimer) {
        clearTimeout(otherPayrollChangePreviewTimer);
        otherPayrollChangePreviewTimer = null;
    }
    document.getElementById("other-payroll-change-modal").classList.add("hidden");
}

async function onOtherPayrollChangeApply(event) {
    event.preventDefault();
    if (!otherPayrollChangeContext?.detail) {
        return;
    }
    const payload = readOtherPayrollChangeDraft();
    if (!payload.changeType) {
        showError(document.getElementById("other-payroll-change-modal-status"), new Error("变动类别不能为空。"));
        return;
    }
    if (!payload.positionCode) {
        showError(document.getElementById("other-payroll-change-modal-status"), new Error("请选择岗位。"));
        return;
    }
    if (!confirm(`确认对 ${otherPayrollChangeContext.detail.name || otherPayrollChangeContext.detail.personCode} 办理「${payload.changeType}」工资变动？`)) {
        return;
    }
    const modalStatus = document.getElementById("other-payroll-change-modal-status");
    modalStatus.className = "status";
    modalStatus.textContent = "正在办理其它情况工资变动...";
    try {
        const result = await postJson(
            `/api/payroll/other-payroll-changes/${encodeURIComponent(otherPayrollChangeContext.detail.payrollHistoryId)}/apply`,
            payload);
        closeOtherPayrollChangeModal();
        const status = document.getElementById("other-payroll-change-status");
        status.className = "status";
        status.textContent = (result && result.message) || "其它情况工资变动处理完成";
        await loadOtherPayrollChanges();
    } catch (error) {
        showError(modalStatus, error);
    }
}

async function loadOtherPayrollChanges() {
    const organizationCode = selectedOrganizationCode("other-payroll-change-organization-code");
    const keyword = document.getElementById("other-payroll-change-keyword").value.trim();
    const page = String(state.otherPayrollChangePage || 0);
    const size = document.getElementById("other-payroll-change-size").value || "20";
    const params = new URLSearchParams({ page, size });
    if (organizationCode) {
        params.set("organizationCode", organizationCode);
    }
    if (keyword) {
        params.set("keyword", keyword);
    }
    const status = document.getElementById("other-payroll-change-status");
    const rows = document.getElementById("other-payroll-change-rows");
    status.className = "status";
    status.textContent = "正在查询其它情况工资变动...";
    rows.innerHTML = "";
    try {
        const result = await getJson(`/api/payroll/other-payroll-changes?${params}`);
        const content = result.content || [];
        const totalPages = Math.max(result.totalPages || 1, 1);
        if ((result.page || 0) > totalPages - 1 && totalPages > 0) {
            state.otherPayrollChangePage = Math.max(totalPages - 1, 0);
            return loadOtherPayrollChanges();
        }
        state.otherPayrollChangePage = result.page || 0;
        state.otherPayrollChangeTotalPages = totalPages;
        rows.innerHTML = content.length ? content.map(row => `
            <tr>
                <td class="col-org" title="${escapeHtml(row.organizationName || row.organizationCode || "")}">${escapeHtml(row.organizationName || row.organizationCode || "")}</td>
                <td class="col-code">${escapeHtml(row.personCode || "")}</td>
                <td class="col-name">${escapeHtml(row.name || "")}</td>
                <td class="col-period">${escapeHtml(row.calculationPeriod || "")}</td>
                <td class="col-type">${escapeHtml(row.changeType || "")}</td>
                <td class="col-position" title="${escapeHtml(row.positionName || row.positionCode || "")}">${escapeHtml(row.positionName || row.positionCode || "")}</td>
                <td class="col-amount">${money(row.positionSalary)}</td>
                <td class="col-amount">${money(row.gradeSalary)}</td>
                <td class="col-amount">${money(row.technicalGradeSalary)}</td>
                <td class="col-amount">${money(row.performanceAllowance)}</td>
                <td class="col-amount">${money(row.retainedAllowance)}</td>
                <td class="col-amount">${money(row.currentTotal)}</td>
                <td class="col-actions">${renderOtherPayrollChangeActions(row)}</td>
            </tr>
        `).join("") : `<tr><td colspan="13">暂无记录</td></tr>`;
        bindOtherPayrollChangeActions(rows, content);
        renderOtherPayrollChangePagination(result.totalElements || 0, totalPages);
        status.textContent = `第 ${state.otherPayrollChangePage + 1} / ${totalPages} 页，共 ${result.totalElements || 0} 条记录`;
    } catch (error) {
        renderOtherPayrollChangePagination(0, 1);
        showError(status, error);
    }
}

async function loadFloatingToFixedConversions() {
    const organizationCode = selectedOrganizationCode("floating-to-fixed-organization-code");
    const keyword = document.getElementById("floating-to-fixed-keyword").value.trim();
    const page = document.getElementById("floating-to-fixed-page").value || "0";
    const size = document.getElementById("floating-to-fixed-size").value || "20";
    const params = new URLSearchParams({ page, size });
    if (organizationCode) {
        params.set("organizationCode", organizationCode);
    }
    if (keyword) {
        params.set("keyword", keyword);
    }

    const status = document.getElementById("floating-to-fixed-status");
    const rows = document.getElementById("floating-to-fixed-rows");
    status.className = "status";
    status.textContent = "正在查询浮动固定试算...";
    rows.innerHTML = "";

    try {
        const result = await getJson(`/api/payroll/floating-to-fixed-conversions?${params}`);
        rows.innerHTML = (result.content || []).map(row => `
            <tr>
                <td>${escapeHtml(row.organizationCode)}</td>
                <td>${escapeHtml(row.personCode)}</td>
                <td>${escapeHtml(row.name)}</td>
                <td>${escapeHtml(row.calculationPeriod || "")}</td>
                <td>${escapeHtml(row.duePeriod || "")}</td>
                <td>${escapeHtml(row.floatingStartYearMonth || "")}</td>
                <td>${escapeHtml(row.positionName || "")}</td>
                <td>${escapeHtml(row.floatingSteps || "")}</td>
                <td>${escapeHtml(row.currentGradeStep || "")}</td>
                <td>${escapeHtml(row.nextGradeStep || "")}</td>
                <td>${money(row.storedFloatingSalary)}</td>
                <td>${money(row.nextTotal)}</td>
                <td>${money(row.differenceAmount)}</td>
                <td>${renderSimplePromotionActions(row, "floating-to-fixed-conversions", "浮动固定", loadFloatingToFixedConversions)}</td>
            </tr>
        `).join("");
        bindSimplePromotionActions(rows, loadFloatingToFixedConversions);
        status.textContent = `第 ${result.page + 1} / ${Math.max(result.totalPages, 1)} 页，共 ${result.totalElements} 条试算记录`;
    } catch (error) {
        showError(status, error);
    }
}

function baseSalarySourceName(source) {
    return {
        GRADE: "级别工资",
        SALARY_LEVEL: "薪级工资",
        WORKER_GRADE: "机关工勤档次工资",
        JUDICIAL_GRADE: "法检档次工资",
        POLICE_GRADE: "级别工资",
        TECHNICAL_GRADE: "技术等级工资",
    }[source] || source || "";
}

function basicStandardUsesPositionCategory(standardType) {
    return standardType === "position"
        || standardType === "position-grade"
        || standardType === "salary-level";
}

function updateBasicStandardPositionCategoryVisibility() {
    const wrap = document.getElementById("basic-standard-position-category-wrap");
    if (!wrap) {
        return;
    }
    const type = document.getElementById("basic-standard-type")?.value || "";
    wrap.classList.toggle("hidden", !basicStandardUsesPositionCategory(type));
    const label = document.getElementById("basic-standard-position-category-label");
    if (label) {
        label.textContent = type === "salary-level" ? "岗位类别" : "职务岗位类别";
    }
}

async function refreshBasicStandardPeriods() {
    const select = document.getElementById("basic-standard-year-month");
    const typeEl = document.getElementById("basic-standard-type");
    if (!select || !typeEl) {
        return;
    }
    const standardType = typeEl.value;
    const previous = select.value;
    try {
        const periods = await getJson(`/api/payroll/basic-standards/periods?standardType=${encodeURIComponent(standardType)}`);
        const options = [`<option value="">请选择</option>`]
            .concat((periods || []).map(period =>
                `<option value="${escapeHtml(period)}">${escapeHtml(period)}</option>`));
        select.innerHTML = options.join("");
        if (previous && (periods || []).includes(previous)) {
            select.value = previous;
        } else if ((periods || []).length) {
            select.value = periods[0];
        }
    } catch (error) {
        console.warn("加载基本工资标准年月失败", error);
        select.innerHTML = `<option value="">请选择</option>`;
    }
}

async function refreshBasicSalaryStandardAdjustmentPeriods() {
    syncBasicSalaryStandardAdjustmentTableHeader();
    const select = document.getElementById("basic-salary-standard-adjustment-target");
    if (!select) {
        return;
    }
    const previous = select.value;
    try {
        const periods = await getJson("/api/payroll/basic-salary-standard-adjustments/periods");
        const options = [`<option value="">请选择</option>`]
            .concat((periods || []).map(period =>
                `<option value="${escapeHtml(period)}">${escapeHtml(period)}</option>`));
        select.innerHTML = options.join("");
        if (previous && (periods || []).includes(previous)) {
            select.value = previous;
        } else if ((periods || []).length) {
            select.value = periods[0];
        }
    } catch (error) {
        console.warn("加载调标标准年月失败", error);
        select.innerHTML = `<option value="">请选择</option>`;
    }
}

async function onBasicSalaryStandardAdjustmentSearch(event) {
    event.preventDefault();
    state.basicSalaryStandardAdjustmentPage = 0;
    await loadBasicSalaryStandardAdjustments();
}

function gotoBasicSalaryStandardAdjustmentPage(page) {
    const totalPages = Math.max(state.basicSalaryStandardAdjustmentTotalPages || 1, 1);
    const target = Math.min(Math.max(page, 0), totalPages - 1);
    if (target === (state.basicSalaryStandardAdjustmentPage || 0)) {
        return;
    }
    state.basicSalaryStandardAdjustmentPage = target;
    void loadBasicSalaryStandardAdjustments();
}

function renderBasicSalaryStandardAdjustmentPagination(totalElements, totalPages) {
    const bar = document.getElementById("basic-salary-standard-adjustment-pagination");
    if (!bar) {
        return;
    }
    const pages = Math.max(totalPages || 1, 1);
    state.basicSalaryStandardAdjustmentTotalPages = pages;
    const current = state.basicSalaryStandardAdjustmentPage || 0;
    bar.classList.toggle("hidden", !totalElements);
    const totalPagesEl = document.getElementById("basic-salary-standard-adjustment-total-pages");
    const totalCountEl = document.getElementById("basic-salary-standard-adjustment-total-count");
    const pageInput = document.getElementById("basic-salary-standard-adjustment-page-input");
    if (totalPagesEl) {
        totalPagesEl.textContent = String(pages);
    }
    if (totalCountEl) {
        totalCountEl.textContent = String(totalElements || 0);
    }
    if (pageInput) {
        pageInput.value = String(current + 1);
        pageInput.max = String(pages);
        pageInput.disabled = !totalElements;
    }
    const noData = !totalElements;
    const first = document.getElementById("basic-salary-standard-adjustment-first");
    const prev = document.getElementById("basic-salary-standard-adjustment-prev");
    const next = document.getElementById("basic-salary-standard-adjustment-next");
    const last = document.getElementById("basic-salary-standard-adjustment-last");
    if (first) {
        first.disabled = noData || current <= 0;
    }
    if (prev) {
        prev.disabled = noData || current <= 0;
    }
    if (next) {
        next.disabled = noData || current >= pages - 1;
    }
    if (last) {
        last.disabled = noData || current >= pages - 1;
    }
}

async function loadBasicSalaryStandardAdjustments() {
    const organizationCode = selectedOrganizationCode("basic-salary-standard-adjustment-organization-code");
    const target = document.getElementById("basic-salary-standard-adjustment-target")?.value?.trim() || "";
    const keyword = document.getElementById("basic-salary-standard-adjustment-keyword")?.value?.trim() || "";
    const status = document.getElementById("basic-salary-standard-adjustment-status");
    const rows = document.getElementById("basic-salary-standard-adjustment-rows");
    if (!target) {
        status.className = "status error";
        status.textContent = "请选择调标标准年月。";
        renderBasicSalaryStandardAdjustmentPagination(0, 1);
        return;
    }
    status.className = "status";
    status.textContent = "正在查询调整基本工资标准试算...";
    rows.innerHTML = "";
    const page = String(state.basicSalaryStandardAdjustmentPage || 0);
    const size = document.getElementById("basic-salary-standard-adjustment-page-size")?.value || "50";
    const params = new URLSearchParams({
        targetStandardYearMonth: target,
        page,
        size,
    });
    if (!document.getElementById("basic-salary-standard-adjustment-include-apply")?.checked) {
        params.set("includeApply", "false");
    }
    if (!document.getElementById("basic-salary-standard-adjustment-include-processed")?.checked) {
        params.set("includeProcessed", "false");
    }
    if (organizationCode) {
        params.set("organizationCode", organizationCode);
    }
    if (keyword) {
        params.set("keyword", keyword);
    }
    const laterPeriodMode = basicSalaryStandardAdjustmentLaterPeriodMode();
    if (laterPeriodMode) {
        params.set("laterPeriodMode", laterPeriodMode);
    }
    try {
        const result = await getJson(`/api/payroll/basic-salary-standard-adjustments?${params}`);
        const totalPages = Math.max(result.totalPages || 0, 1);
        if ((result.page || 0) >= totalPages && totalPages > 0 && (result.totalElements || 0) > 0) {
            state.basicSalaryStandardAdjustmentPage = Math.max(totalPages - 1, 0);
            return loadBasicSalaryStandardAdjustments();
        }
        state.basicSalaryStandardAdjustmentPage = result.page || 0;
        state.basicSalaryStandardAdjustmentTotalPages = totalPages;
        const selectAll = document.getElementById("basic-salary-standard-adjustment-select-all");
        if (selectAll) {
            selectAll.checked = false;
        }
        const byId = {};
        (result.content || []).forEach(row => {
            if (row?.payrollHistoryId) {
                byId[row.payrollHistoryId] = row;
            }
        });
        state.basicSalaryStandardAdjustmentRowsById = byId;
        state.basicSalaryStandardAdjustmentListMeta = {
            target,
            totalElements: result.totalElements ?? (result.content || []).length,
        };
        renderBasicSalaryStandardAdjustmentRows(result.content || []);
        renderBasicSalaryStandardAdjustmentPagination(result.totalElements || 0, totalPages);
        status.textContent = `第 ${state.basicSalaryStandardAdjustmentPage + 1} / ${totalPages} 页，共 ${result.totalElements ?? 0} 条试算记录（目标标准 ${target}）`;
    } catch (error) {
        renderBasicSalaryStandardAdjustmentPagination(0, 1);
        showError(status, error);
    }
}

function sortBasicSalaryStandardAdjustmentRows(rows) {
    return [...(rows || [])].sort((left, right) => {
        const orgCompare = String(left.organizationCode || "").localeCompare(String(right.organizationCode || ""), "zh-CN");
        if (orgCompare !== 0) {
            return orgCompare;
        }
        return String(left.personCode || "").localeCompare(String(right.personCode || ""), "zh-CN", { numeric: true });
    });
}

function syncBasicSalaryStandardAdjustmentTableHeader() {
    const theadRow = document.querySelector("#basic-salary-standard-adjustment .basic-salary-standard-adjustment-table thead tr");
    if (!theadRow) {
        return;
    }
    theadRow.innerHTML = `
        <th class="col-select"><input id="basic-salary-standard-adjustment-select-all" type="checkbox" aria-label="全选工资调标"></th>
        <th>单位</th>
        <th>人员编码</th>
        <th>姓名</th>
        <th>当前 tbnd</th>
        <th>当前变动类别</th>
        <th>变动年月</th>
        <th>岗位</th>
        <th>当前合计</th>
        <th>试算合计</th>
        <th>差额</th>
        <th>说明</th>
        <th class="col-action">操作</th>`;
    document.getElementById("basic-salary-standard-adjustment-select-all")?.addEventListener("change", event => {
        document.querySelectorAll("[data-basic-adj-select]").forEach(checkbox => {
            checkbox.checked = event.target.checked;
        });
    });
}

function renderBasicSalaryStandardAdjustmentRows(content) {
    const rows = document.getElementById("basic-salary-standard-adjustment-rows");
    if (!rows) {
        return;
    }
    const canWrite = hasPayrollFeatureWrite("BASIC_SALARY_STANDARD_ADJUSTMENT_WRITE");
    const selectAll = document.getElementById("basic-salary-standard-adjustment-select-all");
    if (selectAll) {
        selectAll.checked = false;
    }
    rows.innerHTML = sortBasicSalaryStandardAdjustmentRows(content).map(row => {
        const canApply = Boolean(row.applyEligible);
        const canRollback = Boolean(row.rollbackEligible);
        const canSelect = canWrite && (canApply || canRollback);
        const selectAction = canApply ? "apply" : (canRollback ? "rollback" : "");
        return `
            <tr class="${row.rollbackEligible ? "highlight-row" : ""}">
                <td class="col-select${canWrite ? "" : " hidden"}">${canSelect
                    ? `<input type="checkbox" data-basic-adj-select="${escapeHtml(row.payrollHistoryId)}" data-basic-adj-action="${selectAction}" aria-label="选择 ${escapeHtml(row.name || row.personCode || "")}">`
                    : ""}</td>
                <td class="col-org" title="${escapeHtml(row.organizationName || row.organizationCode || "")}">${escapeHtml(row.organizationName || row.organizationCode || "")}</td>
                <td class="col-code">${escapeHtml(row.personCode || "")}</td>
                <td class="col-name">${escapeHtml(row.name || "")}</td>
                <td class="col-period">${escapeHtml(row.currentSalaryStandardYearMonth || "")}</td>
                <td class="col-change">${escapeHtml(row.currentChangeType || "")}</td>
                <td class="col-period">${escapeHtml(row.calculationPeriod || "")}</td>
                <td class="col-position" title="${escapeHtml(row.positionCode || "")}">${escapeHtml(row.positionName || "")}</td>
                <td class="col-money">${money(row.currentTotal)}</td>
                <td class="col-money">${money(row.calculatedTotal)}</td>
                <td class="col-money ${Number(row.differenceAmount) === 0 ? "difference-ok" : "difference-bad"}">${money(row.differenceAmount)}</td>
                <td class="col-note">${escapeHtml(row.standardNote || "")}${row.midChainApply ? "【中段】" : ""}${row.laterPeriodSuccessorCount ? `（后继${row.laterPeriodSuccessorCount}条）` : ""}</td>
                <td class="col-action">${renderBasicSalaryStandardAdjustmentActions(row)}</td>
            </tr>`;
    }).join("");
    bindBasicSalaryStandardAdjustmentActions(rows);
}

function basicSalaryStandardAdjustmentApplyPayload(row) {
    if (!row || !row.payrollHistoryId || row.calculatedTotal == null) {
        return null;
    }
    return {
        payrollHistoryId: row.payrollHistoryId,
        organizationCode: row.organizationCode || "",
        personCode: row.personCode || "",
        calculatedPositionSalary: row.calculatedPositionSalary,
        calculatedGradeSalary: row.calculatedGradeSalary,
        calculatedTechnicalGradeSalary: row.calculatedTechnicalGradeSalary,
        calculatedPerformanceAllowance: row.calculatedPerformanceAllowance,
        calculatedSubsidyAllowance: row.calculatedSubsidyAllowance,
        calculatedFloatingSalary: row.calculatedFloatingSalary,
        calculatedTotal: row.calculatedTotal,
    };
}

function basicSalaryStandardAdjustmentLaterPeriodMode() {
    return document.getElementById("basic-salary-standard-adjustment-later-period-mode")?.value?.trim() || "block";
}

function basicSalaryStandardAdjustmentApplyParams(target) {
    const params = new URLSearchParams({ targetStandardYearMonth: target });
    const laterPeriodMode = basicSalaryStandardAdjustmentLaterPeriodMode();
    if (laterPeriodMode) {
        params.set("laterPeriodMode", laterPeriodMode);
    }
    return params;
}

function confirmBasicSalaryStandardAdjustmentApply(rows, actionLabel) {
    const midChainRows = (rows || []).filter(row => row?.midChainApply);
    if (!midChainRows.length) {
        return confirm(`${actionLabel}？系统会为每人新增一条当前工资变动记录，并将原当前记录转为历史记录。`);
    }
    const successorTotal = midChainRows.reduce((sum, row) => sum + Number(row.laterPeriodSuccessorCount || 0), 0);
    return confirm(
        `${actionLabel}？其中 ${midChainRows.length} 人为后变动人员：`
        + `将在目标年月中段插入调标晋升，并重算后继 ${successorTotal} 条记录（仅改 tbnd 与工资数额）。`
        + " 办理后可导出后继重算清单。");
}

function accumulateBasicSalaryMidChainExports(exports) {
    const incoming = (exports || []).filter(Boolean);
    if (!incoming.length) {
        return;
    }
    state.basicSalaryStandardAdjustmentMidChainExports = [
        ...(state.basicSalaryStandardAdjustmentMidChainExports || []),
        ...incoming,
    ];
}

function downloadBasicSalaryMidChainExportsCsv(exports, filename) {
    const rows = exports || [];
    const lines = ["单位编码,单位名称,人员编码,姓名,调标目标,调标记录ID,后继变动年月,后继变动类别,原tbnd,新tbnd,原合计,新合计,差额"];
    for (const item of rows) {
        for (const successor of item.successors || []) {
            lines.push([
                item.organizationCode || "",
                item.organizationName || "",
                item.personCode || "",
                item.name || "",
                item.targetStandardYearMonth || "",
                item.adjustmentHistoryId || "",
                successor.period || "",
                successor.changeType || "",
                successor.oldSalaryStandardYearMonth || "",
                successor.newSalaryStandardYearMonth || "",
                successor.oldTotal ?? "",
                successor.newTotal ?? "",
                successor.difference ?? "",
            ].map(value => {
                const text = String(value).replace(/"/g, '""');
                return /[",\n]/.test(text) ? `"${text}"` : text;
            }).join(","));
        }
    }
    const blob = new Blob(["\ufeff" + lines.join("\n")], { type: "text/csv;charset=utf-8" });
    const url = window.URL.createObjectURL(blob);
    const anchor = document.createElement("a");
    anchor.href = url;
    anchor.download = filename || `basic-salary-mid-chain-${new Date().toISOString().slice(0, 10)}.csv`;
    document.body.appendChild(anchor);
    anchor.click();
    window.URL.revokeObjectURL(url);
    document.body.removeChild(anchor);
}

async function exportBasicSalaryStandardAdjustmentMidChain() {
    const target = document.getElementById("basic-salary-standard-adjustment-target")?.value?.trim() || "";
    const status = document.getElementById("basic-salary-standard-adjustment-status");
    if (!target) {
        status.className = "status error";
        status.textContent = "请选择调标标准年月。";
        return;
    }
    const accumulated = state.basicSalaryStandardAdjustmentMidChainExports || [];
    if (accumulated.length) {
        downloadBasicSalaryMidChainExportsCsv(accumulated, `basic-salary-mid-chain-session-${target}.csv`);
        status.className = "status";
        status.textContent = `已下载本次会话中段重算清单（${accumulated.length} 人）。`;
        return;
    }
    const organizationCode = selectedOrganizationCode("basic-salary-standard-adjustment-organization-code");
    const keyword = document.getElementById("basic-salary-standard-adjustment-keyword")?.value?.trim() || "";
    status.className = "status";
    status.textContent = "正在导出后变动重算清单...";
    try {
        const params = new URLSearchParams({ targetStandardYearMonth: target });
        if (organizationCode) {
            params.set("organizationCode", organizationCode);
        }
        if (keyword) {
            params.set("keyword", keyword);
        }
        const response = await fetch(`/api/payroll/basic-salary-standard-adjustments/mid-chain-export?${params}`, {
            headers: { Accept: "text/csv" },
        });
        if (!response.ok) {
            throw new Error(`HTTP ${response.status}`);
        }
        const text = await response.text();
        const dataLines = text.split(/\r?\n/).filter((line, index) => index > 0 && line.trim());
        if (!dataLines.length) {
            status.className = "status";
            status.textContent = "无可导出的后变动重算记录。请先将「后变动处理」设为「中段插入重算」并查询试算，或办理中段调标后再导出。";
            return;
        }
        const blob = new Blob(["\ufeff" + text], { type: "text/csv;charset=utf-8" });
        const url = window.URL.createObjectURL(blob);
        const anchor = document.createElement("a");
        anchor.href = url;
        anchor.download = `basic-salary-mid-chain-export-${target}.csv`;
        document.body.appendChild(anchor);
        anchor.click();
        window.URL.revokeObjectURL(url);
        document.body.removeChild(anchor);
        status.textContent = `后变动重算清单导出完成（${dataLines.length} 条后继变动）。`;
    } catch (error) {
        showError(status, error);
    }
}

function markBasicSalaryStandardAdjustmentsProcessedLocally(successItems, target) {
    const items = (successItems || []).filter(item => item && (item.previousPayrollHistoryId || item.payrollHistoryId));
    if (!items.length) {
        return;
    }
    const nextById = { ...(state.basicSalaryStandardAdjustmentRowsById || {}) };
    for (const item of items) {
        const previousId = item.previousPayrollHistoryId || item.payrollHistoryId;
        const newId = item.payrollHistoryId || previousId;
        const row = nextById[previousId];
        if (!row) {
            continue;
        }
        delete nextById[previousId];
        nextById[newId] = {
            ...row,
            payrollHistoryId: newId,
            calculationPeriod: target || row.targetStandardYearMonth || row.calculationPeriod,
            currentChangeType: item.changeType || "调标晋升",
            // current* 保持变动前；calculated* 保持试算后，便于明细对照
            currentSalaryStandardYearMonth: row.currentSalaryStandardYearMonth,
            targetStandardYearMonth: target || row.targetStandardYearMonth,
            differenceAmount: row.differenceAmount,
            standardNote: `已办理调标晋升，可还原。工资标准年月由 ${row.currentSalaryStandardYearMonth || "-"} 调整为 ${target || row.targetStandardYearMonth || "-"}。`,
            applyEligible: false,
            rollbackEligible: true,
        };
    }
    state.basicSalaryStandardAdjustmentRowsById = nextById;
    let visible = Object.values(nextById);
    const includeApply = document.getElementById("basic-salary-standard-adjustment-include-apply")?.checked ?? true;
    const includeProcessed = document.getElementById("basic-salary-standard-adjustment-include-processed")?.checked ?? true;
    if (!includeApply) {
        visible = visible.filter(row => row.rollbackEligible);
    }
    if (!includeProcessed) {
        visible = visible.filter(row => !row.rollbackEligible);
    }
    renderBasicSalaryStandardAdjustmentRows(visible);
}

async function applySelectedBasicSalaryStandardAdjustments() {
    if (!ensurePayrollFeatureWrite("BASIC_SALARY_STANDARD_ADJUSTMENT_WRITE", "调整基本工资标准")) {
        return;
    }
    const target = document.getElementById("basic-salary-standard-adjustment-target")?.value?.trim() || "";
    const status = document.getElementById("basic-salary-standard-adjustment-status");
    if (!target) {
        status.className = "status error";
        status.textContent = "请选择调标标准年月。";
        return;
    }
    const selectedIds = Array.from(document.querySelectorAll("[data-basic-adj-select]:checked"))
        .filter(checkbox => checkbox.dataset.basicAdjAction === "apply")
        .map(checkbox => checkbox.dataset.basicAdjSelect)
        .filter(Boolean);
    if (!selectedIds.length) {
        status.className = "status error";
        status.textContent = "请先勾选需要办理的工资调标记录。";
        return;
    }
    if (!confirmBasicSalaryStandardAdjustmentApply(
            selectedIds.map(id => state.basicSalaryStandardAdjustmentRowsById?.[id]).filter(Boolean),
            `确认批量办理 ${selectedIds.length} 条工资调标`)) {
        return;
    }
    const items = [];
    for (const id of selectedIds) {
        const payload = basicSalaryStandardAdjustmentApplyPayload(state.basicSalaryStandardAdjustmentRowsById?.[id]);
        if (!payload) {
            status.className = "status error";
            status.textContent = "部分勾选记录缺少试算结果，请重新查询后再办理。";
            return;
        }
        items.push(payload);
    }
    status.className = "status";
    status.textContent = `正在批量处理 ${items.length} 条...`;
    const writeStarted = performance.now();
    try {
        const params = basicSalaryStandardAdjustmentApplyParams(target);
        const result = await postJson(
            `/api/payroll/basic-salary-standard-adjustments/batch-apply?${params}`,
            { items });
        accumulateBasicSalaryMidChainExports(result?.midChainExports);
        const writeMs = Math.round(performance.now() - writeStarted);
        const uiStarted = performance.now();
        const successItems = (result?.successes && result.successes.length)
            ? result.successes
            : (result?.successIds || []).map(id => ({
                previousPayrollHistoryId: id,
                payrollHistoryId: id,
            }));
        markBasicSalaryStandardAdjustmentsProcessedLocally(successItems, target);
        const uiMs = Math.round(performance.now() - uiStarted);
        status.className = (result?.failureCount || 0) ? "status error" : "status";
        const baseMessage = result?.message
            || `批量处理完成：成功 ${result?.successCount ?? 0} 条，失败 ${result?.failureCount ?? 0} 条。`;
        const exportHint = (result?.midChainExports || []).length
            ? ` 本次中段重算 ${result.midChainExports.length} 人，可点击「导出后变动重算清单」下载。`
            : "";
        status.textContent = `${baseMessage}${exportHint}（写库 ${writeMs}ms，界面更新 ${uiMs}ms），正在刷新列表...`;
        console.info("[basic-salary-standard-batch]", {
            writeMs,
            uiMs,
            successCount: result?.successCount,
            failureCount: result?.failureCount,
        });
        await loadBasicSalaryStandardAdjustments();
    } catch (error) {
        showError(status, error);
    }
}

async function rollbackSelectedBasicSalaryStandardAdjustments() {
    if (!ensurePayrollFeatureWrite("BASIC_SALARY_STANDARD_ADJUSTMENT_WRITE", "调整基本工资标准")) {
        return;
    }
    const status = document.getElementById("basic-salary-standard-adjustment-status");
    const selectedIds = Array.from(document.querySelectorAll("[data-basic-adj-select]:checked"))
        .filter(checkbox => checkbox.dataset.basicAdjAction === "rollback")
        .map(checkbox => checkbox.dataset.basicAdjSelect)
        .filter(Boolean);
    if (!selectedIds.length) {
        status.className = "status error";
        status.textContent = "请先勾选需要还原的工资调标记录。";
        return;
    }
    if (!confirm(`确认批量还原 ${selectedIds.length} 条工资调标？系统会删除对应链头记录，并恢复上一条工资记录为当前执行工资。`)) {
        return;
    }
    const items = [];
    for (const id of selectedIds) {
        const row = state.basicSalaryStandardAdjustmentRowsById?.[id];
        if (!row?.payrollHistoryId) {
            status.className = "status error";
            status.textContent = "部分勾选记录缺少标识，请重新查询后再还原。";
            return;
        }
        items.push({
            payrollHistoryId: row.payrollHistoryId,
            organizationCode: row.organizationCode || "",
            personCode: row.personCode || "",
        });
    }
    status.className = "status";
    status.textContent = `正在批量还原 ${items.length} 条...`;
    const writeStarted = performance.now();
    try {
        const result = await postJson("/api/payroll/basic-salary-standard-adjustments/batch-rollback", { items });
        const writeMs = Math.round(performance.now() - writeStarted);
        status.className = (result?.failureCount || 0) ? "status error" : "status";
        const baseMessage = result?.message
            || `批量还原完成：成功 ${result?.successCount ?? 0} 条，失败 ${result?.failureCount ?? 0} 条。`;
        status.textContent = `${baseMessage}（写库 ${writeMs}ms），正在刷新试算...`;
        console.info("[basic-salary-standard-batch-rollback]", {
            writeMs,
            successCount: result?.successCount,
            failureCount: result?.failureCount,
        });
        await loadBasicSalaryStandardAdjustments();
    } catch (error) {
        showError(status, error);
    }
}

async function postBasicSalaryStandardAdjustmentChunks(url, items, status, progressLabel) {
    const chunkSize = 200;
    let successCount = 0;
    let failureCount = 0;
    const failures = [];
    const midChainExports = [];
    for (let from = 0; from < items.length; from += chunkSize) {
        const chunk = items.slice(from, Math.min(from + chunkSize, items.length));
        if (status) {
            status.textContent = `${progressLabel} ${Math.min(from + chunk.length, items.length)} / ${items.length}...`;
        }
        const result = await postJson(url, { items: chunk });
        successCount += Number(result?.successCount || 0);
        failureCount += Number(result?.failureCount || 0);
        if (Array.isArray(result?.failures) && result.failures.length) {
            failures.push(...result.failures);
        }
        if (Array.isArray(result?.midChainExports) && result.midChainExports.length) {
            midChainExports.push(...result.midChainExports);
        }
    }
    accumulateBasicSalaryMidChainExports(midChainExports);
    return { successCount, failureCount, failures, midChainExports };
}

async function applyAllEligibleBasicSalaryStandardAdjustments() {
    if (!ensurePayrollFeatureWrite("BASIC_SALARY_STANDARD_ADJUSTMENT_WRITE", "调整基本工资标准")) {
        return;
    }
    const target = document.getElementById("basic-salary-standard-adjustment-target")?.value?.trim() || "";
    const status = document.getElementById("basic-salary-standard-adjustment-status");
    if (!target) {
        status.className = "status error";
        status.textContent = "请选择调标标准年月。";
        return;
    }
    const organizationCode = selectedOrganizationCode("basic-salary-standard-adjustment-organization-code");
    const keyword = document.getElementById("basic-salary-standard-adjustment-keyword")?.value?.trim() || "";
    status.className = "status";
    status.textContent = "正在获取全部待办理人员...";

    let items = [];
    let eligibleRows = [];
    try {
        const baseParams = {
            targetStandardYearMonth: target,
            includeProcessed: "false",
            laterPeriodMode: basicSalaryStandardAdjustmentLaterPeriodMode(),
        };
        if (organizationCode) {
            baseParams.organizationCode = organizationCode;
        }
        if (keyword) {
            baseParams.keyword = keyword;
        }
        const rows = await collectAllPromotionRows(
            "/api/payroll/basic-salary-standard-adjustments",
            baseParams,
            status,
            "正在获取待办理人员");
        eligibleRows = rows.filter(row => row.applyEligible);
        items = eligibleRows
            .map(row => basicSalaryStandardAdjustmentApplyPayload(row))
            .filter(Boolean);
    } catch (error) {
        showError(status, error);
        return;
    }

    if (!items.length) {
        status.className = "status";
        status.textContent = "没有可办理的调标晋升记录。";
        return;
    }
    const scopeText = organizationCode ? `单位 ${organizationCode}` : "全部可见单位";
    if (!confirmBasicSalaryStandardAdjustmentApply(
            eligibleRows,
            `确认办理${scopeText} 目标标准 ${target} 的全部待办理 ${items.length} 人`)) {
        status.className = "status";
        status.textContent = "已取消批量处理全部。";
        return;
    }

    status.className = "status";
    status.textContent = `正在批量处理全部 ${items.length} 条...`;
    const writeStarted = performance.now();
    try {
        const params = basicSalaryStandardAdjustmentApplyParams(target);
        const result = await postBasicSalaryStandardAdjustmentChunks(
            `/api/payroll/basic-salary-standard-adjustments/batch-apply?${params}`,
            items,
            status,
            "正在批量处理全部");
        const writeMs = Math.round(performance.now() - writeStarted);
        status.className = result.failureCount ? "status error" : "status";
        const exportHint = (result.midChainExports || []).length
            ? ` 本次中段重算 ${result.midChainExports.length} 人，可点击「导出后变动重算清单」下载。`
            : "";
        status.textContent = `批量处理全部完成：成功 ${result.successCount} 条，失败 ${result.failureCount} 条。${exportHint}（写库 ${writeMs}ms），正在刷新列表...`;
        console.info("[basic-salary-standard-apply-all]", {
            writeMs,
            successCount: result.successCount,
            failureCount: result.failureCount,
        });
        state.basicSalaryStandardAdjustmentPage = 0;
        await loadBasicSalaryStandardAdjustments();
    } catch (error) {
        showError(status, error);
    }
}

async function rollbackAllProcessedBasicSalaryStandardAdjustments() {
    if (!ensurePayrollFeatureWrite("BASIC_SALARY_STANDARD_ADJUSTMENT_WRITE", "调整基本工资标准")) {
        return;
    }
    const target = document.getElementById("basic-salary-standard-adjustment-target")?.value?.trim() || "";
    const status = document.getElementById("basic-salary-standard-adjustment-status");
    if (!target) {
        status.className = "status error";
        status.textContent = "请选择调标标准年月。";
        return;
    }
    const organizationCode = selectedOrganizationCode("basic-salary-standard-adjustment-organization-code");
    const keyword = document.getElementById("basic-salary-standard-adjustment-keyword")?.value?.trim() || "";
    status.className = "status";
    status.textContent = "正在获取全部已办理人员...";

    let items = [];
    try {
        const baseParams = {
            targetStandardYearMonth: target,
            includeApply: "false",
        };
        if (organizationCode) {
            baseParams.organizationCode = organizationCode;
        }
        if (keyword) {
            baseParams.keyword = keyword;
        }
        const rows = await collectAllPromotionRows(
            "/api/payroll/basic-salary-standard-adjustments",
            baseParams,
            status,
            "正在获取已办理人员");
        items = rows
            .filter(row => row.rollbackEligible && row.payrollHistoryId)
            .map(row => ({
                payrollHistoryId: row.payrollHistoryId,
                organizationCode: row.organizationCode || "",
                personCode: row.personCode || "",
            }));
    } catch (error) {
        showError(status, error);
        return;
    }

    if (!items.length) {
        status.className = "status";
        status.textContent = "没有可还原的调标晋升记录。";
        return;
    }
    const scopeText = organizationCode ? `单位 ${organizationCode}` : "全部可见单位";
    if (!confirm(`确认还原${scopeText} 目标标准 ${target} 的全部已办理 ${items.length} 人？系统会删除对应链头记录并恢复上一条工资记录。`)) {
        status.className = "status";
        status.textContent = "已取消批量还原全部。";
        return;
    }

    status.className = "status";
    status.textContent = `正在批量还原全部 ${items.length} 条...`;
    const writeStarted = performance.now();
    try {
        const result = await postBasicSalaryStandardAdjustmentChunks(
            "/api/payroll/basic-salary-standard-adjustments/batch-rollback",
            items,
            status,
            "正在批量还原全部");
        const writeMs = Math.round(performance.now() - writeStarted);
        status.className = result.failureCount ? "status error" : "status";
        status.textContent = `批量还原全部完成：成功 ${result.successCount} 条，失败 ${result.failureCount} 条。（写库 ${writeMs}ms），正在刷新列表...`;
        console.info("[basic-salary-standard-rollback-all]", {
            writeMs,
            successCount: result.successCount,
            failureCount: result.failureCount,
        });
        state.basicSalaryStandardAdjustmentPage = 0;
        await loadBasicSalaryStandardAdjustments();
    } catch (error) {
        showError(status, error);
    }
}

function renderBasicSalaryStandardAdjustmentActions(row) {
    const canWrite = hasPayrollFeatureWrite("BASIC_SALARY_STANDARD_ADJUSTMENT_WRITE");
    const canApply = Boolean(row.applyEligible);
    const canRollback = Boolean(row.rollbackEligible);
    const parts = [
        `<button class="row-action" type="button" data-basic-adj-detail="${escapeHtml(row.payrollHistoryId)}">明细</button>`,
    ];
    if (canWrite && canApply) {
        parts.push(`<button class="row-action" type="button" data-basic-adj-apply="${escapeHtml(row.payrollHistoryId)}">处理</button>`);
    }
    if (canWrite && canRollback) {
        parts.push(`<button class="row-action danger-button" type="button" data-basic-adj-rollback="${escapeHtml(row.payrollHistoryId)}">还原</button>`);
    }
    return parts.join(" ");
}

function bindBasicSalaryStandardAdjustmentActions(container) {
    container.querySelectorAll("button[data-basic-adj-detail]").forEach(button => {
        button.addEventListener("click", () => openBasicSalaryStandardAdjustmentDetailModal(button.dataset.basicAdjDetail));
    });
    container.querySelectorAll("button[data-basic-adj-apply]").forEach(button => {
        button.addEventListener("click", () => applyBasicSalaryStandardAdjustment(button.dataset.basicAdjApply));
    });
    container.querySelectorAll("button[data-basic-adj-rollback]").forEach(button => {
        button.addEventListener("click", () => rollbackBasicSalaryStandardAdjustment(button.dataset.basicAdjRollback));
    });
}

function closeBasicSalaryStandardAdjustmentDetailModal() {
    document.getElementById("basic-salary-standard-adjustment-detail-modal")?.classList.add("hidden");
}

function renderBasicSalaryStandardAdjustmentDetailContent(row) {
    const statusText = row.rollbackEligible
        ? "（已处理，可还原）"
        : row.applyEligible
            ? "（待处理）"
            : "";
    document.getElementById("basic-salary-standard-adjustment-detail-summary").textContent =
        `${row.organizationName || row.organizationCode || ""}-${row.personCode || ""} ${row.name || ""}`
        + ` / ${row.currentChangeType || "调标晋升"}${statusText}`;

    const positionText = `${row.positionCode || ""} ${row.positionName || ""}`.trim() || "-";
    const gradeLabel = row.gradeSalaryLabel || "级别/薪级工资";
    const performanceCaption = row.performanceAllowanceCaption || "生活性补贴";
    const subsidyCaption = row.subsidyAllowanceCaption || "工作性津贴";
    const showSubsidy = row.showSubsidyAllowance !== false && Boolean(subsidyCaption);
    const showTechnical = Number(row.currentTechnicalGradeSalary || 0) !== 0
        || Number(row.calculatedTechnicalGradeSalary || 0) !== 0;
    const showFloating = Number(row.currentFloatingSalary || 0) !== 0
        || Number(row.calculatedFloatingSalary || 0) !== 0;

    const metaRows = [
        `<tr><td>岗位</td><td colspan="3">${escapeHtml(positionText)}</td></tr>`,
        `<tr><td>变动年月</td><td colspan="3">${escapeHtml(row.calculationPeriod || "-")}</td></tr>`,
        `<tr><td>当前变动类别</td><td colspan="3">${escapeHtml(row.currentChangeType || "-")}</td></tr>`,
        positionChangeDetailCompareRow(
            "工资标准年月",
            row.currentSalaryStandardYearMonth || "-",
            row.targetStandardYearMonth || "-"),
        positionChangeDetailCompareRow(
            "津补贴标准年月",
            row.currentAllowanceStandardYearMonth || "-",
            row.targetAllowanceStandardYearMonth || row.currentAllowanceStandardYearMonth || "-"),
        (row.salaryComponents || []).length
            ? renderSalaryComponentCompareRows(row.salaryComponents)
            : [
                positionChangeDetailMoneyRow("职务工资", row.currentPositionSalary, row.calculatedPositionSalary, null),
                positionChangeDetailMoneyRow(gradeLabel, row.currentGradeSalary, row.calculatedGradeSalary, null),
                showTechnical
                    ? positionChangeDetailMoneyRow(
                        "技术等级工资",
                        row.currentTechnicalGradeSalary,
                        row.calculatedTechnicalGradeSalary,
                        null)
                    : "",
                positionChangeDetailMoneyRow(
                    performanceCaption,
                    row.currentPerformanceAllowance,
                    row.calculatedPerformanceAllowance,
                    null),
                showSubsidy
                    ? positionChangeDetailMoneyRow(
                        subsidyCaption,
                        row.currentSubsidyAllowance,
                        row.calculatedSubsidyAllowance,
                        null)
                    : "",
                showFloating
                    ? positionChangeDetailMoneyRow(
                        "浮动工资",
                        row.currentFloatingSalary,
                        row.calculatedFloatingSalary,
                        null)
                    : "",
                positionChangeDetailMoneyRow("合计", row.currentTotal, row.calculatedTotal, row.differenceAmount),
            ].filter(Boolean).join(""),
    ].filter(Boolean).join("");

    const processRows = row.standardNote
        ? `<tr><th>说明</th><td>${escapeHtml(row.standardNote)}</td></tr>`
        : `<tr><th>-</th><td>暂无试算说明</td></tr>`;

    document.getElementById("basic-salary-standard-adjustment-detail-content").innerHTML = `
        <div class="detail-table-panel">
            <h4>变动前后对照</h4>
            <table class="approval-component-table">
                <thead>
                    <tr>
                        <th class="approval-item-header">项目</th>
                        <th>变动前</th>
                        <th>变动后</th>
                        <th>差额/备注</th>
                    </tr>
                </thead>
                <tbody>${metaRows}</tbody>
            </table>
            <h4>试算说明</h4>
            <table class="approval-meta-table position-change-detail-steps">
                <thead>
                    <tr>
                        <th>项目</th>
                        <th>说明</th>
                    </tr>
                </thead>
                <tbody>${processRows}</tbody>
            </table>
        </div>
    `;
}

function openBasicSalaryStandardAdjustmentDetailModal(payrollHistoryId) {
    const row = state.basicSalaryStandardAdjustmentRowsById?.[payrollHistoryId];
    const modal = document.getElementById("basic-salary-standard-adjustment-detail-modal");
    if (!row || !modal) {
        return;
    }
    renderBasicSalaryStandardAdjustmentDetailContent(row);
    modal.classList.remove("hidden");
}

async function applyBasicSalaryStandardAdjustment(payrollHistoryId) {
    if (!ensurePayrollFeatureWrite("BASIC_SALARY_STANDARD_ADJUSTMENT_WRITE", "调整基本工资标准")) {
        return;
    }
    const target = document.getElementById("basic-salary-standard-adjustment-target")?.value?.trim() || "";
    const status = document.getElementById("basic-salary-standard-adjustment-status");
    if (!target) {
        status.className = "status error";
        status.textContent = "请选择调标标准年月。";
        return;
    }
    const row = state.basicSalaryStandardAdjustmentRowsById?.[payrollHistoryId];
    if (!confirmBasicSalaryStandardAdjustmentApply(
            row ? [row] : [],
            "确认按当前试算结果办理工资调标")) {
        return;
    }
    const payload = basicSalaryStandardAdjustmentApplyPayload(row);
    status.className = "status";
    status.textContent = "正在办理工资调标...";
    const writeStarted = performance.now();
    try {
        const params = basicSalaryStandardAdjustmentApplyParams(target);
        const result = await postJson(
            `/api/payroll/basic-salary-standard-adjustments/${encodeURIComponent(payrollHistoryId)}/apply?${params}`,
            payload || {});
        accumulateBasicSalaryMidChainExports(result?.midChainExport ? [result.midChainExport] : []);
        const writeMs = Math.round(performance.now() - writeStarted);
        markBasicSalaryStandardAdjustmentsProcessedLocally([{
            previousPayrollHistoryId: payrollHistoryId,
            payrollHistoryId: result?.payrollHistoryId || payrollHistoryId,
            changeType: result?.changeType || "调标晋升",
        }], target);
        status.textContent = `${(result && result.message) || "调标晋升处理完成"}${result?.midChainExport ? " 可导出后继重算清单。" : ""}（写库 ${writeMs}ms），正在刷新列表...`;
        await loadBasicSalaryStandardAdjustments();
    } catch (error) {
        showError(status, error);
    }
}

async function rollbackBasicSalaryStandardAdjustment(payrollHistoryId) {
    if (!ensurePayrollFeatureWrite("BASIC_SALARY_STANDARD_ADJUSTMENT_WRITE", "调整基本工资标准")) {
        return;
    }
    const status = document.getElementById("basic-salary-standard-adjustment-status");
    if (!confirm("确认还原当前工资调标？系统会删除当前链头记录，并恢复上一条工资记录为当前执行工资。")) {
        return;
    }
    const row = state.basicSalaryStandardAdjustmentRowsById?.[payrollHistoryId];
    const payload = row ? {
        payrollHistoryId: row.payrollHistoryId,
        organizationCode: row.organizationCode || "",
        personCode: row.personCode || "",
    } : {};
    status.className = "status";
    status.textContent = "正在还原工资调标...";
    try {
        const result = await postJson(
            `/api/payroll/basic-salary-standard-adjustments/${encodeURIComponent(payrollHistoryId)}/rollback`,
            payload);
        status.textContent = (result && result.message) || "工资调标已还原";
        await loadBasicSalaryStandardAdjustments();
    } catch (error) {
        showError(status, error);
    }
}

async function refreshBasicStandardPositionCategories() {
    const select = document.getElementById("basic-standard-position-category");
    const typeEl = document.getElementById("basic-standard-type");
    const yearEl = document.getElementById("basic-standard-year-month");
    if (!select || !typeEl || !yearEl) {
        return;
    }
    const standardType = typeEl.value;
    const standardYearMonth = yearEl.value.trim();
    const previous = select.value;
    if (!basicStandardUsesPositionCategory(standardType) || !standardYearMonth) {
        select.innerHTML = `<option value="">全部</option>`;
        return;
    }
    try {
        const params = new URLSearchParams({ standardType, standardYearMonth });
        const categories = await getJson(`/api/payroll/basic-standards/position-categories?${params}`);
        const options = [`<option value="">全部</option>`]
            .concat((categories || []).map(item => {
                const code = item.positionCode || "";
                const name = item.name || "";
                const label = name && name !== code ? `${code} ${name}` : code;
                return `<option value="${escapeHtml(code)}">${escapeHtml(label)}</option>`;
            }));
        select.innerHTML = options.join("");
        if (previous && (categories || []).some(item => item.positionCode === previous)) {
            select.value = previous;
        }
    } catch (error) {
        console.warn("加载基本工资职务岗位类别失败", error);
        select.innerHTML = `<option value="">全部</option>`;
    }
}

async function loadBasicStandards(options = {}) {
    const standardType = document.getElementById("basic-standard-type").value;
    const standardYearMonth = document.getElementById("basic-standard-year-month").value.trim();
    const positionPrefix = document.getElementById("basic-standard-position-category")?.value.trim() || "";
    const status = document.getElementById("basic-standards-status");
    status.className = "status";
    status.textContent = "正在查询工资标准...";
    document.getElementById("basic-standards-head").innerHTML = "";
    document.getElementById("basic-standards-rows").innerHTML = "";
    if (!standardYearMonth) {
        status.textContent = "请选择标准年月";
        updateBasicStandardCreateButton();
        return;
    }
    const params = new URLSearchParams({ standardType, standardYearMonth });
    if (basicStandardUsesPositionCategory(standardType) && positionPrefix) {
        params.set("positionPrefix", positionPrefix);
    }
    try {
        const result = await getJson(`/api/payroll/basic-standards?${params}`);
        const list = result || [];
        renderBasicStandards(list, standardType);
        updateBasicStandardCreateButton();
        if (options.statusMessage) {
            showSuccess(status, options.statusMessage);
        } else {
            status.textContent = `共 ${list.length} 条`;
        }
    } catch (error) {
        showError(status, error);
    }
}

function renderBasicStandards(records, standardType) {
    const head = document.getElementById("basic-standards-head");
    const body = document.getElementById("basic-standards-rows");
    if (!records.length) {
        head.innerHTML = "<tr><th>结果</th></tr>";
        body.innerHTML = "<tr><td>没有查询到标准数据</td></tr>";
        return;
    }
    // gwflmc 合并进岗位分类单元格，不单独成列
    const displayColumns = Object.keys(records[0].values).filter(column =>
        column.toLowerCase() !== "gwflmc");
    const actionHeader = basicStandardMaintainable(standardType) ? `<th class="standard-write-col">操作</th>` : "";
    head.innerHTML = `<tr>${displayColumns.map(column => `<th>${escapeHtml(basicStandardColumnLabel(column))}</th>`).join("")}${actionHeader}</tr>`;
    body.innerHTML = records.map((record, rowIndex) => {
        const values = record.values;
        const cells = displayColumns.map(column => {
            if (column.toLowerCase() === "gwflbm") {
                return `<td>${escapeHtml(basicStandardJobCategoryLabel(values))}</td>`;
            }
            return `<td>${escapeHtml(values[column] ?? "")}</td>`;
        }).join("");
        const actionCell = renderBasicStandardActionCell(standardType, values);
        return `<tr data-row-index="${rowIndex}">${cells}${actionCell}</tr>`;
    }).join("");
    bindBasicStandardActions(body, standardType, records);
    updateStandardWriteUi();
}

function basicStandardJobCategoryLabel(values) {
    const code = String(values.gwflbm ?? values.GWFLBM ?? "").trim();
    const name = String(values.gwflmc ?? values.GWFLMC ?? "").trim();
    if (!code) {
        return name;
    }
    if (name && name !== code) {
        return `${code} ${name}`;
    }
    return code;
}

function basicStandardColumnLabel(column) {
    const labels = {
        tbnd: "标准年月",
        zwbm: "职务编码",
        zwmc: "职务名称",
        bz: "标准",
        jb: "级别",
        gwflbm: "岗位分类",
        xj: "薪级",
        jc: "级差",
        jce: "级差额",
        jsdjgz: "技术等级工资",
    };
    if (labels[column]) {
        return labels[column];
    }
    if (/^dc\d+$/i.test(column)) {
        return "档次" + column.replace(/^dc/i, "");
    }
    return column;
}

function renderBasicStandardActionCell(standardType, values) {
    if (!basicStandardMaintainable(standardType)) {
        return "";
    }
    if (standardType === "position") {
        return `<td class="standard-write-col">
            <button class="row-action" type="button"
                data-basic-action="position-edit"
                data-year="${escapeHtml(values.tbnd)}"
                data-code="${escapeHtml(values.zwbm)}"
                data-amount="${escapeHtml(values.bz ?? 0)}">编辑</button>
            <button class="row-action" type="button"
                data-basic-action="position-delete"
                data-year="${escapeHtml(values.tbnd)}"
                data-code="${escapeHtml(values.zwbm)}">删除</button>
        </td>`;
    }
    if (standardType === "grade") {
        return `<td class="standard-write-col">
            <button class="row-action" type="button"
                data-basic-action="grade-edit"
                data-year="${escapeHtml(values.tbnd)}"
                data-code="${escapeHtml(values.jb)}">编辑</button>
        </td>`;
    }
    if (standardType === "position-grade") {
        return `<td class="standard-write-col">
            <button class="row-action" type="button"
                data-basic-action="position-grade-edit"
                data-year="${escapeHtml(values.tbnd)}"
                data-code="${escapeHtml(values.zwbm)}">编辑</button>
            <button class="row-action" type="button"
                data-basic-action="position-grade-delete"
                data-year="${escapeHtml(values.tbnd)}"
                data-code="${escapeHtml(values.zwbm)}">删除</button>
        </td>`;
    }
    if (standardType === "salary-level") {
        return `<td class="standard-write-col">
            <button class="row-action" type="button"
                data-basic-action="salary-level-edit"
                data-year="${escapeHtml(values.tbnd)}"
                data-job-category="${escapeHtml(values.gwflbm)}"
                data-level="${escapeHtml(values.xj)}">编辑</button>
        </td>`;
    }
    return "";
}

function bindBasicStandardActions(body, standardType, records) {
    body.querySelectorAll("button[data-basic-action]").forEach(button => {
        button.addEventListener("click", () => {
            const action = button.dataset.basicAction;
            const rowIndex = Number(button.closest("tr")?.dataset.rowIndex);
            const values = records[rowIndex]?.values || {};
            if (action === "position-edit") {
                editPositionSalaryStandard(button.dataset.year, button.dataset.code, button.dataset.amount);
                return;
            }
            if (action === "position-delete") {
                deletePositionSalaryStandard(button.dataset.year, button.dataset.code);
                return;
            }
            if (action === "grade-edit") {
                openGradeStandardModal("edit", "grade", {
                    standardYearMonth: button.dataset.year,
                    code: button.dataset.code,
                    gradeSteps: extractGradeSteps(values),
                });
                return;
            }
            if (action === "position-grade-edit") {
                openGradeStandardModal("edit", "position-grade", {
                    standardYearMonth: button.dataset.year,
                    code: button.dataset.code,
                    technicalGradeSalary: Number(values.jsdjgz ?? 0),
                    gradeSteps: extractGradeSteps(values),
                });
                return;
            }
            if (action === "position-grade-delete") {
                deletePositionGradeSalaryStandard(button.dataset.year, button.dataset.code);
                return;
            }
            if (action === "salary-level-edit") {
                openSalaryLevelStandardModal("edit", {
                    standardYearMonth: button.dataset.year,
                    jobCategoryCode: button.dataset.jobCategory,
                    salaryLevel: button.dataset.level,
                    amount: Number(values.bz ?? 0),
                    baseAmount: Number(values.jc ?? 0),
                    baseAmountExtra: Number(values.jce ?? 0),
                });
            }
        });
    });
    void standardType;
}

async function loadAllowanceStandards() {
    const yearMonthEl = document.getElementById("allowance-standard-year-month");
    const categoryEl = document.getElementById("allowance-standard-category");
    const positionCategoryEl = document.getElementById("allowance-standard-position-category");
    const standardYearMonth = yearMonthEl ? yearMonthEl.value.trim() : "";
    const performanceCategory = categoryEl ? categoryEl.value.trim() : "";
    const positionPrefix = positionCategoryEl ? positionCategoryEl.value.trim() : "";
    const status = document.getElementById("allowance-standards-status");
    const rows = document.getElementById("allowance-standards-rows");
    if (status) {
        status.className = "status";
        status.textContent = "正在查询津贴补贴标准...";
    }
    if (rows) {
        rows.innerHTML = "";
    }
    if (!standardYearMonth) {
        if (status) {
            status.textContent = "请选择标准年月";
        }
        return;
    }
    const params = new URLSearchParams({ standardYearMonth });
    if (performanceCategory !== "") {
        params.set("performanceCategory", performanceCategory);
    }
    if (positionPrefix !== "") {
        params.set("positionPrefix", positionPrefix);
    }
    try {
        const result = await getJson(`/api/payroll/allowance-standards/by-position?${params}`);
        const list = result || [];
        const captions = updateAllowanceStandardColumnHeaders(performanceCategory, list);
        const showSdbt = performanceCategory === "1"
            || (performanceCategory === "" && list.some(row => row.sdbtId != null || row.sdbtAmount != null));
        const showCategory = performanceCategory === "";
        const tableWrap = document.querySelector("#allowance-standards .allowance-standards-table-wrap");
        if (tableWrap) {
            tableWrap.classList.toggle("allowance-hide-sdbt", !showSdbt);
            tableWrap.classList.toggle("allowance-hide-category", !showCategory);
        }
        if (rows) {
            rows.innerHTML = list.map(row => {
                const sdbtCell = showSdbt
                    ? `<td class="allowance-col-sdbt">${row.sdbtAmount == null ? "" : money(row.sdbtAmount)}</td>`
                    : "";
                const categoryCell = showCategory
                    ? `<td class="allowance-col-category">${escapeHtml(allowancePerformanceCategoryLabel(row.performanceCategory))}</td>`
                    : "";
                const sdbtActions = showSdbt && hasStandardWrite()
                    ? `<button class="row-action" type="button"
                        data-allowance-edit-sdbt="${row.sdbtId || ""}"
                        data-position-code="${escapeHtml(row.positionCode || "")}"
                        data-position-name="${escapeHtml(row.name || "")}"
                        data-performance-category="${escapeHtml(row.performanceCategory ?? "")}"
                        data-sdbt-amount="${row.sdbtAmount == null ? "" : row.sdbtAmount}">${escapeHtml(captions.editSdbt)}</button>`
                    : "";
                return `
            <tr>
                <td>${escapeHtml(row.positionCode)}</td>
                <td title="${escapeHtml(row.name || "")}">${escapeHtml(row.name)}</td>
                <td>${row.dfbt2Amount == null ? "" : money(row.dfbt2Amount)}</td>
                ${sdbtCell}
                ${categoryCell}
                ${hasStandardWrite() ? `<td class="standard-write-col">
                    <button class="row-action" type="button"
                        data-allowance-edit-dfbt2="${row.dfbt2Id || ""}"
                        data-position-code="${escapeHtml(row.positionCode || "")}"
                        data-position-name="${escapeHtml(row.name || "")}"
                        data-performance-category="${escapeHtml(row.performanceCategory ?? "")}"
                        data-dfbt2-amount="${row.dfbt2Amount == null ? "" : row.dfbt2Amount}">${escapeHtml(captions.editDfbt2)}</button>
                    ${sdbtActions}
                </td>` : ""}
            </tr>`;
            }).join("");
            rows.querySelectorAll("button[data-allowance-edit-dfbt2]").forEach(button => {
                button.addEventListener("click", () => editAllowanceStandardAmount(
                    Number(button.dataset.allowanceEditDfbt2) || null,
                    "DFBT2",
                    button.dataset.positionCode,
                    button.dataset.positionName,
                    button.dataset.performanceCategory === "" ? null : Number(button.dataset.performanceCategory),
                    button.dataset.dfbt2Amount === "" ? null : Number(button.dataset.dfbt2Amount)));
            });
            rows.querySelectorAll("button[data-allowance-edit-sdbt]").forEach(button => {
                button.addEventListener("click", () => editAllowanceStandardAmount(
                    Number(button.dataset.allowanceEditSdbt) || null,
                    "SDBT",
                    button.dataset.positionCode,
                    button.dataset.positionName,
                    button.dataset.performanceCategory === "" ? null : Number(button.dataset.performanceCategory),
                    button.dataset.sdbtAmount === "" ? null : Number(button.dataset.sdbtAmount)));
            });
        }
        updateStandardWriteUi();
        if (status) {
            status.textContent = `共 ${list.length} 个职务`;
        }
    } catch (error) {
        showError(status, error);
    }
}

async function loadRankAllowanceStandards() {
    const standardYearMonth = document.getElementById("rank-standard-year-month").value.trim();
    const category = document.getElementById("rank-standard-category").value.trim();
    const params = new URLSearchParams({ page: "0", size: "500" });
    if (standardYearMonth) {
        params.set("standardYearMonth", standardYearMonth);
    }
    if (category) {
        params.set("category", category);
    }
    const status = document.getElementById("rank-standards-status");
    const rows = document.getElementById("rank-standards-rows");
    status.className = "status";
    status.textContent = "正在查询津贴标准...";
    rows.innerHTML = "";
    try {
        const result = await getJson(`/api/payroll/rank-allowance-standards?${params}`);
        const list = result.content || [];
        rows.innerHTML = list.map(row => `
            <tr>
                <td>${escapeHtml(row.id)}</td>
                <td>${escapeHtml(row.standardYearMonth)}</td>
                <td>${escapeHtml(row.rankCode)}</td>
                <td title="${escapeHtml(row.rankName || "")}">${escapeHtml(row.rankName)}</td>
                <td>${money(row.amount)}</td>
                <td>${escapeHtml(rankAllowanceCategoryLabel(row.category))}</td>
                ${hasStandardWrite() ? `<td class="standard-write-col">
                    <button class="row-action" type="button" data-rank-edit="${row.id}">编辑</button>
                </td>` : ""}
            </tr>
        `).join("");
        rows.querySelectorAll("button[data-rank-edit]").forEach(button => {
            button.addEventListener("click", () => editRankAllowanceStandard(Number(button.dataset.rankEdit)));
        });
        updateStandardWriteUi();
        status.textContent = `共 ${result.totalElements ?? list.length} 条`;
    } catch (error) {
        showError(status, error);
    }
}

async function loadRetainedAllowanceStandards() {
    const positionPrefix = document.getElementById("retained-standard-position-category")?.value.trim() || "";
    const params = new URLSearchParams({ page: "0", size: "500" });
    if (positionPrefix) {
        params.set("positionPrefix", positionPrefix);
    }
    const status = document.getElementById("retained-standards-status");
    const rows = document.getElementById("retained-standards-rows");
    status.className = "status";
    status.textContent = "正在查询保留福补标准...";
    rows.innerHTML = "";
    try {
        const result = await getJson(`/api/payroll/retained-allowance-standards?${params}`);
        const list = result.content || [];
        rows.innerHTML = list.map(row => `
            <tr>
                <td>${escapeHtml(row.positionCode)}</td>
                <td>${escapeHtml(row.name)}</td>
                <td>${money(row.amount)}</td>
                ${hasStandardWrite() ? `<td class="standard-write-col">
                    <button class="row-action" type="button" data-retained-edit="${escapeHtml(row.positionCode)}">编辑</button>
                </td>` : ""}
            </tr>
        `).join("");
        rows.querySelectorAll("button[data-retained-edit]").forEach(button => {
            button.addEventListener("click", () => editRetainedAllowanceStandard(button.dataset.retainedEdit));
        });
        updateStandardWriteUi();
        status.textContent = `共 ${result.totalElements ?? list.length} 条`;
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
                ${hasStandardWrite() ? `<td class="standard-write-col">
                    <button class="row-action" type="button" data-year-edit="${escapeHtml(row.standardYearMonth)}">编辑</button>
                    <button class="row-action" type="button" data-year-delete="${escapeHtml(row.standardYearMonth)}">删除</button>
                </td>` : ""}
            </tr>
        `).join("");
        rows.querySelectorAll("button[data-year-edit]").forEach(button => {
            button.addEventListener("click", () => editYearAllowanceStandard(button.dataset.yearEdit));
        });
        rows.querySelectorAll("button[data-year-delete]").forEach(button => {
            button.addEventListener("click", () => deleteYearAllowanceStandard(button.dataset.yearDelete));
        });
        updateStandardWriteUi();
        status.textContent = `第 ${result.page + 1} / ${Math.max(result.totalPages, 1)} 页，共 ${result.totalElements} 条`;
    } catch (error) {
        showError(status, error);
    }
}

async function loadInternSalaryStandards(options = {}) {
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
        rows.innerHTML = (result.content || []).map((row, rowIndex) => `
            <tr data-row-index="${rowIndex}">
                <td>${escapeHtml(row.standardYearMonth)}</td>
                <td>${escapeHtml(row.educationCode)}</td>
                <td>${escapeHtml(row.educationName)}</td>
                <td>${escapeHtml(row.regularPositionCode)}</td>
                <td>${escapeHtml(row.regularPositionName)}</td>
                <td>${escapeHtml(row.regularGradeStep)}</td>
                <td>${escapeHtml(row.regularLevel)}</td>
                <td>${money(row.firstYearAmount)}</td>
                <td>${money(row.secondYearAmount)}</td>
                ${hasStandardWrite() ? `<td class="standard-write-col">
                    <button class="row-action" type="button" data-intern-edit="${rowIndex}">编辑</button>
                    <button class="row-action" type="button" data-intern-delete="${rowIndex}">删除</button>
                </td>` : ""}
            </tr>
        `).join("");
        const content = result.content || [];
        rows.querySelectorAll("button[data-intern-edit]").forEach(button => {
            button.addEventListener("click", () => {
                const row = content[Number(button.dataset.internEdit)];
                if (row) {
                    openInternSalaryStandardModal("edit", row);
                }
            });
        });
        rows.querySelectorAll("button[data-intern-delete]").forEach(button => {
            button.addEventListener("click", () => {
                const row = content[Number(button.dataset.internDelete)];
                if (row) {
                    deleteInternSalaryStandard(row.standardYearMonth, row.educationCode, row.regularPositionCode);
                }
            });
        });
        updateStandardWriteUi();
        if (options.statusMessage) {
            showSuccess(status, options.statusMessage);
        } else {
            status.textContent = `第 ${result.page + 1} / ${Math.max(result.totalPages, 1)} 页，共 ${result.totalElements} 条`;
        }
    } catch (error) {
        showError(status, error);
    }
}

async function loadWageReformStandards(options = {}) {
    const positionPrefix = document.getElementById("wage-reform-position-category")?.value.trim() || "";
    const positionCode = document.getElementById("wage-reform-position")?.value.trim() || "";
    const params = new URLSearchParams();
    if (positionCode) {
        params.set("positionCode", positionCode);
    } else if (positionPrefix) {
        params.set("positionPrefix", positionPrefix);
    }
    const status = document.getElementById("wage-reform-standards-status");
    const rows = document.getElementById("wage-reform-standards-rows");
    status.className = "status";
    status.textContent = "正在查询2006套改标准...";
    rows.innerHTML = "";
    try {
        const result = await getJson(`/api/payroll/wage-reform-standards?${params}`);
        const content = result.content || [];
        rows.innerHTML = content.map((row, rowIndex) => `
            <tr data-row-index="${rowIndex}">
                <td>${escapeHtml(row.positionCode)}</td>
                <td title="${escapeHtml(row.positionName || "")}">${escapeHtml(row.positionName || "")}</td>
                <td>${escapeHtml(row.appointmentYearsLower)}</td>
                <td>${escapeHtml(row.appointmentYearsUpper)}</td>
                <td>${escapeHtml(row.reformYearsLower)}</td>
                <td>${escapeHtml(row.reformYearsUpper)}</td>
                <td>${escapeHtml(row.convertedLevel)}</td>
                <td>${escapeHtml(row.convertedStep)}</td>
                ${hasStandardWrite() ? `<td class="standard-write-col">
                    <button class="row-action" type="button" data-wage-reform-edit="${rowIndex}">编辑</button>
                </td>` : ""}
            </tr>
        `).join("");
        rows.querySelectorAll("button[data-wage-reform-edit]").forEach(button => {
            button.addEventListener("click", () => {
                const row = content[Number(button.dataset.wageReformEdit)];
                if (row) {
                    openWageReformStandardModal("edit", row);
                }
            });
        });
        updateStandardWriteUi();
        if (options.statusMessage) {
            showSuccess(status, options.statusMessage);
        } else {
            const total = result.totalElements ?? content.length;
            status.textContent = content.length < total
                ? `共 ${total} 条，当前显示 ${content.length} 条`
                : `共 ${total} 条`;
        }
    } catch (error) {
        showError(status, error);
    }
}

async function loadOtherAllowanceStandards(options = {}) {
    const standardType = document.getElementById("other-allowance-standard-type").value;
    const standardYearMonth = document.getElementById("other-allowance-filter-year-month")?.value.trim() || "";
    const positionPrefix = document.getElementById("other-allowance-position-category")?.value.trim() || "";
    const params = new URLSearchParams({ standardType });
    if (standardType !== "civilized" && standardYearMonth) {
        params.set("standardYearMonth", standardYearMonth);
    }
    if (otherAllowanceUsesPositionCategory(standardType) && positionPrefix) {
        params.set("positionPrefix", positionPrefix);
    }
    const status = document.getElementById("other-allowance-status");
    const rows = document.getElementById("other-allowance-rows");
    status.className = "status";
    status.textContent = "正在查询其他补贴标准...";
    rows.innerHTML = "";
    try {
        const result = await getJson(`/api/payroll/other-allowance-standards?${params}`);
        const content = result.content || [];
        rows.innerHTML = content.map((row, rowIndex) => `
            <tr data-row-index="${rowIndex}">
                <td>${escapeHtml(otherAllowanceTypeName(row.standardType))}</td>
                <td>${escapeHtml(row.standardYearMonth || "")}</td>
                <td>${escapeHtml(row.code || "")}</td>
                <td title="${escapeHtml(row.name || "")}">${escapeHtml(row.name || "")}</td>
                <td>${money(row.amount)}</td>
                <td>${money(row.averageAmount)}</td>
                <td>${escapeHtml(row.multiplier ?? "")}</td>
                ${hasStandardWrite() ? `<td class="standard-write-col">
                    <button class="row-action" type="button" data-other-allowance-edit="${rowIndex}">编辑</button>
                </td>` : ""}
            </tr>
        `).join("");
        rows.querySelectorAll("button[data-other-allowance-edit]").forEach(button => {
            button.addEventListener("click", () => {
                const row = content[Number(button.dataset.otherAllowanceEdit)];
                if (row) {
                    openOtherAllowanceStandardModal("edit", row);
                }
            });
        });
        updateStandardWriteUi();
        if (options.statusMessage) {
            showSuccess(status, options.statusMessage);
        } else {
            const total = result.totalElements ?? content.length;
            status.textContent = content.length < total
                ? `共 ${total} 条，当前显示 ${content.length} 条`
                : `共 ${total} 条`;
        }
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

const SECURITY_PAGE_SIZE = 20;

function wireSecurityAdminUi() {
    document.querySelectorAll("[data-security-tab]").forEach(button => {
        button.addEventListener("click", () => {
            const tab = button.dataset.securityTab;
            if (!tab || tab === state.security.activeTab) {
                return;
            }
            switchSecurityTab(tab);
            void loadSecurityAdmin();
        });
    });

    ["security-user-filter", "security-role-filter", "security-menu-filter", "security-audit-filter"].forEach(id => {
        document.getElementById(id)?.addEventListener("input", debounceSecurityReload);
    });
    ["security-audit-from", "security-audit-to"].forEach(id => {
        document.getElementById(id)?.addEventListener("change", () => {
            state.security.auditPageIndex = 0;
            void loadSecurityAdmin();
        });
    });
    document.getElementById("security-organization-filter")?.addEventListener("input", () => {
        renderSecurityRoleOrgTree();
    });
    document.getElementById("security-user-home-org-filter")?.addEventListener("input", () => {
        renderSecurityUserHomeOrgTree();
    });
    document.getElementById("security-user-data-scope-all")?.addEventListener("change", () => {
        state.security.userAllOrganizationsSelected = true;
        syncSecurityUserDataScopePanel();
    });
    document.getElementById("security-user-data-scope-home")?.addEventListener("change", () => {
        state.security.userAllOrganizationsSelected = false;
        syncSecurityUserDataScopePanel();
    });

    document.getElementById("security-user-prev")?.addEventListener("click", () => gotoSecurityPage("users", state.security.userPageIndex - 1));
    document.getElementById("security-user-next")?.addEventListener("click", () => gotoSecurityPage("users", state.security.userPageIndex + 1));
    document.getElementById("security-role-prev")?.addEventListener("click", () => gotoSecurityPage("roles", state.security.rolePageIndex - 1));
    document.getElementById("security-role-next")?.addEventListener("click", () => gotoSecurityPage("roles", state.security.rolePageIndex + 1));
    document.getElementById("security-audit-prev")?.addEventListener("click", () => gotoSecurityPage("audit", state.security.auditPageIndex - 1));
    document.getElementById("security-audit-next")?.addEventListener("click", () => gotoSecurityPage("audit", state.security.auditPageIndex + 1));

    document.getElementById("security-create-user-button")?.addEventListener("click", () => openSecurityModal("security-create-user-modal"));
    document.getElementById("security-create-role-button")?.addEventListener("click", () => openSecurityModal("security-create-role-modal"));
    document.getElementById("security-create-menu-button")?.addEventListener("click", async () => {
        await fillSecurityMenuParentOptions("new-menu-parent");
        openSecurityModal("security-create-menu-modal");
    });
    document.getElementById("security-menu-save-order")?.addEventListener("click", saveSecurityMenuOrder);
    document.getElementById("security-audit-export")?.addEventListener("click", exportSecurityAuditCsv);
    document.getElementById("security-users-enable")?.addEventListener("click", () => batchUpdateSecurityUsersEnabled(true));
    document.getElementById("security-users-disable")?.addEventListener("click", () => batchUpdateSecurityUsersEnabled(false));
    document.getElementById("security-user-select-all")?.addEventListener("change", event => {
        document.querySelectorAll("[data-user-select]").forEach(input => {
            input.checked = event.target.checked;
        });
    });

    [
        ["security-create-user-modal", "security-create-user-modal-close", "security-create-user-cancel"],
        ["security-create-role-modal", "security-create-role-modal-close", "security-create-role-cancel"],
        ["security-create-menu-modal", "security-create-menu-modal-close", "security-create-menu-cancel"],
        ["security-user-roles-modal", "security-user-roles-modal-close", "security-user-roles-cancel"],
        ["security-user-ukey-modal", "security-user-ukey-modal-close", "security-user-ukey-cancel"],
        ["security-user-password-modal", "security-user-password-modal-close", "security-user-password-cancel"],
        ["security-role-orgs-modal", "security-role-orgs-modal-close", "security-role-orgs-cancel"],
        ["security-menu-edit-modal", "security-menu-edit-modal-close", "security-menu-edit-cancel"],
    ].forEach(([modalId, ...closeIds]) => {
        closeIds.forEach(id => {
            document.getElementById(id)?.addEventListener("click", () => closeSecurityModal(modalId));
        });
        document.getElementById(modalId)?.addEventListener("click", event => {
            if (event.target.id === modalId) {
                closeSecurityModal(modalId);
            }
        });
    });

    document.getElementById("security-user-roles-save")?.addEventListener("click", saveSecurityUserRoles);
    document.getElementById("security-user-ukey-form")?.addEventListener("submit", saveSecurityUserUkey);
    document.getElementById("security-user-password-form")?.addEventListener("submit", saveSecurityUserPassword);
    document.getElementById("security-role-orgs-save")?.addEventListener("click", saveSecurityRoleOrganizations);
    document.getElementById("security-menu-edit-form")?.addEventListener("submit", saveSecurityMenuEdit);
}

function openSecurityModal(modalId) {
    const modal = document.getElementById(modalId);
    if (!modal) {
        return;
    }
    modal.classList.remove("hidden");
}

function closeSecurityModal(modalId) {
    const modal = document.getElementById(modalId);
    if (!modal) {
        return;
    }
    modal.classList.add("hidden");
    const status = modal.querySelector(".status");
    if (status) {
        status.className = "status";
        status.textContent = "";
    }
}

function switchSecurityTab(tab) {
    state.security.activeTab = tab;
    document.querySelectorAll("[data-security-tab]").forEach(button => {
        button.classList.toggle("active", button.dataset.securityTab === tab);
    });
    ["users", "roles", "menus", "audit"].forEach(name => {
        document.getElementById(`security-tab-${name}`)?.classList.toggle("hidden", name !== tab);
    });
}

function gotoSecurityPage(tab, page) {
    const pageKey = {
        users: "userPageIndex",
        roles: "rolePageIndex",
        menus: "menuPageIndex",
        audit: "auditPageIndex",
    }[tab];
    const pageObjKey = {
        users: "userPage",
        roles: "rolePage",
        menus: "menuPage",
        audit: "auditPage",
    }[tab];
    if (!pageKey) {
        return;
    }
    const pageInfo = state.security[pageObjKey];
    const totalPages = Math.max(pageInfo?.totalPages || 1, 1);
    const target = Math.min(Math.max(page, 0), totalPages - 1);
    if (target === state.security[pageKey]) {
        return;
    }
    state.security[pageKey] = target;
    state.security.activeTab = tab;
    void loadSecurityAdmin();
}

async function loadSecurityAdmin(options = {}) {
    const status = document.getElementById("security-status");
    const tab = state.security.activeTab || "users";
    status.className = "status";
    status.textContent = "正在加载权限配置...";
    try {
        if (tab === "users") {
            const params = new URLSearchParams({
                keyword: document.getElementById("security-user-filter")?.value.trim() || "",
                page: String(state.security.userPageIndex || 0),
                size: String(SECURITY_PAGE_SIZE),
            });
            const usersPromise = getJson(`/api/security/users-page?${params}`);
            const orgTreePromise = state.security.organizationNodes?.length
                ? Promise.resolve(state.security.organizationNodes)
                : getJson("/api/organizations/tree");
            const [users, organizationNodes] = await Promise.all([usersPromise, orgTreePromise]);
            state.security.organizationNodes = organizationNodes || [];
            state.security.users = users.content || [];
            state.security.userPage = users;
            state.security.userPageIndex = users.page ?? state.security.userPageIndex;
            renderSecurityUsers();
        } else if (tab === "roles") {
            const params = new URLSearchParams({
                keyword: document.getElementById("security-role-filter")?.value.trim() || "",
                page: String(state.security.rolePageIndex || 0),
                size: String(SECURITY_PAGE_SIZE),
            });
            const [roles, permissions] = await Promise.all([
                getJson(`/api/security/roles-page?${params}`),
                state.security.permissions?.length
                    ? Promise.resolve(state.security.permissions)
                    : getJson("/api/security/permissions"),
            ]);
            state.security.roles = roles.content || [];
            state.security.rolePage = roles;
            state.security.rolePageIndex = roles.page ?? state.security.rolePageIndex;
            state.security.permissions = Array.isArray(permissions) ? permissions : state.security.permissions;
            renderSecurityRoles();
        } else if (tab === "menus") {
            const params = new URLSearchParams({
                keyword: document.getElementById("security-menu-filter")?.value.trim() || "",
            });
            const menus = await getJson(`/api/security/menus?${params}`);
            state.security.menus = menus || [];
            state.security.menuDraft = (menus || []).map(menu => ({
                id: menu.id,
                parentId: menu.parentId ?? null,
                sortOrder: menu.sortOrder ?? 0,
            }));
            state.security.menuPage = {
                page: 0,
                totalPages: 1,
                totalElements: (menus || []).length,
            };
            renderSecurityMenus();
        } else if (tab === "audit") {
            const params = new URLSearchParams({
                keyword: document.getElementById("security-audit-filter")?.value.trim() || "",
                page: String(state.security.auditPageIndex || 0),
                size: String(SECURITY_PAGE_SIZE),
            });
            const from = document.getElementById("security-audit-from")?.value;
            const to = document.getElementById("security-audit-to")?.value;
            if (from) {
                params.set("from", from);
            }
            if (to) {
                params.set("to", to);
            }
            const auditLogs = await getJson(`/api/security/audit-logs-page?${params}`);
            state.security.auditLogs = auditLogs.content || [];
            state.security.auditPage = auditLogs;
            state.security.auditPageIndex = auditLogs.page ?? state.security.auditPageIndex;
            renderSecurityAudit();
        }
        if (options.statusMessage) {
            showSuccess(status, options.statusMessage);
        } else {
            status.textContent = "权限配置已加载";
        }
    } catch (error) {
        showError(status, error);
    }
}

async function runSecuritySave(successMessage, action) {
    const status = document.getElementById("security-status");
    try {
        status.className = "status";
        status.textContent = "正在保存...";
        await action();
        await loadSecurityAdmin({ statusMessage: successMessage });
    } catch (error) {
        showError(status, error);
    }
}

function securityRoleSummary(user) {
    const codes = user.roleCodes || [];
    if (!codes.length) {
        return "未分配";
    }
    return codes.join(", ");
}

function securityUkeySummary(user) {
    if (user.ukeyId) {
        return user.ukeyId;
    }
    if (user.sm2UserId) {
        return `SM2:${user.sm2UserId}`;
    }
    if (user.ukeyRequired === 1) {
        return "要求UKey";
    }
    return "未绑定";
}

function securityDataScopeSummary(user) {
    if (user?.allOrganizations) {
        return "全部单位";
    }
    const code = user?.homeOrganizationCode;
    if (!code) {
        return "未设置";
    }
    const nodes = state.security.organizationNodes || [];
    const node = nodes.find(item => item.code === code);
    const name = node?.name || node?.shortName || "";
    return name ? `${code} — ${name}（含下属）` : `${code}（含下属）`;
}

function renderSecurityUsers() {
    const users = state.security.users || [];
    const selectAll = document.getElementById("security-user-select-all");
    if (selectAll) {
        selectAll.checked = false;
    }
    document.getElementById("security-user-rows").innerHTML = users.map(user => `
        <tr>
            <td class="security-check-col"><input type="checkbox" data-user-select="${user.id}"></td>
            <td>${escapeHtml(user.id)}</td>
            <td>${escapeHtml(user.username)}</td>
            <td>${escapeHtml(user.displayName)}</td>
            <td>${user.enabled ? "是" : "否"}</td>
            <td title="${escapeHtml(securityRoleSummary(user))}">${escapeHtml(securityRoleSummary(user))}</td>
            <td title="${escapeHtml(securityDataScopeSummary(user))}">${escapeHtml(securityDataScopeSummary(user))}</td>
            <td title="${escapeHtml(securityUkeySummary(user))}">${escapeHtml(securityUkeySummary(user))}</td>
            <td class="security-row-actions">
                <button class="row-action" data-user-roles="${user.id}">编辑权限</button>
                <button class="row-action" data-user-ukey="${user.id}">编辑UKey</button>
                <button class="row-action" data-user-password="${user.id}">重置密码</button>
                <button class="row-action" data-user-toggle="${user.id}" data-enabled="${!user.enabled}">${user.enabled ? "停用" : "启用"}</button>
            </td>
        </tr>
    `).join("");
    renderSecurityPageControls("users", state.security.userPage);

    document.querySelectorAll("[data-user-roles]").forEach(button => {
        button.addEventListener("click", () => openSecurityUserRolesModal(Number(button.dataset.userRoles)));
    });
    document.querySelectorAll("[data-user-ukey]").forEach(button => {
        button.addEventListener("click", () => openSecurityUserUkeyModal(Number(button.dataset.userUkey)));
    });
    document.querySelectorAll("[data-user-password]").forEach(button => {
        button.addEventListener("click", () => openSecurityUserPasswordModal(Number(button.dataset.userPassword)));
    });
    document.querySelectorAll("[data-user-toggle]").forEach(button => {
        button.addEventListener("click", async () => {
            const enabled = button.dataset.enabled === "true";
            await runSecuritySave(enabled ? "用户已启用" : "用户已停用", () => putJson(
                `/api/security/users/${button.dataset.userToggle}/enabled`,
                { enabled },
            ));
        });
    });
}

async function batchUpdateSecurityUsersEnabled(enabled) {
    const userIds = Array.from(document.querySelectorAll("[data-user-select]:checked"))
        .map(input => Number(input.dataset.userSelect))
        .filter(id => Number.isFinite(id) && id > 0);
    if (!userIds.length) {
        const status = document.getElementById("security-status");
        showError(status, new Error("请先勾选用户"));
        return;
    }
    const actionLabel = enabled ? "启用" : "停用";
    if (!confirm(`确认批量${actionLabel}选中的 ${userIds.length} 个用户？`)) {
        return;
    }
    await runSecuritySave(`已批量${actionLabel} ${userIds.length} 个用户`, () => putJson("/api/security/users/batch-enabled", {
        userIds,
        enabled,
    }));
}

function renderSecurityRoles() {
    const roles = state.security.roles || [];
    document.getElementById("security-role-rows").innerHTML = roles.map(role => `
        <tr>
            <td>${escapeHtml(role.id)}</td>
            <td>${escapeHtml(role.code)}</td>
            <td>${escapeHtml(role.name)}</td>
            <td>${renderPermissionChoices(role)}</td>
            <td>
                <button class="row-action" data-role-permissions="${role.id}">保存权限</button>
            </td>
        </tr>
    `).join("");
    renderSecurityPageControls("roles", state.security.rolePage);

    document.querySelectorAll("[data-role-permissions]").forEach(button => {
        button.addEventListener("click", async () => {
            const id = button.dataset.rolePermissions;
            await runSecuritySave("角色权限保存成功", () => putJson(`/api/security/roles/${id}/permissions`, {
                codes: checkedValues(`role-permissions-${id}`),
            }));
        });
    });
}

function renderSecurityMenus() {
    const container = document.getElementById("security-menu-tree");
    if (!container) {
        return;
    }
    const menus = state.security.menus || [];
    const draftById = new Map((state.security.menuDraft || []).map(item => [Number(item.id), item]));
    const childrenByParent = new Map();
    menus.forEach(menu => {
        const draft = draftById.get(Number(menu.id));
        const parentId = draft ? draft.parentId : (menu.parentId ?? null);
        const key = parentId == null ? "__ROOT__" : String(parentId);
        if (!childrenByParent.has(key)) {
            childrenByParent.set(key, []);
        }
        childrenByParent.get(key).push({
            ...menu,
            parentId,
            sortOrder: draft ? draft.sortOrder : menu.sortOrder,
        });
    });
    childrenByParent.forEach(list => list.sort((a, b) => (a.sortOrder - b.sortOrder) || (a.id - b.id)));

    const rows = [];
    const walk = (parentKey, depth) => {
        (childrenByParent.get(parentKey) || []).forEach(menu => {
            const childKey = String(menu.id);
            const hasChildren = (childrenByParent.get(childKey) || []).length > 0;
            const expanded = !hasChildren || state.security.menuExpandedIds.has(menu.id) || !!document.getElementById("security-menu-filter")?.value.trim();
            rows.push({ menu, depth, hasChildren, expanded });
            if (expanded) {
                walk(childKey, depth + 1);
            }
        });
    };
    walk("__ROOT__", 0);

    if (!rows.length) {
        container.innerHTML = "<div class='empty-state'>没有匹配的菜单</div>";
        renderPageInfo("security-menu-page-info", state.security.menuPage);
        return;
    }

    container.innerHTML = rows.map(({ menu, depth, hasChildren, expanded }) => `
        <div class="security-menu-node ${hasChildren ? "branch" : "leaf"}" style="--depth:${depth}"
             draggable="true" data-menu-id="${menu.id}" data-has-children="${hasChildren}">
            <em data-menu-toggle="${menu.id}">${hasChildren ? (expanded ? "▾" : "▸") : "•"}</em>
            <strong>${escapeHtml(menu.title)}</strong>
            <span>${escapeHtml(menu.code)}</span>
            <span class="security-menu-meta">${escapeHtml(menu.path)} · ${escapeHtml(menu.permissionCode)}${menu.enabled ? "" : " · 停用"}</span>
            <button type="button" class="row-action" data-menu-edit="${menu.id}">编辑</button>
        </div>
    `).join("");
    renderPageInfo("security-menu-page-info", {
        page: 0,
        totalPages: 1,
        totalElements: menus.length,
    });

    container.querySelectorAll("[data-menu-toggle]").forEach(toggle => {
        toggle.addEventListener("click", event => {
            event.stopPropagation();
            const id = Number(toggle.dataset.menuToggle);
            if (state.security.menuExpandedIds.has(id)) {
                state.security.menuExpandedIds.delete(id);
            } else {
                state.security.menuExpandedIds.add(id);
            }
            renderSecurityMenus();
        });
    });
    container.querySelectorAll("[data-menu-edit]").forEach(button => {
        button.addEventListener("click", event => {
            event.stopPropagation();
            openSecurityMenuEditModal(Number(button.dataset.menuEdit));
        });
    });
    wireSecurityMenuDrag(container);
}

function wireSecurityMenuDrag(container) {
    let dragId = null;
    container.querySelectorAll(".security-menu-node").forEach(node => {
        node.addEventListener("dragstart", event => {
            dragId = Number(node.dataset.menuId);
            event.dataTransfer.effectAllowed = "move";
            node.classList.add("dragging");
        });
        node.addEventListener("dragend", () => {
            node.classList.remove("dragging");
            container.querySelectorAll(".drag-over").forEach(el => el.classList.remove("drag-over"));
            dragId = null;
        });
        node.addEventListener("dragover", event => {
            event.preventDefault();
            node.classList.add("drag-over");
        });
        node.addEventListener("dragleave", () => node.classList.remove("drag-over"));
        node.addEventListener("drop", event => {
            event.preventDefault();
            node.classList.remove("drag-over");
            const targetId = Number(node.dataset.menuId);
            if (!dragId || dragId === targetId) {
                return;
            }
            applySecurityMenuDrop(dragId, targetId, event.offsetY > node.offsetHeight / 2);
            renderSecurityMenus();
        });
    });
}

function applySecurityMenuDrop(dragId, targetId, placeAfter) {
    const draft = state.security.menuDraft || [];
    const byId = new Map(draft.map(item => [Number(item.id), { ...item }]));
    const drag = byId.get(dragId);
    const target = byId.get(targetId);
    if (!drag || !target) {
        return;
    }
    const isDescendant = (ancestorId, maybeChildId) => {
        let current = byId.get(maybeChildId);
        while (current && current.parentId != null) {
            if (Number(current.parentId) === Number(ancestorId)) {
                return true;
            }
            current = byId.get(Number(current.parentId));
        }
        return false;
    };
    if (isDescendant(dragId, targetId)) {
        return;
    }
    // Drop onto upper half: nest under target; lower half: sibling after target.
    if (!placeAfter) {
        drag.parentId = targetId;
        const siblings = draft.filter(item => Number(item.parentId) === Number(targetId) && Number(item.id) !== dragId);
        drag.sortOrder = siblings.length ? Math.max(...siblings.map(item => item.sortOrder)) + 1 : 0;
    } else {
        drag.parentId = target.parentId ?? null;
        const siblings = draft
            .filter(item => (item.parentId ?? null) === (drag.parentId ?? null) && Number(item.id) !== dragId)
            .sort((a, b) => a.sortOrder - b.sortOrder);
        const targetIndex = siblings.findIndex(item => Number(item.id) === targetId);
        siblings.splice(targetIndex + 1, 0, drag);
        siblings.forEach((item, index) => {
            byId.get(Number(item.id)).sortOrder = index;
            byId.get(Number(item.id)).parentId = drag.parentId;
        });
    }
    byId.set(dragId, drag);
    state.security.menuDraft = Array.from(byId.values());
    state.security.menuExpandedIds.add(Number(drag.parentId));
}

async function saveSecurityMenuOrder() {
    const items = (state.security.menuDraft || []).map(item => ({
        id: item.id,
        parentId: item.parentId,
        sortOrder: item.sortOrder,
    }));
    await runSecuritySave("菜单排序已保存", () => putJson("/api/security/menus/reorder", { items }));
}

async function fillSecurityMenuParentOptions(selectId, excludeId = null) {
    const select = document.getElementById(selectId);
    if (!select) {
        return;
    }
    let menus = state.security.menus || [];
    if (!menus.length) {
        menus = await getJson("/api/security/menus");
        state.security.menus = menus || [];
    }
    const current = select.value;
    select.innerHTML = `<option value="">无（顶级）</option>` + (menus || [])
        .filter(menu => excludeId == null || Number(menu.id) !== Number(excludeId))
        .map(menu => `<option value="${menu.id}">${escapeHtml(menu.title)} (${escapeHtml(menu.code)})</option>`)
        .join("");
    if (current && [...select.options].some(option => option.value === current)) {
        select.value = current;
    }
}

async function openSecurityMenuEditModal(menuId) {
    const menu = (state.security.menus || []).find(item => Number(item.id) === Number(menuId));
    if (!menu) {
        return;
    }
    const draft = (state.security.menuDraft || []).find(item => Number(item.id) === Number(menuId));
    await fillSecurityMenuParentOptions("security-menu-edit-parent", menuId);
    document.getElementById("security-menu-edit-modal-title").textContent = `编辑菜单 — ${menu.title}`;
    document.getElementById("security-menu-edit-id").value = String(menu.id);
    document.getElementById("security-menu-edit-code").value = menu.code || "";
    document.getElementById("security-menu-edit-title").value = menu.title || "";
    document.getElementById("security-menu-edit-path").value = menu.path || "";
    document.getElementById("security-menu-edit-permission").value = menu.permissionCode || "";
    document.getElementById("security-menu-edit-parent").value = draft?.parentId != null
        ? String(draft.parentId)
        : (menu.parentId != null ? String(menu.parentId) : "");
    document.getElementById("security-menu-edit-sort").value = draft?.sortOrder ?? menu.sortOrder ?? 0;
    document.getElementById("security-menu-edit-enabled").checked = !!menu.enabled;
    openSecurityModal("security-menu-edit-modal");
}

async function saveSecurityMenuEdit(event) {
    event.preventDefault();
    const status = document.getElementById("security-menu-edit-status");
    const id = document.getElementById("security-menu-edit-id").value;
    const parentRaw = document.getElementById("security-menu-edit-parent").value;
    status.className = "status";
    status.textContent = "正在保存...";
    try {
        await putJson(`/api/security/menus/${id}`, {
            title: document.getElementById("security-menu-edit-title").value.trim(),
            path: document.getElementById("security-menu-edit-path").value.trim(),
            permissionCode: document.getElementById("security-menu-edit-permission").value.trim(),
            parentId: parentRaw ? Number(parentRaw) : null,
            sortOrder: Number(document.getElementById("security-menu-edit-sort").value || 0),
            enabled: document.getElementById("security-menu-edit-enabled").checked,
        });
        closeSecurityModal("security-menu-edit-modal");
        await loadSecurityAdmin({ statusMessage: "菜单保存成功" });
    } catch (error) {
        showError(status, error);
    }
}

function renderSecurityAudit() {
    const auditLogs = state.security.auditLogs || [];
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
    renderSecurityPageControls("audit", state.security.auditPage);
}

async function exportSecurityAuditCsv() {
    const status = document.getElementById("security-status");
    const params = new URLSearchParams({
        keyword: document.getElementById("security-audit-filter")?.value.trim() || "",
    });
    const from = document.getElementById("security-audit-from")?.value;
    const to = document.getElementById("security-audit-to")?.value;
    if (from) {
        params.set("from", from);
    }
    if (to) {
        params.set("to", to);
    }
    status.className = "status";
    status.textContent = "正在导出审计日志...";
    try {
        const response = await fetch(`/api/security/audit-logs/export.csv?${params}`, { credentials: "same-origin" });
        await ensureAuthenticatedApiResponse(response, "导出失败");
        if (!response.ok) {
            throw new Error(await response.text() || `导出失败（${response.status}）`);
        }
        const blob = await response.blob();
        const disposition = response.headers.get("Content-Disposition") || "";
        const match = disposition.match(/filename="?([^";]+)"?/i);
        const filename = match ? match[1] : "security-audit.csv";
        const link = document.createElement("a");
        link.href = URL.createObjectURL(blob);
        link.download = filename;
        link.click();
        URL.revokeObjectURL(link.href);
        showSuccess(status, `导出完成：${filename}`);
    } catch (error) {
        showError(status, error);
    }
}

function renderOrganizationSummary(role) {
    if (role.dataScope === "ALL") {
        return `<div class="scope-all">全部单位</div>`;
    }
    if (role.code === "UNIT_ADMIN") {
        return `<span class="status">由用户主管单位决定</span>`;
    }
    const codes = role.organizationCodes || [];
    if (!codes.length) {
        return `<span class="status">未指定单位</span>`;
    }
    const preview = codes.slice(0, 3).join(", ");
    const more = codes.length > 3 ? ` 等 ${codes.length} 个` : "";
    return `<span title="${escapeHtml(codes.join(", "))}">${escapeHtml(preview)}${escapeHtml(more)}</span>`;
}

async function openSecurityRoleOrgsModal(roleId) {
    const role = (state.security.roles || []).find(item => Number(item.id) === Number(roleId));
    if (!role) {
        return;
    }
    const status = document.getElementById("security-role-orgs-status");
    status.className = "status";
    status.textContent = "正在加载单位树...";
    document.getElementById("security-role-orgs-modal-title").textContent = `编辑单位范围 — ${role.name || role.code}`;
    document.getElementById("security-role-orgs-role-id").value = String(role.id);
    document.getElementById("security-organization-filter").value = "";
    state.security.roleOrgSelectedCodes = new Set(role.organizationCodes || []);
    openSecurityModal("security-role-orgs-modal");
    try {
        if (!state.security.organizationNodes?.length) {
            state.security.organizationNodes = await getJson("/api/organizations/tree");
        }
        state.security.organizationExpandedCodes = new Set();
        renderSecurityRoleOrgTree();
        status.textContent = `已选 ${state.security.roleOrgSelectedCodes.size} 个单位`;
    } catch (error) {
        showError(status, error);
    }
}

function renderSecurityRoleOrgTree() {
    const container = document.getElementById("security-role-orgs-tree");
    if (!container) {
        return;
    }
    const filter = document.getElementById("security-organization-filter")?.value.trim().toLowerCase() || "";
    const allNodes = state.security.organizationNodes || [];
    const childrenByParent = organizationChildrenByParent(allNodes);
    const roots = rootOrganizationNodes(allNodes);
    const selected = state.security.roleOrgSelectedCodes || new Set();
    const visibleNodes = [];
    const appendVisibleNodes = (node, depth) => {
        const children = childrenByParent.get(node.code) || [];
        const descendantMatches = children.some(child => organizationNodeMatchesFilter(child, filter, childrenByParent));
        const selfMatches = organizationNodeTextMatches(node, filter);
        if (!filter || selfMatches || descendantMatches) {
            visibleNodes.push({ node, depth, hasChildren: children.length > 0 });
            const expanded = filter || state.security.organizationExpandedCodes.has(node.code);
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
        const expanded = filter || state.security.organizationExpandedCodes.has(node.code);
        return `
            <label class="dictionary-node organization-node security-org-node ${hasChildren ? "branch" : "leaf"}" style="--depth:${depth}">
                <em data-org-expand="${escapeHtml(node.code)}" data-has-children="${hasChildren}">${hasChildren ? (expanded ? "▾" : "▸") : "•"}</em>
                <input type="checkbox" value="${escapeHtml(node.code)}" ${selected.has(node.code) ? "checked" : ""}>
                <strong>${escapeHtml(node.name || node.shortName || "")}</strong>
                <span>${escapeHtml(node.code)}</span>
            </label>
        `;
    }).join("");
    container.querySelectorAll("[data-org-expand]").forEach(toggle => {
        toggle.addEventListener("click", event => {
            event.preventDefault();
            event.stopPropagation();
            if (toggle.dataset.hasChildren !== "true") {
                return;
            }
            const code = toggle.dataset.orgExpand;
            if (state.security.organizationExpandedCodes.has(code)) {
                state.security.organizationExpandedCodes.delete(code);
            } else {
                state.security.organizationExpandedCodes.add(code);
            }
            renderSecurityRoleOrgTree();
        });
    });
    container.querySelectorAll("input[type='checkbox']").forEach(input => {
        input.addEventListener("change", () => {
            if (input.checked) {
                state.security.roleOrgSelectedCodes.add(input.value);
            } else {
                state.security.roleOrgSelectedCodes.delete(input.value);
            }
            const status = document.getElementById("security-role-orgs-status");
            if (status) {
                status.className = "status";
                status.textContent = `已选 ${state.security.roleOrgSelectedCodes.size} 个单位`;
            }
        });
    });
}

async function saveSecurityRoleOrganizations() {
    const status = document.getElementById("security-role-orgs-status");
    const roleId = document.getElementById("security-role-orgs-role-id").value;
    status.className = "status";
    status.textContent = "正在保存...";
    try {
        await putJson(`/api/security/roles/${roleId}/organizations`, {
            codes: Array.from(state.security.roleOrgSelectedCodes || []),
        });
        closeSecurityModal("security-role-orgs-modal");
        await loadSecurityAdmin({ statusMessage: "角色单位范围保存成功" });
    } catch (error) {
        showError(status, error);
    }
}

function renderSecurityPageControls(tab, page) {
    const infoId = {
        users: "security-user-page-info",
        roles: "security-role-page-info",
        menus: "security-menu-page-info",
        audit: "security-audit-page-info",
    }[tab];
    const prevId = {
        users: "security-user-prev",
        roles: "security-role-prev",
        menus: "security-menu-prev",
        audit: "security-audit-prev",
    }[tab];
    const nextId = {
        users: "security-user-next",
        roles: "security-role-next",
        menus: "security-menu-next",
        audit: "security-audit-next",
    }[tab];
    renderPageInfo(infoId, page);
    const current = page?.page ?? 0;
    const totalPages = Math.max(page?.totalPages || 1, 1);
    const prev = document.getElementById(prevId);
    const next = document.getElementById(nextId);
    if (prev) {
        prev.disabled = current <= 0;
    }
    if (next) {
        next.disabled = current >= totalPages - 1;
    }
}

async function ensureSecurityAllRoles() {
    if (state.security.allRoles?.length) {
        return state.security.allRoles;
    }
    const roles = await getJson("/api/security/roles");
    state.security.allRoles = roles || [];
    return state.security.allRoles;
}

async function openSecurityUserRolesModal(userId) {
    const user = (state.security.users || []).find(item => Number(item.id) === Number(userId));
    if (!user) {
        return;
    }
    const status = document.getElementById("security-user-roles-status");
    status.className = "status";
    status.textContent = "正在加载...";
    openSecurityModal("security-user-roles-modal");
    document.getElementById("security-user-roles-modal-title").textContent = `编辑权限 — ${user.displayName || user.username}`;
    document.getElementById("security-user-roles-user-id").value = String(user.id);
    document.getElementById("security-user-home-org-filter").value = "";
    state.security.userAllOrganizationsSelected = Boolean(user.allOrganizations);
    state.security.userHomeOrgSelectedCode = user.homeOrganizationCode || null;
    try {
        const roles = await ensureSecurityAllRoles();
        const selected = new Set(user.roleCodes || []);
        document.getElementById("security-user-roles-choices").innerHTML = roles.map(role => `
            <label class="checkbox-item" title="${escapeHtml(role.name || "")}">
                <input type="checkbox" value="${escapeHtml(role.code)}" ${selected.has(role.code) ? "checked" : ""}>
                <span>${escapeHtml(role.code)} ${escapeHtml(role.name || "")}</span>
            </label>
        `).join("") || "<p class=\"status\">暂无角色</p>";
        if (!state.security.organizationNodes?.length) {
            state.security.organizationNodes = await getJson("/api/organizations/tree");
        }
        state.security.userHomeOrgExpandedCodes = new Set();
        document.getElementById("security-user-data-scope-all").checked = state.security.userAllOrganizationsSelected;
        document.getElementById("security-user-data-scope-home").checked = !state.security.userAllOrganizationsSelected;
        syncSecurityUserDataScopePanel();
        renderSecurityUserHomeOrgTree();
        status.textContent = "";
    } catch (error) {
        showError(status, error);
    }
}

function syncSecurityUserDataScopePanel() {
    const panel = document.getElementById("security-user-home-org-panel");
    if (!panel) {
        return;
    }
    panel.classList.toggle("hidden", Boolean(state.security.userAllOrganizationsSelected));
}

function renderSecurityUserHomeOrgTree() {
    const container = document.getElementById("security-user-home-org-tree");
    if (!container) {
        return;
    }
    const filter = document.getElementById("security-user-home-org-filter")?.value.trim().toLowerCase() || "";
    const allNodes = state.security.organizationNodes || [];
    const childrenByParent = organizationChildrenByParent(allNodes);
    const roots = rootOrganizationNodes(allNodes);
    const selectedCode = state.security.userHomeOrgSelectedCode || null;
    const visibleNodes = [];
    const appendVisibleNodes = (node, depth) => {
        const children = childrenByParent.get(node.code) || [];
        const descendantMatches = children.some(child => organizationNodeMatchesFilter(child, filter, childrenByParent));
        const selfMatches = organizationNodeTextMatches(node, filter);
        if (!filter || selfMatches || descendantMatches) {
            visibleNodes.push({ node, depth, hasChildren: children.length > 0 });
            const expanded = filter || (state.security.userHomeOrgExpandedCodes || new Set()).has(node.code);
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
        const expanded = filter || (state.security.userHomeOrgExpandedCodes || new Set()).has(node.code);
        return `
            <label class="dictionary-node organization-node security-org-node ${hasChildren ? "branch" : "leaf"}" style="--depth:${depth}">
                <em data-user-home-org-expand="${escapeHtml(node.code)}" data-has-children="${hasChildren}" title="${hasChildren ? "展开/收起" : ""}">${hasChildren ? (expanded ? "▾" : "▸") : "•"}</em>
                <input type="radio" name="security-user-home-org" value="${escapeHtml(node.code)}" ${selectedCode === node.code ? "checked" : ""} aria-label="选择 ${escapeHtml(node.name || node.shortName || node.code)}">
                <strong title="${escapeHtml(node.name || node.shortName || "")}">${escapeHtml(node.name || node.shortName || "")}</strong>
                <span>${escapeHtml(node.code)}</span>
            </label>
        `;
    }).join("");
    container.querySelectorAll("[data-user-home-org-expand]").forEach(toggle => {
        toggle.addEventListener("click", event => {
            event.preventDefault();
            event.stopPropagation();
            if (toggle.dataset.hasChildren !== "true") {
                return;
            }
            const code = toggle.dataset.userHomeOrgExpand;
            if (!state.security.userHomeOrgExpandedCodes) {
                state.security.userHomeOrgExpandedCodes = new Set();
            }
            if (state.security.userHomeOrgExpandedCodes.has(code)) {
                state.security.userHomeOrgExpandedCodes.delete(code);
            } else {
                state.security.userHomeOrgExpandedCodes.add(code);
            }
            renderSecurityUserHomeOrgTree();
        });
    });
    container.querySelectorAll("input[type='radio']").forEach(input => {
        input.addEventListener("change", () => {
            if (input.checked) {
                state.security.userHomeOrgSelectedCode = input.value;
            }
        });
    });
}

async function saveSecurityUserRoles() {
    const status = document.getElementById("security-user-roles-status");
    const userId = document.getElementById("security-user-roles-user-id").value;
    status.className = "status";
    status.textContent = "正在保存...";
    try {
        const allOrganizations = Boolean(state.security.userAllOrganizationsSelected);
        await putJson(`/api/security/users/${userId}/data-scope`, {
            allOrganizations,
            organizationCode: allOrganizations ? "" : (state.security.userHomeOrgSelectedCode || ""),
        });
        await putJson(`/api/security/users/${userId}/roles`, {
            codes: checkedValues("security-user-roles-choices"),
        });
        closeSecurityModal("security-user-roles-modal");
        await loadSecurityAdmin({ statusMessage: "用户权限保存成功（重新登录后数据范围生效）" });
    } catch (error) {
        showError(status, error);
    }
}

function openSecurityUserUkeyModal(userId) {
    const user = (state.security.users || []).find(item => Number(item.id) === Number(userId));
    if (!user) {
        return;
    }
    document.getElementById("security-user-ukey-modal-title").textContent = `编辑 UKey — ${user.displayName || user.username}`;
    document.getElementById("security-ukey-user-id").value = String(user.id);
    document.getElementById("security-ukey-id").value = user.ukeyId || "";
    document.getElementById("security-ukey-sm2-id").value = user.sm2UserId || "";
    document.getElementById("security-ukey-sm2-x").value = user.sm2PubkeyX || "";
    document.getElementById("security-ukey-sm2-y").value = user.sm2PubkeyY || "";
    document.getElementById("security-ukey-enc-key").value = user.encAlgoKey || "";
    document.getElementById("security-ukey-modes").value = user.ukeyAuthModes || "";
    document.getElementById("security-ukey-required").value = user.ukeyRequired == null ? "" : String(user.ukeyRequired);
    openSecurityModal("security-user-ukey-modal");
}

async function saveSecurityUserUkey(event) {
    event.preventDefault();
    const status = document.getElementById("security-user-ukey-status");
    const userId = document.getElementById("security-ukey-user-id").value;
    status.className = "status";
    status.textContent = "正在保存...";
    try {
        const rawRequired = document.getElementById("security-ukey-required").value;
        await putJson(`/api/security/users/${userId}/ukey`, {
            ukeyId: document.getElementById("security-ukey-id").value.trim(),
            sm2UserId: document.getElementById("security-ukey-sm2-id").value.trim(),
            sm2PubkeyX: document.getElementById("security-ukey-sm2-x").value.trim(),
            sm2PubkeyY: document.getElementById("security-ukey-sm2-y").value.trim(),
            encAlgoKey: document.getElementById("security-ukey-enc-key").value.trim(),
            ukeyAuthModes: document.getElementById("security-ukey-modes").value.trim(),
            ukeyRequired: rawRequired === "" ? null : Number(rawRequired),
        });
        closeSecurityModal("security-user-ukey-modal");
        await loadSecurityAdmin({ statusMessage: "用户 UKey 绑定保存成功" });
    } catch (error) {
        showError(status, error);
    }
}

function openSecurityUserPasswordModal(userId) {
    const user = (state.security.users || []).find(item => Number(item.id) === Number(userId));
    if (!user) {
        return;
    }
    document.getElementById("security-user-password-modal-title").textContent = `重置密码 — ${user.displayName || user.username}`;
    document.getElementById("security-password-user-id").value = String(user.id);
    document.getElementById("security-password-new").value = "";
    document.getElementById("security-password-confirm").value = "";
    openSecurityModal("security-user-password-modal");
}

async function saveSecurityUserPassword(event) {
    event.preventDefault();
    const status = document.getElementById("security-user-password-status");
    const userId = document.getElementById("security-password-user-id").value;
    const password = document.getElementById("security-password-new").value;
    const confirmPassword = document.getElementById("security-password-confirm").value;
    status.className = "status";
    if (password !== confirmPassword) {
        showError(status, new Error("两次输入的密码不一致"));
        return;
    }
    if (password.length < 8) {
        showError(status, new Error("新密码长度至少 8 位"));
        return;
    }
    status.textContent = "正在重置...";
    try {
        await putJson(`/api/security/users/${userId}/password`, { password });
        closeSecurityModal("security-user-password-modal");
        await loadSecurityAdmin({ statusMessage: "密码已重置" });
    } catch (error) {
        showError(status, error);
    }
}

let securityReloadTimer = null;

function debounceSecurityReload() {
    clearTimeout(securityReloadTimer);
    securityReloadTimer = setTimeout(() => {
        const tab = state.security.activeTab || "users";
        if (tab === "users") {
            state.security.userPageIndex = 0;
        } else if (tab === "roles") {
            state.security.rolePageIndex = 0;
        } else if (tab === "menus") {
            state.security.menuPageIndex = 0;
        } else if (tab === "audit") {
            state.security.auditPageIndex = 0;
        }
        loadSecurityAdmin();
    }, 350);
}

function renderPageInfo(elementId, page) {
    const element = document.getElementById(elementId);
    if (!element) {
        return;
    }
    if (!page) {
        element.textContent = "";
        return;
    }
    element.textContent = `第 ${page.page + 1} / ${Math.max(page.totalPages, 1)} 页，共 ${page.totalElements} 条`;
}

function renderPermissionChoices(role) {
    const selected = new Set(role.permissionCodes || []);
    return `<div class="checkbox-grid" id="role-permissions-${role.id}">
        ${(state.security.permissions || []).map(permission => `
            <label class="checkbox-item" title="${escapeHtml(permission.code)}">
                <input type="checkbox" value="${escapeHtml(permission.code)}" ${selected.has(permission.code) ? "checked" : ""}>
                <span>${escapeHtml(permission.name || permission.code)}</span>
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

function formatLicenseSubjectText(data) {
    if (!data) {
        return "";
    }
    const name = String(data.subjectName || "").trim();
    const code = String(data.subjectCode || "").trim();
    if (name && code) {
        return `签约主体：${name}（${code}）`;
    }
    if (name) {
        return `签约主体：${name}`;
    }
    if (code) {
        return `签约主体：${code}`;
    }
    return "签约主体：未导入授权";
}

function applyLicenseSubjectDisplay(data) {
    const text = formatLicenseSubjectText(data);
    const authorized = !!(data && data.authorized);
    ["dashboard-license-subject", "domain-gate-license-subject"].forEach(id => {
        const el = document.getElementById(id);
        if (!el) {
            return;
        }
        if (!text) {
            el.hidden = true;
            el.textContent = "";
            el.removeAttribute("data-authorized");
            return;
        }
        el.hidden = false;
        el.textContent = text;
        el.dataset.authorized = authorized ? "true" : "false";
    });
}

async function refreshDashboardLicenseSubject() {
    try {
        if (!state.licenseStatus) {
            state.licenseStatus = await getJson("/api/license/status");
        }
        applyLicenseSubjectDisplay(state.licenseStatus);
    } catch (error) {
        applyLicenseSubjectDisplay(null);
    }
}

async function refreshLicenseBanner() {
    try {
        const data = await getJson("/api/license/status");
        state.licenseStatus = data;
        applyLicenseSubjectDisplay(data);
        let banner = document.getElementById("license-banner");
        if (!banner) {
            banner = document.createElement("div");
            banner.id = "license-banner";
            banner.className = "note-card";
            banner.style.margin = "0.75rem 1rem";
            const workspace = document.querySelector(".workspace") || document.querySelector("main") || document.body;
            workspace.prepend(banner);
        }
        if (data.authorized) {
            banner.classList.add("hidden");
            banner.textContent = "";
            return;
        }
        banner.classList.remove("hidden");
        banner.innerHTML = `<strong>尚未完成单位授权。</strong> ${escapeHtml(data.message || "")}
            ${hasMenu("LICENSE_IMPORT") ? ` <a href="#license-import">前往导入授权包</a>` : ""}`;
    } catch (error) {
        // ignore
    }
}

async function loadLicenseStatus() {
    const summary = document.getElementById("license-status-summary");
    const message = document.getElementById("license-status-message");
    if (!summary || !message) {
        return;
    }
    message.textContent = "正在加载授权状态...";
    try {
        const [data] = await Promise.all([
            getJson("/api/license/status"),
        ]);
        summary.innerHTML = `
            <div class="summary-item"><span>状态</span><strong>${data.authorized ? "已授权" : "未授权"}</strong></div>
            <div class="summary-item"><span>签约主体编码</span><strong>${escapeHtml(data.subjectCode || "-")}</strong></div>
            <div class="summary-item"><span>签约主体名称</span><strong>${escapeHtml(data.subjectName || "-")}</strong></div>
            <div class="summary-item"><span>种子单位数</span><strong>${escapeHtml(data.organizationCount ?? 0)}</strong></div>
            <div class="summary-item"><span>签发时间</span><strong>${escapeHtml(data.issuedAt || "-")}</strong></div>
            <div class="summary-item"><span>到期</span><strong>${escapeHtml(data.expiresAt || "长期")}</strong></div>
            <div class="summary-item"><span>UKey</span><strong>${formatLicenseUkeyPolicy(data)}</strong></div>
            <div class="summary-item"><span>指纹</span><strong>${escapeHtml(data.fingerprint || "-")}</strong></div>
        `;
        message.textContent = data.message || "";
        await refreshLicenseBanner();
    } catch (error) {
        showError(message, error);
    }
}

function formatLicenseUkeyPolicy(data) {
    if (!data || data.ukeyEnabled == null) {
        return "未声明（沿用环境变量）";
    }
    if (!data.ukeyEnabled) {
        return "关闭";
    }
    if (data.ukeyRequired === true) {
        return "启用，默认要求双认证";
    }
    return "启用，不强制双认证";
}

async function onOperationLogSearch(event) {
    event.preventDefault();
    await loadOperationLogs();
}

async function loadOperationLogs() {
    const status = document.getElementById("operation-log-status");
    const rows = document.getElementById("operation-log-rows");
    if (!status || !rows) {
        return;
    }
    status.className = "status";
    status.textContent = "正在查询上机日志...";
    try {
        const params = new URLSearchParams({
            keyword: document.getElementById("operation-log-keyword")?.value.trim() || "",
            page: document.getElementById("operation-log-page")?.value || "0",
            size: document.getElementById("operation-log-size")?.value || "50",
        });
        const page = await getJson(`/api/operation-logs/page?${params}`);
        const logs = page.content || [];
        rows.innerHTML = logs.map(log => `
            <tr>
                <td>${escapeHtml(log.id)}</td>
                <td>${escapeHtml(log.actorUsername)}</td>
                <td>${escapeHtml(log.action)}</td>
                <td>${escapeHtml(log.targetType)}</td>
                <td>${escapeHtml(log.targetId)}</td>
                <td>${escapeHtml(log.summary)}</td>
                <td>${escapeHtml(log.createdAt)}</td>
            </tr>
        `).join("");
        if (!logs.length) {
            status.textContent = "没有匹配的上机日志";
        } else {
            status.textContent = `第 ${(page.page || 0) + 1} / ${Math.max(page.totalPages || 1, 1)} 页，共 ${page.totalElements || 0} 条`;
        }
    } catch (error) {
        rows.innerHTML = "";
        showError(status, error);
    }
}

async function importUkeyBindingsPackage() {
    const status = document.getElementById("ukey-bind-import-status");
    const fileInput = document.getElementById("ukey-bind-import-file");
    const file = fileInput?.files?.[0];
    if (!status) {
        return;
    }
    if (!file) {
        status.textContent = "请先选择绑定包文件";
        return;
    }
    if (!confirm("确认导入 UKey 绑定包？将按用户名或已有芯片绑定更新用户 SM2 公钥。")) {
        return;
    }
    status.textContent = "正在导入绑定包...";
    try {
        const form = new FormData();
        form.append("file", file);
        const response = await fetch("/api/security/ukey-bindings/import", {
            method: "POST",
            credentials: "same-origin",
            body: form,
        });
        await ensureAuthenticatedApiResponse(response, "导入失败");
        const data = await response.json();
        if (!response.ok) {
            throw new Error(data.message || data.detail || "导入失败");
        }
        const failures = (data.failures || []).slice(0, 8).join("；");
        status.textContent = `导入完成：成功 ${data.successCount || 0}，失败 ${data.failureCount || 0}`
            + (failures ? `。部分失败：${failures}` : "");
        await loadSecurityAdmin();
    } catch (error) {
        showError(status, error);
    }
}

async function importLicensePackage() {
    const status = document.getElementById("license-import-status");
    const fileInput = document.getElementById("license-import-file");
    const file = fileInput?.files?.[0];
    if (!file) {
        status.textContent = "请先选择授权文件";
        return;
    }
    if (!confirm("确认导入单位授权？将写入签约主体并更新初始单位种子；不会删除本地已有多余单位。")) {
        return;
    }
    status.textContent = "正在导入授权包...";
    try {
        const form = new FormData();
        form.append("file", file);
        const response = await fetch("/api/license/import", {
            method: "POST",
            credentials: "same-origin",
            body: form,
        });
        await ensureAuthenticatedApiResponse(response, "导入失败");
        const data = await response.json();
        if (!response.ok) {
            throw new Error(data.message || data.detail || "导入失败");
        }
        status.textContent = data.message || "导入成功";
        await loadLicenseStatus();
    } catch (error) {
        showError(status, error);
    }
}

async function exportLicenseOrgsForOps() {
    const status = document.getElementById("license-orgs-export-status");
    if (!status) return;
    status.textContent = "正在导出单位目录...";
    try {
        const response = await fetch("/api/license/orgs-export", {
            method: "GET",
            credentials: "same-origin",
            headers: { Accept: "application/json" },
        });
        await ensureAuthenticatedApiResponse(response, "导出失败");
        if (!response.ok) {
            const err = await response.json().catch(() => ({ message: "导出失败" }));
            throw new Error(err.message || err.detail || "导出失败");
        }
        const blob = await response.blob();
        const url = URL.createObjectURL(blob);
        const link = document.createElement("a");
        link.href = url;
        link.download = "license-orgs-v1.json";
        document.body.appendChild(link);
        link.click();
        link.remove();
        URL.revokeObjectURL(url);
        status.textContent = "已下载 license-orgs-v1.json，请在 rsgzgl-ops「单位目录」中导入。";
    } catch (error) {
        showError(status, error);
    }
}

function syncLicenseIssueScopeControls() {
    const includeAll = document.getElementById("license-issue-include-all")?.checked === true;
    const wrap = document.getElementById("license-issue-subordinates-wrap");
    const subordinates = document.getElementById("license-issue-include-subordinates");
    if (wrap) {
        wrap.classList.toggle("hidden", includeAll);
    }
    if (subordinates) {
        subordinates.disabled = includeAll;
    }
}

async function refreshLicenseIssuePreview() {
    const preview = document.getElementById("license-issue-preview");
    if (!preview) {
        return;
    }
    syncLicenseIssueScopeControls();
    const code = (document.getElementById("license-issue-code")?.value || selectedOrganizationCode("license-issue-org") || "").trim();
    const includeAll = document.getElementById("license-issue-include-all")?.checked === true;
    if (!includeAll && !code) {
        preview.textContent = "请填写签约主体编码，或勾选「包含本地全部单位」。";
        return;
    }
    const includeSubordinates = document.getElementById("license-issue-include-subordinates")?.checked !== false;
    preview.textContent = "正在预览将写入授权包的单位种子...";
    try {
        const params = new URLSearchParams({
            includeSubordinates: String(includeSubordinates),
            includeAllOrganizations: String(includeAll),
        });
        if (code) {
            params.set("organizationCode", code);
        }
        const data = await getJson(`/api/license/issue-preview?${params}`);
        const nameInput = document.getElementById("license-issue-name");
        const levelInput = document.getElementById("license-issue-level");
        const cityInput = document.getElementById("license-issue-city");
        if (nameInput && !nameInput.value.trim() && data.organizationName) {
            nameInput.value = data.organizationName;
        }
        if (levelInput && !levelInput.value.trim() && data.organizationLevel != null) {
            levelInput.value = data.organizationLevel || "";
        }
        if (cityInput) {
            cityInput.value = data.city || "";
        }
        const subjectLabel = (document.getElementById("license-issue-name")?.value || data.organizationName || code || "未命名").trim();
        const sample = (data.organizationCodes || []).slice(0, 12).join("、");
        const more = (data.organizationCodes || []).length > 12
            ? ` 等共 ${data.organizationCount} 个`
            : `（共 ${data.organizationCount} 个）`;
        const cityLabel = data.city ? `；所在城市 ${data.city}` : "；所在城市（cyxx.szds）未填写";
        if (includeAll && !data.city) {
            preview.textContent = `签约主体「${subjectLabel}」；初始种子为本地全部单位：${sample}${more}${cityLabel}。全部单位签发时城市必填，请先在本地政策中维护。`;
            return;
        }
        if (includeAll) {
            preview.textContent = `签约主体「${subjectLabel}」；初始种子为本地全部单位：${sample}${more}${cityLabel}`;
        } else if (includeSubordinates) {
            preview.textContent = `签约主体「${subjectLabel}」；按编码前缀含下属：${sample}${more}${cityLabel}`;
        } else {
            preview.textContent = `签约主体「${subjectLabel}」；选定范围：${sample}${more}${cityLabel}`;
        }
    } catch (error) {
        preview.textContent = error.message || "预览失败";
    }
}

async function onLicenseIssueSubmit(event) {
    event.preventDefault();
    const status = document.getElementById("license-issue-status");
    const organizationCode = (document.getElementById("license-issue-code")?.value
        || selectedOrganizationCode("license-issue-org") || "").trim();
    const organizationName = document.getElementById("license-issue-name").value.trim();
    if (!organizationCode) {
        status.textContent = "请填写签约主体编码";
        return;
    }
    if (!organizationName) {
        status.textContent = "请填写签约主体名称";
        return;
    }
    const includeAll = document.getElementById("license-issue-include-all")?.checked === true;
    const includeSubordinates = document.getElementById("license-issue-include-subordinates")?.checked !== false;
    const city = document.getElementById("license-issue-city").value.trim();
    if (includeAll && !city) {
        status.className = "status error";
        status.textContent = "对全部单位签发授权时，所在城市（cyxx.szds）不能为空，请先在「本地政策/系统配置」中填写所在城市后再签发。";
        return;
    }
    const body = {
        organizationCode,
        organizationName,
        organizationLevel: document.getElementById("license-issue-level").value.trim(),
        city,
        supervisor: document.getElementById("license-issue-supervisor").value.trim(),
        expiresAt: document.getElementById("license-issue-expires").value.trim() || null,
        issuer: document.getElementById("license-issue-issuer").value.trim() || "鼎星软件",
        includeSubordinates,
        includeAllOrganizations: includeAll,
        organizations: [],
        ukeyEnabled: document.getElementById("license-issue-ukey-enabled")?.checked !== false,
        ukeyRequired: document.getElementById("license-issue-ukey-required")?.checked === true,
    };
    status.textContent = "正在签发授权包...";
    try {
        const response = await fetch("/api/license/issue", {
            method: "POST",
            credentials: "same-origin",
            headers: {
                Accept: "application/json",
                "Content-Type": "application/json",
            },
            body: JSON.stringify(body),
        });
        await ensureAuthenticatedApiResponse(response, "签发失败");
        if (!response.ok) {
            const err = await response.json().catch(() => ({ message: "签发失败" }));
            throw new Error(err.message || err.detail || "签发失败");
        }
        const blob = await response.blob();
        let filename = `单位授权-${body.organizationCode || "license"}.rsauth.json`;
        const disposition = response.headers.get("content-disposition") || "";
        const utfMatch = disposition.match(/filename\*=UTF-8''([^;]+)/i);
        if (utfMatch) {
            filename = decodeURIComponent(utfMatch[1]);
        }
        const url = URL.createObjectURL(blob);
        const link = document.createElement("a");
        link.href = url;
        link.download = filename;
        document.body.appendChild(link);
        link.click();
        link.remove();
        URL.revokeObjectURL(url);
        status.textContent = `已签发并下载：${filename}`
            + (includeAll ? "（本地全部单位作初始种子）" : (includeSubordinates ? "（含前缀下属）" : "（选定范围）"));
    } catch (error) {
        showError(status, error);
    }
}

function systemSetupClearOrgsChecked() {
    return document.getElementById("system-setup-init-clear-orgs")?.checked === true;
}

async function downloadExcelImportTemplate() {
    const status = document.getElementById("system-setup-import-status");
    if (!status) {
        return;
    }
    status.className = "status";
    status.textContent = "正在下载 Excel 模板...";
    try {
        const response = await fetch("/api/system-setup/excel-import/template", { credentials: "same-origin" });
        await ensureAuthenticatedApiResponse(response, "下载失败");
        if (!response.ok) {
            throw new Error(await readErrorMessage(response));
        }
        const blob = await response.blob();
        const url = URL.createObjectURL(blob);
        const a = document.createElement("a");
        a.href = url;
        a.download = "personnel-build-template.xlsx";
        document.body.appendChild(a);
        a.click();
        a.remove();
        URL.revokeObjectURL(url);
        status.textContent = "模板已下载";
    } catch (error) {
        showError(status, error);
    }
}

async function previewExcelImport() {
    const organizationCode = selectedOrganizationCode("system-setup-import-organization");
    const fileInput = document.getElementById("system-setup-import-file");
    const file = fileInput?.files?.[0];
    const status = document.getElementById("system-setup-import-status");
    const summary = document.getElementById("system-setup-import-summary");
    const rows = document.getElementById("system-setup-import-rows");
    if (!status || !rows) {
        return;
    }
    status.className = "status";
    rows.innerHTML = "";
    summary?.classList.add("hidden");
    if (!organizationCode) {
        status.className = "status error";
        status.textContent = "请先选择目标单位。";
        return;
    }
    if (!file) {
        status.className = "status error";
        status.textContent = "请先选择 Excel 文件。";
        return;
    }
    status.textContent = "正在解析 Excel...";
    try {
        const formData = new FormData();
        formData.append("organizationCode", organizationCode);
        formData.append("file", file);
        const response = await fetch("/api/system-setup/excel-import/preview", {
            method: "POST",
            credentials: "same-origin",
            body: formData,
        });
        await ensureAuthenticatedApiResponse(response, "预览失败");
        if (!response.ok) {
            throw new Error(await readErrorMessage(response));
        }
        const result = await response.json();
        if (summary) {
            summary.classList.remove("hidden");
            summary.innerHTML = `<p><strong>${escapeHtml(result.organizationCode || "")} ${escapeHtml(result.organizationName || "")}</strong>：${escapeHtml(result.message || "")}</p>`;
        }
        rows.innerHTML = (result.rows || []).map(row => `
            <tr>
                <td>${row.rowNumber}</td>
                <td>${escapeHtml(row.personCode || "")}</td>
                <td>${escapeHtml(row.name || "")}</td>
                <td>${escapeHtml(row.personnelCategory || "")}</td>
                <td>${escapeHtml(row.highestEducation || "")}</td>
                <td>${escapeHtml(row.positionLevel || "")}</td>
                <td>${escapeHtml(row.action || "")}</td>
                <td>${escapeHtml(row.message || "")}</td>
            </tr>
        `).join("") || "<tr><td colspan='8'>没有可预览的数据</td></tr>";
        status.textContent = result.message || "预览完成";
    } catch (error) {
        showError(status, error);
    }
}

async function executeExcelImport() {
    const organizationCode = selectedOrganizationCode("system-setup-import-organization");
    const fileInput = document.getElementById("system-setup-import-file");
    const file = fileInput?.files?.[0];
    const status = document.getElementById("system-setup-import-status");
    if (!status) {
        return;
    }
    if (!organizationCode) {
        status.className = "status error";
        status.textContent = "请先选择目标单位。";
        return;
    }
    if (!file) {
        status.className = "status error";
        status.textContent = "请先选择 Excel 文件。";
        return;
    }
    if (!confirm("确认按当前 Excel 正式导入人员？单位内已存在的人员编码将跳过，不会覆盖。")) {
        return;
    }
    status.className = "status";
    status.textContent = "正在导入 Excel 人员...";
    try {
        const formData = new FormData();
        formData.append("organizationCode", organizationCode);
        formData.append("file", file);
        const response = await fetch("/api/system-setup/excel-import/execute", {
            method: "POST",
            credentials: "same-origin",
            body: formData,
        });
        await ensureAuthenticatedApiResponse(response, "导入失败");
        if (!response.ok) {
            throw new Error(await readErrorMessage(response));
        }
        const result = await response.json();
        status.textContent = result.message
            || `导入完成：成功 ${result.importedCount ?? 0}，跳过 ${result.skippedCount ?? 0}`;
        await previewExcelImport();
    } catch (error) {
        showError(status, error);
    }
}

async function previewSystemInitialization() {
    const summary = document.getElementById("system-setup-init-preview-summary");
    const warning = document.getElementById("system-setup-init-warning");
    const status = document.getElementById("system-setup-init-status");
    if (!summary) {
        return;
    }
    const clearOrgs = systemSetupClearOrgsChecked();
    if (status) {
        status.className = "status";
        status.textContent = "正在加载清理范围...";
    }
    try {
        const data = await getJson(`/api/system-setup/initialization/preview?clearOrganizationsAndLicense=${clearOrgs}`);
        summary.classList.remove("hidden");
        const entries = Object.entries(data.tableCounts || {});
        summary.innerHTML = entries.map(([table, count]) => `
            <div class="summary-item"><span>${escapeHtml(table)}</span><strong>${escapeHtml(count)}</strong></div>
        `).join("") + `
            <div class="summary-item"><span>在册人员</span><strong>${escapeHtml(data.totalPersonnelRecords ?? 0)}</strong></div>
            <div class="summary-item"><span>单位数</span><strong>${escapeHtml(data.organizationCount ?? 0)}</strong></div>
            <div class="summary-item"><span>主体记录</span><strong>${escapeHtml(data.subjectCount ?? 0)}</strong></div>
            <div class="summary-item"><span>授权记录</span><strong>${escapeHtml(data.licenseCount ?? 0)}</strong></div>
        `;
        if (warning) {
            warning.textContent = data.warningMessage || "";
        }
        if (status) {
            status.textContent = clearOrgs
                ? "预览完成：将清空人员业务数据，以及单位与授权。"
                : "预览完成：将清空人员业务数据（保留单位）。";
        }
    } catch (error) {
        if (status) {
            showError(status, error);
        }
    }
}

async function executeSystemInitialization() {
    const status = document.getElementById("system-setup-init-status");
    const confirmInput = document.getElementById("system-setup-init-confirm");
    if (!status) {
        return;
    }
    const confirmPhrase = (confirmInput?.value || "").trim();
    const clearOrgs = systemSetupClearOrgsChecked();
    if (confirmPhrase !== "系统初始化") {
        status.className = "status error";
        status.textContent = "请输入确认短语：系统初始化";
        return;
    }
    const tip = clearOrgs
        ? "确认执行系统初始化？将删除人员业务数据，并清空单位、主体与授权记录。此操作不可撤销。"
        : "确认执行系统初始化？将删除全部人员及相关业务数据（单位保留）。此操作不可撤销。";
    if (!confirm(tip)) {
        return;
    }
    status.className = "status";
    status.textContent = "正在执行系统初始化...";
    try {
        const params = new URLSearchParams({
            confirmPhrase,
            clearOrganizationsAndLicense: String(clearOrgs),
        });
        const response = await fetch(`/api/system-setup/initialization/execute?${params}`, {
            method: "POST",
            credentials: "same-origin",
        });
        await ensureAuthenticatedApiResponse(response, "初始化失败");
        if (!response.ok) {
            throw new Error(await readErrorMessage(response));
        }
        const result = await response.json();
        status.textContent = result.message || "系统初始化完成";
        if (confirmInput) {
            confirmInput.value = "";
        }
        await previewSystemInitialization();
        await refreshLicenseBanner();
        await loadLicenseStatus();
    } catch (error) {
        showError(status, error);
    }
}

async function getJson(url) {
    const response = await fetch(url, {
        headers: { Accept: "application/json" },
        credentials: "same-origin",
    });
    await ensureAuthenticatedApiResponse(response, "请求失败");
    const contentType = response.headers.get("content-type") || "";
    if (!contentType.includes("json")) {
        window.location.href = "/login.html";
        throw new Error("登录已失效，请重新登录后再操作。");
    }
    return response.json();
}

async function loadDataMaintenanceDiagnostics() {
    const status = document.getElementById("data-maintenance-status");
    const summary = document.getElementById("data-maintenance-summary");
    if (!status || !summary) {
        return;
    }
    status.textContent = "正在加载库表概况...";
    try {
        const data = await getJson("/api/data-maintenance/diagnostics");
        summary.innerHTML = `
            <div class="summary-item"><span>在册人员</span><strong>${escapeHtml(data.personnelCount)}</strong></div>
            <div class="summary-item"><span>工资历史</span><strong>${escapeHtml(data.payrollHistoryCount)}</strong></div>
            <div class="summary-item"><span>操作日志</span><strong>${escapeHtml(data.auditLogCount)}</strong></div>
            <div class="summary-item"><span>记录标记</span><strong>${escapeHtml(data.appRecordMarkerCount)}</strong></div>
            <div class="summary-item"><span>孤立标记</span><strong>${escapeHtml(data.orphanAppRecordMarkerCount)}</strong></div>
        `;
        const tableLines = Object.entries(data.tableCounts || {})
            .map(([name, count]) => `<div class="summary-item"><span>${escapeHtml(name)}</span><strong>${escapeHtml(count)}</strong></div>`)
            .join("");
        summary.innerHTML += tableLines;
        status.textContent = "库表概况已更新";
    } catch (error) {
        showError(status, error);
    }
}

async function purgeDataMaintenanceAuditLogs() {
    const status = document.getElementById("data-maintenance-status");
    if (!confirm("确认清理 90 天前的操作日志？")) {
        return;
    }
    status.textContent = "正在清理操作日志...";
    try {
        const deleted = await postJson("/api/data-maintenance/purge-audit-logs?keepDays=90", {});
        status.textContent = `已清理 ${deleted} 条过期操作日志`;
        await loadDataMaintenanceDiagnostics();
    } catch (error) {
        showError(status, error);
    }
}

async function purgeDataMaintenanceOrphanMarkers() {
    const status = document.getElementById("data-maintenance-status");
    if (!confirm("确认清理孤立记录标记？")) {
        return;
    }
    status.textContent = "正在清理孤立标记...";
    try {
        const deleted = await postJson("/api/data-maintenance/purge-orphan-markers", {});
        status.textContent = `已清理 ${deleted} 条孤立标记`;
        await loadDataMaintenanceDiagnostics();
    } catch (error) {
        showError(status, error);
    }
}

async function exportDataBackup() {
    const status = document.getElementById("data-backup-status");
    const scopes = selectedBackupScopes();
    if (scopes.length === 0) {
        status.textContent = "请至少选择一个导出导入范围";
        return;
    }
    status.textContent = "正在导出新系统备份，请稍候...";
    try {
        const params = new URLSearchParams();
        scopes.forEach(scope => params.append("scopes", scope));
        const response = await fetch(`/api/data-maintenance/backup/export?${params}`, {
            method: "POST",
            credentials: "same-origin",
            headers: { Accept: "application/zip, application/json" },
        });
        await ensureAuthenticatedApiResponse(response, "导出备份失败");
        if (!(response.headers.get("content-type") || "").includes("zip")
                && !(response.headers.get("content-disposition") || "").includes("filename")) {
            const err = await response.json().catch(() => ({ message: "导出备份失败" }));
            throw new Error(err.message || "导出备份失败");
        }
        const blob = await response.blob();
        let filename = "rsgzgl-backup.rsbak";
        const disposition = response.headers.get("content-disposition") || "";
        const utfMatch = disposition.match(/filename\*=UTF-8''([^;]+)/i);
        const plainMatch = disposition.match(/filename=\"?([^\";]+)\"?/i);
        if (utfMatch) {
            filename = decodeURIComponent(utfMatch[1]);
        } else if (plainMatch) {
            filename = plainMatch[1];
        }
        const url = URL.createObjectURL(blob);
        const link = document.createElement("a");
        link.href = url;
        link.download = filename;
        document.body.appendChild(link);
        link.click();
        link.remove();
        URL.revokeObjectURL(url);
        status.textContent = `已导出备份：${filename}（范围：${scopes.join(", ")}）`;
    } catch (error) {
        showError(status, error);
    }
}

function selectedBackupScopes() {
    const checked = [...document.querySelectorAll('input[name="backup-scope"]:checked')]
        .map(el => el.value);
    if (checked.includes("ALL")) {
        return ["ALL"];
    }
    return checked;
}

async function inspectDataBackup() {
    const status = document.getElementById("data-backup-status");
    const summary = document.getElementById("data-backup-inspect-summary");
    const fileInput = document.getElementById("data-backup-file");
    const file = fileInput?.files?.[0];
    if (!file) {
        status.textContent = "请先选择备份文件";
        return;
    }
    status.textContent = "正在识别备份格式...";
    try {
        const form = new FormData();
        form.append("file", file);
        const response = await fetch("/api/data-maintenance/backup/inspect", {
            method: "POST",
            credentials: "same-origin",
            body: form,
        });
        await ensureAuthenticatedApiResponse(response, "识别失败");
        const data = await response.json();
        summary.classList.remove("hidden");
        const files = (data.tableFiles || []).slice(0, 30).map(name => `<li>${escapeHtml(name)}</li>`).join("");
        const more = (data.tableFiles || []).length > 30
            ? `<li>… 另有 ${(data.tableFiles || []).length - 30} 个文件</li>`
            : "";
        const scopeHints = (data.availableScopes || [])
            .filter(scope => scope.id && scope.id !== "ALL")
            .map(scope => `${escapeHtml(scope.label || scope.id)} ${escapeHtml(scope.matchedTables ?? (scope.tables || []).length)} 张`)
            .join("；");
        summary.innerHTML = `
            <p><strong>${escapeHtml(data.formatLabel || data.format)}</strong> — ${escapeHtml(data.message || "")}</p>
            <p>标识文件：${escapeHtml(data.markerFile || "无")}；版本：${escapeHtml(data.legacyVersion || "-")}</p>
            <p>${escapeHtml(data.organizationHint || "")}</p>
            ${scopeHints ? `<p>包内分组：${scopeHints}</p>` : ""}
            <p>恢复时将按左侧「导出导入范围」勾选导入。</p>
            <ul>${files}${more}</ul>
        `;
        status.textContent = data.message || "识别完成";
    } catch (error) {
        showError(status, error);
    }
}

async function restoreDataBackup() {
    const status = document.getElementById("data-backup-status");
    const fileInput = document.getElementById("data-backup-file");
    const confirmInput = document.getElementById("data-backup-confirm");
    const file = fileInput?.files?.[0];
    const confirmPhrase = (confirmInput?.value || "").trim();
    if (!file) {
        status.textContent = "请先选择备份文件";
        return;
    }
    if (confirmPhrase !== "数据恢复") {
        status.textContent = "请输入确认短语：数据恢复";
        return;
    }
    let scopes = selectedBackupScopes();
    if (scopes.length === 0) {
        status.textContent = "请至少选择一个导出导入范围";
        return;
    }
    const scopeText = scopes.includes("ALL") ? "全部表" : scopes.join(", ");
    if (!confirm(`确认执行数据恢复（范围：${scopeText}）？将覆盖当前库中对应表数据，且不可撤销。`)) {
        return;
    }
    status.textContent = "正在恢复数据，请勿关闭页面...";
    try {
        const form = new FormData();
        form.append("file", file);
        form.append("confirmPhrase", confirmPhrase);
        scopes.forEach(scope => form.append("scopes", scope));
        const response = await fetch("/api/data-maintenance/backup/restore", {
            method: "POST",
            credentials: "same-origin",
            body: form,
        });
        await ensureAuthenticatedApiResponse(response, "恢复失败");
        const data = await response.json();
        if (!response.ok) {
            throw new Error(data.message || "恢复失败");
        }
        status.textContent = data.message || "恢复完成";
        const summary = document.getElementById("data-backup-inspect-summary");
        summary.classList.remove("hidden");
        const restored = (data.restoredTables || []).map(name => `<li>${escapeHtml(name)}：${escapeHtml((data.rowCounts || {})[name] ?? 0)} 行</li>`).join("");
        const skipped = (data.skippedTables || []).map(name => `<li>${escapeHtml(name)}</li>`).join("");
        summary.innerHTML = `
            <p><strong>${escapeHtml(data.formatLabel || data.format)}</strong> — ${escapeHtml(data.message || "")}</p>
            <p>已恢复 ${escapeHtml(data.tablesRestored)} 张表 / ${escapeHtml(data.rowsRestored)} 行</p>
            <h4>已恢复</h4><ul>${restored || "<li>无</li>"}</ul>
            <h4>跳过</h4><ul>${skipped || "<li>无</li>"}</ul>
        `;
        await loadDataMaintenanceDiagnostics();
    } catch (error) {
        showError(status, error);
    }
}

function wireBackupScopeExclusiveChecks() {
    const box = document.getElementById("data-backup-scopes");
    if (!box) {
        return;
    }
    box.querySelectorAll('input[name="backup-scope"]').forEach(input => {
        input.addEventListener("change", () => {
            if (input.value === "ALL" && input.checked) {
                box.querySelectorAll('input[name="backup-scope"]').forEach(el => {
                    if (el.value !== "ALL") {
                        el.checked = false;
                    }
                });
            } else if (input.value !== "ALL" && input.checked) {
                const all = box.querySelector('input[name="backup-scope"][value="ALL"]');
                if (all) {
                    all.checked = false;
                }
            }
        });
    });
}

async function postJson(url, body) {
    return writeJson("POST", url, body);
}

async function putJson(url, body) {
    return writeJson("PUT", url, body);
}

async function deleteJson(url) {
    const response = await fetch(url, {
        method: "DELETE",
        headers: { Accept: "application/json" },
        credentials: "same-origin",
    });
    await ensureAuthenticatedApiResponse(response, "删除失败");
    return null;
}

async function writeJson(method, url, body) {
    const response = await fetch(url, {
        method,
        headers: {
            Accept: "application/json",
            "Content-Type": "application/json",
        },
        credentials: "same-origin",
        body: JSON.stringify(body),
    });
    await ensureAuthenticatedApiResponse(response, "请求失败");
    const contentType = response.headers.get("content-type") || "";
    if (!contentType.includes("json")) {
        // void 接口常返回 200/204 空 body，不能当成登录失效
        const text = await response.text();
        if (!text.trim()) {
            return null;
        }
        if (isLoginRedirectResponse(response) || looksLikeLoginPage(text)) {
            window.location.href = "/login.html";
            throw new Error("登录已失效，请重新登录后再操作。");
        }
        try {
            return JSON.parse(text);
        } catch (_error) {
            throw new Error(text);
        }
    }
    if (response.status === 204) {
        return null;
    }
    const text = await response.text();
    if (!text.trim()) {
        return null;
    }
    return JSON.parse(text);
}

function looksLikeLoginPage(text) {
    const sample = String(text || "").slice(0, 500).toLowerCase();
    return sample.includes("login.html") || sample.includes('id="login-form"') || sample.includes("用户登录");
}

function isLoginRedirectResponse(response) {
    return response.redirected && String(response.url || "").includes("/login.html");
}

function isAuthenticationFailure(response, bodyText) {
    if (response.status === 401 || response.status === 403) {
        return true;
    }
    if (isLoginRedirectResponse(response)) {
        return true;
    }
    const text = String(bodyText || "").trim().toLowerCase();
    return text === "authentication required"
            || text.includes("full authentication is required")
            || text.includes("access denied");
}

async function ensureAuthenticatedApiResponse(response, fallbackLabel) {
    if (response.ok) {
        return;
    }
    const message = await readErrorMessage(response.clone());
    if (isAuthenticationFailure(response, message)) {
        window.location.href = "/login.html";
        throw new Error("登录已失效，请重新登录后再操作。");
    }
    throw new Error(message || `${fallbackLabel}（${response.status}）`);
}

async function readErrorMessage(response) {
    const text = await response.text();
    if (text) {
        try {
            const parsed = JSON.parse(text);
            const message = parsed && (parsed.detail || parsed.message || parsed.title);
            if (message) {
                return message;
            }
        } catch (error) {
            // Response body was not JSON; fall back to the raw text below.
        }
    }
    return text || `HTTP ${response.status}`;
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
    const message = String(error?.message || error || "");
    if (/failed to fetch/i.test(message)) {
        element.textContent = "出错：网络请求失败（数据包过大、处理超时或服务中断）。请先点「预览接收」，或缩小单位范围后重试。";
        return;
    }
    element.textContent = `出错：${message}`;
}

function showSuccess(element, message) {
    const text = String(message || "保存成功");
    if (element) {
        element.className = "status success";
        element.textContent = text;
    }
    showAppToast(text);
}

function showAppToast(message) {
    const text = String(message || "").trim();
    if (!text) {
        return;
    }
    let toast = document.getElementById("app-toast");
    if (!toast) {
        toast = document.createElement("div");
        toast.id = "app-toast";
        document.body.appendChild(toast);
    }
    toast.className = "app-toast";
    toast.textContent = text;
    requestAnimationFrame(() => {
        toast.classList.add("app-toast-show");
    });
    clearTimeout(showAppToast._timer);
    showAppToast._timer = setTimeout(() => {
        toast.classList.remove("app-toast-show");
    }, 2800);
}

function escapeHtml(value) {
    return String(value ?? "")
        .replaceAll("&", "&amp;")
        .replaceAll("<", "&lt;")
        .replaceAll(">", "&gt;")
        .replaceAll('"', "&quot;")
        .replaceAll("'", "&#039;");
}

async function loadDataExchange() {
    const personnelStatus = document.getElementById("data-exchange-personnel-status");
    const personnelImportStatus = document.getElementById("data-exchange-personnel-import-status");
    const annualStatus = document.getElementById("data-exchange-annual-status");
    if (personnelStatus) {
        personnelStatus.textContent = "准备就绪";
    }
    if (personnelImportStatus) {
        personnelImportStatus.textContent = "准备就绪";
    }
    if (annualStatus) {
        annualStatus.textContent = "准备就绪";
    }
    renderDataExchangeSubmissionOrganizations();
    renderDataExchangeApprovalOrganizations();
    showDataExchangeGroup("submission");
}

const DATA_EXCHANGE_GROUPS = {
    submission: ["submission-export", "submission-review"],
    approval: ["approval-dispatch", "approval-receive"],
    export: ["personnel", "personnel-import", "annual"],
};

function dataExchangeGroupForTab(tab) {
    return Object.keys(DATA_EXCHANGE_GROUPS).find(group => DATA_EXCHANGE_GROUPS[group].includes(tab)) || "submission";
}

function showDataExchangeGroup(group, preferredTab) {
    const tabs = DATA_EXCHANGE_GROUPS[group] || DATA_EXCHANGE_GROUPS.submission;
    const tab = tabs.includes(preferredTab) ? preferredTab : tabs[0];
    showDataExchangeTab(tab);
}

function showDataExchangeTab(tab) {
    const panels = {
        "submission-export": "data-exchange-submission-export-panel",
        "submission-review": "data-exchange-submission-review-panel",
        "approval-dispatch": "data-exchange-approval-dispatch-panel",
        "approval-receive": "data-exchange-approval-receive-panel",
        personnel: "data-exchange-personnel-panel",
        "personnel-import": "data-exchange-personnel-import-panel",
        annual: "data-exchange-annual-panel",
    };
    const group = dataExchangeGroupForTab(tab);
    document.querySelectorAll("[data-exchange-group]").forEach(button => {
        button.classList.toggle("active", button.dataset.exchangeGroup === group);
    });
    document.querySelectorAll("[data-exchange-group-hint]").forEach(hint => {
        hint.classList.toggle("hidden", hint.dataset.exchangeGroupHint !== group);
    });
    document.querySelectorAll("[data-exchange-subtabs]").forEach(bar => {
        bar.classList.toggle("hidden", bar.dataset.exchangeSubtabs !== group);
    });
    document.querySelectorAll("[data-exchange-tab]").forEach(button => {
        button.classList.toggle("active", button.dataset.exchangeTab === tab);
    });
    Object.entries(panels).forEach(([key, id]) => {
        document.getElementById(id)?.classList.toggle("hidden", key !== tab);
    });
}

function renderDataExchangeDispatchOrganizations() {
    const container = document.getElementById("data-exchange-dispatch-organizations");
    if (!container) {
        return;
    }
    if (!state.dataExchangeDispatchOrganizations.length) {
        container.innerHTML = "<span>尚未选择导出单位</span>";
        return;
    }
    container.innerHTML = state.dataExchangeDispatchOrganizations.map(item => `
        <span>${escapeHtml(item.name)} (${escapeHtml(item.code)})
            <button type="button" class="row-action" data-remove-dispatch-org="${escapeHtml(item.code)}">移除</button>
        </span>
    `).join("");
    container.querySelectorAll("[data-remove-dispatch-org]").forEach(button => {
        button.addEventListener("click", () => {
            state.dataExchangeDispatchOrganizations = state.dataExchangeDispatchOrganizations.filter(item => item.code !== button.dataset.removeDispatchOrg);
            renderDataExchangeDispatchOrganizations();
        });
    });
}

async function onDataExchangeDispatchSearch(event) {
    event.preventDefault();
    await loadDispatchPackagePreview();
}

async function loadDispatchPackagePreview() {
    const status = document.getElementById("data-exchange-dispatch-status");
    const rows = document.getElementById("data-exchange-dispatch-rows");
    status.className = "status";
    status.textContent = "正在生成下发预览...";
    rows.innerHTML = "";
    try {
        const payload = await fetchDispatchPackage(false);
        state.dataExchangeDispatchRows = payload.personnel || [];
        rows.innerHTML = state.dataExchangeDispatchRows.map(row => `
            <tr>
                <td><input type="checkbox" data-dispatch-select value="${escapeHtml(row.organizationCode)}|${escapeHtml(row.personCode)}"></td>
                <td>${escapeHtml(row.organizationName || row.organizationCode)}</td>
                <td>${escapeHtml(row.personCode)}</td>
                <td>${escapeHtml(row.name)}</td>
                <td>${escapeHtml(maskIdCardClient(row.idCard))}</td>
                <td>${escapeHtml(row.currentJob || "")}</td>
                <td>${escapeHtml(row.currentGrade || "")}</td>
            </tr>
        `).join("");
        status.textContent = `下发预览 ${state.dataExchangeDispatchRows.length} 人；勾选人员后可只下发勾选人员。`;
    } catch (error) {
        showError(status, error);
    }
}

async function downloadDispatchPackage() {
    const status = document.getElementById("data-exchange-dispatch-status");
    status.className = "status";
    if (!state.dataExchangeDispatchOrganizations.length) {
        status.className = "status error";
        status.textContent = "请先选择下发单位。";
        return;
    }
    const selectedKeys = selectedExchangeKeys("[data-dispatch-select]:checked");
    if (!selectedKeys.length && !(state.dataExchangeDispatchRows || []).length) {
        status.className = "status error";
        status.textContent = "请先筛选人员，或勾选要下发的人员。";
        return;
    }
    if (!selectedKeys.length
            && (state.dataExchangeDispatchRows || []).length > 800
            && !confirm(`当前筛选约 ${(state.dataExchangeDispatchRows || []).length} 人，超过单次建议上限 800。\n请优先勾选分批下发；仍要全部生成可能较慢或失败，是否继续？`)) {
        return;
    }
    if (!selectedKeys.length
            && (state.dataExchangeDispatchRows || []).length <= 800
            && !confirm(`未勾选人员，将按当前条件下发全部人员（约 ${(state.dataExchangeDispatchRows || []).length || "全部"} 人），是否继续？`)) {
        return;
    }
    status.textContent = "正在生成建库包...";
    try {
        const keyword = document.getElementById("data-exchange-dispatch-keyword").value.trim();
        const payload = {
            organizationCodes: state.dataExchangeDispatchOrganizations.map(item => item.code),
            includeDescendants: document.getElementById("data-exchange-include-descendants").checked,
            keyword: keyword || null,
            selectedPersonnel: selectedKeys,
        };
        const response = await fetch("/api/data-exchange/dispatch/personnel", {
            method: "POST",
            credentials: "same-origin",
            headers: {
                Accept: "application/json",
                "Content-Type": "application/json",
            },
            body: JSON.stringify(payload),
        });
        await ensureAuthenticatedApiResponse(response, "生成建库包失败");
        if (!response.ok) {
            throw new Error(await readErrorMessage(response));
        }
        const blob = await response.blob();
        let filename = `rsgzgl_personnel_package_${new Date().toISOString().slice(0, 10)}.json`;
        const disposition = response.headers.get("content-disposition") || "";
        const utfMatch = disposition.match(/filename\*=UTF-8''([^;]+)/i);
        const plainMatch = disposition.match(/filename=\"?([^\";]+)\"?/i);
        if (utfMatch) {
            filename = decodeURIComponent(utfMatch[1]);
        } else if (plainMatch) {
            filename = plainMatch[1];
        }
        const url = URL.createObjectURL(blob);
        const a = document.createElement("a");
        a.href = url;
        a.download = filename;
        document.body.appendChild(a);
        a.click();
        a.remove();
        URL.revokeObjectURL(url);
        const countHint = selectedKeys.length || (state.dataExchangeDispatchRows || []).length || "";
        status.textContent = `建库包已生成${countHint ? `（约 ${countHint} 人）` : ""}`;
    } catch (error) {
        showError(status, error);
    }
}

function renderDataExchangeSubmissionOrganizations() {
    const container = document.getElementById("data-exchange-submission-organizations");
    if (!container) {
        return;
    }
    if (!state.dataExchangeSubmissionOrganizations.length) {
        container.innerHTML = "<span>尚未选择申报单位</span>";
        return;
    }
    container.innerHTML = state.dataExchangeSubmissionOrganizations.map(item => `
        <span>${escapeHtml(item.name)} (${escapeHtml(item.code)})
            <button type="button" class="row-action" data-remove-submission-org="${escapeHtml(item.code)}">移除</button>
        </span>
    `).join("");
    container.querySelectorAll("[data-remove-submission-org]").forEach(button => {
        button.addEventListener("click", () => {
            state.dataExchangeSubmissionOrganizations = state.dataExchangeSubmissionOrganizations.filter(item => item.code !== button.dataset.removeSubmissionOrg);
            renderDataExchangeSubmissionOrganizations();
        });
    });
}

async function onDataExchangeSubmissionSearch(event) {
    event.preventDefault();
    await loadSubmissionPackagePreview();
}

async function loadSubmissionPackagePreview() {
    const status = document.getElementById("data-exchange-submission-export-status");
    const rows = document.getElementById("data-exchange-submission-rows");
    status.className = "status";
    status.textContent = "正在生成申报预览...";
    rows.innerHTML = "";
    try {
        const payload = await fetchSubmissionPackage(false);
        state.dataExchangeSubmissionRows = payload.personnel || [];
        state.dataExchangeSubmissionPayrollTables = payload.payrollTables || [];
        rows.innerHTML = state.dataExchangeSubmissionRows.map(row => {
            const payroll = findSubmissionPayrollSummary(state.dataExchangeSubmissionPayrollTables, row.organizationCode, row.personCode);
            return `
            <tr>
                <td><input type="checkbox" data-submission-select value="${escapeHtml(row.organizationCode)}|${escapeHtml(row.personCode)}"></td>
                <td>${escapeHtml(row.organizationName || row.organizationCode)}</td>
                <td>${escapeHtml(row.personCode)}</td>
                <td>${escapeHtml(row.name)}</td>
                <td>${escapeHtml(payroll.changeType || "")}</td>
                <td>${escapeHtml(payroll.period || "")}</td>
                <td>${escapeHtml(payroll.total ?? "")}</td>
                <td>${escapeHtml(formatApprovalStatusLabel(payroll.approvalStatus || ""))}</td>
                <td>${escapeHtml(payroll.remark || "")}</td>
            </tr>
        `;
        }).join("");
        status.textContent = `申报预览 ${state.dataExchangeSubmissionRows.length} 人；勾选人员后可只导出勾选人员。`;
    } catch (error) {
        showError(status, error);
    }
}

function findSubmissionPayrollSummary(payrollTables, organizationCode, personCode) {
    const hisbase = (payrollTables || []).find(table => String(table.tableName || "").toLowerCase() === "hisbase");
    if (!hisbase?.rows?.length) {
        return {};
    }
    const personRows = hisbase.rows.filter(row =>
        String(row.dwbm || row.DWBM || "") === organizationCode
        && String(row.grbm || row.GRBM || "") === personCode);
    const current = personRows.find(row => !row.sid || String(row.sid).trim() === "");
    const row = current || personRows[personRows.length - 1];
    if (!row) {
        return {};
    }
    return {
        changeType: row.jslb || row.JSLB || "",
        period: `${row.jsnf || row.JSNF || ""}${row.jsyf || row.JSYF || ""}`,
        total: row.hj2 ?? row.HJ2 ?? "",
        remark: row.bbz || row.BBZ || "",
        approvalStatus: row.bbz || row.BBZ || "",
    };
}

async function downloadSubmissionPackage() {
    const status = document.getElementById("data-exchange-submission-export-status");
    status.className = "status";
    status.textContent = "正在生成申报包...";
    try {
        const selectedKeys = selectedExchangeKeys("[data-submission-select]:checked");
        const payload = buildSubmissionRequestPayload(selectedKeys.length > 0);
        const response = await fetch("/api/data-exchange/submission/export", {
            method: "POST",
            headers: { Accept: "application/json", "Content-Type": "application/json" },
            body: JSON.stringify(payload),
        });
        if (!response.ok) {
            throw new Error(await response.text() || `HTTP ${response.status}`);
        }
        const blob = await response.blob();
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement("a");
        a.href = url;
        a.download = `rsgzgl_submission_package_${new Date().toISOString().slice(0, 10)}.json`;
        document.body.appendChild(a);
        a.click();
        window.URL.revokeObjectURL(url);
        document.body.removeChild(a);
        status.textContent = `申报包已生成，共 ${payload.selectedPersonnel?.length || state.dataExchangeSubmissionRows.length || 0} 人，并已标记为“申报”。`;
        await loadSubmissionPackagePreview();
    } catch (error) {
        showError(status, error);
    }
}

async function fetchSubmissionPackage(onlySelected) {
    const payload = buildSubmissionRequestPayload(onlySelected);
    return postJson("/api/data-exchange/submission/preview", payload);
}

function buildSubmissionRequestPayload(onlySelected) {
    const selectedKeys = onlySelected ? selectedExchangeKeys("[data-submission-select]:checked") : [];
    return {
        organizationCodes: state.dataExchangeSubmissionOrganizations.map(item => item.code),
        includeDescendants: document.getElementById("data-exchange-submission-include-descendants").checked,
        keyword: document.getElementById("data-exchange-submission-keyword").value.trim() || null,
        selectedPersonnel: selectedKeys,
    };
}

async function onDataExchangeSubmissionReviewFileSelected(event) {
    const file = event.target.files && event.target.files[0];
    const status = document.getElementById("data-exchange-submission-review-status");
    if (!file) {
        document.getElementById("data-exchange-submission-review-json").value = "";
        return;
    }
    try {
        const text = await file.text();
        JSON.parse(text);
        document.getElementById("data-exchange-submission-review-json").value = text;
        status.className = "status";
        status.textContent = `已选择申报包：${file.name}，可点击“预览审核”。`;
    } catch (error) {
        document.getElementById("data-exchange-submission-review-json").value = "";
        status.className = "status error";
        status.textContent = `申报包文件格式错误：${error.message}`;
    }
}

async function onDataExchangeSubmissionReviewPreview(event) {
    event.preventDefault();
    await previewDataExchangeSubmissionReview();
}

async function previewDataExchangeSubmissionReview() {
    const status = document.getElementById("data-exchange-submission-review-status");
    const rows = document.getElementById("data-exchange-submission-review-rows");
    const summary = document.getElementById("data-exchange-submission-review-summary");
    const diffWrap = document.getElementById("data-exchange-submission-review-diffs");
    const diffRows = document.getElementById("data-exchange-submission-review-diff-rows");
    status.className = "status";
    status.textContent = "正在审核比对申报包与本地数据...";
    rows.innerHTML = "";
    if (diffRows) {
        diffRows.innerHTML = "";
    }
    diffWrap?.classList.add("hidden");
    summary.classList.add("hidden");
    summary.innerHTML = "";
    try {
        const packageJson = document.getElementById("data-exchange-submission-review-json").value;
        if (!packageJson) {
            throw new Error("请先选择申报包文件。");
        }
        const result = await postJson("/api/data-exchange/submission/review/preview", {
            packageJson,
            decision: null,
            selectedPersonnel: [],
            dryRun: null,
        });
        const previewRows = result.previewRows || [];
        rows.innerHTML = previewRows.map(row => {
            const checked = row.auditStatus === "不一致" || row.auditStatus === "新增人员" ? " checked" : "";
            return `
            <tr>
                <td><input type="checkbox" data-submission-review-select value="${escapeHtml(row.organizationCode)}|${escapeHtml(row.personCode)}"${checked}></td>
                <td>${escapeHtml(row.organizationName || row.organizationCode)}</td>
                <td>${escapeHtml(row.personCode)}</td>
                <td>${escapeHtml(row.name)}</td>
                <td>${escapeHtml(row.auditStatus || "")}</td>
                <td>${escapeHtml(row.mismatchSummary || "")}</td>
                <td>${escapeHtml(row.changeType || "")}</td>
                <td>${escapeHtml(row.calculationPeriod || "")}</td>
                <td>${escapeHtml(row.totalAmount ?? "")}</td>
                <td>${escapeHtml(row.action || "")}</td>
            </tr>`;
        }).join("");
        const diffHtml = previewRows.flatMap(row => (row.diffs || []).map(diff => `
            <tr>
                <td>${escapeHtml(row.organizationCode || "")}</td>
                <td>${escapeHtml(row.personCode || "")}</td>
                <td>${escapeHtml(row.name || "")}</td>
                <td>${escapeHtml(diff.item || "")}</td>
                <td>${escapeHtml(diff.localValue || "")}</td>
                <td>${escapeHtml(diff.submittedValue || "")}</td>
            </tr>
        `)).join("");
        if (diffRows && diffHtml) {
            diffRows.innerHTML = diffHtml;
            diffWrap?.classList.remove("hidden");
        }
        summary.innerHTML = renderSubmissionReviewSummary(result.summary);
        summary.classList.remove("hidden");
        status.textContent = result.message || `审核 ${result.totalRecords} 人`;
    } catch (error) {
        showError(status, error);
    }
}

async function applyDataExchangeSubmissionReview(decision, dryRun = false) {
    const status = document.getElementById("data-exchange-submission-review-status");
    status.className = "status";
    const selected = selectedExchangeKeys("[data-submission-review-select]:checked");
    if (!selected.length) {
        status.className = "status error";
        status.textContent = "请先勾选要处理的人员（默认已勾选不一致/新增）。";
        return;
    }
    const confirmMessage = decision === "REJECT"
        ? `将拒绝接收 ${selected.length} 人，不会写入数据库，是否继续？`
        : `将同意接收 ${selected.length} 人，并更新对应工资变动数据，是否继续？`;
    if (!window.confirm(confirmMessage)) {
        return;
    }
    status.textContent = decision === "REJECT" ? "正在拒绝接收..." : "正在同意接收并写入数据...";
    try {
        const packageJson = document.getElementById("data-exchange-submission-review-json").value;
        if (!packageJson) {
            throw new Error("请先选择申报包文件。");
        }
        const result = await postJson("/api/data-exchange/submission/review/apply", {
            packageJson,
            decision,
            selectedPersonnel: selected,
            dryRun,
        });
        status.textContent = result.message || `已处理 ${result.processedRecords || 0} 人`;
        if (!dryRun && decision === "APPROVE") {
            await previewDataExchangeSubmissionReview();
        }
    } catch (error) {
        showError(status, error);
    }
}

function renderSubmissionReviewSummary(summary) {
    if (!summary) {
        return "";
    }
    return `
        <div>共 ${summary.totalRecords || 0} 人：完全一致 ${summary.consistentRecords || 0}，不一致 ${summary.inconsistentRecords || 0}，新增 ${summary.newRecords || 0}；工资记录 ${summary.payrollRecords || 0} 条</div>
    `;
}

function renderDataExchangeApprovalOrganizations() {
    const container = document.getElementById("data-exchange-approval-organizations");
    if (!container) {
        return;
    }
    if (!state.dataExchangeApprovalOrganizations.length) {
        container.innerHTML = "<span>尚未选择审批下发单位</span>";
        return;
    }
    container.innerHTML = state.dataExchangeApprovalOrganizations.map(item => `
        <span>${escapeHtml(item.name)} (${escapeHtml(item.code)})
            <button type="button" class="row-action" data-remove-approval-org="${escapeHtml(item.code)}">移除</button>
        </span>
    `).join("");
    container.querySelectorAll("[data-remove-approval-org]").forEach(button => {
        button.addEventListener("click", () => {
            state.dataExchangeApprovalOrganizations = state.dataExchangeApprovalOrganizations.filter(item => item.code !== button.dataset.removeApprovalOrg);
            renderDataExchangeApprovalOrganizations();
        });
    });
}

async function onDataExchangeApprovalSearch(event) {
    event.preventDefault();
    await loadApprovalPackagePreview();
}

async function loadApprovalPackagePreview() {
    const status = document.getElementById("data-exchange-approval-dispatch-status");
    const rows = document.getElementById("data-exchange-approval-rows");
    status.className = "status";
    status.textContent = "正在生成审批下发预览...";
    rows.innerHTML = "";
    try {
        const result = await fetchApprovalPackage(false);
        const payload = result.packageData || result;
        state.dataExchangeApprovalRows = payload.personnel || [];
        state.dataExchangeApprovalPayrollTables = payload.payrollTables || [];
        state.dataExchangeApprovalStatusCounts = result.statusCounts || [];
        rows.innerHTML = state.dataExchangeApprovalRows.map(row => {
            const payroll = findSubmissionPayrollSummary(state.dataExchangeApprovalPayrollTables, row.organizationCode, row.personCode);
            return `
            <tr>
                <td><input type="checkbox" data-approval-select value="${escapeHtml(row.organizationCode)}|${escapeHtml(row.personCode)}"></td>
                <td>${escapeHtml(row.organizationName || row.organizationCode)}</td>
                <td>${escapeHtml(row.personCode)}</td>
                <td>${escapeHtml(row.name)}</td>
                <td>${escapeHtml(payroll.changeType || "")}</td>
                <td>${escapeHtml(payroll.period || "")}</td>
                <td>${escapeHtml(payroll.total ?? "")}</td>
                <td>${escapeHtml(formatApprovalStatusLabel(payroll.approvalStatus || ""))}</td>
                <td>${escapeHtml(payroll.remark || "")}</td>
            </tr>
        `;
        }).join("");
        status.textContent = result.message
            || `审批下发预览 ${state.dataExchangeApprovalRows.length} 人；勾选后可只下发勾选人员。`;
        if (!state.dataExchangeApprovalRows.length) {
            status.className = "status";
        }
    } catch (error) {
        showError(status, error);
    }
}

async function downloadApprovalPackage() {
    const status = document.getElementById("data-exchange-approval-dispatch-status");
    status.className = "status";
    const filterValue = document.getElementById("data-exchange-approval-status-filter")?.value || "dispatchable";
    if (filterValue === "已下发") {
        status.className = "status error";
        status.textContent = "当前筛选为「已下发」，不能生成审批包。请改回可下发状态，或先「退回已审」。";
        return;
    }
    status.textContent = "正在生成审批包...";
    try {
        const selectedKeys = selectedExchangeKeys("[data-approval-select]:checked");
        const payload = buildApprovalRequestPayload(selectedKeys.length > 0);
        const response = await fetch("/api/data-exchange/approval/export", {
            method: "POST",
            headers: { Accept: "application/json", "Content-Type": "application/json" },
            body: JSON.stringify(payload),
        });
        if (!response.ok) {
            throw new Error(await readErrorMessage(response));
        }
        const blob = await response.blob();
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement("a");
        a.href = url;
        a.download = `rsgzgl_approval_package_${new Date().toISOString().slice(0, 10)}.json`;
        document.body.appendChild(a);
        a.click();
        window.URL.revokeObjectURL(url);
        document.body.removeChild(a);
        status.textContent = `审批包已生成，共 ${payload.selectedPersonnel?.length || state.dataExchangeApprovalRows.length || 0} 人，并已标记为“已下发”。`;
        await loadApprovalPackagePreview();
    } catch (error) {
        showError(status, error);
    }
}

async function revertDispatchedApprovalPackage() {
    const status = document.getElementById("data-exchange-approval-dispatch-status");
    status.className = "status";
    if (!state.dataExchangeApprovalOrganizations.length) {
        status.className = "status error";
        status.textContent = "请先选择下发单位。";
        return;
    }
    const selectedKeys = selectedExchangeKeys("[data-approval-select]:checked");
    const filterValue = document.getElementById("data-exchange-approval-status-filter")?.value || "dispatchable";
    const confirmText = selectedKeys.length
        ? `将勾选的 ${selectedKeys.length} 人从「已下发」退回为「已审」，是否继续？`
        : filterValue === "已下发"
            ? `未勾选时将把当前筛选到的全部「已下发」人员退回为「已审」（约 ${(state.dataExchangeApprovalRows || []).length} 人），是否继续？`
            : "未勾选人员。请先将审批状态筛为「已下发」并勾选，或勾选列表中的已下发人员后再退回。";
    if (!selectedKeys.length && filterValue !== "已下发") {
        status.className = "status error";
        status.textContent = confirmText;
        return;
    }
    if (!window.confirm(confirmText)) {
        return;
    }
    status.textContent = "正在退回已审...";
    try {
        const payload = buildApprovalRequestPayload(selectedKeys.length > 0);
        if (!selectedKeys.length) {
            payload.approvalStatuses = ["已下发"];
        }
        const result = await postJson("/api/data-exchange/approval/revert-dispatched", payload);
        status.textContent = result.message || `已退回 ${result.updatedRecords || 0} 人`;
        const statusFilter = document.getElementById("data-exchange-approval-status-filter");
        if (statusFilter) {
            statusFilter.value = "dispatchable";
        }
        await loadApprovalPackagePreview();
    } catch (error) {
        showError(status, error);
    }
}

async function fetchApprovalPackage(onlySelected) {
    return postJson("/api/data-exchange/approval/preview", buildApprovalRequestPayload(onlySelected));
}

function approvalStatusesFromFilter() {
    const value = document.getElementById("data-exchange-approval-status-filter")?.value || "dispatchable";
    if (value === "dispatchable") {
        return ["申报", "已审", "审批通过"];
    }
    return [value];
}

function formatApprovalStatusLabel(status) {
    const text = String(status || "").trim();
    if (text === "审批通过") {
        return "审批通过（视同已审）";
    }
    return text;
}

function buildApprovalRequestPayload(onlySelected) {
    const selectedKeys = onlySelected ? selectedExchangeKeys("[data-approval-select]:checked") : [];
    return {
        organizationCodes: state.dataExchangeApprovalOrganizations.map(item => item.code),
        includeDescendants: document.getElementById("data-exchange-approval-include-descendants").checked,
        keyword: document.getElementById("data-exchange-approval-keyword").value.trim() || null,
        selectedPersonnel: selectedKeys,
        approvalStatuses: approvalStatusesFromFilter(),
    };
}

async function onDataExchangeApprovalReceiveFileSelected(event) {
    const file = event.target.files && event.target.files[0];
    const status = document.getElementById("data-exchange-approval-receive-status");
    if (!file) {
        document.getElementById("data-exchange-approval-receive-json").value = "";
        return;
    }
    try {
        const text = await file.text();
        JSON.parse(text);
        document.getElementById("data-exchange-approval-receive-json").value = text;
        status.className = "status";
        status.textContent = `已选择审批包：${file.name}，可点击“预览接收”。`;
    } catch (error) {
        document.getElementById("data-exchange-approval-receive-json").value = "";
        status.className = "status error";
        status.textContent = `审批包文件格式错误：${error.message}`;
    }
}

async function onDataExchangeApprovalReceivePreview(event) {
    event.preventDefault();
    await previewDataExchangeApprovalReceive();
}

async function previewDataExchangeApprovalReceive() {
    const status = document.getElementById("data-exchange-approval-receive-status");
    const rows = document.getElementById("data-exchange-approval-receive-rows");
    const summary = document.getElementById("data-exchange-approval-receive-summary");
    status.className = "status";
    status.textContent = "正在解析审批包...";
    rows.innerHTML = "";
    summary.classList.add("hidden");
    summary.innerHTML = "";
    try {
        const packageJson = document.getElementById("data-exchange-approval-receive-json").value;
        if (!packageJson) {
            throw new Error("请先选择审批包文件。");
        }
        const result = await postJson("/api/data-exchange/approval/receive/preview", {
            packageJson,
            selectedPersonnel: [],
            dryRun: null,
        });
        rows.innerHTML = (result.previewRows || []).map(row => `
            <tr>
                <td><input type="checkbox" data-approval-receive-select value="${escapeHtml(row.organizationCode)}|${escapeHtml(row.personCode)}"></td>
                <td>${escapeHtml(row.organizationName || row.organizationCode)}</td>
                <td>${escapeHtml(row.personCode)}</td>
                <td>${escapeHtml(row.name)}</td>
                <td>${escapeHtml(row.changeType || "")}</td>
                <td>${escapeHtml(row.calculationPeriod || "")}</td>
                <td>${escapeHtml(row.totalAmount ?? "")}</td>
                <td>${escapeHtml(row.approvalStatus || "")}</td>
                <td>${escapeHtml(row.submissionStatus || "")}</td>
                <td>${escapeHtml(row.action || "")}</td>
            </tr>
        `).join("");
        summary.innerHTML = renderSubmissionReviewSummary(result.summary);
        summary.classList.remove("hidden");
        status.textContent = result.message || `预览 ${result.totalRecords} 人`;
    } catch (error) {
        showError(status, error);
    }
}

async function applyDataExchangeApprovalReceive(dryRun = false) {
    const status = document.getElementById("data-exchange-approval-receive-status");
    status.className = "status";
    const selected = selectedExchangeKeys("[data-approval-receive-select]:checked");
    if (!dryRun) {
        const confirmMessage = selected.length
            ? `将接收 ${selected.length} 条审批记录并替换本地工资变动数据，是否继续？`
            : "将接收审批包中的全部审批记录并替换本地工资变动数据，是否继续？";
        if (!window.confirm(confirmMessage)) {
            return;
        }
    }
    status.textContent = dryRun ? "正在试运行审批接收..." : "正在确认接收审批数据...";
    try {
        const packageJson = document.getElementById("data-exchange-approval-receive-json").value;
        if (!packageJson) {
            throw new Error("请先选择审批包文件。");
        }
        const result = await postJson("/api/data-exchange/approval/receive/apply", {
            packageJson,
            selectedPersonnel: selected,
            dryRun,
        });
        status.textContent = result.message || `已处理 ${result.processedRecords || 0} 条审批记录`;
        if (!dryRun) {
            await previewDataExchangeApprovalReceive();
        }
    } catch (error) {
        showError(status, error);
    }
}

async function fetchDispatchPackage(onlySelected) {
    const selectedKeys = onlySelected ? selectedExchangeKeys("[data-dispatch-select]:checked") : [];
    const keyword = document.getElementById("data-exchange-dispatch-keyword").value.trim();
    const payload = {
        organizationCodes: state.dataExchangeDispatchOrganizations.map(item => item.code),
        includeDescendants: document.getElementById("data-exchange-include-descendants").checked,
        keyword: keyword || null,
        selectedPersonnel: selectedKeys,
    };
    return postJson("/api/data-exchange/dispatch/preview", payload);
}

async function onDataExchangeReceivePreview(event) {
    event.preventDefault();
    await previewDataExchangeReceive();
}

async function onDataExchangeReceiveFileSelected(event) {
    const file = event.target.files && event.target.files[0];
    const status = document.getElementById("data-exchange-receive-status");
    if (!file) {
        document.getElementById("data-exchange-receive-json").value = "";
        return;
    }
    try {
        const text = await file.text();
        JSON.parse(text);
        document.getElementById("data-exchange-receive-json").value = text;
        status.className = "status";
        status.textContent = `已选择数据包：${file.name}，请先点击“预览”。`;
    } catch (error) {
        document.getElementById("data-exchange-receive-json").value = "";
        status.className = "status error";
        status.textContent = `数据包文件格式错误：${error.message}`;
    }
}

async function previewDataExchangeReceive() {
    const status = document.getElementById("data-exchange-receive-status");
    const rows = document.getElementById("data-exchange-receive-rows");
    const summary = document.getElementById("data-exchange-receive-summary");
    status.className = "status";
    status.textContent = "正在解析数据包...";
    rows.innerHTML = "";
    summary.classList.add("hidden");
    summary.innerHTML = "";
    try {
        const packageJson = document.getElementById("data-exchange-receive-json").value;
        if (!packageJson) {
            throw new Error("请先选择数据包文件。");
        }
        const appendMode = Boolean(selectedOrganizationCode("data-exchange-receive-target-organization"));
        const result = await postJson("/api/data-exchange/receive/preview", {
            packageJson,
            mode: appendMode ? "APPEND" : "REPLACE",
            targetOrganizationCode: selectedOrganizationCode("data-exchange-receive-target-organization"),
            selectedPersonnel: [],
        });
        state.dataExchangeReceiveRows = result.rows || [];
        const previewRows = result.previewRows || [];
        rows.innerHTML = state.dataExchangeReceiveRows.map(row => {
            const preview = previewRows.find(item => item.organizationCode === row.organizationCode && item.personCode === row.personCode) || {};
            return `
            <tr>
                <td><input type="checkbox" data-receive-select value="${escapeHtml(row.organizationCode)}|${escapeHtml(row.personCode)}"></td>
                <td>${escapeHtml(row.organizationName || row.organizationCode)}</td>
                <td>${escapeHtml(row.personCode)}</td>
                <td>${escapeHtml(row.name)}</td>
                <td>${escapeHtml(maskIdCardClient(row.idCard))}</td>
                <td>${escapeHtml(row.currentJob || "")}</td>
                <td>${escapeHtml(row.currentGrade || "")}</td>
                <td>${escapeHtml(preview.action || "")}</td>
                <td>${escapeHtml(preview.targetOrganizationCode || "")}</td>
                <td>${escapeHtml(preview.targetPersonCode || "")}</td>
                <td>${preview.targetOrganizationExists === false ? "否" : "是"}</td>
                <td>${formatTableCounts(preview.relatedCounts || [])}</td>
            </tr>
        `}).join("");
        summary.innerHTML = renderReceiveSummary(result.summary);
        summary.classList.remove("hidden");
        status.textContent = result.message || `预览 ${result.totalRecords} 人`;
    } catch (error) {
        showError(status, error);
    }
}

async function confirmDataExchangeReceive() {
    const targetOrganizationCode = selectedOrganizationCode("data-exchange-receive-target-organization");
    if (targetOrganizationCode) {
        const selected = selectedExchangeKeys("[data-receive-select]:checked");
        if (selected.length === 0) {
            const status = document.getElementById("data-exchange-receive-status");
            status.className = "status error";
            status.textContent = "追加接收请先勾选人员，或清空“追加到单位”后改为整体接收。";
            return;
        }
        await applyDataExchangeReceive("APPEND", false);
        return;
    }
    await applyDataExchangeReceive("REPLACE", false);
}

async function applyDataExchangeReceive(mode, dryRun = false) {
    const status = document.getElementById("data-exchange-receive-status");
    status.className = "status";
    const selected = mode === "APPEND" ? selectedExchangeKeys("[data-receive-select]:checked") : [];
    const targetOrganizationCode = selectedOrganizationCode("data-exchange-receive-target-organization");
    const confirmMessage = dryRun
        ? null
        : mode === "APPEND"
        ? `将追加 ${selected.length} 人到单位 ${document.getElementById("data-exchange-receive-target-organization").value}，并重新生成个人编码，是否继续？`
        : "将整体接收数据包：相同单位编码+个人编码已存在则替换，不存在则新增。是否继续？";
    if (confirmMessage && !window.confirm(confirmMessage)) {
        return;
    }
    status.textContent = dryRun ? "正在试运行接收校验..." : mode === "APPEND" ? "正在追加接收勾选人员..." : "正在整体接收并写入数据...";
    try {
        const packageJson = document.getElementById("data-exchange-receive-json").value;
        if (!packageJson) {
            throw new Error("请先选择数据包文件。");
        }
        const result = await postJson("/api/data-exchange/receive/apply", {
            packageJson,
            mode,
            targetOrganizationCode,
            selectedPersonnel: selected,
            dryRun,
        });
        const mappingText = (result.codeMappings || []).map(item =>
            `${escapeHtml(item.name)}：${escapeHtml(item.sourceOrganizationCode)}-${escapeHtml(item.sourcePersonCode)} -> ${escapeHtml(item.targetOrganizationCode)}-${escapeHtml(item.targetPersonCode)}`
        ).join("；");
        status.textContent = `${result.message || `已接收 ${result.receivedRecords} 人`}。新增 ${result.newRecords || 0}，替换 ${result.replacedRecords || 0}，追加 ${result.appendedRecords || 0}${mappingText ? "。编码映射：" + mappingText : ""}`;
    } catch (error) {
        showError(status, error);
    }
}

function renderReceiveSummary(summary) {
    if (!summary) {
        return "";
    }
    return `
        <strong>接收预览</strong>
        <p>总人数：${summary.totalRecords || 0}；新增：${summary.newRecords || 0}；替换：${summary.replaceRecords || 0}；重新编码追加：${summary.appendRecords || 0}</p>
        <p>子表记录：${formatTableCounts(summary.relatedCounts || [])}</p>
    `;
}

function formatTableCounts(counts) {
    return (counts || [])
        .filter(item => Number(item.count || 0) > 0)
        .map(item => `${escapeHtml(item.tableName)}:${Number(item.count || 0)}`)
        .join("，") || "无";
}

function selectedExchangeKeys(selector) {
    const unique = new Map();
    Array.from(document.querySelectorAll(selector)).forEach(input => {
        const [organizationCode, personCode] = String(input.value || "").split("|");
        const code = (organizationCode || "").trim();
        const person = (personCode || "").trim();
        if (!code || !person) {
            return;
        }
        unique.set(`${code}|${person}`, { organizationCode: code, personCode: person });
    });
    return Array.from(unique.values());
}

function selectedOrganizationCode(inputId) {
    const input = document.getElementById(inputId);
    if (!input) {
        return "";
    }
    if (input.readOnly) {
        return (input.dataset.organizationCode || "").trim();
    }
    return (input.dataset.organizationCode || input.value || "").trim();
}

function setOrganizationInput(inputId, organizationCode, organizationName) {
    const input = document.getElementById(inputId);
    if (!input) {
        return;
    }
    if (!organizationCode) {
        clearOrganizationInput(input);
        return;
    }
    input.dataset.organizationCode = organizationCode;
    input.value = organizationName || organizationCode;
    input.title = organizationName ? `${organizationName} (${organizationCode})` : organizationCode;
}

function maskIdCardClient(idCard) {
    if (!idCard || idCard.length < 8) {
        return idCard || "";
    }
    return `${idCard.slice(0, 4)}****${idCard.slice(-4)}`;
}

async function onDataExchangePersonnelSearch(event) {
    event.preventDefault();
    const organizationCode = selectedOrganizationCode("data-exchange-personnel-organization");
    const keyword = document.getElementById("data-exchange-personnel-keyword").value.trim();
    const includeDescendants = document.getElementById("data-exchange-personnel-include-descendants")?.checked ?? true;
    const status = document.getElementById("data-exchange-personnel-status");
    const tbody = document.getElementById("data-exchange-personnel-rows");
    const selectAll = document.getElementById("data-exchange-personnel-select-all");

    status.className = "status";
    status.textContent = "正在查询...";
    tbody.innerHTML = "";
    if (selectAll) {
        selectAll.checked = false;
    }

    try {
        let rows = [];
        if (organizationCode) {
            const payload = await postJson("/api/data-exchange/dispatch/preview", {
                organizationCodes: [organizationCode],
                includeDescendants,
                keyword: keyword || null,
                selectedPersonnel: [],
            });
            rows = payload.personnel || [];
        } else {
            const data = await getJson(`/api/data-exchange/personnel?organizationCode=&keyword=${encodeURIComponent(keyword)}&page=0&size=200`);
            rows = data.content || [];
            if ((data.totalElements || 0) > rows.length) {
                status.className = "status";
                status.textContent = `未选单位时仅预览前 ${rows.length} / ${data.totalElements} 条；请选择单位以完整筛选并生成人员包。`;
            }
        }
        state.dataExchangePersonnelRows = rows;
        tbody.innerHTML = rows.map(r => `
            <tr>
                <td><input type="checkbox" data-personnel-export-select value="${escapeHtml(r.organizationCode)}|${escapeHtml(r.personCode)}"></td>
                <td>${escapeHtml(r.organizationName || r.organizationCode)}</td>
                <td>${escapeHtml(r.personCode)}</td>
                <td>${escapeHtml(r.name)}</td>
                <td>${escapeHtml(maskIdCardClient(r.idCard))}</td>
                <td>${escapeHtml(r.gender || "")}</td>
                <td>${escapeHtml(r.birthYearMonth || "")}</td>
                <td>${escapeHtml(r.personnelCategory || "")}</td>
                <td>${escapeHtml(r.currentJob || "")}</td>
                <td>${escapeHtml(r.currentGrade || "")}</td>
            </tr>
        `).join("");
        if (!(status.textContent || "").includes("未选单位")) {
            status.textContent = `查询完成，共 ${rows.length} 条；勾选后可下载 CSV 或生成人员包。`;
        }
    } catch (error) {
        showError(status, error);
    }
}

async function downloadPersonnelCsv() {
    const organizationCode = selectedOrganizationCode("data-exchange-personnel-organization");
    const keyword = document.getElementById("data-exchange-personnel-keyword").value;
    const status = document.getElementById("data-exchange-personnel-status");

    status.className = "status";
    status.textContent = "正在下载...";

    try {
        const response = await fetch(`/api/data-exchange/personnel/download?organizationCode=${encodeURIComponent(organizationCode)}&keyword=${encodeURIComponent(keyword)}`, {
            headers: { Accept: "text/csv" }
        });
        if (!response.ok) {
            throw new Error(`HTTP ${response.status}`);
        }
        const blob = await response.blob();
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement("a");
        a.href = url;
        a.download = `personnel_export_${new Date().toISOString().slice(0, 10)}.csv`;
        document.body.appendChild(a);
        a.click();
        window.URL.revokeObjectURL(url);
        document.body.removeChild(a);
        status.textContent = "CSV 下载完成";
    } catch (error) {
        showError(status, error);
    }
}

async function downloadPersonnelPackage() {
    const status = document.getElementById("data-exchange-personnel-status");
    status.className = "status";
    const organizationCode = selectedOrganizationCode("data-exchange-personnel-organization");
    const selectedKeys = selectedExchangeKeys("[data-personnel-export-select]:checked");
    const previewRows = state.dataExchangePersonnelRows || [];

    if (!selectedKeys.length && !organizationCode) {
        status.className = "status error";
        status.textContent = "请先选择单位并查询，或勾选要导出的人员。";
        return;
    }
    if (!selectedKeys.length && !previewRows.length) {
        status.className = "status error";
        status.textContent = "请先查询预览人员，或勾选要导出的人员。";
        return;
    }
    if (!selectedKeys.length
            && previewRows.length > 800
            && !confirm(`当前筛选约 ${previewRows.length} 人，超过单次建议上限 800。\n请优先勾选分批导出；仍要全部生成可能较慢或失败，是否继续？`)) {
        return;
    }
    if (!selectedKeys.length
            && previewRows.length <= 800
            && !confirm(`未勾选人员，将按当前条件导出全部人员（约 ${previewRows.length || "全部"} 人），是否继续？`)) {
        return;
    }

    status.textContent = "正在生成人员包...";
    try {
        const keyword = document.getElementById("data-exchange-personnel-keyword").value.trim();
        const includeDescendants = document.getElementById("data-exchange-personnel-include-descendants")?.checked ?? true;
        const payload = {
            organizationCodes: organizationCode ? [organizationCode] : [],
            includeDescendants,
            keyword: keyword || null,
            selectedPersonnel: selectedKeys,
        };
        const response = await fetch("/api/data-exchange/dispatch/personnel", {
            method: "POST",
            credentials: "same-origin",
            headers: {
                Accept: "application/json",
                "Content-Type": "application/json",
            },
            body: JSON.stringify(payload),
        });
        await ensureAuthenticatedApiResponse(response, "生成人员包失败");
        if (!response.ok) {
            throw new Error(await readErrorMessage(response));
        }
        const blob = await response.blob();
        let filename = `rsgzgl_personnel_package_${new Date().toISOString().slice(0, 10)}.json`;
        const disposition = response.headers.get("content-disposition") || "";
        const utfMatch = disposition.match(/filename\*=UTF-8''([^;]+)/i);
        const plainMatch = disposition.match(/filename=\"?([^\";]+)\"?/i);
        if (utfMatch) {
            filename = decodeURIComponent(utfMatch[1]);
        } else if (plainMatch) {
            filename = plainMatch[1];
        }
        const url = URL.createObjectURL(blob);
        const a = document.createElement("a");
        a.href = url;
        a.download = filename;
        document.body.appendChild(a);
        a.click();
        a.remove();
        URL.revokeObjectURL(url);
        const countHint = selectedKeys.length || previewRows.length || "";
        status.textContent = `人员包已生成${countHint ? `（约 ${countHint} 人）` : ""}`;
    } catch (error) {
        showError(status, error);
    }
}

async function onDataExchangePersonnelImportFileSelected(event) {
    const file = event.target.files && event.target.files[0];
    const status = document.getElementById("data-exchange-personnel-import-status");
    if (!file) {
        document.getElementById("data-exchange-personnel-import-json").value = "";
        return;
    }
    try {
        const text = await file.text();
        JSON.parse(text);
        document.getElementById("data-exchange-personnel-import-json").value = text;
        status.className = "status";
        status.textContent = `已选择人员包：${file.name}，可先预览；确认导入前需选择目标单位。`;
    } catch (error) {
        document.getElementById("data-exchange-personnel-import-json").value = "";
        status.className = "status error";
        status.textContent = `人员包文件格式错误：${error.message}`;
    }
}

async function onDataExchangePersonnelImportPreview(event) {
    event.preventDefault();
    await previewDataExchangePersonnelImport();
}

async function previewDataExchangePersonnelImport() {
    const status = document.getElementById("data-exchange-personnel-import-status");
    const rows = document.getElementById("data-exchange-personnel-import-rows");
    const summary = document.getElementById("data-exchange-personnel-import-summary");
    const selectAll = document.getElementById("data-exchange-personnel-import-select-all");
    status.className = "status";
    status.textContent = "正在解析人员包...";
    rows.innerHTML = "";
    summary.classList.add("hidden");
    summary.innerHTML = "";
    if (selectAll) {
        selectAll.checked = false;
    }
    try {
        const packageJson = document.getElementById("data-exchange-personnel-import-json").value;
        if (!packageJson) {
            throw new Error("请先选择人员包文件。");
        }
        const targetOrganizationCode = selectedOrganizationCode("data-exchange-personnel-import-target-organization");
        const appendMode = Boolean(targetOrganizationCode);
        const result = await postJson("/api/data-exchange/receive/preview", {
            packageJson,
            mode: appendMode ? "APPEND" : "REPLACE",
            targetOrganizationCode: targetOrganizationCode || null,
            selectedPersonnel: [],
        });
        state.dataExchangePersonnelImportRows = result.rows || [];
        const previewRows = result.previewRows || [];
        rows.innerHTML = state.dataExchangePersonnelImportRows.map(row => {
            const preview = previewRows.find(item => item.organizationCode === row.organizationCode && item.personCode === row.personCode) || {};
            return `
            <tr>
                <td><input type="checkbox" data-personnel-import-select value="${escapeHtml(row.organizationCode)}|${escapeHtml(row.personCode)}" checked></td>
                <td>${escapeHtml(row.organizationName || row.organizationCode)}</td>
                <td>${escapeHtml(row.personCode)}</td>
                <td>${escapeHtml(row.name)}</td>
                <td>${escapeHtml(maskIdCardClient(row.idCard))}</td>
                <td>${escapeHtml(row.currentJob || "")}</td>
                <td>${escapeHtml(row.currentGrade || "")}</td>
                <td>${escapeHtml(preview.action || "")}</td>
                <td>${escapeHtml(preview.targetOrganizationCode || "")}</td>
                <td>${escapeHtml(preview.targetPersonCode || "")}</td>
                <td>${preview.targetOrganizationExists === false ? "否" : "是"}</td>
                <td>${formatTableCounts(preview.relatedCounts || [])}</td>
            </tr>
        `;
        }).join("");
        if (selectAll) {
            selectAll.checked = true;
        }
        summary.innerHTML = renderReceiveSummary(result.summary);
        summary.classList.remove("hidden");
        const hint = appendMode
            ? ""
            : "（未选导入单位：仅预览包内容；确认导入前请选择目标单位）";
        status.textContent = `${result.message || `预览 ${result.totalRecords} 人`}${hint}`;
    } catch (error) {
        showError(status, error);
    }
}

async function confirmDataExchangePersonnelImport() {
    const status = document.getElementById("data-exchange-personnel-import-status");
    status.className = "status";
    const targetOrganizationCode = selectedOrganizationCode("data-exchange-personnel-import-target-organization");
    if (!targetOrganizationCode) {
        status.className = "status error";
        status.textContent = "请选择导入到单位。";
        return;
    }
    const allBoxes = document.querySelectorAll("[data-personnel-import-select]");
    const checkedBoxes = document.querySelectorAll("[data-personnel-import-select]:checked");
    if (!allBoxes.length) {
        status.className = "status error";
        status.textContent = "请先预览人员包。";
        return;
    }
    if (!checkedBoxes.length) {
        status.className = "status error";
        status.textContent = "请勾选要导入的人员。";
        return;
    }
    const importAll = checkedBoxes.length === allBoxes.length;
    const selected = importAll ? [] : selectedExchangeKeys("[data-personnel-import-select]:checked");
    const count = importAll
        ? (state.dataExchangePersonnelImportRows || []).length || allBoxes.length
        : selected.length;
    const targetLabel = document.getElementById("data-exchange-personnel-import-target-organization").value
        || targetOrganizationCode;
    if (!window.confirm(`将追加 ${count} 人到单位 ${targetLabel}，并重新生成个人编码，是否继续？`)) {
        return;
    }
    status.textContent = "正在导入人员...";
    try {
        const packageJson = document.getElementById("data-exchange-personnel-import-json").value;
        if (!packageJson) {
            throw new Error("请先选择人员包文件。");
        }
        const result = await postJson("/api/data-exchange/receive/apply", {
            packageJson,
            mode: "APPEND",
            targetOrganizationCode,
            selectedPersonnel: selected,
            dryRun: false,
        });
        const mappingText = (result.codeMappings || []).map(item =>
            `${escapeHtml(item.name)}：${escapeHtml(item.sourceOrganizationCode)}-${escapeHtml(item.sourcePersonCode)} -> ${escapeHtml(item.targetOrganizationCode)}-${escapeHtml(item.targetPersonCode)}`
        ).join("；");
        status.textContent = `${result.message || `已导入 ${result.receivedRecords} 人`}。追加 ${result.appendedRecords || 0}${mappingText ? "。编码映射：" + mappingText : ""}`;
    } catch (error) {
        showError(status, error);
    }
}

async function onDataExchangeAnnualSearch(event) {
    event.preventDefault();
    const organizationCode = selectedOrganizationCode("data-exchange-annual-organization");
    const period = document.getElementById("data-exchange-annual-period").value;
    const keyword = document.getElementById("data-exchange-annual-keyword").value;
    const status = document.getElementById("data-exchange-annual-status");
    const tbody = document.getElementById("data-exchange-annual-rows");

    status.className = "status";
    status.textContent = "正在查询...";

    try {
        const data = await getJson(`/api/data-exchange/annual-report?organizationCode=${encodeURIComponent(organizationCode)}&period=${encodeURIComponent(period)}&keyword=${encodeURIComponent(keyword)}&page=0&size=50`);
        tbody.innerHTML = data.content.map(r => `
            <tr>
                <td>${escapeHtml(r.organizationName || r.organizationCode)}</td>
                <td>${escapeHtml(r.personCode)}</td>
                <td>${escapeHtml(r.name)}</td>
                <td>${escapeHtml(r.period)}</td>
                <td>${escapeHtml(r.changeType)}</td>
                <td>${escapeHtml(r.currentPosition)}</td>
                <td>${money(r.positionSalary)}</td>
                <td>${money(r.gradeSalary)}</td>
                <td>${money(r.total)}</td>
            </tr>
        `).join("");
        status.textContent = `查询完成，共 ${data.totalElements} 条记录`;
    } catch (error) {
        showError(status, error);
    }
}

async function downloadAnnualCsv() {
    const organizationCode = selectedOrganizationCode("data-exchange-annual-organization");
    const period = document.getElementById("data-exchange-annual-period").value;
    const keyword = document.getElementById("data-exchange-annual-keyword").value;
    const status = document.getElementById("data-exchange-annual-status");

    status.className = "status";
    status.textContent = "正在下载...";

    try {
        const response = await fetch(`/api/data-exchange/annual-report/download?organizationCode=${encodeURIComponent(organizationCode)}&period=${encodeURIComponent(period)}&keyword=${encodeURIComponent(keyword)}`, {
            headers: { Accept: "text/csv" }
        });
        if (!response.ok) {
            throw new Error(`HTTP ${response.status}`);
        }
        const blob = await response.blob();
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement("a");
        a.href = url;
        a.download = `annual_report_${period || new Date().toISOString().slice(0, 10)}.csv`;
        document.body.appendChild(a);
        a.click();
        window.URL.revokeObjectURL(url);
        document.body.removeChild(a);
        status.textContent = "下载完成";
    } catch (error) {
        showError(status, error);
    }
}

async function downloadAnnualExcel() {
    const organizationCode = selectedOrganizationCode("data-exchange-annual-organization");
    const period = document.getElementById("data-exchange-annual-period").value;
    const keyword = document.getElementById("data-exchange-annual-keyword").value;
    const status = document.getElementById("data-exchange-annual-status");

    status.className = "status";
    status.textContent = "正在生成固定格式 Excel...";

    try {
        const response = await fetch(`/api/data-exchange/annual-report/excel?organizationCode=${encodeURIComponent(organizationCode)}&period=${encodeURIComponent(period)}&keyword=${encodeURIComponent(keyword)}`, {
            headers: { Accept: "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet" }
        });
        if (!response.ok) {
            throw new Error(await response.text() || `HTTP ${response.status}`);
        }
        const blob = await response.blob();
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement("a");
        a.href = url;
        a.download = `annual_report_${period || new Date().toISOString().slice(0, 10)}.xlsx`;
        document.body.appendChild(a);
        a.click();
        window.URL.revokeObjectURL(url);
        document.body.removeChild(a);
        status.textContent = "固定格式 Excel 已生成";
    } catch (error) {
        showError(status, error);
    }
}
