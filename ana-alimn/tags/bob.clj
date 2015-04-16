(bob-module)

(load-clj-file "../../bob_common.clj")

(defrule
  [ [ (file CLJ-CMD)
      (inp "./gene_ana_alimn_tags.clj")
      (inp  "../data/out/alimony.json.gz")
       "| gzip -c >" (out "out/commo_court_ana_alimn_tags.json.gz") ]
     [ (file CLJ-CMD)
       (inp "./gene_ana_alimn_html.clj")
       (inp  "out/commo_court_ana_alimn_tags.json.gz")
       ">" (out "out/commo_court_ana_alimn.html") ] ])
