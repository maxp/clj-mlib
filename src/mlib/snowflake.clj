(ns mlib.snowflake
  (:import
    [java.security SecureRandom]))
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

(def ^:const EPOCH            1577836800000)         ;;  2020-01-01 00:00:00.0000 Z

;; ;; ;; ;; ;; ;; ;; ;; ;; ;;

(defn- random16 []
  (let [buff  (byte-array 2)]
    (.nextBytes (SecureRandom.) buff)
    (bit-or
      (bit-shift-left
        (bit-and (aget buff 1) 0xff) 8)
      (bit-and (aget buff 0) 0xff))))
;

(defn- next-tick [last-ts]
  (loop [n 20]
    (Thread/sleep 0 100)
    (let [t (System/currentTimeMillis)]
      (if (= t last-ts)
        (when (> n 0)
          (recur (dec n)))
        t))))
;

(defn make-state [ts machine]
  { :last-ts  ts
    :seqn     0
    :machine  (bit-and (bit-shift-left machine MACHINE_SHIFT) MACHINE_MASK)})
;

(def ^:dynamic *state* 
  (atom (make-state (System/currentTimeMillis) (random16)))) 
;-

(defn set-machine! [num]
  (swap! *state* 
    #(assoc % 
        :machine 
        (bit-and (bit-shift-left num MACHINE_SHIFT) MACHINE_MASK))))
;;

(defn- combine-bits [^long ts ^long machine ^long seqn]
  (swap! *state* #(assoc % :last-ts ts :seqn seqn))
  (bit-or
    (bit-and TIMESTAMP_MASK
      (bit-shift-left (- ts EPOCH) TIMESTAMP_SHIFT))
    (bit-or machine seqn)))
;

(defn next-id ^long []
  (locking *state*
    (let [{:keys [last-ts machine seqn]} @*state*
          ts  (System/currentTimeMillis)]
      ;
      (when (< ts last-ts)
        (throw (ex-info "clock moved backwards" {})))
      ;
      (if (> ts last-ts)
        (combine-bits ts machine 0)
        (let [seqn  (bit-and (inc seqn) SEQUENCE_MASK)]
          (if (not= 0 seqn)
            (combine-bits ts machine seqn)
            (let [ts (next-tick last-ts)]
              ;
              (when-not ts
                (throw (ex-info "clock advance failed" {})))
              ;
              (combine-bits ts machine 0))))))))
;;

(defn ts->id ^long [^long ts]
  (when (< ts EPOCH)
    (throw (IllegalArgumentException. (str "ts must be grater than EPOCH: " EPOCH))))
  (combine-bits ts 0 0))
;;

(defn id->ts [^long id]
  (+ EPOCH
    (bit-shift-right
      (bit-and id TIMESTAMP_MASK)
      TIMESTAMP_SHIFT)))
;;

;;,

(comment


  (id->ts 1111)
  ;; => 1577836800000

  (ts->id 1577836800001)
  ;; => 4194304

  
  (require '[criterium.core :refer [quick-bench]])

  (next-id)
  ;; => 111403924373422080


  (time
    (count (set (repeatedly 10000 next-id))))

  (time
    (dotimes [_ 10000]
      (next-id)))
    
  (format "%08x" (next-id))

  (-
    (id-ts (next-id))
    (System/currentTimeMillis))

  (quick-bench (next-id))

  *state*
  
  ,)
