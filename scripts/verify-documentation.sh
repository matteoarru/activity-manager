#!/usr/bin/env bash
set -euo pipefail

required=(
  docs/PRODUCT_BRIEF.md docs/REQUIREMENTS.md docs/ARCHITECTURE.md docs/DATA_MODEL.md
  docs/TEST_PLAN.md docs/TRACEABILITY.md docs/DEPLOYMENT.md docs/ENGINEERING_PRACTICES.md
  docs/SECURITY.md docs/BACKLOG.md CHANGELOG.md AGENTS.md
)

for file in "${required[@]}"; do
  [[ -f "$file" ]] || { echo "Missing required documentation: $file" >&2; exit 1; }
done

requirements=$(grep -Eo '\b(FR|NFR|SEC|DATA)-[0-9]{3}\b' docs/REQUIREMENTS.md | sort -u || true)
traceability=$(grep -Eo '\b(FR|NFR|SEC|DATA)-[0-9]{3}\b' docs/TRACEABILITY.md | sort -u || true)

while IFS= read -r requirement; do
  [[ -z "$requirement" ]] && continue
  grep -qx "$requirement" <<<"$traceability" || {
    echo "Requirement $requirement has no traceability entry" >&2
    exit 1
  }
done <<<"$requirements"

echo "Documentation contract passed."
