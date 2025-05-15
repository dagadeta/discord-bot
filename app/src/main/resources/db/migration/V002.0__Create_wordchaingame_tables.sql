CREATE TABLE IF NOT EXISTS wordchaingame.game_state (
    id int4 NOT NULL,
    last_user varchar(255) NULL,
    started bool NOT NULL,
    CONSTRAINT game_state_pkey PRIMARY KEY (id)
);

CREATE TABLE wordchaingame.used_words (
	word varchar(255) NOT NULL,
	CONSTRAINT used_words_pkey PRIMARY KEY (word)
);
