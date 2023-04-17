(ns charts.backend.hugsql
  (:require [clojure.java.io :as io]
            [hugsql.core :as hugsql]))

(hugsql/def-db-fns (io/resource "sql/schema.sql"))
(hugsql/def-db-fns (io/resource "sql/init.sql"))
(hugsql/def-db-fns (io/resource "sql/ticker.sql"))
(hugsql/def-db-fns (io/resource "sql/point.sql"))
