
(ns util
  (:import
    [java.time.format DateTimeFormatter])
  (:require
    [clojure.string :as str]
    [clojure.java.shell :refer [sh]]
    [java-time :as time]
    [mount.core :as mount]
    ;
    [mlib.config :refer [conf]]
    [mlib.util :refer [edn-read edn-resource]]))
    ;
;=

(defn get-commit-hash []
  (->
    (sh "git" "rev-parse" "HEAD")
    (:out)
    (str/trim-newline)
    (str/trim)))
;;

(defn iso-datetime [& [dt]]
  (time/format DateTimeFormatter/ISO_LOCAL_DATE_TIME 
    (or dt (time/local-date-time))))
;;

(defn configs []
  [ (edn-resource "config.edn")
    (edn-read "../conf/dev.edn")
    {:build
      { :commit    (get-commit-hash)
        :timestamp (iso-datetime)}}])
;

(time/format DateTimeFormatter/ISO_LOCAL_DATE_TIME (time/local-date-time))

(defn start-conf[]
  (mount/stop)
  (->
    (mount/only [#'conf])
    (mount/with-args (configs))
    (mount/start)))
;

(defn restart []
  (mount/stop)
  (mount/start-with-args
    (configs)))
;

(defn start []
  (prn "dev/start with configs")
  (mount/start-with-args (configs)))
;

(defn stop []
  (prn "dev/stop")
  (mount/stop))
;


(comment

  (restart)  

  (start-conf)

  .)
;

;;.
