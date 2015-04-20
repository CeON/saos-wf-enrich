(ns clj.ref-regus
  (:require [clojure.string :as str ]
            [langlab.core.parsers :as parsers]
            [langlab.core.characters :as chars]
            [automat.core :as a]))

(def N 20)

(defn exify [ s ]
  (apply str (repeat (count s) "x")))

(defn exify-if [cond-f s]
  (if (cond-f s) (exify s) s))

(defn take-till-item [ item limit inp-seq ]
  (take N (take-while #(not= % item) inp-seq)))

(defn transform-seq
  ( [ inp-seq res ]
      (let [
         res-item
           (if (= "art." (first inp-seq))
              (concat
                 [ :art "art." ]
                   (map
                     (partial exify-if chars/contains-digits-only?)
                     (take-till-item "art." (dec N) (rest inp-seq))))
              (first inp-seq))
         n
          (if (string? res-item)
            1
            (count res-item))
         inp-seq*
           (drop n inp-seq)
         res*
           (conj res res-item)
       ]
      (if (empty? inp-seq*)
        res*
        (recur inp-seq* res*))))
  ( [ inp-seq ] (transform-seq inp-seq [])))

(defn parse-txt [ s ]
  (let [
          seq (parsers/split* s #"(?m)\s+")
        ]
    (transform-seq seq)))

(defn is-blank-itoken? [ [token index] ]
  (not (chars/contains-whitespace-only? token)))

(def ^:private token-lut
  {
   "k.p.c." :kpc
   "KPC." :kpc
   "k.p.k." :kpk
   "KPK" :kpk
   "k.c." :kc
   "KC" :kc
   "k.k." :kk
   "KK" :kk
   })

(defn keywordize-token [ token ]
  (if-let [
            token* (token-lut token)
           ]
    token*
    (cond
      (chars/contains-digits-only? token) :num
      (= (str/lower-case token) "art.") :art
      (= (str/lower-case token) "ust.") :ust
      :else token)))

(defn keywordize-itoken [ [token index] ]
  [ (keywordize-token token) index ])


(defn parse [ s ]
  (let [
         tokens
           (parsers/split* s #"(?m)\s+")
         itokens
           (map vector tokens (range))
         filtered-itokens
           (->> itokens
             (filter is-blank-itoken?)
             (map keywordize-itoken))

        ]
    filtered-itokens))

(def a-ust
  [ (a/? [ "§" :num ]) (a/? [ "ust." :num ])])

(def a-art-c
  (a/compile
     [ :art :num (a/or :kpk :kc) ]))

(defn run-automat [ s ]
  (let [
         tokens
           (into [] (parsers/split* s #"(?m)\s+"))
         itokens
           (map vector tokens (range))
         filtered-itokens
           (->> itokens
             (filter is-blank-itoken?)
             (map keywordize-itoken))
         filtered-indices
            (map second filtered-itokens)
         filtered-tokens
            (map first filtered-itokens)
         res
           (a/find a-art-c nil filtered-tokens)
         [ i j ]
           (if (:accepted? res)
             [ (nth filtered-indices
                 (:start-index res))
               (inc (nth filtered-indices
                  (dec (:stream-index res)))) ]
             [ nil nil ])
         _ (println i j)
        ]
    (if i
      (subvec tokens i j)
      [])))

(defn extract-ref-regus [s]
  nil)
