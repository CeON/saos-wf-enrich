(bob-module)

(load-clj-file "../../bob_common.clj")

(defrule
  [ (file CLJ-CMD)
    (inp "./put_json.clj")
    (inp "./auto")
    (inp CASE-TAG-ALL-COURT-JSON-FILES)
    ">" (out "out/put_json.log") ])
