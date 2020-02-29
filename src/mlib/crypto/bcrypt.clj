(ns mlib.crypto.bcrypt
  (:import
    [mlib-org.mindrot.jbcrypt BCrypt]))
;=

;; http://www.mindrot.org/projects/jBCrypt/

(defn check-password 
  "check password hash match"
  [password hash]
  (BCrypt/checkpw password hash))
;

(defn hash-password 
  "generate bcrypt hash for the given password string"
  [password]
  (BCrypt/hashpw password (BCrypt/gensalt)))
;

;;.
