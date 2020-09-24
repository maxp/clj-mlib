(ns mlib.http.util
  (:require
    [clojure.string   :refer  [split blank? lower-case]]
    [jsonista.core    :as     json]))
;=

; - - - - - - - - - - - - - - - - - - -

(def RE_APPLICATION_JSON  #"^application/(.+?\+)?json")

(defn json-content-type? [{headers :headers}]
  (when-let [ctype
             (or
              (get headers :content-type)
              (get headers "content-type"))]
    (and
     (string? ctype)
     (boolean (re-find RE_APPLICATION_JSON ctype)))))
;-

(defn parse-json-body [resp]
  (when (json-content-type? resp)
    (json/read-value (:body resp) json/keyword-keys-object-mapper)))
;;

(defn json-string [obj]
  (json/write-value-as-string obj json/keyword-keys-object-mapper))
;;

; - - - - - - - - - - - - - - - - - - -

;; DEPRECATED

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

; - - - - - - - - - - - - - - - - - - -

(defn x-real-ip [req]
  (let [headers (get req :headers)]
    (or
      (get headers :x-real-ip)
      (get headers "x-real-ip")
      (get headers :x-forwarded-for)
      (get headers "x-forwarded-for")
      (get req     :remote-addr))))
;;

;; ;; ;; ;; ;; ;; ;; ;; ;; ;;

;; (defn parse-json-value [body]
;;   (json/read-value body json/keyword-keys-object-mapper))
;; ;;

(defn json-response [data]
  { :status   200
    :headers  {"Content-Type" "application/json;charset=utf-8"}
    :body     (json-string data)})
;;

(defn edn-response [data]
  { :status   200
    :headers  {"Content-Type" "application/edn"}
    :body     (pr-str data)})
;;


(comment

  (json-response {:a {"B" [true]}})
  (edn-response {:a nil :b [1 2 3]})

  (require '[criterium.core :refer [quick-bench]])

  (let [data {:a [1 2 3 4] :b "qweqweqweqweqw" :c "123123123123"}]
    (quick-bench    ;; <- this one is better
      (json/write-value-as-string data json/keyword-keys-object-mapper))
    (quick-bench
      (json/write-value-as-bytes data json/keyword-keys-object-mapper)))

  ,)

;;
