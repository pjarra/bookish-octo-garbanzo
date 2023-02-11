(ns charts.frontend.app
  (:require [cljs.core.async :as async :refer [chan go]]
            ["vega" :as vega]
            ["vega-lite" :as vega-lite]
            ["vega-embed" :as vega-embed]))

(def streaming-switch (atom false))
(def streaming-status (chan))

(defn stop-streaming
  []
  (when @streaming-switch
    (reset! streaming-switch false)
    (go
      (if (= :inactive (async/<! streaming-status))
        (js/console.log "Stream stopped")))))

(defn gen-value
  []
  #js {:a (char (+ 65 (rand-int 26)))
       :b (rand-int 100)})

;; https://vega.github.io/vega-lite/tutorials/streaming.html
(defn stream
  [view]
  (println "Starting stream for" view)
  (letfn [(insert []
            (let [cs (.insert (vega/changeset) (gen-value))]
              (-> view
                  (.change "thedata" cs)
                  (.run))))
          (streamer []
            (if-not @streaming-switch
              (async/put! streaming-status :inactive)
              (do
                (insert)
                (js/setTimeout streamer 1000))))]
    (reset! streaming-switch true)
    (streamer)))

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
                (println view)
                (then view)))
      (.catch (or error (fn [err]
                          (js/console.error err))))))

(defn ^:dev/after-load after-load
  []
  (embed-vega {:then #(js/setTimeout stream 1000 %)}))

(defn ^:dev/before-load before-load
  []
  (stop-streaming)
  (js/console.log "stop"))

(defn init
  []
  (after-load))
