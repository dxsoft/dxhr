-- Add per-row approval status (bbz) to personnel sub-record tables.
-- Default status is 草稿 (draft) in the simplified approval workflow.

ALTER TABLE dxl ADD COLUMN bbz CHAR(8) NULL DEFAULT '草稿' COMMENT '审批状态' AFTER bz;
ALTER TABLE dryzwbh ADD COLUMN bbz CHAR(8) NULL DEFAULT '草稿' COMMENT '审批状态' AFTER jsbz;
ALTER TABLE dndkh ADD COLUMN bbz CHAR(8) NULL DEFAULT '草稿' COMMENT '审批状态' AFTER khjg;
ALTER TABLE dxlb ADD COLUMN bbz CHAR(8) NULL DEFAULT '草稿' COMMENT '审批状态' AFTER bz;
ALTER TABLE dryzwbhb ADD COLUMN bbz CHAR(8) NULL DEFAULT '草稿' COMMENT '审批状态' AFTER jsbz;
ALTER TABLE dndkhb ADD COLUMN bbz CHAR(8) NULL DEFAULT '草稿' COMMENT '审批状态' AFTER khjg;

UPDATE dxl SET bbz = '草稿' WHERE bbz IS NULL OR TRIM(bbz) = '' OR bbz = '初始建库';
UPDATE dryzwbh SET bbz = '草稿' WHERE bbz IS NULL OR TRIM(bbz) = '' OR bbz = '初始建库';
UPDATE dndkh SET bbz = '草稿' WHERE bbz IS NULL OR TRIM(bbz) = '' OR bbz = '初始建库';
UPDATE dxlb SET bbz = '草稿' WHERE bbz IS NULL OR TRIM(bbz) = '' OR bbz = '初始建库';
UPDATE dryzwbhb SET bbz = '草稿' WHERE bbz IS NULL OR TRIM(bbz) = '' OR bbz = '初始建库';
UPDATE dndkhb SET bbz = '草稿' WHERE bbz IS NULL OR TRIM(bbz) = '' OR bbz = '初始建库';
