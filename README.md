# Discord Bot
This is a simple Discord bot written in Kotlin that uses [JDA](https://jda.wiki/introduction/jda/).

## Setup & Deployment
### Prerequisites
* Ubuntu Server with:
  * Java 17 or higher
  * [Screen](https://www.gnu.org/software/screen/manual/screen.html#Invoking-Screen)

### Local setup
1. Clone the repository
2. Add a `config.properties` file in the root directory with the following content:
    ```
    bot.token=ENTER_YOUR_BOT_TOKEN
    ```
    You can get your bot token in the [Discord Developer Portal](https://discord.com/developers/applications).

### Deployment
To deploy the bot, you can run the deployment script [`deploy.sh`](deploy.sh).
This will build the project, copy the jar file to the server, and start the bot in a detached screen session.

## Functions
### Commands
* `/ding` - Responds with "Dong!" and only works in the channel "🤖｜bot-spielplatz"