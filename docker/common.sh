#!/usr/bin/env bash
SCRIPT_DIR=$(dirname $0)

set -euo pipefail

export POSTGRES_HOST="localhost"
export LOCAL="true"

function cleanup() {
    docker-compose -f "$PWD/$SCRIPT_DIR"/common.yml down
}
trap cleanup EXIT

docker-compose -f "$PWD/$SCRIPT_DIR"/common.yml up
