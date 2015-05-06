(require
   '[clojure.set :as set]
   '[clojure.java.io :as io]
   '[clojure.string :as str]
   '[cheshire.core :as cc]
   '[langlab.core.parsers :as lp]
   '[squeezer.core :as sc]
   '[saos-tm.extractor.law-links :as ell])

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
                    (ell/extract-law-links-greedy text true true true))
                (catch Exception e
                  (do
                    (println
                      (format
                        "ERROR, extracting referenced regulations for id=%d failed" id))
                    [])))
                [])
          ]
    (if-not (empty? ref-regus-raw)
      [ { :id id
          :tagType "REF_REGUS"
          :value ref-regus-raw } ]
      [])))

(defn process [inp-fname out-fname]
  (let [
        inp-data
           (-> inp-fname
             sc/slurp-compr
             (cc/parse-string true))
          out-data
            (mapcat
              conv-judgment-to-tag
              inp-data)
       ]
    (sc/spit-compr
      out-fname
      (cc/generate-string out-data {:pretty true}))))

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
