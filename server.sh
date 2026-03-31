#!/usr/bin/env bash
set -euo pipefail

# ─── DeviceLens Backend Server ─────────────────────────────────────
# Usage:
#   ./server.sh          Start the server (dev mode with hot reload)
#   ./server.sh start    Same as above
#   ./server.sh prod     Start in production mode (no hot reload)
#   ./server.sh setup    Run migrations + seed (first time only)
#   ./server.sh migrate  Run database migrations only
#   ./server.sh seed     Seed the database only
#   ./server.sh health   Check if the server is running
#   ./server.sh stop     Stop any running instance

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR/backend"

PORT="${PORT:-3000}"
PID_FILE="/tmp/devicelens-backend.pid"

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
CYAN='\033[0;36m'
NC='\033[0m'

log() { echo -e "${CYAN}[DeviceLens]${NC} $1"; }
ok()  { echo -e "${GREEN}[✓]${NC} $1"; }
err() { echo -e "${RED}[✗]${NC} $1"; }
warn(){ echo -e "${YELLOW}[!]${NC} $1"; }

check_bun() {
    if ! command -v bun &>/dev/null; then
        err "Bun is not installed. Install it: curl -fsSL https://bun.sh/install | bash"
        exit 1
    fi
}

check_env() {
    if [ ! -f .env ]; then
        err ".env file not found. Copy .env.example and set DATABASE_URL."
        echo "  cp .env.example .env"
        exit 1
    fi
    if ! grep -q "DATABASE_URL" .env 2>/dev/null; then
        err "DATABASE_URL not set in .env"
        exit 1
    fi
    # Check it's not the placeholder
    if grep -q "ep-xxx" .env 2>/dev/null; then
        err "DATABASE_URL still has placeholder value. Set your actual Neon connection string."
        exit 1
    fi
}

check_deps() {
    if [ ! -d node_modules ]; then
        log "Installing dependencies..."
        bun install
        ok "Dependencies installed"
    fi
}

stop_server() {
    if [ -f "$PID_FILE" ]; then
        local pid
        pid=$(cat "$PID_FILE")
        if kill -0 "$pid" 2>/dev/null; then
            kill "$pid"
            rm -f "$PID_FILE"
            ok "Server stopped (PID $pid)"
            return 0
        else
            rm -f "$PID_FILE"
        fi
    fi
    # Also kill by port
    local pid_on_port
    pid_on_port=$(lsof -ti ":$PORT" 2>/dev/null || true)
    if [ -n "$pid_on_port" ]; then
        kill $pid_on_port 2>/dev/null || true
        ok "Killed process on port $PORT"
    fi
}

start_dev() {
    check_bun
    check_env
    check_deps
    stop_server

    log "Starting DeviceLens Backend (dev mode, port $PORT)..."
    bun --watch src/index.ts &
    echo $! > "$PID_FILE"
    ok "Server started (PID $(cat $PID_FILE))"
    wait
}

start_prod() {
    check_bun
    check_env
    check_deps
    stop_server

    log "Starting DeviceLens Backend (production, port $PORT)..."
    NODE_ENV=production bun src/index.ts &
    echo $! > "$PID_FILE"
    ok "Server started (PID $(cat $PID_FILE))"
    wait
}

run_setup() {
    check_bun
    check_env
    check_deps

    log "Running database migrations..."
    bun src/db/migrate.ts
    ok "Migrations complete"

    log "Seeding database..."
    bun src/db/seed.ts
    ok "Seed complete"
}

run_migrate() {
    check_bun; check_env; check_deps
    log "Running database migrations..."
    bun src/db/migrate.ts
}

run_seed() {
    check_bun; check_env; check_deps
    log "Seeding database..."
    bun src/db/seed.ts
}

health_check() {
    local response
    response=$(curl -s "http://localhost:$PORT/api/v1/health" 2>/dev/null || echo "FAIL")
    if echo "$response" | grep -q '"healthy"'; then
        ok "Server is healthy"
        echo "$response" | python3 -m json.tool 2>/dev/null || echo "$response"
    else
        err "Server is not responding on port $PORT"
        exit 1
    fi
}

# ─── Main ──────────────────────────────────────────────────────────

case "${1:-start}" in
    start|dev)   start_dev ;;
    prod)        start_prod ;;
    setup)       run_setup ;;
    migrate)     run_migrate ;;
    seed)        run_seed ;;
    health)      health_check ;;
    stop)        stop_server ;;
    *)
        echo "Usage: $0 {start|dev|prod|setup|migrate|seed|health|stop}"
        exit 1
        ;;
esac
