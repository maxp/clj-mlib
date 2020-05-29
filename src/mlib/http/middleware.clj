(ns mlib.http.middleware
  (:import
    [java.io PushbackReader InputStreamReader]
    [com.fasterxml.jackson.core JsonParseException])
  (:require
    [clojure.edn      :as     edn]
    [mlib.http.util   :refer  [parse-json-value]]))
;=

(def CORS_HEADERS
  { "Access-Control-Allow-Origin"   "*"
    "Access-Control-Allow-Methods"  "GET, POST, OPTIONS"
    "Access-Control-Allow-Headers"  "Content-Type, Authorization"
    "Access-Control-Expose-Headers" "X-ServerTime, X-ServerName, *"})
;-

(defn wrap-cors [handler]
  (fn [req]
    (if (= :options (:request-method req))
      {:status 200 :headers CORS_HEADERS :body ""}
      (->
        (handler req)
        (update :headers merge CORS_HEADERS)))))
;;

(defn wrap-server-name [handler server-name]
  (fn [req]
    (-> 
      (handler req)
      (update :headers merge
        { "X-ServerName"  server-name
          "X-ServerTime"  (str (System/currentTimeMillis))}))))
;;

;; ;; ;; ;; ;; ;; ;; ;; ;; ;;

(def ^:dynamic 
  *malformed-json-response*
  {:status  400
   :headers {"Content-Type" "text/plain"}
   :body    "Malformed JSON request."})
;-

(def RE_APPLICATION_JSON #"^application/(.+?\+)?json")

(defn- json-request? [request]
  (when-let [ctype (get-in request [:headers "content-type"])]
    (boolean (re-find RE_APPLICATION_JSON ctype))))
;-

(defn- parse-json-data [body]
  (try
    [true (parse-json-value body)]
    (catch JsonParseException _ex
      [false nil])))
;-

(defn- merge-json-params [req data]
  (let [request (assoc req :json-params data)]
    (if (map? data)
      (update-in request [:params] merge data)
      request)))
;-

(defn wrap-json-params [handler]
  (fn [req]
    (if (json-request? req)
      (let [[valid? data] (parse-json-data (:body req))]
        (if valid?
          (handler (merge-json-params req data))
          *malformed-json-response*))
      (handler req))))
;;

(comment
  (json-request? {:headers {"content-type" "application/vnd+json"} :body "[true]"})
  (parse-json-value "null")

  ( 
    (wrap-json-params :params)
    { :headers {"content-type" "application/json; charset=utf-8"}
      :body "{\"a\":[true],\"b\":1}"})
  
  ,)

;; ;; ;; ;; ;; ;; ;; ;; ;; ;;

(def ^:dynamic *edn-readers* nil)

(def ^:dynamic *malformed-edn-response*
  {:status  400
   :headers {"Content-Type" "text/plain"}
   :body    "Malformed EDN request."})
;-

(defn- edn-request? [request]
  (when-let [ctype (get-in request [:headers "content-type"])]
    (boolean (re-find #"^application/(.+?\+)?edn" ctype))))
;-

(defn- parse-edn-data [body]
  (try
    [ true
      (edn/read
        {:eof nil :readers *edn-readers*}
        (PushbackReader. (InputStreamReader. body "UTF-8")))]  
    (catch Exception _ex
      [false nil])))
;-

(defn- merge-edn-params [req data]
  (let [request (assoc req :edn-params data)]
    (if (map? data)
      (update-in request [:params] merge data)
      request)))
;-

(defn wrap-edn-params [handler]
  (fn [req]
    (if (edn-request? req)
      (let [[valid? data] (parse-edn-data (:body req))]
        (if valid?
          (handler (merge-edn-params req data))
          *malformed-edn-response*))
      (handler req))))
;;

(comment

  (import '[java.io ByteArrayInputStream])
  
  (def edn-body 
    (.getBytes "{:a :b :inst #inst \"2020-01-01T10:10:10\"}"))

  (parse-edn-data 
    (ByteArrayInputStream. edn-body))

  ((wrap-edn-params :params)
   {:headers {"content-type" "application/edn"}
    :body (ByteArrayInputStream. edn-body)})

  ,)
