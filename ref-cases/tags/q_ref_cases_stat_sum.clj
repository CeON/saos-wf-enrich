(defn get-only-resolved-case-numbers [ tag ]
  (filter
    #(not (empty? (:judgmentIds %)))
    (:value tag)))

(defn calc-stat-sum [ tags ]
  (let [
         num-judgments
           (count tags)
         num-judgments-with-tag
           (count (filter #(:value %) tags))
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
    :num-judgments-with-tag num-judgments-with-tag
    :num-links num-links
    :num-resolved-links num-resolved-links}))

(defn print-stat-sum [ stat ]
  (println
    (format
       (str "Number of judgments:           %6d\n"
            "Number of judgments with tag:  %6d (%4.2f %%)\n"
            "Number of links:               %6d (%4.2f per judgment)\n"
            "Number of resolved links:      %6d (%4.2f per judgment)\n"
            "Perc. of resolved links:       %6.2f %%")
         (:num-judgments stat)
         (:num-judgments-with-tag stat)
         (float
            (* 100 (/ (:num-judgments-with-tag stat) (:num-judgments stat))))
         (:num-links stat)
         (float (/ (:num-links stat) (:num-judgments stat)))
         (:num-resolved-links stat)
         (float (/ (:num-resolved-links stat) (:num-judgments stat)))
         (float (* 100 (/ (:num-resolved-links stat) (:num-links stat)))))))

{
   :transform-f
     calc-stat-sum
   :merge-f
     (partial merge-with +)
   :print-f
     print-stat-sum
}
