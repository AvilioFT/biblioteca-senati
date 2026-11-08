#!/usr/bin/env bash
# ============================================================
#  Biblioteca SENATI · preparación de la base de datos
#  Crea db_biblioteca, las tablas y los datos de demostración.
#
#  Uso:
#    ./scripts/setup-db.sh                    # root sin contraseña, socket por defecto
#    DB_USER=root DB_PASS=clave ./scripts/setup-db.sh
#    DB_HOST=127.0.0.1 DB_PORT=3306 ./scripts/setup-db.sh
# ============================================================
set -euo pipefail

PROJECT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
DB_HOST="${DB_HOST:-localhost}"
DB_PORT="${DB_PORT:-3306}"
DB_USER="${DB_USER:-root}"
DB_PASS="${DB_PASS:-}"

MYSQL_ARGS=(-h "$DB_HOST" -P "$DB_PORT" -u "$DB_USER")
if [[ -n "$DB_PASS" ]]; then MYSQL_ARGS+=("-p$DB_PASS"); fi

echo "==> Creando esquema"
mysql "${MYSQL_ARGS[@]}" < "$PROJECT_DIR/db/schema.sql"

echo "==> Cargando datos de demostración"
mysql "${MYSQL_ARGS[@]}" < "$PROJECT_DIR/db/seed.sql"

echo "==> Listo. Tablas en db_biblioteca:"
mysql "${MYSQL_ARGS[@]}" -e "USE db_biblioteca; SHOW TABLES; SELECT COUNT(*) AS libros FROM libros; SELECT COUNT(*) AS usuarios FROM usuarios; SELECT COUNT(*) AS prestamos FROM prestamos;"
