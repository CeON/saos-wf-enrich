(require '[clj.query :refer :all])

{
   :transform-f
     (fn [t]
       (count
         (keep #(get-in % [:value :defendant]) t)))
   :merge-f
     +
   :print-f
     println
}
