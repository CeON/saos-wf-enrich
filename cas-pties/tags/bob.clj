(bob-module)

(load-clj-file "../../bob_common.clj")

(def N 20)

(def Q-FILES
  [ "q_defendant_frq.clj"
    "q_defendant_tot.clj"
    "q_plaintiff_frq.clj"
    "q_plaintiff_tot.clj"
    "q_prosecutor_frq.clj"
    "q_prosecutor_tot.clj" ])

(def ANS-FILES
  [ "out/stat/defendant_frq.txt"
    "out/stat/defendant_tot.txt"
    "out/stat/plaintiff_frq.txt"
    "out/stat/plaintiff_tot.txt"
    "out/stat/prosecutor_frq.txt"
    "out/stat/prosecutor_tot.txt" ])

(defn gen-query-rule [ query-file ans-file]
  [ (file CLQ-CMD)
    (inp query-file)
    (inp CAS-PTIES-TAG-FILES)
    ">" (out ans-file)])

(defn gen-parties-tags-rule [judgment-files tag-files]
  [ (file CLJ-CMD)
    (inp "./gene_parties_tags.clj")
    (inp judgment-files)
    (out tag-files) ])

(defrule
  (concat
     (map gen-query-rule Q-FILES ANS-FILES)
     (gen-partitioned-rules
        N gen-parties-tags-rule
        COMMO-COURT-FILES
        CAS-PTIES-TAG-FILES)))
