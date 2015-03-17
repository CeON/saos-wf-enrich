(require '[clj.query :refer :all])

(def monster-regexp #"p> *o +podwyższenie +alimentów|p> *o +alimenty|p> *o +obniżenie +alimentów|p> *o +uchylenie +obowiązku +alimentacyjnego|p> *o +ustalenie +wygaśnięcia +obowiązku +alimentacyjnego|p> *o +wygaśnięcie +obowiązku +alimentacyjnego|p> *o +uchylenie +alimentów|p> *o +zasądzenie +alimentów|p> *o +ustalenie, +że +obowiązek +alimentacyjny +ustał|p> *o +ustalenie, +że +obowiązek +alimentacyjny +wygasł|p> *o +ustalenie +ojcostwa +i +alimenty|p> *o +zmianę +orzeczenia +w +zakresie +alimentów|p> *o +ustalenie +ustania +obowiązku +alimentacyjnego|p> *o +zmianę +obowiązku +alimentacyjnego")

(def small-regexp
  #"(?i)p> *o +podwyższenie +alimentów|p> *o +alimenty|p> *o +obniżenie +alimentów")

;(defn refers-to-alimony? [j]
;  (re-find #"(?i)aliment[^a]|^aliment[^a]" (:textContent j)))

;(defn refers-to-alimony? [j]
;  (re-find #"(?i)obowiąz[^ ]* +aliment" (:textContent j)))

(defn refers-to-alimony? [j]
  (and (= "SENTENCE" (:judgmentType j))
       (re-find small-regexp (:textContent j))))

{
   :transform-f
     (fn [judgments]
       (into []
         (filter refers-to-alimony? judgments)))
   :merge-f
     concat
   :print-f
     print-json
}
