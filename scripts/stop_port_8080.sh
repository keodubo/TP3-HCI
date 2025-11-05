#!/usr/bin/env bash

set -euo pipefail

PORT=8080
FORCE=false

print_usage() {
    cat <<EOF
Usage: $(basename "$0") [--force]

Finds processes listening on TCP port ${PORT} and optionally terminates them.

Options:
  --force    Skip the confirmation prompt and terminate immediately.
EOF
}

while [[ $# -gt 0 ]]; do
    case "$1" in
        --force)
            FORCE=true
            shift
            ;;
        -h|--help)
            print_usage
            exit 0
            ;;
        *)
            echo "Unknown option: $1" >&2
            print_usage >&2
            exit 1
            ;;
    esac
done

if ! command -v lsof >/dev/null 2>&1; then
    echo "[stop_port_8080] Required tool 'lsof' not found. Install it (brew install lsof) and retry."
    exit 1
fi

LISTEN_ARGS=(-nP -iTCP:${PORT} -sTCP:LISTEN)
PID_OUTPUT=$(lsof "${LISTEN_ARGS[@]}" -t 2>/dev/null || true)
PIDS=$(echo "${PID_OUTPUT}" | tr '\n' ' ' | sed 's/^[[:space:]]*//;s/[[:space:]]*$//')

if [[ -z "${PIDS// }" ]]; then
    echo "[stop_port_8080] No process is listening on port ${PORT}."
    exit 0
fi

echo "[stop_port_8080] Processes currently listening on port ${PORT}:"
lsof "${LISTEN_ARGS[@]}"

if [[ "${FORCE}" != true ]]; then
    read -r -p "Terminate these processes? [y/N] " answer
    if [[ ! "${answer}" =~ ^[Yy]$ ]]; then
        echo "[stop_port_8080] Aborted. No processes were terminated."
        exit 0
    fi
fi

for pid in ${PIDS}; do
    if kill "${pid}" 2>/dev/null; then
        echo "[stop_port_8080] Sent SIGTERM to PID ${pid}."
    else
        echo "[stop_port_8080] Failed to terminate PID ${pid}. You may need to run this script with elevated permissions." >&2
    fi
done

sleep 1

if lsof "${LISTEN_ARGS[@]}" >/dev/null 2>&1; then
    echo "[stop_port_8080] Port ${PORT} is still in use. Use 'kill -9' manually if the process did not exit cleanly." >&2
    exit 1
fi

echo "[stop_port_8080] Port ${PORT} is now free."
