(ns dev
  (:require [integrant.repl :as ig-repl]
            [shadow.cljs.devtools.server :as shadow-server]
            [shadow.cljs.devtools.api :as shadow]))

(defn start-with-shadow
  [{:keys [build]}]
  (shadow-server/start!)
  (shadow/watch build)
  (clojure.main/repl))
