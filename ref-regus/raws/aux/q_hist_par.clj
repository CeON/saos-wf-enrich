(require
  '[clj.query :refer :all])

(defn extract-fields [ v tag]
  (map #(get-in % v) (:value tag)))

(defn calc-frequencies [ tags ]
  (->> tags
       (mapcat #(extract-fields [:art :par] %))
       frequencies))

{
   :transform-f
     calc-frequencies
   :merge-f
     (partial merge-with +)
   :print-f
     print-freq-desc
}
