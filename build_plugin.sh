#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR=$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)
cd "$SCRIPT_DIR"

if [ -f ./gradlew ]; then
  ./gradlew clean buildPlugin
else
  gradle clean buildPlugin
fi

echo "\nPlugin build complete. Artifacts:" 
ls -1 build/distributions
