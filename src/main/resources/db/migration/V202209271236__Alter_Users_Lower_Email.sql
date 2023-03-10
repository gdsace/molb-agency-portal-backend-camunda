UPDATE users SET email=lower(email);

ALTER TABLE users
    ADD CONSTRAINT users_email_lowercase_ck
        CHECK (email = lower(email));
