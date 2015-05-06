(bob-module)

(load-clj-file "../../bob_common.clj")

(def N 20)

(defn gen-ref-regus-rule [ judgment-files tag-files ]
  [ (file CLJ-CMD)
    (inp "./gene_ref_regus_raws.clj")
    (inp judgment-files)
    (out tag-files) ])

(defrule
  (gen-partitioned-rules N gen-ref-regus-rule
    REF-REGUS-INP-FILES REF-REGUS-RAW-FILES))
