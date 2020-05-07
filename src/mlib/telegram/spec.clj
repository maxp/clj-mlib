(ns mlib.telegram.spec
  (:require
    [clojure.spec.alpha :as     s]
    [clojure.string     :refer  [blank?]]))
;=

;; ;; ;; ;; ;; ;; ;; ;; ;; ;;

(defn not-blank? [s]
  (and (string? s) (not (blank? s))))

(s/def  ::apikey    not-blank?)   ;; telegram api key
(s/def  ::botname   not-blank?)   ;; @username of the bot
(s/def  ::timeout   pos-int?)     ;; connection timeout
(s/def  ::longpoll  pos-int?)     ;; seconds to wait for updates
(s/def  ::retry     pos-int?)     ;; retry count

(s/def  ::host      not-blank?)   ;; http proxy host
(s/def  ::port      pos-int?)     ;; http proxy port
(s/def  ::proxy     
  (s/keys :req-un [::host ::port]))

(s/def  ::config
  (s/keys
    :req-un [::apikey]
    :opt-un [::botname ::timeout ::longpoll ::retry ::proxy]))
;-
