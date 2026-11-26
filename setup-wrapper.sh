#!/bin/sh
# Generates the standard Gradle 9.5 wrapper on macOS/Linux.
set -eu

VERSION=9.5.0
WORK_DIR="$(mktemp -d)"
trap 'rm -rf "$WORK_DIR"' EXIT

curl -fsSL -o "$WORK_DIR/gradle.zip" "https://services.gradle.org/distributions/gradle-${VERSION}-bin.zip"
unzip -q "$WORK_DIR/gradle.zip" -d "$WORK_DIR"
"$WORK_DIR/gradle-${VERSION}/bin/gradle" --no-daemon wrapper --gradle-version "$VERSION"
