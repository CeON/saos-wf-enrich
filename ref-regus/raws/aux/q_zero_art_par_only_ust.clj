(require
  '[clj.query :refer :all])

(defn only-ust-not-empty? [ art-map ]
  (let [ 
         ust (:ust art-map)
         art-map* (dissoc art-map :ust)
       ]
    (and
      (not= ust "0")
      (every? #(= "0" %) (vals art-map*)))))

(defn matches? [ ref-regu ]
  (only-ust-not-empty? (:art ref-regu)))

(defn filter-single-tag [ tag ]
  (let [
         ref-regus
           (filter matches? (:value tag))
    ]
    (if (empty? ref-regus)
      []
      [ (assoc tag :value ref-regus) ] )))

(defn filter-tags [ tags ]
  (mapcat filter-single-tag tags))

{
   :transform-f
     filter-tags
   :merge-f
     concat
   :print-f
     print-json
}
