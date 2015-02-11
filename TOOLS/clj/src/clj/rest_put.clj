(ns clj.rest-put
  (require
    [clojure.string :as str]
    [cheshire.core :as cc]
    [clj-http.client :as client]
    [byte-streams :as bs]
    [squeezer.core :as sc])
  (import
    [java.net URL]
    [java.io OutputStreamWriter]
    [java.io InputStreamReader]
    [javax.xml.bind DatatypeConverter]))

;;; Relevatnt SO hints
;;; http://stackoverflow.com/questions/15678208/making-put-request-with-json-data-using-httpurlconnection-is-not-working
;;;  http://stackoverflow.com/questions/496651/connecting-to-remote-url-which-requires-authentication-using-java

(defn create-url-conn [ url user-colon-pass ]
  (let [
         url-object (URL. url)
         basic-auth
           (str/trim
            (str "Basic "
                  (DatatypeConverter/printBase64Binary (. user-colon-pass getBytes))))
         conn
           (doto  (. url-object openConnection)
             (.setRequestMethod "PUT")
             (.setRequestProperty "Authorization" basic-auth)
             (.setDoInput true)
             (.setDoOutput true)
             (.setRequestProperty "Content-Type" "application/json")
             (.setRequestProperty "Accept" "application/json"))
        ]
    conn))

(defn get-conn-stream [ conn ]
  (OutputStreamWriter. (. conn getOutputStream)))

(defn get-message-and-response-code [ conn ]
  { :code (. conn getResponseCode)
    :message (. conn getResponseMessage) })
