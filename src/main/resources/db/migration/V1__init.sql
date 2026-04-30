-- V1__init.sql

CREATE EXTENSION IF NOT EXISTS citext;

CREATE TABLE players (
                         id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                         name CITEXT NOT NULL UNIQUE,
                         email CITEXT UNIQUE,
                         password VARCHAR(255),
                         access_code VARCHAR(10) UNIQUE,
                         verification_code VARCHAR(6),
                         verified BOOLEAN DEFAULT FALSE,
                         is_blocked BOOLEAN DEFAULT FALSE,
                         created_at TIMESTAMP
);

CREATE TABLE subscription (
                              id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                              player_id UUID UNIQUE NOT NULL REFERENCES players(id),
                              active BOOLEAN DEFAULT FALSE,
                              started_at TIMESTAMP,
                              expires_at TIMESTAMP,
                              created_at TIMESTAMP DEFAULT NOW()
);

CREATE TABLE tournament (
                            id BIGSERIAL PRIMARY KEY,
                            external_id BIGINT UNIQUE NOT NULL,
                            link TEXT,
                            date DATE,
                            time VARCHAR(10),
                            started BOOLEAN DEFAULT FALSE,
                            finished BOOLEAN DEFAULT FALSE,
                            cancelled BOOLEAN DEFAULT FALSE,
                            processed BOOLEAN DEFAULT FALSE
);

CREATE TABLE tournament_results (
                                    id BIGSERIAL PRIMARY KEY,
                                    player_id UUID NOT NULL REFERENCES players(id),
                                    tournament_id BIGINT NOT NULL REFERENCES tournament(id),
                                    player_name CITEXT NOT NULL,
                                    amount DOUBLE PRECISION NOT NULL,
                                    date DATE NOT NULL,
                                    is_night BOOLEAN DEFAULT FALSE,
                                    bonus DOUBLE PRECISION,
                                    UNIQUE(player_id, tournament_id)
);

CREATE TABLE player_notification (
                                     id BIGSERIAL PRIMARY KEY,
                                     player_id UUID NOT NULL REFERENCES players(id),
                                     tournament_id BIGINT NOT NULL REFERENCES tournament(id),
                                     hall INTEGER,
                                     reminder_sent BOOLEAN DEFAULT FALSE,
                                     evening_sent BOOLEAN DEFAULT FALSE,
                                     UNIQUE(player_id, tournament_id)
);

CREATE TABLE lineup (
                        id BIGSERIAL PRIMARY KEY,
                        league VARCHAR(50) NOT NULL,
                        time VARCHAR(10) NOT NULL,
                        players TEXT NOT NULL,
                        date DATE NOT NULL,
                        city VARCHAR(100) NOT NULL
);

CREATE INDEX idx_lineup_date_city ON lineup(date, city);