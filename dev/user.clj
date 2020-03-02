
(ns user
  (:require
    [clojure.tools.namespace.repl :as tnr]
  ;  [mount.core :as mount]
    [criterium.core :refer [quick-bench]]
    [util :as util]))
    ;
    ; [__root__.app.main :as main]))
;=

(let [number 5]
  (quick-bench 
    (condp = number
      1 1 
      2 2 
      3 3 
      4 4 
      5 5))) 
;

(defn restart []
  (prn "restart")
  (util/stop)
  (util/start))
;

(defn reset []
  (tnr/refresh :after 'user/restart))
;

; (mount/defstate dev-main
;   :start
;     (main/main))
; ;=

(comment

  (restart)

  (reset)
  
  ,)

;;.
