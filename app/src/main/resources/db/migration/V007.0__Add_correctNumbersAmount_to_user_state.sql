ALTER TABLE countinggame.user_state ADD COLUMN correct_numbers_amount integer;
ALTER TABLE countinggame.user_state ALTER COLUMN correct_numbers_amount SET NOT NULL,
                                    ALTER COLUMN correct_numbers_amount TYPE integer using longest_streak
