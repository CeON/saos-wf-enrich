(defn get-only-resolved-case-numbers [ tag ]
  (filter
    #(not (empty? (:judgmentIds %)))
    (:value tag)))

(defn calc-stat-sum [ tags ]
  (let [
         num-links
           (map
             #(count (:value %))
             tags)
         num-resolved-links
           (map
             #(count (get-only-resolved-case-numbers %))
             tags)
        ]
  { :frq-links (frequencies num-links)
    :frq-resolved-links (frequencies num-resolved-links) }))

(defn merge-stats [s1 s2]
  (zipmap
    (keys s1)
    (map
      #(merge-with + %1 %2)
      (map s1 (keys s1))
      (map s2 (keys s1)))))

(defn print-stats [s]
  (let [
          frq-links
            (:frq-links s)
          tot-links
            (reduce + (vals frq-links))
          frq-resolved-links
            (:frq-resolved-links s)
          tot-resolved-links
            (reduce + (vals frq-resolved-links))
          all-keys
            (mapcat keys (vals s))
          min
            (apply min all-keys)
          max
            (apply max all-keys)
          range
            (range min (inc max))
          println-entry-f
            #(println
               (format "%4d, %17d, %16.4f, %25d, %25.4f"
                  %
                  (get frq-links % 0)
                  (float (/ (get frq-links % 0) tot-links))
                  (get frq-resolved-links % 0)
                  (float (/ (get frq-resolved-links % 0) tot-resolved-links))))
        ]
    (println (format "%4s, %17s, %16s, %25s, %25s"
                     "num"
                     "#j. with num refs"
                     "prob of num refs"
                     "#j. with num resolv. refs"
                     "prob of num resolv. refs"))
    (dorun
      (map println-entry-f range))))


{
   :transform-f
     calc-stat-sum
   :merge-f
     merge-stats
   :print-f
     print-stats
}
