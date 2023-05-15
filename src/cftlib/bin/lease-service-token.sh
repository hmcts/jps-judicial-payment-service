#!/usr/bin/env bash

set -eu

microservice=${1:-jps_webapp}

curl --insecure --fail --show-error --silent -X POST \
  ${SERVICE_AUTH_PROVIDER_API_BASE_URL:-http://localhost:8489}/testing-support/lease \
  -H "Content-Type: application/json" \
  -d '{
    "microservice": "'${microservice}'"
  }' \
  -w "\n"
