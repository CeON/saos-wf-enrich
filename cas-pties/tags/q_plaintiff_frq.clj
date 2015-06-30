(require '[clj.query :refer :all])

{
   :transform-f
     (fn [tags]
       (->> tags
         (filter #(not (nil? (:value %))))
         (filter #(not (contains? (:value %) :prosecutor)))
         (map #(get-in % [:value :plaintiff]))
         frequencies))
   :merge-f
     (partial merge-with +)
   :print-f
     print-freq-desc
}
