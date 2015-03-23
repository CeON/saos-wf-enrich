(require
   '[clojure.set :as set]
   '[cheshire.core :as cc]
   '[squeezer.core :as sc]
   '[saos-tm.extractor.judgment-links :as jl]
   '[clj.query :refer :all])

(defn get-all-case-numbers [tag]
  (map :caseNumber (:value tag)))

(defn calc-frequencies [ tags ]
  (->> tags 
       (mapcat get-all-case-numbers)
       frequencies))

{
   :transform-f
     calc-frequencies
   :merge-f
     (partial merge-with +)
   :print-f
     print-freq-desc
}
