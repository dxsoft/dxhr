#!/usr/bin/env bash
set -euo pipefail

DOMAIN=pq.dxsoft.cn
TENANT=pq
UPSTREAM=127.0.0.1:18082
EMAIL=support@dxsoft.cn
EXPECTED_IP=101.201.76.253
APP_ROOT=/opt/rsgzgl

echo "==> check public DNS for ${DOMAIN}"
RESOLVED=$(getent ahostsv4 "${DOMAIN}" 2>/dev/null | awk '{print $1; exit}' || true)
if [ -z "${RESOLVED}" ]; then
  RESOLVED=$(dig +short "${DOMAIN}" A 2>/dev/null | head -n1 || true)
fi
echo "resolved=${RESOLVED:-none}"
if [ "${RESOLVED}" != "${EXPECTED_IP}" ]; then
  echo "ERROR: ${DOMAIN} must A-record to ${EXPECTED_IP} (now: ${RESOLVED:-none})"
  echo "Fix DNS at registrar, wait TTL, then rerun this script."
  exit 2
fi

echo "==> write nginx HTTP bootstrap"
cat > /etc/nginx/conf.d/${DOMAIN}.conf <<EOF
upstream rsgzgl_pq {
    server ${UPSTREAM};
    keepalive 16;
}

server {
    listen 80;
    server_name ${DOMAIN};
    client_max_body_size 512m;
    location / {
        proxy_pass http://rsgzgl_pq;
        proxy_http_version 1.1;
        proxy_set_header Host \$host;
        proxy_set_header X-Real-IP \$remote_addr;
        proxy_set_header X-Forwarded-For \$proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto \$scheme;
        proxy_set_header X-Forwarded-Host \$host;
        proxy_set_header Connection "";
        proxy_read_timeout 3600s;
        proxy_send_timeout 3600s;
    }
}
EOF

nginx -t
systemctl reload nginx

echo "==> request certificate"
certbot --nginx -d "${DOMAIN}" --non-interactive --agree-tos -m "${EMAIL}" --redirect

SSL_OPTS=""
if [ -f /etc/letsencrypt/options-ssl-nginx.conf ]; then
  SSL_OPTS="include /etc/letsencrypt/options-ssl-nginx.conf;"
fi
SSL_DH=""
if [ -f /etc/letsencrypt/ssl-dhparams.pem ]; then
  SSL_DH="ssl_dhparam /etc/letsencrypt/ssl-dhparams.pem;"
fi

cat > /etc/nginx/conf.d/${DOMAIN}.conf <<EOF
upstream rsgzgl_pq {
    server ${UPSTREAM};
    keepalive 16;
}

server {
    listen 80;
    server_name ${DOMAIN};
    return 301 https://\$host\$request_uri;
}

server {
    listen 443 ssl http2;
    server_name ${DOMAIN};

    ssl_certificate     /etc/letsencrypt/live/${DOMAIN}/fullchain.pem;
    ssl_certificate_key /etc/letsencrypt/live/${DOMAIN}/privkey.pem;
    ${SSL_OPTS}
    ${SSL_DH}

    client_max_body_size 512m;

    location / {
        proxy_pass http://rsgzgl_pq;
        proxy_http_version 1.1;
        proxy_set_header Host \$host;
        proxy_set_header X-Real-IP \$remote_addr;
        proxy_set_header X-Forwarded-For \$proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto \$scheme;
        proxy_set_header X-Forwarded-Host \$host;
        proxy_set_header Connection "";
        proxy_read_timeout 3600s;
        proxy_send_timeout 3600s;
    }
}
EOF

nginx -t
systemctl reload nginx

echo "==> enable Secure cookies for pq tenant"
ENV=${APP_ROOT}/tenants/${TENANT}/app.env
sed -i 's/^RSGZGL_FORWARD_HEADERS_STRATEGY=.*/RSGZGL_FORWARD_HEADERS_STRATEGY=framework/' "$ENV" || true
grep -q '^RSGZGL_FORWARD_HEADERS_STRATEGY=' "$ENV" || echo 'RSGZGL_FORWARD_HEADERS_STRATEGY=framework' >> "$ENV"
sed -i 's/^RSGZGL_SESSION_COOKIE_SECURE=.*/RSGZGL_SESSION_COOKIE_SECURE=true/' "$ENV" || true
grep -q '^RSGZGL_SESSION_COOKIE_SECURE=' "$ENV" || echo 'RSGZGL_SESSION_COOKIE_SECURE=true' >> "$ENV"
grep -q '^RSGZGL_SESSION_COOKIE_SAME_SITE=' "$ENV" || echo 'RSGZGL_SESSION_COOKIE_SAME_SITE=lax' >> "$ENV"

systemctl restart "rsgzgl@${TENANT}"
for i in $(seq 1 30); do
  if curl -sf -m 3 "http://${UPSTREAM#http://}/actuator/health" | grep -q UP; then
    echo pq_ok
    break
  fi
  sleep 2
done

if [ -f "${APP_ROOT}/tenants/${TENANT}/credentials.txt" ]; then
  sed -i "s|^URL=.*|URL=https://${DOMAIN}|" "${APP_ROOT}/tenants/${TENANT}/credentials.txt" || true
fi

echo "==> verify"
curl -sI -m 10 "http://${DOMAIN}/" | head -n 6 || true
curl -skI -m 10 "https://${DOMAIN}/" | head -n 10 || true
curl -sk -m 10 "https://${DOMAIN}/actuator/health" || true
echo
echo "DONE: https://${DOMAIN}/  -> ${UPSTREAM}"
