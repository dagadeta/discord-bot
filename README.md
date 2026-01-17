# Discord Bot
This is a simple Discord bot written in Kotlin that uses [JDA](https://jda.wiki/introduction/jda/).

## Features
### WordChain game
The WordChain game is a game where players have to find a word that begins with the letter of the previously written word.

**Rules**:
* The game starts with any word.
* A word cannot be written multiple times.
* Configurable: Only words that exist in the dictionary can be used.
* The game never ends but can be paused and restarted. If any rule gets violated, the bot will simply delete the word,
  tell the user why, and continue the game.

**Commands**:

| Command                                        | Description                                                                                   | Parameters          | Requirements                         |
|:-----------------------------------------------|-----------------------------------------------------------------------------------------------|---------------------|--------------------------------------|
| `/config word-chain-game channel-id`           | Sets the WordChain game's channel ID                                                          | `id` - String       | An administrator in an admin-channel |
| `/config word-chain-game check-word-existence` | Configure if the WordChain game should check if words exist in the dictionary (default: true) | `check` - Boolean   | An administrator in an admin-channel |
| `/config word-chain-game language`             | Sets the WordChain game's language (default: en)                                              | `language` - String | An administrator in an admin-channel |
| `/start-word-chain-game`                       | Starts the WordChain game                                                                     | None                | None                                 |
| `/restart-word-chain-game`                     | Restarts the WordChain game                                                                   | None                | None                                 |
| `/pause-word-chain-game`                       | Pauses the WordChain game (Memory will remain)                                                | None                | None                                 |
| `/stop-word-chain-game`                        | Stops the WordChain game (Memory will be cleared)                                             | None                | None                                 |


### DingDong
The `/ding`-command simply responds with "Dong!"

**Commands**:

| Command | Description  | Parameters | Requirements                     |
|:--------|--------------|------------|----------------------------------|
| `/ding` | Answers Dong | None       | Any user in a bottalking-channel |


### Configuration
The bot can be configured using the following commands:

| Command                         | Description                            | Parameters    | Requirements                         |
|:--------------------------------|----------------------------------------|---------------|--------------------------------------|
| `/config bottalking channel-id` | Sets the general bottalking channel ID | `id` - String | An administrator in an admin-channel |

There is also some configuration done in a config file.
See the [Setup & Deployment](#setup--deployment) section of this README for more information.


## Setup & Deployment
> [!NOTE]
> Any uses of a terminal are intended for a Unix-based shell. This includes Linux and macOS. If you use Windows,
> you can either use [WSL](https://learn.microsoft.com/de-de/windows/wsl/install) or the [Git Bash](https://git-scm.com/install/windows).

### Prerequisites
* A Discord application
  * Create your Discord application [here](https://discord.com/developers/applications)
  * You will need the auth token for the app
  * Once your application is set up, you can open the following invite link in your browser.
    It includes all the permissions the bot needs. You need to replace the client ID.
    ```
    https://discord.com/oauth2/authorize?client_id=[ENTER_YOUR_ID]&permissions=8&integration_type=0&scope=bot
    ```
* Local machine with:
  * Java 17 or higher
  * OCI-runtime (e.g. Docker)
  * docker-compose (v2 – depending on the distribution)
* Debian/Ubuntu Server with:
  * docker-compose
  * SSH access
  * Java 17 or higher
  * [Screen](https://www.gnu.org/software/screen/manual/screen.html#Invoking-Screen)

### Local setup
1. Clone the repository
2. In the `config` directory, add an `application-prod.yml` file. 
   Find a template with explanations to copy in [config/application-prod.yml.template](config/application-prod.yml.template).

#### When developing
You can also create a `application-dev.yml` file in the `config` directory.
This creates a second profile which you can use while developing and debugging.
It enables you to configure different Discord servers and channels for this purpose while still remaining the prod config.
This profile is also the default profile for local development.

### Deployment
To deploy the bot, you first need to copy the [`docker-compose.yml`](docker-compose.yml) file to the server and run `docker-compose up -d`.

After that, you can run the deployment script [`deploy.sh`](deploy.sh).
This will build the project locally, copy the jar file to the server, and start the bot in a detached screen session
**using the prod profile**.

To run the script, you can pass your username and the host name like this: `./deploy.sh -u USERNAME -d HOSTNAME`
When you just execute the script without passing these arguments, the script will ask you for them.

You could also build the project on the server and run it there by yourself,
but this script automates the process for you
and creates a good [directory structure](#directory-structure) and a logging configuration on the server.

#### Final Configuration
After the bot has been deployed, you have to configure the bot in the Discord server.
To do so, you can use `/config` and its sub-commands.

### Run the tests

#### Unit Tests
The deployment script already runs all unit tests before packaging the application.

You can run them separately with the Gradle task `test`:

```shell
./gradlew app:test
```

Find the test results in [the test report](app/build/reports/tests/test/index.html) afterwards.

#### Integration Tests
Additionally to the unit tests, there are some integration tests that connect to a local database (in a docker container
using [Zonky Embedded Database](https://github.com/zonkyio/embedded-database-spring-test)) and to the Wiktionary API.
The deployment script already runs all integration tests before packaging the application.

You can run them seperatly with the Gradle task `integTest`:

```shell
./gradlew app:integTest
```

Find the test results in [the test report](app/build/reports/tests/integTest/index.html) afterwards.

#### End-to-End Tests
The end-to-end tests additionally connect to a Discord server.
While the database runs completely locally, the Discord server has to be a "real" one.
In order for the tests to run, take a look at the
[`app/src/main/resources/application.yml`](app/src/main/resources/application.yml) and set the environment variables
that are needed there. Use e.g., a run configuration in your IDE for that.

You can run them seperatly with the Gradle task `e2eTest`:

```shell
./gradlew app:e2eTest
```

Find the test results in [the test report](app/build/reports/tests/e2eTest/index.html) afterwards.

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
