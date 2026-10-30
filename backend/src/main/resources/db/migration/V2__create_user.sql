CREATE TABLE users
(
    id              UUID            NOT NULL DEFAULT gen_random_uuid() PRIMARY KEY,
    name            VARCHAR(100)    NOT NULL,
    email           VARCHAR(254)    NOT NULL,
    password_hash   VARCHAR(255)    NOT NULL,
    role            VARCHAR(50)     NOT NULL
);

CREATE UNIQUE INDEX users_email_lower_unique
    ON users (LOWER(email));