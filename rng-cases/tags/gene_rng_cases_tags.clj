(require
   '[clojure.set :as set]
   '[cheshire.core :as cc]
   '[squeezer.core :as sc]
   '[saos-tm.extractor.common :as jc]
   '[saos-tm.extractor.judgment-links :as jl]
   '[clj.common :as cljc])

(defn conv-judgment-to-tag [ id->referencing-ids j]
  (let [
         id
           (:id j)
         referencing-ids
           (id->referencing-ids (str id))
       ]
    (if-not (empty? referencing-ids)
      [ { :judgmentId id
          :tagType "REFERENCING_COURT_CASES"
          :value referencing-ids } ]
      [])))

(defn process [id->referencing-ids inp-fname out-fname]
  (let [
         inp-data
           (cljc/read-json inp-fname true)
          out-data
             (mapcat
               (partial conv-judgment-to-tag id->referencing-ids)
               inp-data)
       ]
    (sc/spit-compr
      out-fname
      (cc/generate-string out-data {:pretty true}))))

(defn run [argv]
  (let [
         id->referencing-ids
           (cljc/read-json (first argv) false)
         argv*
           (rest argv)
         n
           (quot (count argv*) 2)
         inp-fnames
           (take n argv*)
         out-fnames
           (drop n argv*)
        ]
  (dorun
    (map
      (partial process id->referencing-ids)
      inp-fnames
      out-fnames))))

(when (> (count *command-line-args*) 0)
  (run  *command-line-args*))
