CREATE TABLE IF NOT EXISTS countinggame.game_state (
    id int4 NOT NULL,
    count int4 NOT NULL,
    last_user varchar(255) NULL,
    CONSTRAINT game_state_pkey PRIMARY KEY (id)
);
