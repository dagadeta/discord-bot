# Discord Bot
This is a simple Discord bot written in Kotlin that uses [JDA](https://jda.wiki/introduction/jda/).


## Setup & Deployment

### Prerequisites
* Java 17 or higher on your local machine to build the project
* Debian/Ubuntu Server with:
    * SSH access
    * Java 17 or higher
    * [Screen](https://www.gnu.org/software/screen/manual/screen.html#Invoking-Screen)

### Local setup
1. Clone the repository
2. Add a `config.properties` file in the root directory with the following content:
    ```
    bot.token=ENTER_YOUR_BOT_TOKEN
    
    logging.guildId=ENTER_YOUR_LOGGING_GUILD_ID
    logging.channelId=ENTER_YOUR_LOGGING_CHANNEL_ID
    ```
    You can get your bot token in the [Discord Developer Portal](https://discord.com/developers/applications).
    
    `logging.guildId` and `logging.channelId` are the IDs of the Discord server and channel where you want to log messages. 
    You can get them by opening Discord in your browser and navigating to the channel you want to log messages to.
    Then, click into the address bar and copy the IDs out of the URL:
    ```
    https://discord.com/channels/[GUILD_ID]/[CHANNEL_ID]
    ```

### Deployment
To deploy the bot, you can run the deployment script [`deploy.sh`](deploy.sh).
This will build the project locally, copy the jar file to the server, and start the bot in a detached screen session.
When using this script, you'll get asked to enter the server's hostname/IP-address and username.

You could also build the project on the server and run it there by yourself,
but this script automates the process for you
and creates a good [directory structure](#directory-structure) and a logging configuration on the server.


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
  To detach from the screen session again without stopping it, press `CTRL+A` and then `D`.
* Read the log files in the `logs` directory which are named with a timestamp.
  Every new run will create a new log file.
  * `ls -l` in the `discord-bot/logs` directory will show you the names of the log files with their timestamps.
  * `cat [FILE NAME]` in the same directory will show you the content of the log file.

### Stopping the bot
To stop the bot, enter the following command on the server:
```bash
screen -XS bot_run quit
```


## Functions

### Commands
* `/ding` - Responds with "Dong!" and only works in the channel with the name "🤖｜bot-spielplatz"