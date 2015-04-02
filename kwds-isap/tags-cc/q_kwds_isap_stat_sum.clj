(require
   '[clojure.set :as set]
   '[cheshire.core :as cc]
   '[squeezer.core :as sc]
   '[saos-tm.extractor.judgment-links :as jl]
   '[clj.query :refer :all])

(defn has-non-empty-kwds-isap? [ tag ]
  (not (empty? (get-in tag [:value :keywordsISAP ]))))

(defn calc-sum-stat [ tags ]
  { :totKeywords
      (count tags)
    :totNonEmptyKeywords
      (count (filter has-non-empty-kwds-isap? tags))
  })

(defn print-sum-stat [ stat ]
  (println (format "All generated keyword tags: %d" (:totKeywords stat)))
  (println (format "All non-empty keyword tags: %d" (:totNonEmptyKeywords stat))))

{
   :transform-f
     calc-sum-stat
   :merge-f
     (partial merge-with +)
   :print-f
     print-sum-stat
}
