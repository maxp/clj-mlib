
(ns user
  (:require
    [clojure.tools.namespace.repl :as tnr]
  ;  [mount.core :as mount]
    [util :as util]))
    ;
    ; [__root__.app.main :as main]))
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
