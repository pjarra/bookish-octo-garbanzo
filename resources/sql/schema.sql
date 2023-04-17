-- :name create-schema :!
CREATE SCHEMA IF NOT EXISTS ohlcv;

-- :name drop-schema :!
DROP SCHEMA IF EXISTS ohlcv;

-- :name create-pair-table
CREATE TABLE IF NOT EXISTS ohlcv.pair (
       base VARCHAR NOT NULL,
       quote VARCHAR NOT NULL,
       PRIMARY KEY (base, quote)
)

-- :name drop-pair-table :!
DROP TABLE IF EXISTS ohlcv.pair;

-- :name create-exchange-table :!
CREATE TABLE IF NOT EXISTS ohlcv.exchange (
       label VARCHAR PRIMARY KEY
)

-- :name drop-exchange-table :!
DROP TABLE IF EXISTS ohlcv.exchange;

-- :name create-ticker-sequence :!
CREATE SEQUENCE IF NOT EXISTS ohlcv.seq_ticker START 1;

-- :name drop-ticker-sequence :!
DROP SEQUENCE IF EXISTS ohlcv.seq_ticker;

-- :name create-ticker-table :!
CREATE TABLE IF NOT EXISTS ohlcv.ticker (
       id INTEGER PRIMARY KEY DEFAULT nextval('ohlcv.seq_ticker'),
       exchange_label VARCHAR, pair_base VARCHAR, pair_quote VARCHAR,
       FOREIGN KEY (pair_base, pair_quote) REFERENCES ohlcv.pair (base, quote),
       FOREIGN KEY (exchange_label) REFERENCES ohlcv.exchange (label)
)

-- :name drop-ticker-table :!
DROP TABLE IF EXISTS ohlcv.ticker;

-- :name create-period-table :!
CREATE TABLE IF NOT EXISTS ohlcv.period (
       seconds INTEGER PRIMARY KEY CHECK(seconds % 60 = 0),
       label VARCHAR UNIQUE
)

-- :name drop-period-table :!
DROP TABLE IF EXISTS ohlcv.period;

-- :name create-point-table :!
CREATE TABLE IF NOT EXISTS ohlcv.point (
       ticker_id INTEGER NOT NULL,
       period_seconds INTEGER NOT NULL,
       ts INT64 NOT NULL,
       open FLOAT NOT NULL,
       high FLOAT NOT NULL CHECK (high >= open),
       low FLOAT NOT NULL CHECK (low <= high),
       close FLOAT NOT NULL CHECK (close >= low AND close <= high),
       volume FLOAT NOT NULL,
       FOREIGN KEY (ticker_id) REFERENCES ohlcv.ticker (id),
       FOREIGN KEY (period_seconds) REFERENCES ohlcv.period (seconds),
       PRIMARY KEY (ticker_id, period_seconds, ts)
);

-- :name drop-point-table :!
DROP TABLE IF EXISTS ohlcv.point;
