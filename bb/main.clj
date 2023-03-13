(ns main
  (:require [babashka.cli :as cli]))

(defonce bitstamp-api "https://www.bitstamp.net/api/v2/ohlc/")

(defn req-add-opt [s k v]
  (let [sep (if (empty? s) "?" "&")]
    (str s sep (name k) "=" v)))

(defn opts->request [opts]
  (reduce-kv req-add-opt "" opts))

(defn endpoint [pair opts]
  (str bitstamp-api pair (opts->request opts)))

(defn timeframe? [x]
  (some #{x} #{60, 180, 300, 900, 1800, 3600, 7200, 14400, 21600, 43200, 86400, 259200}))

(defn pair? [x]
  (some #{x} #{"btcusd" "btceur" "btcgbp" "btcpax" "gbpusd" "gbpeur"
               "eurusd" "xrpusd" "xrpeur" "xrpbtc" "xrpgbp"
               "ltcbtc" "ltcusd" "ltceur" "ltcgbp" "ethbtc" "ethusd" "etheur"}))

(def min-time 1420066800)
(def max-time 1704063600)

(defn timestamp? [x]
  (<= min-time x max-time))

(def timestamp-validate-map
  {:pred timestamp?
   :ex-msg (fn [m] (format "Timestamps must be in range [%s %s]" min-time max-time))})

(def spec {:currency   {:desc         "A currency pair supported by bitstamp"
                        :alias        :c
                        :default-desc "btcusd"
                        :default      "btcusd"
                        :validate     pair?}
           :since {:desc         "Timestamp of first requested datapoint."
                   :alias        :s
                   :validate     timestamp-validate-map}
           :until {:desc         "Timestamp of last requested datapoint."
                   :alias        :u
                   :validate     timestamp-validate-map}
           :timeframe  {:desc         "Seconds; 60, 180, 300, 900, 1800, ... 259200"
                        :default      86400
                        :default-desc "86400; daily"
                        :validate {:pred timeframe?
                                   :ex-msg (fn [m] (str (:value m) " is not a valid timeframe"))}}})

(defn -main [& args]
  (let [opts (cli/parse-opts args {:spec spec :restrict (keys spec)})
        ep (endpoint (:currency opts) {:limit 10
                                       :step (:timeframe opts)})]
    (println (slurp ep))))
