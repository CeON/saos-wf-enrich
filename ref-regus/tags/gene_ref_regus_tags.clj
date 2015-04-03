(require
   '[clojure.set :as set]
   '[clojure.java.io :as io]
   '[clojure.string :as str]
   '[cheshire.core :as cc]
   '[langlab.core.parsers :as lp]
   '[squeezer.core :as sc]
   '[saos-tm.extractor.common :as lc]
   '[saos-tm.extractor.law-links :as ll])

(defn read-law-journal-dict [fname]
  (with-open [r (sc/reader-compr fname)]
    (cc/parse-stream  r false)))

(def act-dictionary
  (ll/load-dictionary (io/resource "act_dictionary.txt")))

(defn get-ref-regu-title [ law-journal-dict ref-regu ]
  (let [

         key (str "D" (or (:journalYear ref-regu) "0") "/"
                      (or (:journalNo ref-regu) "0") "/"
                      (or (:journalEntry ref-regu) "0"))
        ]
     (get-in law-journal-dict [key "title"])))

(defn normalize-ref-regu [ law-journal-dict ref-regu ]
  (let [
         title
          (get-ref-regu-title law-journal-dict (:act ref-regu))
        ]
    (if title
      [ (assoc-in ref-regu [:act :journalTitle ] title) ]
      [])))

(defn conv-judgment-to-tag [law-journal-dict j]
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
                    (ll/extract-law-links text act-dictionary))
                (catch Exception e
                  (do
                    (println
                      (format
                        "ERROR, extracting referenced regulations for id=%d failed" id))
                    [])))
                [])
           ref-regus
             (mapcat
               (partial normalize-ref-regu law-journal-dict)
               ref-regus-raw)
          ]
    (if-not (empty? ref-regus)
      [ { :id id
          :tagType "REF_REGUS"
          :value ref-regus } ]
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
