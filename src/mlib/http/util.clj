(ns mlib.http.util
  (:require
    [clojure.string   :refer  [split blank? lower-case]]
    [clojure.java.io  :refer  [input-stream]]
    [jsonista.core    :refer  
      [object-mapper read-value write-value-as-bytes write-value-as-string]])) 
;=

;; NOTE: deprecated
(defn authorization-split [req]
  (when-let [auth (get-in req [:headers "authorization"])]
    (when (string? auth)
      (split auth #"\s+" 2))))
;;

(comment

  (authorization-split
    {:headers {"authorization" "Bearer 123456 789"}}) ;; => ["Bearer" "123456 789"]

  ,)

;; ;; ;; ;; ;; ;; ;; ;; ;; ;;

(defn- split-pair [s]
  (let [[k v] (split s #"\s+" 2)]
    (when (and k (not (blank? k)))
      [ (keyword (lower-case k))
        (or v "")])))
;-
      
(defn parse-request-authorization [req]
  (when-let [auth (get-in req [:headers "authorization"])]
    (cond
      (string? auth)  
      (when-let [pair (split-pair auth)]
        [pair])
      ;
      (vector? auth)  
      (reduce
        (fn [acc s]
          (if-let [pair (split-pair s)]
            (conj acc pair)
            acc))
        []
        auth)
      ;
      :else nil)))
;=

(comment

  (into {}
    (parse-request-authorization
      {:headers {"authorization" ["Bearer qwe123" "basic  123" "Session sid.key" "AWS"]}}))

  (into {}
    (parse-request-authorization
      {:headers {"authorization" "Bearer qwe123"}}))

  ,)

;; ;; ;; ;; ;; ;; ;; ;; ;; ;;

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

(defn edn-response [data]
  { :status   200
    :headers  {"Content-Type" "application/edn"}
    :body     (pr-str data)})
;;


(comment

  (->
    (json-response {:a {"B" [true]}})
    (:body)
    (parse-json-value))

  (edn-response {:a nil :b [1 2 3]})

  ,)

;;
