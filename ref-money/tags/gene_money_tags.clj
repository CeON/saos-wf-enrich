;(ns clj.ref-regus
(require '[clojure.string :as str ]
         '[langlab.core.parsers :as parsers]
         '[langlab.core.characters :as chars]
         '[automat.core :as a])

(defn is-number-token? [s]
  (some? (re-matches #"[0-9.]*[,.;]?|[0-9.]*\,[0-9]*[,.;]?" s)))

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

(defn parse-to-maps [ s ]
  (as->
    s ---
    (parse-to-tokens ---)
    (map
      #(hash-map :token  %1 :index %2)
      --- (range))
    (filter is-not-blank-token-map? ---)
    (map keywordize-token-map ---)))

(def a-money
  [ (a/$ :init)
    (a/+ [ :num (a/$ :num)])
    (a/? [ (a/or "tys." "mln" "tys" "mln.")  (a/$ :suffix)])
    (a/or "zł." "zł," "zł")])

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
                     }
      :signal :token }))

(defn conv-state-to-res [state tokens]
  [ (:index
       (nth tokens (:start-index state)))
    (:index
       (nth tokens (:stream-index state) (last tokens)))
    (get-in state [:value :money])])

(defn extract-money-refs-from-token-maps [ results tokens ]
  (let [
          state (a/find a-money-c nil tokens)
          n (:stream-index state)
       ]
    (if-not (:accepted? state)
      results
      (recur
         (conj results (conv-state-to-res state tokens))
         (drop n tokens)))))

(defn extract-money-refs [s]
  (extract-money-refs-from-token-maps [] (parse-to-maps s)))
