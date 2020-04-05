(ns mlib.http.server
  (:require
    [clojure.spec.alpha     :as     s]
    [clojure.string         :refer  [blank?]]
    ;
    [ring.adapter.jetty     :refer  [run-jetty]]
    ;
    [mlib.http.middleware   :refer  [wrap-cors wrap-server-name]]))
;=

(defn not-blank? [s]
  (and (string? s) (not (blank? s))))

(s/def ::host         not-blank?)
(s/def ::port         pos?)
(s/def ::server-name  not-blank?)

(s/def ::server-options
  (s/keys :req-un [::host ::port ::server-name]))
;=

;; ;; ;; ;; ;; ;; ;; ;; ;; ;;

(defn start-server [handler options]
  {:pre
   [(s/valid? fn? handler)
    (s/valid? ::server-options options)]}
  ;
  (->
    handler
    (wrap-server-name (:server-name options))
   
    (wrap-cors)
    (run-jetty 
      (assoc options 
        :join? false
        :send-date-header? false
        :send-server-version? false))))
;;

(defn stop-server [server]
  (.stop server))
;;


