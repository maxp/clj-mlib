(ns mlib.config.core
  (:require
    [clojure.edn            :as     edn]
    [clojure.string         :refer  [blank? split]]
    [clojure.java.io        :refer  [resource]]
    [mlib.config.envreaders :refer  [readers]]))
;=

(defn edn-slurp [file env]
  (edn/read-string 
    {:readers (readers env)}
    (slurp file)))
;;

(defn split-path [envvar]
  (->> 
    (split (str envvar) #"\:") 
    (remove blank?) 
    (seq)))
;;

(defn load-configs [& [paths]]
  (let [env   (System/getenv)
        ep    (or paths (split-path (get env "CONFIG_EDN")))
        conf  (-> "config.edn" resource (edn-slurp env))
        build (when-let [build-file (resource "build.edn")]
                {:build (edn-slurp build-file env)})]
    ;
    (concat [conf build]
      (map #(edn-slurp % env) ep))))
;;

(comment

  (load-configs)

  (let [test-data
        "{:test
          {:user   #envstr   \"USER\"
          :defval  #envstr   \"DEFVAL:Default\"
          :shlvl   #envint   \"SHLVL\"
          :bool    #envbool  \"BOOL:False\"
        }}"
        env (System/getenv)
        stream (-> test-data
                (.getBytes "UTF-8")
                (java.io.ByteArrayInputStream.))]
    (edn-slurp stream env))

  (let [env (System/getenv)]
    (-> "config.edn" 
      (resource) 
      (edn-slurp env)
      (:test)))
  ,)

;; example:
;; 
;; cfg:
;;   (defstate conf
;;     :start (b/conform! ::conf (apply deep-merge (args))))
;;
;; main:
;;   (start-with-args  (load-configs))
