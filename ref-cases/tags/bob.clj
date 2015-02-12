(bob-module)

(load-clj-file "../../bob_common.clj")

(defn gen-stat-rules [ court-type script switch out-suffix ]
  (let [
         court-type-re
           (if (= court-type "every_court")
             #".*"
             (re-pattern (str "^" court-type)))
         inp-files
           (filter-files court-type-re CASE-TAG-EVERY-COURT-JSON-FILES)
         out-file
           (str "out/stat/" court-type out-suffix)
        ]
    [ [ (file CLJ-CMD) (inp script) switch (inp inp-files)
          ">" (out out-file) ]]))

(defrule
  (concat
     [[ (file CLJ-CMD)
        (inp "./gene_case_tags.clj")
        (inp CASE-DICT-JSON-FILE)
        (inp EVERY-COURT-JSON-FILES)
        (out CASE-TAG-EVERY-COURT-JSON-FILES) ]]
        (mapcat
          #(concat
             (gen-stat-rules % "./gene_case_stat_sum.clj" "" "_sum.txt")
             (gen-stat-rules % "./gene_case_stat_frq.clj" "" "_frq.txt")
             (gen-stat-rules % "./gene_case_stat_frq.clj" "-r" "_frq_resolved.txt")
             (gen-stat-rules %"./gene_case_stat_ids.clj" "" "_ids.txt"))
             [ "every_court" "commo_court" "supre_court" "const_tribu" "appea_chamb" ])))
