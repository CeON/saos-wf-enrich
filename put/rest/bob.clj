(bob-module)

(load-clj-file "../../bob_common.clj")

(defrule
  [ (file CLJ-CMD)
    (inp "./put_json.clj")
    (inp "./../../config")
    (inp REF-CASES-TAG-FILES)
    (inp CAS-PTIES-TAG-FILES)
    (inp REF-MONEY-TAG-FILES)
    (inp REF-REGUS-TAG-FILES)
    ">" (out "out/put_json.log") ])
