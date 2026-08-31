# rsgzgl-ops

独立运维控制台：SoftKey SM2 制锁台账 + 单位授权签发。

## 启动

```bash
cd ops
mvn spring-boot:run
```

生产部署见 [deploy/ops](../deploy/ops/install.sh)：`/opt/rsgzgl-ops` + systemd `rsgzgl-ops`，端口 **18090**。

默认端口 **18090**，账号 `ops` / `ops123`（可用环境变量覆盖）。登录后右上角「修改密码」可改当前账号，只写库不改 `app.env`。

| 变量 | 说明 |
|------|------|
| `RSGZGL_OPS_ADMIN_USERNAME` | 运维登录名 |
| `RSGZGL_OPS_ADMIN_PASSWORD` | 运维密码（仅首次创建或 `RESET_PASSWORD=true` 时写入） |
| `RSGZGL_LICENSE_HMAC_SECRET` | 与人事系统相同的授权 HMAC 密钥 |
| `PORT` | 端口，默认 18090 |

H2 文件库：`./data/rsgzgl-ops`（相对 `ops` 工作目录）。

## 能力

1. **服务器监控**：本机 CPU / 内存 / 磁盘 + HTTP 健康探测（默认 `http://127.0.0.1:8080/actuator/health`），状态变化写入告警
2. **制锁**：浏览器 + SoftKey 本地服务写入 SM2 密钥对与身份；服务端只存芯片 ID / 公钥
3. **台账导出**：`ukey-bind-v1.json`，供人事系统权限管理「导入 UKey 绑定包」
4. **单位目录 + 签发**：下载 `.rsauth.json`，供人事系统「单位授权」导入

### 监控

登录后打开「监控」页。可添加各租户健康检查，例如 `http://127.0.0.1:18081/actuator/health`。运维台会顺带抓取同机 ` /internal/runtime`（仅 127.0.0.1，含堆 / 连接池 / Tomcat / 数据库）。默认每 30 秒采集一次，历史保留 72 小时。

| 变量 | 说明 |
|------|------|
| `RSGZGL_OPS_MONITOR_INTERVAL_MS` | 采集间隔，默认 30000 |
| `RSGZGL_OPS_MONITOR_HISTORY_HOURS` | 历史保留小时，默认 72 |
| `RSGZGL_OPS_MONITOR_DEFAULT_HEALTH_URL` | 空库时自动添加的探测地址 |
| `RSGZGL_OPS_MONITOR_WEBHOOK` | 状态变化时 POST JSON 的地址（仅 http/https） |
| `RSGZGL_OPS_MONITOR_CPU_WARN` / `_CRIT` | CPU 告警阈值（%） |
| `RSGZGL_OPS_MONITOR_MEMORY_WARN` / `_CRIT` | 内存告警阈值（%） |
| `RSGZGL_OPS_MONITOR_DISK_WARN` / `_CRIT` | 磁盘告警阈值（%） |
| `RSGZGL_OPS_MONITOR_SWAP_WARN` / `_CRIT` | Swap 告警阈值（%） |
| `RSGZGL_OPS_MONITOR_INODE_WARN` / `_CRIT` | inode 告警阈值（%） |
| `RSGZGL_OPS_MONITOR_UNITS` | 要检查的 systemd 单元，逗号分隔 |
| `RSGZGL_OPS_MONITOR_CERT_HOSTS` | 要检查证书到期的主机名 |
| `RSGZGL_OPS_MONITOR_CERT_WARN_DAYS` / `_CRIT_DAYS` | 证书剩余天数阈值 |
| `RSGZGL_OPS_MONITOR_HEAP_WARN` / `_CRIT` | 实例堆使用告警阈值（%） |

## 与人事系统配合

- 客户现场 `rsgzgl`：导入授权、导入绑定、UKey 登录
- 桌面/生产建议 `RSGZGL_LICENSE_ISSUE_ENABLED=false`，签发仅在本控制台进行
- **单位目录 + 本地政策同步**：人事「单位授权」页导出 `license-seed-v2.json`（来自 `dwbm` + `cyxx` 政策），在本控制台「单位目录」导入到独立 H2；签发页会附带 `localPolicy` 写入授权包
- 仍兼容旧版 `license-orgs-v1.json`（仅单位，不含政策；签发时目标租户 cyxx 政策为空）
