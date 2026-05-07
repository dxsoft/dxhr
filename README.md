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

人员接口默认会对身份证号做脱敏处理，避免直接暴露完整证件号码。
