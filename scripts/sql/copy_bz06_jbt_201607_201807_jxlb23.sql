-- 将 201607、201807 的 bz06_jbt 标准从 jxlb=5 复制为
-- 义务教育(jxlb=2) 与 公共卫生(jxlb=3)，金额不变。幂等可重复执行。

INSERT INTO bz06_jbt (tbnd, item, zwbm, mc, worklower, workupper, bz, jxlb)
SELECT s.tbnd, s.item, s.zwbm, s.mc, s.worklower, s.workupper, s.bz, t.jxlb
FROM bz06_jbt s
CROSS JOIN (SELECT 2 AS jxlb UNION ALL SELECT 3) t
WHERE s.tbnd IN ('201607', '201807')
  AND s.jxlb = 5
  AND NOT EXISTS (
    SELECT 1 FROM bz06_jbt x
    WHERE x.tbnd = s.tbnd AND x.item = s.item AND x.zwbm = s.zwbm AND x.jxlb = t.jxlb
  );
