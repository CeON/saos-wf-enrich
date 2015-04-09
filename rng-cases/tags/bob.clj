(bob-module)

(load-clj-file "../../bob_common.clj")

(def N 20)

(defn gen-ref-cases-tags-rule [judgment-files tag-files]
  [ (file CLJ-CMD)
    (inp "./gene_rng_cases_tags.clj")
    (inp RNG-CASES-DICT-FILE)
    (inp judgment-files)
    (out tag-files) ])

(defrule
  (concat
    (gen-partitioned-rules
      N gen-ref-cases-tags-rule EVERY-COURT-FILES RNG-CASES-TAG-FILES)))
