-- 人员审批操作人字段历史回填（审计日志优先；可覆盖「历史导入」占位）
-- 用法：mysql --default-character-set=utf8mb4 ... gzjsgl < ops/migrate-personnel-approval-actors.sql

-- dryjbxx 提交
UPDATE dryjbxx p
INNER JOIN (
    SELECT al.target_id, al.actor_username, al.created_at
    FROM app_security_audit_log al
    INNER JOIN (
        SELECT target_id, MAX(id) AS max_id
        FROM app_security_audit_log
        WHERE target_type = 'personnel'
          AND action = 'PERSONNEL_APPROVAL_SUBMIT'
        GROUP BY target_id
    ) pick ON al.id = pick.max_id
) sub ON sub.target_id COLLATE utf8mb4_unicode_ci = CAST(p.uid AS CHAR) COLLATE utf8mb4_unicode_ci
SET p.tjr = sub.actor_username, p.tjsj = sub.created_at
WHERE TRIM(COALESCE(p.bbz, '')) = '申报'
  AND (p.tjr IS NULL OR p.tjr = '历史导入');

-- dryjbxx 审核
UPDATE dryjbxx p
INNER JOIN (
    SELECT al.target_id, al.actor_username, al.created_at
    FROM app_security_audit_log al
    INNER JOIN (
        SELECT target_id, MAX(id) AS max_id
        FROM app_security_audit_log
        WHERE target_type = 'personnel'
          AND action = 'PERSONNEL_APPROVE'
        GROUP BY target_id
    ) pick ON al.id = pick.max_id
) apr ON apr.target_id COLLATE utf8mb4_unicode_ci = CAST(p.uid AS CHAR) COLLATE utf8mb4_unicode_ci
SET p.shr = apr.actor_username, p.shsj = apr.created_at
WHERE TRIM(COALESCE(p.bbz, '')) = '审批通过'
  AND (p.shr IS NULL OR p.shr = '历史导入');

UPDATE dryjbxx p
INNER JOIN (
    SELECT al.target_id, al.actor_username, al.created_at
    FROM app_security_audit_log al
    INNER JOIN (
        SELECT target_id, MAX(id) AS max_id
        FROM app_security_audit_log
        WHERE target_type = 'personnel'
          AND action = 'PERSONNEL_APPROVAL_SUBMIT'
        GROUP BY target_id
    ) pick ON al.id = pick.max_id
) sub ON sub.target_id COLLATE utf8mb4_unicode_ci = CAST(p.uid AS CHAR) COLLATE utf8mb4_unicode_ci
SET p.tjr = sub.actor_username, p.tjsj = sub.created_at
WHERE TRIM(COALESCE(p.bbz, '')) = '审批通过'
  AND (p.tjr IS NULL OR p.tjr = '历史导入');

-- dxl
UPDATE dxl t INNER JOIN (
    SELECT al.target_id, al.actor_username, al.created_at FROM app_security_audit_log al
    INNER JOIN (SELECT target_id, MAX(id) max_id FROM app_security_audit_log
        WHERE target_type='dxl' AND action='PERSONNEL_SUBRECORD_SUBMIT' GROUP BY target_id) pick ON al.id=pick.max_id
) sub ON sub.target_id COLLATE utf8mb4_unicode_ci = CAST(t.id AS CHAR) COLLATE utf8mb4_unicode_ci
SET t.tjr=sub.actor_username, t.tjsj=sub.created_at
WHERE TRIM(COALESCE(t.bbz, '')) = '申报' AND (t.tjr IS NULL OR t.tjr = '历史导入');

UPDATE dxl t INNER JOIN (
    SELECT al.target_id, al.actor_username, al.created_at FROM app_security_audit_log al
    INNER JOIN (SELECT target_id, MAX(id) max_id FROM app_security_audit_log
        WHERE target_type='dxl' AND action='PERSONNEL_SUBRECORD_APPROVE' GROUP BY target_id) pick ON al.id=pick.max_id
) apr ON apr.target_id COLLATE utf8mb4_unicode_ci = CAST(t.id AS CHAR) COLLATE utf8mb4_unicode_ci
SET t.shr=apr.actor_username, t.shsj=apr.created_at
WHERE TRIM(COALESCE(t.bbz, '')) = '审批通过' AND (t.shr IS NULL OR t.shr = '历史导入');

-- dryzwbh
UPDATE dryzwbh t INNER JOIN (
    SELECT al.target_id, al.actor_username, al.created_at FROM app_security_audit_log al
    INNER JOIN (SELECT target_id, MAX(id) max_id FROM app_security_audit_log
        WHERE target_type='dryzwbh' AND action='PERSONNEL_SUBRECORD_SUBMIT' GROUP BY target_id) pick ON al.id=pick.max_id
) sub ON sub.target_id COLLATE utf8mb4_unicode_ci = CAST(t.id AS CHAR) COLLATE utf8mb4_unicode_ci
SET t.tjr=sub.actor_username, t.tjsj=sub.created_at
WHERE TRIM(COALESCE(t.bbz, '')) = '申报' AND (t.tjr IS NULL OR t.tjr = '历史导入');

UPDATE dryzwbh t INNER JOIN (
    SELECT al.target_id, al.actor_username, al.created_at FROM app_security_audit_log al
    INNER JOIN (SELECT target_id, MAX(id) max_id FROM app_security_audit_log
        WHERE target_type='dryzwbh' AND action='PERSONNEL_SUBRECORD_SUBMIT' GROUP BY target_id) pick ON al.id=pick.max_id
) sub ON sub.target_id COLLATE utf8mb4_unicode_ci = CAST(t.id AS CHAR) COLLATE utf8mb4_unicode_ci
SET t.tjr=sub.actor_username, t.tjsj=sub.created_at
WHERE TRIM(COALESCE(t.bbz, '')) = '审批通过' AND (t.tjr IS NULL OR t.tjr = '历史导入');

UPDATE dryzwbh t INNER JOIN (
    SELECT al.target_id, al.actor_username, al.created_at FROM app_security_audit_log al
    INNER JOIN (SELECT target_id, MAX(id) max_id FROM app_security_audit_log
        WHERE target_type='dryzwbh' AND action='PERSONNEL_SUBRECORD_APPROVE' GROUP BY target_id) pick ON al.id=pick.max_id
) apr ON apr.target_id COLLATE utf8mb4_unicode_ci = CAST(t.id AS CHAR) COLLATE utf8mb4_unicode_ci
SET t.shr=apr.actor_username, t.shsj=apr.created_at
WHERE TRIM(COALESCE(t.bbz, '')) = '审批通过' AND (t.shr IS NULL OR t.shr = '历史导入');

-- dndkh
UPDATE dndkh t INNER JOIN (
    SELECT al.target_id, al.actor_username, al.created_at FROM app_security_audit_log al
    INNER JOIN (SELECT target_id, MAX(id) max_id FROM app_security_audit_log
        WHERE target_type='dndkh' AND action='PERSONNEL_SUBRECORD_SUBMIT' GROUP BY target_id) pick ON al.id=pick.max_id
) sub ON sub.target_id COLLATE utf8mb4_unicode_ci = CAST(t.id AS CHAR) COLLATE utf8mb4_unicode_ci
SET t.tjr=sub.actor_username, t.tjsj=sub.created_at
WHERE TRIM(COALESCE(t.bbz, '')) = '申报' AND (t.tjr IS NULL OR t.tjr = '历史导入');

UPDATE dndkh t INNER JOIN (
    SELECT al.target_id, al.actor_username, al.created_at FROM app_security_audit_log al
    INNER JOIN (SELECT target_id, MAX(id) max_id FROM app_security_audit_log
        WHERE target_type='dndkh' AND action='PERSONNEL_SUBRECORD_APPROVE' GROUP BY target_id) pick ON al.id=pick.max_id
) apr ON apr.target_id COLLATE utf8mb4_unicode_ci = CAST(t.id AS CHAR) COLLATE utf8mb4_unicode_ci
SET t.shr=apr.actor_username, t.shsj=apr.created_at
WHERE TRIM(COALESCE(t.bbz, '')) = '审批通过' AND (t.shr IS NULL OR t.shr = '历史导入');

-- hjxx
UPDATE hjxx t INNER JOIN (
    SELECT al.target_id, al.actor_username, al.created_at FROM app_security_audit_log al
    INNER JOIN (SELECT target_id, MAX(id) max_id FROM app_security_audit_log
        WHERE target_type='hjxx' AND action='PERSONNEL_SUBRECORD_SUBMIT' GROUP BY target_id) pick ON al.id=pick.max_id
) sub ON sub.target_id COLLATE utf8mb4_unicode_ci = CAST(t.id AS CHAR) COLLATE utf8mb4_unicode_ci
SET t.tjr=sub.actor_username, t.tjsj=sub.created_at
WHERE TRIM(COALESCE(t.bbz, '')) = '申报' AND (t.tjr IS NULL OR t.tjr = '历史导入');

UPDATE hjxx t INNER JOIN (
    SELECT al.target_id, al.actor_username, al.created_at FROM app_security_audit_log al
    INNER JOIN (SELECT target_id, MAX(id) max_id FROM app_security_audit_log
        WHERE target_type='hjxx' AND action='PERSONNEL_SUBRECORD_APPROVE' GROUP BY target_id) pick ON al.id=pick.max_id
) apr ON apr.target_id COLLATE utf8mb4_unicode_ci = CAST(t.id AS CHAR) COLLATE utf8mb4_unicode_ci
SET t.shr=apr.actor_username, t.shsj=apr.created_at
WHERE TRIM(COALESCE(t.bbz, '')) = '审批通过' AND (t.shr IS NULL OR t.shr = '历史导入');

-- jx
UPDATE jx t INNER JOIN (
    SELECT al.target_id, al.actor_username, al.created_at FROM app_security_audit_log al
    INNER JOIN (SELECT target_id, MAX(id) max_id FROM app_security_audit_log
        WHERE target_type='jx' AND action='PERSONNEL_SUBRECORD_SUBMIT' GROUP BY target_id) pick ON al.id=pick.max_id
) sub ON sub.target_id COLLATE utf8mb4_unicode_ci = CAST(t.id AS CHAR) COLLATE utf8mb4_unicode_ci
SET t.tjr=sub.actor_username, t.tjsj=sub.created_at
WHERE TRIM(COALESCE(t.bbz, '')) = '申报' AND (t.tjr IS NULL OR t.tjr = '历史导入');

UPDATE jx t INNER JOIN (
    SELECT al.target_id, al.actor_username, al.created_at FROM app_security_audit_log al
    INNER JOIN (SELECT target_id, MAX(id) max_id FROM app_security_audit_log
        WHERE target_type='jx' AND action='PERSONNEL_SUBRECORD_APPROVE' GROUP BY target_id) pick ON al.id=pick.max_id
) apr ON apr.target_id COLLATE utf8mb4_unicode_ci = CAST(t.id AS CHAR) COLLATE utf8mb4_unicode_ci
SET t.shr=apr.actor_username, t.shsj=apr.created_at
WHERE TRIM(COALESCE(t.bbz, '')) = '审批通过' AND (t.shr IS NULL OR t.shr = '历史导入');
