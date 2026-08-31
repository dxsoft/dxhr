#!/usr/bin/env bash
set -euo pipefail

DOMAIN=renshi.dxsoft.cn
UPSTREAM=127.0.0.1:8080
EMAIL=support@dxsoft.cn

echo "==> install nginx + certbot"
if command -v dnf >/dev/null 2>&1; then
  dnf install -y nginx certbot python3-certbot-nginx || yum install -y nginx certbot python3-certbot-nginx
elif command -v yum >/dev/null 2>&1; then
  yum install -y nginx certbot python3-certbot-nginx
elif command -v apt-get >/dev/null 2>&1; then
  apt-get update -y
  apt-get install -y nginx certbot python3-certbot-nginx
else
  echo "ERROR: no known package manager" >&2
  exit 1
fi

mkdir -p /etc/nginx/conf.d

# Initial HTTP-only config so certbot can authenticate
cat > /etc/nginx/conf.d/renshi.dxsoft.cn.conf <<EOF
upstream rsgzgl_renshi {
    server ${UPSTREAM};
    keepalive 16;
}

server {
    listen 80;
    server_name ${DOMAIN};

    client_max_body_size 512m;

    location / {
        proxy_pass http://rsgzgl_renshi;
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

# Open firewall if present
firewall-cmd --permanent --add-service=http 2>/dev/null || true
firewall-cmd --permanent --add-service=https 2>/dev/null || true
firewall-cmd --reload 2>/dev/null || true

systemctl enable --now nginx
nginx -t
systemctl reload nginx

echo "==> request certificate"
certbot --nginx -d "${DOMAIN}" --non-interactive --agree-tos -m "${EMAIL}" --redirect

# Ensure HTTPS proxy headers are present after certbot rewrite
if ! grep -q 'X-Forwarded-Proto' /etc/nginx/conf.d/renshi.dxsoft.cn.conf; then
  echo "WARN: certbot may have rewritten conf; verifying manually"
fi

# Rewrite to our known-good HTTPS config using live cert paths
cat > /etc/nginx/conf.d/renshi.dxsoft.cn.conf <<EOF
upstream rsgzgl_renshi {
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
    include /etc/letsencrypt/options-ssl-nginx.conf;
    ssl_dhparam /etc/letsencrypt/ssl-dhparams.pem;

    client_max_body_size 512m;

    location / {
        proxy_pass http://rsgzgl_renshi;
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

echo "==> enable Secure session cookies on primary app"
APP_ENV=/opt/rsgzgl/app.env
if grep -q '^RSGZGL_FORWARD_HEADERS_STRATEGY=' "$APP_ENV"; then
  sed -i 's/^RSGZGL_FORWARD_HEADERS_STRATEGY=.*/RSGZGL_FORWARD_HEADERS_STRATEGY=framework/' "$APP_ENV"
else
  echo 'RSGZGL_FORWARD_HEADERS_STRATEGY=framework' >> "$APP_ENV"
fi
if grep -q '^RSGZGL_SESSION_COOKIE_SECURE=' "$APP_ENV"; then
  sed -i 's/^RSGZGL_SESSION_COOKIE_SECURE=.*/RSGZGL_SESSION_COOKIE_SECURE=true/' "$APP_ENV"
else
  echo 'RSGZGL_SESSION_COOKIE_SECURE=true' >> "$APP_ENV"
fi
if ! grep -q '^RSGZGL_SESSION_COOKIE_SAME_SITE=' "$APP_ENV"; then
  echo 'RSGZGL_SESSION_COOKIE_SAME_SITE=lax' >> "$APP_ENV"
fi

systemctl restart rsgzgl
for i in $(seq 1 30); do
  if curl -sf -m 3 http://127.0.0.1:8080/actuator/health | grep -q UP; then
    echo primary_ok
    break
  fi
  sleep 2
done

echo "==> verify"
curl -sI -m 10 "http://${DOMAIN}/" | head -n 8 || true
curl -sI -m 10 "https://${DOMAIN}/" | head -n 12 || true
curl -sk -m 10 "https://${DOMAIN}/actuator/health" || true
echo
echo "DONE: https://${DOMAIN}/"
