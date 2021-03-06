(require
   '[clojure.set :as set]
   '[cheshire.core :as cc]
   '[squeezer.core :as sc]
   '[saos-tm.extractor.common :as jc]
   '[saos-tm.extractor.judgment-links :as jl]
   '[clj.common :as cljc])

(defn is-resolved? [ tag-value ]
  (not= [] (:judgmentIds tag-value)))

(defn sort-by-case-number [ tag-values ]
  (sort #(compare (:caseNumber %1) (:caseNumber %2)) tag-values))

(defn sort-tag-value [ tag-values ]
  (let [
        tag-values-resolved
          (sort-by-case-number
            (filter is-resolved? tag-values))
        tag-values-unresolved
          (sort-by-case-number
            (filter (complement is-resolved?) tag-values))
        ]
    (concat tag-values-resolved  tag-values-unresolved)))

(defn conv-judgment-to-tag [ case-number->ids j]
  (let [
         id (:id j)

         court-type (:courtType j)

         this-case-numbers
           (into
             #{}
             (map
                #(cljc/normalize-case-number (:caseNumber %))
                (:courtCases j)))

         text
           (if (= court-type "COMMON")
             (try
                (jc/conv-html-to-text (:textContent j))
                (catch Exception e
                  (cljc/println-err
                    (format "ERROR, converting html to text failed for id=%d" id))
                  (:textContent j)))
             (:textContent j))

         dirty-referenced-case-numbers
           (try
             (into #{}
               (map cljc/normalize-case-number
                 (jl/extract-judgment-links text)))
             (catch Exception e
               (cljc/println-err
                 (format "ERROR, extracting singnatres for id=%d failed" id))
               #{}))

         referenced-case-numbers
           (set/difference
             dirty-referenced-case-numbers this-case-numbers)

         referenced-case-numbers-tag-value
           (sort-tag-value
             (map
               #(hash-map :caseNumber %
                         :judgmentIds (case-number->ids % []))
               referenced-case-numbers))
       ]
      { :judgmentId id
        :tagType "REFERENCED_COURT_CASES"
        :value
          (if (empty? referenced-case-numbers)
             nil
             referenced-case-numbers-tag-value)}))

(defn process [case-number->ids inp-fname out-fname]
  (let [
         inp-data
           (-> inp-fname
             sc/slurp-compr
             (cc/parse-string true))
          out-data
            (map
              (partial conv-judgment-to-tag case-number->ids)
              inp-data)
       ]
  (cljc/write-json out-fname out-data)))

(defn run [argv]
  (let [
         case-number->ids
           (-> (first argv)
             sc/slurp-compr
             (cc/parse-string false))
         argv* (rest argv)
         n
           (quot (count argv*) 2)
         inp-fnames
           (take n argv*)
         out-fnames
           (drop n argv*)
        ]
  (dorun
    (map
      (partial process case-number->ids)
      inp-fnames out-fnames))))

(when (> (count *command-line-args*) 0)
  (run  *command-line-args*))
