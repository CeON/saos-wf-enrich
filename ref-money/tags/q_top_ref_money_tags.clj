(require
  '[clojure.set :as set]
  '[cheshire.core :as cc]
  '[squeezer.core :as sc]
  '[saos-tm.extractor.judgment-links :as jl]
  '[clj.query :refer :all])

(def TOP-N 100)

(defn get-max-money [tag]
  (get-in tag [:value :amount] tag))

;; Complicated because it has to glue the results in case of tie
;; at the end of a list

(defn get-top-n [ top-n l1 l2 ]
  (let [
          merged-lists
            (concat l1 l2)
          sorted-lists
            (sort-by get-max-money > merged-lists)
          top-value
             (if-let [
                       last-element (nth sorted-lists (dec top-n) nil)
                      ]
               (get-max-money last-element)
               0)
        ]
     (filter #(>= (get-max-money %) top-value) sorted-lists)))

(defn print-val-asc [ hist ]
  (let [
         keys-sort
           (sort (keys hist))
         print-hist-row-f
           #(println (format "%4d %4d" % (hist %)))
        ]
    (doall
      (map print-hist-row-f keys-sort))))

{
   :transform-f
     (fn [ tags ]
        (get-top-n TOP-N []
          (filter #(:value %) tags)))
   :merge-f
     (partial get-top-n TOP-N)
   :print-f
     print-json
}
