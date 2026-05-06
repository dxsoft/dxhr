# VFP 到 Spring Boot 迁移分析

本文基于当前仓库中的 `rsgzgl2006nid` 目录整理，用于指导后续把 Visual FoxPro 人事工资管理系统逐步重构为 Spring Boot 项目。

## 1. 当前源码快照

当前已上传内容主要包括：

| 路径 | 内容 |
| --- | --- |
| `rsgzgl2006nid/PROGS/` | 约 405 个 `.prg` / `.PRG` 程序文件 |
| `rsgzgl2006nid/LIBS/` | `registry.h` |
| `rsgzgl2006nid/` | `db_init.bat`、2 个 CSV、`FOXUSER.DBF`、菜单清理记录文本 |

目前未在仓库中看到以下 VFP 运行资产：

- 项目文件：`.pjx` / `.pjt`
- 表单：`.scx` / `.sct`
- 菜单：`.mnx` / `.mnt` / `.mpr`
- 类库：`.vcx` / `.vct`
- 报表：`.frx` / `.frt`
- 业务数据目录：`data/`
- 完整数据库建表 SQL 或存储过程脚本

因此，当前阶段适合先做源码梳理、业务模块拆分、数据访问模式识别和 Spring Boot 目标架构设计。若要实现完整功能迁移，还需要补齐表单、菜单、报表和数据库结构。

## 2. 入口和运行方式

主入口位于：

- `rsgzgl2006nid/PROGS/rsgzgl.prg`

该程序负责：

1. 声明大量全局变量。
2. 设置 VFP 运行环境、默认目录、搜索路径。
3. 读取 `sys.ini` 中的数据库配置。
4. 根据 `dbtype` 连接 SQL Server、MySQL 或 SQLite。
5. 加载登录表单、菜单并进入 `READ EVENTS`。
6. 退出时断开数据库连接。

重要特征：

- 当前系统不是纯 DBF 单机应用，代码中大量使用 `SQLSTRINGCONNECT` / `SQLEXEC` 访问数据库。
- SQL Server 是主路径，MySQL 和 SQLite 分支也存在。
- 连接配置来自 `sys.ini`，可迁移为 Spring Boot 的 `application.yml`。
- VFP 使用全局变量和可更新游标，迁移时需要改为 Spring Bean、事务边界和显式 Repository/Service。

配置初始化脚本：

- `rsgzgl2006nid/db_init.bat`

它生成 `sys.ini`，默认数据库名为 `gzjsgl`。

系统初始化逻辑：

- `rsgzgl2006nid/PROGS/initi.prg`

其中调用：

- SQL Server: `p_init ?@result`
- MySQL: `call usp_init(@result)`

这些存储过程定义当前未上传。数据库迁移前必须从现有数据库导出表结构、索引、视图、存储过程和基础数据。

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

迁移建议：

- Excel COM 自动化改为 Apache POI、EasyExcel 或 CSV 导出。
- VFP 报表预览改为浏览器预览、PDF 或 Excel 模板。
- 先迁移纯查询导出，再处理带格式、套打和审批流的报表。

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

- 若需要保留大量历史 SQL：优先使用 MyBatis 或 JdbcTemplate。
- 若后续要重建清晰领域模型：可在稳定表结构上逐步引入 JPA。
- 工资计算不建议写在 Repository 中，应放在 `payroll.calculator` 或 `payroll.service`。

## 5. 推荐迁移顺序

### 阶段 1：补齐资产和逆向数据库

必须收集：

- 数据库表结构、索引、视图、存储过程。
- 菜单、表单、报表和类库文件。
- `sys.ini` 示例配置，但不要提交真实账号密码。
- 脱敏测试数据，至少覆盖人员、工资、职务、学历、考核。

输出物：

- `schema.sql`
- 表字典
- 功能菜单清单
- 核心工资计算样本

### 阶段 2：搭建 Spring Boot 基础项目

建议包含：

- Spring Web
- Spring Validation
- Spring JDBC 或 MyBatis
- 数据库驱动
- Flyway 或 Liquibase
- OpenAPI 文档
- JUnit 测试

先实现：

- 健康检查接口
- 数据库连接配置
- 通用异常处理
- 人员基础信息只读查询

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

1. 当前实际使用的是 SQL Server、MySQL 还是 SQLite？
2. 现网数据库中 `gzjsgl` 的完整表结构在哪里？
3. `p_init` / `usp_init` 存储过程定义是否能导出？
4. 是否还有 `forms/`、`menu/`、`reports/`、`libs/*.vcx` 等源码目录？
5. 是否允许上传脱敏后的 `dryjbxx`、`fldgz`、工资标准表、历史工资表样本？
6. 登录、权限、用户表对应哪些表？
7. 第一阶段要优先替换哪类功能：查询、导出、工资计算，还是完整 Web 系统？

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

1. 先补齐数据库结构和缺失 VFP 资产。
2. 用 Spring Boot 建立只读查询和导出能力。
3. 将简单字典、人员、职务、学历模块迁移为服务。
4. 最后用测试驱动迁移工资计算链。

工资计算是项目核心风险点，必须以旧系统结果为金标准做自动化回归，不能只凭代码人工翻译。
