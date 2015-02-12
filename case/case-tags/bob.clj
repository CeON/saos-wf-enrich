(bob-module)

(load-clj-file "../../bob_common.clj")

(defn make-stat-rule [[ script switch inp-files out-file]]
  [ (file CLJ-CMD) switch (inp script) (inp inp-files) ">" (out out-file)])

;(defn gen-stat-rules* []
;  (map
;    make-stat-rule
;    [ [ "./gene-case-stat-sum.clj" ""
;        CASE-TAG-ALL-COURT-JSON-FILES "out/stat/stat-sum-all-court.txt" ]
;      [ "./gene-case-stat-sum.clj" ""
;        CASE-TAG-SUPRE-COURT-JSON-FILES "out/stat/stat-sum-supre-court.txt" ]
;      [ "./gene-case-stat-sum.clj" ""
;        CASE-TAG-COMMO-COURT-JSON-FILES "out/stat/stat-sum-commo-court.txt" ]
;      [ "./gene-case-stat-frq.clj" ""
;        CASE-TAG-ALL-COURT-JSON-FILES "out/stat/stat-frq-all-court.txt" ]
;      [ "./gene-case-stat-frq.clj" ""
;        CASE-TAG-SUPRE-COURT-JSON-FILES "out/stat/stat-frq-supre-court.txt" ]
;      [ "./gene-case-stat-frq.clj" ""
;        CASE-TAG-COMMO-COURT-JSON-FILES "out/stat/stat-frq-commo-court.txt" ]
;      [ "./gene-case-stat-frq.clj" "-r"
;        CASE-TAG-ALL-COURT-JSON-FILES "out/stat/stat-frr-all-court.txt" ]
;      [ "./gene-case-stat-frq.clj" "-r"
;        CASE-TAG-SUPRE-COURT-JSON-FILES "out/stat/stat-frr-supre-court.txt" ]
;      [ "./gene-case-stat-frq.clj" "-r"
;        CASE-TAG-COMMO-COURT-JSON-FILES "out/stat/stat-frr-commo-court.txt" ]
;      [ "./gene-case-stat-ids.clj" ""
;        CASE-TAG-ALL-COURT-JSON-FILES "out/stat/stat-ids-all-court.txt" ]
;      [ "./gene-case-stat-ids.clj" ""
;        CASE-TAG-SUPRE-COURT-JSON-FILES "out/stat/stat-ids-supre-court.txt" ]
;      [ "./gene-case-stat-ids.clj" ""
;        CASE-TAG-COMMO-COURT-JSON-FILES "out/stat/stat-ids-commo-court.txt" ] ]))

(defn gen-stat-rules [ court-type script switch out-suffix ]
  (let [
         court-type-re
           (if (= court-type "every_court")
             #".*"
             (re-pattern (str "^" court-type)))
         inp-files
           (filter-files court-type-re CASE-TAG-ALL-COURT-JSON-FILES)
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
        (inp  ALL-COURT-JSON-FILES)
        (out  CASE-TAG-ALL-COURT-JSON-FILES) ]]
        (mapcat
          #(concat
             (gen-stat-rules % "./gene_case_stat_sum.clj" "" "_sum.txt")
             (gen-stat-rules % "./gene_case_stat_frq.clj" "" "_frq.txt")
             (gen-stat-rules % "./gene_case_stat_frq.clj" "-r" "_frq_resolved.txt")
             (gen-stat-rules %"./gene_case_stat_ids.clj" "" "_ids.txt"))
             [ "every_court" "commo_court" "supre_court" "const_tribu" "appea_chamb" ])))
