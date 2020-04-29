(ns mlib.logger.format
  (:import 
    [java.time        ZonedDateTime]
    [java.time.format DateTimeFormatter])
  (:require
    [clojure.string :as str]
    [jsonista.core :refer [object-mapper write-value-as-string]]))
;=

;; ;; ;; ;; ;; ;; ;; ;; ;; ;;

(def ^:const LEFT_PAD   " ")
(def ^:const DELIMITER  " ")

;; ;; ;; ;; ;; ;; ;; ;; ;; ;;

;; https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/time/format/DateTimeFormatter.html
;; 
(defn iso-timestamp []
  (.format DateTimeFormatter/ISO_LOCAL_DATE_TIME 
    (. ZonedDateTime now)))
;

(def mapper (object-mapper))

(defn to-json [data]
  (write-value-as-string data mapper))
;

(def ^:dynamic *trace-include* nil)
(def ^:dynamic *trace-exclude* [#"^clojure\..*"])

(defn re-include-exclude [s]
  (when   
    (or    (some #(re-matches % s) *trace-include*)
      (not (some #(re-matches % s) *trace-exclude*)))
    s))
;;

(defn format-trace [trace]
  ;; ^StackTraceElement[]
  (->> trace
    (map
      (fn [^StackTraceElement t]
        (str (.getClassName t) ":" (.getLineNumber t))))
    (filter re-include-exclude)))
;;

(defn format-entry-args [args]
  (->> args
    (map
      (fn [arg]
        (if (instance? Throwable arg)
          (str LEFT_PAD 
            "{\"exception\":\"" (class arg) "\", "
             "\"trace\":" (to-json (format-trace (.getStackTrace arg))) "}")
          (str LEFT_PAD (to-json arg)))))
    (str/join ",\n")))
;;

(defn format-entry [metainfo args]
  (let [level (:level metainfo)
        level (cond
                (keyword? level)  (name level)
                (string?  level)  level
                :else             "!!!!!")
        fra   (try 
                (format-entry-args args)
                (catch Exception _ignore
                  (str LEFT_PAD "\"Internal logger error!\"")))]

    (str     
      (iso-timestamp)           DELIMITER
      (str/upper-case level)    DELIMITER
      (:ns metainfo "???")      DELIMITER
      "(" (:line metainfo) ")"  DELIMITER
      "[\n" fra " ]\n")))
;;


(comment

  (try
    (throw 
      (ex-info "test exception" {:data [1 2 3]}))
    (catch Exception ex
      (binding [*trace-include* [#"^clojure.*"]]
        (format-entry {:ns *ns* :line 100 :level "debug"} [ex]))))
  
  ,)
