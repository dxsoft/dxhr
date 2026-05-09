# VFP 菜单迁移清单

本文根据 `rsgzgl2006nid/Menu/GZJSGL.MPR` 整理，用于把 VFP 桌面菜单逐步迁移到 Spring Boot 的 `app_menu` 菜单体系。

## 迁移策略

1. 先把 VFP 顶级菜单录入 `app_menu`，状态为禁用，作为“待迁移菜单”。
2. 当前已实现的 Spring Boot 页面继续保持启用：
   - 人员查询
   - 工资试算
   - 批量对账
   - 权限管理
3. 后续每完成一个 VFP 功能模块迁移，再把对应菜单改为启用并绑定具体前端页面。
4. 原 VFP 菜单里的 `DO FORM xxx` 可作为后续模块命名和接口设计线索。

## 当前已启用菜单

| 菜单编码 | 标题 | 路径 | 权限码 | 说明 |
| --- | --- | --- | --- | --- |
| `PERSONNEL` | 人员查询 | `#personnel` | `PERSONNEL_READ` | 当前 Spring Boot 已实现 |
| `PAYROLL` | 工资试算 | `#payroll` | `PAYROLL_READ` | 当前 Spring Boot 已实现 |
| `AUDIT` | 批量对账 | `#audit` | `AUDIT_READ` | 当前 Spring Boot 已实现 |
| `BASIC_STANDARDS` | 基本工资标准 | `#basic-standards` | `STANDARD_READ` | 当前 Spring Boot 已实现，只读查询 |
| `INTERN_SALARY_STANDARDS` | 见习工资标准 | `#intern-salary-standards` | `STANDARD_READ` | 当前 Spring Boot 已实现，只读查询 |
| `ALLOWANCE_STANDARDS` | 津贴补贴标准 | `#allowance-standards` | `STANDARD_READ` | 当前 Spring Boot 已实现，只读查询 |
| `RANK_ALLOWANCE_STANDARDS` | 警衔津贴标准 | `#rank-allowance-standards` | `STANDARD_READ` | 当前 Spring Boot 已实现，只读查询 |
| `RETAINED_ALLOWANCE_STANDARDS` | 保留福补标准 | `#retained-allowance-standards` | `STANDARD_READ` | 当前 Spring Boot 已实现，只读查询 |
| `YEAR_ALLOWANCE_STANDARDS` | 年补贴标准 | `#year-allowance-standards` | `STANDARD_READ` | 当前 Spring Boot 已实现，只读查询 |
| `SECURITY` | 权限管理 | `#security` | `SECURITY_ADMIN` | 当前 Spring Boot 已实现 |

## VFP 顶级菜单种子

这些菜单已作为禁用状态种子写入 `app_menu`，不会影响当前导航。

| 菜单编码 | VFP 菜单 | 现代标题 | 权限码 | 状态 |
| --- | --- | --- | --- | --- |
| `LEGACY_INFO_MAINTENANCE` | `1.信息维护` | VFP-信息维护（待迁移） | `PERSONNEL_READ` | 禁用 |
| `LEGACY_PAYROLL_CHANGE` | `2.工资变动` | VFP-工资变动（待迁移） | `PAYROLL_READ` | 禁用 |
| `LEGACY_DATA_EXCHANGE` | `3.数据交换` | VFP-数据交换（待迁移） | `DATA_EXCHANGE_READ` | 禁用 |
| `LEGACY_REPORT_PRINT` | `4.报表打印` | VFP-报表打印（待迁移） | `REPORT_READ` | 禁用 |
| `LEGACY_QUERY_STATISTICS` | `5.查询统计` | VFP-查询统计（待迁移） | `REPORT_READ` | 禁用 |
| `LEGACY_INITIAL_SETTINGS` | `6.初始设置` | VFP-初始设置（待迁移） | `SYSTEM_CONFIG` | 禁用 |
| `LEGACY_SYSTEM_MAINTENANCE` | `7.系统维护` | VFP-系统维护（待迁移） | `SECURITY_ADMIN` | 禁用 |
| `LEGACY_HELP` | `8.系统帮助` | VFP-系统帮助（待迁移） | `HELP_READ` | 禁用 |

## VFP 业务入口摘录

### 信息维护

| 菜单项 | VFP 调用 | 迁移建议 |
| --- | --- | --- |
| 人员信息维护 | `DO FORM zzryjkxx` | 人员档案维护模块 |
| 工资变动情况 | `DO FORM gzjshmc` | 工资历史/变动花名册 |
| 变动人员信息 | `DO FORM bdryxx` | 变动人员清单 |
| 年度考核结果录入 | `DO FORM ndkhlr1` | 年度考核录入 |
| 年度考核结果统计 | `DO FORM khjgtj` | 年度考核统计 |

### 工资变动

| 菜单项 | VFP 调用 | 迁移建议 |
| --- | --- | --- |
| 职务／职级（岗位）变动 | `DO FORM zwbhgzjs` | 工资变动审批/计算 |
| 调整教（护）龄津贴 | `DO FORM tzjhljt` | 教护龄津贴调整 |
| 见习工资变动处理 | `DO FORM jxgz2` | 见习工资计算 |
| 见习人员转正定级 | `DO FORM zzdj` | 转正定级 |
| 新增人员确定工资 | `DO FORM drdz` | 新增人员工资确定 |
| 其它情况工资变动 | `DO FORM fzcgzbdcl` | 其他工资变动 |
| 浮动转固定工资变动 | `DO FORM fdgd` | 浮动工资转固定 |
| 级别滚动晋升 | `DO FORM jbgdgzjs` | 级别滚动晋升 |
| 转正高定档次薪级 | `DO FORM zzgd` | 高定档次薪级 |
| 月平均工资计算 | `DO FORM dybdmx` | 月平均工资 |
| 重算津补贴 | `DO FORM jbtjs` | 津补贴重算 |
| 2024.07调标 | `DO FORM forms\tbjs2021` | 调标计算 |
| 2006年工资套改 | `DO FORM tg2006` | 工资套改 |

### 数据交换

| 菜单项 | VFP 调用 | 迁移建议 |
| --- | --- | --- |
| 导出下属单位建库数据 | `DO FORM jksjdc` | 数据包导出 |
| 导出下属单位申报数据 | `DO FORM sjsb` | 申报数据导出 |
| 读取上级单位下发数据 | `DO FORM sjdr` | 数据包导入 |
| 读入下属单位建库数据 | `DO FORM jksjdr` | 下属数据导入 |
| 读入审核下属单位申报数据 | `DO FORM sjsh` | 申报审核 |
| 下发工资审批数据 | `DO FORM sjdc` | 审批数据下发 |
| 按人员导出 | `DO FORM dc` | 人员导出 |
| 按人员导入 | `DO FORM dr` | 人员导入 |
| 导出工资年报数据 | `DO FORM dcnbsj` | 年报导出 |

### 报表打印

| 菜单项 | VFP 调用 | 迁移建议 |
| --- | --- | --- |
| 打印工资变动审批表 | `DO FORM dy WITH "spb"` | 审批表报表 |
| 打印工资变动花名册 | `DO FORM dy WITH "hmc"` | 花名册报表 |
| 打印人员信息采集表 | `DO FORM dycjb` | 信息采集表 |
| 打印人员信息登记表 | `DO FORM dyjdb` | 信息登记表 |
| 打印2006套改公示表 | `DO FORM dygsb` | 套改公示表 |

### 查询统计

| 菜单项 | VFP 调用 | 迁移建议 |
| --- | --- | --- |
| 人员基本情况统计 | `DO FORM tj WITH "tj"` | 人员统计 |
| 工资变动情况统计 | `DO FORM tj WITH "bdqk"` | 工资变动统计 |
| 人员信息综合查询 | `DO FORM xxcx` | 综合查询 |
| 已达退休年龄人员查询 | `DO FORM ytxrytj` | 退休年龄查询 |

### 初始设置

| 菜单项 | VFP 调用 | 迁移建议 |
| --- | --- | --- |
| 本地工资政策 | `DO FORM zcbdh` | 政策参数 |
| 基本工资标准 | `DO FORM zwgzsz06` | 工资标准维护 |
| 见习工资标准 | `DO FORM jxgzbzwh` | 见习标准维护 |
| 津贴补贴(绩效工资) | `DO FORM jbtbzsz06` | 津补贴标准 |
| 警衔津贴标准 | `DO FORM jxjtbzsz` | 警衔津贴标准 |
| 检察津贴标准 | `DO FORM jcjtbzsz` | 检察津贴标准 |
| 审判津贴标准 | `DO FORM spjtbzsz` | 审判津贴标准 |
| 保留福补标准 | `DO FORM blfb` | 保留福补标准 |
| 农村学校教师补贴 | `DO FORM njbtsz` | 农村教师补贴 |
| 单位信息维护 | `DO FORM dwxx` | 单位维护 |
| 设置常用值 | `DO FORM dmwh` | 字典维护 |
| 系统初始化 | `DO initi` | 初始化流程 |
| 从Excel导入 | `DO infromtemp` | Excel 导入 |
| 系统选项 | `DO FORM options` | 系统配置 |

### 系统维护与帮助

| 菜单项 | VFP 调用 | 迁移建议 |
| --- | --- | --- |
| 数据维护 | `DO FORM czsjgl` | 数据维护 |
| 上机日志 | `_7gk0ozrjp` | 操作日志 |
| 用户管理 | `_7gk0ozrjq` | 已迁移为权限管理 |
| 重新登录 | `DO FORM login` | 已迁移为登录/退出 |
| 帮助 | `_7gk0ozrjr` | 帮助页面 |
| 软件注册、升级 | `DO FORM zhuce` | 注册/升级信息 |

## 后续落地建议

1. 先从“初始设置”中的标准表维护开始，因为当前工资试算已大量依赖这些标准表。
2. 再迁移“查询统计”和“报表打印”，它们以只读查询和导出为主，风险较低。
3. 最后迁移写入型“工资变动”流程，并继续保持和旧 `hisbase` 的逐项对账。
