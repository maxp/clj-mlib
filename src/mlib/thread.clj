;;
;;  mlib: run proccessing loop in separate thread
;;

(ns mlib.thread
  (:require
    [clojure.core.async :refer [thread <!!]]))
;=


(defn- thread-loop [state' init step cleanup]
  (thread
    (try
      (init state')
      (loop []
        (step state')
        (if (::loop-flag @state')
          (recur)
          (cleanup @state' nil)))
      (catch Exception ex
        (cleanup @state' ex)))))
;

(defn start-loop
  " start loop in separate thread using state returned by init-fn,
    exit loop on empty new-state or on exception in step-fn or cleared loop-flag

    init-fn: setup state' atom in context of the new thread
    step-fn: do work using state' atom
    cleanup-fn: get final state and optional exception, free resources bound to the state
  "
  [init step cleanup]
  ;;
  (assert fn? init)
  (assert fn? step)
  (assert fn? cleanup)
  ;
  (let [state' (atom {::loop-flag true})]
    { ::state state'
      ::thread (thread-loop state' init step cleanup)}))
;

(defn clear-loop-flag [state']
  (swap! state' assoc ::loop-flag false))
;

(defn stop-loop
  "reset loop-flag and wait for the thread"
  [thread-state]
  (when thread-state
    (clear-loop-flag (::state  thread-state))
    (<!!             (::thread thread-state))))
;

(defn join [thread-state]
  (<!! (::thread thread-state)))
;

(comment

  ;; example

  ; (defn init [state']
  ;   (swap! state' assoc :conn (make-connection cfg)))
  ; ;

  ; (defn step [state']
  ;   (println
  ;     (read (:conn @state'))))
  ; ;

  ; (defn cleanup [state ex]
  ;   (when ex
  ;     (error ex))
  ;   (when-let [conn (:conn state)]
  ;     (close conn)))
  ; ;

  ; (defstate service
  ;   :start
  ;     (start-loop init step cleanup)
  ;   :stop
  ;     (stop-loop service))
  ; ;

  .)

;;.
