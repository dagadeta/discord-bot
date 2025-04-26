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

## On the server

### Directory structure
When you used the deployment script earlier, the directory structure on the server should look like this:
```
.
└── discord-bot
    ├── bin
    │   └── [ALL APP AND LIBRARY FILES]
    └── logs
        └── [LOG FILES WITH TIMECODE]
```

### Reading logs
To read the logs, you have two options:
* Attach to the screen session and see the logs of the current run in real-time:
    ```bash
    screen -DR "bot_run"
    ```
* Read the log files in the `logs` directory which are named with a timestamp.
  Every new run will create a new log file.
  * `ls -l` in the `discord-bot/logs` directory will show you the names of the log files with their timestamps.
  * `cat [FILE NAME]` in the same directory will show you the content of the log file.

## Functions
### Commands
* `/ding` - Responds with "Dong!" and only works in the channel "🤖｜bot-spielplatz"