(require
   '[clojure.set :as set]
   '[clojure.java.io :as io]
   '[clojure.string :as str]
   '[cheshire.core :as cc]
   '[langlab.core.parsers :as lp]
   '[squeezer.core :as sc]
   '[saos-tm.extractor.common :as lc]
   '[saos-tm.extractor.law-links :as ll])

(def act-dictionary
  (ll/load-dictionary (io/resource "act_dictionary.txt")))

(defn conv-judgment-to-tag [j]
  (let [
          id (:id j)
          court-type (:courtType j)
          text
            (if (= court-type "COMMON")
              (try
                 (lc/conv-html-to-text
                   (:textContent j))
                 (catch Exception e
                   (println "Problem for id=" id)
                   nil))
              (:textContent j))
          law-links
            (if text
               (try
                 (:extracted-links
                    (ll/extract-law-links text act-dictionary))
                (catch Exception e
                  (do
                    (println "Link problem for id=" id)
                    [])))
                [])
          ]
    [ { :id id
        :tagType "LAW_LINKS"
        :value law-links } ]))

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
