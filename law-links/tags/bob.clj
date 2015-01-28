(bob-module)

(load-clj-file "../../bob_common.clj")

(defn gen-stat-rules []
  [])

(defn sequentialize [x]
  (if-not (sequential? x)
    [x]
    x))

(defn gen-law-links-rule [ inp-fname out-fname ]
    [ (file CLJ-CMD)
      (inp "./gene_law_links_tags.clj")
      (apply inp (sequentialize inp-fname))
      (apply out (sequentialize out-fname)) ])

(defn gen-law-links-rule-grouped [n]
  (map
    gen-law-links-rule
    (partition-all n ALL-COURT-JSON-FILES)
    (partition-all n LAW-LINKS-TAG-ALL-COURT-JSON-FILES)))

(defrule
  (concat
    (gen-law-links-rule-grouped 10)
    (gen-stat-rules)))
