(require
  '[clojure.string :as str]
  '[clj.common :as com]
  '[cheshire.core :as ches])

(defn remove-justification [s]
  (first
    (str/split s #"(?i)<h2>UZASADNIENIE</h2>")))

(defn flatten-reason [s]
  (cond
    (re-find #"^o alimenty" s) "o alimenty"
    (re-find #"^o podwyższenie alimentów" s) "o podwyższenie alimentów"
    (re-find #"^o obniżenie alimentów" s) "o obniżenie alimentów"
    :else "NIEZNANE"))

(defn get-reason [s]
  (as->
    s ---
    (remove-justification ---)
    (re-find #"(?i)p> *o [^<]*" ---)
    (str/replace --- "p>" "")
    (str/trim ---)
    (flatten-reason ---)))

(defn get-defendant-sex [ s ]
  (let [
          male
            (some?
              (or
                (re-find #"(?i)\spozwany\s" s)
                (re-find #"(?i)\spozwanego\s" s)))
          female
            (some?
              (or
                (re-find #"(?i)\spozwana\s" s)
                (re-find #"(?i)\spozwanej\s" s)))
       ]
    (case [male female]
       [ true true ] "both"
       [ false true ] "female"
       [ true false ] "male"
       [ false false ] "?")))

(defn conv-summary-to-map [ lines ]
  (zipmap
    (map #(keyword (str "summary" %)) (range))
    lines))

(defn not-summary-clutter? [s]
  (not (or (str/blank? s)
           (some? (re-find #"^na skutek apelacji" s))
           (some? (re-find #"^od wyroku" s))
           (some? (re-find #"^z dnia" s))
           (some? (re-find #"^oraz z" s))
           (some? (re-find #"^oraz" s))
           (some? (re-find #"^przeciwko " s))
           (some? (re-find #"^o " s))
           (some? (re-find #"^w " s))
           (some? (re-find #"(?i)^sygn" s)))))

(defn clean-summary-entry [s]
  (-> s
   (str/replace  "<strong>" "")
   (str/replace  "</strong>" "")
   (str/replace  "-->" "")
   (str/replace  "<!--" "")
   (str/replace  "<em>" "")
   str/trim))

(defn get-raw-summary [ s ]
  (as->
    s ---
    (remove-justification ---)
    (str/split --- #"(?i)p> *o [^<]*</p>" 2)
    (second ---)
    (str ---)))

(defn remove-numbers [ s ]
  (-> s
    (str/replace #"^[VIX]+[\.\-/]?[\u00a0 ]" "")
    (str/replace #"^[0-9]+." "")
    (str/replace #"^[\s\u00a0]*" "")))

(defn get-summary [s]
  (as->
    s ---
    (get-raw-summary ---)
    (str/split --- #"<p>")
    (map clean-summary-entry ---)
    (filter not-summary-clutter? ---)
    (map remove-numbers ---)))

(defn get-result [ raw-summary summaries ]
  (let [
          summary0
            (if-let [ s0 (first summaries) ]
              s0
              "")

          increases
            (if (re-find #"(?i)podwyższa" raw-summary)
              "podwyższa:"
              "")
          decreases
            (if (re-find #"(?i)obniża" raw-summary)
              "obniża:"
              "")
          dismisses
            (if (re-find #"(?i)oddala" summary0)
              "oddala:"
              "")
          repeals
            (if (re-find #"(?i)^uchyla" summary0)
              "uchyla:"
              "")
          awards
            (if (re-find #"(?i)^zasądza" summary0)
              "zasądza:"
              "")
          changes
            (if (re-find #"(?i)^zmienia" summary0)
              "zmienia:"
              "?")
        ]
    (re-find
      #"[^:]*"
      (str increases decreases dismisses repeals awards changes))))

(defn get-is-appeal [ raw-summary ]
   (some? (re-find #"(?i)na skutek apelacji" raw-summary)))

(defn conv-record-to-tag [ id is-appeal defendant-sex reason result ]
  { :judgmentId id
    :tagType "ANA_ALIMN"
    :value { :isAppeal is-appeal
             :reason reason
             :defendantSex defendant-sex
             :result result }})

(defn conv-record-to-tag-extended [ id is-appeal defendant-sex reason result
                                  summary-raw summary-list]
  { :judgmentId id
    :tagType "ANA_ALIMN"
    :value (merge
             { :isAppeal is-appeal
               :reason reason
               :defendantSex defendant-sex
               :result result
               :summaryRaw summary-raw }
             (conv-summary-to-map summary-list)) })

(defn conv-record-to-csv [ tag ]
  (format "https://saos-test.icm.edu.pl/judgments/%d, %5s, %6s, %s, %s\n"
    (:judgmentId tag)
    (str (get-in tag [:value :isAppeal]))
    (get-in tag [:value :defendantSex])
    (get-in tag [:value :reason])
    (get-in tag [:value :result])))

(defn generate-csv [tags]
  (apply str
    (map conv-record-to-csv tags)))

(defn generate-json [tags]
  (ches/generate-string
    tags
    {:pretty true }))

(defn run [argv]
  (let [
         judgments
           (com/read-json (first argv))
         texts
           (map :textContent judgments)
         defendant-sexes
           (map get-defendant-sex texts)
         ids
           (map #(:id %) judgments)
         reasons
           (map get-reason texts)
         raw-summaries
           (map get-raw-summary texts)
         summaries
           (map get-summary texts)
         results
           (map get-result raw-summaries summaries)
         is-appeals
           (map get-is-appeal raw-summaries)
         tags
           (map conv-record-to-tag ids is-appeals defendant-sexes reasons results)
         tags-extended
           (map conv-record-to-tag-extended
              ids is-appeals defendant-sexes reasons results raw-summaries summaries)
        ]
    (print (generate-json tags))))

(when (> (count *command-line-args*) 0)
  (run  *command-line-args*))
