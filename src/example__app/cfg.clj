(ns example__app.cfg
  (:require
    [clojure.spec.alpha   :as s]
    [mount.core   :refer  [defstate args]]
    [medley.core  :refer  [deep-merge]]
    [mlib.util    :refer  [not-blank?]]))
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

(s/def ::example__app
  (s/keys
    :req-un [::api]))
;=

(s/def ::conf
  (s/keys
    :req-un [::example_app]))
;=

;; ;; ;; ;; ;; ;; ;; ;; ;; ;;

(defstate conf
  :start
    (let [d (apply deep-merge (args))
          c (s/conform ::conf d)]
      (if (= ::s/invalid c)
        (throw  
          (ex-info "invalid config" 
            {:problems (::s/problems (s/explain-data ::conf d))}))
        c)))
;=

(defstate app
  :start
    (:example__app conf))
;=
