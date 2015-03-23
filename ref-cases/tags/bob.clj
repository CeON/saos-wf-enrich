(bob-module)

(load-clj-file "../../bob_common.clj")

(def N 20)

(defn gen-stat-rules [court-type q-script out-suffix]
  (let [
         court-type-re
           (if (= court-type "every_court")
             #".*"
             (re-pattern (str "^" court-type)))
         inp-files
           (filter-files court-type-re REF-CASES-TAG-FILES)
         out-file
           (str "out/stat/" court-type out-suffix)
        ]
    [ [ (file CLQ-CMD) "-p1" (inp q-script) (inp inp-files)
          ">" (out out-file) ]]))

(defn gen-ref-cases-tags-rule [judgment-files tag-files]
  [ (file CLJ-CMD)
    (inp "./gene_ref_cases_tags.clj")
    (inp REF-CASES-DICT-FILE)
    (inp judgment-files)
    (out tag-files) ])

(defrule
  (concat
    (gen-partitioned-rules N gen-ref-cases-tags-rule EVERY-COURT-FILES REF-CASES-TAG-FILES)
    (mapcat
      #(concat
         (gen-stat-rules % "./q_ref_cases_stat_sum.clj" "_sum.txt")
         (gen-stat-rules % "./q_ref_cases_stat_frq.clj" "_frq.txt")
         (gen-stat-rules % "./q_ref_cases_stat_frq_unresolved.clj" "_frq_unresolved.txt")
         (gen-stat-rules % "./q_ref_cases_stat_ids.clj""_ids.txt"))
         [ "every_court" "commo_court" "supre_court" "const_tribu" "appea_chamb" ])))
