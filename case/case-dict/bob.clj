(bob-module)

(load-clj-file "../../bob-common.clj")

(defrule
  [ [ CLJ-CMD
      (inp "gene-case-dict.clj")
      (out CASE-DICT-JSON-FILE)
      (inp ALL-COURT-JSON-FILES)] ])
