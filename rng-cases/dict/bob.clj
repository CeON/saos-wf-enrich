(bob-module)

(load-clj-file "../../bob_common.clj")

(defrule
  [ [ (file CLJ-CMD)
      (inp "./gene_rng_cases_tags.clj")
      (out RNG-CASES-DICT-FILE)
      (inp REF-CASES-TAG-FILES)] ])
