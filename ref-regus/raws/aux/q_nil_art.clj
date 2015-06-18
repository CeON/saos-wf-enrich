(require
  '[clj.query :refer :all])

(defn empty-nil-any? [ map ]
  (some nil? (vals map)))

(defn matches? [ ref-regu ]
  (empty-nil-any? (:art ref-regu)))

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
