(bob-module)

(load-clj-file "../../bob_common.clj")

(defn make-stat-rule [[ script switch inp-files out-file]]
  [ CLJ-CMD switch (inp script) (inp inp-files) ">" (out out-file)])

(defn gen-stat-rules []
  [])
(defn sequentialize [x]
  (if-not (sequential? x)
    [x]
    x))

(defn gen-parties-rule [ inp-fname out-fname ]
    [ CLJ-CMD
      (inp "./gene_parties_tags.clj")
      (apply inp (sequentialize inp-fname))
      (apply out (sequentialize out-fname)) ])

(defn gen-parties-rule-multiple []
  (map
    gen-parties-rule
    COMMO-COURT-JSON-FILES
    PARTIES-TAG-COMMO-COURT-JSON-FILES))

(defn gen-parties-rule-grouped [n]
  (map
    gen-parties-rule
    (partition-all n COMMO-COURT-JSON-FILES)
    (partition-all n PARTIES-TAG-COMMO-COURT-JSON-FILES)))

(defn gen-parties-rule-single []
  [[ CLJ-CMD
     (inp "./gene_parties_tags.clj")
     (inp COMMO-COURT-JSON-FILES)
     (out PARTIES-TAG-COMMO-COURT-JSON-FILES)]])

(defrule
  (concat
     (gen-stat-rules)
     (gen-parties-rule-grouped 10)))
