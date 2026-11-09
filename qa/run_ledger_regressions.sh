#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
TMP="$(mktemp -d)"; trap 'rm -rf "$TMP"' EXIT
javac -encoding UTF-8 -d "$TMP" \
  "$ROOT/source/src/main/java/dev/arandu/piratesmca/CrewLedger.java" \
  "$ROOT/qa/LedgerRegressions.java"
java -cp "$TMP" LedgerRegressions
