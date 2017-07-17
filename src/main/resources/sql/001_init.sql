  BEGIN TRANSACTION;

  DROP TABLE IF EXISTS "users" CASCADE;
    DROP SEQUENCE IF EXISTS "users_seq" CASCADE;
  CREATE SEQUENCE "users_seq" START 1;

  CREATE TABLE "users" (
    "id"            BIGINT PRIMARY KEY DEFAULT "nextval"('"users_seq"'),
    "login"         TEXT NOT NULL,
    "password"      TEXT NOT NULL,
    "created_on"    TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    "deleted_on"    TIMESTAMP WITHOUT TIME ZONE DEFAULT NULL,
    "account_type"  INT NOT NULL DEFAULT 0
  );

  END TRANSACTION;