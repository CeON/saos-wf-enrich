(bob-module)

(load-clj-file "../../bob_common.clj")

(def N 20)

(defn gen-ref-cases-tags-rule [judgment-files tag-files]
  [ (file CLJ-CMD)
    (inp "./gene_ref_cases_tags.clj")
    (inp REF-CASES-DICT-FILE)
    (inp judgment-files)
    (out tag-files) ])

(defn gen-stat-rules* [ court-type inp-q-script out-suffix ]
  (gen-stat-rules court-type inp-q-script
    REF-CASES-TAG-FILES "out/stat" out-suffix))

(defrule
  (concat
    (gen-partitioned-rules
      N gen-ref-cases-tags-rule EVERY-COURT-FILES REF-CASES-TAG-FILES)
    (mapcat
      #(concat
         (gen-stat-rules* % "./q_ref_cases_stat_sum.clj" "_sum.txt")
         (gen-stat-rules* % "./q_ref_cases_stat_frq.clj" "_frq.txt")
         (gen-stat-rules* % "./q_ref_cases_stat_frq_unresolved.clj"
            "_frq_unresolved.txt")
         (gen-stat-rules* % "./q_ref_cases_stat_ids.clj" "_ids.txt"))
         [ "every_court" "commo_court" "supre_court"
           "const_tribu" "appea_chamb" ])))
