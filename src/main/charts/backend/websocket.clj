(ns charts.backend.websocket
  (:require [org.httpkit.server :as kit]
            [cognitect.transit :as t]
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
  (println "Received" msg)
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
