(ns mlib.pg.conn
  (:import 
    [com.zaxxer.hikari    HikariDataSource])
  (:require 
    [next.jdbc            :as     jdbc]
    [next.jdbc.connection :refer  [->pool]]))
;=

(set! *warn-on-reflection* true)

;; https://github.com/seancorfield/next-jdbc/blob/master/doc/all-the-options.md#
;; https://github.com/seancorfield/next-jdbc/blob/master/src/next/jdbc/connection.clj
;; 
(defn get-connection 
  "db-spec {
    :jdbcUrl \"jdbc:postgresql://host:5432/database?username=test&password=qwe123\"}
    :auto-commit false (for :fetch-size statement option)
  "
  ^HikariDataSource [db-spec]
  (->pool HikariDataSource db-spec))
;;

(comment

  (let [db-spec 
        {:jdbcUrl "jdbc:postgresql://localhost/test?user=test&password=qwe123"}]
    ;
    (with-open [ds (get-connection db-spec)]
      (jdbc/execute! ds ["insert into t1(id, tx) values (1,1)"]))

    (with-open [ds (get-connection db-spec)]
      (jdbc/execute! ds ["select * from t1"])))

      ;(jdbc/execute! ds ...)
      ; (do-other-stuff ds args)
      ;(into [] (map :column) (jdbc/plan ds ...))))

  ,)

;;.
