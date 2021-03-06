(require '[clojure.string :as str])
(require '[cheshire.core :as cc])
(require '[clj.rest-put :as put])
(require '[clj.common :as cljc])

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
                 cljc/read-json
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
         { :keys [ putAuth putURL ] }
           (cljc/read-properties (first argv))
         argv* (rest argv)
         response
           (put-data-files putURL putAuth argv*)
         _
           (println response)
       ]
    (if (= 200 (:code response))
       0
       1)))

(when (> (count *command-line-args*) 0)
  (System/exit
   (run  *command-line-args*)))
