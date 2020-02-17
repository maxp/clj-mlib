(ns __root__.app.cfg
  (:require
    [clojure.spec.alpha   :as s]
    [mount.core   :refer  [defstate]]
    [mlib.config  :as     config]
    [mlib.util  :refer    [not-blank?]]))
;=

;; example config spec

(s/def ::port pos-int?)
(s/def ::host not-blank?)
(s/def ::url  not-blank?)

(s/def ::api 
  (s/keys
    :req-un [::host ::port]
    :opt-un [::url]))
;

(s/def ::conf
  (s/keys
    :req-un [::api]))
;=

;; ;; ;; ;; ;; ;; ;; ;; ;; ;;

(defstate conf
  :start
    (let [c (s/conform ::conf config/conf)]
      (when (= ::s/invalid c)
        (throw  
          (ex-info "invalid config" 
            (s/explain-data ::conf config/conf))))
      c))
;=

(defstate app
  :start
    (:api conf))
;=

;;.
