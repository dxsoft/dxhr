import pathlib
import re

ROOT = pathlib.Path(__file__).resolve().parents[1]
CONTROLLERS = [
    ROOT / "src/main/java/com/dxsoft/rsgzgl/payroll/PayrollController.java",
    ROOT / "src/main/java/com/dxsoft/rsgzgl/payroll/StandardMaintenanceController.java",
]
SERVICE = ROOT / "src/main/java/com/dxsoft/rsgzgl/payroll/PayrollService.java"

STUBS = {
    "prosecutionAllowanceAdjustments": "public PageResponse<RankAllowanceStandardAdjustment> prosecutionAllowanceAdjustments(String organizationCode, String keyword, PageRequest pageRequest) { return unsupportedPage(); }",
    "judicialAllowanceAdjustments": "public PageResponse<RankAllowanceStandardAdjustment> judicialAllowanceAdjustments(String organizationCode, String keyword, PageRequest pageRequest) { return unsupportedPage(); }",
    "policeAllowanceAdjustments": "public PageResponse<RankAllowanceStandardAdjustment> policeAllowanceAdjustments(String organizationCode, String keyword, PageRequest pageRequest) { return unsupportedPage(); }",
    "supervisionAllowanceAdjustments": "public PageResponse<RankAllowanceStandardAdjustment> supervisionAllowanceAdjustments(String organizationCode, String keyword, PageRequest pageRequest) { return unsupportedPage(); }",
    "reformLevelRollingPreviews": "public PageResponse<ReformLevelRollingPreview> reformLevelRollingPreviews(String organizationCode, String keyword, String year, Boolean includeApply, Boolean includeProcessed, PageRequest pageRequest) { return unsupportedPage(); }",
    "applyReformLevelRolling": "public PromotionActionResult applyReformLevelRolling(String payrollHistoryId, String year) { return unsupportedAction(\"applyReformLevelRolling\"); }",
    "rollbackReformLevelRolling": "public PromotionActionResult rollbackReformLevelRolling(String payrollHistoryId) { return unsupportedAction(\"rollbackReformLevelRolling\"); }",
    "regularizationHighGradePreviews": "public PageResponse<RegularizationHighGradePreview> regularizationHighGradePreviews(String organizationCode, String keyword, PageRequest pageRequest) { return unsupportedPage(); }",
    "applyRegularizationHighGrade": "public PromotionActionResult applyRegularizationHighGrade(String payrollHistoryId) { return unsupportedAction(\"applyRegularizationHighGrade\"); }",
    "rollbackRegularizationHighGrade": "public PromotionActionResult rollbackRegularizationHighGrade(String payrollHistoryId) { return unsupportedAction(\"rollbackRegularizationHighGrade\"); }",
    "monthlyAverageSalaryPreviews": "public PageResponse<MonthlyAverageSalaryPreview> monthlyAverageSalaryPreviews(String organizationCode, String keyword, String year, PageRequest pageRequest) { return unsupportedPage(); }",
    "applyMonthlyAverageSalary": "public PromotionActionResult applyMonthlyAverageSalary(String payrollHistoryId, String year) { return unsupportedAction(\"applyMonthlyAverageSalary\"); }",
    "rollbackMonthlyAverageSalary": "public PromotionActionResult rollbackMonthlyAverageSalary(String payrollHistoryId, String year) { return unsupportedAction(\"rollbackMonthlyAverageSalary\"); }",
    "wageReform2006Previews": "public PageResponse<WageReform2006Preview> wageReform2006Previews(String organizationCode, String keyword, PageRequest pageRequest) { return unsupportedPage(); }",
    "applyWageReform2006": "public PromotionActionResult applyWageReform2006(int uid) { return unsupportedAction(\"applyWageReform2006\"); }",
    "backfillWageReform2006Dtgxx": "public PromotionActionResult backfillWageReform2006Dtgxx(int uid) { return unsupportedAction(\"backfillWageReform2006Dtgxx\"); }",
    "rollbackWageReform2006Dtgxx": "public PromotionActionResult rollbackWageReform2006Dtgxx(int uid) { return unsupportedAction(\"rollbackWageReform2006Dtgxx\"); }",
    "rollbackWageReform2006": "public PromotionActionResult rollbackWageReform2006(String payrollHistoryId) { return unsupportedAction(\"rollbackWageReform2006\"); }",
    "applyEducationPromotion": "public PromotionActionResult applyEducationPromotion(String payrollHistoryId) { return unsupportedAction(\"applyEducationPromotion\"); }",
    "rollbackEducationPromotion": "public PromotionActionResult rollbackEducationPromotion(String payrollHistoryId) { return unsupportedAction(\"rollbackEducationPromotion\"); }",
    "applyRegularization": "public PromotionActionResult applyRegularization(String payrollHistoryId) { return unsupportedAction(\"applyRegularization\"); }",
    "rollbackRegularization": "public PromotionActionResult rollbackRegularization(String payrollHistoryId) { return unsupportedAction(\"rollbackRegularization\"); }",
    "applyTeachingAllowanceAdjustment": "public PromotionActionResult applyTeachingAllowanceAdjustment(String payrollHistoryId) { return unsupportedAction(\"applyTeachingAllowanceAdjustment\"); }",
    "rollbackTeachingAllowanceAdjustment": "public PromotionActionResult rollbackTeachingAllowanceAdjustment(String payrollHistoryId) { return unsupportedAction(\"rollbackTeachingAllowanceAdjustment\"); }",
    "applyProsecutionAllowanceAdjustment": "public PromotionActionResult applyProsecutionAllowanceAdjustment(String payrollHistoryId) { return unsupportedAction(\"applyProsecutionAllowanceAdjustment\"); }",
    "rollbackProsecutionAllowanceAdjustment": "public PromotionActionResult rollbackProsecutionAllowanceAdjustment(String payrollHistoryId) { return unsupportedAction(\"rollbackProsecutionAllowanceAdjustment\"); }",
    "applyJudicialAllowanceAdjustment": "public PromotionActionResult applyJudicialAllowanceAdjustment(String payrollHistoryId) { return unsupportedAction(\"applyJudicialAllowanceAdjustment\"); }",
    "rollbackJudicialAllowanceAdjustment": "public PromotionActionResult rollbackJudicialAllowanceAdjustment(String payrollHistoryId) { return unsupportedAction(\"rollbackJudicialAllowanceAdjustment\"); }",
    "applyPoliceAllowanceAdjustment": "public PromotionActionResult applyPoliceAllowanceAdjustment(String payrollHistoryId) { return unsupportedAction(\"applyPoliceAllowanceAdjustment\"); }",
    "rollbackPoliceAllowanceAdjustment": "public PromotionActionResult rollbackPoliceAllowanceAdjustment(String payrollHistoryId) { return unsupportedAction(\"rollbackPoliceAllowanceAdjustment\"); }",
    "applySupervisionAllowanceAdjustment": "public PromotionActionResult applySupervisionAllowanceAdjustment(String payrollHistoryId) { return unsupportedAction(\"applySupervisionAllowanceAdjustment\"); }",
    "rollbackSupervisionAllowanceAdjustment": "public PromotionActionResult rollbackSupervisionAllowanceAdjustment(String payrollHistoryId) { return unsupportedAction(\"rollbackSupervisionAllowanceAdjustment\"); }",
    "policeRankChangePromotions": "public PageResponse<RankAllowanceChangePromotion> policeRankChangePromotions(String organizationCode, String keyword, PageRequest pageRequest) { return unsupportedPage(); }",
    "prosecutionRankChangePromotions": "public PageResponse<RankAllowanceChangePromotion> prosecutionRankChangePromotions(String organizationCode, String keyword, PageRequest pageRequest) { return unsupportedPage(); }",
    "judicialRankChangePromotions": "public PageResponse<RankAllowanceChangePromotion> judicialRankChangePromotions(String organizationCode, String keyword, PageRequest pageRequest) { return unsupportedPage(); }",
    "supervisionRankChangePromotions": "public PageResponse<RankAllowanceChangePromotion> supervisionRankChangePromotions(String organizationCode, String keyword, PageRequest pageRequest) { return unsupportedPage(); }",
    "applyPoliceRankChangePromotion": "public PromotionActionResult applyPoliceRankChangePromotion(String payrollHistoryId) { return unsupportedAction(\"applyPoliceRankChangePromotion\"); }",
    "rollbackPoliceRankChangePromotion": "public PromotionActionResult rollbackPoliceRankChangePromotion(String payrollHistoryId) { return unsupportedAction(\"rollbackPoliceRankChangePromotion\"); }",
    "applyProsecutionRankChangePromotion": "public PromotionActionResult applyProsecutionRankChangePromotion(String payrollHistoryId) { return unsupportedAction(\"applyProsecutionRankChangePromotion\"); }",
    "rollbackProsecutionRankChangePromotion": "public PromotionActionResult rollbackProsecutionRankChangePromotion(String payrollHistoryId) { return unsupportedAction(\"rollbackProsecutionRankChangePromotion\"); }",
    "applyJudicialRankChangePromotion": "public PromotionActionResult applyJudicialRankChangePromotion(String payrollHistoryId) { return unsupportedAction(\"applyJudicialRankChangePromotion\"); }",
    "rollbackJudicialRankChangePromotion": "public PromotionActionResult rollbackJudicialRankChangePromotion(String payrollHistoryId) { return unsupportedAction(\"rollbackJudicialRankChangePromotion\"); }",
    "applySupervisionRankChangePromotion": "public PromotionActionResult applySupervisionRankChangePromotion(String payrollHistoryId) { return unsupportedAction(\"applySupervisionRankChangePromotion\"); }",
    "rollbackSupervisionRankChangePromotion": "public PromotionActionResult rollbackSupervisionRankChangePromotion(String payrollHistoryId) { return unsupportedAction(\"rollbackSupervisionRankChangePromotion\"); }",
    "otherPayrollChanges": "public PageResponse<OtherPayrollChangePreview> otherPayrollChanges(String organizationCode, String keyword, PageRequest pageRequest) { return unsupportedPage(); }",
    "applyOtherPayrollChange": "public PromotionActionResult applyOtherPayrollChange(String payrollHistoryId, PayrollHistoryMaintenanceRequest request) { return unsupportedAction(\"applyOtherPayrollChange\"); }",
    "rollbackOtherPayrollChange": "public PromotionActionResult rollbackOtherPayrollChange(String payrollHistoryId) { return unsupportedAction(\"rollbackOtherPayrollChange\"); }",
    "salaryStandardAdjustments": "public PageResponse<SalaryStandardAdjustmentPreview> salaryStandardAdjustments(String organizationCode, String keyword, String targetStandardYearMonth, String scope, PageRequest pageRequest) { return unsupportedPage(); }",
    "basicSalaryStandardAdjustments": "public PageResponse<SalaryStandardAdjustmentPreview> basicSalaryStandardAdjustments(String organizationCode, String keyword, String targetStandardYearMonth, PageRequest pageRequest) { return unsupportedPage(); }",
    "applyBasicSalaryStandardAdjustment": "public PromotionActionResult applyBasicSalaryStandardAdjustment(String payrollHistoryId, String targetStandardYearMonth) { return unsupportedAction(\"applyBasicSalaryStandardAdjustment\"); }",
    "rollbackBasicSalaryStandardAdjustment": "public PromotionActionResult rollbackBasicSalaryStandardAdjustment(String payrollHistoryId) { return unsupportedAction(\"rollbackBasicSalaryStandardAdjustment\"); }",
    "civilAllowanceStandardAdjustments": "public PageResponse<SalaryStandardAdjustmentPreview> civilAllowanceStandardAdjustments(String organizationCode, String keyword, String targetStandardYearMonth, PageRequest pageRequest) { return unsupportedPage(); }",
    "applyCivilAllowanceStandardAdjustment": "public PromotionActionResult applyCivilAllowanceStandardAdjustment(String payrollHistoryId, String targetStandardYearMonth) { return unsupportedAction(\"applyCivilAllowanceStandardAdjustment\"); }",
    "rollbackCivilAllowanceStandardAdjustment": "public PromotionActionResult rollbackCivilAllowanceStandardAdjustment(String payrollHistoryId) { return unsupportedAction(\"rollbackCivilAllowanceStandardAdjustment\"); }",
    "performanceStandardAdjustments": "public PageResponse<SalaryStandardAdjustmentPreview> performanceStandardAdjustments(String organizationCode, String keyword, String targetStandardYearMonth, PageRequest pageRequest) { return unsupportedPage(); }",
    "applyPerformanceStandardAdjustment": "public PromotionActionResult applyPerformanceStandardAdjustment(String payrollHistoryId, String targetStandardYearMonth) { return unsupportedAction(\"applyPerformanceStandardAdjustment\"); }",
    "rollbackPerformanceStandardAdjustment": "public PromotionActionResult rollbackPerformanceStandardAdjustment(String payrollHistoryId) { return unsupportedAction(\"rollbackPerformanceStandardAdjustment\"); }",
    "applySalaryStandardAdjustment": "public PromotionActionResult applySalaryStandardAdjustment(String payrollHistoryId, String targetStandardYearMonth) { return unsupportedAction(\"applySalaryStandardAdjustment\"); }",
    "rollbackSalaryStandardAdjustment": "public PromotionActionResult rollbackSalaryStandardAdjustment(String payrollHistoryId) { return unsupportedAction(\"rollbackSalaryStandardAdjustment\"); }",
    "performanceRatioAdjustments": "public PageResponse<PerformanceRatioAdjustmentPreview> performanceRatioAdjustments(String organizationCode, String keyword, PageRequest pageRequest) { return unsupportedPage(); }",
    "applyPerformanceRatioAdjustment": "public PromotionActionResult applyPerformanceRatioAdjustment(String payrollHistoryId) { return unsupportedAction(\"applyPerformanceRatioAdjustment\"); }",
    "rollbackPerformanceRatioAdjustment": "public PromotionActionResult rollbackPerformanceRatioAdjustment(String payrollHistoryId) { return unsupportedAction(\"rollbackPerformanceRatioAdjustment\"); }",
    "allowanceRecalculations": "public PageResponse<AllowanceRecalculationPreview> allowanceRecalculations(String organizationCode, String keyword, PageRequest pageRequest) { return unsupportedPage(); }",
    "applyAllowanceRecalculation": "public PromotionActionResult applyAllowanceRecalculation(String payrollHistoryId) { return unsupportedAction(\"applyAllowanceRecalculation\"); }",
    "rollbackAllowanceRecalculation": "public PromotionActionResult rollbackAllowanceRecalculation(String payrollHistoryId) { return unsupportedAction(\"rollbackAllowanceRecalculation\"); }",
    "newPersonnelSalaryDeterminations": "public PageResponse<NewPersonnelSalaryPreview> newPersonnelSalaryDeterminations(String organizationCode, String keyword, PageRequest pageRequest) { return unsupportedPage(); }",
    "applyNewPersonnelSalaryDetermination": "public PromotionActionResult applyNewPersonnelSalaryDetermination(int uid) { return unsupportedAction(\"applyNewPersonnelSalaryDetermination\"); }",
    "rollbackNewPersonnelSalaryDetermination": "public PromotionActionResult rollbackNewPersonnelSalaryDetermination(String payrollHistoryId) { return unsupportedAction(\"rollbackNewPersonnelSalaryDetermination\"); }",
    "internSalaryChanges": "public PageResponse<InternSalaryChangePreview> internSalaryChanges(String organizationCode, String keyword, PageRequest pageRequest) { return unsupportedPage(); }",
    "applyInternSalaryChange": "public PromotionActionResult applyInternSalaryChange(String payrollHistoryId) { return unsupportedAction(\"applyInternSalaryChange\"); }",
    "rollbackInternSalaryChange": "public PromotionActionResult rollbackInternSalaryChange(String payrollHistoryId) { return unsupportedAction(\"rollbackInternSalaryChange\"); }",
    "floatingToFixedPreviews": "public PageResponse<FloatingToFixedPreview> floatingToFixedPreviews(String organizationCode, String keyword, PageRequest pageRequest) { return unsupportedPage(); }",
    "applyFloatingToFixedConversion": "public PromotionActionResult applyFloatingToFixedConversion(String payrollHistoryId) { return unsupportedAction(\"applyFloatingToFixedConversion\"); }",
    "rollbackFloatingToFixedConversion": "public PromotionActionResult rollbackFloatingToFixedConversion(String payrollHistoryId) { return unsupportedAction(\"rollbackFloatingToFixedConversion\"); }",
    "createAllowanceStandard": "public AllowanceStandard createAllowanceStandard(AllowanceStandardRequest request) { requireStandardWritePermission(); int id = payrollRepository.insertAllowanceStandard(request); return payrollRepository.findAllowanceStandardById(id); }",
    "updateAllowanceStandard": "public AllowanceStandard updateAllowanceStandard(int id, AllowanceStandardRequest request) { requireStandardWritePermission(); payrollRepository.updateAllowanceStandard(id, request); return payrollRepository.findAllowanceStandardById(id); }",
    "deleteAllowanceStandard": "public void deleteAllowanceStandard(int id) { requireStandardWritePermission(); payrollRepository.deleteAllowanceStandard(id); }",
    "createRankAllowanceStandard": "public RankAllowanceStandard createRankAllowanceStandard(RankAllowanceStandardRequest request) { requireStandardWritePermission(); int id = payrollRepository.insertRankAllowanceStandard(request); return payrollRepository.findRankAllowanceStandardById(id); }",
    "updateRankAllowanceStandard": "public RankAllowanceStandard updateRankAllowanceStandard(int id, RankAllowanceStandardRequest request) { requireStandardWritePermission(); payrollRepository.updateRankAllowanceStandard(id, request); return payrollRepository.findRankAllowanceStandardById(id); }",
    "deleteRankAllowanceStandard": "public void deleteRankAllowanceStandard(int id) { requireStandardWritePermission(); payrollRepository.deleteRankAllowanceStandard(id); }",
    "createRetainedAllowanceStandard": "public RetainedAllowanceStandard createRetainedAllowanceStandard(RetainedAllowanceStandardRequest request) { requireStandardWritePermission(); payrollRepository.insertRetainedAllowanceStandard(request); return payrollRepository.findRetainedAllowanceStandardByPositionCode(request.positionCode()); }",
    "updateRetainedAllowanceStandard": "public RetainedAllowanceStandard updateRetainedAllowanceStandard(String positionCode, RetainedAllowanceStandardRequest request) { requireStandardWritePermission(); payrollRepository.updateRetainedAllowanceStandard(positionCode, request); return payrollRepository.findRetainedAllowanceStandardByPositionCode(positionCode); }",
    "deleteRetainedAllowanceStandard": "public void deleteRetainedAllowanceStandard(String positionCode) { requireStandardWritePermission(); payrollRepository.deleteRetainedAllowanceStandard(positionCode); }",
    "createYearAllowanceStandard": "public YearAllowanceStandard createYearAllowanceStandard(YearAllowanceStandardRequest request) { requireStandardWritePermission(); payrollRepository.insertYearAllowanceStandard(request); return payrollRepository.findYearAllowanceStandardByYearMonth(request.standardYearMonth()); }",
    "updateYearAllowanceStandard": "public YearAllowanceStandard updateYearAllowanceStandard(String standardYearMonth, YearAllowanceStandardRequest request) { requireStandardWritePermission(); payrollRepository.updateYearAllowanceStandard(standardYearMonth, request); return payrollRepository.findYearAllowanceStandardByYearMonth(standardYearMonth); }",
    "deleteYearAllowanceStandard": "public void deleteYearAllowanceStandard(String standardYearMonth) { requireStandardWritePermission(); payrollRepository.deleteYearAllowanceStandard(standardYearMonth); }",
    "createPositionSalaryStandard": "public PositionSalaryStandard createPositionSalaryStandard(PositionSalaryStandardRequest request) { requireStandardWritePermission(); payrollRepository.insertPositionSalaryStandard(request); return payrollRepository.findPositionSalaryStandard(request.standardYearMonth(), request.positionCode()); }",
    "updatePositionSalaryStandard": "public PositionSalaryStandard updatePositionSalaryStandard(String standardYearMonth, String positionCode, PositionSalaryStandardRequest request) { requireStandardWritePermission(); payrollRepository.updatePositionSalaryStandard(standardYearMonth, positionCode, request); return payrollRepository.findPositionSalaryStandard(standardYearMonth, positionCode); }",
    "deletePositionSalaryStandard": "public void deletePositionSalaryStandard(String standardYearMonth, String positionCode) { requireStandardWritePermission(); payrollRepository.deletePositionSalaryStandard(standardYearMonth, positionCode); }",
    "createGradeSalaryStandard": "public GradeSalaryStandard createGradeSalaryStandard(GradeSalaryStandardRequest request) { requireStandardWritePermission(); payrollRepository.insertGradeSalaryStandard(request); return payrollRepository.findGradeSalaryStandard(request.standardYearMonth(), request.gradeLevel()); }",
    "updateGradeSalaryStandard": "public GradeSalaryStandard updateGradeSalaryStandard(String standardYearMonth, String gradeLevel, GradeSalaryStandardRequest request) { requireStandardWritePermission(); payrollRepository.updateGradeSalaryStandard(standardYearMonth, gradeLevel, request); return payrollRepository.findGradeSalaryStandard(standardYearMonth, gradeLevel); }",
    "deleteGradeSalaryStandard": "public void deleteGradeSalaryStandard(String standardYearMonth, String gradeLevel) { requireStandardWritePermission(); payrollRepository.deleteGradeSalaryStandard(standardYearMonth, gradeLevel); }",
    "createPositionGradeSalaryStandard": "public PositionGradeSalaryStandard createPositionGradeSalaryStandard(PositionGradeSalaryStandardRequest request) { requireStandardWritePermission(); payrollRepository.insertPositionGradeSalaryStandard(request); return payrollRepository.findPositionGradeSalaryStandard(request.standardYearMonth(), request.positionCode()); }",
    "updatePositionGradeSalaryStandard": "public PositionGradeSalaryStandard updatePositionGradeSalaryStandard(String standardYearMonth, String positionCode, PositionGradeSalaryStandardRequest request) { requireStandardWritePermission(); payrollRepository.updatePositionGradeSalaryStandard(standardYearMonth, positionCode, request); return payrollRepository.findPositionGradeSalaryStandard(standardYearMonth, positionCode); }",
    "deletePositionGradeSalaryStandard": "public void deletePositionGradeSalaryStandard(String standardYearMonth, String positionCode) { requireStandardWritePermission(); payrollRepository.deletePositionGradeSalaryStandard(standardYearMonth, positionCode); }",
    "createSalaryLevelStandard": "public SalaryLevelStandard createSalaryLevelStandard(SalaryLevelStandardRequest request) { requireStandardWritePermission(); payrollRepository.insertSalaryLevelStandard(request); return payrollRepository.findSalaryLevelStandard(request.standardYearMonth(), request.jobCategoryCode(), request.salaryLevel()); }",
    "updateSalaryLevelStandard": "public SalaryLevelStandard updateSalaryLevelStandard(String standardYearMonth, String jobCategoryCode, String salaryLevel, SalaryLevelStandardRequest request) { requireStandardWritePermission(); payrollRepository.updateSalaryLevelStandard(standardYearMonth, jobCategoryCode, salaryLevel, request); return payrollRepository.findSalaryLevelStandard(standardYearMonth, jobCategoryCode, salaryLevel); }",
    "deleteSalaryLevelStandard": "public void deleteSalaryLevelStandard(String standardYearMonth, String jobCategoryCode, String salaryLevel) { requireStandardWritePermission(); payrollRepository.deleteSalaryLevelStandard(standardYearMonth, jobCategoryCode, salaryLevel); }",
    "createInternSalaryStandard": "public InternSalaryStandard createInternSalaryStandard(InternSalaryStandardRequest request) { requireStandardWritePermission(); payrollRepository.insertInternSalaryStandard(request); return payrollRepository.findInternSalaryStandardByKey(request.standardYearMonth(), request.educationCode(), request.regularPositionCode()); }",
    "updateInternSalaryStandard": "public InternSalaryStandard updateInternSalaryStandard(String standardYearMonth, String educationCode, String regularPositionCode, InternSalaryStandardRequest request) { requireStandardWritePermission(); payrollRepository.updateInternSalaryStandard(standardYearMonth, educationCode, regularPositionCode, request); return payrollRepository.findInternSalaryStandardByKey(standardYearMonth, educationCode, regularPositionCode); }",
    "deleteInternSalaryStandard": "public void deleteInternSalaryStandard(String standardYearMonth, String educationCode, String regularPositionCode) { requireStandardWritePermission(); payrollRepository.deleteInternSalaryStandard(standardYearMonth, educationCode, regularPositionCode); }",
    "createWageReformStandard": "public WageReformStandard createWageReformStandard(WageReformStandardRequest request) { requireStandardWritePermission(); payrollRepository.insertWageReformStandard(request); return payrollRepository.findWageReformStandardByKey(request.positionCode(), request.appointmentYearsLower(), request.appointmentYearsUpper(), request.reformYearsLower(), request.reformYearsUpper()); }",
    "updateWageReformStandard": "public WageReformStandard updateWageReformStandard(String positionCode, int appointmentYearsLower, int appointmentYearsUpper, int reformYearsLower, int reformYearsUpper, WageReformStandardRequest request) { requireStandardWritePermission(); payrollRepository.updateWageReformStandard(positionCode, appointmentYearsLower, appointmentYearsUpper, reformYearsLower, reformYearsUpper, request); return payrollRepository.findWageReformStandardByKey(positionCode, appointmentYearsLower, appointmentYearsUpper, reformYearsLower, reformYearsUpper); }",
    "deleteWageReformStandard": "public void deleteWageReformStandard(String positionCode, int appointmentYearsLower, int appointmentYearsUpper, int reformYearsLower, int reformYearsUpper) { requireStandardWritePermission(); payrollRepository.deleteWageReformStandard(positionCode, appointmentYearsLower, appointmentYearsUpper, reformYearsLower, reformYearsUpper); }",
    "createOtherAllowanceStandard": "public OtherAllowanceStandard createOtherAllowanceStandard(OtherAllowanceStandardRequest request) { requireStandardWritePermission(); payrollRepository.insertOtherAllowanceStandard(request); return payrollRepository.findOtherAllowanceStandardByKey(request.standardType(), request.standardYearMonth(), request.code()); }",
    "updateOtherAllowanceStandard": "public OtherAllowanceStandard updateOtherAllowanceStandard(String standardType, String standardYearMonth, String code, OtherAllowanceStandardRequest request) { requireStandardWritePermission(); OtherAllowanceStandardRequest merged = new OtherAllowanceStandardRequest(standardType, standardYearMonth != null ? standardYearMonth : request.standardYearMonth(), code != null ? code : request.code(), request.name(), request.amount(), request.averageAmount(), request.multiplier()); payrollRepository.updateOtherAllowanceStandard(standardType, merged); return payrollRepository.findOtherAllowanceStandardByKey(standardType, merged.standardYearMonth(), merged.code()); }",
    "deleteOtherAllowanceStandard": "public void deleteOtherAllowanceStandard(String standardType, String standardYearMonth, String code) { requireStandardWritePermission(); payrollRepository.deleteOtherAllowanceStandard(standardType, standardYearMonth, code); }",
}


def existing_methods(text: str) -> set[str]:
    return set(re.findall(r"public\s+[\w<>,\[\]\s]+\s+(\w+)\s*\(", text))


def called_methods() -> set[str]:
    names: set[str] = set()
    for path in CONTROLLERS:
        text = path.read_text(encoding="utf-8")
        names.update(re.findall(r"payrollService\.(\w+)\s*\(", text))
    return names


def main() -> None:
    service_text = SERVICE.read_text(encoding="utf-8")
    missing = sorted(name for name in called_methods() if name not in existing_methods(service_text))
    lines = [
        "",
        "    private void requireStandardWritePermission() {",
        "        accessControlService.requireAuthority(\"STANDARD_WRITE\");",
        "    }",
        "",
        "    private PromotionActionResult unsupportedAction(String operation) {",
        "        throw new UnsupportedOperationException(\"Not implemented: \" + operation);",
        "    }",
        "",
        "    private <T> PageResponse<T> unsupportedPage() {",
        "        throw new UnsupportedOperationException(\"Not implemented\");",
        "    }",
        "",
        "    public PromotionActionResult applyPositionChangePromotion(String payrollHistoryId, PositionChangeApplyRequest request) {",
        "        return applyPositionChangePromotion(payrollHistoryId);",
        "    }",
        "",
    ]
    for name in missing:
        if name not in STUBS:
            raise SystemExit(f"Missing stub definition for {name}")
        lines.append("    @Transactional")
        lines.append(f"    {STUBS[name]}")
        lines.append("")
    out = ROOT / "tmp-payroll-stubs.java"
    out.write_text("\n".join(lines), encoding="utf-8")
    print(f"missing={len(missing)} written to {out}")


if __name__ == "__main__":
    main()
