import re
import pathlib

ROOT = pathlib.Path(__file__).resolve().parents[1]
js = (ROOT / "src/main/resources/static/app.js").read_text(encoding="utf-8")
start = js.index('document.addEventListener("DOMContentLoaded"')
end = js.index("async function initializeAuth")
block = js[start:end]

defined = set(re.findall(r"^(?:async )?function (\w+)", js, re.M))
defined.update(re.findall(r"^const (\w+) = (?:async )?\(", js, re.M))

handlers = re.findall(
    r"addEventListener\(\s*\"(?:submit|click|change|input)\",\s*(?:\(\)\s*=>\s*)?(\w+)",
    block,
)
handlers += re.findall(r"addEventListener\(\s*\"(?:submit|click|change|input)\",\s*(\w+)", block)

missing = sorted({name for name in handlers if name not in defined and name not in {"debounceSecurityReload", "loadSecurityAdmin", "closePersonnelChangeMenu", "closePayrollPreviewModal", "closeGradeStandardModal", "closeSalaryLevelStandardModal", "closeInternSalaryStandardModal", "closeWageReformStandardModal", "closeOtherAllowanceStandardModal", "renderAssessmentBatchRows", "loadNormalPromotions", "showDataExchangeTab", "showPersonnelTab", "applyDataExchangeReceive", "applyDataExchangeSubmissionReview", "applyDataExchangeApprovalReceive", "downloadProjectionAuditExport", "openInternSalaryStandardModal", "openWageReformStandardModal", "openOtherAllowanceStandardModal", "updateOtherAllowanceStandardModalFields"}})

# also check inline only - filter known inline patterns
for name in sorted(set(handlers)):
    if name not in defined:
        print("MISSING HANDLER:", name)
