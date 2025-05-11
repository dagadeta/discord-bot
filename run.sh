#!/bin/bash

set -ex

VERSION=0.0.1-SNAPSHOT

echo "Starting the app..."
export JAVA_OPTS="-DLOG_DIR=../logs"
app-boot-$VERSION/bin/app
