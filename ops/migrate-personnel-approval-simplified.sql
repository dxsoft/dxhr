-- Simplified personnel approval statuses: 草稿 -> 申报 -> 审批通过
-- Run against existing databases after deploying the simplified approval workflow.

UPDATE dryjbxx SET bbz = '草稿'
  WHERE bbz IS NULL OR TRIM(bbz) = '' OR bbz = '初始建库';

UPDATE dxl SET bbz = '草稿'
  WHERE bbz IS NULL OR TRIM(bbz) = '' OR bbz = '初始建库';
UPDATE dryzwbh SET bbz = '草稿'
  WHERE bbz IS NULL OR TRIM(bbz) = '' OR bbz = '初始建库';
UPDATE dndkh SET bbz = '草稿'
  WHERE bbz IS NULL OR TRIM(bbz) = '' OR bbz = '初始建库';
UPDATE dxlb SET bbz = '草稿'
  WHERE bbz IS NULL OR TRIM(bbz) = '' OR bbz = '初始建库';
UPDATE dryzwbhb SET bbz = '草稿'
  WHERE bbz IS NULL OR TRIM(bbz) = '' OR bbz = '初始建库';
UPDATE dndkhb SET bbz = '草稿'
  WHERE bbz IS NULL OR TRIM(bbz) = '' OR bbz = '初始建库';
