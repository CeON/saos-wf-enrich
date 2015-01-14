(def MOD-PATH "../../")

(def CLJ-CMD (str MOD-PATH "TOOLS/clj/sbin/clj"))

(def COMMO-COURT-JSON-FILES 
  (ls-cur-dir-with-path (str MOD-PATH "inp/json/out/") #"commo.*json.gz"))

(def SUPRE-COURT-JSON-FILES
  (ls-cur-dir-with-path (str MOD-PATH "inp/json/out/") #"supre.*json.gz"))

(def ALL-COURT-JSON-FILES
  (concat
    COMMO-COURT-JSON-FILES
    SUPRE-COURT-JSON-FILES))
