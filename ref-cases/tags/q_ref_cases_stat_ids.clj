(defn get-id-counts [ tag ]
  (map
    #(count (:judgmentIds %))
    (filter
      #(not= nil (:judgmentIds %))
      (:value tag))))

(defn calc-stat-ids [ tags ]
  (->> tags
        (mapcat get-id-counts)
        frequencies))

(defn print-stat-ids [stat]
  (println (format "# %s %10s" "Number of resolving ids" "Count"))
  (dorun
    (map
      #(println (format "%25d %10d" % (get stat %)))
      (sort (keys stat)))))

{
   :transform-f
     calc-stat-ids
   :merge-f
     (partial merge-with +)
   :print-f
     print-stat-ids
}
