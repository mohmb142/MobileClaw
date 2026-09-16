#!/usr/bin/env bash
set -euo pipefail

chmod +x ./gradlew
./gradlew clean assembleDebug testDebugUnitTest

APK="app/build/outputs/apk/debug/app-debug.apk"
if [ ! -f "$APK" ]; then
  echo "ERROR: APK was not produced: $APK" >&2
  exit 1
fi

echo "Astra APK built successfully: $APK"
ls -lh "$APK"
