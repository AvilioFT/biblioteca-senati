#!/usr/bin/env bash
# ============================================================
#  Biblioteca SENATI · entorno de desarrollo local
#  Descarga Tomcat 10.1, compila el WAR y lo despliega.
#
#  Uso:
#    ./scripts/dev.sh          # compila, despliega y arranca Tomcat
#    ./scripts/dev.sh --no-build
# ============================================================
set -euo pipefail

PROJECT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
TOMCAT_VERSION="${TOMCAT_VERSION:-10.1.34}"
TOOLS_DIR="$PROJECT_DIR/.tools"
TOMCAT_DIR="$TOOLS_DIR/apache-tomcat-$TOMCAT_VERSION"
WAR="$PROJECT_DIR/target/biblioteca.war"
PORT="${PORT:-8080}"

if [[ "${1:-}" != "--no-build" ]]; then
    echo "==> Compilando el proyecto"
    (cd "$PROJECT_DIR" && mvn -B clean package)
fi

if [[ ! -f "$WAR" ]]; then
    echo "ERROR: no existe $WAR. Ejecuta sin --no-build." >&2
    exit 1
fi

if [[ ! -x "$TOMCAT_DIR/bin/catalina.sh" ]]; then
    mkdir -p "$TOOLS_DIR"
    ARCHIVE="$TOOLS_DIR/apache-tomcat-$TOMCAT_VERSION.tar.gz"
    if [[ ! -f "$ARCHIVE" ]]; then
        echo "==> Descargando Tomcat $TOMCAT_VERSION"
        curl -fL --retry 3 \
            "https://archive.apache.org/dist/tomcat/tomcat-10/v$TOMCAT_VERSION/bin/apache-tomcat-$TOMCAT_VERSION.tar.gz" \
            -o "$ARCHIVE"
    fi
    echo "==> Extrayendo Tomcat"
    tar -xzf "$ARCHIVE" -C "$TOOLS_DIR"
fi

echo "==> Desplegando biblioteca.war"
rm -rf "$TOMCAT_DIR/webapps/biblioteca" "$TOMCAT_DIR/webapps/biblioteca.war"
cp "$WAR" "$TOMCAT_DIR/webapps/biblioteca.war"

export CATALINA_HOME="$TOMCAT_DIR"
export CATALINA_BASE="$TOMCAT_DIR"
export CATALINA_PID="$TOOLS_DIR/tomcat.pid"

# Puerto HTTP configurable sin editar server.xml
if [[ "$PORT" != "8080" ]]; then
    sed -i "s/port=\"8080\"/port=\"$PORT\"/g" "$TOMCAT_DIR/conf/server.xml"
fi

echo "==> Iniciando Tomcat en http://localhost:$PORT/biblioteca/"
echo "    Logs: $TOMCAT_DIR/logs/catalina.out"
exec "$TOMCAT_DIR/bin/catalina.sh" run
