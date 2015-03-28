(require
  '[cheshire.core :as cc]
  '[squeezer.core :as sc])

(defn clean-keywords [ [ k v ] ]
  (let [
          clean-f
            #(if (> (count %) 6)
               []
               %)
        ]
  [ k (update-in v [ "keywords" ] clean-f) ]))

(defn read-law-journal-dict [ fname ]
  (let [
         raw-law-journal-dict
           (with-open [r (sc/reader-compr fname)]
             (cc/parse-stream  r false))
        ]
    (into {}
      (map
        clean-keywords
        raw-law-journal-dict))))

(defn conv-ref-regu-to-key [ ref-regu ]
  (let [
         x (str "D" (:journalYear ref-regu) "/"
                    (:journalNo ref-regu) "/"
                    (:journalEntry ref-regu))
        ]
     x))

(defn conv-one-ref-regu-to-keywords [law-journal-dict ref-regu]
  (if-let [
           ref-regu-record
             (law-journal-dict (conv-ref-regu-to-key ref-regu))
          ]
       (frequencies (ref-regu-record "keywords"))
    {}))

(defn conv-ref-regus-to-keywords [law-journal-dict ref-regus]
  (reduce
    (partial merge-with +)
    (map
      (partial conv-one-ref-regu-to-keywords law-journal-dict)
      ref-regus)))

(defn conv-judgment-to-tag [law-journal-dict j]
  (let [
          id (:id j)
          court-type (:courtType j)
          ref-regus (:referencedRegulations j)
          keywords (:keywords j)
          isap-keywords (conv-ref-regus-to-keywords law-journal-dict ref-regus)
        ]
    (if-not (empty? ref-regus)
      [ { :id id
          :tagType "KWD_ISAP"
          :value {
                   :keywordsISAP isap-keywords
                   :keywords keywords
                  }} ]
      [])))

(defn process [law-journal-dict inp-fname out-fname]
  (let [
         inp-data
           (-> inp-fname
             sc/slurp-compr
             (cc/parse-string true))
          out-data
            (mapcat
              (partial conv-judgment-to-tag law-journal-dict)
              inp-data)
       ]
    (sc/spit-compr
      out-fname
      (cc/generate-string out-data {:pretty true}))))

(defn run [argv]
  (let [
         law-journal-dict
           (read-law-journal-dict (first argv))
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
      (partial process law-journal-dict)
      inp-fnames out-fnames))))

(when (> (count *command-line-args*) 0)
  (run  *command-line-args*))
