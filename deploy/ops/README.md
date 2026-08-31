# 部署 rsgzgl-ops（监控 + 制锁签发）

## 本机打包

```bash
cd ops
mvn -DskipTests package
```

把 `ops/target/rsgzgl-ops-0.1.0-SNAPSHOT.jar` 改名为 `app.jar`，与本目录的 `install.sh`、`rsgzgl-ops.service`、`app.env.example` 一起上传。

## 服务器安装

```bash
cd /tmp/rsgzgl-ops-install
sudo bash install.sh
```

安装到 `/opt/rsgzgl-ops`，systemd 单元 `rsgzgl-ops`，端口 **18090**。首次安装会生成 `/opt/rsgzgl-ops/credentials.txt`。

```bash
sudo systemctl status rsgzgl-ops
curl -s http://127.0.0.1:18090/actuator/health
```

云厂商安全组需放行 TCP 18090（若要从公网打开控制台）。
