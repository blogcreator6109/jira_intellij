#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR=$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)
cd "$SCRIPT_DIR"

ensure_wrapper_jar() {
  local jar_path="gradle/wrapper/gradle-wrapper.jar"
  if [[ -f "$jar_path" ]]; then
    return
  fi

  mkdir -p gradle/wrapper

  echo "Gradle wrapper JAR not found. Downloading a compatible version..."

  if [[ ! -f gradle/wrapper/gradle-wrapper.properties ]]; then
    echo "gradle/wrapper/gradle-wrapper.properties is missing." >&2
    exit 1
  fi

  local distribution_url
  distribution_url=$(grep -E '^distributionUrl=' gradle/wrapper/gradle-wrapper.properties | cut -d'=' -f2-)
  distribution_url=${distribution_url//\\/}

  if [[ -z "$distribution_url" ]]; then
    echo "Unable to determine Gradle distribution URL." >&2
    exit 1
  fi

  local base_url="${distribution_url%.zip}"
  base_url="${base_url%-bin}"
  base_url="${base_url%-all}"
  local jar_url="${base_url}-wrapper.jar"

  download_with() {
    local target_url=$1
    local output=$2
    if command -v curl >/dev/null 2>&1; then
      if curl -fL "$target_url" -o "$output"; then
        return 0
      fi
    elif command -v wget >/dev/null 2>&1; then
      if wget -O "$output" "$target_url"; then
        return 0
      fi
    fi
    return 1
  }

  if download_with "$jar_url" "$jar_path"; then
    echo "Downloaded Gradle wrapper from $jar_url"
    return
  fi

  echo "Direct download failed, falling back to extracting from the Gradle distribution..."

  if ! command -v unzip >/dev/null 2>&1; then
    echo "The 'unzip' utility is required to extract gradle-wrapper.jar. Please install unzip and retry." >&2
    exit 1
  fi

  local tmp_zip
  tmp_zip=$(mktemp)

  if ! download_with "$distribution_url" "$tmp_zip"; then
    echo "Failed to download Gradle distribution from $distribution_url" >&2
    rm -f "$tmp_zip"
    exit 1
  fi

  unzip -j "$tmp_zip" 'gradle-*/lib/gradle-wrapper.jar' -d gradle/wrapper >/dev/null
  rm -f "$tmp_zip"

  if [[ ! -f "$jar_path" ]]; then
    echo "Unable to extract gradle-wrapper.jar from the downloaded distribution." >&2
    exit 1
  fi

  echo "Extracted Gradle wrapper JAR from $distribution_url"
}

ensure_wrapper_jar

if [[ -f ./gradlew ]]; then
  ./gradlew --no-daemon clean buildPlugin
else
  echo "Gradle wrapper script is missing. Please restore ./gradlew." >&2
  exit 1
fi

echo -e "\nPlugin build complete. Artifacts:"
ls -1 build/distributions
