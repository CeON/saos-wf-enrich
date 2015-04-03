(require '[ clojure.string :as str])
(require '[ me.raynes.fs :as fs])

(def MOD-PATH "../../")

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

; TOOLS

(def CLJ-CMD (str MOD-PATH "TOOLS/clj/sbin/clj"))
(def CLQ-CMD (str MOD-PATH "TOOLS/clj/sbin/clq"))

; INPUT FILES

(def EVERY-COURT-FILES
  (sort
    (ls-cur-dir-with-path
      (str MOD-PATH "get/rest/out") #".*json.gz")))

(def APPEA-CHAMB-FILES
  (filter-files #"^appea_chamb" EVERY-COURT-FILES))

(def COMMO-COURT-FILES
  (filter-files #"^commo_court" EVERY-COURT-FILES))

(def CONST-TRIBU-FILES
  (filter-files #"^const_tribu" EVERY-COURT-FILES))

(def SUPRE-COURT-FILES
  (filter-files #"^supre_court" EVERY-COURT-FILES))

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

(def REF-REGUS-TAG-FILES
  (map
     #(conv-fname %
        "../../get/rest/out/"
        "../../ref-regus/tags/out/json/"
        ".json.gz" "_ref_regus_tag.json.gz")
     REF-REGUS-INP-FILES))

;; KEYWORDS ISAP

(def LAW-JOURN-DICT-FILE
  "../../ext/law_journal_dict.json.gz")

(def KWDS-ISAP-TAG-FILES
  (map
     #(conv-fname %
        "../../get/rest/out/"
        "../../kwds-isap/tags-cc/out/json/"
        ".json.gz" "_kwds_isap_tag.json.gz")
    COMMO-COURT-FILES))

(def KWDS-ISAP-OTHER-TAG-FILES
  (map
     #(conv-fname %
        "../../ref-regus/tags/out/json/"
        "../../kwds-isap/tags-other/out/json/"
        "_ref_regus_tag.json.gz" "_kwds_isap_tag.json.gz")
    REF-REGUS-TAG-FILES))
