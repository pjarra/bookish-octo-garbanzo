(ns charts.backend.websocket
  (:require [org.httpkit.server :as kit]
            [cognitect.transit :as t]
            [clojure.core.async :as async]
            [clojure.data.json :as j]
            #_[clojure.tools.logging :as log])
  (:import [java.time LocalDate]
           [java.time.format DateTimeFormatter]))

(defonce channels (atom #{}))

(defn connect!
  [channel]
  (println "channel open")
  (swap! channels conj channel))

(defn disconnect!
  [channel status]
  (println "Channel disconnect:" status)
  (swap! channels #(remove #{channel} %)))

(defn notify-clients
  [msg]
  (doseq [c @channels]
    (kit/send! c (j/write-str msg))))

(def handler-map {:on-open connect!
                  :on-close disconnect!
                  :on-receive #(notify-clients %2)})

(defn handler
  [request]
  (if-not (:websocket? request)
    {:status 200 :body "Awaiting websocket connection"}
    (kit/as-channel request handler-map)))

(def routes ["/ws" handler])

(let [dm (atom "03-Jun-2009")]
  (defn next-date
    []
    (let [formatter (DateTimeFormatter/ofPattern "dd-MMM-yyyy")
          next (-> @dm
                   (LocalDate/parse formatter)
                   (.plusDays 1))]
      (reset! dm (.format formatter next)))))

(comment
  (next-date))

(defn random-ohlcv
  []
  (let [d (next-date)
        low (rand-int 30)
        high (+ low (rand-int 20))
        open (+ low (rand-int (- high low)))
        close (+ low (rand-int (- high low)))]
    [d open high low close]))

(defn send-random
  ([]
   (let [value (random-ohlcv)]
     (notify-clients {:type :value
                      :value value})))
  ([amount]
   (let [values (mapv (fn [_] (random-ohlcv)) (range amount))]
     (notify-clients {:type :list
                      :list values})))
  ([amount delay]
   (async/go-loop [n amount]
     (send-random)
     (when-not (zero? n)
       (Thread/sleep delay)
       (recur (- n 1))))))

(send-random)
(send-random 10 1000)
(send-random 10)
