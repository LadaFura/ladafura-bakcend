#!/usr/bin/env bash
# Lance le backend sur la base de développement, migrations Flyway V1 à V5 comprises.
#
# Les paramètres sont passés en arguments de Spring, et non par DB_HOST ou FLYWAY_ENABLED :
# DotenvEnvironmentPostProcessor donne la priorité à .env sur les variables d'environnement.
# Ces clés-ci sont absentes de .env : la connexion reste donc sur la base locale.
set -euo pipefail

cd "$(dirname "$0")/../.."

URL='jdbc:mysql://127.0.0.1:3308/ladafura_dev?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC'

docker ps -q -f name='^ladafura-dev-mysql$' | grep -q . || {
  echo "La base de développement n'est pas démarrée : lancez d'abord ./scripts/dev/base-locale.sh" >&2
  exit 1
}

exec ./mvnw spring-boot:run -Dspring-boot.run.arguments="--spring.datasource.url=${URL} --spring.datasource.username=root --spring.datasource.password= --spring.flyway.enabled=true --spring.jpa.hibernate.ddl-auto=validate"
