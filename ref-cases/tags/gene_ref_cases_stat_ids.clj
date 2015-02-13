(require
   '[clojure.set :as set]
   '[cheshire.core :as cc]
   '[squeezer.core :as sc]
   '[saos-tm.extractor.judgment-links :as jl])

(defn keys-sorted-by-val-desc [ m ]
  (sort-by m #(compare %2 %1) (keys m)))

(defn get-id-counts [ tag ]
  (map
    #(count (:judgmentIds %))
    (filter
      #(not= nil (:judgmentIds %))
      (:value tag))))

(defn conv-fname-to-stat-ids [ fname ]
  (as-> fname ---
        (sc/slurp-compr ---)
        (cc/parse-string --- true)
        (mapcat get-id-counts ---)
        (frequencies ---)))

(defn merge-stats [ stat1 stat2 ]
  (merge-with + stat1 stat2))

(defn print-stat [stat]
  (println (format "# %s %10s" "Number of resolving ids" "Count"))
  (doall
    (map
      #(println (format "%25d %10d" % (get stat %)))
      (sort (keys stat)))))

(defn run [argv]
  (->> argv
      (map conv-fname-to-stat-ids)
      (reduce  merge-stats)
      print-stat))

(when (> (count *command-line-args*) 0)
    (run *command-line-args*))
