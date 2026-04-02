#!/bin/bash
# Build DeviceLens debug APK and print its path
set -e
cd "$(dirname "$0")/android"
./gradlew assembleDebug -q
APK="app/build/outputs/apk/debug/app-debug.apk"
echo ""
echo "✓ Built: $(du -h "$APK" | cut -f1)  →  android/$APK"
