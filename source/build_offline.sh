#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")"
# Historical entry point. Requires a previously populated REAL Gradle cache.
# No compile-only interfaces are generated or accepted as release validation.
exec bash ../gradlew --offline clean build "$@"
