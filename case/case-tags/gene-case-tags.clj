(require
   '[clojure.set :as set]
   '[cheshire.core :as cc]
   '[squeezer.core :as sc]
   '[saos-tm.extractor.judgment-links :as jl])

(import '[org.apache.commons.io IOUtils]
        '[org.apache.tika.parser Parser ParseContext]
        '[org.apache.tika.parser.html HtmlParser]
        '[org.apache.tika.language LanguageIdentifier]
        '[org.apache.tika.metadata Metadata]
        '[org.apache.tika Tika]
        '[org.apache.tika.sax BodyContentHandler])

(defn conv-html-to-text [^String s]
  (let [
         istream (IOUtils/toInputStream s "UTF-8");
         parser (HtmlParser.)
         context (ParseContext.)
         metadata (Metadata.)
         handler (BodyContentHandler.)
       ]
    (.set context Parser parser)
    (.parse parser istream handler metadata context)
    (.toString  handler)))

(defn conv-judgment-to-tag [ case-number->ids j]
  (let [
         id (:id j)
         court-type (:courtType j)
         this-case-numbers
           (into
             #{}
             (map :caseNumber (:courtCases j)))
         text
           (if (= court-type "COMMON")
             (try
                (conv-html-to-text
                  (:textContent j))
                (catch Exception e
                  (println "Problem for id=" id)
                  (:textContent j)))
             (:textContent j))
         dirty-referenced-case-numbers
           (jl/extract-all-signatures text)
         referenced-case-numbers
           (set/difference
             dirty-referenced-case-numbers this-case-numbers)
         referenced-case-numbers-tag-value
           (map
             #(hash-map :referencedCaseNumber %
                        :referencedIds (case-number->ids %))
             referenced-case-numbers)
       ]
    { :id id
      :tagType "REFERENCED_CASE_NUMBERS"
      :value referenced-case-numbers-tag-value}))

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
    (sc/spit-compr
      out-fname
      (cc/generate-string out-data {:pretty true}))))

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
