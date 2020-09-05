(ns mlib.jwt.core
  (:import 
    [java.util Date]
    [com.auth0.jwt JWT]
    [com.auth0.jwt.algorithms Algorithm]))
;=

;; https://tools.ietf.org/html/rfc7519
;; https://www.iana.org/assignments/jwt/jwt.xhtml

;                      
; https://github.com/auth0/java-jwt/blob/master/lib/src/main/java/com/auth0/jwt/JWTCreator.java                        
; 
(defn create-token [sub issuer jwt-conf]
  (let [{:keys [secret exp]} (get-in jwt-conf [:issuers issuer])
        alg   (Algorithm/HMAC256 secret)
        now   (System/currentTimeMillis)]
    (-> (JWT/create)
      (.withIssuer    issuer)
      (.withSubject   (str sub))
      (.withIssuedAt  (Date. now))
      (.withExpiresAt (Date. (+ now exp)))
      (.sign          alg))))
;;

(comment

  (create-token "user" "test" {:issuers {"test" {. ...}}})

  ;; => "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ1c2VyIiwiZXhwIjoxNjAxNDU4MTQ3LCJpYXQiOjE1OTI4MTgxNDd9.H9WXYusvxu4bxOqkG55eihEZSba6kLbioiI6aOJ19D0"
  ;; => "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ1c2VyIiwiZXhwIjoxNTkyNzE5NDg2LCJpYXQiOjE1OTI3MTU4ODZ9.WQEtyv3f0iPUrSWBeNCYP02pebDvJM2B9HoKL4adwww"

  ,)

; {
;   "name": "John Doe",
;   "nickname": "john.doe",
;   "picture": "https://myawesomeavatar.com/avatar.png",
;   "updated_at": "2017-03-30T15:13:40.474Z",
;   "email": "john.doe@test.com",
;   "email_verified": false,
;   "iss": "https://YOUR_DOMAIN/",
;   "sub": "auth0|USER-ID",
;   "aud": "YOUR_CLIENT_ID",
;   "exp": 1490922820,
;   "iat": 1490886820,
;   "nonce": "crypto-value",
;   "at_hash": "IoS3ZGppJKUn3Bta_LgE2A"
; }
