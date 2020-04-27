(ns util
  (:import
    [java.time        LocalDateTime]
    [java.time.format DateTimeFormatter])
  (:require
    [clojure.string     :refer  [trim trim-newline]]
    [clojure.java.shell :refer  [sh]]
    [clojure.java.io    :refer  [resource]]
    [clojure.tools.namespace.repl :as tnr]    
    [mount.core :as mount]
    [mlib.config.core :refer [edn-slurp]]))
    ;
;=

(defn get-commit-hash []
  (->
    (sh "git" "rev-parse" "HEAD")
    (:out)
    (trim-newline)
    (trim)))
;;

(defn iso-datetime [& [dt]]
  (.format DateTimeFormatter/ISO_LOCAL_DATE_TIME 
    (or dt (LocalDateTime/now))))
;;

(defn configs []
  (let [env (System/getenv)]
    [(-> "config.edn" resource (edn-slurp env))
     (->  "../conf/dev.edn" (edn-slurp env))
     { :build
       {:commit    (get-commit-hash)
        :timestamp (iso-datetime)}}]))
;;

(defn restart []
  (mount/stop)
  (mount/start-with-args (configs)))
;

(defn start []
  (prn "dev/start with configs")
  (mount/start-with-args (configs)))
;

(defn stop []
  (prn "dev/stop")
  (mount/stop))
;;

(defn reset []
  (tnr/refresh :after 'util/restart))
;;
