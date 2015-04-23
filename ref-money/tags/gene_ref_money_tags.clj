;(ns clj.ref-regus

(require
  '[clojure.string :as str ]
  '[langlab.core.parsers :as parsers]
  '[langlab.core.characters :as chars]
  '[automat.core :as a])

(require
   '[cheshire.core :as cc]
   '[squeezer.core :as sc]
   '[saos-tm.extractor.common :as jc]
   '[saos-tm.extractor.judgment-links :as jl]
   '[clj.common :as cljc])

(defn is-number-token? [s]
  (some? (re-matches #"(?:[0-9]+\.?)+|(?:[0-9]+\.?)+,[0-9]*" s)))

(defn is-not-blank-token-map? [ token-map ]
  (not (chars/contains-whitespace-only? (:token token-map))))

(defn keywordize-token-map [ token-map ]
  (let [
         token (:token token-map)
       ]
    (if (is-number-token? token)
      (assoc token-map :token :num :value token)
      token-map)))

(defn parse-to-tokens [s]
  (parsers/split* s #"(?m)\s+"))

(defn conv-tokens-to-maps [ tokens ]
  (as->
    tokens ---
    (map
      #(hash-map :token  %1 :index %2)
      --- (range))
    (filter is-not-blank-token-map? ---)
    (map keywordize-token-map ---)))

(def a-money
  [ (a/$ :init)
    (a/+ [ :num (a/$ :num)])
    (a/? [ (a/or "tys." "mln" "tys" "mln.")  (a/$ :suffix)])
    (a/or "zł." "zł," "zł") (a/$ :pln)
    (a/? [ :num (a/$ :num) (a/or "gr." "gr," "gr") ])])

(defn reset-money-state [m val]
  (assoc m :money val))

(defn update-money-state [m val]
  (update-in m [:money] str val))

(def a-money-c
  (a/compile
    a-money
    { :reducers { :init
                    (fn [m _] (reset-money-state m ""))
                  :num
                    (fn [m token-map] (update-money-state m (:value token-map)))
                  :suffix
                    (fn [m token-map]
                      (case (:token token-map)
                        ("tys." "tys") (update-money-state m "000")
                        ("mln" "mln.") (update-money-state m "000000")))
                   :pln
                      (fn [m token-map]
                        (update-money-state m ","))
                  }
      :signal :token }))

(defn conv-state-to-res [state tokens]
  [ (:index
       (nth tokens (:start-index state)))
    (inc
      (:index
         (nth tokens (dec (:stream-index state)))))
    (get-in state [:value :money])])

(defn extract-money-refs-from-token-maps [ results tokens ]
  (let [
          state (a/greedy-find a-money-c nil tokens)
          n (:stream-index state)
       ]
    (if-not (:accepted? state)
      results
      (recur
        (conj results (conv-state-to-res state tokens))
        (drop n tokens)))))

(defn extract-money-refs [s]
  (let [
         tokens
           (parse-to-tokens s)
         money-refs
           (extract-money-refs-from-token-maps [] (conv-tokens-to-maps tokens))
       ]
    [ (into [] tokens) money-refs ]))

(defn conv-money-refs-to-ngrams [ tokens money-refs ]
  (map
    #(subvec tokens %1 %2)
    (map first money-refs)
    (map second money-refs)))

(defn conv-money-value-to-num [ ^String s ]
  (try
    (-> s
      (str/replace " " "")
      (str/replace "." "")
      ;; Extra coma can be added to string by the fact that zl is converted to coma
      (str/replace #",$" "")
      (str/replace "," ".")
      (Double.))
    (catch Throwable e (str "ERROR - " s))))

(defn extract-money-ref-values [s]
  (let [
         [ _ refs ] (extract-money-refs s)
       ]
    (map #(conv-money-value-to-num (nth % 2)) refs)))

;--- tag generation

(defn conv-judgment-to-tag [j]
  (let [
         id
           (:id j)
         court-type
           (:courtType j)
         text
           (if (= court-type "COMMON")
             (try
                (jc/conv-html-to-text (:textContent j))
                (catch Exception e
                  (cljc/println-err
                    (format "ERROR, converting html to text failed for id=%d" id))
                  (:textContent j)))
             (:textContent j))
         money-ref-values
           (into []
             (extract-money-ref-values text))
       ]
    (if-not (empty? money-ref-values)
      [ { :judgmentId id
          :tagType "REFERENCED_MONEY"
          :value money-ref-values } ]
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
