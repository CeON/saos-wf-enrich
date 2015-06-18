(require
  '[clj.query :refer :all])

(defn empty-any? [ map ]
  (some empty? (vals map)))

(defn matches? [ ref-regu ]
  (or  
    (empty-any? (:art ref-regu))
    (empty-any? (:act ref-regu))))

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
