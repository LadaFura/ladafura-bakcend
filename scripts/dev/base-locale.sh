#!/usr/bin/env bash
# Démarre la base MySQL de développement : conteneur ladafura-dev-mysql, port 3308.
# Indépendante de la base docker-compose (3306) et du conteneur de test (3307).
#   ./base-locale.sh                  démarre la base (la crée si besoin)
#   ./base-locale.sh --reinitialiser  supprime ce conteneur et ses données, puis la recrée
set -euo pipefail

CONTENEUR=ladafura-dev-mysql
VOLUME=ladafura_dev_data
BASE=ladafura_dev
PORT=3308

if [[ "${1:-}" == "--reinitialiser" ]]; then
  docker rm -f "$CONTENEUR" >/dev/null 2>&1 || true
  docker volume rm "$VOLUME" >/dev/null 2>&1 || true
  echo "Base de développement supprimée."
fi

if [[ "$(docker ps -q -f name="^${CONTENEUR}$")" == "" ]]; then
  if [[ "$(docker ps -aq -f name="^${CONTENEUR}$")" != "" ]]; then
    docker start "$CONTENEUR" >/dev/null
  else
    docker run -d --name "$CONTENEUR" -p "127.0.0.1:${PORT}:3306" -v "${VOLUME}:/var/lib/mysql" \
      -e MYSQL_ALLOW_EMPTY_PASSWORD=yes -e MYSQL_DATABASE="$BASE" \
      mysql:8.0 --character-set-server=utf8mb4 --collation-server=utf8mb4_unicode_ci >/dev/null
  fi
fi

for _ in $(seq 1 60); do
  if docker exec "$CONTENEUR" mysql -uroot -h127.0.0.1 -e "USE ${BASE}" >/dev/null 2>&1; then
    echo "Base prête : 127.0.0.1:${PORT}/${BASE}"
    exit 0
  fi
  sleep 2
done
echo "La base n'a pas démarré à temps." >&2
exit 1
