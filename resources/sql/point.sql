-- :name insert-point :! :1
-- :doc "Insert-or-upsert statement for a point row."
INSERT INTO ohlcv.point (ticker_id, period_seconds, ts, open, high, low, close, volume)
VALUES (:ticker, :period, :ts, :open, :high, :low, :close, :volume)
ON CONFLICT (ticker_id, period_seconds, ts)
DO UPDATE
SET open = excluded.open, high = excluded.high, low = excluded.low, close = excluded.close, volume = excluded.volume

-- :name point :? :1
SELECT open, high, low, close, volume
FROM ohlcv.point
WHERE ticker_id = :ticker AND period_seconds = :period AND ts = :ts

-- :name points-between :? :n
SELECT ts, open, high, low, close, volume
FROM ohlcv.point
WHERE ticker_id = :ticker AND period_seconds = :period AND ts >= :low AND ts <= :high
