(bob-module)

(load-clj-file "../../bob_common.clj")

(def N 20)

(defn gen-ref-regus-rule [ judgment-files tag-files ]
  [ (file CLJ-CMD)
    (inp "./gene_ref_regus_raws.clj")
    (inp judgment-files)
    (out tag-files) ])

(defn gen-stat-rules* [ court-type ]
  (gen-stat-rules court-type
    "./q_ref_regus_stat_sum.clj" REF-REGUS-RAW-FILES
    "out/stat" "_sum.txt"))

(defrule
  (concat
    (gen-partitioned-rules N gen-ref-regus-rule
      REF-REGUS-INP-FILES REF-REGUS-RAW-FILES)
    (mapcat
      gen-stat-rules*
      [ "every_court" "supre_court"
        "const_tribu" "appea_chamb" ])))
