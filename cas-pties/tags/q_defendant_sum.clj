(require '[clj.query :refer :all])

(defn calc-stat-sum [ tags ]
  (let [
         non-nil-tags
           (filter #(not (nil? (:value %))) tags)
         defendant-or-plaintiff-tags
           (filter
             #(not (contains? (:value %) :prosecutor))
             non-nil-tags)
         defendant-non-nil-tags
           (keep #(get-in % [:value :defendant])
             defendant-or-plaintiff-tags)
        ]
  { :num-judgments (count tags)
    :num-judgments-with-tag
      (count non-nil-tags)
    :num-judgments-with-defendant-or-plaintiff
      (count defendant-or-plaintiff-tags)
    :num-judgments-with-non-empty-defendant
      (count defendant-non-nil-tags)
  }))

(defn print-stat-sum [ stat ]
  (println
    (format
       (str "Number of judgments:                             %6d\n"
            "Number of of judgment with all parties tag:      %6d (%4.2f %%)\n"
            "Number of judgments with defendant or plaintiff: %6d\n"
            "Number of judgments with non-empty defendant:    %6d  (%4.2f %%)")
         (:num-judgments stat)
         (:num-judgments-with-tag stat)
         (float
            (* 100 (/ (:num-judgments-with-tag stat) (:num-judgments stat))))
         (:num-judgments-with-defendant-or-plaintiff stat)
         (:num-judgments-with-non-empty-defendant stat)
         (float
            (* 100 (/ (:num-judgments-with-non-empty-defendant stat)
                      (:num-judgments-with-defendant-or-plaintiff stat)))))))

{
   :transform-f
     calc-stat-sum
   :merge-f
     (partial merge-with +)
   :print-f
     print-stat-sum
}
