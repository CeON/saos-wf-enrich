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

; TOOLS

(def CLJ-CMD (str MOD-PATH "TOOLS/clj/sbin/clj"))

; INPUT FILES

(def ALL-COURT-JSON-FILES
  (sort
    (ls-cur-dir-with-path
      (str MOD-PATH "get/json/out/") #".*json.gz")))

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

(def CASE-TAG-ALL-COURT-JSON-FILES
  (conv-inp-json-to-case-tag-files ALL-COURT-JSON-FILES))
