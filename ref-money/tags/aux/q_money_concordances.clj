(require '[clojure.string :as str])
(require '[saos-tm.extractor.common :as jc])
(require '[clj.query :as q])

(defn positions [pred coll]
  (keep-indexed #(when (pred %2) %1) coll))

(defn is-currency-token? [s]
  (some? (#{"zł" "zł." "zł," "zł;" "PLN"} (str/lower-case s))))

(defn is-number-token? [s]
  (some? (re-matches #"[0-9.]*[,.;]?|[0-9.]*\,[0-9]*[,.;]?" s)))

(defn exify-number-tokens [s]
  (if (is-number-token? s) "X" s))

(defn extract-patterns [tokens positions n m]
  (let [
        len (count tokens)
        select-tokens-f
          #(subvec
             tokens
             (max 0 (- % n))
             (min len (inc (+ % m))))
        ]
    (as->
      positions ---
      (map select-tokens-f ---)
      (map #(map exify-number-tokens %) ---)
      (map #(apply str (interpose " " %)) --- ))))

(defn get-plain-text-from-judgment [ j ]
  (if (= (:courtType j) "COMMON")
    (jc/conv-html-to-text (:textContent j))
    (:textContent j)))

(defn conv-str-to-money-patterns-frq [s]
  (let [
         tokens
           (into [] (str/split s #"(?m)\s+"))
         positions
           (positions is-currency-token? tokens)
         patterns
           (extract-patterns tokens positions 7 2)
       ]
    (frequencies patterns)))

(defn conv-judgments-to-money-patterns-frq  [judgments]
  (reduce
    (partial merge-with +)
    (map
      (comp
        conv-str-to-money-patterns-frq
        get-plain-text-from-judgment)
      judgments)))

 {
   :transform-f
     conv-judgments-to-money-patterns-frq
   :merge-f
     (partial merge-with +)
   :print-f
     q/print-freq-desc
 }
