#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")"
java_cmd=java
javac_cmd=javac
if [[ -n "${JAVA_HOME:-}" ]]; then
  java_cmd="$JAVA_HOME/bin/java"
  javac_cmd="$JAVA_HOME/bin/javac"
fi
mkdir -p build/classes
"$javac_cmd" --release 17 -encoding UTF-8 -d build/classes @sources.txt
"$java_cmd" -cp build/classes com.supermercado.Main
