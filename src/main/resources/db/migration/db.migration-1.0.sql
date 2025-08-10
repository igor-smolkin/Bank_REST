--liquibase formatted sql

--changeset ataraxii:1
CREATE TABLE cards (
    id UUID PRIMARY KEY default gen_random_uuid(),
    card_number BYTEA NOT NULL,
    last4 CHAR(4) NOT NULL,
    holder_name VARCHAR(150) NOT NULL,
    expiry_month SMALLINT NOT NULL,
    expiry_year SMALLINT NOT NULL,
    status VARCHAR(20) NOT NULL,
    balance BIGINT NOT NULL DEFAULT 0,
    created_at timestamp default now()
);

CREATE TABLE users (
    id UUID PRIMARY KEY default gen_random_uuid(),
    email VARCHAR(64) NOT NULL UNIQUE,
    password VARCHAR(60) NOT NULL,
    role VARCHAR(20) NOT NULL,
    created_at timestamp default now()
);

