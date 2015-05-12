(bob-module)

(load-clj-file "../../bob_common.clj")

(def N 20)

(defn gen-ref-cases-tags-rule [judgment-files tag-files]
  [ (file CLJ-CMD)
    (inp "./gene_ref_money_tags.clj")
    (inp judgment-files)
    (out tag-files) ])

(defn gen-stat-rules* [ court-type ]
  (gen-stat-rules court-type "./q_top_ref_money_tags.clj"
    REF-MONEY-TAG-FILES "out/stat" "_top_ref_money.json"))

(defrule
  (concat
    (gen-partitioned-rules N gen-ref-cases-tags-rule
       EVERY-COURT-FILES REF-MONEY-TAG-FILES)
    (mapcat gen-stat-rules*
      [ "every_court" "commo_court" "supre_court"
        "const_tribu" "appea_chamb" ])))
