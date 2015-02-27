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
   "../../ref-cases/dict/out/case_dict.json.gz")

(defn conv-inp-json-to-case-tag-files [files]
  (map
     #(conv-fname %
        "../../get/rest/out/"
        "../../ref-cases/tags/out/json/"
        ".json.gz" "_ref_cases_tag.json.gz")
     files))

(def REF-CASES-TAG-FILES
  (conv-inp-json-to-case-tag-files EVERY-COURT-FILES))

;; CASe ParTIES tag

(def CAS-PTIES-TAG-FILES
  (map
     #(conv-fname %
        "../../get/rest/out/"
        "../../cas-pties/tags/out/json/"
        ".json.gz" "_cas_pties_tag.json.gz")
    COMMO-COURT-FILES))

;; REFerenced REGUlationS

(def REF-REGUS-INP-FILES
  (concat APPEA-CHAMB-FILES CONST-TRIBU-FILES SUPRE-COURT-FILES))

(def REF-REGUS-TAG-FILES
  (map
     #(conv-fname %
        "../../get/rest/out/"
        "../../ref-regus/tags/out/json"
        ".json.gz" "_ref_regus_tag.json.gz")
     REF-REGUS-INP-FILES))
