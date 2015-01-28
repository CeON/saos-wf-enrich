(bob-module)

(load-clj-file "../../bob_common.clj")

(defrule
  [ [ (file CLJ-CMD)
      (inp "gene-case-dict.clj")
      (out CASE-DICT-JSON-FILE)
      (inp ALL-COURT-JSON-FILES)] ])
