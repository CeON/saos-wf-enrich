(bob-module)

(load-clj-file "../../bob_common.clj")

(def N 20)

(defn gen-ref-regus-rule [ raw-files tag-files ]
  [ (file CLJ-CMD)
    (inp "./gene_ref_regus_tags.clj")
    (inp LAW-JOURN-DICT-FILE)
    (inp raw-files)
    (out tag-files) ])

(defrule
  (gen-partitioned-rules N gen-ref-regus-rule
    REF-REGUS-RAW-FILES REF-REGUS-TAG-FILES))
