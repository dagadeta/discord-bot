#!/bin/bash

set -ex

rm -f out.log err.log
echo "Starting the app..." | tee -a out.log | tee -a err.log
app/bin/app >> out.log 2>> err.log
