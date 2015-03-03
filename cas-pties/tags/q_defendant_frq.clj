(require '[clj.query :refer :all])

{
   :transform-f
     (fn [t]
       (frequencies
         (keep #(get-in % [:value :defendant]) t)))
   :merge-f
     (partial merge-with +)
   :print-f
     print-freq-desc
}
