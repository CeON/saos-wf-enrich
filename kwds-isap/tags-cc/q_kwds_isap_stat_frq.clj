(require
   '[clojure.set :as set]
   '[cheshire.core :as cc]
   '[squeezer.core :as sc]
   '[saos-tm.extractor.judgment-links :as jl]
   '[clj.query :refer :all])

(defn get-kwds-isap [tag]
  (map name
    (keys (get-in tag [:value :keywordsISAP]))))

(defn calc-kwds-isap-frequencies [ tags ]
  (->> tags
       (mapcat get-kwds-isap)
       frequencies))

{
   :transform-f
     calc-kwds-isap-frequencies
   :merge-f
     (partial merge-with +)
   :print-f
     print-freq-desc
}
