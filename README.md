# Discord Bot
This is a simple Discord bot written in Kotlin that uses JDA.

## Setup
There is a setup script that will set up the project. Run the following command:
```
curl -O https://github.com/dagadeta/discord-bot/setup.sh && chmod +x setup.sh && ./setup.sh
```
After you run the setup script, you should be in the `discord-bot` directory. You will need to set some properties in the `config.properties` file. To do this, open the file via `nano config.properties` and set the following properties:
* `bot.token`: The API-token for your bot. You can get this on the [Discord Developer Portal](https://discord.com/developers/applications).

After setting up the properties, press `CTRL+X`, then `Y` and `ENTER` to save the file.

## Running the bot
To run the bot, you can use the run script. Run the following command:
```
./run.sh
```