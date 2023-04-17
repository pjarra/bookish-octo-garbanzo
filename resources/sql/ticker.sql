-- :name insert-pair :! :1
INSERT INTO ohlcv.pair(base, quote) VALUES (:base, :quote);

-- :name insert-pairs :! :n
INSERT INTO ohlcv.pair(base, quote) VALUES :t*:pairs;

-- :name all-pairs :?
SELECT * FROM ohlcv.pair;

-- :name base-pairs :?
SELECT * FROM ohlcv.pair
WHERE base = :base

-- :name quote-pairs :?
SELECT * FROM ohlcv.pair
WHERE quote = :quote

-- :name pairs-with :?
SELECT * FROM ohlcv.pair
WHERE quote = :symbol OR base = :symbol

-- :name insert-exchange :! :1
INSERT INTO ohlcv.exchange(label) VALUES (:label)

-- :name insert-exchanges :! :n
INSERT INTO ohlcv.exchange(label) VALUES :t*:labels

-- :name exchange :?
SELECT * FROM ohlcv.exchange;

-- :name insert-ticker :! :1
INSERT INTO ohlcv.ticker(exchange_label, pair_base, pair_quote) VALUES (:exchange, :base, :quote)

-- :name exchange-ticker
SELECT * FROM ohlcv.ticker
WHERE exchange_label = :exchange

-- :name exchange-ticker-for-base
SELECT * FROM ohlcv.ticker
WHERE exchange_label = :exchange AND pair_base = :base

-- :name exchange-ticker-for-quote
SELECT * FROM ohlcv.ticker
WHERE exchange_label = :exchange AND pair_quote = :quote

-- :name ticker-id
SELECT id FROM ohlcv.ticker
WHERE exchange_label = :exchange AND pair_base = :base AND pair_quote = :quote
