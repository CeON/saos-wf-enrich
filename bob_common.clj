(require '[ clojure.string :as str])
(require '[ me.raynes.fs :as fs])

(def MOD-PATH "../../")

; TOOLS

(def CLJ-CMD (str MOD-PATH "TOOLS/clj/sbin/clj"))
(def CLQ-CMD (str MOD-PATH "TOOLS/clj/sbin/clq"))

; GLOBAL FUNCTION DEFINITIONS

(defn conv-fname [ fname old-prefix new-prefix old-suffix new-suffix ]
  (-> fname
      (str/replace (re-pattern (str "^" old-prefix)) new-prefix)
      (str/replace (re-pattern (str old-suffix "$")) new-suffix)))

(defn filter-files [ base-regexp fnames ]
  (filter
    #(re-find base-regexp (fs/base-name %))
    fnames))

(defn gen-partitioned-rules [ n gen-rule-f & file-lists ]
  (apply (partial map gen-rule-f)
    (map (partial partition-all n) file-lists)))

(defn gen-stat-rules [court-type inp-q-script inp-files out-dir out-suffix]
  (let [
         court-type-re
           (if (= court-type "every_court")
             #".*"
             (re-pattern (str "^" court-type)))
         inp-files-selected
           (filter-files court-type-re inp-files)
         out-file
           (str out-dir "/" court-type out-suffix)
        ]
    [ [ (file CLQ-CMD) "-p1" (inp inp-q-script) (inp inp-files-selected)
          ">" (out out-file) ]]))

(defn sort* [ coll ]  (sort coll))

; (defn sort* [ coll ]
;  (take 2 (sort coll)))

(def APPEA-CHAMB-FILES
  (sort*
    (ls-cur-dir-with-path
      (str MOD-PATH "get/rest/out") #"^appea_chamb.*json.gz")))

(def COMMO-COURT-FILES
  (sort*
    (ls-cur-dir-with-path
      (str MOD-PATH "get/rest/out") #"^commo_court.*json.gz")))

(def CONST-TRIBU-FILES
  (sort*
    (ls-cur-dir-with-path
      (str MOD-PATH "get/rest/out") #"^const_tribu.*json.gz")))

(def SUPRE-COURT-FILES
  (sort*
    (ls-cur-dir-with-path
      (str MOD-PATH "get/rest/out") #"^supre_court.*json.gz")))

(def EVERY-COURT-FILES
  (concat APPEA-CHAMB-FILES COMMO-COURT-FILES
          CONST-TRIBU-FILES SUPRE-COURT-FILES))

; OUTPUT FILES

;; REF CASES TAG

(def REF-CASES-DICT-FILE
   "../../ref-cases/dict/out/ref_cases_dict.json.gz")

(def REF-CASES-TAG-FILES
  (map
     #(conv-fname %
        "../../get/rest/out/" "../../ref-cases/tags/out/json/"
        ".json.gz" "_ref_cases_tag.json.gz")
     EVERY-COURT-FILES))

;; CASe ParTIES tag

(def CAS-PTIES-TAG-FILES
  (map
     #(conv-fname %
        "../../get/rest/out/" "../../cas-pties/tags/out/json/"
        ".json.gz" "_cas_pties_tag.json.gz")
    COMMO-COURT-FILES))

;; REFerenced MONEY

(def REF-MONEY-TAG-FILES
  (map
     #(conv-fname %
        "../../get/rest/out/" "../../ref-money/tags/out/json/"
        ".json.gz" "_ref_money_tag.json.gz")
     EVERY-COURT-FILES))

;; REFerenced REGUlationS

(def REF-REGUS-INP-FILES
  (concat APPEA-CHAMB-FILES CONST-TRIBU-FILES SUPRE-COURT-FILES))

(def REF-REGUS-RAW-FILES
  (map
     #(conv-fname %
        "../../get/rest/out/"
        "../../ref-regus/raws/out/json/"
        ".json.gz" "_ref_regus_raw.json.gz")
     REF-REGUS-INP-FILES))

(def REF-REGUS-TAG-FILES
  (map
     #(conv-fname %
        "../../get/rest/out/"
        "../../ref-regus/tags/out/json/"
        ".json.gz" "_ref_regus_tag.json.gz")
     REF-REGUS-INP-FILES))

(def LAW-JOURN-DICT-FILE
  "../../ext/law_journal_dict.json.gz")
