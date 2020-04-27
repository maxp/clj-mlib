(ns example__app.main
  (:gen-class)
  (:require
    [mount.core       :refer [defstate start-with-args]]
    ;
    [mlib.config.core :refer [load-configs]]
    [mlib.logger      :refer [debug info warn]]
    ;
    [example__app.cfg :refer [conf]]))
    ;[example__app.app._other :refer [_mount-deps]]))
;=

(defstate app-start
  :start
    (info "started:" (:build conf)))
;

(defn -main []
  (and 
    ; _mount-deps
    :suppress-linter-warning)
  
  (info "init...")
  (start-with-args (load-configs))
  ;; NOTE: catch config exception
  
  (info "main started."))
  ;  (thread/join queue-worker)))
;;
