(bob-module)

(load-clj-file "../../bob_common.clj")

(defrule
  [ [ (file CLJ-CMD)
      (inp "./gene_ref_cases_dict.clj")
      (out REF-CASES-DICT-FILE)
      (inp EVERY-COURT-FILES)] ])
