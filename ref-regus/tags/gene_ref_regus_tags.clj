(require
   '[clojure.set :as set]
   '[clojure.java.io :as io]
   '[clojure.string :as str]
   '[cheshire.core :as cc]
   '[langlab.core.parsers :as lp]
   '[squeezer.core :as sc]
   '[saos-tm.extractor.law-links :as ell])

(defn read-law-journal-dict [fname]
  (with-open [r (sc/reader-compr fname)]
    (cc/parse-stream  r false)))

(defn get-ref-regu-title [ law-journal-dict ref-regu ]
  (let [
         key (str "D" (or (:journalYear ref-regu) "0") "/"
                      (or (:journalNo ref-regu) "0") "/"
                      (or (:journalEntry ref-regu) "0"))
        ]
     (get-in law-journal-dict [key "title"])))

(defn conv-arts-to-str [ arts ]
  (apply str
    (interpose ", "
      (map ell/convert-art-to-str arts))))

(defn clean-up-title [ title ]
  (if (re-matches #".* r.$" title)
    title
    (str/replace title #"\.$" "")))

(defn add-title-and-text-to-act [ law-journal-dict act-arts-map act]
  (if-let [ title
              (get-ref-regu-title law-journal-dict act) ]
    (let [
           text
             (str
               (clean-up-title title)
               " (" (ell/conv-act-to-str act)
               " - " (conv-arts-to-str (ell/sort-arts (act-arts-map act))) ")" )
          ]
      [ (assoc act :journalTitle title :text text) ])
    []))

(defn normalize-act-arts-pair [ [act arts] ]
  [ act (into [] (distinct arts))])

(defn merge-act-arts-maps
  ([ m1 m2 ] (merge-with concat m1 m2))
  ([] {}))

(defn normalize-ref-regus [ law-journal-dict ref-regus-raw ]
  (let [
         ref-regus-raw-act-distinct
           (into [] (distinct (map :act ref-regus-raw)))
         ref-regus-act-arts-map-raw
           (reduce merge-act-arts-maps
             (map
               #(array-map (:act %) (vector (:art %)))
               ref-regus-raw))
          ref-regus-act-arts-map
            (into {}
              (map
                normalize-act-arts-pair
                ref-regus-act-arts-map-raw))
        ]
    (mapcat
      (partial
        add-title-and-text-to-act law-journal-dict ref-regus-act-arts-map)
      ref-regus-raw-act-distinct)))

(defn conv-raw-to-tag [law-journal-dict raw-tag]
  (let [
           normalized-ref-regus
             (normalize-ref-regus law-journal-dict (:value raw-tag))
          ]
    (if-not (empty? normalized-ref-regus)
      (assoc raw-tag :value normalized-ref-regus)
      (assoc raw-tag :value nil))))

(defn process [law-journal-dict inp-fname out-fname]
  (let [
         inp-data
          (-> inp-fname
            sc/slurp-compr
            (cc/parse-string true))
         out-data
          (map
            (partial conv-raw-to-tag law-journal-dict)
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
