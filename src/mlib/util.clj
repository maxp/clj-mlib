;;
;;  mlib.util
;;

(ns mlib.util
  (:require
    [clojure.string   :refer [escape trim blank?]]
    [clojure.java.io  :as io]
    [clojure.edn      :as edn]))
;=


(defn assoc-not-nil [m k v]
  (if (nil? v) m (assoc m k v)))
;

;; ;; ;; number conversion ;; ;; ;;

(defn ^Integer to-int
  "returns nil or default on failure"
  ( [s]
    (to-int s nil))
  ( [s default]
    (try
      (if (string? s) (Integer/parseInt s) (int s))
      (catch Exception _ignore default))))
;

(defn ^Integer to-long
  "returns nil or default on failure"
  ( [s]
    (to-long s nil))
  ( [s default]
    (try
      (if (string? s) (Long/parseLong s) (long s))
      (catch Exception _ignore default))))
;

(defn ^Float to-float
  "returns nil or default on failure"
  ( [s]
    (to-float s nil))
  ( [s default]
    (try
      (if (string? s) (Float/parseFloat s) (float s))
      (catch Exception _ignore default))))
;

(defn ^Double to-double
  "returns nil or default on failure"
  ( [s]
    (to-double s nil))
  ( [s default]
    (try
      (if (string? s) (Double/parseDouble s) (double s))
      (catch Exception _ignore default))))
;

;; ;; ;; time ;; ;; ;;

(defn now-ms []
  (System/currentTimeMillis))
;

;; ;; ;; edn ;; ;; ;;

(defn edn-read [file]
  (edn/read-string (slurp file)))
;

(defn edn-resource [res]
  (-> res io/resource slurp edn/read-string))
;


;; ;; ;; string utils ;; ;; ;;

(defn ^String str-trim [s]
  (trim (str s)))
;

(defn ^String str-head
  "Returns the first n characters of s."
  [n ^String s]
  (if (>= n (.length s)) s (.substring s 0 n)))
;

(defn ^String str-tail
  "Returns the last n characters of s."
  [n ^String s]
  (if (< (count s) n) s (.substring s (- (count s) n))))
;

(defn hesc
  "Replace special characters by HTML character entities."
  [text]
  (escape (str text)
    {\& "&amp;" \< "&lt;" \> "&gt;" \" "&#34;" \' "&#39;"}))
;

(defn cap-first [s]
  (when (string? s)
    (if (> (.length s) 0)
      (str (Character/toUpperCase (.charAt s 0)) (.substring s 1))
      s)))
;

(defn not-blank? [s]
  (and
    (string? s) 
    (not (blank? s))))
;

;; ;; ;; deep merge ;; ;; ;;

(defn- deep-merge* [& maps]
  (let [f (fn [old new]
            (if (and (map? old) (map? new))
                (merge-with deep-merge* old new)
                new))]
    (if (every? map? maps)
      (apply merge-with f maps)
      (last maps))))
;

(defn deep-merge [& maps]
  (let [maps (filter identity maps)]
    (assert (every? map? maps))
    (apply merge-with deep-merge* maps)))
;

;;.
