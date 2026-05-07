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

人员接口默认会对身份证号做脱敏处理，避免直接暴露完整证件号码。

`calculation-context` 接口用于工资计算迁移的第一步：读取单人最新工资历史、已保存的工资项金额和匹配到的标准表数据，暂不写入数据库。
该接口已经包含基础标准表计算值：职务工资、级别工资、薪级工资和技术等级工资，用于和 `hisbase` 存量金额对账。
同时包含部分津补贴计算值：基础绩效、工作性/生活性补贴、保留福补和年补贴，用于继续对齐 `gzjs06.prg` 的津补贴段。
`totalComparison` 会进一步计算教护龄津贴、提高工资，并替换当前已迁移项目后给出合计差额。
