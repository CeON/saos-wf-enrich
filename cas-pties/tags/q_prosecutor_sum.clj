(require '[clj.query :refer :all])

(defn calc-stat-sum [ tags ]
  (let [
         non-nil-tags
           (filter #(not (nil? (:value %))) tags)
         prosecutor-tags
           (filter
             #(contains? (:value %) :prosecutor)
             non-nil-tags)
         prosecutor-non-nil-tags
            (keep #(get-in % [:value :prosecutor])
              prosecutor-tags)
        ]
  { :num-judgments (count tags)
    :num-judgments-with-tag
      (count non-nil-tags)
    :num-judgments-with-prosecutor (count prosecutor-tags)
    :num-judgments-with-non-empty-prosecutor (count prosecutor-non-nil-tags)
  }))

(defn print-stat-sum [ stat ]
  (println
    (format
       (str "Number of judgments:                           %6d\n"
            "Number of of judgment with all parties tag:    %6d (%4.2f %%)\n"
            "Number of judgments with prosecutor:           %6d\n"
            "Number of judgments with non-empty prosecutor: %6d  (%4.2f %%)")
         (:num-judgments stat)
         (:num-judgments-with-tag stat)
         (float
            (* 100 (/ (:num-judgments-with-tag stat) (:num-judgments stat))))
         (:num-judgments-with-prosecutor stat)
         (:num-judgments-with-non-empty-prosecutor stat)
         (float
            (* 100 (/ (:num-judgments-with-non-empty-prosecutor stat)
                      (:num-judgments-with-prosecutor stat)))))))

{
   :transform-f
     calc-stat-sum
   :merge-f
     (partial merge-with +)
   :print-f
     print-stat-sum
}
