(require
  '[clj.query :refer :all])

(defn empty-str-any? [ map ]
  (some #(= "" %) (vals map)))

(defn matches? [ ref-regu ]
  (empty-str-any? (:act ref-regu)))

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
