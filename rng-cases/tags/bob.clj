(bob-module)

(load-clj-file "../../bob_common.clj")

(def N 20)

(defn gen-ref-cases-tags-rule [judgment-files tag-files]
  [ (file CLJ-CMD)
    (inp "./gene_rng_cases_tags.clj")
    (inp RNG-CASES-DICT-FILE)
    (inp judgment-files)
    (out tag-files) ])

(defn gen-stat-rules [court-type q-script out-suffix]
  (let [
         court-type-re
           (if (= court-type "every_court")
             #".*"
             (re-pattern (str "^" court-type)))
         inp-files
           (filter-files court-type-re RNG-CASES-TAG-FILES)
         out-file
           (str "out/stat/" court-type out-suffix)
        ]
    [ [ (file CLQ-CMD) "-p1" (inp q-script) (inp inp-files)
          ">" (out out-file) ]]))

(defrule
  (concat
    (gen-partitioned-rules
      N gen-ref-cases-tags-rule EVERY-COURT-FILES RNG-CASES-TAG-FILES)
    (mapcat
      #(concat
         (gen-stat-rules  % "./q_rng_cases_stat_frq.clj" "_frq.txt")
         (gen-stat-rules  % "./q_top_frq.clj" "_top_frq.json"))
      [ "every_court" "commo_court" "supre_court" "const_tribu"
        "appea_chamb" ])))
