(ns mlib.http.server
  (:require
    [clojure.spec.alpha     :as     s]
    [clojure.string         :refer  [blank?]]
    ;
    [ring.middleware.params           :refer  [wrap-params]]
    [ring.middleware.keyword-params   :refer  [wrap-keyword-params]]
    [ring.middleware.multipart-params :refer  [wrap-multipart-params]]
    [ring.adapter.jetty               :refer  [run-jetty]]
    ;
    [mlib.http.middleware             :refer  [wrap-cors wrap-json-params wrap-server-name]]))
;=

  ;; [ring.middleware.keyword-params :refer [wrap-keyword-params]]
  ;; [ring.middleware.nested-params :refer [wrap-nested-params]]
  ;; [ring.middleware.multipart-params :refer [wrap-multipart-params]]
  ;; [ring.middleware.params :refer [wrap-params]])
            
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
    (wrap-json-params)
    (wrap-keyword-params) 
    (wrap-multipart-params)
    (wrap-params) 
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


