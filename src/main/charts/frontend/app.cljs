(ns charts.frontend.app
  (:require [cljs.core.async :as async]
            ["vega" :as vega]
            ["vega-lite" :as vega-lite]
            ["vega-embed" :as vega-embed]
            [charts.frontend.websocket :as ws]))

(def chan (async/chan))

(defn gen-value
  []
  #js {:a (char (+ 65 (rand-int 26)))
       :b (rand-int 100)})

(defn update-chart
  [view id value]
  (let [n (js/parseInt (get value "a"))
        data (clj->js (assoc value "a" (char n)))
        cs (.insert (vega/changeset) data)]
    (-> view (.change id cs) (.run))))

;; https://vega.github.io/vega-lite/tutorials/streaming.html
(def v1-spec
  #js
  {:$schema "https://vega.github.io/schema/vega-lite/v5.json",
   :description "A simple bar chart with embedded data.",
   :data
   #js
   {:name "thedata"
    :values
    #js
    [#js {:a "A", :b 28} #js {:a "B", :b 55} #js {:a "C", :b 43}
     #js {:a "D", :b 91} #js {:a "E", :b 81} #js {:a "F", :b 53}
     #js {:a "G", :b 19} #js {:a "H", :b 87} #js {:a "I", :b 52}]},
   :mark #js {:type "line" :tooltip true},
   :encoding
   #js
   {:x #js {:field "a", :type "ordinal"},
    :y #js {:field "b", :type "quantitative"}}})

;; To make a react component, see
;; https://github.com/metasoarous/oz/blob/master/src/cljs/oz/core.cljs
(defn embed-vega
  [{:keys [then error]}]
  (-> (vega-embed "#app" v1-spec (clj->js {:actions
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
