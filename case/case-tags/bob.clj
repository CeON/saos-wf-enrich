(bob-module)

(load-clj-file "../../bob-common.clj")

;(defn gen-rule [inp-fname out-fname]
;  [ CMD-CLJ 
;    (inp "gene-case-tags.clj")
;    (inp CASE-DICT-JSON-FILE) 
;    (inp inp-fname)
;    (out out-fname)]

(defrule
  [ [ CLJ-CMD
      (inp "./gene-case-tags.clj")
      (inp CASE-DICT-JSON-FILE) 
      (inp  ALL-COURT-JSON-FILES)
      (out  CASE-TAG-ALL-COURT-JSON-FILES)] ])

