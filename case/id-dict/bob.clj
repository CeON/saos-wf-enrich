(bob-module)

(load-clj-file "../../bob-common.clj")

(defrule
  [ [ CLJ-CMD
      (inp "gene-id-dict.clj")
      (out "out/id-dict.riffle")
      (inp ALL-COURT-JSON-FILES)] ])
