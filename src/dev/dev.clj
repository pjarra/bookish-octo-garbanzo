(ns dev
  (:require [clojure.main :as clj]
            [shadow.cljs.devtools.server :as shadow-server]
            [shadow.cljs.devtools.api :as shadow]))

(defn start-with-shadow
  [{:keys [build]}]
  (shadow-server/start!)
  (shadow/watch build)
  (clj/repl))
