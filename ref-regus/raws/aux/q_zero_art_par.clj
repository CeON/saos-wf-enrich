(require
  '[clj.query :refer :all])

(defn not-empty-art? [ art ]
  (some #(not= % "0") (vals art)))

(defn matches? [ ref-regu ]
  (let [
         art (:art ref-regu)
        ]
    (and (and (= "0" (:art art)) (= "0" (:par art)))
         (not-empty-art? art))))

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
