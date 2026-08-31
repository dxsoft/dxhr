#!/bin/bash
set -e
cp -a /opt/rsgzgl/app.jar "/opt/rsgzgl/app.jar.bak.$(date +%Y%m%d%H%M%S)"
cp -f /tmp/rsgzgl-app-new.jar /opt/rsgzgl/app.jar
chown rsgzgl:rsgzgl /opt/rsgzgl/app.jar
systemctl restart rsgzgl rsgzgl@demo rsgzgl@pq rsgzgl@xyzzb rsgzgl@xyrs
for port in 8080 18081 18082 18083 18084; do
  for i in $(seq 1 30); do
    if curl -sf -m 3 "http://127.0.0.1:${port}/actuator/health" 2>/dev/null | grep -q UP; then
      echo "port ${port} OK"
      break
    fi
    if [ "$i" = "30" ]; then
      echo "port ${port} FAILED"
    fi
    sleep 2
  done
done
systemctl is-active rsgzgl rsgzgl@demo rsgzgl@pq rsgzgl@xyzzb rsgzgl@xyrs
