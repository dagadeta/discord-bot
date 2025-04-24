#!/bin/bash

# Build the project
if ! ./gradlew build; then
  echo "Build failed. Exiting."
  exit 1
fi

# Run the project in a new screen session
screen -dmS gradle_run ./gradlew run
echo "Application is running in a detached screen session named 'gradle_run'."