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

(defn write-seq-to-stream [ i w conn data-seq ]
  (let [
          _ (println "Iterating..." i)
          data-seq* (rest data-seq)
          data-item ^String (first data-seq)
          _
            (do 
              (.write w data-item)
              (.flush w))
         stop
            (not (seq data-seq*))
       ]
       (if stop
         (do
           (.close w)   
           (put/get-response conn))
         (recur (inc i) w conn data-seq*))))

(defn put-data-files [ url user-colon-pass fnames ]
  (let [
         data-seq
           (interpose ","
             (map read-file-content fnames))
         data-seq-with-parens 
           (concat [ "[" ] data-seq [ "]" ])
         conn (put/create-url-conn url user-colon-pass)
         w (put/get-conn-stream conn)
         write-chunk-f
           #(do (.write w %) (.flush w))
         _  
           (dorun
             (map write-chunk-f data-seq-with-parens))
         _ (.close w)
      ]
    (put/get-response conn)))

(defn run [ argv ]
  (let [
         user-colon-pass (str/trim (slurp (first argv)))
         argv* (rest argv)
         url "https://saos-test.icm.edu.pl/api/enrichment/tags"
         response
           (put-data-files url user-colon-pass argv*)
         _
           (println response)
       ]
    (if (= 200 (:code response))
       0 
       1)))

(when (> (count *command-line-args*) 0)
  (System/exit
   (run  *command-line-args*)))
