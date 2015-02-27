(bob-module)

(load-clj-file "../../bob_common.clj")

(def N 20)

(defn gen-stat-rules []
  [])

(defn gen-parties-tags-rule [judgment-files tag-files]
  [ (file CLJ-CMD)
    (inp "./gene_parties_tags.clj")
    (inp judgment-files)
    (out tag-files) ])

(defrule
  (concat
     (gen-stat-rules)
     (gen-partitioned-rules
        N gen-parties-tags-rule
        COMMO-COURT-FILES
        CAS-PTIES-TAG-FILES)))
