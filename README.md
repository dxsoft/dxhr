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

### 首批接口

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
- `GET /api/payroll/calculation-audits`
- `GET /api/payroll/calculation-audit-summary`

人员接口默认会对身份证号做脱敏处理，避免直接暴露完整证件号码。

`calculation-context` 接口用于工资计算迁移的第一步：读取单人最新工资历史、已保存的工资项金额和匹配到的标准表数据，暂不写入数据库。
该接口已经包含基础标准表计算值：职务工资、级别工资、薪级工资和技术等级工资，用于和 `hisbase` 存量金额对账。
同时包含部分津补贴计算值：基础绩效、工作性/生活性补贴、保留福补和年补贴，用于继续对齐 `gzjs06.prg` 的津补贴段。
基础绩效/生活性补贴会按 `dwbm.dfbt` 和个人 `jzgb` 发放审批状态置零。
`totalComparison` 会进一步计算教护龄津贴、提高工资，并替换当前已迁移项目后给出合计差额。
当前也会对比警衔/警务津贴和浮动工资，对应 `JXJT`、`FDGZ2`。
奖金结余 `JJJY2` 按旧系统主链口径处理：已有旧值时保留，旧值为 0 时再按 `cyxx.jjjy`、1993 年前职务和 `bz06_jjjy` 标准表试算。
岗位津贴 `GWJT2` 会按 `gwjtbz/gwjtlb` 查询 `bz_gwjt` 标准表进行对账。
套改/特岗保留 `TGBLBF` 按主链规则处理：机关人员清零，事业单位人员保留旧值。
`QTBT/SIDBT/ZWJT/ZFBT/JZMCBT` 已作为手工或暂不考虑字段列入 `excludedComponents`，只保留旧值。
`PGBC` 已作为特殊工资变动保留项列入 `pgbcComparison`，当前只读对账保留旧值。
批量对账接口会分页执行同一套只读计算，返回每个人的合计差额和差异汇总。
远程数据库对账建议先使用较小分页，例如 `size=5` 或 `size=10`，再逐步扩大范围。
对账结果中的 `componentDifferences` 会标明具体差异字段；`DFBT2` 会按人员性质显示为机关人员“生活性补贴”或事业单位人员“基础性绩效工资”。
