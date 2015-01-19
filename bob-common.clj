(require '[ clojure.string :as str])

(def MOD-PATH "../../")

; FUNCTION DEFINITIONS

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
      (str MOD-PATH "inp/json/out/") #"commo.*json.gz")))

(def SUPRE-COURT-JSON-FILES
  (sort
    (ls-cur-dir-with-path
      (str MOD-PATH "inp/json/out/") #"supre.*json.gz")))

(def ALL-COURT-JSON-FILES
  (concat
    COMMO-COURT-JSON-FILES
    SUPRE-COURT-JSON-FILES))

; OUTPUT FILES

(def CASE-DICT-JSON-FILE
   "../../case/case-dict/out/case-dict.json.gz")

(def CASE-TAG-ALL-COURT-JSON-FILES
  (map
     #(conv-fname %
        "../../inp/json/out/"
        "../../case/case-tags/out"
        ".json.gz" "_case_tag.json.gz")
     ALL-COURT-JSON-FILES))
