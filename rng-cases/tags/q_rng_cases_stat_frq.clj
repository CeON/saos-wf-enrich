(require
   '[clojure.set :as set]
   '[cheshire.core :as cc]
   '[squeezer.core :as sc]
   '[saos-tm.extractor.judgment-links :as jl]
   '[clj.query :refer :all])

(defn get-rng-cases-count [tag]
  (count (:value tag)))

(defn calc-frequencies [ tags ]
  (->> tags
       (map get-rng-cases-count)
       frequencies))

(defn print-val-asc [ hist ]
  (let [
         keys-sort
           (sort (keys hist))
         print-hist-row-f
           #(println (format "%4d %4d" % (hist %)))
        ]
    (doall
      (map print-hist-row-f keys-sort))))

{
   :transform-f
     calc-frequencies
   :merge-f
     (partial merge-with +)
   :print-f
     print-val-asc
}
