(use 'clj.fetch :reload)
(require '[cheshire.core :as cc])

(defn handle-common-court-divisions [ common-courts-fname ]
  (let [
         common-courts
           (first
             (fetch-items-all
               (str "https://saos-test.icm.edu.pl/api/dump/"
                    "courts?pageSize=100&pageNumber=0")))
         ;_
         ; (spit
         ;    common-courts-fname
         ;   (cc/generate-string common-courts { :pretty true }))
        ]
    (gen-division-id->cc-division-map common-courts)))

(defn handle-supreme-court-divisions [ sc-chambers-fname ]
  (let [
         supreme-court-chambers
           (first
             (fetch-items-all
               (str "https://saos-test.icm.edu.pl/api/dump/"
                "scChambers?pageSize=100&pageNumber=0")))
         ; _  (spit
         ;     sc-chambers-fname
         ;     (cc/generate-string supreme-court-chambers
         ;     { :pretty true }))

        ]
    [ (gen-division-id->sc-division-map supreme-court-chambers)
      (gen-chamber-id->sc-chamber-map supreme-court-chambers) ]
    ))

(defn main []
  (let [
         division-id->cc-division
           (handle-common-court-divisions "common_courts.json")
         [division-id->sc-division chamber-id->sc-chamber]
           (handle-supreme-court-divisions
             "supreme_court_chambers.json")
         _ (fetch-buffer-all
             "https://saos-test.icm.edu.pl/api/dump/judgments?pageSize=100&pageNumber=0"
             division-id->cc-division
             division-id->sc-division
             chamber-id->sc-chamber)
       ]
    nil))

(main)
