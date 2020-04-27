
(ns user
  (:require
    [mount.core :as mount]
    [criterium.core :refer [quick-bench]]
    [util :as util]
    ;
    [example__app.cfg :as cfg]))
;=

(defn start-conf []
  (->
    (mount/only [#'cfg/conf])
    (mount/with-args (util/configs))
    (mount/start)))
;


; (mount/defstate dev-main
;   :start
;     (main/main))
; ;=

(comment

  (try
    (start-conf)
    (catch Exception ex
      ex))

  cfg/conf

  (util/restart)

  (util/reset)
  
  (quick-bench (+ 1 2))

  ,)
