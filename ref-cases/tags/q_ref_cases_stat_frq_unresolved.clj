(require
   '[clojure.set :as set]
   '[cheshire.core :as cc]
   '[squeezer.core :as sc]
   '[saos-tm.extractor.judgment-links :as jl]
   '[clj.query :refer :all])

(defn get-only-unresolved-case-numbers [ tag ]
  (map :caseNumber
    (filter
      #(= [] (:judgmentIds %))
      (:value tag))))

(defn calc-frequencies [ tags ]
  (->> tags
       (mapcat get-only-unresolved-case-numbers)
       frequencies))

{
   :transform-f
     calc-frequencies
   :merge-f
     (partial merge-with +)
   :print-f
     print-freq-desc
}
