;;
;;  mlib: configuration handling
;;

(ns mlib.config
  (:require
    [clojure.walk :refer  [postwalk]]
    [medley.core  :refer  [deep-merge]]
    [mount.core   :refer  [defstate args]]))
;=

(defn $subst 
  "substitute string parameters like \"${VAR}\" or \"${VAR:default}\" by os environment variables,
   only the whole parameter value could be replaced with the value of environment variable
  "
  [parameter subst-map]
  (if (string? parameter)
    (let [  [_ var _ default] 
            (re-matches #"^\S\{([^:]+)(\:(.*))?\}" parameter)]
      (if var
        (or        
          (get subst-map var) default "")
        parameter))
    parameter))
;

(comment
  (def subst 
    { "VAR1" "qwe123" 
      "VAR2" nil
      "EMPTY" ""})

  ($subst "${VAR1}" subst)          ;; "qwe123"
  ($subst "${VAR2}" subst)          ;; ""
  ($subst "${VAR3}" subst)          ;; ""
  ($subst "${VAR3:default3}" subst) ;; default3
  ($subst "${EMPTY}" subst)         ;; ""

  ($subst 123 subst)   ;; 123 
  ($subst :a subst)    ;; :a

  .)
;

(defstate conf
  :start
    (->>
      (apply deep-merge (args))
      (postwalk 
        #($subst %1 (System/getenv)))))
;

;;.
