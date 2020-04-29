(ns mlib.logger.core
  (:require
    [mlib.logger.format :refer [format-entry]]))
;=

(defn append-entry [metainfo args]
  (let [s (format-entry metainfo args)]
    (locking *out*
      (.write *out* s)
      (.flush *out*))))
;;

(defn log!
  "metainfo is a map {:level log_level  :ns namespace_string  :line file_line}"
  [metainfo & args]
  (append-entry metainfo args))
;;

(defmacro debug [& args]
  (let [metainfo {:level :debug :ns (str *ns*) :line (:line (meta &form))}]
    `(log! ~metainfo ~@args)))
;;

(defmacro info [& args]
  (let [metainfo {:level :info :ns (str *ns*) :line (:line (meta &form))}]
    `(log! ~metainfo ~@args)))
;;

(defmacro warn [& args]
  (let [metainfo {:level :warn :ns (str *ns*) :line (:line (meta &form))}]
    `(log! ~metainfo ~@args)))
;;

(defmacro error [& args]
  (let [metainfo {:level :error :ns (str *ns*) :line (:line (meta &form))}]
    `(log! ~metainfo ~@args)))
;;

(comment

  (debug "message" {:a :b} (ex-info "exinfo" {:c :d}))

  ,)
