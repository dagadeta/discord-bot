#!/bin/bash

# Install apt dependencies
sudo apt update || { echo "Failed to update package list"; exit 1; }
sudo apt install -y screen openjdk-21-jdk || { echo "Failed to install dependencies"; exit 1; }

# Clone the repository if it doesn't already exist
if [ -d "discord-bot" ]; then
  echo "Repository already exists. Skipping clone."
else
  git clone https://github.com/dagadeta/discord-bot.git || { echo "Failed to clone repository"; exit 1; }
fi

# Create the config.properties file if it doesn't already exist
if [ -f "discord-bot/config.properties" ]; then
  echo "config.properties file already exists. Skipping creation."
else
  echo "bot.token=YOUR_BOT_TOKEN_HERE" > discord-bot/config.properties || { echo "Failed to write to config.properties"; exit 1; }
  echo "config.properties file created with default content. Please edit it with your configuration."
fi

# Change to the project directory
cd discord-bot || { echo "Failed to change directory to discord-bot"; exit 1; }

# Make the run.sh script executable
sudo chmod +x run.sh || { echo "Failed to make run.sh executable"; exit 1; }