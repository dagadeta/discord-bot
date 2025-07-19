#!/bin/bash

DIR=$(dirname "$0")
VERSION=1.0.0

echo "Starting the app..."

set -ex

cd "$DIR"
export JAVA_OPTS="-DLOG_DIR=../logs"
export SPRING_PROFILES_ACTIVE=prod
app-boot-$VERSION/bin/app
