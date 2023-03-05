(ns charts.frontend.app
  (:require [cljs.core.async :as async]
            ["vega" :as vega]
            ["vega-lite" :as vega-lite]
            ["vega-embed" :as vega-embed]
            [charts.frontend.websocket :as ws]))

(def chan (async/chan))

(defn update-chart
  [view id [date open high low close]]
  (let [data (clj->js {:date date
                       :open (js/parseFloat open)
                       :high (js/parseFloat high)
                       :low (js/parseFloat low)
                       :close (js/parseFloat close)})
        cs (.insert (vega/changeset) data)]
    (-> view (.change id cs) (.run))))

;; https://vega.github.io/vega-lite/tutorials/streaming.html
(def spec
  {:$schema "https://vega.github.io/schema/vega-lite/v5.json",
   :description "Candlestick chart from the vega-lite example",
   :encoding {:x {:field "date"
                  :type "temporal"
                  :title "Datum"
                  :axis {:format "%m/%d"
                         :labelAngle -45
                         :title "Datum"}}
              :y {:type "quantitative"
                  :scale {:zero false}
                  :axis {:title "Price"}}
              :color {:condition {:test "datum.open < datum.close",
                                  :value "#06982d"}
                      :value "#ae1325"}}
   :layer [{:mark "rule"
            :encoding {:y {:field "low"}
                       :y2 {:field "high"}}}
           {:mark "bar"
            :encoding {:y {:field "open"}
                       :y2 {:field "close"}}}]
   :data {:name "thedata"
          :values [{:date "01-Jun-2009" :open 28.7 :high 30.05 :low 28.45 :close 30.04}
                   {:date "02-Jun-2009" :open 30.04 :high 30.13 :low 28.3 :close 29.63}
                   {:date "03-Jun-2009" :open 29.62 :high 31.79 :low 29.62 :close 31.02}]}})

;; To make a react component, see
;; https://github.com/metasoarous/oz/blob/master/src/cljs/oz/core.cljs
(defn embed-vega
  [{:keys [then error]}]
  (-> (vega-embed "#app" (clj->js spec) (clj->js {:actions
                                                  {:export true
                                                   :source false
                                                   :compiled false
                                                   :editor false}}))
      (.then #(let [view (:view (js->clj % :keywordize-keys true))]
                (then view)))
      (.catch (or error (fn [err]
                          (js/console.error err))))))

(defn websocket-handler
  [channels]
  (fn [msg]
    (println "Received" msg)
    (if-let [t (get msg "type")]
      (if-let [c (get channels t)]
        (async/put! c (get msg t))
        (.log js/console "No handler for" t))
      (.log js/console "No specifier in websocket message"))))

(defn connect-websocket-to-view
  [view]
  (let [handler (websocket-handler {"value" chan})]
    (ws/make-websocket! "ws://localhost:3000/ws" handler)
    (async/go-loop []
      (when-let [v (async/<! chan)]
        (update-chart view "thedata" v)
        (recur)))))

(defn ^:dev/after-load after-load
  []
  (embed-vega {:then connect-websocket-to-view}))

(defn ^:dev/before-load before-load
  []
  (ws/close-websocket!)
  (js/console.log "stop"))

(defn init
  []
  (after-load))
