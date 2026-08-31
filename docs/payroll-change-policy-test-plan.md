# 工资变动政策符合性测试方案

本文档用于验证 Web 版人事工资管理系统各类**工资变动试算/办理**是否符合**政策文件 + 本地 cyxx/单位参数**。验收以书面政策与本地参数为准，不以 VFP 旧系统结果为金标准。

**版本**：1.0  
**适用系统**：Spring Boot 迁移版（rsgzgl）  
**相关文档**：[hisbase 对账清单](hisbase-reconciliation-checklist.md)、[VFP 菜单清单](vfp-menu-inventory.md)

---

## 1. 引言

### 1.1 目的

- 确认各工资变动模块的**入列条件、计算规则、执行年月、写入字段**与现行政策一致。
- 在标准表、本地参数变更后，提供可重复的回归测试路径。
- 为业务验收与上线 sign-off 提供可追溯记录。

### 1.2 范围

| 类别 | 功能模块（菜单编码） |
| --- | --- |
| 晋升类 | `NORMAL_PROMOTION`、`LEVEL_PROMOTION`、`POSITION_CHANGE_PROMOTION`、`EDUCATION_PROMOTION` |
| 定资转正 | `NEW_PERSONNEL_SALARY`、`REGULARIZATION`、`REGULARIZATION_HIGH_GRADE`、`WAGE_REFORM_2006` |
| 津贴标准 | `TEACHING_ALLOWANCE_ADJUSTMENT`、`POLICE_ALLOWANCE_ADJUSTMENT`、`PROSECUTION_ALLOWANCE_ADJUSTMENT`、`JUDICIAL_ALLOWANCE_ADJUSTMENT`、`SUPERVISION_ALLOWANCE_ADJUSTMENT` |
| 等级变化 | `POLICE_RANK_CHANGE_PROMOTION`、`PROSECUTION_RANK_CHANGE_PROMOTION`、`JUDICIAL_RANK_CHANGE_PROMOTION`、`SUPERVISION_RANK_CHANGE_PROMOTION` |
| 标准调标 | `BASIC_SALARY_STANDARD_ADJUSTMENT`、`ALLOWANCE_RECALCULATION`（如启用）、`PERFORMANCE_RATIO_ADJUSTMENT`、`SALARY_STANDARD_ADJUSTMENT`（2024.07，如启用） |
| 特殊变动 | `FLOATING_TO_FIXED`、`OTHER_PAYROLL_CHANGE`、`INTERN_SALARY_CHANGE`、`MONTHLY_AVERAGE_SALARY` |
| 辅助验证 | `AUDIT`（工资推算对账）、`PAYROLL_CHANGE_APPROVAL_REPORT`、`PAYROLL_CHANGE_REGISTER_REPORT`、`PAYROLL_HISTORY` |

**不纳入本方案主流程**：权限/UKey、备份恢复、数据交换、离退休人员（可单列附录）。

### 1.3 角色分工

| 角色 | 职责 |
| --- | --- |
| 政策负责人 | 提供政策文件索引、解释争议条款、判定是否符合政策 |
| 测试执行人 | 准备数据、执行试算/办理/还原、填写用例与截图 |
| 业务骨干 | 抽审典型人员、确认金额与变动类别合理 |
| 开发支持 | 解释系统实现、修复缺陷、协助查日志 |

### 1.4 建议测试环境

- 与生产隔离的租户库（如 demo / 独立验收库）。
- 测试账号具备对应模块的 `PAYROLL_READ` / `PAYROLL_WRITE` 权限。
- **cyxx 本地政策**、**标准表**、**单位 dwbm 属性**与待验收单位一致。
- 试算前导出或截图标准表生效年月，附在用例证据中。

---

## 2. 政策基准层（验收依据）

测试前须整理「政策包」，作为所有用例的预期来源。

### 2.1 政策文件索引表（填写）

| 序号 | 文号/名称 | 生效日期 | 适用变动类型 | 存放位置 |
| --- | --- | --- | --- | --- |
| P-01 | （例：国办发〔2014〕60号及配套解释） | | 职务工资、级别工资、晋升 | |
| P-02 | （例：2006 工改政策文件） | | 套改、滚动 | |
| P-03 | （例：本省调标文件 2024.07） | | 基本工资标准调整 | |
| P-04 | （例：警衔/检察/审判/监察津贴政策） | | 津贴调整、等级变化 | |
| P-05 | （例：事业绩效工资/薪级政策） | | 事业薪级、岗位工资 | |
| P-06 | （本地补充规定） | | 浮动、高定、教护龄等 | |

用例中的「政策依据」栏引用上表序号 + 条款摘要，**不写死具体金额**（金额以系统标准表当时版本为准）。

### 2.2 本地 cyxx 参数对照表

系统路径：**系统配置 → 本地工资政策**（`#local-policy-config`）。以下字段来自 `cyxx`，影响试算/办理逻辑。

| cyxx 字段 | 含义 | 测试关注点 | 本次验收值（填写） |
| --- | --- | --- | --- |
| `roundingMode` | 舍入方式 | 各工资项合计、差额 | |
| `roundToInteger` | 是否取整 | 与政策及审批表一致 | |
| `policeRankStartLevel` | 警衔起算级别 | 警衔变化、执法勤务定级 | |
| `internSalaryMode` | 见习工资模式 | 见习定资、转正衔接 | |
| `floatingSalaryMode` | 浮动工资模式 | 浮动转固定 | |
| `payGradeRetentionMode` | 保留档次模式 | 职务变化后档次保留 | |
| `bonusBalanceMode` | 奖金结余模式 | 试算/对账中 JJJY2 等 | |
| `positionChangeIncludeTechnicalGrade` | 职务变化含技术等级 | 机关职务变化合计 | |
| `rankChangeIncludeTechnicalGrade` | 警衔变化含技术等级 | 警衔等级变化 | |
| `payrollTitle` | 审批表标题 | 报表打印 | |
| `approvalMode` / `approvalFlag` | 审批模式 | 是否需审批流（如启用） | |
| `szds`（所在城市） | 签约/政策城市 | 授权与地方政策适用 | |

### 2.3 单位属性（dwbm）

| 字段 | 含义 | 政策影响 |
| --- | --- | --- |
| `category` / 单位性质 | 行政 / 事业 | 岗位序列、薪级 vs 档次 |
| `payrollCategory`（工资财政供给） | 公务员管理(0)、参照(1)、依照(2)、事业管理等 | 是否按机关规则试算（见 `UnitPayrollClassification`） |
| `performanceAllowanceEnabled` 等 | 绩效、年终一次性 | 津补贴、绩效比例类变动 |

### 2.4 标准表快照清单（试算前填写）

| 标准表 | 用途 | 生效年月（填写） | 截图/导出编号 |
| --- | --- | --- | --- |
| `bz06_zwgz` / 职务工资标准 | 机关职务工资 | | |
| `bz06_jbgz` / 级别工资标准 | 机关级别档次工资 | | |
| `bz06_djgz` | 执法勤务等级工资 | | |
| `jbtbz*` / 津补贴标准 | 公务员/事业津补贴 | | |
| `jxjtbz` / 警衔津贴标准 | 警衔、监察(lb=mt) | | |
| 检察/审判标准表 | 检察、审判津贴 | | |
| `jxjtbz`（教护） | 教护龄津贴 | | |
| 2006 套改对照表 | 套改、滚动 | | |

### 2.5 验收判定原则

1. **资格判定**：是否入列、提示语是否符合政策（含负例不入列）。
2. **金额判定**：职务/级别/档次/薪级/津贴/合计的计算路径与政策条款一致（允许因标准表版本产生的已知差异，须注明文号）。
3. **写入判定**：`hisbase` 变动类别、执行年月、链表、关键字段符合第 7 节核对表。
4. **可逆性**：支持「还原」的模块，还原后链与金额恢复试算前状态。

---

## 3. 抽样人员矩阵

每类至少 **2 人**（1 人正常路径 + 1 人边界/例外）。下表为**登记表模板**，执行前复制填写。

### 3.1 分类矩阵

| 维度 | 档位示例 | 验证重点 | 人员 A（填写） | 人员 B（边界，填写） |
| --- | --- | --- | --- | --- |
| 机关公务员 | 综合管理 01xx | 级别/档次、五年考核、职务变化 | dwbm/grbm: | dwbm/grbm: |
| 执法勤务 | 004 序列 | 等级工资、警衔津贴 | | |
| 警务技术 | 005 序列 | 与执法勤务区分 | | |
| 参照/依照公务员 | gzczbz=1/2 | 按机关规则 | | |
| 事业管理 | 岗位 07–11 | 薪级晋升、岗位工资 | | |
| 教护 | 含教护龄 | 教护龄津贴 | | |
| 检察/审判/监察 | 对应子表 | 等级变化 + 津贴标准年月 | | |
| 见习 | zwbm2 含 F | 转正定级、见习工资 | | |
| 边界 | 调入定资、考核中断、浮动薪级、套改后滚动 | 政策例外 | | |

### 3.2 单人登记字段

| 字段 | 说明 |
| --- | --- |
| `dwbm` / `grbm` | 单位编码、个人编码 |
| 姓名 | |
| 单位性质 / 工资财政供给 | 行政、事业、参照等 |
| 当前链头 | `hisbase` 中 `sid=''` 记录的 `id`、`jsnf`、`jsyf` |
| `zwbm` / `zwmc` | 当前职务或岗位 |
| `jx` / `dc` / `xj` | 级别、档次或薪级 |
| 起算年月 | `dcnd`、`xjnd`、`tbnd` 等 |
| 近 5–6 年考核 | 年度与结果（合格/不合格） |
| 任职关键节点 | 最近 2 次职务/岗位变化年月 |
| 学历关键节点 | 最高学历及取得年月 |
| 警衔/检察/审判/监察 | 如有，当前等级与起算年月 |
| 备注 | 浮动、高定、调入等特殊标记 |

---

## 4. 用例执行说明

### 4.1 通用操作步骤

1. 确认政策包、cyxx、标准表快照已填写。
2. 登录测试环境，进入对应菜单（见附录 A）。
3. 选择**单位、年度/月份**（与用例前置一致）。
4. **试算**：核对列表是否入列、试算说明行、前后工资项与差额。
5. **办理**（若模块支持）：确认提示与二次确认（`confirmBeforeAction` 如有）。
6. **核对 hisbase**：新记录 `jslb`、执行年月、`sid` 链、第 7 节字段。
7. **还原**（若支持）：确认链恢复、金额恢复。
8. 填写用例「结果」栏，保存截图与操作日志编号。

### 4.2 用例编号规则

`TC-{模块缩写}-{序号}`，模块缩写：NP=正常晋升，LP=级别晋升，PC=职务变化，ED=学历，RG=定资转正，AL=津贴，RC=等级变化，ST=标准调标，SP=特殊，XC=横切。

---

## 5. 测试用例（共 32 条）

### 5.1 正常档次/薪级晋升（NORMAL_PROMOTION）

#### TC-NP-01 机关公务员五年考核合格升一档

- **政策依据**：P-01，连续五年年度考核合格，正常晋升工资档次。
- **本地参数**：cyxx 舍入与取整按 2.2 表。
- **前置数据**：抽样「机关公务员 A」；`dcnd` 起算满 5 年；近 5 年考核均为合格；当前为机关综合管理序列。
- **操作步骤**：进入 `#normal-promotion` → 选单位与晋升年度 → 试算 → 办理 → 查 `#payroll-history`。
- **预期结果**：
  - 资格：入列；说明含考核年数、起算依据。
  - 金额：档次 +1（不越政策上限）；级别工资按新标准档差；职务工资不变（如无职务变化）。
  - 写入：`jslb` 为正常档次晋升类；新记录 `sid=''`；旧链头 `sid` 指向新 id。
- **还原**：办理后执行还原；链与档次恢复。
- **结果**：□通过 □失败（备注：________）

#### TC-NP-02 事业人员五年考核合格升一薪级

- **政策依据**：P-05，事业单位工作人员正常增加薪级。
- **本地参数**：单位 `payrollCategory` 为事业管理。
- **前置数据**：抽样「事业管理 A」；`xjnd` 起算满 5 年；考核合格。
- **操作步骤**：同 TC-NP-01，模块 `#normal-promotion`。
- **预期结果**：
  - 资格：入列（事业口径，非机关档次）。
  - 金额：薪级 +1；岗位工资按岗位等级标准；不与机关档次混淆。
  - 写入：`jslb` 与事业薪级晋升政策一致。
- **还原**：支持则还原。
- **结果**：□通过 □失败（备注：________）

#### TC-NP-03 考核不合格年度不入列（负例）

- **政策依据**：P-01，考核不合格年度不晋升。
- **前置数据**：抽样「机关公务员 B」；起算年满 5 年但其中 1 年考核为不合格。
- **操作步骤**：试算列表查询该人员。
- **预期结果**：
  - 资格：**不入列**或提示不满足考核条件；不得办理。
  - 金额：无新 hisbase 记录。
- **结果**：□通过 □失败（备注：________）

---

### 5.2 级别晋升 / 套改滚动（LEVEL_PROMOTION）

#### TC-LP-01 级别起算满五年升一级

- **政策依据**：P-01，级别工资晋升条件（起算满五年、考核合格等）。
- **前置数据**：机关人员；`jx` 起算年月满 5 年；考核合格；未达职务对应最高级别。
- **操作步骤**：`#level-promotion` → 试算 → 办理。
- **预期结果**：
  - 资格：入列。
  - 金额：级别数减少 1（数字越小级别越高，以政策口径为准）；档次按政策重新套入或保留规则。
  - 写入：`jslb` 为级别晋升类。
- **还原**：支持则还原。
- **结果**：□通过 □失败（备注：________）

#### TC-LP-02 2007–2010 套改级别滚动

- **政策依据**：P-02，工改后滚动晋升政策（2007–2010 窗口）。
- **前置数据**：2006 套改人员；滚动条件满足（按政策文件）。
- **操作步骤**：`#level-promotion` 或工资推算 `#audit` 辅助看重放说明。
- **预期结果**：
  - 资格：仅在政策窗口内入列。
  - 金额：滚动升一级规则与文号一致。
- **结果**：□通过 □失败（备注：________）

#### TC-LP-03 已办理正常档次同年不得重复级别晋升（负例）

- **政策依据**：P-01，同周期晋升互斥规则（按本地解释）。
- **前置数据**：同一人同年已办理正常档次晋升。
- **操作步骤**：`#level-promotion` 试算。
- **预期结果**：不入列或提示已处理/互斥；不得重复办理。
- **结果**：□通过 □失败（备注：________）

---

### 5.3 职务变化晋升（POSITION_CHANGE_PROMOTION）

#### TC-PC-01 同序列职务晋升就近就高

- **政策依据**：P-01，职务变动后工资按新任职务、就近就高套入。
- **本地参数**：`positionChangeIncludeTechnicalGrade` 按 2.2 表。
- **前置数据**：任职子表有未处理职务变化；新职务同序列且高于原任。
- **操作步骤**：`#position-change-promotion` → 试算 → 办理。
- **预期结果**：
  - 资格：入列；关联任职记录年月正确。
  - 金额：职务工资按新职务标准；级别/档次按就近就高，不高定（除非政策允许）。
  - 写入：`jslb` 为职务变化类；执行年月为任职次月或政策规定月。
- **还原**：支持则还原。
- **结果**：□通过 □失败（备注：________）

#### TC-PC-02 执法勤务 004 序列等级工资

- **政策依据**：P-01 / 执法勤务等级工资政策；标准表 `bz06_djgz`。
- **前置数据**：004 序列人员；职务/职级变化。
- **操作步骤**：`#position-change-promotion` 试算。
- **预期结果**：
  - 金额：级别工资取自**等级工资标准**，非普通 `bz06_jbgz`。
  - 说明行明确序列与标准来源。
- **结果**：□通过 □失败（备注：________）

#### TC-PC-03 转序列（机关 ↔ 执法勤务）政策适用

- **政策依据**：P-01 / 本地 P-06，转序列定资规则。
- **前置数据**：抽样「边界 B」转序列人员。
- **操作步骤**：试算并核对说明行政策路径。
- **预期结果**：按转序列政策套定，非简单同序列晋升；不入列情形有明确提示。
- **结果**：□通过 □失败（备注：________）

---

### 5.4 学历晋升（EDUCATION_PROMOTION）

#### TC-ED-01 取得更高学历后定级

- **政策依据**：P-01 / P-06，学历变动工资定级。
- **前置数据**：学历子表有更高学历且未处理；取得年月明确。
- **操作步骤**：`#education-promotion` → 试算 → 办理。
- **预期结果**：
  - 资格：入列。
  - 金额：按新政策套定职务/级别（档次）或事业岗位薪级；不低于原工资（就高原则，如政策要求）。
  - 写入：`jslb` 为学历晋升类。
- **还原**：支持则还原。
- **结果**：□通过 □失败（备注：________）

---

### 5.5 新进 / 转正 / 高定 / 套改

#### TC-RG-01 见习人员转正定级

- **政策依据**：P-01 / P-06，见习期满转正定级。
- **前置数据**：抽样「见习 A」；见习期满；考核合格。
- **操作步骤**：`#regularization` → 试算 → 办理。
- **预期结果**：按转正政策套定职务/级别工资；`internSalaryMode` 生效。
- **还原**：支持则还原。
- **结果**：□通过 □失败（备注：________）

#### TC-RG-02 新进人员确定工资

- **政策依据**：P-01，新进人员定资（学历、职务层次）。
- **前置数据**：新录入人员，无 hisbase 链或仅有一条定资记录。
- **操作步骤**：`#new-personnel-salary` → 试算 → 办理。
- **预期结果**：定资类别、标准年月、合计与政策一致。
- **结果**：□通过 □失败（备注：________）

#### TC-RG-03 转正高定档次薪级

- **政策依据**：P-06，转正高定政策（高定档/薪级增量）。
- **前置数据**：符合高定条件（政策规定的情形）。
- **操作步骤**：`#regularization-high-grade` → 试算 → 办理。
- **预期结果**：高定增量与 `RegularizationHighGradePolicy` 及政策文件一致；执法勤务高定开关正确。
- **结果**：□通过 □失败（备注：________）

#### TC-RG-04 2006 年工资套改

- **政策依据**：P-02，2006 工改套改对照。
- **前置数据**：套改对象人员；套改年限、任职、考核数据完整。
- **操作步骤**：`#wage-reform-2006` 试算（或人员工资试算重放套改节点）。
- **预期结果**：套改级别、档次、年限扣减与政策对照表一致。
- **结果**：□通过 □失败（备注：________）

---

### 5.6 津贴调整

#### TC-AL-01 调整教护龄津贴

- **政策依据**：P-06，教护龄津贴计发规则。
- **前置数据**：教护人员；教护龄满档/变动。
- **操作步骤**：`#teaching-allowance-adjustment` → 试算 → 办理 → 还原。
- **预期结果**：津贴差额 = 新标准 − 旧标准；合计正确；标准年月写入正确字段。
- **结果**：□通过 □失败（备注：________）

#### TC-AL-02 调整警衔津贴

- **政策依据**：P-04，警衔津贴标准。
- **操作步骤**：`#police-allowance-adjustment` → 试算 → 办理 → 还原。
- **预期结果**：`jxjtbz`（lb=jx）更新；`jxjt` 差额计入合计。
- **结果**：□通过 □失败（备注：________）

#### TC-AL-03 调整检察津贴

- **政策依据**：P-04。
- **操作步骤**：`#prosecution-allowance-adjustment` → 试算 → 办理 → 还原。
- **预期结果**：`jcjtbz` 更新；检察津贴差额正确。
- **结果**：□通过 □失败（备注：________）

#### TC-AL-04 调整审判津贴

- **政策依据**：P-04。
- **操作步骤**：`#judicial-allowance-adjustment` → 试算 → 办理 → 还原。
- **预期结果**：`spjtbz` 更新；审判津贴差额正确。
- **结果**：□通过 □失败（备注：________）

#### TC-AL-05 调整监察津贴

- **政策依据**：P-04。
- **操作步骤**：`#supervision-allowance-adjustment` → 试算 → 办理 → 还原。
- **预期结果**：监察标准写在 `jxjtbz`（lb=mt）；`mtjt` 差额正确（见第 7 节）。
- **结果**：□通过 □失败（备注：________）

---

### 5.7 等级变化晋升

#### TC-RC-01 警衔变化晋升

- **政策依据**：P-04，警衔变动工资处理。
- **本地参数**：`rankChangeIncludeTechnicalGrade`、`policeRankStartLevel`。
- **前置数据**：`jx` 子表有警衔变化未处理。
- **操作步骤**：`#police-rank-change-promotion` → 试算 → 办理。
- **预期结果**：`jslb` 为警衔变化类；警衔津贴与级别工资按政策调整。
- **还原**：支持则还原。
- **结果**：□通过 □失败（备注：________）

#### TC-RC-02 检察官等级变化晋升

- **政策依据**：P-04。
- **前置数据**：检察等级子表变化。
- **操作步骤**：`#prosecution-rank-change-promotion` → 试算 → 办理。
- **预期结果**：`jslb` 为检察变化类；检察津贴与标准年月正确。
- **结果**：□通过 □失败（备注：________）

#### TC-RC-03 警衔取消停发津贴

- **政策依据**：P-04，警衔取消后停发警衔津贴。
- **前置数据**：当前 tip 有警衔（如三级警督）且 `jxjt > 0`；`jx` 子表新增取消记录（`lb=jx`、`sysj=取消执行年月`、警衔字段留空或填「无」），历史警衔记录保留。
- **操作步骤**：`#police-rank-change-promotion` → 试算 → 办理。
- **预期结果**：待办可见，目标警衔显示为取消；试算 `jxjt=0`、差额为负；办理后 `jslb` 为警衔变化类、`jx` 为空、`jxjt=0`，执行年月为 `sysj` 次月。
- **还原**：支持则还原。
- **结果**：□通过 □失败（备注：________）

---

### 5.8 标准调标类

#### TC-ST-01 调整基本工资标准（职务/级别工资标准变更）

- **政策依据**：P-03 或国家调标文件。
- **操作步骤**：`#basic-salary-standard-adjustment` → 选调标年月 → 试算 → 办理。
- **预期结果**：仅职务工资、级别（薪级）工资按新标准重算；津贴不变；`tbnd` 更新。
- **还原**：支持则还原。
- **结果**：□通过 □失败（备注：________）

#### TC-ST-02 调整津补贴标准

- **政策依据**：P-05 / 本地津补贴政策。
- **操作步骤**：`#allowance-recalculation` 或事业/公务员津补贴调标入口（如菜单启用）→ 试算。
- **预期结果**：`jbtbz` 相关项按单位性质分别处理；个人比例不变除非另有政策。
- **结果**：□通过 □失败（备注：________）

#### TC-ST-03 调整绩效比例

- **政策依据**：P-05，绩效工资比例调整。
- **操作步骤**：`#performance-ratio-adjustment` → 试算 → 办理。
- **预期结果**：`jtbl` 同步；按个人/单位比例重算绩效相关项。
- **结果**：□通过 □失败（备注：________）

#### TC-ST-04 2024.07 调标（如菜单启用）

- **政策依据**：P-03，2024.07 调标文件。
- **操作步骤**：`#salary-standard-adjustment` → 试算 → 办理。
- **预期结果**：调标年月、职务/级别工资与文件一致；与 TC-ST-01 政策关系在备注中说明。
- **结果**：□通过 □失败（备注：________）

---

### 5.9 特殊变动

#### TC-SP-01 浮动转固定

- **政策依据**：P-06，浮动薪级（档次）转固定规则。
- **本地参数**：`floatingSalaryMode`。
- **前置数据**：存在浮动记录且满足转固定条件。
- **操作步骤**：`#floating-to-fixed` → 试算 → 办理 → 还原。
- **预期结果**：浮动项清零或转固定后档次/薪级正确；合计变化符合政策。
- **结果**：□通过 □失败（备注：________）

#### TC-SP-02 其它情况工资变动

- **政策依据**：P-06 / 本地特殊审批规定。
- **操作步骤**：`#other-payroll-change` → 录入对账表 → 自动计算项 → 办理 → 还原。
- **预期结果**：手动项保留；自动计算项按公式；`jslb` 正确。
- **结果**：□通过 □失败（备注：________）

#### TC-SP-03 见习工资变动

- **政策依据**：P-01，见习期工资标准。
- **操作步骤**：`#intern-salary-change`（菜单如启用）→ 试算 → 办理。
- **预期结果**：按见习标准表；`internSalaryMode` 一致。
- **结果**：□通过 □失败（备注：________）

#### TC-SP-04 月平均工资计算

- **政策依据**：P-06 / 统计口径文件。
- **操作步骤**：`#monthly-average-salary` → 选范围试算。
- **预期结果**：统计口径、包含工资项与政策一致；可导出核对。
- **结果**：□通过 □失败（备注：________）

---

### 5.10 横切验证

#### TC-XC-01 工资推算对账（AUDIT）

- **政策依据**：全政策链重放，无单一文号；用于发现断链。
- **前置数据**：抽样人员 3–5 人（含机关、事业、警衔各 1）。
- **操作步骤**：`#audit` → 选单位 → 批量推算 → 查看差异与说明行。
- **预期结果**：说明行覆盖考核、职务、调标、晋升节点；无未解释大额差异；差异可关联到具体政策节点。
- **结果**：□通过 □失败（备注：________）

#### TC-XC-02 审批表与花名册

- **政策依据**：本地 `payrollTitle`、审批表样式规定。
- **前置数据**：已完成 TC-PC-01 或 TC-NP-01 办理记录。
- **操作步骤**：`#payroll-change-approval-report`、`#payroll-change-register-report` 打印/导出。
- **预期结果**：变动类别、前后工资、审批标题与 cyxx 一致；与 hisbase 一致。
- **结果**：□通过 □失败（备注：________）

#### TC-XC-03 舍入与 cyxx 一致

- **政策依据**：2.2 表 `roundingMode`、`roundToInteger`。
- **前置数据**：任选一条产生小数差额的标准调标或津贴变动。
- **操作步骤**：对比试算明细各分项之和与合计。
- **预期结果**：舍入规则全模块一致；与 `PayrollRoundingPolicy` 及 cyxx 配置一致。
- **结果**：□通过 □失败（备注：________）

---

## 6. 执行流程与里程碑

| 阶段 | 活动 | 产出 |
| --- | --- | --- |
| 0. 准备 | 整理政策包、cyxx 截图、标准表快照 | 第 2 节表格已填 |
| 1. 抽样 | 完成第 3 节人员登记 | 抽样台账 |
| 2. 试算勾检 | 执行第 5 节用例试算部分 | 截图、说明行记录 |
| 3. 办理核对 | 办理 + hisbase 核对 | 第 7 节字段表 |
| 4. 还原复测 | 可还原用例执行还原 | 还原前后对比 |
| 5. 收尾 | 缺陷汇总、sign-off | 第 8 节 |

**建议周期**：按模块分批（晋升类 → 定资类 → 津贴调标 → 特殊 → 横切），每批完成后召开简短评审。

---

## 7. hisbase 写入核对表

办理后对新写入的 `hisbase` 记录核对（**合格判定仍以政策为准**；本表确保写入完整）。

| 字段 | 政策/业务说明 | 试算前 | 试算后 | 办理后 | □OK |
| --- | --- | --- | --- | --- | --- |
| `jsnf` / `jsyf` | 执行年月符合政策（次月/当月） | | | | |
| `jslb` | 变动类别与菜单/政策一致 | | | | |
| `zwbm` / `jx` / `dc` / `xj` | 职务、级别、档次/薪级 | | | | |
| `zwgz` / `jbgz` 等 | 分项工资 | | | | |
| `jxjtbz` | 警衔(lb=jx)或监察(lb=mt)标准年月 | | | | |
| `jcjtbz` / `spjtbz` | 检察/审判标准年月 | | | | |
| `jxjt` 等 | 津贴合成（机关 01–03） | | | | |
| `hj2` / `bbz` | 合计；调标类差额规则 | | | | |
| `sid` | 新记录 `''`；旧链头指向新 id | | | | |
| `tbnd` / `dcnd` / `xjnd` | 起算年月是否更新 | | | | |

**链表检查**：链头 `sid=''`（非 NULL）；无多链头；无悬空 `sid`（恢复后亦同）。

---

## 8. 缺陷记录与 sign-off

### 8.1 缺陷记录模板

| 缺陷编号 | 用例编号 | 人员 grbm | 执行年月 | 政策条款 | 现象 | 严重级别 | 状态 | 处理人 |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| DEF-001 | TC-PC-02 | | | P-01 §… | | 高/中/低 | 打开/关闭 | |

严重级别建议：

- **高**：金额错误、错误入列、办理后数据不可还原、违反强制性政策。
- **中**：提示语不清、字段写入偏差但不影响合计、边界情形未覆盖。
- **低**：报表样式、说明行措辞。

### 8.2 测试汇总

| 模块 | 用例数 | 通过 | 失败 | 阻塞 | 通过率 |
| --- | --- | --- | --- | --- | --- |
| 正常/级别晋升 | 6 | | | | |
| 职务/学历 | 4 | | | | |
| 定资转正套改 | 4 | | | | |
| 津贴/等级变化 | 7 | | | | |
| 标准调标 | 4 | | | | |
| 特殊变动 | 4 | | | | |
| 横切 | 3 | | | | |
| **合计** | **32** | | | | |

### 8.3 Sign-off 签字页

| 角色 | 姓名 | 日期 | 签字 | 意见 |
| --- | --- | --- | --- | --- |
| 政策负责人 | | | | □同意验收 □有条件通过 □不通过 |
| 测试执行人 | | | | |
| 业务骨干 | | | | |
| 项目负责人 | | | | |

**有条件通过条件**（如有）：仅允许 P-__ 以下低级别缺陷遗留，且须在 ____ 前关闭。

---

## 附录 A：Web 菜单与 VFP 对照

| Web 菜单编码 | 标题 | 路径 | 写权限码 | VFP 参考 |
| --- | --- | --- | --- | --- |
| `NORMAL_PROMOTION` | 正常档次/薪级晋升 | `#normal-promotion` | `NORMAL_PROMOTION_WRITE` | `zcjbgzjs` |
| `LEVEL_PROMOTION` | 级别晋升 | `#level-promotion` | `LEVEL_PROMOTION_WRITE` | `zcjbgzjs` / `jbgdgzjs` |
| `POSITION_CHANGE_PROMOTION` | 职务变化晋升 | `#position-change-promotion` | `POSITION_CHANGE_PROMOTION_WRITE` | `zwbhgzjs` |
| `EDUCATION_PROMOTION` | 学历晋升 | `#education-promotion` | `EDUCATION_PROMOTION_WRITE` | 学历定级 |
| `REGULARIZATION` | 转正定级 | `#regularization` | `REGULARIZATION_WRITE` | `zzdj` |
| `NEW_PERSONNEL_SALARY` | 新进定资 | `#new-personnel-salary` | `NEW_PERSONNEL_SALARY_WRITE` | `drdz` |
| `REGULARIZATION_HIGH_GRADE` | 转正高定 | `#regularization-high-grade` | `REGULARIZATION_HIGH_GRADE_WRITE` | `zzgd` |
| `WAGE_REFORM_2006` | 2006 套改 | `#wage-reform-2006` | `WAGE_REFORM_2006_WRITE` | `tg2006` |
| `TEACHING_ALLOWANCE_ADJUSTMENT` | 教护龄津贴 | `#teaching-allowance-adjustment` | `TEACHING_ALLOWANCE_ADJUSTMENT_WRITE` | `tzjhljt` |
| `POLICE_ALLOWANCE_ADJUSTMENT` | 警衔津贴 | `#police-allowance-adjustment` | `POLICE_ALLOWANCE_ADJUSTMENT_WRITE` | `tzjxjtbz` |
| `PROSECUTION_ALLOWANCE_ADJUSTMENT` | 检察津贴 | `#prosecution-allowance-adjustment` | `PROSECUTION_ALLOWANCE_ADJUSTMENT_WRITE` | `tzjcjtbz` |
| `JUDICIAL_ALLOWANCE_ADJUSTMENT` | 审判津贴 | `#judicial-allowance-adjustment` | `JUDICIAL_ALLOWANCE_ADJUSTMENT_WRITE` | `tzspjtbz` |
| `SUPERVISION_ALLOWANCE_ADJUSTMENT` | 监察津贴 | `#supervision-allowance-adjustment` | `SUPERVISION_ALLOWANCE_ADJUSTMENT_WRITE` | `tzmtjtbz` |
| `POLICE_RANK_CHANGE_PROMOTION` | 警衔变化 | `#police-rank-change-promotion` | `POLICE_RANK_CHANGE_PROMOTION_WRITE` | `jxbhgzjs` |
| `PROSECUTION_RANK_CHANGE_PROMOTION` | 检察官等级变化 | `#prosecution-rank-change-promotion` | `PROSECUTION_RANK_CHANGE_PROMOTION_WRITE` | `jcbhgzjs` |
| `JUDICIAL_RANK_CHANGE_PROMOTION` | 法官等级变化 | `#judicial-rank-change-promotion` | `JUDICIAL_RANK_CHANGE_PROMOTION_WRITE` | 审判等级 |
| `SUPERVISION_RANK_CHANGE_PROMOTION` | 监察等级变化 | `#supervision-rank-change-promotion` | `SUPERVISION_RANK_CHANGE_PROMOTION_WRITE` | 监察等级 |
| `BASIC_SALARY_STANDARD_ADJUSTMENT` | 调整基本工资标准 | `#basic-salary-standard-adjustment` | `BASIC_SALARY_STANDARD_ADJUSTMENT_WRITE` | `tbjs` |
| `PERFORMANCE_RATIO_ADJUSTMENT` | 调整绩效比例 | `#performance-ratio-adjustment` | `PERFORMANCE_RATIO_ADJUSTMENT_WRITE` | `tzjtbl` |
| `ALLOWANCE_RECALCULATION` | 重算津补贴 | `#allowance-recalculation` | `ALLOWANCE_RECALCULATION_WRITE` | `jbtjs` |
| `SALARY_STANDARD_ADJUSTMENT` | 2024.07 调标 | `#salary-standard-adjustment` | `SALARY_STANDARD_ADJUSTMENT_WRITE` | `tbjs2021` |
| `FLOATING_TO_FIXED` | 浮动转固定 | `#floating-to-fixed` | `FLOATING_TO_FIXED_WRITE` | `fdgd` |
| `OTHER_PAYROLL_CHANGE` | 其它情况工资变动 | `#other-payroll-change` | `OTHER_PAYROLL_CHANGE_WRITE` | `fzcgzbdcl` |
| `INTERN_SALARY_CHANGE` | 见习工资变动 | `#intern-salary-change` | `INTERN_SALARY_CHANGE_WRITE` | `jxgz2` |
| `MONTHLY_AVERAGE_SALARY` | 月平均工资 | `#monthly-average-salary` | `MONTHLY_AVERAGE_SALARY_WRITE` | `dybdmx` |
| `AUDIT` | 工资推算对账 | `#audit` | `AUDIT_READ` | 批量对账 |
| `PAYROLL_HISTORY` | 工资变动历史 | `#payroll-history` | `PAYROLL_READ` | `gzjshmc` |
| `PAYROLL_CHANGE_APPROVAL_REPORT` | 工资变动审批表 | `#payroll-change-approval-report` | `REPORT_READ` | `dy spb` |
| `PAYROLL_CHANGE_REGISTER_REPORT` | 工资变动花名册 | `#payroll-change-register-report` | `REPORT_READ` | `dy hmc` |
| `LOCAL_POLICY_CONFIG` | 本地工资政策 | `#local-policy-config` | `SYSTEM_CONFIG` | `zcbdh` |

---

## 附录 B：操作日志关键字

办理/还原后在 **上机日志**（`OPERATION_LOG_READ`）中检索：

- `APPLY_*`：办理成功记录
- `ROLLBACK_*`：还原记录
- `DATA_RESTORE`：非本方案范围，勿与变动办理混淆

缺陷报告中应附日志时间与操作类型。

---

## 附录 C：不在本方案内的模块（参考）

| 模块 | 说明 |
| --- | --- |
| 权限 / UKey | `#security` |
| 数据备份恢复 | 数据维护 |
| 数据交换 | `#data-exchange` |
| 离退休人员 | 退休域菜单 |

如需扩展，可复制第 4 节用例模板追加用例。

---

*文档结束。执行时请同步更新第 2、3、8 节填表内容与用例结果栏。*
