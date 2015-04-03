(bob-module)

(load-clj-file "../../bob_common.clj")

(def N 20)

(defn gen-ref-cases-tags-rule [judgment-files tag-files]
  [ (file CLJ-CMD)
    (inp "./gene_kwds_isap_tags.clj")
    (inp LAW-JOURN-DICT-FILE)
    (inp judgment-files)
    (out tag-files) ])

(defrule
  (concat
    (gen-partitioned-rules
       N gen-ref-cases-tags-rule
       REF-REGUS-TAG-FILES KWDS-ISAP-OTHER-TAG-FILES)
    [ [ (file CLQ-CMD) "-p1"
          (inp "./q_kwds_isap_stat_sum.clj")
          (inp KWDS-ISAP-OTHER-TAG-FILES)
          ">" (out "out/stat/kwds_isap_sum.txt") ]
      [ (file CLQ-CMD) "-p1"
          (inp "./q_kwds_isap_stat_frq.clj")
          (inp KWDS-ISAP-OTHER-TAG-FILES)
          ">" (out "out/stat/kwds_isap_frq.txt") ] ]))
