#!/bin/bash

set -ex

VERSION=1.0.0

echo "Starting the app..."
export JAVA_OPTS="-DLOG_DIR=../logs"
app-boot-$VERSION/bin/app
