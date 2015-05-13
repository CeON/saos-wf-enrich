(require '[clojure.string :as str])
(require '[cheshire.core :as cc])
(require '[clj.rest-put :as put])
(require '[clj.common :as com])

(defn has-non-empty-value? [ tag ]
  (not (nil? (:value tag))))

(defn put-data-files [ url user-colon-pass fnames* ]
  (loop [
          conn (put/create-url-conn url user-colon-pass)
          w (put/get-conn-stream conn)
          fnames fnames*
          print-coma? false
          _ (.write w "[")
        ]
    (if  (empty? fnames)
      (do
        (.write w "]")
        (.close w)
        (put/get-response conn))
      (let [
             tags
               (->> (first fnames)
                 com/read-json
                 (filter has-non-empty-value?)
                 (map cc/generate-string)
                 (interpose ","))
              _
               (when (and print-coma? (seq tags))
                 (.write w ","))
              _ (.write w (apply str tags))
              print-coma?*
                (or print-coma? (seq tags))
            ]
      (recur conn w (rest fnames) print-coma?* nil)))))

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
