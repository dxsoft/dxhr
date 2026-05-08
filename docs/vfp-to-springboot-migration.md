# VFP 到 Spring Boot 迁移分析

本文基于当前仓库中的 `rsgzgl2006nid` 目录整理，用于指导后续把 Visual FoxPro 人事工资管理系统逐步重构为 Spring Boot 项目。

## 1. 当前源码快照

当前已上传内容主要包括：

| 路径 | 内容 |
| --- | --- |
| `rsgzgl2006nid/dxrsgzgl.pjx` / `dxrsgzgl.PJT` | VFP 项目文件 |
| `rsgzgl2006nid/PROGS/` | 约 405 个 `.prg` / `.PRG` 程序文件 |
| `rsgzgl2006nid/FORMS/` | 192 个 `.scx` 和 192 个 `.sct` 表单文件 |
| `rsgzgl2006nid/Menu/` | `gzjsgl.mnx`、`gzjsgl.MNT`、`GZJSGL.MPR` |
| `rsgzgl2006nid/REPORTS/` | 109 个 `.frx` 和 109 个 `.frt` 报表文件 |
| `rsgzgl2006nid/LIBS/` | `gzjsgl.vcx`、`_base.vcx`、`registry.vcx` 及对应 `.vct` |
| `rsgzgl2006nid/` | `db_init.bat`、2 个 CSV、`FOXUSER.DBF`、菜单清理记录文本 |

当前仍需要从运行环境或数据库服务器补充的资料：

- MySQL 数据库 `gzjsgl` 的完整 DDL、索引、视图、触发器和存储过程。
- `usp_init` 等初始化存储过程定义。
- 脱敏业务数据样本，尤其是人员、工资、职务、学历、考核和工资标准表。
- 真实 `sys.ini` 的结构示例，但不要提交生产账号密码。

因此，当前阶段已经可以做完整 VFP 源码梳理、菜单到表单映射、报表清单整理、业务模块拆分和 Spring Boot 目标架构设计。若要开始实现可运行的 Spring Boot 服务，下一步重点是导出 MySQL 表结构和脱敏测试数据。

## 2. 入口和运行方式

主入口位于：

- `rsgzgl2006nid/PROGS/rsgzgl.prg`

该程序负责：

1. 声明大量全局变量。
2. 设置 VFP 运行环境、默认目录、搜索路径。
3. 读取 `sys.ini` 中的数据库配置。
4. 根据 `dbtype` 连接 SQL Server、MySQL 或 SQLite；本项目实际目标数据库确认为 MySQL。
5. 加载登录表单、菜单并进入 `READ EVENTS`。
6. 退出时断开数据库连接。

重要特征：

- 当前系统不是纯 DBF 单机应用，代码中大量使用 `SQLSTRINGCONNECT` / `SQLEXEC` 访问 MySQL 数据库。
- `rsgzgl.prg` 中仍保留 SQL Server 和 SQLite 分支，但 Spring Boot 迁移应以 MySQL 为准。
- 连接配置来自 `sys.ini`，可迁移为 Spring Boot 的 `application.yml`。
- VFP 使用全局变量和可更新游标，迁移时需要改为 Spring Bean、事务边界和显式 Repository/Service。

配置初始化脚本：

- `rsgzgl2006nid/db_init.bat`

它生成 `sys.ini`，默认数据库名为 `gzjsgl`。

系统初始化逻辑：

- `rsgzgl2006nid/PROGS/initi.prg`

其中调用：

- MySQL: `call usp_init(@result)`
- SQL Server 旧分支: `p_init ?@result`

这些存储过程定义当前未上传。数据库迁移前必须从现有 MySQL 数据库导出表结构、索引、视图、存储过程和基础数据。

## 3. 模块划分

### 3.1 数据访问封装

大量 `crtv*.prg` 文件用于创建 VFP 可更新游标，类似旧系统的数据访问层。

代表文件：

| 文件 | 作用 |
| --- | --- |
| `PROGS/crtvryjbxx.prg` | 人员基本信息，查询 `dryjbxx` 并生成 `ryjbxx` 游标 |
| `PROGS/crtvryzwbh.prg` | 人员职务变化 |
| `PROGS/crtvxl.prg` | 学历信息 |
| `PROGS/crtvndkh.prg` | 年度考核 |
| `PROGS/crtvrptinfo.prg` | 报表信息 |

迁移建议：

- 将 `crtv*.prg` 按表/业务域迁移为 Spring `Repository`。
- 对简单 `select * from dxxx` 的游标先做只读 API。
- 写入逻辑从 VFP 的 `CURSORSETPROP` / `TABLEUPDATE` 改为 Spring 事务。

### 3.2 人员信息

核心文件：

- `PROGS/crtvryjbxx.prg`
- `PROGS/rzjl.prg`
- `PROGS/rzjlall.prg`
- `PROGS/rzjlper.prg`
- `PROGS/rzsj.prg`

核心表/游标：

- `dryjbxx`
- `ryjbxx`
- `ryzwbh`
- `jdzw`

迁移建议：

- 先建立人员基础资料 API：查询、分页、按单位过滤。
- 再迁移任职记录、学历、职务变化等子表。
- 人员编码常由 `dwbm + grbm` 组成，需要确认数据库中是否已有稳定主键 `uid`。

### 3.3 工资计算

核心文件：

| 文件 | 说明 |
| --- | --- |
| `PROGS/gzjs06.prg` | 2006 工资计算主链 |
| `PROGS/jbgz06.prg` | 基本工资计算 |
| `PROGS/jxgz.prg` / `PROGS/jxgz06.prg` | 绩效工资 |
| `PROGS/xjgz.prg` / `PROGS/xjgz06.prg` | 薪级工资 |
| `PROGS/jydjgz06.prg` | 警员等级工资 |
| `PROGS/jsdjgz06.prg` | 技术等级工资 |
| `PROGS/tg06.prg` | 工资套改 |
| `PROGS/jytg.prg` | 警员套改 |
| `PROGS/tzcs.prg` | 调资测算 |
| `PROGS/pushgz.prg` / `PROGS/pushhis.prg` | 推送工资和历史记录 |

工资计算迁移风险最高，原因：

- `gzjs06.prg` 中大量使用宏替换、全局变量和当前游标状态。
- 计算字段由 `fldgz` 元数据驱动。
- 同一计算链会读取人员、职务、学历、标准表、考核、津补贴等多个业务域。
- 金额结果必须和旧系统逐人逐项对账。

迁移建议：

1. 先把 `fldgz`、工资标准表、人员历史表结构导出。
2. 选取一个固定年度和少量脱敏人员作为金标准样本。
3. 在 Java 中实现纯计算服务，不直接依赖 Web 层。
4. 用自动化测试对比 VFP 旧结果和 Java 新结果。
5. 通过后再扩大人员范围和工资场景。

### 3.4 职务、职级、学历

职务相关：

- `PROGS/zwbm.prg`
- `PROGS/zwmc.prg`
- `PROGS/zwgw.prg`
- `PROGS/zwbhjs06.prg`
- `PROGS/zwbhjs16.prg`

学历相关：

- `PROGS/xl.prg`
- `PROGS/xlcc.prg`
- `PROGS/xlmc.prg`
- `PROGS/crtvxl.prg`

迁移建议：

- 优先把编码、名称、层次等转换函数整理成 Java 枚举、字典表或查询服务。
- 对依赖历史时间点的函数，统一建立日期值对象，避免继续使用字符串拼接日期。

### 3.5 导入、导出和报表

导入导出：

- `PROGS/infromtemp.prg`
- `PROGS/outinfotoxls.prg`
- `PROGS/outexcel*.prg`
- `PROGS/readfromxls.prg`

报表：

- `PROGS/rptpreview.prg`
- `PROGS/crtvrptinfo.prg`
- `REPORTS/*.frx` / `REPORTS/*.frt`

迁移建议：

- Excel COM 自动化改为 Apache POI、EasyExcel 或 CSV 导出。
- VFP 报表预览改为浏览器预览、PDF 或 Excel 模板。
- 先迁移纯查询导出，再处理带格式、套打和审批流的报表。

### 3.6 菜单和表单

核心文件：

- `Menu/gzjsgl.mnx`
- `Menu/gzjsgl.MNT`
- `Menu/GZJSGL.MPR`
- `FORMS/*.scx` / `FORMS/*.sct`

`GZJSGL.MPR` 中包含大量 `DO FORM ...` 菜单入口，例如：

- `do form gzjshmc`
- `do form ndkhlr1`
- `do form zwbhgzjs`
- `do form jxgz2`
- `do form drdz`
- `do form tg2006`
- `do form dc`
- `do form dr`
- `do form dwxx`
- `do initi`
- `do infromtemp`
- `do form login`

迁移建议：

- 先从 `GZJSGL.MPR` 提取菜单项、表单名、调用参数，形成“功能清单”。
- 再按表单对应的业务域归类到 Spring Boot 模块和前端页面。
- `.scx/.sct` 是 VFP 表单元数据，不能直接转换为 Web 页面；应读取其中控件、数据源和事件代码，重做为 Vue/React/Thymeleaf 页面。

## 4. Spring Boot 目标结构建议

建议新项目使用分层结构：

```text
src/main/java/com/dxsoft/rsgzgl
├── RsgzglApplication.java
├── config
│   ├── DatabaseConfig.java
│   └── JacksonConfig.java
├── common
│   ├── error
│   └── model
├── organization
│   ├── controller
│   ├── service
│   ├── repository
│   └── dto
├── personnel
│   ├── controller
│   ├── service
│   ├── repository
│   └── dto
├── position
│   ├── service
│   └── repository
├── education
│   ├── service
│   └── repository
├── payroll
│   ├── calculator
│   ├── service
│   ├── repository
│   └── dto
├── importexport
│   ├── service
│   └── controller
└── report
    ├── service
    └── controller
```

数据访问技术建议：

- 目标数据库为 MySQL，优先使用 MySQL Connector/J。
- 若需要保留大量历史 SQL：优先使用 MyBatis 或 JdbcTemplate。
- 若后续要重建清晰领域模型：可在稳定表结构上逐步引入 JPA。
- 工资计算不建议写在 Repository 中，应放在 `payroll.calculator` 或 `payroll.service`。
- 旧系统连接串中使用 `CHARSET=gbk`，迁移时需确认 MySQL 库表字符集和排序规则；若现库为 GBK/GB2312，需要在 JDBC URL、导出脚本和测试数据中统一处理编码。

## 5. 推荐迁移顺序

### 阶段 1：逆向 MySQL 数据库和整理功能清单

必须收集：

- MySQL 数据库表结构、索引、视图、触发器、存储过程。
- 菜单到表单的功能清单。
- 表单到业务表、`crtv*.prg`、报表文件的对应关系。
- `sys.ini` 示例配置，但不要提交真实账号密码。
- 脱敏测试数据，至少覆盖人员、工资、职务、学历、考核。

输出物：

- `schema.sql`
- `procedures.sql`
- 表字典
- 功能菜单清单
- 核心工资计算样本

### 阶段 2：搭建 Spring Boot 基础项目

建议包含：

- Spring Web
- Spring Validation
- Spring JDBC 或 MyBatis
- MySQL 数据库驱动
- Flyway 或 Liquibase
- OpenAPI 文档
- JUnit 测试

先实现：

- 健康检查接口
- 数据库连接配置
- 通用异常处理
- 人员基础信息只读查询

当前仓库已经开始搭建 Spring Boot 后端骨架：

- `pom.xml`
- `src/main/java/com/dxsoft/rsgzgl`
- `src/main/resources/application.yml`

首批只读接口覆盖：

- `GET /api/organizations`
- `GET /api/personnel`
- `GET /api/personnel/{uid}`
- `GET /api/personnel/{uid}/positions`
- `GET /api/personnel/{uid}/education`
- `GET /api/personnel/{uid}/assessments`
- `GET /api/payroll/fields`
- `GET /api/payroll/position-standards`
- `GET /api/payroll/allowance-standards`
- `GET /api/payroll/personnel/{uid}/calculation-context`
- `GET /api/payroll/personnel/{uid}/calculation-preview`
- `GET /api/payroll/calculation-audits`
- `GET /api/payroll/calculation-audit-summary`

人员接口已先做身份证号脱敏；工资标准接口目前只读，用于后续拆解 `gzjs06.prg` 前准备标准表和字段元数据。
`calculation-context` 会按人员最新 `hisbase` 记录汇总现有工资项和匹配标准表，作为 Java 侧逐项复刻计算前的对账输入。
其中已先复刻 `zwgz06.prg`、`jbgz06.prg`、`xjgz06.prg`、`jsdjgz06.prg` 对应的基础标准表计算，暂不执行整条 `gzjs06.prg` 工资重算链。
`ZWGZSE2` 已按主链组合 `zwgz06_gr + zwgz06` 对账，覆盖机关技师/工人等依赖 `bz06_zwgz_gr` 档次工资的岗位。
当前还加入了 `jcjx.prg`、`sdbt.prg`、`blfb.prg`、`njbt.prg` 的只读计算结果，覆盖基础绩效、工作性/生活性补贴、保留福补和年补贴，仍以和 `hisbase` 存量金额对账为主。
基础绩效/生活性补贴还按 `dwbm.dfbt` 和个人 `jzgb` 发放审批状态执行置零逻辑。
随后加入 `jhljt.prg` 和 `jsfszwtg2` 提高工资的对账值，并在 `totalComparison` 中用已迁移项目替换旧值后计算合计差额。
继续加入 `jxjt.prg` 和 `FDGZ06.PRG` 的对账值，覆盖警衔/警务津贴和浮动工资。
`JJJY2` 奖金结余按旧系统主链口径加入：已有旧值时保留，旧值为 0 时再按 `jjjy06.prg` 规则试算，包含 `cyxx.jjjy` 模式、1993 年前职务和 `bz06_jjjy` 标准表。
`GWJT2` 岗位津贴已确认不考虑迁移，列入 `excludedComponents`，当前只读对账保留旧值。
`TGBLBF` 套改/特岗保留按主链规则加入字段级对账：机关人员清零，事业单位人员保留旧值。
`QTBT/SIDBT/ZWJT/ZFBT/JZMCBT/GWJT2` 作为手工或暂不考虑字段列入 `excludedComponents`，仅保留旧值。
`PGBC` 作为特殊工资变动保留项列入 `pgbcComparison`，当前只读对账保留旧值，后续如实现写入再处理冲销。
`calculation-preview` 基于同一套上下文输出更适合前端展示的只读试算结果，包含已迁移工资项、排除字段、PGBC 和合计差额。
批量对账接口会分页遍历有 `hisbase` 历史的人员，复用单人上下文并统计差异人员，作为后续定位未迁移工资项的入口。
由于远程 MySQL 往返成本较高，批量对账建议先按小分页执行，再根据差异人员继续迁移剩余工资项。
对账结果会在 `componentDifferences` 中列出字段级差异；`DFBT2` 按人员性质区分为机关人员“生活性补贴”和事业单位人员“基础性绩效工资”。

### 阶段 3：迁移基础资料和查询

优先模块：

1. 单位信息。
2. 人员基本信息。
3. 职务变化。
4. 学历信息。
5. 年度考核。

这些模块边界相对清晰，适合作为 Spring Boot 第一批 API。

### 阶段 4：迁移导入导出

优先迁移：

- `outinfotoxls.prg` 中的纯查询导出。
- Excel 导入中的字段映射、校验和错误报告。

避免继续依赖桌面 Excel，服务端统一生成文件。

### 阶段 5：迁移工资计算

工资计算应最后集中处理：

1. 固化旧系统输出。
2. 拆解 `gzjs06.prg` 调用链。
3. 将标准表查询封装为 Repository。
4. 将计算逻辑拆成可测试的 Java calculator。
5. 建立金额逐字段对账测试。

## 6. 需要重点确认的问题

在继续编码前，需要从现有系统补充以下信息：

1. MySQL 版本、字符集、排序规则是什么？
2. 现网数据库中 `gzjsgl` 的完整表结构在哪里？
3. `usp_init` 以及其他存储过程定义是否能导出？
4. 是否允许上传脱敏后的 `dryjbxx`、`fldgz`、工资标准表、历史工资表样本？
5. 登录、权限、用户表对应哪些表？
6. 第一阶段要优先替换哪类功能：查询、导出、工资计算，还是完整 Web 系统？

## 7. 建议的第一批 Spring Boot API

若目标是逐步替换旧系统，第一批 API 建议如下：

| API | 说明 | VFP 参考 |
| --- | --- | --- |
| `GET /api/personnel` | 人员分页查询 | `crtvryjbxx.prg` |
| `GET /api/personnel/{uid}` | 人员详情 | `dryjbxx` / `ryjbxx` |
| `GET /api/personnel/{uid}/positions` | 职务变化 | `crtvryzwbh*.prg` |
| `GET /api/personnel/{uid}/education` | 学历信息 | `crtvxl.prg` / `xl.prg` |
| `GET /api/organizations` | 单位列表 | `dwbm` |
| `GET /api/payroll/standards` | 工资标准表查询 | `bz06_*` |
| `POST /api/exports/personnel` | 人员信息导出 | `outinfotoxls.prg` |

这样可以先建立数据库连接、DTO、分页、权限和导出框架，再逐步进入工资计算。

## 8. 结论

当前项目已经可以开始做迁移准备，但还不适合直接一次性重写完整系统。最稳妥的路径是：

1. 先导出 MySQL 数据库结构、存储过程和脱敏测试数据。
2. 用 Spring Boot 建立只读查询和导出能力。
3. 将简单字典、人员、职务、学历模块迁移为服务。
4. 最后用测试驱动迁移工资计算链。

工资计算是项目核心风险点，必须以旧系统结果为金标准做自动化回归，不能只凭代码人工翻译。

## 9. 权限模型

Spring Boot 迁移版不复用 VFP 的简化登录表单作为最终权限体系，而采用独立 RBAC 模型：

- `app_user`：用户
- `app_role`：角色，包含数据范围类型
- `app_permission`：系统功能权限
- `app_user_role`：用户角色
- `app_role_permission`：角色功能权限
- `app_role_org_scope`：角色可访问单位范围

权限脚本见 `docs/security-rbac-schema.sql`。当前已接入：

- 表单登录与会话退出
- 可通过 `RSGZGL_ADMIN_USERNAME`、`RSGZGL_ADMIN_PASSWORD`、`RSGZGL_ADMIN_DISPLAY_NAME` 初始化管理员账号；只在账号不存在时创建，不覆盖已有密码
- 功能权限：单位查询、人员查询、工资试算、批量对账、权限管理
- 人员信息和工资接口的单位数据范围控制
- 权限管理页面：用户、角色、功能权限绑定、角色单位范围维护
- 安全审计：登录成功、登录失败、退出登录、修改密码和权限管理操作

人员信息权限以单位为最小范围。角色的数据范围为 `ALL` 时可访问所有单位；为 `CUSTOM` 时只能访问 `app_role_org_scope` 中配置的单位。
