(ns mlib.logger
  (:import 
    [java.time ZonedDateTime]
    [java.time.format DateTimeFormatter]))
;    

;; TODO: configurable formatter, appenders

(defn timestamp []
  ;; https://docs.oracle.com/javase/8/docs/api/java/time/format/DateTimeFormatter.html
  (.format
    DateTimeFormatter/ISO_LOCAL_DATE_TIME
    (. ZonedDateTime now)))
;

(defn format-metainfo [mi]
  (str (.toUpperCase (name (:level mi))) ":" (:ns mi) "." (:line mi)))
;

(defn format-item [metainfo & args]
  (str 
    (timestamp) 
    " " 
    (format-metainfo metainfo) 
    " " 
    (apply pr-str args)))
;

(defn stdout-handler [logger & args]
  (.println System/out (apply format-item logger args)))  

(defn stderr-handler [logger & args]
  (.println System/err (apply format-item logger args)))  
    
(def level-handler-map 
  {
    :debug stdout-handler
    :info  stdout-handler
    :warn  stderr-handler
    :error stderr-handler})
;

(defn get-handler [metainfo]
  (or 
    (get level-handler-map (:level metainfo))
    (get level-handler-map :error)))
;

(defn log! 
  "metainfo is a map {:level log_level  :ns namespace_string  :line file_line}"
  [metainfo & args]
  (apply (get-handler metainfo) metainfo args))
;

(defmacro debug [& args]
  (let [metainfo {:level :debug :ns (str *ns*) :line (:line (meta &form))}]
    `(log! ~metainfo ~@args)))
;

(defmacro info [& args]
  (let [metainfo {:level :info :ns (str *ns*) :line (:line (meta &form))}]
    `(log! ~metainfo ~@args)))
;

(defmacro warn [& args]
  (let [metainfo {:level :warn :ns (str *ns*) :line (:line (meta &form))}]
    `(log! ~metainfo ~@args)))
;

(defmacro error [& args]
  (let [metainfo {:level :error :ns (str *ns*) :line (:line (meta &form))}]
    `(log! ~metainfo ~@args)))
;

(comment

  (debug "message" {:qwe 123})
  ;; => 2019-03-17T12:13:23.793 DEBUG:mlib.logger.2 message {:qwe 123}

  .)

;;.
