(ns mlib.http.util
  (:require
    [clojure.string   :refer  [split]]
    [clojure.java.io  :refer  [input-stream]]
    [jsonista.core    :refer  
      [object-mapper read-value write-value-as-bytes write-value-as-string]])) 
;=

(defn authorization-split [req]
  (when-let [auth (get-in req [:headers "authorization"])]
    (when (string? auth)
      (split auth #"\s+" 2))))
;;

(comment

  (authorization-split
    {:headers {"authorization" "Bearer 123456 789"}}) ;; => ["Bearer" "123456 789"]

  ,)

;; 

(defn x-real-ip [req]
  (let [headers (get req :headers)]
    (or
     (get headers "x-real-ip")
     (get headers "x-forwarded-for")
     (get req     :remote-addr))))
;;

;; ;; ;; ;; ;; ;; ;; ;; ;; ;;

(def json-mapper 
  (object-mapper {:decode-key-fn true}))
;;

(defn parse-json-value [body]
  (read-value body json-mapper))
;;

(defn json-response [data]
  { :status   200
    :headers  {"Content-Type" "application/json; charset=utf-8"}
    :body     (-> data write-value-as-bytes input-stream)})
;;

(defn json-string [data]
  (write-value-as-string data json-mapper))
;;

(comment

  (->
    (json-response {:a {"B" [true]}})
    (:body)
    (parse-json-value))

  ,)

;;
