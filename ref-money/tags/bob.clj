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
           (filter-files court-type-re REF-MONEY-TAG-FILES)
         out-file
           (str "out/stat/" court-type out-suffix)
        ]
    [ [ (file CLQ-CMD) "-p1" (inp q-script) (inp inp-files)
          ">" (out out-file) ]]))

(defn gen-ref-cases-tags-rule [judgment-files tag-files]
  [ (file CLJ-CMD)
    (inp "./gene_ref_money_tags.clj")
    (inp judgment-files)
    (out tag-files) ])

(defrule
  (concat
    (gen-partitioned-rules N gen-ref-cases-tags-rule
       EVERY-COURT-FILES REF-MONEY-TAG-FILES)
    (mapcat
      #(gen-stat-rules % "./q_top_ref_money_tags.clj" "_top_ref_money.json")
      [ "every_court" "commo_court" "supre_court" "const_tribu" "appea_chamb" ])))
