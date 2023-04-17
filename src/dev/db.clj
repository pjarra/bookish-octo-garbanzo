(ns db
  (:require [clojure.java.io :as io]
            [next.jdbc :as jdbc]
            [charts.backend.hugsql :as hug]))

(def db-path (io/file (System/getProperty "user.home") "data.duckdb"))

(def ds (jdbc/get-datasource {:connection-uri (str "jdbc:duckdb:/" db-path)}))

(defn create-ohlcv-schema [ds]
  (doto ds
    (hug/create-schema)
    (hug/create-pair-table)
    (hug/create-exchange-table)
    (hug/create-ticker-sequence)
    (hug/create-ticker-table)
    (hug/create-period-table)
    (hug/create-point-table)
    (hug/init-period-table)))

(defn drop-ohlcv-schema-cascading [ds]
  (doto ds
    (hug/drop-point-table)
    (hug/drop-period-table)
    (hug/drop-ticker-table)
    (hug/drop-ticker-sequence)
    (hug/drop-exchange-table)
    (hug/drop-pair-table)
    (hug/drop-schema)))


;;(hug/insert-exchange ds {:label "bitstamp"})
;;(hug/insert-pair ds {:base "usd" :quote "eth"})
;;(hug/insert-pairs ds {:pairs [["usd" "btc"] ["usd" "xrp"] ["btc" "xrp"]]})
;;(hug/insert-ticker ds {:exchange "bitstamp" :base "usd" :quote "xrp"})
;;(hug/all-pairs ds)
;;(hug/base-pairs ds {:base "usd"})
;;(hug/quote-pairs ds {:quote "btc"})
;;(hug/pairs-with ds {:symbol "btc"})
;;(hug/ticker-id ds {:exchange "bitstamp" :base "usd" :quote "xrp"})
