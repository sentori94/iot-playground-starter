ALTER TABLE runs ADD COLUMN username VARCHAR(20) NOT NULL DEFAULT 'anonymous';
ALTER TABLE runs ADD CONSTRAINT run_username_len CHECK (char_length(username) BETWEEN 3 AND 20);
