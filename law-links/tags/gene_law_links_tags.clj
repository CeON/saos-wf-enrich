(require
   '[clojure.set :as set]
   '[clojure.java.io :as io]
   '[clojure.string :as str]
   '[cheshire.core :as cc]
   '[langlab.core.parsers :as lp]
   '[squeezer.core :as sc]
   '[saos-tm.extractor.law-links :as ll])

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

(def act-dictionary
  (ll/load-dictionary (io/resource "act_dictionary.txt")))

(defn conv-judgment-to-tag [j]
  (let [
          id (:id j)
          court-type (:courtType j)
          text
            (if (= court-type "COMMON")
              (try
                 (conv-html-to-text
                   (:textContent j))
                 (catch Exception e
                   (println "Problem for id=" id)
                   (:textContent j)))
              nil)
          law-links
            (when text
              (try
                (ll/extract-law-links text act-dictionary)
              (catch Exception e
                (println "Links problem for id=" id)
                nil)))
          ]
    [ { :id id
        :tagType "LAW_LINKS"
        :value (:extracted-links law-links) } ]))

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
