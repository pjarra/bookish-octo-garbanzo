;; The user namespace is automatically required from Clojure, when found.
;; Alternative: clj -X:dev calls dev/start-with-shadow
(ns user
  (:require [integrant.repl :as igr]
            [integrant.repl.state :as irstate]
            [shadow.cljs.devtools.server :as shadow-server]
            [shadow.cljs.devtools.api :as shadow]
            [org.httpkit.server :as server]
            [charts.backend.system :as system]
            [db :as db]))

;; https://stackoverflow.com/questions/61727582/how-to-avoid-unresolved-symbol-with-clj-kond-when-using-hugsql-def-db-fns-macro

(defn init-db []
  (db/create-ohlcv-schema db/ds))

(defn reset-db []
  (db/drop-ohlcv-schema-cascading db/ds)
  (init-db))

(defn shadow-watch
  [build-id]
  (shadow/watch build-id))

(defn shadow-start-and-watch
  [build-id]
  (shadow-server/start!)
  (shadow-watch build-id))

(defn http-server-status
  []
  (when-let [http-server (:app/adapter irstate/system)]
    (let [status (server/server-status http-server)]
      (if (= :running status)
        (.getPort http-server)
        status))))

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
