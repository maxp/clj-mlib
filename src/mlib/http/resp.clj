(ns mlib.http.resp
  (:require
    [taoensso.timbre  :refer  [warn]]
    [jsonista.core    :refer  [write-value-as-string keyword-keys-object-mapper]]))
;=

(def ^:const ERROR_RESPONSE_STATUS 200)

(defn ok [body]
  {:status 200
   :body body})
;;

(defn bad [body]
  {:status 400
   :body body})
;;

(defn err [body]
  {:status 500
   :body body})
;;

(defn json [status data]  
  { :status   status
    :headers  {"Content-Type" "application/json;charset=utf-8"}
    :body     (write-value-as-string data keyword-keys-object-mapper)})
;;

(defn jsok [data]
  (json 200 data))
;;

(defn jserr [error-data]
  (json 400 error-data))
;;

;; reitit middleware related functions
;; 

(defn throw-response! [message response]
  (throw
    (ex-info message
      { :type :reitit.ring/response
        :response response})))
;;

(defn throw-error! [message data]
  (let [err (if (:message data) data (assoc data :message message))]
    (throw-response! message
      { :status ERROR_RESPONSE_STATUS
        :body {:error err}})))
;;
