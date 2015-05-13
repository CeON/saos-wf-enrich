(defn calc-stat-sum [ tags ]
  (let [
         num-judgments
           (count tags)
         num-judgments-with-tag
           (count (filter #(:value %) tags))
         num-links
           (reduce +
             (map #(count (:value %)) tags))
        ]
   { :num-judgments  num-judgments
     :num-judgments-with-tag num-judgments-with-tag
     :num-links num-links }))

(defn print-stat-sum [ stat ]
  (println
    (format
       (str "Number of judgments:           %6d\n"
            "Number of judgments with tag:  %6d (%4.2f %%)\n"
            "Number of links:               %6d (%4.2f per judgment)\n")
         (:num-judgments stat)
         (:num-judgments-with-tag stat)
         (float
           (* 100 (/ (:num-judgments-with-tag stat) (:num-judgments stat))))
         (:num-links stat)
         (float (/ (:num-links stat) (:num-judgments stat))))))

{
   :transform-f
     calc-stat-sum
   :merge-f
     (partial merge-with +)
   :print-f
     print-stat-sum
}
