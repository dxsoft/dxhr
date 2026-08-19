#!/usr/bin/env bash
set -euo pipefail
USER=$(grep '^USERNAME=' /opt/rsgzgl-ops/credentials.txt | cut -d= -f2-)
PASS=$(grep '^PASSWORD=' /opt/rsgzgl-ops/credentials.txt | cut -d= -f2-)
rm -f /tmp/ops-cj.txt
curl -s -c /tmp/ops-cj.txt -b /tmp/ops-cj.txt -X POST http://127.0.0.1:18090/login \
  --data-urlencode "username=${USER}" --data-urlencode "password=${PASS}" -o /dev/null
curl -s -c /tmp/ops-cj.txt -b /tmp/ops-cj.txt http://127.0.0.1:18090/api/monitor/overview -o /tmp/ov.json
python3 - <<'PY'
import json
d=json.load(open("/tmp/ov.json", encoding="utf-8"))
latest=d["latest"]
print("overall", latest.get("overall"), "cpu", round(latest.get("cpuPercent") or 0,1))
print("probes")
for p in latest.get("probes") or []:
    print(" ", p.get("name"), p.get("status"), p.get("httpStatus"), p.get("message"))
print("services")
for s in latest.get("services") or []:
    print(" ", s.get("name"), s.get("status"), s.get("active"))
print("certs")
for c in latest.get("certificates") or []:
    print(" ", c.get("host"), c.get("status"), c.get("daysLeft"))
print("runtimes")
for r in latest.get("runtimes") or []:
    print(" ", r.get("name"), r.get("status"), r.get("message"))
PY
