(ns mlib.crypto.util
  (:import 
    [java.security MessageDigest]))
;=

(defn ^String hexbyte [^Integer b]
  (.substring (Integer/toString (+ 0x100 (bit-and 0xff b)) 16) 1))
;

(defn ^bytes byte-array-hash
  "calculate hash of byte array"
  [^String hash-name ^bytes barray]
  (let [md (MessageDigest/getInstance hash-name)]
    (.update md barray)
    (.digest md)))
;

(defn ^bytes calc-hash
  "calculate hash byte array of utf string using hash-function"
  [^String hash-name ^String s]
  (let [md (MessageDigest/getInstance hash-name)]
    (.update md (.getBytes s "UTF-8"))
    (.digest md)))
;

(defn ^String md5
  "returns md5 lowercase string calculated on utf-8 bytes of input"
  [^String s]
  (apply str (map hexbyte (calc-hash "MD5" s))))
;

(defn ^String sha1
  "returns sha1 lowercase string calculated on utf-8 bytes of input"
  [^String s]
  (apply str (map hexbyte (calc-hash "SHA-1" s))))
;

(defn ^String sha256
  "returns sha256 lowercase string calculated on utf-8 bytes of input"
  [^String s]
  (apply str (map hexbyte (calc-hash "SHA-256" s))))
;

;;.
