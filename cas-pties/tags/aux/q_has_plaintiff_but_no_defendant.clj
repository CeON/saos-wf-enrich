(require
  '[clj.query :refer :all])

(defn has-plaintiff? [tag]
  (some? (get-in tag [:value :plaintiff])))

(defn has-defendant? [tag]
  (some? (get-in tag [:value :defendant])))

(defn filter-tags [ tags ]
  (filter #(and (not (has-plaintiff? %))
                (has-defendant? %))
    tags))

{
   :transform-f
     filter-tags
   :merge-f
     concat
   :print-f
     print-json
}
