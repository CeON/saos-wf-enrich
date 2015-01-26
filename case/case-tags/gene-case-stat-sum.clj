(require
   '[clojure.set :as set]
   '[cheshire.core :as cc]
   '[squeezer.core :as sc]
   '[saos-tm.extractor.judgment-links :as jl])

(defn get-only-resolved-case-numbers [ tag ]
  (filter
    #(not= nil (:referencedIds %))
    (:value tag)))

(defn conv-fname-to-stat-sum [ fname ]
  (let [
         tags
           (-> fname
               sc/slurp-compr
               (cc/parse-string true))
         num-judgments
           (count tags)
         num-links
           (reduce +
             (map #(count (:value %)) tags))
         num-resolved-links
           (reduce +
             (map
               #(count (get-only-resolved-case-numbers %))
               tags))
        ]
  { :num-judgments  num-judgments
    :num-links num-links
    :num-resolved-links num-resolved-links}))

(defn merge-stats [ stat1 stat2 ]
  (merge-with + stat1 stat2))

(defn conv-stat-to-str [ stat ]
  (format
     (str "Number of judgments:      %6d\n"
          "Number of links:          %6d (%4.2f per judgment)\n"
          "Number of resolved links: %6d (%4.2f per judgment)\n"
          "Perc. of resolved links:  %6.2f %%")
       (:num-judgments stat)
       (:num-links stat)
       (float (/ (:num-links stat) (:num-judgments stat)))
       (:num-resolved-links stat)
       (float (/ (:num-resolved-links stat) (:num-judgments stat)))
       (float (* 100 (/ (:num-resolved-links stat) (:num-links stat))))))

(defn run [argv]
  (->> argv
      (map conv-fname-to-stat-sum)
      (reduce  merge-stats)
      conv-stat-to-str
      println))

(when (> (count *command-line-args*) 0)
  (run  *command-line-args*))
