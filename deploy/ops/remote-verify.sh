#!/usr/bin/env bash
set -euo pipefail
USER=$(grep '^USERNAME=' /opt/rsgzgl-ops/credentials.txt | cut -d= -f2-)
PASS=$(grep '^PASSWORD=' /opt/rsgzgl-ops/credentials.txt | cut -d= -f2-)
rm -f /tmp/ops-cj.txt
curl -s -c /tmp/ops-cj.txt -b /tmp/ops-cj.txt -X POST http://127.0.0.1:18090/login \
  -H 'Content-Type: application/x-www-form-urlencoded' \
  --data-urlencode "username=${USER}" \
  --data-urlencode "password=${PASS}" \
  -o /tmp/ops-login.html -w 'login:%{http_code}\n'
curl -s -c /tmp/ops-cj.txt -b /tmp/ops-cj.txt -X POST http://127.0.0.1:18090/api/monitor/collect >/dev/null
curl -s -c /tmp/ops-cj.txt -b /tmp/ops-cj.txt http://127.0.0.1:18090/api/monitor/overview -o /tmp/ops-overview.json
python3 - <<'PY'
import json
data=json.load(open("/tmp/ops-overview.json", encoding="utf-8"))
latest=data.get("latest") or {}
print("overall:", latest.get("overall"))
print("host:", latest.get("hostname"))
print("cpu:", round(latest.get("cpuPercent") or 0, 1))
print("swap:", latest.get("swapUsedBytes"), "/", latest.get("swapTotalBytes"))
print("disks:", len(latest.get("disks") or []))
for d in latest.get("disks") or []:
    print(" disk", d.get("name"), "used%", round((d.get("usedBytes") or 0)*100/max(d.get("totalBytes") or 1,1),1), "inode", d.get("usedInodes"), "/", d.get("totalInodes"))
print("services:")
for s in latest.get("services") or []:
    print(" -", s.get("name"), s.get("active"), s.get("status"), s.get("restarts"), s.get("message"))
print("certs:")
for c in latest.get("certificates") or []:
    print(" -", c.get("host"), c.get("status"), "days", c.get("daysLeft"), c.get("message"))
print("probes:")
for p in latest.get("probes") or []:
    print(" -", p.get("name"), p.get("httpStatus"), p.get("status"), p.get("message"))
PY
