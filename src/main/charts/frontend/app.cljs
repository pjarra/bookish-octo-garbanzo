(ns charts.frontend.app
  (:require [cljs.core.async :as async]
            ["vega" :as vega]
            ["vega-lite" :as vega-lite]
            ["vega-embed" :as vega-embed]
            [charts.frontend.websocket :as ws]))

(def chan (async/chan))

(defmulti chart-update identity)

(defn value->changeset
  [v]
  (.insert (vega/changeset) v))

(defn update-chart-with-changeset
  [view id changeset]
  (-> view (.change id changeset) (.run)))

(defn value->js
  [[date open high low close]]
  (clj->js {:date date
            :open (js/parseFloat open)
            :high (js/parseFloat high)
            :low (js/parseFloat low)
            :close (js/parseFloat close)}))

(defmethod chart-update :value
  [_ view id value]
  (let [cs (value->changeset (value->js value))]
    (update-chart-with-changeset view id cs)))

(defmethod chart-update :list
  [_ view id values]
  (let [arr (clj->js (mapv value->js values))]
    (->> values
         (mapv value->js)
         (clj->js)
         (value->changeset)
         (update-chart-with-changeset view id))))

;; https://vega.github.io/vega-lite/tutorials/streaming.html
(def spec
  {:$schema "https://vega.github.io/schema/vega-lite/v5.json",
   :description "Candlestick chart from the vega-lite example",
   :width "500"
   :height "400"
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
            :stroke "black"
            :encoding {:y {:field "open"} :y2 {:field "close"}}}]
   :data {:name "thedata"}})

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
    (if-let [t (get msg "type")]
      (if-let [c (get channels t)]
        (async/put! c [(keyword t) (get msg t)])
        (.log js/console "No handler for" t))
      (.log js/console "No specifier in websocket message"))))

(defn connect-websocket-to-view
  [view]
  (let [handler (websocket-handler {"value" chan "list" chan})]
    (ws/make-websocket! "ws://localhost:3000/ws" handler)
    (async/go-loop []
      (when-let [[t v] (async/<! chan)]
        (chart-update t view "thedata" v)
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
