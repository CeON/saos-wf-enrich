(bob-module)

(load-clj-file "../../bob_common.clj")

(defrule
  [ [ (file CLQ-CMD)
      (inp "./q_alimony.clj")
      (inp  COMMO-COURT-FILES)
       "| gzip -c >" (out "out/alimony.json.gz") ] ])
