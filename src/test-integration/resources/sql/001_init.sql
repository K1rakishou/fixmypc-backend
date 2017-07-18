BEGIN TRANSACTION;

DROP TABLE IF EXISTS "users" CASCADE;

CREATE TABLE "users" (
    "login"             text PRIMARY KEY NOT NULL,
    "password"          text NOT NULL,
    "created_on"        timestamp with time zone DEFAULT now(),
    "deleted_on"        timestamp without time zone,
    "account_type"      integer NOT NULL DEFAULT 0,
    CONSTRAINT "users_login_key" UNIQUE ("login")
);

END TRANSACTION;