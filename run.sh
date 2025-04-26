#!/bin/bash

set -ex

echo "Starting the app..."
export JAVA_OPTS="-DLOG_DIR=../logs"
app/bin/app
