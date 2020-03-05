(ns mlib.snowflake
  (:import
    [java.security SecureRandom])
  (:require
    [criterium.core :refer [quick-bench]]))
;=


;; snowflake bits:
;; 1  - 0
;; 41 - timestamp
;; 10 - machine
;; 12 - sequence


(def ^:const SEQUENCE_MASK    0x0000000000000fff)    ;; 12 bits
(def ^:const MACHINE_MASK     0x00000000003ff000)    ;; 10 bits
(def ^:const TIMESTAMP_MASK   0x7fffffffffc00000)    ;; 41 bits
                             
(def ^:const MACHINE_SHIFT    12)
(def ^:const TIMESTAMP_SHIFT  22)

(def ^:const EPOCH            1577836800000)          ;;  2020-01-01 00:00:00.0000 Z


(defn- now-ms []
  (System/currentTimeMillis))
;

(defn- random16 []
  (let [buff  (byte-array 2)
        _     (.nextBytes (SecureRandom.) buff)]
    (+  
      (bit-shift-left
        (bit-and (aget buff 1) 0xff) 8)
      (bit-and (aget buff 0) 0xff))))
;

(defn make-state [ts rnd1 rnd2]
  { :last-ts  ts
    :seqn     (bit-and rnd1 SEQUENCE_MASK)
    :machine  (bit-and (bit-shift-left rnd2 MACHINE_SHIFT) MACHINE_MASK)})
;
;
(def ^:dynamic *state* 
  (atom (make-state (now-ms) (random16) (random16)))) 
;-


(defn next-id []
  (locking *state* nil))



;;,

(comment

  *state*

  (make-state 1 1 1)

  (format "%012x" (random16))

  (format "%016x"
    (bit-shift-left SEQUENCE_MASK 12))

  (quick-bench
    (Thread/sleep 0 100))

  (java.util.Date. (+ 100 20) 1 1)
  
  (.getTime (java.util.Date. 120 01 01))

  (java.time.ZonedDateTime/from 1577836800000)

  (->
    (java.time.ZonedDateTime/of 
      2020 1 1 
      0 0 0 
      0
      (java.time.ZoneId/of "UTC"))

    (.toInstant)
    (.toEpochMilli))

  ,)
