# 人事系统托管式 SaaS（每客户独立实例）

本目录提供「一客户一套」开通材料。不支持一库多客户；真多租户不在本阶段范围。

## 架构

- 客户浏览器 → Nginx（HTTPS）→ 该客户独立 Spring Boot（独立端口或容器）
- 每客户独立 MySQL 库：`gzjsgl_<客户编码>`
- 授权与 UKey 绑定由运维台 `rsgzgl-ops` 签发后导入；生产关闭应用内签发

## 开通清单

1. 建库与账号，并导入 `gzjsgl.sql` 业务结构（见 `provision-tenant.sh`，可用 `SCHEMA_SQL` 指定路径）
2. 部署 jar + `app.env`（见 `app.env.saas.example`）
3. systemd 多实例（见 `rsgzgl@.service`）或 Docker（见 `docker-compose.tenant.example.yml`）
4. Nginx 按域名反代（见 `nginx-tenant.conf.example`）
5. 导入单位授权包；可选导入 UKey 绑定包
6. 备份该库；在运维台「监控」页添加该实例 `/actuator/health`

## 快速开通（systemd）

```bash
# 在已安装公共 jar 的服务器上（参考 deploy/linux/install.sh）
export TENANT=acme
export PORT=18081
export DB_PASSWORD='强密码'
export ADMIN_PASSWORD='管理员强密码'
export HMAC_SECRET='与签发授权包一致的密钥'

sudo bash provision-tenant.sh
# 编辑 /opt/rsgzgl/tenants/acme/app.env 后：
sudo systemctl enable --now rsgzgl@acme
curl -s http://127.0.0.1:18081/actuator/health
```

然后配置 Nginx：把 `acme.hr.example.com` 反代到 `127.0.0.1:18081`，申请证书。

> 注意：用 `http://IP:端口` 直接访问时，`RSGZGL_SESSION_COOKIE_SECURE` 必须为 `false`。若为 `true`，浏览器不保存会话 Cookie，表现为「登录即退出」。仅在 HTTPS 反代后再打开 Secure。

浏览器打开 `https://acme.hr.example.com`，用管理员登录 → **单位授权** 导入 `.rsauth`；需要 UKey 时到 **权限管理** 导入绑定包。

## UKey 说明（SaaS）

UKey 登录依赖客户电脑上的 SoftKey 本地服务，与是否云端托管无关。无锁客户可仅用密码登录（`RSGZGL_UKEY_ENABLED=true` 仍可保留，不插锁则走密码）。

## 本机隔离冒烟

在 Windows 开发机（已有 MySQL + 已打包 jar）执行：

```powershell
$env:RSGZGL_DB_PASSWORD = '你的MySQL密码'
cd e:\dxhr\deploy\saas
.\smoke-isolation.ps1
```

脚本会创建 `gzjsgl_saas_a` / `gzjsgl_saas_b`，在 18081/18082 各起一个实例，校验健康检查与授权状态相互独立，然后停止进程。

## 文件一览

| 文件 | 用途 |
|------|------|
| `app.env.saas.example` | 单租户生产环境变量模板 |
| `provision-tenant.sh` | Linux 上开通租户目录与库 |
| `rsgzgl@.service` | systemd 模板单元 |
| `nginx-tenant.conf.example` | HTTPS 反代示例 |
| `docker-compose.tenant.example.yml` | 一客户一容器示例 |
| `smoke-isolation.ps1` | 双实例隔离冒烟 |
| `tools/MysqlProbe.java` | 冒烟用 JDBC 探针 |
