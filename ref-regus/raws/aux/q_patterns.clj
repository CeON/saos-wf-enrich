(require
  '[clj.query :refer :all])

(defn gen-pattern-from-str [s]
   (if (= s "0") "-" "X"))

(defn conv-ref-regu-to-pattern [r]
   (let [
       art (:art r)
     ]
   (str "art " (gen-pattern-from-str (:art art))
        " par " (gen-pattern-from-str (:par art))
        " ust " (gen-pattern-from-str (:ust art))
        " pkt " (gen-pattern-from-str (:pkt art))
        " lit " (gen-pattern-from-str (:lit art))
        " zd  " (gen-pattern-from-str (:zd art)))))

(defn calc-frequencies [ tags ]
  (->> tags
       (mapcat :value)
       (map conv-ref-regu-to-pattern)
       frequencies))

{
   :transform-f
     calc-frequencies
   :merge-f
     (partial merge-with +)
   :print-f
     print-freq-desc
}
