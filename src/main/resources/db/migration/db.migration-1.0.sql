--liquibase formatted sql

--changeset ataraxii:1
CREATE TABLE users
(
    id         UUID PRIMARY KEY default gen_random_uuid(),
    email      VARCHAR(64)                   NOT NULL UNIQUE,
    password   VARCHAR(60)                   NOT NULL,
    role       VARCHAR(20)                   NOT NULL,
    created_at timestamp        default now(),
    is_enabled boolean          default TRUE NOT NULL
);

CREATE TABLE cards
(
    id           UUID PRIMARY KEY                    default gen_random_uuid(),
    card_number  CHAR(16)                   NOT NULL,
    last4        CHAR(4)                    NOT NULL,
    holder_name  VARCHAR(150)               NOT NULL,
    expiry_month SMALLINT                   NOT NULL,
    expiry_year  SMALLINT                   NOT NULL,
    status       VARCHAR(20)                NOT NULL,
    balance      BIGINT                     NOT NULL DEFAULT 0,
    created_at   timestamp                           default now(),
    user_id      UUID references users (id) NOT NULL
);

CREATE TABLE refresh_tokens
(
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id     UUID      NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    token       TEXT      NOT NULL,
    expiry_date timestamp NOT NULL
);

CREATE TABLE transactions
(
    id               UUID PRIMARY KEY     DEFAULT gen_random_uuid(),
    status           VARCHAR(20) NOT NULL,
    amount           BIGINT      NOT NULL,
    from_card        UUID references cards (id),
    from_card_last4 CHAR(4) NOT NULL,
    to_card          UUID references cards (id),
    to_card_last4 CHAR(4) NOT NULL,
    transaction_date timestamp   NOT NULL default now(),
    balance_after    BIGINT      NOT NULL
);
