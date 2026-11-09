#!/usr/bin/env bash
# Charge les données de démonstration dans la base de développement, et elle seule.
#   ./jeu-essai.sh <email-gerant> [--vendeur email] [--comptable email] [--stock email] [--achats email]
# Le gérant doit avoir un compte Firebase portant cet email. Les autres sont facultatifs :
# sans email, le membre existe avec une adresse fictive et ne peut pas se connecter.
set -euo pipefail

CONTENEUR=ladafura-dev-mysql
BASE=ladafura_dev
DOSSIER="$(cd "$(dirname "$0")" && pwd)"

usage() {
  echo "Usage : $0 <email-gerant> [--vendeur email] [--comptable email] [--stock email] [--achats email]" >&2
  exit 1
}

verifier_email() {
  [[ "$1" =~ ^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$ ]] || { echo "Email invalide : $1" >&2; exit 1; }
}

[[ $# -ge 1 ]] || usage
GERANT="$1"; shift
VENDEUR=vendeur@demo.ladafura.local
COMPTABLE=comptable@demo.ladafura.local
STOCK=stock@demo.ladafura.local
ACHATS=achats@demo.ladafura.local

while [[ $# -gt 0 ]]; do
  [[ $# -ge 2 ]] || usage
  case "$1" in
    --vendeur) VENDEUR="$2" ;;
    --comptable) COMPTABLE="$2" ;;
    --stock) STOCK="$2" ;;
    --achats) ACHATS="$2" ;;
    *) usage ;;
  esac
  shift 2
done

for email in "$GERANT" "$VENDEUR" "$COMPTABLE" "$STOCK" "$ACHATS"; do
  verifier_email "$email"
done

docker ps -q -f name="^${CONTENEUR}$" | grep -q . || {
  echo "La base de développement n'est pas démarrée : lancez d'abord ./base-locale.sh" >&2
  exit 1
}

sql() { docker exec -i "$CONTENEUR" mysql -uroot --default-character-set=utf8mb4 "$@"; }

if ! sql -N "$BASE" -e "SELECT 1 FROM offres_abonnement LIMIT 1" >/dev/null 2>&1; then
  echo "Les migrations ne sont pas appliquées : lancez d'abord ./backend-local.sh" >&2
  exit 1
fi

DEJA=$(sql -N "$BASE" -e "SELECT COUNT(*) FROM pharmacopees WHERE nom = 'Officine de démonstration'")
if [[ "$DEJA" != "0" ]]; then
  echo "Le jeu d'essai est déjà chargé. Pour repartir de zéro : ./base-locale.sh --reinitialiser" >&2
  exit 1
fi

{
  echo "SET NAMES utf8mb4 COLLATE utf8mb4_unicode_ci;"
  echo "SET @email_gerant='${GERANT}', @email_vendeur='${VENDEUR}', @email_comptable='${COMPTABLE}', @email_stock='${STOCK}', @email_achats='${ACHATS}';"
  cat "${DOSSIER}/jeu-essai-saas.sql"
} | sql "$BASE"

echo "Jeu d'essai chargé. Gérant : ${GERANT}"
