(require
   '[clojure.set :as set]
   '[clojure.pprint :as pp]
   '[cheshire.core :as cc]
   '[squeezer.core :as sc]
   '[saos-tm.extractor.judgment-links :as jl])

(defn keys-sorted-by-key-asc [ m ]
  (sort-by identity #(compare %1 %2) (keys m)))

(defn keys-sorted-by-val-dsc [ m ]
  (sort-by m #(compare %2 %1) (keys m)))

(defn get-keywords-count-f [j]
  (if (= (:courtType j) "COMMON")
    [ (count (:keywords j)) ]
    []))

(defn get-keywords-f [j]
  (if (= (:courtType j) "COMMON")
    (:keywords j)
    []))

(defn conv-fname-to-stat-frq [ get-stat-f fname ]
  (as-> fname ---
        (sc/slurp-compr ---)
        (cc/parse-string --- true)
        (mapcat get-stat-f ---)
        (frequencies ---)))

(defn merge-stats [ stat1 stat2 ]
  (merge-with + stat1 stat2))

(defn print-stat [sort-keys-f stat]
  (doall
    (map
      #(println (format "%10d %s" (get stat %) %))
      (sort-keys-f stat))))

(defn run [get-stat-f sort-keys-f argv]
  (->> argv
      (map (partial conv-fname-to-stat-frq get-stat-f))
      (reduce  merge-stats)
      (print-stat sort-keys-f)))

(when (> (count *command-line-args*) 0)
  (if (= "-n" (first *command-line-args*))
    (run get-keywords-count-f keys-sorted-by-key-asc (rest *command-line-args*))
    (run get-keywords-f keys-sorted-by-val-dsc *command-line-args*)))
