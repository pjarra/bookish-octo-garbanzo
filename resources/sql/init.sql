-- :name init-period-table :!
INSERT INTO ohlcv.period (seconds, label) VALUES
       (60, 'minute'), (3600, 'hour'), (14400, 'four-hour'), (86400, 'day'), (604800, 'week'), (2419200, 'month')
