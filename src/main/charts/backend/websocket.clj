(ns charts.backend.websocket
  (:require [org.httpkit.server :as kit]
            [cognitect.transit :as t]
            [clojure.core.async :as async]
            [clojure.data.json :as j]
            #_[clojure.tools.logging :as log]))

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
    (kit/send! c msg)))

(def handler-map {:on-open connect!
                  :on-close disconnect!
                  :on-receive #(notify-clients %2)})

(defn handler
  [request]
  (if-not (:websocket? request)
    {:status 200 :body "Awaiting websocket connection"}
    (kit/as-channel request handler-map)))

(def routes ["/ws" handler])

(defn send-random
  ([]
   (notify-clients (j/write-str {:type :value
                                 :value {:a (str (+ 65 (rand-int 26)))
                                         :b (rand-int 100)}})))
  ([amount delay]
   (async/go-loop [n amount]
     (send-random)
     (when-not (zero? n)
       (Thread/sleep delay)
       (recur (- n 1))))))
