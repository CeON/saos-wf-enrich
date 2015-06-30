(require '[clj.query :refer :all])

{
   :transform-f
     (fn [tags]
       (->> tags
         (filter #(not (nil? (:value %))))
         (filter #(contains? (:value %) :prosecutor))
         (map #(get-in % [:value :prosecutor]))
         frequencies))
   :merge-f
     (partial merge-with +)
   :print-f
     print-freq-desc
}
