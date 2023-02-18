(ns charts.backend.system
  (:require [clojure.java.io :as io]
            [integrant.core :as ig]))

(def system
  {:app/config {}})

(defmethod ig/init-key :app/config [_ _]
  (read-string (slurp (io/resource "config.edn"))))

(comment
  (slurp (io/resource "config.edn"))
  )
