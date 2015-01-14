(bob-module)

(load-clj-file "../../bob-common.clj")

(defrule*
  [ [ (out* COMMO-COURT-JSON-FILES) (tag "commo-court-json")]
    [ (out* SUPRE-COURT-JSON-FILES) (tag "supre-court-json")]])
