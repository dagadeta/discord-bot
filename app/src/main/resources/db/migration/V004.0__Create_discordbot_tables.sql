CREATE TABLE IF NOT EXISTS discordbot.bot_config (
    command_group varchar(32) not null,
    name          varchar(32) not null,
    value         varchar(255),
    timestamp     timestamp(6),
    primary key (command_group, name)
);
