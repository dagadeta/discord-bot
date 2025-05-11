#!/bin/bash

set -e

printf "\nPlease enter your username on the target host: "
read -r TARGET_USER
printf "\nPlease enter the hostname or IP address of the target host: "
read -r TARGET_HOST
printf "\n"

set -x

VERSION=1.0.0
TARGET_DIR=/home/$TARGET_USER/discord-bot/bin

./gradlew app:bootDistZip
# shellcheck disable=SC2029
ssh "$TARGET_USER@$TARGET_HOST" "screen -XS bot_run quit" || true
# shellcheck disable=SC2029
ssh "$TARGET_USER@$TARGET_HOST" "rm -rf $TARGET_DIR && mkdir -p $TARGET_DIR"
scp app/build/distributions/app-boot-$VERSION.zip "$TARGET_USER@$TARGET_HOST:$TARGET_DIR"
scp run.sh "$TARGET_USER@$TARGET_HOST:$TARGET_DIR"
scp config.properties "$TARGET_USER@$TARGET_HOST:$TARGET_DIR"
# shellcheck disable=SC2029
ssh "$TARGET_USER@$TARGET_HOST" "cd $TARGET_DIR && unzip app-boot-$VERSION.zip && chmod +x run.sh"
# shellcheck disable=SC2029
ssh "$TARGET_USER@$TARGET_HOST" "cd $TARGET_DIR && screen -dmS bot_run ./run.sh"

set +x

echo "Application is running in a detached screen session named 'bot_run'."
