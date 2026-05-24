CREATE TABLE IF NOT EXISTS countinggame.game_state (
    id int4 NOT NULL,
    count int4 NOT NULL,
    last_user varchar(255) NULL,
    CONSTRAINT game_state_pkey PRIMARY KEY (id)
);

CREATE TABLE countinggame.user_state (
    user_id        varchar(255) NOT NULL PRIMARY KEY,
    can_not_count  boolean      NOT NULL,
    longest_streak integer      NOT NULL,
    streak         integer      NOT NULL
);
