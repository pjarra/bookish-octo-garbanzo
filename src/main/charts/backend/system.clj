(ns charts.backend.system
  (:require [clojure.java.io :as io]
            [integrant.core :as ig]
            [org.httpkit.server :as server]
            [reitit.ring :as ring]))

(def system
  {:app/config {}
   :app/handler {}
   :app/adapter {:config (ig/ref :app/config)
                 :handler (ig/ref :app/handler)}})

(defmethod ig/init-key :app/config
  [_ _]
  (read-string (slurp (io/resource "config.edn"))))

(defmethod ig/init-key :app/handler
  [_ _]
  (ring/ring-handler
   (ring/router
    [["/ping" (constantly {:status 200 :body "pong"})]])
   (ring/routes
    (ring/create-resource-handler {:path "/"})
    (ring/create-default-handler))))

(defmethod ig/init-key :app/adapter
  [_ {:keys [config handler]}]
  (server/run-server handler {:port (:port config 3000)
                              ;; when true, returns a stop-fn only
                              :legacy-return-value? false}))

(defmethod ig/halt-key! :app/adapter
  [_ http-server]
  (server/server-stop! http-server))

(comment
  (slurp (io/resource "config.edn"))
  )
