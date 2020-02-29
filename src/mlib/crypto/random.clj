(ns mlib.crypto.random
  (:import
    [java.math BigInteger]
    [java.security SecureRandom]))
;=


(def ^:dynamic *random* (SecureRandom.))


(defn random-bytes [^:long n]
  (let [b (byte-array n)]
    (.nextBytes *random* b)
    b))
;;

(defn urand32 ^:long []
  (reduce
    #(+' (*' 256 %1) (bit-and 255 %2))
    0
    (random-bytes 4)))
;;

(defn big-rand ^:BigInteger []
  (->>
    (random-bytes 8)
    (BigInteger. 1)))
;;

(defn predefined-next-bytes 
  "make not random bytes sequence"
  [^bytes data]
  (let [byte-seq* (atom (cycle data))
        next-byte #(ffirst (swap-vals! byte-seq* next))]
    (proxy
      [java.util.Random] []
      (nextBytes [^bytes buff]
        (doseq [i (range (alength buff))]
          (aset buff i (next-byte)))))))
;;
      

(comment

  ;; how to make random not random
  (let [BYTES [ (byte 0) (byte 0) (byte 0) (byte 1)
                (byte 0) (byte 0) (byte 1) (byte 1)]]
    ;
    (binding [*random* (predefined-next-bytes BYTES)]
      [ (urand32)
        (urand32)])) ;; => [1 257]

  (format "%022d" (big-rand))
  
  ,)

;;.
