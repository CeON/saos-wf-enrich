(require '[clojure.string :as str])
(require '[clj.query :refer :all])

(defn contains-justification? [ j ]
  (some?
    (re-find #"<h2>UZASADNIENIE</h2>" (:textContent j))))

{
   :transform-f
     (fn [d]
       (map :id
         (filter (complement contains-justification?) d)))
   :print-f
     print-plain
}
