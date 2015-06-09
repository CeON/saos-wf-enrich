(require
   '[cheshire.core :as cc]
   '[squeezer.core :as sc]
   '[saos-tm.extractor.common :as jc]
   '[saos-tm.extractor.ref-money :as e]
   '[clj.common :as cljc])

(defn conv-judgment-to-tag [j]
  (let [
         id
           (:id j)
         court-type
           (:courtType j)
         text
           (if (= court-type "COMMON")
             (try
                (jc/conv-html-to-text (:textContent j))
                (catch Exception e
                  (cljc/println-err
                    (format "ERROR, converting html to text failed for id=%d" id))
                  ""))
             (:textContent j))
         max-money-ref
           (try
             (e/extract-max-money-ref text)
             (catch Throwable e
               (do
                 (cljc/println-err
                   (format "ERROR, extracting money for id=%d" id))
                 nil)))
       ]
     { :judgmentId id
       :tagType "MAX_REFERENCED_MONEY"
       :value max-money-ref }))

(defn process [inp-fname out-fname]
  (let [
         inp-data
           (-> inp-fname
             sc/slurp-compr
             (cc/parse-string true))
          out-data
            (map
              conv-judgment-to-tag
              inp-data)
       ]
    (cljc/write-json out-fname out-data)))

(defn run [argv]
  (let [
         n
           (quot (count argv) 2)
         inp-fnames
           (take n argv)
         out-fnames
           (drop n argv)
        ]
  (dorun
    (map
      process
      inp-fnames out-fnames))))

(when (> (count *command-line-args*) 0)
  (run  *command-line-args*))
