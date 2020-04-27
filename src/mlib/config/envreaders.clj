(ns mlib.config.envreaders
  (:require
    [clojure.string     :refer  [blank? split lower-case]]
    [clojure.spec.alpha :as     s]))
;=

(s/def  ::env-name  (s/and string? (complement blank?)))

(defn env-val [env s]
  {:pre [(s/valid? ::env-name s)]}
  (let [[name default] (split s #":" 2)
        val (get env name)]
    (if (nil? val) default val)))
;=

(defn to-bool [s]
  (when s
    (condp = (lower-case s)
      "true"  true
      "false" false
      nil)))
;-

(defn to-int [s]
  (when s
    (try
      (Long/parseLong s)
      (catch Exception _ignore))))
;-

(defn to-key [s]
  (when s 
    (keyword s)))
;-

(defn readers [env]
  { 'envbool  #(to-bool (env-val env %))
    'envint   #(to-int  (env-val env %))
    'envkey   #(to-key  (env-val env %))
    'envstr   #(env-val env %)})
;;

;; ;; ;; ;; ;; ;; ;; ;; ;; ;;

(comment

  (readers (System/getenv))

  (let [env {"A" "aaa" "BB" "bb"}]
    (env-val env "A")       ;; => "aaa"
    (env-val env "BB")      ;; => "bb"
    (env-val env "C:C_DEF")) ;; => "C_DEF"
    ;
    ;(env-val env :a))       ;; => Exception

  (to-bool "True") ;; => true
  (to-bool "qwe") ;; => nil
  (to-int "") ;; => nil

  ,)

