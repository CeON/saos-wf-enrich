(bob-module)

(load-clj-file "../../bob_common.clj")

(def N 20)

(defn gen-ref-regus-rule [ raw-files tag-files ]
  [ (file CLJ-CMD)
    (inp "./gene_ref_regus_tags.clj")
    (inp LAW-JOURN-DICT-FILE)
    (inp raw-files)
    (out tag-files) ])

(defn gen-stat-rules* [ court-type ]
  (concat
    (gen-stat-rules court-type
       "./q_ref_regus_stat_journ_frq.clj" REF-REGUS-TAG-FILES
       "out/stat" "_journ_frq.txt")
    (gen-stat-rules court-type
       "./q_ref_regus_stat_texts_frq.clj" REF-REGUS-TAG-FILES
       "out/stat" "_texts_frq.txt")))

(defrule
  (concat
    (gen-partitioned-rules N gen-ref-regus-rule
      REF-REGUS-RAW-FILES REF-REGUS-TAG-FILES)
    (mapcat
       gen-stat-rules*
       [ "every_court" "supre_court"
         "const_tribu" "appea_chamb" ])))
