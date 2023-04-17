(ns charts.backend.sql
  (:require [hugsql.core :as hugsql]
            [hugsql.adapter.next-jdbc :as next-jdbc]))

(hugsql/set-adapter! (next-jdbc/hugsql-adapter-next-jdbc))
