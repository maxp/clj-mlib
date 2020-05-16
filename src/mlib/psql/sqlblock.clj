(ns mlib.psql.sqlblock
  (:require
    [clojure.string :as str]))
;=


(def RE_DOLLAR_NAME #"(\$\{(.*?)\})")

(defn dollar-subst [line subst-map]
  (str/replace line RE_DOLLAR_NAME
    (fn [[_ _ name]]
      (if-let [s (get subst-map (keyword name))]
        s
        (throw 
          (ex-info (str "dollar-subst - unexpected ${name}: " name) 
            {:name name}))))))
;;

(comment

  (dollar-subst "some text ${A}-${B} ${}" {:A "AaA" :B "bbb ${qqq}" (keyword "") "empty"})
  ;; => "some text AaA-bbb ${qqq} empty"

  (dollar-subst "some text ${bad}" {:A "AaA" :B "bbb ${qqq}"})
  ;; => exception: "dollar-subst - unexpected ${name}: bad"
  
  ,)


(def RE_SQL_COMMENT #"^\s*--")

(def RE_SQL_BLOCK #"(?i)^\s*--\s*@block:\s*(\S+).*$")

(defn- sql-block-name [line]
  (when-let [groups (re-matches RE_SQL_BLOCK line)]
    (second groups)))
;;

(comment

  (sql-block-name "  -- @block: name")  ;; => "name"
  (sql-block-name "--@block:Name qwe")  ;; => "Name"
  (sql-block-name "-- @ block: name")   ;; => nil
  (sql-block-name "-- @block:")         ;; => nil

  ,)

(defn split-blocks [lines]
  (let [blocks (transient [])
        res
        (reduce  
          (fn [acc line]
            (let [bn (sql-block-name line)]
              (if bn
                (do
                  (conj! blocks 
                    [ (:name acc) 
                      (persistent! (:lines acc))])
                  (assoc acc :name bn :lines (transient [])))                ;
                (do
                  (when-not (or (str/blank? line) (re-find RE_SQL_COMMENT line))
                    (conj! (:lines acc) line));))
                  acc))))
          ;;
          {:name "" :lines (transient [])}
          lines)]
      ;;
      (persistent!
        (conj! blocks 
          [ (:name res)
            (persistent! (:lines res))]))))
;;


(comment
  
  ;; (line-seq (java.io.BufferedReader. *in*))
  
  (->> "./src/docstor/sql/tables.sql"
    (slurp)
    (str/split-lines)
    (split-blocks))

  ,)
