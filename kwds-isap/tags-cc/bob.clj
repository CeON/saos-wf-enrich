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
    (inp "./gene_kwds_isap_tags.clj")
    (inp LAW-JOURN-DICT-FILE)
    (inp judgment-files)
    (out tag-files) ])

(defn gen-ref-cases-html-rule [ tag-json-files html-files]
  [ (file CLJ-CMD)
    (inp GEN-KWDS-ISAP-HTML-CLJ)
    (inp tag-json-files)
    (out html-files) ])

(defrule
  (concat
    (gen-partitioned-rules
       N gen-ref-cases-tags-rule
       COMMO-COURT-FILES KWDS-ISAP-CC-TAG-FILES)
    (gen-partitioned-rules
       N gen-ref-cases-html-rule
       KWDS-ISAP-CC-TAG-FILES KWDS-ISAP-CC-HTML-FILES)
    [ [ (file CLQ-CMD) "-p1"
          (inp "./q_kwds_isap_stat_sum.clj")
          (inp KWDS-ISAP-CC-TAG-FILES)
          ">" (out "out/stat/kwds_isap_sum.txt") ]
      [ (file CLQ-CMD) "-p1"
          (inp "./q_kwds_isap_stat_frq.clj")
          (inp KWDS-ISAP-CC-TAG-FILES)
          ">" (out "out/stat/kwds_isap_frq.txt") ] ]))
