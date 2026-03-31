#!/usr/bin/env bash
# deploy.sh — Deploy NexGen Credit Risk Gateway to EC2
# Usage: ./deploy.sh <path-to-jar>
set -euo pipefail

JAR_SOURCE="${1:?Usage: $0 <path-to-jar>}"
DEPLOY_DIR="/opt/nexgen/creditrisk"
JAR_NAME="nexgen-creditrisk-gateway.jar"
SERVICE_NAME="nexgen-creditrisk"
HEALTH_URL="http://localhost:8080/nexgen/actuator/health"
HEALTH_RETRIES=30
HEALTH_INTERVAL=5
# Source env.conf to pick up BASIC_AUTH_USER / BASIC_AUTH_PASSWORD if the
# actuator health endpoint is protected. (Spring Security permitAll is
# recommended for /actuator/health; adjust CURL_AUTH below accordingly.)
ENV_CONF="${DEPLOY_DIR}/env.conf"
CURL_AUTH=()
if [ -r "${ENV_CONF}" ]; then
    # shellcheck source=/dev/null
    source "${ENV_CONF}"
    if [ -n "${BASIC_AUTH_USER:-}" ] && [ -n "${BASIC_AUTH_PASSWORD:-}" ]; then
        CURL_AUTH=(-u "${BASIC_AUTH_USER}:${BASIC_AUTH_PASSWORD}")
    fi
fi

# ── 1. Stop the service ────────────────────────────────────────────────────────
echo "[deploy] Stopping ${SERVICE_NAME} service..."
sudo systemctl stop "${SERVICE_NAME}" || true

# ── 2. Copy the new JAR ────────────────────────────────────────────────────────
echo "[deploy] Deploying JAR to ${DEPLOY_DIR}/${JAR_NAME}..."
sudo cp "${JAR_SOURCE}" "${DEPLOY_DIR}/${JAR_NAME}"
sudo chown nexgen:nexgen "${DEPLOY_DIR}/${JAR_NAME}"

# ── 3. Start the service ───────────────────────────────────────────────────────
echo "[deploy] Starting ${SERVICE_NAME} service..."
sudo systemctl start "${SERVICE_NAME}"

# ── 4. Wait for health check ───────────────────────────────────────────────────
echo "[deploy] Waiting for health check at ${HEALTH_URL}..."
for i in $(seq 1 "${HEALTH_RETRIES}"); do
    STATUS=$(curl -s -o /dev/null -w "%{http_code}" "${CURL_AUTH[@]}" "${HEALTH_URL}" || true)
    if [ "${STATUS}" = "200" ]; then
        echo "[deploy] Health check passed (attempt ${i})."
        exit 0
    fi
    echo "[deploy] Attempt ${i}/${HEALTH_RETRIES}: HTTP ${STATUS} — retrying in ${HEALTH_INTERVAL}s..."
    sleep "${HEALTH_INTERVAL}"
done

echo "[deploy] ERROR: Health check failed after $((HEALTH_RETRIES * HEALTH_INTERVAL)) seconds." >&2
sudo systemctl status "${SERVICE_NAME}" --no-pager || true
exit 1
