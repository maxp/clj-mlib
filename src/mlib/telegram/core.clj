(ns mlib.telegram.core
  (:import
    [java.util.concurrent.atomic  AtomicLong]
    [java.io                      IOException])
  (:require
    [clojure.spec.alpha :as     s]
    [clojure.string     :refer  [escape]]
    [clj-http.client    :as     http]
    [jsonista.core      :as     json]
    [mlib.telegram.spec :as     spc]))
;=

;; ;; ;; ;; ;; ;; ;; ;; ;; ;;
;; 
(def ^:const RETRY_COUNT      5)
(def ^:const SOCKET_TIMEOUT   8000)
(def ^:const LONGPOLL         8)   ;; telegram api parameter: 8 seconds

; (def SOCKET_ERR_DELAY 1000)

(def ^:const E_RETRY_LIMIT   ::E_RETRY_LIMIT)

(def ^:const API_CALL_INTERVAL_MS 20)   ;; 30 rps - connection time

;; ;; ;; ;; ;; ;; ;; ;; ;; ;;
 
(def last-api-call-timestamp (AtomicLong. 0))

(defn throttle [delay-max]
  (let [last  (.get last-api-call-timestamp)
        now   (System/currentTimeMillis)
        delay (- (+ last delay-max) now)]
    (when (< 0 delay)
      (Thread/sleep (min delay delay-max)))
    (.set last-api-call-timestamp (System/currentTimeMillis))
    delay))
;;

(comment

  (for [t [0 10 20 30 40 50 22 10]]
    (do
      (Thread/sleep t)
      (throttle API_CALL_INTERVAL_MS)))

  ,)

;; ;; ;; ;; ;; ;; ;; ;; ;; ;;


(defonce ^:dynamic *telegram-opts* nil)

(defn set-opts! [opts]
  (if (s/valid? ::spc/config opts)
    (alter-var-root #'*telegram-opts* (constantly opts))
    (throw 
      (ex-info "incorrect telegram-opts" 
        (s/explain-data ::spc/config opts)))))
;;

;; ;; ;; ;; ;; ;; ;; ;; ;; ;;

(def ^:const TELEGRAM_API "https://api.telegram.org")

(defn api-url [^String method]
  (str TELEGRAM_API "/bot" (:apikey *telegram-opts*) "/" method))
;

(defn file-url [^String path]
  (str TELEGRAM_API "/file/bot" (:apikey *telegram-opts*) "/" path))
;

(defn hesc [text]
  (when (string? text)
    (escape text {\& "&amp;" \< "&lt;" \> "&gt;" \" "&quot;"})))
;

;; ;; ;; ;; ;; ;; ;; ;; ;; ;;

(def json-read-keyword-mapper
  (json/object-mapper {:decode-key-fn true}))

(defn parse-json-string [^String s]
  (json/read-value s json-read-keyword-mapper))
;;

(defn json-body [resp]
  (let [ctype (get-in resp [:headers "content-type"])
        body  (:body resp)]
    (if (and (string? body) (string? ctype) (.startsWith ctype "application/json"))
      (assoc resp :body (parse-json-string body))
      resp)))
;;

;; ;; ;; ;; ;; ;; ;; ;; ;; ;;

(defn api-call [method params]
  (let [timeout (:timeout *telegram-opts* SOCKET_TIMEOUT)
        proxy   (:proxy *telegram-opts*)
        url   (api-url method)
        body  (json/write-value-as-bytes params)
        data  {:content-type      :json
               :body              body
               :throw-exceptions  false
               :socket-timeout    timeout
               :conn-timeout      timeout
               :proxy-host        (:host proxy)
               :proxy-port        (:port proxy)}]
    ;;
    (try
      (->
        (http/post url data)
        (json-body))
      (catch IOException ex
        (throw 
          (ex-info "telegram api-call" 
            {:url TELEGRAM_API :proxy proxy :message (.getMessage ex)} ex))))))
;;

(defn api [^String method params]
  (throttle API_CALL_INTERVAL_MS)
  (loop [retry (:retry *telegram-opts* RETRY_COUNT)]
    (let [res
          (let [{:keys [status body]} (api-call method params)]
            (if (= 200 status)
              (:result body)
              (if (-> body (:error_code) (str) (first) #{\3 \5}) ;; 3xx or 5xx codes
                ::retry
                (throw (ex-info "telegram.api/call"
                          (assoc body :status status))))))]
          ;
      (if (and (= ::retry res) (< 0 retry))
        (recur (dec retry))
        res))))
;;

(defn get-updates [offset limit]
  (let [longpoll (:longpoll *telegram-opts* LONGPOLL)
        timeout  (:timeout  *telegram-opts* SOCKET_TIMEOUT)]
    (binding [*telegram-opts* 
              (assoc *telegram-opts* :timeout (+ timeout (* 1000 longpoll)))]
      (api "getUpdates" 
        {:offset offset :limit limit :timeout longpoll})))) 
;;

;; ;; ;; ;; ;; ;; ;; ;; ;; ;;

(defn send-text [chat-id text]
  (api "sendMessage" {:chat_id chat-id :text text :parse_mode "HTML"}))
;

(defn send-message [chat-id params]
  (api "sendMessage" (assoc params :chat_id chat-id)))
;

(defn file-info 
  "{:file_id \"...\" :file_size 999 :file_path \"dir/file.ext\""
  [file-id]
  (api "getFile" {:file_id file-id}))
;

(defn file-fetch [file-id]
  (let [;
        {file-path :file_path :as rc} (file-info file-id)
        ;
        url       (file-url file-path)
        timeout   (:timeout *telegram-opts* SOCKET_TIMEOUT)
        proxy     (:proxy *telegram-opts*)
        data      { :as               :byte-array
                    :socket-timeout   timeout
                    :conn-timeout     timeout
                    :proxy-host       (:host proxy)
                    :proxy-port       (:port proxy)
                    :throw-exceptions false}
        ;
        {status :status body :body} 
        (http/get url data)]
    ;
    (if (= 200 status)
      (assoc rc :body body)
      (throw (ex-info "http/get failed"
                {:status status :body body})))))
;;

; (defn send-file
;   "params should be stringable (json/generate-string)
;     or File/InputStream/byte-array"
;   [method mpart & [{timeout :timeout :as opts}]]
;   (try
;     (let [tout (or timeout SOCKET_TIMEOUT)
;           res (:body
;                 (http/post (api-url (:apikey opts) method)
;                   { :multipart
;                       (for [[k v] mpart]
;                         {:name (name k) :content v :encoding "utf-8"})
;                     :as :json
;                     :throw-exceptions false
;                     :socket-timeout tout
;                     :conn-timeout tout}))]
;           ;
;       (if (:ok res)
;         (:result res)
;         (info "send-file:" method res)))
;     (catch Exception e
;       (warn "send-file:" method (.getMessage e)))))
; ;


; (defn set-webhook-cert [url cert-file]
;   (http/post (api-url apikey "setWebhook")
;     {:multipart [ {:name "url" :content url}
;                   {:name "certificate" :content cert-file}]}))
; ;

