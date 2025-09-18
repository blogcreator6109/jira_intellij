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

  echo "Gradle wrapper JAR not found. Hydrating it from the configured Gradle distribution..."

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

  if ! command -v curl >/dev/null 2>&1 && ! command -v wget >/dev/null 2>&1; then
    echo "Either curl or wget is required to download the Gradle wrapper." >&2
    exit 1
  fi

  if ! command -v unzip >/dev/null 2>&1; then
    echo "The 'unzip' utility is required to extract gradle-wrapper.jar. Please install unzip and retry." >&2
    exit 1
  fi

  local tmp_zip
  if tmp_zip=$(mktemp "${TMPDIR:-/tmp}/gradle-wrapper.XXXXXX" 2>/dev/null); then
    :
  else
    tmp_zip=$(mktemp 2>/dev/null)
  fi

  if [[ -z "${tmp_zip:-}" ]]; then
    echo "Failed to create a temporary file for downloading the Gradle distribution." >&2
    exit 1
  fi

  cleanup() {
    [[ -n "${tmp_zip:-}" && -f "$tmp_zip" ]] && rm -f "$tmp_zip"
  }
  trap cleanup EXIT

  echo "Downloading Gradle distribution from $distribution_url"
  if ! download_with "$distribution_url" "$tmp_zip"; then
    echo "Failed to download Gradle distribution from $distribution_url" >&2
    exit 1
  fi

  verify_checksum "$tmp_zip"

  unzip -j "$tmp_zip" 'gradle-*/lib/gradle-wrapper.jar' -d gradle/wrapper >/dev/null

  if [[ ! -f "$jar_path" ]]; then
    echo "Unable to extract gradle-wrapper.jar from the downloaded distribution." >&2
    exit 1
  fi

  echo "Extracted Gradle wrapper JAR from $distribution_url"

  trap - EXIT
  cleanup
}

download_with() {
  local target_url=$1
  local output=$2
  if command -v curl >/dev/null 2>&1; then
    if curl -fL --retry 3 --continue-at - "$target_url" -o "$output"; then
      return 0
    fi
  fi
  if command -v wget >/dev/null 2>&1; then
    if wget --tries=3 --continue --output-document="$output" "$target_url"; then
      return 0
    fi
  fi
  return 1
}

verify_checksum() {
  local archive=$1
  local checksum
  checksum=$(grep -E '^distributionSha256Sum=' gradle/wrapper/gradle-wrapper.properties | cut -d'=' -f2-)
  checksum=${checksum//\\/}

  if [[ -z "$checksum" ]]; then
    return
  fi

  if command -v sha256sum >/dev/null 2>&1; then
    if ! echo "$checksum  $archive" | sha256sum --check --status; then
      echo "Gradle distribution checksum verification failed." >&2
      exit 1
    fi
  elif command -v shasum >/dev/null 2>&1; then
    if ! echo "$checksum  $archive" | shasum -a 256 --check --status; then
      echo "Gradle distribution checksum verification failed." >&2
      exit 1
    fi
  else
    echo "Warning: Unable to verify distribution checksum (sha256sum/shasum missing)." >&2
  fi
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
