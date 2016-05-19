(require
   '[cheshire.core :as cc]
   '[squeezer.core :as sc]
   '[clj.common :as cljc])

(defn conv-judgment-to-kv-pair [j]
  (let [
         case-numbers
           (map
              #(cljc/normalize-case-number (:caseNumber %))
                (:courtCases j))
       ]
    (map
      hash-map
      case-numbers
      (repeat
        [ (:id j)]))))

(defn make-kv-pairs [ fname ]
  (as->
      fname ---
      (sc/slurp-compr ---)
      (cc/parse-string --- true)
      (mapcat conv-judgment-to-kv-pair ---)))

(defn run [argv]
  (let [
         kv-store-fname
           (first argv)
         inp-json-files
           (rest argv)
         dups
           (reduce
             (fn [ m1 m2 ] (merge-with concat m1 m2))
             (mapcat make-kv-pairs inp-json-files))
        ]
    (with-open
      [ w (sc/writer kv-store-fname :compr "gzip") ]
        (cc/generate-stream dups w {:pretty true}))))

(when (> (count *command-line-args*) 0)
  (run  *command-line-args*))
