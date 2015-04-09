(require
   '[clojure.set :as set]
   '[cheshire.core :as cc]
   '[squeezer.core :as sc]
   '[saos-tm.extractor.judgment-links :as jl]
   '[clj.query :refer :all])

(def TOP-N 10)

(defn get-rng-cases-count [tag]
  (count (:value tag)))

(defn calc-frequencies [ tags ]
  (->> tags
       (map get-rng-cases-count)
       frequencies))

;; Complicated because it has to glue the results in case of tie
;; at the end of a list

(defn get-top-n [ top-n l1 l2 ]
  (let [
          cases-count-f
           #(count (:value %))
          merged-lists
            (concat l1 l2)
          sorted-lists
            (sort-by cases-count-f > merged-lists)
          top-value
             (if-let [
                       last-element (nth sorted-lists (dec top-n) nil)
                      ]
               (cases-count-f last-element)
               0)
        ]
     (filter #(>= (cases-count-f %) top-value) sorted-lists)))

(defn print-val-asc [ hist ]
  (let [
         keys-sort
           (sort (keys hist))
         print-hist-row-f
           #(println (format "%4d %4d" % (hist %)))
        ]
    (doall
      (map print-hist-row-f keys-sort))))

(defn add-value-count [tags]
  (map #(assoc % :valueCount (count (:value %))) tags))

{
   :transform-f
     (partial get-top-n TOP-N [])
   :merge-f
     (partial get-top-n TOP-N)
   :print-f
     (comp print-json add-value-count)
}
