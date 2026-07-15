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
    dataExchangeDispatchOrganizations: [],
    dataExchangeDispatchRows: [],
    dataExchangeSubmissionOrganizations: [],
    dataExchangeSubmissionRows: [],
    dataExchangeSubmissionPayrollTables: [],
    dataExchangeApprovalOrganizations: [],
    dataExchangeApprovalRows: [],
    dataExchangeApprovalPayrollTables: [],
    dataExchangeReceiveRows: [],
    assessmentBatchRows: [],
    assessmentBatchMeta: null,
    activePanelId: null,
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
            ["studyYears", "学制", "number"], ["educationType", "学历类别"], ["remark", "备注"],
        ],
    },
    position: {
        title: "职务变化信息",
        wideModal: true,
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
    "SALARY_STANDARD_ADJUSTMENT",
    "BASIC_SALARY_STANDARD_ADJUSTMENT",
    "CIVIL_ALLOWANCE_STANDARD_ADJUSTMENT",
    "PERFORMANCE_STANDARD_ADJUSTMENT",
    "PERFORMANCE_RATIO_ADJUSTMENT",
];

const menuGroups = [
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
                    "PAYROLL", "PAYROLL_HISTORY", "TEACHING_ALLOWANCE_ADJUSTMENT",
                    "NORMAL_PROMOTION", "LEVEL_PROMOTION", "POSITION_CHANGE_PROMOTION",
                    "EDUCATION_PROMOTION", "REGULARIZATION", "FLOATING_TO_FIXED", "INTERN_SALARY_CHANGE",
                    "NEW_PERSONNEL_SALARY", "OTHER_PAYROLL_CHANGE", "ALLOWANCE_RECALCULATION",
                    "REFORM_LEVEL_ROLLING", "REGULARIZATION_HIGH_GRADE", "MONTHLY_AVERAGE_SALARY",
                    "WAGE_REFORM_2006", "AUDIT",
                ],
            },
            { title: "调整标准", codes: standardAdjustmentMenuCodes },
            { title: "警衔法检监", codes: rankAllowanceMenuCodes },
        ],
    },
    {
        title: "标准维护",
        codes: [
            "BASIC_STANDARDS", "INTERN_SALARY_STANDARDS", "ALLOWANCE_STANDARDS",
            "RANK_ALLOWANCE_STANDARDS", "PROSECUTION_ALLOWANCE_STANDARDS", "JUDICIAL_ALLOWANCE_STANDARDS",
            "RETAINED_ALLOWANCE_STANDARDS", "YEAR_ALLOWANCE_STANDARDS", "RURAL_TEACHER_ALLOWANCE_STANDARDS",
            "WAGE_REFORM_STANDARDS", "OTHER_ALLOWANCE_STANDARDS",
        ],
    },
    {
        title: "报表查询",
        codes: [
            "PAYROLL_CHANGE_REGISTER_REPORT", "PAYROLL_CHANGE_APPROVAL_REPORT",
            "WAGE_REFORM_2006_PUBLIC_NOTICE_REPORT", "PERSONNEL_INFORMATION_COLLECTION_REPORT",
            "PERSONNEL_INFORMATION_REGISTRATION_REPORT", "DATA_EXCHANGE",
        ],
    },
    {
        title: "系统管理",
        codes: [
            "LOCAL_POLICY_CONFIG", "DICTIONARY_MAINTENANCE", "SECURITY",
            "OPERATION_LOG", "DATA_MAINTENANCE", "SYSTEM_HELP", "SYSTEM_SETUP",
        ],
    },
];

function menuGroupCodes(group) {
    if (group.sections?.length) {
        return group.sections.flatMap(section => section.codes);
    }
    return group.codes || [];
}

function findMenuGroupByCode(code) {
    return menuGroups.find(group => menuGroupCodes(group).includes(code));
}

function renderMenuLinks(codes, menuByCode) {
    return codes
        .map(menuCode => menuByCode.get(menuCode))
        .filter(Boolean)
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
    { code: "PERSONNEL", label: "人员管理", desc: "查询、维护及附属信息管理" },
    { code: "PAYROLL_HISTORY", label: "工资变动历史", desc: "查看历次调资与变动记录" },
    { code: "POSITION_CHANGE_PROMOTION", label: "职务变化晋升", desc: "筛选待处理职务变动人员" },
    { code: "AUDIT", label: "工资推算对账", desc: "批量重放推算并比对差异" },
    { code: "PAYROLL_CHANGE_REGISTER_REPORT", label: "工资变动花名册", desc: "生成与打印变动花名册" },
    { code: "BASIC_STANDARDS", label: "基本工资标准", desc: "查询职务与级别工资标准" },
    { code: "SECURITY", label: "权限管理", desc: "维护用户、角色与单位范围" },
];

const menuDescriptions = {
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
    POLICE_RANK_CHANGE_PROMOTION: "警衔工资晋升试算与处理",
    PROSECUTION_RANK_CHANGE_PROMOTION: "检察官等级变化晋升",
    JUDICIAL_RANK_CHANGE_PROMOTION: "法官等级变化晋升",
    SUPERVISION_RANK_CHANGE_PROMOTION: "监察等级变化晋升",
    FLOATING_TO_FIXED: "浮动转固定试算",
    INTERN_SALARY_CHANGE: "见习工资变动试算",
    SALARY_STANDARD_ADJUSTMENT: "2024.07 调标试算",
    BASIC_SALARY_STANDARD_ADJUSTMENT: "调整基本工资标准试算",
    PERFORMANCE_RATIO_ADJUSTMENT: "调整绩效比例试算",
    NORMAL_PROMOTION: "正常档次/薪级晋升试算",
    LEVEL_PROMOTION: "级别晋升与套改滚动试算",
    POSITION_CHANGE_PROMOTION: "职务变化晋升试算与处理",
    EDUCATION_PROMOTION: "学历晋升定级试算",
    REGULARIZATION: "见习人员转正定级试算",
    AUDIT: "逐人工资推算对账与导出",
    BASIC_STANDARDS: "职务工资与级别工资标准",
    ALLOWANCE_STANDARDS: "津补贴标准维护",
    INTERN_SALARY_STANDARDS: "见习期工资标准",
    RANK_ALLOWANCE_STANDARDS: "警衔津贴标准",
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
    initializeAuth();
    initGradeStandardStepsGrid();
    document.getElementById("personnel-search").addEventListener("submit", onPersonnelSearch);
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
    document.getElementById("assessment-tab-entry").addEventListener("click", () => switchAssessmentTab("entry"));
    document.getElementById("assessment-tab-summary").addEventListener("click", () => switchAssessmentTab("summary"));
    document.getElementById("changed-personnel-form").addEventListener("submit", onChangedPersonnelSearch);
    document.getElementById("position-history-form").addEventListener("submit", onPositionHistorySearch);
    document.getElementById("education-history-form").addEventListener("submit", onEducationHistorySearch);
    document.getElementById("organization-maintenance-form").addEventListener("submit", onOrganizationMaintenanceSearch);
    document.getElementById("personnel-statistics-form").addEventListener("submit", onPersonnelStatisticsSearch);
    document.getElementById("dictionary-maintenance-form").addEventListener("submit", onDictionarySearch);
    document.getElementById("local-policy-form").addEventListener("submit", onLocalPolicySearch);
    document.getElementById("audit-form").addEventListener("submit", onAudit);
    document.getElementById("audit-detail-close").addEventListener("click", closeAuditDetail);
    document.getElementById("audit-export-excel").addEventListener("click", () => downloadProjectionAuditExport("xlsx"));
    document.getElementById("audit-export-csv").addEventListener("click", () => downloadProjectionAuditExport("csv"));
    document.getElementById("payroll-change-register-report-form").addEventListener("submit", onPayrollChangeRegisterReportSearch);
    document.getElementById("payroll-change-register-print").addEventListener("click", printSelectedPayrollChangeRegisterPdf);
    document.getElementById("payroll-change-register-export-excel").addEventListener("click", exportSelectedPayrollChangeRegisterExcel);
    document.getElementById("report-payroll-change-generate-selected").addEventListener("click", generateSelectedPayrollChangeRegister);
    document.getElementById("report-payroll-change-select-all").addEventListener("change", event => {
        document.querySelectorAll("[data-register-select]").forEach(checkbox => checkbox.checked = event.target.checked);
    });
    document.getElementById("payroll-change-approval-report-form").addEventListener("submit", onPayrollChangeApprovalReportSearch);
    document.getElementById("payroll-change-approval-print").addEventListener("click", generateAndPrintSelectedPayrollChangeApprovals);
    document.getElementById("payroll-change-approval-export-excel").addEventListener("click", exportSelectedPayrollChangeApprovalsExcel);
    document.getElementById("report-approval-select-all").addEventListener("change", event => {
        document.querySelectorAll("[data-approval-select]").forEach(checkbox => checkbox.checked = event.target.checked);
    });
    document.getElementById("data-exchange-personnel-form").addEventListener("submit", onDataExchangePersonnelSearch);
    document.getElementById("data-exchange-personnel-download").addEventListener("click", downloadPersonnelCsv);
    document.getElementById("data-exchange-annual-form").addEventListener("submit", onDataExchangeAnnualSearch);
    document.getElementById("data-exchange-annual-download").addEventListener("click", downloadAnnualCsv);
    document.getElementById("data-exchange-annual-excel-download").addEventListener("click", downloadAnnualExcel);
    document.getElementById("data-exchange-dispatch-form").addEventListener("submit", onDataExchangeDispatchSearch);
    document.getElementById("data-exchange-dispatch-download").addEventListener("click", downloadDispatchPackage);
    document.getElementById("data-exchange-dispatch-select-all").addEventListener("change", event => {
        document.querySelectorAll("[data-dispatch-select]").forEach(checkbox => checkbox.checked = event.target.checked);
    });
    document.getElementById("data-exchange-receive-file").addEventListener("change", onDataExchangeReceiveFileSelected);
    document.getElementById("data-exchange-receive-form").addEventListener("submit", onDataExchangeReceivePreview);
    document.getElementById("data-exchange-receive-dry-run").addEventListener("click", applyDataExchangeReceiveDryRun);
    document.getElementById("data-exchange-receive-all").addEventListener("click", () => applyDataExchangeReceive("REPLACE"));
    document.getElementById("data-exchange-receive-selected").addEventListener("click", () => applyDataExchangeReceive("APPEND"));
    document.getElementById("data-exchange-receive-select-all").addEventListener("change", event => {
        document.querySelectorAll("[data-receive-select]").forEach(checkbox => checkbox.checked = event.target.checked);
    });
    document.getElementById("data-exchange-submission-export-form").addEventListener("submit", onDataExchangeSubmissionSearch);
    document.getElementById("data-exchange-submission-download").addEventListener("click", downloadSubmissionPackage);
    document.getElementById("data-exchange-submission-select-all").addEventListener("change", event => {
        document.querySelectorAll("[data-submission-select]").forEach(checkbox => checkbox.checked = event.target.checked);
    });
    document.getElementById("data-exchange-submission-review-file").addEventListener("change", onDataExchangeSubmissionReviewFileSelected);
    document.getElementById("data-exchange-submission-review-form").addEventListener("submit", onDataExchangeSubmissionReviewPreview);
    document.getElementById("data-exchange-submission-review-dry-run").addEventListener("click", () => applyDataExchangeSubmissionReview("APPROVE", true));
    document.getElementById("data-exchange-submission-review-approve").addEventListener("click", () => applyDataExchangeSubmissionReview("APPROVE", false));
    document.getElementById("data-exchange-submission-review-reject").addEventListener("click", () => applyDataExchangeSubmissionReview("REJECT", false));
    document.getElementById("data-exchange-submission-review-select-all").addEventListener("change", event => {
        document.querySelectorAll("[data-submission-review-select]").forEach(checkbox => checkbox.checked = event.target.checked);
    });
    document.getElementById("data-exchange-approval-dispatch-form").addEventListener("submit", onDataExchangeApprovalSearch);
    document.getElementById("data-exchange-approval-download").addEventListener("click", downloadApprovalPackage);
    document.getElementById("data-exchange-approval-select-all").addEventListener("change", event => {
        document.querySelectorAll("[data-approval-select]").forEach(checkbox => checkbox.checked = event.target.checked);
    });
    document.getElementById("data-exchange-approval-receive-file").addEventListener("change", onDataExchangeApprovalReceiveFileSelected);
    document.getElementById("data-exchange-approval-receive-form").addEventListener("submit", onDataExchangeApprovalReceivePreview);
    document.getElementById("data-exchange-approval-receive-dry-run").addEventListener("click", () => applyDataExchangeApprovalReceive(true));
    document.getElementById("data-exchange-approval-receive-apply").addEventListener("click", () => applyDataExchangeApprovalReceive(false));
    document.getElementById("data-exchange-approval-receive-select-all").addEventListener("change", event => {
        document.querySelectorAll("[data-approval-receive-select]").forEach(checkbox => checkbox.checked = event.target.checked);
    });
    document.querySelectorAll("[data-exchange-tab]").forEach(button => {
        button.addEventListener("click", () => showDataExchangeTab(button.dataset.exchangeTab));
    });
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
    document.getElementById("level-promotion-batch-apply").addEventListener("click", applySelectedLevelPromotions);
    document.getElementById("level-promotion-select-all").addEventListener("change", event => {
        document.querySelectorAll("[data-level-select]").forEach(checkbox => {
            checkbox.checked = event.target.checked;
        });
    });
    document.getElementById("position-change-promotion-form").addEventListener("submit", onPositionChangePromotionSearch);
    document.getElementById("position-change-batch-apply").addEventListener("click", applySelectedPositionChanges);
    document.getElementById("position-change-select-all").addEventListener("change", event => {
        document.querySelectorAll("[data-position-change-select]").forEach(checkbox => {
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
    document.getElementById("regularization-form").addEventListener("submit", onRegularizationSearch);
    document.getElementById("floating-to-fixed-form").addEventListener("submit", onFloatingToFixedSearch);
    document.getElementById("intern-salary-change-form").addEventListener("submit", onInternSalaryChangeSearch);
    document.getElementById("basic-standards-form").addEventListener("submit", onBasicStandardsSearch);
    document.getElementById("basic-standard-type").addEventListener("change", updateBasicStandardCreateButton);
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
    document.getElementById("rank-allowance-standards-form").addEventListener("submit", onRankAllowanceStandardsSearch);
    document.getElementById("rank-standard-create").addEventListener("click", createRankAllowanceStandard);
    document.getElementById("retained-allowance-standards-form").addEventListener("submit", onRetainedAllowanceStandardsSearch);
    document.getElementById("retained-standard-create").addEventListener("click", createRetainedAllowanceStandard);
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
    initializeNormalPromotionPage();
    initializeLevelPromotionPage();
});

async function initializeAuth() {
    try {
        const user = await getJson("/api/auth/me");
        const menus = await getJson("/api/auth/menus");
        state.currentUser = user;
        state.menus = menus;
        document.getElementById("current-user").textContent = `${user.displayName} (${user.username})`;
        renderMenus();
        updateStandardWriteUi();
        renderDashboard();
        applyRoute();
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
    const message = String(error?.message || error || "");
    return message.includes("需要登录") || message.includes("登录已失效");
}

function renderMenus() {
    const nav = document.getElementById("main-nav");
    const menuByCode = new Map([{ code: "DASHBOARD", title: "工作台", path: "#dashboard", permissionCode: "" }, ...state.menus].map(menu => [menu.code, menu]));
    nav.innerHTML = menuGroups.map(group => {
        const links = renderMenuGroupContent(group, menuByCode);
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

function hasPersonnelWrite() {
    return hasPermission("PERSONNEL_WRITE");
}

function hasPersonnelAccess() {
    return hasMenu("PERSONNEL") || hasPersonnelWrite();
}

function hasPayrollRead() {
    return hasPermission("PAYROLL_READ");
}

function hasSystemConfigWrite() {
    return hasPermission("SYSTEM_CONFIG");
}

function hasOrgWrite() {
    return hasPermission("ORG_WRITE");
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
    let requestedHash = window.location.hash || "#dashboard";
    if (requestedHash === "#annual-assessments" || requestedHash === "#annual-assessment-batch") {
        requestedHash = "#annual-assessment-management";
    }
    const availableMenus = [{ code: "DASHBOARD", title: "工作台", path: "#dashboard" }, ...state.menus];
    const selectedMenu = availableMenus.find(menu => menu.path === requestedHash) || availableMenus[0];
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
    if (selectedId === "normal-promotion") {
        initializeNormalPromotionPage();
    }
    if (selectedId === "level-promotion") {
        initializeLevelPromotionPage();
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
}

function menuGroupTitle(code) {
    const group = findMenuGroupByCode(code);
    return group ? group.title : "工作台";
}

function renderDashboard() {
    const user = state.currentUser;
    const greetingEl = document.getElementById("dashboard-greeting");
    if (greetingEl) {
        greetingEl.textContent = dashboardGreeting();
    }
    const nameEl = document.getElementById("dashboard-user-name");
    if (nameEl) {
        nameEl.textContent = user?.displayName || user?.username || "工作台";
    }
    const scopeEl = document.getElementById("dashboard-scope");
    if (scopeEl) {
        scopeEl.textContent = dashboardScopeText(user);
    }
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
    const totalEl = document.getElementById("dashboard-total-functions");
    if (totalEl) {
        totalEl.textContent = `共 ${state.menus.length} 项可用功能`;
    }
    const menuByCode = new Map(state.menus.map(menu => [menu.code, menu]));
    const groupCounts = {
        "信息维护": 0,
        "工资变动": 0,
        "标准维护": 0,
        "报表查询": 0,
        "系统管理": 0,
    };
    state.menus.forEach(menu => {
        const group = menuGroupTitle(menu.code);
        if (groupCounts[group] != null) {
            groupCounts[group] += 1;
        }
    });
    const counts = {
        "dashboard-personnel-count": groupCounts["信息维护"],
        "dashboard-payroll-count": groupCounts["工资变动"],
        "dashboard-standard-count": groupCounts["标准维护"],
        "dashboard-report-count": groupCounts["报表查询"],
        "dashboard-system-count": groupCounts["系统管理"],
    };
    Object.entries(counts).forEach(([id, value]) => {
        const el = document.getElementById(id);
        if (el) {
            el.textContent = value;
        }
    });
    const cardTargets = {
        "dashboard-card-personnel": firstMenuPath(menuByCode, menuGroupCodes(menuGroups.find(g => g.title === "信息维护") || {})),
        "dashboard-card-payroll": firstMenuPath(menuByCode, menuGroupCodes(menuGroups.find(g => g.title === "工资变动") || {})),
        "dashboard-card-standard": firstMenuPath(menuByCode, menuGroupCodes(menuGroups.find(g => g.title === "标准维护") || {})),
        "dashboard-card-report": firstMenuPath(menuByCode, menuGroupCodes(menuGroups.find(g => g.title === "报表查询") || {})),
        "dashboard-card-system": firstMenuPath(menuByCode, menuGroupCodes(menuGroups.find(g => g.title === "系统管理") || {})),
    };
    Object.entries(cardTargets).forEach(([id, path]) => {
        const card = document.getElementById(id);
        if (card && path) {
            card.href = path;
            card.classList.remove("dashboard-card-disabled");
        } else if (card) {
            card.href = "#dashboard";
            card.classList.add("dashboard-card-disabled");
        }
    });
    const quickLinks = document.getElementById("dashboard-quick-links");
    if (quickLinks) {
        const actions = dashboardQuickActions
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
            : `<div class="dashboard-empty-hint">当前账号暂无快捷入口，请联系管理员分配权限。</div>`;
    }
    const groupsContainer = document.getElementById("dashboard-group-links");
    if (groupsContainer) {
        groupsContainer.innerHTML = menuGroups
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
            })
            .join("");
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
    if (!hasPersonnelWrite()) {
        return;
    }
    initializeOrganizationPickerInput();
    try {
        await loadDictionaryFieldConfigs();
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
                button.addEventListener("click", () => openDictionaryPicker(inputId, {
                    ...config,
                    dictionaryFieldKey: fieldName,
                }));
                combo.appendChild(button);
            }
        });
    } catch (error) {
        console.warn("字典字段配置加载失败", error);
    }
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
    const pickerConfigs = [
        { inputId: "maint-organization-name", buttonId: "maint-organization-picker-button", target: "maintenance" },
        { inputId: "data-exchange-dispatch-organization", buttonId: "data-exchange-dispatch-organization-picker-button", target: "dataExchangeDispatch" },
        { inputId: "data-exchange-submission-organization", buttonId: "data-exchange-submission-organization-picker-button", target: "dataExchangeSubmission" },
        { inputId: "data-exchange-approval-organization", buttonId: "data-exchange-approval-organization-picker-button", target: "dataExchangeApproval" },
        { inputId: "organization-code", buttonId: "organization-code-picker-button" },
        { inputId: "personnel-statistics-organization-code", buttonId: "personnel-statistics-organization-picker-button" },
        { inputId: "assessment-batch-organization-code", buttonId: "assessment-batch-organization-picker-button" },
        { inputId: "assessment-summary-organization-code", buttonId: "assessment-summary-organization-picker-button" },
        { inputId: "changed-personnel-organization-code", buttonId: "changed-personnel-organization-picker-button" },
        { inputId: "position-history-organization-code", buttonId: "position-history-organization-picker-button" },
        { inputId: "education-history-organization-code", buttonId: "education-history-organization-picker-button" },
        { inputId: "payroll-history-organization-code", buttonId: "payroll-history-organization-picker-button" },
        { inputId: "teaching-allowance-organization-code", buttonId: "teaching-allowance-organization-picker-button" },
        { inputId: "normal-promotion-organization-code", buttonId: "normal-promotion-organization-picker-button" },
        { inputId: "level-promotion-organization-code", buttonId: "level-promotion-organization-picker-button" },
        { inputId: "position-change-organization-code", buttonId: "position-change-organization-picker-button" },
        { inputId: "education-promotion-organization-code", buttonId: "education-promotion-organization-picker-button" },
        { inputId: "regularization-organization-code", buttonId: "regularization-organization-picker-button" },
        { inputId: "floating-to-fixed-organization-code", buttonId: "floating-to-fixed-organization-picker-button" },
        { inputId: "intern-salary-change-organization-code", buttonId: "intern-salary-change-organization-picker-button" },
        { inputId: "audit-organization-code", buttonId: "audit-organization-picker-button" },
        { inputId: "report-payroll-change-organization-code", buttonId: "report-payroll-change-organization-picker-button" },
        { inputId: "report-approval-organization-code", buttonId: "report-approval-organization-picker-button" },
        { inputId: "data-exchange-receive-target-organization", buttonId: "data-exchange-receive-target-picker-button" },
        { inputId: "data-exchange-personnel-organization", buttonId: "data-exchange-personnel-organization-picker-button" },
        { inputId: "data-exchange-annual-organization", buttonId: "data-exchange-annual-organization-picker-button" },
    ];
    pickerConfigs.forEach(config => bindOrganizationPickerInput(config.inputId, config.buttonId, config.target || config.inputId));
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
                    : "从单位树中选择单位，支持按单位名称或编码搜索；留空表示不限单位。";
    document.getElementById("organization-picker-filter").value = "";
    document.getElementById("organization-picker-tree").innerHTML = "正在加载单位...";
    modal.classList.remove("hidden");
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
    const input = document.getElementById(target);
    if (input) {
        input.value = name || code;
        input.dataset.organizationCode = code;
        input.title = name ? `${name} (${code})` : code;
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
        state.dictionaryExpandedCodes = new Set(rootDictionaryNodes(state.activeDictionaryNodes).map(node => node.code));
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
        }
    } else if (config.linkedCodeInputId) {
        const linkedInput = document.getElementById(config.linkedCodeInputId);
        if (linkedInput) {
            linkedInput.value = code || node.value || "";
        }
    }
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
    ["basic", "projection", "education", "position", "current-payroll", "payroll", "assessment", "award", "rank-level", "wage-reform", "pre-reform"].forEach(name => {
        document.getElementById(`personnel-tab-${name}`).classList.toggle("hidden", name !== tabName);
    });
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
    if (summaryOrg && batchOrg?.value.trim()) {
        summaryOrg.value = batchOrg.value.trim();
        summaryOrg.dataset.displayName = batchOrg.dataset.displayName || "";
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
    }
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

async function onPersonnelStatisticsSearch(event) {
    event.preventDefault();
    await loadPersonnelStatistics();
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

function initializeNormalPromotionPage() {
    const yearInput = document.getElementById("normal-promotion-year");
    if (yearInput && !yearInput.value) {
        yearInput.value = String(new Date().getFullYear());
    }
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
    return `year=${encodeURIComponent(currentNormalPromotionYear())}`;
}

function initializeLevelPromotionPage() {
    const yearInput = document.getElementById("level-promotion-year");
    if (yearInput && !yearInput.value) {
        yearInput.value = String(new Date().getFullYear());
    }
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
    return `year=${encodeURIComponent(currentLevelPromotionYear())}`;
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
    const organizationCode = selectedOrganizationCode("organization-code");
    const keyword = document.getElementById("keyword").value.trim();
    const size = document.getElementById("page-size").value || "20";
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
        rows.innerHTML = (page.content || []).map(person => `
            <tr>
                <td>${escapeHtml(person.uid)}</td>
                <td>${escapeHtml(person.organizationCode)} ${escapeHtml(person.organizationName || "")}</td>
                <td>${escapeHtml(person.personCode)}</td>
                <td>${escapeHtml(person.name)}</td>
                <td>${escapeHtml(person.idCard || "")}</td>
                <td>${escapeHtml(person.currentPosition || "")}</td>
                <td>${renderPersonnelActions(person)}</td>
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
    } catch (error) {
        showError(status, error);
    }
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
    const status = document.getElementById("personnel-status");
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
        "maint-projection-rows", "maint-projection-excluded-rows",
        "maint-current-payroll-rows", "maint-award-rows", "maint-rank-rows",
        "maint-wage-reform-rows", "maint-pre-reform-rows", "maint-pension-base-rows",
    ].forEach(id => {
        document.getElementById(id).innerHTML = "<tr><td colspan='8'>保存或选择人员后加载记录</td></tr>";
    });
    ["maint-projection-period", "maint-projection-total", "maint-projection-stored-total", "maint-projection-difference"].forEach(id => {
        document.getElementById(id).textContent = "-";
    });
    document.getElementById("maint-projection-pgbc").textContent = "暂无推算结果";
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
    return config.fields || [];
}

function renderSubrecordEditorForm(config, record) {
    const formActions = `<div class="form-actions"><button type="submit">保存记录</button></div>`;
    if (config.sections?.length) {
        return config.sections.map(section => `
            <section class="subrecord-form-section">
                <h4 class="subrecord-form-section-title">${escapeHtml(section.title)}</h4>
                <div class="subrecord-form-section-grid">
                    ${section.fields.map(field => renderSubrecordEditorField(field, record)).join("")}
                </div>
            </section>
        `).join("") + formActions;
    }
    const visibleFields = subrecordEditorFields(config).filter(([, , , options]) => !options?.hidden);
    const hiddenFields = subrecordEditorFields(config).filter(([, , , options]) => options?.hidden);
    if (config.wideModal) {
        return `<div class="subrecord-form-section-grid position-entry-grid">
            ${visibleFields.map(field => renderSubrecordEditorField(field, record)).join("")}
            ${hiddenFields.map(field => renderSubrecordEditorField(field, record)).join("")}
            ${formActions}
        </div>`;
    }
    return visibleFields.map(field => renderSubrecordEditorField(field, record)).join("")
        + hiddenFields.map(field => renderSubrecordEditorField(field, record)).join("")
        + formActions;
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
    document.getElementById("subrecord-editor-card").className = config.wideModal ? "modal-card wide-modal position-subrecord-modal" : "modal-card";
    document.getElementById("subrecord-editor-modal").classList.remove("hidden");
    document.getElementById("subrecord-editor-status").className = "status";
    document.getElementById("subrecord-editor-status").textContent = "";
    document.getElementById("subrecord-editor-form").innerHTML = renderSubrecordEditorForm(config, record);
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
        });
}

function renderSubrecordEditorField([name, label, inputType, options], record) {
    const value = subrecordInputValue(record?.[name], inputType);
    if (options?.hidden) {
        return `<input type="hidden" id="subrecord-field-${escapeHtml(name)}" data-subrecord-field="${escapeHtml(name)}" value="${escapeHtml(value)}">`;
    }
    if (inputType === "select") {
        const choices = subrecordSelectChoices(options);
        const allChoices = choices.some(choice => choice.value === value) || !value
            ? choices
            : [{ value, label: value, code: "" }, ...choices];
        return `
            <label>${escapeHtml(label)}
                <select id="subrecord-field-${escapeHtml(name)}" data-subrecord-field="${escapeHtml(name)}">
                    <option value="">请选择${escapeHtml(label)}</option>
                    ${allChoices.map(choice => `<option value="${escapeHtml(choice.value)}" ${choice.value === value ? "selected" : ""}>${escapeHtml(choice.label)}</option>`).join("")}
                </select>
            </label>
        `;
    }
    return `
        <label>${escapeHtml(label)}
            <input id="subrecord-field-${escapeHtml(name)}" data-subrecord-field="${escapeHtml(name)}" type="${inputType === "number" ? "number" : inputType === "month" ? "month" : "text"}" value="${escapeHtml(value)}" ${options?.readonly ? "readonly" : ""}>
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

function renderMaintPositionRows(positions) {
    return positions.length ? positions.map(row => {
        const isCurrent = String(row.activeFlag ?? "").trim() === "1";
        const codeHint = [row.currentPositionCode, row.positionCode].filter(Boolean).join(" / ");
        return `
            <tr class="${isCurrent ? "highlight-row" : ""}">
                <td class="col-period">${escapeHtml(row.startYearMonth || "")}${row.appCreated ? " <span class='new-badge'>新</span>" : ""}</td>
                <td class="col-position" title="${escapeHtml(row.currentPositionCode || "")}">${escapeHtml(row.currentPosition || "")}</td>
                <td class="col-level">${escapeHtml(row.positionLevel || "")}</td>
                <td class="col-position" title="${escapeHtml(row.positionCode || "")}">${escapeHtml(row.positionName || "")}</td>
                <td class="col-years">${escapeHtml(row.intervalYears ?? "")}</td>
                <td class="col-flag"><span class="assessment-batch-status ${isCurrent ? "" : "status-missing"}">${formatActiveFlag(row.activeFlag)}</span></td>
                <td class="col-code" title="${escapeHtml(codeHint)}">${escapeHtml(codeHint || "-")}</td>
                <td class="col-action">
                    ${isCurrent ? "" : `<button class="row-action" type="button" data-set-current-position="${row.id}">设为现任</button>`}
                    <button class="row-action" type="button" data-edit-position="${row.id}">编辑</button>
                    <button class="row-action danger-button" type="button" data-delete-position="${row.id}">删除</button>
                </td>
            </tr>
        `;
    }).join("") : "<tr><td colspan='8'>暂无任职记录</td></tr>";
}

function subrecordSelectChoices(options) {
    if (options?.optionsProvider === "assessmentResults") {
        const results = isInstitutionPersonnel(state.activePersonnelMaintenance)
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
    return [];
}

function isInstitutionPersonnel(person) {
    const text = `${person?.personnelCategory || ""} ${person?.organizationType || ""}`;
    return text.includes("事业");
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
        payload[name] = inputType === "number" ? Number(input.value || 0) : inputType === "month" ? input.value.replace("-", ".") : input.value.trim();
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
    const text = `${category} ${organizationType}`;
    return text.includes("事业") ? "合格" : "称职";
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
    document.getElementById("maint-projection-rows").innerHTML = (preview.calculatedComponents || []).length ? preview.calculatedComponents.map(component => `
        <tr>
            <td>${escapeHtml(component.fieldName)}</td>
            <td>${escapeHtml(component.caption)}</td>
            <td>${money(component.amount)}</td>
            <td>${escapeHtml(component.source)}</td>
        </tr>
    `).join("") : "<tr><td colspan='4'>暂无工资推算项目</td></tr>";
    document.getElementById("maint-projection-excluded-rows").innerHTML = (preview.excludedComponents || []).length ? preview.excludedComponents.map(component => `
        <tr>
            <td>${escapeHtml(component.fieldName)}</td>
            <td>${escapeHtml(component.caption)}</td>
            <td>${money(component.storedAmount)}</td>
            <td>${escapeHtml(component.reason)}</td>
        </tr>
    `).join("") : "<tr><td colspan='4'>暂无排除字段</td></tr>";
    const pgbc = preview.pgbcComparison || {};
    document.getElementById("maint-projection-pgbc").innerHTML = `
        <strong>处理方式：</strong>${escapeHtml(pgbc.treatment || "-")}<br>
        <strong>旧值：</strong>${money(pgbc.storedAmount)}
        <strong>建议值：</strong>${money(pgbc.recommendedAmount)}<br>
        <span>${escapeHtml(pgbc.note || "")}</span>
    `;
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
    renderWageProjectionSteps(steps, document.getElementById("maint-wage-projection-steps"));
}

function renderWageProjectionSteps(steps, container = document.getElementById("maint-wage-projection-steps")) {
    if (!container) {
        return;
    }
    if (!steps.length) {
        container.innerHTML = "暂无分步明细。";
        return;
    }
    container.innerHTML = steps.map((step, index) => {
        const period = formatProjectionPeriod(step.period);
        const componentRows = (step.components || []).map(component => `
            <tr>
                <td>${escapeHtml(component.fieldName)}</td>
                <td>${escapeHtml(component.caption)}</td>
                <td>${money(component.amount)}</td>
            </tr>
        `).join("");
        return `
            <details class="projection-step-card"${index === steps.length - 1 ? " open" : ""}>
                <summary>
                    <span class="projection-step-index">第 ${index + 1} 步</span>
                    <span class="projection-step-period">${escapeHtml(period)}</span>
                    <span class="projection-step-level">${escapeHtml(step.levelStepDisplay || "-")}</span>
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
    const includeDescendants = state.assessmentBatchMeta?.includeDescendants;
    let count = 7;
    if (!includeDescendants) {
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
    const text = `${row.personnelCategory || ""} ${row.organizationType || ""}`;
    return text.includes("事业") ? assessmentResultOptions.institution : assessmentResultOptions.administrative;
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
    if (!organizationCode) {
        status.className = "status error";
        status.textContent = "请先选择单位。";
        return;
    }
    if (!year) {
        status.className = "status error";
        status.textContent = "请填写考核年度。";
        return;
    }
    status.className = "status";
    status.textContent = "正在查询考核结果...";
    document.getElementById("assessment-batch-rows").innerHTML = "";
    try {
        const params = new URLSearchParams({ organizationCode, year, includeDescendants: String(includeDescendants) });
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
            organizationCode: preview.organizationCode,
            year: preview.year,
            includeDescendants,
        };
        const table = document.getElementById("assessment-batch-table");
        if (table) {
            table.classList.toggle("hide-org-column", !includeDescendants);
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
    updateAssessmentBatchVisibleCount(rows.filter(assessmentBatchRowMatchesFilter).length, rows.length);
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
        const options = assessmentResultOptionsForRow(row);
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
    const text = `${row.personnelCategory || ""} ${row.organizationType || ""}`;
    return text.includes("事业") ? "合格" : "称职";
}

async function saveAssessmentBatch() {
    const meta = state.assessmentBatchMeta;
    if (!meta?.organizationCode || !meta?.year) {
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
        const result = await postJson("/api/personnel/assessments/batch-entry", {
            organizationCode: meta.organizationCode,
            year: meta.year,
            includeDescendants: meta.includeDescendants,
            records,
        });
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
        if (hasPersonnelAccess()) {
            await loadPersonnel();
        }
    } catch (error) {
        showError(status, error);
    }
}

async function loadPositionHistory() {
    const organizationCode = selectedOrganizationCode("position-history-organization-code");
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
                <td>${escapeHtml(row.startYearMonth)}</td>
                <td>${escapeHtml(row.intervalYears)}</td>
                <td>${escapeHtml(row.activeFlag)}</td>
                <td>${formatPromotionFlag(row.promotionFlag)}</td>
            </tr>
        `).join("");
        status.textContent = `第 ${result.page + 1} / ${Math.max(result.totalPages, 1)} 页，共 ${result.totalElements} 条任职记录`;
    } catch (error) {
        showError(status, error);
    }
}

async function loadEducationHistory() {
    const organizationCode = selectedOrganizationCode("education-history-organization-code");
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

async function loadPersonnelStatistics() {
    const organizationCode = selectedOrganizationCode("personnel-statistics-organization-code");
    const year = document.getElementById("personnel-statistics-year")?.value.trim() || "";
    const params = new URLSearchParams();
    if (organizationCode) {
        params.set("organizationCode", organizationCode);
    }
    if (year) {
        params.set("year", year);
    }
    const status = document.getElementById("personnel-statistics-status");
    status.className = "status";
    status.textContent = "正在加载统计数据...";
    try {
        const [summary, payrollChanges] = await Promise.all([
            getJson(`/api/statistics/personnel-summary?${params}`),
            getJson(`/api/statistics/payroll-change-summary?${params}`),
        ]);
        document.getElementById("stat-org-count").textContent = summary.organizationCount ?? "-";
        document.getElementById("stat-active-count").textContent = summary.activePersonnelCount ?? "-";
        document.getElementById("stat-changed-count").textContent = summary.changedPersonnelCount ?? "-";
        document.getElementById("stat-probation-count").textContent = summary.probationPersonnelCount ?? "-";
        const rows = document.getElementById("personnel-statistics-rows");
        rows.innerHTML = (payrollChanges || []).map(row => `
            <tr>
                <td>${escapeHtml(row.period)}</td>
                <td>${escapeHtml(row.changeCount)}</td>
                <td>${escapeHtml(row.personnelCount)}</td>
            </tr>
        `).join("") || "<tr><td colspan='3'>暂无工资变动统计数据</td></tr>";
        status.textContent = "统计加载完成";
    } catch (error) {
        showError(status, error);
    }
}

async function editOrganizationMaintenance(id) {
    const name = prompt("单位名称：");
    if (name === null) {
        return;
    }
    const shortName = prompt("单位简称：", "") ?? "";
    try {
        await putJson(`/api/organizations/${id}`, { name, shortName });
        await loadOrganizationMaintenance();
    } catch (error) {
        showError(document.getElementById("organization-maintenance-status"), error);
    }
}

async function editDictionaryEntry(code) {
    const name = prompt(`字典 ${code} 名称：`);
    if (name === null) {
        return;
    }
    try {
        await putJson(`/api/dictionaries/${encodeURIComponent(code)}`, {
            code,
            name,
            parentCode: "",
            systemFlag: 0,
            enabledFlag: 1,
        });
        await loadDictionaries();
    } catch (error) {
        showError(document.getElementById("dictionary-status"), error);
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
        await loadLocalPolicies();
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
        await loadLocalPolicies();
    } catch (error) {
        showError(document.getElementById("local-policy-status"), error);
    }
}

async function editAllowanceStandard(id) {
    const amount = prompt("标准金额：");
    if (amount === null) {
        return;
    }
    try {
        const existing = (await getJson(`/api/payroll/allowance-standards?page=0&size=1000`)).content.find(row => row.id === id);
        if (!existing) {
            throw new Error("未找到该津贴标准记录");
        }
        await putJson(`/api/standards/allowances/${id}`, {
            standardYearMonth: existing.standardYearMonth,
            item: existing.item,
            positionCode: existing.positionCode,
            name: existing.name,
            workYearsLower: existing.workYearsLower,
            workYearsUpper: existing.workYearsUpper,
            amount: Number(amount),
            performanceCategory: existing.performanceCategory,
        });
        await loadAllowanceStandards();
    } catch (error) {
        showError(document.getElementById("allowance-standards-status"), error);
    }
}

async function createAllowanceStandard() {
    const standardYearMonth = prompt("标准年月：", document.getElementById("allowance-standard-year-month").value.trim() || "202407");
    if (standardYearMonth === null) {
        return;
    }
    const item = prompt("项目编码：", document.getElementById("allowance-standard-item").value.trim() || "DFBT2");
    if (item === null) {
        return;
    }
    const positionCode = prompt("职务编码：", document.getElementById("allowance-standard-position-code").value.trim() || "");
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
        await postJson("/api/standards/allowances", {
            standardYearMonth,
            item,
            positionCode,
            name,
            workYearsLower: 0,
            workYearsUpper: 0,
            amount: Number(amount),
            performanceCategory: 0,
        });
        await loadAllowanceStandards();
    } catch (error) {
        showError(document.getElementById("allowance-standards-status"), error);
    }
}

async function deleteAllowanceStandard(id) {
    if (!confirm("确认删除该津贴补贴标准？")) {
        return;
    }
    try {
        await deleteJson(`/api/standards/allowances/${id}`);
        await loadAllowanceStandards();
    } catch (error) {
        showError(document.getElementById("allowance-standards-status"), error);
    }
}

async function editRankAllowanceStandard(id) {
    try {
        const existing = (await getJson(`/api/payroll/rank-allowance-standards?page=0&size=1000`)).content.find(row => row.id === id);
        if (!existing) {
            throw new Error("未找到该警衔津贴标准");
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
        await loadRankAllowanceStandards();
    } catch (error) {
        showError(document.getElementById("rank-standards-status"), error);
    }
}

async function createRankAllowanceStandard() {
    const standardYearMonth = prompt("标准年月：", document.getElementById("rank-standard-year-month").value.trim() || "202407");
    if (standardYearMonth === null) {
        return;
    }
    const rankCode = prompt("警衔编码：", "");
    if (rankCode === null) {
        return;
    }
    const rankName = prompt("警衔名称：", "");
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
        await loadRankAllowanceStandards();
    } catch (error) {
        showError(document.getElementById("rank-standards-status"), error);
    }
}

async function deleteRankAllowanceStandard(id) {
    if (!confirm("确认删除该警衔津贴标准？")) {
        return;
    }
    try {
        await deleteJson(`/api/standards/ranks/${id}`);
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
    const positionCode = prompt("职务编码：", document.getElementById("basic-standard-code").value.trim() || "");
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
        document.getElementById("basic-standard-year-month").value = standardYearMonth;
        document.getElementById("basic-standard-code").value = positionCode;
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
    codeInput.value = record.code || document.getElementById("basic-standard-code").value.trim() || "";
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
        document.getElementById("basic-standard-year-month").value = standardYearMonth;
        document.getElementById("basic-standard-code").value = code;
        await loadBasicStandards();
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
    levelInput.value = record.salaryLevel || document.getElementById("basic-standard-code").value.trim() || "";
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
        document.getElementById("basic-standard-year-month").value = standardYearMonth;
        document.getElementById("basic-standard-code").value = salaryLevel;
        await loadBasicStandards();
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
        await loadInternSalaryStandards();
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
    positionInput.value = record.positionCode || document.getElementById("wage-reform-position-code").value.trim() || "";
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
        await loadWageReformStandards();
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
        || document.getElementById("other-allowance-standard-year-month").value.trim()
        || "";
    const codeInput = document.getElementById("other-allowance-standard-code");
    codeInput.value = record.code || document.getElementById("other-allowance-code").value.trim() || "";
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
        if (payload.standardYearMonth) {
            document.getElementById("other-allowance-standard-year-month").value = payload.standardYearMonth;
        }
        document.getElementById("other-allowance-code").value = payload.code;
        await loadOtherAllowanceStandards();
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
                ${hasOrgWrite() ? `<td><button class="row-action" type="button" data-org-edit="${row.id}">编辑</button></td>` : ""}
            </tr>
        `).join("");
        rows.querySelectorAll("button[data-org-edit]").forEach(button => {
            button.addEventListener("click", () => editOrganizationMaintenance(Number(button.dataset.orgEdit)));
        });
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
                ${hasSystemConfigWrite() ? `<td><button class="row-action" type="button" data-dict-edit="${escapeHtml(row.code)}">编辑</button></td>` : ""}
            </tr>
        `).join("");
        rows.querySelectorAll("button[data-dict-edit]").forEach(button => {
            button.addEventListener("click", () => editDictionaryEntry(button.dataset.dictEdit));
        });
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
                ${hasSystemConfigWrite() ? `<td><button class="row-action" type="button" data-policy-edit="${row.id}">编辑</button></td>` : ""}
            </tr>
        `).join("");
        rows.querySelectorAll("button[data-policy-edit]").forEach(button => {
            button.addEventListener("click", () => editLocalPolicy(Number(button.dataset.policyEdit)));
        });
        optionRows.innerHTML = (options || []).map(row => `
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
        document.getElementById("system-options-edit")?.addEventListener("click", () => editSystemOptions(options?.[0]));
        status.textContent = `第 ${policies.page + 1} / ${Math.max(policies.totalPages, 1)} 页，共 ${policies.totalElements} 条政策配置`;
    } catch (error) {
        showError(status, error);
    }
}

const payrollPreviewPanelIds = {
    person: "selected-person",
    period: "preview-period",
    total: "preview-total",
    storedTotal: "stored-total",
    totalDifference: "total-difference",
    componentRows: "component-rows",
    excludedRows: "excluded-rows",
    pgbcCard: "pgbc-card",
};

const payrollPreviewModalIds = {
    person: "payroll-preview-modal-person",
    period: "payroll-preview-modal-period",
    total: "payroll-preview-modal-total",
    storedTotal: "payroll-preview-modal-stored-total",
    totalDifference: "payroll-preview-modal-total-difference",
    componentRows: "payroll-preview-modal-component-rows",
    excludedRows: "payroll-preview-modal-excluded-rows",
    pgbcCard: "payroll-preview-modal-pgbc-card",
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

    document.getElementById(ids.componentRows).innerHTML = preview.calculatedComponents.map(component => `
        <tr>
            <td>${escapeHtml(component.fieldName)}</td>
            <td>${escapeHtml(component.caption)}</td>
            <td>${money(component.amount)}</td>
            <td>${escapeHtml(component.source)}</td>
        </tr>
    `).join("");

    document.getElementById(ids.excludedRows).innerHTML = preview.excludedComponents.map(component => `
        <tr>
            <td>${escapeHtml(component.fieldName)}</td>
            <td>${escapeHtml(component.caption)}</td>
            <td>${money(component.storedAmount)}</td>
            <td>${escapeHtml(component.reason)}</td>
        </tr>
    `).join("");

    const pgbc = preview.pgbcComparison;
    document.getElementById(ids.pgbcCard).innerHTML = `
        <strong>处理方式：</strong>${escapeHtml(pgbc.treatment)}<br>
        <strong>旧值：</strong>${money(pgbc.storedAmount)}
        <strong>对账值：</strong>${money(pgbc.comparisonAmount)}<br>
        <span>${escapeHtml(pgbc.note)}</span>
    `;
}

async function openPayrollPreviewModal(uid) {
    const modal = document.getElementById("payroll-preview-modal");
    const status = document.getElementById("payroll-preview-modal-status");
    const content = document.getElementById("payroll-preview-modal-content");
    modal.classList.remove("hidden");
    status.className = "status";
    status.textContent = "正在加载工资试算...";
    content.classList.add("hidden");

    try {
        const preview = await getJson(`/api/payroll/personnel/${uid}/calculation-preview`);
        state.selectedPersonnel = preview;
        renderPayrollPreview(preview, payrollPreviewModalIds);
        status.textContent = "工资试算加载完成";
        content.classList.remove("hidden");
    } catch (error) {
        showError(status, error);
    }
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
    const organizationCode = selectedOrganizationCode("audit-organization-code");
    const page = document.getElementById("audit-page").value || "0";
    const size = document.getElementById("audit-size").value || "5";
    const params = new URLSearchParams({ page, size });
    if (organizationCode) {
        params.set("organizationCode", organizationCode);
    }

    const status = document.getElementById("audit-status");
    const rows = document.getElementById("audit-rows");
    status.className = "status";
    status.textContent = "正在执行推算对账（逐人重放工资推算），查看明细时可展开分步工资，请耐心等待...";
    rows.innerHTML = "";
    closeAuditDetail();

    try {
        const summary = await getJson(`/api/payroll/projection-audit-summary?${params}`);
        document.getElementById("audit-total").textContent = summary.totalPersonnelWithHistory;
        document.getElementById("audit-compared").textContent = summary.comparedPersonnel;
        document.getElementById("audit-difference-count").textContent = summary.latestDifferenceCount;
        document.getElementById("audit-history-person-count").textContent = summary.historyMismatchPersonCount;
        document.getElementById("audit-history-record-count").textContent = summary.totalHistoryRecordMismatches;
        document.getElementById("audit-max-difference").textContent = money(summary.maxAbsoluteDifference);
        rows.innerHTML = (summary.differences || []).map(item => `
            <tr>
                <td>${escapeHtml(item.uid)}</td>
                <td>${escapeHtml(item.name)}</td>
                <td>${escapeHtml(item.latestPeriod)}</td>
                <td>${money(item.storedTotal)}</td>
                <td>${item.latestProjectionEligible ? money(item.projectedTotal) : "-"}</td>
                <td class="${Number(item.latestTotalDifference || 0) === 0 && item.latestMatched ? "difference-ok" : "difference-bad"}">${item.latestProjectionEligible ? money(item.latestTotalDifference) : escapeHtml(item.latestNote || "不可推算")}</td>
                <td>${escapeHtml(item.historyMismatchCount)} / ${escapeHtml(item.historyRecordCount)}</td>
                <td>${formatProjectionAuditSummary(item)}</td>
                <td><button class="row-action" type="button" data-audit-detail="${item.uid}" data-audit-name="${item.name || ""}">查看</button></td>
            </tr>
        `).join("");
        rows.querySelectorAll("[data-audit-detail]").forEach(button => {
            button.addEventListener("click", () => openAuditDetail(button.dataset.auditDetail, button.dataset.auditName));
        });
        status.textContent = `已比较 ${summary.comparedPersonnel} 人，当前工资差异 ${summary.latestDifferenceCount} 人，历次调资差异 ${summary.totalHistoryRecordMismatches} 条`;
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

async function openAuditDetail(uid, name) {
    const panel = document.getElementById("audit-detail-panel");
    const items = document.getElementById("audit-detail-items");
    const status = document.getElementById("audit-detail-status");
    document.getElementById("audit-detail-title").textContent = `${name || ""} 历次调资差异明细`;
    panel.classList.remove("hidden");
    items.innerHTML = "";
    status.className = "status";
    status.textContent = "正在加载对账结果与分步工资明细，请稍候...";
    try {
        const audits = await getJson(`/api/payroll/personnel/${uid}/projection-history-audits`);
        const mismatches = (audits || []).filter(item => !item.matched);
        status.textContent = mismatches.length
            ? `共 ${mismatches.length} 条调资记录存在差异，可展开查看推算分步明细。`
            : "该人员历次调资与推算结果一致。";
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
                <div id="audit-projection-steps-${index}" class="projection-steps"></div>
            </article>
        `).join("");
        mismatches.forEach((item, index) => {
            const stepContainer = document.getElementById(`audit-projection-steps-${index}`);
            renderWageProjectionSteps(item.stepDetails || [], stepContainer);
        });
    } catch (error) {
        status.textContent = "";
        showError(status, error);
        items.innerHTML = `<p class="projection-step-description">${escapeHtml(error.message || "加载失败")}</p>`;
    }
}

function closeAuditDetail() {
    document.getElementById("audit-detail-panel").classList.add("hidden");
    document.getElementById("audit-detail-items").innerHTML = "";
    document.getElementById("audit-detail-status").textContent = "";
}

async function downloadProjectionAuditExport(format) {
    const organizationCode = selectedOrganizationCode("audit-organization-code");
    const mismatchesOnly = document.getElementById("audit-export-mismatches-only")?.checked;
    const params = new URLSearchParams();
    if (organizationCode) {
        params.set("organizationCode", organizationCode);
    }
    if (mismatchesOnly) {
        params.set("mismatchesOnly", "true");
    }
    const status = document.getElementById("audit-status");
    status.className = "status";
    status.textContent = "正在对全库人员执行工资推算对账并生成文件，人数较多时可能需要数十分钟，请勿关闭页面...";
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
    await loadPayrollChangeRegisterReport();
}

async function loadPayrollChangeRegisterReport() {
    const organizationCode = selectedOrganizationCode("report-payroll-change-organization-code");
    const reportTypeCode = document.getElementById("report-payroll-change-type-select").value.trim();
    const year = document.getElementById("report-payroll-change-year").value.trim();
    const keyword = document.getElementById("report-payroll-change-keyword").value.trim();
    const size = document.getElementById("report-payroll-change-size").value || "50";
    const params = new URLSearchParams({ page: "0", size });
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
        state.payrollChangeRegisterCandidates = result.content || [];
        document.getElementById("report-payroll-change-total-count").textContent = result.totalElements || 0;
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
        status.textContent = `共 ${result.totalElements} 条，当前预览 ${result.content.length} 条`;
    } catch (error) {
        showError(status, error);
    }
}

async function generateSelectedPayrollChangeRegister() {
    const selectedIds = Array.from(document.querySelectorAll("[data-register-select]:checked"))
        .map(input => input.value)
        .filter(Boolean);
    const status = document.getElementById("report-payroll-change-status");
    if (selectedIds.length === 0) {
        status.className = "status error";
        status.textContent = "请先勾选需要打印的人员。";
        return;
    }
    status.className = "status";
    status.textContent = `正在生成 ${selectedIds.length} 人花名册...`;
    try {
        await renderPayrollChangeRegisterFromServer(selectedIds, "report-payroll-change-type-select");
        status.textContent = `已生成 ${selectedIds.length} 人花名册`;
    } catch (error) {
        showError(status, error);
    }
}

async function onPayrollChangeApprovalReportSearch(event) {
    event.preventDefault();
    await loadPayrollChangeApprovalReport();
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
            getJson("/api/reports/types?category=" + encodeURIComponent("审批表") + "&size=200"),
            getJson("/api/reports/types?category=" + encodeURIComponent("花名册") + "&size=200"),
            getJson("/api/reports/types?size=200"),
        ]);
        const allTypes = allResult.content || [];
        const approvalTypes = (approvalResult.content || []).length
            ? approvalResult.content
            : allTypes.filter(type => isApprovalReportType(type));
        const registerTypes = (registerResult.content || []).length
            ? registerResult.content
            : allTypes.filter(type => isRegisterReportType(type));
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

function fillReportTypeSelect(select, types, fallbackTitle) {
    const options = (types || []).map(type => `
            <option value="${escapeHtml(type.code || "")}" data-title="${escapeHtml(type.title || type.name || "")}">
                ${escapeHtml(type.name || type.title || type.code)}
            </option>
        `).join("");
    select.innerHTML = options || `<option value="">${escapeHtml(fallbackTitle)}</option>`;
}

async function loadPayrollChangeApprovalReport() {
    const organizationCode = selectedOrganizationCode("report-approval-organization-code");
    const reportTypeCode = document.getElementById("report-approval-type-select").value.trim();
    const year = document.getElementById("report-approval-year").value.trim();
    const keyword = document.getElementById("report-approval-keyword").value.trim();
    const size = document.getElementById("report-approval-size").value || "50";
    const params = new URLSearchParams({ page: "0", size });
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
        document.getElementById("report-approval-total-count").textContent = result.totalElements || 0;
        rows.innerHTML = (result.content || []).map(row => `
            <tr>
                <td><input type="checkbox" data-approval-select value="${escapeHtml(row.payrollHistoryId)}"></td>
                <td>${escapeHtml(row.organizationCode)} ${escapeHtml(row.organizationName || "")}</td>
                <td>${escapeHtml(row.personCode)}</td>
                <td>${escapeHtml(row.name)}</td>
                <td>${escapeHtml(row.calculationYear)}${escapeHtml(row.calculationMonth)}</td>
                <td>${escapeHtml(row.changeType)}</td>
                <td>${escapeHtml(row.positionCode)} ${escapeHtml(row.positionName || "")}</td>
                <td>${money(row.totalAmount)}</td>
                <td><button class="row-action" data-approval-id="${escapeHtml(row.payrollHistoryId)}" type="button">生成审批表</button></td>
            </tr>
        `).join("");
        rows.querySelectorAll("button[data-approval-id]").forEach(button => {
            button.addEventListener("click", () => loadPayrollChangeApproval(button.dataset.approvalId));
        });
        status.textContent = `共 ${result.totalElements} 条，当前可选 ${result.content.length} 条`;
    } catch (error) {
        showError(status, error);
    }
}

async function generateAndPrintSelectedPayrollChangeApprovals() {
    const selectedIds = Array.from(document.querySelectorAll("[data-approval-select]:checked"))
        .map(input => input.value)
        .filter(Boolean);
    const status = document.getElementById("report-approval-status");
    const button = document.getElementById("payroll-change-approval-print");
    if (selectedIds.length === 0) {
        status.className = "status error";
        status.textContent = "请先勾选需要打印的人员。";
        return;
    }
    const originalLabel = button?.textContent || "生成并打印审批表";
    if (button) {
        button.disabled = true;
        button.textContent = `正在生成 ${selectedIds.length} 份...`;
    }
    status.className = "status";
    status.textContent = `正在生成 ${selectedIds.length} 份审批表 PDF...`;
    try {
        const startedAt = performance.now();
        await renderPayrollChangeApprovalsFromServer(selectedIds, "report-approval-type-select");
        const blob = await downloadPayrollChangeReportExport(
            "/api/reports/payroll-change-approvals/pdf",
            buildPayrollChangeExportRequest(selectedIds, "report-approval-type-select"),
            status);
        const elapsedMs = Math.max(1, Math.round(performance.now() - startedAt));
        status.className = "status success";
        status.textContent = `已生成 ${selectedIds.length} 份审批表 PDF（${elapsedMs} ms），正在打开打印窗口...`;
        await openPdfBlobForPrint(blob);
        status.textContent = `已生成并送打 ${selectedIds.length} 份审批表 PDF（${elapsedMs} ms）`;
    } catch (error) {
        showError(status, error);
    } finally {
        if (button) {
            button.disabled = false;
            button.textContent = originalLabel;
        }
    }
}

async function exportSelectedPayrollChangeApprovalsExcel() {
    const selectedIds = Array.from(document.querySelectorAll("[data-approval-select]:checked"))
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

async function printSelectedPayrollChangeRegisterPdf() {
    const selectedIds = Array.from(document.querySelectorAll("[data-register-select]:checked"))
        .map(input => input.value)
        .filter(Boolean);
    const status = document.getElementById("report-payroll-change-status");
    if (selectedIds.length === 0) {
        status.className = "status error";
        status.textContent = "请先勾选需要打印的人员。";
        return;
    }
    status.className = "status";
    status.textContent = `正在生成 ${selectedIds.length} 人花名册 PDF...`;
    try {
        await renderPayrollChangeRegisterFromServer(selectedIds, "report-payroll-change-type-select");
        const blob = await downloadPayrollChangeReportExport(
            "/api/reports/payroll-change-register/pdf",
            buildPayrollChangeExportRequest(selectedIds, "report-payroll-change-type-select"),
            status);
        await openPdfBlobForPrint(blob);
        status.className = "status success";
        status.textContent = `已生成并送打 ${selectedIds.length} 人花名册 PDF`;
    } catch (error) {
        showError(status, error);
    }
}

async function exportSelectedPayrollChangeRegisterExcel() {
    const selectedIds = Array.from(document.querySelectorAll("[data-register-select]:checked"))
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
        headers: { "Content-Type": "application/json" },
        credentials: "same-origin",
        body: JSON.stringify(requestBody || {}),
    });
    if (!response.ok) {
        const message = await response.text();
        throw new Error(message || `预览生成失败（${response.status}）`);
    }
    return response.text();
}

async function renderPayrollChangeApprovalsFromServer(selectedIds, titleSelectId) {
    const preview = document.getElementById("report-approval-preview");
    preview.innerHTML = await fetchPayrollChangePreviewHtml(
        "/api/reports/payroll-change-approvals/preview",
        buildPayrollChangeExportRequest(selectedIds, titleSelectId));
    preview.classList.remove("hidden");
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

async function waitForPayrollChangeExportJob(jobId, onProgress) {
    const startedAt = performance.now();
    for (;;) {
        const job = await getJson(`/api/reports/payroll-change-export-jobs/${encodeURIComponent(jobId)}`);
        if (onProgress) {
            onProgress(job, Math.max(1, Math.round(performance.now() - startedAt)));
        }
        if (job.status === "SUCCEEDED" || job.status === "FAILED") {
            return job;
        }
        await new Promise(resolve => setTimeout(resolve, 1000));
    }
}

async function downloadPayrollChangeExportJobBlob(jobId) {
    const response = await fetch(`/api/reports/payroll-change-export-jobs/${encodeURIComponent(jobId)}/download`, {
        credentials: "same-origin",
    });
    if (!response.ok) {
        const message = await response.text();
        throw new Error(message || `下载导出结果失败（${response.status}）`);
    }
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
        const job = await waitForPayrollChangeExportJob(submitted.jobId, (current, elapsedMs) => {
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
        const downloaded = await downloadPayrollChangeExportJobBlob(job.jobId);
        return { blob: downloaded.blob, filename: job.fileName || downloaded.filename };
    }

    const response = await fetch(url, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        credentials: "same-origin",
        body: JSON.stringify(requestBody || {}),
    });
    if (!response.ok) {
        const message = await response.text();
        throw new Error(message || `导出失败（${response.status}）`);
    }
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

async function loadPayrollChangeApproval(payrollHistoryId) {
    const status = document.getElementById("report-approval-status");
    status.className = "status";
    status.textContent = "正在生成工资变动审批表...";
    try {
        await renderPayrollChangeApprovalsFromServer([payrollHistoryId], "report-approval-type-select");
        status.className = "status success";
        status.textContent = "工资变动审批表已生成（1 份），可点击右上角“生成并打印审批表”打印当前勾选人员。";
    } catch (error) {
        showError(status, error);
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

async function applySimplePromotionAction(apiPrefix, payrollHistoryId, moduleName, reloadFn) {
    if (!confirm(`确认按当前试算结果处理${moduleName}？系统会新增一条当前工资变动记录，并将原当前记录转为历史记录。`)) {
        return;
    }
    const statusId = apiPrefix.includes("education") ? "education-promotion-status"
        : apiPrefix.includes("regularization") ? "regularization-status"
        : apiPrefix.includes("floating-to-fixed") ? "floating-to-fixed-status"
        : apiPrefix.includes("intern-salary-changes") ? "intern-salary-change-status"
        : "teaching-allowance-status";
    const status = document.getElementById(statusId);
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
    if (!confirm(`确认还原当前${moduleName}工资变动？`)) {
        return;
    }
    const statusId = apiPrefix.includes("education") ? "education-promotion-status"
        : apiPrefix.includes("regularization") ? "regularization-status"
        : apiPrefix.includes("floating-to-fixed") ? "floating-to-fixed-status"
        : apiPrefix.includes("intern-salary-changes") ? "intern-salary-change-status"
        : "teaching-allowance-status";
    const status = document.getElementById(statusId);
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
    if (!canApply && !canRollback) {
        return "-";
    }
    const parts = [];
    if (canApply) {
        parts.push(`<button class="row-action" type="button" data-simple-apply="${escapeHtml(row.payrollHistoryId)}" data-simple-api="${escapeHtml(apiPrefix)}" data-simple-name="${escapeHtml(moduleName)}">处理</button>`);
    }
    if (canRollback) {
        parts.push(`<button class="row-action" type="button" data-simple-rollback="${escapeHtml(row.payrollHistoryId)}" data-simple-api="${escapeHtml(apiPrefix)}" data-simple-name="${escapeHtml(moduleName)}">还原</button>`);
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
    const dueOnly = document.getElementById("normal-promotion-due-only").checked;
    const page = document.getElementById("normal-promotion-page").value || "0";
    const size = document.getElementById("normal-promotion-size").value || "20";
    const year = currentNormalPromotionYear();
    const params = new URLSearchParams({ page, size, dueOnly, year });
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
        rows.innerHTML = (result.content || []).map(row => {
            const canProcess = Boolean(row.applyEligible);
            const canPromptOverdue = Boolean(row.overdueFromLastYear);
            const levelRequiredFirst = Boolean(row.levelPromotionRequiredFirst);
            const canClickApply = canProcess || canPromptOverdue || levelRequiredFirst;
            return `
            <tr>
                <td class="col-select"><input type="checkbox" data-normal-select="${escapeHtml(row.payrollHistoryId)}" data-normal-eligible="${canProcess ? "true" : "false"}" ${canProcess ? "" : "disabled"} aria-label="选择${escapeHtml(row.name)}"></td>
                <td class="col-org">${escapeHtml(row.organizationCode)}</td>
                <td class="col-code">${escapeHtml(row.personCode)}</td>
                <td class="col-name">${escapeHtml(row.name)}</td>
                <td class="col-period">${escapeHtml(row.calculationPeriod)}</td>
                <td class="col-change">${escapeHtml(row.changeType)}</td>
                <td class="col-position">${escapeHtml(row.positionName)}</td>
                <td class="col-grade">${escapeHtml(row.currentGradeOrLevel)}</td>
                <td class="col-grade">${escapeHtml(row.promotedGradeOrLevel)}</td>
                <td class="col-level">${escapeHtml(row.gradeSalaryLevel || "")}</td>
                <td class="col-tech">${escapeHtml(row.levelAssessmentStartYear || "")}</td>
                <td class="col-tech">${escapeHtml(row.stepAssessmentStartYear || "")}</td>
                <td class="col-years">${escapeHtml(row.assessmentPeriod || "")}</td>
                <td class="col-years">${escapeHtml(row.qualifiedYears ?? "")}</td>
                <td class="col-years">${escapeHtml(row.requiredYears ?? "")}</td>
                <td class="col-flag">${row.eligible ? "是" : "否"}</td>
                <td class="col-note">${escapeHtml(row.note || "")}</td>
                <td class="col-money">${money(row.currentBaseSalary)}</td>
                <td class="col-money">${money(row.promotedBaseSalary)}</td>
                <td class="col-money">${money(row.increaseAmount)}</td>
                <td class="col-type">${escapeHtml(baseSalarySourceName(row.baseSalarySource))}</td>
                <td class="col-action">
                    <button class="row-action" data-normal-apply="${escapeHtml(row.payrollHistoryId)}" data-normal-overdue="${canPromptOverdue ? "true" : "false"}" data-normal-level-first="${levelRequiredFirst ? "true" : "false"}" type="button" ${canClickApply ? "" : "disabled"}>处理</button>
                    <button class="row-action danger-button" data-normal-rollback="${escapeHtml(row.payrollHistoryId)}" type="button" ${row.rollbackEligible ? "" : "disabled"}>还原</button>
                </td>
            </tr>
        `;
        }).join("");
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
        status.textContent = `第 ${result.page + 1} / ${Math.max(result.totalPages, 1)} 页，共 ${result.totalElements} 条试算记录${year ? `（${year} 年）` : ""}`;
    } catch (error) {
        showError(status, error);
    }
}

async function loadLevelPromotions() {
    const organizationCode = selectedOrganizationCode("level-promotion-organization-code");
    const keyword = document.getElementById("level-promotion-keyword").value.trim();
    const page = document.getElementById("level-promotion-page").value || "0";
    const size = document.getElementById("level-promotion-size").value || "20";
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

    const status = document.getElementById("level-promotion-status");
    const rows = document.getElementById("level-promotion-rows");
    status.className = "status";
    status.textContent = "正在查询级别晋升试算...";
    rows.innerHTML = "";

    try {
        const result = await getJson(`/api/payroll/level-promotions?${params}`);
        document.getElementById("level-promotion-select-all").checked = false;
        rows.innerHTML = (result.content || []).map(row => {
            const canProcess = Boolean(row.applyEligible);
            const canPromptOverdue = Boolean(row.overdueFromLastYear);
            const canClickApply = canProcess || canPromptOverdue;
            return `
            <tr>
                <td class="col-select"><input type="checkbox" data-level-select="${escapeHtml(row.payrollHistoryId)}" data-level-eligible="${canProcess ? "true" : "false"}" ${canProcess ? "" : "disabled"} aria-label="选择${escapeHtml(row.name)}"></td>
                <td class="col-org">${escapeHtml(row.organizationCode)}</td>
                <td class="col-code">${escapeHtml(row.personCode)}</td>
                <td class="col-name">${escapeHtml(row.name)}</td>
                <td class="col-period">${escapeHtml(row.calculationPeriod)}</td>
                <td class="col-change">${escapeHtml(row.changeType)}</td>
                <td class="col-change">${row.rollbackEligible ? "已处理" : ""}</td>
                <td class="col-position">${escapeHtml(row.positionName)}</td>
                <td class="col-level">${escapeHtml(row.currentLevel || "")}</td>
                <td class="col-grade">${escapeHtml(row.currentStep || "")}</td>
                <td class="col-level">${escapeHtml(row.promotedLevel || "")}</td>
                <td class="col-grade">${escapeHtml(row.promotedStep || "")}</td>
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
                <td class="col-flag">${row.eligible ? "是" : "否"}</td>
                <td class="col-note">${escapeHtml(row.note || "")}</td>
                <td class="col-action">
                    <button class="row-action" data-level-apply="${escapeHtml(row.payrollHistoryId)}" data-level-overdue="${canPromptOverdue ? "true" : "false"}" type="button" ${canClickApply ? "" : "disabled"}>处理</button>
                    <button class="row-action danger-button" data-level-rollback="${escapeHtml(row.payrollHistoryId)}" type="button" ${row.rollbackEligible ? "" : "disabled"}>还原</button>
                </td>
            </tr>
        `;
        }).join("");
        rows.querySelectorAll("button[data-level-apply]").forEach(button => {
            button.addEventListener("click", () => applyPromotionAction(
                "level",
                button.dataset.levelApply,
                button.dataset.levelOverdue === "true"));
        });
        rows.querySelectorAll("button[data-level-rollback]").forEach(button => {
            button.addEventListener("click", () => rollbackPromotionAction("level", button.dataset.levelRollback));
        });
        status.textContent = `第 ${result.page + 1} / ${Math.max(result.totalPages, 1)} 页，共 ${result.totalElements} 条试算记录（${year} 年）`;
    } catch (error) {
        showError(status, error);
    }
}

const OVERDUE_LEVEL_PROMOTION_MESSAGE = "上年符合级别晋升条件";
const OVERDUE_NORMAL_PROMOTION_MESSAGE = "上年符合档次/薪级晋升条件";
const LEVEL_PROMOTION_REQUIRED_FIRST_MESSAGE = "同年符合级别晋升条件，需先办理级别晋升，再办理档次/薪级晋升。";

function warnOverduePromotionAction(type, overdueFromLastYear) {
    if (!overdueFromLastYear) {
        return false;
    }
    window.alert(type === "normal" ? OVERDUE_NORMAL_PROMOTION_MESSAGE : OVERDUE_LEVEL_PROMOTION_MESSAGE);
    return true;
}

async function applyPromotionAction(type, payrollHistoryId, overdueFromLastYear = false, levelPromotionRequiredFirst = false) {
    if (levelPromotionRequiredFirst) {
        window.alert(LEVEL_PROMOTION_REQUIRED_FIRST_MESSAGE);
        return;
    }
    if (warnOverduePromotionAction(type, overdueFromLastYear)) {
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
        const result = await postJson(applyUrl, {});
        status.textContent = (result && result.message) || `${moduleName}处理完成`;
        if (type === "normal") {
            await loadNormalPromotions();
        } else {
            await loadLevelPromotions();
        }
    } catch (error) {
        showError(status, error);
    }
}

async function applySelectedLevelPromotions() {
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
    if (!confirm(`确认批量处理 ${selectedIds.length} 条级别晋升记录？`)) {
        return;
    }
    status.className = "status";
    status.textContent = `正在批量处理 0 / ${selectedIds.length}...`;
    let successCount = 0;
    const failures = [];
    const yearQuery = levelPromotionYearParam();
    for (const id of selectedIds) {
        try {
            await postJson(`/api/payroll/level-promotions/${encodeURIComponent(id)}/apply?${yearQuery}`, {});
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
    await loadLevelPromotions();
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
    const yearQuery = normalPromotionYearParam();
    for (const id of selectedIds) {
        try {
            const applyUrl = `/api/payroll/normal-promotions/${encodeURIComponent(id)}/apply${yearQuery ? `?${yearQuery}` : ""}`;
            await postJson(applyUrl, {});
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
        status.textContent = (result && result.message) || `${moduleName}已还原`;
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
    const organizationCode = selectedOrganizationCode("position-change-organization-code");
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
        document.getElementById("position-change-select-all").checked = false;
        rows.innerHTML = (result.content || []).map(row => `
            <tr>
                <td class="col-select"><input type="checkbox" data-position-change-select="${escapeHtml(row.payrollHistoryId)}" data-position-change-eligible="${row.applyEligible ? "true" : "false"}" ${row.applyEligible ? "" : "disabled"} aria-label="选择${escapeHtml(row.name)}"></td>
                <td class="col-org">${escapeHtml(row.organizationCode)}</td>
                <td class="col-code">${escapeHtml(row.personCode)}</td>
                <td class="col-name">${escapeHtml(row.name)}</td>
                <td class="col-position" title="${escapeHtml(row.currentPositionCode || "")}">${escapeHtml(row.currentPositionName || "")}</td>
                <td class="col-position" title="${escapeHtml(row.newPositionCode || "")}">${escapeHtml(row.newPositionName || "")}</td>
                <td class="col-change">${escapeHtml(row.changeType || "")}</td>
                <td class="col-change">${row.rollbackEligible ? "已处理" : ""}</td>
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
                    <button class="row-action" data-position-change-detail="${escapeHtml(row.payrollHistoryId)}" type="button" ${row.rollbackEligible ? "" : "disabled"}>明细</button>
                    <button class="row-action" data-position-change-apply="${escapeHtml(row.payrollHistoryId)}" type="button" ${row.applyEligible ? "" : "disabled"}>处理</button>
                    <button class="row-action danger-button" data-position-change-rollback="${escapeHtml(row.payrollHistoryId)}" type="button" ${row.rollbackEligible ? "" : "disabled"}>还原</button>
                </td>
            </tr>
        `).join("");
        rows.querySelectorAll("button[data-position-change-detail]").forEach(button => {
            button.addEventListener("click", () => openPositionChangeDetailModal(button.dataset.positionChangeDetail));
        });
        rows.querySelectorAll("button[data-position-change-apply]").forEach(button => {
            button.addEventListener("click", () => applyPositionChangeAction(button.dataset.positionChangeApply));
        });
        rows.querySelectorAll("button[data-position-change-rollback]").forEach(button => {
            button.addEventListener("click", () => rollbackPositionChangeAction(button.dataset.positionChangeRollback));
        });
        status.textContent = `第 ${result.page + 1} / ${Math.max(result.totalPages, 1)} 页，共 ${result.totalElements} 条试算记录`;
    } catch (error) {
        showError(status, error);
    }
}

async function applySelectedPositionChanges() {
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
    if (!confirm(`确认批量处理 ${selectedIds.length} 条职务变化记录？`)) {
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

async function applyPositionChangeAction(payrollHistoryId) {
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

function positionChangeDetailFieldRow(label, value) {
    if (value == null || value === "" || value === false) {
        return "";
    }
    return `<tr><th>${escapeHtml(label)}</th><td colspan="3">${escapeHtml(String(value))}</td></tr>`;
}

function positionChangeDetailFlagRow(label, active) {
    return active ? `<tr><th>${escapeHtml(label)}</th><td colspan="3">是</td></tr>` : "";
}

function positionChangeDetailMoneyRow(label, before, after, delta) {
    return `<tr><td>${escapeHtml(label)}</td><td>${money(before)}</td><td>${money(after)}</td><td>${money(delta)}</td></tr>`;
}

function positionChangeDetailCompareRow(label, before, after) {
    const beforeText = before == null || before === "" ? "-" : String(before);
    const afterText = after == null || after === "" ? "-" : String(after);
    const delta = beforeText === afterText ? "-" : "变化";
    return `<tr><td>${escapeHtml(label)}</td><td>${escapeHtml(beforeText)}</td><td>${escapeHtml(afterText)}</td><td>${delta === "-" ? "-" : escapeHtml(delta)}</td></tr>`;
}

function renderPositionChangeDetailContent(row) {
    document.getElementById("position-change-detail-summary").textContent =
        `${row.organizationCode}-${row.personCode} ${row.name} / ${row.currentPositionName || row.currentPositionCode || ""} → ${row.newPositionName || row.newPositionCode || ""} / ${row.changeType || ""}`;

    const basicRows = [
        positionChangeDetailFieldRow("识别类型", row.changeType),
        `<tr><th>是否处理</th><td colspan="3">${row.rollbackEligible ? "已处理" : ""}</td></tr>`,
        positionChangeDetailFieldRow("任职年月", row.positionStartYearMonth),
        positionChangeDetailFieldRow("执行年月", row.effectivePeriod),
        positionChangeDetailFieldRow("标准年月", row.salaryStandardYearMonth),
        positionChangeDetailFieldRow("新职务级别范围",
            row.newPositionMinimumLevel || row.newPositionMaximumLevel
                ? `${row.newPositionMinimumLevel || "-"} ~ ${row.newPositionMaximumLevel || "-"}`
                : ""),
        positionChangeDetailFlagRow("序列转换", row.sequenceConversion),
        positionChangeDetailFlagRow("警员套改", row.policeOfficerConversion),
        positionChangeDetailFlagRow("法检套改", row.judicialConversion),
        positionChangeDetailFlagRow("职级套改", row.rankConversion),
        positionChangeDetailFlagRow("事业岗位变动", row.institutionPositionChange),
        positionChangeDetailFlagRow("警员高套晋升", row.policeHighPositionPromotion),
        positionChangeDetailFlagRow("跨层晋升", row.rankHighPositionPromotion),
        positionChangeDetailFieldRow("警员平套",
            row.policeSameRankLevel || row.policeSameRankStep
                ? `${row.policeSameRankLevel || "-"} / ${row.policeSameRankStep || "-"}`
                : ""),
        positionChangeDetailFieldRow("综合管理类回放",
            row.administrativeReplayLevel || row.administrativeReplayStep
                ? `${row.administrativeReplayLevel || "-"} / ${row.administrativeReplayStep || "-"}`
                : ""),
        positionChangeDetailFieldRow("事业薪级",
            row.institutionStartSalaryLevel || row.institutionPromotedSalaryLevel
                ? `${row.institutionStartSalaryLevel || "-"} → ${row.institutionPromotedSalaryLevel || "-"}`
                : ""),
        positionChangeDetailFlagRow("级别增资超档差", row.gradeIncreaseExceedsStepDifference),
        positionChangeDetailFieldRow("可处理", row.applyEligible ? "是" : "否"),
        positionChangeDetailFieldRow("可还原", row.rollbackEligible ? "是" : "否"),
    ].filter(Boolean).join("");

    const compareRows = [
        positionChangeDetailCompareRow(
            "职务",
            `${row.currentPositionCode || ""} ${row.currentPositionName || ""}`.trim(),
            `${row.newPositionCode || ""} ${row.newPositionName || ""}`.trim()),
        positionChangeDetailCompareRow("级别", row.currentLevel, row.promotedLevel),
        positionChangeDetailCompareRow("档次", row.currentStep, row.promotedStep),
        positionChangeDetailCompareRow("级别考核起算年", row.levelAssessmentStartYear, row.nextLevelAssessmentStartYear),
        positionChangeDetailCompareRow("档次考核起算年", row.stepAssessmentStartYear, row.nextStepAssessmentStartYear),
        positionChangeDetailMoneyRow(
            "职务工资",
            row.currentPositionSalary,
            row.newPositionSalary,
            row.netPositionSalaryIncrease ?? row.positionSalaryIncrease),
        positionChangeDetailMoneyRow("级别工资", row.currentGradeSalary, row.promotedGradeSalary, row.gradeSalaryIncrease),
        row.pgbcRetainedAmount || row.pgbcOffsetAmount
            ? `<tr><td>PGBC</td><td>保留 ${money(row.pgbcRetainedAmount)}</td><td>冲销 ${money(row.pgbcOffsetAmount)}</td><td>-</td></tr>`
            : "",
        positionChangeDetailMoneyRow("合计增资", null, null, row.totalIncrease),
    ].filter(Boolean).join("");

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
            <h4>基本信息</h4>
            <table class="approval-meta-table">
                <tbody>${basicRows}</tbody>
            </table>
            <h4>变动对比</h4>
            <table class="approval-component-table">
                <thead>
                    <tr>
                        <th class="approval-item-header">项目</th>
                        <th>变动前</th>
                        <th>变动后</th>
                        <th>增减</th>
                    </tr>
                </thead>
                <tbody>${compareRows}</tbody>
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
                <td>${escapeHtml(formatEducationPromotionLevelStep(row))}</td>
                <td>${money(row.currentPositionSalary)}</td>
                <td>${money(row.promotedPositionSalary)}</td>
                <td>${money(row.currentGradeSalary)}</td>
                <td>${money(row.promotedGradeSalary)}</td>
                <td>${money(row.positionSalaryIncrease)}</td>
                <td>${money(row.gradeSalaryIncrease)}</td>
                <td>${money(row.totalIncrease)}</td>
                <td>${escapeHtml(row.nextLevelAssessmentStartYear || "")}</td>
                <td>${escapeHtml(row.nextStepAssessmentStartYear || "")}</td>
                <td>${row.eligible ? "是" : "否"}</td>
                <td>${escapeHtml(row.note || "")}</td>
                <td>${renderSimplePromotionActions(row, "education-promotions", "学历晋升", loadEducationPromotions)}</td>
            </tr>
        `).join("");
        bindSimplePromotionActions(rows, loadEducationPromotions);
        status.textContent = `第 ${result.page + 1} / ${Math.max(result.totalPages, 1)} 页，共 ${result.totalElements} 条试算记录`;
    } catch (error) {
        showError(status, error);
    }
}

function formatEducationPromotionLevelStep(row) {
    const level = row.promotedLevel || "";
    const step = row.promotedStep || "";
    const difference = row.promotedGradeStepDifference || "0";
    if (!level && !step) {
        return "";
    }
    if (Number(difference) > 0) {
        return `${level}-${step}+${difference}`;
    }
    return level && step ? `${level}-${step}` : level || step;
}

async function loadRegularizations() {
    const organizationCode = selectedOrganizationCode("regularization-organization-code");
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
                <td>${renderSimplePromotionActions(row, "regularizations", "转正定级", loadRegularizations)}</td>
            </tr>
        `).join("");
        bindSimplePromotionActions(rows, loadRegularizations);
        status.textContent = `第 ${result.page + 1} / ${Math.max(result.totalPages, 1)} 页，共 ${result.totalElements} 条试算记录`;
    } catch (error) {
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
    status.textContent = "正在查询浮动转固定试算...";
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
                <td>${renderSimplePromotionActions(row, "floating-to-fixed-conversions", "浮动转固定", loadFloatingToFixedConversions)}</td>
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
        POLICE_GRADE: "级别工资",
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
        renderBasicStandards(result.content || [], standardType);
        updateBasicStandardCreateButton();
        status.textContent = `第 ${result.page + 1} / ${Math.max(result.totalPages, 1)} 页，共 ${result.totalElements} 条`;
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
    const columns = Object.keys(records[0].values);
    const actionHeader = basicStandardMaintainable(standardType) ? `<th class="standard-write-col">操作</th>` : "";
    head.innerHTML = `<tr>${columns.map(column => `<th>${escapeHtml(column)}</th>`).join("")}${actionHeader}</tr>`;
    body.innerHTML = records.map((record, rowIndex) => {
        const values = record.values;
        const cells = columns.map(column => `<td>${escapeHtml(values[column] ?? "")}</td>`).join("");
        const actionCell = renderBasicStandardActionCell(standardType, values);
        return `<tr data-row-index="${rowIndex}">${cells}${actionCell}</tr>`;
    }).join("");
    bindBasicStandardActions(body, standardType, records);
    updateStandardWriteUi();
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
            <button class="row-action" type="button"
                data-basic-action="grade-delete"
                data-year="${escapeHtml(values.tbnd)}"
                data-code="${escapeHtml(values.jb)}">删除</button>
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
            <button class="row-action" type="button"
                data-basic-action="salary-level-delete"
                data-year="${escapeHtml(values.tbnd)}"
                data-job-category="${escapeHtml(values.gwflbm)}"
                data-level="${escapeHtml(values.xj)}">删除</button>
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
            if (action === "grade-delete") {
                deleteGradeSalaryStandard(button.dataset.year, button.dataset.code);
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
                return;
            }
            if (action === "salary-level-delete") {
                deleteSalaryLevelStandard(button.dataset.year, button.dataset.jobCategory, button.dataset.level);
            }
        });
    });
    void standardType;
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
                ${hasStandardWrite() ? `<td class="standard-write-col">
                    <button class="row-action" type="button" data-allowance-edit="${row.id}">编辑</button>
                    <button class="row-action" type="button" data-allowance-delete="${row.id}">删除</button>
                </td>` : ""}
            </tr>
        `).join("");
        rows.querySelectorAll("button[data-allowance-edit]").forEach(button => {
            button.addEventListener("click", () => editAllowanceStandard(Number(button.dataset.allowanceEdit)));
        });
        rows.querySelectorAll("button[data-allowance-delete]").forEach(button => {
            button.addEventListener("click", () => deleteAllowanceStandard(Number(button.dataset.allowanceDelete)));
        });
        updateStandardWriteUi();
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
                ${hasStandardWrite() ? `<td class="standard-write-col">
                    <button class="row-action" type="button" data-rank-edit="${row.id}">编辑</button>
                    <button class="row-action" type="button" data-rank-delete="${row.id}">删除</button>
                </td>` : ""}
            </tr>
        `).join("");
        rows.querySelectorAll("button[data-rank-edit]").forEach(button => {
            button.addEventListener("click", () => editRankAllowanceStandard(Number(button.dataset.rankEdit)));
        });
        rows.querySelectorAll("button[data-rank-delete]").forEach(button => {
            button.addEventListener("click", () => deleteRankAllowanceStandard(Number(button.dataset.rankDelete)));
        });
        updateStandardWriteUi();
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
                ${hasStandardWrite() ? `<td class="standard-write-col">
                    <button class="row-action" type="button" data-retained-edit="${escapeHtml(row.positionCode)}">编辑</button>
                    <button class="row-action" type="button" data-retained-delete="${escapeHtml(row.positionCode)}">删除</button>
                </td>` : ""}
            </tr>
        `).join("");
        rows.querySelectorAll("button[data-retained-edit]").forEach(button => {
            button.addEventListener("click", () => editRetainedAllowanceStandard(button.dataset.retainedEdit));
        });
        rows.querySelectorAll("button[data-retained-delete]").forEach(button => {
            button.addEventListener("click", () => deleteRetainedAllowanceStandard(button.dataset.retainedDelete));
        });
        updateStandardWriteUi();
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
        rows.innerHTML = (result.content || []).map((row, rowIndex) => `
            <tr data-row-index="${rowIndex}">
                <td>${escapeHtml(row.positionCode)}</td>
                <td>${escapeHtml(row.appointmentYearsLower)}</td>
                <td>${escapeHtml(row.appointmentYearsUpper)}</td>
                <td>${escapeHtml(row.reformYearsLower)}</td>
                <td>${escapeHtml(row.reformYearsUpper)}</td>
                <td>${escapeHtml(row.convertedLevel)}</td>
                <td>${escapeHtml(row.convertedStep)}</td>
                ${hasStandardWrite() ? `<td class="standard-write-col">
                    <button class="row-action" type="button" data-wage-reform-edit="${rowIndex}">编辑</button>
                    <button class="row-action" type="button" data-wage-reform-delete="${rowIndex}">删除</button>
                </td>` : ""}
            </tr>
        `).join("");
        const content = result.content || [];
        rows.querySelectorAll("button[data-wage-reform-edit]").forEach(button => {
            button.addEventListener("click", () => {
                const row = content[Number(button.dataset.wageReformEdit)];
                if (row) {
                    openWageReformStandardModal("edit", row);
                }
            });
        });
        rows.querySelectorAll("button[data-wage-reform-delete]").forEach(button => {
            button.addEventListener("click", () => {
                const row = content[Number(button.dataset.wageReformDelete)];
                if (row) {
                    deleteWageReformStandard(row);
                }
            });
        });
        updateStandardWriteUi();
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
        rows.innerHTML = (result.content || []).map((row, rowIndex) => `
            <tr data-row-index="${rowIndex}">
                <td>${escapeHtml(otherAllowanceTypeName(row.standardType))}</td>
                <td>${escapeHtml(row.standardYearMonth || "")}</td>
                <td>${escapeHtml(row.code || "")}</td>
                <td>${escapeHtml(row.name || "")}</td>
                <td>${money(row.amount)}</td>
                <td>${money(row.averageAmount)}</td>
                <td>${escapeHtml(row.multiplier ?? "")}</td>
                ${hasStandardWrite() ? `<td class="standard-write-col">
                    <button class="row-action" type="button" data-other-allowance-edit="${rowIndex}">编辑</button>
                    <button class="row-action" type="button" data-other-allowance-delete="${rowIndex}">删除</button>
                </td>` : ""}
            </tr>
        `).join("");
        const content = result.content || [];
        rows.querySelectorAll("button[data-other-allowance-edit]").forEach(button => {
            button.addEventListener("click", () => {
                const row = content[Number(button.dataset.otherAllowanceEdit)];
                if (row) {
                    openOtherAllowanceStandardModal("edit", row);
                }
            });
        });
        rows.querySelectorAll("button[data-other-allowance-delete]").forEach(button => {
            button.addEventListener("click", () => {
                const row = content[Number(button.dataset.otherAllowanceDelete)];
                if (row) {
                    deleteOtherAllowanceStandardRecord(row);
                }
            });
        });
        updateStandardWriteUi();
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
    if (isLoginRedirectResponse(response)) {
        window.location.href = "/login.html";
        throw new Error("需要登录");
    }
    if (!response.ok) {
        throw new Error(await readErrorMessage(response));
    }
    const contentType = response.headers.get("content-type") || "";
    if (!contentType.includes("json")) {
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
        throw new Error(await readErrorMessage(response));
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
    if (isLoginRedirectResponse(response)) {
        window.location.href = "/login.html";
        throw new Error("登录已失效，请重新登录后再操作。");
    }
    if (!response.ok) {
        throw new Error(await readErrorMessage(response));
    }
    const contentType = response.headers.get("content-type") || "";
    if (!contentType.includes("json")) {
        window.location.href = "/login.html";
        throw new Error("登录已失效，请重新登录后再操作。");
    }
    return response.json();
}

function isLoginRedirectResponse(response) {
    return response.redirected && String(response.url || "").includes("/login.html");
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

async function loadDataExchange() {
    const personnelStatus = document.getElementById("data-exchange-personnel-status");
    const annualStatus = document.getElementById("data-exchange-annual-status");
    personnelStatus.textContent = "准备就绪";
    annualStatus.textContent = "准备就绪";
    renderDataExchangeDispatchOrganizations();
    renderDataExchangeSubmissionOrganizations();
    renderDataExchangeApprovalOrganizations();
    showDataExchangeTab("dispatch");
}

function showDataExchangeTab(tab) {
    const panels = {
        dispatch: "data-exchange-dispatch-panel",
        receive: "data-exchange-receive-panel",
        "submission-export": "data-exchange-submission-export-panel",
        "submission-review": "data-exchange-submission-review-panel",
        "approval-dispatch": "data-exchange-approval-dispatch-panel",
        "approval-receive": "data-exchange-approval-receive-panel",
        personnel: "data-exchange-personnel-panel",
        annual: "data-exchange-annual-panel",
    };
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
        container.innerHTML = "<span>尚未选择下发单位</span>";
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
    status.textContent = "正在生成下发包...";
    try {
        const response = await fetchDispatchPackage(true);
        const blob = new Blob([JSON.stringify(response, null, 2)], { type: "application/json;charset=UTF-8" });
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement("a");
        a.href = url;
        a.download = `rsgzgl_personnel_package_${new Date().toISOString().slice(0, 10)}.json`;
        document.body.appendChild(a);
        a.click();
        window.URL.revokeObjectURL(url);
        document.body.removeChild(a);
        status.textContent = `下发包已生成，共 ${response.personnel?.length || 0} 人`;
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
        approvalStatus: row.jzgb || row.JZGB || "",
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
    status.className = "status";
    status.textContent = "正在解析申报包...";
    rows.innerHTML = "";
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
        rows.innerHTML = (result.previewRows || []).map(row => `
            <tr>
                <td><input type="checkbox" data-submission-review-select value="${escapeHtml(row.organizationCode)}|${escapeHtml(row.personCode)}"></td>
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

async function applyDataExchangeSubmissionReview(decision, dryRun = false) {
    const status = document.getElementById("data-exchange-submission-review-status");
    status.className = "status";
    const selected = selectedExchangeKeys("[data-submission-review-select]:checked");
    const confirmMessage = dryRun
        ? null
        : decision === "REJECT"
            ? selected.length
                ? `将退回 ${selected.length} 条申报记录，不会写入数据库，是否继续？`
                : "将退回申报包中的全部申报记录，不会写入数据库，是否继续？"
            : selected.length
                ? `将审核通过 ${selected.length} 条申报记录，并替换对应工资变动数据，是否继续？`
                : "将审核通过申报包中的全部申报记录，并替换对应工资变动数据，是否继续？";
    if (confirmMessage && !window.confirm(confirmMessage)) {
        return;
    }
    status.textContent = dryRun
        ? "正在试运行审核..."
        : decision === "REJECT"
            ? "正在退回申报..."
            : "正在审核通过并写入数据...";
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
        status.textContent = result.message || `已处理 ${result.processedRecords || 0} 条申报记录`;
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
        <div>共 ${summary.totalRecords || 0} 人，新增 ${summary.newRecords || 0}，替换 ${summary.replaceRecords || 0}，工资记录 ${summary.payrollRecords || 0} 条</div>
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
        const payload = await fetchApprovalPackage(false);
        state.dataExchangeApprovalRows = payload.personnel || [];
        state.dataExchangeApprovalPayrollTables = payload.payrollTables || [];
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
                <td>${escapeHtml(payroll.approvalStatus || "是")}</td>
                <td>${escapeHtml(payroll.remark || "")}</td>
            </tr>
        `;
        }).join("");
        status.textContent = `审批下发预览 ${state.dataExchangeApprovalRows.length} 人；仅包含当前已审记录，勾选后可只下发勾选人员。`;
    } catch (error) {
        showError(status, error);
    }
}

async function downloadApprovalPackage() {
    const status = document.getElementById("data-exchange-approval-dispatch-status");
    status.className = "status";
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
            throw new Error(await response.text() || `HTTP ${response.status}`);
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

async function fetchApprovalPackage(onlySelected) {
    return postJson("/api/data-exchange/approval/preview", buildApprovalRequestPayload(onlySelected));
}

function buildApprovalRequestPayload(onlySelected) {
    const selectedKeys = onlySelected ? selectedExchangeKeys("[data-approval-select]:checked") : [];
    return {
        organizationCodes: state.dataExchangeApprovalOrganizations.map(item => item.code),
        includeDescendants: document.getElementById("data-exchange-approval-include-descendants").checked,
        keyword: document.getElementById("data-exchange-approval-keyword").value.trim() || null,
        selectedPersonnel: selectedKeys,
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
    const confirmMessage = dryRun
        ? null
        : selected.length
            ? `将接收 ${selected.length} 条审批记录并替换本地工资变动数据，是否继续？`
            : "将接收审批包中的全部审批记录并替换本地工资变动数据，是否继续？";
    if (confirmMessage && !window.confirm(confirmMessage)) {
        return;
    }
    status.textContent = dryRun ? "正在试运行审批接收..." : "正在接收审批数据...";
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
    if (onlySelected) {
        const response = await fetch("/api/data-exchange/dispatch/personnel", {
            method: "POST",
            headers: { Accept: "application/json", "Content-Type": "application/json" },
            body: JSON.stringify(payload),
        });
        if (!response.ok) {
            throw new Error(await response.text() || `HTTP ${response.status}`);
        }
        const text = await response.text();
        return JSON.parse(text);
    }
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
        status.textContent = `已选择数据包：${file.name}，可点击“预览接收”。`;
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
        const result = await postJson("/api/data-exchange/receive/preview", {
            packageJson,
            mode: selectedOrganizationCode("data-exchange-receive-target-organization") ? "APPEND" : "REPLACE",
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

async function applyDataExchangeReceiveDryRun() {
    const appendMode = Boolean(selectedOrganizationCode("data-exchange-receive-target-organization"));
    await applyDataExchangeReceive(appendMode ? "APPEND" : "REPLACE", true);
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
        : "将整体接收数据包并替换本地相同单位编码和个人编码数据，是否继续？";
    if (confirmMessage && !window.confirm(confirmMessage)) {
        return;
    }
    status.textContent = dryRun ? "正在试运行接收校验..." : mode === "APPEND" ? "正在追加接收勾选人员..." : "正在整体接收并替换数据...";
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
    return Array.from(document.querySelectorAll(selector)).map(input => {
        const [organizationCode, personCode] = String(input.value || "").split("|");
        return { organizationCode, personCode };
    });
}

function selectedOrganizationCode(inputId) {
    const input = document.getElementById(inputId);
    if (!input) {
        return "";
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
    const keyword = document.getElementById("data-exchange-personnel-keyword").value;
    const status = document.getElementById("data-exchange-personnel-status");
    const tbody = document.getElementById("data-exchange-personnel-rows");

    status.className = "status";
    status.textContent = "正在查询...";

    try {
        const data = await getJson(`/api/data-exchange/personnel?organizationCode=${encodeURIComponent(organizationCode)}&keyword=${encodeURIComponent(keyword)}&page=0&size=50`);
        tbody.innerHTML = data.content.map(r => `
            <tr>
                <td>${escapeHtml(r.organizationName || r.organizationCode)}</td>
                <td>${escapeHtml(r.personCode)}</td>
                <td>${escapeHtml(r.name)}</td>
                <td>${escapeHtml(r.idCard ? r.idCard.substring(0, 4) + "****" + r.idCard.substring(r.idCard.length - 4) : "")}</td>
                <td>${escapeHtml(r.gender)}</td>
                <td>${escapeHtml(r.birthYearMonth)}</td>
                <td>${escapeHtml(r.personnelCategory)}</td>
                <td>${escapeHtml(r.currentJob)}</td>
                <td>${escapeHtml(r.currentGrade)}</td>
            </tr>
        `).join("");
        status.textContent = `查询完成，共 ${data.totalElements} 条记录`;
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
        status.textContent = "下载完成";
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
