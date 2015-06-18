(require
  '[clj.query :refer :all])

(defn empty-art? [ art-map ]
  (every? #(= "0" %) (vals art-map)))

(defn only-ust-not-empty? [ art-map ]
  (let [ 
         ust (:ust art-map)
         art-map* (dissoc art-map :ust)
       ]
    (and
      (not= ust "0")
      (every? #(= "0" %) (vals art-map*)))))

(defn empty-art-par? [ art-map ]
  (let [ 
         art (:art art-map)
         par (:par art-map)
       ]
    (and
      (= art "0")
      (= par "0"))))

(defn matches? [ ref-regu ]
  (and 
    (empty-art-par? (:art ref-regu))
    (not (only-ust-not-empty? (:art ref-regu)))
    (not (empty-art? (:art ref-regu)))))

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
