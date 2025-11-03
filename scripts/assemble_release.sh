#!/bin/zsh
set -euo pipefail

# Builds all release APK variants (phone + tablet) using the aggregation task defined in build.gradle.kts.
SCRIPT_DIR="$(cd "$(dirname "${(%):-%x}")" && pwd)"
PROJECT_ROOT="${SCRIPT_DIR}/.."

cd "$PROJECT_ROOT"

./gradlew assembleReleaseApk
