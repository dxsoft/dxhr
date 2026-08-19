-- 可选（阶段 C）：将义务教育单位(jxlb=2) 当前在岗专技 10xx 改为 11xx。
-- 执行前请备份，并先在试算环境对比工资结果。默认不自动执行。

-- 任职现任职务
-- UPDATE dryzwbh z
-- INNER JOIN dryjbxx p ON p.dwbm = z.dwbm AND p.grbm = z.grbm
-- INNER JOIN dwbm d ON d.dwbm = z.dwbm
-- SET z.zwbm = CONCAT('11', SUBSTRING(TRIM(z.zwbm), 3))
-- WHERE d.jxlb = 2
--   AND z.xrzwbz = '1'
--   AND TRIM(z.zwbm) LIKE '10__'
--   AND CHAR_LENGTH(TRIM(z.zwbm)) = 4;

-- 当前工资链头职务（sid 为空）
-- UPDATE hisbase h
-- INNER JOIN dwbm d ON d.dwbm = h.dwbm
-- SET h.zwbm2 = CONCAT('11', SUBSTRING(TRIM(h.zwbm2), 3))
-- WHERE d.jxlb = 2
--   AND (h.sid IS NULL OR TRIM(h.sid) = '')
--   AND TRIM(h.zwbm2) LIKE '10__'
--   AND CHAR_LENGTH(TRIM(h.zwbm2)) = 4;

SELECT 'Review and uncomment UPDATE statements after backup and payroll trial comparison.' AS notice;
