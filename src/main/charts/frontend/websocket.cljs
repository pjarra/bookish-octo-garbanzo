(ns charts.frontend.websocket
  (:require [cognitect.transit :as t]))

(defonce ws-channel (atom nil))

(defonce CLOSED 3)

(def json-reader (t/reader :json))
(def json-writer (t/writer :json))

(defn receive-transit-msg!
  [update-fn]
  (fn [msg]
    (update-fn
     (->> msg .-data (t/read json-reader)))))

(defn send-transit-msg!
  [msg]
  (if @ws-channel
    (.send @ws-channel (t/write json-writer msg))
    (throw (js/Error. "Websocket is not available!"))))

(defn close-websocket!
  []
  (when @ws-channel
    (.close @ws-channel)
    (reset! ws-channel nil)))

(defn make-websocket!
  [url receive-handler]
  (if @ws-channel
    (throw (js/Error. "Websocket is not nil"))
    (if-let [chan (js/WebSocket. url)]
      (do
        (set! (.-onmessage chan) (receive-transit-msg! receive-handler))
        (reset! ws-channel chan))
      (throw (js/Error. "Websocket connection failed!")))))
