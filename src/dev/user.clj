;; The user namespace is automatically required from Clojure, when found.
;; Alternative: clj -X:dev calls dev/start-with-shadow
(ns user
  (:require [integrant.repl :as igr]
            [shadow.cljs.devtools.server :as shadow-server]
            [shadow.cljs.devtools.api :as shadow]
            [org.httpkit.server :as server]
            [charts.backend.system :as system]))

(defn shadow-watch
  [build-id]
  (shadow/watch build-id))

(defn shadow-start-and-watch
  [build-id]
  (shadow-server/start!)
  (shadow-watch build-id))

(defn http-server-status
  []
  (when-let [http-server (:app/adapter integrant.repl.state/system)]
    (server/server-status http-server)))

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
  (http-server-status)
  (shadow-start-and-watch :frontend)
  integrant.repl.state/system
  )
