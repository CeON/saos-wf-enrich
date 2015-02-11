(require
   '[clojure.set :as set]
   '[clojure.string :as str]
   '[cheshire.core :as cc]
   '[langlab.core.parsers :as lp]
   '[squeezer.core :as sc]
   '[saos-tm.extractor.osp-parties :as pt])

(def start-anon "<span class=\"anon-block\">")

(def stop-anon "</span>")

(def start-stop-anon-re
  (re-pattern (str start-anon "|" stop-anon)))

(defn is-start-anon? [^String s]
  (= s start-anon))

(defn is-stop-anon? [^String s]
  (= s stop-anon))

(defn handle-start-anon [ s-seq ]
  (let [
         s3-seq (take 3 s-seq)
         s3-len (count s3-seq)
       ]
    (if (and
          (= s3-len 3)
          (is-stop-anon? (nth s3-seq 2)))
      [ (nth s3-seq 1)
        (drop 3 s-seq) ]
      [ (first s-seq) (rest s-seq) ])))

(defn strip-anon [ s ]
  (if-not (string? s)
    (map strip-anon s)
    (loop [
           result []
           remaining (lp/split* s start-stop-anon-re)
         ]
     (let [
            [ new remaining* ]
              (if (is-start-anon? (first remaining))
                (handle-start-anon remaining)
                [ (first remaining) (rest remaining) ])
             result*
              (conj result new)
          ]
       (if (empty? remaining*)
         (str/trim (apply str result*))
         (recur result* remaining*))))))

(defn clean-parties [ p ]
  (if p
    (zipmap (keys p) (map strip-anon (vals p)))
    {}))

(defn conv-judgment-to-tag [j]
  (if-not (= (:judgmentType j) "SENTENCE")
    []
    (let [
          id (:id j)
          court-type (:courtType j)
          include-judgment?
             (and  (= "COMMON" court-type)
                   (not= "Pracy i Ubezpieczeń Społecznych" (get-in j [:division :type])))
          parties
            (if include-judgment?
              (clean-parties (pt/extract-parties-osp-civil (:textContent j)))
              {})
          ]
    (if (empty? parties)
      []
      [ { :id id
         :tagType "PARTIES"
         :value parties} ]))))

(defn conv-judgment-to-tag* [j]
  (if-not (= (:judgmentType j) "SENTENCE")
    []
    (let [
          id (:id j)
          court-type (:courtType j)
          include-judgment?
             (= "COMMON" court-type)
          parties
            (if include-judgment?
              (if (= "Karny" (get-in j [:division :type]))
                (clean-parties (pt/extract-parties-osp-criminal (:textContent j)))
                (clean-parties (pt/extract-parties-osp-civil (:textContent j))))
              {})
          ]
    (if (empty? parties)
      []
      [ { :id id
         :tagType "PARTIES"
         :value parties} ]))))

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
