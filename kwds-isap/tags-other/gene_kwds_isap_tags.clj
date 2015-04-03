(require
  '[cheshire.core :as cc]
  '[squeezer.core :as sc]
  '[clj.query :as q])

(defn keywordize-law-journal-dict-entry [ entry ]
  (let [
         keywords
           (get entry "keywords")
        ]
    (if (empty? keywords)
      {}
      (zipmap
        keywords
        (repeat (float (/ 1.0 (count keywords))))))))


(defn read-law-journal-dict [ fname ]
  (let [
         raw-law-journal-dict
           (with-open [r (sc/reader-compr fname)]
             (cc/parse-stream  r false))
         keys
          (keys raw-law-journal-dict)
         vals-new
           (map
             keywordize-law-journal-dict-entry
             (vals raw-law-journal-dict))
        ]
    (zipmap keys vals-new)))

(defn conv-ref-regu-to-key [ ref-regu ]
  (let [
         x (str "D" (or (:journalYear ref-regu) 0) "/"
                    (or (:journalNo ref-regu) 0)"/"
                    (or (:journalEntry ref-regu) 0))
        ]
     x))

(defn conv-one-ref-regu-to-keywords [law-journal-dict ref-regu]
  (get law-journal-dict
       (conv-ref-regu-to-key ref-regu)
       {}))

(defn conv-ref-regus-to-keywords [law-journal-dict ref-regus]
  (reduce
    (partial merge-with +)
    (map
      (partial conv-one-ref-regu-to-keywords law-journal-dict)
      ref-regus)))

(defn conv-map-to-sorted-pairs [ isap-keywords ]
  (sort-by #(- (second %)) isap-keywords))

(defn filter-keywords [ isap-keywords]
  (into {}
    (filter #(> (second %) 0.11) isap-keywords)))


(defn conv-regu-tag-to-keywords-tag [law-journal-dict tag]
  (let [
          id (:id tag)
          ref-regus
            (distinct (map #(get % :act []) (:value tag)))
          isap-keywords
            (filter-keywords
              (conv-ref-regus-to-keywords
                law-journal-dict ref-regus))
        ]
    (if-not (empty? ref-regus)
      [ { :id id
          :tagType "KWD_ISAP"
          :value {
                   :keywordsISAP isap-keywords
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
              (partial conv-regu-tag-to-keywords-tag law-journal-dict)
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
