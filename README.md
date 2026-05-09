# dxhr

## VFP 迁移资料

- [VFP 到 Spring Boot 迁移分析](docs/vfp-to-springboot-migration.md)

## Spring Boot 后端

当前仓库已包含第一版 Spring Boot + MySQL 后端骨架，使用 `gzjsgl` 数据库结构中的核心表提供只读查询接口。

### 本地运行

```bash
export RSGZGL_DB_URL="jdbc:mysql://localhost:3306/gzjsgl?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true"
export RSGZGL_DB_USERNAME="root"
export RSGZGL_DB_PASSWORD="你的密码"
mvn spring-boot:run
```

首次启动会自动创建 Spring Boot 权限表，详见 `docs/security-rbac-schema.sql`。

如需初始化管理员账号，请设置环境变量；系统只会在账号不存在时创建，不会覆盖已有密码：

```bash
export RSGZGL_ADMIN_USERNAME="admin"
export RSGZGL_ADMIN_PASSWORD="请使用强密码"
export RSGZGL_ADMIN_DISPLAY_NAME="系统管理员"
```

当前权限模型采用用户、角色、功能权限、角色单位数据范围。人员和工资接口会按用户可访问单位限制数据范围。
登录后拥有 `SECURITY_ADMIN` 权限的用户可在首页“权限管理”区域维护用户、角色、功能权限绑定和角色单位范围。
登录成功、登录失败、退出登录、修改密码和权限管理操作都会写入安全审计日志。
前端导航由 `app_menu` 和 `/api/auth/menus` 动态生成，只显示当前用户具备权限的菜单。
“人员结构统计”页面提供 `dryjbxx` 按单位、人员类别、单位属性和岗位分类的人数汇总查询。
“年度考核结果”页面提供 `dndkh` 只读查询，按用户单位数据范围限制可见人员。
“年度考核统计”页面提供 `dndkh` 按年度、单位、考核结果的汇总查询。
“变动人员信息”页面提供 `dryjbxxb` 只读查询，并关联 `hisbaseb` 中 `sid` 为空的当前变动工资，按用户单位数据范围限制可见人员。
“任职岗位信息”页面提供 `dryzwbh` 只读查询，按用户单位数据范围限制可见人员。
“学历信息”页面提供 `dxl` 只读查询，按用户单位数据范围限制可见人员。
“工资变动历史”页面提供 `hisbase` 只读查询，按用户单位数据范围限制可见人员；同一 `dwbm + grbm` 为同一人的工资变动链，`sid` 指向下一次工资变动，`sid` 为空表示当前执行工资。
“单位信息维护”页面提供 `dwbm` 只读查询，按用户单位数据范围限制可见单位。
“设置常用值”页面提供 `dmb` 字典表只读查询，支持编码前缀和关键词筛选。
“本地工资政策”页面提供 `cyxx` 和 `xtcs` 只读查询，用于核对影响工资计算的全局参数。
“基本工资标准”页面提供 `bz06_zwgz`、`bz06_zwgz_gr`、`bz06_jbgz`、`bz06_xjgz` 四类标准表只读查询。
“见习工资标准”页面提供 `bz06_zzdz` 只读查询，支持标准年月和学历/职务关键词筛选。
“津贴补贴标准”页面提供 `bz06_jbt` 只读查询，支持标准年月、项目和职务编码筛选。
“警衔津贴标准”页面提供 `jxjtbz` 只读查询，支持标准年月、警衔名称/编码和类别筛选。
“保留福补标准”页面提供 `bz06_blfb` 只读查询，支持职务编码/名称筛选。
“年补贴标准”页面提供 `njbt` 只读查询，支持标准年月筛选。
“2006套改标准”页面提供 `bz06_tgb`（2006 年 7 月工资制度改革套改标准）只读查询，支持职务编码筛选。

### 首批接口

- `GET /api/organizations`
- `GET /api/personnel`
- `GET /api/personnel/{uid}`
- `GET /api/personnel/structure-summary`
- `GET /api/personnel/{uid}/positions`
- `GET /api/personnel/positions`
- `GET /api/personnel/{uid}/education`
- `GET /api/personnel/education`
- `GET /api/personnel/{uid}/assessments`
- `GET /api/personnel/assessments`
- `GET /api/personnel/assessment-summary`
- `GET /api/personnel/changed`
- `GET /api/organizations/maintenance`
- `GET /api/dictionaries`
- `GET /api/system-config/local-policies`
- `GET /api/system-config/options`
- `GET /api/payroll/fields`
- `GET /api/payroll/histories`
- `GET /api/payroll/position-standards`
- `GET /api/payroll/basic-standards`
- `GET /api/payroll/intern-salary-standards`
- `GET /api/payroll/allowance-standards`
- `GET /api/payroll/rank-allowance-standards`
- `GET /api/payroll/retained-allowance-standards`
- `GET /api/payroll/year-allowance-standards`
- `GET /api/payroll/wage-reform-standards`
- `GET /api/payroll/personnel/{uid}/calculation-context`
- `GET /api/payroll/personnel/{uid}/calculation-preview`
- `GET /api/payroll/calculation-audits`
- `GET /api/payroll/calculation-audit-summary`

人员接口默认会对身份证号做脱敏处理，避免直接暴露完整证件号码。

`calculation-context` 接口用于工资计算迁移的第一步：读取单人最新工资历史、已保存的工资项金额和匹配到的标准表数据，暂不写入数据库。
该接口已经包含基础标准表计算值：职务工资、级别工资、薪级工资和技术等级工资，用于和 `hisbase` 存量金额对账。
职务工资 `ZWGZSE2` 已按 `zwgz06_gr + zwgz06` 组合计算，覆盖机关技师/工人等岗位档次工资。
同时包含部分津补贴计算值：基础绩效、工作性/生活性补贴、保留福补和年补贴，用于继续对齐 `gzjs06.prg` 的津补贴段。
基础绩效/生活性补贴会按 `dwbm.dfbt` 和个人 `jzgb` 发放审批状态置零。
`totalComparison` 会进一步计算教护龄津贴、提高工资，并替换当前已迁移项目后给出合计差额。
当前也会对比警衔/警务津贴和浮动工资，对应 `JXJT`、`FDGZ2`。
奖金结余 `JJJY2` 按旧系统主链口径处理：已有旧值时保留，旧值为 0 时再按 `cyxx.jjjy`、1993 年前职务和 `bz06_jjjy` 标准表试算。
岗位津贴 `GWJT2` 已确认不考虑迁移，列入 `excludedComponents`，只保留旧值。
套改/特岗保留 `TGBLBF` 按主链规则处理：机关人员清零，事业单位人员保留旧值。
`QTBT/SIDBT/ZWJT/ZFBT/JZMCBT/GWJT2` 已作为手工或暂不考虑字段列入 `excludedComponents`，只保留旧值。
`PGBC` 已作为特殊工资变动保留项列入 `pgbcComparison`，当前只读对账保留旧值。
`calculation-preview` 会把已迁移工资项、排除字段、PGBC 和合计整理成更适合界面展示的只读试算结果。
批量对账接口会分页执行同一套只读计算，返回每个人的合计差额和差异汇总。
远程数据库对账建议先使用较小分页，例如 `size=5` 或 `size=10`，再逐步扩大范围。
对账结果中的 `componentDifferences` 会标明具体差异字段；`DFBT2` 会按人员性质显示为机关人员“生活性补贴”或事业单位人员“基础性绩效工资”。
