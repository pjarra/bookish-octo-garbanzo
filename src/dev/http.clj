(ns http
  (:require [org.httpkit.client :as kit]
            [clojure.data.json :as json]))

(defonce bitstamp-api "https://www.bitstamp.net/api/v2/ohlc/")

(defn req-add-opt [s k v]
  (let [sep (if (empty? s) "?" "&")]
    (str s sep (name k) "=" v)))

(defn opts->request [opts]
  (reduce-kv req-add-opt "" opts))

(defn endpoint [pair opts]
  (str bitstamp-api pair (opts->request opts)))

(def min-time 1420066800)
(def max-time (quot (System/currentTimeMillis) 1000))

(def ^:private step
  {:minute 60
   :minute3 180
   :minute5 300
   :minute15 900
   :minute30 1800
   :hour 3600
   :hour2 7200
   :hour4 14400
   :hour6 21600
   :hour12 43200
   :day 86400
   :day3 259200})

(defn tf->step
  [tf]
  (or (get step tf)
      (throw (ex-info "Illegal timeframe" {:tf tf}))))

(defn pair? [x]
  (some #{x} #{"btcusd" "btceur" "btcgbp" "btcpax" "gbpusd" "gbpeur"
               "eurusd" "xrpusd" "xrpeur" "xrpbtc" "xrpgbp"
               "ltcbtc" "ltcusd" "ltceur" "ltcgbp" "ethbtc" "ethusd" "etheur"}))

(defn timestamp? [x]
  (<= min-time x max-time))

(defn transform-val [k v]
  (cond
    (= k :timestamp) (Integer/parseInt v)
    (some #{k} '(:open :high :low :close :volume)) (Double/parseDouble v)
    :else v))

(defn get-points
  [pair since tf limit]
  {:pre [(pair? pair)
         (timestamp? since)]}
  (let [step (tf->step tf)
        dat @(kit/get (endpoint pair {:since since
                                      :limit limit
                                      :step step}))
        status (:status dat)
        body (:body dat)]
    (when-not (= status 200)
      (throw (ex-info "Error requesting the data" {:reason :status :status status})))
    (let [js (json/read-str body :key-fn keyword :value-fn transform-val)]
      (if (contains? js :errors)
        (throw (ex-info "Errors requesting the data" {:reason :errors :erros (:errors js)}))
        (get-in js [:data :ohlc])))))

;;(get-points "btcusd" (- max-time 1000) :hour 10)
