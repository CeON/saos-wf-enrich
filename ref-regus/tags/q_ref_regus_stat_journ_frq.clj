(require
   '[clojure.set :as set]
   '[clojure.string :as str]
   '[cheshire.core :as cc]
   '[squeezer.core :as sc]
   '[saos-tm.extractor.judgment-links :as jl]
   '[clj.query :refer :all])

(defn conv-ref-regu-to-str [ ref-regu ]
  (let [
         key (str "D" (:journalYear ref-regu) "/"
                    (:journalNo ref-regu) "/"
                    (:journalEntry ref-regu))
         title (:journalTitle ref-regu)
         res (str/trim (str key " - " title))
        ]
     res))

(defn get-ref-regus [j]
  (map conv-ref-regu-to-str (:value j)))

(defn calc-ref-regus-frequencies [ tags ]
  (->> tags
       (mapcat get-ref-regus)
       frequencies))

{
   :transform-f
     calc-ref-regus-frequencies
   :merge-f
     (partial merge-with +)
   :print-f
     print-freq-desc
}
