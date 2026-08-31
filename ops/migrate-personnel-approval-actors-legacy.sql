-- 历史导入数据兜底回填（简化条件，兼容 utf8mb4_0900_ai_ci / utf8mb4_unicode_ci）
-- 幂等：不覆盖已有 shr/tjr；在 migrate-personnel-approval-actors.sql 之后执行

UPDATE dryjbxx SET shr = '历史导入', shsj = NULL
WHERE TRIM(COALESCE(bbz, '')) = '审批通过' AND shr IS NULL;

UPDATE dryjbxx SET tjr = '历史导入', tjsj = NULL
WHERE TRIM(COALESCE(bbz, '')) = '申报' AND tjr IS NULL;

UPDATE dryzwbh SET shr = '历史导入', shsj = NULL
WHERE TRIM(COALESCE(bbz, '')) = '审批通过' AND shr IS NULL;
UPDATE dryzwbh SET tjr = '历史导入', tjsj = NULL
WHERE TRIM(COALESCE(bbz, '')) = '申报' AND tjr IS NULL;

UPDATE dxl SET shr = '历史导入', shsj = NULL
WHERE TRIM(COALESCE(bbz, '')) = '审批通过' AND shr IS NULL;
UPDATE dxl SET tjr = '历史导入', tjsj = NULL
WHERE TRIM(COALESCE(bbz, '')) = '申报' AND tjr IS NULL;

UPDATE dndkh SET shr = '历史导入', shsj = NULL
WHERE TRIM(COALESCE(bbz, '')) = '审批通过' AND shr IS NULL;
UPDATE dndkh SET tjr = '历史导入', tjsj = NULL
WHERE TRIM(COALESCE(bbz, '')) = '申报' AND tjr IS NULL;

UPDATE hjxx SET shr = '历史导入', shsj = NULL
WHERE TRIM(COALESCE(bbz, '')) = '审批通过' AND shr IS NULL;
UPDATE hjxx SET tjr = '历史导入', tjsj = NULL
WHERE TRIM(COALESCE(bbz, '')) = '申报' AND tjr IS NULL;

UPDATE jx SET shr = '历史导入', shsj = NULL
WHERE TRIM(COALESCE(bbz, '')) = '审批通过' AND shr IS NULL;
UPDATE jx SET tjr = '历史导入', tjsj = NULL
WHERE TRIM(COALESCE(bbz, '')) = '申报' AND tjr IS NULL;
