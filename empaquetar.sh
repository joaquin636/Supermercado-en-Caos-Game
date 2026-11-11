#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")"
javac_cmd=javac
jar_cmd=jar
if [[ -n "${JAVA_HOME:-}" ]]; then
  javac_cmd="$JAVA_HOME/bin/javac"
  jar_cmd="$JAVA_HOME/bin/jar"
fi
mkdir -p build dist
paquete_dir=$(mktemp -d build/paquete.XXXXXX)
"$javac_cmd" --release 17 -encoding UTF-8 -d "$paquete_dir" @sources.txt
"$jar_cmd" --create --file dist/SupermercadoEnCaos.jar --main-class com.supermercado.Main -C "$paquete_dir" .
echo "JAR generado desde las fuentes de produccion."
