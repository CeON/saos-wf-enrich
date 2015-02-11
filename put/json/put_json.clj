(require '[clojure.string :as str])
(require '[clojure.pprint :as pp])
(require '[cheshire.core :as cc])
(require '[squeezer.core :as sc])
(require '[clj.rest-put :as put])

(defn strip-string-from-parentheses [ ^String s ]
  (let [
         start-index
           (.indexOf s "[")
         stop-index
           (.lastIndexOf s "]")
       ]
    (if (and
          (>= stop-index 0)
          (>= start-index 0)
          (< start-index stop-index))
      (.substring s (inc start-index) stop-index)
      s)))

(defn read-file-content [ fname ]
  (strip-string-from-parentheses
    (sc/slurp-compr fname)))

(defn put-data-files [ url user-colon-pass fnames ]
  (let [
         data-seq
          (interpose ","
            (map read-file-content fnames))
         conn (put/create-url-conn url user-colon-pass)
       ]
  (with-open [ w (put/get-conn-stream conn) ]
    (.write w "[")
    (dorun (map #(.write w %) data-seq))
    (.write w "]" ))
  (put/get-message-and-response-code conn)))

(defn run [ argv ]
  (let [
         user-colon-pass (str/trim (slurp (first argv)))
         argv* (rest argv)
         url "https://saos-test.icm.edu.pl/api/enrichment/tags"
         { :keys [:message :code ] }
           (put-data-files url user-colon-pass argv*)
         _  (println (str "Returned code " code ". " message "."))
       ]
    (if (= code 200)
      0
      1)))

(when (> (count *command-line-args*) 0)
  (System/exit
   (run  *command-line-args*)))
