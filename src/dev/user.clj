;; The user namespace is automatically required from Clojure, when found.
;; Alternative: clj -X:dev calls dev/start-with-shadow
(ns user
  (:require [integrant.repl :as igr]
            [shadow.cljs.devtools.server :as shadow-server]
            [shadow.cljs.devtools.api :as shadow]
            [charts.backend.system :as system]))

(defn shadow-watch
  [build-id]
  (shadow/watch build-id))

(defn shadow-start-and-watch
  [build-id]
  (shadow-server/start!)
  (shadow-watch))

(defn start
  []
  (igr/set-prep! (constantly system/system))
  (igr/go))

(defn stop
  []
  (igr/halt))

(comment
  (start)
  (stop)
  integrant.repl.state/system
  )
