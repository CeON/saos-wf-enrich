(bob-module)

(load-clj-file "../../bob_common.clj")

(defrule*
  [ [ (out* (filter-files #"^appea_chamb" EVERY-COURT-FILES))
        (tag "appea-chamb-json") ]
    [ (out* (filter-files #"^commo_court" EVERY-COURT-FILES))
        (tag "commo-court-json") ]
    [ (out* (filter-files #"^const_tribu" EVERY-COURT-FILES))
        (tag "const-tribu-json") ]
    [ (out* (filter-files #"^supre_court" EVERY-COURT-FILES))
        (tag "supre-court-json") ] ])
