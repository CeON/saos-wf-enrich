(require
   '[clojure.set :as set]
   '[cheshire.core :as cc]
   '[squeezer.core :as sc]
   '[saos-tm.extractor.judgment-links :as jl])

(defn keys-sorted-by-val-desc [ m ]
  (sort-by m #(compare %2 %1) (keys m)))

(defn get-only-resolved-case-numbers [ tag ]
  (map :referencedCaseNumber
    (filter
      #(not= nil (:referencedIds %))
      (:value tag))))

(defn get-all-case-numbers [tag]
  (map :referencedCaseNumber (:value tag)))

(defn conv-fname-to-stat-frq [ get-case-numbers-f fname ]
  (as-> fname ---
        (sc/slurp-compr ---)
        (cc/parse-string --- true)
        (mapcat get-case-numbers-f ---)
        (frequencies ---)))

(defn merge-stats [ stat1 stat2 ]
  (merge-with + stat1 stat2))

(defn print-stat [stat]
  (doall
    (map
      #(println (format "%10d %s" (get stat %) %))
      (keys-sorted-by-val-desc stat))))

(defn run [get-case-numbers-f argv]
  (->> argv
      (map (partial conv-fname-to-stat-frq get-case-numbers-f))
      (reduce  merge-stats)
      print-stat))

(when (> (count *command-line-args*) 0)
  (if (= (first *command-line-args*) "-r")
    (run get-only-resolved-case-numbers (rest *command-line-args*))
    (run get-all-case-numbers *command-line-args*)))
