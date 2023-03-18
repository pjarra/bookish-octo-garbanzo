(ns migrations
  (:require [ragtime.repl :as rr]
            [ragtime.jdbc :as rj]
            [ragtime.protocols :as rp]
            [clojure.java.jdbc :as sql]
            [clojure.java.io :as io])
  (:import [java.text SimpleDateFormat]
           [java.util Date]))

(def migrations "migrations")
(def db-name "/tmp/data-test3.db")

;;
;; The following part is necessary because java.jdbc does not work correctly with duckdb.
;; See comment in `duck-database`.
;;

(defn delete-migration-entry!
  [db-spec table id]
  (let [conn (sql/get-connection db-spec)
        stmt (sql/prepare-statement conn (str "DELETE FROM " table " WHERE id = ?"))]
    (#'sql/set-parameters stmt [id])
    (.executeUpdate stmt)))

;; Have to reimplement the SqlDatabase from ragtime.jdbc; see documentation
;; of `duck-database`
(defrecord DuckDB [db-spec migrations-table]
  rp/DataStore
  (add-migration-id [_ id]
    (#'rj/ensure-migrations-table-exists db-spec migrations-table)
    (sql/insert! db-spec migrations-table
                 {:id         (str id)
                  :created_at (#'rj/format-datetime (Date.))}))

  (remove-migration-id [_ id]
    (#'rj/ensure-migrations-table-exists db-spec migrations-table)
    (delete-migration-entry! db-spec migrations-table id)
    #_(sql/delete! db-spec migrations-table ["id = ?" id]))

  (applied-migration-ids [_]
    (#'rj/ensure-migrations-table-exists db-spec migrations-table)
    (sql/query db-spec
               [(str "SELECT id FROM " migrations-table " ORDER BY created_at")]
               {:row-fn :id})))

(defn duck-database
  "Reimplementation of `ragtime.jdbc/sql-database`.
   The SqlDatabase record defined in ragtime does not work with DuckDB.
   That's more an issue with jdbc, where `.addBatch` is called whenever a parameterized
   statement is received in `db-do-execute-statement` (where all `execute!`-paths lead to).
   This however is incompatible with the contract of jdbc, which states that drivers are
   not required to support the feature of batch processing. So, the proper solution would be
   for `java.jdbc` to query `DatabaseMetaData.supportsBatchUpdates` first, and, if it doesn't,
   process all updates sequientially and manually. The workaround is added here in
   `delete-migrations-entry!` in `remove-migrations-id` of the `DuckDB` record.
   See also `ragtime.protocols/sql-database`."
  ([db-spec]
   (duck-database db-spec {}))
  ([db-spec options]
   (->DuckDB db-spec (:migrations-table options "ragtime_migrations"))))

;;
;; -- SNIP --
;;

(def conf {:datastore (duck-database {:connection-uri (str "jdbc:duckdb:" db-name)})
           :migrations (rj/load-resources migrations)})

(defn create-migration
  [name]
  (let [formatter (SimpleDateFormat. "YmmddHHMMss")
        now (.format formatter (Date.))
        path (.getFile (io/resource migrations))]
    (spit (io/file path (str now "-" name ".up.sql")) "")
    (spit (io/file path (str now "-" name ".down.sql")) "")))

(comment
  
  (rr/migrate conf)
  (rr/rollback conf 1))
