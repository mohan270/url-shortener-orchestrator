#!/usr/bin/env sh
set -eu

gradle wrapper --gradle-version 8.7
chmod +x gradlew
printf '\nGradle wrapper generated. Run: ./gradlew clean test\n'
