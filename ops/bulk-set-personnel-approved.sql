-- 批量将人员主表/子表审核状态设为「审批通过」（幂等）
-- 用法：mysql --default-character-set=utf8mb4 ... gzjsgl < ops/bulk-set-personnel-approved.sql
-- 本地 Windows：ops/run-bulk-set-personnel-approved-local.ps1

UPDATE dryjbxx SET bbz = '审批通过';
UPDATE dryjbxxb SET bbz = '审批通过';
UPDATE dxl SET bbz = '审批通过';
UPDATE dxlb SET bbz = '审批通过';
UPDATE dryzwbh SET bbz = '审批通过';
UPDATE dryzwbhb SET bbz = '审批通过';
UPDATE dndkh SET bbz = '审批通过';
UPDATE dndkhb SET bbz = '审批通过';
UPDATE hjxx SET bbz = '审批通过';
UPDATE hjxxb SET bbz = '审批通过';
UPDATE jx SET bbz = '审批通过';
UPDATE jxb SET bbz = '审批通过';
