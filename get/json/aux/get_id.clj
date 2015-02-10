(require
   '[clojure.string :as str]
   '[cheshire.core :as cc]
   '[squeezer.core :as sc])

(defn filter-matching-recs-from-file [ fname filter-f ]
  (as-> fname ---
        (sc/slurp-compr ---)
        (cc/parse-string --- true)
        (filter filter-f ---)))


(defn run [argv]
  (let [
          id-set
             (->> (first argv)
               slurp
               str/split-lines
               (map #(Integer. %))
               (into #{}))
           mapcat-f
             (fn [fname]
               (filter-matching-recs-from-file
                 fname
                 #(contains? id-set (:id %))))
          fnames (rest argv)
        ]
    (as-> fnames ---
      (mapcat mapcat-f ---)
      (cc/generate-string --- {:pretty true})
      (println ---))))

(when (> (count *command-line-args*) 0)
  (run *command-line-args*))

