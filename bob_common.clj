(require '[ clojure.string :as str])

(def MOD-PATH "../../")

; GLOBAL FUNCTION DEFINITIONS

(defn conv-fname [ fname old-prefix new-prefix old-suffix new-suffix ]
  (-> fname
        (str/replace (re-pattern (str "^" old-prefix)) new-prefix)
              (str/replace (re-pattern (str old-suffix "$")) new-suffix)))

; TOOLS

(def CLJ-CMD (str MOD-PATH "TOOLS/clj/sbin/clj"))

; INPUT FILES

(def COMMO-COURT-JSON-FILES
  (sort
    (ls-cur-dir-with-path
      (str MOD-PATH "get/json/out/") #"commo.*json.gz")))

(def SUPRE-COURT-JSON-FILES
  (sort
    (ls-cur-dir-with-path
      (str MOD-PATH "get/json/out/") #"supre.*json.gz")))

(def ALL-COURT-JSON-FILES
  (concat
    COMMO-COURT-JSON-FILES
    SUPRE-COURT-JSON-FILES))

; OUTPUT FILES

(def CASE-DICT-JSON-FILE
   "../../case/case-dict/out/case-dict.json.gz")

(defn conv-inp-json-to-case-tag-files [files]
  (map
     #(conv-fname %
        "../../get/json/out/"
        "../../case/case-tags/out/tags"
        ".json.gz" "_case_tag.json.gz")
     files))

;; CASE TAG

(def CASE-TAG-SUPRE-COURT-JSON-FILES
  (conv-inp-json-to-case-tag-files SUPRE-COURT-JSON-FILES))

(def CASE-TAG-COMMO-COURT-JSON-FILES
  (conv-inp-json-to-case-tag-files COMMO-COURT-JSON-FILES))

(def CASE-TAG-ALL-COURT-JSON-FILES
  (conv-inp-json-to-case-tag-files ALL-COURT-JSON-FILES))

;; PARTIES-TAG

(def PARTIES-TAG-COMMO-COURT-JSON-FILES
  (map
     #(conv-fname %
        "../../get/json/out/"
        "../../parties/tags/out/tags"
        ".json.gz" "_parties_tag.json.gz")
     COMMO-COURT-JSON-FILES))

;; LAW-LINKS-TAG

(def LAW-LINKS-TAG-ALL-COURT-JSON-FILES
  (map
     #(conv-fname %
        "../../get/json/out/"
        "../../law-links/tags/out/tags"
        ".json.gz" "_law_links_tag.json.gz")
     ALL-COURT-JSON-FILES))
