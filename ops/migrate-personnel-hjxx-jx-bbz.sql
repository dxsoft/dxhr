-- Add per-row approval status (bbz) to award and rank sub-record tables.

ALTER TABLE hjxx ADD COLUMN bbz CHAR(8) NULL DEFAULT '草稿' COMMENT '审批状态' AFTER jljb;
ALTER TABLE hjxxb ADD COLUMN bbz CHAR(8) NULL DEFAULT '草稿' COMMENT '审批状态' AFTER jljb;
ALTER TABLE jx ADD COLUMN bbz CHAR(8) NULL DEFAULT '草稿' COMMENT '审批状态' AFTER lb;
ALTER TABLE jxb ADD COLUMN bbz CHAR(8) NULL DEFAULT '草稿' COMMENT '审批状态' AFTER lb;

UPDATE hjxx SET bbz = '草稿' WHERE bbz IS NULL OR TRIM(bbz) = '' OR bbz = '初始建库';
UPDATE hjxxb SET bbz = '草稿' WHERE bbz IS NULL OR TRIM(bbz) = '' OR bbz = '初始建库';
UPDATE jx SET bbz = '草稿' WHERE bbz IS NULL OR TRIM(bbz) = '' OR bbz = '初始建库';
UPDATE jxb SET bbz = '草稿' WHERE bbz IS NULL OR TRIM(bbz) = '' OR bbz = '初始建库';
