#!/usr/bin/env bash
set -euo pipefail

DOMAIN=xyrs.dxsoft.cn
UPSTREAM=127.0.0.1:8080
EMAIL=support@dxsoft.cn

SSL_OPTS=""
if [[ -f /etc/letsencrypt/options-ssl-nginx.conf ]]; then
  SSL_OPTS="include /etc/letsencrypt/options-ssl-nginx.conf;"
fi
SSL_DH=""
if [[ -f /etc/letsencrypt/ssl-dhparams.pem ]]; then
  SSL_DH="ssl_dhparam /etc/letsencrypt/ssl-dhparams.pem;"
fi

cat > "/etc/nginx/conf.d/${DOMAIN}.conf" <<EOF
upstream rsgzgl_xyrs {
    server ${UPSTREAM};
    keepalive 16;
}

server {
    listen 80;
    server_name ${DOMAIN};
    client_max_body_size 512m;
    location / {
        proxy_pass http://rsgzgl_xyrs;
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

if [[ ! -f "/etc/letsencrypt/live/${DOMAIN}/fullchain.pem" ]]; then
  certbot --nginx -d "${DOMAIN}" --non-interactive --agree-tos -m "${EMAIL}" --redirect
fi

if [[ -f "/etc/letsencrypt/live/${DOMAIN}/fullchain.pem" ]]; then
  cat > "/etc/nginx/conf.d/${DOMAIN}.conf" <<EOF
upstream rsgzgl_xyrs {
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
        proxy_pass http://rsgzgl_xyrs;
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
fi

curl -sk "https://${DOMAIN}/actuator/health" || true
echo
echo "DONE: https://${DOMAIN}/ -> ${UPSTREAM}"
