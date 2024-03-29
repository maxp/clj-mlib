(ns mlib.psql.adapters
  (:import
    [java.sql             Array PreparedStatement]
    [org.postgresql.util  PGobject])
  (:require 
    [jsonista.core        :as    json]
    [next.jdbc.date-time  :refer [read-as-instant]]
    [next.jdbc.prepare    :refer [SettableParameter]]
    [next.jdbc.result-set :refer [ReadableColumn]]))
;=

(set! *warn-on-reflection* true)

;; java.time.Instant conversion
(read-as-instant)

;; https://cljdoc.org/d/seancorfield/next.jdbc/1.0.424/doc/getting-started/tips-tricks

(def mapper (json/object-mapper {:decode-key-fn keyword}))
(def ->json json/write-value-as-string)
(def <-json #(json/read-value % mapper))

(defn ->pgobject
  "Transforms Clojure data to a PGobject that contains the data as JSON. 
  PGObject type defaults to `jsonb` but can be changed via metadata key `:pgtype`"
  [x]
  (let [pgtype (or (:pgtype (meta x)) "jsonb")]
    (doto (PGobject.)
      (.setType pgtype)
      (.setValue (->json x)))))

(defn <-pgobject
  "Transform PGobject containing `json` or `jsonb` value to Clojure data."
  [^PGobject v]
  (let [type  (.getType v)
        value (.getValue v)]
    (if (#{"jsonb" "json"} type)
      (with-meta (<-json value) {:pgtype type})
      value)))
;;


;; if a SQL parameter is a Clojure hash map or vector, it'll be transformed
;; to a PGobject for JSON/JSONB:
(extend-protocol SettableParameter
  clojure.lang.IPersistentMap
  (set-parameter [m, ^PreparedStatement s, i]
    (.setObject s i (->pgobject m)))

  clojure.lang.IPersistentVector
  (set-parameter [v, ^PreparedStatement s, i]
    (.setObject s i (->pgobject v))))
;;

;; if a row contains a PGobject then we'll convert them to Clojure data
;; while reading (if column is either "json" or "jsonb" type):
(extend-protocol ReadableColumn
  PGobject
  (read-column-by-label [^PGobject v _]     (<-pgobject v))
  (read-column-by-index [^PGobject v _2 _3] (<-pgobject v)))
;;

;; sql Array -> Vector
;; 
(extend-protocol ReadableColumn
  Array
  (read-column-by-label [^Array v _]    (vec (.getArray v)))
  (read-column-by-index [^Array v _ _]  (vec (.getArray v))))
;;
