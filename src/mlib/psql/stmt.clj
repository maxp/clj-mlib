(ns mlib.psql.stmt
  (:require
    [clojure.string :as     str]
    [clojure.set    :refer  [difference]]))
;=

;; thanks for the regular expression idea -
;; https://github.com/tatut/jeesql/blob/master/src/jeesql/statement_parser.clj
;; 
(def ^{:doc 
        "Regular expression to split statement into three parts: before the first parameter,
        the parameter name and the rest of the statement. A parameter always starts with a single colon and
        may contain alphanumerics as well as '-', '_' and '?' characters."}
  RE_COLONPARAM #"(?s)(.*?[^:\\]):(\p{Alpha}[\p{Alnum}\_\-\?\./]*)(.*)")
;-

(defn- unescape-colon [string]
  (str/replace string #"\\:" ":"))
;-

(defn parse-colon-params [^String statement]
  (let [ sql-buffer  (StringBuilder.)
         colon-names (transient [])]
    (loop [rest-stmt statement]
      (let [[match before param-name after] (re-find RE_COLONPARAM rest-stmt)]
        (if-not match
          (do
            (.append sql-buffer (unescape-colon rest-stmt))
            ;; return
            { :sql-text    (.toString sql-buffer)
              :colon-names (persistent! colon-names)})
          ;
          (do
            (conj! colon-names (keyword param-name))
            (doto sql-buffer
              (.append (unescape-colon before))
              (.append "?"))
            (recur after)))))))
;;

(defn check-undefined-params [colon-names params]
  (seq
    (difference 
      (set colon-names) 
      (set (keys params)))))
;;

(defn sql-exec-list [sqltext+names params]
  (let [colon-names (:colon-names sqltext+names)]
    (when-let [missing (check-undefined-params colon-names params)]
      (throw (ex-info (str "undefined exec parameters - " missing) {:keys missing})))
    ;
    (cons
      (:sql-text sqltext+names)
      (map #(get params %) colon-names))))
;;


(comment

  (parse-colon-params "select *\n from tbl\n where a = :a AND :q? or :a AND :with-dash")
  ;; => {:sql-text "select *\n from tbl\n where a = ? AND ? or ? AND ?", :colon-names [:a :q? :a :with-dash]}


  (let [st "select *\n from tbl\n where a = :a AND :q? or :a AND :with-dash"
        par {:a 1 :q? true :with-dash "dash"}
        cp (parse-colon-params st)]
    (sql-exec-list cp par))
    ;; => ("select *\n from tbl\n where a = ? AND ? or ? AND ?" 1 true 1 "dash")
  
  ,)


