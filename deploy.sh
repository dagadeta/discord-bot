#!/bin/bash

set -ex

echo "Please enter your username on the target host: "
read -r TARGET_USER
echo "Please enter the hostname or IP address of the target host: "
read -r TARGET_HOST
TARGET_DIR=/home/$TARGET_USER/discord-bot

./gradlew distZip
# shellcheck disable=SC2029
ssh "$TARGET_USER@$TARGET_HOST" "rm -rf $TARGET_DIR && mkdir -p $TARGET_DIR"
scp app/build/distributions/app.zip "$TARGET_USER@$TARGET_HOST:$TARGET_DIR"
scp run.sh "$TARGET_USER@$TARGET_HOST:$TARGET_DIR"
scp config.properties "$TARGET_USER@$TARGET_HOST:$TARGET_DIR"
# shellcheck disable=SC2029
ssh "$TARGET_USER@$TARGET_HOST" "cd $TARGET_DIR && unzip app.zip && chmod +x run.sh"
# shellcheck disable=SC2029
ssh "$TARGET_USER@$TARGET_HOST" "cd $TARGET_DIR && screen -dmS bot_run ./run.sh"
echo "Application is running in a detached screen session named 'bot_run'."
