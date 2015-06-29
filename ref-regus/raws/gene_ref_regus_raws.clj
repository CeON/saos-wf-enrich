(require
   '[clojure.set :as set]
   '[clojure.java.io :as io]
   '[clojure.string :as str]
   '[cheshire.core :as cc]
   '[langlab.core.parsers :as lp]
   '[squeezer.core :as sc]
   '[saos-tm.extractor.law-links :as ell]
   '[clj.common :as cljc])

(defn conv-judgment-to-tag [j]
  (let [
          id (:id j)
          court-type (:courtType j)
          text
            (if (not= court-type "COMMON")
              (:textContent j)
              nil)
          ref-regus-raw
            (if text
               (try
                 (:extracted-links
                    (ell/extract-law-links text true true true))
                (catch Exception e
                  (do
                    (println
                      (format
                        "ERROR, extracting referenced regulations for id=%d failed" id))
                    nil)))
                nil)
          ]
    { :judgmentId id
      :tagType "REFERENCED_REGULATIONS"
      :value
        (if-not (empty? ref-regus-raw)
          ref-regus-raw
          nil)}))

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
