-- 启用义务教育专技岗位序列（11）字典：类别 05111 + 叶子职务 05111xx（对标 05110xx）。
-- 幂等可重复执行。人员/标准表仍用四位职务码 11xx（= SUBSTRING(bm,4,4)）。

-- 1) 岗位类别节点
INSERT INTO dmb (bm, mc, czbm, xt, sfsy)
SELECT '05111', '义务教育专技岗位', '051', 1, 1
FROM (SELECT 1 AS _) t
WHERE NOT EXISTS (
    SELECT 1 FROM dmb WHERE TRIM(bm) = '05111'
);

-- 若已存在但名称为空或仅为编码，纠正为业务名称
UPDATE dmb
SET mc = '义务教育专技岗位'
WHERE TRIM(bm) = '05111'
  AND (TRIM(mc) = '' OR TRIM(mc) = '11' OR TRIM(mc) = '05111');

-- 2) 从普通专技叶子（05110xx，7 位）复制为义务教育专技（05111xx）
INSERT INTO dmb (bm, mc, czbm, xt, sfsy)
SELECT CONCAT('05111', SUBSTRING(TRIM(s.bm), 6, 2)) AS bm,
       CONCAT('义务教育', TRIM(s.mc)) AS mc,
       '051',
       COALESCE(s.xt, 1),
       COALESCE(s.sfsy, 1)
FROM dmb s
WHERE TRIM(s.bm) LIKE '05110__'
  AND CHAR_LENGTH(TRIM(s.bm)) = 7
  AND NOT EXISTS (
      SELECT 1 FROM dmb x
      WHERE TRIM(x.bm) = CONCAT('05111', SUBSTRING(TRIM(s.bm), 6, 2))
  );

-- 3) 核对：标准表若缺 11 序列职务工资，可从 10 复制（仅补缺，不覆盖已有）
INSERT INTO bz06_zwgz (tbnd, zwbm, bz)
SELECT s.tbnd, CONCAT('11', SUBSTRING(TRIM(s.zwbm), 3)) AS zwbm, s.bz
FROM bz06_zwgz s
WHERE TRIM(s.zwbm) LIKE '10__'
  AND CHAR_LENGTH(TRIM(s.zwbm)) = 4
  AND NOT EXISTS (
      SELECT 1 FROM bz06_zwgz x
      WHERE x.tbnd = s.tbnd
        AND TRIM(x.zwbm) = CONCAT('11', SUBSTRING(TRIM(s.zwbm), 3))
  );

-- 4) 薪级工资岗位分类：缺 gwflbm=11 时从 10 复制
INSERT INTO bz06_xjgz (tbnd, gwflbm, xj, bz, jc, jce)
SELECT s.tbnd, '11' AS gwflbm, s.xj, s.bz, s.jc, s.jce
FROM bz06_xjgz s
WHERE TRIM(s.gwflbm) = '10'
  AND NOT EXISTS (
      SELECT 1 FROM bz06_xjgz x
      WHERE x.tbnd = s.tbnd
        AND TRIM(x.gwflbm) = '11'
        AND x.xj = s.xj
  );
