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

(defn gen-stat-rules [ source script switch out-suffix ]
  (let [
         source-re
           (re-pattern (str "^" source))
         inp-files
           (filter-files source-re CASE-TAG-ALL-COURT-JSON-FILES)
         out-file
           (str "out/stat/" source out-suffix)
        ]
    [ [ (file CLJ-CMD) switch (inp script) (inp inp-files)
          ">" (out out-file) ]]))

(defrule
  (concat
     [[ (file CLJ-CMD)
        (inp "./gene_case_tags.clj")
        (inp CASE-DICT-JSON-FILE)
        (inp  ALL-COURT-JSON-FILES)
        (out  CASE-TAG-ALL-COURT-JSON-FILES) ]]
     (gen-stat-rules "commo_court" "./gene_case_stat_ids.clj" "" "_stat_ids.txt" )))
